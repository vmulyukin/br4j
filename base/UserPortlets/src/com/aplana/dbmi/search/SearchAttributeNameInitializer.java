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


import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.NoSuchMessageException;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.ContextProvider;
import com.aplana.web.tag.util.StringUtils;

/**
 * Initializes Attribute name(english/russin) from properties file
 * It takes property keys and initialize value from property file
 * It supposes that property file is defined in spring xml by name message  
 * 
 * @author skashanski
 *
 */
public class SearchAttributeNameInitializer implements ApplicationContextAware {
	
	private static ApplicationContext applicationContext;
	
	private static Log logger = LogFactory.getLog(SearchFilterPortlet.class);
	
	
	public void setApplicationContext(ApplicationContext context)
			throws BeansException {
		
		this.applicationContext = context;
		
	}
	
	

	public void initialize(Attribute attr) {
		
		String propertyKeyRu = attr.getNameRu();

		if (StringUtils.hasText(propertyKeyRu)) {
			String nameRu = getMessage(propertyKeyRu, ContextProvider.LOCALE_RUS);
			if (StringUtils.hasText(nameRu))
				attr.setNameRu(nameRu);
		}	
			
		String propertyKeyEng = attr.getNameEn();
		
		if (StringUtils.hasText(propertyKeyEng)) {
			String nameEng = getMessage(propertyKeyEng, ContextProvider.LOCALE_ENG);
			if (StringUtils.hasText(nameEng))
				attr.setNameEn(nameEng);
		}	
		
	}



	private String getMessage(String propertyKey, Locale locale) {
		
		String name = propertyKey;
		try {
			name = applicationContext.getMessage(propertyKey,
					new Object[0], locale);
		} catch (NoSuchMessageException e) {
			logger.warn("Invalid property key  " + propertyKey, e);		
		}
		
		return name;
	}

	
}
