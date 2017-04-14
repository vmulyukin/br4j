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
package com.aplana.dbmi.service.impl.query;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.aplana.dbmi.action.CreateCard;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.DefaultAttributeValue;
import com.aplana.dbmi.model.HtmlAttribute;
import com.aplana.dbmi.model.IntegerAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.SecurityAttribute;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.TextAttribute;
import com.aplana.dbmi.model.TreeAttribute;
import com.aplana.dbmi.model.TypedCardLinkAttribute;
import com.aplana.dbmi.model.DatedTypedCardLinkAttribute;
import com.aplana.dbmi.model.Workflow;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.ChildrenQueryBase;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.Parametrized;

/**
 * Query used to perform {@link CreateCard} action
 * @author dsultanbekov
 *
 */
public class DoNewCard extends ActionQueryBase implements Parametrized, WriteQuery {
	private static final long serialVersionUID = 1L;
	private Map<ObjectId, ObjectId> attributesToMap;
	
	/**
	 * Creates new instance of {@link Card} class by given {@link Template}
	 * and initializes its attributes with default values if any specified.
	 * <br>
	 * @return newly create {@link Card} object
	 */
	@SuppressWarnings("rawtypes")
	public Object processQuery() throws DataException {
		CreateCard action = getAction();
		final ObjectId templateId = action.getTemplate();
		ObjectQueryBase subQuery = getQueryFactory().getFetchQuery(Template.class);
		subQuery.setId(templateId);
		final Template template = getDatabase().executeQuery(getUser(), subQuery);

		final Card card = new Card();
		card.setTemplate(templateId);
		card.setTemplateNameRu(template.getNameRu());
		card.setTemplateNameEn(template.getNameEn());		
		
		subQuery = getQueryFactory().getFetchQuery(Workflow.class);
		subQuery.setId(template.getWorkflow());
		Workflow wf = getDatabase().executeQuery(getUser(), subQuery);

		final ObjectId statusId = wf.getInitialState();
		subQuery = getQueryFactory().getFetchQuery(CardState.class);
		subQuery.setId(statusId);
		final CardState cardState = getDatabase().executeQuery(getUser(), subQuery);
		card.setState(statusId);
		if (cardState!=null){
			card.setStateName(cardState.getName());
		}
		
		card.setAttributes(template.getBlocks());
		card.<PersonAttribute>getAttributeById(Attribute.ID_AUTHOR).setPerson(getUser().getPerson());
		card.<DateAttribute>getAttributeById(Attribute.ID_CREATE_DATE).setValue(new Date());

		if (Template.ID_REQUEST.equals(templateId))
			card.<PersonAttribute>getAttributeById(ObjectId.predefined(PersonAttribute.class,
					"request.customer")).setPerson(getUser().getPerson());

		final ChildrenQueryBase listChildren = getQueryFactory().getChildrenQuery(Template.class, DefaultAttributeValue.class);
		listChildren.setParent(templateId);
		final Collection<DefaultAttributeValue> defaultValues = getDatabase().executeQuery(getUser(), listChildren);
		
		Iterator<DefaultAttributeValue> i = defaultValues.iterator();
		while (i.hasNext()) {
			final DefaultAttributeValue defaultValue = i.next();
			Attribute attribute = card.getAttributeById(defaultValue.getAttributeId());
			if (attribute == null) {
				continue;
			}
			if (attribute instanceof StringAttribute) {
				((StringAttribute)attribute).setValue((String)defaultValue.getValue());
			} else if (attribute instanceof TextAttribute) {
				((TextAttribute)attribute).setValue((String)defaultValue.getValue());
			} else if (attribute instanceof IntegerAttribute) {
				((IntegerAttribute)attribute).setValue(((Integer)defaultValue.getValue()).intValue());
			} else if (attribute instanceof DateAttribute) {
				Date value = (Date) defaultValue.getValue();
				if (value.getYear() == 70) {
					/*
					 * boruroev: ��������� ����� ����� ������, ���� �� ����� ��������� ������� ������� �����,
					 * ����� �������, ��� ���-�� � ��������� ��������� ������� �������� ������� ���� � ������
					 * ������� ����. ���� ���� ��� �����������, ��� ���� ������ ��-������� 
					 */
					//value.setTime(System.currentTimeMillis() + value.getTime());
					value.setTime(System.currentTimeMillis());
				}
				((DateAttribute)attribute).setValue(value);
			} else if (attribute instanceof ListAttribute) {
				((ListAttribute)attribute).setValue((ReferenceValue)defaultValue.getValue());
			} else if (attribute instanceof PersonAttribute) {
				List<Person> values = (List<Person>) defaultValue.getValue();
				for (Iterator<Person> itr = values.iterator(); itr.hasNext(); ) {
					Person person = itr.next();
					if (Person.ID_CURRENT.equals(person.getId())) {
						itr.remove();
						values.add(getUser().getPerson());
						break;
					}
				}
				((PersonAttribute)attribute).setValues(values);
			} else if (attribute instanceof TreeAttribute) {
				((TreeAttribute)attribute).setValues((List)defaultValue.getValue());
			} else if (attribute instanceof HtmlAttribute) {
				((HtmlAttribute)attribute).setValue((String)defaultValue.getValue());
			} else if (attribute instanceof CardLinkAttribute) {
				// (2010/02, RuSA) OLD: ((CardLinkAttribute)attribute).setValues((List)defaultValue.getValue());
				// ((CardLinkAttribute)attribute).setLabelLinkedCards( (List)defaultValue.getValue() );
				((CardLinkAttribute)attribute).setIdsLinked((Collection)defaultValue.getValue());
			} else if (attribute instanceof TypedCardLinkAttribute) {
				((TypedCardLinkAttribute) attribute).setIdsLinked((Collection) defaultValue.getValue());
			} else if (attribute instanceof DatedTypedCardLinkAttribute) {
				((DatedTypedCardLinkAttribute) attribute).setIdsLinked((Collection) defaultValue.getValue());
			} else if (attribute instanceof SecurityAttribute) {
				((SecurityAttribute)attribute).setAccessList((List)defaultValue.getValue());
			}
		}
		
		Card parent = action.getParent();
		if(attributesToMap != null && !attributesToMap.isEmpty() && parent != null){
			for(Map.Entry<ObjectId, ObjectId> entry : attributesToMap.entrySet()){
				Attribute from = parent.getAttributeById(entry.getKey());
				Attribute to = card.getAttributeById(entry.getValue());
				if(from != null && to != null){
					try{
						to.setValueFromAttribute(from);
					} catch(UnsupportedOperationException e){}
				}
			}
		}
		/* A.P. 08.04.2010 - Following code was used only for subsequent call of
		 * AttributeUtils.initCardLinkAttributes
		final List cardLinks = new ArrayList();
		for(i = card.getAttributes().iterator(); i.hasNext(); ) {
			final TemplateBlock block = (TemplateBlock)i.next();
			for (Iterator j = block.getAttributes().iterator(); j.hasNext(); ) {
				final Attribute attr = (Attribute)j.next();
				if (attr instanceof CardLinkAttribute) {
					cardLinks.add(attr);
				}
			} 
		}
		*/
		// (2010/02, RuSA) OLD: AttributeUtils.initCardLinkAttributes(cardLinks, getQueryFactory(), getDatabase(), getUser());

		return card;
	}

	public void setParameter(String name, String value) {
		if(name.equalsIgnoreCase("mapAttributesFromParent")){
			attributesToMap = ObjectIdUtils.stringToAttrIdsMap(value, ",", "->", ":");
		}	
	}
}
