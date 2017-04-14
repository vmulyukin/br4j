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
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

import com.aplana.dbmi.action.CreateCard;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.OverwriteCardAttributes;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.card.util.ARMUtils;
import com.aplana.dbmi.card.util.SearchUtils;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.HtmlAttribute;
import com.aplana.dbmi.model.LinkAttribute;
import com.aplana.dbmi.model.ListAttribute;
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
import com.aplana.dbmi.model.filter.PersonCardIdFilter;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.AsyncDataServiceBean;
import com.aplana.dbmi.service.AsyncDataServiceBean.ExecuteOption;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.PortletUtil;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.dbmi.service.SystemUser;
import com.aplana.dbmi.util.JspUtils;
import com.aplana.util.DigitalSignatureUtil;
import com.aplana.web.tag.util.StringUtils;


public class MassResolutionPortlet extends GenericPortlet {
	private Log logger = LogFactory.getLog(getClass());
	
	public static final String SESSION_BEAN = "massResolutionPortletSessionBean";
	public static final String APPLY_DS = "MI_APPLY_SIGNATURE";
	public static final String DS_PARAMS = "MI_SIGNATURE_PARAMS";
	private static final String APPLICATION_SESSION_BEAN_PREFIX = "massResolutionPortletSessionBean:";

	public static final String CONFIG_FOLDER = "dbmi/card/";
	public static final String JSP_FOLDER = "/WEB-INF/jsp/quickResolution/";

	public static final String ACTION_CANCEL = "cancel";
	public static final String ACTION_BACK = "back";
	public static final String ACTION_DONE = "done";
	public static final String ACTION_UPLOAD = "upload";
	public static final String ACTION_INIT = "init"; // ����� ������ ��������
														// - �������� �����
														// ��������� ���
														// �������������� ������
	public static final String ACTION_SAVE_DS_AND_EXIT = "saveDSandExit";

	public static final String STATE_INIT_CREATE = "initCreate";

	public static final String PARAM_STATE_INIT = "stateInit";
	
	public static final String MATERIAL_UPLOAD_URL = "/DBMI-UserPortlets/servlet/arm-upload";
	public static final String PARAM_PARENT_CARD_ID = "parentCardId";
	public static final String PARAM_BACK_URL = "backURL";
	public static final String PARAM_DONE_URL = "doneURL";
	public static final String PARAM_SIGNATURE = "signature";

	public static final String FIELD_ACTION = "formAction";
	public static final String FIELD_RESPONSIBLE_EXECUTOR = "responsibleExecutor";
	public static final String FIELD_REFUSE_ATACHMENTS = "refuseAttachments";
	public static final String FIELD_SUPPORTS_DS = "supportsDS";
	public static final String FIELD_RESOLUTION_ATTACHMENTS = "resolutionAttachments";

	public static final String FIELD_NAMESPACE = "namespace";
	public static final String FIELD_TEXT_RESOLUTION = "textResolution";
	public static final String FIELD_TERM = "term";
	public static final String FIELD_TERM_DATE = "term_date";
	public static final String FIELD_TERM_TIME = "term_time";
	public static final String FIELD_CONTROL = "control";
	public static final String FIELD_CONTROL_TERM = "controlTerm";
	public static final String FIELD_ON_CONTROL = "onControl";
	public static final String FIELD_CONTROLLERS = "controllers";
	public static final String FIELD_OTHER_EXEC = "otherExec";
	public static final String FIELD_SIGNATURE = "signature";
	public static final String FIELD_FILE_ATTACHMENT = "fileAttachment";
	public static final String FIELD_PRELIMINARY_TERM = "preliminaryTerm";

	// �������� ������� "���������"
	public static final ObjectId ATTR_EXECUTOR = ObjectId.predefined(
			PersonAttribute.class, "jbr.AssignmentExecutor");
	public static final ObjectId ATTR_RESOLUT = ObjectId.predefined(
			TextAttribute.class, "jbr.resolutionText");
	public static final ObjectId ATTR_SHORT_DESC = ObjectId.predefined(
			TextAttribute.class, "jbr.document.title");
	public static final ObjectId ATTR_SHORT_CONTEXT = ObjectId.predefined(
			TextAttribute.class, "jbr.shortcontext");

	public static final ObjectId ATTR_TERM = ObjectId.predefined(
			DateAttribute.class, "jbr.resolutionTerm");
	public static final ObjectId ATTR_PRELIMINARY_TERM = ObjectId.predefined(
			DateAttribute.class, "jbr.resolutionTermPreliminary");
	public static final ObjectId ATTR_TCON_INSPECTOR = ObjectId.predefined(
			PersonAttribute.class, "jbr.commission.inspector");
	public static final ObjectId ATTR_TCON_ONCONT = ObjectId.predefined(
			ListAttribute.class, "jbr.oncontrol");

	public static final ObjectId ATTR_SIGNATURE = ObjectId.predefined(
			HtmlAttribute.class, "jbr.uzdo.signature");
	public static final ObjectId ATTR_SIGNATORY = ObjectId.predefined(
	        PersonAttribute.class, "jbr.resolution.FioSign");
	public static final ObjectId ATTR_DATESIGN = ObjectId.predefined(
			DateAttribute.class, "jbr.outcoming.signdate");
	public static final ObjectId ATTR_RESOLUT_ATTACH = ObjectId.predefined(
			TypedCardLinkAttribute.class, "jbr.attachRes");
	public static final ObjectId ATTR_MAINDOC = ObjectId.predefined(
			CardLinkAttribute.class, "jbr.main.doc");
	public static final ObjectId ATTR_DOCB_BYDOC = ObjectId.predefined(
			CardLinkAttribute.class, "jbr.rimp.bydoc");
	
	public static final ObjectId ATTR_DOCLINKS = ObjectId.predefined(CardLinkAttribute.class, "jbr.files");
	
	public static final ObjectId ATTR_REPORT_ATTACHMENTS = ObjectId.predefined(CardLinkAttribute.class, "jbr.report.attachments");
	
	public static final ObjectId JBR_INFD_TYPEDOC = ObjectId.predefined(CardLinkAttribute.class, "jbr.reg.doctype");
	
