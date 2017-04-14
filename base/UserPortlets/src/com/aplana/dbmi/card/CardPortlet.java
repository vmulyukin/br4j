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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.PortletService;
import com.aplana.dbmi.action.Action;
import com.aplana.dbmi.action.AddToFavorites;
import com.aplana.dbmi.action.BatchAsyncExecution;
import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.CloneCard;
import com.aplana.dbmi.action.CreateCard;
import com.aplana.dbmi.action.GetActiveTab;
import com.aplana.dbmi.action.GetAdminEmail;
import com.aplana.dbmi.action.GetEmptyTabs;
import com.aplana.dbmi.action.ListProject;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.Search.TextSearchConfigValue;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.action.ValidateMandatoryAttributes;
import com.aplana.dbmi.actionhandler.descriptor.ActionsDescriptor;
import com.aplana.dbmi.actionhandler.descriptor.ActionsDescriptorReader;
import com.aplana.dbmi.card.CardPortletSessionBean.PortletMessage;
import com.aplana.dbmi.card.CardPortletSessionBean.PortletMessage.PortletMessageType;
import com.aplana.dbmi.card.actionhandler.CardPortletActionsManager;
import com.aplana.dbmi.card.actionhandler.jbr.CopyFilesHandler;
import com.aplana.dbmi.card.actionhandler.multicard.SpecificCustomStoreHandler;
import com.aplana.dbmi.card.actionhandler.multicard.SpecificCustomStoreHandlerFactory;
import com.aplana.dbmi.card.delivery.SendDeliveryDispatcher;
import com.aplana.dbmi.card.dialog.EditorDialogHelper;
import com.aplana.dbmi.card.dialog.GroupExecutionSameCardEditorDialog;
import com.aplana.dbmi.card.doclinked.DoclinkCreateActionHandler;
import com.aplana.dbmi.card.doclinked.DoclinkCreateData;
import com.aplana.dbmi.component.AccessComponent;
import com.aplana.dbmi.gui.AttributeView;
import com.aplana.dbmi.gui.BlockView;
import com.aplana.dbmi.gui.BlockViewsBuilder;
import com.aplana.dbmi.gui.TabView;
import com.aplana.dbmi.gui.TabsManager;
import com.aplana.dbmi.gui.TripleContainer;
import com.aplana.dbmi.model.AsyncTicket;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.AttributeBlock;
import com.aplana.dbmi.model.AttributeViewParam;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.BlockViewParam;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.ContextProvider;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.HtmlAttribute;
import com.aplana.dbmi.model.IntegerAttribute;
import com.aplana.dbmi.model.LinkAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.MaterialAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.Tab;
import com.aplana.dbmi.model.TabBlockViewParam;
import com.aplana.dbmi.model.TabViewParam;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.TextAttribute;
import com.aplana.dbmi.model.TypedCardLinkAttribute;
import com.aplana.dbmi.model.ViewMode;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.model.filter.CardViewModeFilter;
import com.aplana.dbmi.model.filter.TemplateForCreateNewCard;
import com.aplana.dbmi.numerator.action.SetRegistrationNumber;
import com.aplana.dbmi.service.AccumulativeDataException;
import com.aplana.dbmi.service.AsyncDataServiceBean;
import com.aplana.dbmi.service.CardVersionException;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.MessageException;
import com.aplana.dbmi.service.NeedConfirmationException;
import com.aplana.dbmi.service.ObjectLockedException;
import com.aplana.dbmi.service.PortletUtil;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.dbmi.service.SystemUser;
import com.aplana.dbmi.service.UserPrincipal;
import com.aplana.dbmi.service.AsyncDataServiceBean.ExecuteOption;
import com.aplana.dbmi.showlist.MIShowListPortlet;
import com.aplana.dbmi.support.action.BatchChangeState;
import com.aplana.dbmi.util.IdUtils;
import com.aplana.dbmi.util.TemplateComparator;
import com.aplana.util.DigitalSignatureUtil;
import com.aplana.web.tag.util.StringUtils;

/**
 * The simpliest portlet based on GenericPortlet
 */
@SuppressWarnings("all")
public class CardPortlet extends GenericPortlet {
	protected static Log logger = LogFactory.getLog(CardPortlet.class);

	// JSP folder name
	public static final String JSP_FOLDER = "/WEB-INF/jsp/html/";
	private static final String CONFIG_FILE_PREFIX = "dbmi/";
	// JSP file name to be rendered
	public static final String CARD_VIEW_JSP = "CardView";
	public static final String CARD_EDIT_JSP = "CardEdit";
	public static final String TEMPLATE_VIEW_JSP = "SelectTemplate";
	public static final String SELECT_ATTACHMENTS_VIEW_JSP = "/WEB-INF/jsp/orderWithAttachmentList.jsp";
	
	public static final String PREF_CARD_DETECTOR = "cardDetector";

	// Bean name for the portlet session
	public static final String SESSION_BEAN = "CardPortletSessionBean";
	public static final String SESSION_USER = "MISessionUser";

	public static final String ADD_FAVORITES_ACTION = "MI_ADD_FAVORITES_ACTION";
	public static final String CLONE_CARD_ACTION = "MI_CLONE_CARD_ACTION";
	public static final String EDIT_CARD_ACTION = "MI_EDIT_CARD_ACTION";
	public static final String CREATE_CARD_ACTION = "MI_CREATE_CARD_ACTION";
	public static final String PRINT_ACTION = "MI_PRINT_ACTION";
	public static final String STORE_CARD_ACTION = "MI_STORE_CARD_ACTION";
	public static final String CUSTOM_STORE_AND_CHANGE_STATE_CARD_ACTION = "MI_CUSTOM_STORE_AND_CHANGE_STATE_CARD_ACTION";
	public static final String CUSTOM_STORE_AND_CHANGE_STATE_CURRENT_CARD_ACTION = "MI_CUSTOM_STORE_AND_CHANGE_STATE_CURRENT_CARD_ACTION";
	public static final String CUSTOM_STORE_CARD_ACTION = "MI_CUSTOM_STORE_CARD_ACTION";
	public static final String BACK_ACTION = "MI_BACK_ACTION";
	public static final String SIGN_CARD_ACTION = "MI_SIGN_CARD_ACTION";
	public static final String PREPARE_FOR_SIGN_CARD_ACTION = "MI_PREPARE_FOR_SIGN_CARD_ACTION";
	public static final String SELECT_ATTACHMENTS_CARD_ACTION = "MI_SIGN_ATTACHMENTS_CARD_ACTION";

	public static final String CLOSE_EDIT_MODE_ACTION = "MI_CLOSE_EDIT_MODE_ACTION";
	public static final String CLOSE_EDIT_MODE_ANYWAY_ACTION = "MI_CLOSE_EDIT_MODE_ANYWAY_ACTION";
	public static final String CLOSE_CARD_ACTION = "MI_CLOSE_CARD_ACTION";
	public static final String CUSTOM_CLOSE_CARD_ACTION = "MI_CUSTOM_CLOSE_CARD_ACTION";
	public static final String SAVE_AND_CLOSE_EDIT_MODE_ACTION = "MI_SAVE_AND_CLOSE_EDIT_MODE_ACTION";
	public static final String SAVE_AND_CLOSE_CARD_ACTION = "MI_SAVE_AND_CLOSE_CARD_ACTION";
	public static final String COPY_FILES_ACTION = "MI_COPY_FILES_ACTION";

	public static final String CHANGE_TAB_CARD_ACTION = "CHANGE_TAB_CARD_ACTION";
	public static final String OPEN_NESTED_CARD_ACTION = "MI_OPEN_NESTED_CARD";
	public static final String GET_REGISTRATION_NUMBER_ACTION = "MI_GET_REG_NUMBER";
	public static final String CREATE_DOCLINK_ACTION = "CREATE_DOCLINK"; // �������
																			// ���������
																			// ��������,
																			// ���
																			// ��
																			// ���������:
																			// ���
																			// ���������
																			// �
																			// ���
																			// �����.
	
	public static final String SORT_ACTION = "SORT_ACTION";
	public static final String SORT_HREF = "SORT_HREF";
	public static final String EXPORT_XLS_ACTION = "EXPORT_XLS_ACTION";
	public static final String DOWNLOAD_MATERIAL_ACTION = "DOWNLOAD_MATERIAL_ACTION";
	
	// ATTR_ID_FIELD : ��� backlink-��������
	public static final String PARAM_DOCLINK_TEMPLATE = "PDOCLINK_TEMPLATE"; // ������
																				// ������������
																				// ����
	public static final String PARAM_DOCLINK_TYPE = "PDOCLINK_TYPE"; // ���
																		// ������������
																		// ����
																		// (����
																		// ����-���
																		// �������
																		// ��������
																		// ��������
																		// TypedCardLink)

	// workflow action
	public static final String CHANGE_STATE_ACTION = "MI_CHANGE_STATE_ACTION";

	// mail action
	public static final String ACCESS_SEND_MAIL_ACTION = "MI_ACCESS_SEND_MAIL_ACTION";
	
	public static final String CHECK_ON_REPEATED_ACTION = "CHECK_ON_REPEATED_ACTION";

	// Mode
	public static final String CARD_VIEW_MODE = "MI_CARD_VIEW_MODE";
	public static final String CARD_EDIT_MODE = "MI_CARD_EDIT_MODE";
	public static final String TEMPLATE_VIEW_MODE = "MI_TEMPLATE_VIEW_MODE";
	public static final String SELECT_ATTACHMENTS_MODE = "MI_SELECT_ATTACHMENTS_MODE";
	public static final String FIELD_THIS_PAGE = "link_this";

	// GUI field name
	public static final String EDIT_FORM_NAME = "EditCardForm";

	public static final String VERSION_ID_FIELD = "MI_VERSION_ID_FIELD";
	public static final String ACTION_FIELD = "MI_ACTION_FIELD";
	public static final String PAGE_FIELD = "MI_PAGE_FIELD_";
	public static final String CARD_ID_FIELD = "MI_CARD_ID_FIELD";
	public static final String TEMPLATE_ID_FIELD = "MI_TEMPLATE_ID_FIELD";
	public static final String ATTR_ID_FIELD = "MI_ATTR_ID_FIELD";

	public static final String MSG_PARAM_NAME = "MI_CARD_MSG_PARAM_NAME";
	public static final String CARD_URL_FIELD = "MI_CARD_URL_FIELD";

	// use IN parameter
	public static final String EDIT_CARD_ID_FIELD = "MI_EDIT_CARD";
	public static final String OPEN_FOR_EDIT_FIELD = "MI_OPEN_FOR_EDIT";
	public static final String CREATE_CARD_ID_FIELD = "MI_CREATE_CARD";
	public static final String SPEC_ACTION_MODE_FIELD = "MI_SPEC_ACTION_MODE";
	public static final String BACK_URL_FIELD = "MI_BACK_URL_FIELD";
	public static final String INTERNAL_REQUEST_FLAG_FIELD = "MI_INTERNAL_REQUEST_FLAG_FIELD";

	public static final String CARD_MODE = "MI_CARD_MODE";
	public static final String COLLAPSE_ID_BLOCKS = "MI_COLLAPSE_ID_BLOCKS";

	public static final String NEED_CLOSE_CONFIRMATION = "MI_NEED_CLOSE_CONFIRMATION";
	public static final String NEED_CUSTOM_CLOSE_CONFIRMATION = "MI_NEED_CUSTOM_CLOSE_CONFIRMATION";
	public static final String NEED_CLOSE_CARD_CONFIRMATION = "MI_NEED_CLOSE_CARD_CONFIRMATION";
	public static final String CARD_TAB_ID = "TAB_ID";
	public static final String DIALOG_ACTION_FIELD = "MI_DIALOG_ACTION";
	public static final String DIALOG_ACTION_OK = "MI_DIALOG_ACTION_OK";
	public static final String DIALOG_ACTION_CACNCEL = "MI_DIALOG_ACTION_CANCEL";
	
	
	public static final String DIALOG_EDITOR_VALUE = "MI_DIALOG_EDITOR_VALUE";	
	public static final String DIALOG_EDITOR_ACTION_FIELD = "MI_DIALOG_EDITOR_ACTION";	
	public static final String DIALOG_EDITOR_ACTION_OK = "OK";

	public final static ObjectId TEMPLATE_REPORT = ObjectId.predefined(Template.class, "jbr.report.internal");
	public final static ObjectId TEMPLATE_RESOLUT = ObjectId.predefined(Template.class, "jbr.resolution");
	public final static ObjectId TEMPLATE_OUTCOMING = ObjectId.predefined(Template.class, "jbr.outcoming");
	public final static ObjectId TEMPLATE_REQUEST = ObjectId.predefined(Template.class, "jbr.request.change");
	public final static ObjectId STATUS_DONE = ObjectId.predefined(CardState.class, "done");
	public final static ObjectId STATUS_DRAFT = ObjectId.predefined(CardState.class, "draft");
	
	public final static ObjectId WFM_BEFORE_REG_OUTCOMING = ObjectId.predefined(WorkflowMove.class, "jbr.outcoming.before-registration.registration");
	public final static ObjectId WFM_PREP_REG_OUTCOMING = ObjectId.predefined(WorkflowMove.class, "jbr.outcoming.preparation.registration");

	public static final String SHOW_BARCODE_PRINT_DIALOG = "MI_SHOW_BARCODE_PRINT_DIALOG";
	public static final String PRINT_BLANK = "MI_PRINT_BLANK";

	private static final String ACTIONS_CONFIG_PATH = "dbmi/card/actions.xml";
	private static final String DOCLINK_CRETAE_CONFIG_PATH = "dbmi/card/doclink.xml";

	private static final String EXTERNAL_PARENT_CARD = "EXTERNAL_PARENT_CARD";
	
	public static final ObjectId ATTR_DOCLINKS = ObjectId.predefined(
			CardLinkAttribute.class, "jbr.files");

	public static final String HAS_ATTACHMENTS_TAG = "%HASATTACH%";

	public static final String DIALOG_INPUT_PARAM_NAME = "DIALOG_INPUT_PARAM_NAME";

	public static final ObjectId ATTR_NAME = ObjectId.predefined(
			StringAttribute.class, "name");

	public static final ObjectId ATTR_REGNUM = ObjectId.predefined(
			StringAttribute.class, "regnumber");

	public static final ObjectId ATTR_NUMOUT = ObjectId.predefined(
			StringAttribute.class, "jbr.incoming.outnumber");

	// textattribute.jbr.document.title.text=JBR_INFD_SHORTDESC
	public static final ObjectId ATTR_SHORTDESC = ObjectId.predefined(
			TextAttribute.class, "jbr.document.title.text");
	
