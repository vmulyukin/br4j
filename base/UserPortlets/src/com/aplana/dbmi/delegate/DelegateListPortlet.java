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
package com.aplana.dbmi.delegate;

import java.io.IOException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletPreferences;
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
import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.CreateCard;
import com.aplana.dbmi.action.DeleteDelegateAction;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.SaveDelegatesAction;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.card.CardPortlet;
import com.aplana.dbmi.delegate.DelegateListSessionBean.EDelegatePgTab;
import com.aplana.dbmi.delegate.DelegateListSessionBean.EFilterByActiveMode;
import com.aplana.dbmi.filter.DelegateFilterUtil;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.ContextProvider;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.Delegation;
import com.aplana.dbmi.model.IntegerAttribute;
import com.aplana.dbmi.model.LongAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.PersonView;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.TextAttribute;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.PortletUtil;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.dbmi.service.SystemUser;
import com.aplana.dbmi.service.UserPrincipal;
import com.aplana.dbmi.util.JspUtils;

public class DelegateListPortlet extends GenericPortlet 
{
	static Log logger = LogFactory.getLog(CardPortlet.class);

	/***************************************************************************
	 * JSP block
	 ***************************************************************************/
	// JSP root folder name
	static final String JSP_FOLDER = "/WEB-INF/jsp/html/delegate/"; //$NON-NLS-1$

	// portlet main jsp file
	static final String JSP_PORTLET_DELEGATE_LIST = "DelegateListPortlet.jsp"; //$NON-NLS-1$

	public static final String JSP_TABLE_ID = "delegateTable";  //$NON-NLS-1$

	/**
	 * ��������� �������� �������� (property ������ xxx-portal-object.xml)
	 */
	public static final String PORTLET_PARAM_ADMIN_MODE = "admin_mode";  //$NON-NLS-1$
	public static final String PORTLET_PARAM_SELECTABLE_ACTIVE_MODE_FILTER = "enSelectFilterActiveMode";  //$NON-NLS-1$
	public static final String PORTLET_PARAM_SELECTABLE_ROLE_ID_FILTER = "enSelectFilterRoleId";  //$NON-NLS-1$
	public static final String PORTLET_PARAM_SELECTABLE_ACTIVE_MODE_NAME = "enSelectFilterActiveModeStr";  //$NON-NLS-1$
	public static final String PORTLET_PARAM_PAGE_TAB = "page_tab";  //

	/***************************************************************************
	 * bean block
	 ***************************************************************************/
	// Bean name for the portlet session must be dynamic
	// via getFormSessionAttributeName(request);
	public static final String SESSION_BEAN_DELEGATE = "DelegateListPortletSessionBean"; //$NON-NLS-1$
	public static final String BEAN_EDIT = "delegateEditBean"; //$NON-NLS-1$
	public static final String FORM_NAME = "delegateForm";

	//  ... �������� ����������� ������� ��. DelegateListSessionBean.EDeletagePgTab ...

	/***************************************************************************
	 * ��������� URL-������
	 ***************************************************************************/
	public static final String PARAM_MSG = "DELEGATE_MSG_TEXT"; //$NON-NLS-1$
	public static final String PARAM_ACTION = "DELEGATE_ACTION"; //$NON-NLS-1$

	public static final String PARAM_TAB_ID = "DELEGATE_ID_TAB"; //$NON-NLS-1$
	public static final String PARAM_DELEGATE_IDX = "DELEGATE_IDX_DELEGATE"; //$NON-NLS-1$
	public static final String PARAM_BACK_URL = "DELEGATE_URL_BACK"; //$NON-NLS-1$

	public static final String PARAM_USER_ID = "DELEGATE_USER_ID"; //$NON-NLS-1$

	/***************************************************************************
	 * �������� ��������, ������������ � url-��������� PARAM_ACTION 
	 ***************************************************************************/
	public static final String ACTION_TAG_FILTER = "FILTER";
	public static final String ACTION_TAG_REFRESH = "REFRESH";
	public static final String ACTION_TAG_GOTO_TAB = "GOTO_TAB";// + (client)param TAB
	public static final String ACTION_TAG_BACK = "BACK";
	// public static final String ACTION_TAG_SAVE = "SAVE";

	public static final String ACTION_TAG_CREATE_DELEGATE = "CREATE_DELEGATE";
	public static final String ACTION_TAG_EDIT_DELEGATE = "EDIT_DELEGATE";    	// + client param ID_DELEGATE
	 public static final String ACTION_TAG_DELETE_DELEGATE = "DELETE_DELEGATE";	// + client param ID_DELEGATE
	
	/* � ����������� �� �������������� ����������� �������������:
	 *  ��� �������� ��������������:
	 *  	cancel: �������� ��������������,
	 *  	accept: ���������,
	 *  ��� ���������:
	 *  	accept: ��������� ��� ��������� (� ���������),
	 *  	cancel: �������� �������������� = �����,
	 */
	public static final String ACTION_TAG_CANCEL = "CANCEL"; 
	public static final String ACTION_TAG_ACCEPT_EDIT = "ACCEPT_EDIT";
	public static final String ACTION_TAG_ACCEPT_EDIT_NEW = "ACCEPT_EDIT_NEW";

	public static final String EDIT_ACCESS_ROLES = "editAccessRoles";

	// public static final String ACTION_TAG_LIST = "LIST";
	public static final ObjectId personDeptAttrId = ObjectId.predefined(CardLinkAttribute.class, "jbr.person.dept");
	public static final ObjectId personDeptFullNameAttrId = ObjectId.predefined(TextAttribute.class, "jbr.department.fullName");
	public static final ObjectId personPositionAttrId = ObjectId.predefined(StringAttribute.class, "jbr.person.position");
	
	public static final ObjectId personSnameAttrId = ObjectId.predefined(StringAttribute.class, "jbr.person.lastName");
	public static final ObjectId personNameAttrId = ObjectId.predefined(StringAttribute.class, "jbr.person.firstName");
	public static final ObjectId personMnameAttrId = ObjectId.predefined(StringAttribute.class, "jbr.person.middleName");
	
	public static final ObjectId delegationMessageId = ObjectId.predefined(StringAttribute.class, "jbr.delegation.message");
	public static final ObjectId delegationFromId = ObjectId.predefined(PersonAttribute.class, "jbr.delegation.from");
	public static final ObjectId delegationToId = ObjectId.predefined(PersonAttribute.class, "jbr.delegation.to");
	public static final ObjectId delegationDateStartId = ObjectId.predefined(DateAttribute.class, "jbr.delegation.date.start");
	public static final ObjectId delegationDateEndId = ObjectId.predefined(DateAttribute.class, "jbr.delegation.date.end");
    public static final ObjectId delegationId = ObjectId.predefined(LongAttribute.class, "jbr.delegation.id");
	/***************************************************************************
	 * PRIVATE
	 ***************************************************************************/
	private PortletService portletService;
	private boolean redirected = false;

	private final static String NOTIF_NOT_FOUND_MSG = "������ ��������: �������� ����������� � ������������� �� �������";
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
	 * Get SessionBean.
	 * @param request PortletRequest
	 * @return DelegateListPortletSessionBean
	 */
	public static synchronized DelegateListSessionBean getSessionBean( PortletRequest request) {
		try {
			final PortletSession session = request.getPortletSession();
			if( session == null )
				return null;

			DelegateListSessionBean sessionBean = (DelegateListSessionBean) 
				session.getAttribute(SESSION_BEAN_DELEGATE);

			if( sessionBean == null )
				sessionBean = createBean( request);
			else {
			    String userName = (String) request.getPortletSession().getAttribute(DataServiceBean.USER_NAME, PortletSession.APPLICATION_SCOPE);
	            if (userName != null) {
	                sessionBean.getDataServiceBean().setUser(new UserPrincipal(userName));
	                sessionBean.getDataServiceBean().setIsDelegation(true);
	                sessionBean.getDataServiceBean().setRealUser(request.getUserPrincipal());
	            } else {
	                sessionBean.getDataServiceBean().setUser(request.getUserPrincipal());
	                sessionBean.getDataServiceBean().setIsDelegation(false);
	            }
			}

			request.getPortletSession().setAttribute( SESSION_BEAN_DELEGATE, sessionBean);

			return sessionBean;

		} catch (Exception ex) {
			logger.error("An error has occured while trying to get the DelegateListSessionBean", ex);
			return null;
		}

	}

