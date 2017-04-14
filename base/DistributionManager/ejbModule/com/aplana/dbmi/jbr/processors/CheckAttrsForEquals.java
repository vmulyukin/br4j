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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.aplana.dbmi.jbr.util.PathAttributeDescriptior;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.IntegerAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.TextAttribute;
import com.aplana.dbmi.model.TreeAttribute;
import com.aplana.dbmi.model.TypedCardLinkAttribute;
import com.aplana.dbmi.service.DataException;

/**
 * ��������� ��������� ��������� �� ��������. � ���� �� ���������, �� ������������ ���������� �� ��������� �� ����� ���������.
 * ������������ �������� �������� ������ ��������� ������ ���� �������� � ����������� �� ������ ���� �������:
 * 1) ���� ���� �� ���� �������� ������������ ������� ��������� ��������� ��������, �� � ��� ��������� �������������� ���������, � �������� ������������
 * 2) ���� ���� �� ���� �������� ������������ ������� ������, �� ������������ ���������� �� �������� ��������� ���������, ���� ����� ����, ����� �������� ������������
 *
 * ������������: ���� �������� ���������, �� ������������ ���������� �� ��������� ���������.
 *
 * ������������/�������������� ��������� �����������, ������ � ��� ������ ����� ����������� ������� �������

 * ��������� ����������� �� TestEmptyAttrProcessor, �.�. �� ������ ���� �� ������ � ���������, � ���� �� � ��������� ���������� �������� �������
 *
 * ��� �������� ������� 4 ���������: <br>
 * 1. condition - ������� ������� � ���� "<������� ������� ��������><�������� ���������><���������>" <br>
 * 2. linked_attr_test - ������� ������� � ���� "<������� ������� ��������><�������� ���������><���������>", �� ������ ��� ��������� �������� <br>
 * 3. linked_attr_state - ������ �������� ��������� ��������, ��� ������� ����������� ���������.<br>
 * 4. no_system_user - ������ � ��� ������, ����� ������� ������������ �� ������� (������ ��� ������������� ������ � �������������� �������� � ����������)
 *
 * �����������:
 * 1. ����� ���������� �������� ������ �����: ��������� �������������� �� ������ ���������� �������� ������� ��������
 * 2. ����� ���������� ��� ����� �������� � TypedCardlinkAttribute
 * @author ynikitin
 *
 */
public class CheckAttrsForEquals extends TestEmptyAttrProcessor {

	private static final long serialVersionUID = 1L;

	/**
	 * ����������� ����������� ��������� ����������, ������� ��� � ��������: PARAM_XXX.
	 */
	private static final String MSG_CARD_0_DID_NOT_SATISFY_CONDITIONS = "card ''{0}'' did not satisfy conditions => cancel check attributes for equals";
	private static final String MSG_CARD_0_CREATE_ANYWAY = "card ''{0}'' did not satisfy conditions => cancel check attributes for equals";
	private static final String MSG_ATTRIBUTE_MULTI_VALUE = "attribute ''{0}'' in card ''{1}'' have multivalue => it have ignored in check equals processing";
	private static final String MSG_ATTRIBUTE_NOT_SUPPORTED = "attribute ''{0}'' is not supported by CheckAttrsForEquals-processor";
	public static final String MSG_CARD_0_HAS_EMPTY_CARDTYPE = "Card ''{0}'' contain EMPTY cardtype for TypedCardlinkAttribute ''{1}''";
	private static final String REG_SEPARATOR = "[;]";

	/**
	 * ������������ ��������
	 * ������ ��������� ��� ��������
	 * ������ �������:
	 * --------------------------------------------------------------------------------------------------------------------------------------------------
	 * {��������/�������_1_�_�������_��������.}{��������/�������_�_���������_��������_1.}...{��������/�������_�_���������_��������_n.}��������_�������_1;
	 * ...
	 * {��������/�������_m_�_�������_��������.}{��������/�������_�_���������_��������_1.}...{��������/�������_�_���������_��������_mn.}��������_�������_m
	 * --------------------------------------------------------------------------------------------------------------------------------------------------
	 */
	private static final String PARAM_CHECK_FOR_EQUALS_ATTRS = "checkAttrsForEquals";


