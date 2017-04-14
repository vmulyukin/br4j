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
package com.aplana.dbmi.filestorage.convertmanager;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.model.ContextProvider;

public class ResourceMessageReader {

	private final Log logger = LogFactory.getLog(getClass());
	private final static String resourceFile = "nls.exceptions";

	public ResourceMessageReader() {

	}

	public String getMessage(String key, Object[] params) {

		return generateMsg(key, params);
	}

	public String getMessage(String key) {
		return generateMsg(key, null);
	}

	private String generateMsg(String key, Object[] params) {
		try {
			ResourceBundle rb = getResourceBundle();
			if (params == null) {
				return rb.getString(key);
			}
			return MessageFormat.format(rb.getString(key), params);
		} catch (MissingResourceException e) {
			logger.info("Missing resource: " + key);
			return key;
		}
	}

	private static ResourceBundle getResourceBundle() {
		return ResourceBundle.getBundle(resourceFile, ContextProvider
				.getContext().getLocale());
	}

}
