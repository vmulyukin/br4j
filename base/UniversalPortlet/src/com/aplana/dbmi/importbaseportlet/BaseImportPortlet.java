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
package com.aplana.dbmi.importbaseportlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.portlet.PortletFileUpload;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.aplana.dbmi.common.utils.portlet.PortletMessage;
import com.aplana.dbmi.common.utils.portlet.PortletMessage.PortletMessageType;
import com.aplana.dbmi.rolelistportlet.RoleListPortlet;
import com.aplana.dbmi.service.DataException;
import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletConfig;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletResponse;
import javax.portlet.PortletSession;
import javax.portlet.PortletURL;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import org.jboss.portal.portlet.aspects.portlet.ContextDispatcherInterceptor;
import org.jboss.portal.portlet.invocation.PortletInvocation;
import com.aplana.dbmi.Portal;
import com.aplana.dbmi.PortletService;
import com.aplana.dbmi.action.CreateCard;
import com.aplana.dbmi.action.GetDelegateListByLogin;
import com.aplana.dbmi.action.ImportObjects;
import com.aplana.dbmi.action.ImportRoles;
import com.aplana.dbmi.action.ImportResult;
import com.aplana.dbmi.action.CardsImportResult;
import com.aplana.dbmi.action.ParseImportFile;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.action.UploadFile;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ContextProvider;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.action.ImportAttribute;
import com.aplana.dbmi.model.MaterialAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.TextAttribute;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.dbmi.service.SystemUser;
import com.aplana.dbmi.universalportlet.UniversalPortlet;
import com.aplana.dbmi.util.CardImportSettings;
import com.aplana.dbmi.util.EncodingUtils;
import com.aplana.dbmi.importbaseportlet.BaseImportPortletSessionBean;

public class BaseImportPortlet extends GenericPortlet {
	private static Log logger = LogFactory.getLog(BaseImportPortlet.class);
	
	public static final String CHECK_DOUBLETS_PARAM = "CHECK_DOUBLETS";
	public static final String UPDATE_DOUBLETS_PARAM = "UPDATE_DOUBLETS";
	public static final String UPDATE_DOUBLETS_SUPPORT_PARAM = "UPDATE_DOUBLETS_SUPPORT";
	public static final String CHECK_DOUBLETS_SUPPORT_PARAM = "CHECK_DOUBLETS_SUPPORT";
	public static final String CUSTOM_IMPORT_TITLE_PARAM = "CUSTOM_IMPORT_TITLE";
	public static final String BACK_URL_FIELD = "MI_BACK_URL_FIELD";
	public static final String SHOW_WARNING_MESSAGE = "SHOW_WARNING_MESSAGE";
	public static final String OVER_LIMIT_STRING = "...";	// �������, �� ������� ���������� ������� �� �����������
	public static final String INIT = "INIT";
	
	// For cards
	public static final String EDIT_CARD_ID_FIELD = "MI_EDIT_CARD";
	public static final String OPEN_FOR_EDIT_FIELD = "MI_OPEN_FOR_EDIT";
	

	private static final ObjectId TEMPLATE_FILE =
		ObjectId.predefined(Template.class, "jbr.file");
	private static final ObjectId TEMPLATE_IMPORT_RESULT =
		ObjectId.predefined(Template.class, "jbr.importResult");
	private static final ObjectId ATTR_IMPORT_DATE =
		ObjectId.predefined(DateAttribute.class, "jbr.import.date");
	private static final ObjectId ATTR_IMPORT_DICT =
		ObjectId.predefined(StringAttribute.class, "jbr.import.dictionary");
	private static final ObjectId ATTR_IMPORT_RESULT =
		ObjectId.predefined(TextAttribute.class, "jbr.import.resultText");
	private static final ObjectId ATTR_FILES =
		ObjectId.predefined(CardLinkAttribute.class, "jbr.files");
	private static final ObjectId ATTR_MATERIAL_NAME =
		ObjectId.predefined(StringAttribute.class, "jbr.materialName");
	private static final ObjectId ATTR_IMPORT_SUCCESS =
		ObjectId.predefined(ListAttribute.class, "jbr.import.success");
	private static final ObjectId YES_ID = ObjectId.predefined(ReferenceValue.class, "jbr.incoming.control.yes");
	private static final ObjectId NO_ID = ObjectId.predefined(ReferenceValue.class, "jbr.incoming.control.no");
	

	public static final String FILENAME_PARAM = "file";
	public static final String MATERIALNAME_PARAM = "materialName";
	public static final String DOUBLETS_CHECK_HIDDEN_PARAM = "isDoubletsChecked_value";
	public static final String UPDATE_DOUBLETS_HIDDEN_PARAM = "isUpdateDoublets_value";
	public static final int SELECTED_VAL = 1;
	public static final int UNSELECTED_VAL = 0;
	private static final String IMPORT_ERROR_MSG = ContextProvider.getContext().getLocaleMessage("card.import.portlet.error");
	
