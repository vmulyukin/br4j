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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
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
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.aplana.dbmi.ConfigHolder;
import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.CreateCard;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.action.SearchResult.Column;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.card.AttributeEditorDialog;
import com.aplana.dbmi.card.CardPortlet;
import com.aplana.dbmi.card.CardPortletCardInfo;
import com.aplana.dbmi.card.CardPortletSessionBean;
import com.aplana.dbmi.card.dialog.EditorDialogHelper;
import com.aplana.dbmi.card.dialog.GroupExecutionSameCardEditorDialog;
import com.aplana.dbmi.card.util.ARMUtils;
import com.aplana.dbmi.card.util.CardAttrComparator;
import com.aplana.dbmi.card.util.CardAttributesComparator;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.HtmlAttribute;
import com.aplana.dbmi.model.LinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.TextAttribute;
import com.aplana.dbmi.model.TypedCardLinkAttribute;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.model.util.SearchUtils;
import com.aplana.dbmi.service.AsyncDataServiceBean;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.PortletUtil;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.dbmi.service.SystemUser;
import com.aplana.dbmi.service.UserPrincipal;
import com.aplana.dbmi.service.AsyncDataServiceBean.ExecuteOption;
import com.aplana.dbmi.support.action.ProcessGroupExecution;
import com.aplana.dbmi.util.IdUtils;
import com.aplana.web.tag.util.StringUtils;

public class ResolutionReportPortlet extends GenericPortlet {
	
	private static final String APPLICATION_SESSION_BEAN_PREFIX = "resolutionReportPortletSessionBean:";
	
	public static final String SESSION_BEAN = "resolutionReportPortletSessionBean";

	public static final String VIEW_JSP = "/WEB-INF/jsp/resolutionReport/resolutionReport.jsp";
	public static final String GROUP_EXECUTION_JSP = "/WEB-INF/jsp/resolutionReport/groupExecution.jsp";

	public static final String PARAM_BACK_URL = "backURL";
	public static final String PARAM_EDIT_CARD_ID = "editCardId";

	public static final String FIELD_ACTION = "formAction";	
	public static final String FIELD_NAMESPACE = "namespace";
	public static final String FIELD_PRESSED_BTN = "pressedBtn";
	public static final String CREATE_DOC_BTN = "createPreparedDocBtn";
	public static final String REPORT_GROUP_PARAM = "REPORT_GROUP";
	
	
	public static final ObjectId TMP_ATTR_REPORT_AUTHOR_NAME = new ObjectId(StringAttribute.class, "TMP_ATTR_REPORT_AUTHOR_NAME");
	public static final ObjectId ATTR_NAME = ObjectId.predefined(StringAttribute.class, "name");
	public static final ObjectId ATTR_FIO = ObjectId.predefined(StringAttribute.class, "jbr.person.lastnameNM");
	
	public static final ObjectId ATTR_RESOLUT = ObjectId.predefined(TextAttribute.class, "jbr.resolutionText");
	public static final ObjectId ATTR_REPORT_TEXT = ObjectId.predefined(TextAttribute.class, "jbr.report.hidden.text");
	public static final ObjectId ATTR_REPORT_HTML = ObjectId.predefined(HtmlAttribute.class, "jbr.report.text");
	public static final ObjectId ATTR_CURRENT_REPORT = ObjectId.predefined(TextAttribute.class, "jbr.report.currentText");
	
	public static final ObjectId ATTR_SIGN_DATE = ObjectId.predefined(DateAttribute.class, "jbr.resolution.SignDate");
	public static final ObjectId ATTR_CREATED_DATE = ObjectId.predefined(DateAttribute.class, "created");
	
	public static final ObjectId ATTR_REPORT_SIGN = ObjectId.predefined(PersonAttribute.class, "jbr.report.int.executor");
	
	public static final ObjectId ATTR_REPORT_EXTERNAL_EXECUTOR = ObjectId.predefined(CardLinkAttribute.class, "jbr.report.ext.executor");
	public static final ObjectId ATTR_RES_PARENT = ObjectId.predefined(CardLinkAttribute.class, "jbr.report.int.parent");
	public static final ObjectId ATTR_RIMP_RELASSIG = ObjectId.predefined(BackLinkAttribute.class, "jbr.linkedResolutions");
	public static final ObjectId ATTR_IMPL_RESOLUT = ObjectId.predefined(BackLinkAttribute.class, "jbr.resolutions");
	public static final ObjectId ATTR_EXEC_EXT = ObjectId.predefined(CardLinkAttribute.class, "jbr.ExtExecutor");
	public static final ObjectId ATTR_PREPARED_DOCS = ObjectId.predefined(TypedCardLinkAttribute.class, "jbr.report.result");
	public static final ObjectId ATTR_REPORTS = ObjectId.predefined(BackLinkAttribute.class, "jbr.reports");
	public static final ObjectId ATTR_EXTERNAL_REPORTS = ObjectId.predefined(BackLinkAttribute.class, "jbr.resolution.ExtReport");
	public static final ObjectId ATTR_REPORT_ATTACHMENTS = ObjectId.predefined(CardLinkAttribute.class, "jbr.report.attachments");
	public static final ObjectId ATTR_FILES = ObjectId.predefined(CardLinkAttribute.class, "jbr.files");
	
	public static final ObjectId CARD_STATE_DRAFT = ObjectId.predefined(CardState.class, "draft");
	public static final ObjectId CARD_STATE_CANCELLED = ObjectId.predefined(CardState.class, "cancelled");
	public static final ObjectId CARD_STATE_TRASH = ObjectId.predefined(CardState.class, "trash");
	public static final ObjectId CARD_STATE_DONE = ObjectId.predefined(CardState.class, "done");
	public static final ObjectId CARD_STATE_EXECUTION = ObjectId.predefined(CardState.class, "execution");

	public static final ObjectId TEMPLATE_INSTRUCTION = ObjectId.predefined(Template.class, "jbr.resolution");
	public static final ObjectId TEMPLATE_INDEPENDENT = ObjectId.predefined(Template.class, "jbr.independent.resolution");

	public static final ObjectId WFM_REPORT_EXECUTE = ObjectId.predefined(WorkflowMove.class, "jbr.report.int.execute");

	public static final ObjectId GROUP_REPORT_TEMPLATE_ID = ObjectId.predefined(Template.class, "jbr.group.report.int");