	public static final ObjectId JBR_REGD_REGNUM = ObjectId.predefined(StringAttribute.class, "regnumber");

	public static final ObjectId ATTR_REPORTS = ObjectId.predefined(
			BackLinkAttribute.class, "jbr.reports");

	public static final long VALUE_YES_ON_TCON_ONCONT = 1449;
	public static final long VALUE_NO_ON_TCON_ONCONT = 1450;

	public static final Long VALUE_GRAPHICAL = Long.valueOf(901);
	public static final Long VALUE_AUDIO = Long.valueOf(902);
	// �������� ������� "��������� ��� ������������"
	public static final ObjectId ATTR_ARM_MANAGER = ObjectId.predefined(
			PersonAttribute.class, "jbr.arm.manager");
	public static final ObjectId ATTR_CONTROLLER = ObjectId.predefined(
			PersonAttribute.class, "jbr.arm.inspector");
	public static final ObjectId ATTR_ARM_RESOL_LIST = ObjectId.predefined(
			CardLinkAttribute.class, "jbr.arm.resolutionList");
	public static final ObjectId ATTR_COMPANY = ObjectId.predefined(
			PersonAttribute.class, "jbr.arm.company");
	public static final ObjectId ATTR_RESOLUTION_ON_CONTROL = ObjectId.predefined(
			ListAttribute.class, "jbr.arm.resolutionOnControl");
	// �������� ������� "������� ��������� ����"
	public static final ObjectId ATTR_TYPE_RESOLUTION = ObjectId.predefined(
			TextAttribute.class, "jbr.typeResolution.resolution");
	// �������� ������� "������ ��������"
	public static final ObjectId ATTR_PCON_PERSON = ObjectId.predefined(
			PersonAttribute.class, "jbr.boss.control.inspector");
	public static final ObjectId ATTR_PCON_DOC = ObjectId.predefined(
			CardLinkAttribute.class, "jbr.boss.control.document");
	public static final ObjectId ATTR_PCON_DATE = ObjectId.predefined(
			DateAttribute.class, "jbr.boss.control.date");

	public static final ObjectId ATTR_MAINDOC_EXECUTOR = ObjectId.predefined(
			PersonAttribute.class, "jbr.resolutionExecutor");

	// �������� ������� "��������"
	public static final ObjectId ATTR_IMPL_RESOLUT = ObjectId.predefined(
			BackLinkAttribute.class, "jbr.resolutions");
	public static final ObjectId ATTR_TYPE_CONTROL = ObjectId.predefined(
			ListAttribute.class, "jbr.inbound.typecontrol");
	public static final ObjectId ATTR_ON_CONTROL = ObjectId.predefined(
			ListAttribute.class, "jbr.incoming.oncontrol");
	public static final ObjectId ATTR_CONSIDIRATIONS = ObjectId.predefined(
			CardLinkAttribute.class, "jbr.examby");
	public static final ObjectId VALUE_BOSS_CONTROL = ObjectId.predefined(
			ReferenceValue.class, "jbr.incoming.typecontrol.boss");
	public static final ObjectId VALUE_ON_CONTROL = ObjectId.predefined(
			ReferenceValue.class, "jbr.incoming.control.yes");

	// �������� ������� "����� �� ����������"
	public static final ObjectId ATTR_REPORT_SIGN = ObjectId.predefined(
			PersonAttribute.class, "jbr.report.int.executor");

	// ����� ���������
	public static final ObjectId ATTR_NAME = ObjectId.predefined(
			StringAttribute.class, "name");

	public static final ObjectId TEMPLATE_RESOLUTION = ObjectId.predefined(
			Template.class, "jbr.resolution");
	public static final ObjectId TEMPLATE_PERSONAL_CONTROL = ObjectId
			.predefined(Template.class, "jbr.boss.control");
	public static final ObjectId TEMPLATE_PERSON = ObjectId.predefined(
			Template.class, "jbr.internalPerson");
	public static final ObjectId TEMPLATE_OG = ObjectId.predefined(
			Template.class, "jbr.incomingpeople");

	public static final ObjectId CARDSTATE_ACTIVE_USER = ObjectId.predefined(
			CardState.class, "user.active");
	public static final ObjectId DICTIONARY_NEW = ObjectId.predefined(
			CardState.class, "dictionaryNew");

	public static final ObjectId ATTR_RES_SIGNIMG = ObjectId.predefined(
			HtmlAttribute.class, "boss.settings.image1");

	// ����
	public static final ObjectId ROLE_MINISTR = ObjectId.predefined(
			SystemRole.class, "jbr.minister");

	public static final String SEPARATOR = "#separator#";
	public static final String ID_DELIMITER = "#id_delim#";
	
	public static final String IS_DS_SUPPORT = "isDsSupport";
	
	public static final String DEFAULT_TIME = "23:59:59";
	public static final String FMT_YYYY_MM_DD = "yyyy-MM-dd";
	public static final String FMT_YYYY_MM_DD_T_HH_MM_SS = "yyyy-MM-dd'T'HH:mm:ss";
	public static final SimpleDateFormat FORMATTER_YYYY_MM_DD_T_HH_MM_SS = new SimpleDateFormat(FMT_YYYY_MM_DD_T_HH_MM_SS);
	public static final SimpleDateFormat FORMATTER_YYYY_MM_DD = new SimpleDateFormat(FMT_YYYY_MM_DD);

