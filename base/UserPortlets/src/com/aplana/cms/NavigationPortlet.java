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
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.PortletSecurityException;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.portal.portlet.aspects.portlet.ContextDispatcherInterceptor;
import org.jboss.portal.portlet.invocation.PortletInvocation;
import org.springframework.context.ApplicationContext;

import com.aplana.cms.cache.CacheManager;
import com.aplana.dbmi.Portal;
import com.aplana.dbmi.action.GetDelegateListByLogin;
import com.aplana.dbmi.action.ServiceQuery;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.IntegerAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.TextAttribute;
import com.aplana.dbmi.search.SimpleSearchPortlet;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.dbmi.service.SystemUser;


public class NavigationPortlet extends ContentPortlet
{
	public static final String DEFAULT_NAVIGATOR = "navigator";
	public static final String PREF_SWITCHABLE_NAVIGATOR = "switchableNavigator";
	
	public static final String PARAM_NEW_NAVIGATOR_NAME = "navigatorName";
	
	public static final String ATTR_CURRENT_NAVIGATOR_NAME = "currentNavigatorName";
	public static final String ATTR_NEW_NAVIGATOR_NAME = "newNavigatorName";
	public static final String ATTR_NAVIGATOR = "navigator";
	
	public static final ObjectId ATTR_LEVEL = new ObjectId(IntegerAttribute.class, "_LEVEL");

	protected static final Log logger = LogFactory.getLog(NavigationPortlet.class);
	
