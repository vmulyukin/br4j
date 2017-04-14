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
package com.aplana.agent.plugin;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import com.aplana.agent.conf.ConfigService;

/**
 * Factory creates plugins by names using Spring
 */
public class PluginFactory {

    public final static String APPLICATION_CONTEXT = "beans.xml";

    static BeanFactory beanFactory = null;
    
    public static void initApplicationContext() {
        try {
        	Resource res = new UrlResource(ConfigService.getResourceURL("/conf/"+APPLICATION_CONTEXT));
            beanFactory = new XmlBeanFactory(res);
        } catch (Exception e) {
            throw new RuntimeException("Spring configuration reading exception: " + e.getMessage());
        }        
    }

    public static Plugin getBean(String name){
    	return (Plugin) getOrdinalBean(name);
    }
    
    public static Object getOrdinalBean(String name){
        if (beanFactory == null){
            throw new IllegalStateException("ApplicationContext not initialized!");
        }
        if (containsBean(name)) {
            return beanFactory.getBean(name);
        }
        return null;
    }

    public static boolean containsBean(String name){
        return beanFactory.containsBean(name);
    }
}
