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
/**
 * 
 */
package com.aplana.dbmi.jbr.processors.card.runcheck;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.aplana.dbmi.jbr.processors.ProcessCard;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.Validator;

/**
 * @author RAbdullin
 *
 */
public class ChkLinkedCardStates 
			extends ProcessCard 
			implements CardChecker, Validator
{

	private static final long serialVersionUID = 1L;

	private final static String PARAM_SRC_LINK_ATTR_ID = "srcLinkAttrId"; 
	private final static String PARAM_ALLOWED_STATES = "allowed_states"; 
	private final static String PARAM_DISABLED_STATES = "disabled_states"; 

	private Card curCard;

	/* (non-Javadoc)
	 * @see com.aplana.dbmi.service.impl.AsyncProcessor#doProcess()
	 */
	@Override
	public Object process() throws DataException 
	{
		if (curCard == null) {
			curCard = super.loadCardById( getCardId() );
			if (curCard == null)
				throw new CardCheckException( "general.null", new Object[] {"card"});
		}

		final String sId = getParameter(PARAM_SRC_LINK_ATTR_ID, null);
		final Set<ObjectId> enabled = IdUtils.makeStateIdsList( getParameter(PARAM_ALLOWED_STATES, null));
		final Set<ObjectId> disabled = IdUtils.makeStateIdsList( getParameter(PARAM_DISABLED_STATES, null));

		if (logger.isDebugEnabled()) {
			logger.debug(" Enabled states ["+ enabled + "],  disabled states ["+ disabled+ "]");
		}

		if (sId == null) {
			logger.warn( "Checking card "+ curCard.getId() + ": parameter \'"+ PARAM_SRC_LINK_ATTR_ID + "\'is not configured -> check is TRUE");
			return null;
		}
		final ObjectId attrId = IdUtils.smartMakeAttrId( sId, CardLinkAttribute.class); 
		final Attribute attr = curCard.getAttributeById(attrId);
		if (attrId == null)
		{
			logger.warn( MessageFormat.format( MSG_CARD_0_HAS_NO_ATTRIBUTE_1, curCard.getId(), attrId) + " -> check is TRUE");
			return null;
		}

		final List<Card> cards = super.loadAllLinkedCardsByAttr( curCard.getId(), attr);
		if (cards != null) {
			for (Card card : cards) {
				if (enabled != null && !enabled.contains(card.getState())) {
					logger.debug("Linked card "+ card.getId()+ " is not at enabled state: "+ card.getState()+ " -> check is FALSE");
					throw new CardCheckException( "jbr.processor.actiondenied");
				}
				if (disabled != null && disabled.contains(card.getState())) {
					logger.debug("Linked card "+ card.getId()+ " is at disabled state: "+ card.getState()+ " -> check is FALSE");
					throw new CardCheckException( "jbr.processor.actiondenied");
				}
			}
		}

		logger.debug( "Card "+ curCard.getId()+ " has all its linked via "+ attrId +" cards at correct states -> check is TRUE");
		return null;
	}

	/* (non-Javadoc)
	 * @see com.aplana.dbmi.jbr.processors.card.runcheck.CardChecker#checkCard()
	 */
	public void checkCard() throws CardCheckException {
		try {
			process();
		} catch (DataException ex) {
			throw new CardCheckException(ex);
		}
	}

	/* (non-Javadoc)
	 * @see com.aplana.dbmi.jbr.processors.card.runcheck.CardChecker#setCard(com.aplana.dbmi.model.Card)
	 */
	public void setCard(Card card) {
		this.curCard = card;
	}

	/* (non-Javadoc)
	 * @see com.aplana.dbmi.jbr.processors.card.runcheck.CardChecker#setParameters(java.util.Map)
	 */
	public void setParameters(Map<String, String> parameters)
			throws CardCheckException 
	{
		super.params.clear();
		if (parameters != null) {
			// params.putAll(parameters);   <- ����� ������ ����� ������������ 
			// ���������� getParameter-������, �.�. �� ����� �������� �������� 
			// �������� ��� ������, ��� ��� ��������� ������ ����� setParaneter ...
			for (Map.Entry<String, String> item : parameters.entrySet()) {
				setParameter(item.getKey(), item.getValue());
			}
		}
	}

	@Override
	public String toString(){
		return MessageFormat.format( "{0}=''{1}'', enabled states [{2}], disable states [{3}]", 
					PARAM_SRC_LINK_ATTR_ID, 
					getParameter( PARAM_SRC_LINK_ATTR_ID, null),
					getParameter( PARAM_ALLOWED_STATES, null), 
					getParameter( PARAM_DISABLED_STATES, null) 
				);
	}
}
