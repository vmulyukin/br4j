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
package com.aplana.dbmi.showlist;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.PortletService;
import com.aplana.dbmi.action.*;
import com.aplana.dbmi.model.*;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.portlet.CardImportPortlet;
import com.aplana.dbmi.search.SearchFilterPortlet;
import com.aplana.dbmi.service.*;
import com.aplana.dbmi.showlist.MIShowListPortletSessionBean.GroupExecutionMode;
import com.aplana.dbmi.showlist.MIShowListPortletSessionBean.GroupResolutionMode;
import com.aplana.dbmi.util.JspUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.displaytag.pagination.PaginatedList;
import org.displaytag.properties.SortOrderEnum;
import org.displaytag.util.SortingState;
import org.jboss.portal.portlet.aspects.portlet.ContextDispatcherInterceptor;
import org.jboss.portal.portlet.invocation.PortletInvocation;

import javax.ejb.CreateException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.portlet.*;
import javax.rmi.PortableRemoteObject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.rmi.RemoteException;
import java.text.MessageFormat;
import java.util.*;

/**
 * A sample portlet based on GenericPortlet
 */
public class MIShowListPortlet extends GenericPortlet {
	
	protected final Log logger = LogFactory.getLog(getClass());

	// JSP folder name
	public static final String JSP_FOLDER = "/WEB-INF/jsp/html/";

	// JSP file name to be rendered
	public static final String CARD_LIST_JSP		= "MIShowListPortletView";

	// Bean name for the portlet session
	public static final String SESSION_BEAN  = "MIShowListPortletSessionBean";

	// Attribute name for application-scoped search object
	public static final String SEARCH_BEAN = "SEARCH_BEAN";

	// Action name for submit form
	public static final String PRINT_ACTION    		= "MI_PRINT_ACTION";
	public static final String MOVE_ACTION    		= "MI_MOVE_ACTION";
	public static final String IMPORT_ACTION    	= "MI_IMPORT_ACTION";
	public static final String CONFIRM_MOVE_ACTION    = "MI_CONFIRM_MOVE_ACTION";
	public static final String REJECT_MOVE_ACTION 	= "MI_REJECT_MOVE_ACTION";
	public static final String MOVE_PAGE_ACTION    		= "MI_MOVE_PAGE_ACTION";
	public static final String ADD_FAVORITES_ACTION = "MI_ADD_FAVORITES_ACTION";
	public static final String REMOVE_FAVORITES_ACTION = "MI_REMOVE_FAVORITES_ACTION";
	public static final String BACK_TO_REQUEST_ACTION = "MI_BACK_TO_REQUEST_ACTION";
	public static final String REFRESH_ACTION = "MI_REFRESH_ACTION";
	public static final String CAN_IMPORT_CARDS = "canImportCards";
	public static final String EDIT_ACCESS_ROLES = "editAccessRoles";
	public static final String CUSTOM_IMPORT_TITLE = "customImportTitle";
	public static final String DOWNLOAD_IMPORT_TEMPLATE = "downloadImportTemplate";
	public static final String CAN_GROUP_EXECT = "canGroupExection";
	public static final String GROUP_RESOLUTION_MODE = "groupResolutionMode";

	public static final String ACTION_FIELD 		= "MI_ACTION_FIELD";

	public static final String ENTITY_ID_FIELD 	= "MI_ENTITY_ID_FIELD";

//	public static final String TITLE_COLUMN_KEY = "TITLE_COLUMN_KEY";
//	public static final String SORTABLE_COLUMN_KEY = "SORTABLE_COLUMN_KEY";
//	public static final String LINK_COLUMN_KEY = "LINK_COLUMN_KEY";
//	public static final String LENGTH_COLUMN_KEY = "LENGTH_COLUMN_KEY";

	public static final String MSG_PARAM_NAME = "MI_MSG_PARAM_NAME";

//    public static final String CONFIG_FILE_PREFIX = "config/mts/";
	public static final String CONFIG_FILE_PREFIX = "dbmi/";

	public static final String INIT_SEARCH_PARAM_KEY = "defaultSearch";
	public static final String FAVORITES_PARAM_KEY = "isFavoritesPortlet";
	public static final String SHOW_ROW_EDITVIEW_ICON_PARAM_KEY = "enShowRowEditViewIcon";

	public static final String TABLE_ID = "dataItem";

	public static final String PREF_SHOW_HEAD = "showHeader";
	public static final boolean PREF_DEFAULT_SHOW_HEAD = true;

	public static final String PREF_PG_SIZE = "pgSize";
	public static final int PREF_DEFAULT_PG_SIZE = 20;

	public static final String PREF_LINK_URL = "link.Url";
	public static final String PREF_LINK_PG = "link.Page";

	// public static final String PREF_LIST_COLUMNS_HAS_LINK = "listColumnsHasLink";
	// public static final String PREF_DEFAULT_COLUMNS_HAS_LINK = "NAME";

	public static final String PREF_HEDAER_JSP  = "header.jsp";
	public static final String PREF_HEDAER_TEXT = "header.text";

	public static final String PREF_SHOW_BTN_REFRESH = "showBtnRefresh";
	public static final boolean PREF_DEFAULT_SHOW_BTN_REFRESH = true;

	// ������� ���������� ������� ������ ������� ��������
	public static final String PREF_SHOW_BTN_CREATE = "showBtnCreate";

	public static final String PREF_SHOW_TOOLBAR = "showToolbar";
	public static final boolean PREF_DEFAULT_SHOW_TOOBAR = true;

	public static final String BACK_URL_ATTR = "backURL";
	public static final String VIEW_MODE = "viewMode";
		
	public static final String CLEAR_REQ = "ClearShowListParam";
	public static final String CLEAR_ATTR = "clear";
	
	public static final String FAVORITES_TITLE_ID_NAME = "favorites.name";

	public static final String ATTR_DELIMITER = "cardLinkDelimiter";		// �������� ��������� � ���������� �������� ��� ���������� ������� � ��������� CardLink, Person � TypedCardLink (���������� � SearchAdapter)
	public static final String PREF_DEFAULT_ATTR_DELIMITER = ", ";			// ��� �������� �� ���������

	private static final String SEARCH_CONTROL_ATTRIBUTE_NAME = "searchControl"; // ��� �������� �������� ������
	private static final String CLEAR_SEARCH_TEXT_ACTION = "clearSearchText"; // ������� ���� ������

	private boolean isFavoritesViewPortlet = false;

