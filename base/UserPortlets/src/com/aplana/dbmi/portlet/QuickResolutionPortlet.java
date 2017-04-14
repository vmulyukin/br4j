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
import java.io.InputStream;
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
import java.util.Map.Entry;
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

import com.aplana.dbmi.model.*;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.portlet.PortletFileUpload;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.aplana.cms.ContentUtils;
import com.aplana.cms.cache.CounterCache;
import com.aplana.dbmi.ConfigHolder;
import com.aplana.dbmi.Portal;
import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.CheckLock;
import com.aplana.dbmi.action.CreateCard;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.OverwriteCardAttributes;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.action.UploadFile;
import com.aplana.dbmi.card.CardPortlet;
import com.aplana.dbmi.card.DigitalSignatureConfiguration;
import com.aplana.dbmi.card.QuickResolutionPortletListEditForm;
import com.aplana.dbmi.card.util.ARMUtils;
import com.aplana.dbmi.card.util.SearchUtils;
import com.aplana.dbmi.gui.AddRefuseVisaLinkedCardAttachmentData;
import com.aplana.dbmi.gui.ListDataProvider;
import com.aplana.dbmi.gui.ListEditor;
import com.aplana.dbmi.gui.RefuseVisaLinkedCardEditor;
import com.aplana.dbmi.model.filter.PersonCardIdFilter;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.AsyncDataServiceBean;
import com.aplana.dbmi.service.AsyncDataServiceBean.ExecuteOption;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ObjectLockedException;
import com.aplana.dbmi.service.ObjectNotLockedException;
import com.aplana.dbmi.service.PortletUtil;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.dbmi.service.SystemUser;
import com.aplana.dbmi.support.action.ProcessGroupResolution;
import com.aplana.dbmi.util.JspUtils;
import com.aplana.util.DigitalSignatureUtil;
import com.aplana.web.tag.util.StringUtils;


public class QuickResolutionPortlet extends GenericPortlet {
	private Log logger = LogFactory.getLog(getClass());
	
	public static final String SESSION_BEAN = "quickResolutionPortletSessionBean";
	public static final String APPLY_DS = "MI_APPLY_SIGNATURE";
	public static final String DS_PARAMS = "MI_SIGNATURE_PARAMS";
	private static final String APPLICATION_SESSION_BEAN_PREFIX = "quickResolutionPortletSessionBean:";

	public static final String CONFIG_FOLDER = "dbmi/card/";
	public static final String JSP_FOLDER = "/WEB-INF/jsp/quickResolution/";
	public static final String MODE_SELECT_EXECUTORS = "selectExecutors";
	public static final String MODE_EDIT_RESOLUTION = "editResolution";
	public static final String MODE_VISA_SIGN_RESOLUTION = "visaSignResolution";
	public static final String MODE_GROUP_RESOLUTION = "group";

	public static final String ACTION_NEXT = "next";
	public static final String ACTION_CANCEL = "cancel";
	public static final String ACTION_BACK = "back";
	public static final String ACTION_DONE = "done";
	public static final String ACTION_UPLOAD = "upload";
	public static final String ACTION_ADD_LINKED_FILES = "addLinkedFiles";
	public static final String ACTION_DONE_PLUS = "domePlus";
	public static final String ACTION_INIT = "init"; // ����� ������ ��������
														// - �������� �����
														// ��������� ���
														// �������������� ������
	public static final String ACTION_SAVE_DS_AND_EXIT = "saveDSandExit";

	public static final String STATE_INIT_CREATE = "initCreate";
	public static final String STATE_INIT_EDIT = "initEdit";

	// ����
	public static final String TARGET_INIT_CONSIDERATION = "consideration"; // ��c���������
	public static final String TARGET_INIT_EXECUTION = "execution"; // ����������
	public static final String TARGET_INIT_SIGNING = "signing"; // ����������
	public static final String TARGET_INIT_VISA = "visa"; // ������������
	public static final String TARGET_INIT_PROTOTYPE = "prototype"; // ������������

	// ���������� ���������, ��������� ��� ����������� ������� ����
	public static final String TARGET_INIT_INBOUND = "inbound";
	public static final String TARGET_INIT_OUTBOUND = "outbound";

	// ��� ������������ ���������: 1-�� ������, ��� ������������
	public static final String TYPE_RESOLUTION_FIRST = "first"; // ��������� ������� ������
	public static final String TYPE_RESOLUTION_SECOND = "second"; // ������������
	public static final String TYPE_RESOLUTION_GROUP = "group"; // ��������� ���������

	public static final String PARAM_STATE_INIT = "stateInit";
	public static final String PARAM_TARGET_INIT = "targetInit"; 	// ��� ����
																	// �����������
																	// �������,
																	// ���
																	// ������ �
																	// ������ ������������
																	// ��� ����������...
	
	public static final String PARAM_DECISION = "decision";
	public static final String PARAM_DECISION_COMMENT = "comment"; //��������� � ��������������
	public static final String PARAM_DECISION_DECLINE = "decline"; //���������� 
	
	public static final String MATERIAL_UPLOAD_URL =  "/DBMI-UserPortlets/servlet/arm-upload";
	public static final String PARAM_PARENT_CARD_ID = "parentCardId";
	public static final String PARAM_BACK_URL = "backURL";
	public static final String PARAM_EDIT_CARD_ID = "editCardId";
	public static final String PARAM_DONE_URL = "doneURL";
	public static final String PARAM_TYPE_RESOLUTION = "typeResolution"; // ��� ������������ ���������
	public static final String PARAM_DOCS_GROUP = "DOCS_GROUP";
	// ��� ����������
	// c - ������� ���������� � ����������� ������������ ��������, ����� ������������ � �������� �������� - ? extend CardLink
	// b - ���������� ��� ���������� ������������ ��������, ����� ������������ � �������� �������� - ? extend BackLink
	public static final String PARAM_TYPE_LINK = "typeLink";
	public static final String PARAM_WORKFLOW_MOVE = "workflowMove";
	public static final String PARAM_SIGNATURE = "signature";

	public static final String FIELD_ACTION = "formAction";
	public static final String FIELD_RESPONSIBLE_EXECUTOR = "responsibleExecutor";
	public static final String FIELD_ADDITIONAL_EXECUTORS = "additionalExecutors";
	public static final String FIELD_EXTERNAL_EXECUTORS = "externalExecutors";
	public static final String FIELD_FYI = "fyiExecutors";
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
	public static final ObjectId ATTR_COEXECUTORS = ObjectId.predefined(
			PersonAttribute.class, "jbr.CoExecutor");
	public static final ObjectId ATTR_RESOLUT = ObjectId.predefined(
			TextAttribute.class, "jbr.resolutionText");
	public static final ObjectId ATTR_SHORT_DESC = ObjectId.predefined(
			TextAttribute.class, "jbr.document.title");
	public static final ObjectId ATTR_SHORT_CONTEXT = ObjectId.predefined(
			TextAttribute.class, "jbr.shortcontext");
	
	public static final ObjectId ATTR_RIMP_RELASSIG = ObjectId.predefined(
			BackLinkAttribute.class, "jbr.linkedResolutions");
	public static final ObjectId ATTR_TERM = ObjectId.predefined(
			DateAttribute.class, "jbr.resolutionTerm");
	public static final ObjectId ATTR_PRELIMINARY_TERM = ObjectId.predefined(
			DateAttribute.class, "jbr.resolutionTermPreliminary");
	public static final ObjectId ATTR_TCON_INSPECTOR = ObjectId.predefined(
			PersonAttribute.class, "jbr.commission.inspector");
	public static final ObjectId ATTR_TCON_ONCONT = ObjectId.predefined(
			ListAttribute.class, "jbr.oncontrol");

	public static final ObjectId ATTR_FYI = ObjectId.predefined(
	        PersonAttribute.class, "jbr.Fyi");
/*	�������������� ������� �� �������� ���������
 *   7.05.10 �������
 *  public static final ObjectId ATTR_OTHER_EXEC = ObjectId.predefined(
			TextAttribute.class, "jbr.otherExecutors");
	---������ �� ������ ���� "jbr.ExtExecutor"----
*/
	public static final ObjectId ATTR_EXEC_EXT = ObjectId.predefined(
			CardLinkAttribute.class, "jbr.ExtExecutor");
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
	public static final ObjectId ATTR_PARENT_RES = ObjectId.predefined(
			CardLinkAttribute.class, "jbr.rimp.byrimp");
	
	public static final ObjectId ATTR_DOCLINKS = ObjectId.predefined(CardLinkAttribute.class, "jbr.files");
	
	public static final ObjectId ATTR_REPORT_ATTACHMENTS = ObjectId.predefined(CardLinkAttribute.class, "jbr.report.attachments");
	public static final ObjectId ATTR_VISA_ATTACHMENTS = ObjectId.predefined(CardLinkAttribute.class, "jbr.visa.attachments");
	public static final ObjectId ATTR_SIGN_ATTACHMENTS = ObjectId.predefined(CardLinkAttribute.class, "jbr.sign.attachments");
	
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
	public static final ObjectId ATTR_ARM_VISA_LIST = ObjectId.predefined(
			CardLinkAttribute.class, "jbr.arm.visaList");
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
	private final static ObjectId DOC_LIST_ATTR_ID = ObjectId.predefined(
			CardLinkAttribute.class, "jbr.doc.list");
	/**
	 * ����������� �� ���-��������� (��������, ����, ��, �� � ��)
	 * personattribute.jbr.resolutionExecutor=JBR_INFD_EXECUTOR
	 */
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
	// �������� ������� "������������"
	public static final ObjectId ATTR_SEISED = ObjectId.predefined(
			PersonAttribute.class, "jbr.exam.person");
	// �������� ������� "���������"
	public static final ObjectId ATTR_SIGNING = ObjectId.predefined(
			CardLinkAttribute.class, "jbr.sign.set");
	public static final ObjectId ATTR_VISA = ObjectId.predefined(
			CardLinkAttribute.class, "jbr.visa.set");
	public static final ObjectId ATTR_VISA_HIDDEN = ObjectId.predefined(
			CardLinkAttribute.class, "jbr.visa.set.hidden");

	// �������� ������� "�������"
	public static final ObjectId ATTR_SIGNER = ObjectId.predefined(
			PersonAttribute.class, "jbr.sign.person");
	public static final ObjectId ATTR_DECISION = ObjectId.predefined(
			HtmlAttribute.class, "jbr.sign.comment");
	public static final ObjectId ATTR_VISA_DECISION = ObjectId.predefined(
			HtmlAttribute.class, "jbr.visa.decision");
	public static final ObjectId ATTR_VISA_CURRENT_RESOLUTION = ObjectId.predefined(
			TextAttribute.class, "jbr.visa.current.resolution");
	public static final ObjectId ATTR_SIGN_CURRENT_RESOLUTION = ObjectId.predefined(
			TextAttribute.class, "jbr.sign.current.resolution");
	// �������� ������� "����� �� ����������"
	public static final ObjectId ATTR_REPORT_SIGN = ObjectId.predefined(
			PersonAttribute.class, "jbr.report.int.executor");
	// �������� ������� "�����������"
	public static final ObjectId ATTR_RESPONSIBLE = ObjectId.predefined(
			PersonAttribute.class, "jbr.visa.person");

	// ����� ���������
	public static final ObjectId ATTR_NAME = ObjectId.predefined(
			StringAttribute.class, "name");

	public static final ObjectId TEMPLATE_RESOLUTION = ObjectId.predefined(
			Template.class, "jbr.resolution");
	public static final ObjectId TEMPLATE_GROUP_RESOLUTION = ObjectId.predefined(
			Template.class, "jbr.group.resolution");
	public static final ObjectId TEMPLATE_INBOUND = ObjectId.predefined(
			Template.class, "jbr.incoming");
	public static final ObjectId TEMPLATE_PERSONAL_CONTROL = ObjectId
			.predefined(Template.class, "jbr.boss.control");
	public static final ObjectId TEMPLATE_PERSON = ObjectId.predefined(
			Template.class, "jbr.internalPerson");
	public static final ObjectId TEMPLATE_EXT_PERSON = ObjectId.predefined(
			Template.class, "jbr.externalPerson");
	public static final ObjectId TEMPLATE_OUTBOUND = ObjectId.predefined(
			Template.class, "jbr.outcoming");
	public static final ObjectId TEMPLATE_FILE = ObjectId.predefined(
			Template.class, "jbr.file");
	public static final ObjectId TEMPLATE_INSIDE = ObjectId.predefined(
			Template.class, "jbr.interndoc");
	public static final ObjectId TEMPLATE_ORD = ObjectId.predefined(
			Template.class, "jbr.ord");
	public static final ObjectId TEMPLATE_OG = ObjectId.predefined(
			Template.class, "jbr.incomingpeople");

	public static final ObjectId CARDSTATE_ACTIVE_USER = ObjectId.predefined(
			CardState.class, "user.active");
	public static final ObjectId CARDSTATE_CONSIDERATION = ObjectId.predefined(
			CardState.class, "consideration");
	public static final ObjectId CARDSTATE_SIGNING = ObjectId.predefined(
			CardState.class, "sign");
	public static final ObjectId CARDSTATE_MATCHING  = ObjectId.predefined(
			CardState.class, "agreement");
	public static final ObjectId CARDSTATE_DRAFT = ObjectId.predefined(
			CardState.class, "draft");
	public static final ObjectId CARDSTATE_SENT = ObjectId.predefined(
			CardState.class, "sent");
	public static final ObjectId DICTIONARY_NEW = ObjectId.predefined(
			CardState.class, "dictionaryNew");
	public static final ObjectId CARDSTATE_READY_ARCHIVE = ObjectId.predefined(
			CardState.class, "ready-to-write-off");
	public static final ObjectId CARDSTATE_ARCHIVE = ObjectId.predefined(
			CardState.class, "delo");
	public static final ObjectId CARDSTATE_ACTIVE = ObjectId.predefined(
			CardState.class, "user.active");

