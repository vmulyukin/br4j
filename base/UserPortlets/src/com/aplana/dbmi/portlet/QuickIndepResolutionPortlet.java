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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Matcher;

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

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.CreateCard;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.card.util.ARMUtils;
import com.aplana.dbmi.card.util.SearchUtils;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.HtmlAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.MaterialAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.Role;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.SystemRole;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.TextAttribute;
import com.aplana.dbmi.model.TypedCardLinkAttribute;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.model.filter.PersonCardIdFilter;
import com.aplana.dbmi.service.AsyncDataServiceBean;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.PortletUtil;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.dbmi.util.JspUtils;
import com.aplana.web.tag.util.StringUtils;

public class QuickIndepResolutionPortlet extends GenericPortlet {
	private Log logger = LogFactory.getLog(getClass());

	public static final String SESSION_BEAN = "quickIndepResolutionPortletSessionBean";
	public static final String APPLY_DS = "MI_APPLY_SIGNATURE";
	public static final String DS_PARAMS = "MI_SIGNATURE_PARAMS";
	private static final String APPLICATION_SESSION_BEAN_PREFIX = "quickResolutionPortletSessionBean:";

	public static final String CONFIG_FOLDER = "dbmi/card/";
	public static final String JSP_FOLDER = "/WEB-INF/jsp/quickResolution/";
	public static final String MODE_SELECT_EXECUTORS = "selectExecutors";
	public static final String MODE_EDIT_RESOLUTION = "editResolution";
	public static final String MODE_VISA_SIGN_RESOLUTION = "visaSignResolution";

	public static final String ACTION_CANCEL = "cancel";
	public static final String ACTION_DONE = "done";
	public static final String ACTION_UPLOAD = "upload";
	public static final String ACTION_ADD_LINKED_FILES = "addLinkedFiles";
	public static final String ACTION_INIT_INDEP_RES = "initIndepRes";
	public static final String ACTION_SAVE_INDEP_RES = "saveIndepRes";
	public static final String ACTION_SUBMIT_INDEP_RES = "submitIndepRes";

	public static final String STATE_INIT_CREATE = "initCreate";
	public static final String STATE_INIT_EDIT = "initEdit";

	public static final String PARAM_STATE_INIT = "stateInit";
	
	private static final String PARAM_IS_CARD_LINKED = "isCardLinked";


	public static final String MATERIAL_UPLOAD_URL = "/DBMI-UserPortlets/servlet/arm-upload";
	public static final String PARAM_BACK_URL = "backURL";
	public static final String PARAM_EDIT_CARD_ID = "editCardId";
	public static final String PARAM_DONE_URL = "doneURL";
	public static final String PARAM_WORKFLOW_MOVE = "workflowMove";
	public static final String PARAM_SIGNATURE = "signature";

	public static final String FIELD_ACTION = "formAction";
	public static final String FIELD_RESPONSIBLE_EXECUTOR = "responsibleExecutor";
	public static final String FIELD_ADDITIONAL_EXECUTORS = "additionalExecutors";
	public static final String FIELD_RESOLUTION_ATTACHMENTS = "resolutionAttachments";

	public static final String FIELD_NAMESPACE = "namespace";
	public static final String FIELD_TEXT_RESOLUTION = "textResolution";
	public static final String FIELD_TERM = "term";
	public static final String FIELD_CONTROL_TERM = "controlTerm";
	public static final String FIELD_ON_CONTROL = "onControl";
	public static final String FIELD_CONTROLLERS = "controllers";
	public static final String FIELD_SIGNATURE = "signature";
	public static final String FIELD_FILE_ATTACHMENT = "fileAttachment";
	
	public static final String KEY_CREATE_RES = "header.createResolution";
	public static final String KEY_EDIT_RES = "header.editResolution";

	public static final String PARAM_LINK_TO_CARD = "linkToCard";
	
	// �������� ������� "���������"
	public static final ObjectId ATTR_EXECUTOR = ObjectId.predefined(
			PersonAttribute.class, "jbr.AssignmentExecutor");
	public static final ObjectId ATTR_COEXECUTORS = ObjectId.predefined(
			PersonAttribute.class, "jbr.CoExecutor");
	public static final ObjectId ATTR_RESOLUT = ObjectId.predefined(
			TextAttribute.class, "jbr.resolutionText");
	public static final ObjectId ATTR_TERM = ObjectId.predefined(
			DateAttribute.class, "jbr.incoming.deadline");
	public static final ObjectId ATTR_TCON_INSPECTOR = ObjectId.predefined(
			PersonAttribute.class, "jbr.incoming.inspector");
	public static final ObjectId ATTR_TCON_ONCONT = ObjectId.predefined(
			ListAttribute.class, "jbr.incoming.oncontrol");
	public static final ObjectId ATTR_FYI = ObjectId.predefined(
			PersonAttribute.class, "jbr.Fyi");
	public static final ObjectId ATTR_SIGNATURE = ObjectId.predefined(
			HtmlAttribute.class, "jbr.uzdo.signature");
	public static final ObjectId ATTR_SIGNATORY = ObjectId.predefined(
			PersonAttribute.class, "jbr.resolution.FioSign");
	public static final ObjectId ATTR_DOCLINKS = ObjectId.predefined(
			CardLinkAttribute.class, "jbr.files");
	public static final ObjectId ATTR_VISA_ATTACHMENTS = ObjectId.predefined(
			CardLinkAttribute.class, "jbr.visa.attachments");
	public static final ObjectId JBR_REGD_REGNUM = ObjectId.predefined(
			StringAttribute.class, "regnumber");
	
	public static final ObjectId WFM_SEND_ON_EXECUTION = ObjectId.predefined(
			WorkflowMove.class, "jbr.independent.resolution.sign.on_execution");
	public static final ObjectId WFM_SEND_FOR_SINGING = ObjectId.predefined(
			WorkflowMove.class, "jbr.independent.resolution.draft.sign");
	public static final ObjectId CARDSTATE_DRAFT = ObjectId.predefined(
			CardState.class, "draft");
	public static final ObjectId CARDSTATE_SIGN = ObjectId.predefined(
			CardState.class, "sign");
	


	public static final long VALUE_YES_ON_TCON_ONCONT = 1432;
	public static final long VALUE_NO_ON_TCON_ONCONT = 1433;