	public static final ObjectId ATTR_REGDATE = ObjectId.predefined(DateAttribute.class, "regdate");
	
	protected final static ObjectId THEMES_ID = ObjectId.predefined(CardLinkAttribute.class, "jbr.ThemeOfQuery");
	protected final static ObjectId APPLICANT_SNAME_ID = ObjectId.predefined(StringAttribute.class, "jbr.InfOGSecondName");
	protected final static ObjectId APPLICANT_FNAME_ID = ObjectId.predefined(StringAttribute.class, "jbr.InfOGFirstName");
	protected final static ObjectId APPLICANT_PATR_ID = ObjectId.predefined(StringAttribute.class, "jbr.InfOGPatronimic");
	protected final static ObjectId PREVIOUS_APPEALS_ID = ObjectId.predefined(CardLinkAttribute.class, "jbr.previous.appeals");
	protected final static ObjectId TEMPLATE_CA_ID = ObjectId.predefined(Template.class, "jbr.citizenrequest");
	protected final static ObjectId CHECK_REPEAT_ATTR_ID = ObjectId.predefined(ListAttribute.class, "jbr.income.repeatChkEnable");
	protected final static ObjectId OG_REQ_AUTHOR_ATTR_ID = ObjectId.predefined(CardLinkAttribute.class, "jbr.ReqAuthor");
	protected final static ObjectId ANONYMOUS = ObjectId.predefined(Card.class, "jbr.requestAuthor.anonymous");
	protected final static ObjectId JBR_CONTROL_YES = ObjectId.predefined(ReferenceValue.class, "jbr.commission.control.yes");
	protected final static ObjectId JBR_REG_NUMBER = ObjectId.predefined(ReferenceValue.class, "regnumber");
	protected final static ObjectId JBR_VERIFY_DS = ObjectId.predefined(ListAttribute.class, "jbr.verify.ds");

	public static final ObjectId reservationRequestsAttrId = ObjectId
			.predefined(CardLinkAttribute.class, "jbr.doc.reservationRequests");
	public static final ObjectId requestPublishedStatusId = ObjectId
			.predefined(CardState.class, "jbr.reservationRequest.published");
	public static final ObjectId requestDocumentAttrId = ObjectId.predefined(
			BackLinkAttribute.class, "jbr.reservationRequest.document");

	public static final ObjectId regnumberAttrId = ObjectId.predefined(
			StringAttribute.class, "regnumber");
	public static final ObjectId manuallyNumberAttrId = ObjectId.predefined(
			IntegerAttribute.class, "jbr.manually.number");

	public static final String WARNING_KEY_PREFIX = "MI_WARNING_KEY_PREFIX";

	protected PortletService portletService;

	public static final String PARAM_LINK_TO_CARD = "MI_LINK_TO_CARD";
	
	public static final String DISABLE_DS = "DISABLE_DS";

	public static final String RELOAD_ON_REFRESH_PAGE = "reloadOnRefresh";
	
	public static final String VIEW_MODE = "viewMode";
	
	public static final String STAMP_POSITION = "stampPosition";
	
	
	
	/**
	 * @see javax.portlet.Portlet#init()
	 */
	@Override
	public void init() throws PortletException {
		super.init();
		portletService = Portal.getFactory().getPortletService();
	}

	/**
	 * Serve up the <code>view</code> mode.
	 * 
	 * @see javax.portlet.GenericPortlet#doView(javax.portlet.RenderRequest,
	 *      javax.portlet.RenderResponse)
	 */
	@Override
	public void doView(RenderRequest request, RenderResponse response)
			throws PortletException, IOException {
		// Process URL parameters
		externalRequestHandler(request, response);

		// Set the MIME type for the render response
		response.setContentType(request.getResponseContentType());
		// Set user locale
		ContextProvider.getContext().setLocale(request.getLocale());

		// Check if portlet session exists
		CardPortletSessionBean sessionBean = getSessionBean(request);
		if (sessionBean == null) {
			sessionBean = createSessionBean(request);
			/*response.getWriter().println(
					"<b>PORTLET SESSION IS NOT INITIALIZED YET</b>");
			return;*/
		}
		if(sessionBean.getMessage() == null
				|| sessionBean.getMessage().equals(""))
			sessionBean.setPortletMessage(null);
			
		sessionBean.setResourceBundle(getPortletConfig().getResourceBundle(
				request.getLocale()));

		CardPortletCardInfo cardInfo = sessionBean.getActiveCardInfo();
		if (cardInfo == null)
			cardInfo = initDefaultCard(request);

		AsyncDataServiceBean serviceBean = sessionBean.getServiceBean();
		initDoclinkCreateData(sessionBean);
		try {
			boolean reload = Boolean.valueOf(request.getParameter(RELOAD_ON_REFRESH_PAGE));
			if (cardInfo.getReloadRequired() || reload) {
				reloadCard(request, cardInfo.getMode());
			}

			if (cardInfo.getCard() != null && cardInfo.isRefreshRequired()) {
				loadCardInfo(sessionBean, serviceBean);
				ObjectId activeTabId = null;
				if (cardInfo.getTabsManager().getActiveTab() != null)
					activeTabId = cardInfo.getTabsManager().getActiveTab()
							.getId();
				initTabs(request, activeTabId, cardInfo.getCard());
			}
		} catch (Exception e) {
			throw new PortletException(e);
		}

		if (cardInfo.getPortletFormManager().processRender(request, response)) {
			return;
		}

		String jspFile = CARD_VIEW_JSP;
		if (getAccessComponent(request).isAccessHandlerAction()
				|| getPersonComponent(request).isAccessHandlerAction()) {
			jspFile = AccessComponent.SELECTED_LIST_JSP;
		} else if (CARD_EDIT_MODE.equals(cardInfo.getMode())) {
			jspFile = CARD_EDIT_JSP;
		} else if (TEMPLATE_VIEW_MODE.equals(cardInfo.getMode())) {
			jspFile = TEMPLATE_VIEW_JSP;
		}

		saveSessionBeanForServlet(request, response, sessionBean);

		// Invoke the JSP to render
		PortletRequestDispatcher rd = getPortletContext().getRequestDispatcher(
				getJspFilePath(request, jspFile));
		rd.include(request, response);
	}
	
	protected CardPortletCardInfo initDefaultCard(RenderRequest request) {
		String detectorName = request.getPreferences().getValue(PREF_CARD_DETECTOR, null);
		if (detectorName == null)
			return null;
		CardDetector detector;
		try {
			Class<? extends CardDetector> clazz = (Class<? extends CardDetector>) Class.forName(detectorName);
			detector = clazz.newInstance();
		} catch (Exception e) {
			logger.error("Error creating class for card detector named " + detectorName, e);
			return null;
		}
		CardPortletSessionBean sessionBean = getSessionBean(request);
		DataServiceBean service = sessionBean.getServiceBean();
		Card card;
		ObjectId cardId;
		try {
			cardId = detector.getCardToShow(request);
			card = (Card) service.getById(cardId);
		} catch (Exception e) {
			logger.error("Error find or get card by detector " + detector.getClass(), e);
			String message = getMessage(request, "card.edit.failture.msg") +
					e.getMessage();
			sessionBean.setMessageWithType(message, PortletMessageType.ERROR);
			return null;
		}
		sessionBean.openNestedCard(card, null, false);
		return sessionBean.getActiveCardInfo();
	}

	protected boolean externalRequestHandler(RenderRequest request,
			RenderResponse response) {
		String param = portletService.getUrlParameter(request,
				EDIT_CARD_ID_FIELD);
		boolean isInternalRequest = "true".equals(request
				.getParameter(INTERNAL_REQUEST_FLAG_FIELD));
		if (param != null) {
			CardPortletSessionBean sessionBean = createSessionBean(request);
			String backURL = portletService.getUrlParameter(request,
					BACK_URL_FIELD);
			sessionBean.setBackURL(backURL);
			
			String viewMode = (String)request.getPortletSession().getAttribute(VIEW_MODE, PortletSession.APPLICATION_SCOPE);
			if (viewMode != null && !viewMode.isEmpty()) {
				sessionBean.setViewMode(new ObjectId(ViewMode.class, viewMode));
			} else {
				sessionBean.setViewMode(null);
			}
			Card card = null;
			boolean openInEditMode = "true".equals(request
					.getParameter(OPEN_FOR_EDIT_FIELD));
			try {
				Long cardId = Long.parseLong(param);
				DataServiceBean serviceBean = sessionBean.getServiceBean();
				ObjectId cardObjectId = new ObjectId(Card.class, cardId);
				if (openInEditMode && !isInternalRequest) {
					try {
						serviceBean.doAction(new LockObject(cardObjectId));
					} catch (ObjectLockedException e1) {
						openInEditMode = false;
						String msg = getMessage(request, "card.edit.failture.msg")
						+ e1.getMessage();
						sessionBean.setMessageWithType(msg, PortletMessageType.ERROR);
						logger.error("Failed to open card width id = " + param + "in edit mode", e1);
					}
				}
				card = (Card) serviceBean.getById(cardObjectId);
			} catch (Exception e) {
				String msg = getMessage(request, "db.side.error.msg")
						+ e.getMessage();
				sessionBean.setMessageWithType(msg, PortletMessageType.ERROR);
				logger.error("Failed to open card width id = " + param, e);
				openInEditMode=false;
			}
			sessionBean.openNestedCard(card, null, openInEditMode);
			initDoclinkCreateData(sessionBean);

			sessionBean.setResourceBundle(getPortletConfig().getResourceBundle(
					request.getLocale()));

			if (openInEditMode
					&& isActualDSExists(sessionBean, request.getLocale(), card)) {
				request.setAttribute(WARNING_KEY_PREFIX, "edit.dsfound.warning");
			}

			return true;
		}

		param = portletService.getUrlParameter(request, CREATE_CARD_ID_FIELD);
		if (param != null && !isInternalRequest) {
			CardPortletSessionBean sessionBean = createSessionBean(request);
			String backURL = portletService.getUrlParameter(request,
					BACK_URL_FIELD);
			String viewMode = (String)request.getPortletSession().getAttribute(VIEW_MODE, PortletSession.APPLICATION_SCOPE);
			if (viewMode != null && !viewMode.isEmpty()) {
				sessionBean.setViewMode(new ObjectId(ViewMode.class, viewMode));
			} else {
				sessionBean.setViewMode(null);
			}
			Long parentId = null;
			try{ parentId = Long.parseLong(request.getParameter(PARAM_LINK_TO_CARD));} catch(NumberFormatException e){}
			sessionBean.setBackURL(backURL);
			Long templateId = null;
			try {
				templateId = Long.valueOf(param.trim());
			} catch (Exception ex) {
			}
			if (templateId == null) {
				CardPortletCardInfo cardInfo = createCardPortletCardInfo();
				cardInfo.setMode(TEMPLATE_VIEW_MODE);
				sessionBean.setActiveCardInfo(cardInfo);
				viewTemplateHandler(request);
				if(parentId != null) request.getPortletSession().setAttribute(EXTERNAL_PARENT_CARD, parentId);
			} else {
				CardPortletCardInfo cardInfo = createCardPortletCardInfo();
				sessionBean.setActiveCardInfo(cardInfo);
				createCardHandler(request, new ObjectId(Template.class, templateId), parentId == null ? null : new ObjectId(Card.class, parentId));
			}
			initDoclinkCreateData(sessionBean);

			param = portletService.getUrlParameter(request, SPEC_ACTION_MODE_FIELD);
			if(param != null) {
				SpecificCustomStoreHandler groupExecutionCustomStoreHandler = 
							SpecificCustomStoreHandlerFactory.getCustomStoreHandler(
									param, sessionBean, request);
				sessionBean.getActiveCardInfo().setStoreHandler(groupExecutionCustomStoreHandler);
				
			}
			return true;
		}
		return false;
	}

	private boolean isActualDSExists(CardPortletSessionBean sessionBean,
			Locale locale) {
		return isActualDSExists(sessionBean, locale,
				sessionBean.getActiveCard());
	}

	private boolean isActualDSExists(CardPortletSessionBean sessionBean,
			Locale locale, Card card) {
		DateFormat dateFormat = DateFormat.getDateTimeInstance(
				SimpleDateFormat.MEDIUM, SimpleDateFormat.MEDIUM, locale);
		List<CertificateInfo> certificateInfos = CertificateInfo
				.readCertificateInfo(sessionBean.getActiveCard(),
						sessionBean.getServiceBean(),
						sessionBean.getResourceBundle(), dateFormat);

		boolean valid = true;
		if (certificateInfos != null) {
			for (CertificateInfo cerInfo : certificateInfos) {
				if (!cerInfo.isSignValid()) {
					valid = false;
					break;
				}
			}
		}

		return (certificateInfos != null && certificateInfos.size() > 0 && valid);
	}

	protected CardPortletCardInfo createCardPortletCardInfo() {

		CardPortletCardInfo cardInfo = new CardPortletCardInfo();

		return cardInfo;
	}

	private AccessComponent getAccessComponent(PortletRequest request) {
		PortletSession session = request.getPortletSession();
		if (session == null)
			return null;
		AccessComponent component = (AccessComponent) session
				.getAttribute(AccessComponent.ACCESS_HANDLER);
		if (component == null) {
			CardPortletSessionBean sessionBean = getSessionBean(request);
			DataServiceBean serviceBean = sessionBean.getServiceBean();
			component = new AccessComponent(serviceBean, null, EDIT_FORM_NAME);
			session.setAttribute(AccessComponent.ACCESS_HANDLER, component);
		}
		return component;
	}

	private AccessComponent getPersonComponent(PortletRequest request) {
		PortletSession session = request.getPortletSession();
		if (session == null)
			return null;
		AccessComponent component = (AccessComponent) session
				.getAttribute(AccessComponent.PERSON_HANDLER);
		if (component == null) {
			CardPortletSessionBean sessionBean = getSessionBean(request);
			DataServiceBean serviceBean = sessionBean.getServiceBean();
			component = new AccessComponent(serviceBean, null, EDIT_FORM_NAME);
			session.setAttribute(AccessComponent.PERSON_HANDLER, component);
		}
		return component;
	}

