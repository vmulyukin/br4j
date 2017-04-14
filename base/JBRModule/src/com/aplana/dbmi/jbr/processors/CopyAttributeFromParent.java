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

import java.sql.Types;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.jdbc.core.JdbcTemplate;

import com.aplana.dbmi.action.ListProject;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.jbr.util.AttributeSelector;
import com.aplana.dbmi.jbr.util.CardUtils;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.IntegerAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.TreeAttribute;
import com.aplana.dbmi.model.TypedCardLinkAttribute;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.BasePropertySelector;
import com.aplana.dbmi.service.impl.QueryBase.QueryExecPhase;
import com.aplana.dbmi.service.impl.UserData;
import com.aplana.dbmi.service.impl.access.AccessRuleManager;
import com.aplana.dbmi.service.impl.query.AttributeTypes;

/**
 * @comment RAbdullin
 * � ������� �������� ������� �� ���������� backlink � ������������(��) ��������(��)
 * � ����������� ������ ��������� �������� � ���� ��� ������� ������.
 * � (2010/05/21, RuSA): �������� �������� ����� ���������� ��� ��������� ������-����.
 * � (2012/01/28, Gorik): �������� ����������� ������������ �������� �� ��������, � �������� ���������� �� ��������� � ������������ ��������
 * � (2012/11/29, Gorik): �������� ��������� ��������� ������ ��� ��������� ������������� ��������
 * � (2012/11/29, Gorik): �������� ����� �������� �� �������� ������������ �� PersonAttribute
 * � (2012/11/29, Gorik): �������� ��������� ��������, � �� ������ ��������
 * � (2013/11/22, Gorik): �������� ��������� ������ ������ ������, �������� �� (������� ��� ������ � ���-���� ���������� ������� ��������)
 * � (2013/11/26, Gorik): ��������� ����� ���������� �������� � ��������, ������� ������ ������� (� ������� ��� id) 
 * c (2014/02/19, Akhasanov): �������� �������������� ������� ������� ������� �������� ����� ������������, ��� ������� ��� � writeOperation ������������ preclear
 * 							(������� �������� ����� ������������� � ����� ������, ���� ��� ���������� �������� ��������� �� �������� ����������� �����������)
 */