	public static final String PREPARED_DOC_URL = "/portal/auth/portal/dbmi/card/CardPortletWindow?action=e&windowstate=normal&mode=view&MI_EDIT_CARD=";
	public static final String ATTACHMENT_URL = "/DBMI-UserPortlets/MaterialDownloadServlet?MI_ATTR_ID_FIELD=ADMIN_702355&&MI_CARD_ID_FIELD=";	
	public static final String MATERIAL_UPLOAD_URL =  "/DBMI-UserPortlets/servlet/arm-upload";
	
	public static final String PLACEHOLDER_STRING = "Placeholder";
	
	private static final Long REF_VAL_IN_RSPONSE_ID = 
			Long.parseLong(ObjectId.predefined(ReferenceValue.class, "jbr.inResponse").getId().toString());
		
	public static final String PARAM_EXEC_REPORT = "executeReport";
    public static final String DIALOG_EDITOR_ACTION_FIELD = "MI_DIALOG_EDITOR_ACTION";
	public static final String DIALOG_EDITOR_ACTION_OK = "OK";
	
	private Log logger = LogFactory.getLog(getClass());
	
	@Override
	protected void doView(RenderRequest request, RenderResponse response)
			throws PortletException, PortletSecurityException, IOException {
		
		ResolutionReportPortletSessionBean sessionBean = getSessionBean(request);
		String key = getApplicationSessionBeanKey(response.getNamespace());
		PortletSession session = request.getPortletSession();
		session.setAttribute(key, sessionBean, PortletSession.APPLICATION_SCOPE);
	
		final String backLink = request.getParameter(PARAM_BACK_URL);
		if(backLink != null){
			sessionBean.setBackLink(backLink);
		}
	
		String message = "'";
		if (sessionBean.getMessage() != null) {
			message += sessionBean.getMessage();
			sessionBean.setMessage(null);
		}
		message += "'";
		request.setAttribute("message", message);
		
		try {			
			if(sessionBean.isGroupExecutionMode()){
				request.setAttribute("groupResolutions", getCroupResolutionsJSONDate(sessionBean));
				sessionBean.setHeader(getResourceBundle(request.getLocale()).getString("header.group.execution"));
			} else {
			request.setAttribute("resolutions", getResolutionsJSONData(sessionBean));
			request.setAttribute("attachments", ARMUtils.getAttachmentsJSONData(sessionBean.getAttachments()));		
			request.setAttribute("preparedDocuments", getPreparedDocumentsJSONData(sessionBean.getPreparedDocuments()));
				request.setAttribute("reportCardId", sessionBean.getResolutionReportCard().getId().getId());
			}
			
			request.setAttribute("reportText", sessionBean.getReportText());
			request.setAttribute("reportAttachments", ARMUtils.getAttachmentsJSONData(sessionBean.getReportAttachments()));		
			request.setAttribute("reportPreparedDocs", getPreparedDocumentsJSONData(sessionBean.getReportPreparedDocs()));
			
			createCardPortletSessionBean(request, response, sessionBean);
			
			String preparedDocsHeader = getPortletConfig().
					getResourceBundle(request.getLocale()).getString("header.prepared_documents");
			ARMDocumentPickerAttributeEditor documentPickerAttributeEditor = 
				new ARMDocumentPickerAttributeEditor(request, sessionBean.getServiceBean(), 
						(String) ATTR_PREPARED_DOCS.getId(), preparedDocsHeader);
			documentPickerAttributeEditor.setEditorDataToRequest(request, response);
			
		} catch (DataException e) {
			sessionBean.setMessage(e.getMessage());
			logger.error(e);
		} catch (ServiceException e) {
			sessionBean.setMessage(e.getMessage());
			logger.error(e);
		}
		
		response.setContentType("text/html");
		PortletRequestDispatcher rd = null;
		if(sessionBean.isGroupExecutionMode()){
			rd = getPortletContext().getRequestDispatcher(GROUP_EXECUTION_JSP);
		} else {
			rd = getPortletContext().getRequestDispatcher(VIEW_JSP);
		}
		rd.include(request, response);

	}

	@Override
	public void processAction(ActionRequest request, ActionResponse response)
			throws PortletException, PortletSecurityException, IOException {
		ResolutionReportPortletSessionBean sessionBean = null;
		
		String actionParam = request.getParameter(FIELD_ACTION);
		ResolutionReportPortletAction action = actionParam != null && actionParam.length() > 0 ? 
				ResolutionReportPortletAction.valueOf(actionParam) : ResolutionReportPortletAction.INIT;
		
		if (ResolutionReportPortletAction.INIT == action) {
			PortletSession session = request.getPortletSession();
			session.removeAttribute(SESSION_BEAN, PortletSession.APPLICATION_SCOPE);
			
			sessionBean = getSessionBean(request);
			try {
				fillSessionBean(request, sessionBean);
			} catch (DataException e) {
				sessionBean.setMessage(e.getMessage());
				logger.error(e);
			} catch (ServiceException e) {
				sessionBean.setMessage(e.getMessage());
				logger.error(e);
				return;
			}
		} else {
			sessionBean = getSessionBean(request);
		}
		
		prepareBackUrl(request, sessionBean);
		
	    //Load into card data from AttributeEditorDialog and clear bean attribute
		if(sessionBean.getGroupExecutionReportsSameCard() != null && request.getParameter(DIALOG_EDITOR_ACTION_FIELD) != null) {
			AttributeEditorDialog dialog = sessionBean.getAttributeEditorDialog();
			if(dialog != null && request.getParameter(PARAM_EXEC_REPORT) != null){
				List<ObjectId> ids =  IdUtils.stringToAttrIds((String)request.getParameter(PARAM_EXEC_REPORT), Card.class, true, false);
				List<Card> cardsDub = new ArrayList<Card>();
				GroupExecutionSameCardEditorDialog ered = (GroupExecutionSameCardEditorDialog) dialog;
				for(Card c : ered.getCards()) {
					if(ids.contains(c.getId())) {
						cardsDub.add(c);
					}
				}
				sessionBean.setGroupExecutionReportsSameCard(cardsDub);
				sessionBean.setAttributeEditorDialog(null);
			}
		}
			
		try {
			if(sessionBean.getGroupExecutionReportsSameCard() == null
					&& !sessionBean.isGroupExecutionMode()
					&& request.getParameter(DIALOG_EDITOR_ACTION_FIELD) == null){
				sessionBean.setGroupExecutionReportsSameCard(new ArrayList<Card>());
				GroupExecutionSameCardEditorDialog dialog = new GroupExecutionSameCardEditorDialog();
				dialog.setTitle(getMessage(request, "dialog.editor.resolutions.confirm"));
				dialog.setActionCode(request.getParameter(FIELD_ACTION));
				dialog.setActiveCard(sessionBean.getResolutionReportCard());
				dialog.setDataServiceBean(sessionBean.getServiceBean());
				dialog.setCache(true);
				if(dialog.isData()){
					sessionBean.setAttributeEditorDialog(dialog);
					return;
				}
			}	
		} catch (DataException e) {
			sessionBean.setMessage(e.getMessage());
			logger.error(e);
		} catch (ServiceException e) {
			sessionBean.setMessage(e.getMessage());
			logger.error(e);
		} 
		
		switch (action) {
			case SAVE: processActionSave(request, response, sessionBean); break;
			case EXECUTE: processActionExecute(request, response, sessionBean); break;	
			case CANCEL: doExit(request, response, sessionBean); break;
			case GROUP_EXECUTE: processActionGroupExecution(request, response, sessionBean, 
					ResolutionReportPortletAction.GROUP_EXECUTE, ExecuteOption.ASYNC); break;
			case GROUP_SAVE: processActionGroupExecution(request, response, sessionBean, 
					ResolutionReportPortletAction.GROUP_SAVE, ExecuteOption.SYNC); break;
			case REDIRECT: processRedirectAction(request, response, sessionBean);
		}
	}