	/**
	 * Process an action request.
	 * 
	 * @see javax.portlet.Portlet#processAction(javax.portlet.ActionRequest,
	 *      javax.portlet.ActionResponse)
	 */
	@Override
	public void processAction(ActionRequest request, ActionResponse response)
			throws PortletException, IOException {
		// Set user locale
		ContextProvider.getContext().setLocale(request.getLocale());
		CardPortletSessionBean sessionBean = getSessionBean(request);

		if (sessionBean == null) {
			logger.warn("Session bean not found. Redirecting to portal default page");
			redirectToPortalDefaultPage(request, response);
			return;
		}

		sessionBean.setResourceBundle(getPortletConfig().getResourceBundle(
				request.getLocale()));

		final CardPortletCardInfo cardInfo = sessionBean.getActiveCardInfo();
		if (cardInfo == null) {
			logger.warn("No cardInfo found. Action processing is not possible");
			return;
		}

		if (cardInfo.getPortletFormManager().processAction(request, response)) {
			return;
		}

		try {
			if (CARD_EDIT_MODE.equals(cardInfo.getMode())) {
				fillCardFromRequest(request);
			}

			final Card activeCard = cardInfo.getCard();
			
						//Load into card data from AttributeEditorDialog and clear bean attribute
			if(sessionBean.getGroupExecutionReportsSameCard() != null && DIALOG_EDITOR_ACTION_OK.equals(request.getParameter(DIALOG_EDITOR_ACTION_FIELD))) {
			AttributeEditorDialog dialog = sessionBean.getAttributeEditorDialog();
			if(dialog != null && request.getParameter(DIALOG_EDITOR_VALUE) != null) {
				List<ObjectId> ids =  IdUtils.stringToAttrIds((String)request.getParameter(DIALOG_EDITOR_VALUE), Card.class, true, false);
						List<Card> cardsDub = new ArrayList<Card>();
					GroupExecutionSameCardEditorDialog geed = (GroupExecutionSameCardEditorDialog) sessionBean.getAttributeEditorDialog();
						for(Card cc : geed.getCards()) {
							if(ids.contains(cc.getId())) {
								cardsDub.add(cc);
							}
						}
						sessionBean.setGroupExecutionReportsSameCard(cardsDub);
					sessionBean.setAttributeEditorDialog(null);
					}
			} else if(sessionBean.getGroupExecutionReportsSameCard() != null && !DIALOG_EDITOR_ACTION_OK.equals(request.getParameter(DIALOG_EDITOR_ACTION_FIELD))) {
				sessionBean.setGroupExecutionReportsSameCard(null);
				sessionBean.setAttributeEditorDialog(null);
			} else if(sessionBean.getAttributeEditorDialog() != null && request.getParameter(DIALOG_EDITOR_VALUE)!=null) {
				List<ObjectId> ids =  IdUtils.stringToAttrIds((String)request.getParameter(DIALOG_EDITOR_VALUE), Card.class, true, false);
				sessionBean.setDublicates(ids);
				sessionBean.setAttributeEditorDialog(null);
				}
			
			if (processAttributeAction(activeCard, request, response)) {
				return;
			}

			final String action = request.getParameter(ACTION_FIELD);
			if (PRINT_ACTION.equals(action)) {
				sessionBean.getActiveCardInfo().setPrintMode(true);
			} else if (BACK_ACTION.equals(action)) {
				backActionHandler(request, response);
			} else if (CLOSE_CARD_ACTION.equals(action)) {
				closeCardHandler(request, response, sessionBean, false, NEED_CLOSE_CARD_CONFIRMATION);
			} else if (CUSTOM_CLOSE_CARD_ACTION.equals(action)) {
				closeCardHandler(request, response, sessionBean, false, NEED_CUSTOM_CLOSE_CONFIRMATION);
			} else if (SAVE_AND_CLOSE_CARD_ACTION.equals(action)) {
				if (storeCardHandler(request))
					closeCardHandler(request, response, sessionBean);
			} else if (CLOSE_EDIT_MODE_ACTION.equals(action)) {
				boolean closeAnyway = false;
				Card card = sessionBean.getActiveCard();
				//��� ������� ��������� �� "������ �� ��������� ����������������" � ������� �������� �� ���������� ������� ��������������
				if (TEMPLATE_REQUEST.equals(card.getTemplate()) && STATUS_DRAFT.equals(card.getState())) {
					closeAnyway = true;
				}
				closeEditModeHandler(request, response, sessionBean, closeAnyway);
			} else if (CLOSE_EDIT_MODE_ANYWAY_ACTION.equals(action)) {
				closeEditModeHandler(request, response, sessionBean, true);
			} else if (SAVE_AND_CLOSE_EDIT_MODE_ACTION.equals(action)) {
				if (storeCardHandler(request))
					closeEditModeHandler(request, response, sessionBean, true);
			} else if (CLONE_CARD_ACTION.equals(action)) {
				cloneCardHandler(request);
			} else if (EDIT_CARD_ACTION.equals(action)) {
				editCardHandler(request, response);
			} else if (CREATE_CARD_ACTION.equals(action)) {
				Long parentId = (Long) request.getPortletSession().getAttribute(EXTERNAL_PARENT_CARD);
				if (CARD_VIEW_MODE.equals(cardInfo.getMode())) {
					final ObjectId templateId = activeCard.getTemplate();
					createCardHandler(request, templateId, parentId == null ? null : new ObjectId(Card.class, parentId));
				} else if (TEMPLATE_VIEW_MODE.equals(cardInfo.getMode())) {
					final String stTemplateId = request
							.getParameter(TEMPLATE_ID_FIELD);
					final ObjectId templateId = new ObjectId(Template.class,
							Long.parseLong(stTemplateId));
					if (templateId != null) {
						createCardHandler(request, templateId, parentId == null ? null : new ObjectId(Card.class, parentId));
					}
				}
			} else if (OPEN_NESTED_CARD_ACTION.equals(action)) {
				openNestedCardHandler(request);
			} else if (STORE_CARD_ACTION.equals(action)) {
				storeCardSyncHandler(request);
			} else if (PREPARE_FOR_SIGN_CARD_ACTION.equals(action)) {
				prepareForSignHandler(request);
			} else if (SIGN_CARD_ACTION.equals(action)) {
				signCardHandler(request);
			} else if (SELECT_ATTACHMENTS_CARD_ACTION.equals(action)) {
				handleSelectAttachments(request);
			} else if (CUSTOM_STORE_CARD_ACTION.equals(action)) {
				if (customStoreCardHandler(request))
					closeEditModeHandler(request, response, sessionBean, true);
			} else if (CUSTOM_STORE_AND_CHANGE_STATE_CARD_ACTION.equals(action)) {
				if (customStoreCardHandler(request)) {
					customChangeStateCardHandler(request);
					closeEditModeHandler(request, response, sessionBean, true);
				}
			} else if(CUSTOM_STORE_AND_CHANGE_STATE_CURRENT_CARD_ACTION.equals(action)) {
				if (storeCardHandler(request) && customChangeStateCardHandler(request)) {
					closeEditModeHandler(request, response, sessionBean, true);
				}
			} else if (action != null && action.startsWith(CHANGE_STATE_ACTION)) {
				// ��� ����� ������� � action ������������� id ���������, �
				// ������� ����� ��������� ��������
				ObjectId workflowMoveId = new ObjectId(WorkflowMove.class,
						Long.parseLong(action.substring(CHANGE_STATE_ACTION
								.length())));
				changeStateHandler(request, response, sessionBean,
						workflowMoveId);
			} else if (ADD_FAVORITES_ACTION.equals(action)) {
				addFavoritesHandler(request, response);
			} else if (ACCESS_SEND_MAIL_ACTION.equals(action)) {
				String cardId = "";
				if (activeCard.getId() != null) {
					cardId = activeCard.getId().getId().toString();
				}
				String subjectMail = getMessage(request,
						"view.page.mail.subject.msg") + " " + cardId;
				String bodyMail = getMessage(request,
						"view.page.mail.access.msg");
				sendAdminMailHandler(request, subjectMail, bodyMail);
			} else if (CHANGE_TAB_CARD_ACTION.equals(action)) {
				initTabs(request, cardInfo.getCard());
			} else if (CREATE_DOCLINK_ACTION.equals(action)) {
				handleCreateDocLink(request);
			} else if (COPY_FILES_ACTION.equals(action)) {
				handleCopyFiles(request);
			} else if(GET_REGISTRATION_NUMBER_ACTION.equals(action)) {
				getRegNumberHandler(request, sessionBean);
			} else if(SORT_ACTION.equals(action)) {
				//������������� ��������� ��� ���������� �� Action phase � ������������ �� � Render phase
				String url = request.getParameter(SORT_HREF);
				List<NameValuePair> parameters = URLEncodedUtils.parse(new URI(url), "UTF-8");
				for (NameValuePair p : parameters) {
				    response.setRenderParameter(p.getName(), p.getValue());
				}
			} else if (EXPORT_XLS_ACTION.equals(action)) {
				response.sendRedirect(response.encodeURL(request.getContextPath() + "/cardportlet/exporttoexcel?namespace=" + request.getParameter("namespace")));
			} else if (DOWNLOAD_MATERIAL_ACTION.equals(action)) {
				response.sendRedirect(response.encodeURL(request.getContextPath() + "/MaterialDownloadServlet?" + CardPortlet.CARD_ID_FIELD + "=" + request.getParameter(CardPortlet.CARD_ID_FIELD)));
			} else if (CHECK_ON_REPEATED_ACTION.equals(action)){
				checkOnRepeatedActionHandler(request, sessionBean);				
			} else if (cardInfo.getActionsManager() != null) {
				cardInfo.getActionsManager().processAction(request, response);
			}

		} catch (AccumulativeDataException e) {
			sessionBean.setMessageWithType(e.getMessage(), PortletMessageType.ERROR);
			logger.error("Exception caught during processing of portlet action", e);
/*		} catch (CardVersionException e) {
			sessionBean.setMessageWithType(e.getMessage()+getMessage(request, "card.refresh.message"), PortletMessageType.ERROR);
			logger.error(
					"Exception caught during processing of portlet action", e);*/
		} catch (Exception e) {
			sessionBean.setMessageWithType(e.getMessage(), PortletMessageType.ERROR);
			logger.error(
					"Exception caught during processing of portlet action", e);
		}
	}

	/**
	 * ������� ��������� �������� �� �������, � ������� ��������
	 * cardlink-�������, ������ � ��� �����.
	 * 
	 * @throws DataException
	 * @throws ServiceException
	 */
	private void handleCreateDocLink(ActionRequest request)
			throws DataException {
		final CardPortletSessionBean bean = getSessionBean(request);
		final CardPortletCardInfo cardInfo = bean.getActiveCardInfo();

		// ������� � ������ ��������������...
		// if (!cardInfo.isOpenedInEditMode()) editCardHandler(request);

		// ������� �������� ��������...
		final DoclinkCreateActionHandler handler = new DoclinkCreateActionHandler();
		handler.process(null, request, null);
	}

	private void handleSelectAttachments(ActionRequest request)
			throws DataException {
		CardPortletSessionBean sessionBean = getSessionBean(request);
		CardPortletCardInfo cardInfo = sessionBean.getActiveCardInfo();
		cardInfo.getPortletFormManager().openForm(new DSAttachmentList());
	}

	private void openNestedCardHandler(ActionRequest request) throws Exception {
		CardPortletSessionBean sessionBean = getSessionBean(request);
		CardPortletCardInfo cardInfo = sessionBean.getActiveCardInfo();
		String stCardId = request.getParameter(CARD_ID_FIELD);
		ObjectId cardId = new ObjectId(Card.class, Long.parseLong(stCardId));
		String attrCode = request.getParameter(ATTR_ID_FIELD);
		if (attrCode != null) {
			// ���� ������� ��� ��������������, �� ��� ���� �� �������������,
			// ������������ ������ ��������.
			Card card = cardInfo.getCard();
			// TODO: ����� ���-�� �������� ���������� ���� �������������� ��
			// ����
			ObjectId attrId = new ObjectId(CardLinkAttribute.class, attrCode);
			if (card.getAttributeById(attrId) == null) {
				attrId = new ObjectId(BackLinkAttribute.class, attrCode);
			}
			if (card.getAttributeById(attrId) == null) {
				attrId = new ObjectId(TypedCardLinkAttribute.class, attrCode);
			}
			if (card.getAttributeById(attrId) != null) {
				// ��������� ��������� �������� ����� �������� ��������
				// ��������� �������������, �� ����� � ���������������
				// ��������� ���������� ������
				cardInfo.setAttributeEditorData(attrId,
						AttributeEditor.KEY_VALUE_CHANGED, Boolean.TRUE);
			}
		}
		Card card = (Card) sessionBean.getServiceBean().getById(cardId);
		sessionBean.openNestedCard(card, null, cardInfo, false);
	}

	private void sendAdminMailHandler(ActionRequest request, String subject,
			String initBody) {
		CardPortletSessionBean sessionBean = getSessionBean(request);
		String cardURL = request.getParameter(CARD_URL_FIELD);
		String msg = getMessage(request, "mail.send.error.msg");
		if (cardURL != null && cardURL.trim().length() > 0) {
			try {
				PortletUtil.sendMail(sessionBean.getServiceBean(),
						sessionBean.getAdminEmail(), subject, initBody + " "
								+ cardURL.toString());
				msg = getMessage(request, "mail.send.success.msg");
				sessionBean.setMessageWithType(msg, PortletMessageType.EVENT);
			} catch (Exception e) {
				logger.error("Couldn't send email", e);
				msg = getMessage(request, "mail.send.error.msg") + " "
						+ e.getMessage();
				sessionBean.setMessageWithType(msg, PortletMessageType.ERROR);
			}
		}
	}

	/**
	 * ������� ������ "�������" � ������ �������������� ��������
	 * 
	 * @param request
	 * @param response
	 * @param sessionBean
	 * @param closeAnyway
	 *            ������� ����, ��� ����� ������� � ������� ��������� ���������
	 */
	private void closeEditModeHandler(ActionRequest request,
			ActionResponse response, CardPortletSessionBean sessionBean,
			boolean closeAnyway) throws Exception {
		final CardPortletCardInfo cardInfo = sessionBean.getActiveCardInfo();
		final ObjectId cardId = cardInfo.getCard().getId();

		final DataServiceBean serviceBean = sessionBean.getServiceBean();

		if (!closeAnyway) {
			if (hasUnsavedChanges(cardInfo.getCard(), serviceBean)) {
				response.setRenderParameter(NEED_CLOSE_CONFIRMATION,
						Boolean.TRUE.toString());
				return;  // ������ �� �������� ���������������� �������
			}
		}

		if (cardId != null) {
			try {
				sessionBean.getServiceBean().doAction(new UnlockObject(cardId));
			} catch (Exception e) {
				logger.error("Failed to unlock card " + cardId.getId(), e);
				sessionBean.setMessageWithType(getMessage(request, "db.side.error.msg")
						+ e.getMessage(),PortletMessageType.ERROR);
			}
		}
		if (!cardInfo.isOpenedInEditMode()) {
			reloadCard(request, CARD_VIEW_MODE);
		} else {
			reloadCard(request, CARD_VIEW_MODE);
			closeCard(request, response, sessionBean);
		}
	}
	
