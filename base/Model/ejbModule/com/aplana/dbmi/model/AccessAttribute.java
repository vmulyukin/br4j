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

public class AccessAttribute extends AccessOperation {

	public static final String READ_ONLY = "R";
	public static final String HIDDEN = "H";
	public static final String MANDATORY = "M";
	
	public static final String OP_NAME_PREFIX = "access.attribute.";
	
	private static final long serialVersionUID = 1L;
	
	private String operation;
	private ObjectId template;
	private ObjectId status;
	private ObjectId attribute;
	private ObjectId move;

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		if (operation != null &&
				!READ_ONLY.equals(operation) &&
				!HIDDEN.equals(operation) &&
				!MANDATORY.equals(operation))
			throw new IllegalArgumentException("Unknown operation: " + operation);
		this.operation = operation;
	}

	public ObjectId getTemplate() {
		return template;
	}

	public void setTemplate(Template template) {
		//if (template != null && template.getId() == null)
		//	throw new IllegalArgumentException("Can't assign unsaved template");
		this.template = template == null ? null : template.getId();
	}
	
	public void setTemplate(ObjectId templateId) {
		if (templateId != null && !Template.class.equals(templateId.getType()))
			throw new IllegalArgumentException("Not a template ID");
		this.template = templateId;
	}

	public ObjectId getStatus() {
		return status;
	}

	public void setStatus(CardState status) {
		//if (status != null && status.getId() == null)
		//	throw new IllegalArgumentException("Can't assign unsaved card state");
		this.status = status == null ? null : status.getId();
	}
	
	public void setStatus(ObjectId statusId) {
		if (statusId != null && !CardState.class.equals(statusId.getType()))
			throw new IllegalArgumentException("Not a card state ID");
		this.status = statusId;
	}

	public ObjectId getAttribute() {
		return attribute;
	}

	public void setAttribute(Attribute attribute) {
		//if (attribute != null && attribute.getId() == null)
		//	throw new IllegalArgumentException("Can't assign unsaved attribute");
		this.attribute = attribute == null ? null : attribute.getId();
	}
	
	public void setAttribute(ObjectId attributeId) {
		if (attributeId != null && !Attribute.class.equals(attributeId.getType()))
			throw new IllegalArgumentException("Not an attribute ID");
		this.attribute = attributeId;
	}
	
	public ObjectId getMove() {
		return move;
	}
	
	public void setMove(WorkflowMove move) {
		this.move = move == null ? null : move.getId();
	}
	
	public void setMove(ObjectId moveId) {
		if (moveId != null && !WorkflowMove.class.equals(moveId.getType()))
			throw new IllegalArgumentException("Not a workflow move ID");
		this.move = moveId;
	}
	
	public String getOperationName() {
		return ContextProvider.getContext().getLocaleMessage(OP_NAME_PREFIX + operation);
	}
	
	public String getTemplateName() {
		return template == null ? ContextProvider.getContext().getLocaleMessage(EMPTY_KEY) :
			template.toString();
	}
	
	public String getStatusName() {
		return status == null ? ContextProvider.getContext().getLocaleMessage(EMPTY_KEY) :
			status.toString();
	}
	
	public String getAttributeName() {
		return attribute == null ? ContextProvider.getContext().getLocaleMessage(EMPTY_KEY) :
			attribute.toString();
	}
	
	public String getMoveName() {
		return move == null ? ContextProvider.getContext().getLocaleMessage(EMPTY_KEY) :
			move.toString();
	}

	public boolean equals(Object obj) {
		if (obj == null || !AccessAttribute.class.equals(obj.getClass()))
			return false;
		AccessAttribute other = (AccessAttribute) obj;
		return (operation == null ? other.operation == null : operation.equals(other.operation)) &&
				(template == null ? other.template == null : template.equals(other.template)) &&
				(status == null ? other.status == null : status.equals(other.status)) &&
				(attribute == null ? other.attribute == null : attribute.equals(other.attribute)); 
	}
}