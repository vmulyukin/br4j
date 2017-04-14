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
import java.util.List;

import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.ObjectAction;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.jbr.util.CardUtils;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.LockableObject;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.Database;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.Parametrized;
import com.aplana.dbmi.service.impl.ProcessorBase;
import com.aplana.dbmi.service.impl.UserData;
import com.aplana.dbmi.utils.StrUtils;

public class AssistantPassthrough extends ProcessorBase implements Parametrized
{
	public static final String PARAM_PERSON_ATTR = "personAttr";
	public static final String PARAM_MOVE_YES1 = "moveIfExists";
	public static final String PARAM_MOVE_YES2 = "moveYes";

	public static final String PARAM_MOVE_NO1 = "moveIfNotExists";
	public static final String PARAM_MOVE_NO2 = "moveNo";
	// public static final String PARAM_STATE = "state";

	public static final String PARAM_CALCMOVEONLY = "calcMoveOnly";

	private ObjectId moveId_ifYes;	// �������, ���� ���������� ����
	private ObjectId moveId_ifNo;	// �������, ���� �� ���
	private ObjectId personAttrId;
	// (def=false) �������� true, ���� ���� ������ �������� ������� �������!
	// ��������� ������� � ������� ���
	private boolean calcMoveOnly; 

	@Override
	public Object process() throws DataException 
	{
		if (personAttrId == null)
			throw new IllegalStateException( "Parameter " + PARAM_PERSON_ATTR + " should be specified");
		if ( moveId_ifYes == null && moveId_ifNo == null)
			throw new IllegalStateException( "Parameter " + PARAM_MOVE_YES1 + 
					" or " + PARAM_MOVE_NO1 + " should be specified");
					/*"Either " + PARAM_MOVE + " or " +
					PARAM_STATE + " parameter should be specified");*/

		final Card card = getCard();
		final PersonAttribute ownerAttr = (PersonAttribute) card.getAttributeById(personAttrId);
		if (ownerAttr == null) {
			logger.warn( "card "+ card.getId() + " has no attribute " + personAttrId+ " -> exiting");
			return null;
		}

		final boolean condition = !getAssistants(ownerAttr.getPerson()).isEmpty();
		final ObjectId dest_wfmID = (condition) ? this.moveId_ifYes : this.moveId_ifNo;
		logger.debug( (
				(condition) ? " assistents found " : " not found ") +
				" next step will be " + dest_wfmID
			);

		if (dest_wfmID == null)
			return null;

		final ChangeState move = (calcMoveOnly && getAction() != null) 
					? (ChangeState) getAction() // (!) ���� ����� �������� ������� Action
					: new ChangeState();
		move.setCard(card);
		move.setWorkflowMove(getMove(dest_wfmID));

		if (calcMoveOnly) {
			// ������ ���������� ��������...
			logger.info( "return only: workflowMove " + dest_wfmID);
			return move;
		}

		/*
		 * �������� �������
		 */
		logger.info( "performing workflowMove " + dest_wfmID);
		final ActionQueryBase query = getQueryFactory().getActionQuery(move);
		query.setAction(move);

		execAction(new LockObject(card));
		try {
			getDatabase().executeQuery(getSystemUser(), query);
		} finally {
			execAction(new UnlockObject(reloadCard()));
		}
		return null;
	}

	public void execAction(ObjectAction action) throws DataException {
		ActionQueryBase query = getQueryFactory().getActionQuery(action);
		query.setAction(action);
		getDatabase().executeQuery(getSystemUser(), query);
	}
	
	private WorkflowMove getMove(ObjectId moveId) {
		// TODO implement selecting of move by target state
		return (WorkflowMove) DataObject.createFromId(moveId);
	}

	private Card getCard() throws DataException {

		final ChangeState move = (ChangeState) getAction();
		final Card card = move.getCard();

		if (card.getAttributes() != null && card.getAttributeById(personAttrId) != null)
			// ������� ���� -> �����������, ��� �������� ��� ���������...
			return card;

		// ��������� ��������...
		return reloadCard();
	}

	/**
	 * Reloads {@link Card} from database.
	 * @return reloaded {@link Card}
	 * @throws DataException
	 */
	private Card reloadCard() throws DataException {
		final ChangeState move = (ChangeState) getAction();
		final ObjectQueryBase query = getQueryFactory().getFetchQuery(Card.class);
		query.setId(move.getCard().getId());
		return (Card) getDatabase().executeQuery(getSystemUser(), query);
	}

	private Collection<Person> getAssistants(Person person) throws DataException 
	{
		Collection<Person> assistants = new ArrayList<Person>();
		final List<Card> arm =
			CardUtils.getArmSettingsCardsByBoss(person, getQueryFactory(), getDatabase(), getSystemUser());

		if (arm == null || arm.isEmpty())
			return assistants;

		final Card card = arm.iterator().next();
		final Attribute aperson = card.getAttributeById(CardUtils.ATTR_ASSISTANT);
		if (aperson instanceof PersonAttribute)
			try {
				assistants.addAll( CardUtils.getAttrPersons((PersonAttribute) aperson));
			} catch (NullPointerException e) {	}
		
		return assistants;
	}

	public void setParameter(String name, String value) {
		if (PARAM_MOVE_YES1.equalsIgnoreCase(name) 
				|| PARAM_MOVE_YES2.equalsIgnoreCase(name)) {
			moveId_ifYes = ObjectIdUtils.getObjectId(WorkflowMove.class, value, true);
		} else if (PARAM_MOVE_NO1.equalsIgnoreCase(name)
					|| PARAM_MOVE_NO2.equalsIgnoreCase(name) ) {
			moveId_ifNo = ObjectIdUtils.getObjectId(WorkflowMove.class, value, true);
		} else if (PARAM_PERSON_ATTR.equals(name)) {
			personAttrId = ObjectIdUtils.getObjectId(PersonAttribute.class, value, false);
		} else if (PARAM_CALCMOVEONLY.equals(name)) {
			calcMoveOnly = StrUtils.stringToBool( value, false);
		} else {
			// throw new IllegalArgumentException("Unknown parameter: " + name);
			logger.warn("Ignored unlnown parameter '"+ name + "'='"+ value +"'");
		}
	}
}