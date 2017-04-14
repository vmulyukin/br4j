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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.CreateCard;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.OverwriteCardAttributes;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.card.util.ARMUtils;
import com.aplana.dbmi.jbr.util.CardUtils;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.DatedTypedCardLinkAttribute;
import com.aplana.dbmi.model.HtmlAttribute;
import com.aplana.dbmi.model.LinkAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.Reference;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.Role;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.SystemRole;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.TextAttribute;
import com.aplana.dbmi.model.TypedCardLinkAttribute;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.model.util.DateUtils;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.portlet.RequestToChangeConsPortletSessionBean.NewConsiderator;
import com.aplana.dbmi.service.AsyncDataServiceBean;
import com.aplana.dbmi.service.AsyncDataServiceBean.ExecuteOption;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.PortletUtil;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.dbmi.service.SystemUser;
import com.aplana.util.DigitalSignatureUtil;
import com.aplana.web.tag.util.StringUtils;


public class RequestToChangeConsPortlet extends GenericPortlet {
	private Log logger = LogFactory.getLog(getClass());
	
	public static final String SESSION_BEAN = "requestToChangePortletSessionBean";
	public static final String APPLY_DS = "MI_APPLY_SIGNATURE";
	public static final String DS_PARAMS = "MI_SIGNATURE_PARAMS";
	private static final String APPLICATION_SESSION_BEAN_PREFIX = "requestToChangePortletSessionBean:";

	public static final String CONFIG_FOLDER = "dbmi/card/";
	public static final String JSP_FOLDER = "/WEB-INF/jsp/requestsToChange/";

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
	public static final String FIELD_NEW_CONSIDERATORS = "newConsiderators";
	public static final String FIELD_CONSIDERATORS = "considerators";
	public static final String FIELD_RESPONSIBLE = "responsible";
	public static final String FIELD_SUPPORTS_DS = "supportsDS";
	public static final String FIELD_RESOLUTION_ATTACHMENTS = "resolutionAttachments";

	public static final String FIELD_NAMESPACE = "namespace";
	public static final String FIELD_COMMENT = "comment";
	public static final String FIELD_TERM_CHANGE = "term_change";
	public static final String FIELD_TERM_CHANGE1 = "term_change1";
	public static final String FIELD_REQUEST_TYPE = "requestType";
	public static final String FIELD_OTHER_EXEC = "otherExec";
	public static final String FIELD_SIGNATURE = "signature";
	public static final String FIELD_FILE_ATTACHMENT = "fileAttachment";

	// �������� ������� "���������"
	public static final ObjectId ATTR_CONSID = ObjectId.predefined(
			CardLinkAttribute.class, "jbr.request.cons");
	public static final ObjectId ATTR_COMMENT = ObjectId.predefined(
			TextAttribute.class, "jbr.request.comm");
	public static final ObjectId ATTR_SHORT_DESC = ObjectId.predefined(
			TextAttribute.class, "jbr.document.title");
	public static final ObjectId ATTR_SHORT_CONTEXT = ObjectId.predefined(
			TextAttribute.class, "jbr.shortcontext");

	public static final ObjectId ATTR_NEWS = ObjectId.predefined(
			DatedTypedCardLinkAttribute.class, "jbr.request.new");
	public static final ObjectId ATTR_TERM = ObjectId.predefined(
			DateAttribute.class, "jbr.resolutionTerm");
	public static final ObjectId ATTR_REQUEST_TYPE = ObjectId.predefined(
			ListAttribute.class, "jbr.request.type");
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
	public static final ObjectId ATTR_MAINDOC_REQUEST = ObjectId.predefined(
			CardLinkAttribute.class, "jbr.maindoc.request");
	
	public static final ObjectId WFM_DRAFT_CONSID = ObjectId.predefined(
			WorkflowMove.class, "jbr.cc_request.to.consideration");
	
	public static final ObjectId ATTR_DOCLINKS = ObjectId.predefined(CardLinkAttribute.class, "jbr.files");
	
