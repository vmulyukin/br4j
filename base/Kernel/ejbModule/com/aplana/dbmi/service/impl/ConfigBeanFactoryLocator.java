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
package com.aplana.dbmi.service.impl;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.access.BeanFactoryLocator;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.context.access.ContextBeanFactoryReference;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Utility class used by spring framework to lookup and use of BeanFactory.
 * Loads application context from file in classpath with given name.
 */
public class ConfigBeanFactoryLocator implements BeanFactoryLocator
{
	public static final String CONFIG_FILE = "beans.xml";
	
	private static ClassPathXmlApplicationContext ctx =
		new ClassPathXmlApplicationContext(ClassPathXmlApplicationContext.CLASSPATH_ALL_URL_PREFIX + CONFIG_FILE);
	
	/**
	 * @return the BeanFactory instance, wrapped as a BeanFactoryReference object 
	 */
	public BeanFactoryReference useBeanFactory(String factoryKey)
			throws BeansException {
		/*Resource[] xmls = new PathMatchingResourcePatternResolver().getResources(factoryKey);
		for (int i = 0; i < xmls.length; i++) {
			init.parse(xmls[i].getInputStream());
		}*/
		//ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(factoryKey);
		return new ContextBeanFactoryReference(ctx);
	}
}
