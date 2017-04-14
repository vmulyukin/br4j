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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;

import org.springframework.beans.BeanUtils;
import org.springframework.validation.BindException;
import org.springframework.web.portlet.mvc.SimpleFormController;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.component.AccessComponent;
import com.aplana.dbmi.model.ContextProvider;
import com.aplana.dbmi.model.Distribution;
import com.aplana.dbmi.model.Notification;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Reference;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.TreeAttribute;
import com.aplana.dbmi.model.web.ControlUtils;
import com.aplana.dbmi.model.web.TreeControl;
import com.aplana.dbmi.model.web.WebSearchBean;
import com.aplana.dbmi.search.SearchUtils;
import com.aplana.dbmi.service.AsyncDataServiceBean;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.PortletUtil;
import com.aplana.dbmi.service.AsyncDataServiceBean.ExecuteOption;

public class DistributionPortlet extends SimpleFormController{
	
	public static final String RESOURCE_BUNDLE_NAME  	= "subscription";  
	public static final String SESSION_BEAN  = "AccessComponentPortletSessionBean";  // Bean name for the portlet session	
	
	protected Object  formBackingObject(PortletRequest request) throws Exception {
		// Set user locale
		ContextProvider.getContext().setLocale(request.getLocale()); 
		WebDistributionSearchBean distributionBean = null;
		DataServiceBean dataServiceBean  = PortletUtil.createService(request);
		try{
			
			if(isSessionForm() 
					&& request.getPortletSession().getAttribute(getFormSessionAttributeName()) != null){
				distributionBean = (WebDistributionSearchBean) request.getPortletSession().getAttribute(getFormSessionAttributeName());
			} else {
				distributionBean = initBean(request);
			}
			if(request.getParameter("distribution_id") != null){
				distributionBean = initBean(request);
				if (distributionBean  != null) {
					Distribution distribution = (Distribution) dataServiceBean.getById(new ObjectId(Distribution.class, request.getParameter("distribution_id")));
					BeanUtils.copyProperties(distribution, distributionBean.getDistribution(), new String[]{"search"});
					distributionBean.getDistribution().setId(Long.parseLong(distribution.getId().getId().toString()));
					Search  search= distributionBean.getDistribution().getSearch();	
					SearchUtils.initializeFromSearch(search, distributionBean);
				}
			} else {
				distributionBean = initBean(request);
				if (distributionBean  != null) {
					distributionBean.setDistribution(new Distribution());
					distributionBean.getDistribution().setFrequency(Notification.FREQ_DAYLY);
					Search  search= new Search();
					SearchUtils.initializeFromSearch(search, distributionBean);
				}
			}
    	
			if(distributionBean.getDistribution().getRegions() != null){
				for(Iterator it = distributionBean.getDistribution().getRegions().iterator(); it.hasNext(); ){
					ReferenceValue rf = (ReferenceValue)it.next();
					distributionBean.getRegions().put(rf.getId().getId().toString(), rf.getId().getId().toString());
				}
			}
		
			List accessItems = distributionBean.getDistribution().getAccessList() == null ? null : new ArrayList(distributionBean.getDistribution().getAccessList());
			getAccessComponent(request).setAccessItemList(accessItems );	
		
		
			String formAttrName = getFormSessionAttributeName(request);
			request.getPortletSession().setAttribute(formAttrName, distributionBean);
		
		}catch(Exception e){
			ResourceBundle bundle = ResourceBundle.getBundle(RESOURCE_BUNDLE_NAME);
			String err= bundle.getString("loadError");
			request.setAttribute("MSG_PARAM", err + e.getMessage());
		} 
		
		return distributionBean;
	}
	
	private WebDistributionSearchBean initBean(PortletRequest request) {
		WebDistributionSearchBean distributionBean = null;
		try {
			distributionBean = (WebDistributionSearchBean) super.formBackingObject(request);
		} catch (Exception ex) {
			request.setAttribute("MSG_PARAM", "Fatal error");
		}
		if (distributionBean != null) {
			try {
				DataServiceBean dataServiceBean  = PortletUtil.createService(request);	
		    	SearchUtils.loadTemplates(dataServiceBean, distributionBean);
		    	SearchUtils.loadMainAttributes(dataServiceBean, distributionBean);

		    	// init tree
				ResourceBundle bundle = ResourceBundle.getBundle(RESOURCE_BUNDLE_NAME);
				ResourceBundle bundleRu = ResourceBundle.getBundle(RESOURCE_BUNDLE_NAME, new Locale("ru"));
				TreeAttribute attribute = new TreeAttribute();
				attribute.setNameEn(bundle.getString("mtsSubscriptionTreeName"));
				attribute.setNameRu(bundleRu.getString("mtsSubscriptionTreeName"));
				attribute.setId(Reference.ID_REGION.getId().toString());
				attribute.setReference(Reference.ID_REGION);
				TreeControl control = (TreeControl) ControlUtils.initializeControl(attribute, dataServiceBean);
				distributionBean.setTree(control.getTree());
				
				getAccessComponent(request);	

			} catch (Exception ex) {
				ResourceBundle bundle = ResourceBundle.getBundle(RESOURCE_BUNDLE_NAME);
				String err= bundle.getString("loadError");
				request.setAttribute("MSG_PARAM", err + ex.getLocalizedMessage());
			}
			
			
		}
    	return distributionBean;
	}
	
