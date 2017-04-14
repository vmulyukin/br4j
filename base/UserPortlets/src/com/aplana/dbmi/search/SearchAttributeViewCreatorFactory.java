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
package com.aplana.dbmi.search;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.DatePeriodAttribute;
import com.aplana.dbmi.model.MultipleStateSearchItemAttribute;
import com.aplana.dbmi.model.StateSearchAttribute;
import com.aplana.dbmi.model.TemplateSearchAttribute;

/**
 * Represents factory for {@link SearchAttributeViewCreator} children classes
 * @author skashanski
 *
 */
public class SearchAttributeViewCreatorFactory {
	
	private static SearchAttributeViewCreatorFactory singleton = null;

	public static SearchAttributeViewCreatorFactory getFactory() {
		if (singleton == null)
			singleton = new SearchAttributeViewCreatorFactory();
		return singleton;
	}
	
	private SearchAttributeViewCreatorFactory() {}
	
	public <T extends Attribute> SearchAttributeViewCreator getSearchAttributeViewCreator(T attribute) {
		if (attribute instanceof DatePeriodAttribute)
			return new SearchDatePeriodAttributeViewCreator((DatePeriodAttribute)attribute);
		else if (attribute instanceof StateSearchAttribute) 
			return new SearchStateAttributeViewCreator((StateSearchAttribute)attribute);
		else if (attribute instanceof TemplateSearchAttribute) 
			return new TemplateSearchAttributeViewCreator((TemplateSearchAttribute)attribute);		
		else if (attribute instanceof MultipleStateSearchItemAttribute) 
			return new MultipleStateSearchItemAttributeViewCreator((MultipleStateSearchItemAttribute)attribute);
		else	
			return new SearchAttributeViewCreator(attribute);
	}
}
