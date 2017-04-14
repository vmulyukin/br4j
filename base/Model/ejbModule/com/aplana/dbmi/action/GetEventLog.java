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
package com.aplana.dbmi.action;

import java.util.Collection;
import java.util.Date;
import java.util.List;

public class GetEventLog implements Action {
	
	private static final long serialVersionUID = -2137982258158789315L;

	private String user;
	
	private Date fromDate;
	private Date toDate;
	
	private Boolean resultSuccess;
	
	private Boolean showMsg = Boolean.FALSE;
	
	private List<String> ignoredActions;
	
	/**
	 * @return the user
	 */
	public String getUser() {
		return user;
	}

	/**
	 * @param user the user to set
	 */
	public void setUser(String user) {
		this.user = user;
	}

	/**
	 * @return the fromDate
	 */
	public Date getFromDate() {
		return fromDate;
	}

	/**
	 * @param fromDate the fromDate to set
	 */
	public void setFromDate(Date fromDate) {
		this.fromDate = fromDate;
	}

	/**
	 * @return the toDate
	 */
	public Date getToDate() {
		return toDate;
	}

	/**
	 * @param toDate the toDate to set
	 */
	public void setToDate(Date toDate) {
		this.toDate = toDate;
	}

	/**
	 * @return the result
	 */
	public Boolean getResultSuccess() {
		return resultSuccess;
	}

	/**
	 * @param result the result to set
	 */
	public void setResultSuccess(Boolean resultSuccess) {
		this.resultSuccess = resultSuccess;
	}

	/**
	 * @return the showMsg
	 */
	public Boolean getShowMsg() {
		return showMsg;
	}

	/**
	 * @param showMsg the showMsg to set
	 */
	public void setShowMsg(Boolean showMsg) {
		this.showMsg = showMsg;
	}

	@Override
	public Class getResultType() {
		return Collection.class;
	}

	/**
	 * @return the ignoredActions
	 */
	public List<String> getIgnoredActions() {
		return ignoredActions;
	}

	/**
	 * @param ignoredActions the ignoredActions to set
	 */
	public void setIgnoredActions(List<String> ignoredActions) {
		this.ignoredActions = ignoredActions;
	}

}
