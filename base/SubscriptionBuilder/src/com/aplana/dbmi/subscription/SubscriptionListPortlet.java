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

import java.util.Collection;
import java.util.ResourceBundle;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletRequest;

import org.springframework.validation.BindException;
import org.springframework.web.portlet.mvc.SimpleFormController;

import com.aplana.dbmi.model.ContextProvider;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Subscription;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.PortletUtil;

public class SubscriptionListPortlet  extends SimpleFormController {

	public static final String REMOVE_ID  	= "subscription_delete_id";  
	public static final String RESOURCE_BUNDLE_NAME  	= "subscription";  
	public static final String REFRESH_ACTION = "REFRESH_ACTION";	
	
	protected Object  formBackingObject(PortletRequest request) throws Exception {
		// Set user locale
		ContextProvider.getContext().setLocale(request.getLocale()); 
		
		System.out.println("SubscriptionListPortlet.formBackingObject: request=" + request);
		
		WebSubscriptionListBean subscriptionListBean  = (WebSubscriptionListBean)super.formBackingObject(request);
		loadSubscriptionList(request, subscriptionListBean);
		return subscriptionListBean;
	}
	
	private boolean loadSubscriptionList(PortletRequest request, WebSubscriptionListBean subscriptionListBean) {
		boolean result = true;
		DataServiceBean dataServiceBean  = PortletUtil.createService(request);		
		try{
			Collection subscriptions = dataServiceBean.listAll(Subscription.class);
			subscriptionListBean.setSubscriptions(subscriptions);
			
		}catch(Exception e){
			e.printStackTrace(System.out);
			result = false;
			subscriptionListBean.setMessage(e.getLocalizedMessage());
			request.setAttribute("MSG_PARAM", e.getLocalizedMessage());
		} 		
		return result;
	}
    
    
	private boolean removeSubscription(PortletRequest request, WebSubscriptionListBean subscriptionListBean) {
		boolean result = true;
		DataServiceBean dataServiceBean  = PortletUtil.createService(request);		
		String removeId = request.getParameter(REMOVE_ID);
		ResourceBundle bundle = ResourceBundle.getBundle(RESOURCE_BUNDLE_NAME);
		String msg = bundle.getString("mts.subscription.remove.successfully.msg");
		
		try{
			dataServiceBean.deleteObject(new ObjectId(Subscription.class, removeId));
			
		}catch(Exception e){
			result = false;
			msg = e.getLocalizedMessage();
		} 		
		subscriptionListBean.setMessage(msg);
		request.setAttribute("MSG_PARAM", msg);
		return result;
	}
	
	public void onSubmitAction(ActionRequest request, ActionResponse response,
			Object command,	BindException errors) throws Exception {
		// Set user locale
		ContextProvider.getContext().setLocale(request.getLocale()); 

		WebSubscriptionListBean subscriptionListBean = (WebSubscriptionListBean) command;
        if (request.getParameter(REMOVE_ID) != null) {
        	removeSubscription(request, subscriptionListBean);
        	loadSubscriptionList(request, subscriptionListBean);
        }
        if (request.getParameter(REFRESH_ACTION) != null) {
        	loadSubscriptionList(request, subscriptionListBean);
        }
        
	}
}

