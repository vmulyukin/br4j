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
package com.aplana.cms;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;

public class ServletProcessRequest extends ServletContentRequest implements ProcessRequest
{
	public ServletProcessRequest(HttpServletRequest request) {
		super(request);
		if (!"POST".equalsIgnoreCase(request.getMethod()))
			throw new IllegalStateException("Data processing can be requested only via POST method");
	}

	public String getCharacterEncoding() {
		return getServletRequest().getCharacterEncoding();
	}

	public int getContentLength() {
		return getServletRequest().getContentLength();
	}

	public String getContentType() {
		return getServletRequest().getContentType();
	}

	public InputStream getInputStream() throws IOException {
		return getServletRequest().getInputStream();
	}

	public BufferedReader getReader() throws IOException, UnsupportedEncodingException {
		return getServletRequest().getReader();
	}

	public void setCharacterEncoding(String enc) throws UnsupportedEncodingException {
		getServletRequest().setCharacterEncoding(enc);
	}

}