	// �������� ������� "��������� ��� ������������"
	public static final ObjectId ATTR_ARM_MANAGER = ObjectId.predefined(
			PersonAttribute.class, "jbr.arm.manager");
	public static final ObjectId ATTR_CONTROLLER = ObjectId.predefined(
			PersonAttribute.class, "jbr.arm.inspector");
	public static final ObjectId ATTR_ARM_RESOL_LIST = ObjectId.predefined(
			CardLinkAttribute.class, "jbr.arm.resolutionList");
	public static final ObjectId ATTR_ARM_VISA_LIST = ObjectId.predefined(
			CardLinkAttribute.class, "jbr.arm.visaList");
	public static final ObjectId ATTR_COMPANY = ObjectId.predefined(
			PersonAttribute.class, "jbr.arm.company");
	public static final ObjectId ATTR_RESOLUTION_ON_CONTROL = ObjectId
			.predefined(ListAttribute.class, "jbr.arm.resolutionOnControl");
	public static final ObjectId ATTR_TYPE_RESOLUTION = ObjectId.predefined(
			TextAttribute.class, "jbr.typeResolution.resolution");
	public static final ObjectId TEMPLATE_RESOLUTION = ObjectId.predefined(
			Template.class, "jbr.resolution");
	public static final ObjectId TEMPLATE_INDEPENDENT_RESOLUTION = ObjectId
			.predefined(Template.class, "jbr.independent.resolution");
	public static final ObjectId TEMPLATE_PERSONAL_CONTROL = ObjectId
			.predefined(Template.class, "jbr.boss.control");
	public static final ObjectId TEMPLATE_PERSON = ObjectId.predefined(
			Template.class, "jbr.internalPerson");
	public static final ObjectId TEMPLATE_EXT_PERSON = ObjectId.predefined(
			Template.class, "jbr.externalPerson");
	public static final ObjectId CARDSTATE_ACTIVE_USER = ObjectId.predefined(
			CardState.class, "user.active");
	public static final ObjectId DICTIONARY_NEW = ObjectId.predefined(
			CardState.class, "dictionaryNew");
	public static final ObjectId ATTR_RES_SIGNIMG = ObjectId.predefined(
			HtmlAttribute.class, "boss.settings.image1");
	public static final ObjectId ATTR_PCON_PERSON = ObjectId.predefined(
			PersonAttribute.class, "jbr.boss.control.inspector");
	public static final ObjectId ATTR_PCON_DOC = ObjectId.predefined(
			CardLinkAttribute.class, "jbr.boss.control.document");
	public static final ObjectId ATTR_PCON_DATE = ObjectId.predefined(
			DateAttribute.class, "jbr.boss.control.date");
	public static final ObjectId ATTR_NAME = ObjectId.predefined(
			StringAttribute.class, "name");
	
	private static final String PARAM_LINKED_DOC = "PARAM_LINKED_DOC";
	//��������� ��������
	public static final ObjectId LINKED_DOC = ObjectId.predefined(TypedCardLinkAttribute.class, "jbr.relatdocs");
	//��� �����
	private static final Long LINK_TYPE = (Long) ObjectId.predefined(ReferenceValue.class, "jbr.inResponse").getId();
	
	// ����
	public static final ObjectId ROLE_MINISTR = ObjectId.predefined(
			SystemRole.class, "jbr.minister");

	public static final String SEPARATOR = "#separator#";
	public static final String ID_DELIMITER = "#id_delim#";

	@Override
	protected void doView(RenderRequest request, RenderResponse response)
			throws PortletException, PortletSecurityException, IOException {
		final QuickIndepResolutionPortletSessionBean sessionBean = getSessionBean(request);

		response.setContentType("text/html");
		//
		if (sessionBean.getPortletFormManager()
				.processRender(request, response)) {
			return;
		}

		String key = getApplicationSessionBeanKey(response.getNamespace());
		PortletSession session = request.getPortletSession();
		session.setAttribute(key, sessionBean, PortletSession.APPLICATION_SCOPE);
		//
		final String page;

		String message = "'";
		if (sessionBean.getMessage() != null) {
			message += sessionBean.getMessage();
			sessionBean.setMessage(null);
		}
		message += "'";
		request.setAttribute("message", message);

		page = "indepResolutionData.jsp";
		
		fillRequestAttrsFromSessionBean(request,sessionBean);

		PortletRequestDispatcher rd = getPortletContext().getRequestDispatcher(
				JSP_FOLDER + page);
		rd.include(request, response);
	}

	private JSONArray getAttachmentsJSONData(
			Map<ObjectId, String> attachmentsMap) throws PortletException {
		JSONArray attachments = new JSONArray();

		if (attachmentsMap != null) {
			try {
				for (ObjectId attachmentId : attachmentsMap.keySet()) {
					JSONObject attachment = new JSONObject();

					attachment.put("name", attachmentsMap.get(attachmentId));
					attachment.put("cardId", attachmentId.getId().toString());

					attachments.put(attachment);
				}
			} catch (JSONException e) {
				logger.error("JSON error", e);
				throw new PortletException(e);
			}
		}

		return attachments;
	}

