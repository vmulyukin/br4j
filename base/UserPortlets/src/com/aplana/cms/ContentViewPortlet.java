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
package com.aplana.cms;

import java.io.IOException;
import java.util.List;
import java.util.ResourceBundle;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletSecurityException;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.PortletSecurityException;
import org.jboss.portal.portlet.aspects.portlet.ContextDispatcherInterceptor;
import org.jboss.portal.portlet.invocation.PortletInvocation;

import javax.servlet.http.HttpServletRequest;

import org.springframework.context.ApplicationContext;

import com.aplana.cms.cache.CachingDataServiceBean;
import com.aplana.dbmi.action.GetDelegateListByLogin;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.ServiceQuery;
import com.aplana.dbmi.card.CardPortlet;
import com.aplana.dbmi.card.CardPortletCardInfo;
import com.aplana.dbmi.card.CardPortletSessionBean;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ContextProvider;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.TypedCardLinkAttribute;
import com.aplana.dbmi.portlet.ARMDocumentPickerAttributeEditor;
import com.aplana.dbmi.search.SimpleSearchPortlet;
import com.aplana.dbmi.service.AsyncDataServiceBean;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.SystemUser;
import com.aplana.dbmi.service.UserPrincipal;

public class ContentViewPortlet extends ContentPortlet
{
	public static final String PARAM_FORM = "form";
	public static final String PREF_ITEM = "item";
	public static final String PREF_VIEWS = "views";
	public static final String PREF_SEARCH = "search";
	public static final String PREF_LINK_DOCS = "linkDocs";
	public static final String DEFAULT_SORT_COLUMN_ID = "default";
	
	private static final String VIEW = "/WEB-INF/jsp/linkDocument/chooseLinkDocAction.jsp";
	
	public static final ObjectId ATTR_RELAT_DOCS = ObjectId.predefined(TypedCardLinkAttribute.class, "jbr.relatdocs");
	