	private boolean isValidate(Distribution distribution) {
		boolean result = true;
		if (distribution.getName() == null || distribution.getName().trim().equals("")) {
			result = false;
		}
		return result;
	}
	
	public void onSubmitAction(ActionRequest request, ActionResponse response,
			Object command,	BindException errors) throws Exception {
		// Set user locale
		ContextProvider.getContext().setLocale(request.getLocale()); 

        try{
    		// AccessComponent
        	getAccessComponent(request).parseRequest(request, response);
        	String accessComponentAction = request.getParameter(AccessComponent.ACTION_FIELD);
        	
            if (AccessComponent.EDIT_DEPARTMENT_ACCESS_ACTION.equals(accessComponentAction)
                    || AccessComponent.EDIT_INDIVIDUAL_ACCESS_ACTION.equals(accessComponentAction)) {
                getAccessComponent(request).setAccessHandlerAction(true);
            }
    		// AccessComponent
        	WebDistributionSearchBean distributionBean = (WebDistributionSearchBean) command;
        	AsyncDataServiceBean dataServiceBean  = PortletUtil.createService(request);	
        	if (AccessComponent.STORE_ACTION.equals(accessComponentAction)) {
        		// Get Access Item List
        		Collection access = getAccessComponent(request).getAccessItemList();
        		distributionBean.getDistribution().setAccessList(access);
        	} else if("SAVE_ACTION".equals(distributionBean.getAction())){
        		Search search = SearchUtils.getSearch(dataServiceBean, distributionBean);
        		Distribution distribution = new Distribution();
        		BeanUtils.copyProperties(distributionBean.getDistribution(), distribution, new String[]{"search", "personId", "creator"});
        		if(distributionBean.getDistribution().getId()!=null){
        			distribution.setId(Long.parseLong(distributionBean.getDistribution().getId().getId().toString()));
        		}
        		distribution.setSearch(search);
			
        		Collection rootValues =  dataServiceBean.listChildren(Reference.ID_REGION, ReferenceValue.class); 
        		List referenceValues = new ArrayList();
        		ControlUtils.getTreeReferenceValues(referenceValues, rootValues, distributionBean.getRegions());
        		distribution.setRegions(referenceValues);
			
        		// Get Access Item List
        		Collection access = getAccessComponent(request).getAccessItemList();
        		distribution.setAccessList(access);

        		if(distribution.getAccessList() == null){
        			distribution.setAccessList(new ArrayList());
        		}

        		ResourceBundle bundle = ResourceBundle.getBundle(RESOURCE_BUNDLE_NAME);
				String msg= bundle.getString("mts.distribution.save.success.msg");
				if (isValidate(distribution)) {
					ObjectId objId = dataServiceBean.saveObject(distribution, ExecuteOption.SYNC);
					distributionBean.getDistribution().setId(Long.parseLong(objId.getId().toString()));
				} else {
					msg= bundle.getString("mts.distribution.missing.name.error.msg");
				}
				response.setRenderParameter("MSG_PARAM", msg );

				distributionBean.setAction("");
        	
        	}else if(WebSearchBean.ATTRIBUTE_SEARCH_ACTION.equals(distributionBean.getAction())){
        		SearchUtils.loadAttributes(dataServiceBean, distributionBean);
        		distributionBean.setAction("");			
        	}else{
        		SearchUtils.moveToRenderParameters(request, response);
        	}
        }catch(Exception e){
			ResourceBundle bundle = ResourceBundle.getBundle(RESOURCE_BUNDLE_NAME);
			String err= bundle.getString("saveError");
			response.setRenderParameter("MSG_PARAM", err + e.getLocalizedMessage());
		} 
	        if (isSessionForm()) {
	            String formAttrName = getFormSessionAttributeName(request);
	            request.getPortletSession().setAttribute(formAttrName, command);
	        }
			response.setRenderParameter("portlet_action", "distribution");	        
	}

	private AccessComponent getAccessComponent(PortletRequest request) {
		PortletSession session = request.getPortletSession();
		if( session == null )
			return null;
		AccessComponent component = (AccessComponent)session.getAttribute(AccessComponent.ACCESS_HANDLER);
		if (component == null) {
			DataServiceBean serviceBean = PortletUtil.createService(request);
			component = new AccessComponent(serviceBean, null, "DistributionForm");
			session.setAttribute(AccessComponent.ACCESS_HANDLER ,component);
		}
		return component;
	}

}