	private static synchronized DelegateListSessionBean createBean(PortletRequest request)
	{
		final DelegateListSessionBean sessionBean = new DelegateListSessionBean();
		initBean( sessionBean, request);

		// final String formAttrName = getFormSessionAttributeName(request);
		// request.getPortletSession().setAttribute( /*SESSION_BEAN_DELEGATE*/ formAttrName, sessionBean);

		return sessionBean;
	}

	static synchronized void initBean(DelegateListSessionBean bean, PortletRequest request)
	{
		if (bean == null) return; 

		bean.clear();

		setBeanPreferences( bean, request.getPreferences() );

		final DataServiceBean serviceBean = PortletUtil.createService(request);
		bean.setDataServiceBean(serviceBean);

		// bean.setPropXXX();
		final String userId = (serviceBean.getPerson() == null) 
						? null 
						: JspUtils.convertId2Str( serviceBean.getPerson().getId());
		bean.setUserIdStr(userId);
	}

	static String safeGetMsg(ResourceBundle resource, String msgTag, String msgDefault) 
	{
		try {
			return (resource != null) ? resource.getString(msgTag) : msgDefault;
		} catch (Throwable ex) {
			logger.error("An error has occured while trying to get a message safely", ex);
			return msgDefault;
		}
	}

	/**
	 * ��������� ������������� �� ������ ��������.
	 * @param bean
	 * @param preferences
	 */
	static synchronized void setBeanPreferences( DelegateListSessionBean bean, 
			PortletPreferences preferences) 
	{
		if (preferences == null || bean == null) return;

		boolean flag = Boolean.parseBoolean( preferences.getValue(
				"showBtnRefresh",
				Boolean.toString( DelegateListSessionBean.DEFAULT_ShowBtnRefresh)));
		bean.setShowBtnRefresh(flag);

		flag = Boolean.parseBoolean( preferences.getValue(
				"showToolbar",
				Boolean.toString( DelegateListSessionBean.DEFAULT_ShowToolbar)));
		bean.setShowToolbar(flag);


		// ���������...
		final boolean showTitle = Boolean.parseBoolean( preferences.getValue(
				"showTitle", "true" ));

		// DONE: "title.my", "title.history", "title.2me"
		final String title = preferences.getValue("title", "");
		bean.setTitle( new DelegateListSessionBean.Title( title, showTitle));

		final PortletService psrvc = Portal.getFactory().getPortletService();
		// TODO: columns definitions or restrictions...
	}


	/**
	 * �������� ������ �������������� � ������� ������.
	 * @param request
	 * @param createIfNo: ���� true, �� ��� ���������� - ������� ����� ������.
	 * @return
	 */
	public static synchronized DelegateEditBean getEditSessionBean( PortletRequest request, 
			boolean createIfNo ) throws DataException, ServiceException{
		DelegateEditBean editBean = (DelegateEditBean) request.getPortletSession().getAttribute(BEAN_EDIT);
		if (editBean == null && createIfNo) {
			editBean = new DelegateEditBean();
			request.getPortletSession().setAttribute(BEAN_EDIT, editBean);

			final DelegateListSessionBean bean = getSessionBean(request);
			if (bean != null) {
				editBean.setUserList( bean.getPersonsDic() );
				editBean.setEditAccessExists(bean.isEditAccessExists());
		        editBean.setCurrentUserId(bean.getUserIdStr());
				//editBean.setPermissionSets( new ArrayList<PermissionSet>(bean.getPermissionsDic()) );
			}
		}
		return editBean;
	}

	/**
	 * @see javax.portlet.Portlet#init()
	 */
	@Override
	public void init() throws PortletException
	{
		super.init();
		portletService = Portal.getFactory().getPortletService();

		// String isFavorites = getInitParameter(FAVORITES_PARAM_KEY);
		// isFavoritesViewPortlet = "true".equalsIgnoreCase(isFavorites);

		logger.info( String.format( "%s init() SUCCESSFULL", this.getClass().getName() )); //$NON-NLS-1$
	}


	/*
	@Override
	public void init(PortletConfig config) 
		throws PortletException 
	{
		super.init(config);

		// String isFavorites = super.getInitParameter(FAVORITES_PARAM_KEY);
		// isFavoritesViewPortlet = "true".equalsIgnoreCase(isFavorites);

		System.out.format( "%s init(config) done", this.getClass().getName() ); //$NON-NLS-1$
	}
	 */


	void debugPrintRequest(ActionRequest request) {
		final StringBuffer buf = new StringBuffer( "processAction: request attributes: \n" );
		Enumeration<?> enumAttributes = request.getAttributeNames();
		int i=0;
		while (enumAttributes .hasMoreElements()) {
			final String attrKey = enumAttributes.nextElement().toString();
			++i;
			buf.append( MessageFormat.format( "\t[{0}]\t''{1}''\t\t''{2}'' \n", 
						new Object[] { i, attrKey, request.getAttribute(attrKey)} ));
		}
		buf.append( "\n\t request parameters: \n" );
		Enumeration<?> enumParams = request.getParameterNames();
		i=0;
		while (enumParams.hasMoreElements()) {
			final String paramKey = enumParams.nextElement().toString();
			++i;
			buf.append( MessageFormat.format( "\t[{0}]\t''{1}''\t\t''{2}'' \n", 
						new Object[] { i, paramKey, request.getParameter(paramKey)} ));
		}
		logger.info( buf.toString());
	}


	/**
	 * Process an action request.
	 * @see javax.portlet.Portlet#processAction(javax.portlet.ActionRequest, javax.portlet.ActionResponse)
	 */
	@Override
	public void processAction(ActionRequest request, ActionResponse response) 
			throws PortletException, java.io.IOException 
	{
		// invalid: super.processAction(request, response);

		ContextProvider.getContext().setLocale(request.getLocale());

		// debugPrintRequest(request);

		final String action = request.getParameter(PARAM_ACTION);

		logger.info( String.format("DelegationPortlet: processing action [%s]...", action));
		if( action != null ) {
			this.redirected = false;
			final DelegateListSessionBean sessionBean = getSessionBean(request);
			final boolean isDelegation = sessionBean.getDataServiceBean().getIsDelegation() 
			                                && sessionBean.getPgTab() == EDelegatePgTab.delegateFromPerson;
			try {
				//�������� � �������������� ������������� ��������� ��������� � ��������� ������ ������������� ����� ����� 
				if (action.equals(ACTION_TAG_FILTER)) {
				} else if (action.equals(ACTION_TAG_REFRESH)) {
					handleRefreshAction( request, response);
				} else if (action.equals(ACTION_TAG_GOTO_TAB)) {
					handleGotoTab( request, response);
				} else if (action.equals(ACTION_TAG_BACK)) {
					handleBackAction( request, response);
				} else if (action.equals(ACTION_TAG_EDIT_DELEGATE)) {
					if (sessionBean.isEditAccessExists()){
						handleEditAction( request, response);
					} else {
						throw new DataException("admin.edit.access.error");
					}
				} else if (action.equals(ACTION_TAG_CREATE_DELEGATE)) {
					if (sessionBean.isEditAccessExists()){
						handleCreateAction( request, response);
					} else {
						throw new DataException("admin.edit.access.error");
					}
				} else if (action.equals(ACTION_TAG_CANCEL)) {
					if (sessionBean.isEditAccessExists()){
						handleCancelAction( request, response);
					} else {
						throw new DataException("admin.edit.access.error");
					}
				} else if (action.equals(ACTION_TAG_ACCEPT_EDIT)) {
					if (sessionBean.isEditAccessExists()){
						handleAcceptAction( request, response, false);
					} else {
						throw new DataException("admin.edit.access.error");
					}
				} else if (action.equals(ACTION_TAG_ACCEPT_EDIT_NEW)) {
					if (sessionBean.isEditAccessExists()){
						handleAcceptAction( request, response, true);
					} else {
						throw new DataException("admin.edit.access.error");
					}
				} else if (action.equals(ACTION_TAG_DELETE_DELEGATE)) {
					if (sessionBean.isEditAccessExists()){
						handleDeleteAction( request, response);
					} else {
						throw new DataException("admin.edit.access.error");
					}
				 }
				else {
					// final String msg = getPortletConfig().getResourceBundle(request.getLocale()).getString("db.side.error.msg"); 
					throw new PortletException( MessageFormat.format( 
							"{0}: unsupported portlet action ''{1}''", 
							new Object[] { this.getClass().getName(), action} 
					));
				}
			} catch (DataException ex) {
				if (sessionBean != null) {
					logger.error(ex);
					sessionBean.setErrorMsg(ex.getMessage());
				} else
					throw new PortletException(ex);
			} catch (Exception ex) {
				if (sessionBean != null) {
					logger.error(ex);
					sessionBean.setErrorMsg(ex.getMessage());
				} else
					throw new PortletException(ex);
			}

			if (!redirected) {
				// ����� ��������� ��������� redirect, ����� ���������� ����� ���������
				// ������ ����� ���������
				final HashMap reqArgs = new HashMap( request.getParameterMap());
				reqArgs.remove(PARAM_ACTION);
				response.setRenderParameters(reqArgs);
			}
		}
	}