	private void closeCardHandler(ActionRequest request,
			ActionResponse response, CardPortletSessionBean sessionBean) 
					throws Exception {
		closeCardHandler(request, response, sessionBean, true, null);
	}
	
	private void closeCardHandler(ActionRequest request,
			ActionResponse response, CardPortletSessionBean sessionBean,
			boolean closeAnyway,
			String renderParameter) throws Exception {
		final CardPortletCardInfo cardInfo = sessionBean.getActiveCardInfo();
		final ObjectId cardId = cardInfo.getCard().getId();
		final DataServiceBean serviceBean = sessionBean.getServiceBean();

		if (!closeAnyway) {
			if (hasUnsavedChanges(cardInfo.getCard(), serviceBean)) {
				response.setRenderParameter(renderParameter, Boolean.TRUE.toString());
				return;  // ������ �� �������� ���������������� �������
			}
		}

		if (cardId != null) {
			try {
				sessionBean.getServiceBean().doAction(new UnlockObject(cardId));
			} catch (Exception e) {
				logger.error("Failed to unlock card " + cardId.getId(), e);
				sessionBean.setMessageWithType(getMessage(request, "db.side.error.msg")
						+ e.getMessage(),PortletMessageType.ERROR);
			}
		}
		reloadCard(request, CARD_VIEW_MODE);
		closeCard(request, response, sessionBean);
	}

	/**
	 * ���������, ���� �� � ������� ���������� �������� ������� � ���������
	 * ������������ �� ��������� � �������, ����������� � ����
	 * 
	 * @param card
	 *            �������� ��� ���������
	 * @param serviceBean
	 *            ��������� ������ ��� ������� � ����
	 * @return true - ���� ������� ����, false - � ��������� ������
	 * @throws Exception
	 */
	private boolean hasUnsavedChanges(Card card, DataServiceBean serviceBean)
			throws Exception {
		if (card.getId() == null) {
			return true;
		}
		Card storedCard = (Card) serviceBean.getById(card.getId());
		boolean found = false;
		for (Iterator<AttributeBlock> i = card.<AttributeBlock>getAttributes().iterator(); i
				.hasNext();) {
			final AttributeBlock block = i.next();
			for (Iterator<Attribute> j = block.getAttributes().iterator(); j
					.hasNext();) {
				final Attribute attr = j.next();
				if (attr instanceof MaterialAttribute) {
					continue;
				}
				try {
					Attribute storedAttr = storedCard.getAttributeById(attr
							.getId());
					if (!attr.equalValue(storedAttr)) {
						found = true;
						logger.debug("Value of attribute "
								+ attr.getId().toString() + " has been changed");
						if (!logger.isDebugEnabled()) {
							// ��� ���������� ������� ��������� ���
							// ��������������,
							// � ��������� ������ - ��������������� ��� ������
							// ��������� ���������
							return true;
						}
					}
				} catch (Exception e) {
					found = true;
					logger.error("Failed to check attribute for changes: "
							+ attr.getId().toString(), e);
				}
			}
		}
		return found;
	}

	protected void backActionHandler(ActionRequest request,
			ActionResponse response) throws IOException, DataException,
			ServiceException {
		CardPortletSessionBean sessionBean = getSessionBean(request);
		closeCard(request, response, sessionBean);
		final CardPortletCardInfo activeInfo = sessionBean.getActiveCardInfo();
		if (activeInfo == null)
			return;
		//���� ���������� �������� �� ����������, ������������ � ������ ��������
		if(!isCurrentCardExists(request)){
			leaveCardPortlet(request,response,sessionBean);
			return;
		}
		if (CARD_VIEW_MODE.equals(sessionBean.getActiveCardInfo().getMode()))
			reloadCard(request, CARD_VIEW_MODE);
	}

	private void addFavoritesHandler(ActionRequest request,
			ActionResponse response) {
		final CardPortletSessionBean sessionBean = getSessionBean(request);
		String msg;
		try {
			AddToFavorites addFavoritesAction = new AddToFavorites();
			addFavoritesAction.setCard(sessionBean.getActiveCard().getId());
			sessionBean.getServiceBean().doAction(addFavoritesAction);
			msg = getMessage(request, "add.fovorites.success.msg");
			sessionBean.setMessageWithType(msg , PortletMessageType.EVENT);
		} catch (Exception e) {
			logger.error("Couldn't add card to favorites", e);
			msg = getMessage(request, "db.side.error.msg") + e.getMessage();
			sessionBean.setMessageWithType(msg , PortletMessageType.ERROR);
		}
	}

	/**
	 * Overwrites the actual content of the edited card in order to not loose
	 * the updated modified field when is necessary a call to an external form,
	 * before the SAVE command is invoked.
	 * 
	 * @throws DataException
	 * */
	protected void fillCardFromRequest(ActionRequest request)
			throws AccumulativeDataException {
		
		CardPortletSessionBean sessionBean = getSessionBean(request);
		CardPortletCardInfo cardInfo = sessionBean.getActiveCardInfo();
		AccumulativeDataException accumulativeExc = null;
		for (Iterator<AttributeView> i = cardInfo.getTabsManager()
				.getActiveAttributeViews().iterator(); i.hasNext();) {
			final AttributeView av = i.next();
			final AttributeEditor editor = av.getEditor();
			if (editor == null)
				continue;
			try {
				editor.gatherData(request, av.getAttribute());
			} catch (DataException e) {
				logger.error(e);
				if (accumulativeExc == null) {
					accumulativeExc = new AccumulativeDataException();
				}
				accumulativeExc.addException(e);
			}
		}
		
		if (accumulativeExc != null && accumulativeExc.getExceptionsQuantity() > 0) {
			throw accumulativeExc;
		}
	}

	protected boolean storeCardHandler(ActionRequest request) {
		boolean actionResult = true;
		CardPortletSessionBean sessionBean = getSessionBean(request);
		Card card = sessionBean.getActiveCard();
		boolean locked = false;
		try {
			if (card.getId() == null || sessionBean.getActiveCardInfo().getMode().equals(CardPortlet.CARD_EDIT_MODE)) {
				locked = false;
			} else {
				sessionBean.getServiceBean().doAction(new LockObject(card));
				locked = true;
			}
		} catch (Exception e) {
			sessionBean.setMessageWithType(e.getMessage(), PortletMessageType.ERROR);
			logger.error("Can't lock card " + card.getId() + "\n" + e);
			return false;
		}
		String msg = getMessage(request, "card.store.success.msg");
		try {
			final ObjectId id = sessionBean.getServiceBean().saveObject(card);
			if (id.getType().equals(AsyncTicket.class)) {	// ���������� ���������� ������� ? (��������� id AsyncTicket)
				msg = getMessage(request, "card.async.store.success.msg");
			} else { //�� ���������� ���������� ������� (����������) (��������� card id)
				card.setId(Long.parseLong("" + id.getId()));
				reloadCard(request, CARD_EDIT_MODE);
			}
			sessionBean.setMessageWithType(msg, PortletMessageType.EVENT);
		} catch (Exception e) {
			actionResult = false;
			msg = getMessage(request, "db.side.error.msg") + e.getMessage();
			sessionBean.setMessageWithType(msg, PortletMessageType.ERROR);
			logger.error(msg + card.getId() + "\n", e);
		} finally {
			if (locked) {
				try {
					sessionBean.getServiceBean().doAction(new UnlockObject(card));
				} catch (Exception e) {
					logger.error("Can't unlock card " + card.getId() + "\n" , e);
				}	
			}
		}
		return actionResult;
	}
	
	protected boolean storeCardSyncHandler(ActionRequest request) {
		boolean actionResult = true;
		CardPortletSessionBean sessionBean = getSessionBean(request);
		Card card = sessionBean.getActiveCard();
		boolean locked = false;
		try {
			if (card.getId() == null || sessionBean.getActiveCardInfo().getMode().equals(CardPortlet.CARD_EDIT_MODE)) {
				locked = false;
			} else {
				sessionBean.getServiceBean().doAction(new LockObject(card));
				locked = true;
			}
		} catch (Exception e) {
			sessionBean.setMessageWithType(e.getMessage(), PortletMessageType.ERROR);
			logger.error("Can't lock card " + card.getId() + "\n" + e);
			return false;
		}
		String msg = getMessage(request, "card.store.success.msg");
		try {
			final ObjectId id = sessionBean.getServiceBean().saveObject(card, ExecuteOption.SYNC);
			card.setId(Long.parseLong("" + id.getId()));
			reloadCard(request, CARD_EDIT_MODE);
			sessionBean.setMessageWithType(msg, PortletMessageType.EVENT);
		} catch (Exception e) {
			actionResult = false;
			msg = getMessage(request, "db.side.error.msg") + e.getMessage();
			sessionBean.setMessageWithType(msg, PortletMessageType.ERROR);
			logger.error(msg + card.getId() + "\n" + e);
		} finally {
			if (locked) {
				try {
					sessionBean.getServiceBean().doAction(new UnlockObject(card));
				} catch (Exception e) {
					logger.error("Can't unlock card " + card.getId() + "\n" + e);
				}	
			}
		}
		return actionResult;
	}

	private boolean customStoreCardHandler(ActionRequest request) {
		CardPortletSessionBean sessionBean = getSessionBean(request);
		CardPortletCardInfo cardInfo = sessionBean.getActiveCardInfo();
		try {
			// TODO ��� ����� ��������� ����� �����������
			cardInfo.getStoreHandler().storeCard();
			return true;
		} catch (DataException e) {
			String msg = getMessage(request, "db.side.error.msg")
					+ e.getMessage();
			sessionBean.setMessageWithType(msg, PortletMessageType.ERROR);
			return false;
		}
	}

	private boolean customChangeStateCardHandler(ActionRequest request) {
		CardPortletSessionBean sessionBean = getSessionBean(request);
		CardPortletCardInfo cardInfo = sessionBean.getActiveCardInfo();
		try {
			cardInfo.getChangeStateHandler().changeState();
			return true;
		} catch (DataException e) {
			String msg = getMessage(request, "db.side.error.msg")
					+ e.getMessage();
			sessionBean.setMessageWithType(msg, PortletMessageType.ERROR);
			return false;
		} catch (ServiceException e) {
			String msg = getMessage(request, "db.side.error.msg")
					+ e.getMessage();
			sessionBean.setMessageWithType(msg, PortletMessageType.ERROR);
			return false;
		}
	}

	private void prepareForSignHandler(ActionRequest request)
			throws DataException, ServiceException {
		CardPortletSessionBean sessionBean = getSessionBean(request);
		Card card = sessionBean.getActiveCard();
		String msg = getMessage(request, "card.store.success.msg");
		boolean isLocked = false;

		try {
			if (CARD_VIEW_MODE
					.equals(sessionBean.getActiveCardInfo().getMode())) {
				sessionBean.getServiceBean().doAction(
						new LockObject(card.getId()));
				isLocked = true;
			}

			ObjectId id = sessionBean.getServiceBean().saveObject(card);
			card.setId(Long.parseLong("" + id.getId()));

			ValidateMandatoryAttributes validationAction = new ValidateMandatoryAttributes();
			validationAction.setCard(card);
			sessionBean.getServiceBean().doAction(validationAction);

			if (isLocked) {
				sessionBean.getServiceBean().doAction(
						new UnlockObject(card.getId()));
			}

			reloadCard(request, sessionBean.getActiveCardInfo().getMode());
			final String ds_enable = request.getParameter(DISABLE_DS);
			Boolean ds =  Boolean.TRUE;
			if(ds_enable.equals(DISABLE_DS)){
				ds = Boolean.FALSE;
			}
			request.getPortletSession().setAttribute("MI_APPLY_SIGNATURE",
					ds);
			sessionBean.setMessageWithType(msg, PortletMessageType.EVENT);
		} catch (Exception e) {
			if (isLocked) {
				sessionBean.getServiceBean().doAction(
						new UnlockObject(card.getId()));
			}
			msg = getMessage(request, "db.side.error.msg") + e.getMessage();
			sessionBean.setMessageWithType(msg, PortletMessageType.ERROR);
		}
	}

	private void signCardHandler(ActionRequest request) throws DataException,
			ServiceException {
		CardPortletSessionBean sessionBean = getSessionBean(request);
		Card card = sessionBean.getActiveCard();
		String msg = getMessage(request, "ds.card.success.msg");
		boolean isLocked = false;

		try {
			DataServiceBean dataServiceBean = sessionBean.getServiceBean();
			card = (Card) dataServiceBean.getById(sessionBean.getActiveCard().getId());
			HtmlAttribute signatureAttribute = (HtmlAttribute) card.getAttributeById(DigitalSignatureUtil.ATTR_SIGNATURE);
//			String signatureParam = request.getParameter(JspAttributeEditor.getAttrHtmlId(signatureAttribute));
//			TODO: replace with JspAttributeEditor.getAttrHtmlId(...);
			String signatureParam = request.getParameter(CardPortlet.getAttributeFieldName(signatureAttribute));
			if(StringUtils.hasLength(signatureParam)) {
				boolean signBaseCard = CARD_VIEW_MODE.equals(sessionBean.getActiveCardInfo().getMode());
				DigitalSignatureUtil.storeDigitalSignature(signatureParam, card, dataServiceBean, signBaseCard);
			}

			reloadCard(request, sessionBean.getActiveCardInfo().getMode());
			sessionBean.setMessageWithType(msg, PortletMessageType.EVENT);
		} catch (Exception e) {
			if (isLocked) {
				// sessionBean.getServiceBean().doAction(new
				// UnlockObject(card.getId()));
			}
			msg = getMessage(request, "db.side.error.msg") + e.getMessage();
			logger.error(e);
			sessionBean.setMessageWithType(msg, PortletMessageType.ERROR);
		}
	}

	private void viewTemplateHandler(PortletRequest request) {
		CardPortletSessionBean sessionBean = getSessionBean(request);
		final List<Template> result = new ArrayList<Template>();
		// DB
		try {
			final DataServiceBean serviceBean = sessionBean.getServiceBean();
			final List<Template> templateList = (List<Template>) serviceBean
					.filter(Template.class, new TemplateForCreateNewCard());
			Collections.sort(templateList, new TemplateComparator());
			final CreateCard createCardAction = new CreateCard();
			for (Template template : templateList) {
				createCardAction.setTemplate(template.getId());
				if (serviceBean.canDo(createCardAction)) {
					result.add(template);
				}
			}
		} catch (Exception e) {
			logger.error(e);
			String msg = getMessage(request, "db.side.error.msg")
					+ e.getMessage();
			sessionBean.setMessageWithType(msg, PortletMessageType.ERROR);
		}
		sessionBean.setTemplateList(result);
	}