	public static final ObjectId WFM_ON_EXECUTION = ObjectId.predefined(
			WorkflowMove.class, "jbr.exam.execute");
	public static final ObjectId WFM_ON_SEND_EXECUTION = ObjectId.predefined(
			WorkflowMove.class, "jbr.exam.send_execute");
	public static final ObjectId WFM_SIGN_REJECT_BOSS = ObjectId.predefined(
			WorkflowMove.class, "jbr.sign.return.for.revision");
	public static final ObjectId WFM_RES_EXECUTE = ObjectId.predefined(
			WorkflowMove.class, "jbr.commission.execute");
	public static final ObjectId WFM_REPORT_ACCEPT = ObjectId.predefined(
			WorkflowMove.class, "jbr.report.accept");
	public static final ObjectId WFM_VISA_DISAGREE = ObjectId.predefined(
			WorkflowMove.class, "jbr.document.visa.return.for.revision");
	public static final ObjectId WFM_VISA_COMMENT = ObjectId.predefined(
			WorkflowMove.class, "jbr.visa.cancel");
	public static final ObjectId WFM_VISA_DECLINE = ObjectId.predefined(
			WorkflowMove.class, "jbr.visa.reject");
	public static final ObjectId WFM_SIGN_DECLINE = ObjectId.predefined(
			WorkflowMove.class, "jbr.sign.reject.boss");

	public static final ObjectId WFM_OUTCOMING_FOR_SIGN = ObjectId.predefined(
			WorkflowMove.class, "jbr.outcoming.sign.preparation");
	public static final ObjectId WFM_INSIDE_FOR_SIGN = ObjectId.predefined(
			WorkflowMove.class, "jbr.interndoc.sign.preparation");
	public static final ObjectId WFM_ORD_FOR_SIGN = ObjectId.predefined(
			WorkflowMove.class, "jbr.ord.sign.preparation");

	public static final ObjectId WFM_OUTCOMING_FOR_VISA = ObjectId.predefined(
			WorkflowMove.class, "jbr.outcoming.agreement.preparation");
	public static final ObjectId WFM_INSIDE_FOR_VISA = ObjectId.predefined(
			WorkflowMove.class, "jbr.interndoc.agreement.preparation");
	public static final ObjectId WFM_ORD_FOR_VISA = ObjectId.predefined(
			WorkflowMove.class, "jbr.ord.agreement.preparation");


	// public static final ObjectId ATTR_RES_SIGNIMG = new
	// ObjectId(HtmlAttribute.class, "ADMIN_214329");
	public static final ObjectId ATTR_RES_SIGNIMG = ObjectId.predefined(
			HtmlAttribute.class, "boss.settings.image1");

	// ����
	public static final ObjectId ROLE_MINISTR = ObjectId.predefined(
			SystemRole.class, "jbr.minister");

	public static final String FILETYPE_GRAPHICAL = "graphical";
	public static final String FILETYPE_AUDIO = "audio";

	public static final String SEPARATOR = "#separator#";
	public static final String ID_DELIMITER = "#id_delim#";
	
	public static final String IS_DS_SUPPORT = "isDsSupport";

	private Card resolutionCard;

	public static final String DEFAULT_TIME = "23:59:59";
	public static final String FMT_YYYY_MM_DD = "yyyy-MM-dd";
	public static final String FMT_YYYY_MM_DD_T_HH_MM_SS = "yyyy-MM-dd'T'HH:mm:ss";
	public static final SimpleDateFormat FORMATTER_YYYY_MM_DD_T_HH_MM_SS = new SimpleDateFormat(FMT_YYYY_MM_DD_T_HH_MM_SS);
	public static final SimpleDateFormat FORMATTER_YYYY_MM_DD = new SimpleDateFormat(FMT_YYYY_MM_DD);
    
