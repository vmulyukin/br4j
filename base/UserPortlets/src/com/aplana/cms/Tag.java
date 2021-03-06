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
package com.aplana.cms;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Tag
{
	private String name;
	private HashMap attributes = new HashMap();
	private String content;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getContent() {
		return content;
	}
	
	public void setContent(String content) {
		this.content = content;
	}

	public Map getAttributes() {
		return Collections.unmodifiableMap(attributes);
	}
	
	public String getAttribute(String name) {
		return (String) attributes.get(name);
	}
	
	public void addAttribute(String name, String value) {
		attributes.put(name, value);
	}
	
	public boolean hasAttribute(String name) {
		return attributes.containsKey(name);
	}
}
