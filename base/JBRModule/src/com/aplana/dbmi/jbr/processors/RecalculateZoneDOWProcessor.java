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

import java.io.StringWriter;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.springframework.jdbc.core.support.SqlLobValue;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.aplana.dbmi.action.GetTask;
import com.aplana.dbmi.jbr.util.AttributeSelector;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.BasePropertySelector;
import com.aplana.dbmi.service.impl.ObjectQueryBase;

/**
 * @comment RAbdullin
 * Процессор, который вызывает заполнение Зон ДОУ в ДО, в которых прописан входной пользователь или пользователи из входного департамента.
 * Выполняется как правило при выполнении определенных входных условий и при изменении Зоны ДОУ во входной карточке. Внутри себя прописывает параметры запуска для нового спецзадачника. 
 */
public class RecalculateZoneDOWProcessor
	extends ProcessCard
	// implements Parametrized
{
	private static final long serialVersionUID = 1L;

	private static final String PARAM_ATTR_CONDITION = "attr_condition";
	private static final String PARAM_IS_ZONE_ACCESS_FLAG = "isZoneAccess";	// специальный флаг, обозначающий, что надо учитывать только те карточки, для которых применимы профильные правила с атрибутом профиля - Доступ к зонам ДОУ   
	private static final String PARAM_IS_DEPARTMENT_FLAG = "isDepartment";
	private static final String TASK_NAME = "RecalcZoneDOW";
	private static final String CRON_EXPRESSION = "0 0 0 * * ?";

	protected final List<BasePropertySelector> conditions = new ArrayList<BasePropertySelector>();
	protected boolean isDepartment = false;
	protected boolean isZoneAccess = false;
	
	@Override
	public Object process() throws DataException {
		final ObjectId cardId = getCardId();
		final Card card = getCard();
		if (cardId == null||card==null) {
			logger.warn("Impossible execute processor until card is saved -> exiting");
			return null;
		}
		if ((cardId==null&&!checkCardConditons(card))||(cardId!=null&&!checkCardConditons(cardId))) {
		    logger.warn("Card " + (cardId!=null?cardId.getId():card)
			    + " did not satisfies coditions. Exiting");
		    return null;
		}
		if (!conditions.isEmpty())
			if( logger.isDebugEnabled() )
				logger.debug("Card " + (cardId!=null?cardId.getId():card) + " satisfies coditions");

		// пройдемся по всем атрибутам, которые должны быть изменены
		// в зависимости от типа проверки измений, достаточно либо одного измененного атрибута либо всех сразу
		if (changeAttributes!=null){
			boolean someAttributesChanged = false;
			for(ObjectId changeAttributeCode: changeAttributes){
				final Attribute changeAttribute = card.getAttributeById(changeAttributeCode);
				if (!super.isAttributeChanged(changeAttribute, card)){
					if(isMultiplicationChangeAttributeOption){
						logger.warn("Attribute "+changeAttribute.getId().toString()+" in card " + (card.getId()!=null?card.getId().toString():null) + " is not change => exit");
						return null;
					}
				} else {
					someAttributesChanged = true;
				}
			}
			if(!someAttributesChanged){
				logger.warn("No attribute changed in card " + (card.getId()!=null?card.getId().toString():null) + " => exit");
				return null;
			}
		}

		recalculateZoneDow(cardId, isDepartment, isZoneAccess);
		return getResult();
	}
	
	@Override
	public void setParameter(String name, String value) {
		if (PARAM_IS_DEPARTMENT_FLAG.equalsIgnoreCase(name)) {
			this.isDepartment = Boolean.parseBoolean(value);
		} else if (PARAM_IS_ZONE_ACCESS_FLAG.equalsIgnoreCase(name)) {
			this.isZoneAccess = Boolean.parseBoolean(value);
		} else if (name.startsWith(PARAM_ATTR_CONDITION)) {
			try {
				final AttributeSelector selector = AttributeSelector
					.createSelector(value);
				this.conditions.add(selector);
			} catch (DataException ex) {
				ex.printStackTrace();
			}
		} else
			super.setParameter(name, value);
	}

	private boolean checkCardConditons(ObjectId cardId) throws DataException {
		if (conditions == null || conditions.isEmpty())
		    return true;
		final ObjectQueryBase cardQuery = getQueryFactory().getFetchQuery(
			Card.class);
		cardQuery.setAccessChecker(null);
		cardQuery.setId(cardId);
		final Card card = (Card) getDatabase().executeQuery(getSystemUser(),
			cardQuery);
		return checkConditions(conditions, card);
	}

	private boolean checkCardConditons(Card card) throws DataException {
		if (conditions == null || conditions.isEmpty())
		    return true;
		return checkConditions(conditions, card);
	}
    /**
     * Проверить выполнены ли условия conds для карточки card.
     *
     * @param conds
     * @param card
     * @return true, если условия выполнены (в том числе если их нет вовсе),
     *         false, иначе.
     * @throws DataException
     */
	private boolean checkConditions(List<BasePropertySelector> conds, Card card) {
		if (conds == null || card == null)
		    return true;
		for (BasePropertySelector cond : conds) {
		    if (!cond.satisfies(card)) {
			logger.debug("Card " + (card.getId()!=null?card.getId().getId():card)
				+ " did not satisfies codition " + cond);
			return false;
		    }
		}
		return true;
	}

	private void recalculateZoneDow(ObjectId cardId, boolean isDepartment, boolean isZoneAccess) throws DataException{
		Calendar startTime = Calendar.getInstance();
		startTime.add(Calendar.DAY_OF_MONTH, 1);
		startTime.set(Calendar.HOUR_OF_DAY, 0);
		startTime.set(Calendar.MINUTE, 0);
		startTime.clear(Calendar.SECOND);
		
		GetTask action = new GetTask();
		action.setDefaultCronExpression(CRON_EXPRESSION);
		action.setTaskName(TASK_NAME);
		action.setDefaultStartTime(startTime.getTime());
		ActionQueryBase aqb = getQueryFactory().getActionQuery(action);
		aqb.setAccessChecker(null);
		aqb.setAction(action);
		aqb.setUser(getSystemUser());
		String taskId = (String)getDatabase().executeQuery(getSystemUser(), aqb);
		if (taskId==null || taskId.isEmpty()){
			logger.warn("Task "+TASK_NAME+" not fount in system. Processor cancel.");
			return;
		}
		
		final String paramData = GenerateParameter(cardId, isDepartment, isZoneAccess);
		final String sql = 
			"insert into scheduler_parameter(param_id, task_id, param_data, task_module) \n" +
			"select \n" +
			"	nextval('seq_param_id') as param_id, \n" +
			"	st.task_id, \n" +
			"	? as param_data, \n" +
			"	st.task_module \n" +
			"from scheduler_task st \n" +
			"where st.task_id = ?"; 
		int count = getJdbcTemplate().update(sql, new Object[]{new SqlLobValue(paramData), taskId},
				new int[] { Types.BLOB, Types.VARCHAR });
		logger.info("Insert "+count+" parameters for task "+ TASK_NAME +"/"+taskId);
	}
	
	private String GenerateParameter(ObjectId cardId, boolean isDepartment, boolean isZoneAccess) throws DataException{
		Document doc;
		try {
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
			Element parentNode = doc.createElement("config");
			doc.appendChild(parentNode);
	
			Element cardNode = doc.createElement("cardId");
			cardNode.appendChild(doc.createTextNode(cardId.getId().toString()));
			parentNode.appendChild(cardNode);
	
			Element isDepart = doc.createElement("isDepartment");
			isDepart.appendChild(doc.createTextNode(Boolean.toString(isDepartment)));
			parentNode.appendChild(isDepart);
			
			Element isAccess = doc.createElement("isZoneAccess");
			isAccess.appendChild(doc.createTextNode(Boolean.toString(isZoneAccess)));
			parentNode.appendChild(isAccess);

			Long lastVersionId = getLastCardVersion((Long)cardId.getId());
			//проверяем последнюю версию карточки пользователя во входных атрибутах, если её там нет, то просто берём последнюю версию входной карточки пользователя   
			if (lastVersionId!=null&&lastVersionId.intValue()!=0){
				Element versionId = doc.createElement("lastVersionId");
				versionId.appendChild(doc.createTextNode(lastVersionId.toString()));
				parentNode.appendChild(versionId);
			}
			
			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer();

			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.METHOD,"xml");
			StringWriter sw = new StringWriter();

			StreamResult result = new StreamResult(sw);
			DOMSource source = new DOMSource(doc);
			transformer.transform(source, result);
			return sw.toString();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new DataException(e);
		}
	}
	
	/**
	 * Берем последнюю версию карточки
	 * @param cardId
	 * @return
	 */
	private Long getLastCardVersion(Long cardId){
		final String sql = 
			"select max(version_id) from card_version cv \n" +
			"where cv.card_id = ?";
		Long versionId = null;
		try{
			versionId = getJdbcTemplate().queryForLong(sql, new Object[]{cardId},
					new int[] { Types.NUMERIC });
		} catch (Exception e){
			versionId = null;
		}
		return versionId;
	}
}