	private void processRedirectAction(ActionRequest request,
			ActionResponse response,
			ResolutionReportPortletSessionBean sessionBean) throws IOException {
		updateReportCardFromRequest(sessionBean, request);
		try {
			updateSessionBean(sessionBean, request);
		} catch (Exception e) {
			logger.error("Exception due to process portlet redirectAction",e);
		}
		doExit(request, response, sessionBean, false);
	}


	private void processActionGroupExecution(ActionRequest request, ActionResponse response, 
			ResolutionReportPortletSessionBean sessionBean, 
			ResolutionReportPortletAction action, ExecuteOption executeOption) {
		updateReportCardFromRequest(sessionBean, request);
		ProcessGroupExecution processGroupExecution = new ProcessGroupExecution();
		List<ObjectId> reports = new ArrayList<ObjectId>();
		for(Card report: sessionBean.getGroupExecutionReports()){
			reports.add(report.getId());
		}
		processGroupExecution
			.setCurrentReport(sessionBean.getResolutionReportCard())
			.setReports(reports)
			.setOnlyCopy(ResolutionReportPortletAction.GROUP_SAVE.equals(action));
		try {
			sessionBean.getServiceBean().doAction(processGroupExecution, executeOption);
			doExit(request, response, sessionBean);
		} catch (Exception e) {
			sessionBean.setMessage(e.getMessage());
			logger.error(e.getStackTrace());
		} 
	}

	private void prepareBackUrl(ActionRequest request, ResolutionReportPortletSessionBean sessionBean) throws UnsupportedEncodingException {
		String pressedBtnParam = request.getParameter(FIELD_PRESSED_BTN);
		if (CREATE_DOC_BTN.equals(pressedBtnParam)) {
			StringBuilder url = new StringBuilder("/portal/auth/portal/boss/chooseDocTemplate/Content?action=1&windowstate=normal&mode=view&formAction=init");
			StringBuilder backURL = new StringBuilder("/portal/auth/portal/boss/resolutionReport/Content?action=e&windowstate=normal&mode=view");
			if (StringUtils.hasLength(sessionBean.getBackLink())) {
				backURL.append("&backURL=").append(sessionBean.getBackLink());
			}
			url.append("&backURL=").append(URLEncoder.encode(backURL.toString(), "UTF-8"));
			sessionBean.setBackLink(url.toString());
		}
	}
	
	private void processActionExecute(ActionRequest request, ActionResponse response, ResolutionReportPortletSessionBean sessionBean) throws  IOException {
		Card reportCard = sessionBean.getResolutionReportCard();
		DataServiceBean serviceBean = sessionBean.getServiceBean();
		try {
			processActionSave(request, response, sessionBean, ExecuteOption.SYNC);			
			
			ChangeState move = new ChangeState();			
			serviceBean.doAction(new LockObject(reportCard));
			move.setCard(reportCard);
			move.setWorkflowMove((WorkflowMove) DataObject.createFromId(WFM_REPORT_EXECUTE));
			serviceBean.doAction(move);
			
			if(sessionBean.getGroupExecutionReportsSameCard() != null){
				setReportText(request, sessionBean);
    			EditorDialogHelper.executeSameReports(sessionBean.getServiceBean(), 
    												  sessionBean.getResolutionReportCard(),
    												  sessionBean.getGroupExecutionReportsSameCard());
    			sessionBean.setGroupExecutionReportsSameCard(null);
    		}
			
			doExit(request, response, sessionBean);
		} catch (DataException e) {
			final StringBuffer message = new StringBuffer();
			message.append(
					MessageFormat.format(
							getResourceBundle(request.getLocale()).getString("msg.resolutionNotSave"),
							new Object[]{
								e.getMessage().replaceAll(e.getClass().getName(), "").replaceAll("'", "\""),
								ConfigHolder.getPageLabel("hotline.phone")
							}
					)
			);			
			sessionBean.setMessage(message.toString());
			logger.error(e);
		} catch (ServiceException e) {
			sessionBean.setMessage(e.getMessage());
			logger.error(e);
		} finally {
			unlockQuickly(reportCard.getId(), serviceBean);		
		}
	}
	
	private void processActionSave(ActionRequest request, ActionResponse response, ResolutionReportPortletSessionBean sessionBean) throws IOException {
		try {
			processActionSave(request, response, sessionBean, ExecuteOption.ASYNC);
			doExit(request, response, sessionBean);
		} catch (ServiceException e) {
			sessionBean.setMessage(e.getMessage());
			logger.error(e);
		} catch (DataException e) {
			sessionBean.setMessage(e.getMessage());
			logger.error(e);
		}
	}
	
	private void processActionSave(ActionRequest request, ActionResponse response, ResolutionReportPortletSessionBean sessionBean, ExecuteOption sync) throws IOException, ServiceException, DataException {
		update(sessionBean, request, sync);
	}
	private JSONArray getResolutionsJSONData(ResolutionReportPortletSessionBean sessionBean) throws DataException, ServiceException {
		JSONArray resolutions = new JSONArray();
		
		List<Card> resolutionsCardList = sessionBean.getAllResolutions();
		Map<ObjectId, List<Card>> resolutionToReportsMaps = sessionBean.getResolutionToReportsMap();
		
		for(Card resolutionCard : resolutionsCardList) {
			JSONObject resolution = new JSONObject();
			List<Card> resolutionReports = resolutionToReportsMaps.get(resolutionCard.getId());
			
			try {
				resolution.put("resolution", getResolutionText(resolutionCard));
				if(null != resolutionReports) {
					JSONArray reports = new JSONArray();
					
					for(Card reportCard: resolutionReports) {
						String reportText = getReportDisplayText(reportCard, sessionBean);
						if(null == reportText) { 
							continue;
						}
						
						JSONObject report = new JSONObject();
						report.put("report", reportText);
						reports.put(report);
					}
					
					resolution.put("reports", reports);
				}
			} catch (JSONException e) {
				logger.error("JSON error", e);
				throw new DataException(e);
			}
			
			resolutions.put(resolution);
		}
		
		return resolutions;
	}
	