	private PortletService portletService = null;

	//------------------moveMode
	private static final ObjectId DELO = ObjectId.predefined(CardState.class, "delo");
	public static final String MOVE_MODE = "moveMode";
	public static final String NEED_CONFIRM = "MI_NEED_CONFIRM";	
	//------------
	
	//------------------refresh options
	private static final String REFRESH_CARDS_LIST = "refresh_cards_list";
	private static final String DEFAULT_VALUE_REFRESH_DATA = "false";
	private String refresh_cards_list = null;
	//------------------
	/**
	 * @see javax.portlet.Portlet#init()
	 */
	@Override
	public void init() throws PortletException {
		super.init();
		String isFavorites = getInitParameter(FAVORITES_PARAM_KEY);
		portletService = Portal.getFactory().getPortletService();

		isFavoritesViewPortlet = "true".equalsIgnoreCase(isFavorites);
	}

	/**
	 * Serve up the <code>view</code> mode.
	 *
	 * @see javax.portlet.GenericPortlet#doView(javax.portlet.RenderRequest, javax.portlet.RenderResponse)
	 */
	@Override
	public void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
		// Set the MIME type for the render response
		response.setContentType(request.getResponseContentType());
		// Set user locale
		ContextProvider.getContext().setLocale(request.getLocale());
		
		PortletSession session = request.getPortletSession();
		PortletInvocation portletInvocation = (PortletInvocation) request.getAttribute(ContextDispatcherInterceptor.REQ_ATT_COMPONENT_INVOCATION);
        HttpServletRequest req = portletInvocation.getDispatchedRequest();//������ ����� �������� ��������� �� ��� �������, � ������ ������� ��� �� ��������
        if (request.getUserPrincipal() != null) {
            String userName = req.getParameter(DataServiceBean.USER_NAME);
            if (userName != null) {
                DataServiceBean service = new DataServiceBean();
                service.setUser(new SystemUser());
                service.setAddress("localhost");
                GetDelegateListByLogin action = new GetDelegateListByLogin();
                action.setLogin(request.getUserPrincipal().getName());
                try {
                    List<String> list = service.doAction(action);
                    if (list.contains(userName)) {
                        session.setAttribute(DataServiceBean.USER_NAME, userName, PortletSession.APPLICATION_SCOPE);
                    } else if (request.getUserPrincipal().getName().equals(userName)) {
                        session.removeAttribute(DataServiceBean.USER_NAME,  PortletSession.APPLICATION_SCOPE);
                    }
                } catch (Exception e) {
                    logger.error("Error while process GetDelegateListByLogin\n", e);
                }
            }
        }
        if (req.getParameter("logged") != null) {
            session.removeAttribute(DataServiceBean.USER_NAME,  PortletSession.APPLICATION_SCOPE);
        }
		// Check if portlet session exists
		MIShowListPortletSessionBean sessionBean = getSessionBean(request, response);
		if( sessionBean==null ) {
			response.getWriter().println("<b>NO PORTLET SESSION YET</b>");
			return;
		}
		sessionBean.setFavoritesViewPortlet(isFavoritesViewPortlet);
		sessionBean.setResourceBundle(ResourceBundle.getBundle("com.aplana.dbmi.card.nl.CardPortletResource", request.getLocale()));
		
		boolean clearSort = Boolean.parseBoolean((String) session.getAttribute(SearchFilterPortlet.CLEAR_SORT_ATTR, PortletSession.APPLICATION_SCOPE));
		if (clearSort) {
			SortingState.saveSortingStateToSession(null, TABLE_ID, request.getPortletSession());
			session.removeAttribute(SearchFilterPortlet.CLEAR_SORT_ATTR,PortletSession.APPLICATION_SCOPE);
		}
		
		configureSortingState(request, clearSort);

		Search searchAction = (Search) session.getAttribute(SEARCH_BEAN, PortletSession.APPLICATION_SCOPE);
		
		String clearAttr = portletService.getUrlParameter(request, CLEAR_ATTR);
		if(clearAttr == null){
			clearAttr = (String) session.getAttribute(CLEAR_ATTR, PortletSession.APPLICATION_SCOPE);
		}
		session.removeAttribute(CLEAR_ATTR,PortletSession.APPLICATION_SCOPE);
		
		if ("true".equals(clearAttr)) {
			searchAction = null;//clear bean
		} else if (searchAction == null){ 
			searchAction = sessionBean.getExecSearch();
		}else{
			sessionBean.reset();
			sessionBean.setExecSearch(searchAction);
			sessionBean.setRequestViewPortlet(true);
			SortingState.saveSortingStateToSession(null, TABLE_ID, session);
			session.removeAttribute(SEARCH_BEAN, PortletSession.APPLICATION_SCOPE);
		}
		
		String viewMode = portletService.getPageProperty(VIEW_MODE, request, response);
		if(viewMode != null && !viewMode.isEmpty()){
			session.setAttribute(VIEW_MODE, viewMode, PortletSession.APPLICATION_SCOPE);
		} else {
			session.removeAttribute(VIEW_MODE,PortletSession.APPLICATION_SCOPE);
		}
		
		if (searchAction != null) {
			try {
				if(SortingState.loadSortingStateFromSession(TABLE_ID, session) == null){
					setDefaultSortingState(searchAction, session);
				}
				loadCardList(request, response, searchAction);
				setShowBtnRefresh(request.getPreferences(), sessionBean);
			} catch (Exception e) {
				logger.error("Error in 'doView'", e);
				String msg = getPortletConfig().getResourceBundle(request.getLocale()).getString("db.side.error.msg") + e.getMessage();
				sessionBean.setMessage(msg);
			}
			
		} else {
			initBean(request, response);
		}

		saveSessionBeanForServlet(request, response, sessionBean);
/* TODO
    	try {
			InitialContext ctx = new InitialContext();
			PortletServiceHome home = (PortletServiceHome) ctx.lookup("portletservice/" +
					PortalURLGenerationService.class.getName());
			PortalURLGenerationService service = (PortalURLGenerationService) home.getPortletService(PortalURLGenerationService.class);
			PortalURLWriter urlCreator = service.getPortalURLWriter(request, response);
			request.setAttribute(PortalURLWriter.class.getName(), urlCreator);
		} catch (PortletServiceUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
*/
		
