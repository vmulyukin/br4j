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
package com.aplana.dbmi.module.numerator;

import org.springframework.jdbc.core.JdbcTemplate;

import com.aplana.dbmi.action.Action;
import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.ObjectAction;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.numerator.action.SetRegistrationNumber;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.Database;
import com.aplana.dbmi.service.impl.DatabaseClient;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.Parametrized;
import com.aplana.dbmi.service.impl.ProcessorBase;
import com.aplana.dbmi.service.impl.UserData;

/**
 * Class, implementing cards' numeration on any event linked to the card.
 * * @author nzeltser
 */

public class CardNumeration 
	extends ProcessorBase 
	implements Parametrized, DatabaseClient 
{

	/**
	 * Defines template variables and delegates notification delivery to
	 * {@link NotificationBean} object, defined through <code>beanName</code> parameter.
	 * 
	 * @return Unchanged object or result of the action.
	 */
	public Person person = null;
	public Card card = null;
	public UserData sysUser = null;

	private ObjectId PARAM_NUMATTRID = null; 
	private ObjectId PARAM_DATEATTRID = null;
	private ObjectId PARAM_JOURNATTRID = null;
	public static final String NUMPART_INDEX = "index";
	private JdbcTemplate jdbc = null; 

	@Override
	public Object process() throws DataException 
	{
		card = getCard();		
		person = getSystemUser().getPerson();		
		
		if(PARAM_NUMATTRID == null)
			PARAM_NUMATTRID = ObjectId.predefined(StringAttribute.class, "regnumber");
		if(PARAM_DATEATTRID == null)
			PARAM_DATEATTRID = ObjectId.predefined(DateAttribute.class, "regdate");
		if(PARAM_JOURNATTRID == null)
			PARAM_JOURNATTRID = ObjectId.predefined(CardLinkAttribute.class, "regjournal");

		if (!isNeedSetRegistrationNumber(card)) {
			logger.info("Number already exists for card" + card.getId().toString());
			return true;
		}
		
		logger.info("Numeration started for card" + card.getId().toString()+ " for user " + person.getLogin());
		execAction(new LockObject(card));
		try {
			SetRegistrationNumber action = new SetRegistrationNumber();
			action.setCard(card);
			action.setPreliminary(false);
			action.setCheckMandatory(true);
			action.setNumAttrId(PARAM_NUMATTRID);
			action.setDateAttrId(PARAM_DATEATTRID);
			action.setJournalAttrId(PARAM_JOURNATTRID);
			action.setRegistrator(getUser().getPerson());
			ActionQueryBase query = getQueryFactory().getActionQuery(action);
			query.setAction(action);
			query.setJdbcTemplate(jdbc);
			query.setUser(getSystemUser());
			getDatabase().executeQuery(getSystemUser(), query);
		} finally {
			execAction(new UnlockObject(card));
		}

		return true;
	}
	
	protected boolean isNeedSetRegistrationNumber(Card card) {
		if (card.getAttributeById(PARAM_NUMATTRID) != null &&
				card.getAttributeById(PARAM_NUMATTRID).getStringValue() != null &&
				card.getAttributeById(PARAM_NUMATTRID).getStringValue().length() > 0) {
			return false;
		}
		else {
			return true;
		}
	}
	
	public void setJdbcTemplate(JdbcTemplate jdbcT){
		this.jdbc = jdbcT;
	}
	
	/**
	 * Sets up the value of object's parameter.
	 * 
	 * @param name Name of the parameter
	 * @param value Value of the parameter
	 * @throws IllegalArgumentException if parameter is unknown.
	 */
	public void setParameter(String name, String value) {
		
		if (name.equalsIgnoreCase("NUMATTRID"))
			PARAM_NUMATTRID = new ObjectId(StringAttribute.class, value);
		else if (name.equalsIgnoreCase("DATEATTRID"))
			PARAM_DATEATTRID = new ObjectId(DateAttribute.class, value);
		else if (name.equalsIgnoreCase("JOURNATTRID"))
			PARAM_JOURNATTRID = new ObjectId(CardLinkAttribute.class, value);
		/*else if (name.equalsIgnoreCase("unlockCountLim"))
			unlockCountLim = Integer.parseInt(value);
		else if (name.equalsIgnoreCase("unlockStep"))
			unlockStep = Integer.parseInt(value);
		*/
		else
			throw new IllegalArgumentException("Unknown parameter: " + name);
		
		//	throw new IllegalArgumentException("Unknown parameter: " + name);
	}
	
	protected Card getCard() {
	    Card acard = null;
	    // ������ CardNumeration ������������ ������ ��� ChangeState
	    if (getAction() instanceof ChangeState) {
	        acard = ((ChangeState) getAction()).getCard();
	    } else {
	        acard = (Card) getObject();
	    }
		if (acard == null){
			ObjectId id = ((ObjectAction) getAction()).getObjectId();
			acard = getCard(id);
		}
		return acard;
	}
	
	public Card getCard(ObjectId id){	
		try {
			ObjectQueryBase query = getQueryFactory().getFetchQuery(Card.class);
			query.setAccessChecker(null);
			query.setId(id);
			return (Card) getDatabase().executeQuery(getSystemUser(), query);
			
		} catch (DataException e) {
			logger.error("Error fetching card object " + id.toString(), e);
			return null;
		}
	}
	
    protected Object execAction(Action action, UserData user) throws DataException {
		ActionQueryBase queryU = getQueryFactory().getActionQuery(action);
		queryU.setAction(action);
		queryU.setAccessChecker(null);
		queryU.setUser(user);
		return getDatabase().executeQuery(user, queryU);
    }
    
    protected Object execAction(Action action) throws DataException {
 		return execAction(action, getSystemUser());
    }
}
