/**
 *   Licensed to the Apache Software Foundation (ASF) under one or more
 *   contributor license agreements.  See the NOTICE file distributed with
 *   this work for additional information regarding copyright ownership.
 *   The ASF licenses this file to you under the Apache License, Version 2.0
 *   (the "License"); you may not use this file except in compliance with
 *   the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.aplana.dbmi.jbr.processors;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.DatabaseClient;
import com.aplana.dbmi.service.impl.Parametrized;
import com.aplana.dbmi.service.impl.ProcessorBase;

/**
 * @comment AbdullinR
 * ��������: ����������� ��� ���������� �������� � ����� ��������� ��� 
 * ���������� ���� ��������� ���� C (cardlink) ����������� �� ������� �������� 
 * ���, ����� ��� ����� ��������� �� ��������, ��������� � ������.
 * (!) ������ ��������� �������� ������ ��������� ����� ���� ��������. ����� 
 * ����������� ����������.
 */
public class CardDoubletPostProcessor extends ProcessorBase implements DatabaseClient, Parametrized {

	protected final Log logger = LogFactory.getLog(getClass());

	private static final String CONFIG_FILE_PARAM = "config";
	private static final String ATTRIBUTE_CODE = "attributeCode";
	private static final String TARGET_CARD_STATE_ID = "targetCardStateId";
	private static final String TEMPLATE_ID = "templateId";

	private String configFile;

	final private Set<DoubletParamsConfig> configSets = new HashSet<DoubletParamsConfig>();

	private JdbcTemplate jdbcTemplate;

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public Object process() throws DataException {

		ChangeState changeState = (ChangeState) getAction();
		Card card = changeState.getCard();
		ObjectId sourceDoubletCardId = card.getId();

		final List<DoubletParamsConfig> possibleConfigs = 
			getPossibleConfigs(card.getTemplate(), changeState.getWorkflowMove().getToState());

		// flag for use only one config
		boolean configUsed = false;

		for (Iterator<?> iterator = possibleConfigs.iterator(); !configUsed && iterator.hasNext();) {

			DoubletParamsConfig config = (DoubletParamsConfig) iterator.next();
			Attribute attribute = card.getAttributeById(config.getAttributeObjectId());

			//If (templateId, workflowMoveId) defined as unique for doublet, then attibute must exists- otherwise ...  continue
			if (attribute == null) {
				throw new DataException("carddoubletpostprocessor.original.attribute.must.be.defined");
				//	continue;
			}
			configUsed = true;

			final CardLinkAttribute cardLinkAttribute = (CardLinkAttribute) attribute;

			/* (2010/02, RuSA) OLD:
			Collection<?> values = cardLinkAttribute.getValues();

			if (values == null) {
				logger.error("CardLinkAttribute for Doublet has no value");
				throw new DataException();
			}

			switch (values.size()) {
				case 0:
					throw new DataException("carddoubletpostprocessor.no.reference.to.original");
				case 1:
					Card linkedCard = (Card) values.iterator().next();
					doUpdate(doubletCardObjectId, linkedCard.getId());
					break;
				default:
					throw new DataException("carddoubletpostprocessor.too.much.references.to.original");
			}
			 */
			switch (cardLinkAttribute.getLinkedCount()) {
				case -1:
					logger.error("CardLinkAttribute for Doublet has no value");
					throw new DataException();
				case 0:
					throw new DataException("carddoubletpostprocessor.no.reference.to.original");
				case 1:
					final ObjectId destCardId = cardLinkAttribute.getSingleLinkedId();
					boolean postUnlock = false;
					ActionQueryBase query;
					if (sourceDoubletCardId != null) {
						LockObject lock = new LockObject(card);
						query = getQueryFactory().getActionQuery(lock);
						query.setAction(lock);
						getDatabase().executeQuery(getSystemUser(), query);
						postUnlock = true;
					}
					try {
						doUpdate(sourceDoubletCardId, destCardId);
					} finally {
						if (postUnlock) {
							UnlockObject unlock = new UnlockObject(card);
							query = getQueryFactory().getActionQuery(unlock);
							query.setAction(unlock);
							getDatabase().executeQuery(getSystemUser(), query);
						}
					}
					break;
				default:
					throw new DataException("carddoubletpostprocessor.too.much.references.to.original");
			}
			
		}
		return null;
	}

	private List<DoubletParamsConfig> getPossibleConfigs(ObjectId templateObjectId, ObjectId targetCardStateObjectId) {
		final List<DoubletParamsConfig> result = new ArrayList<DoubletParamsConfig>();
		for (Iterator<?> iterator = configSets.iterator(); iterator.hasNext();) {
			DoubletParamsConfig config = (DoubletParamsConfig) iterator.next();
			if (config.getTemplateObjectId().equals(templateObjectId) && config.getTargetCardStateObjectId().equals(targetCardStateObjectId)) {
				result.add(config);
			}
		}
		return result;
	}

	private void doUpdate(ObjectId oldDoubletCardObjectId, ObjectId newCardObjectId) {
		if (newCardObjectId != null && oldDoubletCardObjectId != null) {

			StringBuffer sql = new StringBuffer();
			sql.append("UPDATE attribute_value av ");
			sql.append("SET    number_value   =?  "); // -- original card id
			sql.append("WHERE  av.number_value=?  "); // -- doublet card id
			sql.append("   AND EXISTS ");
			sql.append("       (SELECT 1 ");
			sql.append("       FROM    attribute a ");
			sql.append("       WHERE   a.attribute_code=av.attribute_code ");
			sql.append("           AND a.data_type     = ? ");
			sql.append("       )");

			getJdbcTemplate().update(sql.toString(), new Object[] { newCardObjectId.getId(), oldDoubletCardObjectId.getId(), Attribute.TYPE_CARD_LINK });
		}
	}

