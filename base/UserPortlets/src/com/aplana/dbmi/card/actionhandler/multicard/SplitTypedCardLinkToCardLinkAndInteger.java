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
package com.aplana.dbmi.card.actionhandler.multicard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.card.CardPortletSessionBean;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.IntegerAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.TypedCardLinkAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ServiceException;

public class SplitTypedCardLinkToCardLinkAndInteger implements MappingProcedure {

	public static final String ERROR_INTEGER_FIELD_IS_EMPTY = "errorIntegerFieldIsEmpty";

	protected Log logger = LogFactory.getLog(getClass());
	/**
	 * �������� �� ������� �������� ������� {@link TypedCardLinkAttribute} � ID ��������� � args[0]
	 * (��������� ������� ������ ����� ������ "������� (�������)" ��� �����������, ��� ����� ������ ����� �������� ��������),
	 * ��������� ��� ������ ����������� ������ {@link #execute(Attribute)} ���������� �� ������ �� ���� �������� 
	 * � �������� {@link CardLinkAttribute} � {@link IntegerAttribute}
	 * @param session ������� ������
	 * @param args ������ �� ������ ��������: ��������, �������� �������� �������������� �� �������� ���������
	 * @throws MappingUserException � ����� errorIntegerFieldIsEmpty ���� ���� �� ���� �� �������� ����� �� ���������: {0} - ��� ������� ��� ������� �� ��������� �������� ���� 
	 * @throws MappingSystemException ��� ������� � ���������������� ����� ��� ��
	 */
	public void init(CardPortletSessionBean session, String[] args) 
								throws MappingUserException, MappingSystemException {
		// �������� ������ 1 ��������
		if (args.length != 1)
			throw new MappingSystemException("One argument expected, but " + args.length + " given: " + args);
		TypedCardLinkAttribute splitting;
		try {
			// �������� TypedCardLinkAttribute, �������� �������� ����� ������������
			splitting = (TypedCardLinkAttribute)session.getActiveCard().getAttributeById(
					MappingUtils.stringToAttrId(args[0]));
		} catch (ClassCastException e) {
			MappingSystemException me = new MappingSystemException("Type of attribute \"" + args[0] + "\" must be " + TypedCardLinkAttribute.class.getCanonicalName(), e);
			logger.error(me.getMessage());
			throw me;
		}
		if (splitting == null) {
			MappingSystemException e = new MappingSystemException("Attribute " + args[0] + " not found");
			logger.error(e.getMessage());
			throw e;
		}
		// ��� ������ ����� ��������� �������� ���
		if (splitting.getIdsLinked().isEmpty()) {
			cardIntIter = Collections.EMPTY_LIST.iterator();
			return;
		}
		// ������ ��� ����� ������ �� ���������
		try {
			// ��������� ����� "ID �������� -> ID �������� �� �����������"
			Map<Long, Long> types = splitting.getTypes();
			// ����� "�������� -> �����" �� ������� ����� ���������� ��������
			Map<Card,Integer> linkInteger = new HashMap<Card,Integer>(splitting.getIdsLinked().size());
			for (Object cardId: splitting.getIdsLinked()) {
				// �������� ID �������� �� ����������� (���� ������������ �� ������, ����� null)
				Card card = (Card)session.getServiceBean().getById((ObjectId)cardId);
				Long linkTypeId = types.get((Long)((ObjectId)cardId).getId());
				if (linkTypeId == null) {
					logger.error(ERROR_INTEGER_FIELD_IS_EMPTY);
					throw new MappingUserException(ERROR_INTEGER_FIELD_IS_EMPTY, new Object[] {card.getAttributeById(Attribute.ID_NAME)});
				}
				// �������� �������� �� ID
				ReferenceValue val = (ReferenceValue)session.getServiceBean().getById(
						new ObjectId(ReferenceValue.class, linkTypeId));
				if (val == null) {
					MappingSystemException e = new MappingSystemException(ReferenceValue.class.getName() + " not found");
					logger.error(e.getMessage());
					throw e;
				}
				try {
					// ���������� ������������ ������������ -> �����
					linkInteger.put(card, new Integer(val.getValue()));
				} catch (NumberFormatException e) {
					MappingSystemException me = new MappingSystemException("Reference value don't contain an Integer: " + val.getValue(), e);
					logger.error(me.getMessage());
					throw me;
				}
			}
			// ������� ��������
			cardIntIter = sortByValue(linkInteger).entrySet().iterator();
		} catch (DataException e) {
			logger.error(e.getMessage());
			throw new MappingSystemException(e.getMessage(), e);
		} catch (ServiceException e) {
			logger.error(e.getMessage());
			throw new MappingSystemException(e.getMessage(), e);
		}
	}

	// ���������� ����� �� ��������� �����, � ���� ����� ��������� -- �� ������ ������
	private static Map<Card,Integer> sortByValue(Map<Card,Integer> map) {
	     List<Map.Entry<Card,Integer>> list = new LinkedList<Map.Entry<Card,Integer>>(map.entrySet());
	     Collections.sort(list, new Comparator<Map.Entry<Card,Integer>>() {
	          public int compare(Map.Entry<Card,Integer> o1, Map.Entry<Card,Integer> o2) {
	               int comp = o1.getValue().compareTo(o2.getValue());
	               return comp != 0 ? comp : o1.getKey().getAttributeById(Attribute.ID_NAME).compareTo(o2.getKey().getAttributeById(Attribute.ID_NAME));
	          }
	     });
	     Map<Card,Integer> result = new LinkedHashMap<Card,Integer>();
	     for (Iterator<Map.Entry<Card,Integer>> it = list.iterator(); it.hasNext();) {
	    	 Map.Entry<Card,Integer> entry = (Map.Entry<Card,Integer>)it.next();
	    	 result.put(entry.getKey(), entry.getValue());
	     }
	     return result;
	}
	
	private Iterator<Map.Entry<Card, Integer>> cardIntIter;
	
	private final Collection<Card> singleCardList = new ArrayList<Card>(1);
	public boolean execute(List<Attribute> attrs) throws MappingSystemException {
		// ��������� ����� 2 ��������
		if (attrs.size() != 2) {
			MappingSystemException e = new MappingSystemException(getClass().getSimpleName() + " needs exactly 2 attributes");
			logger.error(e.getMessage(), e);
			throw e;
		}
		// ���� ��������� ������, ���������� false
		if (!cardIntIter.hasNext())
			return false;
		
		// ���� ������ �� ���������
		Map.Entry<Card, Integer> cardInt = cardIntIter.next();
		// ���������� ������� � ������
//		singleCardList.clear();
//		singleCardList.add(cardInt.getKey());
		try {
			// ���������� ������ �� ����� ������� � ������ �������
			((CardLinkAttribute)(attrs.get(0))).addLabelLinkedCard(cardInt.getKey());
		} catch (ClassCastException e) {
			MappingSystemException me = new MappingSystemException("First attribute must be a type of " + CardLinkAttribute.class.getSimpleName(), e);
			logger.error(me.getMessage());
			throw me;
		}
		try {
			// ���������� ��������������� ����� �� ������ �������
			((IntegerAttribute)(attrs.get(1))).setValue(cardInt.getValue());
		} catch (ClassCastException e) {
			MappingSystemException me = new MappingSystemException("Second attribute must be a type of " + IntegerAttribute.class.getSimpleName(), e);
			logger.error(me.getMessage());
			throw me;
		}
		return true;
	}

}