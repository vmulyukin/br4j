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
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.MessageException;

/**
 * ��������� ������ ���������� ��������� � ���������� ���������������� �������� �������� �� ���. 
 * ���� � ������� ��������� ����������� � "��" ���� "��������� �� �����������" 
 * ����������� ����������� ����� ��� ��������� ������� �� �� ���� �� ����������� ��� � ��������
 * "���������������", "������������", "����������", "��������", "����� � �������� � ����", ������� 
 * �������� ����� �������� (������ ���������� "appealAttributes" � ������� ���������� � ���� �� ������ ���������. 
 * ������������ ������ ��������� ����������� �� ���������, �������� ���������� "authorAttributes", 
 * ���� �� �� �����, �� ������ �������� ������. <br/><br/>
 * <i>
 * �������� ��������� = ���������� {("," | ";") ����������}. <br/>
 * �������� ������ ��������� = ���������� {("," | ";") ����������}. <br/>
 * ���������� = [������������� ������ �������� ":"] ��������� �������� | ��� ��������.
 * </i>
 * 
 * @author erentsov
 */

public class FillPreviousAppealsProcessor extends ProcessCard{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected final static ObjectId CHECK_REPEAT_ID = ObjectId.predefined(ListAttribute.class, "jbr.income.repeatChkEnable");
	protected final static ObjectId YES_ID = ObjectId.predefined(ReferenceValue.class, "jbr.commission.control.yes");	
	protected final static ObjectId AUTHOR = ObjectId.predefined(CardLinkAttribute.class, "jbr.ReqAuthor");
	protected final static ObjectId REGDATE_ID = ObjectId.predefined(DateAttribute.class, "regdate");
	protected final static ObjectId CA_TEMPLATE_ID = ObjectId.predefined(Template.class, "jbr.incomingpeople");
	protected final static ObjectId AUTHOR_TEMPLATE_ID = ObjectId.predefined(Template.class, "jbr.medo_og.requestAuthor");
	protected final static ObjectId PREV_APPEALS_ID = ObjectId.predefined(CardLinkAttribute.class, "jbr.previous.appeals");
	protected final static Collection<ObjectId> states = IdUtils.makeStateIdsList(
			"registration, consideration, execution, delo, done, ready-to-write-off"
	);
	
	private List<ObjectId> authorAttributesIds;
	private List<ObjectId> appealAttributeIds;
	
	@SuppressWarnings("unchecked")
	@Override
	public Object process() throws DataException, MessageException
	{
		Card card = loadCardById(getCard().getId());	// �������� ��������, �.�. �� ����� ���� ������� ���������
		ListAttribute checkAttr = (ListAttribute)card.getAttributeById(CHECK_REPEAT_ID);
		CardLinkAttribute authorOfAppeal = (CardLinkAttribute) card.getAttributeById(AUTHOR);
		if(checkAttr != null && checkAttr.getValue() != null && !checkAttr.getValue().getId().equals(YES_ID)){
			logger.info("Filling previous appeals is OFF. Exiting.");
			return card;
		}
	
		Collection<ObjectId> authorsIdsToSearch;
		if(authorAttributesIds != null && !authorAttributesIds.isEmpty()){
			Card authorCard = fetchSingleCard(authorOfAppeal.getSingleLinkedId(), authorAttributesIds, true);
			Search search = new Search();
			search.setByAttributes(true);
			search.setTemplates(Collections.singleton(AUTHOR_TEMPLATE_ID));
			for(ObjectId id : authorAttributesIds) search.addAttribute(authorCard.getAttributeById(id));
			authorsIdsToSearch = ObjectIdUtils.getIdsFromObjects((List<Card>) ((SearchResult) execAction(search)).getCards());
		} else authorsIdsToSearch = Collections.singleton(authorOfAppeal.getSingleLinkedId());
		
		Search search = new Search();
		search.setByAttributes(true);
		search.setTemplates(Collections.singleton(CA_TEMPLATE_ID));
		search.setIgnoredIds(Collections.singleton(this.getCardId()));
		search.setStates(states);
		
		for(ObjectId id :  authorsIdsToSearch) search.addCardLinkAttribute(AUTHOR, id);

		Calendar to = Calendar.getInstance();
		Calendar from = Calendar.getInstance();	
		from.set(to.get(Calendar.YEAR), 0, 1, 0, 0, 0);
		search.addDateAttribute(REGDATE_ID, from.getTime(), to.getTime());

		if(appealAttributeIds != null) for(ObjectId id : appealAttributeIds) search.addAttribute(card.getAttributeById(id));
				
		SearchResult searchResult = (SearchResult) execAction(search);		
	
		ArrayList<ObjectId> duplicates = new ArrayList<ObjectId>();
		for(Card c : (Collection<Card>) searchResult.getCards()) duplicates.add(c.getId());
				
		CardLinkAttribute prevAppeals = (CardLinkAttribute) card.getAttributeById(PREV_APPEALS_ID);
		prevAppeals.setIdsLinked(duplicates);
		doOverwriteCardAttributes(card.getId(), prevAppeals);
		logger.info(duplicates.size() + " previous appeals links saved. \n");
				
		return card;
	}
	
	@Override
	public void setParameter(String name, String value){
		if(name.equalsIgnoreCase("authorAttributes")){
			authorAttributesIds = IdUtils.stringToAttrIds(value, StringAttribute.class);
		} if(name.equalsIgnoreCase("appealAttributes")){
			appealAttributeIds = IdUtils.stringToAttrIds(value, StringAttribute.class);
		} else super.setParameter(name, value);
	}
	
}
