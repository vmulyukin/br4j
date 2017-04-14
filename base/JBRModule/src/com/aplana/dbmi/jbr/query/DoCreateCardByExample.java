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
package com.aplana.dbmi.jbr.query;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.action.CloneCard;
import com.aplana.dbmi.action.CreateCard;
import com.aplana.dbmi.action.CreateCardByExample;
import com.aplana.dbmi.action.file.CopyMaterial;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.HtmlAttribute;
import com.aplana.dbmi.model.IntegerAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.TextAttribute;
import com.aplana.dbmi.model.TreeAttribute;
import com.aplana.dbmi.model.util.AttrUtils;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.Database;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.QueryFactory;
import com.aplana.dbmi.service.impl.SaveQueryBase;
import com.aplana.dbmi.service.impl.UserData;
import com.aplana.dbmi.service.impl.query.WriteQuery;

/**
 * Creation of new card on base of given card example
 * @author DSultanbekov
 */
@SuppressWarnings("unchecked")
public class DoCreateCardByExample extends ActionQueryBase implements WriteQuery {

	static final int COPY_LINK = 0;
	static final int COPY_CARD = 1;
	static final String CONFIG_FOLDER = "dbmi/cardExamples/";
	
	static class ExampleMapping {
		ObjectId destTemplateId;
		List attributeMapping;
	}
	
	static class AttributeMapping {
		ObjectId srcId;
		ObjectId destId;
		int mode = COPY_LINK;
	}
	