	private String showAllImportResultMsg=null;
	private PortletService portletService;
	private static int threadCount = CardImportSettings.getThreadCount();
	private static int countThreshold = CardImportSettings.getCardsCountThreshold();
	
	private String keySessionBean;
	private String importObjectName;
	private String resourceBundle;
	private String jspView;
	private String jspFolder;
	private ParseImportFile.TypeImportObject typeImportObject;
	private ImportObjects importObject;
	private String msgParamName;
	private String msgResultImport;
	
	public void init(PortletConfig portletConfig) throws PortletException {
        super.init(portletConfig);
    }
	
	public void setKeySessionBean(String keySessionBean) {
		this.keySessionBean = keySessionBean;
	}

	public void setImportObjectName(String importObjectName) {
		this.importObjectName = importObjectName;
	}

	public void setResourceBundle(String resourceBundle) {
		this.resourceBundle = resourceBundle;
	}

	public void setJspView(String jspView) {
		this.jspView = jspView;
	}

	public void setJspFolder(String jspFolder) {
		this.jspFolder = jspFolder;
	}

	public void setTypeImportObject(
			ParseImportFile.TypeImportObject typeImportObject) {
		this.typeImportObject = typeImportObject;
	}

	public void setImportObject(ImportObjects importObject) {
		this.importObject = importObject;
	}

	public void setMsgParamName(String msgParamName) {
		this.msgParamName = msgParamName;
	}

	public void setMsgResultImport(String msgResultImport) {
		this.msgResultImport = msgResultImport;
	}

	public String getKeySessionBean() {
		return keySessionBean;
	}

	public String getImportObjectName() {
		return importObjectName;
	}

	public String getResourceBundle() {
		return resourceBundle;
	}

	public String getJspView() {
		return jspView;
	}

	public String getJspFolder() {
		return jspFolder;
	}

	public ParseImportFile.TypeImportObject getTypeImportObject() {
		return typeImportObject;
	}

	public ImportObjects getImportObject() {
		return importObject;
	}

	public String getMsgParamName() {
		return msgParamName;
	}

	public String getMsgResultImport() {
		return msgResultImport;
	}

	/**
	 * Get SessionBean from portlet session.
	 *
	 * @param request PortletRequest
	 * @return ImportPortletSessionBean
	 */
	 private synchronized BaseImportPortletSessionBean getSessionBean(PortletRequest request) {
		PortletSession session = request.getPortletSession();
		if( session == null )
			return null;
		BaseImportPortletSessionBean sessionBaseBean = (BaseImportPortletSessionBean)session.getAttribute(getKeySessionBean());
		if( sessionBaseBean == null ) {
			sessionBaseBean = new BaseImportPortletSessionBean();
			session.setAttribute(getKeySessionBean(), sessionBaseBean);
		}
		return sessionBaseBean;
	}
	
	private void saveSessionBeanForServlet(RenderRequest request, BaseImportPortletSessionBean sessionBean) {
		PortletSession session = request.getPortletSession();
		session.setAttribute(getKeySessionBean(), sessionBean);
	}