	public void setParameter(String name, String value) {

		ObjectId o1 = new ObjectId(Template.class, 1);
		ObjectId o2 = new ObjectId(Template.class, new String("2"));

		System.out.println("new ObjectId(Template.class, 1) equals  new ObjectId(Template.class, new String (\"2\")): " + o1.equals(o2));

		if (CONFIG_FILE_PARAM.equals(name)) {
			configFile = value;
		}

		try {
			readXML();
		} catch (ParserConfigurationException e) {
			logger.error("Error occured by reading of conf file for postprocessor " + getClass(), e);
		} catch (SAXException e) {
			logger.error("Error occured by reading of conf file for postprocessor " + getClass(), e);
		} catch (IOException e) {
			logger.error("Error occured by reading of conf file for postprocessor " + getClass(), e);
		} catch (DataException e) {
			logger.error("Error occured by reading of conf file for postprocessor " + getClass(), e);
		}
		
		System.out.println(configSets);
	}

	private void readXML() throws ParserConfigurationException, SAXException, IOException, DataException {
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		InputStream loadConfigFile = Portal.getFactory().getConfigService().loadConfigFile(configFile);

		Document doc = docBuilder.parse(loadConfigFile);
		NodeList doubletsList = doc.getElementsByTagName("doublet");

		for (int s = 0; s < doubletsList.getLength(); s++) {
			Node doublet = doubletsList.item(s);
			if (doublet.getNodeType() == Node.ELEMENT_NODE) {
				final DoubletParamsConfig config = new DoubletParamsConfig();

				NamedNodeMap attributes = doublet.getAttributes();
				for (int q = 0; q < attributes.getLength(); q++) {
					Node item = attributes.item(q);

					String name = item.getNodeName();
					String value = item.getNodeValue();

					if (TEMPLATE_ID.equals(name)) {
						config.setTemplateObjectId(getObjectId(Template.class, value));
					} else if (TARGET_CARD_STATE_ID.equals(name)) {
						config.setTargetCardStateObjectId(getObjectId(CardState.class, value));
					} else if (ATTRIBUTE_CODE.equals(name)) {
						config.setAttributeObjectId(getObjectId(CardLinkAttribute.class, value));
					}
				}

				// add config only if valid
				if(configSets.contains(config)){
					throw new DataException("carddoubletpostprocessor.has.more.config.attributes");
				}
				
				if (config.isValid() && !configSets.contains(config)) {
					configSets.add(config);
				}
			}
		}
	}

	private ObjectId getObjectId(Class<?> cls, String value) {
		ObjectId result = ObjectId.predefined(cls, value);

		if (result == null) {
			if (CardLinkAttribute.class.equals(cls)) {
				result = new ObjectId(cls, value);
			} else if (Template.class.equals(cls) || CardState.class.equals(cls)) {
				try {
					result = new ObjectId(cls, Long.parseLong(value));
				} catch (NumberFormatException e) {
					logger.warn("Cannot parse long from String for template/workflow", e);
				}
			}
		}
		return result;
	}

	private class DoubletParamsConfig {
		private ObjectId templateObjectId;
		private ObjectId targetCardStateObjectId;
		private ObjectId attributeObjectId;

		@Override
		public String toString() {
			StringBuffer sb = new StringBuffer();
			sb.append("Config {").append(TEMPLATE_ID).append(":").append(templateObjectId).append(" ,");
			sb.append(TARGET_CARD_STATE_ID).append(":").append(targetCardStateObjectId).append(", ");
			sb.append(ATTRIBUTE_CODE).append(":").append(attributeObjectId).append("}");
			return sb.toString();
		}

		public boolean isValid() {
			return templateObjectId != null && targetCardStateObjectId != null && attributeObjectId != null;
		}

		public DoubletParamsConfig() {
		}

		public ObjectId getTemplateObjectId() {
			return templateObjectId;
		}

		public void setTemplateObjectId(ObjectId templateObjectId) {
			this.templateObjectId = templateObjectId;
		}

		public ObjectId getTargetCardStateObjectId() {
			return targetCardStateObjectId;
		}

		public void setTargetCardStateObjectId(ObjectId targetCardStateObjectId) {
			this.targetCardStateObjectId = targetCardStateObjectId;
		}

		public ObjectId getAttributeObjectId() {
			return attributeObjectId;
		}

		public void setAttributeObjectId(ObjectId attributeObjectId) {
			this.attributeObjectId = attributeObjectId;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((targetCardStateObjectId == null) ? 0 : targetCardStateObjectId.hashCode());
			result = prime * result + ((templateObjectId == null) ? 0 : templateObjectId.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			DoubletParamsConfig other = (DoubletParamsConfig) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (targetCardStateObjectId == null) {
				if (other.targetCardStateObjectId != null)
					return false;
			} else if (!targetCardStateObjectId.equals(other.targetCardStateObjectId))
				return false;
			if (templateObjectId == null) {
				if (other.templateObjectId != null)
					return false;
			} else if (!templateObjectId.equals(other.templateObjectId))
				return false;
			return true;
		}

		private CardDoubletPostProcessor getOuterType() {
			return CardDoubletPostProcessor.this;
		}

	}
}
