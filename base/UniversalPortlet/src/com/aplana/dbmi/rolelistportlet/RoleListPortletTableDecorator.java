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
package com.aplana.dbmi.rolelistportlet;

import java.util.List;

import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import com.aplana.dbmi.universalportlet.ColumnDescription;
import com.aplana.dbmi.util.ListPortletTableDecorator;

public class RoleListPortletTableDecorator extends ListPortletTableDecorator {
	
	private static final String page = "dbmi.Role";
	private static final String window = "dbmi.Role.w.Role";

	public RoleListPortletTableDecorator(RenderRequest request, RenderResponse response)
    {
		super(request, response, ListPortletTableDecorator.PortletDecorator.ROLE_LIST_PORTLET);
    }

	public String getLink(int i) {
		return getLink(i, page, window);
    }
	
	public List<ColumnDescription> getColumnDescriptions() {
        return super.columnDescriptions;
    }

    public void setColumnDescriptions(List<ColumnDescription> columnDescriptions) {
    	super.columnDescriptions = columnDescriptions;
    }
}
