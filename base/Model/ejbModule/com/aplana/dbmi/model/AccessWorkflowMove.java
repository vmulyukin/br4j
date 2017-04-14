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

public class AccessWorkflowMove extends AccessOperation {

	private static final long serialVersionUID = 1L;
	
	private ObjectId template;
	private WorkflowMove move;

	public ObjectId getTemplate() {
		return template;
	}
	
	public void setTemplate(ObjectId templateId) {
		if (templateId != null && !Template.class.equals(templateId.getType()))
			throw new IllegalArgumentException("Not a template ID");
		this.template = templateId;
	}

	public void setTemplate(Template template) {
		//if (template != null && template.getId() == null)
		//	throw new IllegalArgumentException("Can't assign unsaved template");
		this.template = template == null ? null : template.getId();
	}
	
	public WorkflowMove getMove() {
		return move;
	}
	
	public void setMove(WorkflowMove move) {
		//if (move != null && move.getId() == null)
		//	throw new IllegalArgumentException("Can't assign unsaved workflow move");
		this.move = move;
	}
	
	public void setMove(ObjectId moveId) {
		if (moveId == null)
			this.move = null;
		else {
			if (!WorkflowMove.class.equals(moveId.getType()))
				throw new IllegalArgumentException("Not a workflow move ID");
			this.move = (WorkflowMove) WorkflowMove.createFromId(moveId);
		}
	}
	
	public ObjectId getStatus() {
		return move == null ? null : move.getFromState();
	}
	
	public String getTemplateName() {
		return template == null ? ContextProvider.getContext().getLocaleMessage(EMPTY_KEY) :
			template.toString();		// Shall nicely work with ObjectIdAndName
	}
	
	public String getMoveName() {
		return move == null ? ContextProvider.getContext().getLocaleMessage(EMPTY_KEY) :
			move.getMoveName();
	}

	public boolean equals(Object obj) {
		if (obj == null || !AccessWorkflowMove.class.equals(obj.getClass()))
			return false;
		AccessWorkflowMove other = (AccessWorkflowMove) obj;
		return (template == null ? other.template == null : template.equals(other.template)) &&
				(move == null ? other.move == null : move.equals(other.move));
	}
}
