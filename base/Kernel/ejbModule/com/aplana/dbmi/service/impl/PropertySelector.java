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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * {@link Asynchronous} {@link BasePropertySelector} implementation
 *
 */
public class PropertySelector extends BasePropertySelector implements Asynchronous, Serializable {

	private static final long serialVersionUID = 6426540171427026062L;

	public PropertySelector(String propName, String value) {
		super(propName, value);
	}

	protected Boolean async;
	protected String policy;
	protected Integer priority;

	@Override
	public Boolean isAsync() {
		return async;
	}

	@Override
	public void setAsync(Boolean async) {
		this.async = async;
	}

	@Override
	public String getPolicyName() {
		return policy;
	}
	
	@Override
	public void setPolicyName(String policy) {
		this.policy = policy;
	}
	
	private void writeObject(ObjectOutputStream oos) throws IOException {
		oos.defaultWriteObject();
		Object[] fields = new Object[4];
		fields[0] = propName;
		fields[1] = value;
		fields[2] = load;
		fields[3] = operEquals;
		oos.writeObject(fields);
	}
	
	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		ois.defaultReadObject();
		Object[] fields = (Object[])ois.readObject();
		propName = (String)fields[0];
		value = (String)fields[1];
		load = (Boolean)fields[2];
		operEquals = (Boolean)fields[3];
		
	}

	@Override
	public Integer getPriority() {
		return priority;
	}

	@Override
	public void setPriority(Integer i) {
		this.priority = i;
	}
}
