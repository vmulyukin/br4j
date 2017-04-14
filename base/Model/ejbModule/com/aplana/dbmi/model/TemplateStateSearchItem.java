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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * Represents template search attribute that can change state
 * It could be checked/unchecked. 
 * 
 * @author skashanski
 *
 */
public class TemplateStateSearchItem extends StateSearchItem {
	
	private Template template = null;

	public TemplateStateSearchItem(Template template, ObjectId id) {
		super(id, template.getName());
		this.template = template;
	}

	public Template getTemplate() {
		return template;
	}

	@Override
	public Collection<DataObject> getValues() {
		
		Collection<DataObject> values = new ArrayList<DataObject>();
		values.add(template);
		
		return values;
	}
	
	
	
	
	
	
	
	

}