	private void handleRefreshAction( ActionRequest request,
			ActionResponse response) 
		throws DataException, ServiceException 
	{
		final DelegateListSessionBean bean = getSessionBean(request);
		initBean( bean, request);
		loadBeanData( bean);
	}


	private void handleCancelAction(ActionRequest request,
			ActionResponse response) throws IOException, DataException, ServiceException
	{
		final DelegateEditBean editBean = getEditSessionBean(request, false);
		final boolean isDelegateEdit = (editBean != null);

		if (isDelegateEdit) {
			// ������ ������ ��� ���������
			request.getPortletSession().removeAttribute(BEAN_EDIT);
		} else {
			// ���������
			handleBackAction(request, response);
		}
	}


	private void handleEditAction(ActionRequest request, ActionResponse response) throws DataException, ServiceException
	{
		final DelegateListSessionBean bean = getSessionBean(request);
		
		prepareBeanView(bean, request);

		// ����� id �������������� �������� �� URL...
		final String delegateIdx = portletService.getUrlParameter(request, PARAM_DELEGATE_IDX);
		final int idx = (delegateIdx != null) ? Integer.parseInt(delegateIdx) - 1 : -1;

		// �������� �������� �� �������� ������ ����...
		final Delegation delegate = bean.getEditDelegateByIdx( idx);
		if (delegate == null) {
			// (?) ����������� ��������� ���...
			bean.setTextMessage( getPortletConfig().getResourceBundle(request.getLocale()).getString("edit.unknown.id") + "("+ delegateIdx +")");
			return;
		}

		// bean.setDelegateId(delegateId);

		final DelegateEditBean editBean = getEditSessionBean(request, true);
		editBean.setCreateNew(false);
		editBean.setDelegateData(delegate, idx);
	}


	private void handleCreateAction(ActionRequest request, ActionResponse response) throws DataException, ServiceException
	{
		final DelegateListSessionBean bean = getSessionBean(request);
		// bean.setDelegateId(null);
		final DelegateEditBean editBean = getEditSessionBean(request, true);
		editBean.setDelegateData(null, -1); 
		editBean.setUser_from( (bean.getUserIdStr() == null) ? "" :  bean.getUserIdStr() );
	}


	private void handleDeleteAction(ActionRequest request,
			ActionResponse response) throws DataException, ServiceException 
	{
       final DelegateListSessionBean bean = getSessionBean(request);
       
        // ����� id �������������� �������� �� URL...
        final String delegateIdx = portletService.getUrlParameter(request, PARAM_DELEGATE_IDX);
        final int idx = (delegateIdx != null) ? Integer.parseInt(delegateIdx) - 1 : -1;

        Delegation delegation = bean.getEditDelegateByIdx(idx);
        
        //������� �������� ����������� � �������
        sendNotificationCardToTrash(bean, delegation);
        
        ObjectId objectId = delegation.getId();
        
        if (objectId == null) {
            bean.getEditDelegates().remove(idx);
        } else {
            Long id = (Long) objectId.getId();
            DeleteDelegateAction action = new DeleteDelegateAction(id);
            int rows = (Integer) bean.getDataServiceBean().doAction(action);
            bean.getEditDelegates().remove(idx);
            if (rows == 0) {
                throw new DataException( "general.unique", new Object[] {"'DELETE' operation failed: id = " + id} );
            }
        }
        handleRefreshAction(request, response);
	}
	

	private Card findNotificationCardByDelegation(DataServiceBean service, Delegation delegation) throws DataException, ServiceException {
		
        //����� �������� ����������� � �������������(2290) �� ��������� 'DLGT_ID'
		Search search = new Search();
	    search.setByAttributes(true);
	    
	    List<DataObject> templates = new ArrayList<DataObject>();
	    templates.add(DataObject.createFromId(ObjectId.predefined(Template.class, "jbr.delegate_notice")));
	    
	    search.setTemplates(templates);
	    search.addAttribute(delegationId, delegation.getId().getId());
	    SearchResult.Column nameColumn = new SearchResult.Column();
		nameColumn.setAttributeId(Attribute.ID_NAME);
		List columns = new ArrayList(1);
		columns.add(nameColumn);
		search.setColumns(columns);
		
	    SearchResult res = (SearchResult)service.doAction(search);
	    List foundCards = res.getCards();
        if(foundCards == null || foundCards.isEmpty())
        	throw new DataException(NOTIF_NOT_FOUND_MSG);
        //��������� �������� �����������
        ObjectId cardId = ((Card)res.getCards().get(0)).getId();
        Card card = (Card)service.getById(cardId);
        return card;
	}
	
	private void sendNotificationCardToTrash(DelegateListSessionBean bean, Delegation delegation) throws DataException, ServiceException {
		
		DataServiceBean service = bean.getDataServiceBean();
		//�������� �����������
		Card card = findNotificationCardByDelegation(service, delegation);
        
		//������ - ������������
        final ObjectId acq = ObjectId.predefined(CardState.class, "acquaintance");
        //������ - �����������
        final ObjectId acqComleted = ObjectId.predefined(CardState.class, "competent");
        
        WorkflowMove wfm = null;
        if(acq.equals(card.getState())) {
        	wfm = (WorkflowMove)service.getById(ObjectId.predefined(WorkflowMove.class, "jbr.delegate_notice.toTrashFromAcq"));
        } else if(acqComleted.equals(card.getState())) {
        	wfm = (WorkflowMove)service.getById(ObjectId.predefined(WorkflowMove.class, "jbr.delegate_notice.toTrashFromAcqCompl"));
        }
        
        if(wfm != null) {
        	ChangeState sendToTrash = new ChangeState();
            sendToTrash.setCard(card);
        	sendToTrash.setWorkflowMove(wfm);
        	service.doAction(sendToTrash);
        }
	}	

