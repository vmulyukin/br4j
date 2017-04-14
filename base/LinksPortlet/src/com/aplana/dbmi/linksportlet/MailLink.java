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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;

import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import com.aplana.dbmi.action.Action;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.PortletUtil;
import com.aplana.dbmi.service.ServiceException;

public class MailLink extends Link {

    private String hrefTemplate;
    private String subject;
    private String actionName;

    public String renderToHTML(RenderRequest request, RenderResponse response) throws PortletException {
        DataServiceBean dataService = PortletUtil.createService(request);
        
        String address = null;
        String subjectEncoded = "";
        try {
            Action action = (Action) Class.forName(actionName).newInstance();
            address = (String) dataService.doAction(action);
            subjectEncoded = URLEncoder.encode(subject, "windows-1251");
            subjectEncoded = subjectEncoded.replaceAll("\\+", "%20");
        } catch (InstantiationException e) {
            e.printStackTrace();
            throw new PortletException(e);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new PortletException(e);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new PortletException(e);
        } catch (DataException e) {
            e.printStackTrace();
            throw new PortletException(e);
        } catch (ServiceException e) {
            e.printStackTrace();
            throw new PortletException(e);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new PortletException(e);
        }
        
        String href = MessageFormat.format(hrefTemplate, new Object[]{address, subjectEncoded});
        
        setHref(href);
        //System.out.println("MailLink.renderToHTML: result=" + href);
        
        return super.renderToHTML(request, response);
    }

    public String getActionName() {
        return actionName;
    }
    public void setActionName(String actionName) {
        this.actionName = actionName;
    }
    public String getHrefTemplate() {
        return hrefTemplate;
    }
    public void setHrefTemplate(String hrefTemplate) {
        this.hrefTemplate = hrefTemplate;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
}
