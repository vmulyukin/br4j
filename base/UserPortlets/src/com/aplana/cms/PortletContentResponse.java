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

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import javax.portlet.PortletURL;
import javax.portlet.RenderResponse;

public class PortletContentResponse implements ContentResponse
{
	private RenderResponse response;
	
	public PortletContentResponse(RenderResponse response) {
		this.response = response;
	}
	
	public RenderResponse getRenderResponse() {
		return response;
	}

	public String createActionURL(Map parameters) {
		PortletURL url = response.createActionURL();
		url.setParameters(convertedParameters(parameters));
		return url.toString();
	}

	public String createRenderURL(Map parameters) {
		PortletURL url = response.createRenderURL();
		url.setParameters(convertedParameters(parameters));
		return url.toString();
	}

	public String encodeURL(String path) {
		return response.encodeURL(path);
	}

	public void flushBuffer() throws IOException {
		response.flushBuffer();
	}

	public int getBufferSize() {
		return response.getBufferSize();
	}

	public String getCharacterEncoding() {
		return response.getCharacterEncoding();
	}

	public String getContentType() {
		return response.getContentType();
	}

	public Locale getLocale() {
		return response.getLocale();
	}

	public String getNamespace() {
		return response.getNamespace();
	}

	public OutputStream getOutputStream() throws IOException {
		return response.getPortletOutputStream();
	}

	public PrintWriter getWriter() throws IOException {
		return response.getWriter();
	}

	public boolean isCommitted() {
		return response.isCommitted();
	}

	public void reset() {
		response.reset();
	}

	public void resetBuffer() {
		response.resetBuffer();
	}

	public void setBufferSize(int size) {
		response.setBufferSize(size);
	}

	public void setContentType(String type) {
		response.setContentType(type);
	}

	public void setTitle(String title) {
		response.setTitle(title);
	}
	
	private Map convertedParameters(Map parameters) {
		HashMap converted = new HashMap(parameters.size());
		for (Iterator itr = parameters.entrySet().iterator(); itr.hasNext(); ) {
			Map.Entry param = (Map.Entry) itr.next();
			converted.put(param.getKey(), new String[] { param.getValue().toString() });
		}
		return converted;
	}
}