public class CopyAttributeFromParent
	extends BaseCopyAttributeProcessor
	// implements Parametrized
{
	private static final long serialVersionUID = 2L;

	static final String PARAM_PARENT = "parent";
	private static final String PARAM_LINK_TYPES = "linkTypes";
	private static final String PARAM_ATTR_CONDITION = "attr_condition";
	private static final String PARAM_ATTR_PARENT_TEMPLATE = "parentTemplate";
	private static final String PARAM_ATTR_SUB_PARENT_LINK = "subParentLink";
	private static final String PARAM_STATES = "affectedStates";
	private static final String PARAM_PRE_CLEAR = "preclear";
	private static final String PARAM_APPEND = "append";

	protected final List<BasePropertySelector> conditions = new ArrayList<BasePropertySelector>();
	private String parentKeyOrCode;
	private String attrcodeParent;
	private List<ObjectId> linkTypes;
	private String parentTemplate;
	private String subParentLink;
	private HashSet<ObjectId> sourceStateIds;
	
	//��������������� ������� ��������
	private boolean preclear = false;

	@Override
	public Object process() throws DataException {

		super.process();
		
		if(preclear && append) {
			logger.error("It is not allowed to use 'preclear' and 'append' together in 'writeOperation' parameter");
			throw new DataException("���������� ������������� preclear � append � ��������� writeOperation ���������� "+this.getClass().getSimpleName()+" �����������");
		}

		attrcodeParent = this.parentKeyOrCode;
		if (attrcodeParent == null) {
			logger.warn( "parent link is NULL -> exiting (if you want to copy attributes inside same card use another processor CopyAttributeToChildren in the same way)" );
			return null;
		}

		// = chkInitAttr( parentKeyOrCode, BackLinkAttribute.class);
		final ObjectId parentAttrId = IdUtils.smartMakeAttrId(attrcodeParent, BackLinkAttribute.class);
		attrcodeParent = (String) parentAttrId.getId();

		ObjectId parentTemplateId = ObjectIdUtils.getObjectId(Template.class, parentTemplate, false);

		final ObjectId cardId = getCardId();
		final Card card = getCard();
		if ((cardId == null&&!isOnlyModel)||card==null) {
			logger.warn("Impossible to copy attributes until card is saved -> exiting");
			return null;
		}
		Class<?> attrType;
		try {
			attrType = AttributeTypes.getAttributeClass(typeCh);
		} catch (IllegalArgumentException e) {
			attrType = Attribute.class;
		}
		final Attribute attrTo = card.getAttributeById(IdUtils.smartMakeAttrId(attrCodeOrKeyTo, attrType));
		
		// check main document
		if (!chkDocMain(card)) {
			logger.info( "Conditions on main doc are not satisfied -> exiting");
			return null;
		}
		
		boolean conditionStatus;
		//�������� ������� �������� �� ��������� �������� ���� ��������� � ���-���� ��� ���� cardId==null
		if (this.getCurExecPhase() == QueryExecPhase.PREPROCESS){
			conditionStatus = checkCardConditons(card);
		} else if (cardId==null){
			conditionStatus = checkCardConditons(card);
		} else {//� ��������� ������� ����������� ���������� �������� �� ��
			conditionStatus = checkCardConditons(cardId);
		}
				
		if(!conditionStatus){
			logger.warn("Card " + (cardId!=null?cardId.getId():card)
				    + " did not satisfies coditions. Exiting");
			    return null;
		}
		
		if (!conditions.isEmpty())
			if( logger.isDebugEnabled() )
				logger.debug("Card " + (cardId!=null?cardId.getId():card) + " satisfies coditions");

		// ���� ����� ������ ��������, �� ��� ����������� ������� ������� �������� � ����, "���" � ��� � �������
		if (sourceStateIds!=null&&sourceStateIds.size()>0&&!sourceStateIds.contains(card.getState())){
			if (logger.isWarnEnabled()){
				logger.warn( MessageFormat.format("Status of card {0}:{1} does not included in set: {3}", new Object[]{(cardId!=null?cardId.getId():card), card.getState().getId(), ObjectIdUtils.numericIdsToCommaDelimitedString(sourceStateIds)}));
			}
			return null;
		}
		
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
		int oldCount = -1;
		// ������� ������ ������ ������ �����, ����� ���� ��������� ��������� � �������� ��� ���� ��
		if (!append&&!isOnlyModel) {
			// ����� ��������� �������, ������� ������ �� ��� � ������� ����
			// ������� ������� ����, ����� � ���������� ���������� ��� ���������
			if (updateAccessList){
				cleanAccessListByAttributeAndCard(attrTo.getId(), cardId);
/*				int accessListCount = getJdbcTemplate().update(
						"DELETE FROM access_list ac " +
						"WHERE source_value_id in (SELECT attr_value_id FROM attribute_value "+
						"WHERE attribute_code=? AND card_id=?)",
						new Object[] { attrcodeTo, cardId.getId() },
						new int[] { Types.VARCHAR, Types.NUMERIC }
					);
				logger.info( accessListCount + " value(s) delete from access_list before delete they from attribute_value");*/
			}

			execAction(new LockObject(cardId));
			try {
				oldCount = getJdbcTemplate().update(
					"DELETE FROM attribute_value " +
					"WHERE attribute_code=? AND card_id=?",
					new Object[] { attrcodeTo, cardId.getId() },
					new int[] { Types.VARCHAR, Types.NUMERIC }
				);
			} finally {
				execAction(new UnlockObject(cardId));
			}
		}
		
		//������ �������� ���������� ������ ����� ������������ ��������
		List<Card> sourceCards = null;
		
		if (BackLinkAttribute.class.isAssignableFrom( parentAttrId.getType())&&cardId!=null ) {
			/* DONE: (2010/08/24 RuSA) ���� ����� ������������ "������" ��������� ��� ��������� �������� �� Back-link (getProject)
			 * ������ ����������� (� �� ������ ����������) inline:
					"\t\t	AND v.card_id IN \n" +
					"\t\t\t	( \n" +
					"\t\t\t		SELECT p.card_id FROM attribute_value p \n" +
					"\t\t\t		WHERE p.number_value=? \n" +
					"\t\t\t			AND p.attribute_code IN \n" +
					"\t\t\t\t		(	SELECT o.option_value FROM attribute_option o \n" +
					"\t\t\t\t			WHERE o.option_code='LINK' AND o.attribute_code=? \n" +
					"\t\t\t\t		) \n" +
					"\t\t\t	)",
			 */

			sourceCards = execListProject( cardId,
					new ObjectId( BackLinkAttribute.class, attrcodeParent),
					getSystemUser());

			// ������� �������
			if(preclear && attrTo != null) {
				attrTo.clear();
			}
			
			if (sourceCards == null || sourceCards.isEmpty()){ 
				logger.warn("There are 0 link cards, break process.");
				return null;
			}
			
			for(Card c : sourceCards) {
				if(c.getTemplate() == null) {
					c = loadCardById(c.getId());
				}
				if (parentTemplateId != null && parentTemplateId.getId() != null) {
	    			if (c.getTemplate()!=null && !c.getTemplate().equals(parentTemplateId)){
	    				logger.warn( "Parent card "+ c.getId() + " is not equal for input template " + parentTemplateId);
	    				return null;
	    			}
	    		}
			}
			// ������� ������� ����, ����� � ���������� ���������� ��� ���������
			if (updateAccessList&&!isOnlyModel){
				cleanAccessList(cardId);
			}

			if (subParentLink!=null&&subParentLink.length()!=0){
				List<Card> sourceCardsTemp = new ArrayList<Card>();
				ObjectId subParentLinkId = IdUtils.smartMakeAttrId(subParentLink, CardLinkAttribute.class);
				if (BackLinkAttribute.class.isAssignableFrom( subParentLinkId.getType()) ) {
					for(Card docFrom : sourceCards) {
						sourceCardsTemp.addAll(execListProject( docFrom.getId(),
								new ObjectId( BackLinkAttribute.class, subParentLinkId),
								getSystemUser()));
					}
				} else {
					for(Card docFrom : sourceCards) {
					  	CardLinkAttribute subParentLinkAttrId = docFrom.getCardLinkAttributeById(subParentLinkId);
					  	if (subParentLinkAttrId==null){
							docFrom = loadCardById(docFrom.getId());
					  		subParentLinkAttrId = docFrom.getCardLinkAttributeById(subParentLinkId);
					  	}
						ObjectId[] allIds = (subParentLinkAttrId!=null)?
											subParentLinkAttrId.getIdsArray():null;
						
						for(ObjectId linkCardId : allIds) {
							if(linkCardId == null) {
								logger.warn( "For parent card "+ docFrom.getId() + " was not found linked cards");
								linkCardId = null;
							} else {
								logger.warn( "For parent card "+ docFrom.getId() + " was getting only first link card");
								sourceCardsTemp.add(loadCardById(linkCardId));
							}
						}
						
					}
				}
				sourceCards = sourceCardsTemp;
			}
			String cardIdsStr = getCardIdsByCommaDelimiter(sourceCards);
			
			if(cardIdsStr == null)
				cardIdsStr = "-1";
			
			logger.debug( "Back-linked (B) card for current "+ cardId + " is found as "+ cardIdsStr);

			if (!isOnlyModel){
				execAction(new LockObject(cardId));
				try {
					final int copiedCount = getJdbcTemplate().update(
						"INSERT INTO attribute_value \n" +
						"\t (	card_id, attribute_code, \n" +
						"\t		number_value, string_value, \n" +
						"\t 	date_value, value_id, \n" +
						"\t		another_value ) \n" +
						"\t SELECT \n" +
						"\t\t	?, ?, \n" +
						"\t\t	v.number_value, v.string_value, \n" +
						"\t\t	v.date_value, v.value_id, \n" +
						"\t\t	v.another_value \n " +
						"\t FROM attribute_value v \n" +
						"\t WHERE v.attribute_code=? \n" +
						"\t\t	AND v.card_id in ("+cardIdsStr+") \n" +
						/* ��������� �������� �� ������������ ��������*/
						"\t 	and not exists( \n"+
						"\t 					select 1 from attribute_value av \n"+
						"\t 					where	av.attribute_code = ? \n"+
						"\t 							and av.card_id = ? \n"+
						"\t 							and coalesce(av.number_value, -1) = coalesce(v.number_value, -1) \n"+
						"\t 							and coalesce(av.string_value, '') = coalesce(v.string_value, '') \n"+
						"\t 							and coalesce(av.date_value, '1970-01-01') = coalesce(v.date_value, '1970-01-01') \n"+
						"\t 							and coalesce(av.value_id, -1) = coalesce(v.value_id, -1) \n"+
						"\t 							and coalesce(av.another_value, '') = coalesce(v.another_value, '') \n"+
						"\t 	)"
						,
						new Object[] {
								cardId.getId(), 	attrcodeTo,
								attrcodeFrom,		attrcodeTo,
								cardId.getId()},
								new int[] {
								Types.NUMERIC,		Types.VARCHAR,
								Types.VARCHAR,		Types.VARCHAR,
								Types.NUMERIC}
					);
	
					logger.info( copiedCount + " value(s) of attribute '" + attrcodeFrom +
						"' of cards "+ cardIdsStr +" did replace " + oldCount +
						" old value(s) of '" + attrcodeTo + "' of card " + cardId.getId() );
					// ��������� ������� ����, ����� � ���������� ���������� ��� ���������
					if (updateAccessList){
						recalculateAccessList(cardId);
					}
				} finally {
					execAction(new UnlockObject(cardId));
				}
			}
			
		} else 	if (CardLinkAttribute.class.isAssignableFrom( parentAttrId.getType()) ) {

			logger.debug( "Linked (C/E) parent card for current "+ cardId + " is via '"+ parentAttrId+ "' ");

			Search search = new Search();
			search.setByCode(true);


			final CardLinkAttribute linkAttr = card.getCardLinkAttributeById(parentAttrId);
			boolean isTypedFiltered = linkTypes != null && linkAttr instanceof TypedCardLinkAttribute;

			if (linkAttr != null) {
				String linkedIds = linkAttr.getLinkedIds();
				if (isTypedFiltered) {
					ObjectId[] allIds = linkAttr.getIdsArray();
					if (allIds == null){
						allIds = new ObjectId[0];
					}
					List<ObjectId> filteredIds = new ArrayList<ObjectId>();
					for (ObjectId id : allIds) {
						ObjectId cardType = ((TypedCardLinkAttribute)linkAttr).getCardType(id);
						if (linkTypes.contains(cardType)) {
							filteredIds.add(id);
						}
					}
					linkedIds = ObjectIdUtils.numericIdsToCommaDelimitedString(filteredIds);
				}
				search.setWords(linkedIds);
			}

			search.setColumns(getColumns());
			final SearchResult res = (SearchResult)super.execAction(search);
			sourceCards = CardUtils.getCardsList(res);
			
			// ������� �������
			if(preclear && attrTo != null) {
				attrTo.clear();
			}
			
			if (sourceCards == null || sourceCards.isEmpty()){ 
				logger.warn("There are 0 link cards, break process.");
				return null;
			}
			
			for(Card c : sourceCards) {
				if(c.getTemplate() == null) {
					c = loadCardById(c.getId());
				}
				if (parentTemplateId != null && parentTemplateId.getId() != null) {
	    			if (c.getTemplate()!=null && !c.getTemplate().equals(parentTemplateId)){
	    				logger.warn( "Parent card "+ c.getId() + " is not equal for input template " + parentTemplateId);
	    				return null;
	    			}
	    		}
			}
			
			// ������� ������� ����, ����� � ���������� ���������� ��� ���������
			if (updateAccessList&&!isOnlyModel){
				cleanAccessList(cardId);
			}

			if (subParentLink!=null&&subParentLink.length()!=0){
				List<Card> sourceCardsTemp = new ArrayList<Card>();
				ObjectId subParentLinkId = IdUtils.smartMakeAttrId(subParentLink, CardLinkAttribute.class);
				if (BackLinkAttribute.class.isAssignableFrom( subParentLinkId.getType()) ) {
					for(Card docFrom : sourceCards) {
						sourceCardsTemp.addAll(execListProject( docFrom.getId(),
								new ObjectId( BackLinkAttribute.class, subParentLinkId),
								getSystemUser()));
					}
				} else {
					for(Card docFrom : sourceCards) {
					  	CardLinkAttribute subParentLinkAttrId = docFrom.getCardLinkAttributeById(subParentLinkId);
					  	if (subParentLinkAttrId==null){
							docFrom = loadCardById(docFrom.getId());
					  		subParentLinkAttrId = docFrom.getCardLinkAttributeById(subParentLinkId);
					  	}
						ObjectId[] allIds = (subParentLinkAttrId!=null)?
											subParentLinkAttrId.getIdsArray():null;
						for(ObjectId linkCardId : allIds) {
							if(linkCardId == null) {
								logger.warn( "For parent card "+ docFrom.getId() + " was not found linked cards");
								linkCardId = null;
							} else {
								logger.warn( "For parent card "+ docFrom.getId() + " was getting only first link card");
								sourceCardsTemp.add(loadCardById(linkCardId));
							}
						}
					}
				}
				sourceCards = sourceCardsTemp;
			}

			String cardIdsStr = getCardIdsByCommaDelimiter(sourceCards);
			
			if(cardIdsStr == null)
				cardIdsStr = "-1";

			if (!isOnlyModel){
				if (subParentLink==null||subParentLink.length()==0){
	
					String typeIds = null;
					if (isTypedFiltered){
						typeIds = ObjectIdUtils.numericIdsToCommaDelimitedString(linkTypes);
						if (typeIds == null || "".equals(typeIds)) {
							typeIds = "0";
						}
					}
	
					execAction(new LockObject(cardId));
					try {
						final int copiedCount = getJdbcTemplate().update(
							"INSERT INTO attribute_value \n" +
							"\t (	card_id, attribute_code, \n" +
							"\t		number_value, string_value, \n" +
							"\t 	date_value, value_id, \n" +
							"\t		another_value ) \n" +
							"\t SELECT \n" +
							"\t\t	vcur.card_id, ?, \n" + // attrTo
							"\t\t	vlink.number_value, vlink.string_value, \n" +
							"\t\t	vlink.date_value, vlink.value_id, \n" +
							"\t\t	vlink.another_value \n " +
							"\t FROM attribute_value vcur \n" +
							"\t\t JOIN attribute_value vlink \n" +
							"\t\t	on vlink.card_id = vcur.number_value \n" +
							"\t\t	and vlink.attribute_code=? \n" + // attr_from
							"\t WHERE \n" +
							"\t\t	vcur.card_id = ? \n" + // current card id
							"\t\t	and vcur.attribute_code=? \n" +// attr_parent
							(isTypedFiltered ? String.format("\t\t	and vcur.value_id in (%s)\n", typeIds) : "") +
							/* ��������� �������� �� ������������ ��������*/
							"\t 	and not exists( \n"+
							"\t 					select 1 from attribute_value av \n"+
							"\t 					where	av.attribute_code = ? \n"+
							"\t 							and av.card_id = vcur.card_id \n"+
							"\t 							and coalesce(av.number_value, -1) = coalesce(vlink.number_value, -1) \n"+
							"\t 							and coalesce(av.string_value, '') = coalesce(vlink.string_value, '') \n"+
							"\t 							and coalesce(av.date_value, '1970-01-01') = coalesce(vlink.date_value, '1970-01-01') \n"+
							"\t 							and coalesce(av.value_id, -1) = coalesce(vlink.value_id, -1) \n"+
							"\t 							and coalesce(av.another_value, '') = coalesce(vlink.another_value, '') \n"+
							"\t 	)"
							,
							new Object[] {
									attrcodeTo,			attrcodeFrom,
									cardId.getId(),		attrcodeParent,
									attrcodeTo},
									new int[] {
									Types.VARCHAR,		Types.VARCHAR,
									Types.NUMERIC,		Types.VARCHAR,
									Types.VARCHAR}
						);
	
						logger.info( copiedCount + " value(s) of attribute '" + attrcodeFrom +
							"' of linked via '"+ attrcodeParent +"' did replace " + oldCount +
							" old value(s) of '" + attrcodeTo + "' of card " + cardId.getId() );
						// ��������� ������� ����, ����� � ���������� ���������� ��� ���������
						if (updateAccessList){
							recalculateAccessList(cardId);
						}
					} finally {
						execAction(new UnlockObject(cardId));
					}
				} else {
					execAction(new LockObject(cardId));
					try {
						final int copiedCount = getJdbcTemplate().update(
							"INSERT INTO attribute_value \n" +
							"\t (	card_id, attribute_code, \n" +
							"\t		number_value, string_value, \n" +
							"\t 	date_value, value_id, \n" +
							"\t		another_value ) \n" +
							"\t SELECT \n" +
							"\t\t	?, ?, \n" +
							"\t\t	v.number_value, v.string_value, \n" +
							"\t\t	v.date_value, v.value_id, \n" +
							"\t\t	v.another_value \n " +
							"\t FROM attribute_value v \n" +
							"\t WHERE v.attribute_code=? \n" +
							"\t\t	AND v.card_id in ("+cardIdsStr+") \n" +
							/* ��������� �������� �� ������������ ��������*/
							"\t 	and not exists( \n"+
							"\t 					select 1 from attribute_value av \n"+
							"\t 					where	av.attribute_code = ? \n"+
							"\t 							and av.card_id = ? \n"+
							"\t 							and coalesce(av.number_value, -1) = coalesce(v.number_value, -1) \n"+
							"\t 							and coalesce(av.string_value, '') = coalesce(v.string_value, '') \n"+
							"\t 							and coalesce(av.date_value, '1970-01-01') = coalesce(v.date_value, '1970-01-01') \n"+
							"\t 							and coalesce(av.value_id, -1) = coalesce(v.value_id, -1) \n"+
							"\t 							and coalesce(av.another_value, '') = coalesce(v.another_value, '') \n"+
							"\t 	)"
							,
							new Object[] {
									cardId.getId(), 	attrcodeTo,
									attrcodeFrom,		attrcodeTo,
									cardId.getId()},
									new int[] {
									Types.NUMERIC,		Types.VARCHAR,
									Types.VARCHAR,		Types.VARCHAR,
									Types.NUMERIC}
						);
	
						logger.info( copiedCount + " value(s) of attribute '" + attrcodeFrom +
							"' of cards "+ cardIdsStr +" did replace " + oldCount +
							" old value(s) of '" + attrcodeTo + "' of card " + cardId.getId() );
						// ��������� ������� ����, ����� � ���������� ���������� ��� ���������
						if (updateAccessList){
							recalculateAccessList(cardId);
						}
					} finally {
						execAction(new UnlockObject(cardId));
					}
				}
			}
		} else if (PersonAttribute.class.isAssignableFrom( parentAttrId.getType()) ) {

			logger.debug( "Person (U) parent card for current "+ cardId + " is via '"+ parentAttrId+ "' ");
			PersonAttribute parentAttr = (PersonAttribute)card.getAttributeById(parentAttrId); 
			final Set<ObjectId> cardIds = super.getCardIdsList( parentAttr);
			boolean isTypedFiltered = false;
			
			Search search = new Search();
			search.setByCode(true);

			String linkedIds = ObjectIdUtils.numericIdsToCommaDelimitedString(cardIds);
			search.setWords(linkedIds);
			search.setColumns(getColumns());

			final SearchResult res = (SearchResult)super.execAction(search);
			
			sourceCards = CardUtils.getCardsList(res);
			
			// ������� �������
			if(preclear && attrTo != null) {
				attrTo.clear();
			}
			
			if (sourceCards == null || sourceCards.isEmpty()){ 
				logger.warn("There are 0 link cards, break process.");
				return null;
			}
			
			for(Card c : sourceCards) {
				if(c.getTemplate() == null) {
					c = loadCardById(c.getId());
				}
				if (parentTemplateId != null && parentTemplateId.getId() != null) {
	    			if (c.getTemplate()!=null && !c.getTemplate().equals(parentTemplateId)){
	    				logger.warn( "Parent card "+ c.getId() + " is not equal for input template " + parentTemplateId);
	    				return null;
	    			}
	    		}
			}

			// ������� ������� ����, ����� � ���������� ���������� ��� ���������
			if (updateAccessList&&!isOnlyModel){
				cleanAccessList(cardId);
			}
			
			//this.getCurExecPhase()

			if (!isOnlyModel){
				
				String cardIdsStr = getCardIdsByCommaDelimiter(sourceCards);
				
				if(cardIdsStr == null)
					cardIdsStr = "-1";
	
					execAction(new LockObject(cardId));
					try {
						// �������� �������� �� �������� ������� ������������, ���������� � PersonAttribute
						final int copiedCount = getJdbcTemplate().update(
							"INSERT INTO attribute_value \n" +
							"\t (	card_id, attribute_code, \n" +
							"\t		number_value, string_value, \n" +
							"\t 	date_value, value_id, \n" +
							"\t		another_value ) \n" +
							"\t SELECT \n" +
							"\t\t	?, ?, \n" +
							"\t\t	v.number_value, v.string_value, \n" +
							"\t\t	v.date_value, v.value_id, \n" +
							"\t\t	v.another_value \n " +
							"\t FROM attribute_value v \n" +
							"\t WHERE v.attribute_code=? \n" +
							"\t\t	AND v.card_id in ("+cardIdsStr+") \n" +
							/* ��������� �������� �� ������������ ��������*/
							"\t 	and not exists( \n"+
							"\t 					select 1 from attribute_value av \n"+
							"\t 					where	av.attribute_code = ? \n"+
							"\t 							and av.card_id = ? \n"+
							"\t 							and coalesce(av.number_value, -1) = coalesce(v.number_value, -1) \n"+
							"\t 							and coalesce(av.string_value, '') = coalesce(v.string_value, '') \n"+
							"\t 							and coalesce(av.date_value, '1970-01-01') = coalesce(v.date_value, '1970-01-01') \n"+
							"\t 							and coalesce(av.value_id, -1) = coalesce(v.value_id, -1) \n"+
							"\t 							and coalesce(av.another_value, '') = coalesce(v.another_value, '') \n"+
							"\t 	)"
							,
							new Object[] {
									cardId.getId(), 	attrcodeTo,
									attrcodeFrom,		attrcodeTo,
									cardId.getId()},
									new int[] {
									Types.NUMERIC,		Types.VARCHAR,
									Types.VARCHAR,		Types.VARCHAR,
									Types.NUMERIC}
						);
	
						logger.info( copiedCount + " value(s) of attribute '" + attrcodeFrom +
							"' of cards "+ cardIdsStr +" did replace " + oldCount +
							" old value(s) of '" + attrcodeTo + "' of card " + cardId.getId() );
						// ��������� ������� ����, ����� � ���������� ���������� ��� ���������
						if (updateAccessList){
							recalculateAccessList(cardId);
						}
					} finally {
						execAction(new UnlockObject(cardId));
					}
				}
			} else {
			throw new DataException("factory.list", new Object[] {parentAttrId} );
		}
		// ������ ����� ���������� �� ��������� ������ ������
		if (sourceCards != null && !sourceCards.isEmpty() && card != null){
			final Attribute attrFrom  = sourceCards.get(0).getAttributeById(IdUtils.smartMakeAttrId(attrCodeOrKeyFrom, attrType));
			if (logger.isInfoEnabled())
				logger.info( MessageFormat.format(" Change attribute ''{0}''(value={1}) in card {2} by attribute ''{3}''(value={4})", attrCodeOrKeyTo.toString(), (attrTo!=null?attrTo.getStringValue():null), card.getId(), attrCodeOrKeyFrom.toString(), (attrFrom!=null?attrFrom.getStringValue():null)));
			
			if (attrFrom != null && attrTo != null){
		    	if (attrFrom instanceof StringAttribute && attrTo instanceof StringAttribute) {
		    		((StringAttribute)attrTo).setValue(((StringAttribute)attrFrom).getValue());
		    	}
		    	else if (!attrFrom.getType().equals(attrTo.getType())){
					logger.error("Attribute types are not equal. Exit.");
					return getResult();
				}
			    else if (attrFrom instanceof DateAttribute) {
					((DateAttribute)attrTo).setValue(((DateAttribute)attrFrom).getValue());
					((DateAttribute)attrTo).setTimePattern(((DateAttribute)attrFrom).getTimePattern());
				}
				else if (attrFrom instanceof IntegerAttribute) {
					((IntegerAttribute)attrTo).setValue(((IntegerAttribute)attrFrom).getValue());
				}
				else if (attrFrom instanceof CardLinkAttribute) {
					if(!append){
						((CardLinkAttribute)attrTo).clear();
					}
					if (!((CardLinkAttribute)attrFrom).isEmpty()){
						for(Card source : sourceCards) {
							final Attribute srcAttr  = source.getAttributeById(IdUtils.smartMakeAttrId(attrCodeOrKeyFrom, attrType));
							if (srcAttr != null)
								((CardLinkAttribute)attrTo).addIdsLinked(((CardLinkAttribute)srcAttr).getIdsLinked());
						}
					}
					if (attrFrom instanceof TypedCardLinkAttribute) {
						if (((TypedCardLinkAttribute)attrFrom).getReferenceValues().isEmpty())
							((TypedCardLinkAttribute)attrTo).setReferenceValues(new ArrayList<ReferenceValue>());
						else
							((TypedCardLinkAttribute)attrTo)
								.setReferenceValues(((TypedCardLinkAttribute)attrFrom).getReferenceValues());
					}
				}
				else if (attrFrom instanceof ListAttribute) {
					((ListAttribute)attrTo).setValue(((ListAttribute)attrFrom).getValue());
				}
				else if (attrFrom instanceof TreeAttribute) {
					((TreeAttribute)attrTo).setValues(((TreeAttribute)attrFrom).getValues());
				}
				else if (attrFrom instanceof PersonAttribute) {
					if(!append){
						((PersonAttribute)attrTo).clear();
					}
					if (!((PersonAttribute)attrFrom).isEmpty()){
						for(Card source : sourceCards) {
							final Attribute srcAttr  = source.getAttributeById(IdUtils.smartMakeAttrId(attrCodeOrKeyFrom, attrType));
							if (srcAttr != null)
								((PersonAttribute)attrTo).setValues(((PersonAttribute)srcAttr).getValues());
						}
					}	
				}
			}
		}
		return getResult();
	}
	
	private String getCardIdsByCommaDelimiter(List<Card> cards) {
		if(cards==null)
			return null;
		if(cards.isEmpty())
			return null;
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<cards.size(); i++) {
			sb.append(cards.get(i).getId().getId());
			if(i!=cards.size()-1)
				sb.append(",");
		}
		return sb.toString();
	}

	@Override
	public void setParameter(String name, String value) {
		if (PARAM_PARENT.equalsIgnoreCase(name))
			this.parentKeyOrCode = value;
		else if (PARAM_LINK_TYPES.equalsIgnoreCase(name)){
			linkTypes = ObjectIdUtils.commaDelimitedStringToNumericIds(value, ReferenceValue.class);
		} else if (name.startsWith(PARAM_ATTR_CONDITION)) {
			try {
				final AttributeSelector selector = AttributeSelector
					.createSelector(value);
				this.conditions.add(selector);
			} catch (DataException ex) {
				ex.printStackTrace();
			}
		} else if (name.equalsIgnoreCase(PARAM_ATTR_PARENT_TEMPLATE)) {
			this.parentTemplate = value.trim();
		} else if (name.equalsIgnoreCase(PARAM_ATTR_SUB_PARENT_LINK)) {
			this.subParentLink = value.trim();
		} else if(name.equalsIgnoreCase(PARAM_WRITE_OPERATION) && value.toLowerCase().indexOf(PARAM_PRE_CLEAR) != -1) {
			this.preclear = true;
			if(value.toLowerCase().indexOf(PARAM_APPEND) != -1) {
				this.append = true;
			} else this.append = false;
		} else if (PARAM_STATES.equalsIgnoreCase(name)) {
			sourceStateIds = new HashSet<ObjectId>();
			String[] states = value.split(",");
			for (int i = 0; i < states.length; i++) {
				ObjectId stateId = ObjectId.predefined(CardState.class, states[i].trim());
				if (stateId == null)
					try {
						stateId = new ObjectId(CardState.class, Long.parseLong(states[i]));
					} catch (NumberFormatException e) {
						throw new IllegalArgumentException(states[i] +
								" is neither predefined nor physical card state id");
					}
				sourceStateIds.add(stateId);
			}
		} else
			super.setParameter(name, value);
	}


	List<Card> execListProject( ObjectId cardId, ObjectId backLinkAttrId,
			UserData user) throws DataException
	{
		final ListProject action = new ListProject();
		action.setCard( cardId);
		action.setAttribute( backLinkAttrId);
		action.setColumns(getColumns());

		final SearchResult rs = (SearchResult) super.execAction(action, user);
		final List<Card> cards = CardUtils.getCardsList(rs);
		return (cards == null || cards.isEmpty()) ? null : cards;
	}


	/**
	 * ��������� �������� �������� � ������ ��������� (����� ���� � ��� ��
	 * ��������� ��� ��������� � ������ ���� ���������:
	 * Store/ChangeState, pre/post.
	 * @return �������� �������� ��� null (���� �� ���������)
	 */
/*	Card getCard()	{
		Card acard = null;
		if (getObject() instanceof Card) {
			acard = (Card) getObject();
		} else if (getAction() instanceof ChangeState) {
			acard = ((ChangeState) getAction()).getCard();
		}
		return acard;
	}
*/
	List<SearchResult.Column> getColumns()	{
		final List<SearchResult.Column> columns = new ArrayList<SearchResult.Column>();
		columns.add(CardUtils.createColumn(IdUtils.smartMakeAttrId(attrcodeFrom, Attribute.class)));
		columns.add(CardUtils.createColumn(IdUtils.smartMakeAttrId("NAME", StringAttribute.class)));
		columns.add(CardUtils.createColumn(Card.ATTR_TEMPLATE));
		if (subParentLink!=null&&subParentLink.length()!=0){
			columns.add(CardUtils.createColumn(IdUtils.smartMakeAttrId(subParentLink, CardLinkAttribute.class)));
		}
		return columns;
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
     * ��������� ��������� �� ������� conds ��� �������� card.
     *
     * @param conds
     * @param card
     * @return true, ���� ������� ��������� (� ��� ����� ���� �� ��� �����),
     *         false, �����.
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
}