	private ExampleMapping readMapping(ObjectId templateId) throws DataException {
		final ExampleMapping result = new ExampleMapping();
		final List attributes = new ArrayList();
		result.attributeMapping = attributes;
		final String configFile = CONFIG_FOLDER + templateId.getId().toString() + ".xml";		
		try {
			final InputStream stream = Portal.getFactory().getConfigService().getConfigFileUrl(configFile).openStream();
			final Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream);
			final XPath xpath = XPathFactory.newInstance().newXPath();
			final Element root = doc.getDocumentElement();
			result.destTemplateId = ObjectIdUtils.getObjectId(Template.class, root.getAttribute("destTemplateId"), true);
			final NodeList attrNodes = (NodeList)xpath.evaluate("attribute", root, XPathConstants.NODESET);
			for (int i = 0; i < attrNodes.getLength(); ++i) {
				final Element elem = (Element)attrNodes.item(i);
				final AttributeMapping m = new AttributeMapping();
				final Class attrClass = AttrUtils.getAttrClass(elem.getAttribute("type"));
				m.srcId = ObjectIdUtils.getObjectId(attrClass, elem.getAttribute("srcId"), false);
				m.destId = ObjectIdUtils.getObjectId(attrClass, elem.getAttribute("destId"), false);
				
				final String mode = elem.getAttribute("mode");
				if (CardLinkAttribute.class.equals(attrClass)) {
					if ("copyCard".equals(mode)) {
						m.mode = COPY_CARD;
					} else if ("copyLink".equals(mode)) {
						m.mode = COPY_LINK;
					} else {
						throw new IllegalArgumentException("Wrong mapping mode: '" + mode + "'");
					}
				} else if (!"".equals(mode)) {
					logger.warn("Mode attribute for non-cardLink attribute found. Ignored");
				}
				attributes.add(m);
			}
		} catch (Exception e) {
			throw new DataException("createCardByExample.fileError", new Object[] {configFile, e.getMessage()});
		}
		return result;
	}
	
	@Override
	public Object processQuery() throws DataException 
	{
		final CreateCardByExample action = (CreateCardByExample)getAction();
		final ObjectId exampleId = action.getExampleId();
		logger.debug("Processing request for creation of card by example: exampleId = " + exampleId.getId());

		final QueryFactory f = getQueryFactory();
		final Database d = getDatabase();
		final UserData user = getUser();

		final ObjectQueryBase q = f.getFetchQuery(Card.class);
		q.setId(exampleId);

		final Card exampleCard = (Card)d.executeQuery(user, q);
		logger.debug("Example card fetched successfully: templateId = " + exampleCard.getTemplate().getId());

		final ExampleMapping mapping = readMapping(exampleCard.getTemplate());
		logger.debug("Mapping file read. Destination templateId = " + mapping.destTemplateId.getId());
		ActionQueryBase aq = f.getActionQuery(CreateCard.class);
		CreateCard createAction = new CreateCard();
		createAction.setTemplate(mapping.destTemplateId);
		aq.setAction(createAction);
		Card card = (Card)d.executeQuery(user, aq);
		logger.debug("New card created successfully");
		
		Iterator i = mapping.attributeMapping.iterator();
		while (i.hasNext()) {
			AttributeMapping m = (AttributeMapping)i.next();
			logger.trace("Copying value of attribute '" + m.srcId.getId() + "' to attrinbute '" + m.destId.getId() + "'");
			Class attrClass = m.destId.getType();
			Attribute srcAttr = exampleCard.getAttributeById(m.srcId),
				destAttr = card.getAttributeById(m.destId);
			if (srcAttr == null) {
				logger.warn("Attribute '" + m.srcId.getId() + "' not found in example card. Ignoring");
				continue;
			}
			if (destAttr == null) {
				logger.warn("Attribute '" + m.destId.getId() + "' not found in newly created card. Ignoring");
				continue;
			}			
			if (StringAttribute.class.equals(attrClass) || TextAttribute.class.equals(attrClass) || HtmlAttribute.class.equals(attrClass)) {
				((StringAttribute)destAttr).setValue(((StringAttribute)srcAttr).getValue());
			} else if (IntegerAttribute.class.equals(attrClass)) {
				((IntegerAttribute)destAttr).setValue(((IntegerAttribute)srcAttr).getValue());
			} else if (DateAttribute.class.equals(attrClass)) {
				((DateAttribute)destAttr).setValue(((DateAttribute)srcAttr).getValue());
			} else if (ListAttribute.class.equals(attrClass)) {
				((ListAttribute)destAttr).setValue(((ListAttribute)srcAttr).getValue());
			} else if (TreeAttribute.class.equals(attrClass)) {
				((TreeAttribute)destAttr).setValues(((TreeAttribute)srcAttr).getValues());
			} else if (PersonAttribute.class.equals(attrClass)) {
				((PersonAttribute)destAttr).setValues(((PersonAttribute)srcAttr).getValues());
			} else if (CardLinkAttribute.class.equals(attrClass)){
				// final Collection values = ((CardLinkAttribute)srcAttr).getValues();
				final Collection<ObjectId> ids = ((CardLinkAttribute)srcAttr).getIdsLinked();
				if (m.mode == COPY_CARD) {
					final List clonedCards = new ArrayList(ids.size());
					/*for (Iterator iterator = clonedCards.iterator(); iterator.hasNext();) {
						Object object = (Object) iterator.next();
					}*/
					for( Iterator<ObjectId> j = ids.iterator(); j.hasNext(); ) {
						/* (2010/02, RuSA) 
						 Card c = (Card)j.next();
						 ObjectId origId = c.getId();
						 */
						final ObjectId origId = j.next();
						// �������� � ��������� ���� ��������
						final CloneCard cloneAction = new CloneCard();
						cloneAction.setOrigId(origId);
						cloneAction.getEnabledAttrIds().add(Attribute.ID_MATERIAL);
						aq = f.getActionQuery(cloneAction);
						aq.setAction(cloneAction);
						final Card loaded = (Card)d.executeQuery(user, aq);
						SaveQueryBase sq = getQueryFactory().getSaveQuery(loaded);
						sq.setObject(loaded);
						final ObjectId clonedCardId = (ObjectId)d.executeQuery(user, sq);
						clonedCards.add(DataObject.createFromId(clonedCardId));

						// �������� ���� ���������, ���� �� ����
						CopyMaterial copyMaterial = new CopyMaterial();
						copyMaterial.setFromCardId(origId);
						copyMaterial.setToCardId(clonedCardId);
						aq = f.getActionQuery(copyMaterial);
						aq.setAction(copyMaterial);
						d.executeQuery(getSystemUser(), aq);
					}
					// DONE: (done 2011/09/05, RuSA) (2010/02, RuSA) ����� ��� ??
					 //values = clonedCards; 
					 ((CardLinkAttribute)destAttr).setIdsLinked(clonedCards);
				} else if (m.mode == COPY_LINK) {
					((CardLinkAttribute)destAttr).setIdsLinked(ids);
				} else /*if (m.mode != COPY_LINK)*/ {
					throw new IllegalArgumentException("Wrong mapping mode: " + m.mode);
				}
			}
		}
		logger.debug("Successfully copied values of " + mapping.attributeMapping.size() + " attributes. Saving newly created card.");
		final SaveQueryBase sq = f.getSaveQuery(card);
		sq.setObject(card);

		final ObjectId result = (ObjectId)d.executeQuery(user, sq);
		logger.info("Card created: exampleId = " + exampleId.getId() + ", newly created cadrId = " + result.getId());
		return result;
	}
	
}
