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
package com.aplana.dbmi.model.filter.access;

import java.util.HashSet;
import java.util.Set;

import com.aplana.dbmi.model.AccessOperation;
import com.aplana.dbmi.model.AccessRule;
import com.aplana.dbmi.model.filter.Filter;

public class AccessRuleFilter implements Filter {

	private static final long serialVersionUID = 1L;
	
	private HashSet ruleTypes;
	private HashSet operationTypes;
	
	public Set getRuleTypes() {
		return ruleTypes;
	}
	
	public boolean needRuleType(Class type) {
		return ruleTypes == null || ruleTypes.contains(type);
	}
	
	public void addRuleType(Class type) {
		if (!AccessRule.class.isAssignableFrom(type))
			throw new IllegalArgumentException("Not a rule type");
		if (ruleTypes == null)
			ruleTypes = new HashSet();
		ruleTypes.add(type);
	}
	
	public Set getOperationTypes() {
		return operationTypes;
	}
	
	public boolean needOperationType(Class type) {
		return operationTypes == null || operationTypes.contains(type);
	}
	
	public void addOperationType(Class type) {
		if (!AccessOperation.class.isAssignableFrom(type))
			throw new IllegalArgumentException("Not an operation type");
		if (operationTypes == null)
			operationTypes = new HashSet();
		operationTypes.add(type);
	}
}