	@SuppressWarnings("unchecked")
	@Override
	protected void doView(RenderRequest request, RenderResponse response)
			throws PortletException, PortletSecurityException, IOException {
		
		final MassResolutionPortletSessionBean sessionBean = getSessionBean(request);
		
		response.setContentType("text/html");
		
		if (sessionBean.getPortletFormManager().processRender(request, response)) {
			return;
		}
		
		String key = getApplicationSessionBeanKey(response.getNamespace());
		PortletSession session = request.getPortletSession();
		session.setAttribute(key, sessionBean, PortletSession.APPLICATION_SCOPE);

		final String page;
		
		String message = "'";
		if (sessionBean.getMessage() != null) {
			message += sessionBean.getMessage();
			sessionBean.setMessage(null);
		}
		message += "'";
		request.setAttribute("message", message);
		
			// ���� ��������� - �������� ������ ���������
			if (STATE_INIT_CREATE.equalsIgnoreCase(sessionBean.getStateInit())){
				// ������� ����� �������� ���������, ����� ��������� ����������
				try{
					DataServiceBean serviceBean = PortletUtil.createService(request);
					CreateCard createCard = new CreateCard(TEMPLATE_RESOLUTION);
					createCard.setLinked(true);
					Card card = null;
					ObjectId mainDocCardId = sessionBean.getParentId();
					Card mainDocCard = (Card)serviceBean.getById(mainDocCardId);
					createCard.setParent(mainDocCard);
					card = (Card) serviceBean.doAction(createCard);				
	
					// �����������������������, ���� � ������� �������� ������������� ��� ��������� ���������, ��� ������� ��������� ���������� �������� �������� (�������� 16738)
				 	sessionBean.setTermAttribute(((DateAttribute) card.getAttributeById(ATTR_TERM)));
	
					// �������� ������������ ����� card_id � person_id ������������
				 	Collection<Person> persons = ((PersonAttribute)card.getAttributeById(ATTR_TCON_INSPECTOR)).getValues();
	
					if (persons!=null){
					 	final Map<Long, String> cardNames = getCardsAndNamesByPersons(persons);
						sessionBean.setControllers(cardNames);
					}
						
					final ReferenceValue onCont = ((ListAttribute)card.getAttributeById(ATTR_TCON_ONCONT)).getValue();
					boolean onContVal = false;
					if(((Long)onCont.getId().getId()).longValue()==VALUE_YES_ON_TCON_ONCONT) {
						onContVal = true;
					}
					sessionBean.setIsOnControl(onContVal);
				} catch (Exception e){
					logger.error("Error by create new resolution and filling control-fields:", e);
				}
			}
			final JSONArray responsible = new JSONArray();
			try {
				final Map<Long, String> map = sessionBean.getResponsibles();
				if (map != null) {
					for(final Long cardId : map.keySet() ) {
						final JSONObject j = new JSONObject();
						j.put("cardId", cardId);
						j.put("name", map.get(cardId));
						responsible.put(j);
					}
				}
			} catch (JSONException e) {
				logger.error("JSON exception caught:", e);
			}
			request.setAttribute("responsible", responsible);
			
			final JSONArray controllers = new JSONArray();
			try {
				final Map<Long, String> map = sessionBean.getControllers();
				if (map != null) {
					for(final Long cardId : map.keySet() ) {
						final JSONObject j = new JSONObject();
						j.put("cardId", cardId);
						j.put("name", map.get(cardId));
						controllers.put(j);
					}
				}
			} catch (JSONException e) {
				logger.error("JSON exception caught:", e);
			}
			
			request.setAttribute("controllers", controllers);
			request.setAttribute(FIELD_RESOLUTION_ATTACHMENTS, getAttachmentsJSONData(sessionBean.getAttachedFiles()));
			page = "../massResolution/massResolutionData.jsp";
		

		PortletRequestDispatcher rd = getPortletContext().getRequestDispatcher(
				JSP_FOLDER + page);
		rd.include(request, response);
	}
	
	
	private JSONArray getAttachmentsJSONData(Map<ObjectId, String> attachmentsMap) throws PortletException {
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
			
			String action = request.getParameter(FIELD_ACTION);
			if (ACTION_INIT.equals(action)) {
				PortletSession session = request.getPortletSession();
				// ������� sessionBean, ���� �� �����������, ����� ������� �����
				session.removeAttribute(SESSION_BEAN); 
			}
			MassResolutionPortletSessionBean sessionBean = getSessionBean(request);
			DataServiceBean serviceBean = PortletUtil.createService(request);
			
			if (sessionBean.getPortletFormManager().processAction(request, response)) {
				return;
			}
			
			parseFormValuesIntoSessionBeanFromRequest(sessionBean, request);
		
			if (ACTION_INIT.equals(action)) {
				if (sessionBean.getCurrentPerson() == null) {
						final StringBuffer message = new StringBuffer();
						message.append(getResourceBundle(request.getLocale())
								.getString("error.ArmSettingEmpty"));
						
						sessionBean.setMessage(message.toString());
						return; 
				}
				
				final String backLink = request.getParameter(PARAM_BACK_URL);
				sessionBean.setBackLink(backLink);
				String doneLink = request.getParameter(PARAM_DONE_URL);
				if (doneLink == null) {
					doneLink = backLink;
				}
				sessionBean.setDoneLink(doneLink);
	
				String stateInit = request.getParameter(PARAM_STATE_INIT);
				if (stateInit == null) {
					stateInit = STATE_INIT_CREATE;
				}
				
				sessionBean.setStateInit(STATE_INIT_CREATE);
				final String parentCardId = request.getParameter(PARAM_PARENT_CARD_ID);
				if (parentCardId != null && !parentCardId.trim().equals("")) {
					final Long parentId = Long.valueOf(parentCardId);
					ObjectId objId = ObjectIdUtils.getObjectId(Card.class, parentId.toString(), true);
					sessionBean.setParentId(objId);
						
					initFromParentCard(request, sessionBean);
					searchPersonalControlCard(sessionBean, serviceBean);
				} else {
					doExit(request, response, sessionBean, false);
				}
				
			/*
			 * ���� �������������� ���� ��� �������,  ���������� ������������
			 * ��������� ����� �������, � �� �������� ���������. (�.�., 19.05.2011)
			 */
				initBaseCardProperties(request, sessionBean, serviceBean);
			} else if (ACTION_CANCEL.equals(action)) {
				doExit(request, response, sessionBean, false);
			} else if (ACTION_BACK.equals(action)) {
				doExit(request, response, sessionBean, false);
			} else if (ACTION_DONE.equals(action)) {
				try{
					update(sessionBean, request);
					updateControl(sessionBean, request);
					doExit(request, response, sessionBean, true);
				} catch(Exception e){
					logger.error(e);
						// ����� ��������� ��������� ������� � ���������� ���
					StringBuffer message = new StringBuffer();
					String msg = ( e.getMessage() != null) ? e.getMessage() : "";
					message.append(MessageFormat.format( getResourceBundle(request.getLocale()).getString("message.resolutionNotSave"), msg.replaceAll(e.getClass().getName(), "").replaceAll("'", "\"")));
					sessionBean.setMessage(message.toString());
				}
				
			} else if (ACTION_SAVE_DS_AND_EXIT.equals(action)){
				signCardHandler(request, serviceBean);
				doExit(request, response, sessionBean, true);
			}
		}
		
