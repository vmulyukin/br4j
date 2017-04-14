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
package com.aplana.ireferent.endpoint.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.service.DataException;
import com.aplana.ireferent.IReferentException;
import com.aplana.ireferent.WSObjectFactory;
import com.aplana.ireferent.types.WSOCollection;
import com.aplana.ireferent.types.WSOItem;
import com.aplana.ireferent.types.WSObject;
import com.aplana.ireferent.util.ExtensionUtils;
import com.aplana.ireferent.util.ServiceUtils;

public class ObjectsGetterByConfig {

    public final static String SET_ID_EXTENSION_ID = "setId";

    private final String configFileNameFormat;
    private final HashMap<WSObjectFactory, String[]> setObjectFactory;
    private Map<String, Object> paramsAliases = new HashMap<String, Object>();

    private boolean isAddExtension;

    public ObjectsGetterByConfig(String configFileNameFormat,
    	HashMap<WSObjectFactory, String[]> setObjectFactory) {
    	this.configFileNameFormat = configFileNameFormat;
    	this.setObjectFactory = setObjectFactory;
    }

    public boolean isAddExtension() {
    	return this.isAddExtension;
    }

    public void setAddExtension() {
    	this.isAddExtension = true;
    }
    
    public void setParamsAliases(Map<String, Object> paramsAliases) {
    	this.paramsAliases = paramsAliases;
    }
    
    public Map<String, Object> getParamsAliases() {
    	return paramsAliases;
    }

    public WSOCollection createObjects() throws Exception {
    	WSOCollection objects = new WSOCollection();
    	List<Object> objectsData = objects.getData();
    	for(Map.Entry<WSObjectFactory, String[]> entry : setObjectFactory.entrySet()) {
	    	for (String setId : entry.getValue()) {
	    	    objectsData.addAll(createObjects(setId, entry.getKey()));
	    	}
    	}
    	return objects;
    }

    private List<Object> createObjects(String setId, WSObjectFactory objectFactory) throws DataException,
	    IReferentException {
	try {
	    Search search = initFromFile(String.format(configFileNameFormat,
		    setId));
	    objectFactory.setSetId(setId);
	    if (null != paramsAliases && !paramsAliases.isEmpty())
	    	search.setParamsAliases(paramsAliases);	    
	    List<Object> objects = objectFactory.newWSOCollection(search)
		    .getData();
	    if (isAddExtension) {
	    	addExtension(objects, setId);
	    }
	    return objects;
	} catch (IOException ex) {
	    throw new IllegalStateException("Unable to get documents by setId "
		    + setId, ex);
	}
    }

    private Search initFromFile(String fileName) throws IOException,
	    DataException {
    	Search search = new Search();
    	InputStream config = ServiceUtils.readConfig(fileName);
    	search.initFromXml(config);
    	return search;
    }

    private void addExtension(List<Object> objects, String setId) {
    	WSOItem setIdExtension = ExtensionUtils.createItem(SET_ID_EXTENSION_ID,
    			setId);
    	for (Object object : objects) {
    		ExtensionUtils.addExtensions((WSObject) object, setIdExtension);
    	}
    }
}