	@Override
	public void processAction(ActionRequest request, ActionResponse response)
			throws PortletException, PortletSecurityException, IOException {

		QuickIndepResolutionPortletSessionBean sessionBean = null;
		String action = request.getParameter(FIELD_ACTION);
		final ResourceBundle resource = getPortletConfig().getResourceBundle(request.getLocale());

		// TODO: double check for action being equal to ACTION_INIT_INDEP_RES,
		// not good
		if (ACTION_INIT_INDEP_RES.equals(action)) {
			PortletSession session = request.getPortletSession();
			// in order to create new sessionBean remove the old one if existed
			session.removeAttribute(SESSION_BEAN);
		}
		sessionBean = getSessionBean(request);

		// What's this???
		if (sessionBean.getPortletFormManager().processAction(request, response)) {
			return;
		}

		AsyncDataServiceBean serviceBean = PortletUtil.createService(request);

		if (ACTION_INIT_INDEP_RES.equals(action)) {
			final String backLink = request.getParameter(PARAM_BACK_URL);
			sessionBean.setBackUrl(backLink);
			String doneLink = request.getParameter(PARAM_DONE_URL);
			if (doneLink == null) {
				doneLink = backLink;
			}
			sessionBean.setBackUrl(backLink);
			sessionBean.setDoneUrl(doneLink);

			Card resolutionCard = null;
			String state = request.getParameter(PARAM_STATE_INIT);
			if (STATE_INIT_EDIT.equals(state)) {
				String stringCardId = request.getParameter(PARAM_EDIT_CARD_ID);
				if (stringCardId == null || stringCardId.trim().equals("")) {
					doExit(request, response, sessionBean, sessionBean.getBackUrl());
				}
				ObjectId cardId = new ObjectId(Card.class, Long.valueOf(stringCardId));

				// try to lock card, redirect back if fails
				try {
					serviceBean.doAction(new LockObject(cardId));
				} catch (Exception e) {
					logger.error("Couldn't lock card " + cardId + ": " + e);
					doExit(request, response, sessionBean, sessionBean.getBackUrl());
				}
				// get the card
				try {
					resolutionCard = (Card) serviceBean.getById(cardId);
				} catch (Exception e) {
					logger.error("Error while fetching resolution card: " + e);
				}
				sessionBean.setResolutionCard(resolutionCard);
				initBeanFromResolutionCard(request, sessionBean);
				sessionBean.setControlTerm(fetchPersonalControlTerm(sessionBean, serviceBean));
				sessionBean.setHeader((resource != null && resource.getString(KEY_EDIT_RES) != null ) ? resource.getString(KEY_EDIT_RES) : "");
			} else { // else is STATE_INIT_CREATE
				final String isLinked = request.getParameter(PARAM_IS_CARD_LINKED);
				if(isLinked != null && !"".equals(isLinked))
					sessionBean.setCardLinked(true);
				final String paramLink = request.getParameter(PARAM_LINK_TO_CARD);
				if(paramLink != null && !paramLink.isEmpty()) {
					Long linkedDocId = Long.parseLong(paramLink);
					request.getPortletSession().setAttribute(PARAM_LINKED_DOC, linkedDocId);
				}				
				resolutionCard = createIndepResolutionCard(serviceBean);
				sessionBean.setResolutionCard(resolutionCard);
				sessionBean.setHeader((resource != null && resource.getString(KEY_CREATE_RES) != null ) ? resource.getString(KEY_CREATE_RES) : "");
			}
			sessionBean.setResolutionCard(resolutionCard);
		} else if (ACTION_SAVE_INDEP_RES.equals(action)) {
			parseFormValuesIntoSessionBeanFromRequest(request, sessionBean);
			// savePersonalControlCard(sessionBean, serviceBean); /* Commented in terms of BR4J00029569 fix*/
			setResolutionAttributes(sessionBean, serviceBean);
			try {
				ObjectId resolutionId = serviceBean.saveObject(sessionBean
						.getResolutionCard());
				sessionBean.setResolutionCard((Card)serviceBean.getById(resolutionId));
				unlockQuickly(sessionBean.getResolutionCard().getId(), serviceBean);
				fillBackUrl(request, sessionBean);
				doExit(request, response, sessionBean, sessionBean.getBackUrl());
			} catch (Exception e) {
				logger.error("Error while saving resolution card:", e);
				String msg = e.getMessage();
				if(msg != null)
					msg = msg.replaceAll("'", "\"");
				sessionBean.setMessage(msg);
			}
			if(sessionBean.getControlTerm() != null){
				savePersonalControlCard(sessionBean, serviceBean);
			}
		} else if (ACTION_SUBMIT_INDEP_RES.equals(action)) {
			parseFormValuesIntoSessionBeanFromRequest(request, sessionBean);
			setResolutionAttributes(sessionBean, serviceBean);
			try {
				Card card = sessionBean.getResolutionCard();
				card.setId(serviceBean.saveObject(card));
				sessionBean.setResolutionCard((Card)serviceBean.getById(card.getId()));
				//a personal control card is created only if a control term date attribute is filled
				if(sessionBean.getControlTerm() != null)
					savePersonalControlCard(sessionBean, serviceBean);				WorkflowMove wfm = null;
				if (CARDSTATE_SIGN.equals(card.getState())) {
					wfm = (WorkflowMove) serviceBean.getById(WFM_SEND_ON_EXECUTION);
				} else if (CARDSTATE_DRAFT.equals(card.getState())){
					wfm = (WorkflowMove) serviceBean.getById(WFM_SEND_FOR_SINGING);
				} else {
					throw new IllegalStateException("Can't move card from state " + card.getState().getId());
				}
				ChangeState sendOnExecution = new ChangeState();
				sendOnExecution.setCard(card);
				sendOnExecution.setWorkflowMove(wfm);
				serviceBean.doAction(sendOnExecution);
			
				unlockQuickly(sessionBean.getResolutionCard().getId(), serviceBean);
				fillBackUrl(request, sessionBean);
				doExit(request, response, sessionBean, sessionBean.getBackUrl());
			} catch (Exception e) {
				logger.error("Error while moving card " + sessionBean.getResolutionCard().getId() 
						+ " via workflow move " + WFM_SEND_ON_EXECUTION.getId() + " " + e);
				String msg = e.getMessage();
				if(msg != null)
					msg = msg.replaceAll("'", "\"");
				sessionBean.setMessage(msg);
			}
		} else if (ACTION_CANCEL.equals(action)) {
			// TODO unlock here? Yes
			unlockQuickly(sessionBean.getResolutionCard().getId(), serviceBean);
			doExit(request, response, sessionBean, sessionBean.getBackUrl());
		}
	}
	
	
	private void fillRequestAttrsFromSessionBean(PortletRequest request,
			QuickIndepResolutionPortletSessionBean sessionBean) 
			throws IllegalArgumentException, PortletException {

		final JSONObject responsible = new JSONObject();
		try {

			responsible.put("cardId", sessionBean.getResponsibleId());
			responsible.put("name", sessionBean.getResponsibleName());
		} catch (JSONException e) {
			logger.error("JSON exception caught:", e);
		}
		request.setAttribute("responsible", responsible);

		final JSONArray additionals = new JSONArray();
		try {
			final Map<Long, String> map = sessionBean.getAdditionals();
			if (map != null) {
				for (Long cardId : map.keySet()) {
					final JSONObject j = new JSONObject();
					j.put("cardId", cardId);
					j.put("name", map.get(cardId));
					additionals.put(j);
				}
			}
		} catch (JSONException e) {
			logger.error("JSON exception caught:", e);
		}

		final JSONArray controllers = new JSONArray();
		try {
			final Map<Long, String> map = sessionBean.getControllers();
			if (map != null) {
				for (final Long cardId : map.keySet()) {
					final JSONObject j = new JSONObject();
					j.put("cardId", cardId);
					j.put("name", map.get(cardId));
					controllers.put(j);
				}
			}
		} catch (JSONException e) {
			logger.error("JSON exception caught:", e);
		}

		request.setAttribute("additionals", additionals);
		request.setAttribute("controllers", controllers);
		request.setAttribute(FIELD_RESOLUTION_ATTACHMENTS,
				getAttachmentsJSONData(sessionBean.getAttachedFiles()));		
	}
	

