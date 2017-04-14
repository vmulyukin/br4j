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

import java.util.Set;

import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.jboss.portal.identity.Role;

import com.aplana.dbmi.action.Action;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.PortletUtil;
import com.aplana.dbmi.service.ServiceException;

public class Link extends AbstractLink {
    private String href;
    private String visualClass;
    private String text;
    private Action checkAction;
    private Set<String> roles;
    
    public String renderToHTML(RenderRequest request, RenderResponse response) throws PortletException {
        String res = "";
        try {
            if (checkAction == null || PortletUtil.createService(request).canDo(checkAction)) {
                res = "<div" + (visualClass != null ? (" class=\"" + visualClass + '"') : "") + "><a href=\"" + href + "\">" + text + "</a></div>";
            }
        } catch (DataException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ServiceException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //System.out.println("Link: res=" + res);
        return res;
    }

    public String getHref() {
        return href;
    }

    public void setHref(String href) {
        this.href = href;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getVisualClass() {
        return visualClass;
    }

    public void setVisualClass(String visualClass) {
        this.visualClass = visualClass;
    }

    public Action getCheckAction() {
        return checkAction;
    }

    public void setCheckAction(Action action) {
        this.checkAction = action;
    }

	@Override
	public Set<String> getRoles() {
		return roles;
	}

	@Override
	public void setRoles(Set<String> roles) {
		this.roles = roles;		
	}
}
