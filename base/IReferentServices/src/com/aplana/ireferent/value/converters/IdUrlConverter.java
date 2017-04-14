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
package com.aplana.ireferent.value.converters;

import java.net.URLEncoder;

import com.aplana.ireferent.Parametrized;
import com.aplana.ireferent.types.WSOUrl;

public class IdUrlConverter implements Converter, Parametrized {
	
	private static final String IS_PDF = "isPdf";
	
	private boolean isPdf = false;
	
	public void setParameter(String key, Object value) {
		if (null != value && IS_PDF.equals(key))
			isPdf = Boolean.parseBoolean((String) value);
	    }

	public Object convert(Object value) {
		WSOUrl result = new WSOUrl();
		String url;
		url = isPdf?
		WSOUrl.URI + String.valueOf(value) + WSOUrl.PDF:
		WSOUrl.URI + String.valueOf(value);
		url = URLEncoder.encode(url);
		result.setUrl(url);
		return result;
	}
}