	private void handleAcceptAction(ActionRequest request,
			ActionResponse response, boolean isCardNew) 
		throws DataException, IOException, ServiceException 
	{
		boolean removeEditBean = true;
		
		final DelegateListSessionBean bean = getSessionBean(request);

		final DelegateEditBean editBean = getEditSessionBean(request, false);
		//final boolean isDelegateEdit = (editBean != null);
		
		        // fix BR4J00006666: ���������� �������� ������ "�������" � "���������" 
		//if (isDelegateEdit) {  
			// ������� ������ �� ���� ���������, � ����� ������ ���...
			try {
			    fillInEditDelegateBean( request, editBean);
			    if(!checkFilledParams(request, editBean, bean)) {
			    	removeEditBean = false;
			    	editBean.setRefreshUserFrom(true);
			    	return;
			    }
			    
                    // �������� - "����" � "�� ����" �� ���������������� ������ �������������
                final List<PersonView> persons = DelegateHelper.getPersonViewDelegatableList(bean.getDataServiceBean());
                boolean isFrom = false, isTo = false;
                
                Long fromId = JspUtils.convertStr2IdLong(editBean.getUser_from());
                Long toId = JspUtils.convertStr2IdLong(editBean.getUser_to());
                if (fromId != null && toId != null) {
                    for (PersonView person : persons) {
                        Long personId = (Long) person.getId().getId();
                        if ( fromId.equals(personId) ) {isFrom = true;}
                        if ( toId.equals(personId) ) {isTo = true;}
                        if (isFrom && isTo) break;
                    }
                }
                
                if (!isFrom || !isTo) {
                    // error 
                    bean.getAllDelegates();
                    bean.setTextMessage(
                            getPortletConfig().getResourceBundle(request.getLocale())
                                .getString("DelegateListPortlet.error")
                        );
                    return;
                }
			    
				final int editIdx = editBean.getEditDelegateIdx(); 
				Delegation delegate = bean.getEditDelegateByIdx(editIdx);
				if (delegate == null) {
					delegate = new Delegation(); // �����
					bean.getEditDelegates().add(delegate); // <- ���������� ������ � ����� ���
				}
				if(isCardNew) {
					DataServiceBean service = bean.getDataServiceBean();
					//���� �������� ������������� - ������� ����
					delegate.setCreatedAt(new Date());
					if(service.getIsDelegation()) {
			        	delegate.setCreatorId(service.getRealUser().getPerson().getId());
			        } else delegate.setCreatorId(service.getPerson().getId());
				}
				editBean.getDelegateData(delegate);
				delegate.setEndAt(new Date(delegate.getEndAt().getTime()));
			} finally {
				if(removeEditBean) {
					request.getPortletSession().removeAttribute(BEAN_EDIT);
				}
			}
		//} else {
			try {
				// ����������...
				saveBeanData( bean);
				
				sendNoticeForToUser(bean, editBean, isCardNew); // ������� ����������� ������������, �������� ������������ �����

				// (?) ���������: handleBackAction(request, response);

				// ������������ ������...
				handleRefreshAction(request, response);

				bean.setTextMessage(
						getPortletConfig().getResourceBundle(request.getLocale())
							.getString("DelegateListPortlet.save_success")
					);
			} catch (Throwable ex) {
				bean.setErrorMsg(ex.getMessage());
			}
		//}
	}


	private void sendNoticeForToUser(DelegateListSessionBean bean, DelegateEditBean editBean, boolean isCardNew) throws DataException, ServiceException {
	       // ������� �������� �����������
	    String userFromStr = editBean.getUser_from();
	    String userToStr = editBean.getUser_to();
	    
	    if (userFromStr == null || userToStr == null) {
	        // warn!
	        return;
	    }
	    
        DataServiceBean service = new DataServiceBean();
        service.setUser(new SystemUser());
        service.setAddress("localhost");
        
        Delegation delegation = bean.getEditDelegateByIdx(editBean.getEditDelegateIdx());
        
        //�������� �����������
        Card noticeCard = null;
        
        if(isCardNew) {
        	//������� ����� ��������
        	
	        CreateCard createCard = new CreateCard();
	        ObjectId templateId = ObjectIdUtils.getObjectId(Template.class, "jbr.delegate_notice", true);
	        createCard.setTemplate( templateId );
	        createCard.setLinked(true);
	        try {
	            noticeCard = (Card) service.doAction(createCard);
	        }  catch (Exception e) {
	        	logger.error("An error has occured while trying to create a notification card for delegation", e);
	        }
        } else {//����������� ��� ������������ ��������
        	
        	noticeCard = findNotificationCardByDelegation(service, delegation);
        	
        }
        
        Long userFromId = Long.parseLong(userFromStr);
        Long userToId = Long.parseLong(userToStr);
        Person userFrom = null;
        Person userTo = null;
        for (Person person : bean.getPersonsDic()) {
            if (person.getId().getId().equals(userFromId)) {userFrom = person;}
            if (person.getId().getId().equals(userToId)) {userTo = person;}
            if (userFrom != null && userTo != null) {break;}
        }
        
        //  ��������� ����
        PersonAttribute personAttr;
        personAttr = (PersonAttribute) noticeCard.getAttributeById(delegationFromId); // FROM
        personAttr.setPerson( userFrom ); 

        personAttr = (PersonAttribute) noticeCard.getAttributeById(delegationToId); // To
        personAttr.setPerson( userTo );

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        DateAttribute dateAttr;
        String startDateStr = editBean.getFrom_date();
        String endDateStr = editBean.getTo_date();
        
        if (startDateStr != null && !"".equals(startDateStr)) {
            dateAttr = (DateAttribute) noticeCard.getAttributeById(delegationDateStartId); // From date
            try {
                dateAttr.setValue(format.parse(startDateStr));
            } catch (ParseException e) {
            	logger.error(e);
            }
        }

        if (endDateStr != null && !"".equals(endDateStr)) {
            dateAttr = (DateAttribute) noticeCard.getAttributeById(delegationDateEndId); // From date
            try {
                dateAttr.setValue(new Date(format.parse(endDateStr).getTime()));
            } catch (ParseException e) {
            	logger.error(e);
            }
        }

        if(delegation == null) {
            delegation = bean.getEditDelegateByIdx(bean.getEditDelegates().size()-1);
        }

        if(delegation != null) {
            LongAttribute delegationIdAttribute = (LongAttribute) noticeCard.getAttributeById(delegationId);
            delegationIdAttribute.setValue((Long) delegation.getId().getId());
        }
        
        //��������� �� ����� ������������
        service = bean.getDataServiceBean();

        // ���������
        try {
        	//lock
        	LockObject lock = new LockObject(noticeCard);
        	service.doAction(lock);
        	
            ObjectId noticeCardId = service.saveObject(noticeCard);
            
            // unlock
            UnlockObject unlockCard = new UnlockObject(noticeCardId);
            service.doAction(unlockCard);
        }  catch (Exception e) {
        	logger.error("An error has occured while trying to save delegation notification card", e);
        }
	}
	
	//�������� ���������� �� �������������
	private boolean checkFilledParams(ActionRequest request, DelegateEditBean editBean, DelegateListSessionBean bean) {
		
		String dateOrder = "���� ��������� ������������� �� ����� ���� ������ ���� ������.";
		String delegPersonErr = "�������� ��������� '�� ����' � '����' �� ������ ���������.";
		
		if(editBean == null) return false;
		List<String> msgs = new ArrayList<String>();
				
		if(editBean.getUser_from() == null || editBean.getUser_from().equals("")) {
			msgs.add("'�� ����'");
		}
		if(editBean.getUser_to() == null || editBean.getUser_to().equals("")) {
			msgs.add("'����'");
		}
		if(editBean.getFrom_date() == null || editBean.getFrom_date().equals("")) {
			msgs.add("'���� ������ �������������'");
		}
		if(editBean.getTo_date() == null || editBean.getTo_date().equals("")) {
			msgs.add("'���� ��������� �������������'");
		}
		
		int size = msgs.size();
		
		//������������ ��������� �� ������
		if(size>0) {
			StringBuilder sb = new StringBuilder();
			if(size == 1) {
				sb.append("������� ");
			} else sb.append("�������� ");
			for(int i=0; i<size; i++) {
				sb.append((String)msgs.get(i));
				if(i != (size-1)) {
					sb.append(", ");
				}
			}
			if(size == 1) {
				sb.append(" ������ ���� ��������.");
			} else sb.append(" ������ ���� ���������.");
			
			bean.setErrorMsg(sb.toString());
			return false;
		}
		
		String fromDateString = editBean.getFrom_date();
		String toDateString = editBean.getTo_date();
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try{
			Date fromDate = fmt.parse(fromDateString);
			Date toDate = fmt.parse(toDateString);
			if(fromDate.after(toDate)) {
				bean.setErrorMsg(dateOrder);
				return false;
			}
		}catch(ParseException e) {
			logger.error("An error has occured while trying to parse a delegation date", e);
			return false;
		}
		
		if(editBean.getUser_from().equals(editBean.getUser_to())) {
			bean.setErrorMsg(delegPersonErr);
			return false;
		}
		return true;
	}
	

