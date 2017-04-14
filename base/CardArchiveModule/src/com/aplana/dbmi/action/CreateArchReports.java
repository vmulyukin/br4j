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

import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;

public class CreateArchReports implements Action
{

/**
 * reportName: ��� ������, �������� �����
 * jasperUrl: ���� � �������� ���������� (req.getSession().getServletContext().getResource("/jasper").toString();)
 */
	private static final long serialVersionUID = 8847892172059459982L;
	private ObjectId cardId;
	private String reportName;
	private String jasperUrl;

	/**
	 * Gets identifier of  {@link Card} object
	 * @return identifier of  {@link Card} object
	 */
	public ObjectId getCardId() {
		return cardId;
	}

	/**
	 * Sets identifier of  {@link Card} object
	 * @param cardId identifier of  {@link Card} object
	 */
	public void setCardId(ObjectId cardId) {
		this.cardId = cardId;
	}
	
	/**
	 * Gets identifier of  {@link Card} object
	 * @return identifier of  {@link Card} object
	 */
	public String getReportName() {
		return reportName;
	}

	/**
	 * Sets identifier of  {@link Card} object
	 * @param cardId identifier of  {@link Card} object
	 */
	public void setReportName(String reportName) {
		this.reportName = reportName;
	}
	
	public String getjasperUrl() {
		return jasperUrl;
	}

	/**
	 * Sets identifier of  {@link Card} object
	 * @param cardId identifier of  {@link Card} object
	 */
	public void setjasperUrl(String jasperUrl) {
		this.jasperUrl = jasperUrl;
	}
	

	public Class getResultType() {
		// TODO Auto-generated method stub
		return null;
	}
	
	

}
