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
package com.aplana.dbmi.event;

import java.io.IOException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletSecurityException;
import javax.portlet.PortletSession;
import javax.portlet.PortletURL;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.PortletService;
import com.aplana.dbmi.action.GetActionName;
import com.aplana.dbmi.action.GetAsyncActions;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.GetAsyncActions.ActionState;
import com.aplana.dbmi.action.GetEventLog;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.card.CardPortlet;
import com.aplana.dbmi.card.DigitalSignatureConfiguration.Template;
import com.aplana.dbmi.event.EventLogPortletSessionBean.EventPeriod;
import com.aplana.dbmi.event.EventLogPortletSessionBean.EventResult;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ContextProvider;
import com.aplana.dbmi.model.EventEntry;
import com.aplana.dbmi.model.InfoMessage;
import com.aplana.dbmi.model.LogAction;
import com.aplana.dbmi.model.LogEntry;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.PortletUtil;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.dbmi.util.IdUtils;
import com.aplana.web.tag.util.StringUtils;

public class EventLogPortlet extends GenericPortlet {
	
	static Log logger = LogFactory.getLog(EventLogPortlet.class);
	
	/***************************************************************************
	 * JSP block
	 ***************************************************************************/
	// JSP root folder name
	static final String JSP_FOLDER = "/WEB-INF/jsp/html/event/";

	// portlet main jsp file
	static final String JSP_PORTLET_EVENTLOG_LIST = "EventLogPortlet.jsp";
	
	/**
	 * ��������� �������� �������� (property ������ xxx-portal-object.xml)
	 */
	
	private PortletURL portalURL;
	
	
	/***************************************************************************
	 * bean block
	 ***************************************************************************/
	// Bean name for the portlet session must be dynamic
	// via getFormSessionAttributeName(request);
	public static final String SESSION_BEAN_EVENTLOG = "EventLogPortletSessionBean";
	
	
	/***************************************************************************
	 * PRIVATE
	 ***************************************************************************/
	private PortletService portletService;
	
	
	/**
	 * Portlet properties
	 */
	
	private static final String SHOW_TOOLBAR = "showToolbar";
	private static final String SHOW_TITLE = "showTitle";
	private static final String SHOW_BUTTON_REFRESH = "showBtnRefresh";
	
	
	/**
	 * Page properties
	 */
	
	private static final String ADMIN_MODE = "adminMode";
	private static final String PAGE_PERIOD = "pagePeriod";
	private static final String PAGE_RESULT = "pageResult";
	private static final String PAGE_TITLE = "title";
	private static final String SHOW_STATE = "showState";
	private static final String SHOW_MESSAGE = "showMsg";
		
	
	/**
	 * ������ �������
	 */
	
	private static final Integer COLUMN_DATE = 1;
	private static final Integer COLUMN_USER = 2;
	private static final Integer COLUMN_ACT = 3;
	private static final Integer COLUMN_DOC = 4;
	private static final Integer COLUMN_TEM = 5;
	private static final Integer COLUMN_ST = 6;
	private static final Integer COLUMN_MSG = 7;
	
	
	/**
	 * ���������� �������
	 * �������� � ������ ������ ������� ��� ���������� �� ��������,
	 * ��������� ����� ������������� �� ����������� (����� ������� �� �������)
	 */
	
	/*private static final List<Integer> SORTING_DESC = new ArrayList<Integer>();
	static {
		SORTING_DESC.add(COLUMN_DATE);
	}*/
	
	
	/**
	 * �������� ������� 
	 */
	private static final Map<Integer, String> table_columns = new TreeMap<Integer, String>();
	
	static {		
		table_columns.put(COLUMN_DATE, "EventLogPortlet.col_date");
		table_columns.put(COLUMN_USER, "EventLogPortlet.col_user");
		table_columns.put(COLUMN_ACT, "EventLogPortlet.col_act");
		table_columns.put(COLUMN_DOC, "EventLogPortlet.col_doc");
		table_columns.put(COLUMN_TEM, "EventLogPortlet.col_temp");
		table_columns.put(COLUMN_ST, "EventLogPortlet.col_st");
		table_columns.put(COLUMN_MSG, "EventLogPortlet.col_msg");
	}	
	