	public static Card createCard(CardPortletSessionBean sessionBean,
			ObjectId templateId, ObjectId parentId) throws DataException, ServiceException {
		final CreateCard createCardAction = new CreateCard();
		createCardAction.setTemplate(templateId);
		final DataServiceBean serviceBean = sessionBean.getServiceBean();
		Card parent = null;
		if(parentId != null) parent = (Card) serviceBean.getById(parentId);
		createCardAction.setParent(parent);
		final Card card = (Card) serviceBean.doAction(createCardAction);
		final CardPortletCardInfo cardInfo = sessionBean.getActiveCardInfo();
		if (cardInfo.getCard() == null) {
			cardInfo.setCard(card);
			cardInfo.setMode(CARD_EDIT_MODE);
			cardInfo.setOpenedInEditMode(true);
			cardInfo.setRefreshRequired(true);
		} else {
			sessionBean.openNestedCard(card, null, true);
		}
		return card;
	}
	
	public static Card createCard(CardPortletSessionBean sessionBean,
			ObjectId templateId) throws DataException, ServiceException {
		return createCard(sessionBean, templateId, null);
	}

	protected Card createCardHandler(PortletRequest request, ObjectId templateId, ObjectId parentId) {
		final CardPortletSessionBean sessionBean = getSessionBean(request);
		

		try {
			return createCard(sessionBean, templateId, parentId);

		} catch (Exception e) {
			logger.error("Exception caught", e);
			String msg = getMessage(request, "db.side.error.msg")
					+ e.getMessage();
			sessionBean.setMessageWithType(msg, PortletMessageType.ERROR);
			return null;
		}
	}

	private WorkflowMove getWorkflowMove(ObjectId workflowMoveId,
			CardPortletSessionBean sessionBean) {
		if (workflowMoveId != null) {
			final List<WorkflowMove> list = sessionBean.getActiveCardInfo()
					.getAvailableWorkflowMoves();
			if (list != null) {
				for (WorkflowMove wfm : list) {
					if (workflowMoveId.equals(wfm.getId()))
						return wfm;
				}
			}
		}
		return null;
	}

	private boolean unlockQuietly(Card c, DataServiceBean serviceBean) {
		try {
			serviceBean.doAction(new UnlockObject(c));
			return true;
		} catch (Exception e) {
			final String msg = "(!) Failed to unlock card: "
					+ (c == null ? "card is null" : c.getId()) + "\n"
					+ e.getMessage();
			logger.error(msg);
			// logger.warn(msg, e);
			return false;
		}
	}

	private boolean isReservationRequestPublished(
			CardPortletSessionBean sessionBean, Card documentCard)
			throws DataException, ServiceException {
		boolean result = false;
		CardLinkAttribute requests = (CardLinkAttribute) documentCard
				.getAttributeById(reservationRequestsAttrId);
		if (requests != null) {
			Search search = new Search();
			search.setByCode(true);
			search.setWords(requests.getLinkedIds());
			SearchResult searchResult = (SearchResult) sessionBean
					.getServiceBean().doAction(search);
			List requestCards = searchResult.getCards();
			for (int i = 0; i < requestCards.size(); i++) {
				if (requestPublishedStatusId
						.equals(((Card) requestCards.get(i)).getState())) {
					result = true;
				}
			}
		}
		return result;
	}