	private static final String PARAM_MUST_EQUALS = "must_equals";				// "��������" ������ ���������� - ���������� ����� �������� � ������ �������� ���������
	private static final String PARAM_EQUALS_MESSAGE = "equals_message";		// ��������� ��� ���������� � ������ ��������� ���������
	private static final String PARAM_NOT_EQUALS_MESSAGE = "not_equals_message";// ��������� ��� ���������� � ������ ����������� ���������

	boolean noSystemUser = false;	// �� ��������� ����������� ������������ �� ����� ��������
	boolean mustEquals = true;		// �� ��������� ��������� ���������, ����� ��� �������� ���� � ����������� ����������
									// false � ���� ������� ��������, ��� ���� �� 2 �������� ������ ���� �� �����
	boolean emptyError = false;		// �� ��������� ��������� ��������� ������� ������ �� ���������
	private String emptyMessage;	// ���������, ������� ����� ����������, ���� ���� �� ���� �� ��������� �� ��������
	private String equalsMessage = "All input attributes must be equals";	// ���������, ������� ����� ��������
	private String notEqualsMessage = "Same 2 attributes can be not equals";

	private List<PathAttributeDescriptior> checkAttrsForEquals = new ArrayList<PathAttributeDescriptior>();
	
	@Override
	public Object process() throws DataException {
		Card card = getCard();
		//Card parentCard = null;
		List<Card> linkedCards = null;
		if (noSystemUser&&getUser().getPerson().getId().getId().equals(getSystemUser().getPerson().getId().getId())){
			logger.warn("Proceesor working when current user is only no _SYSTEM_, but current user is "+getUser().getPerson().getFullName());
			return null;
		}
		if ( (linkedConditions != null) && (linkedAttr != null) ){
			linkedCards = getLinked(card.getId(), linkedAttr);
		}

		// ���� ������� �������� ������ ��� ��� ����������, ���� ������� �������� � ������������ �������� �����������, ���� ������� � ��������� ��������� �����������, �� ��������� �������� �� ���������������
		if (((conditionAttrs != null && conditionAttrs.check(card, getUser()))||conditionAttrs==null)&&checkLinkedConditions(linkedConditions, linkedCards)) {
			Set<String> setOfAttrList = processCheckAttributes(checkAttrsForEquals, card);
			// ���� ������ ���� ������������, �� ����� ������ ������ ���� ����� 1
			if(mustEquals&&setOfAttrList.size()>1){
				throw new DataException(equalsMessage);
			}
			if(!mustEquals&&setOfAttrList.size()==1){
				throw new DataException(notEqualsMessage);
			}
		}
		return null;
	}


	@Override
	public void setParameter(String name, String value) {
		// ��������� ������� �������
		if (PARAM_MUST_EQUALS.equals(name)){
			mustEquals = Boolean.parseBoolean(value);
		} else if (PARAM_NO_SYSTEM_USER.equalsIgnoreCase(name)){
			noSystemUser = Boolean.parseBoolean(value);
		} else if (PARAM_EMPTY_MESSAGE.equalsIgnoreCase(name)){
			emptyMessage = value.trim();
			emptyError = (emptyMessage!=null&&!emptyMessage.equals(""));
		} else if (PARAM_EQUALS_MESSAGE.equalsIgnoreCase(name)){
			equalsMessage = value.trim();
		} else if (PARAM_NOT_EQUALS_MESSAGE.equalsIgnoreCase(name)){
			notEqualsMessage = value.trim();
		// ��������� ��� ������������� ���������
		} else if (PARAM_CHECK_FOR_EQUALS_ATTRS.equals(name)){
			final String[] sIds = value.trim().split(REG_SEPARATOR);
			for (String s: sIds) {
				if (s == null || s.length() == 0) continue;
				final PathAttributeDescriptior id = makeObjectId(s);
				checkAttrsForEquals.add(id);
			}
		} else
			super.setParameter(name, value);
	}

