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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RemoveParameters implements Action {
	private String task_id;
	private List<Long> paramIds = new ArrayList<Long>(); 
	
	public String getTask_id() {
		return task_id;
	}

	public void setTask_id(String taskId) {
		task_id = taskId;
	}

	public List<Long> getParamIds() {
		return paramIds;
	}

	public void setParamIds(List<Long> paramIds) {
		this.paramIds = paramIds;
	}

	public void addParamId(Long paramId) {
		this.paramIds.add(paramId);
	}

	@Override
	public Class getResultType() {
		// TODO Auto-generated method stub
		return Long.class;
	}
}