	private JSONArray getPreparedDocumentsJSONData(List<Card> preparedDocumentsCardList) throws DataException, ServiceException {
		JSONArray preparedDocuments = new JSONArray();		
		
		try {
			for(Card preparedDocumentCard : preparedDocumentsCardList) {
				JSONObject preparedDocument = new JSONObject();

				StringAttribute nameAttribute = (StringAttribute) preparedDocumentCard.getAttributeById(ATTR_NAME);
				preparedDocument.put("name", nameAttribute.getValue());
				preparedDocument.put("cardId", preparedDocumentCard.getId().getId().toString());

				preparedDocuments.put(preparedDocument);
			}
		} catch (JSONException e) {
			logger.error("JSON error", e);
			throw new DataException(e);
		}
		
		return preparedDocuments;
	}
	
	private Map<ObjectId, List<Card>> getResolutionToReportsMap(ResolutionReportPortletSessionBean sessionBean) throws DataException, ServiceException {
		Map<ObjectId, List<Card>> resolutionToReportMap = new HashMap<ObjectId, List<Card>>();
		
		List<Card> resolutionsList = sessionBean.getAllResolutions();
		
		for(Card resolutionCard : resolutionsList) {
			List<Card> resolutionReports = getResolutionReports(resolutionCard, sessionBean);
			resolutionToReportMap.put(resolutionCard.getId(), resolutionReports);
		}
		
		return resolutionToReportMap;
	}
	
	private List<Card> getAttachments(ResolutionReportPortletSessionBean sessionBean) throws DataException, ServiceException {
		List<Card> attachmentsList = new ArrayList<Card>();
		
		List<Card> resolutionsList = sessionBean.getAllResolutions();
		Map<ObjectId, List<Card>> resolutionToReportsMap = sessionBean.getResolutionToReportsMap();
		for(Card resolutionCard : resolutionsList) {
			List<Card> resolutionReports = resolutionToReportsMap.get(resolutionCard.getId());
			if(null == resolutionReports) {
				continue;
			}
			
			for(Card report : resolutionReports) {
				CardLinkAttribute reportAttachmentAttribute = 
					(CardLinkAttribute) report.getAttributeById(ATTR_REPORT_ATTACHMENTS);
				if(null == reportAttachmentAttribute) {
					continue;
				}
				for(Object attachmentId : reportAttachmentAttribute.getIdsLinked()) {
					Card attachment = (Card) sessionBean.getServiceBean().getById((ObjectId) attachmentId);
					attachmentsList.add(attachment);
				}
			}
		}
		
		CardAttrComparator comparator = new CardAttrComparator(ATTR_CREATED_DATE, false);
		Collections.sort(attachmentsList, comparator);
		
		return attachmentsList;
	}
	
	private List<Card> getLinkedCards(ResolutionReportPortletSessionBean sessionBean, ObjectId attributeId) 
				throws DataException, ServiceException {
		List<Card> cardsList = new ArrayList<Card>();
		
		CardLinkAttribute attribute = 
			sessionBean.getResolutionReportCard().getCardLinkAttributeById(attributeId);
		if(null != attribute) {
			Collection<ObjectId> cardIds = attribute.getIdsLinked();
			if(null != cardIds) {
				for(ObjectId cardId : cardIds) {
					try {
						Card attachment = (Card) sessionBean.getServiceBean().getById(cardId);
						cardsList.add(attachment);
					} catch(DataException da){
						logger.error("Can't get by id card " + cardId);
					}						
				}
			}
		}
		
		return cardsList;
	}
	
	private List<Card> getPreparedDocuments(ResolutionReportPortletSessionBean sessionBean) throws DataException, ServiceException {
		List<Card> preparedDocsList = new ArrayList<Card>();
		
		List<Card> resolutionsList = sessionBean.getAllResolutions();
		Map<ObjectId, List<Card>> resolutionToReportsMap = sessionBean.getResolutionToReportsMap();
		for(Card resolutionCard : resolutionsList) {
			List<Card> resolutionReports = resolutionToReportsMap.get(resolutionCard.getId());
			if(null == resolutionReports) {
				continue;
			}
			
			for(Card report : resolutionReports) {
				TypedCardLinkAttribute reportPreparedDocsAttribute = 
					(TypedCardLinkAttribute) report.getAttributeById(ATTR_PREPARED_DOCS);
				if(null == reportPreparedDocsAttribute) {
					continue;
				}
				for(Object preparedDocId : reportPreparedDocsAttribute.getIdsLinked()) {
					try {
						Card preparedDoc = (Card) sessionBean.getServiceBean().getById((ObjectId) preparedDocId);
						preparedDocsList.add(preparedDoc);
					} catch (DataException e) {
						// If access to the prepared document card is forbidden then just go forward
						if(!"general.access".equals(e.getMessageID())) {
							throw e;
						}
					}
				}
			}
		}
		
		CardAttrComparator comparator = new CardAttrComparator(ATTR_CREATED_DATE, false);
		Collections.sort(preparedDocsList, comparator);
		
		return preparedDocsList;
	}
	
	private List<Card> getReportPreparedDocuments(ResolutionReportPortletSessionBean sessionBean, PortletRequest request) throws DataException, ServiceException {
		List<Card> repPreparedDocs = getLinkedCards(sessionBean, ATTR_PREPARED_DOCS);

		if (repPreparedDocs.size() > 0) {
			CardAttrComparator comparator = new CardAttrComparator(ATTR_CREATED_DATE, false);
			Collections.sort(repPreparedDocs, comparator);
		}

		return repPreparedDocs;
	}
	
