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
import java.util.Locale;
import java.util.Map;

public interface ContentResponse
{
	// copied from PortletResponse
	public String createActionURL(Map parameters);
	public String createRenderURL(Map parameters);
	public String encodeURL(String path);
	public void flushBuffer() throws IOException;
	public int getBufferSize();
	public String getCharacterEncoding();
	public String getContentType();
	public Locale getLocale();
	public String getNamespace();
	public OutputStream getOutputStream() throws IOException;
	public PrintWriter getWriter() throws IOException;
	public boolean isCommitted();
	public void reset();
	public void resetBuffer();
	public void setBufferSize(int size);
	public void setContentType(String type);
	public void setTitle(String title);
}