	/**
	 * ��������
	 */
	private static final String GET_CARD_ACTION = "GET_CARD";
	
	/**
	 * ������������ ��������
	 */
	private static final List<String> ignored_actions = new ArrayList<String>();
	
	static {		
		ignored_actions.add(GET_CARD_ACTION.trim());
	}
	
	private static final String MSG_SUCCESS = "EventLogPortlet.msg.success"; 
	private static final String MSG_FAILURE = "EventLogPortlet.msg.failure";
	private static final String MSG_WAITING = "EventLogPortlet.msg.waiting"; 
	private static final String MSG_RUNNING = "EventLogPortlet.msg.running";
	private static final String MSG_REPEATED = "EventLogPortlet.msg.repeated"; 
	private static final String MSG_WAIT_REP = "EventLogPortlet.msg.waitrep"; 
			
	//��������� ������ �������
	static public final int defColWidthInEm = 1000;
	
	
	//��������
	final static public String ATTR_NAME = "name";
	
	//Other
	final static public String EMPTY_VALUE = "empty.value";
	final static public String EMPTY_STRING = "";
	final static public String DROPPED_CARD = "dropped.card";
	final static public String NO_MESSAGE = "no.message";
	final static public String CARDLINK = "link";
	
	
	//����� ���-�� ���� ���� �������� �� ������� Action, ����� �� ����� ��� �� �������� ���� GetActionName 
	final public static Map<String, String> actionCash = new HashMap<String, String>();
	
	/***************************************************************************
	 * ��������� URL-������
	 ***************************************************************************/
	public static final String PARAM_ACTION = "DELEGATE_ACTION";
	
	
	/***************************************************************************
	 * �������� ��������, ������������ � url-��������� PARAM_ACTION 
	 ***************************************************************************/
	public static final String ACTION_TAG_REFRESH = "REFRESH";
	
	
	/***************************************************************************
	 * Returns JSP file path.
	 * @param request Render request
	 * @param jspFile JSP file name (with ".jsp" if any)
	 * @return JSP file path
	 */
	static String getJspFilePath(RenderRequest request, String jspFile) {
		return JSP_FOLDER  + jspFile;
	}
	
	
	/**
	 * �����������.
	 */
	@Override
	protected void doView(RenderRequest request, RenderResponse response)
			throws PortletException, PortletSecurityException, IOException 
	{
		// Set the MIME type for the render response
		response.setContentType(request.getResponseContentType());

		// Set user locale
		ContextProvider.getContext().setLocale(request.getLocale());
		
		final ResourceBundle resource = getPortletConfig().getResourceBundle(request.getLocale());
		
		// (!) Process URL parameters
		setParamsFromExternalRequest(request, response, resource);
		
		portalURL = response.createActionURL();
		
		final EventLogPortletSessionBean bean = getSessionBean(request);
		if(bean == null) {
			final String msg = (resource != null) 
						? resource.getString("EventLogPortlet.no_session_bean")
						: "No Session Bean (and even no portlet resource bundle)";
			response.getWriter().println(msg);
			return;
		}
		
		try {
			
			loadBeanData(bean, resource);
			
			prepareBeanView(bean, request, resource);
			
			// Invoke the JSP to render
			final PortletRequestDispatcher rd = getPortletContext().getRequestDispatcher(
					getJspFilePath(request, JSP_PORTLET_EVENTLOG_LIST));
			rd.include(request,response);
			
		} catch (Exception ex) {
			final String msg = safeGetMsg(resource,
						"db.side.error.msg", 
						"System error: ");
			logger.error(ex.getMessage(), ex);
			bean.setErrorMsg(msg + ex.getMessage());
		}
	}
	
	
	/***************************************************************************
	 * �������������� ��� �����������
	 ***************************************************************************/
	private void prepareBeanView(EventLogPortletSessionBean bean, PortletRequest request, ResourceBundle resource)
	{
		final DataServiceBean service = bean.getDataServiceBean();
		
		final List<SearchResult.Column> columns = new ArrayList<SearchResult.Column>();
		
		for(Iterator i = table_columns.keySet().iterator(); i.hasNext();) {
			
			final Integer key = (Integer) i.next();
			
			if(!bean.isAdminMode() && COLUMN_USER.equals(key))
				continue;
			
			if(!bean.isShowState() && COLUMN_ST.equals(key))
				continue;
			
			if(!bean.isShowMsg() && COLUMN_MSG.equals(key))
				continue;
			
			final SearchResult.Column column = createColumn(key, defColWidthInEm);
			
			/*if(SORTING_DESC.contains(key)) {
				column.setSorting(SearchResult.Column.SORT_DESCENGING);
			}*/
			
			String colName = safeGetMsg(resource, table_columns.get(key));
			
			column.setNameRu(colName);
			column.setNameEn(colName);
			
			columns.add(column);
		}
		
		bean.setColumns(columns);
		
	}
	
	
	private SearchResult.Column createColumn(int colNum, int width)
	{
		SearchResult.Column col = new SearchResult.Column();
		col.setWidth((width >= 0) ? width : defColWidthInEm);
		col.setAttributeId(new ObjectId(Attribute.class, colNum));
		return col;
	}
	
	
	private Calendar getTomorrow() {
		final Calendar c = getToday();
		c.setTimeInMillis(c.getTimeInMillis() + 1000 * 60 * 60 * 24);
		return c;
	}
	
