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
package com.aplana.ireferent.actions;

import java.util.ArrayList;
import java.util.List;

import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.ireferent.IReferentException;
import com.aplana.ireferent.config.ConfigurationException;
import com.aplana.ireferent.types.LinkType;
import com.aplana.ireferent.types.TypedLink;
import com.aplana.ireferent.types.WSOItem;
import com.aplana.ireferent.types.WSObject;
import com.aplana.ireferent.util.ExtensionUtils;

public class CreateCardByModifiedObject extends CreateCardAction {

    private static final String EXTENSION_ID_PARAM = "extensionId";
    private static final String LINK_TYPE_PARAM = "linkType";

    private String extensionId;
    private String linkType;

    @Override
    public void setParameter(String key, Object value) {
	if (LINK_TYPE_PARAM.equals(key)) {
	    linkType = (String) value;
	} else if (EXTENSION_ID_PARAM.equals(key)) {
	    extensionId = (String) value;
	} else {
	    super.setParameter(key, value);
	}
    }

    @Override
    public void doAction(DataServiceBean serviceBean, WSObject object)
	    throws IReferentException {
	if (extensionId == null || "".equals(extensionId)) {
	    throw new ConfigurationException(EXTENSION_ID_PARAM
		    + " mandatory param is not set");
	}
	if (linkType == null || "".equals(linkType)) {
	    throw new ConfigurationException(LINK_TYPE_PARAM
		    + " mandatory param is not set");
	}
	updateObject(object);
	super.doAction(serviceBean, object);
    }

    private WSObject updateObject(WSObject object) {
	List<WSOItem> extensions = ExtensionUtils.getExtensions(object);
	for (WSOItem extension : extensions) {
	    if (extensionId.equals(extension.getId())) {
		updateItemValues(extension);
	    }
	}
	return object;
    }

    private void updateItemValues(WSOItem item) {
	List<Object> extensionValues = ExtensionUtils.getExtensionValues(item);
	List<Object> newItemValues = new ArrayList<Object>();
	for (Object valueObject : extensionValues) {
	    if (valueObject instanceof WSObject) {
		newItemValues.add(new TypedLink(new LinkType(linkType),
			(WSObject) valueObject));
	    }
	}
	ExtensionUtils.setExtensionValues(item, newItemValues);
    }
}
