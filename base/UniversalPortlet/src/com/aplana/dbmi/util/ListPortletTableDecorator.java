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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;

import javax.portlet.PortletURL;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.displaytag.decorator.TableDecorator;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.PortletService;
import com.aplana.dbmi.grouplistportlet.GroupListPortlet;
import com.aplana.dbmi.rolelistportlet.RoleListPortlet;
import com.aplana.dbmi.universalportlet.ColumnDescription;

public class ListPortletTableDecorator extends TableDecorator {
	
	private static final String BACK_URL_FIELD = "MI_BACK_URL_FIELD";

	private PortletService portletService;
	private RenderRequest request;
	private RenderResponse response;
	private String backURL;
	private String backUrlField;
	protected List<ColumnDescription> columnDescriptions;
	public enum PortletDecorator {
		ROLE_LIST_PORTLET,
		GROUP_LIST_PORTLET
	}
	
	protected ListPortletTableDecorator(RenderRequest request, RenderResponse response, PortletDecorator portletDecorator) {
		this.request = request;
	    this.response = response;
    	//System.out.println("Creating JBoss ShowListTableDecorator");
	    portletService = Portal.getFactory().getPortletService();
		try {
			PortletURL backPortletURL = response.createRenderURL();
			backPortletURL.setParameters(request.getParameterMap());
			backURL = URLEncoder.encode(backPortletURL.toString(), "UTF-8");
			switch(portletDecorator) {
				case ROLE_LIST_PORTLET: backUrlField = RoleListPortlet.BACK_URL_FIELD;
					break;
				case GROUP_LIST_PORTLET: backUrlField = GroupListPortlet.BACK_URL_FIELD;
					break;
				default: backUrlField = BACK_URL_FIELD;
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected String getLink(int i, String page, String window)
    {
    	List<?> currentRow = (List<?>) getCurrentRowObject();

    	ColumnDescription columnDescription = columnDescriptions.get(i);
        int linkColumnIndex = columnDescription.getLinkColumnIndex();
        MessageFormat link = columnDescription.getLink();

        Object columnValue = currentRow.get(i);

        String stringValue;
        if (columnValue != null) {
            stringValue = columnValue.toString().trim();
            if (stringValue.length() == 0) {
                stringValue = "-";
            }
        } else {
            stringValue = "-";
        }

        String param = "&" + link.format(new Object[]{ currentRow.get(linkColumnIndex) });
        final HashMap<String, String> urlParams = new HashMap<String, String>();
		urlParams.put(backUrlField, backURL);
		final String url = portletService.generateLink(page, window, urlParams, request, response);
        return "<a href=\"" + url + param + "\">" + stringValue + "</a>";
    }
}
