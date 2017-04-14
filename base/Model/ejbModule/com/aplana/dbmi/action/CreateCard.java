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

import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Template;

/**
 * {@link Action} implementation used for new cards creation.
 * <br>
 * Creates new instance of {@link Card} class by given {@link Template}
 * and initializes its attributes with default values if any specified 
 * (see {@link com.aplana.dbmi.model.DefaultAttributeValue}).  
 * <br>
 * Returns newly created card as result. 
 */
public class CreateCard implements ObjectAction<Card>
{
	private static final long serialVersionUID = 2L;
	private ObjectId template;
	private boolean linked;
	private Card parent;
	
	/**
	 * Default constructor
	 */
	public CreateCard() {
	}
	
	/**
	 * Creates new instance of action class and initializes its
	 * {@link #setTemplate(ObjectId) template} property with given value
	 * @param template identifier of {@link Template} object to be used for card creation
	 */
	public CreateCard(ObjectId template) {
		this.template = template;
	}
	
	/**
	 * Creates new instance of action class and initializes its
	 * {@link #setTemplate(ObjectId) template} property.
	 * @param template {@link Template} object to be used for card creation.
	 */
	public CreateCard(Template template) {
		this.template = template == null ? null : template.getId();
	}
	
	/**
	 * Returns identifier of {@link Template} object to be used for {@link Card} creation
	 * @return identifier of {@link Template} object to be used for {@link Card} creation
	 */
	public ObjectId getTemplate() {
		return template;
	}

	/**
	 * Sets identifier of {@link Template} object to be used for {@link Card} creation 
	 * @param template identifier of {@link Template} object to be used for {@link Card} creation
	 */
	public void setTemplate(ObjectId template) {
		this.template = template;
	}

	/**
	 * Sets identifier of {@link Template} object to be used for {@link Card} creation.
	 * @param template {@link Template} object whose identifier will be set as value of 
	 * {@link #setTemplate(ObjectId) template} property 
	 */
	public void setTemplate(Template template) {
		this.template = template == null ? null : template.getId();
	}

	/**
	 * @see Action#getResultType()
	 */
	public Class<?> getResultType() {
		return Card.class;
	}

	/**
	 * @see #getTemplate()
	 */
	public ObjectId getObjectId() {
		return getTemplate();
	}

	/**
	 * Returns <code>true</code> if a card created will have a parent with the same template
	 */
	public boolean isLinked() {
		return linked;
	}

	/**
	 * Sets the flag that the card created will have a parent with the same template
	 */
	public void setLinked(boolean linked) {
		this.linked = linked;
	}
	
	public Card getParent(){
		return parent;
	}
	
	public void setParent(Card parent){
		this.parent = parent;
	}
}