	private void fillInEditDelegateBean(ActionRequest request,
		DelegateEditBean editBean) 
	{
		if (editBean == null) return;

		String value;// = request.getParameter("roleId");
//		if (value != null)
	//		editBean.setRoleId(value);

		value = request.getParameter("user_from");
		if (value != null)
			editBean.setUser_from(value);

		value = request.getParameter("user_to");
		if (value != null)
			editBean.setUser_to(value);

		value = request.getParameter("from_date");
		if (value != null)
			editBean.setFrom_date(value);

		value = request.getParameter("to_date");
		if (value != null)
			editBean.setTo_date(value);

		// (!) checkBox ��������� ���������� ������ ��� ������������� ���������,
		// ����� ���� �� ����������.
		/*value = request.getParameter("active");
		editBean.setActive( JspUtils.getChkBoxValue(value));

		value = request.getParameter("exclusive");
		editBean.setExclusive( JspUtils.getChkBoxValue(value));*/
	}


	private void handleBackAction(ActionRequest request, ActionResponse response) 
		throws IllegalArgumentException, IllegalStateException, IOException
	{
		final DelegateListSessionBean bean = getSessionBean(request);

		String backURL = bean.getBackURL();
		bean.clear();
		request.getPortletSession().removeAttribute(SESSION_BEAN_DELEGATE);
		request.getPortletSession().removeAttribute(BEAN_EDIT);
		// ... ������� Application Scope ...

		if (backURL == null)
			backURL = portletService.generateLink("dbmi.defaultPage", null, null, request, response);
		response.sendRedirect(backURL);
		this.redirected = true;
	}


	private void handleGotoTab(ActionRequest request, ActionResponse response) 
	{
		final DelegateListSessionBean bean = getSessionBean(request);
		bean.setPgTabByTag( request.getParameter(PARAM_TAB_ID) );

		if (!bean.isSelectableFilterByActiveMode()) 
		{
			// ���� �������� ����� ���������� - ��������� �������� ��-���������
			final EFilterByActiveMode mode = mapDefaultShowActive.get(bean.getPgTab());
			if (mode != null)
				bean.setFilterByActiveMode(mode);
		}

		// if (bean.isEnWebSelectFilterByRoleId())
	}


	/**
	 * �����������.
	 */
	@Override
	protected void doView(RenderRequest request, RenderResponse response)
			throws PortletException, PortletSecurityException, IOException 
	{
		// System.out.format( "%s doView() ENTER", this.getClass().getName() ); //$NON-NLS-1$

		// Set the MIME type for the render response
		response.setContentType(request.getResponseContentType());

		// Set user locale
		ContextProvider.getContext().setLocale(request.getLocale()); 

		// (!) Process URL parameters
		setParamsFromExternalRequest(request, response);		

		// bean.setResourceBundle(getPortletConfig().getResourceBundle(request.getLocale()));		
		final DelegateListSessionBean bean = getSessionBean(request);		
		
		// Check if portlet session exists
		final ResourceBundle resource = getPortletConfig().getResourceBundle(request.getLocale());
		if( bean == null ) {
			final String msg = (resource != null) 
						? resource.getString("DelegateListPortlet.no_session_bean") //$NON-NLS-1$
						: "No Session Bean (and even no portlet resource bundle)";
			response.getWriter().println( msg);
			return;
		} 		
				
		try { // catch

			if (bean.getColumns() == null || bean.isNeedReload())
				this.loadBeanData( bean);

			// ���������� ������������� �������
			this.prepareBeanView( bean, request);
			
		} catch (Exception ex) {
			logger.error(ex);
			final String msg = safeGetMsg( resource,
						"db.side.error.msg", 
						"System error: "); //$NON-NLS-1$
			bean.setErrorMsg( msg + ex.getMessage());
		}
		
		final DelegateEditBean editBean = (DelegateEditBean) request.getPortletSession().getAttribute(BEAN_EDIT);
		
		if (editBean!=null){	
			
			editBean.setUserList( bean.getPersonsDic() );
			editBean.setCurrentUserId(bean.getUserIdStr());	        
			editBean.setUser_from(bean.getUserIdStr());				
	        //request.getPortletSession().setAttribute(BEAN_EDIT, editBean);
		}
		
		final String msgTitle = resource.getString( bean.getActivePgTitleKey() ); 
		bean.setTitleText( msgTitle);

		final PortletService psrvc = Portal.getFactory().getPortletService();
		String editAccessRoles = psrvc.getPageProperty(EDIT_ACCESS_ROLES, request, response);
		if (editAccessRoles != null&&!editAccessRoles.isEmpty()){
			bean.setEditAccessRoles(editAccessRoles);
		}
		
		// Invoke the JSP to render
		final PortletRequestDispatcher rd = getPortletContext().getRequestDispatcher(
				getJspFilePath(request, JSP_PORTLET_DELEGATE_LIST));
		rd.include(request,response);
	}


	private void setParamsFromExternalRequest(RenderRequest request, RenderResponse response) 
	{
		final DelegateListSessionBean bean = getSessionBean(request);

		/* 
		 * ���������-�������� �������� ...
		 */

		bean.setSourceUserSelectable(DelegateHelper.isFromPersonSelectable(bean.getDataServiceBean(), true));

        String sPg = portletService.getPageProperty(PORTLET_PARAM_PAGE_TAB, request, response);
        if (sPg != null) {
            bean.setPgTabByTag(sPg);
        }
        String s = portletService.getPageProperty(PORTLET_PARAM_SELECTABLE_ACTIVE_MODE_FILTER, request, response);
		if (s != null)
			bean.setSelectableFilterByActiveMode( Boolean.parseBoolean(s)); 
		/*
		s = portletService.getPageProperty(PORTLET_PARAM_SELECTABLE_ROLE_ID_FILTER, request, response);
		if (s != null)
			bean.setSelectableFilterByRoleId( Boolean.parseBoolean(s) );
		*/
		/*
		 * ��������� �� �������� WEB-�����  ...
		 */
		/*if (bean.isSelectableFilterByRoleId()) {
			final String value = request.getParameter( "filterByRoleIdStr");
			if (value != null)
				bean.setFilterByRoleIdStr( value);
		}
		*/
		if (bean.isSelectableFilterByActiveMode()) {
			final String value = portletService.getPageProperty(PORTLET_PARAM_SELECTABLE_ACTIVE_MODE_NAME, request, response);
			if (value != null)
				bean.setFilterByActiveModeStr( value);
		}

		/*
		 * ��������� � URL...
		 */
		final String backURL = portletService.getUrlParameter(request, PARAM_BACK_URL);
		if (backURL != null)
			bean.setBackURL(backURL);

		final String userId = portletService.getUrlParameter(request, PARAM_USER_ID);
		if (userId != null)
			bean.setUserIdStr( userId);

		// ���� � ��������� �� ������ page_tab - �������� �������� ��� �� URL
		if (sPg == null) {
		    sPg = portletService.getUrlParameter(request, PARAM_TAB_ID);
		      if (sPg != null)
		            bean.setPgTabByTag(sPg);
		}


		// final String sDelegateId = portletService.getUrlParameter(request, PARAM_DELEGATE_IDX);
		// if (sDelegateId != null) ...;

	}


	/*
	@Override
	protected void doEdit(RenderRequest request, RenderResponse response)
			throws PortletException, PortletSecurityException, IOException {
		// System.out.format( "%s doEdit() ENTER", this.getClass().getName() ); //$NON-NLS-1$
		super.doEdit(request, response);
		// System.out.format( "%s doEdit() LEAVE", this.getClass().getName() ); //$NON-NLS-1$
	}
	 */


	// �������� ��� ������� Column.action
	static public final String COL_ACTION_EDIT = "edit";
	static public final String COL_ACTION_DELETE = "delete";

	static public final int defColWidthInEm = 50;

