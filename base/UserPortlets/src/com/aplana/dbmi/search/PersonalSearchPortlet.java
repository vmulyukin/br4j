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

import java.util.Collection;
import java.util.Iterator;
import java.util.ResourceBundle;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;

import org.springframework.validation.BindException;
import org.springframework.web.portlet.mvc.SimpleFormController;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.model.ContextProvider;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.PersonalSearch;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.PortletUtil;

public class PersonalSearchPortlet extends SimpleFormController {

	public static final String REMOVE_ID  	= "remove_personal_search_id";  
	public static final String RESOURCE_BUNDLE_NAME  	= "search";  
	public static final String PERSONAL_SEARCH_PORTLET_ACTION = "personalSearch"; 
	public static final String PERSONAL_SEARCH_ID = "personalSearchId"; 

	public static final String REFRESH_ACTION = "REFRESH_ACTION";	
	
    protected Object formBackingObject(PortletRequest request) throws Exception {
        ContextProvider.getContext().setLocale(request.getLocale());
    	WebPersonalSearchBean personalSearchBean = (WebPersonalSearchBean) super.formBackingObject(request);
        loadPersonalSearchList(request, personalSearchBean);
        return personalSearchBean;
    }

	private boolean loadPersonalSearchList(PortletRequest request, WebPersonalSearchBean personalSearchBean) {
		boolean result = true;
		DataServiceBean dataServiceBean  = PortletUtil.createService(request);		
		try{
	        Collection personalSearchList = dataServiceBean.listAll(PersonalSearch.class);
	        personalSearchBean.setSearchList(personalSearchList);
			
		}catch(Exception e){
			result = false;
			request.setAttribute("MSG_PARAM", e.getLocalizedMessage());
		} 		
		return result;
	}
    
    
	private boolean removePersonalSearch(PortletRequest request) {
		boolean result = true;
		DataServiceBean dataServiceBean  = PortletUtil.createService(request);		
		String removeId = request.getParameter(REMOVE_ID);
		ResourceBundle bundle = ResourceBundle.getBundle(RESOURCE_BUNDLE_NAME);
		String msg = bundle.getString("person.search.remove.successfully.msg");
		
		try{
			dataServiceBean.deleteObject(new ObjectId(PersonalSearch.class, removeId));
			
		}catch(Exception e){
			result = false;
			msg = e.getLocalizedMessage();
		} 		
		request.setAttribute("MSG_PARAM", msg);
		return result;
	}
    
    
    protected void onSubmitAction(ActionRequest request, ActionResponse response, Object command, BindException errors) throws Exception {
        super.onSubmitAction(request, response, command, errors);
		// Set user locale
		ContextProvider.getContext().setLocale(request.getLocale()); 
        
        WebPersonalSearchBean personalSearchBean = (WebPersonalSearchBean) command;
        Collection personalSearchList = personalSearchBean.getSearchList();

        if (request.getParameter(REMOVE_ID) != null) {
        	removePersonalSearch(request);
        	loadPersonalSearchList(request, personalSearchBean);
        }
        
        if (request.getParameter(REFRESH_ACTION) != null) {
        	loadPersonalSearchList(request, personalSearchBean);
        }
        
        String portletAction = request.getParameter("personal_search_action");
//        System.out.println("PersonalSearchPortlet.onSubmitAction: portletAction=" + portletAction);
        
        if ("doSearch".equals(portletAction)) {
            String personalSearchIdStr = request.getParameter("personal_search_id");
            System.out.println("PersonalSearchPortlet.onSubmitAction: personalSearchIdStr=" + personalSearchIdStr);
            Long personalSearchId = Long.valueOf(personalSearchIdStr);
            Search search = null;
            for (Iterator psIter = personalSearchList.iterator(); psIter.hasNext();) {
                PersonalSearch personalSearch = (PersonalSearch) psIter.next();
                System.out.println("PersonalSearchPortlet.onSubmitAction: personalSearch.id=" + personalSearch.getId().getId());
                if (personalSearchId.equals(personalSearch.getId().getId())) {
                    search = personalSearch.getSearch();
                    break;
                }
            }
            System.out.println("PersonalSearchPortlet.onSubmitAction: search=" + search);
            request.getPortletSession().setAttribute("SEARCH_BEAN", search, PortletSession.APPLICATION_SCOPE);
        }
    }

}
