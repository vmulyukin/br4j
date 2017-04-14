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
package com.aplana.dbmi.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;

public class CardLinkData extends FilteredCards
{
	private CardLinkAttribute attribute;

	public CardLinkData(CardLinkAttribute attr, DataServiceBean service)
			throws DataException, ServiceException {
		super(service, attr.getFilter(), attr.getName());
		this.attribute = attr;
		// (2010/02, RuSA) OLD: super.setSelectedList((List) attr.getValues());
		super.setSelectedList( new ArrayList(attr.getIdsLinked()) );
	}

	/*public List getSelectedListData() {
		//return selected == null ? new ArrayList() : selected;
		List ret = null;
		if (attribute != null) {
			ret = attribute.getValues() == null ? new ArrayList() : (List) attribute.getValues();
		} else {
			ret = selected == null ? new ArrayList() : selected;
		}
		return ret; 
	}*/

	@Override
	public String getSelectedListTitle() {
		return attribute.getName();
	}

	@Override
	public void setSelectedList(List data) {
		super.setSelectedList(data);
		attribute.setIdsLinked(data);
	}

	public void includeSearchForm(RenderRequest request, RenderResponse response)
			throws PortletException, IOException {
		request.getPortletSession().getPortletContext().getRequestDispatcher(JSP_SEARCH_PATH)
				.include(request, response);
	}

	/*private Search getFilter(Attribute attr) {
		Search ret = new Search();
		ret.setTemplates(new ArrayList()); // to delete when the "search.getTemplates().iterator()" bug, will be fixed
		if (attr != null && (attr.getClass().isAssignableFrom(CardLinkAttribute.class))) {
			try {
				ret = ((CardLinkAttribute)attr).getFilter();
			} catch (DataException e) {
				e.printStackTrace();
			}
		}
		return ret;
	}*/
}