	public static final ObjectId ATTR_REPORT_ATTACHMENTS = ObjectId.predefined(CardLinkAttribute.class, "jbr.report.attachments");
	
	public static final ObjectId JBR_INFD_TYPEDOC = ObjectId.predefined(CardLinkAttribute.class, "jbr.reg.doctype");
	
	public static final ObjectId JBR_REGD_REGNUM = ObjectId.predefined(StringAttribute.class, "regnumber");
	public static final ObjectId JBR_REGD_DATAREG = ObjectId.predefined(DateAttribute.class, "regdate");

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
	public static final ObjectId ATTR_CHANGE_TERM = ObjectId.predefined(
			DateAttribute.class, "jbr.request.change");
	public static final ObjectId VALUE_BOSS_CONTROL = ObjectId.predefined(
			ReferenceValue.class, "jbr.incoming.typecontrol.boss");
	public static final ObjectId VALUE_ON_RESP = ObjectId.predefined(
			ReferenceValue.class, "jbr.incoming.control.yes");
	public static final ObjectId REF_RESP = new ObjectId(Reference.class, "ADMIN_27736");

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
	public static final ObjectId TEMPLATE_REQUEST = ObjectId.predefined(
			Template.class, "jbr.request.change");

	public static final ObjectId CARDSTATE_ACTIVE_USER = ObjectId.predefined(
			CardState.class, "user.active");
	public static final ObjectId DICTIONARY_NEW = ObjectId.predefined(
			CardState.class, "dictionaryNew");

	public static final ObjectId ATTR_RES_SIGNIMG = ObjectId.predefined(
			HtmlAttribute.class, "boss.settings.image1");
	
	public static final ObjectId REF_REQUEST_TYPE = new ObjectId(Reference.class, "JBR_REQUEST_TYPE_LST");

	public static final ObjectId CONSIDERATION_CANCELLED_STATE_ID = ObjectId.state("poruchcancelled");

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

	@Override
	protected void doView(RenderRequest request, RenderResponse response)
			throws PortletException, PortletSecurityException, IOException {
		
		final RequestToChangeConsPortletSessionBean sessionBean = getSessionBean(request);
		
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
		
			// ���� ��������� - �������� ������ �������
			if (STATE_INIT_CREATE.equalsIgnoreCase(sessionBean.getStateInit())){
				// ������� ����� �������� ���������, ����� ��������� ����������
				try{
					/*DataServiceBean serviceBean = PortletUtil.createService(request);
					CreateCard createCard = new CreateCard(TEMPLATE_RESOLUTION);
					createCard.setLinked(true);
					Card card = null;
					ObjectId mainDocCardId = sessionBean.getParentId();
					Card mainDocCard = (Card)serviceBean.getById(mainDocCardId);
					createCard.setParent(mainDocCard);
					card = (Card) serviceBean.doAction(createCard);	*/			

				} catch (Exception e){
					logger.error("Error by create new resolution and filling control-fields:", e);
				}
			}
			/*final JSONArray news = new JSONArray();
			try {
				final Map<Long, String> map = sessionBean.getNews();
				if (map != null) {
					for(final Long cardId : map.keySet() ) {
						final JSONObject j = new JSONObject();
						j.put("cardId", cardId);
						j.put("name", map.get(cardId));
						news.put(j);
					}
				}
			} catch (JSONException e) {
				logger.error("JSON exception caught:", e);
			}*/
			//request.setAttribute("news", news);
			request.setAttribute(FIELD_RESOLUTION_ATTACHMENTS, getAttachmentsJSONData(sessionBean.getAttachedFiles()));
			page = "../requestsToChange/requestToChangeConsiderationData.jsp";
		

		PortletRequestDispatcher rd = getPortletContext().getRequestDispatcher(
				JSP_FOLDER + page);
		rd.include(request, response);
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
			RequestToChangeConsPortletSessionBean sessionBean = getSessionBean(request);
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
					Card parent;
					try {
						parent = serviceBean.getById(objId);
						sessionBean.setParent(parent);
						initFromParentCard(request, sessionBean);
					} catch (DataException e) {
						e.printStackTrace();
					} catch (ServiceException e) {
						e.printStackTrace();
					}
				} else {
					doExit(request, response, sessionBean, false);
				}
				