	private void parseFormValuesIntoSessionBeanFromRequest(
			ActionRequest request,
			QuickIndepResolutionPortletSessionBean sessionBean) {

		String p = request.getParameter(FIELD_RESPONSIBLE_EXECUTOR);
		if (p != null) {
			if (!"".equals(p.trim())) {
				final String[] value = p.split(":");
				sessionBean.setResponsible(Long.valueOf(value[0]), value[1]);
			} else 
				sessionBean.resetResponsible();

		}
		p = request.getParameter(FIELD_ADDITIONAL_EXECUTORS);
		if (p != null) {
			final Map<Long, String> additionals = new LinkedHashMap<Long, String>();
			if (!"".equals(p.trim())) {
				final String[] peoples = p.split(SEPARATOR);
				for (int i = 0; i < peoples.length; i++) {
					final String[] value = peoples[i].split(":");
					additionals.put(Long.valueOf(value[0]), value[1]);
				}
			}
			sessionBean.setAdditionals(additionals);
		}
		p = request.getParameter(FIELD_FILE_ATTACHMENT);
		if (p != null) {
			final Map<ObjectId, String> attachments = new LinkedHashMap<ObjectId, String>();
			if (!"".equals(p.trim())) {
				final String[] files = p.split(SEPARATOR);
				for (int i = 0; i < files.length; i++) {
					final String[] value = files[i].split(ID_DELIMITER);
					ObjectId fileId = new ObjectId(Card.class,
							Long.valueOf(value[0]));
					attachments.put(fileId, value[1]);
				}
			}
			sessionBean.setAttachedFiles(attachments);
		}

		String textResolution = request.getParameter(FIELD_TEXT_RESOLUTION);
		if (textResolution != null && !textResolution.trim().equals("")) {
			sessionBean.setResolutionText(textResolution);
		}

		// �� ������ ��������
		String controlTerm = request.getParameter(FIELD_CONTROL_TERM);
		if (controlTerm != null && !controlTerm.trim().equals("")) {
			try {
				Date termDate = (new SimpleDateFormat("yyyy-MM-dd"))
						.parse(controlTerm);
				sessionBean.setControlTerm(termDate);
			} catch (Exception e) {
				logger.error("Bad format date of term person control:", e);
			}
		} else {
			sessionBean.setControlTerm(null);
		}
		// ����
		String term = request.getParameter(FIELD_TERM);
		if (term != null && !term.trim().equals("")) {
			try {
				// term date should be after current time (checked in the
				// preprocessor on resolution save),
				// so it should be set to the end of the day
				Date termDate = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"))
						.parse(term + " 23:59:59");
				sessionBean.setTerm(termDate);
			} catch (Exception e) {
				logger.error("Bad format date of term:", e);
			}
		} else {
			sessionBean.setTerm(null);
		}

		// �� ��������
		String onControl = request.getParameter(FIELD_ON_CONTROL);
		if (onControl != null) {
			sessionBean.setIsOnControl(JspUtils.getChkBoxValue(onControl));
		}

		// �����������
		String controllers = request.getParameter(FIELD_CONTROLLERS);
		if (controllers != null) {
			final Map<Long, String> controllersMap = new LinkedHashMap<Long, String>();
			if (!"".equals(controllers.trim())) {
				final String[] peoples = controllers.split(SEPARATOR);
				for (int i = 0; i < peoples.length; i++) {
					final String[] value = peoples[i].split(":");
					controllersMap.put(Long.valueOf(value[0]), value[1]);
				}
			}
			sessionBean.setControllers(controllersMap);
		}
		// �������
		String signature = request.getParameter(FIELD_SIGNATURE);
		if (signature != null && !signature.trim().equals("")) {
			sessionBean.setSignature(signature);
		}

		Long linkedDocId = (Long) request.getPortletSession().getAttribute(PARAM_LINKED_DOC);
		if(linkedDocId != null) {
			sessionBean.setLinkedDoc(new ObjectId(Card.class, linkedDocId));
		}
	}

	private Card createIndepResolutionCard(DataServiceBean serviceBean) {
		Card card = null;
		CreateCard createCard = new CreateCard(TEMPLATE_INDEPENDENT_RESOLUTION);
		try {
			card = (Card) serviceBean.doAction(createCard);
		} catch (Exception e) {
			logger.error("Error while creating card: " + e);
		}
		return card;
	}

	private QuickIndepResolutionPortletSessionBean getSessionBean(
			PortletRequest request) {
		PortletSession session = request.getPortletSession();
		QuickIndepResolutionPortletSessionBean result = (QuickIndepResolutionPortletSessionBean) session
				.getAttribute(SESSION_BEAN);
		if (result == null) {
			result = createSessionBean(request);
			session.setAttribute(SESSION_BEAN, result);
		}
		return result;
	}

	public static QuickIndepResolutionPortletSessionBean getPortletSessionBean(
			PortletRequest request) {
		PortletSession session = request.getPortletSession();
		QuickIndepResolutionPortletSessionBean result = (QuickIndepResolutionPortletSessionBean) session
				.getAttribute(SESSION_BEAN);
		if (result == null) {
			return null;
		}
		return result;
	}

