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
 package com.aplana.dbmi.portlet;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletSecurityException;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.PortletService;
import com.aplana.dbmi.action.CreateCard;
import com.aplana.dbmi.card.util.ARMUtils;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.filter.TemplateIdListFilter;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.PortletUtil;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.web.tag.util.StringUtils;

/**
 * Represents portlet for choosing document type to create a new one inside Supervisor/Minister Workstation.
 * 
 * @author EStatkevich
 */
public class ChooseDocTemplatePortlet extends GenericPortlet {
		
	public static final String ACTION_INIT = "init";
	public static final String ACTION_CANCEL = "cancel";
	public static final String FIELD_ACTION = "formAction";
	public static final String FIELD_NAMESPACE = "namespace";
	public static final String FIELD_BACK_URL = "backURL";
	public static final String FIELD_LINK_TO_CARD = "linkToCard";
	
	public static final String VIEW = "/WEB-INF/jsp/chooseDocTemplate.jsp";
	public static final String SESSION_BEAN = "chooseDocTemplatePortletSessionBean";
	protected Log logger = LogFactory.getLog(getClass());
	protected PortletService portletService = null;
	
	private String backUrl;

	@Override
	public void init() throws PortletException {
		// TODO Auto-generated method stub
		super.init();
		portletService = Portal.getFactory().getPortletService();
	}
	
	@Override
	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, PortletSecurityException, IOException {
		response.setContentType("text/html");
		PortletRequestDispatcher rd = getPortletContext().getRequestDispatcher(VIEW);
		rd.include(request, response);
	}
	
	@Override
	public void processAction(ActionRequest request, ActionResponse response) throws PortletException, PortletSecurityException, IOException {
		ChooseDocTemplatePortletSessionBean sessionBean = null;
		String action = request.getParameter(FIELD_ACTION);

		try {
			if (ACTION_INIT.equals(action)) {
				sessionBean = prepareSessionBean(request, response);
			} else if (ACTION_CANCEL.equals(action)) {
				sessionBean = getSessionBean(request);
				if(sessionBean != null)
					backUrl = sessionBean.getBackUrl();
				request.getPortletSession().removeAttribute(SESSION_BEAN);
				response.sendRedirect(
						backUrl.replaceAll("%2F", "/")
						.replaceAll("%3D", "=")
						.replaceAll("%3F", "?")
						);
			}
		} catch (DataException e) {
			sessionBean = getSessionBean(request);
			if (null != sessionBean) {
				sessionBean.setMessage(e.getMessage());
			}
			logger.error(e);
		} catch (ServiceException e) {
			sessionBean = getSessionBean(request);
			if (null != sessionBean) {
				sessionBean.setMessage(e.getMessage());
			}
			logger.error(e);
		}
	}

	/**
	 * Fills session bean with the necessary data. 
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws DataException
	 * @throws ServiceException
	 * @throws PortletException
	 */
	protected ChooseDocTemplatePortletSessionBean prepareSessionBean(ActionRequest request, ActionResponse response) throws DataException, ServiceException, PortletException, IOException {
		final ChooseDocTemplatePortletSessionBean sessionBean = new ChooseDocTemplatePortletSessionBean();
		final PortletSession session = request.getPortletSession();
		session.setAttribute(SESSION_BEAN, sessionBean);

		final DataServiceBean serviceBean = PortletUtil.createService(request);
		sessionBean.setServiceBean(serviceBean);
		sessionBean.setBackUrl(request.getParameter(FIELD_BACK_URL));
		sessionBean.setHeader(getResourceBundle(request.getLocale()).getString("header"));
		sessionBean.setSwitchNavigatorLink(ARMUtils.retrieveSwitchNavigatorButton(request, serviceBean));
		String linkToCard = request.getParameter(FIELD_LINK_TO_CARD);
		sessionBean.setLinkToCard( StringUtils.hasLength(linkToCard) ? linkToCard : StringUtils.EMPTY_STRING);

		retrieveAllowedTemplates(request, response);
		return sessionBean;
	}

	/**
	 * Retrieves allowed templates (for new card creation) for user.
	 * 
	 * @param request
	 * @param response
	 * @throws DataException
	 */
	private void retrieveAllowedTemplates(ActionRequest request, ActionResponse response) throws DataException {
		ArrayList<ObjectId> theAllowedTemplateIds = new ArrayList<ObjectId>();
		String requiredTemplates = portletService.getPageProperty("requiredTemplates", request, response);
		if (StringUtils.hasLength(requiredTemplates)) {
			String[] theTemplates = requiredTemplates.split(",");
			for (String theTemplateStr : theTemplates) {
				Long theTemplId = Long.valueOf(theTemplateStr);
				CreateCard createAction;
				try {
					ObjectId theTemplateId = new ObjectId(Template.class, theTemplId);
					createAction = new CreateCard(theTemplateId);
					if (getSessionBean(request).getServiceBean().canDo(createAction)) {
						theAllowedTemplateIds.add(theTemplateId);
					}
				} catch (Exception e) {
					logger.error(e);
				}
			}
			final Map<ObjectId, String> theTemplatesMap = new LinkedHashMap<ObjectId, String>();
			if (theAllowedTemplateIds.size() > 0) {
				List<Template> theAllowedTemplates = loadTemplates(getSessionBean(request).getServiceBean(), theAllowedTemplateIds);
				if (theAllowedTemplates != null && theAllowedTemplates.size() > 0) {
					for (ObjectId theAllowedTemplateId : theAllowedTemplateIds) {
						for (Template theAllowedTemplate : theAllowedTemplates) {
							if (theAllowedTemplateId.equals(theAllowedTemplate.getId())) {
								theTemplatesMap.put(theAllowedTemplate.getId(), theAllowedTemplate.getNameRu());
							}
						}
					}
				}
			}
			getSessionBean(request).setAllowedTemplates(theTemplatesMap);
		}
	}

	/**
	 * Loads allowed templates form DB.
	 * 
	 * @param dataServiceBean
	 * @param filterTemplates
	 * @return
	 * @throws DataException
	 */
	@SuppressWarnings("unchecked")
	public List<Template> loadTemplates(DataServiceBean dataServiceBean, Collection<ObjectId> filterTemplates) throws DataException {
		try {
			final TemplateIdListFilter filter = new TemplateIdListFilter(filterTemplates);
			return (List<Template>) dataServiceBean.filter(Template.class, filter);
		} catch (ServiceException e) {
			throw new DataException(e);
		}
	}
	
	/**
	 * Retrieves session bean form session.
	 * 
	 * @param request
	 * @return
	 */
	protected ChooseDocTemplatePortletSessionBean getSessionBean(PortletRequest request) {
		PortletSession session = request.getPortletSession();
		return (ChooseDocTemplatePortletSessionBean) session.getAttribute(SESSION_BEAN);
	}
}
