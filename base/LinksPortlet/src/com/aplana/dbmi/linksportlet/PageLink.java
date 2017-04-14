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
package com.aplana.dbmi.linksportlet;

import java.io.IOException;
import java.io.StringWriter;
import java.text.ParseException;
import java.util.HashMap;

import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.PortletService;

public class PageLink extends AbstractPageLink
{
    private String pageName;
    private String portletName;
    private String params;
    private PortletService service;

    public PageLink() {
    	service = Portal.getFactory().getPortletService();
    }
    
    public String renderToHTML(RenderRequest request, RenderResponse response) throws PortletException {
        StringWriter stringWriter = new StringWriter();
        try {
            if (portletName == null || portletName.length() == 0)
            	stringWriter.append(service.generateLink(pageName, null, null, request, response));
            else {
            	HashMap map = new HashMap();
            	if (params != null && params.length() > 0) {
	            	String[] array = params.split(";");
	            	for (int i = 0; i < array.length; i++) {
	            		String[] param = array[i].split("=");
	            		if (param.length != 2)
	            			throw new ParseException(params, 0);
	            		if ("$".equals(param[1]))
	            			param[1] = response.createRenderURL().toString();
//                        map.put(param[0], param[1].split(",")); // TODO At the moment JBossPortletService cann't generate link with multivalue parameters [AVorotnikov 08.11.2008]
                        map.put(param[0], param[1]);
	            	}
            	}
            	stringWriter.append(service.generateLink(pageName, portletName, map, request, response));
            }
            stringWriter.close();
        } catch (IOException e) {
        	logger.error("Error generating link to page " + pageName, e);
        } catch (ParseException e) {
        	logger.error("Error parsing parameters for portlet " + portletName, e);
        }
        String href = stringWriter != null ? stringWriter.toString() : "";
        setHref(href);
        return super.renderToHTML(request, response);
    }

    public String getPageName() {
        return pageName;
    }

    public void setPageName(String pageName) {
        this.pageName = pageName;
    }

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}

	public String getPortletName() {
		return portletName;
	}

	public void setPortletName(String portletName) {
		this.portletName = portletName;
	}

}
