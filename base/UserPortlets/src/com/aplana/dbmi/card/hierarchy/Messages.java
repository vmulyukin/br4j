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
package com.aplana.dbmi.card.hierarchy;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.model.ContextProvider;
import com.aplana.dbmi.model.LocalizedString;

public class Messages {
	private Log logger = LogFactory.getLog(getClass());
	private Map strings = new HashMap();
	public void add(String key, String msg, String lang) {
		LocalizedString st = (LocalizedString)strings.get(key);
		if (st == null) {
			st = new LocalizedString();
			strings.put(key, st);
		}
		if (ContextProvider.LOCALE_RUS.getLanguage().equalsIgnoreCase(lang)) {
			st.setValueRu(msg);
		} else if (ContextProvider.LOCALE_ENG.getLanguage().equalsIgnoreCase(lang)) {
			st.setValueEn(msg);
		} else {
			logger.warn("Unsupported language code: " + lang);
		}
	}
	
	public void add(String key, LocalizedString st) {
		strings.put(key, st);
	}
	
	public LocalizedString getMessage(String key) {
		return (LocalizedString)strings.get(key);
	}
	
	public void clear() {
		strings.clear();
	}
}
