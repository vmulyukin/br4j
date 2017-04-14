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
package com.aplana.dbmi.card;

import java.util.Map;

import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.ajax.CardLinkPickerSearchFilterParameters;
import com.aplana.dbmi.ajax.CardLinkPickerSearchParameters;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.search.SearchFilterPortlet;
import com.aplana.dbmi.search.SearchFilterPortletSessionBean;


/**
 * Helper class for getting CardAttribute Parameters
 *  
 * @author skashanski
 *
 */
public class CardAttributeEditorParameterHelper {

	
	private static Log logger = LogFactory.getLog(CardAttributeEditorParameterHelper.class);
	
	public static Object  getAttributeEditorData(PortletRequest request, ObjectId attrId, String key) {
		
		PortletSession session = request.getPortletSession();
		if (session == null) {
			logger.warn("Portlet session is not exists yet.");
			return null;
		}
		
		
		if (isCardPortletRequest(session)) {
			
			CardPortletSessionBean sessionBean = (CardPortletSessionBean)getSessionBean(session, CardPortlet.SESSION_BEAN);
			return sessionBean.getActiveCardInfo().getAttributeEditorData(attrId, key);
			
		} else if (isSearchFilterRequest(session)) {
			
			SearchFilterPortletSessionBean sessionBean = (SearchFilterPortletSessionBean)getSessionBean(session, SearchFilterPortlet.SESSION_BEAN);
			return sessionBean.getSearchEditorData(attrId, key);
			
		} else 
			throw new IllegalArgumentException("Unsupported portlet request. It shouls be instance of  SearchFilterPortlet or CardPortlet!");
			
		
	}
	
	public static String getCallerField(PortletRequest request) {
		
		PortletSession session = request.getPortletSession();
		
		if (isCardPortletRequest(session)) {
			return CardLinkPickerSearchParameters.CALLER;
		} else if (isSearchFilterRequest(session)) {
			return CardLinkPickerSearchFilterParameters.CALLER;
		} else 
			throw new IllegalArgumentException("Unsupported portlet request. It shouls be instance of  SearchFilterPortlet or CardPortlet!");
		
		
	}

	private static boolean isCardPortletRequest(PortletSession session) {
		
		Object sessionBean = getSessionBean(session,CardPortlet.SESSION_BEAN);
		
		return (sessionBean != null);

	}

	private static Object getSessionBean(PortletSession session, String sessionBeanName) {
		
		return session.getAttribute(sessionBeanName);
	}
	
	
	private static boolean isSearchFilterRequest(PortletSession session) {
		
		
		Object sessionBean = getSessionBean(session, SearchFilterPortlet.SESSION_BEAN);
		
		return (sessionBean != null);

	}	
	
	

}
