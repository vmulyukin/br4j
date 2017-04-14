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

/**
 * Supplement {@link AttributeBlock} class with information
 * about attribute block layout (order and column).<br>
 * This class is used to represent attribute blocks associated with
 * {@link Template}.
 */
public class TemplateBlock extends AttributeBlock
{
	private static final long serialVersionUID = 1L;
	private ObjectId template;
	
	/**
	 * Sets identifier of {@link Template} which owns this attribute block 
	 * @param template identifier of template associated with this attribute block
	 */
	public void setTemplate(ObjectId template) {
		this.template = template;
	}

	/**
	 * Sets identifier of {@link Template} which owns this attribute block 
	 * @param template numeric template identifier of template associated with this attribute block
	 */
	public void setTemplate(long template) {
		this.template = new ObjectId(Template.class, template);
	}
	
	/**
	 * Gets identifier of {@link Template} which owns this attribute block
	 * @return identifier of {@link Template} which owns this attribute block
	 */
	public ObjectId getTemplate() {
		return template;
	}
	
	/**
	 * Creates new TemplateBlock instance and initialize its id, nameRu, nameEn, isActive, isSystem
	 * and attributes fields with values from given AttributeBlock
	 * @param ab AttributeBlock
	 * @return new TemplateBlock instance based on existsing {@link AttributeBlock} instance. 
	 */
	public static TemplateBlock createFromAttributeBlock(AttributeBlock ab) {
		TemplateBlock tb = new TemplateBlock();
		tb.setId((String) ab.getId().getId());
		tb.setNameRu(ab.getNameRu());
		tb.setNameEn(ab.getNameEn());
		tb.setActive(ab.isActive());
		tb.setSystem(ab.isSystem());
		tb.setAttributes(ab.getAttributes());
		return tb;
	}
}