	/**
	 * �������� ������ ������� ��� ����������� � ������� ���������.
	 * �� ������ ������ ����� - ����� �������� Search.Column.
	 * ������ �������� ����� �������:
	 * 	(1)String, NOT NULL: En-�������� �������,
	 * 	(2)String, NOT NULL: Ru-�������� �������,
	 * 	(3)Integer, null == 100: ������ ��� ��������� ������� � �������� html/em, 
	 * 	(4)Boolean, null == true: ���� �� ���������� �� �������� �������� 
	 * ������������� ("�� ���� ������"),
	 * 	(5) -//-: �� �������� "�������",
	 * 	(6) -//-: �� �������� "�������������� �� ������"�
	 * 	(7) String, ������ null: ������, ��������� � Column.action (��������,
	 * ��� �������� ������� �������� ������ ������� ������ JSP).
	 * ���������� ������-���� �������� ����������� null-��������.
	 * @SEE COL_DEFCOLUMNS_XXX		��������� ������� ������ �����,
	 * @SEE COL_ACTION_XXX			�������� (������� action ������ �����),
	 * @SEE IDX_DEFCOLUMNS_XXX 		��������� ������.
	 */
	static final Object[][] DEFCOLUMNS_DELEGATE = {
		// en-name			ru name				width[em]	inFrom	inHist	inTo,	action 
		{ "N", 				"�/�", 				10},
		//{ "id",				"id",				10},

		// ������������ ����
		//{ "DELEGATE ROLE",		"������������ ����",100},

		// �� ����
		{ "From user", 			"�� ����",		100,	true, 	true, 	true},
		
		// ������������� ���� ����
		{ "Department from",			"�������������(�� ����)",	120,	false, 	true, 	false},
				
		// ��������� ���� ����
		{ "Position from",			"���������(�� ����)",	120,	false, 	true, 	false},

		// ����
		{ "DEST",				"����",			100,	true, 	true, 	true},

		// ������������� ����
		{ "Department",			"�������������(����)",	100,	true, 	false, 	false},
		
		// ������������� �� ����
		{ "Department",			"�������������(�� ����)",	100,	false, 	false, 	true},
		
		// ������������� ���� ����
		{ "Department to",			"�������������(����)",	120,	false, 	true, 	false},
		
		// ��������� ����
		{ "Position",			"���������(����)",	100,	true, 	false, 	false},
		
		// ��������� �� ����
		{ "Position",			"���������(�� ����)",	100,	false, 	false, 	true},
		
		// ��������� ���� ����
		{ "Position to",			"���������(����)",	120,	false, 	true, 	false},
		
		// ������� � 
		{ "atStart",			"������� �",	50},
		// ����������
		{ "atEnd",				"����������",	50},

		// �������������
		//{ "Exclusive",			"�������������",10},
		// �������
		//{ "Active",				"�������",		10},

		// �������������
		{ "Edit",				"�������������",25,			true, 	false, 	false, 	COL_ACTION_EDIT},
		{ "Delete",             "�������",25,         true,   false,  false,  COL_ACTION_DELETE}

	};

	/**
	 * ��� ���������� ���������� �� ������ ��������� ��-���������.
	 * (!) ��� ��������� ����� ������� ������� ��� ������ ������� ������ ��������.
	 */
	static final Map<EDelegatePgTab, EFilterByActiveMode> mapDefaultShowActive = 
		new HashMap<EDelegatePgTab, EFilterByActiveMode>();
	static {
		mapDefaultShowActive.put(EDelegatePgTab.delegateFromPerson, EFilterByActiveMode._ActiveOnly);
		mapDefaultShowActive.put(EDelegatePgTab.history, EFilterByActiveMode._InactiveOnly);
		mapDefaultShowActive.put(EDelegatePgTab.givenToPerson, EFilterByActiveMode._ActiveOnly);
	}

	/**
	 * COL_DEFCOLUMNS_XXX: C������������ �������� ��������� ������ ������ 
	 * ������ ������� (@SEE DEFCOLUMNS_DELEGATE_FROMPERSON)
	 */
	// en-name/ru-name	width[em]	inFrom	inHist	inTo,	action 
	static final int COL_DEFCOLUMNS_ENNAME			= 0;
	static final int COL_DEFCOLUMNS_RUNAME			= 1;
	static final int COL_DEFCOLUMNS_DEFWIDTH		= 2;

	static final int COL_DEFCOLUMNS_VIS_AT_FROM 	= 3;
	static final int COL_DEFCOLUMNS_VIS_AT_HIST 	= 4;
	static final int COL_DEFCOLUMNS_VIS_AT_TO   	= 5;

	static final int COL_DEFCOLUMNS_ACTION			= 6;


	/**
	 * IDX_DEFCOLUMNS_XXX: ������������� ����������� ���������� ������� - ������ 
	 * ������ ������ ������� ���������� (@SEE DEFCOLUMNS_DELEGATE_FROMPERSON).
	 */
	static final int IDX_DEFCOLUMNS_N 					= 0;
	//static final int IDX_DEFCOLUMNS_ID 			= 1;
	//static final int IDX_DEFCOLUMNS_ROLE 		= 2;

	static final int IDX_DEFCOLUMNS_SRCUSER 			= 1; //3
	
	static final int IDX_DEFCOLUMNS_DEPARTMENT_FROM_HIST= 2; //3
	static final int IDX_DEFCOLUMNS_POSITION_FROM_HIST 	= 3; //3
	
	static final int IDX_DEFCOLUMNS_DESTUSER 			= 4; //4

	static final int IDX_DEFCOLUMNS_DEPARTMENT_TO		= 5; //5
	static final int IDX_DEFCOLUMNS_DEPARTMENT_FROM		= 6; //5
	static final int IDX_DEFCOLUMNS_DEPARTMENT_TO_HIST	= 7; //5
	
	static final int IDX_DEFCOLUMNS_POSITION_TO 		= 8; //6
	static final int IDX_DEFCOLUMNS_POSITION_FROM		= 9; //6
	static final int IDX_DEFCOLUMNS_POSITION_TO_HIST	= 10; //6
	
	static final int IDX_DEFCOLUMNS_ATSTART		= 11; //5
	static final int IDX_DEFCOLUMNS_ATEND 		= 12; //6

	//static final int IDX_DEFCOLUMNS_ISESCLUSIVE	= 7;
	//static final int IDX_DEFCOLUMNS_ISACTIVE	= 8;

	static final int IDX_DEFCOLUMNS_ACTION		= 13; //9


	static final Object safeGet( final Object[] array, int index, Object defaultValue) {
		return (index>=0) && (index<array.length) && (array[index] != null) 
					? array[index] 
					: defaultValue;
	} 

	static final Object safeGet( final Object[] array, int index) {
		return (index>=0) && (index<array.length) ? array[index] : null;
	} 

	
	/**
	 * ���������� ����� �� ������� �������� ����������� �� ��������� ��������.
	 * @param colDefIdx: ������ ��������� ������� � ������� �����������.
	 * @param destTab: ������ ��������.
	 * @return true, ���� �����.
	 */
	static final boolean isColumnVisible( int colDefIdx, EDelegatePgTab destTab)
	{
		
		// ����������� ������� �������� � ������ � �������-������ �����������...
		int colIdx_IsVisible = COL_DEFCOLUMNS_VIS_AT_HIST;
		switch(destTab)
		{
			case delegateFromPerson: 
				colIdx_IsVisible = COL_DEFCOLUMNS_VIS_AT_FROM;
				break;
			case givenToPerson: 
				colIdx_IsVisible = COL_DEFCOLUMNS_VIS_AT_TO;
				break;
			// default: // case history: colIsVisible = COL_DEFCOLUMNS_VIS_AT_HIST;
		} // switch
		// ��������� ��������...
		final Boolean isVisible = (Boolean) safeGet( 
					DEFCOLUMNS_DELEGATE[colDefIdx], 
					colIdx_IsVisible, 
					true);
		return isVisible;
	}


