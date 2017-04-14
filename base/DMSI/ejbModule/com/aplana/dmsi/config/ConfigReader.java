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
package com.aplana.dmsi.config;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.aplana.dmsi.DMSIException;
import com.aplana.dmsi.types.DMSIObject;
import com.aplana.dmsi.util.ReflectionUtils;
import com.aplana.dmsi.util.ServiceUtils;

public abstract class ConfigReader<T> {

    private static final String DEFAULT_TYPES_PACKAGE = "com.aplana.dmsi.types";

    private String defaultPackage;

    protected Log logger = LogFactory.getLog(getClass());

    public List<T> getConfigs(String fileName) throws ConfigurationException {
	try {
	    Document config = ServiceUtils.readXmlConfig(fileName);
	    Element rootElement = config.getDocumentElement();
	    String packageName = rootElement.getAttribute("package");
	    if (packageName == null || "".equals(packageName)) {
		this.defaultPackage = DEFAULT_TYPES_PACKAGE;
	    } else {
		this.defaultPackage = packageName;
	    }
	    return readConfigs(rootElement);
	} catch (DMSIException ex) {
	    throw new ConfigurationException("Error during parse of file "
		    + fileName, ex);
	}
    }

    protected abstract List<T> readConfigs(Element root)
	    throws ConfigurationException;

    protected Class<?> initializeDMSIType(String simpleName) {
	return initializeType(Object.class, simpleName);
    }

    protected Class<? extends DMSIObject> initializeDMSIObjectType(
	    String simpleName) {
	return initializeType(DMSIObject.class, simpleName);
    }

    private <S> Class<? extends S> initializeType(Class<S> clazz, String name) {
	try {
	    return ReflectionUtils.initializeClass(clazz, name);
	} catch (IllegalStateException ex) {
	    final String fullName = defaultPackage + "." + name;
	    logger.info("There is no class with name " + name
		    + ". Trying to get class with name " + fullName);
	    return ReflectionUtils.initializeClass(clazz, fullName);
	}
    }
}
