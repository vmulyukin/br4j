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
package com.aplana.dbmi.support.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.action.ChangeState;

public class BatchChangeState extends ChangeState {
	private static final long serialVersionUID = 1L;

	private boolean isEditMode = false;
	private boolean unlockOnError;
	private boolean isLocked;
	private ObjectId attrToParent;
	private HashMap<String, String> messages=new HashMap<String, String>();
	private List<Card> openActiveCards=new ArrayList<Card>();
	private List<ObjectId> dublicates = new ArrayList<ObjectId>();
	private boolean haveReservationRequests = false;
	public static String ERROR_FLAG = "ERROR:";
	public Class<?> getResultType() {
		return null;
	}
	
	public boolean isEditMode() {
		return isEditMode;
	}

	public void setEditMode(boolean isEditMode) {
		this.isEditMode = isEditMode;
	}

	public boolean isUnlockOnError() {
		return unlockOnError;
	}

	public void setUnlockOnError(boolean unlockOnError) {
		this.unlockOnError = unlockOnError;
	}

	public boolean isLocked() {
		return isLocked;
	}

	public void setLocked(boolean isLocked) {
		this.isLocked = isLocked;
	}

	public void addMessage(String key, String message) {
		if (messages==null){
			messages = new HashMap<String, String>();
		}
		messages.put(key, message);
	}

	public String getMessage(String key) {
		if (messages==null){
			return null;
		}
		return messages.get(key);
	}

	public List<Card> getOpenActiveCards() {
		return openActiveCards;
	}

	public void setOpenActiveCards(List<Card> openActiveCards) {
		this.openActiveCards = openActiveCards;
	}

	public void addOpenActiveCards(List<Card> openActiveCards) {
		this.openActiveCards.addAll(openActiveCards);
	}

	public List<ObjectId> getDublicates() {
		return dublicates;
	}

	public void setDublicates(List<ObjectId> dublicates) {
		this.dublicates = dublicates;
	}

	public boolean isHaveReservationRequests() {
		return haveReservationRequests;
	}

	public void setHaveReservationRequests(boolean haveReservationRequests) {
		this.haveReservationRequests = haveReservationRequests;
	}

	public void setAttrToParent(ObjectId attrToParent) {
		this.attrToParent = attrToParent;
	}
	
	public ObjectId getAttrToParent() {
		return attrToParent;
	}
}