	/**
	 * ������ �������� ��� ������� ������ ��������.
	 * @param column: ���������� �������,
	 * @param destTab: ������� ��������, �� ������� ����� �������.
	 * @param colIdx: ������ ����������� ������� ������
	 * @result true, ���� ������� ����� � false �����.  
	 */
	static final boolean setColumnProps( SearchResult.Column column,  
			EDelegatePgTab destTab, int colDefIdx)
	{
		
		if (column == null) return false;
		if (colDefIdx < 0 || colDefIdx >= DEFCOLUMNS_DELEGATE.length)
			return false;

		final Object[] props = DEFCOLUMNS_DELEGATE[colDefIdx];

		column.setNameEn( (String) props[COL_DEFCOLUMNS_ENNAME]);
		column.setNameRu( (String) props[COL_DEFCOLUMNS_RUNAME]);
		SearchResult.Action action= new SearchResult.Action();
		action.setId((String) safeGet( props, COL_DEFCOLUMNS_ACTION));
		column.setAction(action);

		final boolean isVisible = isColumnVisible(colDefIdx, destTab);
		final Integer defWidth = (Integer) safeGet( props, COL_DEFCOLUMNS_DEFWIDTH);
		final int width = (isVisible)
							? ( (defWidth != null) ? defWidth.intValue() : defColWidthInEm) 
							: 0;
		column.setWidth(width);
		return isVisible;
	}


	private void loadBeanData(DelegateListSessionBean bean) 
		throws DataException, ServiceException
	{
		// DONE: load data

		final DataServiceBean service = bean.getDataServiceBean();

		/*
		 * ������ ������ ������ ... 
		 */
		//if (bean.getPersonsDic() == null || bean.getPersonsDic().isEmpty()) {
			bean.setPersonsDic(DelegateHelper.getPersonDictionary(bean.getDataServiceBean()));
		//}

		/* 
		 * ������ ������ ������������ ����� ...
		 */
/*		if (bean.getPermissionsDic() == null || bean.getPermissionsDic().isEmpty()) {
			bean.setPermissionsDic( getAllPermissionSets( service));
		}
*/
		/*
		 *  (!) ���������� �������� ������� �� ��������
		 */
		final List<Delegation> delegates = DelegateFilterUtil.loadFilteredDelegates(service, null);
		bean.setAllDelegates(delegates);

		// DROP COLUMNS
		bean.setColumns(null);

		// Refresh userId
		final String userId = (service.getPerson() == null) 
									? null 
									: JspUtils.convertId2Str( service.getPerson().getId());
		bean.setUserIdStr(userId);

		bean.setNeedReload(false);
	}


/*	static Set<PermissionSet> getAllPermissionSets( DataServiceBean service) 
		throws DataException, ServiceException
	{
		if (service == null) return null;
		return new HashSet<PermissionSet>( (List) service.listAll(PermissionSet.class));
	}
*/



	/***************************************************************************
	 * �������������� ��� �����������
	 ***************************************************************************/
	private void prepareBeanView( DelegateListSessionBean bean, PortletRequest request) throws DataException, ServiceException
	{
		final DataServiceBean service = bean.getDataServiceBean();
		//BR4J00020661. ���������� ������ ����� ������������.
		loadBeanData(bean);
		/*
		 * ������� � ������� ���������...
		 */
		final List<SearchResult.Column> columns = new ArrayList<SearchResult.Column>();
		final boolean[] column_visible = new boolean[DEFCOLUMNS_DELEGATE.length];
		for (int i = 0; i < DEFCOLUMNS_DELEGATE.length; i++) {			
			final SearchResult.Column column = createColumn( i, defColWidthInEm);
			column_visible[i] = setColumnProps(column, bean.getPgTab(), i);
			if (column_visible[i])
				columns.add(column);
		}
		bean.setColumns(columns);

		/*
		 * �������� ������ ��������� ��� ����������� �� ������� ��������...
		 */
        List<Delegation> viewDelegates = null;
        DelegateFilterUtil.IFilter filter = null;//
        List<Delegation> allDelegates = bean.getAllDelegates();
        
        switch (bean.getPgTab()) {
            case givenToPerson:
                //if (!bean.isSourceUserSelectable()) {
                filter = new DelegateFilterUtil.FilterByToPerson(service.getPerson().getId());
                //}
                viewDelegates = DelegateFilterUtil.funcFilterDelegates(allDelegates, filter);
                
                //������ �� �������� ����������� � �������������
                Iterator<Delegation> itr = viewDelegates.iterator();
                while(itr.hasNext()) {
                	Delegation delegation = itr.next();
                	Card notifCard = findNotificationCardByDelegation(service, delegation);
					if(notifCard == null)
						throw new DataException("Cannot find the notification of delegation");
					/* FIX BR4J00037259
						//���� ����������� �� � ������� ����������� - ������������� �� �������
					if(notifCard.getState() != null
							&& !notifCard.getState().equals(ObjectId.predefined(CardState.class, "competent")))
						itr.remove();
						 
						 */
                }
                DelegateFilterUtil.filterByActiveMode(viewDelegates, bean.getFilterByActiveMode());
                break;

            case delegateFromPerson:
                // ����������� ������ ������ �� ������ �������������
                filter = new DelegateFilterUtil.FilterByPersonViewList(bean.getPersonsDic(), true, true);
                viewDelegates = DelegateFilterUtil.funcFilterDelegates(allDelegates, filter);
                if (!bean.isSourceUserSelectable()) {
                    // ���� ������������ �� ����� ������ ���� "�� ����", �� 
                    // ������������� ����������� �� �������� ������������.
                    filter = new DelegateFilterUtil.FilterByFromPerson(service.getPerson().getId());
                    viewDelegates = DelegateFilterUtil.funcFilterDelegates(viewDelegates, filter);
                }
                DelegateFilterUtil.filterHidden(viewDelegates);
                bean.setEditDelegates(viewDelegates);
                break;

            default: // case history:
                if (!bean.isSourceUserSelectable()) {
                	filter = new DelegateFilterUtil.FilterByPersonHistory(service.getPerson().getId());
                } else {
                	filter = new DelegateFilterUtil.FilterByPersonViewList(bean.getPersonsDic(), true, true);
                }
                viewDelegates = DelegateFilterUtil.funcFilterDelegates(allDelegates, filter);
                DelegateFilterUtil.filterByActiveMode(viewDelegates, bean.getFilterByActiveMode());
                break;
        }

		/*
		 * ������ �� id ������������ ���� ...
		 */
		/*if (bean.isSelectableFilterByRoleId() && bean.getFilterByRoleId() != null)
			viewDelegates = funcFilterDelegates( viewDelegates, new FilterByRoleId( bean.getFilterByRoleId()) );
		

		/*
		 * ������������ ��������� string-������...
		 */
		final String newId = getPortletConfig().getResourceBundle(request.getLocale()).getString("table.newid");
		final ArrayList<List<String>> dataList = new ArrayList<List<String>>();
		if (viewDelegates != null && !viewDelegates.isEmpty()) {
			int nRowId = 0;
			// for (int i = 0; i < list.size(); i++)
			for (Delegation dlg : viewDelegates)
			{				
				Person personTo = null;
				Person personFrom = null;
				
				Card cardTo = null;
				Card cardFrom = null;
				
				try {
					
					personTo = (Person) service.getById(dlg.getToPersonId());
					cardTo = loadCard(service, personTo);
					
					personFrom = (Person) service.getById(dlg.getFromPersonId());
					cardFrom = loadCard(service, personFrom);
					
				} catch (Exception e) {
					logger.error(e);
				}
				// final PermissionDelegate dlg = list.get(i);
				final List<String> row = new ArrayList<String>();

				nRowId++;
				row.add( String.valueOf(nRowId));

				/*if (column_visible[IDX_DEFCOLUMNS_ID])
					row.add( JspUtils.convertId2Str( dlg.getId(), newId));		// ID
				 */
				// DELEGATE ROLE
				/*if (column_visible[IDX_DEFCOLUMNS_ROLE])
					row.add( makePermissionSetInfo( dlg.getPermissonSetId(), bean.getPermissionsDic() ));
				 */

				if (column_visible[IDX_DEFCOLUMNS_SRCUSER])
					row.add( makePersonInfo(cardFrom) );	// person from
				
				if (column_visible[IDX_DEFCOLUMNS_DEPARTMENT_FROM_HIST])
					row.add( makeStringValue(getByCardLink(cardFrom, personDeptAttrId, personDeptFullNameAttrId, service), 
														   personDeptFullNameAttrId));	// person_dept_from_hist
				if (column_visible[IDX_DEFCOLUMNS_POSITION_FROM_HIST])
					row.add( makeStringValue(cardFrom, personPositionAttrId));	// person_pos_from_hist
				
				if (column_visible[IDX_DEFCOLUMNS_DESTUSER])
					row.add( makePersonInfo(cardTo) );	// person_to
				
				if (column_visible[IDX_DEFCOLUMNS_DEPARTMENT_TO])
					row.add( makeStringValue(getByCardLink(cardTo, personDeptAttrId, personDeptFullNameAttrId, service), 
														   personDeptFullNameAttrId));	// person_dept_to
				if (column_visible[IDX_DEFCOLUMNS_DEPARTMENT_FROM])
					row.add( makeStringValue(getByCardLink(cardFrom, personDeptAttrId, personDeptFullNameAttrId, service), 
														   personDeptFullNameAttrId));	// person_dept_from
				if (column_visible[IDX_DEFCOLUMNS_DEPARTMENT_TO_HIST])
					row.add( makeStringValue(getByCardLink(cardTo, personDeptAttrId, personDeptFullNameAttrId, service), 
														   personDeptFullNameAttrId));	// person_dept_to_hist
				
				if (column_visible[IDX_DEFCOLUMNS_POSITION_TO])
					row.add( makeStringValue(cardTo, personPositionAttrId));	// person_pos_to
				if (column_visible[IDX_DEFCOLUMNS_POSITION_FROM])
					row.add( makeStringValue(cardFrom, personPositionAttrId));	// person_pos_from
				if (column_visible[IDX_DEFCOLUMNS_POSITION_TO_HIST])
					row.add( makeStringValue(cardTo, personPositionAttrId));	// person_pos_to_hist
				
				if (column_visible[IDX_DEFCOLUMNS_ATSTART])
					row.add( JspUtils.Date2Str(dlg.getStartAt()) );
				if (column_visible[IDX_DEFCOLUMNS_ATEND])
					row.add(dlg.getEndAt()!=null?(JspUtils.Date2Str(new Date(dlg.getEndAt().getTime()-1000))):"");
				/*
				if (column_visible[IDX_DEFCOLUMNS_ISESCLUSIVE])
					row.add( (!dlg.isFromPersonHasAccessToo() ? "+" : "-"));		// �������������
				if (column_visible[IDX_DEFCOLUMNS_ISACTIVE])
					row.add( (dlg.isActive() ? "+" : "-" ));						// ����������
				*/

				if (column_visible[IDX_DEFCOLUMNS_ACTION])
					row.add( " " );													// edit

				dataList.add(row);
			}
		}
		bean.setDataList(dataList);
	}


