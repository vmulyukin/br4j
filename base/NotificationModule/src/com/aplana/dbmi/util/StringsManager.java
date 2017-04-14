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
package com.aplana.dbmi.util;

import java.util.Locale;
import java.util.ResourceBundle;

import com.aplana.dbmi.model.ContextProvider;
import com.aplana.dbmi.module.notif.ConfigResourceLoader;

public class StringsManager {

	private String bundleName;
	private ResourceBundle stringsRu;
	private ResourceBundle stringsEn;
	
	public void setBundleName(String bundleName) {
		this.bundleName = bundleName;
	}

	public String getStringRu(String key) {
		if (getStringsRu() == null)
			return null;
		return stringsRu.getString(key);
	}

	public String getStringEn(String key) {
		if (getStringsEn() == null)
			return null;
		return stringsEn.getString(key);
	}
	
	public String getLocaleString(Locale locale, String key) {
		if (ContextProvider.LOCALE_RUS.equals(locale))
			return getStringRu(key);
		if (ContextProvider.LOCALE_ENG.equals(locale))
			return getStringEn(key);
		throw new IllegalArgumentException("Unsupported locale: " + locale);
	}
	
	public ResourceBundle getStringsRu() {
		if (stringsRu == null)
			stringsRu = ResourceBundle.getBundle(bundleName, ContextProvider.LOCALE_RUS,
					new ConfigResourceLoader());
		return stringsRu;
	}
	
	public ResourceBundle getStringsEn() {
		if (stringsEn == null)
			stringsEn = ResourceBundle.getBundle(bundleName, ContextProvider.LOCALE_ENG,
					new ConfigResourceLoader());
		return stringsEn;
	}
	
	public ResourceBundle getLocaleStrings(Locale locale) {
		if (ContextProvider.LOCALE_RUS.equals(locale))
			return getStringsRu();
		if (ContextProvider.LOCALE_ENG.equals(locale))
			return getStringsEn();
		throw new IllegalArgumentException("Unsupported locale: " + locale);
	}
}
