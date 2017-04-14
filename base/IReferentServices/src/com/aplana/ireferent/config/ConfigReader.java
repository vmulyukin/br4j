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
package com.aplana.ireferent.config;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.aplana.ireferent.IReferentException;
import com.aplana.ireferent.types.WSObject;
import com.aplana.ireferent.util.ReflectionUtils;
import com.aplana.ireferent.util.ServiceUtils;

public abstract class ConfigReader<T> {

    private static final String TYPES_PACKAGE = "com.aplana.ireferent.types.";

    protected Log logger = LogFactory.getLog(getClass());

    public List<T> getConfigs(String fileName) throws ConfigurationException {
	try {
	    Document config = ServiceUtils.readXmlConfig(fileName);
	    return readConfigs(config.getDocumentElement());
	} catch (IReferentException ex) {
	    throw new ConfigurationException("Error during parse of file "
		    + fileName, ex);
	}
    }

    protected abstract List<T> readConfigs(Element root)
	    throws ConfigurationException;

    protected Class<?> initializeIReferentType(String simpleName) {
	return ReflectionUtils.initializeClass(Object.class, TYPES_PACKAGE
		+ simpleName);
    }

    protected Class<? extends WSObject> initializeWSObjectType(String simpleName) {
	return ReflectionUtils.initializeClass(WSObject.class, TYPES_PACKAGE
		+ simpleName);
    }
}