	final static String makePersonInfo(Card card) {
		if (card == null)
			return "";
		
		StringAttribute sname = (StringAttribute)card.getAttributeById(personSnameAttrId);
		if (sname == null || sname.isEmpty())
			return "";
		
		StringAttribute name = (StringAttribute)card.getAttributeById(personNameAttrId);
		StringAttribute mname = (StringAttribute)card.getAttributeById(personMnameAttrId);
		
		String result = sname.getValue();
		
		if (name != null && !name.isEmpty())
			result += " " + name.getValue();
		
		if (mname != null && !mname.isEmpty())
			result += " " + mname.getValue();
		

		/*if (persons != null) {
			for (Person item: persons) {
				if (item != null && personId.equals( item.getId())) // FOUND
					// FullName (Login)
					return MessageFormat.format( "{0}",
									item.getFullName()
								); 
			}
		}*/
		
		return MessageFormat.format( "{0}",
				result
				);

		// NOT FOUND
		// return "";
	}
	
	String makeStringValue(Card card, ObjectId attr) {
		if (card == null)
			return "";
		StringAttribute sa = (StringAttribute)card.getAttributeById(attr);
		if (sa == null)
			return "";
		return MessageFormat.format( "{0}",
				sa.getValue()
		); 
	}
	
	Card getByCardLink(Card card, ObjectId attrLink, ObjectId attrDest, DataServiceBean service) {
		if (card == null || attrLink == null || attrDest == null)
			return null;
		if(!CardLinkAttribute.class.equals(attrLink.getType()))
			return null;
		CardLinkAttribute ca = (CardLinkAttribute)card.getAttributeById(attrLink);
		if (ca == null || ca.isEmpty()) 
			return null;
		
		Card resultCard = null;
		
		List<ObjectId> curCardIds = ca.getIdsLinked();
		
		ObjectId curCardId = null;
		if(curCardIds != null && !curCardIds.isEmpty())
			curCardId = curCardIds.get(0);
		
		if(curCardId == null)
			return null;
		
		try {
			List<SearchResult.Column> cols = new ArrayList();
			SearchResult.Column col = new SearchResult.Column();
			col.setAttributeId(attrDest);
			cols.add(col);
			Search search = new Search();
			search.setByCode(true);
			search.setColumns(cols);
			search.setWords(curCardId.getId().toString());
			SearchResult searchResult = (SearchResult) service.doAction(search);
			List resultCards = searchResult.getCards();
			if(resultCards != null && !resultCards.isEmpty())
				resultCard = (Card) resultCards.get(0);
		}	catch (Exception e) {
			logger.error(e);
		}	finally {
				return resultCard;
		}
	}


/*	final static String makePermissionSetInfo( ObjectId permissonSetId,
			Set<PermissionSet> permissions) 
	{
		if (permissonSetId == null)
			return "";

		if (permissions != null) {
			for (PermissionSet item: permissions) {
				if (item != null && permissonSetId.equals( item.getId()) )
					return item.getName().getValue(); // FOUND
			}
		}

		// NOT FOUND: (?) return pseudoRole ?
		// return "roleSet_" + JspUtils.convertId2Str(permissonSetId);
		return MessageFormat.format( "id({0})", permissonSetId.getId() ); // "";
	}*/


	private SearchResult.Column createColumn( int colNum, int width)
	{
		SearchResult.Column col = new SearchResult.Column();
		// col.setNameRu( nameRU);
		// col.setNameEn( nameENU);
		// col.setSorting(SearchResult.Column.SORT_ASCENDING);
		col.setWidth( (width >= 0) ? width : defColWidthInEm);
		col.setAttributeId( colNum == IDX_DEFCOLUMNS_N ? new ObjectId( IntegerAttribute.class,  colNum + 1 ) : new ObjectId( Attribute.class, colNum + 1 ));
		return col;
	}


	private void saveBeanData(DelegateListSessionBean bean) 
		throws DataException, ServiceException 
	{
		// bean.setTextMessage("Save is not implemented yet");
		if (bean == null) return;
		// final DelegateManager manager = (DelegateManager) bean.getDataServiceBean().doAction( new SaveDelegatesAction());
		final SaveDelegatesAction action = new SaveDelegatesAction( bean.getEditDelegates());
		bean.getDataServiceBean().doAction(action);
	}

	private Card loadCard(DataServiceBean serviceBean, Person person) throws DataException, ServiceException{
		Card card = null;
		if (person != null && person.getCardId() != null) {
				List<SearchResult.Column> cols = new ArrayList();
				
				SearchResult.Column colDept = new SearchResult.Column();
				colDept.setAttributeId(personDeptAttrId);
				cols.add(colDept);
				
				SearchResult.Column colPos = new SearchResult.Column();
				colPos.setAttributeId(personPositionAttrId);
				cols.add(colPos);
				
				SearchResult.Column colSname = new SearchResult.Column();
				colSname.setAttributeId(personSnameAttrId);
				cols.add(colSname);
				
				SearchResult.Column colName = new SearchResult.Column();
				colName.setAttributeId(personNameAttrId);
				cols.add(colName);
				
				SearchResult.Column colMname = new SearchResult.Column();
				colMname.setAttributeId(personMnameAttrId);
				cols.add(colMname);
				
				Search search = new Search();
				search.setByCode(true);
				search.setColumns(cols);
				search.setWords(person.getCardId().getId().toString());
				SearchResult searchResult = (SearchResult) serviceBean.doAction(search);
				List requestCards = searchResult.getCards();
				if(requestCards != null && !requestCards.isEmpty())
					card = (Card) requestCards.get(0);			
				return card;
		}
		return card;	
	}
}