	protected void doView(RenderRequest request, RenderResponse response)
			throws PortletException, PortletSecurityException, IOException
	{
		//processParameters(request);
	    PortletSession session = request.getPortletSession();
        PortletInvocation portletInvocation = (PortletInvocation) request.getAttribute(ContextDispatcherInterceptor.REQ_ATT_COMPONENT_INVOCATION);
        HttpServletRequest req = portletInvocation.getDispatchedRequest();//������ ����� �������� ��������� �� ��� �������, � ������ ������� ��� �� ��������
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
		response.setContentType("text/html");
		PortletContentRequest wrappedReq = new PortletContentRequest(request);
		PortletContentResponse wrappedResp = new PortletContentResponse(response);
		
		ApplicationContext applicationContext = getSpringApplicationContext();
		
		ContentProducer cms = new ContentProducer(wrappedReq, wrappedResp, applicationContext, false);
		Card navigator = getNavigator(request, response, cms);
		if (navigator == null) {
			logger.error("No navigator defined");
			response.getWriter().write("<div style='color:red;'>No navigator defined!</div>");
			return;
		}
		
		writeSubFolders(response.getWriter(), cms, navigator, false);
		
		// Writing to log collected statistics
		logger.info(CacheManager.getCacheStats());
		try {
			cms.getService().doAction(new ServiceQuery("cacheStats"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void processAction(ActionRequest request, ActionResponse response) 
			throws PortletException, PortletSecurityException, IOException {
		String navigatorName = request.getParameter(PARAM_NEW_NAVIGATOR_NAME);
		if(null != navigatorName && navigatorName.trim().length() > 0) {
			request.getPortletSession().removeAttribute(SimpleSearchPortlet.APP_ATTR_SEARCH_BEAN, PortletSession.APPLICATION_SCOPE);
			String currentNavigatorName = (String) request.getPortletSession().getAttribute(ATTR_CURRENT_NAVIGATOR_NAME);
			String switchableNavigator = getPreference(request, response, PREF_SWITCHABLE_NAVIGATOR);
			if(null != switchableNavigator && Boolean.valueOf(switchableNavigator) 
					&& !navigatorName.equals(currentNavigatorName)) {
				request.getPortletSession().setAttribute(ATTR_NEW_NAVIGATOR_NAME, navigatorName, PortletSession.APPLICATION_SCOPE);
			}
		}
	}
	
	public static void writeSubFolders(PrintWriter out, ContentProducer cms, Card navigator, boolean isBottomMode) {
		Card area = cms.getCurrentSiteArea();
		
		String parentAreaId = (String) cms.getVariable(ContentProducer.VAR_PARENT_AREA);
		Card parentArea = null;
		if(null != parentAreaId) {
			try {
				parentArea = cms.getSiteArea(new ObjectId(Card.class, Long.parseLong(parentAreaId)));
			} catch (NumberFormatException e) {
				logger.error("Invalid parent area id: " + parentAreaId, e);
			} catch (DataException e) {
				logger.error("Can not retreive parent area with id: " + parentAreaId, e);
			} catch (ServiceException e) {
				logger.error("Can not retreive parent area with id: " + parentAreaId, e);
			}
		}
		
		List cards = getNavigatedAreas(navigator, area, cms/*.getService()*/);
		if (cards == null || cards.size() == 0) {
			TextAttribute html = (TextAttribute) navigator.getAttributeById(ContentIds.ATTR_HTML_EMPTY);
			cms.writeContent(out, html.getValue(), navigator);
			return;
		}
		
		TextAttribute html = (TextAttribute) navigator.getAttributeById(ContentIds.ATTR_HTML_ITEM);
		TextAttribute htmlSel = (TextAttribute) navigator.getAttributeById(ContentIds.ATTR_HTML_SEL_ITEM);
		if (htmlSel == null || htmlSel.getValue() == null || htmlSel.getValue().length() == 0)
			htmlSel = html;
		TextAttribute htmlOpen = (TextAttribute) navigator.getAttributeById(ContentIds.ATTR_HTML_OPEN_ITEM);
		if (htmlOpen == null || htmlOpen.getValue() == null || htmlOpen.getValue().length() == 0)
			htmlOpen = html;
		HashSet open = getOpenAreas(area, cms);
		
		cms.writeContent(out,
				((TextAttribute) navigator.getAttributeById(ContentIds.ATTR_HTML_HEADER)).getValue(),
				navigator);
		int level = 0;
		for (Iterator itr = cards.iterator(); itr.hasNext(); ) {
			Card item = (Card) itr.next();
			
			if(isBottomMode) {
				int levelNew = ((IntegerAttribute) item.getAttributeById(ATTR_LEVEL)).getValue();
				if (levelNew > level) {
					TextAttribute htmlLevel = (TextAttribute) navigator.getAttributeById(ContentIds.ATTR_HTML_LEVEL_START);
					if (htmlLevel != null)
						do {
							cms.writeContent(out, htmlLevel.getValue(), item);
						} while (levelNew > ++level);
				} else if (levelNew < level) {
					TextAttribute htmlLevel = (TextAttribute) navigator.getAttributeById(ContentIds.ATTR_HTML_LEVEL_END);
					if (htmlLevel != null)
						do {
							cms.writeContent(out, htmlLevel.getValue(), item);
						} while (levelNew < --level);
				}
			} else {
				int levelNew = ((IntegerAttribute) item.getAttributeById(ATTR_LEVEL)).getValue();
				if(levelNew > 0) {
					continue;
				}
			}
			
			TextAttribute htmlItem = html;
			if (area != null) {
				if (item.getId().equals(area.getId()) || 
						(!isBottomMode && null != parentArea && item.getId().equals(parentArea.getId()))) {
					htmlItem = htmlSel;
				} else if (open.contains(item.getId())) {
					htmlItem = htmlOpen;
				}
			}
			cms.writeContent(out, htmlItem.getValue(), item);
			if (itr.hasNext())
				cms.writeContent(out,
						((TextAttribute) navigator.getAttributeById(ContentIds.ATTR_HTML_SEPARATOR)).getValue(),
						navigator);
		}
		cms.writeContent(out,
				((TextAttribute) navigator.getAttributeById(ContentIds.ATTR_HTML_FOOTER)).getValue(),
				navigator);
	}

    private static HashSet getOpenAreas(Card current, ContentProducer cms) {
		HashSet open = new HashSet();
		if (current != null) {
			while (true) {
				ObjectId id = ContentUtils.getParentAreaId(current);
				if (id == null)
					break;
				if (open.contains(id)) {
					logger.error("Cyclic reference in site structure found: " + id.getId());
					break;
				}
				open.add(id);
				try {
					current = cms.getContentDataServiceFacade().getSiteArea(id);
				} catch (Exception e) {
					logger.error("Error fetching parent site area " + id.getId(), e);
					break;
				}
			}
		}
		return open;
	}

	private Card getNavigator(PortletRequest request, PortletResponse response, ContentProducer cms)
	{
		String switchableNavigator = getPreference(request, response, PREF_SWITCHABLE_NAVIGATOR);
		if(null == switchableNavigator || !Boolean.valueOf(switchableNavigator)) {
			String id = getPreference(request, response, DEFAULT_NAVIGATOR);
			return getNavigator(cms, id);
		}
		
		String newNavigatorId = (String) request.getPortletSession().getAttribute(ATTR_NEW_NAVIGATOR_NAME, PortletSession.APPLICATION_SCOPE);
		String currentNavigatorId = (String) request.getPortletSession().getAttribute(ATTR_CURRENT_NAVIGATOR_NAME, PortletSession.APPLICATION_SCOPE);
		
		String navigatorName = DEFAULT_NAVIGATOR;
		if(null != newNavigatorId && !newNavigatorId.equals(currentNavigatorId)) {
			navigatorName = newNavigatorId;
		} else if(null != currentNavigatorId) {
			navigatorName = currentNavigatorId;
		}
		
		if(navigatorName.equals(currentNavigatorId)) {
			return (Card) request.getPortletSession().getAttribute(ATTR_NAVIGATOR, PortletSession.APPLICATION_SCOPE);
		} else {
			//NavigatorConfig navigator = (NavigatorConfig) AppContext.getApplicationContext().getBean(navigatorName);
			String id = getPreference(request, response, navigatorName);
			Card navigatorCard = getNavigator(cms, id);
			
			request.getPortletSession().setAttribute(ATTR_CURRENT_NAVIGATOR_NAME, navigatorName, PortletSession.APPLICATION_SCOPE);
			request.getPortletSession().setAttribute(ATTR_NAVIGATOR, navigatorCard, PortletSession.APPLICATION_SCOPE);
			request.getPortletSession().removeAttribute(ATTR_NEW_NAVIGATOR_NAME, PortletSession.APPLICATION_SCOPE);
			
			return navigatorCard;
		}
	}
	
	public static Card getNavigator(ContentProducer cms, String id) {
		if (id == null) {
			return null;
		}
		
		ObjectId navId = new ObjectId(Card.class, new Long(id));
		Card navigator = null;
		try {
			navigator = cms.getContentDataServiceFacade().getViewCardById(navId);
		} catch (DataException e) {
			logger.error("Error retrieving navigator card", e);
		} catch (ServiceException e) {
			logger.error("Error retrieving navigator card", e);
		}
		
		return navigator;
	}
	
	private String getPreference(PortletRequest request, PortletResponse response, String preferenceName) {
		String preferenceValue = request.getPreferences().getValue(preferenceName, null);
		
		if (preferenceValue == null){
			preferenceValue = Portal.getFactory().getPortletService().getPageProperty(preferenceName, request, response);
		}
		
		if (preferenceValue == null) {
			preferenceValue = getInitParameter(preferenceName);
		}
		
		return preferenceValue;
	}
	
	private Card saveNavigator(PortletRequest request, String navigatorId, Card navigator) {
		request.getPortletSession().setAttribute(ATTR_CURRENT_NAVIGATOR_NAME, navigatorId, PortletSession.APPLICATION_SCOPE);
		request.getPortletSession().setAttribute(ATTR_NAVIGATOR, navigator, PortletSession.APPLICATION_SCOPE);
		request.getPortletSession().removeAttribute("newNavigatorName", PortletSession.APPLICATION_SCOPE);
		return navigator;
	}

	private static List getNavigatedAreas(Card navigator, Card current, ContentProducer cms)//DataServiceBean service)
	{
		// determining start area
		Attribute attr = navigator.getAttributeById(ContentIds.ATTR_ROOT_TYPE);
		ReferenceValue rootType = ((ListAttribute) attr).getValue();
		List start = null;
		if (rootType == null || ContentIds.VAL_TYPE_SELECTED.equals(rootType.getId())) {
			attr = navigator.getAttributeById(ContentIds.ATTR_ROOT_AREA);
			if (attr == null || !(attr instanceof CardLinkAttribute)) {
				logger.error("Root areas not defined in navigator " + navigator.getId().getId());
				return null;
			}
			start = cms.getLinkedCards(navigator, attr);
			ContentUtils.sortCards(start, ContentIds.ATTR_ORDER, true);
			//fillAttributeNames(start, (List) ((CardLinkAttribute) attr).getColumns());
		} else if (ContentIds.VAL_TYPE_CURRENT.equals(rootType.getId())) {
			start = Collections.singletonList(current);
		} else if (ContentIds.VAL_TYPE_ROOT.equals(rootType.getId())) {
			Card area = current;
			while (true) {
				ObjectId parentId = ContentUtils.getParentAreaId(area);
				if (parentId == null)
					break;
				try {
					area = cms.getContentDataServiceFacade().getSiteArea(parentId);
				} catch (Exception e) {
					logger.error("Error fetching parent site area " + parentId.getId(), e);
					break;
				}
			}
			start = Collections.singletonList(area);
		}

		int openLevels = ((IntegerAttribute) navigator.getAttributeById(ContentIds.ATTR_LEVELS_OPEN)).getValue();
		if (openLevels > 0) {
			if (start.size() > 1)
				logger.error("Displaying parents for multiple start areas not supported");
			else {
				Card area = (Card) start.iterator().next();
				for (int i = 0; i < openLevels; i++) {
					ObjectId parentId = ContentUtils.getParentAreaId(area);
					if (parentId == null) {
						openLevels = i;
						break;
					}
					try {
						area = cms.getContentDataServiceFacade().getSiteArea(parentId);
					} catch (Exception e) {
						logger.error("Error fetching parent site area " + parentId.getId(), e);
						break;
					}
				}
				start = Collections.singletonList(area);
			}
		}
		
		// collecting upper level areas
		ArrayList areas = new ArrayList();
		int level = 0;
		int maxLevels = ((IntegerAttribute) navigator.getAttributeById(ContentIds.ATTR_LEVELS_UP)).getValue();
		if (maxLevels > 0 && start.size() > 1)
			logger.error("Displaying parents for multiple start areas not supported");
		else {
			maxLevels -= openLevels;
			Card area = (start.iterator().hasNext())?(Card) start.iterator().next():null;
			Stack parents = new Stack();
			while (maxLevels-- > 0&&area!=null) {
				ObjectId parentId = ContentUtils.getParentAreaId(area);
				if (parentId == null)
					break;
				try {
					area = cms.getContentDataServiceFacade().getSiteArea(parentId);
				} catch (Exception e) {
					logger.error("Error fetching parent site area " + parentId.getId(), e);
					break;
				}
				parents.push(area);
			}
			while (!parents.empty())
				addArea(areas, (Card) parents.pop(), level++, 0, cms);
		}
		
		// collecting lower level areas
		maxLevels = ((IntegerAttribute) navigator.getAttributeById(ContentIds.ATTR_LEVELS_DOWN)).getValue();
		for (Iterator itr = start.iterator(); itr.hasNext(); ) {
			Card area = (Card) itr.next();
			addArea(areas, area, level, maxLevels + openLevels, cms);
		}
		
		// unfolding current area, if needed
		attr = navigator.getAttributeById(ContentIds.ATTR_CURRENT);
		ReferenceValue currShow = ((ListAttribute) attr).getValue();
		if (currShow == null || ContentIds.VAL_CURR_NOSHOW.equals(currShow.getId()))
			return areas;
		Stack parents = new Stack();
		if (ContentIds.VAL_CURR_CHILDREN.equals(currShow.getId())) {
			try {
				List children = getChildren(current.getId(), cms);
				if (children.size() > 0 &&
						findArea(areas, 0, ((Card) children.iterator().next()).getId()) == -1)
					parents.push(current.getId());
			} catch (Exception e) {
				logger.error("Error retrieving children for area " + current.getId().getId(), e);
			}
		}
		while (findArea(areas, 0, current.getId()) == -1) {
			ObjectId parentId = ContentUtils.getParentAreaId(current);
			if (parentId == null)
				break;
			parents.push(parentId);
			try {
				current = cms.getContentDataServiceFacade().getSiteArea(parentId);
			} catch (Exception e) {
				logger.error("Error retrieving parent for area " + parentId.getId(), e);
				break;
			}
		}
		addChildren(areas, parents, cms);
		return areas;
	}
	

    private static void addArea(List list, Card area, int level, int childLevels, ContentProducer cms)
	{
		try {
			area = cms.getContentDataServiceFacade().getSiteArea(area.getId());	//***** Too slow...
			if (area.getTemplate() == null)
				area.setTemplate(ContentIds.TPL_AREA);
			IntegerAttribute levelAttr = (IntegerAttribute) area.getAttributeById(ATTR_LEVEL);
			if (levelAttr == null) {
				levelAttr = (IntegerAttribute) DataObject.createFromId(ATTR_LEVEL);
				area.getAttributes().add(levelAttr);
			}
			levelAttr.setValue(level);
			list.add(area);
			if (childLevels > 0 && ContentIds.TPL_AREA.equals(area.getTemplate())) {
				List children = getChildren(area.getId(), cms);
				ContentUtils.sortCards(children, ContentIds.ATTR_ORDER, true);
				for (Iterator itr = children.iterator(); itr.hasNext(); ) {
					addArea(list, (Card) itr.next(), level + 1, childLevels - 1, cms);
				}
			}
        } catch (Exception e) {
            if ( e instanceof DataException && "general.access".equals(((DataException) e).getMessageID())){
                logger.error("Error retrieving children for area " + area.getId().getId() + ": " + e.getMessage());
            }else{
                logger.error("Error retrieving children for area " + area.getId().getId(), e);
            }
        }
	}
	
	private static void addChildren(List list, Stack areas, ContentProducer cms)
	{
		try {
			int idx = 0;
			while (areas.size() > 0) {
				ObjectId areaId = (ObjectId) areas.pop();
				idx = findArea(list, idx, areaId);
				if (idx == -1) {
					logger.warn("Current site area is not in navigation tree");
					return;
				}
				Card area = (Card) list.get(idx++);
				int level = ((IntegerAttribute) area.getAttributeById(ATTR_LEVEL)).getValue();
				List children = getChildren(areaId, cms);
				ContentUtils.sortCards(children, ContentIds.ATTR_ORDER, true);
				int i = idx;
				for (Iterator itr = children.iterator(); itr.hasNext(); ) {
					area = (Card) itr.next();
					area = cms.getContentDataServiceFacade().getSiteArea(area.getId());	// added 17.12.2009
					//area.setTemplate(ContentIds.TPL_AREA);
					IntegerAttribute levelAttr = (IntegerAttribute) area.getAttributeById(ATTR_LEVEL);
					if (levelAttr == null) {
						levelAttr = DataObject.createFromId(ATTR_LEVEL);
						area.getAttributes().add(levelAttr);
					}
					levelAttr.setValue(level + 1);
					list.add(i++, area);
				}
			}
		} catch (Exception e) {
			logger.error("Error retrieving children of site area", e);
		}
	}
	
	private static int findArea(List list, int start, ObjectId areaId)
	{
		for (int i = start; i < list.size(); i++) {
			Card area = (Card) list.get(i);
			if (areaId.equals(area.getId()))
				return i;
		}
		return -1;
	}
	
	private static List getChildren(ObjectId areaId, ContentProducer cms)
	{
		return cms.getContentDataServiceFacade().getChildren(areaId);
	}


}
