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
package com.aplana.dbmi.service.impl;

import java.util.HashMap;
import java.util.Map;

import com.aplana.dbmi.utils.StrUtils;

public abstract class ParametrizedProcessor extends ProcessorBase implements Parametrized {
	private static final long serialVersionUID = 2L;
	
	private final Map<String, String> params = new HashMap<String, String>(5);
	
	public void setParameter(String name, String value) {
		params.put(name, value);
	}
	
	public String getParameter(String param) {
		return params.get(param);
	}
	
	public boolean getBooleanParameter(String param, boolean defaultValue) {
		return StrUtils.stringToBool( getParameter(param), defaultValue);
	}
	
	public Map<String, String> getParameters() {
		return this.params;
	}
}