	private QuickIndepResolutionPortletSessionBean createSessionBean(
			PortletRequest request) {
		QuickIndepResolutionPortletSessionBean result = new QuickIndepResolutionPortletSessionBean();
		// result.setMode(MODE_SELECT_EXECUTORS);

		DataServiceBean serviceBean = PortletUtil.createService(request);

		// �������� �������� "��������� ��� ��� ������������"
		Card armCard = ARMUtils.getArmSettings(serviceBean);

		// �������� ������� ���������� �� �������� �������������� ����������
		// "��������� ��� ��� ������������"
		// --------------------------------------------------------------
		final Set<Long> controllerIds = new HashSet<Long>();
		// final PersonAttribute attrPer = (PersonAttribute)
		// armCard.getAttributeById(ATTR_CONTROLLER);
		try {
			final Collection<Person> persons = SearchUtils.getAttrPersons(
					armCard, ATTR_CONTROLLER, false);
			if (persons != null) {
				for (Person person : persons) {
					try {
						final Person contrPer = (Person) serviceBean
								.getById(new ObjectId(Person.class, person
										.getId().getId()));
						Long controllerId = (Long) contrPer.getCardId().getId();
						controllerIds.add(controllerId);
					} catch (Exception e) {
						logger.error(
								"Error retrieving Person of the Controller of the 'Settings for ARM Manager':",
								e);
					}
				}
			}
		} catch (Exception e) {
			logger.error("Error retrieving attribute " + ATTR_CONTROLLER
					+ " of the 'Settings for ARM Manager' card "
					+ ((armCard == null) ? "null" : armCard.getId()), e);
		}
		if (controllerIds.isEmpty()) {
			// ���� � ���������� �� ������ ���������
			// �� � �������� ���������� ��������� �������� ������������
			final Person contrPer = serviceBean.getPerson();
			Long controllerId = (Long) contrPer.getCardId().getId();
			controllerIds.add(controllerId);
		}
		// cardIds.addAll(controllerIds);
		final List<Long> cardIds = new ArrayList<Long>(controllerIds);

		// --------------------------------------------------------------

		// �������� ������ ������� ���������
		result.setStandartResolutionTexts(retrieveTypicalResolutionsOrVisas(
				serviceBean, armCard, ATTR_ARM_RESOL_LIST));
		// �������� ������ ������� ���
		result.setTypicalVisasTexts(retrieveTypicalResolutionsOrVisas(
				serviceBean, armCard, ATTR_ARM_VISA_LIST));

		// �������� �������
		try {
			final HtmlAttribute signImg = (armCard == null) ? null
					: (HtmlAttribute) armCard
							.getAttributeById(ATTR_RES_SIGNIMG);
			if (signImg != null) {
				logger.debug("Attribute" + ATTR_RES_SIGNIMG + "="
						+ signImg.getValue());
				result.setSignImage(signImg.getValue());
			} else {
				logger.warn("Attribute" + ATTR_RES_SIGNIMG + " is null");
			}
		} catch (Exception e) {
			logger.error("Error when getting signature image: ", e);
		}
		// --------------------------------------------------------------

		// �������� ��������� �����������
		// -------------------------------------------------------------
		final List<ObjectId> immediateCardIds = new ArrayList<ObjectId>();
		try {
			final Collection<Person> companyPersons = SearchUtils
					.getAttrPersons(armCard, ATTR_COMPANY, false);
			if (companyPersons != null) {
				for (final Person person : companyPersons) {
					final ObjectId cardId = person.getCardId();
					immediateCardIds.add(cardId);
					cardIds.add((Long) cardId.getId());
				}
			}
		} catch (DataException ex) {
			logger.error("Error getting persons from attribute " + ATTR_COMPANY
					+ " of card "
					+ (armCard == null ? "null" : armCard.getId()), ex);
		}

		serviceBean = PortletUtil.createService(request);
		final Map<Long, String> names = ARMUtils.getNameByCardIds(cardIds);

		final Map<ObjectId, String> immediate = new LinkedHashMap<ObjectId, String>();
		for (final ObjectId cardId : immediateCardIds) {
			immediate.put(cardId, StringEscapeUtils.escapeHtml(names.get(cardId.getId())));
		}
		result.setImmediateEmployees(immediate);

		// �������� �������� ������������
		// -----------------------------------------------------------
		final PersonAttribute manager = (armCard == null) ? null
				: (PersonAttribute) armCard.getAttributeById(ATTR_ARM_MANAGER);
		if (manager != null && manager.getValues() != null)
			result.setCurrentPerson((Person) (manager.getValues().iterator()
					.next()));
		// -----------------------------------------------------------

		// �������� ������� �������� ��������� �� �������� �������� ����
		// ������������
		boolean isOnControl = false;
		if (armCard != null
				&& ((ListAttribute) armCard
						.getAttributeById(ATTR_RESOLUTION_ON_CONTROL)) != null) {
			ReferenceValue value = ((ListAttribute) armCard
					.getAttributeById(ATTR_RESOLUTION_ON_CONTROL)).getValue();
			if (value != null && value.getId() != null) {
				if (VALUE_YES_ON_TCON_ONCONT == ((Long) value.getId().getId())
						.longValue()) {
					isOnControl = true;
				}
			}
		}
		result.setIsOnControl(isOnControl);
		if (isOnControl) {
			final Map<Long, String> controllers = new LinkedHashMap<Long, String>();
			for (final Long cardId : controllerIds) {
				controllers.put(cardId, names.get(cardId));
			}
			result.setControllers(controllers);
		}

		// ������������� ����� ��� �������� - ���������� ������ �������������
		result.setEmployeesSearch(getSearchPerson());
		result.setExtPersonsSearch(getSearchExternalPerson());

		// ���������� �������� �� ������� ������������ ���������
		try {
			final Person curPerson = (Person) serviceBean
					.getById(Person.ID_CURRENT);
			for (Role role : (Collection<Role>) curPerson.getRoles()) {
				if (ROLE_MINISTR.equals(role.getSystemRole().getId())) {
					result.setMinister(true);
					break;
				}
			}
		} catch (Exception e) {
			logger.error("Error fetch current person", e);
		}

		return result;
	}

	private ArrayList<String> retrieveTypicalResolutionsOrVisas(
			DataServiceBean serviceBean, Card armCard, ObjectId attrId) {
		final ArrayList<String> textRess = new ArrayList<String>();

		Search search = new Search();
		search.setByCode(true);

		final List<SearchResult.Column> columns = new ArrayList<SearchResult.Column>();
		SearchUtils.addColumns(columns, ATTR_TYPE_RESOLUTION);
		search.setColumns(columns);

		final CardLinkAttribute linksRes = (armCard == null) ? null
				: (CardLinkAttribute) armCard.getAttributeById(attrId);

		// >>> (2010/02, RuSA) CardLink::getValues()
		if (linksRes != null && !linksRes.isEmpty()) {
			search.setWords(linksRes.getLinkedIds());
			// <<< (2010/02, RuSA) CardLink::getValues()

			try {
				final List<Card> resCards = SearchUtils.execSearchCards(search,
						serviceBean);
				if (resCards != null) {
					for (Card c : resCards) {
						final TextAttribute attr = (TextAttribute) c
								.getAttributeById(ATTR_TYPE_RESOLUTION);
						textRess.add(attr.getValue());
					}
				}
			} catch (Exception e) {
				logger.error("Error when searching for standard resolutions: ",
						e);
			}
		}
		return textRess;
	}

	public static QuickIndepResolutionPortletSessionBean getSessionBean(
			HttpServletRequest request, String namespace) {
		HttpSession session = request.getSession();
		String key = getApplicationSessionBeanKey(namespace);
		return (QuickIndepResolutionPortletSessionBean) session
				.getAttribute(key);
	}

