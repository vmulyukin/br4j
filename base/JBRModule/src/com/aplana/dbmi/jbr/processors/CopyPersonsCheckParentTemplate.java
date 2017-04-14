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
import java.util.Collection;
import java.util.HashMap;

import org.springframework.jdbc.core.JdbcTemplate;

import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.TypedCardLinkAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.access.AccessRuleManager;
/**
 ������������������ ���������, ������������ ��� ���������� ������� �����, ���������� ����
 � ����������� �� ������� ���������-���������.
 ���������:
 	parentLink - BackLink �� ��������-���������;
 	srcAttrId - Person, ������� ����� ����������� ����;
 	associationsList - ��������� ������ ������ - ����������� Person
 	(������: "jbr.outcoming -> jbr.visa.person.outbound;
			jbr.interndoc -> jbr.visa.person.internal").
*/
@SuppressWarnings("serial")
public class CopyPersonsCheckParentTemplate extends AbstractCopyPersonProcessor {
	public final static ObjectId TEMPLATE = ObjectId.predefined(ListAttribute.class, "template.name");
	private ObjectId parentLinkId;
	private ObjectId srcAttrId;
	private HashMap <Long, ObjectId> associationsList = new HashMap <Long, ObjectId>();

	private SearchResult getSearchForCardAnsParentAttrId(ObjectId cardId, ObjectId parentId) throws DataException{
		Search search = new Search();
		search.addCardLinkAttribute(parentId, cardId);
		search.setByAttributes(true);
		SearchResult.Column column = new SearchResult.Column();
		column.setAttributeId(TEMPLATE);
		ArrayList<SearchResult.Column> columns = new ArrayList<SearchResult.Column>();
		columns.add(column);
		search.setColumns(columns);
		final ActionQueryBase query = getQueryFactory().getActionQuery(search);
		query.setAccessChecker(null);
		query.setAction(search);
		return (SearchResult) getDatabase().executeQuery(getSystemUser(), query);
	}
	
	@Override
	public Object process() throws DataException 
	{
		Card card = ((ChangeState) getAction()).getCard();
		ObjectId parentId = ((BackLinkAttribute)card.getAttributeById(parentLinkId)).getLinkSource();
		SearchResult searchResult = getSearchForCardAnsParentAttrId(card.getId(), parentId);

		if (searchResult.getCards() == null||searchResult.getCards().isEmpty()){	// ���� ��� ��������� �� ������� �� ����� ������������ ��������, �� ���� �� ������ ���������� 
			Collection<ObjectId> linkSources = ((BackLinkAttribute)card.getAttributeById(parentLinkId)).getLinkSources();
			if (linkSources!=null){
				for(ObjectId linkSourceId: linkSources){
					parentId = ((BackLinkAttribute)card.getAttributeById(parentLinkId)).getLinkSource();
					searchResult = getSearchForCardAnsParentAttrId(card.getId(), parentId);
					if (searchResult.getCards() != null&&!searchResult.getCards().isEmpty())
						break;
				}
			}
		}

		if (searchResult.getCards().iterator().hasNext()) {
			Card parentCard = (Card) searchResult.getCards().iterator().next();
			Long template = (Long) ((ListAttribute)parentCard.getAttributeById(TEMPLATE)).getValue().getId().getId();
			if (associationsList.containsKey(template))
			{
			    cleanAccessList(card.getId());
				PersonAttribute targetAttr = 
					(PersonAttribute) card.getAttributeById(associationsList.get(template));
				PersonAttribute srcAttr = (PersonAttribute) card.getAttributeById(srcAttrId);
				String sql =
					"DELETE FROM attribute_value " +
	                "WHERE attribute_code = " + 
	                "'" + targetAttr.getId().getId() + "' " + 
	                "and card_id= " +
	                card.getId().getId() + ";" +
	                "INSERT INTO attribute_value" +
	                "(card_id, attribute_code, number_value) " + 
	                "VALUES (" +
	                card.getId().getId() + ", " +
	                "'" + targetAttr.getId().getId() + "'" + ", " +
	                srcAttr.getPerson().getId().getId() + ");";
				getJdbcTemplate().update(sql);
				
				recalculateAccessList(card.getId());
			}
		}
		return null;
	}
	
	public void setParameter(String name, String value) 
	{
		if(name.equalsIgnoreCase("parentLink"))
			parentLinkId = ObjectId.predefined(BackLinkAttribute.class, value);
		else if(name.equalsIgnoreCase("srcAttr"))
			srcAttrId = ObjectId.predefined(PersonAttribute.class, value);
		else if(name.equalsIgnoreCase("associationsList"))
		{
			String[] temp = value.trim().split(";");
			for(int i = 0; i < temp.length; i++)
			{
				String temp0 = temp[i].split("->")[0].trim();
				String temp1 = temp[i].split("->")[1].trim();
				ObjectId key = ObjectId.predefined(Template.class, temp0);
				ObjectId mapValue = ObjectId.predefined(PersonAttribute.class, temp1);
				associationsList.put((Long)key.getId(), mapValue);
			}
		}
		super.setParameter(name, value);
	}
}