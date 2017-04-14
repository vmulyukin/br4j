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
package com.aplana.dbmi.universalportlet;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.List;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.displaytag.decorator.TableDecorator;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.PortletService;

public class UniversalPortletTableDecorator extends TableDecorator
{
	private PortletService service;
	private String backURL;
	private RenderRequest request;
	private RenderResponse response;

	private List<ColumnDescription> columnDescriptions;

	public UniversalPortletTableDecorator(RenderRequest request, RenderResponse response)
    {
    	//System.out.println("Creating JBoss ShowListTableDecorator");
	    this.request = request;
	    this.response = response;
		service = Portal.getFactory().getPortletService();
		try {
			backURL = URLEncoder.encode(response.createRenderURL().toString(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		emptyValue = ResourceBundle.getBundle("com.aplana.dbmi.showlist.nl.MIShowListPortletResource",
//				request.getLocale()).getString("name.empty");
        backURL = response.createRenderURL().toString();
    }

    public String getLink(int i)
    {
    	List currentRow = (List) getCurrentRowObject();

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

        String url = link.format(new Object[]{ currentRow.get(linkColumnIndex) });

        return "<a href=\"" + url + "\">" + stringValue + "</a>";

    }

    public List<ColumnDescription> getColumnDescriptions() {
        return columnDescriptions;
    }

    public void setColumnDescriptions(List<ColumnDescription> columnDescriptions) {
        this.columnDescriptions = columnDescriptions;
    }

}