	private List<Card> getChildResolutions(ResolutionReportPortletSessionBean sessionBean) throws DataException, ServiceException {
		List<Card> childResolutions = new LinkedList<Card>(); 
		Card resolution = sessionBean.getResolutionCard();
		LinkAttribute childResolutionsAttribute = null;
		Collection<ObjectId> childResolutionIds = null;
		if (resolution.getTemplate().equals(TEMPLATE_INSTRUCTION)) {
			childResolutionsAttribute = (BackLinkAttribute) resolution.getAttributeById(ATTR_RIMP_RELASSIG);
			childResolutionIds = SearchUtils.getBackLinkedCardsObjectIds(resolution, ATTR_RIMP_RELASSIG, sessionBean.getServiceBean());
		}
		else if (resolution.getTemplate().equals(TEMPLATE_INDEPENDENT)) {
			childResolutionsAttribute = (BackLinkAttribute) resolution.getAttributeById(ATTR_IMPL_RESOLUT);
			childResolutionIds = SearchUtils.getBackLinkedCardsObjectIds(resolution, ATTR_IMPL_RESOLUT, sessionBean.getServiceBean());
		}
		
		if(null != childResolutionsAttribute && null != childResolutionIds && !childResolutionIds.isEmpty()) {
				for(ObjectId childResolutionId: childResolutionIds) {
					try {
						Card childResolution = (Card) sessionBean.getServiceBean().getById(childResolutionId);
						if(CARD_STATE_EXECUTION.equals(childResolution.getState()) || 
								CARD_STATE_DONE.equals(childResolution.getState())) {
							childResolutions.add(childResolution);
						}
					} catch (DataException e) {
						logger.error(e);
					}
				}
			}
		
		if(childResolutions.size() > 0) {
			CardAttrComparator comparator = new CardAttrComparator(ATTR_SIGN_DATE, false);
			Collections.sort(childResolutions, comparator);
		}
		
		return childResolutions;
	}
	
	private List<Card> getResolutionReports(Card resolutionCard, ResolutionReportPortletSessionBean sessionBean) throws DataException, ServiceException {
		List<Card> result = new LinkedList<Card>();
		
		if(CardLinkAttribute.class.isAssignableFrom(ATTR_REPORTS.getType())) {
		CardLinkAttribute resolutionReportsAttribute = resolutionCard.getCardLinkAttributeById(ATTR_REPORTS);
		extractReportsByAttribute(result, resolutionReportsAttribute, sessionBean);
		} else if(BackLinkAttribute.class.isAssignableFrom(ATTR_REPORTS.getType())) {
			List<ObjectId> backLinked = SearchUtils.getBackLinkedCardsObjectIds(resolutionCard, ATTR_REPORTS, sessionBean.getServiceBean());
			extractReportsByAttribute0(result, backLinked, sessionBean);
		}
		
		if(CardLinkAttribute.class.isAssignableFrom(ATTR_EXTERNAL_REPORTS.getType())) {
		CardLinkAttribute resolutionExternalReportsAttribute = resolutionCard.getCardLinkAttributeById(ATTR_EXTERNAL_REPORTS);
		extractReportsByAttribute(result, resolutionExternalReportsAttribute, sessionBean);
		} else if(BackLinkAttribute.class.isAssignableFrom(ATTR_EXTERNAL_REPORTS.getType())) {
			List<ObjectId> backLinked = SearchUtils.getBackLinkedCardsObjectIds(resolutionCard, ATTR_EXTERNAL_REPORTS, sessionBean.getServiceBean());
			extractReportsByAttribute0(result, backLinked, sessionBean);
		}
		
		if(!result.isEmpty()) {
			List<ObjectId> sortAttributes = new ArrayList<ObjectId>(2);
			sortAttributes.add(TMP_ATTR_REPORT_AUTHOR_NAME);
			sortAttributes.add(ATTR_CREATED_DATE);				
			Collections.sort(result, new CardAttributesComparator(sortAttributes, false));
		} else {
			result = null;
		}
		
		return result;
	}

    private void extractReportsByAttribute(List<Card> result, CardLinkAttribute reportsAttribute, 
                    ResolutionReportPortletSessionBean sessionBean) throws DataException, ServiceException {

        if(null != reportsAttribute) {
            Collection<ObjectId> reportsIds = reportsAttribute.getIdsLinked();
            extractReportsByAttribute0(result, reportsIds, sessionBean);
        }
	}

    private void extractReportsByAttribute0(List<Card> result, Collection<ObjectId> reportsIds, 
                    ResolutionReportPortletSessionBean sessionBean) throws DataException, ServiceException {
        
            if(null != reportsIds && !reportsIds.isEmpty()) {
    	
                //Load cards' states with permission checking before loading entire cards' data. 
                final Search searchAction = new Search();
                searchAction.getFilter().setCurrentUserRestrict(Search.Filter.CU_READ_PERMISSION);
                searchAction.setByCode(true);
                searchAction.setWords(ObjectIdUtils.numericIdsToCommaDelimitedString(reportsIds));
                Collection<Column> columns = new ArrayList<Column>();
                Column col = new Column();
                col.setAttributeId(Card.ATTR_STATE);
                columns.add(col);
                searchAction.setColumns(columns);

                SearchResult foundList = (SearchResult) sessionBean.getServiceBean().doAction(searchAction);
                
                //����� �������� ������� ���������� �� ����� �������
                //�.�. �������� ������� ����� ���� � ������� ���������� ������������ -> ����� �� ���� ���� ������� � ��������
                DataServiceBean dataService = new AsyncDataServiceBean();
                dataService.setAddress("127.0.0.1");
                dataService.setUser(new SystemUser());
                
                List<Card> cardList = foundList.getCards();
                for(Card card : cardList) {

                    if(!card.getId().equals(sessionBean.getResolutionReportCard().getId()) &&
                       !card.getState().equals(CARD_STATE_DRAFT) &&
                       !card.getState().equals(CARD_STATE_CANCELLED) &&
                       !card.getState().equals(CARD_STATE_TRASH) ) {

                        Card report = (Card) sessionBean.getServiceBean().getById(card.getId());

                        String reportAuthorName = null;
                        PersonAttribute reportAuthorAttribute = (PersonAttribute) report.getAttributeById(ATTR_REPORT_SIGN);
                        if(null != reportAuthorAttribute && null != reportAuthorAttribute.getPerson()) {
                            // Get the name of the report executor
                            Card personCard = (Card) dataService.getById(reportAuthorAttribute.getPerson().getCardId());
                            reportAuthorName = personCard.getAttributeById(ATTR_FIO).getStringValue();
                        } else {
                            // Get the name of the external executor
                            CardLinkAttribute externalExecutorAttribute =  
                                report.getCardLinkAttributeById(ATTR_REPORT_EXTERNAL_EXECUTOR);
                            Card externalExecutorCard = 
                                (Card) dataService.getById(externalExecutorAttribute.getSingleLinkedId());
                            reportAuthorName = externalExecutorCard.getAttributeById(ATTR_NAME).getStringValue();
                        }

                        StringAttribute reportAuthorNameAttribute = new StringAttribute();
                        reportAuthorNameAttribute.setId(TMP_ATTR_REPORT_AUTHOR_NAME);
                        reportAuthorNameAttribute.setValue(reportAuthorName);
                        report.getAttributes().add(reportAuthorNameAttribute);

                        result.add(report);
                    }
                }
                //������������� ������� �������� ������������
                //dataService.setUser(sessionBean.getRealUser());
            }
        }