	private void changeStateHandler(ActionRequest request,
			ActionResponse response, CardPortletSessionBean sessionBean,
			ObjectId workflowMoveId) {
		WorkflowMove wfm = getWorkflowMove(workflowMoveId, sessionBean);
		if (wfm == null) {
			return;
		}
		final Card curCard = sessionBean.getActiveCard();
		final String ds_enable = request.getParameter(DISABLE_DS);
		if(ds_enable.equals(DISABLE_DS)){
			sessionBean.setDisableDS(true);
		}
		// ����� ��������� �������
		String inputRegNumber = null;
		
		final String stampPosition = request.getParameter(STAMP_POSITION);
		if(stampPosition != null && !stampPosition.isEmpty()){
			sessionBean.setStampPosition(stampPosition);		
		}
		
		// (Smirnov A. : 11.7.12  ��������� �������� ��������������, ���� ���� -
		//						  �� ������ ������ � ������� ������ ������������.)
		boolean haveReservationRequests = false;
		if (wfm.getToState().equals(ObjectId.predefined(CardState.class, "trash"))){
			
			boolean dialogOk = DIALOG_ACTION_OK.equals(request.getParameter(DIALOG_ACTION_FIELD));
			if(!dialogOk){
				try {
					Attribute attribute = curCard.getAttributeById(ObjectId.predefined(CardLinkAttribute.class, "jbr.doc.reservationRequests"));
					if (attribute != null){
						Search search = new Search();
						search.setByCode(true);
						search.setWords(attribute.getStringValue());
						SearchResult searchResult = (SearchResult) sessionBean.getServiceBean().doAction(search);
						
						for  (Object resReq :  searchResult.getCards()){
							Card resReqCardO = (Card) resReq;
							Card resReqCard = (Card) sessionBean.getServiceBean().getById(resReqCardO.getId());
							if (resReqCard.getState().equals(ObjectId.predefined(CardState.class, "jbr.reservationRequest.published"))){
								haveReservationRequests = true;
							}
						}
						
						if (haveReservationRequests){
							// ���������
							CardPortletDialog dialog = new CardPortletDialog();
							dialog.setTitle(getMessage(request,"edit.close.confirmation.title"));
							dialog.setMessage(getMessage(request,"reservationRequests.dialog.message"));
							dialog.setCardPortletAction(request.getParameter(ACTION_FIELD));
							sessionBean.setDialog(dialog);
						}
					}
				} catch (DataException e) {
					sessionBean.setMessageWithType(e.getMessage(), PortletMessageType.ERROR);
					return;
				} catch (ServiceException e) {
					sessionBean.setMessageWithType(e.getMessage(), PortletMessageType.ERROR);
					return;
				}
			}
		}

		if(wfm.getToState().equals(STATUS_DONE) &&
				curCard.getTemplate().equals(TEMPLATE_REPORT)) {
			if(!DIALOG_EDITOR_ACTION_OK.equals(request.getParameter(DIALOG_EDITOR_ACTION_FIELD))
					&& sessionBean.getGroupExecutionReportsSameCard() == null){
				try {
					GroupExecutionSameCardEditorDialog dialog = new GroupExecutionSameCardEditorDialog();
					dialog.setTitle(getMessage(request, "dialog.editor.resolutions.confirm"));
					dialog.setActionCode(request.getParameter(ACTION_FIELD));
					dialog.setActiveCard(curCard);
					dialog.setDataServiceBean(sessionBean.getServiceBean());
					dialog.setCache(true);
					if(dialog.isData()){
						sessionBean.setAttributeEditorDialog(dialog);
						sessionBean.setGroupExecutionReportsSameCard(new ArrayList<Card>());
						return;
					}
				} catch(DataException e) {
					throw new RuntimeException(e);
				} catch(ServiceException e) {
					throw new RuntimeException(e);
				}
			}
		}

		// ����� �������� ����� �������. ��������������� �������� ������
		// ����������� ��� ���� �������� ��������. CQ BR4J00001144
		// ��� �������� �� ������, ������� �������� �� �����������
		if (wfm.getToState().equals(ObjectId.predefined(CardState.class, "registration"))  
				&& !wfm.getFromState().equals(ObjectId.predefined(CardState.class, "delo"))) {
			
			/*
			 * If the document to register is citizen's appeal, give him dialog to confirm
			 * previous appeals.
			 */
			if(curCard.getTemplate().equals(TEMPLATE_CA_ID)){
				if(!(initRegNumberDialog(request, sessionBean, curCard)))
					return;
			}
			inputRegNumber = request.getParameter(DIALOG_INPUT_PARAM_NAME);
			if(fillRegNumberDialog(request, sessionBean, curCard,true))
				return;

		}

		if (wfm.getToState().equals(
				ObjectId.predefined(CardState.class,
						"jbr.reservationRequest.registered"))) {
			boolean dialogOk = DIALOG_ACTION_OK.equals(request
					.getParameter(DIALOG_ACTION_FIELD));
			if (sessionBean.getDialog() == null && !dialogOk) {

				try {
					Card documentCard = null;
					BackLinkAttribute documentLink = (BackLinkAttribute) curCard
							.getAttributeById(requestDocumentAttrId);
					ListProject listAction = new ListProject();
					listAction.setCard(curCard.getId());
					listAction.setAttribute(documentLink.getId());
					Object listResult = sessionBean.getServiceBean().doAction(
							listAction);
					if (listResult instanceof SearchResult) {
						SearchResult searchResult = (SearchResult) listResult;
						if (searchResult.getCards().size() > 0) {
							documentCard = (Card) sessionBean.getServiceBean()
									.getById(
											((Card) searchResult.getCards()
													.get(0)).getId());
						}
					}

					SetRegistrationNumber dgrn = new SetRegistrationNumber();
					dgrn.setCard(documentCard);
					dgrn.setPreliminary(true);
					dgrn.setCheckMandatory(true);
					String num = (String) sessionBean.getServiceBean().doAction(dgrn);
					CardPortletDialog dialog = new CardPortletDialog();
					dialog.setTitle(getMessage(request,"card.register.dialog.title"));
					dialog.setMessage(MessageFormat.format(getMessage(request,"card.register.dialog.message"), num));
					dialog.setCardPortletAction(request.getParameter(ACTION_FIELD));
					sessionBean.setDialog(dialog);
				} catch (DataException e) {
					sessionBean.setMessageWithType(e.getMessage(), PortletMessageType.ERROR);
					return;
				} catch (ServiceException e) {
					sessionBean.setMessageWithType(e.getMessage(), PortletMessageType.ERROR);
					return;
				}
				return;
			} else
				sessionBean.setDialog(null);
		}

		boolean unlockOnError=false;
		boolean isLocked=false;
		DataServiceBean serviceBean = sessionBean.getServiceBean();
		Card card = sessionBean.getActiveCard();
				
		// ��������� � �������� ������ ���������� �������
		if (inputRegNumber != null && inputRegNumber.length() > 0) {
			IntegerAttribute manNumberAttr = (IntegerAttribute) card.getAttributeById(manuallyNumberAttrId);
			try {
				manNumberAttr.setValue(Integer.parseInt(inputRegNumber));	
			} catch (NumberFormatException e) {
				logger.error("Manually number is incorrect.", e);
				sessionBean.setMessageWithType("������� ����� ������ �����������.",PortletMessageType.ERROR);
				return;
			} catch (Exception e) {
				sessionBean.setMessageWithType(getMessage(request, "db.side.error.msg") + e.getMessage(), PortletMessageType.ERROR);
				return;
			}
		}
		
		// 1. ���� ����� ��������������, �� �� ��������� ��������, �������� ��� ��� ������������� 
		if (CardPortlet.CARD_EDIT_MODE.equals(sessionBean.getActiveCardInfo().getMode())) {
			isLocked = true;
			unlockOnError = false;
		} else {
			// 2. ���� ���, �� ������� ��������� 
			try {
				serviceBean.doAction(new LockObject(sessionBean.getActiveCard()));
				isLocked = true;
				unlockOnError = true;
			} catch (Exception e) {
				sessionBean.setMessageWithType(e.getMessage(), PortletMessageType.ERROR);
				return;
			}
		}
		
		// ������ ��� ����� ������� � c���������� ������� � �������� �������� ����� ����������� � ����� ������ (� ����� ����������)
		final BatchChangeState bChangeStateAction = new BatchChangeState();
		bChangeStateAction.setEditMode(CardPortlet.CARD_EDIT_MODE.equals(sessionBean.getActiveCardInfo().getMode()));
		bChangeStateAction.setCard(sessionBean.getActiveCard());
		bChangeStateAction.addMessage("card.store.success.msg", getMessage(request, "card.store.success.msg"));
		bChangeStateAction.addMessage("db.side.error.msg", getMessage(request, "db.side.error.msg"));
		bChangeStateAction.addMessage("edit.page.async.change.state.success.msg", getMessage(request, "edit.page.async.change.state.success.msg"));
		bChangeStateAction.addMessage("edit.page.change.state.success.msg", getMessage(request, "edit.page.change.state.success.msg"));
		bChangeStateAction.addOpenActiveCards(getOpenedActiveCardsList(sessionBean));
		bChangeStateAction.setWorkflowMove(wfm);
		bChangeStateAction.setLastDialogOk(DIALOG_ACTION_OK.equals(request.getParameter(DIALOG_ACTION_FIELD)));
		bChangeStateAction.setDublicates(sessionBean.getDublicates());
		bChangeStateAction.setHaveReservationRequests(haveReservationRequests);
		bChangeStateAction.putParameter("stampPosition", sessionBean.getStampPosition());

		
		boolean isRassm = false;
		if(sessionBean.getActiveCard().getTemplate().equals(ObjectId.predefined(Template.class, "jbr.rassm")))
			isRassm = true;
		if(isRassm) {
			//��� �������� ��� �������� �������� ������������ �� ����������
			bChangeStateAction.setAttrToParent(ObjectId.predefined(BackLinkAttribute.class, "jbr.exam.parent"));

			//��� �������� ��� �������� �������� ��������� �� ����������
		} else {
			bChangeStateAction.setAttrToParent(ObjectId.predefined(CardLinkAttribute.class, "jbr.main.doc"));
		}
		String msg = getMessage(request, "edit.page.change.state.success.msg");
		PortletMessageType msgType = PortletMessageType.EVENT;
		
		try {
			Object result = serviceBean.doAction(bChangeStateAction);
			if (result instanceof AsyncTicket) {
				msg = getMessage(request, "edit.page.async.change.state.success.msg");
			}
			
			for (CardPortletCardInfo cardInfo : sessionBean.getOpenedActiveCards()) {
				final ObjectId id = cardInfo.getCard().getId();
				try {
					cardInfo.setCard((Card) sessionBean.getServiceBean().getById(id));
					serviceBean.doAction(new UnlockObject(cardInfo.getCard()));
					cardInfo.setRefreshRequired(true);
					cardInfo.setMode(CARD_VIEW_MODE);
					cardInfo.setReloadRequired(false);
				} catch (Exception e) {
					logger.error(e);
				}
			}
			
			sessionBean.setMessageWithType(msg, msgType);

			//clear this field because current's card status is already changed and dublicates are no longer needed. 
            sessionBean.clearDublicates();

			// ��� �������� � ������ ���������������� ��������� �� �������������
			// ������ �����-����/������ � ���. ������� � �����
			boolean isNotCloseCard = false;
			if (  (card.getTemplate().equals(ObjectId.predefined(Template.class,"jbr.incoming"))
				|| card.getTemplate().equals(ObjectId.predefined(Template.class,"jbr.interndoc"))
				|| card.getTemplate().equals(ObjectId.predefined(Template.class,"jbr.incomingpeople"))
				|| card.getTemplate().equals(ObjectId.predefined(Template.class,"jbr.citizenrequest")) 
				|| card.getTemplate().equals(ObjectId.predefined(Template.class,"jbr.informationrequest"))
				)
				&& wfm.getToState().equals(ObjectId.predefined(CardState.class,"registration"))) {

				response.setRenderParameter(SHOW_BARCODE_PRINT_DIALOG,Boolean.TRUE.toString());
				isNotCloseCard = true;
			}
			else if (card.getTemplate().equals(ObjectId.predefined(Template.class,"jbr.outcoming"))
				&& wfm.getToState().equals(ObjectId.predefined(CardState.class,"registration"))) 
			{
				isNotCloseCard = true;
			}

			unlockQuietly(sessionBean.getActiveCard(), serviceBean);

			/* old code		
				// 1. ���� ����� ��������������, �� ��������� �������� 
				if (CardPortlet.CARD_EDIT_MODE.equals(sessionBean.getActiveCardInfo().getMode())) {
					if (!storeCardSyncHandler(request)) { //����� ���������� ���������� ���������� ��� ��� ����� ����� ChangeState
						return;
					}
					card = sessionBean.getActiveCard();
		
					isLocked = true;
					unlockOnError = false;
				} else {
					// 2. ���� ���, �� ������� ��������� 
					isLocked = false;
					unlockOnError = true;
					try {
						// ���� ������������ ��������� ������� �������� �� ������
						// ���������, ��
						// ����� ������� ������������� ��������.
						serviceBean.doAction(new LockObject(sessionBean.getActiveCard()));
						isLocked = true;
						// ��������� ������ �� ��� ������������, �� ����� ��������
						// ��������� �������� �� ��
						card = (Card) sessionBean.getServiceBean().getById(sessionBean.getActiveCard().getId());
					} catch (Exception e) {
						if (isLocked) {
							unlockQuietly(sessionBean.getActiveCard(), serviceBean);
						}
						sessionBean.setMessageWithType(e.getMessage(), PortletMessageType.ERROR);
						return;
					}
				}
				
				//��������� ��� �������� ������������� �� �������������� �������� � ���������
				//�� � ����� ���������, ����� ����� ���������� ������� �������� ��������� ���
				//��������� ���������� ������ � ��������� ��������� � ��������� ��������������
				//���� �������� �� ����� ���������� �������� �� ����� ������� (BR4J00026125)
				for (CardPortletCardInfo cardInfo : sessionBean.getOpenedActiveCards()) {
					try {
						final ObjectId id = sessionBean.getServiceBean().saveObject(cardInfo.getCard(), ExecuteOption.SYNC);
						cardInfo.getCard().setId(Long.parseLong("" + id.getId()));
						cardInfo.setCard((Card) sessionBean.getServiceBean().getById(id));
						cardInfo.setRefreshRequired(true);
						cardInfo.setMode(CARD_VIEW_MODE);
						cardInfo.setReloadRequired(false);
						try {
							sessionBean.getServiceBean().doAction(new UnlockObject(id));
						} catch (Exception e) {
							logger.error("Failed to unlock card " + id.getId(), e);
							sessionBean.setMessageWithType(getMessage(request, "db.side.error.msg")
									+ e.getMessage(),PortletMessageType.ERROR);
							throw e;
						}
					} catch (Exception e) {
						sessionBean.setMessageWithType(e.getMessage(), PortletMessageType.ERROR);
						return;
					}
				}
				
				if(sessionBean.getDublicates()!=null){
					try{	
						CardLinkAttribute prevAppeals = (CardLinkAttribute) card.getAttributeById(PREV_APPEALS_ID);
						prevAppeals.setIdsLinked(sessionBean.getDublicates());
						//curCard.getAttributes().add(prevAppeals);
						final OverwriteCardAttributes writer = new OverwriteCardAttributes();
						writer.setCardId(curCard.getId());
						writer.setAttributes(Collections.singletonList(prevAppeals));
						sessionBean.getServiceBean().doAction(writer, ExecuteOption.SYNC);
					} catch (DataException e) {
						sessionBean.setMessageWithType(e.getMessage(), PortletMessageType.ERROR);
						return;
					} catch (ServiceException e) {
						sessionBean.setMessageWithType(e.getMessage(), PortletMessageType.ERROR);
						return;
					}
				}
					
				String msg = getMessage(request, "edit.page.change.state.success.msg");
				PortletMessageType msgType = PortletMessageType.EVENT;
				try {
					card = (Card) sessionBean.getServiceBean().getById(card.getId());
					
					final ChangeState changeStateAction = new ChangeState();
					changeStateAction.setCard(card);
					changeStateAction.setWorkflowMove(wfm);
					changeStateAction.setLastDialogOk(DIALOG_ACTION_OK.equals(request.getParameter(DIALOG_ACTION_FIELD)));
		
					// ��������� �������
					if (!haveReservationRequests){
						Object result = sessionBean.getServiceBean().doAction(changeStateAction);
						if (result instanceof AsyncTicket) {
							msg = getMessage(request, "edit.page.async.change.state.success.msg");
							sessionBean.setMessageWithType(msg, msgType);
						}
						isLocked = !unlockQuietly(card, serviceBean);
					}  
					
				//if (card.getTemplate().equals(
				//	ObjectId.predefined(Template.class, "jbr.outcoming"))
				//	&& wfm.getToState()
				//			.equals(ObjectId.predefined(CardState.class,
				//					"registration"))) {

				//response.setRenderParameter(PRINT_BLANK,
				//		Boolean.TRUE.toString());
				//isNotCloseCard = true;
				//}
			*/

			if (!isNotCloseCard && wfm.isCloseCard()){
				if (haveReservationRequests){
					reloadCard(request, CARD_VIEW_MODE);
				}else{
					closeCard(request, response, sessionBean);
				}
			}
			else
				reloadCard(request, CARD_VIEW_MODE);
		} catch (NeedConfirmationException confE) {
			boolean dialogOk = DIALOG_ACTION_OK.equals(request
					.getParameter(DIALOG_ACTION_FIELD));
			if (sessionBean.getDialog() == null && !dialogOk) {
				CardPortletDialog dialog = new CardPortletDialog();
				dialog.setTitle(MessageFormat.format(
						getMessage(request, confE.getConfirmationTitleId()),
						confE.getConfirmationParams()));
				dialog.setMessage(MessageFormat.format(
						getMessage(request, confE.getConfirmationMessageId()),
						confE.getConfirmationParams()));
				dialog.setCardPortletAction(request.getParameter(ACTION_FIELD));
				sessionBean.setDialog(dialog);
				return;
			} else {
				sessionBean.setDialog(null);
			}
		} catch (MessageException mex) { // or MessageException
			final List<ObjectId> cardIds = mex.getContainer();
			final List<Card> cards = new ArrayList<Card>();
			try {
				final DataServiceBean service = new DataServiceBean();
				service.setAddress("localhost");
				service.setUser(new SystemUser());
				for (ObjectId cId : cardIds)
					cards.add((Card) (service.getById(cId)));
			} catch (DataException e) {
				logger.error(e);
			} catch (ServiceException e) {
				logger.error(e);
			}
			final Map<ObjectId, String> dubletsContainer = new HashMap<ObjectId, String>();
			for (Card c : cards) {
				final StringBuilder dubletEntry = new StringBuilder();
				boolean hasAttachments = !((CardLinkAttribute) c
						.getAttributeById(ATTR_DOCLINKS)).isEmpty();
				//Card theme = (Card) serviceBean.getById(((CardLinkAttribute)c.getAttributeById(THEMES_ID)));
				String s = "";
				CardLinkAttribute cla = (CardLinkAttribute) ((Card)cards.get(0)).getAttributeById(THEMES_ID);
				ObjectId nameId = ObjectId.predefined(StringAttribute.class, "name");
				if(cla != null) {
					try{
						Collection<ObjectId> themes = (Collection<ObjectId>) cla.getIdsLinked();
						for(ObjectId id: themes) {if (s != "") s+= ", "; s+= ((Card) serviceBean.getById(id)).getAttributeById(nameId).getStringValue();}		
					} catch (Exception e){logger.error(e);}
				}
				// (2012-07-03, GoRik) ������ ������ ������ ��� �������� ���������� ���������
				dubletEntry.append( c.getTemplateName())
					.append(" ")
					.append( safeGetAttrValue(c, ATTR_REGNUM))
					.append(", (����� ���������� ").append( safeGetAttrValue(c, ATTR_NUMOUT))
					.append(") ").append( safeGetAttrValue(c, ATTR_SHORTDESC)) // ������� ����������
					.append( "&nbsp;");

				dubletEntry.append("&nbsp;");
				if (hasAttachments)
					dubletEntry.append(HAS_ATTACHMENTS_TAG);
				dubletsContainer.put(c.getId(), dubletEntry.toString());
			}
			//final String msgText = mex.getLocalizedMessage();
			final String msgText = getMessage(request, "duplicate.citizen.request.title");
			final PortletMessage pm = new PortletMessage(msgText, cardIds,
					dubletsContainer, PortletMessageType.ERROR);
			sessionBean.setPortletMessage(pm);
			return;
		} catch (Exception e) {
			logger.error("Exception caught", e);
			if (isLocked && unlockOnError) {
				unlockQuietly(card, serviceBean);
			}
			msg = e.getLocalizedMessage();
			msgType = PortletMessageType.ERROR;
			//�������� ��������� ������� �����
			IntegerAttribute manNumAttr = (IntegerAttribute) curCard.getAttributeById(manuallyNumberAttrId);
			if(manNumAttr!=null) manNumAttr.setValue(0);
		}
		
		if(sessionBean.getGroupExecutionReportsSameCard() != null) {
			List<Card> cards = sessionBean.getGroupExecutionReportsSameCard();
			EditorDialogHelper.executeSameReports(serviceBean, card, cards);
			sessionBean.setGroupExecutionReportsSameCard(null);
		}
		
		if (!haveReservationRequests) sessionBean.setMessageWithType(msg, msgType);

		HashMap configuration = DigitalSignatureConfiguration
				.getConfiguration();
		DigitalSignatureConfiguration.Template templateConfig = (DigitalSignatureConfiguration.Template) configuration
				.get(card.getTemplate().getId());

		int applySignature = wfm.getApplyDigitalSignatureOnMove();
		if(sessionBean.isDisableDS()){
			applySignature = 0;
			sessionBean.setDisableDS(false);
		}
		
		if (applySignature > 0 && null != templateConfig) {
			request.getPortletSession().setAttribute("MI_APPLY_SIGNATURE",
						Integer.valueOf(applySignature));
			request.getPortletSession().setAttribute("FROM_STATE",
					wfm.getFromState());
		}
		
		if(msgType != PortletMessageType.ERROR
			&& TEMPLATE_OUTCOMING.equals(card.getTemplate())
				&& (WFM_BEFORE_REG_OUTCOMING.equals(wfm.getId())
					|| WFM_PREP_REG_OUTCOMING.equals(wfm.getId()))) {
			final LinkAttribute recipientsAttr = card.getAttributeById(ObjectId.predefined(CardLinkAttribute.class,"jbr.outcoming.receiver"));
			if(recipientsAttr != null) {
				final SendDeliveryDispatcher sdd = new SendDeliveryDispatcher((AsyncDataServiceBean) serviceBean, card, recipientsAttr.getIdsLinked());
				sdd.firstDispatch();
			}
		}
	}

	/**
	 * @param c
	 * @param attrid
	 * @return �������� �������� ��� ������ ������, ���� ��� ��� - �.�. ������
	 *         ���-�� ���-����, �� �� NULL.
	 */
	private Object safeGetAttrValue(Card c, ObjectId attrId) {
		if (c == null || attrId == null)
			return "";
		final Attribute attr = c.getAttributeById(attrId);
		return (attr == null || attr.getStringValue() == null) ? "" : attr
				.getStringValue();
	}

	private void editCardHandler(ActionRequest request, ActionResponse response) {
		CardPortletSessionBean sessionBean = getSessionBean(request);
		// DB
		try {
			sessionBean.getServiceBean().doAction(
					new LockObject(sessionBean.getActiveCard()));
			CardPortletCardInfo cardInfo = reloadCard(request, CARD_EDIT_MODE);
			cardInfo.setOpenedInEditMode(false);

			if (isActualDSExists(sessionBean, request.getLocale())) {
				response.setRenderParameter(WARNING_KEY_PREFIX,
						"edit.dsfound.warning");
			}

		} catch (Exception e) {
			logger.error(e);
			String msg = getMessage(request, "db.side.error.msg")
					+ e.getMessage();
			sessionBean.setMessageWithType(msg, PortletMessageType.ERROR);
		}
	}

	private List<Card> getOpenedActiveCardsList(CardPortletSessionBean sessionBean){
		List<Card> cardList = new ArrayList<Card>(); 
		for (CardPortletCardInfo cardInfo : sessionBean.getOpenedActiveCards()) {
			cardList.add(cardInfo.getCard());
		}
		return cardList;
	}

	private void cloneCardHandler(ActionRequest request) {
		CardPortletSessionBean sessionBean = getSessionBean(request);
		// DB
		try {
			DataServiceBean serviceBean = sessionBean.getServiceBean();
			CloneCard cloneAction = new CloneCard();
			cloneAction.setOrigId(sessionBean.getActiveCard().getId());
			cloneAction.setTemplate(sessionBean.getActiveCard().getTemplate());
			if (ObjectId.predefined(Template.class, "jbr.incoming").equals(
					sessionBean.getActiveCard().getTemplate())) {
				final Set<ObjectId> enSet = cloneAction.getEnabledAttrIds();
				enSet.add(ObjectId.predefined(CardLinkAttribute.class,
						"jbr.incoming.sender"));
				enSet.add(ObjectId.predefined(CardLinkAttribute.class,
						"jbr.signext"));
				enSet.add(ObjectId.predefined(PersonAttribute.class,
						"jbr.incoming.addressee"));
				enSet.add(ObjectId.predefined(CardLinkAttribute.class,
						"regjournal"));

				final Set<ObjectId> disabledSet = cloneAction
						.getDisabledAttrIds();
				disabledSet.add(ObjectId.predefined(StringAttribute.class,
						"jbr.incoming.outnumber"));
				disabledSet.add(ObjectId.predefined(DateAttribute.class,
						"jbr.incoming.outdate"));
				disabledSet.add(ObjectId.predefined(TextAttribute.class,
						"jbr.document.title"));
			}
			Card card = (Card) serviceBean.doAction(cloneAction);
			sessionBean.openNestedCard(card, null, true);
		} catch (Exception e) {
			logger.error(e);
			String msg = getMessage(request, "db.side.error.msg")
					+ e.getMessage();
			sessionBean.setMessageWithType(msg, PortletMessageType.ERROR);
		}
	}

