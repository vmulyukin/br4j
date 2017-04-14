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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.jbr.util.CardUtils;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.DatabaseClient;
import com.aplana.dbmi.service.impl.Parametrized;
import com.aplana.dbmi.service.impl.ProcessorBase;
import com.aplana.dbmi.service.impl.UserData;

public class CallCardsLinkedToBaseProcessor extends ProcessCard implements Parametrized {
	//private Map<String, String> parameters = new HashMap<String, String>();
	private ObjectId templateId;
	private ObjectId linkId;
	private String processorClassName;
	private boolean isSystemUser;

	@SuppressWarnings("unchecked")
	@Override
	public Object process() throws DataException {

		if (templateId == null || linkId == null || processorClassName == null){
			logger.error("CallChildrensProcessor: template or/and link is not set.");
			return null;
		}
		final UserData user;
		if(isSystemUser){
			user = getSystemUser();
		} else {
			user = getUser();
		}
		final Card card = getCard();
		// ��������� �� ���� ���������, ������� ������ ���� ��������
		// � ������� �� ���� �������� �������, ���������� ���� ������ ����������� �������� ���� ���� �����
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
		final List<Card> cards = getCardsLinkedToBase(linkId, templateId);
		if (cards == null)
			return null;
		for(Card mainCard : cards){
			try {
				ProcessorBase processor = null;
				try {
					final Class<ProcessorBase> prClass = (Class<ProcessorBase>) Class.forName(processorClassName);
					processor = prClass.newInstance();
					final Set<Class> ints = new HashSet<Class>( Arrays.asList(prClass.getInterfaces()));
					Class<?> supa = prClass.getSuperclass();
					while (supa != null){
						ints.addAll(Arrays.asList(supa.getInterfaces()));
						supa = supa.getSuperclass();
					}	

					if (ints.contains(DatabaseClient.class)){
						((DatabaseClient)processor).setJdbcTemplate(getJdbcTemplate());
					}
					/* (BR4J00035582, YNikitin) ����� ���, ��� �������� ��������� ��� ������������� � ����������� CallOriginsProcessor � CallCardsLinkedToBaseProcessor ���� ��� ������� �������� ��� JdbcTemplate � BeanFactory, ��� ��� �������� � QueryFactory, 
					 * � ����� ��� ��������� � ��� � ������ SetParameter ��������� ������. 
					 */
					processor.setBeanFactory(getBeanFactory());
					if (ints.contains(Parametrized.class)){
						for (Iterator<String> i = params.keySet().iterator(); i.hasNext(); ){
							String name = i.next();
							// ������������� ��������� �������� ����� ������ � ������� ��������, � ��������� ������!!!
							if (name.equalsIgnoreCase(PARAM_CHANGE_ATTRIBUTE)||name.equalsIgnoreCase("PARAM_CHANGE_ATTRIBUTE_OPTION")){
								continue;
							}
							((Parametrized)processor).setParameter(name, params.get(name));
						}
					}
				} catch (Exception e) {
					logger.error("Class "+processorClassName+" not found !");
					throw new Exception(e);
				}
				try {
					processor.init(getCurrentQuery());
					processor.setObject( mainCard);
					processor.setUser(user);
					processor.setAction(getAction());
					processor.setCurExecPhase(getCurExecPhase());
				} catch (Exception e) {
					logger.error("There is an error when setting "+processorClassName+" processor!");
					e.printStackTrace();
					throw new Exception(e);
				}
				try {
					processor.process();
				} catch (DataException e) {
					logger.error("There is an error when running "+processorClassName+" processor!");
					e.printStackTrace();
					throw e;
				} catch (Exception e) {
					logger.error("There is an error when running "+processorClassName+" processor!");
					e.printStackTrace();
					throw new Exception(e);
				}
			} catch (DataException e) {
				throw e;
			} catch (Exception e) {
				throw new DataException(e);
			}
		}
		return null;
	}

	/*	���������� ������ �������� � �������� c id-������ templateId, ������� ��������� �� ������ 
	 *  ����� ��������� � id-������ linkId
	 */
	@SuppressWarnings("unchecked")
	private List<Card> getCardsLinkedToBase(ObjectId loadAttrId, ObjectId filterTemplateId) 
		throws DataException
	{
		final UserData user = getSystemUser();

		final Search action = new Search();
		action.setByAttributes(true);
		action.setWords(null);
		if (loadAttrId.getType().equals(PersonAttribute.class)){
			final Set<Person> found = CardUtils.getPersonsByCards(Collections.singleton(getCardId()), 
					getQueryFactory(), getDatabase(), getSystemUser() );
			if (found==null||found.size()==0){
				return null;	// ������� ������ ������ ���������, ��� ������� � ��� ��� � ��, ������� ����� �������� �� ����� ������
			}
			ObjectId curPerson = found.iterator().next().getId();
			action.addPersonAttribute(loadAttrId, curPerson);
		} 
		else {
			action.addCardLinkAttribute(loadAttrId, getCardId());
		}

		final List<DataObject> templates = Collections.singletonList(DataObject.createFromId(filterTemplateId));
		action.setTemplates( templates);

		final List<SearchResult.Column> columns = new ArrayList<SearchResult.Column>();
		columns.add( CardUtils.createColumn(Card.ATTR_STATE));
		columns.add( CardUtils.createColumn(Card.ATTR_TEMPLATE));
		action.setColumns(columns);

		final List<Card> cards = CardUtils.execSearchCards(action, getQueryFactory(), getDatabase(), user); 
		return cards;
	}

	public void setParameter(String name, String value) {
		if ("processorClassName".equals(name)) {
			this.processorClassName = value;
		} else	if ("templateId".equals(name)) {
			this.templateId = ObjectId.predefined(Template.class, value);
		} else if ("linkId".equals(name)) {
			this.linkId = IdUtils.smartMakeAttrId(value, CardLinkAttribute.class, false);
		} else if("performFromSystem".equals(name)){
			this.isSystemUser = Boolean.valueOf(value);
		} else
			super.setParameter(name, value);
	}
}