	private Calendar getToday() {
		final Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR, 0);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return c;
	}
	
	private Calendar getMondayOfCurrentWeek() {
		final Calendar c = getToday();
		int day = c.get(Calendar.DAY_OF_WEEK);
		int offset = 0;
		if(day == 1)
			offset = 7;
		else if(day > 2)
			offset = day - 2;
		c.setTimeInMillis(c.getTimeInMillis() - offset * 1000 * 60 * 60 * 24);
		return c;
	}
	
	/**
	 * �������� ������ �� ��
	 * @param bean
	 * @throws DataException
	 * @throws ServiceException
	 */
	@SuppressWarnings("unchecked")
	private void loadBeanData(EventLogPortletSessionBean bean, ResourceBundle resource) 
			throws DataException, ServiceException
	{
		final DataServiceBean service = bean.getDataServiceBean();
		
		final GetEventLog action = new GetEventLog();
		
		//������������� ������������ ��������
		action.setIgnoredActions(ignored_actions);
		
		// ������������� ��������� �������
		if(bean.getPeriod().equals(EventPeriod.TODAY)) {
			action.setFromDate(getToday().getTime());
			action.setToDate(getTomorrow().getTime());
		}
		else if(bean.getPeriod().equals(EventPeriod.WEEK)) {
			action.setFromDate(getMondayOfCurrentWeek().getTime());
			action.setToDate(getTomorrow().getTime());
		}
		
		// ������������� ��������� ������
		if(!bean.isAdminMode())
			action.setUser(bean.getUserLoginStr());
		
		// ������������� ��������� � ���, ����� ���������� ��������
		if(bean.getResult().equals(EventResult.SUCCESS))
			action.setResultSuccess(true);
		else if(bean.getResult().equals(EventResult.FAILURE))
			action.setResultSuccess(false);
		
		//���������� �� ���������
		if(bean.isShowMsg())
			action.setShowMsg(true);
		
		
		final List<LogEntry> list = (List<LogEntry>) service.doAction(action);
		
		//����� ������ ����������������� ������ ��� ������ �� JSP
		List<List<String>> dataList = new ArrayList<List<String>>();
		
		makeDataList(bean, dataList, list, resource, null, true);
		
		// ���� ����� ���������� ���������, �� ���������� ������� ��������
		if(bean.isShowState()) {
		
			final GetAsyncActions actionWait = new GetAsyncActions();
			final GetAsyncActions actionRun = new GetAsyncActions();
			final GetAsyncActions actionRep = new GetAsyncActions();
			final GetAsyncActions actionWaitRep = new GetAsyncActions();
			
			actionWait.setRunActions(ActionState.WAITING);
			actionRun.setRunActions(ActionState.RUNNING);
			actionRep.setRunActions(ActionState.REPEATED);
			actionWaitRep.setRunActions(ActionState.WAITING_FOR_REPEAT);
			
			final List<LogEntry> listWait = (List<LogEntry>) service.doAction(actionWait);
			final List<LogEntry> listRun = (List<LogEntry>) service.doAction(actionRun);
			final List<LogEntry> listRep = (List<LogEntry>) service.doAction(actionRep);
			final List<LogEntry> listWaitRep = (List<LogEntry>) service.doAction(actionWaitRep);
			
			makeDataList(bean, dataList, listWait, resource, MSG_WAITING, false);
			makeDataList(bean, dataList, listRun, resource, MSG_RUNNING, false);
			makeDataList(bean, dataList, listRep, resource, MSG_REPEATED, false);
			makeDataList(bean, dataList, listWaitRep, resource, MSG_WAIT_REP, false);
		}
		
		bean.setDataList(dataList);
		
	}
	
	
	
	private void makeDataList(EventLogPortletSessionBean bean, 
							  List<List<String>> dataList, 
							  List<LogEntry> list, 
							  ResourceBundle resource,
							  String msg,
							  boolean isEnded) 
				throws DataException, ServiceException {
		
		// ��������� �� �����
		if (list != null && !list.isEmpty()) {
			
			final DataServiceBean service = bean.getDataServiceBean();
			
			final String emptyValue = safeGetMsg(resource, EMPTY_VALUE);
			final String droppedCard = safeGetMsg(resource, DROPPED_CARD);
			final String link = safeGetMsg(resource, CARDLINK);
			
			//������ ��� �������� � ��������� �����, ����� ����� ��� �� ��������� DoSerch
			Map<String, Card> realCards = searchAllNeededCards(service, list);
			
			//������ � ������
			for(LogEntry ee : list) {
				
				final List<String> row = new ArrayList<String>();
				
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
				
				row.add(sdf.format(ee.getTimestamp()));
				
				if(bean.isAdminMode()) {
					row.add(ee.getUser().getFullName());
				}
				
				String actionCode = ee.getEvent();
				if(actionCode == null)
					row.add(emptyValue);
				else if(actionCash.containsKey(actionCode)) {
					row.add((String)actionCash.get(actionCode));
				} else {
					final GetActionName event = new GetActionName();
					event.setActionCode(actionCode);
					final LogAction eventName = (LogAction) service.doAction(event);
					if(eventName != null && eventName.getName() != null && eventName.getName().getValue() != null) {
						final String eventValue = eventName.getName().getValue();
						row.add(eventValue);
						actionCash.put(actionCode, eventValue);
					} else {
						row.add(emptyValue);
						actionCash.put(actionCode, emptyValue);
					}
				}
				
				if(ee.getObject() == null) {
					row.add(emptyValue);
					row.add(EMPTY_STRING);
				} else if(Card.class.isAssignableFrom(ee.getObject().getType())) {
					final ObjectId cardId = ee.getObject();
					Card c = null;
					if (cardId != null && cardId.getId() != null) {
						c = realCards.get(cardId.getId().toString());
					}
					if(c == null){
						row.add(droppedCard);
						row.add(EMPTY_STRING);
					} else {
						ObjectId attr = IdUtils.smartMakeAttrId(ATTR_NAME, StringAttribute.class);
						
						final String cardlink = MessageFormat.format(
								link,
								new Object[] {CardPortlet.BACK_URL_FIELD,
											  portalURL,
											  CardPortlet.EDIT_CARD_ID_FIELD,
											  ee.getObject().getId().toString(),
											  (c.getAttributeById(attr) != null 
											  	&& c.getAttributeById(attr).getStringValue().length() > 0)
											  			? c.getAttributeById(attr).getStringValue() 
											  			: emptyValue
											  }
							);
						
						row.add(cardlink.toString());
						row.add(c.getTemplateName());
					}
				} else if (Template.class.isAssignableFrom(ee.getObject().getType())) {
					row.add(emptyValue);
					row.add(((EventEntry)ee).getTemplate().getName());
				} else {
					row.add(emptyValue);
					row.add(EMPTY_STRING);
				}
				
				if(isEnded && bean.isShowState()) {
					if(((EventEntry)ee).isSuccess())
						row.add(safeGetMsg(resource, MSG_SUCCESS));
					else
						row.add(safeGetMsg(resource, MSG_FAILURE));
				} else if(!isEnded && bean.isShowState()) {
					row.add(safeGetMsg(resource, msg));
				}
				
				
				if(isEnded && bean.isShowMsg()) {
					final String smsg = ((EventEntry)ee).getMessage();
					if(smsg != null && !smsg.trim().isEmpty())
						row.add(smsg.trim());
					else
						row.add(NO_MESSAGE);
				} else if(!isEnded && bean.isShowMsg()) {
					if (ee instanceof InfoMessage) {
						row.add(((InfoMessage)ee).getMessage());
					} else {
						row.add(EMPTY_STRING);
					}
				}
				
				dataList.add(row);
			}
			
		}
	}

	
	/**
	 * ���� � ��������� ������ ��������, �������� �� ID
	 * @param cards
	 * @param cardId
	 * @return �������� � ����� �������� ID
	 */
	private Card fetchCardById(List<Card> cards, long cardIdToFetch) {
		
		for(Card card : cards) {
			final ObjectId curId = card.getId();
			if (curId != null && curId.getId() != null) {
				final long curLongId = (Long) curId.getId();
				if(curLongId == cardIdToFetch)
					return card;
			}
		}
		return null;
	}
	
	/**
	 * ����� ��� �������� �� ID, ���������� � ������ LogEntry
	 * @return ������ ����������� ��������
	 * @throws ServiceException 
	 * @throws DataException 
	 */
	private Map<String, Card> searchAllNeededCards(DataServiceBean service, List<LogEntry> list) throws DataException, ServiceException {
		//Set ���������� ������ ������ ������������� id ��������
		Set<String> cardIdsCollection = new HashSet<String>();
		for(LogEntry ee : list) {
			if(ee.getObject() != null && Card.class.isAssignableFrom(ee.getObject().getType())) {
				ObjectId cardId = ee.getObject();
				if (cardId != null && cardId.getId() != null) {
					cardIdsCollection.add(cardId.getId().toString());
				}
			}
		}
		List<Card> result = loadCards(service, StringUtils.collectionToCommaDelimitedString(cardIdsCollection));
		Map<String, Card> resultMap = new HashMap<String, Card>();
		for(int i = 0; i < result.size(); i++) {
			resultMap.put(result.get(i).getId().getId().toString(), result.get(i));
		}
		return resultMap;
	}
	
	
	/**
	 * �������� ��������� �������� �� �������
	 * @param resource
	 * @param msgTag
	 * @param msgDefault
	 * @return String
	 */
	static String safeGetMsg(ResourceBundle resource, String msgTag, String msgDefault) 
	{
		try {
			return (resource != null) ? resource.getString(msgTag) : msgDefault;
		} catch (Throwable ex) {
			ex.printStackTrace();
			return msgDefault;
		}
	}
	
	static String safeGetMsg(ResourceBundle resource, String msgTag)
	{
		return safeGetMsg(resource, msgTag, "");
	}
	
	
	/**
	 * �������� ��������� �� ��������
	 * @param request
	 * @param response
	 */
	private void setParamsFromExternalRequest(RenderRequest request, RenderResponse response, ResourceBundle resource) 
	{
		final EventLogPortletSessionBean bean = getSessionBean(request);
		
		DataServiceBean service = bean.getDataServiceBean();
		
		bean.setUserLoginStr(request.getUserPrincipal().getName());
		
		/* 
		 * ���������-�������� �������� ...
		 */
		
		final String period = portletService.getPageProperty(PAGE_PERIOD, request, response);
		if(period != null && period.trim().length() > 0) {
			EventLogPortletSessionBean.EventPeriod showPeriod = 
					EventLogPortletSessionBean.EventPeriod.valueOf(period.trim().toUpperCase());
			
			bean.setPeriod(showPeriod);
		}
		
		final String result = portletService.getPageProperty(PAGE_RESULT, request, response);
		if(result != null && result.trim().length() > 0) {
			EventLogPortletSessionBean.EventResult showResult = 
					EventLogPortletSessionBean.EventResult.valueOf(result.trim().toUpperCase());
			
			bean.setResult(showResult);
		}
		
		final String admin = portletService.getPageProperty(ADMIN_MODE, request, response);
		
		boolean adminMode = (admin == null || admin.trim().length() == 0)
				? false
				: Boolean.parseBoolean(admin);
		
		if(adminMode)
			bean.setAdminMode(adminMode);
		
		
		final String state = portletService.getPageProperty(SHOW_STATE, request, response);
		
		boolean showState = (state == null || state.trim().length() == 0)
				? false
				: Boolean.parseBoolean(state);
		
		if(showState)
			bean.setShowState(showState);
		
		
		final String mes = portletService.getPageProperty(SHOW_MESSAGE, request, response);
		
		boolean showMsg = (mes == null || mes.trim().length() == 0)
				? false
				: Boolean.parseBoolean(mes);
		
		if(showMsg)
			bean.setShowMsg(showMsg);
		
				
		final String title = portletService.getPageProperty(PAGE_TITLE, request, response);
		if(title != null && title.trim().length() > 0) {
			bean.setTitle(safeGetMsg(resource, title.trim()));
		}
		
	}
	
	
	/**
	 * �������� ��� �� ������ ��������
	 * @param request
	 * @return EventLogPortletSessionBean
	 */
	public static synchronized EventLogPortletSessionBean getSessionBean(PortletRequest request) {
		try {
			
			final PortletSession session = request.getPortletSession();
			if(session == null)
				return null;
			
			EventLogPortletSessionBean sessionBean = 
					(EventLogPortletSessionBean) session.getAttribute(SESSION_BEAN_EVENTLOG);
			
			if(sessionBean == null)
				sessionBean = createBean(request);			
			
			request.getPortletSession().setAttribute(SESSION_BEAN_EVENTLOG, sessionBean);

			return sessionBean;
			
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			return null;
		}
		
	}
	
	
	/**
	 * ������� � ������� ��� ��� ��������
	 * @param request
	 * @return ������ ��� ��������� EventLogPortletSessionBean
	 */
	private static synchronized EventLogPortletSessionBean createBean(PortletRequest request)
	{
		final EventLogPortletSessionBean sessionBean = new EventLogPortletSessionBean();
		
		initBean(sessionBean, request);
		
		return sessionBean;
	}
	
	
	/**
	 * ��������� ������� ��� ��� ��������
	 * @param bean
	 * @param request
	 */
	static synchronized void initBean(EventLogPortletSessionBean bean, PortletRequest request)
	{
		if (bean == null) return; 

		bean.clear();
		
		setBeanPreferences(bean, request.getPreferences());
		
		final DataServiceBean serviceBean = PortletUtil.createService(request);
		bean.setDataServiceBean(serviceBean);
		
	}
	
	
	/**
	 * ��������� ������������� �� ������ ��������.
	 * @param bean
	 * @param preferences
	 */
	static synchronized void setBeanPreferences(EventLogPortletSessionBean bean, 
			PortletPreferences preferences) 
	{
		if (preferences == null || bean == null) return;
		
		boolean flag = Boolean.parseBoolean(preferences.getValue(
				SHOW_BUTTON_REFRESH,
				Boolean.toString(true)));
		bean.setShowBtnRefresh(flag);

		flag = Boolean.parseBoolean(preferences.getValue(
				SHOW_TOOLBAR,
				Boolean.toString(true)));
		bean.setShowToolbar(flag);
		
		flag = Boolean.parseBoolean(preferences.getValue(
				SHOW_TITLE,
				Boolean.toString(true)));
		bean.setShowTitle(flag);

	}
	
	
	@Override
	public void init() throws PortletException
	{
		super.init();
		portletService = Portal.getFactory().getPortletService();
		if (logger.isInfoEnabled())
			logger.info(String.format( "%s init() SUCCESSFULL", this.getClass().getName() ));
	}
	
	
	@Override
	public void processAction(ActionRequest request, ActionResponse response) 
			throws PortletException, java.io.IOException 
	{
		ContextProvider.getContext().setLocale(request.getLocale());
		
		final String action = request.getParameter(PARAM_ACTION);
		
		if (logger.isDebugEnabled())
			logger.debug(String.format("LogEventPortlet: processing action [%s]...", action));
		
		final EventLogPortletSessionBean sessionBean = getSessionBean(request);
		
		try {
			if (action.equals(ACTION_TAG_REFRESH)) {
				handleRefreshAction(request, response);
			}
			else {
				throw new PortletException(MessageFormat.format( 
						"{0}: unsupported portlet action ''{1}''", 
						new Object[] { this.getClass().getName(), action} 
						));
			}
		
		} catch (DataException ex) {
			if (sessionBean != null) {
				ex.printStackTrace();
				sessionBean.setErrorMsg(ex.getMessage());
			} else
				throw new PortletException(ex);
		} catch (Exception ex) {
			if (sessionBean != null) {
				ex.printStackTrace();
				sessionBean.setErrorMsg(ex.getMessage());
			} else
				throw new PortletException(ex);
		}
	}
	
	
	private void handleRefreshAction(ActionRequest request,
			ActionResponse response) 
		throws DataException, ServiceException 
	{
		final EventLogPortletSessionBean bean = getSessionBean(request);
		initBean(bean, request);
		//loadBeanData(bean);
	}
	
	
	/**
	 * ��������� �������� ����� DoSearch �� IDs.
	 * @param serviceBean
	 * @param cardIds
	 * @return ���������� �������� � ���������� ������ � ��������
	 * @throws DataException
	 * @throws ServiceException
	 */
	private List<Card> loadCards(DataServiceBean serviceBean, String cardIds) throws DataException, ServiceException{
		List<Card> cards = null;
		if (cardIds != null && cardIds.length() > 0) {
				List<SearchResult.Column> cols = new ArrayList();
				
				SearchResult.Column col = new SearchResult.Column();
				col.setAttributeId(Card.ATTR_TEMPLATE);
				cols.add(col);
				
				ObjectId attr = IdUtils.smartMakeAttrId(ATTR_NAME, StringAttribute.class);
				col = new SearchResult.Column();
				col.setAttributeId(attr);
				cols.add(col);
				
				Search search = new Search();
				search.setByCode(true);
				search.setWords(cardIds);
				search.setColumns(cols);
				SearchResult searchResult = (SearchResult) serviceBean.doAction(search);
				List<Card> requestCards = searchResult.getCards();
				if(requestCards != null && !requestCards.isEmpty())
					return requestCards;
		}
		return cards != null ? cards : new ArrayList<Card>();
	}

}