	/**
	 * @param s ������ ���� "{���:} id {@id2}",
	 * {x} = �������������� ����� x.
	 * ��������, "string: jbr.organization.shortName"
	 * 			 "back: jbr.sender: fullname"
	 * ���� ��� ������, �� ������ ���������������� �������� � ����� ������ �
	 * �����-���� �����, ���� ��� ������, �� ����������� �� "string".
	 * @return
	 * @throws DataException
	 */
	protected static PathAttributeDescriptior makeObjectId(final String s)	{
		return (s == null) ? null : new PathAttributeDescriptior(s);
	}

	/** �������� ��������� ��������� �������� �������� ���������, ���������� ��� ����������� �� ���� ����������/��������� �� ��������� checkAttrListForEquals
	 *
	 * @param card - ������� ��������
	 * @param checkAttrListForEquals - 	������ ��������� ��� �������� � �������:
	 * --------------------------------------------------------------------------------------------------------------------------------------------------
	 * {��������/�������_1_�_�������_��������.}{��������/�������_�_���������_��������_1.}...{��������/�������_�_���������_��������_n.}��������_�������_1;
	 * ...
	 * {��������/�������_m_�_�������_��������.}{��������/�������_�_���������_��������_1.}...{��������/�������_�_���������_��������_mn.}��������_�������_m
	 * --------------------------------------------------------------------------------------------------------------------------------------------------
	 * @return Set<String> - ��������� ��������� �������� ��������� �� �������� ��������
	 * @throws DataException
	 */
	protected Set<String> processCheckAttributes(List<PathAttributeDescriptior> checkAttrListForEquals, Card processCard) throws DataException
	{
		Set<String> result = new HashSet<String>();
		//
		// ����������� �� ���� ������������� ��������� � ��������� ������ ������������� ��������
		for (PathAttributeDescriptior item: checkAttrListForEquals) {
//			List<Attribute> finalAttributes = new ArrayList();
			List<Card> foundFarCards;	// ��������� ������ ����������� ��������

			// ��������� ������� � ������ ���������� ������������� ������, �������� � typedlink:jbr.relatdocs@_CARDTYPE ��� _CARDTYPE
			Attribute finalAttr = null;
			Iterator<ObjectId> iterIds = item.getAttrIds().iterator();
			// ����� �� ���� ��������� (��� ����� ���������� - �����)

			final List<Card> finalCards = new ArrayList<Card>();		// ������������� ������ ��������
			final List<Card> prevFinalCards = new ArrayList<Card>();	// ������������� ������ �������� �� ���������� ��������
			finalCards.add(processCard);

			int finalCardsCount = 0;

			while (iterIds.hasNext()) {
				foundFarCards = new ArrayList<Card>();
				final ObjectId attrId = iterIds.next();
				if (iterIds.hasNext()){
//					finalCards.clear();
					// �������� �� ���� ��������� ��������� ������ ��� �������� �����
					for(Card analizeCard: finalCards){
						finalAttr = analizeCard.getAttributeById(attrId);
						if (finalAttr == null) {
							logger.error( MessageFormat.format(MSG_CARD_0_HAS_NO_ATTRIBUTE_1, analizeCard.getId(), attrId));
							throw new DataException(MessageFormat.format(MSG_CARD_0_HAS_NO_ATTRIBUTE_1, analizeCard.getId(), attrId));
						};
						List<Card> foundCurCards = loadAllLinkedCardsByAttr(analizeCard.getId(), finalAttr);
						if (foundCurCards!=null)
							foundFarCards.addAll(foundCurCards);
					}
					if (foundFarCards.isEmpty() ) {
						logger.warn( MessageFormat.format(MSG_CARD_0_HAS_EMPTY_ATTR_1, finalCards.toString(), attrId));
					};
				}
				else {
					// ���� �� ���� ��������� �� link-�� ������������ ������ ��������, ��������� �����, ��� finalCards = �����
					// �� ������ ����� ��������� �������� ��������, ��������� �����
					if (finalCards.size()==0){
						finalCards.addAll(foundFarCards);
					}



					finalCardsCount = finalCards.size();
					// ���� ���������� �������������� �������� ������� � ���� �������� ��� �����
					if ("_CARDTYPE".equals(attrId.getId())&&(finalAttr!=null)&&(finalAttr instanceof TypedCardLinkAttribute)){
						// ��������� �� ��������, ������� �� ������������� ������������ ���� �����
						for (Card finalCard: prevFinalCards){
							final ObjectId finalCardType = ((TypedCardLinkAttribute)finalAttr).getCardType(finalCard.getId());
							if ((finalCardType==null)&&(emptyError)){
								throw new DataException(emptyMessage);
							}
							if ((finalCardType==null)&&(!emptyError)){
								logger.warn( MessageFormat.format(MSG_CARD_0_HAS_EMPTY_CARDTYPE, finalCard.toString(), finalAttr.getId()));
								continue;
							}

							result.add(finalCardType.getId().toString());
						}
						//TODO: ������� �� �������� � �������������� ������� ������������� ������� �������� � ���� ���������
					} else if ("_CARDTYPE".equals(attrId.getId())&&(finalAttr!=null)&&(finalAttr instanceof BackLinkAttribute)){
						// ��������� �� ��������, ������� �� ������������� ������������ ���� �����
						ObjectId temp = new ObjectId(TypedCardLinkAttribute.class, ((BackLinkAttribute)finalAttr).getLinkSource().getId().toString());

						for (Card finalCard: prevFinalCards){
							TypedCardLinkAttribute attr = (TypedCardLinkAttribute)finalCard.getAttributeById(temp);
							if (attr == null){	// ���� ������� �� ��� ������, �� ���������� �� ���� ����������, ������� ������ ��� �������
								Collection<ObjectId> linkSources = ((BackLinkAttribute)finalAttr).getLinkSources();
								if (linkSources!=null){
									for(ObjectId linkSourceId: linkSources){
										temp = new ObjectId(TypedCardLinkAttribute.class, linkSourceId.getId().toString());
										attr = (TypedCardLinkAttribute)finalCard.getAttributeById(temp);
										if (attr!=null)
											break;
									}
								}
							}
							if ((attr==null)&&(emptyError)){
								throw new DataException(emptyMessage);
							}
							if ((attr==null)&&(!emptyError)){
								logger.warn( MessageFormat.format(MSG_CARD_0_HAS_EMPTY_ATTR_1, finalCard.toString(), temp));
								continue;
							}
							final ObjectId finalCardType = attr.getCardType(processCard.getId());
							if ((finalCardType==null)&&(emptyError)){
								throw new DataException(emptyMessage);
							}
							if ((finalCardType==null)&&(!emptyError)){
								logger.warn( MessageFormat.format(MSG_CARD_0_HAS_EMPTY_CARDTYPE, finalCard.toString(), temp));
								continue;
							}

							result.add(finalCardType.getId().toString());
						}
					} else if ("_TEMPLATE".equals(attrId.getId())){
						// ��������� �� ��������, ������� �� ������������� ��������
						for (Card finalCard: finalCards){ // �������� ��������� �������� �������� - �� id-�����
							final ObjectId templateType = finalCard.getTemplate();
							result.add(templateType.getId().toString());
						}
					} else if ("_STATE".equals(attrId.getId())){
						// ��������� �� ��������, ������� �� ������������� ���������
						for (Card finalCard: finalCards){
							result.add(finalCard.getState().getId().toString());
						}
					} else {
						// ���������� �� ���� ��������� � �������� ���������
						for (Card finalCard: finalCards){
							finalAttr = finalCard.getAttributeById(attrId);
							// ���� ������� �� ����� ��� �� �������� � ��� ��� ������� ���� ���������� ����������, ���������� ���
							if ((finalAttr==null||finalAttr.getStringValue()==null)&&(emptyError)){
								throw new DataException(emptyMessage);
							}
							if ((finalAttr==null||finalAttr.getStringValue()==null)&&(!emptyError)){
								logger.warn( MessageFormat.format(MSG_CARD_0_HAS_EMPTY_ATTR_1, finalCard.toString(), attrId));
								continue;
							}

							String attrValue;
							// �������� �������� ���������, �� ������� � ���, ��� ��������� ������ �� ��������, ������� ����� ������������ ��������
							// �������� �� ���������� �������� ����������
							if (finalAttr instanceof DateAttribute) {
								attrValue = ((DateAttribute)finalAttr).getValue().toString();
							} else if (finalAttr instanceof StringAttribute||finalAttr instanceof TextAttribute) {
								attrValue = ((StringAttribute)finalAttr).getValue();
							} else if (finalAttr instanceof IntegerAttribute) {
								attrValue =  ((IntegerAttribute)finalAttr).getStringValue();
							} else if (finalAttr instanceof CardLinkAttribute) {
								Collection<ObjectId> values = ((CardLinkAttribute)finalAttr).getIdsLinked();
								if (checkValues(values, finalCard, attrId))
									attrValue = ((ObjectId)values.toArray()[0]).getId().toString();
								else
									continue;
							} else if (finalAttr instanceof ListAttribute) {
								Collection<ReferenceValue> values = ((ListAttribute)finalAttr).getReferenceValues();
								if (checkValues(values, finalCard, attrId))
									attrValue = ((ReferenceValue)values.toArray()[0]).getId().getId().toString();
								else
									continue;
							}
							else if (finalAttr instanceof TreeAttribute) {
								Collection<ReferenceValue> values = ((TreeAttribute)finalAttr).getValues();
								if (checkValues(values, finalCard, attrId))
									attrValue = ((ReferenceValue)values.toArray()[0]).getId().getId().toString();
								else
									continue;
							}
							else if (finalAttr instanceof PersonAttribute) {
								Collection values = ((PersonAttribute)finalAttr).getValues();
								if (checkValues(values, finalCard, attrId))
									attrValue = ((Person)values.toArray()[0]).getId().getId().toString();
								else
									continue;
							}
							else{
								throw new DataException( MessageFormat.format(MSG_ATTRIBUTE_NOT_SUPPORTED, attrId));
							}
							// ���� � �������� �� ������ ��������, � ��� ������ ���� ����������� ������, �� ���������� ����������
							if ((attrValue==null)&&(emptyError)){
								throw new DataException(emptyMessage);
							}
							// ���� ��������� � ������� �������� �� ����, �� ������ ������� � ���, � ��� ������� ���������
							if ((attrValue==null)&&(!emptyError)){
								logger.warn( MessageFormat.format(MSG_CARD_0_HAS_EMPTY_ATTR_1, finalCard.toString(), attrId));
								continue;
							}
							result.add(attrValue);
						}
					}
				}
				prevFinalCards.clear();
				prevFinalCards.addAll(finalCards);
				finalCards.clear();
				// ����� ���� ���, ������ �� ���� ��������� �� ����� ���������, ������ �������� ������ ��������� ��������
				if (foundFarCards != null)
					finalCards.addAll(foundFarCards);
			}
		}
		return result;
	}

	private boolean checkValues(Collection values, Card card, ObjectId attrId){
		if (values==null){
			logger.warn( MessageFormat.format(MSG_CARD_0_HAS_EMPTY_ATTR_1, card.toString(), attrId));
			return false;
		}
		if (values.size()>1){
			logger.warn( MessageFormat.format(MSG_ATTRIBUTE_MULTI_VALUE, attrId, card.toString()));
			return false;
		}
		return true;
	}
}
