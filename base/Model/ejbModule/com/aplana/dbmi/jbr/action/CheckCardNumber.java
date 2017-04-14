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
package com.aplana.dbmi.jbr.action;

import java.util.Date;
import java.util.List;

import com.aplana.dbmi.action.Action;
import com.aplana.dbmi.model.ObjectId;

public class CheckCardNumber implements Action {
	private static final long serialVersionUID = 4L;

	private ObjectId cardId;

	private String number;
	private boolean numberCanBeNullOrEmpty = false;

	private Date date;
	private boolean dateCanBeNull = false;

	private ObjectId organization;
	//    
	private boolean organizationCanBeNull = true;

	private ObjectId zoneDOW;
	
	private boolean zoneDOWCanBeNull = true;

	public ObjectId getZoneDOW() {
		return zoneDOW;
	}

	public void setZoneDOW(ObjectId zoneDOU) {
		this.zoneDOW = zoneDOU;
	}

	public ObjectId getCardId() {
		return cardId;
	}

	public void setCardId(ObjectId cardId) {
		this.cardId = cardId;
	}

	public Class<?> getResultType() {
		return String.class;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	/**
	 * @return true if the number field can have null value
	 */ 
	public boolean isNumberCanBeNullOrEmpty() {
		return this.numberCanBeNullOrEmpty;
	}

	/**
	 * @param numberCanBeNull set true to enable null values in field number
	 */
	public void setNumberCanBeNullOrEmpty(boolean value) {
		this.numberCanBeNullOrEmpty = value;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	/**
	 * @return true if the date can have null value
	 */
	public boolean isDateCanBeNull() {
		return this.dateCanBeNull;
	}

	/**
	 * @param dateCanBeNullOrEmpty set true to enable null values in date
	 */
	public void setDateCanBeNull(boolean value) {
		this.dateCanBeNull = value;
	}

	/**
	 * @return the organization
	 */
	public ObjectId getOrganization() {
		return this.organization;
	}

	/**
	 * @param organization the organization to set
	 */
	public void setOrganization(ObjectId organization) {
		this.organization = organization;
	}

	/**
	 * @return true if the organization can have null value 
	 */
	public boolean isOrganizationCanBeNull() {
		return this.organizationCanBeNull;
	}

	/**
	 * @param organizationCanBeNullOrEmpty set true to enable null values in organization
	 */
	public void setOrganizationCanBeNull(boolean value) {
		this.organizationCanBeNull = value;
	}

	public boolean isZoneDOWCanBeNull() {
		return zoneDOWCanBeNull;
	}

	public void setZoneDOWCanBeNull(boolean zoneDOWCanBeNull) {
		this.zoneDOWCanBeNull = zoneDOWCanBeNull;
	}

	/**
	 * ���������, ������� ������ ��������� ����������� ��������.
	 */
	public static class CheckCardNumberResult{

		private List<ObjectId> problemCardIds;

		public CheckCardNumberResult(){};

		public CheckCardNumberResult(List<ObjectId> cardIds){
			this.problemCardIds = cardIds;
		};

		public List<ObjectId> getProblemCardIds(){
			return problemCardIds;
		};

		public void setProblemCardIds(List<ObjectId> list){
			this.problemCardIds = list;
		};
	}
	
}
