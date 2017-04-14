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
package com.aplana.dbmi.model;

public class AccessTemplate extends AccessOperation {

	public static final String CREATE_CARD = "C";
	
	public static final String OP_NAME_PREFIX = "access.template.";
	
	private static final long serialVersionUID = 1L;
	
	private String operation;
	private ObjectId template;
	
	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		if (operation != null &&
				!CREATE_CARD.equals(operation))
			throw new IllegalArgumentException("Unknown operation: " + operation);
		this.operation = operation;
	}

	public ObjectId getTemplate() {
		return template;
	}

	public void setTemplate(ObjectId templateId) {
		if (template != null && !Template.class.equals(template.getType()))
			throw new IllegalArgumentException("Not a template ID");
		this.template = templateId;
	}
	
	public void setTemplate(Template template) {
		this.template = template == null ? null : template.getId();
	}
	
	public String getOperationName() {
		return ContextProvider.getContext().getLocaleMessage(OP_NAME_PREFIX + operation);
	}
	
	public String getTemplateName() {
		return template == null ? ContextProvider.getContext().getLocaleMessage(EMPTY_KEY) :
			template.toString();		// Shall nicely work with ObjectIdAndName
	}

	public boolean equals(Object obj) {
		if (obj == null || !AccessTemplate.class.equals(obj.getClass()))
			return false;
		AccessTemplate other = (AccessTemplate) obj;
		return (operation == null ? other.operation == null : operation.equals(other.operation)) &&
				(template == null ? other.template == null : template.equals(other.template)) ;
	}
}
