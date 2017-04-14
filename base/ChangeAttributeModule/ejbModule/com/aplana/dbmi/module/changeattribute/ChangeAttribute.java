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
package com.aplana.dbmi.module.changeattribute;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.action.*;
import com.aplana.dbmi.action.xml.SearchXmlHelper;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.InfoMessage;
import com.aplana.dbmi.model.LogEntry;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.util.DateUtils;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.SystemUser;
import org.apache.log4j.Logger;
import org.springframework.context.access.ContextSingletonBeanFactoryLocator;
import org.springframework.ejb.support.AbstractStatelessSessionBean;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.ejb.CreateException;
import javax.ejb.SessionBean;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class ChangeAttribute extends AbstractStatelessSessionBean implements SessionBean {
	private static final String EVENT_ACTION_NAME = "CHG_ATTR";
	private static final String ATTR_ATTRIBUTE_NAME = "attrname";
	private static final String CONFIG_FILE = "dbmi/ChangeAttributeConfig.xml";
	private static final Logger logger = Logger.getLogger(ChangeAttribute.class);

	private static final String PARAM_CURRENT_DATE = "CURRENT_DATE";

	private DataServiceBean serviceBean;
	// --Commented out by Inspection (28.05.2015 15:46):private int lockDuration;

	private List<Query> queries = new ArrayList<Query>();

	public ChangeAttribute() {
		setBeanFactoryLocator(ContextSingletonBeanFactoryLocator.getInstance());
		setBeanFactoryLocatorKey("businessBeanFactory");
	}

	protected void onEjbCreate() throws CreateException {
		serviceBean = createServiceBean();
		readConfig();
	}

	public void process(Map parameters) {
		long startTime = System.currentTimeMillis();
		logger.debug("Start task 'ChangeAttribute'");
		try {
			logger.info("There is " + queries.size() + " queries");
			for (Query curQuery : queries) {
				GetCardListBySql getCardsListBySql = new GetCardListBySql(curQuery.getSelectSQL());
				List<Long> cardList = serviceBean.doAction(getCardsListBySql);
				logger.info("There is " + cardList.size() + " cards in query for attr '" + curQuery.getAttrName());
				for (Long cardId : cardList) {
					UpdateCard updateCard = new UpdateCard(curQuery.getUpdateSQL(), cardId);
					ObjectId objId = new ObjectId(Card.class, cardId);
					// (YNikitin, 2013/07/05, ��� 28951) ���� ������ ��� ���������� ����� �� ��������, �� ���� ���������, ��������� � ������
					boolean locked = false;
					try {
						serviceBean.doAction(new LockObject(objId));
						locked = true;
						Long attributesCount = (Long) serviceBean.doAction(updateCard);
						if (attributesCount > 0) {
							logger.info("Update " + attributesCount + " rows for attribute '" + curQuery.getAttrName() + " in card " + cardId);
							addInfoMessageInEventLog(cardId, attributesCount, curQuery.getAttrName(), curQuery.getUpdateSQL());
						}
					} catch (Exception ex) {
						logger.error("Error while update attributes in card " + cardId, ex);
					} finally {
						if (locked)
							serviceBean.doAction(new UnlockObject(objId));
					}
				}
				//cardSearch.setSqlXmlName(sqlXmlName)
			}
		} catch (Exception ex) {
			logger.error("Error on task 'ChangeAttribute'", ex);
		} finally {
			logger.debug("Finish processing task 'ChangeAttribute': workingtime = " + (System.currentTimeMillis() - startTime));
		}
	}

	private DataServiceBean createServiceBean() {
		DataServiceBean serviceBean = new DataServiceBean();
		serviceBean.setUser(new SystemUser());
		serviceBean.setAddress("localhost");
		return serviceBean;
	}

	private void readConfig() {
		try {
			InputStream input = Portal.getFactory().getConfigService().loadConfigFile(CONFIG_FILE);

			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input);
			Element root = doc.getDocumentElement();
			if (!"config".equals(root.getNodeName()))
				throw new Exception("config element expected");
			Collection queriesNodes = SearchXmlHelper.getNodeList(root, "./query", null, null);
			if (queriesNodes.size() == 0)
				queries = new ArrayList<Query>(0);
			else {
				queries = new ArrayList<Query>(queriesNodes.size());
				for (Object queriesNode : queriesNodes) {
					Element tag = (Element) queriesNode;
					String selectSQL = SearchXmlHelper.getTagContent(tag, "./selectSQL", null, null);
					String updateSQL = SearchXmlHelper.getTagContent(tag, "./updateSQL", null, null);
					Collection paramNodes = SearchXmlHelper.getNodeList(tag, "./updateSQL-params", null, null);
					if (paramNodes != null && !paramNodes.isEmpty()) {
						updateSQL = updateSQL.replaceAll("'", "''");
						updateSQL = MessageFormat.format(updateSQL, getParameters(paramNodes));
					}
					String attrName = tag.getAttribute(ATTR_ATTRIBUTE_NAME);
					Query sql = new Query(attrName, selectSQL, updateSQL);
					queries.add(sql);
				}
			}
		} catch (Exception ex) {
			queries = new ArrayList<Query>();
			logger.warn("Error read ChangeAttribute config. Clear settings.", ex);
		}
	}

	private Object[] getParameters(Collection paramNodes) {
		final List<Object> params = new ArrayList<Object>(paramNodes.size());
		for (Object paramNode : paramNodes) {
			Element tag = (Element) paramNode;
			String param = SearchXmlHelper.getTagContent(tag, "./parameter", null, null);
			if (PARAM_CURRENT_DATE.equals(param)) {
				param = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(DateUtils.toUTC(new Date()));
			}
			params.add(param);
		}
		return params.toArray();
	}

	private void addInfoMessageInEventLog(Long cardId, Long attributesCount, String attrName, String sql) {
		LogEntry entry = new LogEntry();
		entry.setEvent(EVENT_ACTION_NAME);
		entry.setObject(new ObjectId(Card.class, cardId));
		entry.setUser(serviceBean.getPerson());
		entry.setAddress("localhost");
		entry.setTimestamp(new Date());
		InfoMessage msg = new InfoMessage(entry);
		msg.setMessage("There are " + attributesCount.intValue() + " values add for attribute '" + attrName + "'");
		msg.setDescriptionMessage(sql.trim());
		msg.isSucces(1l);    // �������� ������ � ��� - ��� 1, � �� 0
		try {
			WriteEventLog writeEventLog = new WriteEventLog();
			writeEventLog.setEntry(msg);
			serviceBean.doAction(writeEventLog);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * ����� ������, ������� ��
	 * 1) select-������� ��� ������ ������ ��������
	 * 2) update-������� ��� ���������� ��������� ������ �������� �� ��������� select-��������
	 *
	 * @author ynikitin
	 */
	private class Query {
		private String selectSQL;
		private String updateSQL;
		private String attrName;

		public Query(String attrName, String selectSQL, String updateSQL) {
			this.selectSQL = selectSQL;
			this.updateSQL = updateSQL;
			this.attrName = attrName;
		}

		public String getAttrName() {
			return attrName;
		}

		public String getSelectSQL() {
			return selectSQL;
		}

		public String getUpdateSQL() {
			return updateSQL;
		}
	}
}