	private static final String FOLDER_PERSONAL_CONTROL = "8544";
	@SuppressWarnings("unchecked")
	@Override
	protected void doView(RenderRequest request, RenderResponse response)
			throws PortletException, PortletSecurityException, IOException {
		
		final QuickResolutionPortletSessionBean sessionBean = getSessionBean(request);
		
		response.setContentType("text/html");
		
		if (sessionBean.getPortletFormManager().processRender(request, response)) {
			return;
		}
		
		String key = getApplicationSessionBeanKey(response.getNamespace());
		PortletSession session = request.getPortletSession();
		session
				.setAttribute(key, sessionBean,
						PortletSession.APPLICATION_SCOPE);

		final String page;
		final String mode = sessionBean.getMode();
		
		String message = "'";
		if (sessionBean.getMessage() != null) {
			message += sessionBean.getMessage();
			sessionBean.setMessage(null);
		}
		message += "'";
		request.setAttribute("message", message);
		
		
		if(sessionBean.isLockedBaseDoc() || !sessionBean.isValidStateBaseDoc()) {
			page = "messageFormData.jsp";			
			final StringBuffer mes = new StringBuffer();
			if (sessionBean.isLockedBaseDoc()) {
				mes.append(getResourceBundle(request.getLocale()).getString("error.BaseDocIsLocked"));
				sessionBean.setLockedBaseDoc(false);
			} else {
				CardState cs = null;
				try {
					cs = (CardState)PortletUtil.createService(request).getById(sessionBean.getBaseDocState());
				} catch (Exception e) {
					logger.error("Can't get state of base doc", e);
				}
				mes.append(MessageFormat.format(getResourceBundle(request.getLocale()).getString("error.BaseDocWrongState")
						, cs != null ? cs.getName() : sessionBean.getBaseDocState()));
				
				sessionBean.setValidStateBaseDoc(true);
				sessionBean.setBaseDocState(null);
			}
			sessionBean.setMessage(mes.toString());
		} else
		//if (MODE_SELECT_EXECUTORS.equals(mode)) {
		if(MODE_VISA_SIGN_RESOLUTION.equals(mode)) {
			if(sessionBean.getDecision() == null){
				page = "refuseFormData.jsp";
				
				final JSONArray responsibles = new JSONArray();
				try {
					final Map<Long, String> map = sessionBean.getResponsible();
					if (map != null) {
						for( Long cardId : map.keySet() ) {
							final JSONObject j = new JSONObject();
							j.put("cardId", cardId);
							j.put("name", map.get(cardId));
							responsibles.put(j);
						}
					}
				} catch (JSONException e) {
					logger.error("JSON exception caught:", e);
				}
				request.setAttribute("responsible", responsibles);
			}else{
				request.setAttribute("responsible", "null");
				request.setAttribute(PARAM_DECISION, sessionBean.getDecision());
				page = "commentFormData.jsp";
				
				if(PARAM_DECISION_COMMENT.equals(sessionBean.getDecision())){					
					
				}else if(PARAM_DECISION_DECLINE.equals(sessionBean.getDecision())){					
					sessionBean.setResolutionText("��������");
				}
			}
			
			request.setAttribute(FIELD_REFUSE_ATACHMENTS, getAttachmentsJSONData(sessionBean.getAttachedFiles()));
		} else {
			// ���� ��������� - �������� ������ ���������
			if (STATE_INIT_CREATE.equalsIgnoreCase(sessionBean.getStateInit())){
				ObjectId templateId = TEMPLATE_RESOLUTION;
				if(MODE_GROUP_RESOLUTION.equals(mode)){
					templateId =  TEMPLATE_GROUP_RESOLUTION;
				}
				// ������� ����� �������� ���������, ����� ��������� ����������
				try{
					DataServiceBean serviceBean = PortletUtil.createService(request);
					CreateCard createCard = new CreateCard(templateId);
					if(!MODE_GROUP_RESOLUTION.equals(mode)){
					createCard.setLinked(true);
					Long mainDocCardIdLong = sessionBean.getBaseId();
					ObjectId mainDocCardId = ObjectIdUtils.getObjectId(Card.class, mainDocCardIdLong.toString(), true);
					Card mainDocCard = (Card)serviceBean.getById(mainDocCardId);
					createCard.setParent(mainDocCard);
					}
					Card card = (Card) serviceBean.doAction(createCard);				
					resolutionCard = card;
					// �����������������������, ���� � ������� �������� ������������� ��� ��������� ���������, ��� ������� ��������� ���������� �������� �������� (�������� 16738)
				 	sessionBean.setTermAttribute((DateAttribute) card.getAttributeById(ATTR_TERM));
	
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
					for( Long cardId : map.keySet() ) {
						final JSONObject j = new JSONObject();
						j.put("cardId", cardId);
						j.put("name", map.get(cardId));
						additionals.put(j);
					}
				}
			} catch (JSONException e) {
				logger.error("JSON exception caught:", e);
			}

			final JSONArray fyi = new JSONArray();
			try {
				final Map<Long, String> map = sessionBean.getFyi();
				if (map != null) {
					for( Long cardId : map.keySet() ) {
						final JSONObject j = new JSONObject();
						j.put("cardId", cardId);
						j.put("name", map.get(cardId));
						fyi.put(j);
					}
				}
			} catch (JSONException e) {
				logger.error("JSON exception caught:", e);
			}

			final JSONArray externals = new JSONArray();
			try {
				final Map<Long, String> map = sessionBean.getExternals();
				if (map != null) {
					for(final Long cardId : map.keySet() ) {
						final JSONObject j = new JSONObject();
						j.put("cardId", cardId);
						j.put("name", map.get(cardId));
						externals.put(j);
					}
				}
			} catch (JSONException e) {
				logger.error("JSON exception caught:", e);
			}
			
			
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
			
			JSONArray docsGroup = new JSONArray();
			for(Entry<ObjectId,Card> entry: sessionBean.getDocsGroup().entrySet()){
				JSONObject object = new JSONObject();
				try {
					Card card = entry.getValue();
					object.put("id", card.getId().getId());
					object.put("name", card.getAttributeById(ATTR_NAME).getStringValue());
					docsGroup.put(object);
				} catch (JSONException e) {
					logger.error("JSON exception caught:", e);
				}
			}

			request.setAttribute("additionals", additionals);
			request.setAttribute("fyi", fyi);
			request.setAttribute("externals", externals);
			request.setAttribute("controllers", controllers);
			request.setAttribute(FIELD_RESOLUTION_ATTACHMENTS, getAttachmentsJSONData(sessionBean.getAttachedFiles()));
			request.setAttribute("docsGroup", docsGroup);

			if(MODE_GROUP_RESOLUTION.equals(mode)){
				page = "groupResolutionData.jsp";
			} else {
			page = "resolutionData.jsp";
		}
		}

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
	
	
	
	private Search loadActionConfig(String value) {
		Search search = new Search();
		InputStream xml = null;
		try {
			String fullPath = CONFIG_FOLDER + value;
			xml = Portal.getFactory().getConfigService().loadConfigFile(
					fullPath);
			search.initFromXml(xml);
			return search;

		} catch (Exception e) {
			logger.error("Error initializing search ", e);
			return null;
		} finally {
			try {
				xml.close();
			} catch (Exception e2) {
			}
		}
	}	

		@SuppressWarnings("unchecked")
		@Override
		public void processAction(ActionRequest request, ActionResponse response)
				throws PortletException, PortletSecurityException, IOException {
			
			String action = request.getParameter(FIELD_ACTION);
			if (ACTION_INIT.equals(action)) {
				PortletSession session = request.getPortletSession();
				// ������� sessionBean, ���� �� �����������, ����� ������� �����
				session.removeAttribute(SESSION_BEAN); 
			}
			QuickResolutionPortletSessionBean sessionBean = getSessionBean(request);
			DataServiceBean serviceBean = PortletUtil.createService(request);
			
			if (sessionBean.getPortletFormManager().processAction(request, response)) {
				return;
			}
			
			parseFormValuesIntoSessionBeanFromRequest(sessionBean, request);
			
	
				// TODO: ������������ �� ����� ���� ������� ��������� �����
				if (PortletFileUpload.isMultipartContent(request)) {
					final FileItemFactory factory = new DiskFileItemFactory();
					final PortletFileUpload upload = new PortletFileUpload(factory);
					try {
						final List<FileItem> items = upload.parseRequest(request);
	
						// ���� ���� ����������� ���������
						for (Iterator<FileItem> it = items.iterator(); it.hasNext();) {
							final FileItem item = it.next();
							String name = item.getFieldName();
							if (FILETYPE_GRAPHICAL.equals(name)) {
								sessionBean.setGrapFile(item);
								sessionBean.setChangeGrap(true);
								break;
							}
						}
					} catch (Exception e) {
						logger.error("Error in submit graphical resolution", e);
					}
				}
			
			
			if (ACTION_INIT.equals(action)) {
				if (sessionBean.getCurrentPerson() == null) {
						final StringBuffer message = new StringBuffer();
						message.append(getResourceBundle(request.getLocale())
								.getString("error.ArmSettingEmpty"));
						
						sessionBean.setMessage(message.toString());
						return; 
				}
				
				final ObjectId workflowMove = ObjectId.predefined(
						WorkflowMove.class, request.getParameter(PARAM_WORKFLOW_MOVE));
				sessionBean.setWorkflowMoveId(workflowMove);

				final String backLink = request.getParameter(PARAM_BACK_URL);
				sessionBean.setBackLink(backLink);
				String doneLink = request.getParameter(PARAM_DONE_URL);
				if (doneLink == null) {
					doneLink = backLink;
				}
				sessionBean.setDoneLink(doneLink);
	
				String target = request.getParameter(PARAM_TARGET_INIT);
				// ��� �������������� ������� ������ ��� ���������� � TARGET_INIT_INBOUND � TARGET_INIT_OUTBOUND
				if (target == null || TARGET_INIT_INBOUND.equals(target)) {
					target = TARGET_INIT_CONSIDERATION;
				} else if (TARGET_INIT_OUTBOUND.equals(target)) {
					target = TARGET_INIT_SIGNING;
				}
	
				sessionBean.setTargetInit(target);
	
				String typeRes = request.getParameter(PARAM_TYPE_RESOLUTION);
				// ��� �������������� ������ ������� ���
				if (typeRes == null) {
					typeRes = TYPE_RESOLUTION_FIRST;
				} else {
					typeRes = typeRes.trim();
				}
				sessionBean.setTypeRes(typeRes);
	
				String typeLink = request.getParameter(PARAM_TYPE_LINK);
				// ��� �������������� ������ ������� ���
				if (typeRes != null) {
					for (TypeLink ts : TypeLink.values()) {
						if(ts.type.equalsIgnoreCase(typeLink)) {
							sessionBean.setTypeLink(ts);
							break;
						}
					}
				}
	
				String stateInit = request.getParameter(PARAM_STATE_INIT);
				// ��������� ��������� (������� ����)
				if (stateInit == null) {
					stateInit = STATE_INIT_CREATE;
				}
				
				if (STATE_INIT_CREATE.equals(stateInit)) {
					sessionBean.setStateInit(STATE_INIT_CREATE);
					final String parentCardId = request.getParameter(PARAM_PARENT_CARD_ID);
					if (parentCardId != null && !parentCardId.trim().equals("")) {
						final Long parentId = Long.valueOf(parentCardId);
						sessionBean.setParentId(parentId);
						
						if(TARGET_INIT_SIGNING.equals(sessionBean.getTargetInit())) {
							try {
								sessionBean.setBaseId(parentId);
								loadAttachments(sessionBean, serviceBean);
							} catch (Exception e) {
								logger.error("Error in updating attachments", e);
							}
						}
	
						if (TYPE_RESOLUTION_SECOND.equals(sessionBean.getTypeRes())) {
							final Card parentCard = getCardById(new ObjectId(Card.class, parentId), serviceBean);
							if(parentCard != null) {
								CardLinkAttribute cla = parentCard.getCardLinkAttributeById(ATTR_MAINDOC);
								if(cla != null && !cla.isEmpty()) {
									final ObjectId baseObjectId = cla.getIdsLinked().get(0);
									final Long baseId = (Long) baseObjectId.getId();
							sessionBean.setBaseId(baseId);
								}
							}
						} else {
							sessionBean.setBaseId(sessionBean.getParentId());
						}
						
						if (TYPE_RESOLUTION_SECOND.equals(sessionBean.getTypeRes()) ||
						    TYPE_RESOLUTION_FIRST.equals(sessionBean.getTypeRes())) {
							try {
								if(!checkStateBaseDoc(sessionBean, serviceBean)) {
									sessionBean.setValidStateBaseDoc(false);
									return;
								}
							} catch (Exception e) {
								logger.error("Error checking maindoc status " + sessionBean.getBaseId() + " : \n", e);
							}
						}
						
						
						try {
							if(TypeLink.CARDLINK.equals(sessionBean.getTypeLink()) && !checkLockBaseDoc(request, sessionBean, serviceBean)) {
								sessionBean.setLockedBaseDoc(true);
								return;
							}
						} catch (Exception e) {
							logger.error("Error checking maindoc lock " + sessionBean.getBaseId() + " : \n", e);
						}
						
						initFromParentCard(request, sessionBean);
						searchPersonalControlCard(sessionBean, serviceBean);
						
						// ��������� ������������ �� ���-��� ��� ���, ��������� � ���������� � �������� "������������" � "����������" 
						final Long mainDocId = sessionBean.getBaseId();
						try {
							final Card mainCard = (Card) serviceBean.getById(new ObjectId(Card.class, mainDocId) );
							if (CheckMainCard(mainCard))
								searchMainDocExecutors(sessionBean, serviceBean);
						} catch (Exception e) {
							logger.error("Error search of maindoc card " + mainDocId + " attribute "+ ATTR_MAINDOC_EXECUTOR, e);
						}
					} else if (TYPE_RESOLUTION_GROUP.equals(sessionBean.getTypeRes())){
						String docsGroup = request.getParameter(PARAM_DOCS_GROUP);
						if(docsGroup == null || docsGroup.trim().isEmpty()){
							doExit(request, response, sessionBean, false);
					} else {
							for(String stringId : docsGroup.split("_")){
								ObjectId docId = new ObjectId(Card.class, Long.parseLong(stringId));
								try {
									sessionBean.getDocsGroup().put(docId, (Card)serviceBean.getById(docId));
								}
								catch (Exception e) {
									logger.error("Cant getById card "+ docId, e);
								}
							}
						}
					} else {
						doExit(request, response, sessionBean, false);
					}
				} else {
					sessionBean.setStateInit(STATE_INIT_EDIT);
	
					String editCardId = request.getParameter(PARAM_EDIT_CARD_ID);
					if (editCardId != null && !editCardId.trim().equals("")) {
						Long cardId = Long.valueOf(editCardId);
						sessionBean
								.setIdResolution(new ObjectId(Card.class, cardId));
						//initialize parent id
						final String parentCardId = request.getParameter(PARAM_PARENT_CARD_ID);
						if (parentCardId != null && !parentCardId.trim().equals("")) {
							final Long parentId = Long.valueOf(parentCardId);
							sessionBean.setParentId(parentId);
						}	
						initFromResolutionCard(request, sessionBean, cardId.longValue());
						if (sessionBean.getBaseId()!=null)
							initFromParentCard(request, sessionBean);
						
						searchPersonalControlCard(sessionBean, serviceBean);
					} else {
						doExit(request, response, sessionBean, false);
					}
				}
	
				if (TYPE_RESOLUTION_SECOND.equals(sessionBean.getTypeRes())) {
					// ��������� ����� � ������ ������
					changeStatusReportForSecondRes(sessionBean, serviceBean);
				}
				
			/*
			 * ���� �������������� ���� ��� �������,  ���������� ������������
			 * ��������� ����� �������, � �� �������� ���������. (�.�., 19.05.2011)
			 */
				if (sessionBean.isVisaOrSign()) {
					sessionBean.setMode(MODE_VISA_SIGN_RESOLUTION);	
					//��� ������ ������� �� ������������ � ����������
					sessionBean.setDecision(request.getParameter(PARAM_DECISION));
					sessionBean.setWorkflowMoveId(getVisaOrSignWfm(target, sessionBean.getDecision()));
				} else if (!sessionBean.getDocsGroup().isEmpty()){
					sessionBean.setMode(MODE_GROUP_RESOLUTION);
				}
				initBaseCardProperties(request, sessionBean, serviceBean);
			} else if (ACTION_ADD_LINKED_FILES.equals(action)) {
				Search search = loadActionConfig("attrActions/addVisaLinkedConfig.xml");
				Card parentCard = getCardById(sessionBean.getBaseId(), serviceBean);
				try {
					ListDataProvider adapter = new AddRefuseVisaLinkedCardAttachmentData(search,
							parentCard, serviceBean, sessionBean.getAttachedFiles());
					ListEditor editor = new RefuseVisaLinkedCardEditor();
					editor.setDataProvider(adapter);

					//display linked columns as "Link:
					editor.setDisplayLinkedColumns(true);
					
					
					//do not display Search Parameters : "By Number", "By Attributes", "By Material"
					editor.setDisplaySearchByNumber(false);
					editor.setDisplaySearchByAttributes(false);
					editor.setDisplaySearchByMaterial(false);
					
					sessionBean.getPortletFormManager().openForm(new QuickResolutionPortletListEditForm(editor));
					return;
					
				} catch (Exception e) {
					logger.error(e);
				}
			} else if (ACTION_NEXT.equals(action)) {
				// ��������� ��� �� ��� �������� ������������ ������������ Person
				final List<Long> cardIds = new ArrayList<Long>();
				cardIds.addAll(sessionBean.getResponsible().keySet());
				cardIds.addAll(sessionBean.getAdditionals().keySet());
				final List<Long> nonexistentPersons = getNonexistentPersons(serviceBean, cardIds);
				if (nonexistentPersons.size() == 0) {
					//sessionBean.setMode(MODE_EDIT_RESOLUTION);
				} else {
				final Map<Long, String> map = ARMUtils.getNameByCardIds(serviceBean, nonexistentPersons);
					final StringBuffer message = new StringBuffer();
					message.append(getResourceBundle(request.getLocale())
							.getString("label.nonexistent"));
	
					final Iterator<Long> i = map.keySet().iterator();
					while (i.hasNext()) {
						message.append( map.get( i.next()));
						if (i.hasNext())
							message.append(", ");
					}
					sessionBean.setMessage(message.toString());
				}
			} else if (ACTION_CANCEL.equals(action)) {
				doExit(request, response, sessionBean, false);
			} else if (ACTION_BACK.equals(action)) {
				doExit(request, response, sessionBean, false);
			} else if (ACTION_DONE.equals(action)) {
				// �������� ���������� ���-���������
				try {
					if(TypeLink.CARDLINK.equals(sessionBean.getTypeLink())
							&& !checkLockBaseDoc(request, sessionBean, serviceBean)) {
						
						final StringBuffer message = new StringBuffer();
						message.append(getResourceBundle(request.getLocale())
								.getString("error.BaseDocIsLocked"));
						sessionBean.setMessage(message.toString());
						logger.error(message.toString());
						return;
					}
				} catch (Exception e) {
					logger.error("Error checking maindoc lock " + sessionBean.getBaseId() + " : \n", e);
				}
				// ������ ����������, ������ ��� � ���������+ ������ ��� �������� ����� ��������
				String target = sessionBean.getTargetInit();
				try{
					WorkflowMove wfm = null;
					if (TARGET_INIT_SIGNING.equals(target)) {
												
						setDecisionForSign(sessionBean, serviceBean);
						
						if(sessionBean.getAttachedFiles().isEmpty() == false){
							updateAttachedFiles(sessionBean, serviceBean, ATTR_SIGN_ATTACHMENTS);
						}
						Card sign = (Card) serviceBean.getById(getSign(sessionBean, serviceBean));
						wfm = changeStatusSign(sessionBean, serviceBean);
						if(wfm != null && prepareForDS(request, response, wfm, sign)) return;
						
					} else if (TARGET_INIT_VISA.equals(target)) {
						
						setDecisionForVisa(sessionBean, serviceBean);
						if(sessionBean.getAttachedFiles().isEmpty() == false){
							updateAttachedFiles(sessionBean, serviceBean, ATTR_DOCLINKS);//ATTR_VISA_ATTACHMENTS
						}
						Card visa = (Card) serviceBean.getById(getVisa(sessionBean, serviceBean));
						wfm = changeStatusVisa(sessionBean, serviceBean);
						if(wfm != null && prepareForDS(request, response, wfm, visa)) return;
					} else if (TARGET_INIT_PROTOTYPE.equals(target)){
						processGroupResolution(sessionBean, (AsyncDataServiceBean) serviceBean);
					} else {	// ���������
						//��������� ������ �� (�� ����� ������ ������� ������������ � ���� 
						//�� ��� ���������� ��� ������ ������, ��������, �� ������ ����������)
						try {
							if(!checkStateBaseDoc(sessionBean, serviceBean)) {
								final StringBuffer message = new StringBuffer();
								message.append(getResourceBundle(request.getLocale())
										.getString("error.BaseDocWrongState"));
								sessionBean.setMessage(message.toString());
								logger.error(message.toString());
								return;
							}
						} catch (Exception e) {
							logger.error("Error checking maindoc state " + sessionBean.getBaseId() + " : \n", e);
						}
						update(sessionBean, request);
						updateControl(sessionBean, request);
					}
					doExit(request, response, sessionBean, true);
				} catch(Exception e){
					logger.error(e);
					// ������ ��������� ������ ��� ���������
					if ((TARGET_INIT_VISA.equals(target))||(TARGET_INIT_SIGNING.equals(target)))
						doExit(request, response, sessionBean, true);
					else{
						// ����� ��������� ��������� ������� � ���������� ���
						final StringBuffer message = new StringBuffer();
						String msg = ( e.getMessage() != null) ? e.getMessage() : "";
						message.append(
								MessageFormat.format(
										getResourceBundle(request.getLocale()).getString("message.resolutionNotSave"),
										new Object[]{
											msg.replaceAll(e.getClass().getName(), "").replaceAll("'", "\""),
											ConfigHolder.getPageLabel("hotline.phone")
										}
								)
						);
						sessionBean.setMessage(message.toString());
					}
				}
				
			} else if (ACTION_DONE_PLUS.equals(action)) {
				// ��������� �� �������������� ��������
				String target = sessionBean.getTargetInit();
				try{
					if (TARGET_INIT_SIGNING.equals(target)) {
						updateControl(sessionBean, request);
						setDecisionForSign(sessionBean, serviceBean);
						changeStatusSign(sessionBean, serviceBean);
					} else if (TARGET_INIT_VISA.equals(target)) {
						updateControl(sessionBean, request);
						setDecisionForVisa(sessionBean, serviceBean);
						changeStatusVisa(sessionBean, serviceBean);
					} else {
						update(sessionBean, request);
						updateControl(sessionBean, request);
					}
					// ������� ����� sessionBean
					Long parentId = sessionBean.getParentId();
					if (parentId != null) {
						QuickResolutionPortletSessionBean newSessionBean = createSessionBean(request);
						PortletSession session = request.getPortletSession();
						session.setAttribute(SESSION_BEAN, newSessionBean);
		
						newSessionBean.setDoneLink(sessionBean.getDoneLink());
						newSessionBean.setTargetInit(sessionBean.getTargetInit());
						newSessionBean.setTypeRes(sessionBean.getTypeRes());
						newSessionBean.setBackLink(sessionBean.getBackLink());
						newSessionBean.setStateInit(STATE_INIT_CREATE);
						newSessionBean.setParentId(parentId);
						newSessionBean.setBaseId(sessionBean.getBaseId());
						newSessionBean.setIdControlCard(sessionBean.getIdControlCard());
						newSessionBean.setControlTerm(sessionBean.getControlTerm());
						newSessionBean.setMinister(sessionBean.isMinister());
					} else {
						doExit(request, response, sessionBean, false);
					} 
					
				} catch(Exception e){
					logger.error(e);
					// ������ ��������� ������ ��� ���������
					if ((TARGET_INIT_VISA.equals(target))||(TARGET_INIT_SIGNING.equals(target)))
						doExit(request, response, sessionBean, true);
					else{
						// ����� ��������� ��������� ������� � ���������� ���
						final StringBuffer message = new StringBuffer();
						String msg = ( e.getMessage() != null) ? e.getMessage() : "";
						message.append(
								MessageFormat.format(
										getResourceBundle(request.getLocale()).getString("message.resolutionNotSave"),
										new Object[]{
											msg.replaceAll(e.getClass().getName(), "").replaceAll("'", "\""),
											ConfigHolder.getPageLabel("hotline.phone")
										}
								)
						);
						sessionBean.setMessage(message.toString());
					}
				}
			} else if (ACTION_SAVE_DS_AND_EXIT.equals(action)){
				signCardHandler(request, serviceBean);
				doExit(request, response, sessionBean, true);
			}
		}

	private void processGroupResolution(QuickResolutionPortletSessionBean sessionBean,
				AsyncDataServiceBean serviceBean) throws DataException, ServiceException {
		
		setResolutionAttributesFromSessionBean(resolutionCard,sessionBean,serviceBean);

		for(ObjectId userDoc: sessionBean.getDocsGroup().keySet()){
			ProcessGroupResolution groupResolution = new ProcessGroupResolution();
			groupResolution.setCurrentResolution(resolutionCard);
			groupResolution.setPersonalControlDate(sessionBean.getControlTerm());
			groupResolution.setDocs(Collections.singletonList(userDoc));
			serviceBean.doAction(groupResolution);
		}

		if(sessionBean.getControlTerm()!=null){
			clearFolderQtyCache(FOLDER_PERSONAL_CONTROL, serviceBean);
		}
	}


	private void loadAttachments(QuickResolutionPortletSessionBean sessionBean, DataServiceBean serviceBean) throws DataException, ServiceException {
		
		Map<ObjectId, String> attachedFiles = new HashMap<ObjectId, String>();
		
		ObjectId cardId = getSign(sessionBean, serviceBean);
		if(cardId != null) {
			try {
				Card card = (Card)serviceBean.getById(cardId);
				List<ObjectId> fileIds = ((CardLinkAttribute) card.getAttributeById(ATTR_SIGN_ATTACHMENTS)).getIdsLinked();
				Search search = new Search();
				search.setByCode(true);
				search.setWords(ObjectIdUtils.numericIdsToCommaDelimitedString(fileIds));
				List<SearchResult.Column> columns = new ArrayList<SearchResult.Column>(1);
				SearchResult.Column column = new SearchResult.Column();
				
				column.setAttributeId(Attribute.ID_NAME);
				columns.add(column);
				search.setColumns(columns);
				
				SearchResult result = (SearchResult) serviceBean.doAction(search);
				List<Card> resultCards = result.getCards();
				for(Card c : resultCards) {
					attachedFiles.put(c.getId(), ((StringAttribute)c.getAttributeById(Attribute.ID_NAME)).getValue());
				}
				
			} catch (DataException e) {
				logger.error("An error has occured while trying to retreive card:"+cardId.getId(), e);
			} catch (ServiceException e) {
				logger.error("An error has occured while trying to retreive card:"+cardId.getId(), e);
			}
			sessionBean.setAttachedFiles(attachedFiles);
		}
	}
		
	private void parseFormValuesIntoSessionBeanFromRequest(QuickResolutionPortletSessionBean sessionBean,
			ActionRequest request) {

		// ������������� �����������
		String p = request.getParameter(FIELD_RESPONSIBLE_EXECUTOR);
		if (p != null) {
			if (!"".equals(p.trim())) {
				final String[] value = p.split(":");
				sessionBean.setResponsible(Long.valueOf(value[0]), value[1]);
			}
		}
		
		// �������������
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
		
		// � ��������
		p = request.getParameter(FIELD_FYI);
		if (p != null) {
			final Map<Long, String> fyi = new LinkedHashMap<Long, String>();
			if (!"".equals(p.trim())) {
				final String[] peoples = p.split(SEPARATOR);
				for (int i = 0; i < peoples.length; i++) {
					final String[] value = peoples[i].split(":");
					fyi.put(Long.valueOf(value[0]), value[1]);
				}
			}
			sessionBean.setFyi(fyi);
		}
		
		// ������� �����������
		p = request.getParameter(FIELD_EXTERNAL_EXECUTORS);
		if (p != null) {
			final Map<Long, String> externals = new LinkedHashMap<Long, String>();
			if (!"".equals(p.trim())) {
				final String[] peoples = p.split(SEPARATOR);
				for (int i = 0; i < peoples.length; i++) {
					final String[] value = peoples[i].split(":");
					if(value.length > 0) {
						externals.put(Long.valueOf(value[0]), value.length > 1 ? value[1] : "");
					}
				}
			}
			sessionBean.setExternals(externals);
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
			} catch (Exception e) {
				logger.error("Bad format date of term:", e);
			}
			}
		} else {
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
		
		
	private void initBaseCardProperties(ActionRequest request, QuickResolutionPortletSessionBean sessionBean, DataServiceBean serviceBean) {

		if (sessionBean.isVisaOrSign()) {
			String headerResource = "header.refuse";

			if (PARAM_DECISION_COMMENT.equals(sessionBean.getDecision())) {
				headerResource = "header.visa.comment";
			} else if (PARAM_DECISION_DECLINE.equals(sessionBean.getDecision())) {
				headerResource = "header.visa.decline";
			}
			sessionBean.setHeader(getResourceBundle(request.getLocale()).getString(headerResource));
			sessionBean.setWorkflowRequired(false);
		} else if(MODE_GROUP_RESOLUTION.equals(sessionBean.getTypeRes())) {
			sessionBean.setHeader(getResourceBundle(request.getLocale()).getString("header.groupResolution"));
			sessionBean.setWorkflowRequired(false);
		} else {
			// creation/changing of resolution
			formResolutionHeader(request, sessionBean);
			sessionBean.setWorkflowRequired(true);
		}
		sessionBean.setSwitchNavigatorLink(ARMUtils.retrieveSwitchNavigatorButton(request, serviceBean));
	}

	/**
	 * Forms the header of create/edit resolution page.
	 * 
	 * @param request
	 * @param sessionBean
	 */
	private void formResolutionHeader(ActionRequest request, QuickResolutionPortletSessionBean sessionBean) {
		StringBuffer theHeaderBuf = new StringBuffer(100);

		String stateInit = sessionBean.getStateInit();
		if (STATE_INIT_EDIT.equals(stateInit)) {
			theHeaderBuf.append(getResourceBundle(request.getLocale()).getString("header.editResolution"));
		} else {
			theHeaderBuf.append(getResourceBundle(request.getLocale()).getString("header.createResolution"));
		}

		if (sessionBean.getRegNumber() != null) {
			theHeaderBuf.append(" ").append(getResourceBundle(request.getLocale()).getString("header.onDocument")).append(": ");
			theHeaderBuf.append(sessionBean.getTypeDoc()).append(" �");
			theHeaderBuf.append(sessionBean.getRegNumber());
		}

		sessionBean.setHeader(theHeaderBuf.toString());
	}

	// ������� �� ���������� � ����������, �.�. �� 108 � 106 - 355543
//	private void changeStatusBaseForSign(
//			QuickResolutionPortletSessionBean sessionBean,
//			DataServiceBean serviceBean) {
//		boolean locked = false;
//		ObjectId baseId = new ObjectId(Card.class, sessionBean.getBaseId());
//		try {
//
//			// �������� ������ �������� �������
//			serviceBean.doAction(new LockObject(baseId));
//			locked = true;
//
//			Card base = (Card) serviceBean.getById(baseId);
//
//			ObjectId templateId = base.getTemplate();
//			ObjectId wfmId = null;
//			// ���������� ������ ������� �������� � � ����������� �� ������
//			if (TEMPLATE_OUTBOUND.equals(templateId)) {
//				wfmId = WFM_OUTCOMING_FOR_SIGN;
//			} else if (TEMPLATE_INSIDE.equals(templateId)) {
//				wfmId = WFM_INSIDE_FOR_SIGN;
//			} else if (TEMPLATE_ORD.equals(templateId)) {
//				wfmId = WFM_ORD_FOR_SIGN;
//			}
//			if (wfmId != null) {
//
//				WorkflowMove wfm = (WorkflowMove) serviceBean.getById(wfmId);
//				ChangeState actionChange = new ChangeState();
//				actionChange.setCard(base);
//				actionChange.setWorkflowMove(wfm);
//
//				serviceBean.doAction(actionChange);
//			}
//		} catch (Exception e) {
//			logger.error("Error change status base card", e);
//		} finally {
//			if (locked) {
//				unlockQuickly(baseId, serviceBean);
//			}
//		}
//	}

	// ������� �� ������������ � ����������, �.�. �� 107 � 106 - 355545
//	private void changeStatusBaseForVisa(
//			QuickResolutionPortletSessionBean sessionBean,
//			DataServiceBean serviceBean) {
//		boolean locked = false;
//		ObjectId baseId = new ObjectId(Card.class, sessionBean.getBaseId());
//
//		try {
//
//			// �������� ������ �������� �������
//			serviceBean.doAction(new LockObject(baseId));
//			locked = true;
//
//			Card base = (Card) serviceBean.getById(baseId);
//			ObjectId templateId = base.getTemplate();
//			ObjectId wfmId = null;
//			// ���������� ������ ������� �������� � � ����������� �� ������
//			if (TEMPLATE_OUTBOUND.equals(templateId)) {
//				wfmId = WFM_OUTCOMING_FOR_VISA;
//			} else if (TEMPLATE_INSIDE.equals(templateId)) {
//				wfmId = WFM_INSIDE_FOR_VISA;
//			} else if (TEMPLATE_ORD.equals(templateId)) {
//				wfmId = WFM_ORD_FOR_VISA;
//			}
//			if (wfmId != null) {
//
//				WorkflowMove wfm = (WorkflowMove) serviceBean.getById(wfmId);
//				ChangeState actionChange = new ChangeState();
//				actionChange.setCard(base);
//				actionChange.setWorkflowMove(wfm);
//
//				serviceBean.doAction(actionChange);
//			}
//		} catch (Exception e) {
//			logger.error("Error change status base card", e);
//		} finally {
//			if (locked) {
//				unlockQuickly(baseId, serviceBean);
//			}
//		}
//	}

	// ��������� ���� ������� �������� ����
	private void setDecisionForVisa(
			QuickResolutionPortletSessionBean sessionBean,
			DataServiceBean serviceBean) {
		
		ObjectId visaId = null;
		try {
			visaId = getVisa(sessionBean, serviceBean);
		} catch (Exception e) {
			logger.error("Error getting card 'Visa' ", e);
		}
		setDecision(visaId, ATTR_VISA_CURRENT_RESOLUTION, sessionBean, serviceBean);//ATTR_VISA_DECISION
		
/*		
		ObjectId visaId = null;
		try {
			visaId = getVisa(sessionBean, serviceBean);
			TextAttribute resolution = new TextAttribute();
			resolution.setId((String)ATTR_VISA_CURRENT_RESOLUTION.getId());
			resolution.setValue(sessionBean.getResolutionText());
			LockObject lock = new LockObject();
			lock.setId(visaId);
			serviceBean.doAction(lock);
			OverwriteCardAttributes action = new OverwriteCardAttributes();
			action.setCardId(visaId);
			action.setAttributes(Collections.singletonList(resolution));
			action.setInsertOnly(false);
			serviceBean.doAction(action);
			UnlockObject unlock = new UnlockObject();
			unlock.setId(visaId);
			serviceBean.doAction(unlock);
		} catch (Exception e) {
			logger.error("Error during resolution update.", e);
		}
	*/
	}

	private ObjectId getVisa(QuickResolutionPortletSessionBean sessionBean,
			DataServiceBean serviceBean
		) throws DataException, ServiceException 
	{
		ObjectId visaId = null;

		try {
			visaId = getVisa(sessionBean, serviceBean, ATTR_VISA_HIDDEN);
		} catch (DataException ex) {
			logger.warn( ex.getMessage());
		}
		// ����� ���������� �� ����� ���, �.�. ���� ��� ������� �� �����, �� ��� 
		// ��� "�������" ������ ...
		if (visaId == null)
			visaId = getVisa(sessionBean, serviceBean, ATTR_VISA);
		
		return visaId;
	}

	private ObjectId getVisa(QuickResolutionPortletSessionBean sessionBean,
			DataServiceBean serviceBean, ObjectId listVisaAttrId 
		) throws DataException, ServiceException 
	{
		// �������� id ��������� ����
		final ObjectId baseId = new ObjectId(Card.class, sessionBean.getBaseId());
		final Card outbound = (Card) serviceBean.getById(baseId);
		final CardLinkAttribute attr = outbound	.getCardLinkAttributeById(listVisaAttrId);

		// ������� id �������� ���� � ������� 107 � ������� �������������
		// � �������� ������������
		final Search search = new Search();
		search.setByCode(true);
		search.setByAttributes(true);
		search.addPersonAttribute(ATTR_RESPONSIBLE, sessionBean.getCurrentPerson().getId());
		final List<String> states = new ArrayList<String>(1);
		states.add(CARDSTATE_MATCHING.getId().toString());
		search.setStates(states);

		// (2010/02, RuSA) CardLink::getValues()
		if (attr.getIdsLinked() != null)
			search.setWords(ObjectIdUtils.numericIdsToCommaDelimitedString(ObjectIdUtils.collectionToSetOfIds(attr.getIdsLinked())));

		final Collection<Card> cards = SearchUtils.execSearchCards(search, serviceBean);
		if (cards == null || cards.size() == 0)
			throw new DataException(
					"No suitable Visa '"+ listVisaAttrId +"' cards at status " + CARDSTATE_MATCHING.getId());
		if (cards.size() > 1)
			logger.warn("Several cards Visa '"+ listVisaAttrId +"' are at status " + CARDSTATE_MATCHING.getId());
		return cards.iterator().next().getId();
	}

	/*��������� ������� - ���� ������� ������������ ��� ������������� ��������� �������� ������������ ��� ��������������,
	* ����� � ������������ ��������� ���� ����� ������������� (� ����� ������?) �������� ������������,
    * � ���� �� � ������� ���������, ��������� ��� � ������ �������.
	*/
	private void changeStatusReportForSecondRes(QuickResolutionPortletSessionBean sessionBean,
			DataServiceBean serviceBean) {
		boolean locked = false;
		ObjectId reportId = null;
		try {
			ObjectId parentId = new ObjectId(Card.class, sessionBean.getParentId());
			Card parent = (Card)  serviceBean.getById(parentId);

			// ���������, ��� ������� ������������ �������� ������������ ��� ��������������
			boolean isExecutor = false;
			// ������� ����������� � ��������������
			{
				final Collection<Person> executorsPerson = SearchUtils.getAttrPersons(parent, ATTR_EXECUTOR, false ); 
				final Collection<Person> additionalsPerson = SearchUtils.getAttrPersons(parent, ATTR_COEXECUTORS, false );
				if (additionalsPerson != null)
					executorsPerson.addAll(additionalsPerson);

				final ObjectId curId = sessionBean.getCurrentPerson().getId();
				if (executorsPerson != null && curId != null) {
					for(Person p : executorsPerson) {
						if (curId.equals(p.getId())) {
							isExecutor = true;
							break;
						}
					}
				}
				if (!isExecutor)
					return;
			}

			// ���� ����� ������������� �������� ������������
			final List<ObjectId> reportsIds = com.aplana.dbmi.model.util.SearchUtils.getBackLinkedCardsObjectIds(parent, ATTR_REPORTS, serviceBean);

			final Search search = new Search();
			search.setByCode(true);
			search.setByAttributes(true);
			search.addPersonAttribute(ATTR_REPORT_SIGN,
					sessionBean.getCurrentPerson().getId());
			final List<String> states = new ArrayList<String>(1);
			states.add(CARDSTATE_SENT.getId().toString());
			search.setStates(states);
			search.setWords(ObjectIdUtils.numericIdsToCommaDelimitedString(reportsIds));

			final Collection<Card> reports = SearchUtils.execSearchCards(search, serviceBean); 

			// ��������� �������� �� ������� ��������� (556656) � ������ ������ (702239)
			if (reports != null) {
				for( Card report: reports ) {
					reportId = report.getId();

					serviceBean.doAction(new LockObject(reportId));
					locked = true;

					final WorkflowMove wfm = (WorkflowMove) serviceBean.getById(WFM_REPORT_ACCEPT);
					final ChangeState actionChange = new ChangeState();
					actionChange.setCard(report);
					actionChange.setWorkflowMove(wfm);

					serviceBean.doAction(actionChange);
					unlockQuickly(reportId, serviceBean);
					locked = false;
				}
			}
		} catch (Exception e) {
			logger.error("Error change status of card 'Report':", e);
		} finally {
			if (locked) {
				unlockQuickly(reportId, serviceBean);
			}
		}
	}

//	private void changeStatusResOnExecution(
//			QuickResolutionPortletSessionBean sessionBean,
//			DataServiceBean serviceBean) {
//		boolean locked = false;
//		ObjectId curResId = null;
//		try {
//			// �������� id ��������� ������� ��������� ���������,
//			// � ������� ��������,
//			// ��� ��� ������������ ��������� - ������� ������������
//
//			// ������� ������ �������� ���������
//			ObjectId parentId = new ObjectId(Card.class, sessionBean
//					.getParentId());
//			Card parent = (Card) serviceBean.getById(parentId);
//			CardLinkAttribute attrRes = null;
//			if (TYPE_RESOLUTION_FIST.equals(sessionBean.getTypeRes())) {
//				attrRes = parent.getCardLinkAttributeById(ATTR_IMPL_RESOLUT);
//			} else {
//				attrRes = parent.getCardLinkAttributeById(ATTR_RIMP_RELASSIG);
//			}
//			Collection resolutionIds = attrRes.getIdsLinked();
//
//			final Search search = new Search();
//			search.setByCode(true);
//			search.setByAttributes(true);
//			search.addCardLinkAttribute(ATTR_SIGNATORY, sessionBean.getCurrentPerson().getCardId());
//
//			final List states = new ArrayList(1);
//			states.add(CARDSTATE_DRAFT.getId().toString());
//			search.setStates(states);
//
//			search.setWords(ObjectIdUtils.numericIdsToCommaDelimitedString(resolutionIds));
//
//			Collection resolutions = ((SearchResult)serviceBean.doAction(search)).getCards();
//
//			// ��������� �������� ��������� � ������ "�� ����������"(103)
//			Iterator iter = resolutions.iterator();
//			while (iter.hasNext()) {
//				Card res = (Card) iter.next();
//				curResId = res.getId();
//
//				serviceBean.doAction(new LockObject(curResId));
//				locked = true;
//				WorkflowMove wfm = (WorkflowMove) serviceBean
//					.getById(WFM_RES_EXECUTE);
//				ChangeState actionChange = new ChangeState();
//				actionChange.setCard(res);
//				actionChange.setWorkflowMove(wfm);
//
//				serviceBean.doAction(actionChange);
//
//				unlockQuickly(curResId, serviceBean);
//				locked = false;
//			}
//		} catch (Exception e) {
//			logger.error("Error change status of card 'On execution':", e);
//		} finally {
//			if (locked) {
//				unlockQuickly(curResId, serviceBean);
//			}
//		}
//	}
	
	
	// ��������� ���� ������� �������� �������
	private void setDecisionForSign(QuickResolutionPortletSessionBean sessionBean, DataServiceBean serviceBean) {
		ObjectId signingId = null;
		try {
			signingId = getSign(sessionBean, serviceBean);
		} catch (Exception e) {
			logger.error("Error get card 'Signing'", e);
		}
		setDecision(signingId, ATTR_SIGN_CURRENT_RESOLUTION, sessionBean, serviceBean);//ATTR_DECISION
	}


	private QuickResolutionPortletSessionBean getSessionBean(
			PortletRequest request) {
		PortletSession session = request.getPortletSession();
		QuickResolutionPortletSessionBean result = (QuickResolutionPortletSessionBean) session
				.getAttribute(SESSION_BEAN);
		if (result == null) {
			result = createSessionBean(request);
			session.setAttribute(SESSION_BEAN, result);
		}
		return result;
	}
	
	public static QuickResolutionPortletSessionBean getPortletSessionBean(PortletRequest request) {
		PortletSession session = request.getPortletSession();
		QuickResolutionPortletSessionBean result = (QuickResolutionPortletSessionBean) session
				.getAttribute(SESSION_BEAN);
		if (result == null) {
			return null;
		}
		return result;
	}
	

	@SuppressWarnings("unchecked")
	private QuickResolutionPortletSessionBean createSessionBean(
			PortletRequest request) {
		QuickResolutionPortletSessionBean result = new QuickResolutionPortletSessionBean();
		//result.setMode(MODE_SELECT_EXECUTORS);

		DataServiceBean serviceBean = PortletUtil.createService(request);

		// �������� �������� "��������� ��� ��� ������������"
		Card armCard = ARMUtils.getArmSettings(serviceBean);
			

		// �������� ������� ���������� �� �������� �������������� ����������
		// "��������� ��� ��� ������������"
		// --------------------------------------------------------------
		final Set<Long> controllerIds = new HashSet<Long>();
		// final PersonAttribute attrPer = (PersonAttribute) armCard.getAttributeById(ATTR_CONTROLLER);
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
		// cardIds.addAll(controllerIds);
		final List<Long> cardIds = new ArrayList<Long>(controllerIds);

		// --------------------------------------------------------------

		// �������� ������ ������� ���������
		result.setStandartResolutionTexts(retrieveTypicalResolutionsOrVisas(serviceBean, armCard, ATTR_ARM_RESOL_LIST));
		// �������� ������ ������� ���
		result.setTypicalVisasTexts(retrieveTypicalResolutionsOrVisas(serviceBean, armCard, ATTR_ARM_VISA_LIST));

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
		final Map<Long, String> names = ARMUtils.getNameByCardIds(serviceBean, cardIds, CardAccess.READ_CARD);
		
		final Map<ObjectId, String> immediate = new LinkedHashMap<ObjectId, String>();
		for(final ObjectId cardId: immediateCardIds) {
			if(names.containsKey(cardId.getId())) {
				immediate.put(cardId, StringEscapeUtils.escapeHtml(names.get(cardId.getId())));
			}
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
		result.setExtPersonsSearch(getSearchExternalPerson());

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

	public static QuickResolutionPortletSessionBean getSessionBean(
			HttpServletRequest request, String namespace) {
		HttpSession session = request.getSession();
		String key = getApplicationSessionBeanKey(namespace);
		return (QuickResolutionPortletSessionBean) session.getAttribute(key);
	}

	private static String getApplicationSessionBeanKey(String namespace) {
		return APPLICATION_SESSION_BEAN_PREFIX + namespace;
	}

	private ObjectId saveResolution(
			QuickResolutionPortletSessionBean sessionBean, ActionRequest request) throws Exception{
		DataServiceBean serviceBean = PortletUtil.createService(request);
		CreateCard createCard = new CreateCard(TEMPLATE_RESOLUTION);
		createCard.setLinked(true);
		Card card = null;
		ObjectId cardId = null;
		try {
			Long mainDocCardIdLong = sessionBean.getBaseId();
			ObjectId mainDocCardId = ObjectIdUtils.getObjectId(Card.class, mainDocCardIdLong.toString(), true);
			Card mainDocCard = (Card)serviceBean.getById(mainDocCardId);
			createCard.setParent(mainDocCard);
			card = (Card) serviceBean.doAction(createCard);

			setResolutionAttributesFromSessionBean(card, sessionBean, serviceBean);

			if(TypeLink.BACKLINK.equals(sessionBean.getTypeLink())) {
				LinkAttribute mainDocAttr = (LinkAttribute) card.getAttributeById(ATTR_MAINDOC);
				mainDocAttr.addLinkedId(mainDocCardIdLong);
				if(TYPE_RESOLUTION_SECOND.equals(sessionBean.getTypeRes())) {
					LinkAttribute parentResAttr = (LinkAttribute) card.getAttributeById(ATTR_PARENT_RES);
					parentResAttr.addLinkedId(sessionBean.getParentId());
				} else if(TYPE_RESOLUTION_FIRST.equals(sessionBean.getTypeRes())) {
					LinkAttribute byDocLink = (LinkAttribute) card.getAttributeById(ATTR_DOCB_BYDOC);
					byDocLink.addLinkedId(mainDocCardIdLong);
				} else {
					logger.error("Attribute for link resolution with maindoc not found");
				}
			}
			
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
	private ObjectId saveControl(QuickResolutionPortletSessionBean sessionBean,
			ActionRequest request, ObjectId resolution) {
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
	
	/**
	 * ��������� ��������� ���������� ��������� �� � id
	 * @param resolutionCardId
	 * @param serviceBean
	 */
	private void saveRetryResolution(ObjectId resolutionCardId, DataServiceBean serviceBean){
		try {
			Card savedCard = (Card) serviceBean.getById(resolutionCardId);
			serviceBean.doAction(new LockObject(resolutionCardId));
			resolutionCardId = ((AsyncDataServiceBean) serviceBean).saveObject(savedCard, ExecuteOption.SYNC);
		} catch (Exception e) {
			e.printStackTrace();
			logger.warn("Cannot resave resolution for write controllers in base doc.");
			// ��� ���������� ��������� ��������� �� ��������
			// ToDo: ������������ esc-������������������, ����� � ������� ������ ���� ���������� ��������� �� ������
			//throw new Exception(e.getMessage().replace(Matcher.quoteReplacement("\n"), "\\n").replaceAll(Matcher.quoteReplacement("\t"), "\\t").replaceAll(Matcher.quoteReplacement("\r"), "\\r"));	// esc-������������������ �������� ������ � ��������� �������� �� �������������� �������);
		} finally {
			unlockQuickly(resolutionCardId, serviceBean);
		}
	}

	// ���������� �������� ��� ���������� � ������������
	private void update(QuickResolutionPortletSessionBean sessionBean,
			ActionRequest request) throws Exception {
		DataServiceBean serviceBean = PortletUtil.createService(request);

		ObjectId resolutionCardId = null;
		if (STATE_INIT_CREATE.equals(sessionBean.getStateInit())) {
			// ��������� ���������
			resolutionCardId = saveResolution(sessionBean, request);
			sessionBean.setIdResolution(resolutionCardId);
			// ������ ������ � ���. �������� �� ���������
			final ObjectId parentId = new ObjectId(Card.class, sessionBean.getParentId());
			if(TypeLink.CARDLINK.equals(sessionBean.getTypeLink()))
			if (TYPE_RESOLUTION_SECOND.equals(sessionBean.getTypeRes())) {
					setCardLink(parentId, ATTR_RIMP_RELASSIG, resolutionCardId, serviceBean);				
			} else {
					setCardLink(parentId, ATTR_IMPL_RESOLUT, resolutionCardId, serviceBean);
			}
			saveRetryResolution(resolutionCardId, serviceBean);
		} else {
			// ��������� ��������� ���������
			resolutionCardId = sessionBean.getIdResolution();
			updateResolution(sessionBean, request);
		}

		// ��������� �������� ����� ����������� ���������, � ��������� ������ ��
		// ��� � ���������
		if (sessionBean.getChangeGrap()) {
			saveGraphicalResolution(resolutionCardId, sessionBean, serviceBean);
		}

		// (YNikitin, 2013/07/18) ��������� ��������� ���������� �� (� ����� ������ ��� => � ����� ��� �� �����)
		// ��������� �������� ��������� (���������� � ������������)
/*		if (	TARGET_INIT_EXECUTION.equals(sessionBean.getTargetInit())
			|| 	TARGET_INIT_CONSIDERATION.equals(sessionBean.getTargetInit())) {
			ObjectId baseDocId = new ObjectId(Card.class, sessionBean
					.getBaseId());
			updateBaseDocument(baseDocId, sessionBean, PortletUtil.createService(request));
		}*/
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

	private void initFromParentCard(PortletRequest request, QuickResolutionPortletSessionBean sessionBean)  throws PortletException {

		DataServiceBean serviceBean = PortletUtil.createService(request);
		
		Card parentCard = getCardById(sessionBean.getBaseId(), serviceBean);
		
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

	private Card getCardById(Long id,
			DataServiceBean serviceBean) throws PortletException {
		
		ObjectId cardId = new ObjectId(Card.class, id);
		
		return  getCardById(cardId, serviceBean);
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
	
	private void initFromResolutionCard(PortletRequest request,
			QuickResolutionPortletSessionBean sessionBean, long cardId) {
		try {
			DataServiceBean serviceBean = PortletUtil.createService(request);
			final Card card = (Card) serviceBean.getById(new ObjectId(Card.class, cardId));

			final Collection<Long> cardIds = new ArrayList<Long>();
			// ������������� �����������
			Long responsibleId = null;
			PersonAttribute perAttr = (PersonAttribute) card.getAttributeById(ATTR_EXECUTOR);
			Collection<Person> perList = SearchUtils.getAttrPersons(perAttr);
			if (perList != null && perList.size() > 0) {
				responsibleId = (Long) perList.iterator().next().getCardId().getId();
				cardIds.add(responsibleId);
			}


			// �������������
			final Map<Long, String> additionals = new LinkedHashMap<Long, String>();
			perAttr = (PersonAttribute) card.getAttributeById(ATTR_COEXECUTORS);
			perList = SearchUtils.getAttrPersons(perAttr);
			if (perList != null){
				for(Person p : perList) {
					final Long id = (Long) p.getCardId().getId();
					additionals.put(id, "");
					cardIds.add(id);
				}
			}

			// � ��������
			final Map<Long, String> fyi = new LinkedHashMap<Long, String>();
			perAttr = (PersonAttribute) card.getAttributeById(ATTR_FYI);
			perList = SearchUtils.getAttrPersons(perAttr);
            if (perList != null){
                for(Person p : perList) {
                    final Long id = (Long) p.getCardId().getId();
                    fyi.put(id, "");
                    cardIds.add(id);
                }
            }
			/*final Collection<ObjectId> idsFYI = SearchUtils.getAttrLinks(card, ATTR_FYI);
			if (idsFYI != null) {
				for( ObjectId objId : idsFYI ) {
					final Long id = (Long) objId.getId();
					fyi.put(id, "");
					cardIds.add(id);
				}
			}*/

			// ������� �����������
			final Map<Long, String> externals = new LinkedHashMap<Long, String>();
			final Collection<ObjectId> idsExecExt = SearchUtils.getAttrLinks(card, ATTR_EXEC_EXT);
			if (idsExecExt != null) {
				for( ObjectId objId : idsExecExt ) {
					final Long id = (Long) objId.getId();
					externals.put(id, "");
					cardIds.add(id);
				}
			}

			// ���������
			final Map<Long, String> controllers = new LinkedHashMap<Long, String>();
			final Collection<Person> personsInspect = SearchUtils.getAttrPersons(card, ATTR_TCON_INSPECTOR);
			if (personsInspect != null) {
				for(Person p: personsInspect) {
					Long controllerId = (Long) p.getCardId().getId();
					controllers.put(controllerId, "");
					cardIds.add(controllerId);
				}
			}

			final Map<Long, String> names = ARMUtils.getNameByCardIds(serviceBean, cardIds);
			if (responsibleId != null) {
				sessionBean.setResponsible(responsibleId, names.get(responsibleId));
			}
			for( Long id: additionals.keySet() ) {
				additionals.put(id, names.get(id));
			}
			sessionBean.setAdditionals(additionals);

			for( Long id: externals.keySet() ) {
				externals.put(id, names.get(id));
			}
			sessionBean.setExternals(externals);

			for( Long id: fyi.keySet()) {
				fyi.put(id, names.get(id));
			}
			sessionBean.setFyi(fyi);

			for( Long id: controllers.keySet()) {
				controllers.put(id, names.get(id));
			}
			sessionBean.setControllers(controllers);
			// -----------------------------------------------------------

			// ����� ���������
			final String textResolution = ((TextAttribute) card.getAttributeById(ATTR_RESOLUT)).getValue();
			sessionBean.setResolutionText(textResolution);
			

			// ���� ����������
			sessionBean.setTermAttribute((DateAttribute) card.getAttributeById(ATTR_TERM));
			
			// ��������������� ����
			final Date preliminaryTerm = ((DateAttribute) card.getAttributeById(ATTR_PRELIMINARY_TERM)).getValue();
			sessionBean.setPreliminaryTerm(preliminaryTerm);
			
			// �� ��������
			boolean isOnControl = false;
			ReferenceValue value = ((ListAttribute)card.getAttributeById(ATTR_TCON_ONCONT)).getValue();
			if(value != null && value.getId() != null) {
				if(VALUE_YES_ON_TCON_ONCONT == ((Long) value.getId().getId()).longValue()){
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

			// ���������� id ��������� ���������
			final CardLinkAttribute cla = card.getCardLinkAttributeById(ATTR_MAINDOC);
			Long baseId = null;
			if(cla != null && !cla.isEmpty()) {
				final ObjectId baseObjId = cla.getIdsLinked().get(0);
				baseId = baseObjId != null ? (Long) baseObjId.getId() : null;
				if(cla.getIdsLinked().size() > 1) {
					logger.info("Attribute " + ATTR_MAINDOC + " in card " + card.getId().toString() + " has more than 1 value");
				}
			}
			sessionBean.setBaseId(baseId);

			// ���������� id ������������ ��������
			if (TYPE_RESOLUTION_SECOND.equals(sessionBean.getTypeRes())) {
				// ���. �������� � ���. ��������� �� ���������
				// ���������� ���. ��������
				final CardLinkAttribute parentAttr = card.getCardLinkAttributeById(ATTR_PARENT_RES);
				Long parentId = null;
				if(parentAttr != null && !parentAttr.isEmpty()) {
					final ObjectId parentObjId = parentAttr.getIdsLinked().get(0);
					parentId = parentObjId != null ? (Long) parentObjId.getId() : null;
					if(parentAttr.getIdsLinked().size() > 1) {
						logger.info("Attribute " + ATTR_PARENT_RES + " in card " + card.getId().toString() + " has more than 1 value");
					}
				}
				sessionBean.setParentId(parentId);
			} else {
				sessionBean.setParentId(sessionBean.getBaseId());
			}
 			// ���������� ���� ����������� ���������
			ObjectId grapResId = getGraphicalResolution(card);
			// ��������� ���� � �����������
			if (grapResId != null)
				sessionBean.setGrapResId( (Long) grapResId.getId());
			// ----------------------------------------------------
			
			// gets attachments
			CardLinkAttribute docLinks = null;
			if (TEMPLATE_RESOLUTION.equals(card.getTemplate())) {
				docLinks = (CardLinkAttribute) card.getAttributeById(ATTR_DOCLINKS);
			} else {
				docLinks = (CardLinkAttribute) card.getAttributeById(ATTR_VISA_ATTACHMENTS);
			}
			Map<ObjectId, String> attachedFilesMap = new LinkedHashMap<ObjectId, String>();
			if (docLinks != null) {
				Collection<ObjectId> attachedFileIds = docLinks.getIdsLinked();
				for (ObjectId attachedFileId : attachedFileIds) {
					Card attachedCard = getCardById(attachedFileId, serviceBean);
					MaterialAttribute materialAttr = (MaterialAttribute) attachedCard.getAttributeById(Attribute.ID_MATERIAL);
					attachedFilesMap.put(attachedFileId, materialAttr.getMaterialName());
				}
			}
			// init attached files field
			sessionBean.setAttachedFiles(attachedFilesMap);
			
		} catch (Exception e) {
			logger.error(
					"Error reading attributes from the card 'Resolution':", e);
		}
	}

	/*private Long getCardIdFromBackLink(ObjectId cardId, ObjectId attrId, DataServiceBean serviceBean) {
		Long linkedId = null;
		try {
			final ListProject search = new ListProject();
			search.setAttribute(attrId);
			search.setCard(cardId);
			final List<Card> cards = SearchUtils.execSearchCards(search, serviceBean);
			final Card linked = cards.get(0);
			linkedId = (Long) linked.getId().getId();
		} catch (Exception e) {
			logger.error("Error in get cardId from backlink: " + e );
		}
		return linkedId;
	}*/

	private ObjectId getGraphicalResolution(Card card) {
		ObjectId grapResId = null;
		// ���� �������� ����������� ���������
		final TypedCardLinkAttribute attrLink = (TypedCardLinkAttribute)card.getAttributeById(ATTR_RESOLUT_ATTACH);
		final ObjectId[] fileCardIds = (attrLink!=null)?attrLink.getIdsArray():null;
		if (fileCardIds != null)
		{
			for (int j = 0; j < fileCardIds.length; j++) {
				final Long typeLink = (Long) attrLink.getTypes().get( fileCardIds[j].getId() );
				if (VALUE_GRAPHICAL.equals(typeLink)) {
					grapResId = fileCardIds[j]; // found
					break;
				}
			}
		}
		return grapResId;
	}

	private void updateResolution(QuickResolutionPortletSessionBean sessionBean, ActionRequest request) throws Exception{
		boolean locked = false;
		final DataServiceBean serviceBean = PortletUtil.createService(request);
		final ObjectId cardId = sessionBean.getIdResolution();
		try {
			serviceBean.doAction(new LockObject(cardId));
			locked = true;
			Card card = (Card) serviceBean.getById(cardId);

			setResolutionAttributesFromSessionBean(card, sessionBean, serviceBean);

			// ��������� ��������� � ��������
			((AsyncDataServiceBean) serviceBean).saveObject(card, ExecuteOption.SYNC);
		} catch (Exception e) {
			logger.error("Error to update resolution:", e);
			// ToDo: ������������ esc-������������������, ����� � ������� ������ ���� ���������� ��������� �� ������
			throw new Exception(e.getMessage().replace(Matcher.quoteReplacement("\n"), "\\n").replaceAll(Matcher.quoteReplacement("\t"), "\\t").replaceAll(Matcher.quoteReplacement("\r"), "\\r"));	// esc-������������������ �������� ������ � ��������� �������� �� �������������� �������);
		} finally {
			if (locked) {
				unlockQuickly(cardId, serviceBean);
			}
		}
	}
	
	private void updateAttachedFiles(QuickResolutionPortletSessionBean sessionBean, DataServiceBean serviceBean, ObjectId attachmentAttrId) throws DataException, ServiceException {
		ObjectId cardId = null;
		if (ATTR_DOCLINKS.equals(attachmentAttrId)) {// attachments for visas are stored in ATTR_DOCLINKS instead of ATTR_VISA_ATTACHMENTS
			cardId = getVisa(sessionBean, serviceBean);
		} else if (ATTR_SIGN_ATTACHMENTS.equals(attachmentAttrId)) {
			cardId = getSign(sessionBean, serviceBean);
		}

		if (cardId != null) {
			// lock card
			serviceBean.doAction(new LockObject(cardId));

			Card theCard = null;
			try {
				theCard = (Card) serviceBean.getById(cardId);
				final CardLinkAttribute attrLink = (CardLinkAttribute) theCard.getAttributeById(attachmentAttrId);
				if (attrLink != null) {
					attrLink.setIdsLinked(sessionBean.getAttachedFiles().keySet());
					((AsyncDataServiceBean) serviceBean).saveObject(theCard, ExecuteOption.SYNC);
				}
			} finally {
				if (theCard != null) {
					serviceBean.doAction(new UnlockObject(theCard.getId()));
				}
			}
		}
	}

	// ��������� null, ���� �������� '�� ������ ��������' �� ���������� �
	// ��������� �� �� ����
	// ��������� id �������� ��� �������� ��� ���������� ��������
	private ObjectId updateControl(
			QuickResolutionPortletSessionBean sessionBean, ActionRequest request) {
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
			return saveControl(sessionBean, request, sessionBean.getIdResolution());
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
				ObjectId cardId = new ObjectId(Card.class, i.next());
				if(cardId != null)
					cardIds.add(cardId);
				else if(logger.isWarnEnabled())
					logger.warn("NULL card id was provided");
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
			QuickResolutionPortletSessionBean sessionBean,
			DataServiceBean serviceBean) {
		// �������� ������������ ����� card_id � person_id ������������
		final List<Long> cardIds = new ArrayList<Long>();

		cardIds.add(sessionBean.getResponsibleId());
		final Set<Long> controllerIds = sessionBean.getControllers().keySet();
		cardIds.addAll(controllerIds);
		cardIds.addAll( sessionBean.getAdditionals().keySet());
		cardIds.addAll( sessionBean.getFyi().keySet());
		// map of all persons, just for convenience 
		final Map<Long, Person> persons = getPersonsByCardIds(serviceBean, cardIds);

		// executor
		final Person executor = persons.get(sessionBean.getResponsibleId());
		if (executor != null) {
			((PersonAttribute) card.getAttributeById(ATTR_EXECUTOR)).setPerson(executor);
		}

		// coexecutor
		PersonAttribute persAttr = (PersonAttribute) card.getAttributeById(ATTR_COEXECUTORS);
		persAttr.clear();

		List<Person> personsCol = new ArrayList<Person>();
		for( final Iterator<Long> iter = sessionBean.getAdditionals().keySet().iterator();
				iter.hasNext(); ) {
			final Person itemP = persons.get(iter.next());
			if(itemP != null){
				personsCol.add(itemP);
			}
		}
		if(personsCol.size()>0){
			persAttr.setValues(personsCol);
		}

        persAttr = (PersonAttribute) card.getAttributeById(ATTR_FYI);
        persAttr.clear();

        personsCol = new ArrayList<Person>();
		for( final Iterator<Long> iterFyi = sessionBean.getFyi().keySet().iterator();
			iterFyi.hasNext(); ) {
            final Person itemP = persons.get(iterFyi.next());
            if (itemP != null) {
            	personsCol.add(itemP);
            }
        }
        if(personsCol.size()>0){
            persAttr.setValues(personsCol);
		}

        CardLinkAttribute linkAttr = (CardLinkAttribute) card.getAttributeById(ATTR_EXEC_EXT);
		linkAttr.clear();

		for (final Iterator<Long> iter = sessionBean.getExternals().keySet().iterator();
				iter.hasNext(); ) {
			linkAttr.addLinkedId(new ObjectId(Card.class, iter.next()));
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
			QuickResolutionPortletSessionBean sessionBean, boolean isDone)
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
	private Search getSearchExternalPerson() {
		final Search search = new Search();
		List<SearchResult.Column> columns = ARMUtils.getFullNameColumns();
		SearchUtils.addColumns(columns, ObjectId.predefined(StringAttribute.class, "jbr.person.position"));
		search.setColumns(columns);

		search.setByAttributes(true);
		search.setWords(null);

		final List<DataObject> templates = new ArrayList<DataObject>(1);
		templates.add(DataObject.createFromId(TEMPLATE_EXT_PERSON));
		search.setTemplates(templates);

		search.addStringAttribute(Attribute.ID_NAME);
		
		// (BR4J00033862, YNikitin, 2013/10/29) ��� ����������� ������ ������� ������ ����������� ��������� ����� �������, �.�. �������� ���������� �� ����� ���
		search.getFilter().setCurrentUserRestrict(Search.Filter.CU_READ_PERMISSION);
//		Collection<ReferenceValue> values = new ArrayList<ReferenceValue>(1);
//		ReferenceValue refValue = new ReferenceValue();
//		refValue.setId(ObjectId.predefined(ReferenceValue.class, "jbr.incoming.control.yes"));
//		values.add(refValue);
//		search.addListAttribute(ObjectId.predefined(ListAttribute.class, "jbr.person.choseExt"), values);
		
		final List<String> states = new ArrayList<String>(2);
		states.add(DICTIONARY_NEW.getId().toString());
		states.add(CardState.PUBLISHED.getId().toString());
		search.setStates(states);

		return search;
	}


	// Collections cardIds - ��������� id �������� ���� Long
	// ���������� List id �������� (���� Long) ������� �� ����� ������
	private List<Long> getNonexistentPersons(DataServiceBean serviceBean,
			Collection<Long> cardIds) {
		final List<Long> nonex = new ArrayList<Long>();
		final Collection<Long> persons = getPersonsByCardIds(serviceBean, cardIds).keySet();
		final Iterator<Long> i = cardIds.iterator();
		while (i.hasNext()) {
			final Long id = i.next();
			if (!persons.contains(id))
				nonex.add(id);
		}
		return nonex;
	}

	// ���������� �������� "������ ��������" ��������� � ������������ ����������
	// ���� ����� �������� �� ������� �� ������������� null
	private void searchPersonalControlCard(
			QuickResolutionPortletSessionBean sessionBean,
			DataServiceBean serviceBean) {
		// ���� �������� "������ ��������" ��������� � ���������� ����������
		final Search search = new Search();
		search.setWords("");
		search.setByAttributes(true);

		search.addCardLinkAttribute(ATTR_PCON_DOC, new ObjectId(Card.class, sessionBean.getBaseId()));
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

	/**
	 * �������� ������������ �� ������������� ���-��������� � ����� �� ��� ����.
	 * @param sessionBean
	 * @param serviceBean
	 */
	private void searchMainDocExecutors(
			QuickResolutionPortletSessionBean sessionBean,
			DataServiceBean serviceBean) 
	{
		if (sessionBean == null || serviceBean == null || sessionBean.getBaseId() == null)
			return;

		final Long mainDocId = sessionBean.getBaseId();

		try {
			final Card mainCard = (Card) serviceBean.getById( new ObjectId( Card.class, mainDocId));
			if (mainCard == null) {
				logger.warn( "Main doc card "+ mainDocId + " not loaded");
			} else {
				final Map<Long, String> execMap = loadPersonsMap(serviceBean, mainCard, ATTR_MAINDOC_EXECUTOR);
				if (execMap != null && !execMap.isEmpty()) {
					// final Map.Entry< Long, String> entry = execMap.entrySet().iterator().next();
					// sessionBean.setResponsible( entry.getKey(), entry.getValue() );
					final Map<Long, String> resp = sessionBean.getResponsible();
					resp.clear();
					resp.putAll(execMap);
				}
			}
		} catch (Exception e) {
			logger.error("Error search of maindoc card " + mainDocId + " attribute "+ ATTR_MAINDOC_EXECUTOR, e);
		}
	}

	private void setAttributePersonControl(Card card,
			QuickResolutionPortletSessionBean sessionBean) {
		// ����������
		Person controller = sessionBean.getCurrentPerson();
		((PersonAttribute) card.getAttributeById(ATTR_PCON_PERSON)).setPerson(controller);

		final CardLinkAttribute attr = (CardLinkAttribute)card.getAttributeById(ATTR_PCON_DOC);
		/* (2010/02, RuSA)
		Card res = new Card();
		res.setId(sessionBean.getParentId().longValue());
		attr.setValues(new ArrayList(1));
		attr.addValue(res);
		 */
		attr.addSingleLinkedId(sessionBean.getBaseId().longValue());

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

	/*private void updateBaseDocument(ObjectId cardId,
			QuickResolutionPortletSessionBean sessionBean,
			DataServiceBean serviceBean) throws Exception {
		if (sessionBean.getTerm() == null) {
			return;
		}
		boolean locked = false;
		try {
			// �� id �������� ��������� ��������� ��������� �� ������
			serviceBean.doAction(new LockObject(cardId));
			locked = true;
			final Card card = (Card) serviceBean.getById(cardId);*/
			/* (2011/03/21, RuSA) ������� ������������ ���������� �� �������� - 
			 * ������ ��������� ����-����������:
			 * 
			final ListAttribute typeControl = (ListAttribute) card.getAttributeById(ATTR_TYPE_CONTROL);
			if (typeControl != null && typeControl.getValue() == null) {
				typeControl.setValue((ReferenceValue) DataObject.createFromId(VALUE_BOSS_CONTROL));

				final ListAttribute onControl = (ListAttribute) card.getAttributeById(ATTR_ON_CONTROL);
				onControl.setValue((ReferenceValue) DataObject.createFromId(VALUE_ON_CONTROL));
				serviceBean.saveObject(card);
			}
			*/
			/*((AsyncDataServiceBean) serviceBean).saveObject(card, ExecuteOption.SYNC);
		} catch (Exception e) {
			logger.error("Error updating base document card:", e);
			// ToDo: ������������ esc-������������������, ����� � ������� ������ ���� ���������� ��������� �� ������
			//throw new Exception(e.getMessage().replace(Matcher.quoteReplacement("\n"), "\\n").replaceAll(Matcher.quoteReplacement("\t"), "\\t").replaceAll(Matcher.quoteReplacement("\r"), "\\r"));	// esc-������������������ �������� ������ � ��������� �������� �� �������������� �������);
		} finally {
			if (locked) {
				unlockQuickly(cardId, serviceBean);
			}
		}
	}*/

	/*
	private void changeStatusConsideration(
			QuickResolutionPortletSessionBean sessionBean,
			DataServiceBean serviceBean) {
		boolean locked = false;
		ObjectId considerId = null;
		try {
			// 1. �������� id �������� ������������
			// 1.1. ���������� id �������� �� ������� ���������� ��������
			ObjectId inboundId = new ObjectId(Card.class, sessionBean
					.getBaseId());
			Card inbound = (Card) serviceBean.getById(inboundId);
			CardLinkAttribute attr = inbound
					.getCardLinkAttributeById(ATTR_CONSIDIRATIONS);

			// 1.2. ������� id �������� ����������� � ������� 102 � �������
			// ������������� � �������� ����������������
			final Search search = new Search();
			search.setByCode(true);
			search.setByAttributes(true);
			search.addPersonAttribute(ATTR_SEISED, sessionBean
					.getCurrentPerson().getId());

			final List<String> states = new ArrayList<String>(1);
			states.add(CARDSTATE_CONSIDERATION.getId().toString());
			search.setStates(states);

			if (attr.getIdsLinked() != null)
				search.setWords(attr.getLinkedIds());
			else
				search.setWords("");

			final Collection<Card> cards = ((SearchResult)serviceBean.doAction(search)).getCards();
			if (cards.size() == 0)
				throw new Exception(
						"Not suitable of card for a 'Consideration' to change the status");

			if (cards.size() > 1)
				logger.warn("Several cards 'Consideration' is suitable to change the status");

			considerId = cards.iterator().next().getId();

			// 2. �������� ������ �������� ������������
			serviceBean.doAction(new LockObject(considerId));
			locked = true;

			Card consider = (Card) serviceBean.getById(considerId);
			WorkflowMove wfm = (WorkflowMove) serviceBean
					.getById(WFM_ON_SEND_EXECUTION);
			ChangeState actionChange = new ChangeState();
			actionChange.setCard(consider);
			actionChange.setWorkflowMove(wfm);

			serviceBean.doAction(actionChange);
		} catch (Exception e) {
			logger.error("Error change status of card 'Consideration':", e);
		} finally {
			if (locked) {
				unlockQuickly(considerId, serviceBean);
			}
		}
	}
	*/

	
	
	// ��������� �������� ������� �� ������� ���������� � ������ '�������� �������������', �.�. �� ������� 108 �
	// 477933
	private WorkflowMove changeStatusSign(
			QuickResolutionPortletSessionBean sessionBean,
			DataServiceBean serviceBean) {
		WorkflowMove wfm = null;
		boolean locked = false;
		ObjectId signingId = null;
		try {
			signingId = getSign(sessionBean, serviceBean);
			if (signingId == null)
				return null;

			// �������� ������ �������� �������
			serviceBean.doAction(new LockObject(signingId));
			locked = true;
			
			ObjectId wfmId = WFM_SIGN_REJECT_BOSS;
			
			if(PARAM_DECISION_DECLINE.equals(sessionBean.getDecision())){
				wfmId = WFM_SIGN_DECLINE;
			}

			Card signing = (Card) serviceBean.getById(signingId);
			wfm = (WorkflowMove) serviceBean.getById(wfmId);
			ChangeState actionChange = new ChangeState();
			actionChange.setCard(signing);
			actionChange.setWorkflowMove(wfm);

			serviceBean.doAction(actionChange);
		} catch (Exception e) {
			logger.error("Error change status of card 'Signing'", e);
		} finally {
			if (locked) {
				unlockQuickly(signingId, serviceBean);
			}
		}
		return wfm;
	}

	// ��������� �������� ����������� �� ������� ������������ � ������ '�� ��������',
	// �.�. �� ������� 107 � 2031 - 35266 ��� ��������� ��� ����������. 
	//���� ������� ���������, �� 
	
	private WorkflowMove changeStatusVisa(
			QuickResolutionPortletSessionBean sessionBean,
			DataServiceBean serviceBean) {
		boolean locked = false;
		ObjectId visaId = null;
		WorkflowMove wfm = null;
		try {
			visaId = getVisa(sessionBean, serviceBean);
			if (visaId == null)
				return null;

			// �������� ������ �������� �������
			serviceBean.doAction(new LockObject(visaId));
			locked = true;
			
			ObjectId wfmId = WFM_VISA_DISAGREE;
			
			if(PARAM_DECISION_COMMENT.equals(sessionBean.getDecision())){					
				wfmId = WFM_VISA_COMMENT;					
			}else if(PARAM_DECISION_DECLINE.equals(sessionBean.getDecision())){
				wfmId = WFM_VISA_DECLINE;
			}

			Card visa = (Card) serviceBean.getById(visaId);
			wfm = (WorkflowMove) serviceBean.getById(wfmId);
			ChangeState actionChange = new ChangeState();
			actionChange.setCard(visa);
			actionChange.setWorkflowMove(wfm);

			serviceBean.doAction(actionChange);
		} catch (Exception e) {
			logger.error("Error change status of card 'Visa'", e);
		} finally {
			if (locked) {
				unlockQuickly(visaId, serviceBean);
			}
		}
		return wfm;
	}

	private ObjectId getSign(QuickResolutionPortletSessionBean sessionBean,
			DataServiceBean serviceBean) throws DataException, ServiceException {
		// �������� id �������� �������
		ObjectId baseId = new ObjectId(Card.class, sessionBean
				.getBaseId());
		Card outbound = (Card) serviceBean.getById(baseId);
		CardLinkAttribute attr = outbound
				.getCardLinkAttributeById(ATTR_SIGNING);
		// ������� id �������� ������� � ������� 108 � ������� �������������
		// � �������� ���������
		final Search search = new Search();
		search.setByCode(true);
		search.setByAttributes(true);
		search.addPersonAttribute(ATTR_SIGNER, sessionBean
				.getCurrentPerson().getId());
		final List<String> states = new ArrayList<String>(1);
		states.add(CARDSTATE_SIGNING.getId().toString());
		search.setStates(states);

		// (2010/02, RuSA) CardLink::getValues()
		if (attr.getIdsLinked() != null)
			search.setWords(ObjectIdUtils.numericIdsToCommaDelimitedString(ObjectIdUtils.collectionToSetOfIds(attr.getIdsLinked())));

		final List<Card> cards = SearchUtils.execSearchCards(search, serviceBean);
		if (cards == null || cards.size() == 0)
			throw new DataException(
					"No suitable card for a 'Signing' at status " + CARDSTATE_SIGNING.getId());
		if (cards.size() > 1)
			logger.warn("Several cards 'Signing' is suitable to change the status");
		return cards.get(0).getId();
	}
	
	/*private Card createFileCard(DataServiceBean serviceBean) throws ServiceException, DataException {
		//create new file card
		CreateCard createCard = new CreateCard(TEMPLATE_FILE);
		return (Card) serviceBean.doAction(createCard);
	}*/
	
	/*private void uploadFileCard(Card fileCard, FileItem fileItem, DataServiceBean serviceBean) throws ServiceException, DataException , IOException {

		//upload file 
		UploadFile uploadAction = new UploadFile();
		uploadAction.setCardId(fileCard.getId());
		uploadAction.setFileName(fileItem.getFieldName());
		uploadAction.setData(fileItem.getInputStream());
		serviceBean.doAction(uploadAction);
		
	}*/
	
	/*private Card saveResolutionAttacment(FileItem fileItem,	DataServiceBean serviceBean)  throws PortletException {
		try {
			Card newFileCard = createFileCard(serviceBean);
			uploadFileCard(newFileCard, fileItem, serviceBean);
			return newFileCard;
		} catch (ServiceException e) {
			logger.error("Error in creating attachment card", e);
			throw new PortletException(e);
		} catch (DataException e) {
			logger.error("Error in creating attachment card", e);
			throw new PortletException(e);
		} catch (IOException e) {
			logger.error("Error in creating attachment card", e);
			throw new PortletException(e);
		} 
	}*/

	// �������/��������� �������� ����. ���������
	// � ������� ������ � ��������� �� ���
	private ObjectId saveGraphicalResolution(ObjectId resId,
			QuickResolutionPortletSessionBean sessionBean,
			DataServiceBean serviceBean) throws Exception {

		Card grapRes = null;
		Card res = null;
		ObjectId grapId = null;

		boolean lockedGrap = false;
		boolean lockedRes = false;
		boolean newFileCard = false;

		try {
			// ��������� �������� ���������
			serviceBean.doAction(new LockObject(resId));
			lockedRes = true;
			res = (Card) serviceBean.getById(resId);

			// ���� �������� ����������� ���������
			// >>> (2010/02, RuSA) CardLink::getValues()
			//final ObjectId grapResId = this.getGraphicalResolution(res);
			final TypedCardLinkAttribute attrLink = (TypedCardLinkAttribute)res.getAttributeById(ATTR_RESOLUT_ATTACH);

			// ���� �������� �� �������, �� �������, ����� ��������� ��� ������
			//if (grapRes == null) {
				CreateCard createCard = new CreateCard(TEMPLATE_FILE);
				grapRes = (Card) serviceBean.doAction(createCard);
				// TODO: ������� ������������� �������� � ��������
				((StringAttribute) grapRes.getAttributeById(ATTR_NAME))
						.setValue("���������");
				grapId = ((AsyncDataServiceBean) serviceBean).saveObject(grapRes, ExecuteOption.SYNC);
				grapRes.setId(((Long) grapId.getId()).longValue());
				lockedGrap = true;
				newFileCard = true;
			/*} else {
				serviceBean.doAction(new LockObject(grapResId));
				lockedGrap = true;
				grapRes = (Card) serviceBean.getById(grapResId);
				grapId = grapRes.getId();
			}*/
			// <<< (2010/02, RuSA) CardLink::getValues()

			// ��������� ���� � ��������
			final FileItem grapItem = sessionBean.getGrapFile();
			final UploadFile uploadAction = new UploadFile();
			uploadAction.setCardId(grapRes.getId());
			final String filePath = grapItem.getName();
			String fileName = filePath.substring((filePath.lastIndexOf("\\") + 1));
			if (fileName.indexOf('.') < 0) {
				fileName += ".jpg";
			}
			uploadAction.setFileName(fileName);
			uploadAction.setData(grapItem.getInputStream());

			serviceBean.doAction(uploadAction);

			// ��������� ������ � ��������� �� �������� �����
			if (newFileCard) {
				attrLink.addLabelLinkedCard(grapRes);
				attrLink.addType((Long) grapRes.getId().getId(),
						VALUE_GRAPHICAL);
				((AsyncDataServiceBean) serviceBean).saveObject(res, ExecuteOption.SYNC);
			}
		} catch (Exception e) {
			logger.error("Error saving graphical resolution", e);
			// ��� ���������� ������������ ��������� ��������� �� ��������
			// ToDo: ������������ esc-������������������, ����� � ������� ������ ���� ���������� ��������� �� ������
			//throw new Exception(e.getMessage().replace(Matcher.quoteReplacement("\n"), "\\n").replaceAll(Matcher.quoteReplacement("\t"), "\\t").replaceAll(Matcher.quoteReplacement("\r"), "\\r"));	// esc-������������������ �������� ������ � ��������� �������� �� �������������� �������);
		} finally {
			if (lockedGrap && grapRes != null) {
				unlockQuickly(grapRes.getId(), serviceBean);
			}
			if (lockedRes && res != null) {
				unlockQuickly(res.getId(), serviceBean);
			}
		}
		return (grapRes != null) ? grapRes.getId() : null;
	}

	private void setDecision(ObjectId cardForRecordId,
							 ObjectId attrForRecordId,
							 QuickResolutionPortletSessionBean sessionBean,
							 DataServiceBean serviceBean) {
		// ��������� �������� (������� ��� �����������) ��� ������
		boolean locked = false;
		try {
			if (cardForRecordId == null) {
				return;
			}

			serviceBean.doAction(new LockObject(cardForRecordId));
			locked = true;
			final Card cardRecord = (Card) serviceBean.getById(cardForRecordId);
			final TextAttribute attr = ((TextAttribute) cardRecord.getAttributeById(attrForRecordId));
			attr.setValue(sessionBean.getResolutionText());
			
//			final StringBuffer text = formFullResolutionText(sessionBean);
//			final HtmlAttribute attr = ((HtmlAttribute) cardRecord.getAttributeById(attrForRecordId));
//			final ReportXMLEditor editor = new RepeatableReportXMLEditor(attr.getValue(), null);
//			editor.appendPart("", null, "", text.toString());
//			attr.setValue(editor.serialize());
			
			((AsyncDataServiceBean)serviceBean).saveObject(cardRecord, ExecuteOption.SYNC);
		} catch (Exception e) {
			logger.error("Error change status of card 'Signing'", e);
		} finally {
			if (locked) {
				unlockQuickly(cardForRecordId, serviceBean);
			}
		}
	}

	/*private StringBuffer formFullResolutionText(QuickResolutionPortletSessionBean sessionBean) {
		final StringBuffer text = new StringBuffer();
		final String responsibles = sessionBean.getResponsibleNames();
		if (responsibles != null && responsibles.trim().length() > 0) {
			text.append("���. �����������: " + responsibles + "\n");
		}
		final String additionals = sessionBean.getAdditionalsNames();
		if (additionals != null && additionals.trim().length() > 0) {
			text.append("�������������: " + additionals + "\n");
		}
		final String fyi = sessionBean.getFyiNames();
		if (fyi != null && fyi.trim().length() > 0) {
			text.append("� ��������: " + fyi + "\n");
		}
		final String externals = sessionBean.getExternalsNames();
		if (externals != null && externals.trim().length() > 0) {
			text.append("������� �����������: " + externals + "\n");
		}
		if (sessionBean.getTerm() != null) {
			String term = new SimpleDateFormat("dd.MM.yyyy").format(sessionBean
					.getTerm());
			text.append("����: " + term + "\n");
		}
		if (sessionBean.getControlTerm() != null) {
			String controlTerm = new SimpleDateFormat("dd.MM.yyyy")
					.format(sessionBean.getControlTerm());
			text.append("�� ��������: " + controlTerm + "\n");
		}
		final String resolution = sessionBean.getResolutionText();
		if (resolution != null && resolution.trim().length() > 0) {
			text.append("����� ���������: " + resolution);
		}
		return text;
	}*/

	/**
	 * �������� ����� id-������������ �������� - ��� ��� ������ �� ������ ������ 
	 * ��������.
	 * @param card
	 * @param personAttrId
	 * @return ����� ����=id ������������ ��������, �������� = ���.
	 */
	private Map<Long, String> loadPersonsMap( 
			DataServiceBean serviceBean,
			Card card, ObjectId personAttrId)
	{
		Map<Long, String> result = new HashMap<Long, String>(0);
		if (serviceBean != null && card != null && personAttrId != null) {

			try {
				final Collection<Person> perList = SearchUtils.getAttrPersons( card, personAttrId);
				if (perList != null) {
					final Collection<Long> cardIds = new ArrayList<Long>();
					for(Person p: perList ) {
						final Long id = (Long) p.getCardId().getId();
						cardIds.add(id);
					}
					result = ARMUtils.getNameByCardIds(serviceBean, cardIds);
				}
			} catch (DataException ex) {
				logger.warn( "Error getting persons from attribute "+ personAttrId 
						+ " of card " + card.getId(), ex);
			}
		}
		return result;

	}
	
	/**
	 * �������� �������� �� �������������� � ������������ ������� � ������� ��� ����������� �� ��� �����������
	 * @param card - ����������� ��������
	 * @return
	 */
	private boolean CheckMainCard(Card card){
		// ����� ���� ���, ���������� ��� ���������
		if (card.getTemplate().equals(TEMPLATE_ORD)||card.getTemplate().equals(TEMPLATE_OUTBOUND)||card.getTemplate().equals(TEMPLATE_INSIDE)){
			// � ������� ���������� ��� ������������
			if(card.getState().equals(CARDSTATE_MATCHING)||card.getState().equals(CARDSTATE_SIGNING))
				return true;
		}
		return false;
			
	}
	
	/**
	 * �������� ��������� ��� ���, ���� ��� ����������
	 * @param request {@link ActionRequest}
	 * @param wfm ������� �������
	 * @return true, ���� ��� ����� ��������� � ��������� ������� ��������, false � ��������� ������
	 */
	private boolean prepareForDS(ActionRequest request, ActionResponse response, WorkflowMove wfm, Card card){
		String dsSupport = request.getParameter(FIELD_SUPPORTS_DS);
		if(dsSupport == null || !Boolean.parseBoolean(dsSupport)) return false;
		DataServiceBean serviceBean = PortletUtil.createService(request);
		try{
			if(!DigitalSignatureUtil.isDsSupport(serviceBean)){
				request.getPortletSession().setAttribute(IS_DS_SUPPORT, "false");
				return false;
			} else {
				request.getPortletSession().setAttribute(IS_DS_SUPPORT, "true");
			}
			HashMap<?,?> configuration = DigitalSignatureConfiguration.getConfiguration();
			DigitalSignatureConfiguration.Template templateConfig = (DigitalSignatureConfiguration.Template) configuration
					.get(card.getTemplate().getId());
	
			int applySignature = wfm.getApplyDigitalSignatureOnMove();
			if (applySignature > 0 && null != templateConfig) {
				request.getPortletSession().setAttribute(APPLY_DS, applySignature);	
				if(applySignature>2){
					applySignature = applySignature - 2;
				}
				ArrayList<String> params = DigitalSignatureUtil.prepareSignatureParams(serviceBean, card, applySignature == 2, response.encodeURL(request.getContextPath()  + "/MaterialDownloadServlet?" + CardPortlet.CARD_ID_FIELD + "="));
				request.getPortletSession().setAttribute(DS_PARAMS, params);
				return true;
			}
		} catch(Exception e){e.printStackTrace();}
		return false;
	}
	
	private void signCardHandler(ActionRequest request, DataServiceBean serviceBean) {
		//QuickResolutionPortletSessionBean sessionBean = getSessionBean(request);	
		try {
			//Card card = (Card) serviceBean.getById(new ObjectId(Card.class, sessionBean.getParentId()));
			String signatureParam = request.getParameter(PARAM_SIGNATURE);
			if(StringUtils.hasLength(signatureParam)) 
				DigitalSignatureUtil.storeDigitalSignature(signatureParam, null, serviceBean, true);		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private ObjectId getVisaOrSignWfm(String mode, String decision){
		if(QuickResolutionPortlet.TARGET_INIT_SIGNING.equals(mode)){
			if(PARAM_DECISION_DECLINE.equals(decision)){
				return WFM_SIGN_DECLINE;
			} else {
				return WFM_SIGN_REJECT_BOSS;
			}
		} else if(QuickResolutionPortlet.TARGET_INIT_VISA.equals(mode)){
			if(PARAM_DECISION_COMMENT.equals(decision)){					
				return WFM_VISA_COMMENT;					
			}else if(PARAM_DECISION_DECLINE.equals(decision)){
				return WFM_VISA_DECLINE;
			} else {
				return WFM_VISA_DISAGREE;
			}
		}
		return null;
	}
	
	private boolean checkLockBaseDoc (
			ActionRequest request,
			QuickResolutionPortletSessionBean sessionBean,
			DataServiceBean serviceBean) throws DataException, ServiceException {
		final Long baseId = sessionBean.getBaseId();
		final ObjectId baseObjectId = new ObjectId(Card.class, baseId);
		CheckLock checkLock = new CheckLock(baseObjectId);
		try{
			serviceBean.doAction(checkLock);
		}catch (ObjectNotLockedException e) {				
			return true;
		}catch (ObjectLockedException e) {
			return false;
		}
		return false;
	}
	
	// ��� ���������� �������� ��������
	public enum TypeLink {
		
		CARDLINK("c"),
		
		BACKLINK("b");
		
		private final String type;
		
		TypeLink(String type) {
			this.type = type;
		}

	}

	private boolean checkStateBaseDoc(
			QuickResolutionPortletSessionBean sessionBean, 
			DataServiceBean serviceBean) throws DataException, ServiceException {
		final Long baseId = sessionBean.getBaseId();
		final ObjectId baseObjectId = new ObjectId(Card.class, baseId);
		Card base = (Card)serviceBean.getById(baseObjectId);
		sessionBean.setBaseDocState(base.getState());
		if (base.getState().equals(CARDSTATE_READY_ARCHIVE) || base.getState().equals(CARDSTATE_ARCHIVE)) {
			return false;
		}
		return true;
	}
	
    private void clearFolderQtyCache(String folderId, DataServiceBean service) {
		long[] permissionTypesArray = ContentUtils.getPermissionTypes(new Search());
        int personId = Integer.parseInt(service.getPerson().getId().getId().toString());
        CounterCache.instance().clearValue(folderId, personId, permissionTypesArray);
	}
}