	private String getResolutionText(Card resolutionCard) {
		TextAttribute resolutionTextAttribute = (TextAttribute) resolutionCard.getAttributeById(ATTR_RESOLUT);
		String resolution = resolutionTextAttribute.getValue();
		if(null != resolution && resolution.length() > 60) {
			resolution = resolution.substring(0, 60);
		}
		return resolution;
	}
	
	private String getReportDisplayText(Card reportCard, ResolutionReportPortletSessionBean sessionBean) throws DataException, ServiceException {
		TextAttribute reportTextAttribute = (TextAttribute) reportCard.getAttributeById(ATTR_REPORT_TEXT);
		String reportText = reportTextAttribute.getValue();
		if(null == reportText || reportText.length() == 0) {
			return null;
		}
		
		String reportAuthorFIO = reportCard.getAttributeById(TMP_ATTR_REPORT_AUTHOR_NAME).getStringValue();		
		return "(" + reportAuthorFIO + ", " + reportCard.getStateName().getValue() + ") " + reportText;
	}

	private ResolutionReportPortletSessionBean getSessionBean(PortletRequest request){
		PortletSession session = request.getPortletSession();
		ResolutionReportPortletSessionBean result = 
			(ResolutionReportPortletSessionBean) session.getAttribute(SESSION_BEAN, PortletSession.APPLICATION_SCOPE);
		if (result == null) {
			result = createSessionBean(request);
			session.setAttribute(SESSION_BEAN, result, PortletSession.APPLICATION_SCOPE);
		} else {
		    String userName = (String) request.getPortletSession().getAttribute(DataServiceBean.USER_NAME, PortletSession.APPLICATION_SCOPE);
            if (userName != null) {
                result.getServiceBean().setUser(new UserPrincipal(userName));
                result.getServiceBean().setIsDelegation(true);
                result.getServiceBean().setRealUser(request.getUserPrincipal());
            } else {
                result.getServiceBean().setUser(request.getUserPrincipal());
                result.getServiceBean().setIsDelegation(false);
            }
		}
		return result;
	}

	private ResolutionReportPortletSessionBean createSessionBean(PortletRequest request) {
		ResolutionReportPortletSessionBean sessionBean = new ResolutionReportPortletSessionBean();		
		AsyncDataServiceBean serviceBean = PortletUtil.createService(request);
		sessionBean.setDataServiceBean(serviceBean);	
		sessionBean.setRealUser(request.getUserPrincipal());
		
		sessionBean.setSwitchNavigatorLink(ARMUtils.retrieveSwitchNavigatorButton(request, serviceBean));
		sessionBean.setHeader(getResourceBundle(request.getLocale()).getString("header.main"));
		
		return sessionBean;
	}
	
	private void fillSessionBean(PortletRequest request, ResolutionReportPortletSessionBean sessionBean) throws DataException, ServiceException {
		final String backLink = request.getParameter(PARAM_BACK_URL);
		sessionBean.setBackLink(backLink);
		String backLinkSplitItem[] = StringUtils.hasText(backLink) ? backLink.split("item=") : null;
    	if(backLinkSplitItem != null && backLinkSplitItem.length > 1) {
        	String backLinkSplitAmp[] = backLinkSplitItem[1].split("&");
        	sessionBean.setMainCardid(backLinkSplitAmp[0].trim());
    	}
		
		DataServiceBean serviceBean = sessionBean.getServiceBean();
		
		final String reportGroupString = request.getParameter(REPORT_GROUP_PARAM);
		if(reportGroupString!=null){
			CreateCard createCardAction = new CreateCard(GROUP_REPORT_TEMPLATE_ID); 
			Card groupReport = (Card) serviceBean.doAction(createCardAction);
			sessionBean.setResolutionReportCard(groupReport);
			for(String stringId : reportGroupString.split("_")){
				ObjectId groupReportCardId = new ObjectId(Card.class, Long.parseLong(stringId));
				sessionBean.getGroupExecutionReports().add((Card)serviceBean.getById(groupReportCardId));
			}
			updateSessionBean(sessionBean, request);
		} else {
			String resolutionReportCardIdStr = request.getParameter(PARAM_EDIT_CARD_ID);
			if(null == resolutionReportCardIdStr || resolutionReportCardIdStr.trim().length() == 0) {
				return;
			}
			Long resolutionReportCardIdLong = Long.parseLong(resolutionReportCardIdStr);
			ObjectId resolutionReportCardId = new ObjectId(Card.class, resolutionReportCardIdLong);
			Card resolutionReportCard = (Card) serviceBean.getById(resolutionReportCardId);
			sessionBean.setResolutionReportCard(resolutionReportCard);
		
			sessionBean.setResolutionCard(getResolutionCard(resolutionReportCard, serviceBean));
			sessionBean.setChildResolutions(getChildResolutions(sessionBean));
			sessionBean.setResolutionToReportsMap(getResolutionToReportsMap(sessionBean));
			sessionBean.setAttachments(getAttachments(sessionBean));
			sessionBean.setPreparedDocuments(getPreparedDocuments(sessionBean));
		
			sessionBean.setBaseCardAttachedFiles(getParentCardAttachedFiles(request, sessionBean));
			sessionBean.setShortDescription(getShortDescription(sessionBean));
			sessionBean.setHeader(createHeader(request, sessionBean));
		
			updateSessionBean(sessionBean, request);
		}
	}
	
	private String getShortDescription(ResolutionReportPortletSessionBean sessionBean) throws DataException, ServiceException {
		String mainCardIdStr = sessionBean.getMainCardid();
		final ObjectId mainCardId = (mainCardIdStr != null && !mainCardIdStr.equals("")) ?
										new ObjectId(Card.class, Long.valueOf(mainCardIdStr))
										: null;
		if(mainCardId != null) {
			Card card = (Card) sessionBean.getServiceBean().getById(mainCardId);
			TextAttribute contentAttr = (TextAttribute) card.getAttributeById(ObjectId.predefined(TextAttribute.class, "jbr.document.title"));
			if(contentAttr != null)
				return contentAttr.getValue();
		}
		return null;
	}
	
