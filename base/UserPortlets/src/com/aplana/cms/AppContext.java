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
package com.aplana.cms;

import org.springframework.context.ApplicationContext;


/**
 * This class provides application-wide access to the Spring ApplicationContext.
 * The ApplicationContext is injected by the class "ApplicationContextProvider".
 * 
 */
public class AppContext {
	
	

	private static ApplicationContext ctx;

	/**
	 * Injected from the class "ApplicationContextProvider" which is
	 * automatically loaded during Spring-Initialization.
	 */
	public static void setApplicationContext(
			ApplicationContext applicationContext) {
		ctx = applicationContext;
	}

	/**
	 * Get access to the Spring ApplicationContext from everywhere in your
	 * Application.
	 * 
	 * @return
	 */
	public static ApplicationContext getApplicationContext() {
		return ctx;
	}

}