	private void parseFormValuesIntoSessionBeanFromRequest(MassResolutionPortletSessionBean sessionBean,
			ActionRequest request) {

		// ������������� �����������
		String p = request.getParameter(FIELD_RESPONSIBLE_EXECUTOR);
		if (p != null) {
			final Map<Long, String> responsibles = new LinkedHashMap<Long, String>();
			if (!"".equals(p.trim())) {
				final String[] peoples = p.split(SEPARATOR);
				for (int i = 0; i < peoples.length; i++) {
					final String[] value = peoples[i].split(":");
					responsibles.put(Long.valueOf(value[0]), value[1]);
				}
				sessionBean.setResponsibles(responsibles);
			}
		}
		
		// ��������
		p = request.getParameter(FIELD_FILE_ATTACHMENT);
		if (p != null) {
			final Map<ObjectId, String> attachments = new LinkedHashMap<ObjectId, String>();
			if (!"".equals(p.trim())) {
				final String[] files = p.split(SEPARATOR);
				for (int i = 0; i < files.length; i++) {
					final String[] value = files[i].split(ID_DELIMITER);
					ObjectId fileId = new ObjectId(Card.class, Long.valueOf(value[0]));
					attachments.put(fileId, value[1]);
				}
			}
			sessionBean.setAttachedFiles(attachments);
		}
		
		// ����� ���������
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
					logger.error("Bad format date of term person control:",
							e);
				}
		} else {
			sessionBean.setControlTerm(null);
		}
		
		// ����
		final String termDate = request.getParameter(FIELD_TERM_DATE);
		String termTime = request.getParameter(FIELD_TERM_TIME);
		final boolean timeIsSet = (termTime != null) && !termTime.equals("");
		final boolean showTime =  sessionBean.isTermShowTime();
		SimpleDateFormat formatter = null;

		if (termDate != null && !termDate.trim().equals("")) {
		// ���� ������
			if (!showTime) {
				// ����� �� ����������
				formatter = FORMATTER_YYYY_MM_DD;
				termTime = "";
			}else {
				formatter = FORMATTER_YYYY_MM_DD_T_HH_MM_SS;
				if (!timeIsSet) {
					// ����� ����������, �� ��� �� ������
					termTime = "T" + DEFAULT_TIME;
				}
			}
			if (formatter != null) {
				try {
					final Date newDate = formatter.parse(termDate + termTime);
					sessionBean.getTermAttribute().setValue(newDate);
				}catch (Exception e) {
					logger.error("Bad format date of term:", e);
				}
			}
		}else {
			sessionBean.setTermAttribute(null);
		}
		
		// �� ��������
		String onControl = request.getParameter(FIELD_ON_CONTROL);
		if(onControl != null) {
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
		
		// ��������������� ����
		String preliminaryTerm = request.getParameter(FIELD_PRELIMINARY_TERM);
		if (preliminaryTerm != null && !preliminaryTerm.trim().equals("")) {
			try {
				Date preTermDate = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).parse(preliminaryTerm + " 23:59:59");
				sessionBean.setPreliminaryTerm(preTermDate);
			} catch (Exception e) {
				logger.error("Bad format date of term:", e);
			}
		} else {
			sessionBean.setPreliminaryTerm(null);
		}
	}
		
		
	private void initBaseCardProperties(ActionRequest request, MassResolutionPortletSessionBean sessionBean, DataServiceBean serviceBean) {
		formResolutionHeader(request, sessionBean);
		sessionBean.setSwitchNavigatorLink(ARMUtils.retrieveSwitchNavigatorButton(request, serviceBean));
	}

	/**
	 * Forms the header of create/edit resolution page.
	 * 
	 * @param request
	 * @param sessionBean
	 */
	private void formResolutionHeader(ActionRequest request, MassResolutionPortletSessionBean sessionBean) {
		StringBuffer theHeaderBuf = new StringBuffer(100);
		
		theHeaderBuf.append(getResourceBundle(request.getLocale()).getString("header.massResolution"));

		if (sessionBean.getRegNumber() != null) {
			theHeaderBuf.append(" ").append(getResourceBundle(request.getLocale()).getString("header.onDocument")).append(": ");
			theHeaderBuf.append(sessionBean.getTypeDoc()).append(" �");
			theHeaderBuf.append(sessionBean.getRegNumber());
		}

		sessionBean.setHeader(theHeaderBuf.toString());
	}

	private MassResolutionPortletSessionBean getSessionBean(
			PortletRequest request) {
		PortletSession session = request.getPortletSession();
		MassResolutionPortletSessionBean result = (MassResolutionPortletSessionBean) session
				.getAttribute(SESSION_BEAN);
		if (result == null) {
			result = createSessionBean(request);
			session.setAttribute(SESSION_BEAN, result);
		}
		return result;
	}
	
	public static MassResolutionPortletSessionBean getPortletSessionBean(PortletRequest request) {
		PortletSession session = request.getPortletSession();
		MassResolutionPortletSessionBean result = (MassResolutionPortletSessionBean) session
				.getAttribute(SESSION_BEAN);
		if (result == null) {
			return null;
		}
		return result;
	}
	

	@SuppressWarnings("unchecked")
	private MassResolutionPortletSessionBean createSessionBean(
			PortletRequest request) {
		MassResolutionPortletSessionBean result = new MassResolutionPortletSessionBean();

		DataServiceBean serviceBean = PortletUtil.createService(request);

		// �������� �������� "��������� ��� ��� ������������"
		Card armCard = ARMUtils.getArmSettings(serviceBean);
			

		// �������� ������� ���������� �� �������� �������������� ����������
		// "��������� ��� ��� ������������"
		// --------------------------------------------------------------
		final Set<Long> controllerIds = new HashSet<Long>();
		try {
			final Collection<Person> persons = SearchUtils.getAttrPersons(armCard, ATTR_CONTROLLER, false);
			if (persons != null) {
				for( Person person: persons) {
					try {
						final Person contrPer = (Person) serviceBean.getById(
								new ObjectId( Person.class, person.getId().getId()));
						Long controllerId = (Long) contrPer.getCardId().getId();
						controllerIds.add(controllerId);
					} catch (Exception e) {
						logger.error( "Error retrieving Person of the Controller of the 'Settings for ARM Manager':", e);
					}
				}
			}
		} catch (Exception e) {
			logger.error( "Error retrieving attribute "+ ATTR_CONTROLLER
					+ " of the 'Settings for ARM Manager' card "
					+ ((armCard == null) ? "null" : armCard.getId())
					, e);
		}
		if (controllerIds.isEmpty()) {
			//���� � ���������� �� ������ ���������
			//�� � �������� ���������� ��������� �������� ������������
			final Person contrPer = serviceBean.getPerson();
			Long controllerId = (Long) contrPer.getCardId().getId();
			controllerIds.add(controllerId);
		}
		final List<Long> cardIds = new ArrayList<Long>(controllerIds);

		// --------------------------------------------------------------

		// �������� ������ ������� ���������
		result.setStandartResolutionTexts(retrieveTypicalResolutionsOrVisas(serviceBean, armCard, ATTR_ARM_RESOL_LIST));

		// �������� �������
		try {
			final HtmlAttribute signImg = (armCard == null) ? null
					: (HtmlAttribute) armCard.getAttributeById(ATTR_RES_SIGNIMG);
			if(signImg != null){
				logger.debug( "Attribute" + ATTR_RES_SIGNIMG + "="+ signImg.getValue());
				result.setSignImage(signImg.getValue());
			} else {
				logger.warn( "Attribute" + ATTR_RES_SIGNIMG + " is null");
			}
		} catch (Exception e) {
			logger.error("Error when getting signature image: ", e);
		}
		// --------------------------------------------------------------

		// �������� ��������� �����������
		// -------------------------------------------------------------
		final List<ObjectId> immediateCardIds = new ArrayList<ObjectId>();
		try {
			final Collection<Person> companyPersons = SearchUtils.getAttrPersons(armCard, ATTR_COMPANY, false);
			if (companyPersons != null) {
				for (final Person person: companyPersons ) {
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
		final Map<Long, String> names = ARMUtils.getNameByCardIds(serviceBean, cardIds);
		
		final Map<ObjectId, String> immediate = new LinkedHashMap<ObjectId, String>();
		for(final ObjectId cardId: immediateCardIds) {
			immediate.put(cardId, StringEscapeUtils.escapeHtml(names.get(cardId.getId())));
		}
		result.setImmediateEmployees(immediate);
		
		

		// �������� �������� ������������
		// -----------------------------------------------------------
		final PersonAttribute manager = (armCard == null) ? null
				: (PersonAttribute) armCard.getAttributeById(ATTR_ARM_MANAGER);
		if (manager != null &&  manager.getValues() != null)
		result.setCurrentPerson( (Person) (manager.getValues().iterator().next()) );
		// -----------------------------------------------------------
		
		// �������� ������� �������� ��������� �� �������� �������� ���� ������������ 
		boolean isOnControl = false;
		if (armCard != null && ((ListAttribute) armCard.getAttributeById(ATTR_RESOLUTION_ON_CONTROL)) != null) {
			ReferenceValue value = ((ListAttribute) armCard.getAttributeById(ATTR_RESOLUTION_ON_CONTROL)).getValue();
			if (value != null && value.getId() != null) {
				if (VALUE_YES_ON_TCON_ONCONT == ((Long) value.getId().getId()).longValue()) {
					isOnControl = true;
				}
			}
		}
		result.setIsOnControl(isOnControl);
		if (isOnControl) {
			final Map<Long, String> controllers = new LinkedHashMap<Long, String>();
			for(final Long cardId: controllerIds) {
				controllers.put(cardId, names.get(cardId));
			}
			result.setControllers(controllers);
		}
		
		// ������������� ����� ��� �������� - ���������� ������ �������������
		result.setEmployeesSearch(getSearchPerson());

		// ���������� �������� �� ������� ������������ ���������
		try {
			final Person curPerson = (Person) serviceBean.getById(Person.ID_CURRENT);
			for(Role role : (Collection<Role>)curPerson.getRoles() )
			{
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

	private ArrayList<String> retrieveTypicalResolutionsOrVisas(DataServiceBean serviceBean, Card armCard, ObjectId attrId) {
		final ArrayList<String> textRess = new ArrayList<String>();

		Search search = new Search();
		search.setByCode(true);

		final List<SearchResult.Column> columns = new ArrayList<SearchResult.Column>();
		SearchUtils.addColumns(columns, ATTR_TYPE_RESOLUTION);
		search.setColumns(columns);

		final CardLinkAttribute linksRes = (armCard == null) ? null : (CardLinkAttribute) armCard.getAttributeById(attrId);

		// >>> (2010/02, RuSA) CardLink::getValues()
		if (linksRes != null && !linksRes.isEmpty()) {
			search.setWords(linksRes.getLinkedIds());
			// <<< (2010/02, RuSA) CardLink::getValues()

			try {
				final List<Card> resCards = SearchUtils.execSearchCards(search, serviceBean);
				if (resCards != null) {
					for (Card c : resCards) {
						final TextAttribute attr = (TextAttribute) c.getAttributeById(ATTR_TYPE_RESOLUTION);
						textRess.add(attr.getValue());
					}
				}
			} catch (Exception e) {
				logger.error("Error when searching for standard resolutions: ", e);
			}
		}
		return textRess;
	}

	public static MassResolutionPortletSessionBean getSessionBean(
			HttpServletRequest request, String namespace) {
		HttpSession session = request.getSession();
		String key = getApplicationSessionBeanKey(namespace);
		return (MassResolutionPortletSessionBean) session.getAttribute(key);
	}

	private static String getApplicationSessionBeanKey(String namespace) {
		return APPLICATION_SESSION_BEAN_PREFIX + namespace;
	}

	private ObjectId saveResolution(
			MassResolutionPortletSessionBean sessionBean, Long key, ActionRequest request) throws Exception{
		DataServiceBean serviceBean = PortletUtil.createService(request);
		CreateCard createCard = new CreateCard(TEMPLATE_RESOLUTION);
		createCard.setLinked(true);
		Card card = null;
		ObjectId cardId = null;
		try {
			ObjectId mainDocCardId = sessionBean.getParentId();
			Card mainDocCard = (Card)serviceBean.getById(mainDocCardId);
			createCard.setParent(mainDocCard);
			card = (Card) serviceBean.doAction(createCard);

			setResolutionAttributesFromSessionBean(card, sessionBean, serviceBean, key);
			
			LinkAttribute mainDocAttr = (LinkAttribute) card.getAttributeById(ATTR_MAINDOC);
			mainDocAttr.addLinkedId((Long) mainDocCardId.getId());
			LinkAttribute byDocLink = (LinkAttribute) card.getAttributeById(ATTR_DOCB_BYDOC);
			byDocLink.addLinkedId((Long) mainDocCardId.getId());
			
			serviceBean.setUser(new SystemUser());
			cardId = ((AsyncDataServiceBean) serviceBean).saveObject(card, ExecuteOption.SYNC);
			return cardId;
		} catch (Exception e) {
			logger.error("Error saving resolution:", e);
			// ��� ���������� ��������� ��������� �� ��������
			// ToDo: ������������ esc-������������������, ����� � ������� ������ ���� ���������� ��������� �� ������
			throw new Exception(e.getMessage().replace(Matcher.quoteReplacement("\n"), "\\n").replaceAll(Matcher.quoteReplacement("\t"), "\\t").replaceAll(Matcher.quoteReplacement("\r"), "\\r"));	// esc-������������������ �������� ������ � ��������� �������� �� �������������� �������);
		} finally {
			// � ����� �� �������� ��������������
			if (cardId!=null)
				unlockQuickly(cardId, serviceBean);
		}
//		return null;
	}

	/**
	 * @param sessionBean
	 * @param request
	 * @param resolution
	 * @return
	 */
	private ObjectId saveControl(MassResolutionPortletSessionBean sessionBean,
			ActionRequest request) {
		DataServiceBean serviceBean = PortletUtil.createService(request);
		CreateCard createCard = new CreateCard(TEMPLATE_PERSONAL_CONTROL);
		Card card = null;
		try {
			card = (Card) serviceBean.doAction(createCard);
			setAttributePersonControl(card, sessionBean);
			ObjectId cardId = ((AsyncDataServiceBean) serviceBean).saveObject(card, ExecuteOption.SYNC);
			unlockQuickly(cardId, serviceBean);
			return cardId;
		} catch (Exception e) {
			logger.error("Error saving card 'On personal control':", e);
		}
		return null;
	}

	// ���������� �������� ��� ���������� � ������������
	private void update(MassResolutionPortletSessionBean sessionBean,
			ActionRequest request) throws Exception {

		ObjectId resolutionCardId = null;
		Set<ObjectId> resolutions = new HashSet<ObjectId>(sessionBean.getResponsibles().size());
		for(Long key : sessionBean.getResponsibles().keySet()) {
			// ��������� ���������
			resolutionCardId = saveResolution(sessionBean, key, request);
			resolutions.add(resolutionCardId);
		}
		sessionBean.setIdsResolution(resolutions);
	}

	// ������������� ������ � fromCard �� toCard ����� �������������� attrId
	public void setCardLink(ObjectId fromCardId, ObjectId attrId, ObjectId toCardId, DataServiceBean serviceBean) throws Exception{
		boolean locked = false;
		try {
			serviceBean.doAction(new LockObject(fromCardId));
			locked = true;

			final Card fromCard = (Card) serviceBean.getById(fromCardId);

			final CardLinkAttribute attr = (CardLinkAttribute)fromCard.getAttributeById(attrId);
			attr.addLinkedId(toCardId);

			// (YNikitin, 2013/07/23) ��������� ������ ���� ������� ��, ����� �� ���������� ��������� �������� ������ ��������� ����������� ����� ��
			OverwriteCardAttributes overwrite_action = new OverwriteCardAttributes();
			overwrite_action.setCardId( fromCardId );
			overwrite_action.setAttributes( Collections.singletonList(attr) );
			overwrite_action.setInsertOnly( false );
			((AsyncDataServiceBean) serviceBean).doAction(overwrite_action);

			//((AsyncDataServiceBean) serviceBean).saveObject(fromCard, ExecuteOption.SYNC);
		} catch (Exception e) {
			logger.error("Error updating parent card:", e);
			// ToDo: ������������ esc-������������������, ����� � ������� ������ ���� ���������� ��������� �� ������
			throw new Exception(e.getMessage().replace(Matcher.quoteReplacement("\n"), "\\n").replaceAll(Matcher.quoteReplacement("\t"), "\\t").replaceAll(Matcher.quoteReplacement("\r"), "\\r"));	// esc-������������������ �������� ������ � ��������� �������� �� �������������� �������);
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

	private void initFromParentCard(PortletRequest request, MassResolutionPortletSessionBean sessionBean)  throws PortletException {

		DataServiceBean serviceBean = PortletUtil.createService(request);
		
		Card parentCard = getCardById(sessionBean.getParentId(), serviceBean);
		
		CardLinkAttribute docLinks = parentCard.getCardLinkAttributeById(ATTR_DOCLINKS);
		
		Map<ObjectId, String> attachedFilesMap = ARMUtils.getAttachedFilesMap(serviceBean, docLinks);
		
		sessionBean.setBaseCardAttachedFiles(attachedFilesMap);

		CardLinkAttribute typeDocAttr = (CardLinkAttribute)parentCard.getAttributeById(JBR_INFD_TYPEDOC);
		if(!typeDocAttr.isEmpty()){
			Card typeDocCard = (Card) getCardById(typeDocAttr.getSingleLinkedId(), serviceBean);
			StringAttribute typeDocAttrName = (StringAttribute) typeDocCard.getAttributeById(ATTR_NAME);
			sessionBean.setTypeDoc(typeDocAttrName.getValue());
		}else{
			sessionBean.setTypeDoc("");
		}

		StringAttribute regNumAttr = (StringAttribute)parentCard.getAttributeById(JBR_REGD_REGNUM);
		sessionBean.setRegNumber(regNumAttr.getValue());
		
		
		// resolution's short description
		TextAttribute theShortDescrAttr = null;
		if (TEMPLATE_OG.equals(parentCard.getTemplate())) {
			theShortDescrAttr = (TextAttribute) parentCard.getAttributeById(ATTR_SHORT_CONTEXT);
		} else {
			theShortDescrAttr = (TextAttribute) parentCard.getAttributeById(ATTR_SHORT_DESC);
		}

		if (theShortDescrAttr != null) {
			sessionBean.setShortDescription(theShortDescrAttr.getValue());
		}
	}
	
	private Card getCardById(ObjectId cardId,
			DataServiceBean serviceBean) throws PortletException {
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

	// ��������� null, ���� �������� '�� ������ ��������' �� ���������� �
	// ��������� �� �� ����
	// ��������� id �������� ��� �������� ��� ���������� ��������
	private ObjectId updateControl(
			MassResolutionPortletSessionBean sessionBean, ActionRequest request) {
		// ���� ������� "�� ������ ��������" �� ����������� � ���� �������� "�� ��������" (������ ��� ����� ��������)
		if (sessionBean.getIdControlCard() == null
				&& sessionBean.getControlTerm() == null) { // ���������
															// �������� "��
															// ������ ��������"
															// �� ����
			return null;
		}
		// ���� �������� ��� �� ����, �� ������� ��
		if (sessionBean.getIdControlCard() == null) {
			return saveControl(sessionBean, request);
		}
		final DataServiceBean serviceBean = PortletUtil.createService(request);
		boolean locked = false;
		final ObjectId cardId = new ObjectId(Card.class, sessionBean.getIdControlCard());
		try {
			serviceBean.doAction(new LockObject(cardId));
			locked = true;
			Card card = (Card) serviceBean.getById(cardId);
			setAttributePersonControl(card, sessionBean);
			((AsyncDataServiceBean) serviceBean).saveObject(card, ExecuteOption.SYNC);
			return cardId;
		} catch (Exception e) {
			logger.error(
					"Error to update the card 'on personal control':",
					e);
		} finally {
			if (locked) {
				unlockQuickly(cardId, serviceBean);
			}
		}
		return null;
	}

	// ���������� ������������ ����� �������� �������� � Person ������� �� ���
	// ���������
	// Collection ids - ��������� id ��������� ���� Long
	// return Map Long -> Person
	@SuppressWarnings("unchecked")
	private Map<Long, Person> getPersonsByCardIds(DataServiceBean serviceBean, Collection<Long> ids) {
		Map<Long, Person> map = null;
		try {
			final List<ObjectId> cardIds = new ArrayList<ObjectId>(ids.size());
			final Iterator<Long> i = ids.iterator();
			while (i.hasNext()) {
				cardIds.add(new ObjectId(Card.class, i.next()));
			}

			final PersonCardIdFilter filter = new PersonCardIdFilter();
			filter.setCardIds(cardIds);
			final Collection<Person> persons = serviceBean.filter(Person.class, filter); // ��������� Person ����-� ��������� ������
			if (persons.size() != cardIds.size()) {
				logger.warn("There is persons with wrong card_id. Values can be empty or non-unique.");
			}

			map = new LinkedHashMap<Long, Person>();
			for( Person p: persons) {
				map.put((Long) p.getCardId().getId(), p);
			}
		} catch (Exception e) {
			logger.error("Error by search Person:", e);
		}
		return map;
	}

	/* ���������� ���, ��������� �� id �������� ������������ � ��� �����
	 * persons - ������ ������, ������� ���� ����������
	 */
	private Map<Long, String> getCardsAndNamesByPersons(Collection<Person> persons){
		Map<Long, String> result = new HashMap<Long, String> ();
		for(Person p:persons){
			result.put((Long)p.getCardId().getId(), p.getFullName());
		}
		return result;
	}
	private void setResolutionAttributesFromSessionBean(Card card,
			MassResolutionPortletSessionBean sessionBean,
			DataServiceBean serviceBean,
			Long key) {
		// �������� ������������ ����� card_id � person_id ������������
		final List<Long> cardIds = new ArrayList<Long>();

		cardIds.addAll(sessionBean.getResponsibles().keySet());
		final Set<Long> controllerIds = sessionBean.getControllers().keySet();
		cardIds.addAll(controllerIds);
		// map of all persons, just for convenience 
		final Map<Long, Person> persons = getPersonsByCardIds(serviceBean, cardIds);

		// executor
		PersonAttribute persAttr = (PersonAttribute) card.getAttributeById(ATTR_EXECUTOR);
		persAttr.clear();
		
		List<Person> personsCol = new ArrayList<Person>();
		final Person itemP = persons.get(key);
		if(itemP != null) {
			personsCol.add(itemP);
			persAttr.setValues(personsCol);
		}
		
		// resolution
		String textResolution = sessionBean.getResolutionText();
		((TextAttribute) card.getAttributeById(ATTR_RESOLUT))
				.setValue(textResolution);

		// deadline
		Date term = sessionBean.getTermValue();
		((DateAttribute) card.getAttributeById(ATTR_TERM)).setValue(term);

		// preliminary deadline
		if (sessionBean.getIsOnControl()) {
			Date preliminaryTerm = sessionBean.getPreliminaryTerm();
			((DateAttribute) card.getAttributeById(ATTR_PRELIMINARY_TERM)).setValue(preliminaryTerm);
		}

		// controller
		final List<Person> controller = new ArrayList<Person>();
		for ( final Iterator<Long> iter = controllerIds.iterator(); iter.hasNext(); ) {
			controller.add( persons.get(iter.next()));
		}
		((PersonAttribute)card.getAttributeById(ATTR_TCON_INSPECTOR)).setValues(controller);
	
		// on control
		final ReferenceValue onCont = new ReferenceValue();
		long onContVal = VALUE_NO_ON_TCON_ONCONT;
		if(Boolean.TRUE.equals(sessionBean.getIsOnControl())) {
			onContVal = VALUE_YES_ON_TCON_ONCONT;
		}
		onCont.setId(onContVal);
		((ListAttribute)card.getAttributeById(ATTR_TCON_ONCONT)).setValue(onCont);

		// signature
		Attribute attr = card.getAttributeById(ATTR_SIGNATURE);
		if (attr != null) {
			String signature = sessionBean.getSignature();
			((HtmlAttribute) card.getAttributeById(ATTR_SIGNATURE))
					.setValue(signature);
		}

		attr = (PersonAttribute)card.getAttributeById(ATTR_SIGNATORY);
		if ( ((PersonAttribute)attr).getPerson() == null ) {
		    ((PersonAttribute) attr).setPerson( sessionBean.getCurrentPerson() );
		}

		attr  = card.getAttributeById(ATTR_DATESIGN);
		if (attr!=null)
			((DateAttribute) attr)
				.setValue(new Date(System.currentTimeMillis()));
		
		// add attachments
		final CardLinkAttribute attrDocLinks = (CardLinkAttribute) card.getAttributeById(ATTR_DOCLINKS);
		if (attrDocLinks != null) {
			attrDocLinks.setIdsLinked(sessionBean.getAttachedFiles().keySet());
		}
	}

	private void doExit(PortletRequest request, ActionResponse response,
			MassResolutionPortletSessionBean sessionBean, boolean isDone)
			throws IOException {
		PortletSession session = request.getPortletSession();
		String namespace = request.getParameter(FIELD_NAMESPACE);
		session.removeAttribute(SESSION_BEAN);
		session.removeAttribute(getApplicationSessionBeanKey(namespace),
				PortletSession.APPLICATION_SCOPE);
		if (isDone) {
			response.sendRedirect(sessionBean.getDoneLink());
		} else {
			response.sendRedirect(sessionBean.getBackLink());
		}
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
		// (BR4J00033862, YNikitin, 2013/10/29) ��� ����������� ������ ���������� ������ ����������� ��������� ����� �������, �.�. �������� ���������� �� ����� ���
		search.getFilter().setCurrentUserRestrict(Search.Filter.CU_READ_PERMISSION);
		final List<String> states = new ArrayList<String>(1);
		states.add(CARDSTATE_ACTIVE_USER.getId().toString());
		search.setStates(states);

		return search;
	}

	// ���������� �������� "������ ��������" ��������� � ������������ ����������
	// ���� ����� �������� �� ������� �� ������������� null
	private void searchPersonalControlCard(
			MassResolutionPortletSessionBean sessionBean,
			DataServiceBean serviceBean) {
		// ���� �������� "������ ��������" ��������� � ���������� ����������
		final Search search = new Search();
		search.setWords("");
		search.setByAttributes(true);

		search.addCardLinkAttribute(ATTR_PCON_DOC, sessionBean.getParentId());
		search.addPersonAttribute(ATTR_PCON_PERSON, sessionBean.getCurrentPerson().getId());

		// ������ "������ ��������"
		final List<DataObject> templates = new ArrayList<DataObject>(1);
		templates.add(DataObject.createFromId(TEMPLATE_PERSONAL_CONTROL));
		search.setTemplates(templates);

		// ������� "���� ��������"
		final List<SearchResult.Column> columns = new ArrayList<SearchResult.Column>();
		SearchUtils.addColumns(columns, ATTR_PCON_DATE);
		search.setColumns(columns);

		try {
			final Collection<Card> controlCards = SearchUtils.execSearchCards(search, serviceBean); 
				// ((SearchResult) serviceBean.doAction(search)).getCards();
			if (controlCards != null && controlCards.size() > 0) {
				if (controlCards.size() > 1) {
					logger.warn("More than one card, \"Personal Control\" links to the order id="
							+ sessionBean.getParentId());
				}
				final Card control = controlCards.iterator().next();
				sessionBean.setIdControlCard((Long) control.getId().getId());

				final DateAttribute term = (DateAttribute) control.getAttributeById(ATTR_PCON_DATE);
				if (term != null) {
					sessionBean.setControlTerm(term.getValue());
				}
			}
		} catch (Exception e) {
			logger.error("Error search of card \"Person control\":", e);
		}
	}

	private void setAttributePersonControl(Card card,
			MassResolutionPortletSessionBean sessionBean) {
		// ����������
		Person controller = sessionBean.getCurrentPerson();
		((PersonAttribute) card.getAttributeById(ATTR_PCON_PERSON)).setPerson(controller);

		final CardLinkAttribute attr = (CardLinkAttribute)card.getAttributeById(ATTR_PCON_DOC);
		attr.addSingleLinkedId(sessionBean.getParentId());

		// ���� ��������
		Date term = sessionBean.getControlTerm();
		((DateAttribute) card.getAttributeById(ATTR_PCON_DATE)).setValue(term);

		// ��������� ���� NAME ��� ����, ��� �� ������� �����
		((StringAttribute) card.getAttributeById(ATTR_NAME))
				.setValue("������ ��������");
	}

	private void unlockQuickly(ObjectId objectId, DataServiceBean serviceBean) {
		try {
			serviceBean.doAction(new UnlockObject(objectId));
		} catch (Exception e) {
			logger.error("Couldn't unlock object " + objectId.toString(), e);
		}
	}
	
	private void signCardHandler(ActionRequest request, DataServiceBean serviceBean) {
		try {
			String signatureParam = request.getParameter(PARAM_SIGNATURE);
			if(StringUtils.hasLength(signatureParam)) 
				DigitalSignatureUtil.storeDigitalSignature(signatureParam, null, serviceBean, true);		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
