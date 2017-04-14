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
package com.aplana.dbmi.model.reports;

import java.util.Date;
import java.util.List;


public class StatisticReport extends Report {

	private static final long serialVersionUID = 1L;
	private int amount;
	private Date reportDate = null;
	private String lastUserName = null; 	
	
	// All list consist of SimpleRow
	private List segmentation = null;
	private List operator = null;
	private List direction = null;
	
	public List getDirection() {
		return direction;
	}
	public void setDirection(List direction) {
		this.direction = direction;
	}
	
	
	public int getAmount() {
		return amount;
	}
	public void setAmount(int amount) {
		this.amount = amount;
	}
	public String getLastUserName() {
		return lastUserName;
	}
	public void setLastUserName(String lastUserName) {
		this.lastUserName = lastUserName;
	}
	public List getOperator() {
		return operator;
	}
	public void setOperator(List operator) {
		this.operator = operator;
	}
	public Date getReportDate() {
		return reportDate;
	}
	public void setReportDate(Date reportDate) {
		this.reportDate = reportDate;
	}
	public List getSegmentation() {
		return segmentation;
	}
	public void setSegmentation(List segmentation) {
		this.segmentation = segmentation;
	}
		
	
}