	protected void doView(RenderRequest request, RenderResponse response)
			throws PortletException, PortletSecurityException, IOException
	{
		//processParameters(request);
		response.setContentType("text/html");
		ContextProvider.getContext().setLocale(request.getLocale());
	    PortletSession session = request.getPortletSession();
        PortletInvocation portletInvocation = (PortletInvocation) request.getAttribute(ContextDispatcherInterceptor.REQ_ATT_COMPONENT_INVOCATION);
        HttpServletRequest req = portletInvocation.getDispatchedRequest();//������ ����� �������� ��������� �� ��� �������, � ������ ������� ��� �� ��������
        // (YNikitin, 31481, 2013/08/16) ����� ������������ �������� � ���� ���������� ���������������� �� ����� ����� ����� �������� � ���� � ���������� ������ ����� ������������, �� �������� ��� ���
        if (request.getUserPrincipal() != null) {
        	String userName = req.getParameter(DataServiceBean.USER_NAME); 	// � ������� ������ ����� ������������, �� ����� ����� ������ � ����� ��������
            if (userName != null) {
                DataServiceBean service = new DataServiceBean();
                service.setUser(new SystemUser());
                service.setAddress("localhost");
                GetDelegateListByLogin action = new GetDelegateListByLogin();
                action.setLogin(request.getUserPrincipal().getName());
                try {
                    List<String> list = (List<String>) service.doAction(action);
                    if (list.contains(userName)) {
                        session.setAttribute(DataServiceBean.USER_NAME, userName, PortletSession.APPLICATION_SCOPE);
                    } else if (request.getUserPrincipal().getName().equals(userName)) {
                        session.removeAttribute(DataServiceBean.USER_NAME, PortletSession.APPLICATION_SCOPE);
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        if (req.getParameter("logged") != null) {
            session.removeAttribute(DataServiceBean.USER_NAME, PortletSession.APPLICATION_SCOPE);
        }
        //DataServiceBean service = getDataService(request);
		PortletContentRequest wrappedReq = new PortletContentRequest(request);
		PortletContentResponse wrappedResp = new PortletContentResponse(response);
		ApplicationContext applicationContext = getSpringApplicationContext(); 

		ContentProducer cms = new ContentProducer(wrappedReq, wrappedResp, applicationContext);

		// Define default sort column and sort order if needed
		String currentSortColumnId = 
			(String) session.getAttribute(ContentProducer.SESS_ATTR_SORT_COLUMN_ID, PortletSession.APPLICATION_SCOPE);	
		String newSortColumnId = cms.getRequest().getParameter(ContentRequest.PARAM_SORT_COLUMN_ID);
		if(null == currentSortColumnId || DEFAULT_SORT_COLUMN_ID.equals(newSortColumnId)) {
			session.setAttribute(ContentProducer.SESS_ATTR_SORT_COLUMN_ID, DEFAULT_SORT_COLUMN_ID, PortletSession.APPLICATION_SCOPE);
			session.setAttribute(ContentProducer.SESS_ATTR_STRAIGHT_ORDER, true, PortletSession.APPLICATION_SCOPE);
		}
		
		// Define simple search filter
		Search search = (Search) session.getAttribute(SimpleSearchPortlet.APP_ATTR_SEARCH_BEAN, 
				PortletSession.APPLICATION_SCOPE);
		if(null != search && null != search.getWords() && search.getWords().length() > 0) {
			cms.setVariable(ContentProducer.VAR_SIMPLE_SEARCH_FILTER, search.getWords());
		}
		
		// Make user's credentials available for servlet
		if (wrappedReq.getSessionAttribute(DataServiceBean.USER_NAME) != null) {
		    wrappedReq.setSessionAttribute(ContentRequest.APP_ATTR_USER, new UserPrincipal((String) wrappedReq.getSessionAttribute(DataServiceBean.USER_NAME)));
        } else {
            wrappedReq.setSessionAttribute(ContentRequest.APP_ATTR_USER, wrappedReq.getUserPrincipal());
        }
		
		Card area = cms.getCurrentSiteArea();
		if (area == null)
			return;
		PortletPreferences prefs = request.getPreferences();
		Card content = null;
		String item = prefs.getValue(PREF_ITEM, null);
		if (item != null && item.length() > 0)
			content = getContent(item, cms);
		else {
			item = prefs.getValue(PREF_SEARCH, null);
			if (item != null && item.length() > 0) {
				writeContent(cms.getCurrentSiteArea(),
						"<content type='list' list='search' view='" + item + "'/>",
						response.getWriter(), cms);
				return;
			}
			if (content == null)
				content = cms.getCurrentContent(false);
		}
		if (content == null)
			return;
		
        Card viewCard = cms.getContentPresentationCard(content, area, getDefaultViewIds(prefs));
        String view = cms.getContentPresentation(viewCard);
        
        cms.writeDocListColumnsInfo(area.getId(), response.getWriter());
		writeContent(content, view, response.getWriter(), cms);
		
		// Writing to log collected statistics
		try {
			cms.getService().doAction(new ServiceQuery("cacheStats"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		prepareLinkDocumentsData(request, response, cms, prefs);
	}

	private void prepareLinkDocumentsData(RenderRequest request, RenderResponse response, ContentProducer cms, PortletPreferences prefs) throws IOException, PortletException {
		String linkDocsPref = prefs.getValue(PREF_LINK_DOCS, null);
		if (linkDocsPref != null && linkDocsPref.length() > 0 && "true".equals(linkDocsPref)) {
			createCardPortletSessionBean(request, response, cms.getService());
			try {
				ARMDocumentPickerAttributeEditor documentPickerAttributeEditor = new ARMDocumentPickerAttributeEditor(request, cms.getService(), (String) ATTR_RELAT_DOCS.getId(), "��������� ���������");
				documentPickerAttributeEditor.setEditorDataToRequest(request, response);
			} catch (DataException e) {
				logger.error(e);
			}
			PortletRequestDispatcher rd = getPortletContext().getRequestDispatcher(VIEW);
			rd.include(request, response);
		}
	}
	
	private CardPortletSessionBean createCardPortletSessionBean(RenderRequest request, RenderResponse response, AsyncDataServiceBean serviceBean) {
		CardPortletSessionBean cardPortletSessionBean = CardPortlet.getSessionBean(request);
		if (null != cardPortletSessionBean) {
			return cardPortletSessionBean;
		}

		PortletSession session = request.getPortletSession(true);
		cardPortletSessionBean = new CardPortletSessionBean();
		cardPortletSessionBean.setDataServiceBean(serviceBean);
		cardPortletSessionBean.setResourceBundle(ResourceBundle.getBundle("com.aplana.dbmi.card.nl.CardPortletResource", request.getLocale()));

		CardPortletCardInfo cardInfo = new CardPortletCardInfo();
		cardInfo.setCard(new Card());
		cardInfo.setMode(CardPortlet.CARD_EDIT_MODE);
		cardPortletSessionBean.setActiveCardInfo(cardInfo);

		session.setAttribute(CardPortlet.SESSION_BEAN, cardPortletSessionBean);
		session.setAttribute(CardPortlet.SESSION_BEAN + "." + response.getNamespace(), cardPortletSessionBean, PortletSession.APPLICATION_SCOPE);
		session.setAttribute("namespace", response.getNamespace(), PortletSession.APPLICATION_SCOPE);

		return cardPortletSessionBean;
	}

	public void processAction(ActionRequest request, ActionResponse response)
			throws PortletException, PortletSecurityException, IOException
	{
		//processParameters(request);
		
		// Define sort column and sort order
		String newSortColumnId = request.getParameter(ContentRequest.PARAM_SORT_COLUMN_ID);
		PortletSession session = request.getPortletSession();
		String currentSortColumnId = 
			(String) session.getAttribute(ContentProducer.SESS_ATTR_SORT_COLUMN_ID, PortletSession.APPLICATION_SCOPE);
		
		if(newSortColumnId == null || newSortColumnId.length() == 0 || null == currentSortColumnId) {
			session.setAttribute(ContentProducer.SESS_ATTR_SORT_COLUMN_ID, DEFAULT_SORT_COLUMN_ID, PortletSession.APPLICATION_SCOPE);
			session.setAttribute(ContentProducer.SESS_ATTR_STRAIGHT_ORDER, true, PortletSession.APPLICATION_SCOPE);
		} else {
			if(newSortColumnId.equals(currentSortColumnId)) {
				Boolean straightOrder = 
					(Boolean) session.getAttribute(ContentProducer.SESS_ATTR_STRAIGHT_ORDER, PortletSession.APPLICATION_SCOPE);
				session.setAttribute(ContentProducer.SESS_ATTR_STRAIGHT_ORDER, !straightOrder, PortletSession.APPLICATION_SCOPE);
			} else {
				session.setAttribute(ContentProducer.SESS_ATTR_SORT_COLUMN_ID, newSortColumnId, PortletSession.APPLICATION_SCOPE);
				session.setAttribute(ContentProducer.SESS_ATTR_STRAIGHT_ORDER, true, PortletSession.APPLICATION_SCOPE);
			}
		}
		
		if (request.getParameter(PARAM_FORM) == null) {
			logger.warn("Action request without form!");
			return;
		}
		Object proc = TagFactory.getProcessor(request.getParameter(PARAM_FORM));
		if (proc == null || !(proc instanceof FormProcessor)) {
			logger.error("Processor for form " + proc + " not found");
			return;
		}
		PortletProcessRequest wrappedReq = new PortletProcessRequest(request);
		PortletProcessResponse wrappedResp = new PortletProcessResponse(response);
		((FormProcessor) proc).processForm(wrappedReq, wrappedResp,
				CachingDataServiceBean.createBean(wrappedReq));
	}
	
	private String getDefaultViewIds(PortletPreferences prefs) {
		return prefs.getValue(PREF_VIEWS, null);
	}

	private Card getContent(String item, ContentProducer csm) {
		try {
			ObjectId itemId = new ObjectId(Card.class, new Long(item));
			return csm.getContent(itemId);
		} catch (Exception e) {
			logger.error("Error retrieving content item " + item, e);
			return null;
		}
	}
}
