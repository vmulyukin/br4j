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
package com.aplana.dbmi.module.notif;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;

import freemarker.template.Configuration;

public class FreemarkerConfig extends Configuration implements InitializingBean, BeanFactoryAware
{
	public static final String VAR_UTIL = "util";
	public static final String BEAN_UTIL = "templateUtil";
	
	private BeanFactory beanFactory;
	
	public void afterPropertiesSet() throws Exception {
		//BeansWrapper wrapper = BeansWrapper.getDefaultInstance();
		//TemplateHashModel models = wrapper.getStaticModels();
		//setSharedVariable(VAR_UTIL, models.get(TemplateUtil.class.getName()));
		setSharedVariable(VAR_UTIL, beanFactory.getBean(BEAN_UTIL));
	}

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}
}