				// ���� ���������
				JSONArray ja = new JSONArray();
				JSONObject jo;
				try {
					Collection<ReferenceValue> attrCol = serviceBean.listChildren(REF_REQUEST_TYPE, ReferenceValue.class);
					for(ReferenceValue rv : attrCol) {
						jo = new JSONObject();
						jo.put("id", rv.getId().getId());
						jo.put("label", rv.getValue());
						ja.put(jo);
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (DataException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ServiceException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				sessionBean.setJsonTypes(ja.toString());
				
				// ������������� ���������������
				ja = new JSONArray();
				try {
					Collection<ReferenceValue> attrCol = serviceBean.listChildren(REF_RESP, ReferenceValue.class);
					for(ReferenceValue rv : attrCol) {
						jo = new JSONObject();
						jo.put("id", rv.getId().getId());
						jo.put("label", rv.getValue());
						ja.put(jo);
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (DataException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ServiceException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				sessionBean.setJsonResp(ja.toString());
				
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
					doCreate(sessionBean,serviceBean);
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
		
	private void parseFormValuesIntoSessionBeanFromRequest(RequestToChangeConsPortletSessionBean sessionBean,
			ActionRequest request) {

		// ���������������
		String c = request.getParameter(FIELD_CONSIDERATORS);
		if (c != null) {
			if (!"".equals(c.trim())) {
				sessionBean.setConsideratorCardId(Long.valueOf(c.trim()));
			}
		}

		// ����� ���������������
		String p = request.getParameter(FIELD_NEW_CONSIDERATORS);
		if (p != null && !"".equals(p.trim())) {
			final List<NewConsiderator> news = new ArrayList<NewConsiderator>();
			NewConsiderator nc;
			final String[] peoples = p.split(SEPARATOR);
			for (int i = 0; i < peoples.length; i++) {
				final String[] value = peoples[i].split(":");
				nc = new NewConsiderator();
				nc.setConsiderator(Long.valueOf(value[0]));
				if(value.length < 2) {
					nc.setResp(VALUE_NO_ON_TCON_ONCONT);
				} else if (StringUtils.hasText(value[1])) {
					nc.setResp(Long.valueOf(value[1].trim()));
				} else {
					nc.setResp(VALUE_NO_ON_TCON_ONCONT);
				}
				if(value.length < 3) {
					nc.setTerm(DateUtils.addDaysToCurrent(29));
				} else {
					Date d = parseDate(value[2], FORMATTER_YYYY_MM_DD, true);
					if(d == null) {
						nc.setTerm(DateUtils.addDaysToCurrent(29));
					} else {
						nc.setTerm(d);
					}
				}
				news.add(nc);
			}
			sessionBean.setNews(news);
		}
		
		// ��� �������
		String t = request.getParameter(FIELD_REQUEST_TYPE);
		if (t != null) {
			final List<Long> types = new ArrayList<Long>();
			if (!"".equals(t.trim())) {
				types.add(Long.valueOf(t.trim()));
				sessionBean.setTypes(types);
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
		
		// �����������
		String comment = request.getParameter(FIELD_COMMENT);
		if (comment != null && !comment.trim().equals("")) {
			sessionBean.setComment(comment);
		}
		
		// ��������� �����
		String date = request.getParameter(FIELD_TERM_CHANGE);
		sessionBean.setChangeTerm(parseDate(date, FORMATTER_YYYY_MM_DD));
		
		// �������
		String signature = request.getParameter(FIELD_SIGNATURE);
		if (signature != null && !signature.trim().equals("")) {
			sessionBean.setSignature(signature);
		}
		
	}
	
	
	private Date parseDate(String date, SimpleDateFormat dateFormatter, boolean nullIfError) {
		SimpleDateFormat formatter = null;

		if (date != null && !date.trim().equals("")) {
			// ���� ������
			formatter = dateFormatter;
			if (formatter != null) {
				try {
					final Date newDate = formatter.parse(date);
					return newDate;
				} catch (ParseException e) {
					logger.error("Bad format date of term:", e);
					if(nullIfError)
						return null;
				}
			}
		}
		return new Date();
	}
	
	private Date parseDate(String date, SimpleDateFormat dateFormatter) {
		return parseDate(date, dateFormatter, false);
	}
		
		
	private void initBaseCardProperties(ActionRequest request, RequestToChangeConsPortletSessionBean sessionBean, DataServiceBean serviceBean) {
		formResolutionHeader(request, sessionBean);
		sessionBean.setSwitchNavigatorLink(ARMUtils.retrieveSwitchNavigatorButton(request, serviceBean));
	}

	/**
	 * Forms the header of create/edit resolution page.
	 * 
	 * @param request
	 * @param sessionBean
	 */
	private void formResolutionHeader(ActionRequest request, RequestToChangeConsPortletSessionBean sessionBean) {
		StringBuffer theHeaderBuf = new StringBuffer(100);
		
		theHeaderBuf.append(getResourceBundle(request.getLocale()).getString("header.requestToChangeConsideration"));

		if (sessionBean.getRegNumber() != null) {
			theHeaderBuf.append(" ").append(getResourceBundle(request.getLocale()).getString("header.onDocument")).append(" � ");
			theHeaderBuf.append(sessionBean.getRegNumber());
		}
		
		if (sessionBean.getRegDate() != null) {
			theHeaderBuf.append(" �� ");
			theHeaderBuf.append(new SimpleDateFormat(RequestToChangeConsPortletSessionBean.DEFAULT_TIME_PATTERN).format(sessionBean.getRegDate()));
		}

		sessionBean.setHeader(theHeaderBuf.toString());
	}

	private RequestToChangeConsPortletSessionBean getSessionBean(PortletRequest request) {
		PortletSession session = request.getPortletSession();
		RequestToChangeConsPortletSessionBean result = (RequestToChangeConsPortletSessionBean) session
				.getAttribute(SESSION_BEAN);
		if (result == null) {
			result = createSessionBean(request);
			session.setAttribute(SESSION_BEAN, result);
		}
		return result;
	}
	
	public static RequestToChangeConsPortletSessionBean getPortletSessionBean(PortletRequest request) {
		PortletSession session = request.getPortletSession();
		RequestToChangeConsPortletSessionBean result = (RequestToChangeConsPortletSessionBean) session
				.getAttribute(SESSION_BEAN);
		if (result == null) {
			return null;
		}
		return result;
	}
	

	@SuppressWarnings("unchecked")
	private RequestToChangeConsPortletSessionBean createSessionBean(PortletRequest request) {
		RequestToChangeConsPortletSessionBean result = new RequestToChangeConsPortletSessionBean();

		DataServiceBean serviceBean = PortletUtil.createService(request);

		// �������� �������� "��������� ��� ��� ������������"
		Card armCard = ARMUtils.getArmSettings(serviceBean);
			

		// �������� �������� ������������
		// -----------------------------------------------------------
		final PersonAttribute manager = (armCard == null) ? null
				: (PersonAttribute) armCard.getAttributeById(ATTR_ARM_MANAGER);
		if (manager != null &&  manager.getValues() != null)
		result.setCurrentPerson( (Person) (manager.getValues().iterator().next()) );
		// -----------------------------------------------------------
		
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


	public static RequestToChangeConsPortletSessionBean getSessionBean(
			HttpServletRequest request, String namespace) {
		HttpSession session = request.getSession();
		String key = getApplicationSessionBeanKey(namespace);
		return (RequestToChangeConsPortletSessionBean) session.getAttribute(key);
	}

	private static String getApplicationSessionBeanKey(String namespace) {
		return APPLICATION_SESSION_BEAN_PREFIX + namespace;
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

	private void initFromParentCard(PortletRequest request, RequestToChangeConsPortletSessionBean sessionBean)  throws PortletException, DataException, ServiceException {

		DataServiceBean serviceBean = new DataServiceBean();
		serviceBean.setAddress("127.0.0.1");
		serviceBean.setUser(new SystemUser());

		Card parentCard = sessionBean.getParent();
		Person currUser = sessionBean.getCurrentPerson();
		CardLinkAttribute considDocs = parentCard.getAttributeById(considSet);
		List<Person> assists;
		if(!considDocs.isEmpty()){

			JSONArray jsonConsiderators = new JSONArray();

			for(ObjectId id : considDocs.getIdsLinked()) {
				Card considCard = (Card) getCardById(id, serviceBean);
				PersonAttribute pa = considCard.getAttributeById(considPerson);

				//��������� ���������� �� ������������ � ������, ��� �� ����� ���� ������� �������� ����������������
				if (!CONSIDERATION_CANCELLED_STATE_ID.equals(considCard.getState())) {
					try {
						JSONObject jo = new JSONObject();
						jo.put("id", (Long) id.getId());
						jo.put("label", pa.getPersonName());
						jsonConsiderators.put(jo);
					} catch (JSONException e) {
						logger.error("Unable to add considerator from card " + id + "due to " + e.getMessage(), e);
					}
				}

				if (sessionBean.getCurrentConsidDoc() == null) {
					assists = getAssistants(pa.getPerson(), serviceBean);
					assists.add(pa.getPerson());
					if(assists.contains(currUser)) {
						sessionBean.setCurrentConsidDoc(considCard);
					}
				}
			}
			sessionBean.setJsonConsiderators(jsonConsiderators.toString());
		} else {
			throw new DataException("No one consideration card in main card " + ObjectIdUtils.getCardIdToString(parentCard));
		}

		StringAttribute regNumAttr = parentCard.getAttributeById(JBR_REGD_REGNUM);
		sessionBean.setRegNumber(regNumAttr.getValue());
		
		DateAttribute regDateAttr = parentCard.getAttributeById(JBR_REGD_DATAREG);
		sessionBean.setRegDate(regDateAttr.getValue());
		
		
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
	
	private List<Person> getAssistants(Person person, DataServiceBean serviceBean) throws DataException, ServiceException 
	{
		List<Person> assistants = new ArrayList<Person>();
		List<Card> arm = null;
		final Search search = new Search();
		search.setByAttributes(true);
		search.setTemplates(Collections.singletonList(DataObject.createFromId(CardUtils.TEMPL_WS_SETTINGS)));
		if (person != null)
			search.addPersonAttribute(CardUtils.ATTR_BOSS, person.getId());

		search.setColumns( Arrays.asList( new SearchResult.Column[] {
				CardUtils.createColumn(CardUtils.ATTR_BOSS), 
				CardUtils.createColumn(CardUtils.ATTR_ASSISTANT),

				CardUtils.createColumn(Attribute.ID_NAME),
				CardUtils.createColumn(CardUtils.ATTR_TEMPLATE),
				CardUtils.createColumn(CardUtils.ATTR_STATUS)
			} ));


		final SearchResult result = (SearchResult) serviceBean.doAction(search);
		if (result != null) {
			arm = result.getCards();
		}

		if (arm == null || arm.isEmpty())
			return assistants;

		final Card card = arm.iterator().next();
		final Attribute aperson = card.getAttributeById(CardUtils.ATTR_ASSISTANT);
		if (aperson instanceof PersonAttribute)
			try {
				assistants.addAll(CardUtils.getAttrPersons((PersonAttribute) aperson));
			} catch (NullPointerException e) {	}
		
		return assistants;
	}
	
	public static final ObjectId considSet = ObjectId.predefined(CardLinkAttribute.class, "jbr.exam.set");
	public static final ObjectId considPerson = ObjectId.predefined(PersonAttribute.class, "jbr.exam.person");
	
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

	private void setResolutionAttributesFromSessionBean(Card card,
			RequestToChangeConsPortletSessionBean sessionBean,
			DataServiceBean serviceBean) throws DataException, ServiceException {
		
		CardLinkAttribute cons = card.getAttributeById(ATTR_CONSID);
		cons.clear();
		if (sessionBean.getConsideratorCardId() != null) {
			cons.addSingleLinkedId(IdUtils.makeCardId(sessionBean.getConsideratorCardId()));
		} else {
			cons.addSingleLinkedId(sessionBean.getCurrentConsidDoc().getId());
		}
		
		// news
		DatedTypedCardLinkAttribute newsPers = card.getAttributeById(ATTR_NEWS);
		Long newId;
		for(NewConsiderator nc : sessionBean.getNews()) {
			newId = nc.getConsiderator();
			newsPers.addLinkedId(newId);
			newsPers.addType(newId, nc.getResp());
			newsPers.addDate(newId, nc.getTerm());
		}
		
		// change term
		if(sessionBean.getChangeTerm() != null) {
			Date term = sessionBean.getChangeTerm();
			DateAttribute changeTerm = card.getAttributeById(ATTR_CHANGE_TERM);
			changeTerm.setValue(term);
		}
		
		
		// type
		List<Long> tList = sessionBean.getTypes();
		ListAttribute typeList = card.getAttributeById(ATTR_REQUEST_TYPE);
		typeList.clear();
		ReferenceValue rv = serviceBean.getById(new ObjectId(ReferenceValue.class, tList.get(0)));
		typeList.setValue(rv);

		
		// �����������
		String text = sessionBean.getComment();
		((TextAttribute) card.getAttributeById(ATTR_COMMENT))
				.setValue(text);

		// signature
		Attribute attr = card.getAttributeById(ATTR_SIGNATURE);
		if (attr != null) {
			String signature = sessionBean.getSignature();
			((HtmlAttribute) card.getAttributeById(ATTR_SIGNATURE))
					.setValue(signature);
		}
		
		// add attachments
		final CardLinkAttribute attrDocLinks = (CardLinkAttribute) card.getAttributeById(ATTR_DOCLINKS);
		if (attrDocLinks != null) {
			attrDocLinks.setIdsLinked(sessionBean.getAttachedFiles().keySet());
		}
	}
	
	private void doCreate(RequestToChangeConsPortletSessionBean sessionBean, DataServiceBean serviceBean) throws DataException, ServiceException {
		CreateCard createCard = new CreateCard();
		createCard.setTemplate(TEMPLATE_REQUEST);
		createCard.setLinked(true);
		Card parent = new Card();
		parent.setId(sessionBean.getParent().getId());
		createCard.setParent(parent);
		Card card = (Card) serviceBean.doAction(createCard);
				
		// ��������� ������� ��������-���������
		setMainDoc(card, sessionBean);
				
		setResolutionAttributesFromSessionBean(card, sessionBean, serviceBean);
		
		ObjectId cardId = serviceBean.saveObject(card);
		
		final WorkflowMove wfm = (WorkflowMove) serviceBean.getById(WFM_DRAFT_CONSID);
		final ChangeState actionChange = new ChangeState();
		Card newCard = new Card();
		newCard.setId(cardId);
		actionChange.setCard(newCard);
		actionChange.setWorkflowMove(wfm);
		//���������
		((AsyncDataServiceBean) serviceBean).doAction(actionChange, ExecuteOption.SYNC);
        //������� ����������, ���������� ����� ������� ���������� ��������
        serviceBean.doAction(new UnlockObject(cardId));
		
	}
	
	protected void setMainDoc(Card card, RequestToChangeConsPortletSessionBean sessionBean) {
		Card parentCard = sessionBean.getParent();
		LinkAttribute linkAttr = (LinkAttribute) card.getAttributeById(ATTR_MAINDOC_REQUEST);
		if(linkAttr != null) {
			linkAttr.clear();
			linkAttr.addSingleLinkedId((Long) parentCard.getId().getId());
		}
	}

	private void doExit(PortletRequest request, ActionResponse response,
			RequestToChangeConsPortletSessionBean sessionBean, boolean isDone)
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
