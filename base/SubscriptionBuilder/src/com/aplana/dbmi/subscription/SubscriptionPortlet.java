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
package com.aplana.dbmi.subscription;

import java.util.ResourceBundle;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletRequest;

import org.springframework.beans.BeanUtils;
import org.springframework.validation.BindException;
import org.springframework.web.portlet.mvc.SimpleFormController;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.model.ContextProvider;
import com.aplana.dbmi.model.Notification;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Subscription;
import com.aplana.dbmi.model.web.WebSearchBean;
import com.aplana.dbmi.search.SearchUtils;
import com.aplana.dbmi.service.AsyncDataServiceBean;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.PortletUtil;
import com.aplana.dbmi.service.AsyncDataServiceBean.ExecuteOption;


public class SubscriptionPortlet extends SimpleFormController {
	
	public static final String RESOURCE_BUNDLE_NAME  	= "subscription";  
		
	protected Object  formBackingObject(PortletRequest request) throws Exception {
		// Set user locale
		ContextProvider.getContext().setLocale(request.getLocale()); 
		
		WebSubscriptionSearchBean subscriptionBean = null;
		DataServiceBean dataServiceBean  = PortletUtil.createService(request);	
		try{
			if(isSessionForm() 
					&& request.getPortletSession().getAttribute(getFormSessionAttributeName()) != null) {
				subscriptionBean = (WebSubscriptionSearchBean) request.getPortletSession().getAttribute(getFormSessionAttributeName());
			} else {
				subscriptionBean = initBean(request);
			}
			// load
			if(request.getParameter("subscription_id")!= null){
				subscriptionBean = initBean(request);
				if (subscriptionBean  != null) {
				    Subscription subscription = (Subscription) dataServiceBean.getById(new ObjectId(Subscription.class, request.getParameter("subscription_id")));
				    BeanUtils.copyProperties(subscription, subscriptionBean.getSubscription(), new String[]{"search"});
				    subscriptionBean.getSubscription().setId(Long.parseLong(subscription.getId().getId().toString()));
		            Search search= subscriptionBean.getSubscription().getSearch();
		    	    SearchUtils.initializeFromSearch(search, subscriptionBean);
				}
	    	// create    
			}else{
				subscriptionBean = initBean(request);
				if (subscriptionBean  != null) {
					subscriptionBean.setSubscription(new Subscription());
					subscriptionBean.getSubscription().setFrequency(Notification.FREQ_DAYLY);
		            Search  search= new Search();
		            SearchUtils.initializeFromSearch(search, subscriptionBean);
				}
			}
			
			String formAttrName = getFormSessionAttributeName(request);
			request.getPortletSession().setAttribute(formAttrName, subscriptionBean);
			
		}catch(Exception e){
			ResourceBundle bundle = ResourceBundle.getBundle(RESOURCE_BUNDLE_NAME);
			String err= bundle.getString("loadError");
			request.setAttribute("MSG_PARAM", err + e.getLocalizedMessage());
		} 
		return subscriptionBean;
	}
	
	private WebSubscriptionSearchBean initBean(PortletRequest request) {
		WebSubscriptionSearchBean subscriptionBean = null;
		try {
			subscriptionBean = (WebSubscriptionSearchBean) super.formBackingObject(request);
		} catch (Exception ex) {
			request.setAttribute("MSG_PARAM", "Fatal error");
		}
		if (subscriptionBean != null) {
			try {
				DataServiceBean dataServiceBean  = PortletUtil.createService(request);	
		    	SearchUtils.loadTemplates(dataServiceBean, subscriptionBean);
		    	SearchUtils.loadMainAttributes(dataServiceBean, subscriptionBean);
			} catch (Exception ex) {
				ResourceBundle bundle = ResourceBundle.getBundle(RESOURCE_BUNDLE_NAME);
				String err= bundle.getString("loadError");
				request.setAttribute("MSG_PARAM", err + ex.getLocalizedMessage());
			}
		}
    	return subscriptionBean;
	}
	
	private boolean isValidate(Subscription subscription) {
		boolean result = true;
		if (subscription.getName() == null || subscription.getName().trim().equals("")) {
			result = false;
		}
		return result;
	}
	
	public void onSubmitAction(ActionRequest request, ActionResponse response,
			Object command,	BindException errors) throws Exception {
		// Set user locale
		ContextProvider.getContext().setLocale(request.getLocale()); 
		
		WebSubscriptionSearchBean subscriptionBean = (WebSubscriptionSearchBean) command;
		AsyncDataServiceBean dataServiceBean  = PortletUtil.createService(request);	
		try{
			if("SAVE_ACTION".equals(subscriptionBean.getAction())) {
				Search search = SearchUtils.getSearch(dataServiceBean, subscriptionBean);
				Subscription subscription = new Subscription();
				BeanUtils.copyProperties(subscriptionBean.getSubscription(), subscription, new String[]{"search", "personId"});
				if(subscriptionBean.getSubscription().getId() != null ){
					subscription.setId(Long.parseLong(subscriptionBean.getSubscription().getId().getId().toString()));
				}
				subscription.setSearch(search);
				
				ResourceBundle bundle = ResourceBundle.getBundle(RESOURCE_BUNDLE_NAME);
				String msg= bundle.getString("mts.subscription.save.success.msg");
				if (isValidate(subscription)) {
					ObjectId objId = dataServiceBean.saveObject(subscription, ExecuteOption.SYNC);
					subscriptionBean.getSubscription().setId(Long.parseLong(objId.getId().toString()));
				} else {
					msg= bundle.getString("mts.subscription.missing.name.error.msg");
				}
				response.setRenderParameter("MSG_PARAM", msg );

				subscriptionBean.setAction("");
			} else if (WebSearchBean.ATTRIBUTE_SEARCH_ACTION.equals(subscriptionBean.getAction())) {
				SearchUtils.loadAttributes(dataServiceBean, subscriptionBean);
				subscriptionBean.setAction("");			
			} else {
				SearchUtils.moveToRenderParameters(request, response);
			}
		}catch(Exception e){
			ResourceBundle bundle = ResourceBundle.getBundle(RESOURCE_BUNDLE_NAME);
			String err= bundle.getString("loadError");
			response.setRenderParameter("MSG_PARAM", err + e.getLocalizedMessage());
		} 
	    
		if (isSessionForm()) {
			String formAttrName = getFormSessionAttributeName(request);
	        request.getPortletSession().setAttribute(formAttrName, command);
	    }
		response.setRenderParameter("portlet_action", "subscription");	  
			
	}

}

