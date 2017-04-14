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
package com.aplana.dbmi.numerator.action;

import com.aplana.dbmi.action.Action;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;

public class SetRegistrationNumber implements Action {
	private static final long serialVersionUID = 1L;
	private Card card;
	private ObjectId numAttrId; 
	private ObjectId numDigAttrId;
	private ObjectId dateAttrId;
	private ObjectId journalAttrId;
	private ObjectId registrarAttrId;
	private boolean preliminary = false;
	private boolean checkMandatory = true;
	private Person registrator = null;

	public Card getCard() {
		return card;
	}

	public void setCard(Card card) {
		this.card = card;
	}

	public ObjectId getNumAttrId() {
		return numAttrId;
	}
	
	public ObjectId getNumDigAttrId() {
		return numDigAttrId;
	}

	public void setNumAttrId(ObjectId numAttrId) {
		this.numAttrId = numAttrId;
	}

	public ObjectId getDateAttrId() {
		return dateAttrId;
	}

	public void setDateAttrId(ObjectId dateAttrId) {
		this.dateAttrId = dateAttrId;
	}

	public ObjectId getJournalAttrId() {
		return journalAttrId;
	}

	public void setJournalAttrId(ObjectId journalAttrId) {
		this.journalAttrId = journalAttrId;
	}

	public ObjectId getRegistrarAttrId() {
		return registrarAttrId;
	}

	public void setRegistrarAttrId(ObjectId registrarAttrId) {
		this.registrarAttrId = registrarAttrId;
	}

	public boolean isPreliminary() {
		return preliminary;
	}

	public void setPreliminary(boolean preliminary) {
		this.preliminary = preliminary;
	}

	public Class getResultType() {
		return String.class;
	}

	public void setCheckMandatory(boolean checkMandatory) {
		this.checkMandatory = checkMandatory;
	}

	public boolean isCheckMandatory() {
		return checkMandatory;
	}

	public Person getRegistrator() {
		return registrator;
	}

	public void setRegistrator(Person registrator) {
		this.registrator = registrator;
	}
	
}
