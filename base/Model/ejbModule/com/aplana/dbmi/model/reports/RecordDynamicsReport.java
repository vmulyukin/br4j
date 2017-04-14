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

import java.util.List;

public class RecordDynamicsReport extends Report {

	private static final long serialVersionUID = 1L;
	public static final int PERIOD_WEEK = 0;
	public static final int PERIOD_MONTH = 1;
	public static final int PERIOD_QUARTER = 2;
	public static final int PERIOD_YEAR = 3;
	
	private int reportPeriod = PERIOD_MONTH;

	private List periodTitle = null;

	private List direction = null;
	private List segmentation = null;
	private List operator = null;
	
	public List getDirection() {
		return direction;
	}
	public void setDirection(List direction) {
		this.direction = direction;
	}
	public List getOperator() {
		return operator;
	}
	public void setOperator(List operator) {
		this.operator = operator;
	}
	public List getPeriodTitle() {
		return periodTitle;
	}
	public void setPeriodTitle(List periodTitle) {
		this.periodTitle = periodTitle;
	}
	public int getReportPeriod() {
		return reportPeriod;
	}
	public void setReportPeriod(int reportPeriod) {
		this.reportPeriod = reportPeriod;
	}
	public List getSegmentation() {
		return segmentation;
	}
	public void setSegmentation(List segmentation) {
		this.segmentation = segmentation;
	}
	
	
}
