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
package com.aplana.dbmi.search;

import javax.portlet.PortletURL;
import javax.portlet.RenderResponse;
import org.displaytag.decorator.TableDecorator;

import com.aplana.dbmi.model.PersonalSearch;

public class PersonalSearchTableDecorator extends TableDecorator {
	
    private RenderResponse renderResponse;

    public String getSearchLink() {
        PersonalSearch currentPersonalSearch = (PersonalSearch) getCurrentRowObject();
        PortletURL searchActionURL = renderResponse.createActionURL();
        searchActionURL.setParameter("portlet_action", "personalSearch");
        searchActionURL.setParameter("personal_search_id", currentPersonalSearch.getId().getId().toString());
        searchActionURL.setParameter("personal_search_action", "doSearch");
        return "<a href=\"" + searchActionURL + "\">" + currentPersonalSearch.getName() + "</a>";
    }

    public RenderResponse getRenderResponse() {
        return renderResponse;
    }

    public void setRenderResponse(RenderResponse renderResponse1) {
        this.renderResponse = renderResponse1;
    }
    
}