	private Map<ObjectId, String> getParentCardAttachedFiles(PortletRequest request, ResolutionReportPortletSessionBean sessionBean) throws DataException, ServiceException {
		Map<ObjectId, String> attachedFilesMap = new LinkedHashMap<ObjectId, String>();
		String mainCardIdStr = sessionBean.getMainCardid();
		final ObjectId mainCardId = (mainCardIdStr != null && !mainCardIdStr.equals("")) ?
										new ObjectId(Card.class, Long.valueOf(mainCardIdStr))
										: null;
		if(mainCardId != null) {
			DataServiceBean serviceBean = sessionBean.getServiceBean();
			if(serviceBean.getRealUser() != null
					&& serviceBean.getRealUser().getPerson() != null) {
				final String userName = serviceBean.getUserName();
				serviceBean = new AsyncDataServiceBean();
				serviceBean.setAddress("127.0.0.1");
				serviceBean.setUser(new UserPrincipal(userName != null ? userName : "__system__"));
			}
			Card card = (Card) serviceBean.getById(mainCardId);
			CardLinkAttribute docLinks = card.getCardLinkAttributeById(ATTR_FILES);
			attachedFilesMap = ARMUtils.getAttachedFilesMap(serviceBean, docLinks);
		}
		
		return attachedFilesMap;
	}
	
	private String createHeader(PortletRequest request, ResolutionReportPortletSessionBean sessionBean) throws DataException, ServiceException {
		StringBuilder sb = new StringBuilder();
		sb.append(getResourceBundle(request.getLocale()).getString("header.main")).append(": ");
		String mainCardIdStr = sessionBean.getMainCardid();
		final ObjectId mainCardId = (mainCardIdStr != null && !mainCardIdStr.equals("")) ?
										new ObjectId(Card.class, Long.valueOf(mainCardIdStr))
										: null;
		if(mainCardId != null) {
			Card card = (Card) sessionBean.getServiceBean().getById(mainCardId);
			StringAttribute regnumAttr = (StringAttribute) card.getAttributeById(ObjectId.predefined(StringAttribute.class, "jbr.maindoc.regnum"));
			if(regnumAttr != null) {
				sb.append("�")
					.append(regnumAttr.getValue());
			}
			DateAttribute regDate = (DateAttribute) card.getAttributeById(ObjectId.predefined(DateAttribute.class, "jbr.maindoc.regdate"));
			if(regDate != null) {
				sb.append(" �� ")
					.append(regDate.getStringValue("dd.MM.yyyy"));
			}
		}
		return sb.toString();
	}
	
	private void updateSessionBean(ResolutionReportPortletSessionBean sessionBean, PortletRequest request) throws DataException, ServiceException {
		sessionBean.setReportText(getReportText(sessionBean));
		sessionBean.setReportAttachments(getLinkedCards(sessionBean, ATTR_REPORT_ATTACHMENTS));
		sessionBean.setReportPreparedDocs(getReportPreparedDocuments(sessionBean, request));
	}
	
	private String getReportText(ResolutionReportPortletSessionBean sessionBean) {
		TextAttribute reportTextAttribute = (TextAttribute) sessionBean.getResolutionReportCard().getAttributeById(ATTR_CURRENT_REPORT);
		String reportText = "";
		if (null != reportTextAttribute && null != reportTextAttribute.getValue()) {
			reportText = reportTextAttribute.getValue();
		}
		return reportText;
	}
	
	private Card getResolutionCard(Card resolutionReportCard, DataServiceBean serviceBean) throws DataException, ServiceException {
		CardLinkAttribute parentRes = resolutionReportCard.getCardLinkAttributeById(ATTR_RES_PARENT);
		
		Search search = new Search();
		search.setWords(parentRes.getLinkedIds());
		search.setByCode(true);

		final List<DataObject> templates = new ArrayList<DataObject>(2);
		templates.add(DataObject.createFromId(TEMPLATE_INSTRUCTION));
		templates.add(DataObject.createFromId(TEMPLATE_INDEPENDENT));
		search.setTemplates(templates);
		
		List<Card> resolutionCards = ((SearchResult) serviceBean.doAction(search)).getCards();
		
		if(null == resolutionCards || resolutionCards.isEmpty()) {
			String errorMessage = "Resolution report " + resolutionReportCard.getId().getId() + " doesn't have a resolution card";
			throw new ResolutionReportPortletException(errorMessage);	
		}
		
		if(resolutionCards.size() > 1) {
			String errorMessage = "Resolution report " + resolutionReportCard.getId().getId() + " has multiple resolution cards";
			throw new ResolutionReportPortletException(errorMessage);	
		}
		
		Card resolutionCard = resolutionCards.get(0);
		resolutionCard = (Card) serviceBean.getById(resolutionCard.getId());
		return resolutionCard;
	}

	public static QuickResolutionPortletSessionBean getSessionBean(
			HttpServletRequest request, String namespace) {
		HttpSession session = request.getSession();
		String key = getApplicationSessionBeanKey(namespace);
		return (QuickResolutionPortletSessionBean) session.getAttribute(key);
	}

	private static String getApplicationSessionBeanKey(String namespace) {
		return APPLICATION_SESSION_BEAN_PREFIX + namespace;
	}

	// ���������� �������� ��� ���������� � ������������
	private void update(ResolutionReportPortletSessionBean sessionBean, ActionRequest request, ExecuteOption sync) throws DataException, ServiceException {		
		updateReportCardFromRequest(sessionBean, request);				
		
		Card reportCard = sessionBean.getResolutionReportCard();
		DataServiceBean serviceBean = sessionBean.getServiceBean();
		
		serviceBean.doAction(new LockObject(reportCard));
		try {
			((AsyncDataServiceBean) serviceBean).saveObject(reportCard, sync);
			updateSessionBean(sessionBean, request);
		} finally {
			serviceBean.doAction(new UnlockObject(reportCard.getId()));
		}
	}
	
