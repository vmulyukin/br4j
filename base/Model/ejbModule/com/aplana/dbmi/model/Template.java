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

import java.util.Collection;
import java.util.Collections;

/**
 * Template for {@link Card} creation. Represent one record of TEMPLATE table and it's child records
 * Defines set of {@link TemplateBlock attribute blocks} comprising card,
 * {@link Workflow workflow} of processing and other properties. 
 */
public class Template extends NamedLockableObject
{
    /**
	 * @deprecated Shall not be used anymore
	 */
	public static final ObjectId ID_REQUEST = ObjectId.predefined(Template.class, "request");// new ObjectId(Template.class, 3L);
	
	private static final long serialVersionUID = 5L;
	private boolean active;
	private boolean system;
	private Collection blocks;
	private ObjectId workflow;
	private boolean showInCreateCard;
	private boolean showInSearch;
	private Collection attributesViewParamDetails;
    private Collection workflowMoveRequiredFields;

	
    /**
     * Get a collection of {@link WorkflowMoveRequiredField}.
     * @return collection of {@link WorkflowMoveRequiredField}.
     */
	public Collection getWorkflowMoveRequiredFields() {
		return workflowMoveRequiredFields;
	}

	/**
	 * Set a collection of {@link WorkflowMoveRequiredField}.
	 * @param workflowMoveRequiredFields collection of {@link WorkflowMoveRequiredField}.
	 */
	public void setWorkflowMoveRequiredFields(Collection workflowMoveRequiredFields) {
		this.workflowMoveRequiredFields = workflowMoveRequiredFields;
	}

	/**
	 * Get a collection of of {@link AttributeViewParamDetail}.
	 * @return collection of {@link AttributeViewParamDetail}.
	 */
	public Collection getAttributesViewParamDetails() {
		return attributesViewParamDetails;
	}

	/**
	 * Set a collection of {@link AttributeViewParamDetail}
	 * @param attributesViewParamDetails collection of {@link AttributeViewParamDetail}
	 */
	public void setAttributesViewParamDetails(Collection attributesViewParamDetails) {
		this.attributesViewParamDetails = attributesViewParamDetails;
	}

	/**
	 * Sets identity of template
	 * @param id value of id
	 */
	public void setId(long id) {
		super.setId(new ObjectId(Template.class, id));
	}
	
/**
	 * Returns localized name of the template
	 * @return returns value of {@link #getNameRu} or {@link #getNameEn} properties depending of caller's locale preferences
	 */
	public String getName() {
		return name == null ? null : name.getValue();
	}
	/**
	 * Checks if given template is active. It is not possible to create new card by inactive template
	 * @return true is given template is active, false otherwise
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * Set isActive flag of template.
	 * @param active desired value of isActive flag
	 */
	public void setActive(boolean active) {
		this.active = active;
	}

	/**
	 * Returns isSystem flag of template object.
	 * @return value of isSystem flag
	 */
	public boolean isSystem() {
		return system;
	}

	/**
	 * Sets value of isSystem flag of template object
	 * @param system
	 */
	public void setSystem(boolean system) {
		this.system = system;
	}

	/**
	 * Returns collection of {@link TemplateBlock} objects, included in template.
	 * Could be used to get set of attributes comprises template
	 * @return collection of {@link TemplateBlock} objects, included in template
	 */
	public Collection getBlocks() {
		return blocks;
	}

	/**
	 * Sets collection of {link TemplateBlock} objects comprising template
	 * @param blocks collection of {link TemplateBlock} objects
	 */
	public void setBlocks(Collection blocks) {
		this.blocks = blocks;
	}
	
	/**
     * @deprecated What's that?!
	 * @return returns true if this.getId() equals {@link Template#ID_REQUEST }
	 */
	public boolean isMaterialSignificant() {
		return ID_REQUEST.equals(getId());
	}

	/**
	 * Gets identity of {@link Workflow} object associated with given template.
	 * Workflow defines available flow of processing for {@link Card cards} created by template.
	 * @return identity of {@link Workflow} object associated with given template
	 */
	public ObjectId getWorkflow() {
		return workflow;
	}

	/**
	 * Sets identity of {@link Workflow} object associated with given template. 
	 * @param workflow desired Workflow object identity
	 */
	public void setWorkflow(ObjectId workflow) {
		this.workflow = workflow;
	}

	/**
     * @deprecated Access rules no more linked to the template
	 * Gets access permissions defined for this {@link Template} instance
	 * @return collection of {@link CardAccess} items defining access permissions
	 */
	public Collection getCardAccess() {
		return Collections.emptySet();
	}

	/**
	 * @deprecated Access rules no more linked to the template
	 * Sets access permissions for this {@link Template} instance
	 * @param cardAccess collection of {@link CardAccess} items defining access permissions
	 */
	public void setCardAccess(Collection cardAccess) {
		if (cardAccess != null && cardAccess.size() > 0)
			System.err.println("Card access items collection will not be saved anymore");
	}

	
	/**
	 * Checks if this template should be shown in new card creation dialog
	 * @return true if this template should be shown in new card creation dialog, false - otherwise
	 */
	public boolean isShowInCreateCard() {
		return showInCreateCard;
	}

	/**
	 * Sets value of showInCreateCard flag
	 * @param showInCreateCard  true if this template should be shown in new card creation dialog, false - otherwise
	 */
	public void setShowInCreateCard(boolean showInCreateCard) {
		this.showInCreateCard = showInCreateCard;
	}

	/**
	 * Checks if this template should be shown in search dialog
	 * @return true if this template should be shown in search dialog, false - otherwise
	 */
	public boolean isShowInSearch() {
		return showInSearch;
	}
	
	/**
	 * Sets value of showInSearch flag
	 * @param showInSearch  true if this template should be shown in search dialog, false - otherwise
	 */	
	public void setShowInSearch(boolean showInSearch) {
		this.showInSearch = showInSearch;
	}
}
