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
package com.aplana.dbmi.card.extra;

import java.util.HashMap;

import javax.portlet.PortletException;

import com.aplana.dbmi.card.ExtraJavascriptInfo;
import com.aplana.dbmi.model.Attribute;

public abstract class ExtraJavascriptBuilder {
	
	private HashMap<String,String> params = new HashMap();
	
	public abstract void addJavascript(Attribute attr, StringBuilder stringBuilder) throws PortletException;
	
	public static ExtraJavascriptBuilder newInstance(ExtraJavascriptInfo extraJavascriptInfo) throws PortletException {
		Class builderClass = null;
		try {
			builderClass = Class.forName(extraJavascriptInfo.getClassName());
		} catch (Exception e) {
			throw new PortletException("Can't find class for extraJavascript builder", e);
		}

		ExtraJavascriptBuilder result = null;
		try {
			result = (ExtraJavascriptBuilder) builderClass.newInstance();
		} catch (Exception e) {
			throw new PortletException("Can't create new extraJavascript builder instance", e);
		}
		result.params = extraJavascriptInfo.getParams();
		return result;
	}
	
	public String getParam(String key) {
		return params.get(key);
	}
	
	public boolean hasParam(String key) {
		return params.containsKey(key);
	}

    public String getEntryPoint(Attribute attr) {
        return null;
    }
}
