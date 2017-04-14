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
import java.util.Map;
import java.util.List;

import com.aplana.dbmi.action.Action;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;

public class ProcessGroupExecution implements Action {
	private static final long serialVersionUID = 1L;
	
	
	private boolean onlyCopy = false;
	
	private List<ObjectId> reports;
	
	private Card currentReport;
	

	@Override
	public Class getResultType() {
		return Map.class;
	}


	public List<ObjectId> getReports() {
		return reports;
	}


	public ProcessGroupExecution setReports(List<ObjectId> reports) {
		this.reports = reports;
		return this;
	}

	public Card getCurrentReport() {
		return currentReport;
	}


	public ProcessGroupExecution setCurrentReport(Card currentReport) {
		this.currentReport = currentReport;
		return this;
	}


	public boolean isOnlyCopy() {
		return onlyCopy;
	}


	public ProcessGroupExecution setOnlyCopy(boolean onlyCopy) {
		this.onlyCopy = onlyCopy;
		return this;
	}


	
	

}