	private void handleCopyFiles(ActionRequest request) throws DataException {
		final CopyFilesHandler handler = new CopyFilesHandler();
		CardPortletSessionBean sessionBean = getSessionBean(request);
		CardPortletCardInfo cardInfo = sessionBean.getActiveCardInfo();
		handler.setPortletFormManager(cardInfo.getPortletFormManager());
		handler.process(null, request, null);
	}
	
	private void getRegNumberHandler(ActionRequest request,CardPortletSessionBean sessionBean) throws DataException, ServiceException{
		List<ObjectId> baseDocGetRegNumber = Arrays.asList(
				ObjectId.predefined(Template.class, "jbr.incoming"),
				ObjectId.predefined(Template.class, "jbr.incomingpeople"));
		Card curCard = sessionBean.getActiveCard();
		if(!baseDocGetRegNumber.contains(curCard.getTemplate())){
			return;
		}
		boolean dialogOk = DIALOG_ACTION_OK.equals(request
				.getParameter(DIALOG_ACTION_FIELD));
		if(dialogOk){
			sessionBean.setCurrentPerson(sessionBean.getServiceBean().getPerson());
			sessionBean.getServiceBean().doAction(new UnlockObject(curCard.getId()));
			sessionBean.getServiceBean().setUser(new UserPrincipal("__system__"));
			sessionBean.getServiceBean().doAction(new LockObject(curCard.getId()));
			fillRegNumberDialog(request, sessionBean, curCard, false);
			sessionBean.getServiceBean().doAction(new UnlockObject(curCard.getId()));
			sessionBean.getServiceBean().setUser(request.getUserPrincipal());
			sessionBean.getServiceBean().doAction(new LockObject(curCard.getId()));
			reloadCard(request, CARD_EDIT_MODE);
			return;
		}
		if(!initRegNumberDialog(request, sessionBean, curCard))
			return;
		fillRegNumberDialog(request, sessionBean, curCard, true);

	}

	/**
	 * ���������������� �������� �������� �� �������
	 * 
	 * @param request
	 * @param card
	 */
	private void initTabs(PortletRequest request, ObjectId activeTabId,
			Card card) {
		CardPortletSessionBean sessionBean = getSessionBean(request);
		try {
			DataServiceBean serviceBean = sessionBean.getServiceBean();
			CardViewModeFilter viewModeFilter = null;
			if (sessionBean.getViewMode() != null) {
				viewModeFilter = new CardViewModeFilter(sessionBean.getViewMode());
			}
			Collection tabs = serviceBean.listChildren(card.getTemplate(),
					TabViewParam.class, viewModeFilter);
			if (tabs.size() == 0) {
				TabView tv = new TabView();
				tv.setId(-1);
				tabs.add(tv);
			}
			TabsManager tm = sessionBean.getActiveCardInfo().getTabsManager();
			tm.setTabs(tabs);
			try {
				if (activeTabId == null)
					activeTabId = tm.getActiveTab().getId();
			} catch (Exception e) {
			}
			if (activeTabId == null) {
				GetActiveTab action = new GetActiveTab();
				action.setTemplate(card.getTemplate());
				action.setStatus(card.getState());
				tabs = (List) serviceBean.doAction(action);
				if (tabs.size() > 0) {
					activeTabId = ((Tab) tabs.iterator().next()).getId();
				}
			}
			tm.setActiveTabId(activeTabId);
			TabView activeTab = new TabView(tm.getActiveTab());
			activeTab.setContainer(new TripleContainer());

			Collection tbvps = serviceBean.listChildren(tm.getActiveTab().getId(), TabBlockViewParam.class);
			Collection bvps = serviceBean.listChildren(card.getTemplate(), BlockViewParam.class);
			ArrayList tbs = new ArrayList(card.getAttributes());

			BlockViewsBuilder plant = new BlockViewsBuilder();
			plant.setBlockViewParams(bvps);
			plant.setTabBlockViewParams(tbvps);
			plant.setTemplateBlocks(tbs);
			plant.setCardState(card.getState());
			plant.setAttributeViewParams(sessionBean.getActiveCardInfo().getAttributeViewParams());
			plant.setRequest(request);
			plant.setActiveTabId(tm.getActiveTab().getId());
			
			Map bvsAll = sessionBean.getActiveCardInfo().getTabInfo();
			if (bvsAll == null) {
				bvsAll = plant.build();
				sessionBean.getActiveCardInfo().setTabInfo(bvsAll);
			}
			
			plant.setBvsAll(bvsAll);
			List<BlockView> bvs = plant.getActiveTab();
			
			activeTab.getContainer().setComponents(bvs);

			tm.addTab(activeTab);
			tm.setActiveTabId(activeTab.getId());

			GetEmptyTabs action = new GetEmptyTabs();
			tabs = (Collection) serviceBean.doAction(action);
			tm.setEmptyTabs(tabs);
		} catch (Exception e) {
			logger.error("Exception caught", e);
			String msg = getMessage(request, "db.side.error.msg")
					+ e.getMessage();
			sessionBean.setMessageWithType(msg, PortletMessageType.ERROR);
		}
	}

	/**
	 * ���������������� �������� �������� �� �������
	 * 
	 * @param request
	 * @param card
	 */
	private void initTabs(PortletRequest request, Card card) {
		String s = portletService.getUrlParameter(request, CARD_TAB_ID);
		ObjectId activeTabId = null;
		try {
			activeTabId = new ObjectId(Tab.class, Long.parseLong(s));
		} catch (NumberFormatException e) {
		}
		initTabs(request, activeTabId, card);
	}

	protected CardPortletSessionBean createSessionBean(PortletRequest request) {
		PortletSession session = request.getPortletSession(true);
		CardPortletSessionBean sessionBean = createCardPortletSessionBean();
		AsyncDataServiceBean serviceBean = PortletUtil.createService(request);
		sessionBean.setDataServiceBean(serviceBean);
		try {
			sessionBean.setAdminEmail(serviceBean.doAction(new GetAdminEmail())
					.toString());
		} catch (Exception e) {
			logger.error("Failed to determine Admin email", e);
		}
		//initDoclinkCreateData(sessionBean);
		session.setAttribute(SESSION_BEAN, sessionBean);
		return sessionBean;
	}

	protected CardPortletSessionBean createCardPortletSessionBean() {
		return new CardPortletSessionBean();
	}

	private void initDoclinkCreateData(CardPortletSessionBean bean) {
		if (bean == null)
			return;
		DoclinkCreateData data = null;
		
		ObjectId templateId = (bean.getActiveCardInfo()!=null&&bean.getActiveCard()!=null)?bean.getActiveCard().getTemplate():null;
		if (DOCLINK_CRETAE_CONFIG_PATH != null) {
			try {
				final InputStream stream = Portal.getFactory()
						.getConfigService()
						.loadConfigFile(DOCLINK_CRETAE_CONFIG_PATH);
				data = DoclinkCreateData.xmlStreamLoad(stream,
						bean.getServiceBean(), templateId);
			} catch (Exception ex) {
				logger.warn(ex);
				data = null;
			}
		}
		bean.setDoclinkCreateData(data);
	}

	/**
	 * Get SessionBean.
	 * 
	 * @param request
	 *            PortletRequest
	 * @return CardPortletSessionBean
	 */
	public static synchronized CardPortletSessionBean getSessionBean(
			PortletRequest request) {
		PortletSession session = request.getPortletSession();
		if (session == null) {
			logger.warn("Portlet session is not exists yet.");
			return null;
		}
		CardPortletSessionBean sessionBean = (CardPortletSessionBean) session
				.getAttribute(SESSION_BEAN);
		if (sessionBean == null)
			return null;
		String userName = (String) request.getPortletSession().getAttribute(
				DataServiceBean.USER_NAME, PortletSession.APPLICATION_SCOPE);
		if (userName != null) {
			sessionBean.getServiceBean().setUser(new UserPrincipal(userName));
			sessionBean.getServiceBean().setIsDelegation(true);
			sessionBean.getServiceBean()
					.setRealUser(request.getUserPrincipal());
		} else {
			
			sessionBean.getServiceBean().setIsDelegation(false);
		}
		if (Long.valueOf(0).equals(sessionBean.getServiceBean().getPerson().getId().getId())){
			String traceString = "[TRACE_0_USER] stackTrace: \r\n";
			StackTraceElement[] stack = Thread.currentThread().getStackTrace();
			for (StackTraceElement stackTraceElement : stack) {
				traceString += " " + stackTraceElement.getClassName() + "." + stackTraceElement.getMethodName() + "(" + stackTraceElement.getFileName() + ":" + stackTraceElement.getLineNumber() + ")\r\n";
			}
			logger.error(traceString);		
		}
		return sessionBean;
	}

	private static String getSessionBeanAttrNameForServlet(String namespace) {
		return SESSION_BEAN + '.' + namespace;
	}

	public static CardPortletSessionBean getSessionBean(
			HttpServletRequest request, String namespace) {
		CardPortletSessionBean sessionBean = (CardPortletSessionBean) request
				.getSession(false).getAttribute(
						getSessionBeanAttrNameForServlet(namespace));
		if (sessionBean == null)
			return null;
		String userName = (String) request.getSession().getAttribute(
				DataServiceBean.USER_NAME);
		if (userName != null) {
			sessionBean.getServiceBean().setUser(new UserPrincipal(userName));
			sessionBean.getServiceBean().setIsDelegation(true);
			sessionBean.getServiceBean()
					.setRealUser(request.getUserPrincipal());
		} else {
			sessionBean.getServiceBean().setUser(request.getUserPrincipal());
			sessionBean.getServiceBean().setIsDelegation(false);
		}
		return sessionBean;
	}

	protected void saveSessionBeanForServlet(RenderRequest request,
		RenderResponse response, CardPortletSessionBean sessionBean) {

		PortletSession session = request.getPortletSession();
		String namespace = response.getNamespace();
		session.setAttribute(getSessionBeanAttrNameForServlet(namespace),
				sessionBean, PortletSession.APPLICATION_SCOPE);

	}

	protected String getMessage(PortletRequest request, String key) {
		return getPortletConfig().getResourceBundle(request.getLocale())
				.getString(key);
	}

	/**
	 * Returns JSP file path.
	 * 
	 * @param request
	 *            Render request
	 * @param jspFile
	 *            JSP file name
	 * @return JSP file path
	 */
	protected static String getJspFilePath(RenderRequest request, String jspFile) {
		return JSP_FOLDER + jspFile + ".jsp";
	}

	protected boolean processAttributeAction(Card card, ActionRequest request,
			ActionResponse response) {
		if (card == null)
			return false;
		final CardPortletSessionBean sessionBean = getSessionBean(request);
		for (AttributeView av : sessionBean.getActiveCardInfo()
				.getTabsManager().getActiveAttributeViews()) {
			final AttributeEditor editor = av.getEditor();
			final Attribute attr = av.getAttribute();
			if (editor == null)
				continue;
			try {
				if (editor.processAction(request, response, attr)) {
					return true;
				}
			} catch (DataException e) {
				logger.error(
						"Exception caught while processing actions for attribute "
								+ attr.getId().getId(), e);
			}
		}
		return false;
	}

	/**
	 * @deprecated use {@link JspAttributeEditor#getAttrHtmlId(Attribute)}
	 *             instead
	 */
	public static String getAttributeFieldName(Attribute attr) {
		return "A_" + ((String) attr.getBlockId().getId()).replaceAll("_", "")
				+ "_" + ((String) attr.getId().getId()).replaceAll("_", "");
	}

	/**
	 * ����� ����������, ����� ����� ���������� ������� ��������
	 * @return CardPortletCardInfo
	 * @throws ServiceException
	 * @throws DataException
	 */
	protected CardPortletCardInfo reloadCard(PortletRequest request, String mode)
			throws DataException, ServiceException {
		CardPortletSessionBean sessionBean = getSessionBean(request);
		CardPortletCardInfo cardInfo = sessionBean.getActiveCardInfo();
		ObjectId cardId = cardInfo.getCard().getId();
		// ��������� �������� ������, ���� ��� ��� ��������� � ��
		if (cardId!=null){
			cardInfo.setCard((Card) sessionBean.getServiceBean().getById(cardId));
			cardInfo.setRefreshRequired(true);
			cardInfo.setMode(mode);

			cardInfo.setReloadRequired(false);
			cardInfo.setTabInfo(null);
		}
		return cardInfo;
	}

	/**
	 * ����������� ���������, ����������� ��� ����������� ��������
	 * 
	 * @param cardInfo
	 * @param serviceBean
	 * @throws DataException
	 * @throws ServiceException
	 */
	protected void loadCardInfo(CardPortletSessionBean sessionBean,
			AsyncDataServiceBean serviceBean) throws DataException, ServiceException {
		CardPortletCardInfo cardInfo = sessionBean.getActiveCardInfo();
		final Card card = cardInfo.getCard();
		final ObjectId cardId = card.getId();
		if (cardId == null) {
			// ����� �������� (��� �������� �������������) ����������� � ������
			// ��������������
			cardInfo.setAvailableWorkflowMoves(new ArrayList(0));
			cardInfo.setAttributeViewParams(loadTemplateAttributeViewParams(
					serviceBean, card, sessionBean));
			cardInfo.setCanChange(true);
			cardInfo.setTabsManager(new TabsManager());
		} else {
			cardInfo.setCanChange(serviceBean.canChange(card.getId()));
			if (cardInfo.isCanChange()){  // �������� �������� ������ ����� ���� ����� ������ ���
				cardInfo.setAvailableWorkflowMoves((List) serviceBean.listChildren(
						card.getId(), WorkflowMove.class));
			} else {
				cardInfo.clearAvailableWorkflowMoves();
			}
			cardInfo.setAttributeViewParams(loadCardAttributeViewParams(
					serviceBean, card, sessionBean));
			// ������������� ����������� �������� ��� ��������� ���������
			initCardLinkAttributes(card, serviceBean);
		}
		cardInfo.clearAttributeEditorsData();
		CreateCard createCardAction = new CreateCard();
		createCardAction.setTemplate(card.getTemplate());
		cardInfo.setCanCreate(serviceBean.canDo(createCardAction));
		cardInfo.setCardState((CardState) serviceBean.getById(card.getState()));

		ActionsDescriptor ad;
		try {
			ActionsDescriptorReader r = new ActionsDescriptorReader();
			InputStream stream = Portal.getFactory().getConfigService()
					.loadConfigFile(ACTIONS_CONFIG_PATH);
			ad = r.readFromFile(stream, serviceBean);
			logger.info("Actions descriptor read successfully");
		} catch (Exception e) {
			ad = new ActionsDescriptor();
			logger.error("Failed to read actions descriptor", e);
		}
		final CardPortletActionsManager am = new CardPortletActionsManager();
		am.setServiceBean(serviceBean);
		am.setSessionBean(sessionBean);
		am.setActionsDescriptor(ad);
		cardInfo.setActionsManager(am);
		cardInfo.setRefreshRequired(false);
		cardInfo.setTabInfo(null);
	}