	private void updateReportCardFromRequest(ResolutionReportPortletSessionBean sessionBean, ActionRequest request) {
		Card reportCard = sessionBean.getResolutionReportCard();
		
		String reportText = request.getParameter("reportText");
		if(null != reportText && reportText.trim().length() > 0) {
			// on this branch report text should be inserted in TextAttribute '������� �����',
			// that then should be copied in '�����' by respective processor
			
//			HtmlAttribute reportHtmlAttribute = (HtmlAttribute) reportCard.getAttributeById(ATTR_REPORT_HTML);
//			if(null == reportHtmlAttribute) {
//				reportHtmlAttribute = new HtmlAttribute();
//				reportHtmlAttribute.setId((String) ATTR_REPORT_HTML.getId());
//				reportHtmlAttribute.setValue(reportText);
//				reportCard.getAttributes().add(reportHtmlAttribute);
//			} else {
//				reportHtmlAttribute.setValue(reportText);
//			}
			
			TextAttribute reportAttribute = ((TextAttribute) reportCard.getAttributeById(ATTR_CURRENT_REPORT));
			if(null == reportAttribute) {
				reportAttribute = new TextAttribute();
				reportAttribute.setId((String) ATTR_CURRENT_REPORT.getId());
				reportAttribute.setValue(reportText);
				reportCard.getAttributes().add(reportAttribute);
			}
			reportAttribute.setValue(reportText);
		}
		
		CardLinkAttribute attachmentsAttribute = (CardLinkAttribute) reportCard.getAttributeById(ATTR_REPORT_ATTACHMENTS);
		if(null == attachmentsAttribute) {
			attachmentsAttribute = new CardLinkAttribute();
			attachmentsAttribute.setId((String) ATTR_REPORT_ATTACHMENTS.getId());
			reportCard.getAttributes().add(attachmentsAttribute);
		}
		attachmentsAttribute.clear();
		
		String[] attachmentIds = request.getParameterValues("materialId");
		if(null != attachmentIds) {
			for(String attachmentId : attachmentIds) {
				long attachmentIdLong = Long.parseLong(attachmentId);
				attachmentsAttribute.addLinkedId(attachmentIdLong);
			}
		}
		
		TypedCardLinkAttribute preparedDocsAttribute = (TypedCardLinkAttribute) reportCard.getAttributeById(ATTR_PREPARED_DOCS);
		if(null == preparedDocsAttribute) {
			preparedDocsAttribute = new TypedCardLinkAttribute();
			preparedDocsAttribute.setId((String) ATTR_PREPARED_DOCS.getId());
			reportCard.getAttributes().add(preparedDocsAttribute);
		}
		preparedDocsAttribute.clear();
		
		String[] preparedDocIds = request.getParameterValues("preparedDocId");
		if(null != preparedDocIds) {
			for(String preparedDocId : preparedDocIds) {
				long preparedDocIdIdLong = Long.parseLong(preparedDocId);
//				preparedDocsAttribute.addLinkedId(preparedDocIdIdLong);
				preparedDocsAttribute.addType(preparedDocIdIdLong, REF_VAL_IN_RSPONSE_ID);
			}
		}
	}
	
	private void doExit(PortletRequest request, ActionResponse response, ResolutionReportPortletSessionBean sessionBean) throws IOException {
		doExit(request,response,sessionBean, true);
	}
	
	private void doExit(PortletRequest request, ActionResponse response, ResolutionReportPortletSessionBean sessionBean, boolean removeSessionBeanAttr)
			throws IOException {
		if(null == sessionBean.getBackLink()) {
			return;
		}
		
		PortletSession session = request.getPortletSession();
		String namespace = request.getParameter(FIELD_NAMESPACE);
		if(removeSessionBeanAttr){
			session.removeAttribute(SESSION_BEAN, PortletSession.APPLICATION_SCOPE);
		}
		session.removeAttribute(getApplicationSessionBeanKey(namespace),
				PortletSession.APPLICATION_SCOPE);
		
		session.removeAttribute(CardPortlet.SESSION_BEAN);
		session.removeAttribute(CardPortlet.SESSION_BEAN + "." + namespace, PortletSession.APPLICATION_SCOPE);
		
		response.sendRedirect(sessionBean.getBackLink());
	}

	private void unlockQuickly(ObjectId objectId, DataServiceBean serviceBean) {
		try {
			serviceBean.doAction(new UnlockObject(objectId));
		} catch (Exception e) {
			logger.error("Couldn't unlock object " + objectId.toString(), e);
		}
	}
	
	private JSONArray getCroupResolutionsJSONDate(
			ResolutionReportPortletSessionBean sessionBean) {
		JSONArray array = new JSONArray();
		for(Card report: sessionBean.getGroupExecutionReports()){
			JSONObject object = new JSONObject();
			try {
				object.put("id", report.getId().getId());
				object.put("name", report.getAttributeById(ATTR_NAME).getStringValue());
				array.put(object);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return array;
	}

	
	private CardPortletSessionBean createCardPortletSessionBean(RenderRequest request, RenderResponse response, 
				ResolutionReportPortletSessionBean sessionBean) {
		CardPortletSessionBean cardPortletSessionBean = CardPortlet.getSessionBean(request);
		if(null != cardPortletSessionBean) {
			return cardPortletSessionBean;
		}
		
		PortletSession session = request.getPortletSession(true);
		cardPortletSessionBean = new CardPortletSessionBean();
		cardPortletSessionBean.setDataServiceBean(sessionBean.getServiceBean());
		
		CardPortletCardInfo cardInfo = new CardPortletCardInfo();
		cardInfo.setCard(sessionBean.getResolutionReportCard());
		cardInfo.setMode(CardPortlet.CARD_EDIT_MODE);
		cardPortletSessionBean.setActiveCardInfo(cardInfo);
		
		//initDoclinkCreateData( sessionBean);
		session.setAttribute(CardPortlet.SESSION_BEAN, cardPortletSessionBean);
		session.setAttribute(CardPortlet.SESSION_BEAN + "." + response.getNamespace(), 
				cardPortletSessionBean, PortletSession.APPLICATION_SCOPE);
		
		return cardPortletSessionBean;
	}
	
	protected String getMessage(PortletRequest request, String key) {
		return getPortletConfig().getResourceBundle(request.getLocale()).getString(key);
	}
	
	protected void setReportText(PortletRequest request, ResolutionReportPortletSessionBean sessionBean) {
		Card reportCard = sessionBean.getResolutionReportCard();
    	String reportText = request.getParameter("reportText");
		if(null != reportText && reportText.trim().length() > 0) {
			HtmlAttribute reportHtmlAttribute = (HtmlAttribute) reportCard.getAttributeById(ATTR_REPORT_HTML);
			if(null == reportHtmlAttribute) {
				reportHtmlAttribute = new HtmlAttribute();
				reportHtmlAttribute.setId((String) ATTR_REPORT_HTML.getId());
				reportHtmlAttribute.setValue(reportText);
				reportCard.getAttributes().add(reportHtmlAttribute);
			} else {
				reportHtmlAttribute.setValue(reportText);
			}
		}
	}
}
