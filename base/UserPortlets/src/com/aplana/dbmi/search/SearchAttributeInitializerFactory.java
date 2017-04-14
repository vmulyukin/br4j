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
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.DatePeriodAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.MultipleStateSearchItemAttribute;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.StateSearchAttribute;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.TemplateSearchAttribute;
import com.aplana.dbmi.model.TextAttribute;
import com.aplana.dbmi.model.TreeAttribute;


/**
 * Factory for SearchAttributeInitializer classes
 * @author skashanski
 *
 */
public class SearchAttributeInitializerFactory {
	
	private static SearchAttributeInitializerFactory singleton = null;
	
	
	public static SearchAttributeInitializerFactory getFactory() {
		if (singleton == null)
			singleton = new SearchAttributeInitializerFactory();
		return singleton;
	}
	
	private SearchAttributeInitializerFactory() {
		
	}
		
	
	
	public <T extends Attribute> SearchAttributeInitializer getSearchAttributeInitializer(T attribute) {
/*		
		if (attribute instanceof DateAttribute)
			return new SearchDateAttributeInitializer((DateAttribute)attribute);		
		else if (attribute instanceof TextAttribute)
			return new SearchTextAttributeInitializer((TextAttribute)attribute);
		else if (attribute instanceof StringAttribute)
			return new SearchStringAttributeInitializer((StringAttribute)attribute);
		else if (attribute instanceof StateSearchAttribute)
			return new StateSearchAttributeInitializer((StateSearchAttribute)attribute);
		else if (attribute instanceof TemplateSearchAttribute)
			return new TemplateSearchAttributeInitializer((TemplateSearchAttribute)attribute);
		else if (attribute instanceof ListAttribute)
			return new SearchListAttributeInitializer((ListAttribute)attribute);
		else if (attribute instanceof TreeAttribute)
			return new SearchTreeAttributeInitializer((TreeAttribute)attribute);
		else if (attribute instanceof CardLinkAttribute)
			return new SearchCardLinkAttributeInitializer((CardLinkAttribute)attribute);
		else if (attribute instanceof PersonAttribute)
			return new SearchPersonAttributeInitializer((PersonAttribute)attribute);
		else if (attribute instanceof DatePeriodAttribute)
			return new SearchDatePeriodAttributeInitializer((DatePeriodAttribute)attribute);
		else if (attribute instanceof MultipleStateSearchItemAttribute)
			return new MultipleStateSearchItemAttributeInitializer((MultipleStateSearchItemAttribute)attribute);		
		else
		*/
			throw new IllegalArgumentException("Unsupoprted attribute type : " + attribute.getType());
	}
	
	
	
	
	
	
	
	
	
	

}