	private void initBean(PortletRequest request, PortletResponse response) {
		final BaseImportPortletSessionBean sessionBean = getSessionBean(request);
		portletService = Portal.getFactory().getPortletService();
		try {
			sessionBean.setBackUrl(request.getParameter(BACK_URL_FIELD));
			sessionBean.setCheckForExistsDoublets(Boolean.parseBoolean(request.getParameter(CHECK_DOUBLETS_PARAM)));
			sessionBean.setUpdateExistsDoublets(Boolean.parseBoolean(request.getParameter(UPDATE_DOUBLETS_PARAM)));
			if (request.getParameter(UPDATE_DOUBLETS_SUPPORT_PARAM)!=null){
				sessionBean.setSupportUpdateExistsDoublets(Boolean.parseBoolean(request.getParameter(UPDATE_DOUBLETS_SUPPORT_PARAM)));
			}
			if (request.getParameter(CHECK_DOUBLETS_SUPPORT_PARAM)!=null){
				sessionBean.setSupportCheckForExistsDoublets(Boolean.parseBoolean(request.getParameter(CHECK_DOUBLETS_SUPPORT_PARAM)));
			}
			String objectName = getImportObjectName();
			final String customImportObjectName = request.getParameter(CUSTOM_IMPORT_TITLE_PARAM); 
			if (customImportObjectName!=null&&!customImportObjectName.isEmpty()){
				objectName = ContextProvider.getContext().getLocaleMessage(request.getParameter(CUSTOM_IMPORT_TITLE_PARAM));
			}
			sessionBean.setObjectName(objectName);
			showAllImportResultMsg = sessionBean.getResourceBundle().getString(getMsgResultImport());
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}
	
	public void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
		// Set the MIME type for the render response
		response.setContentType(request.getResponseContentType());
		// Set user locale
		ContextProvider.getContext().setLocale(request.getLocale());
		String init = request.getParameter(BaseImportPortlet.INIT);
		boolean isInit = Boolean.parseBoolean(init);
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
                    List<String> list = (List<String>) service.doAction(action);
                    if (list.contains(userName)) {
                        session.setAttribute(DataServiceBean.USER_NAME, userName, PortletSession.APPLICATION_SCOPE);
                    } else if (request.getUserPrincipal().getName().equals(userName)) {
                        session.removeAttribute(DataServiceBean.USER_NAME,  PortletSession.APPLICATION_SCOPE);
                    }
                } catch (Exception e) {
        			if (logger.isErrorEnabled()){
                        logger.error("Error while process GetDelegateListByLogin\n" + e);
        			}
                }
            }
        }
        if (req.getParameter("logged") != null) {
            session.removeAttribute(DataServiceBean.USER_NAME,  PortletSession.APPLICATION_SCOPE);
        }
		// Check if portlet session exists
		final BaseImportPortletSessionBean sessionBean = getSessionBean(request);
		if( sessionBean==null ) {
			response.getWriter().println("<b>NO PORTLET SESSION YET</b>");
			return;
		}
		sessionBean.setResourceBundle(ResourceBundle.getBundle(getResourceBundle(), request.getLocale()));
		if (isInit)
			initBean(request, response); 
		saveSessionBeanForServlet(request, sessionBean);
		
		// Invoke the JSP to render
		PortletRequestDispatcher rd = getPortletContext().getRequestDispatcher(getJspFilePath(request, getJspView()));
		rd.include(request,response);
	}
	
	public void processAction(ActionRequest request, ActionResponse response) throws PortletException, java.io.IOException {
		BaseImportPortletSessionBean sessionBean = getSessionBean(request);
		Map<String,Object> importParams = multipartContentHandle(request, response);
		InputStream originalFileStream = null;
		FileInputStream inputStream = null;
		File tempInputCopy = null;
		try{
			originalFileStream = (InputStream)importParams.get(FILENAME_PARAM);
			if (originalFileStream == null){
				throw new DataException("card.import.file.not.selected", new Object[]{});
			}
				
			// ������� ��������� ����� �������� �����, ����� � ��� ����� ���� ��������� ��������.
			tempInputCopy = createTempCopyFile(originalFileStream);
			if (!EncodingUtils.verifyEncodingUTF8WithoutBOM(tempInputCopy))
				throw new DataException("card.import.file.not.verify.encoding", new Object[]{});
						
			DataServiceBean serviceBean = sessionBean.getServiceBean(request);
			ParseImportFile action = new ParseImportFile();
			action.setTypeImportObject(getTypeImportObject());
			
			String filePath = (String)importParams.get(MATERIALNAME_PARAM);
			inputStream = new FileInputStream(tempInputCopy);
			action.setFile(inputStream);
			// ������
			List<List<ImportAttribute>> importAttributes = (List<List<ImportAttribute>>)serviceBean.doAction(action);

			// ����� ��������� �������� �������� �����, ���������� � �������
			int doubletsCheckInt =  ((Long)importParams.get(DOUBLETS_CHECK_HIDDEN_PARAM)).intValue();
			int updateDoubletsInt = ((Long)importParams.get(UPDATE_DOUBLETS_HIDDEN_PARAM)).intValue();
			boolean isDoubletsCheck = (doubletsCheckInt==SELECTED_VAL);   
			boolean isUpdateDoublets = (updateDoubletsInt==SELECTED_VAL);
			List<ImportAttribute> mandatoryAttributes = getMandatoryForDoubletCheckAttributes((List<ImportAttribute>)importAttributes.get(0));
			if ((isDoubletsCheck||isUpdateDoublets)&&(mandatoryAttributes==null||mandatoryAttributes.isEmpty())){
				throw new DataException("card.import.check.doublet.attributes.empty");
			}

			int allCount = importAttributes.size();
			final DataServiceBean actualServiceBean = sessionBean.getActualServiceBean(request);

			CardsImportResult result = processImport(importAttributes, actualServiceBean, isDoubletsCheck, isUpdateDoublets);

			StringBuffer str = new StringBuffer(MessageFormat.format(ContextProvider.getContext().getLocaleMessage((result.getErrorCount() > 0 ? "card.import.error":"card.import.success")), new Object[]{sessionBean.getObjectName()})); 
			
			str.append("\n"+MessageFormat.format(ContextProvider.getContext().getLocaleMessage("card.import.analyze.import.object"), new Object[]{allCount}));

			if (result.getErrorCount() > 0){
				str.append("\n"+MessageFormat.format(result.getErrorMsg(), new Object[]{result.getErrorCount()}));
			}

			if (result.getDubletCount() > 0){
				final String foundDoubletsMsg = ContextProvider.getContext().getLocaleMessage("card.import.found.doublets");
				final String updatedDoubletsMsg = (isUpdateDoublets?ContextProvider.getContext().getLocaleMessage("card.import.doublets.updated"):"");
				str.append("\n"+MessageFormat.format(foundDoubletsMsg, new Object[]{updatedDoubletsMsg, result.getDubletCount(), sessionBean.getObjectName()}));
				str.append(result.getDoubletMsg());
			}

			if (result.getSuccessCount() > 0){
				// ������� ���������� ������� ����������� ��������
				str.append("\n"+MessageFormat.format(ContextProvider.getContext().getLocaleMessage("card.import.adding.import.object"), new Object[]{result.getSuccessCount(), sessionBean.getObjectName()}));
				// ����� ������� ��������������� �� �������� SUCCESS_CARD_SHOW_ATTRIBUTE ������� ����������� ������� 
				str.append(generateSuccessSortMessage(result.getSuccessMsg()));
			}
			String importMessage = str.toString();
			final Date importDate = new Date();	// ���� ���������� �������
			// ����� ��� ��� ������ ��������� ������������, ������� �������� ���������� �������
			ObjectId importCardId = createImportResult(actualServiceBean, importDate, importMessage, tempInputCopy, filePath, (result.getSuccessCount() > 0));
			if (importMessage!=null){
				sessionBean.setMessage(updateHtmlImportMessage(request, response, importMessage, importCardId, sessionBean.getBackUrl(), true));
			}
			leaveObjectImportPortlet(request, response, sessionBean);
		} catch (Exception e){
			logger.error(e.getMessage());
			sessionBean.setMessage(updateHtmlImportMessage(request, response, e.getMessage(), null, sessionBean.getBackUrl(), false));
			response.setRenderParameter(SHOW_WARNING_MESSAGE,Boolean.TRUE.toString());
			return;
		} finally {
			IOUtils.closeQuietly(originalFileStream);
			IOUtils.closeQuietly(inputStream);

			if (tempInputCopy != null) {
				if (!FileUtils.deleteQuietly(tempInputCopy)) {
					logger.warn("Cannot remove temp file: " + tempInputCopy.getCanonicalFile());
				}
			}
		}
	}

	private File createTempCopyFile (InputStream input) throws IOException{
		String prefix = getTypeImportObject().name();
		String suffix = ".tmp.csv";
		FileOutputStream outputStream = null;

		try {
			File tempFile = File.createTempFile(prefix, suffix);
			outputStream = new FileOutputStream(tempFile);
			IOUtils.copy(input, outputStream);
			if (logger.isDebugEnabled()) {
				logger.debug("Temp file was created successfully: " + tempFile.getCanonicalFile());
			}
			return tempFile;
		} finally {
			IOUtils.closeQuietly(outputStream);	
		}

	}

	private Map<String,Object> multipartContentHandle(ActionRequest request, ActionResponse response) {
		// inbound file with list objects
		
		FileItemFactory factory = new DiskFileItemFactory();
	    PortletFileUpload upload = new PortletFileUpload(factory);

	    List<FileItem> items = new ArrayList<FileItem>();
    	Map<String, Object> inputParams = new HashMap<String, Object>();
	    try {
	    	items = upload.parseRequest(request);
	    	List<Map<String, FileItem>> data = new ArrayList<Map<String, FileItem>>();
	    	for (FileItem item : items){
	    		if(!item.isFormField()){
	    			if(item.getName().length() == 0) continue;
	    			Map<String, FileItem> map = new HashMap<String, FileItem>();
	    			map.put(FILENAME_PARAM, item);
	    			data.add(map);
	    		} else {
	    			if(!data.isEmpty()) {
	    				Map<String, FileItem> map = data.get(data.size() - 1);
	    				if(!map.containsKey(item.getFieldName())) map.put(item.getFieldName(), item);
	    			}
	    		}
	    	}
	    	for (Map<String, FileItem> map : data) {
	    		FileItem item = map.get(FILENAME_PARAM);
	    		inputParams.put(FILENAME_PARAM, item.getInputStream());
	    		inputParams.put(MATERIALNAME_PARAM, item.getName());

	    		FileItem doubletCheckedItem = map.get(DOUBLETS_CHECK_HIDDEN_PARAM);
	    		inputParams.put(DOUBLETS_CHECK_HIDDEN_PARAM, Long.parseLong(doubletCheckedItem.getString()));
	    		
	    		FileItem updateDoubletItem = map.get(UPDATE_DOUBLETS_HIDDEN_PARAM);
	    		inputParams.put(UPDATE_DOUBLETS_HIDDEN_PARAM, Long.parseLong(updateDoubletItem.getString()));
	    		break;
	    	}
	    	return inputParams;
	    } catch (Exception e) {
			e.printStackTrace();
			return inputParams;
	    }   
	}

	private void leaveObjectImportPortlet(ActionRequest request, ActionResponse response,BaseImportPortletSessionBean sessionBean) throws IOException{
		String backURL = sessionBean.getBackUrl();
		// TODO: ������� �� ������� � �� application scope, �� ��� �����
		// ����� ����� namespace
		request.getPortletSession().removeAttribute(getKeySessionBean());
		if (sessionBean.getMessage() != null){
			PortletSession session = request.getPortletSession();
			session.setAttribute(getMsgParamName(), new PortletMessage(sessionBean.getMessage(), PortletMessageType.INFO), PortletSession.APPLICATION_SCOPE);
		}
		sessionBean.reset();
		if (backURL == null) {
			redirectToPortalDefaultPage(request, response);
		} else {
			response.sendRedirect(backURL);
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
	
	/**
	 * �������� �������� ������� ���������� ������� ��� ����������� ���������� �� �������
	 * @actualServiceBean - ��������� � �������� ������������� ��� ������ ������ �������� � ���������� ��������
	 * @importDate - �������� ���� ��������� �������
	 * @importMessage - ��������� � ����������� �������
	 * @file - ������� ���� �������
	 * @filePath - ������ ���� � ���������� ������������ ����� (���� ��� ���)
	 */
	private ObjectId createImportResult(DataServiceBean actualServiceBean, Date importDate, String importMessage, File file, String filePath, boolean isSuccess) throws DataException{
		if (actualServiceBean== null)
			return null;
		FileInputStream fis = null;
		try{
			// ������� �������� ����� ��� ����� �������
			Card fileCard = createFileCard(actualServiceBean);
			fis = new FileInputStream(file);
			addFileToFileCard(fileCard, fis, filePath, actualServiceBean);
			actualServiceBean.doAction(new UnlockObject(fileCard));

			// ������� �������� ����� ��� ��������� � ���������� �������
			Card fileResultCard = createFileCard(actualServiceBean);
			importMessage = convertHtmlToTxt(importMessage);	// ��������� �������������� �������� ��������� � ������� ��������� ���
			InputStream resultFile = new ByteArrayInputStream(importMessage.getBytes("UTF-8"));
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
			addFileToFileCard(fileResultCard, resultFile, dateFormat.format(importDate)+".txt", actualServiceBean);
			actualServiceBean.doAction(new UnlockObject(fileResultCard));
			
			// ������� �������� ����������� �������
			CreateCard createAction = new CreateCard();
			createAction.setTemplate(TEMPLATE_IMPORT_RESULT);
			Card importCard = actualServiceBean.doAction(createAction); 
			
			// ��������� ����������� ���������� � ���������
			DateAttribute loadDate = importCard.getAttributeById(ATTR_IMPORT_DATE);
			if (loadDate!=null){
				loadDate.setValue(importDate);
			}
			
			ListAttribute primacyAttr = (ListAttribute) importCard.getAttributeById(ATTR_IMPORT_SUCCESS);
			if (primacyAttr != null)
			{
				ReferenceValue refValue = new ReferenceValue();
				if (isSuccess){
					refValue.setId(YES_ID);
				} else {
					refValue.setId(NO_ID);
				}
				primacyAttr.setValue(refValue);
			}
			
			TextAttribute importResult = importCard.getAttributeById(ATTR_IMPORT_RESULT);
			if (importResult!=null){
				// ���� ����� ��������� > 4000 ��������, �� �������, ����� �� ������ �������� � �� (��������� ����� ��������� �� ��������)
				if (importMessage.length()>4000){
					importMessage = importMessage.substring(0, 3999);
				}
				importResult.setValue(importMessage);
			}
			
			CardLinkAttribute files = importCard.getAttributeById(ATTR_FILES);
			if (files!=null){
				files.addLinkedId(fileCard.getId());
				files.addLinkedId(fileResultCard.getId());
			}

			ObjectId cardId = actualServiceBean.saveObject(importCard);
			importCard.setId(cardId);
			actualServiceBean.doAction(new UnlockObject(importCard));
			return cardId;
		} catch (Exception e){
			logger.error("Cannot create import result: " + e.getMessage(), e);
			throw new DataException("card.import.result.create.error", new Object[]{e.getMessage()});
		} finally {
			IOUtils.closeQuietly(fis);
		}
	}
	
	private Card createFileCard(DataServiceBean serviceBean) throws DataException {
		CreateCard createCardAction = new CreateCard();
		createCardAction.setTemplate(TEMPLATE_FILE);
		Card fileCard = null;
			
		try {
			fileCard = (Card)serviceBean.doAction(createCardAction);
			StringAttribute name = (StringAttribute)fileCard.getAttributeById(Attribute.ID_NAME);
			name.setValue("file");
			ObjectId fileCardId = serviceBean.saveObject(fileCard);
			fileCard.setId((Long)fileCardId.getId());	
		}catch(Exception e){
			if (logger.isErrorEnabled()){
				logger.error("error creating new file card", e);
			}
			throw new DataException(e);
	    }
		return fileCard;
	}
	
	private void addFileToFileCard(Card fileCard, InputStream file, String filePath, DataServiceBean serviceBean) throws IOException, DataException, ServiceException {
		UploadFile uploadAction = new UploadFile();
        uploadAction.setCardId(fileCard.getId());
        String fileName = filePath.substring((filePath.lastIndexOf("\\") + 1));
        uploadAction.setFileName(fileName);
        uploadAction.setData(file);

        try {
        	serviceBean.doAction(uploadAction);        
	        MaterialAttribute attr = (MaterialAttribute)fileCard.getAttributeById(Attribute.ID_MATERIAL);
	        attr.setMaterialName(fileName);
	        attr.setMaterialType(MaterialAttribute.MATERIAL_FILE);
	        
	        StringAttribute name = (StringAttribute)fileCard.getAttributeById(Attribute.ID_NAME);
			name.setValue(fileName);
			
	        StringAttribute materialName = (StringAttribute)fileCard.getAttributeById(ATTR_MATERIAL_NAME);
	        materialName.setValue(fileName);
			
			serviceBean.saveObject(fileCard);
			
		} catch(Exception e) {
			if (logger.isErrorEnabled()){
				logger.error("Can not add file to card " + fileCard.getId(), e);
			}
			throw new DataException(e);
		} finally {
			IOUtils.closeQuietly(file);
		}
	}

	/**
	 * ��������� ��������� � ����������� �������:
	 * ������ ������� ������� �� CardImportSettings.CHAR_COUNT_IN_LINE_LIMIT
	 * ���������� ����� ������� �� CardImportSettings.LINE_COUNT_LIMIT 
	 * ���� ����� ����������� �������� (� ������ ��� ����� �����), ���������� OVER_LIMIT_STRING 
	 * ���� �� ����� �������, ��� ������� ������ �� ����, �� �� ������� (��������� html-�������� ��� ����������� ����)
	 */
	private String updateHtmlImportMessage(ActionRequest request, ActionResponse response, String importMessage, ObjectId cardId, String backUrl, boolean isStrict ){
		//���� �� ���� ������ null, �������� �� ������ ������
		if (importMessage != null) {
			importMessage = convertHtmlToTxt(importMessage).trim();	// �������� html-���� ���� ������������ � ���� ������, � �� � ��������� ��������� ����������
		} else {
			importMessage = "";
		}
		StringBuilder sb = new StringBuilder();
		// ��� ������ ������ ������� ������ � ��� ������������� ������ ���������
		String patternString = "\n";

		Pattern pattern = Pattern.compile(patternString);
		String[] lines = pattern.split(importMessage);
		for(int i=0; i<lines.length; i++){
			if (isStrict && i >= CardImportSettings.LINE_COUNT_LIMIT){
				sb.append("<br>"+OVER_LIMIT_STRING);
				break;
			}
			final String s = lines[i];
			if (isStrict&&s.length() > CardImportSettings.CHAR_COUNT_IN_LINE_LIMIT){
				sb.append(s.substring(0, CardImportSettings.CHAR_COUNT_IN_LINE_LIMIT));
				sb.append(" <b title='"+s+"'>"+OVER_LIMIT_STRING+"</b>");
			} else
				sb.append(s);	
			sb.append("<br>");	// ��������� ������� �� ����� ������	
		}
		// ����� ��������� �� ����� ������ � ���������� ������ �� ������� � �������� ���������� �������
		if (cardId!=null&&cardId.getId()!=null){
			sb.append("<br>");
			
			String cardPageId = portletService.getPageProperty("cardPage", request, response);
	        if (cardPageId == null) {
	            cardPageId = "dbmi.Card";
	        }
			final Map params = new HashMap();
			params.put( BaseImportPortlet.EDIT_CARD_ID_FIELD, cardId.getId().toString());
			params.put( BaseImportPortlet.BACK_URL_FIELD, backUrl);
			params.put( BaseImportPortlet.OPEN_FOR_EDIT_FIELD, "false");
			String editLink = portletService.generateLink(cardPageId, "dbmi.Card.w.Card", params, request, response);
			
			sb = sb.append("<a href='").append(editLink).append("'>").append(showAllImportResultMsg).append("</a>");
		}
		return sb.toString();
	}
	
	/**
	 * �� ������� ������ ���� ��� ����������� ��������� ���������, �������� ������,
	 * ��� � html-����, ����������� �� �� ������� � ����������� ��� ����������� �����������
	 * ��������� �� ������� � web-�����, � ��������� ���� ��� �� �����, ������� ������ �� 
	 * @param inputMessage - ������� ��������� � ����������� �������
	 * @return �� �� ������, �� ��� html-�����
	 */
	private String convertHtmlToTxt(String inputMessage){
		String patternString = "(<.*?>)";

		Pattern pattern = Pattern.compile(patternString);
		Matcher matcher = pattern.matcher(inputMessage);

		String replaceAll = matcher.replaceAll("");		
		return replaceAll;
	}

	/**
	 * ������������ �� ��������� ������ ������������� ��������� ������ ������������ ��� ������ ���������� ���������
	 * @param ImportAttributes - �������� ������ ������������� ���������
	 * @return ������ ������ ������������ ��� ������ ���������
	 */
	private List<ImportAttribute> getMandatoryForDoubletCheckAttributes(List<ImportAttribute> ImportAttributes){
		List<ImportAttribute> resultList = new ArrayList<ImportAttribute>();
		for(ImportAttribute attr: ImportAttributes){
			if (attr.isDoubletCheck()){
				resultList.add(attr);
			}
		}
		return resultList;
	}
	
	private String generateSuccessSortMessage(String successMessage){
		String[] successArray = successMessage.split("\n");
		StringBuffer result = new StringBuffer();  
		// ������������� �� �����������
		if (successArray!=null&&successArray.length>0){
			Arrays.sort(successArray);
			for(String line: successArray){
				if (line!=null&&!line.isEmpty())
				result.append("\n\t"+line);
			}	
		}
		return result.toString();
	}
	
	/**
     * Returns JSP file path.
     *
     * @param request
     *                Render request
     * @param jspFile
     *                JSP file name
     * @return JSP file path
     */
    private String getJspFilePath(RenderRequest request, String jspFile) {
        String markup = request.getProperty("wps.markup");
        if (markup == null)
            markup = getMarkup(request.getResponseContentType());
        return getJspFolder() + markup + "/" + jspFile + "." + getJspExtension(markup);
    }

    /**
     * Convert MIME type to markup name.
     *
     * @param contentType
     *            MIME type
     * @return Markup name
     */
    private static String getMarkup(String contentType) {
        if ("text/vnd.wap.wml".equals(contentType))
            return "wml";
        else
            return "html";
    }

    /**
     * Returns the file extension for the JSP file
     *
     * @param markupName
     *            Markup name
     * @return JSP extension
     */
    private static String getJspExtension(String markupName) {
        return "jsp";
    }

	private CardsImportResult processImport(List<List<ImportAttribute>> importAttributes, DataServiceBean serviceBean, boolean isDoubletsCheck, boolean isUpdateDoublets) throws DataException {
		CardsImportResult result = new CardsImportResult();

		int recordsCount = importAttributes.size();

		long startTimeParallel = System.currentTimeMillis();

		ExecutorService pool = null;
		List<ImportFutureTask> futures = new LinkedList<ImportFutureTask>();
		int currentThreadCount; //�������� ���������� �������, ������� ����� ������������
		int partSize; //���������� ������������� ����� � ����� ������
		try {

			if (recordsCount > countThreshold) {
				currentThreadCount = threadCount;
				partSize = (recordsCount / currentThreadCount) + 1;
			} else {
				currentThreadCount = 1;
				partSize = recordsCount;
			}


			if (logger.isDebugEnabled()) {
				logger.debug("Starting " + getTypeImportObject().name() + " import process with following params: [recordsCount: " + recordsCount + ", thread count: " + currentThreadCount + ", part size:" + partSize + "]");
			}

			pool = Executors.newFixedThreadPool(currentThreadCount);

			for (int i = 0; i < currentThreadCount; i++) {
				if (logger.isDebugEnabled()) {
					logger.debug("Trying to start " + i + "-th thread");
				}
				ImportFutureTask fTask = new ImportFutureTask(new ImportThread(importAttributes,
																						   serviceBean,
																						   partSize,
																						   i,
																						   recordsCount,
																						   isDoubletsCheck,
																						   isUpdateDoublets),
																						   partSize, i, recordsCount);
				pool.submit(fTask);
				futures.add(fTask);

			}

			while(futures.size() > 0) {
				Iterator<ImportFutureTask> iter = futures.iterator();

				while(iter.hasNext()) {
					ImportFutureTask fCurrent = iter.next();
					if (fCurrent.isDone()) {

						//�������� ��������� � ����� ������� � ������� ��� �� ������
						try {
							ImportThread importThread = fCurrent.get();
							if (importThread != null) {

								if (logger.isDebugEnabled()) {
									logger.debug("Thread # " + importThread.currentPartIndex + " is done.");
								}
								result.addDoubletMessages(importThread.getDoubletMsgList());
								result.addErrorMessages(importThread.getErrorMsgList());
								result.addSuccessMessages(importThread.getSuccessMsgList());
							}
						} catch (Exception e) {
							logger.error("Unable to retrieve result from thread# " + fCurrent.getPartIndex() + " due to following exception: " + e.getMessage(), e);
							//��� �������, ������� ����������� ���� �����, �������� �� ������������
							List<String> errorMessages = new LinkedList<String>();
							for (int i = fCurrent.getFromIndex(); i < fCurrent.getToIndex(); i++) {
								errorMessages.add(MessageFormat.format("\n\t" + IMPORT_ERROR_MSG, new Object[]{i+CardImportSettings.LINE_NUMBER_OFFSET, e.getMessage()}));
							}
							result.addErrorMessages(errorMessages);
						}
						iter.remove();
					}
				}
				Thread.sleep(CardImportSettings.THREAD_SLEEP_VALUE);
			}

		} catch (Exception ex) {
			if (pool != null) {
				pool.shutdown();
			}
			logger.error(getTypeImportObject().name() + " import process is terminated due to following exception: " + ex.getMessage(), ex);
			throw new DataException("card.import.portlet.critical.runtime", new Object[]{ex.getMessage()});
		} finally {
			if (logger.isDebugEnabled()) {
				logger.error(getTypeImportObject().name() + " import execution took " + String.valueOf(System.currentTimeMillis() - startTimeParallel) + " ms with count = " + recordsCount);
			}
		}

		return result;
	}

	/**
	 *���������� �������� ����������� �����, ��� ��� ������� ����������� �� ��������� �� ����������� Query ��������� �����������
	 *����� ImportThread �������� ����������� ImportObjects ��� ���������� ��������� ������� ������:
	 *@param templateId - id ������� ������������ ��������
	 *@param importAttributes - ������ ���������� ������������� ��������
	 *@param serviceBean - ������ ������ DataServiceBean, �� �������� �� ����� ��������� ImportObjects
	 *@param partSize - ���������� ���������
	 *@param currentPartIndex - ����� �����, ��� ������� ����� �������� ������ �����
	 *@param allCount - ����� ���������� ������������� ��������
	 *@param isDoubletsCheck - ���� �������� ����������
	 *@param isUpdateDoublets - ���� ���������� ����������
	 */
	class ImportThread implements Callable<ImportThread> {

		private DataServiceBean serviceBean;
		private List<List<ImportAttribute>> importAttributes;
		private int partSize;
		private int currentPartIndex;
		private int allCount;
		private boolean isDoubletsCheck;
		private boolean isUpdateDoublets;

		private List<String> doubletMsgList = new LinkedList<String>();
		private List<String> successMsgList = new LinkedList<String>();
		private List<String> errorMsgList   = new LinkedList<String>();

		ImportThread(List<List<ImportAttribute>> importAttributes, DataServiceBean serviceBean, int partSize, int currentPartIndex, int allCount, boolean isDoubletsCheck, boolean isUpdateDoublets) {

			this.importAttributes = importAttributes;
			this.serviceBean = serviceBean;
			this.partSize = partSize;
			this.currentPartIndex = currentPartIndex;
			this.allCount = allCount;
			this.isDoubletsCheck = isDoubletsCheck;
			this.isUpdateDoublets = isUpdateDoublets;
		}

		public ImportThread call() {
			for (int i = currentPartIndex*partSize; i < (currentPartIndex+1)*partSize && i < allCount; i++) {
				try{

					List<ImportAttribute> importObject = importAttributes.get(i);
					ImportObjects importAction = getImportObject();

					importAction.setCheckDoublets(isDoubletsCheck);
					importAction.setUpdateDoublets(isUpdateDoublets);

					importAction.setImportAttributes(importObject);
					importAction.setLineNumber(i+CardImportSettings.LINE_NUMBER_OFFSET);
					ImportResult importResult = (ImportResult)serviceBean.doAction(importAction);
					if (importResult.getResultType().equals(ImportResult.IMPORT_RESULT_TYPE.DOUBLET)){
						doubletMsgList.add(importResult.getResultMessage());
					}
					if (importResult.getResultType().equals(ImportResult.IMPORT_RESULT_TYPE.SUCCESS)){
						successMsgList.add(importResult.getResultMessage());
					}
				} catch (Exception ex){
					errorMsgList.add(MessageFormat.format("\n\t" + IMPORT_ERROR_MSG, new Object[]{i + CardImportSettings.LINE_NUMBER_OFFSET, ex.getMessage()}));
				} 
			}
			return this;
		}

		public List<String> getDoubletMsgList() {
			return doubletMsgList;
		}

		public List<String> getSuccessMsgList() {
			return successMsgList;
		}

		public List<String> getErrorMsgList() {
			return errorMsgList;
		}
	}

	class ImportFutureTask extends FutureTask<ImportThread> {

		private int fromIndex;
		private int toIndex;
		private int partIndex;

		public ImportFutureTask(Callable callable) {
			super(callable);
		}

		public ImportFutureTask(Callable callable, int partSize, int partIndex, int allCount) {
			super(callable);
			this.fromIndex = partIndex*partSize;
			this.toIndex = Math.min((partIndex+1)*partSize, allCount);
			this.partIndex = partIndex;
		}

		public int getFromIndex() {
			return fromIndex;
		}

		public int getToIndex() {
			return toIndex;
		}
		
		public int getPartIndex() {
			return partIndex;
		}
	}
}