	private static String getApplicationSessionBeanKey(String namespace) {
		return APPLICATION_SESSION_BEAN_PREFIX + namespace;
	}

	// ������������� ������ � fromCard �� toCard ����� �������������� attrId
	public void setLink(ObjectId fromCardId, ObjectId attrId,
			ObjectId toCardId, DataServiceBean serviceBean) throws Exception {
		boolean locked = false;
		try {
			serviceBean.doAction(new LockObject(fromCardId));
			locked = true;

			final Card fromCard = (Card) serviceBean.getById(fromCardId);

			final CardLinkAttribute attr = (CardLinkAttribute) fromCard
					.getAttributeById(attrId);
			attr.addLinkedId(toCardId);

			serviceBean.saveObject(fromCard);
		} catch (Exception e) {
			logger.error("Error updating parent card:", e);
			// TODO: ������������ esc-������������������, ����� � ������� ������
			// ���� ���������� ��������� �� ������
			throw new Exception(e.getMessage()
					.replace(Matcher.quoteReplacement("\n"), "\\n")
					.replaceAll(Matcher.quoteReplacement("\t"), "\\t")
					.replaceAll(Matcher.quoteReplacement("\r"), "\\r")); // esc-������������������
																			// ��������
																			// ������
																			// �
																			// ���������
																			// ��������
																			// ��
																			// ��������������
																			// �������);
		} finally {
			if (locked) {
				unlockQuickly(fromCardId, serviceBean);
			}
		}
	}

