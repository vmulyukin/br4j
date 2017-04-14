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

import java.io.Serializable;
import com.aplana.dbmi.service.DataException;

public class WorkflowMoveRequiredField extends DataObject{
	
	private static final long serialVersionUID = -4383545394087738399L;

	/**
	 * ��� �������� MustBeSetCode, ���-��� "������ ���� ������"
	 */
	public static final int MUSTBESET_BLANK = 0;
	
	/**
	 * ��� �������� MustBeSetCode, ���-��� "������ ���� ��������"
	 */
	public static final int MUSTBESET_ASSIGNED = 1; 
	
	/** 
	 * ��� �������� MustBeSetCode, ���-��� "�� ������������ MustBeSet", ���
	 * ����� �������� ������������ ��������� template::isMandatory.
	 */
	public static final int MUSTBESET_ASTEMPLATE = -1; 
	
    /**
     * Means skipping any validation
     */
    public static final int MUSTBESET_DONT_CHECK = 2;
	
	
	// private boolean required = false;

	private Long workflowMoveId; //part of composite key
	private Long templateAttributeId;//part of composite key
	private String attributeCode;
	private int mustBeSetCode = MUSTBESET_ASSIGNED;
	
	/**
	 * Always throws IllegalArgumentException to prevent from using this method on CardVersion instances
	 * @param id
	 */
	public void setId(long id) {
		throw new IllegalArgumentException("WorkflowMoveRequiredField has a composite key");
	}

	/**
	 * Sets CardVersion identifier
	 * @param id identifier of {@link Card} object
	 * @param version version number
	 */
	public void setId(long workflowMove, long templateAttribute) {
		super.setId(new ObjectId(getClass(), new CompositeId(workflowMove, templateAttribute)));
	}
	
//	/**
//	 * @return
//	 * @deprecated Use getMustBeSetCode() instead.
//	 */
//	public boolean isRequired() {
//		// return required;
//		return this.mustBeSetCode != MUSTBESET_BLANK;
//	}
//
//	/**
//	 * @return
//	 * @deprecated Use setMustBeSetCode(code) instead.
//	 */
//	public void setRequired(boolean required) {
//		// this.required = required;
//		this.mustBeSetCode = (required) ? MUSTBESET_ASSIGNED : MUSTBESET_BLANK;
//	}

	public Long getWorkflowMoveId() {
		return workflowMoveId;
	}

	public void setWorkflowMoveId(Long workflowMoveId) {
		this.workflowMoveId = workflowMoveId;
	}

	public Long getTemplateAttributeId() {
		return templateAttributeId;
	}

	public void setTemplateAttributeId(Long templateAttributeId) {
		this.templateAttributeId = templateAttributeId;
	}

	public String getAttributeCode() {
		return attributeCode;
	}

	public void setAttributeCode(String attributeCode) {
		this.attributeCode = attributeCode;
	}
	
	public int getMustBeSetCode()
	{
		return this.mustBeSetCode;
	}

	public void setMustBeSetCode(int newCode) throws DataException
	{
		if ( 	(newCode != MUSTBESET_ASSIGNED) 
			&& 	(newCode != MUSTBESET_BLANK) 
			&& 	(newCode != MUSTBESET_ASTEMPLATE) 
            && 	(newCode != MUSTBESET_DONT_CHECK)
			)
			throw new DataException( "store.workflow.move.mustbesetinv_1", 
						new Object[] { new Integer(newCode) });
		this.mustBeSetCode = newCode;
	}

	public String toString() {
		final StringBuffer sb 
			= new StringBuffer();
		
		sb.append("WorkflowMoveRequiredField{");
		
		sb.append("workflowMoveId:").append(workflowMoveId);

		sb.append(",");
		sb.append("templateAttributeId:").append(templateAttributeId);

		sb.append(",");
		sb.append("attributeCode:").append(attributeCode);

		sb.append(",");
		// sb.append("(required:").append( this.isRequired() ).append(")");

		sb.append(",");
		sb.append("must be set code:").append(mustBeSetCode);
		
		sb.append("}");

		return sb.toString();
	}

	public class CompositeId implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = -2644362682288724375L;
		private long workflowMove;
		private long templateAttribute;
		public CompositeId(long workflowMove, long templateAttribute) {
			this.workflowMove = workflowMove;
			this.templateAttribute = templateAttribute;
		}
		public long getWorkflowMove() {
			return workflowMove;
		}
		public void setWorkflowMove(long workflowMove) {
			this.workflowMove = workflowMove;
		}
		public long getTemplateAttribute() {
			return templateAttribute;
		}
		public void setTemplateAttribute(long templateAttribute) {
			this.templateAttribute = templateAttribute;
		}
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + (int) (templateAttribute ^ (templateAttribute >>> 32));
			result = prime * result + (int) (workflowMove ^ (workflowMove >>> 32));
			return result;
		}
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final CompositeId other = (CompositeId) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (templateAttribute != other.templateAttribute)
				return false;
			if (workflowMove != other.workflowMove)
				return false;
			return true;
		}
		private WorkflowMoveRequiredField getOuterType() {
			return WorkflowMoveRequiredField.this;
		}
		
		public String toString() {
			final StringBuffer sb= new StringBuffer();
			sb.append("workflowmove:").append(workflowMove);
			sb.append(",");
			sb.append("templateattribute:").append(templateAttribute);
			return sb.toString();
		}
	}
}