	protected Collection loadTemplateAttributeViewParams(DataServiceBean serviceBean,
			final Card card, CardPortletSessionBean sessionBean) throws DataException, ServiceException {
		CardViewModeFilter viewModeFilter = null;
		if (sessionBean.getViewMode() != null) {
			viewModeFilter = new CardViewModeFilter(sessionBean.getViewMode());
		}
		return serviceBean.listChildren(card.getTemplate(),
				AttributeViewParam.class, viewModeFilter);
	}

	protected Collection loadCardAttributeViewParams(DataServiceBean serviceBean,
			final Card card, CardPortletSessionBean sessionBean) throws DataException, ServiceException {
		CardViewModeFilter viewModeFilter = null;
		if (sessionBean.getViewMode() != null) {
			viewModeFilter = new CardViewModeFilter(sessionBean.getViewMode());
		}
		return serviceBean.listChildren(card.getId(),
				AttributeViewParam.class, viewModeFilter);
	}

	/**
	 * ����������� �������� �������� � �������� ��������� � ���������� ��������
	 * �������� ���� ����� ��� - �� ������������ ������� �� BackURL'�.
	 * 
	 * @param request
	 * @param response
	 * @param sessionBean
	 * @throws IOException
	 */
	private void closeCard(ActionRequest request, ActionResponse response,
			CardPortletSessionBean sessionBean) throws IOException {
		sessionBean.getActiveCardInfo().release();

		try {
		sessionBean.closeActiveCard();
		} catch (DataException e) {
			String msg = getMessage(request, "db.side.error.msg")
					+ e.getMessage();
			sessionBean.setMessageWithType(msg, PortletMessageType.ERROR);
			return;
		} catch (ServiceException e) {
			String msg = getMessage(request, "db.side.error.msg")
					+ e.getMessage();
			sessionBean.setMessageWithType(msg, PortletMessageType.ERROR);
			return;
		}
		
		if (sessionBean.getActiveCardInfo() == null) {
			if (sessionBean.getMessage() != null){
				PortletSession session = request.getPortletSession();
				session.setAttribute(MIShowListPortlet.MSG_PARAM_NAME, sessionBean.getMessage(), PortletSession.APPLICATION_SCOPE);
			}
			leaveCardPortlet(request,response,sessionBean);
		}
	}

	/**
	 * ������������� �� ��������� �������� �������
	 */
	private void redirectToPortalDefaultPage(ActionRequest request,
			ActionResponse response) throws IOException {
		String backURL = portletService.generateLink("dbmi.defaultPage", null,
				null, request, response);
		response.sendRedirect(backURL);
	}

	/*
	 * ��������� ��������� �������� ���������� NAME �������� �� ������� ���
	 * ��������. ����� ��� ����������� �������� ��������� ��������� ��� ������
	 * ��������
	 */
	public static void initCardLinkAttributes(Card card,
			DataServiceBean serviceBean) throws ServiceException, DataException {
		Iterator iter = card.getAttributes().iterator();
		while (iter.hasNext()) {
			AttributeBlock block = (AttributeBlock) iter.next();
			Iterator iterAttr = block.getAttributes().iterator();
			while (iterAttr.hasNext()) {
				Attribute attr = (Attribute) iterAttr.next();
				if (attr instanceof CardLinkAttribute) {
					CardLinkAttribute link = (CardLinkAttribute) attr;
					if (link.getIdsLinked().size() > 0) {

						Search search = new Search();
						search.setByCode(true);

						final List columns = new ArrayList();
						final SearchResult.Column col = new SearchResult.Column();
						col.setAttributeId(ATTR_NAME);
						columns.add(col);
						search.setColumns(columns);

						search.setWords(link.getLinkedIds());

						Collection linkedCards = ((SearchResult) serviceBean
								.doAction(search)).getCards();
						if (link.getLabelAttrId() == null) {
							link.setLabelAttrId(ATTR_NAME);
						}
						link.addIdsLinked(linkedCards);
					}
				}
			}
		}
	}
	
	private boolean initRegNumberDialog(ActionRequest request, CardPortletSessionBean sessionBean, Card curCard){
		ListAttribute attribute = (ListAttribute) curCard.getAttributeById(CHECK_REPEAT_ATTR_ID);
		CardLinkAttribute ogReqAuthorAttr = (CardLinkAttribute)curCard.getAttributeById(OG_REQ_AUTHOR_ATTR_ID);
		List<ObjectId> ogReqAuthorIds = (ogReqAuthorAttr != null)?ogReqAuthorAttr.getIdsLinked():null;
		boolean isAnonymOg = (curCard.getTemplate().equals(TEMPLATE_CA_ID) && ogReqAuthorIds != null && ogReqAuthorIds.indexOf(ANONYMOUS) != -1)?true:false;
		if(attribute.getValue().getId().equals(JBR_CONTROL_YES) && !DIALOG_EDITOR_ACTION_OK.equals(request.getParameter(DIALOG_EDITOR_ACTION_FIELD)) &&  !DIALOG_ACTION_OK.equals(request.getParameter(DIALOG_ACTION_FIELD)) && !isAnonymOg){
			try {
				AttributeEditorDialog dialog = new AttributeEditorDialog();
				dialog.setTitle(getMessage(request, "dialog.editor.previousAppeals.confirm"));
				dialog.setActionCode(request.getParameter(ACTION_FIELD));
				dialog.setActiveCard(curCard);
				dialog.setDataServiceBean(sessionBean.getServiceBean());
				dialog.setCache(true);
				if(dialog.isData()){
					sessionBean.setAttributeEditorDialog(dialog);
					return false;
				}
			} catch (Exception e) {
				logger.error(e);
			}
		} else{
			//Already confirmed
			sessionBean.setAttributeEditorDialog(null);
		}
		return true;
	}
	
	private boolean fillRegNumberDialog(ActionRequest request, CardPortletSessionBean sessionBean, Card curCard, boolean preliminary){
		boolean dialogOk = DIALOG_ACTION_OK.equals(request
				.getParameter(DIALOG_ACTION_FIELD));
		String regnumber = ((StringAttribute) curCard
				.getAttributeById(regnumberAttrId)).getValue();
		if ((sessionBean.getAttributeEditorDialog() == null && sessionBean.getDialog() == null && !dialogOk
				&& (regnumber == null || regnumber.trim().length() == 0))||!preliminary) {
			
			try {
				if (isReservationRequestPublished(sessionBean, curCard)) {
					sessionBean.setMessage(getMessage(request,
							"card.register.requestPublished"));
					return false;
				}

				// ������ �������� ��������� ��� ���������������� ������
				Card card = curCard;
				// ��������� ��������, ���� ���������� ID (���� ������ � ��� �� ��������)
				if (CardPortlet.CARD_EDIT_MODE.equals(sessionBean
						.getActiveCardInfo().getMode())) {
					if(curCard.getId()==null){
						// � ����� ������������ ������ ������
						storeCardSyncHandler(request);
					}
				}
				
				SetRegistrationNumber dgrn = new SetRegistrationNumber();
				dgrn.setCard(card);
				dgrn.setPreliminary(preliminary);
				if(!preliminary){
					dgrn.setRegistrator(sessionBean.getCurrentPerson());
				}
				String num = (String) sessionBean.getServiceBean()
						.doAction(dgrn);
				CardPortletDialog dialog = new CardPortletDialog();
				dialog.setTitle(getMessage(request,
						"card.register.dialog.title"));
				dialog.setMessage(MessageFormat
						.format(getMessage(request,
								"card.register.dialog.message"), num));
				dialog.setCardPortletAction(request
						.getParameter(ACTION_FIELD));

				// �������� ���� �� ���� "JBR_MANUALLY_NUMBER" ���
				// ����������� ��� �������
				// ���� ����� ������ ������� � ������� ��������������
				// ��������� ������
				Collection<AttributeViewParam> arrtViewParams = sessionBean
						.getActiveCardInfo().getAttributeViewParams();
				Iterator<AttributeViewParam> arrtViewParamsIter = arrtViewParams
						.iterator();
				boolean showManualInputControl = false;
				while (arrtViewParamsIter.hasNext()) {
					AttributeViewParam attrViewParam = arrtViewParamsIter
							.next();
					if (attrViewParam.getAttribute().getId()
							.equals(manuallyNumberAttrId.getId())) {
						showManualInputControl = !attrViewParam
								.isReadOnly();
						break;
					}
				}
				if (showManualInputControl) {
					dialog.setInputLabel(getMessage(request,
							"card.register.dialog.number"));
				}
				if(preliminary){
					sessionBean.setDialog(dialog);
				} else {
					sessionBean.setDialog(null);
				}
				return true;
			} catch (DataException e) {
				sessionBean.setMessageWithType(e.getMessage(), PortletMessageType.ERROR);
				return false;
			} catch (ServiceException e) {
				sessionBean.setMessageWithType(e.getMessage(), PortletMessageType.ERROR);
				return false;
			}
		} else {
			sessionBean.setDialog(null);
			return false;
		}
	}
	/**
	 * ���������� ����� ��������� ���������� (�������� ��� ��) 
	 * @param request
	 * @param sessionBean
	 */
	private void checkOnRepeatedActionHandler(PortletRequest request, CardPortletSessionBean sessionBean) throws Exception{
		Search baseSearch = new Search();
        InputStream inputStream = Portal.getFactory().getConfigService().
        		loadConfigFile(CONFIG_FILE_PREFIX + "/dialog-settings/income-og-doublet.xml");
        baseSearch.initFromXml(inputStream);
        Card card = sessionBean.getActiveCard();
        Search search = baseSearch.makeCopy();
        search.clearAttributes();
        List<String> errorAttrs = new ArrayList<String>();
        for(ObjectId searchAttribute: baseSearch.getObjectIdAttributes()){
        	Attribute cardAttribute = card.getAttributeById(searchAttribute);
        	if(cardAttribute != null){
        		if(cardAttribute.isEmpty()){
        			errorAttrs.add(cardAttribute.getName());
        		}
        		if(cardAttribute instanceof TextAttribute){
        			TextAttribute textCardAttribute = (TextAttribute) cardAttribute;
        			search.addStringAttribute(textCardAttribute.getId(), 
        					textCardAttribute.getValue(), TextSearchConfigValue.EXACT_MATCH);
        		} else if(cardAttribute instanceof StringAttribute){
        			StringAttribute stringCardAttribute = (StringAttribute) cardAttribute;
        			search.addStringAttribute(stringCardAttribute.getId(), 
        					stringCardAttribute.getValue(), TextSearchConfigValue.EXACT_MATCH);
        		} else if(cardAttribute instanceof DateAttribute){
        			DateAttribute dateCardAttribute = (DateAttribute) cardAttribute;
        			search.addDateAttribute(dateCardAttribute.getId(),
        					dateCardAttribute.getValue(), dateCardAttribute.getValue());
        		} else if(cardAttribute instanceof PersonAttribute){
        			PersonAttribute personCardAttribute = (PersonAttribute) cardAttribute;
        			for(Object p : personCardAttribute.getValues()){
        				search.addPersonAttribute(personCardAttribute.getId(), ((Person) p).getId());
        			}
        		} else if(cardAttribute instanceof CardLinkAttribute) {
        			CardLinkAttribute linkCardAttribute = (CardLinkAttribute) cardAttribute;
        			for(ObjectId id: linkCardAttribute.getIdsLinked()){
        				search.addCardLinkAttribute(linkCardAttribute.getId(), id);
        			}
        		}
        	}
        }
        if(!errorAttrs.isEmpty()){
        	StringBuilder message = new StringBuilder();
        	Iterator<String> i = errorAttrs.iterator();
        	while(i.hasNext()){
        		String attrName = i.next();
        		message.append(attrName);
        		if(i.hasNext()){
        			message.append(", ");
        		} else {
        			message.append(".");
        		}
        	}
        	throw new DataException("check.on.repeated.mandatoty.attr.not.set", new Object[] {message.toString()});
        }
        if(sessionBean.getActiveCard().getId() != null){
        	search.setIgnoredIds(Collections.singleton(sessionBean.getActiveCard().getId()));
        }
        search.setTemplates(Collections.singletonList(card.getTemplate()));
        search.getFilter().setCurrentUserRestrict(Search.Filter.CU_DONT_CHECK_PERMISSIONS_FINAL_DOC);
        SearchResult result = sessionBean.getServiceBean().doAction(search);
        sessionBean.setRepeatedDocuments(new HashMap<Long, String>());
        for(Card resCard: result.getCards()){
        	String name = resCard.getAttributeById(ATTR_NAME).getStringValue();
        	sessionBean.getRepeatedDocuments().put((Long)resCard.getId().getId(), StringEscapeUtils.escapeHtml(name));
        }
	}

	private boolean isCurrentCardExists(PortletRequest request){
		try{
			CardPortletSessionBean sessionBean = getSessionBean(request);
			CardPortletCardInfo cardInfo = sessionBean.getActiveCardInfo();
			ObjectId cardId = cardInfo.getCard().getId();
			//������� ��� �������� ����������, ���� ��� �� ��������� � ���� (�.�. ��� ����� ���� ���������)
			if(cardId == null){
				return true;
			}
			sessionBean.getServiceBean().getById(cardId);
			return true;
		} catch (Exception e) {
			logger.error("Try move back to non-existed card ");
			return false;
		}
	}
	
	private void leaveCardPortlet(ActionRequest request, ActionResponse response,CardPortletSessionBean sessionBean) throws IOException{
		String backURL = sessionBean.getBackURL();
		sessionBean.reset();
		// TODO: ������� �� ������� � �� application scope, �� ��� �����
		// ����� ����� namespace
		request.getPortletSession().removeAttribute(SESSION_BEAN);
		if (backURL == null) {
			redirectToPortalDefaultPage(request, response);
		} else {
			response.sendRedirect(backURL);
		}
	}
}