	public static String getNextDate() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH) + 1);
		SimpleDateFormat forrmater = new SimpleDateFormat("yyyy-MM-dd");
		return forrmater.format(cal.getTime());
	}

	private Card getCardById(ObjectId cardId, DataServiceBean serviceBean)
			throws PortletException {
		Card parentCard;
		try {
			parentCard = (Card) serviceBean.getById(cardId);
		} catch (ServiceException e) {
			logger.error(
					"Error reading attributes from the card 'Resolution':", e);
			throw new PortletException(e);

		} catch (DataException e) {
			logger.error(
					"Error reading attributes from the card 'Resolution':", e);
			throw new PortletException(e);
		}
		return parentCard;
	}

	private void initBeanFromResolutionCard(PortletRequest request,
			QuickIndepResolutionPortletSessionBean sessionBean) {
		try {
			DataServiceBean serviceBean = PortletUtil.createService(request);
			final Card card = sessionBean.getResolutionCard();

			// ��������� ���������� � ���. ����������� � ��������������
			final Collection<Long> cardIds = new ArrayList<Long>();
			// ������������� �����������
			Long responsibleId = null;
			PersonAttribute perAttr = (PersonAttribute) card
					.getAttributeById(ATTR_EXECUTOR);
			Collection<Person> perList = SearchUtils.getAttrPersons(perAttr);
			if (perList != null && perList.size() > 0) {
				responsibleId = (Long) perList.iterator().next().getCardId()
						.getId();
				cardIds.add(responsibleId);
			}

			// �������������
			final Map<Long, String> additionals = new LinkedHashMap<Long, String>();
			perAttr = (PersonAttribute) card.getAttributeById(ATTR_COEXECUTORS);
			perList = SearchUtils.getAttrPersons(perAttr);
			if (perList != null) {
				for (Person p : perList) {
					final Long id = (Long) p.getCardId().getId();
					additionals.put(id, "");
					cardIds.add(id);
				}
			}

			// ���������
			final Map<Long, String> controllers = new LinkedHashMap<Long, String>();
			final Collection<Person> personsInspect = SearchUtils
					.getAttrPersons(card, ATTR_TCON_INSPECTOR);
			if (personsInspect != null) {
				for (Person p : personsInspect) {
					Long controllerId = (Long) p.getCardId().getId();
					controllers.put(controllerId, "");
					cardIds.add(controllerId);
				}
			}

			final Map<Long, String> names = ARMUtils.getNameByCardIds(
					serviceBean, cardIds);
			if (responsibleId != null) {
				sessionBean.setResponsible(responsibleId,
						names.get(responsibleId));
			}
			for (Long id : additionals.keySet()) {
				additionals.put(id, names.get(id));
			}
			sessionBean.setAdditionals(additionals);

			for (Long id : controllers.keySet()) {
				controllers.put(id, names.get(id));
			}
			sessionBean.setControllers(controllers);

			// ����� ���������
			final String textResolution = ((TextAttribute) card
					.getAttributeById(ATTR_RESOLUT)).getValue();
			sessionBean.setResolutionText(textResolution);

			// ���� ����������
			final Date term = ((DateAttribute) card.getAttributeById(ATTR_TERM))
					.getValue();
			sessionBean.setTerm(term);

			// �� ��������
			boolean isOnControl = false;
			ReferenceValue value = ((ListAttribute) card
					.getAttributeById(ATTR_TCON_ONCONT)).getValue();
			if (value != null && value.getId() != null) {
				if (VALUE_YES_ON_TCON_ONCONT == ((Long) value.getId().getId())
						.longValue()) {
					isOnControl = true;
				}
			}
			sessionBean.setIsOnControl(isOnControl);

			// �������
			final Attribute attr = card.getAttributeById(ATTR_SIGNATURE);
			if (attr != null) { // && attr instanceof StringAttribute)
				final String signature = ((HtmlAttribute) attr).getValue();
				sessionBean.setSignature(signature);
			}
			
			// gets attachments
			CardLinkAttribute docLinks = null;
			if (TEMPLATE_RESOLUTION.equals(card.getTemplate()) || TEMPLATE_INDEPENDENT_RESOLUTION.equals(card.getTemplate())) {
				docLinks = (CardLinkAttribute) card
						.getAttributeById(ATTR_DOCLINKS);
			} else {
				docLinks = (CardLinkAttribute) card
						.getAttributeById(ATTR_VISA_ATTACHMENTS);
			}
			Map<ObjectId, String> attachedFilesMap = new LinkedHashMap<ObjectId, String>();
			if (docLinks != null) {
				Collection<ObjectId> attachedFileIds = docLinks.getIdsLinked();
				for (ObjectId attachedFileId : attachedFileIds) {
					Card attachedCard = getCardById(attachedFileId, serviceBean);
					StringAttribute nameAttr = (StringAttribute) attachedCard.getAttributeById(Attribute.ID_NAME);
					attachedFilesMap.put(attachedFileId,
							nameAttr.getValue());
				}
			}
			// init attached files field
			sessionBean.setAttachedFiles(attachedFilesMap);

		} catch (Exception e) {
			logger.error(
					"Error reading attributes from the card 'Resolution':", e);
		}
	}

	// ���������� ������������ ����� �������� �������� � Person ������� �� ���
	// ���������
	// Collection ids - ��������� id ��������� ���� Long
	// return Map Long -> Person
	private Map<Long, Person> getPersonsByCardIds(DataServiceBean serviceBean,
			Collection<Long> ids) {
		Map<Long, Person> map = new LinkedHashMap<Long, Person>();
		try {
			final List<ObjectId> cardIds = new ArrayList<ObjectId>(ids.size());
			final Iterator<Long> i = ids.iterator();
			while (i.hasNext()) {
				cardIds.add(new ObjectId(Card.class, i.next()));
			}

			final PersonCardIdFilter filter = new PersonCardIdFilter();
			filter.setCardIds(cardIds);
			final Collection<Person> persons = serviceBean.filter(Person.class, filter); 
			if (persons.size() != cardIds.size()) {
				logger.warn("There is persons with wrong card_id. Values can be empty or non-unique.");
			}

			for (Person p : persons) {
				map.put((Long) p.getCardId().getId(), p);
			}
		} catch (Exception e) {
			logger.error("Error by search Person:", e);
		}
		return map;
	}

	private void setResolutionAttributes(
			QuickIndepResolutionPortletSessionBean sessionBean,
			DataServiceBean serviceBean) {
		Card resolutionCard = sessionBean.getResolutionCard();
		// �������� ������������ ����� card_id � person_id ������������
		final List<Long> cardIds = new ArrayList<Long>();
		if(sessionBean.getResponsibleId() != null){
			cardIds.add(sessionBean.getResponsibleId());
		}
		final Set<Long> controllerIds = sessionBean.getControllers().keySet();
		cardIds.addAll(controllerIds);
		cardIds.addAll(sessionBean.getAdditionals().keySet());
		
		final Map<Long, Person> persons = getPersonsByCardIds(serviceBean,
				cardIds);

		// adding executor
		final Person executorPerson = persons.get(sessionBean
				.getResponsibleId());
		((PersonAttribute) resolutionCard.getAttributeById(ATTR_EXECUTOR)).clear();
		if (executorPerson != null) {
			((PersonAttribute) resolutionCard.getAttributeById(ATTR_EXECUTOR))
					.setPerson(executorPerson);
		}

		// adding coexecutors
		final PersonAttribute coexecutorsAttr = (PersonAttribute) resolutionCard
				.getAttributeById(ATTR_COEXECUTORS);
		coexecutorsAttr.clear();
		final List<Person> newCoexecutorsList = new ArrayList<Person>();
		for (final Iterator<Long> iter = sessionBean.getAdditionals().keySet()
				.iterator(); iter.hasNext();) {
			Person itemP = persons.get(iter.next());
			if (itemP != null) {
				newCoexecutorsList.add(itemP);
			}
		}
		if (newCoexecutorsList.size() > 0) {
			coexecutorsAttr.setValues(newCoexecutorsList);
		}

		// resolution text attribute
		String textResolution = sessionBean.getResolutionText();
		((TextAttribute) resolutionCard.getAttributeById(ATTR_RESOLUT))
				.setValue(textResolution);

		// termDate attribute
		Date term = sessionBean.getTerm();
		((DateAttribute) resolutionCard.getAttributeById(ATTR_TERM))
				.setValue(term);

		// instpector attribute
		List<Person> controllers = new ArrayList<Person>();
		for (final Iterator<Long> iter = controllerIds.iterator(); iter
				.hasNext();) {
			controllers.add(persons.get(iter.next()));
		}
		((PersonAttribute) resolutionCard.getAttributeById(ATTR_TCON_INSPECTOR))
				.setValues(controllers);

		// on control attribute
		ReferenceValue onCont = new ReferenceValue();
		long onContVal = sessionBean.getIsOnControl() ? VALUE_YES_ON_TCON_ONCONT
				: VALUE_NO_ON_TCON_ONCONT;
		onCont.setId(onContVal);
		((ListAttribute) resolutionCard.getAttributeById(ATTR_TCON_ONCONT))
				.setValue(onCont);

		// signatory attribute
/*		linkAttr = (CardLinkAttribute) resolutionCard
				.getAttributeById(ATTR_SIGNATORY);
		if (linkAttr.getLinkedCount() < 1) {
			linkAttr.addLinkedId(sessionBean.getCurrentPerson().getCardId());
		}*/
		PersonAttribute attr = (PersonAttribute)resolutionCard.getAttributeById(ATTR_SIGNATORY);
		if ( ((PersonAttribute)attr).getPerson() == null ) {
		    ((PersonAttribute) attr).setPerson( sessionBean.getCurrentPerson() );
		}

		// linked docs
		ObjectId linkedDoc = sessionBean.getLinkedDoc();
		if(linkedDoc != null) {
			TypedCardLinkAttribute linkedDocAttr = (TypedCardLinkAttribute)resolutionCard.getAttributeById(LINKED_DOC);
			linkedDocAttr.addLinkedId(linkedDoc);
			linkedDocAttr.addType((Long)linkedDoc.getId(), LINK_TYPE);
		}
		
		//attached files
		Map<ObjectId, String> attachedFiles = sessionBean.getAttachedFiles();
		if(attachedFiles != null) {
			CardLinkAttribute dockLinks = (CardLinkAttribute)resolutionCard.getAttributeById(ATTR_DOCLINKS);
			dockLinks.clear();
			for(Map.Entry<ObjectId, String> docEntry : attachedFiles.entrySet()) {
				dockLinks.addLinkedId(docEntry.getKey());
			}
		}
		
	}

	private void doExit(PortletRequest request, ActionResponse response,
			QuickIndepResolutionPortletSessionBean sessionBean,
			String redirectLink) throws IOException {
		PortletSession session = request.getPortletSession();
		String namespace = request.getParameter(FIELD_NAMESPACE);
		session.removeAttribute(SESSION_BEAN);
		session.removeAttribute(getApplicationSessionBeanKey(namespace),
				PortletSession.APPLICATION_SCOPE);
		
		if(sessionBean != null
				&& sessionBean.isCardLinked() // ���� ������� �������� - ���������, ��������� �������� �� WorkstationCardPortlet
				&& sessionBean.getResolutionCard() != null
				&& sessionBean.getResolutionCard().getId() != null
				&& sessionBean.getResolutionCard().getId().getId() != null)
			redirectLink = "/portal/auth/portal/boss/workstationCard/WorkstationCardWindow?MI_LINK_TO_CARD="+sessionBean.getResolutionCard().getId().getId();
		
		redirectLink = redirectLink.replaceAll("%2F", "/")
				.replaceAll("%3D", "=")
				.replaceAll("%3F", "?");
		response.sendRedirect(redirectLink);
	}

	private Search getSearchPerson() {
		final Search search = new Search();
		search.setColumns(ARMUtils.getFullNameColumns());

		search.setByAttributes(true);
		search.setWords(null);
		final List<DataObject> templates = new ArrayList<DataObject>(1);
		templates.add(DataObject.createFromId(TEMPLATE_PERSON));
		search.setTemplates(templates);
		search.addStringAttribute(Attribute.ID_NAME);

		final List<String> states = new ArrayList<String>(1);
		states.add(CARDSTATE_ACTIVE_USER.getId().toString());
		search.setStates(states);

		return search;
	}

	private Search getSearchExternalPerson() {
		final Search search = new Search();
		List<SearchResult.Column> columns = ARMUtils.getFullNameColumns();
		SearchUtils.addColumns(columns, ObjectId.predefined(
				StringAttribute.class, "jbr.person.position"));
		search.setColumns(columns);

		search.setByAttributes(true);
		search.setWords(null);

		final List<DataObject> templates = new ArrayList<DataObject>(1);
		templates.add(DataObject.createFromId(TEMPLATE_EXT_PERSON));
		search.setTemplates(templates);

		search.addStringAttribute(Attribute.ID_NAME);

		final List<String> states = new ArrayList<String>(2);
		states.add(DICTIONARY_NEW.getId().toString());
		states.add(CardState.PUBLISHED.getId().toString());
		search.setStates(states);

		return search;
	}

	/**
	 * ������� ���������� � �������� �� id.
	 * �������� ������������ id==null. 
	 * @param objectId
	 * @param serviceBean
	 */
	private void unlockQuickly(ObjectId objectId, DataServiceBean serviceBean) {
		if(objectId==null || objectId.getId()==null){
			logger.info("There is no way to unlock the card, because it's not.");
			return;
		}
		try {
			serviceBean.doAction(new UnlockObject(objectId));
		} catch (Exception e) {
			logger.error("Couldn't unlock object " + objectId.toString(), e);
		}
	}
	
	private Date fetchPersonalControlTerm(
			QuickIndepResolutionPortletSessionBean sessionBean,
			DataServiceBean serviceBean) {
		Date personalControlTerm = null;
		
		final Search search = new Search();
		search.setWords("");		// what's this, why do we need it here?
		search.setByAttributes(true);

		search.addCardLinkAttribute(ATTR_PCON_DOC, sessionBean.getResolutionCardId());
		search.addPersonAttribute(ATTR_PCON_PERSON, sessionBean.getCurrentPerson().getId());

		final List<DataObject> templates = new ArrayList<DataObject>(1);
		templates.add(DataObject.createFromId(TEMPLATE_PERSONAL_CONTROL));
		search.setTemplates(templates);

		final List<SearchResult.Column> columns = new ArrayList<SearchResult.Column>();
		SearchUtils.addColumns(columns, ATTR_PCON_DATE);
		search.setColumns(columns);
		
		Collection<Card> controlCards = null;
		try {
			controlCards = SearchUtils.execSearchCards(search, serviceBean); 
		} catch (Exception e) {
			logger.error("Error in \"Personal control\" card search: ", e);
		}
		
		if (controlCards != null && controlCards.size() > 0) {
			if (controlCards.size() > 1) {
				logger.warn("More than one \"Personal Control\" cards are linked to the order with id = "
						+ sessionBean.getResolutionCardId());
			}
			Card controlCard = controlCards.iterator().next();
			
			personalControlTerm = ((DateAttribute)controlCard.getAttributeById(ATTR_PCON_DATE)).getValue();
		}
		
		return personalControlTerm;
	}
	
	private ObjectId savePersonalControlCard(QuickIndepResolutionPortletSessionBean sessionBean,
			DataServiceBean serviceBean) {
		CreateCard createCard = new CreateCard(TEMPLATE_PERSONAL_CONTROL);
		Card card = null;
		try {
			card = (Card) serviceBean.doAction(createCard);
			setPersonalControlCardAttrs(card, sessionBean);
			ObjectId cardId = serviceBean.saveObject(card);
			unlockQuickly(cardId, serviceBean);
			return cardId;
		} catch (Exception e) {
			logger.error("Error saving card 'On personal control':", e);
		}
		return null;
	}
	
	private void setPersonalControlCardAttrs(Card card,
			QuickIndepResolutionPortletSessionBean sessionBean) {
		// ����������
		Person controller = sessionBean.getCurrentPerson();
		((PersonAttribute) card.getAttributeById(ATTR_PCON_PERSON)).setPerson(controller);

		// c����� �� �������� ��������
		final CardLinkAttribute attr = (CardLinkAttribute)card.getAttributeById(ATTR_PCON_DOC);
		attr.addSingleLinkedId(sessionBean.getResolutionCardId());

		// ���� ��������
		Date term = sessionBean.getControlTerm();
		((DateAttribute) card.getAttributeById(ATTR_PCON_DATE)).setValue(term);

		// ��������� ���� NAME ��� ����, ��� �� ������� �����
		((StringAttribute) card.getAttributeById(ATTR_NAME))
				.setValue("������ ��������");
	}
	
	private void fillBackUrl(ActionRequest request, QuickIndepResolutionPortletSessionBean sessionBean) {
		PortletSession session = request.getPortletSession();
		if(session.getAttribute(ResolutionReportPortlet.SESSION_BEAN, PortletSession.APPLICATION_SCOPE) != null){
			ResolutionReportPortletSessionBean resolutionReportPortletSessionBean = 
					(ResolutionReportPortletSessionBean) session.getAttribute(ResolutionReportPortlet.SESSION_BEAN, PortletSession.APPLICATION_SCOPE);
			if(resolutionReportPortletSessionBean.getReportPreparedDocs() != null){
				resolutionReportPortletSessionBean.getReportPreparedDocs().add(sessionBean.getResolutionCard());
			} else {
				List<Card> preparedDocsList = new ArrayList<Card>();
				preparedDocsList.add(sessionBean.getResolutionCard());
				resolutionReportPortletSessionBean.setReportPreparedDocs(preparedDocsList);
			}
		}
	}
}