		try {
			PortletURL backPURL = response.createActionURL();
			backPURL.setParameters(getbackUrlParameters(request.getParameterMap(), clearSort));
			backPURL.setParameter(MIShowListPortlet.ACTION_FIELD, MIShowListPortlet.REFRESH_ACTION);
			backPURL = addRefreshData(backPURL, request);
			request.setAttribute(BACK_URL_ATTR, URLEncoder.encode(backPURL.toString(), "UTF-8"));

		} catch (UnsupportedEncodingException e) {
			logger.error("Error in 'initBean'", e);
		}

		// Invoke the JSP to render
		PortletRequestDispatcher rd = getPortletContext().getRequestDispatcher(getJspFilePath(request, CARD_LIST_JSP));
		rd.include(request,response);
	}
	
	private PortletURL addRefreshData(PortletURL backPURL, RenderRequest request) 
			throws UnsupportedEncodingException
	{
		final String backURL = URLEncoder.encode(backPURL.toString(), "UTF-8");
		if (backURL != null && backURL.toLowerCase().indexOf(REFRESH_CARDS_LIST.toLowerCase()) == -1) {
			final PortletPreferences prefs = request.getPreferences();
			String refresh_data = prefs.getValue(REFRESH_CARDS_LIST, null);
			if(refresh_data == null)
				refresh_data = DEFAULT_VALUE_REFRESH_DATA;
			backPURL.setParameter(REFRESH_CARDS_LIST, refresh_data);
		}
		return backPURL;
	}
	
	private Map getbackUrlParameters(Map rMap, boolean clearSort){
		Map res = new HashMap(rMap);
		res.remove("page");
		res.remove("sort");
		res.remove("dir");
		return res;
	}

	/**
	 * Process an action request.
	 *
	 * @see javax.portlet.Portlet#processAction(javax.portlet.ActionRequest, javax.portlet.ActionResponse)
	 */
	@Override
	public void processAction(ActionRequest request, ActionResponse response) throws PortletException, java.io.IOException {
		ContextProvider.getContext().setLocale(request.getLocale());
		String action = request.getParameter(ACTION_FIELD);
		if( action != null ) {
			MIShowListPortletSessionBean sessionBean = getSessionBean(request, response);
			if(action.equals(PRINT_ACTION)) {
				sessionBean.setPrintMode(true);
			}
			if(action.equals(ADD_FAVORITES_ACTION)) {
				addFavoritesHandler(request, response);
			}
			if(action.equals(REMOVE_FAVORITES_ACTION)) {
				removeFavoritesHandler(request, response);
				// reload
				initBean(request, response);
			}
			if(action.equals(REFRESH_ACTION)) {
				//SortingState.saveSortingStateToSession(null, TABLE_ID, request.getPortletSession());
				
				//-------------------RefreshList
				refresh_cards_list = request.getParameter(REFRESH_CARDS_LIST);
			}
			if(action.equals(MOVE_ACTION)) {
					response.setRenderParameter(NEED_CONFIRM,
						Boolean.TRUE.toString());
				return;
			}
			if(action.equals(MOVE_PAGE_ACTION)) {
				moveAllCards(request, response, false);
			}
			if(action.equals(CONFIRM_MOVE_ACTION)) {
				moveAllCards(request, response, true);
			}
			if(action.equals(IMPORT_ACTION)){
				importNewCards(request, response);
				return;
			}
			if(action.equals(REJECT_MOVE_ACTION)) {
				response.setRenderParameter(NEED_CONFIRM,
						Boolean.FALSE.toString());
				return;
			}
			if(action.equals(BACK_TO_REQUEST_ACTION)) {
				// reload
				clearSearchField(request, response);
				SortingState.saveSortingStateToSession(null, TABLE_ID, request.getPortletSession());
				initBean(request, response);
			}
		}
		
		JspUtils.removeParameters(request, response, ACTION_FIELD, REFRESH_CARDS_LIST);
	}

	private void clearSearchField(PortletRequest request, PortletResponse response) {
		request.getPortletSession().setAttribute(SEARCH_CONTROL_ATTRIBUTE_NAME, 
				CLEAR_SEARCH_TEXT_ACTION, 
				PortletSession.APPLICATION_SCOPE); // ������� ���� ������
	}

	private void removeFavoritesHandler(ActionRequest request, ActionResponse response) {
		MIShowListPortletSessionBean sessionBean = getSessionBean(request, response);
		String entityId = request.getParameter(ENTITY_ID_FIELD);
		String msg = "";
		if (entityId != null) {
			try {
				RemoveFromFavorites removeFavoritesAction = new RemoveFromFavorites();
				removeFavoritesAction.setCard(new ObjectId(Card.class, Long.parseLong(entityId)));
				sessionBean.getServiceBean(request).doAction(removeFavoritesAction);

				msg = getPortletConfig().getResourceBundle(request.getLocale()).getString("remove.fovorites.success.msg");

			} catch (Exception e) {
				logger.error("Error in 'removeFavoritesHandler'", e);
				msg = getPortletConfig().getResourceBundle(request.getLocale()).getString("db.side.error.msg") + e.getMessage();
			}
		}
		sessionBean.setMessage(msg);
//		response.setRenderParameter(MSG_PARAM_NAME, msg);
	}


	private void addFavoritesHandler(ActionRequest request, ActionResponse response) {
		MIShowListPortletSessionBean sessionBean = getSessionBean(request, response);
		String entityId = request.getParameter(ENTITY_ID_FIELD);
		String msg = "";
		if (entityId != null) {
			try {
				AddToFavorites addFavoritesAction = new AddToFavorites();
				addFavoritesAction.setCard(new ObjectId(Card.class, Long.parseLong(entityId)));
				sessionBean.getServiceBean(request).doAction(addFavoritesAction);

				msg = getPortletConfig().getResourceBundle(request.getLocale()).getString("add.fovorites.success.msg");

			} catch (Exception e) {
				logger.error("Error in 'addFavoritesHandler'", e);
				msg = getPortletConfig().getResourceBundle(request.getLocale()).getString("db.side.error.msg") + e.getMessage();
			}
		}
		sessionBean.setMessage(msg);
//		response.setRenderParameter(MSG_PARAM_NAME, msg);
	}

	private void moveAllCards(ActionRequest request, ActionResponse response, boolean fullList) {

		response.setRenderParameter(NEED_CONFIRM,
				Boolean.FALSE.toString()); 
		final MIShowListPortletSessionBean sessionBean = getSessionBean(
				request, response);
		DataServiceBean serviceBean = new AsyncDataServiceBean();
		serviceBean.setUser(request.getUserPrincipal());
		HashSet<Exception> errorCards = new HashSet<Exception>();
		
		Search deloSearch = sessionBean.getDeloSearch();
		if(deloSearch == null){
			sessionBean.setMessage("DeloSearch action is null");
			return;
		}
		List<Card> cards = new ArrayList<Card>();
		try{
			if(fullList){
				deloSearch.getFilter().setPage(1);
				final PortletService psrvc = Portal.getFactory().getPortletService();
				final String moveCount = psrvc.getPageProperty(MOVE_MODE, request,
						response);
				deloSearch.getFilter().setPageSize(Integer.parseInt(moveCount));
			}
			cards = ((SearchResult)serviceBean.doAction(deloSearch)).getCards();
		} catch(Exception e) {
			logger.error("Can't execute deloSearch action", e);
			sessionBean.setMessage(e.getMessage());
			return;
		}

		String msg="";

		int countSuccess = 0;
		if (!cards.isEmpty()){
			for(Card card : cards) {
				ChangeState changeAction = new ChangeState();
				changeAction.setCard(card);
				changeAction.setWorkflowMove(sessionBean.
						getDeloWorkflowMove(card.getTemplate(), card.getState()));
				if(changeAction.getWorkflowMove() == null){
					errorCards.add(new Exception(getPortletConfig().getResourceBundle(request.getLocale())
							.getString("move.cards.no.rules.msg") + card.getId().getId()));
					continue;
				}
				boolean locked = false;
				try {
					LockObject lock = new LockObject(card);
					lock.setWaitTimeout(1);
					serviceBean.doAction(lock);
					locked = true;
					if (serviceBean.canDo(changeAction)) {
						serviceBean.doAction(changeAction);
						countSuccess++;
					} else {
						errorCards.add(new Exception(getPortletConfig().getResourceBundle(request.getLocale())
								.getString("move.cards.no.rules.msg") + card.getId().getId()));
					}
				} catch (ObjectLockedException e) {
					logger.error("Can't lock card " + card.getId() + " before moving to archive\n", e);
					errorCards.add(e);
				} catch (DataException e) {
					logger.error("Can't move card " + card.getId() + " to archive\n", e);
					errorCards.add(e);
				} catch (ServiceException e) {
					logger.error("Can't move card " + card.getId() + " to archive\n", e);
					errorCards.add(e);
				} finally {
					if (locked)
						try {
							serviceBean.doAction(new UnlockObject(card));
						} catch (Exception e) {
							logger.error("Can't unlock card " + card.getId() + " after moving into archive", e);
						}
				}
			}
		}
		
		msg = getPortletConfig().getResourceBundle(
				request.getLocale()).getString(
				"move.cards.success.msg");
        msg = MessageFormat.format(msg, countSuccess);
		if (!errorCards.isEmpty()) {
			for (Exception e : errorCards) {
				msg = msg + "<br>" + e.getMessage();
			}
		}
		sessionBean.setMessage(msg);
	}
	
	private void importNewCards(ActionRequest request, ActionResponse response) throws IOException {
		PortletService portletService = Portal.getFactory().getPortletService();
		MIShowListPortletSessionBean sessionBean = getSessionBean(request, response);
		String cardImportPageId = "dbmi.Card.Import";
		final HashMap urlParams = new HashMap();
		String templateId = sessionBean.getCurrentTemplate() != null ? sessionBean.getCurrentTemplate().toString() :
					null;
		if (templateId==null || templateId.isEmpty()){
			String msg = getPortletConfig().getResourceBundle(request.getLocale()).getString("tool.import.invalid.template");
			logger.error(msg);
			sessionBean.setMessage(msg);
			return;
		}
		urlParams.put(CardImportPortlet.CARD_TEMPLATE_PARAM, templateId);
		if (sessionBean.getCustomImportTitle()!=null){
			urlParams.put(CardImportPortlet.CUSTOM_IMPORT_TITLE_PARAM, sessionBean.getCustomImportTitle());
		}
		urlParams.put(CardImportPortlet.BACK_URL_FIELD, request.getParameter(BACK_URL_ATTR));
		if (templateId.equals("10")){
			urlParams.put(CardImportPortlet.CHECK_DOUBLETS_PARAM, true);
			urlParams.put(CardImportPortlet.UPDATE_DOUBLETS_PARAM, false);
			urlParams.put(CardImportPortlet.UPDATE_DOUBLETS_SUPPORT_PARAM, true);
			urlParams.put(CardImportPortlet.CHECK_DOUBLETS_SUPPORT_PARAM, false);
		} else {
			// ��� ���� �������� �������� �� ����� ��� ������� ��������� (������ ��������� � ��� ������� �� ���������), �� ���� ����� �������� ��������� ��� ��� ��������� (������� ��� ������, ��� ������������5�� �������� �� ��������� ���� ��� ������ ����������, �� ��� ���� ���� ���� ����������� ��������� ����� ��� �������)
			urlParams.put(CardImportPortlet.CHECK_DOUBLETS_PARAM, false);
			urlParams.put(CardImportPortlet.UPDATE_DOUBLETS_PARAM, false);
			urlParams.put(CardImportPortlet.UPDATE_DOUBLETS_SUPPORT_PARAM, true);
			urlParams.put(CardImportPortlet.CHECK_DOUBLETS_SUPPORT_PARAM, false);
		}
		final String importCardURL = portletService.generateLink(cardImportPageId, "dbmi.Card.Import.Card", urlParams, request, response);
		response.sendRedirect(importCardURL);
	}
	
	/**
	 * Get SessionBean from portlet session.
	 *
	 * @param request PortletRequest
	 * @return MIShowListPortletSessionBean
	 */
	private MIShowListPortletSessionBean getSessionBean(PortletRequest request, PortletResponse response) {
		PortletSession session = request.getPortletSession();
		if( session == null )
			return null;
		MIShowListPortletSessionBean sessionBean = (MIShowListPortletSessionBean)session.getAttribute(SESSION_BEAN);
		if( sessionBean == null ) {
			sessionBean = new MIShowListPortletSessionBean();
			session.setAttribute(SESSION_BEAN,sessionBean);
			initBean(request, response);
		}
		String importTemplate = sessionBean.getDownloadImportTemplate();
		if (importTemplate != null && !importTemplate.equals("")) {
			try {
				sessionBean.setCsvTemplateCardId(findCsvTemplateCardId(sessionBean.getServiceBean(request), importTemplate.trim()));
			} catch (Exception e) {
				logger.error(e);
				sessionBean.setMessage(e.getMessage());
			}
		}
		return sessionBean;
	}
	
	/**
	 * Get SessionBean from servlet session.
	 *
	 * @param request PortletRequest
	 * @return MIShowListPortletSessionBean
	 */
	public static MIShowListPortletSessionBean getSessionBean(HttpServletRequest request, String namespace) {
		HttpSession session = request.getSession();
		if( session == null )
			return null;
		MIShowListPortletSessionBean sessionBean = (MIShowListPortletSessionBean)session.getAttribute(SESSION_BEAN + "." + namespace);
		if( sessionBean == null ) {
			return null;
		}
		return sessionBean;
	}
	
	private static void saveSessionBeanForServlet(RenderRequest request, RenderResponse response, MIShowListPortletSessionBean sessionBean) {
		PortletSession session = request.getPortletSession();
		String namespace = response.getNamespace();
		session.setAttribute(SESSION_BEAN + '.' + namespace, sessionBean, PortletSession.APPLICATION_SCOPE);
	}

	private void initBean(PortletRequest request, PortletResponse response) {
		final PortletSession session = request.getPortletSession();
		final MIShowListPortletSessionBean sessionBean = getSessionBean(request, response);
		final DataServiceBean serviceBean = sessionBean.getServiceBean(request);
		final PortletService psrvc = Portal.getFactory().getPortletService();
		try {
			sessionBean.reset();
			sessionBean.setRequestViewPortlet(false);

			if (isFavoritesViewPortlet) {
				final List<ObjectId> result = serviceBean.doAction(new ListFavorites());
				final Search searchAction = new Search();
				searchAction.setByCode(true);
				searchAction.setWords(ObjectIdUtils.numericIdsToCommaDelimitedString(result));
				searchAction.setNameRu(ResourceBundle.getBundle(ContextProvider.MESSAGES, ContextProvider.LOCALE_RUS)
						.getString(FAVORITES_TITLE_ID_NAME));
				searchAction.setNameEn(ResourceBundle.getBundle(ContextProvider.MESSAGES, ContextProvider.LOCALE_ENG)
						.getString(FAVORITES_TITLE_ID_NAME));
//				loadCardList(request, response, searchAction);
				setDefaultSortingState(searchAction, session);
				sessionBean.setExecSearch(searchAction);
			} else 
			{
				final String initSearchFile = psrvc.getPageProperty(INIT_SEARCH_PARAM_KEY, request, response);
				System.out.println("MIShowListPortlet.initBean: initSearchFile=" + initSearchFile);
				if (initSearchFile != null) {
					//������ xml �����, �������� search action
					final Search searchAction = MIShowListHelper.createSearchFromFullParseXml(CONFIG_FILE_PREFIX+initSearchFile);
					setDefaultSortingState(searchAction, session);
					sessionBean.setExecSearch(searchAction);
//					loadCardList(request, response, searchAction);
				}
			}

			// ������ ��������������/�������� � ������ ������ ������...
			final String sShowRowIcon = psrvc.getPageProperty( SHOW_ROW_EDITVIEW_ICON_PARAM_KEY, request, response);
			final boolean  enShowRowIcon = (sShowRowIcon == null || sShowRowIcon.trim().length()== 0)
					? true // �� ��������� = ����������
					: Boolean.parseBoolean(sShowRowIcon.trim());
			sessionBean.setShowRowIconEditView( enShowRowIcon );

			String canImportCards = psrvc.getPageProperty(CAN_IMPORT_CARDS, request, response);
			if (canImportCards == null) canImportCards = "false"; 	//default is false
			sessionBean.setCanImportCards(Boolean.parseBoolean(canImportCards));

			String editAccessRoles = psrvc.getPageProperty(EDIT_ACCESS_ROLES, request, response);
			if (editAccessRoles != null&&!editAccessRoles.isEmpty()){
				sessionBean.setEditAccessRoles(editAccessRoles);
			}

			String downloadImportTemplate = psrvc.getPageProperty(DOWNLOAD_IMPORT_TEMPLATE, request, response);
			if (downloadImportTemplate != null && !downloadImportTemplate.equals("")) {
				sessionBean.setDownloadImportTemplate(downloadImportTemplate.trim());
			}
			
			String customImportTitle = psrvc.getPageProperty(CUSTOM_IMPORT_TITLE, request, response);
			if (customImportTitle!=null&&!customImportTitle.isEmpty())
				sessionBean.setCustomImportTitle(customImportTitle);
			
			String groupExecutionMode = psrvc.getPageProperty(CAN_GROUP_EXECT, request, response);
			if (groupExecutionMode == null) {
				sessionBean.setGroupExectionMode(GroupExecutionMode.DISABLE);
			} else {
				sessionBean.setGroupExectionMode(GroupExecutionMode.valueOf(groupExecutionMode));
			}

			String groupResolutionMode = psrvc.getPageProperty(GROUP_RESOLUTION_MODE, request, response);
			if (groupResolutionMode == null) {
				sessionBean.setGroupResolutionMode(GroupResolutionMode.DISABLE);
			} else {
				sessionBean.setGroupResolutionMode(GroupResolutionMode.valueOf(groupResolutionMode));
			}
			
			sessionBean.setCurrentTemplate( psrvc.getPageProperty("currentTemplate", request, response));
			setPreferences(request.getPreferences(), sessionBean);
			session.removeAttribute(SEARCH_BEAN, PortletSession.APPLICATION_SCOPE);

			String canCreateCards = psrvc.getPageProperty(PREF_SHOW_BTN_CREATE, request, response);
			if (canCreateCards != null) {
				sessionBean.setShowCreate(Boolean.parseBoolean(canCreateCards));
			}

		} catch (Exception e) {
			logger.error("Error in 'initBean'", e);
			String msg = getPortletConfig().getResourceBundle(request.getLocale()).getString("db.side.error.msg") + e.getMessage();
			sessionBean.setMessage(msg);
		}
	}
	
	/**
	 * Looks for cvs import template file
	 * @param service
	 * @param importTemplate
	 * @return
	 * @throws DataException
	 * @throws ServiceException
	 */
	private ObjectId findCsvTemplateCardId(DataServiceBean service, String importTemplate) throws DataException, ServiceException {
		final Search search = new Search();
		search.setTemplates(Collections.singletonList(ObjectId.predefined(Template.class, "jbr.import")));
		search.setStates(Collections.singletonList(ObjectId.predefined(CardState.class, "published")));
		search.setByAttributes(true);
		ReferenceValue refVal = new ReferenceValue();
		refVal.setId(ObjectIdUtils.getObjectId(ReferenceValue.class, importTemplate, true));
		search.addListAttribute(ObjectId.predefined(ListAttribute.class, "jbr.loadingDict"), Collections.singletonList(refVal));
		
		SearchResult result = service.doAction(search);
		List<Card> foundCards = result.getCards();
		if(foundCards == null || foundCards.isEmpty())
			return null;
		CardLinkAttribute attr = foundCards.get(0).getAttributeById(ObjectId.predefined(CardLinkAttribute.class, "jbr.files"));
		if(attr == null) {
			logger.error("No doclink attribute has been found!");
			throw new DataException("material.download.card.not.found");
		}
		return attr.getIdsLinked().get(0);
	}
	
	private boolean isRefreshData(){

		if(refresh_cards_list != null)
			return Boolean.valueOf(refresh_cards_list);
		
		return true;
	}
	
	private void loadCardList(PortletRequest request, PortletResponse response, Search searchAction)
		throws DataException, ServiceException {
		final MIShowListPortletSessionBean sessionBean = getSessionBean(request, response);
		final DataServiceBean serviceBean = sessionBean.getServiceBean(request);

		try {
			//------------------refreshAction
			final boolean hasData = (sessionBean.getDataList() != null) 
								&& (sessionBean.getDataList().size() > 0);
			if(hasData && !isRefreshData()) {
				logger.debug("not reload data list for request " + request);
				return;
			}
			logger.debug("reloading data list by request " + request);
			//------------------
		
			final PortletService psrvc = Portal.getFactory().getPortletService();
			parseSortingParametersToSearch(request, searchAction);
			// access & full list
			final String moveCount = psrvc.getPageProperty(MOVE_MODE, request,
					response);
			if (moveCount != null && Integer.valueOf(moveCount) > 0) {
				sessionBean.setCanMoveAll(true);
				sessionBean.setDeloSearch(searchAction.makeCopy());
			}
		
			//� ������ ������ ������� ������ �������� �������, ��� ������� �� ��������
			if(sessionBean.isPrintMode() && searchAction.getFilter() != null){
				searchAction = searchAction.makeCopy();
				searchAction.getFilter().setPageSize(Search.Filter.PGSIZE_UNLIMITED);
			}
		
			while (true)	// ����� �� ��������� �������� � ������� 
			{
				final SearchAdapter adapter = new SearchAdapter(serviceBean);
				adapter.setCardLinkDelimiter( request.getPreferences().getValue(	// �������� �������� �������� ATTR_DELIMITER 
						ATTR_DELIMITER,
						PREF_DEFAULT_ATTR_DELIMITER));
				adapter.executeSearch(serviceBean, searchAction);
				//������� �������, ���������� ��� "������ ��� ��������"
				deleteIgnoredColumns(adapter);
				sessionBean.setMetaDataDesc(adapter.getMetaDataNotReplaceColumn());

				String language = null;
				//�������� language � adapter �� ��������� ������ ������ � �������� ������� ������.
				if (sessionBean.isPrintMode()) {
			        language =  request.getLocale().getLanguage();
				}
				final List<ArrayList<Object>> answer = adapter.getData(language);
				for (int j=0; j < answer.size(); j++) {
					ArrayList<Object> answ = answer.get(j);
					for (int i=0; i < answ.size(); i++) {
						if (answ.get(i) instanceof String) {
							String value = (String)answ.get(i);
							if (value.length() >= 120) {
								if (value.indexOf(" ") > 120 || value.indexOf(" ") == -1) {
									value = value.substring(0, 120);
									value = value + "...";
								}
							}
							answ.set(i, value.replace("\r", "<br/>"));
						}
					}
					answer.set(j, answ);
				}
			
				sessionBean.setDataList(new PagedList(answer, searchAction.getFilter()));
				sessionBean.setDataOffset(adapter.getDataOffset());
				sessionBean.setTitle(adapter.getTitle());
				sessionBean.setCardList(adapter.getResultObject().getCards());
				
				//�������� �������� ��� ��������� ���������
				sessionBean.setRowExData(MIShowListHelper.loadCardsFromAttribute(request, adapter.getSearchResult(), serviceBean));

				// ���� ������ �� �������� ��� ��� - �������� ����� ��������
				if (answer != null && answer.size() > 0 || searchAction.getFilter() == null)
					break;

				// �� ���� �������� ������ ��� - ������� ��������� ����� ��������...
				final int pgNum = searchAction.getFilter().getPage(); 
				if ( pgNum <= 1) // ������ ��� �� ������ ��������... 
					break;
				searchAction.getFilter().setPage(pgNum - 1); // ��������� �� ����������...
			} // while

			// setPreferences(request.getPreferences(), sessionBean);

			// access
			CreateCard createAction;
			try {
				createAction = new CreateCard(new ObjectId(Template.class, sessionBean.getCurrentTemplate().longValue()));
			} catch (Exception e) {
				createAction = new CreateCard();
			}
			sessionBean.setCanCreate(sessionBean.isShowCreate()&&serviceBean.canDo(createAction));
			logger.debug("reload data list completed SUCCESSFULLY by request " + request);
		} finally {
			refresh_cards_list = null;
		}
	}
	

	private static DataServiceBean ServiceBeanInstance(PortletRequest request) throws ServiceException {
		DataServiceBean serviceBean = null;
		if (serviceBean == null) {
			InitialContext context = null;
			DataServiceHome home = null;
			try {
				context = new InitialContext();
				home = (DataServiceHome) PortableRemoteObject.narrow(context
					.lookup("ejb/dbmi"), DataServiceHome.class);
			} catch (NamingException ex) {
				throw new ServiceException(
					"Error during DataServiceHome context initialization",
					ex);
			}
			if (home == null)
				return null;
		
			try {
				DataService service = home.create();
				serviceBean = new DataServiceBean();
				serviceBean.setAddress(Portal.getFactory().getPortletService().getRemoteAddress(request));
				serviceBean.setService(service, service.authUser(
						new SystemUser(), "127.0.0.1"));
			} catch (RemoteException ex) {
				throw new ServiceException(ex);
			} catch (CreateException ex) {
				throw new ServiceException(ex);
			} catch (DataException ex) {
				throw new ServiceException(ex);
			}
		}
		return serviceBean;
	}
	
	private void configureSortingState(PortletRequest request, boolean clearSort){
		SortingState requestSortingState = null;
		String sortColumn = null;
		String sortOrder = null;
		String pageNum = null;
		if(!clearSort){
			sortColumn = request.getParameter("sort");
			sortOrder = request.getParameter("dir");
			pageNum = request.getParameter("page");
		}
		if (sortColumn != null || sortOrder != null) {
			requestSortingState = new SortingState();
			try {
				requestSortingState.setSortColumn(Integer.parseInt(sortColumn.replace('[', ' ').replace(']', ' ').trim()));
			} catch (Exception e) {}
			try {
				if ("asc".equalsIgnoreCase(sortOrder))
					requestSortingState.setSortOrder(2);
				else 
					if ("desc".equalsIgnoreCase(sortOrder))
						requestSortingState.setSortOrder(1);
					else
						requestSortingState.setSortOrder(0);
			} catch (Exception e) {}
		}

		if (pageNum != null) {
			try {
				if(requestSortingState == null) requestSortingState = new SortingState();
				requestSortingState.setPageNum(Integer.parseInt(pageNum));
			} catch (Exception e) {}
		}
		if (requestSortingState != null) {
			final PortletSession session = request.getPortletSession();
			final SortingState sessionSortingState = SortingState.loadSortingStateFromSession(TABLE_ID, session);
			if (sessionSortingState != null) {
				if (sortColumn == null) requestSortingState.setSortColumn(sessionSortingState.getSortColumn());
				if (sortOrder == null) requestSortingState.setSortOrder(sessionSortingState.getSortOrder());
				
				sessionSortingState.merge(requestSortingState);
			} else {
				SortingState.saveSortingStateToSession(requestSortingState, TABLE_ID, session);
			}
		}
	}

	private void parseSortingParametersToSearch(PortletRequest request, Search searchAction){

		MIShowListPortletSessionBean sessionBean = (MIShowListPortletSessionBean)request.getPortletSession().getAttribute(SESSION_BEAN);
		try {
			searchAction.getFilter().setCurrentUserRestrict(Search.Filter.CU_READ_PERMISSION);
			searchAction.getFilter().setPageSize(Integer.parseInt( request.getPreferences().getValue(
					PREF_PG_SIZE,
					Integer.toString(PREF_DEFAULT_PG_SIZE))));
		} catch (Exception e1) {}


		SortingState st = SortingState.loadSortingStateFromSession(TABLE_ID, request.getPortletSession());
		if(st == null) {
			searchAction.getFilter().getOrderedColumns().clear();
			return;
		}
		try {
			int sortColumnNum = st.getSortColumn();
			SearchResult.Column columnMetadataSource;
//			TODO ������ �������� Collection.toArray()[i] - ������� ����� ��������� �� ���, Search.getColumns() ���������� Collection, � ������ List !
			if (!sessionBean.getMetaDataDesc().isEmpty()){
				columnMetadataSource = (SearchResult.Column)sessionBean.getMetaDataDesc().toArray()[sortColumnNum];
				searchAction.getFilter().getOrderColumn().setAttributeId(columnMetadataSource.getAttributeId());
				searchAction.getFilter().getOrderColumn().setLabelAttrId(columnMetadataSource.getLabelAttrId());
				searchAction.getFilter().getOrderColumn().setPathToLabelAttr(columnMetadataSource.getPathToLabelAttr());
				searchAction.getFilter().getOrderColumn().setSortAttrPaths(columnMetadataSource.getSortAttrPaths());
				searchAction.getFilter().getOrderColumn().setGroupId(columnMetadataSource.getGroupId());
				searchAction.getFilter().setOrderColumnN(sortColumnNum);				
			} else if (!searchAction.getFilter().getOrderedColumns().isEmpty()){
				searchAction.getFilter().setOrderColumnN(sortColumnNum);
			} else return;
			
		} catch (Exception e1) {}

		try {
			if (st.getSortOrder() == 2 /*SortingState.SORT_ASCENDING*/ )
				searchAction.getFilter().getOrderColumn().setSorting(SearchResult.Column.SORT_ASCENDING);
			if (st.getSortOrder() == 1 /*SortingState.SORT_DESCENDING*/ )
				searchAction.getFilter().getOrderColumn().setSorting(SearchResult.Column.SORT_DESCENGING);
			if (st.getSortOrder() == 0 /*SortingState.SORT_NONE*/ )
				searchAction.getFilter().getOrderColumn().setSorting(SearchResult.Column.SORT_NONE);
		} catch (Exception e) {}
		try {
			searchAction.getFilter().setPage(st.getPageNum());
		} catch (Exception e) {}
	}

	private void setPreferences(PortletPreferences preferences,
			MIShowListPortletSessionBean sessionBean) {

		if (preferences == null || sessionBean == null) return;

		// assign "Show Header"
		boolean flag = Boolean.parseBoolean( preferences.getValue(
					PREF_SHOW_HEAD,
					Boolean.toString(PREF_DEFAULT_SHOW_HEAD )
			));
		sessionBean.setShowHeader( flag );

		// assign int: "Rows per Page"
		final int rowsPerPg = Integer.parseInt( preferences.getValue(
					PREF_PG_SIZE,
					Integer.toString(PREF_DEFAULT_PG_SIZE)
			));
		sessionBean.setListSize(rowsPerPg);

		// assign format string: Header/Title format
		String fmtTitle = preferences.getValue(PREF_HEDAER_TEXT, null);
		sessionBean.setHeaderText( fmtTitle);

		fmtTitle = preferences.getValue(PREF_HEDAER_JSP, null);
		sessionBean.setHeaderJsp( fmtTitle);

		// assign Url: links to the Card
		final String linkUrl = preferences.getValue(PREF_LINK_URL, null);
		sessionBean.setLinkUrl(linkUrl);

		// assign Url: links to the page
		final String linkPg = preferences.getValue(PREF_LINK_PG, null);
		sessionBean.setLinkPg(linkPg);

		/*
		// assign List: links inside columns
		final String colNames
			= preferences.getValue(PREF_LIST_COLUMNS_HAS_LINK, PREF_DEFAULT_COLUMNS_HAS_LINK);
		sessionBean.setColumnsHasLink(columnsHasLink);
		 */


		// assign "Show Button Refresh"
		setShowBtnRefresh(preferences, sessionBean);

		// assign "Show Button Refresh"
		flag = Boolean.parseBoolean( preferences.getValue(
					PREF_SHOW_TOOLBAR,
					Boolean.toString(PREF_DEFAULT_SHOW_TOOBAR)
			));
		sessionBean.setShowToolbar(flag);
	}
	
	/**
	 * ���������� ��������� ��� ������ "��������"
	 * @param preferences
	 * @param sessionBean
	 * @author ppolushkin
	 */
	private void setShowBtnRefresh(PortletPreferences preferences,
			MIShowListPortletSessionBean sessionBean) {
		if (preferences == null || sessionBean == null) return;
		
		boolean flag = Boolean.parseBoolean(preferences.getValue(
				PREF_SHOW_BTN_REFRESH,
				Boolean.toString(PREF_DEFAULT_SHOW_BTN_REFRESH)
		));
		sessionBean.setShowBtnRefresh(flag);
	}

	/**
	 * Returns JSP file path.
	 *
	 * @param request Render request
	 * @param jspFile JSP file name
	 * @return JSP file path
	 */

	private static String getJspFilePath(RenderRequest request, String jspFile) {
		return JSP_FOLDER  + jspFile + ".jsp";
	}
	
	private void setDefaultSortingState(Search search, PortletSession session){
		if (SortingState.loadSortingStateFromSession(TABLE_ID, session) != null )
			return;
		// ���������� ����� ������� �� �������� ����������� ����������
		int j = 1; // The first is invisible 'id'
		// groupId
		int k = 0;
		// ���������� ��������, �� ������� ������ ������������ ����������
		int sortingColumnCount = 0;
		try {
			SortingState st = null;
			for (SearchResult.Column src : search.getColumns()) {
				if (src.getGroupId() == 0) {
					j++;
					k = 0;
				} else if (k == src.getGroupId()) {
					continue;
				} else {
					j++;
					k = src.getGroupId();
				}
				if (src.getSorting() != SearchResult.Column.SORT_NONE) {
					if (++sortingColumnCount > 1) {
						return;
					}
					st = new SortingState();
					st.setPageNum(1);
					st.setSortColumn(j);
					if (SearchResult.Column.SORT_ASCENDING == src.getSorting())
						st.setSortOrder(2);
					else if (SearchResult.Column.SORT_DESCENGING == src.getSorting())
						st.setSortOrder(1);
					else st.setSortOrder(0);
				}
			}
			if (st != null) {
				SortingState.saveSortingStateToSession(st, TABLE_ID, session);
			}
		} catch (Exception e) {
			//do nothing
		}
	}
	
    private void deleteIgnoredColumns (SearchAdapter adapter){
   	 Collection<SearchResult.Column> cleanedColumns = new LinkedList<SearchResult.Column>();
   	 for (Iterator<SearchResult.Column> columnIter = adapter.getResultObject().getColumns().iterator(); columnIter.hasNext();) {
   		 SearchResult.Column column = columnIter.next();
   		 if(!column.isExportOnly()){
   			 cleanedColumns.add(column);
   		 }
   	 }
   	 adapter.getResultObject().setColumns(cleanedColumns);
    }

	public static class PagedList implements PaginatedList, List<Object> {
		private List<Object> list;
		private Search.Filter filter;
		
		public void add(int index, Object element) {
			list.add(index, element);
		}

		public boolean add(Object o) {
			return list.add(o);
		}

		public boolean addAll(Collection<?> c) {
			return list.addAll(c);
		}

		public boolean addAll(int index, Collection<?> c) {
			return list.addAll(index, c);
		}

		public void clear() {
			list.clear();
		}

		public boolean contains(Object o) {
			return list.contains(o);
		}

		public boolean containsAll(Collection<?> c) {
			return list.containsAll(c);
		}

		public boolean equals(Object o) {
			return list.equals(o);
		}

		public Object get(int index) {
			return list.get(index);
		}

		public int hashCode() {
			return list.hashCode();
		}

		public int indexOf(Object o) {
			return list.indexOf(o);
		}

		public boolean isEmpty() {
			return list.isEmpty();
		}

		public Iterator<Object> iterator() {
			return list.iterator();
		}

		public int lastIndexOf(Object o) {
			return list.lastIndexOf(o);
		}

		public ListIterator<Object> listIterator() {
			return list.listIterator();
		}

		public ListIterator<Object> listIterator(int index) {
			return list.listIterator(index);
		}

		public Object remove(int index) {
			return list.remove(index);
		}

		public boolean remove(Object o) {
			return list.remove(o);
		}

		public boolean removeAll(Collection<?> c) {
			return list.removeAll(c);
		}

		public boolean retainAll(Collection<?> c) {
			return list.retainAll(c);
		}

		public Object set(int index, Object element) {
			return list.set(index, element);
		}

		public int size() {
			return list.size();
		}

		public List<Object> subList(int fromIndex, int toIndex) {
			return list.subList(fromIndex, toIndex);
		}

		public Object[] toArray() {
			return list.toArray();
		}

		public <T> T[] toArray(T[] a) {
			return list.toArray(a);
		}

		public PagedList(List list, Search.Filter filter){
			this.list=list;
			this.filter = filter;
		}

		public int getFullListSize() {
			if (filter != null)
				return filter.getWholeSize();
			return list.size();
		}

		public List<Object> getList() {
			return list;
		}

		public int getObjectsPerPage() {
			try {
				return filter.getPageSize()==0 ? 1 : filter.getPageSize();
			} catch (Exception e) {}
			return 1;
		}

		public int getPageNumber() {
			if (filter != null)
				return filter.getPage()>0 ? filter.getPage() : 1;
			return 1;
		}

		public String getSearchId() {
			return null;
		}

		public String getSortCriterion() {
			if (filter != null)
				return "["+filter.getOrderColumnN()+"]";
			return "[1]";
		}

		public SortOrderEnum getSortDirection() {
			try {
				if (filter.getOrderColumn().getSorting()==SearchResult.Column.SORT_DESCENGING)  
					return SortOrderEnum.DESCENDING;
				if (filter.getOrderColumn().getSorting()==SearchResult.Column.SORT_ASCENDING)  
					return SortOrderEnum.ASCENDING;
				if (filter.getOrderColumn().getSorting()==SearchResult.Column.SORT_NONE)  
					return null;
			} catch (Exception e) {}
			return null;
		}
	}
}
