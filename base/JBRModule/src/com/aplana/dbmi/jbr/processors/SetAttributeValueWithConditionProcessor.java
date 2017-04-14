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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.aplana.dbmi.jbr.util.AttributeSelector;
import com.aplana.dbmi.jbr.util.parser.BooleanParser;
import com.aplana.dbmi.jbr.util.parser.FactoryBooleanParser;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.util.AttrUtils;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.BasePropertySelector;

public class SetAttributeValueWithConditionProcessor extends
	SetAttributeValueProcessor {

    private static final long serialVersionUID = 1L;
    private static final String PARAM_ATTR_CONDITION = "attr_condition";
    private static final String PARAM_PATH_CONDITION = "path_condition";
    
    protected final List<BasePropertySelector> conditions = new ArrayList<BasePropertySelector>();
    protected final HashMap<String, String> pathCond = new HashMap<String, String>();
    protected final HashMap<AttributeSelector, String> nameConditions = new HashMap<AttributeSelector, String>();

    @Override
    public void setParameter(String name, String value) {
		if (name.startsWith(PARAM_ATTR_CONDITION)) {
		    try {
		    	final AttributeSelector selector = AttributeSelector.createSelector(value);
		    	
		    	this.conditions.add(selector);
		    	this.nameConditions.put(selector, name);
		    } catch (DataException ex) {
		    	ex.printStackTrace();
		    }
		} else if(name.startsWith(PARAM_PATH_CONDITION)) {
			final String[] desc = value.trim().split("=");
			if (desc.length > 1) { // ���� ������� �� �������, ���������� ��������
				String condName = desc[0].trim();
				String path = desc[1].trim();
				this.pathCond.put(condName, path);
			}
		} else
		    super.setParameter(name, value);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.aplana.dbmi.jbr.processors.SetAttributeValueProcessor#doProcess()
     */
    @Override
    public Object process() throws DataException
    {
		final ObjectId cardId = getCardId();
		if( cardId == null ) {
		    logger.warn( "Impossible to set value until card is saved. Exiting" );
		    return null;
		}
	
		if (!checkCardConditons(cardId)) {
		    logger.warn("Card " + cardId.getId()
			    + " did not satisfy coditions. Exiting");
		    return null;
		}
		if (!conditions.isEmpty())
		    logger.debug("Card " + cardId.getId() + " satisfies coditions");
		
		return super.process();
	}
	
	private boolean checkCardConditons( ObjectId cardId ) throws DataException {
		if( conditions == null || conditions.isEmpty() )
		    return true;
		final ObjectQueryBase cardQuery = getQueryFactory().getFetchQuery( Card.class );
		cardQuery.setAccessChecker( null );
		cardQuery.setId( cardId );
		final Card card = (Card) getDatabase().executeQuery( getSystemUser(), cardQuery );
		return checkConditions( conditions, card );
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
    private boolean checkConditions(List<BasePropertySelector> conds, Card card) throws DataException {
		if (conds == null || card == null)
		    return true;
		BooleanParser parser = FactoryBooleanParser.create(booleanExpression, FactoryBooleanParser.Parser.POLISH);	
		HashMap<String, Boolean> conditionMap = new HashMap<String, Boolean>();
		for (BasePropertySelector cond : conds) {
			String nameCondition = nameConditions.get(cond);
			Boolean isSatis = true;
			// ���� ��� ������� ����� ����, ��������� ������� �� �������� �������� ����� �� �������
			if (pathCond.containsKey(nameCondition)) {
				String path = pathCond.get(nameCondition);
				final String[] attrs = path.split("->");
				final LinkedList<ObjectId> attrList = new LinkedList<ObjectId>();
				for (int j=0; j < attrs.length; j++) {
					attrList.add( makeObjectId(attrs[j], CardLinkAttribute.class) );
				}
				if (!walkWithCondition(card, attrList, cond)) {
					logger.debug("Card " + card.getId().getId()
						+ " did not satisfy codition " + cond);
					//return false;
					isSatis = false;
			    }
			} else {
			    if (!cond.satisfies(card)) {
					logger.debug("Card " + card.getId().getId()
						+ " did not satisfy codition " + cond);
					//return false;
					isSatis = false;
			    }
			}
			conditionMap.put(nameCondition, isSatis);
		}
		
		if (conditionMap.isEmpty())
			return true;
		
		boolean resumeExpr = true;
		// ���� ��� ������� - ��������� ������ "�"
		if (null == parser) {
			for(boolean exp : conditionMap.values()) {
				if (!exp)
					resumeExpr = false;
					break;
			}
		} else {
			resumeExpr = parser.calculate(conditionMap);
		}
		return resumeExpr;
    }
    
    protected ObjectId makeObjectId(String typeCode, Class<?> defType) {
		Class<?> type;
		String code;
		final String[] desc = typeCode.trim().split(":");
		if (desc.length == 1) { // ��� ("xxx:") �� ������
			type = defType;
			code = desc[0].trim();
		} else {
			type = AttrUtils.getAttrClass(desc[0].trim());
			code = desc[1].trim();
		}
		return ObjectIdUtils.getObjectId(type, code, false);
	}
    
    protected boolean walkWithCondition(Card card, LinkedList<ObjectId> attrList, BasePropertySelector cond) throws DataException {
    	boolean isSatisfies = true;
    	final List<ObjectId> cardsEndLayer = calculateChildren(card, attrList);
    	// ��������� ������� �� ���� �������� ���������
    	isSatisfies = cardsEndLayer.isEmpty();
    	for(ObjectId cardId : cardsEndLayer) {
    		Card cardEndLayer = fetchCard(cardId);
    		// ���� ���� �� ���� �������� �������� �������� - ������� �������� �������� (�������� '���')
    		if (cond.satisfies(cardEndLayer))
    			isSatisfies = true;
    	}
    	return isSatisfies;
    }

}
