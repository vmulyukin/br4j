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
import java.util.Collections;
import java.util.Collection;
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

import com.aplana.dbmi.service.DataException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletPreferences;
import javax.portlet.PortletRequest;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletResponse;
import javax.portlet.PortletSession;
import javax.portlet.PortletURL;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.rmi.PortableRemoteObject;
import javax.servlet.http.HttpSession;

import org.jboss.portal.portlet.aspects.portlet.ContextDispatcherInterceptor;
import org.jboss.portal.portlet.invocation.PortletInvocation;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.PortletService;
import com.aplana.dbmi.action.CreateCard;
import com.aplana.dbmi.action.GetActiveTab;
import com.aplana.dbmi.action.GetAdminEmail;
import com.aplana.dbmi.action.GetDelegateListByLogin;
import com.aplana.dbmi.action.GetEmptyTabs;
import com.aplana.dbmi.action.ImportCards;
import com.aplana.dbmi.action.ImportResult;
import com.aplana.dbmi.action.CardsImportResult;
import com.aplana.dbmi.action.ParseCardImportFile;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.action.UploadFile;
import com.aplana.dbmi.gui.BlockView;
import com.aplana.dbmi.gui.BlockViewsBuilder;
import com.aplana.dbmi.gui.TabView;
import com.aplana.dbmi.gui.TabsManager;
import com.aplana.dbmi.gui.TripleContainer;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.AttributeBlock;
import com.aplana.dbmi.model.AttributeViewParam;
import com.aplana.dbmi.model.BlockViewParam;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.ContextProvider;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.DatedTypedCardLinkAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.Tab;
import com.aplana.dbmi.model.TabBlockViewParam;
import com.aplana.dbmi.model.TabViewParam;
import com.aplana.dbmi.model.TypedCardLinkAttribute;
import com.aplana.dbmi.model.ViewMode;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.action.ImportAttribute;
import com.aplana.dbmi.actionhandler.descriptor.ActionsDescriptor;
import com.aplana.dbmi.actionhandler.descriptor.ActionsDescriptorReader;
import com.aplana.dbmi.model.MaterialAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.TextAttribute;
import com.aplana.dbmi.service.AsyncDataServiceBean;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.PortletUtil;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.dbmi.service.SystemUser;
import com.aplana.dbmi.service.AsyncDataServiceBean.ExecuteOption;
import com.aplana.dbmi.showlist.MIShowListPortlet;
import com.aplana.dbmi.util.CardImportSettings;
import com.aplana.dbmi.util.EncodingUtils;
import com.aplana.dbmi.card.CardPortlet;
import com.aplana.dbmi.card.CardPortletCardInfo;
import com.aplana.dbmi.card.CardPortletSessionBean;
import com.aplana.dbmi.card.CardPortletSessionBean.PortletMessage;
import com.aplana.dbmi.card.CardPortletSessionBean.PortletMessage.PortletMessageType;
import com.aplana.dbmi.card.actionhandler.CardPortletActionsManager;
import com.aplana.dbmi.card.actionhandler.multicard.SpecificCustomStoreHandler;
import com.aplana.dbmi.card.actionhandler.multicard.SpecificCustomStoreHandlerFactory;
import com.aplana.dbmi.card.cardlinkpicker.descriptor.CardLinkPickerDescriptor;
import com.aplana.dbmi.card.cardlinkpicker.descriptor.CardLinkPickerVariantDescriptor;

public class CardImportPortlet extends GenericPortlet {
	protected static Log logger = LogFactory.getLog(CardImportPortlet.class);
	public static final String VIEW = "/WEB-INF/jsp/html/ImportCards.jsp";
	public static final String SESSION_BEAN = "cardImportPortletSessionBean";
	public static final String CARD_TEMPLATE_PARAM = "CARD_TEMPLATE";
	public static final String CHECK_DOUBLETS_PARAM = "CHECK_DOUBLETS";
	public static final String UPDATE_DOUBLETS_PARAM = "UPDATE_DOUBLETS";
	public static final String UPDATE_DOUBLETS_SUPPORT_PARAM = "UPDATE_DOUBLETS_SUPPORT";
	public static final String CHECK_DOUBLETS_SUPPORT_PARAM = "CHECK_DOUBLETS_SUPPORT";
	public static final String CUSTOM_IMPORT_TITLE_PARAM = "CUSTOM_IMPORT_TITLE";
	public static final String BACK_URL_FIELD = "BACK_URL_FIELD";
	public static final String SHOW_WARNING_MESSAGE = "SHOW_WARNING_MESSAGE";
	public static final String OVER_LIMIT_STRING = "...";	// символы, на которые заменяются символы из сверхлимита 

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

	private static final String CARD_IMPORT_ERROR_MSG = ContextProvider.getContext().getLocaleMessage("card.import.portlet.error");

	private String showAllImportResultMsg=null;

	protected PortletService portletService;

	private static int threadCount = CardImportSettings.getThreadCount();
	private static int cardsCountThreshold = CardImportSettings.getCardsCountThreshold();

	/**
	 * Get SessionBean from portlet session.
	 *
	 * @param request PortletRequest
	 * @return MIShowListPortletSessionBean
	 */
	private synchronized CardImportPortletSessionBean getSessionBean(PortletRequest request) {
		PortletSession session = request.getPortletSession();
		if( session == null )
			return null;
		CardImportPortletSessionBean sessionBean = (CardImportPortletSessionBean)session.getAttribute(SESSION_BEAN);
		if( sessionBean == null ) {
			sessionBean = new CardImportPortletSessionBean();
			session.setAttribute(SESSION_BEAN,sessionBean);
		}
		return sessionBean;
	}
	
	private static void saveSessionBeanForServlet(RenderRequest request, CardImportPortletSessionBean sessionBean) {
		PortletSession session = request.getPortletSession();
		//String namespace = response.getNamespace();
		session.setAttribute(SESSION_BEAN, sessionBean);
	}

	private void initBean(PortletRequest request, PortletResponse response) {
		final PortletSession session = request.getPortletSession();
		final CardImportPortletSessionBean sessionBean = getSessionBean(request);
		final DataServiceBean serviceBean = sessionBean.getServiceBean(request);
		portletService = Portal.getFactory().getPortletService();
		//final PortletService psrvc = Portal.getFactory().getPortletService();
		try {
			final String templateId = request.getParameter(CARD_TEMPLATE_PARAM);
			Template template = (Template)serviceBean.getById(new ObjectId(Template.class, Long.parseLong(templateId)));
			sessionBean.setTemplate(template);
			sessionBean.setBackUrl(request.getParameter(BACK_URL_FIELD));
			sessionBean.setCheckForExistsDoublets(Boolean.parseBoolean(request.getParameter(CHECK_DOUBLETS_PARAM)));
			sessionBean.setUpdateExistsDoublets(Boolean.parseBoolean(request.getParameter(UPDATE_DOUBLETS_PARAM)));
			if (request.getParameter(UPDATE_DOUBLETS_SUPPORT_PARAM)!=null){
				sessionBean.setSupportUpdateExistsDoublets(Boolean.parseBoolean(request.getParameter(UPDATE_DOUBLETS_SUPPORT_PARAM)));
			}
			if (request.getParameter(CHECK_DOUBLETS_SUPPORT_PARAM)!=null){
				sessionBean.setSupportCheckForExistsDoublets(Boolean.parseBoolean(request.getParameter(CHECK_DOUBLETS_SUPPORT_PARAM)));
			}
			String objectName = MessageFormat.format(ContextProvider.getContext().getLocaleMessage("card.import.success.object.name.default"), new Object[]{template.getName()});
			final String customImportObjectName = request.getParameter(CUSTOM_IMPORT_TITLE_PARAM); 
			if (customImportObjectName!=null&&!customImportObjectName.isEmpty()){
				objectName = ContextProvider.getContext().getLocaleMessage(request.getParameter(CUSTOM_IMPORT_TITLE_PARAM));
			}
			sessionBean.setObjectName(objectName);
			showAllImportResultMsg = sessionBean.getResourceBundle().getString("show.import.result.card");
		} catch (Exception e) {
			logger.error(e.getMessage());
			//sessionBean.setMessage(e.getMessage());
		}
	}
	
	@Override
	public void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
		// Set the MIME type for the render response
		response.setContentType(request.getResponseContentType());
		// Set user locale
		ContextProvider.getContext().setLocale(request.getLocale());
		
		PortletSession session = request.getPortletSession();
		PortletInvocation portletInvocation = (PortletInvocation) request.getAttribute(ContextDispatcherInterceptor.REQ_ATT_COMPONENT_INVOCATION);
        HttpServletRequest req = portletInvocation.getDispatchedRequest();//отсюда можно забирать параметры из гет запроса, в рендер реквест они не приходят
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
		final CardImportPortletSessionBean sessionBean = getSessionBean(request);
		if( sessionBean==null ) {
			response.getWriter().println("<b>NO PORTLET SESSION YET</b>");
			return;
		}
		sessionBean.setResourceBundle(ResourceBundle.getBundle("com.aplana.dbmi.portlet.nl.CardImportPortlet", request.getLocale()));
		initBean(request, response);
		saveSessionBeanForServlet(request, sessionBean);
		
		// Invoke the JSP to render
		PortletRequestDispatcher rd = getPortletContext().getRequestDispatcher(VIEW);
		rd.include(request,response);
	}
	
	@Override
	public void processAction(ActionRequest request, ActionResponse response) throws PortletException, java.io.IOException {
		CardImportPortletSessionBean sessionBean = getSessionBean(request);
		ObjectId templateId = sessionBean.getTemplate().getId();
		Map<String,Object> importParams = multipartContentHandle(request, response);
		InputStream originalFileStream = null;
		FileInputStream inputStream = null;
		File tempInputCopy = null;
		try{
			originalFileStream = (InputStream)importParams.get(FILENAME_PARAM);
			if (originalFileStream == null){
				throw new DataException("card.import.file.not.selected", new Object[]{});
			}
			// Создаем временную копию входного файла, чтобы с ним можно было безопасно работать.
			tempInputCopy = createTempCopyFile(originalFileStream);
			if (!EncodingUtils.verifyEncodingUTF8WithoutBOM(tempInputCopy))
				throw new DataException("card.import.file.not.verify.encoding", new Object[]{});
			
			DataServiceBean serviceBean = sessionBean.getServiceBean();
			ParseCardImportFile action = new ParseCardImportFile();
			action.setTemplateId(templateId);

			String filePath = (String)importParams.get(MATERIALNAME_PARAM);
			
			inputStream = new FileInputStream(tempInputCopy);
			action.setFile(inputStream);
			// здесь необходимо передать список сёрчев для каждого кардлинка импортируемого шаблона, чтобы шапка анализировалась полностью
			action.setCardLinkAttributeSearchMap(getCardLinkSearchMap(request, templateId));
			List<List<ImportAttribute>> importCardAttributes = (List<List<ImportAttribute>>)serviceBean.doAction(action);

			// после успешного парсинга входного файла, приступаем к импорту карточек
			int doubletsCheckInt =  ((Long)importParams.get(DOUBLETS_CHECK_HIDDEN_PARAM)).intValue();
			int updateDoubletsInt = ((Long)importParams.get(UPDATE_DOUBLETS_HIDDEN_PARAM)).intValue();
			boolean isDoubletsCheck = (doubletsCheckInt==SELECTED_VAL);   
			boolean isUpdateDoublets = (updateDoubletsInt==SELECTED_VAL);
			List<ImportAttribute> mandatoryCardAttributes = getMandatoryForDoubletCheckAttributes((List<ImportAttribute>)importCardAttributes.get(0));
			if ((isDoubletsCheck||isUpdateDoublets)&&(mandatoryCardAttributes==null||mandatoryCardAttributes.isEmpty())){
				throw new DataException("card.import.check.doublet.attributes.empty");
			}

			int allCount = importCardAttributes.size();
			final DataServiceBean actualServiceBean = sessionBean.getActualServiceBean(request);

			CardsImportResult result = processCardsImport(templateId, importCardAttributes, actualServiceBean, isDoubletsCheck, isUpdateDoublets);

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
				// выводим количество успешно добавленных карточек
				str.append("\n"+MessageFormat.format(ContextProvider.getContext().getLocaleMessage("card.import.adding.import.object"), new Object[]{result.getSuccessCount(), sessionBean.getObjectName()}));
				// потом выводим отсортированные по атрибуту SUCCESS_CARD_SHOW_ATTRIBUTE успешно добавленные карточки 
				str.append(generateSuccessSortMessage(result.getSuccessMsg()));
			}
			String importMessage = str.toString();
			final Date importDate = new Date();	// дата завершения импорта
			// перед тем как выдать сообщение пользователю, создаем карточку Результаты импорта
			ObjectId importCardId = createImportResult(actualServiceBean, importDate, sessionBean.getTemplate(), importMessage, tempInputCopy, filePath, (result.getSuccessCount() > 0));
			if (importMessage!=null){
				sessionBean.setMessage(updateHtmlImportMessage(request, response, importMessage, importCardId, sessionBean.getBackUrl(), true));
			}
			leaveCardImportPortlet(request, response, sessionBean);
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
		String prefix = "card_import_file";
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
		// Получаем родительскую карточку 
		
		FileItemFactory factory = new DiskFileItemFactory();
	    PortletFileUpload upload = new PortletFileUpload(factory);
	       
	    List<String> nameFiles = null;
	    //ArrayList<String> attachmentCardIds = new ArrayList<String>();
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

	private void leaveCardImportPortlet(ActionRequest request, ActionResponse response,CardImportPortletSessionBean sessionBean) throws IOException{
		String backURL = sessionBean.getBackUrl();
		// TODO: неплохо бы удалять и из application scope, но для этого
		// нужно знать namespace
		request.getPortletSession().removeAttribute(SESSION_BEAN);
		if (sessionBean.getMessage() != null){
			PortletSession session = request.getPortletSession();
			session.setAttribute(MIShowListPortlet.MSG_PARAM_NAME, sessionBean.getMessage(), PortletSession.APPLICATION_SCOPE);
		}
		sessionBean.reset();
		if (backURL == null) {
			redirectToPortalDefaultPage(request, response);
		} else {
			response.sendRedirect(backURL);
		}
	}

	/**
	 * Перенаправить на стартовую страницу портала
	 */
	private void redirectToPortalDefaultPage(ActionRequest request,
			ActionResponse response) throws IOException {
		String backURL = portletService.generateLink("dbmi.defaultPage", null,
				null, request, response);
		response.sendRedirect(backURL);
	}
	
	/*
	 * Создание карточки шаблона Результаты импорта для логирования информации об импорте
	 * @actualServiceBean - сервисбин с реальным пользователем для вызова экшена создания и сохранения карточки
	 * @importDate - реальная дата окончания импорта
	 * @template - импортируемый шаблон 
	 * @importMessage - сообщение о результатах импорта
	 * @file - входной файл импорта
	 * @filePath - полный путь к локальному расположению файла (либо его имя)
	 */
	private ObjectId createImportResult(DataServiceBean actualServiceBean, Date importDate, Template template, String importMessage, File file, String filePath, boolean isSuccess) throws DataException{
		if (actualServiceBean== null)
			return null;
		FileInputStream fis = null;
		try{
			// создаем карточку файла для файла импорта
			Card fileCard = createFileCard(actualServiceBean);
			fis = new FileInputStream(file);
			addFileToFileCard(fileCard, fis, filePath, actualServiceBean);
			actualServiceBean.doAction(new UnlockObject(fileCard));

			// создаем карточку файла для сообщения о результате импорта
			Card fileResultCard = createFileCard(actualServiceBean);
			importMessage = convertHtmlToTxt(importMessage);	// небольшое преобразование входного сообщения в простой текстовый вид
			InputStream resultFile = new ByteArrayInputStream(importMessage.getBytes("UTF-8"));
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
			addFileToFileCard(fileResultCard, resultFile, dateFormat.format(importDate)+".txt", actualServiceBean);
			actualServiceBean.doAction(new UnlockObject(fileResultCard));
			
			// создаем карточку результатов импорта
			CreateCard createAction = new CreateCard();
			createAction.setTemplate(TEMPLATE_IMPORT_RESULT);
			Card importCard = actualServiceBean.doAction(createAction); 
			
			// заполняем оставшимися атрибутами и сохраняем
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

			StringAttribute loadTemplate = importCard.getAttributeById(ATTR_IMPORT_DICT);
			if (loadTemplate!=null){
				loadTemplate.setValue(template.getName());
			}
			
			TextAttribute importResult = importCard.getAttributeById(ATTR_IMPORT_RESULT);
			if (importResult!=null){
				// если длина сообщения > 4000 символов, то урезаем, иначе не сможем записать в БД (полностью текст запишется во вложение)
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
	 * Обновляем сообщение о результатах импорта:
	 * каждую строчку урезаем до CardImportSettings.CHAR_COUNT_IN_LINE_LIMIT
	 * количество строк урезаем до CardImportSettings.LINE_COUNT_LIMIT 
	 * если лимит отображения превышен (в строку или число строк), отображаем OVER_LIMIT_STRING 
	 * если на входе сказано, что урезать ничего не надо, то не урезаем (формируем html-собщение для диалогового окна)
	 */
	private String updateHtmlImportMessage(ActionRequest request, ActionResponse response, String importMessage, ObjectId cardId, String backUrl, boolean isStrict ){
		//Если на вход пришел null, заменяем на пустую строку
		if (importMessage != null) {
			importMessage = convertHtmlToTxt(importMessage).trim();	// возможно html-теги надо генерировать в этом методе, а не в генерации сообщения результата
		} else {
			importMessage = "";
		}
		StringBuilder sb = new StringBuilder();
		// для начала парсим входную строку и при необходимости делаем обрезание
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
			sb.append("<br>");	// добавляем переход на новую строку	
		}
		// потом переходим на новую строку и генирируем ссылку на переход в карточку Результата импорта
		if (cardId!=null&&cardId.getId()!=null){
			sb.append("<br>");
			
			String cardPageId = portletService.getPageProperty("cardPage", request, response);
	        if (cardPageId == null) {
	            cardPageId = "dbmi.Card";
	        }
			final Map params = new HashMap();
			params.put( CardPortlet.EDIT_CARD_ID_FIELD, cardId.getId().toString());
			params.put( CardPortlet.BACK_URL_FIELD, backUrl);
			params.put( CardPortlet.OPEN_FOR_EDIT_FIELD, "false");
			String editLink = portletService.generateLink(cardPageId, "dbmi.Card.w.Card", params, request, response);
			
			sb = sb.append("<a href='").append(editLink).append("'>").append(showAllImportResultMsg).append("</a>");
		}
		return sb.toString();
	}
	
	/**
	 * Во входном тексте есть как стандартные операторы табуляции, перевода строки,
	 * так и html-теги, выполняющие ту же функцию и необходимые для корректного отображения
	 * сообщения об импорте в web-форме, в текстовом виде они не нужны, поэтому уберем их 
	 * @param inputMessage - входное сообщение о результатах импорта
	 * @return та же строка, но без html-тегов
	 */
	private String convertHtmlToTxt(String inputMessage){
		String patternString = "(<.*?>)";

		Pattern pattern = Pattern.compile(patternString);
		Matcher matcher = pattern.matcher(inputMessage);

		String replaceAll = matcher.replaceAll("");		
		return replaceAll;
	}

	/**
	 * Сформировать из исходного списка импортируемых атрибутов список обязательных для поиска дубликатов атрибутов
	 * @param ImportAttributes - исходный список импортируемых атрибутов
	 * @return список только обязательных для поиска атрибутов
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
		// здесь по идее ещё надо отсортировать по возрастанию
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
	 * Загрузить серчи для всех кардлинков из данного щаблона
	 * @param request - входной запрос
	 * @param templateId - id входного шаблона
	 */
	private Map<ObjectId, Search> getCardLinkSearchMap(PortletRequest request, ObjectId templateId){
		Map<ObjectId, Search> result = new HashMap<ObjectId, Search>(); 
		CardPortletSessionBean sessionBean = createNewCard(request, templateId);
		CardPortletCardInfo cardInfo = sessionBean.getActiveCardInfo();
		AsyncDataServiceBean serviceBean = sessionBean.getServiceBean();

		try {
			if (cardInfo.getCard() != null && cardInfo.isRefreshRequired()) {
				// подгружаем инфу о карточке
				loadCardInfo(sessionBean, serviceBean);
				// загружаем свойства её атрибутов
				initTabs(request, sessionBean, cardInfo.getCard());
			}
			// пройдемся по всем атрибутам создаваемой карточки и для кардлников выпишем их сёрчи
			for( Iterator iter = cardInfo.getCard().getAttributes().iterator(); iter.hasNext();){
				AttributeBlock attrBlock = (AttributeBlock)iter.next();
				for (Attribute attr: attrBlock.getAttributes()){
					if (attr instanceof CardLinkAttribute||attr instanceof TypedCardLinkAttribute||attr instanceof DatedTypedCardLinkAttribute){
						CardLinkPickerDescriptor cardLinkPickerDescriptor = (CardLinkPickerDescriptor)cardInfo.getAttributeEditorData(attr.getId(), "cardLinkPickerDescriptor");
						if (cardLinkPickerDescriptor!=null){
							CardLinkPickerVariantDescriptor descr = cardLinkPickerDescriptor.getDefaultVariantDescriptor();
							Search attrSearch = (descr!=null)?descr.getSearch():null;
							if (attrSearch!=null){
								result.put(attr.getId(), attrSearch);
							}
						}
					}
				}
			}			
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		request.getPortletSession().removeAttribute(CardPortlet.SESSION_BEAN);

		return result;
	}
	
	protected CardPortletSessionBean createSessionBean(PortletRequest request) {
		PortletSession session = request.getPortletSession(true);
		CardPortletSessionBean sessionBean = new CardPortletSessionBean();
		AsyncDataServiceBean serviceBean = PortletUtil.createService(request);
		sessionBean.setDataServiceBean(serviceBean);
		try {
			sessionBean.setAdminEmail(serviceBean.doAction(new GetAdminEmail())
					.toString());
		} catch (Exception e) {
			logger.error("Failed to determine Admin email", e);
		}
		session.setAttribute(CardPortlet.SESSION_BEAN, sessionBean);
		return sessionBean;
	}
	
	/**
	 * Загружаются параметры, необходимые для отображения карточки
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
			// новая карточка (или например клонированная) открывается в режиме
			// редактирования
			cardInfo.setAvailableWorkflowMoves(new ArrayList(0));
			cardInfo.setAttributeViewParams(loadAttributeViewParams(
					serviceBean, card));
			cardInfo.setCanChange(true);
			cardInfo.setTabsManager(new TabsManager());
		} 
		cardInfo.clearAttributeEditorsData();
		CreateCard createCardAction = new CreateCard();
		createCardAction.setTemplate(card.getTemplate());
		cardInfo.setCanCreate(serviceBean.canDo(createCardAction));
		cardInfo.setCardState((CardState) serviceBean.getById(card.getState()));

		ActionsDescriptor ad = new ActionsDescriptor();

		final CardPortletActionsManager am = new CardPortletActionsManager();
		am.setServiceBean(serviceBean);
		am.setSessionBean(sessionBean);
		am.setActionsDescriptor(ad);
		cardInfo.setActionsManager(am);
		cardInfo.setRefreshRequired(false);
		cardInfo.setTabInfo(null);
	}
	
	private CardPortletSessionBean createNewCard(PortletRequest request, ObjectId templateId){
		CardPortletSessionBean sessionBean = createSessionBean(request);
		sessionBean.setViewMode(ObjectId.predefined(ViewMode.class, "admin"));
		CardPortletCardInfo cardInfo = createCardPortletCardInfo();
		sessionBean.setActiveCardInfo(cardInfo);
		Card card = createCardHandler(request, templateId, sessionBean);
		cardInfo.setAvailableWorkflowMoves(new ArrayList(0));
		try{
			cardInfo.setAttributeViewParams(loadAttributeViewParams(
				sessionBean.getServiceBean(), card));
		} catch (Exception e){
			logger.error("Error while load AttributeViewParams:"+e.getMessage());
		}
		cardInfo.setCanChange(true);
		cardInfo.setTabsManager(new TabsManager());
		
		return sessionBean;		
	}

	protected CardPortletCardInfo createCardPortletCardInfo() {

		CardPortletCardInfo cardInfo = new CardPortletCardInfo();

		return cardInfo;
	}

	protected Card createCardHandler(PortletRequest request, ObjectId templateId, CardPortletSessionBean sessionBean) {
		try {
			return CardPortlet.createCard(sessionBean, templateId, null);

		} catch (Exception e) {
			logger.error("Exception caught", e);
			return null;
		}
	}
	/**
	 * инициализация всех закладок входной карточки
	 * @param request
	 * @param activeTabId
	 * @param card
	 */
	private void initTabs(PortletRequest request, CardPortletSessionBean sessionBean, Card card) {
		try {
			DataServiceBean serviceBean = sessionBean.getServiceBean();
			Collection tabs = serviceBean.listChildren(card.getTemplate(),
					TabViewParam.class);
			if (tabs.size() == 0) {
				TabView tv = new TabView();
				tv.setId(-1);
				tabs.add(tv);
			}
			TabsManager tm = sessionBean.getActiveCardInfo().getTabsManager();
			tm.setTabs(tabs);
			// пройдёмся по всем закладкам карточки загрузим все блоки и атрибуты
			if (tabs.size() > 0){
				for (Iterator iter = tabs.iterator();iter.hasNext();){
					ObjectId activeTabId  = ((Tab) iter.next()).getId();

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
					//if (bvsAll == null) {
						bvsAll = plant.build();
						sessionBean.getActiveCardInfo().setTabInfo(bvsAll);
					//}
			
					plant.setBvsAll(bvsAll);
					List<BlockView> bvs = plant.getActiveTab();
					
					activeTab.getContainer().setComponents(bvs);
		
					tm.addTab(activeTab);
					tm.setActiveTabId(activeTab.getId());
		
					/*GetEmptyTabs action = new GetEmptyTabs();
					tabs = (Collection) serviceBean.doAction(action);
					tm.setEmptyTabs(tabs);*/
				}
			}
		} catch (Exception e) {
			logger.error("Exception caught", e);
		}
	}
	
	protected Collection loadAttributeViewParams(DataServiceBean serviceBean,
			final Card card) throws DataException, ServiceException {
		Collection avps = serviceBean.listChildren(card.getTemplate(),
				AttributeViewParam.class);
		// т.к. посредством импорта могут заполняться любые атрибуты - даже те, которые пользователею не видны, то все атрибуты надо поменить как видимые (это необходимо для корректной подгрузки эдиторов)  
		for(Object o: avps){
			((AttributeViewParam)o).setHidden(false);  
		}
		return avps;
	}

	private static String getTemplateNameById(DataServiceBean serviceBean, ObjectId templateId) {
		String result = null;
		try {
			Template template = (Template)serviceBean.getById(templateId);
			if (template != null) {
				result = template.getName();
			}
		}
		catch (Exception e) {
			if (logger.isWarnEnabled()) {
				logger.warn("Unable to retreive Template object by name due to following error:" + e.getMessage(), e);
			}
		}

		return result;
	}

	private CardsImportResult processCardsImport(ObjectId templateId, List<List<ImportAttribute>> importCardAttributes, DataServiceBean serviceBean, boolean isDoubletsCheck, boolean isUpdateDoublets) throws DataException {
		CardsImportResult result = new CardsImportResult();

		int recordsCount = importCardAttributes.size();

		long startTimeParallel = System.currentTimeMillis();

		ExecutorService pool = null;
		List<CardImportFutureTask> futures = new LinkedList<CardImportFutureTask>();
		int currentThreadCount; //Реальное количество потоков, которое будет использовано
		int partSize; //Количество импортируемых карточек в одном потоке
		try {

			if (recordsCount > cardsCountThreshold) {
				currentThreadCount = threadCount;
				partSize = (recordsCount / currentThreadCount) + 1;
			} else {
				currentThreadCount = 1;
				partSize = recordsCount;
			}

			String templateName = getTemplateNameById(serviceBean, templateId);

			if (logger.isDebugEnabled()) {
				logger.debug("Starting cards import process with following params: [template: " + templateName + ", recordsCount: " + recordsCount + ", thread count: " + currentThreadCount + ", part size:" + partSize + "]");
			}

			pool = Executors.newFixedThreadPool(currentThreadCount);

			for (int i = 0; i < currentThreadCount; i++) {
				if (logger.isDebugEnabled()) {
					logger.debug("Trying to start " + i + "-th thread");
				}
				CardImportFutureTask fTask = new CardImportFutureTask(new CardImportThread(templateId,
																						   templateName,
																						   importCardAttributes,
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
				Iterator<CardImportFutureTask> iter = futures.iterator();

				while(iter.hasNext()) {
					CardImportFutureTask fCurrent = iter.next();
					if (fCurrent.isDone()) {

						//Собираем результат в общую копилку и убираем его из списка
						try {
							CardImportThread cardImportThread = fCurrent.get();
							if (cardImportThread != null) {

								if (logger.isDebugEnabled()) {
									logger.debug("Thread # " + cardImportThread.currentPartIndex + " is done.");
								}
								result.addDoubletMessages(cardImportThread.getDoubletMsgList());
								result.addErrorMessages(cardImportThread.getErrorMsgList());
								result.addSuccessMessages(cardImportThread.getSuccessMsgList());
							}
						} catch (Exception e) {
							logger.error("Unable to retrieve result from thread# " + fCurrent.getPartIndex() + " due to following exception: " + e.getMessage(), e);
							//Все карточек, которые обрабатывал этот поток, помечаем не загруженными
							List<String> errorMessages = new LinkedList<String>();
							for (int i = fCurrent.getFromIndex(); i < fCurrent.getToIndex(); i++) {
								errorMessages.add(MessageFormat.format("\n\t" + CARD_IMPORT_ERROR_MSG, new Object[]{i+CardImportSettings.LINE_NUMBER_OFFSET, e.getMessage()}));
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
			logger.error("Card import process is terminated due to following exception: " + ex.getMessage(), ex);
			throw new DataException("card.import.portlet.critical.runtime", new Object[]{ex.getMessage()});
		} finally {
			if (logger.isDebugEnabled()) {
				logger.error("Card import execution took " + String.valueOf(System.currentTimeMillis() - startTimeParallel) + " ms with cardsCount = " + recordsCount);
			}
		}

		return result;
	}

	/**
	 *Приходится вызывать асинхронный вызов, так как текущая архитекнута не позволяет из синхронного Query запускать асинхронные
	 *Класс CardImportThread вызывает параллельно ImportCards для указанного диапазона входных данных:
	 *@param templateId - id шаблона импортируемы карточек
	 *@param importCardAttributes - список актрибутов импортируемых карточек
	 *@param serviceBean - объект класса DataServiceBean, из которого мы будем запускать ImportCards
	 *@param partSize - количество элементов
	 *@param currentPartIndex - номер части, над которой будет работать данный поток
	 *@param allCount - общее количество импортируемых карточек
	 *@param isDoubletsCheck - флаг проверки дубликатов
	 *@param isUpdateDoublets - флаг обновления дубликатов
	 */
	class CardImportThread implements Callable<CardImportThread> {

		private DataServiceBean serviceBean;
		private ObjectId templateId;
		private String templateName;
		private List<List<ImportAttribute>> importCardAttributes;
		private int partSize;
		private int currentPartIndex;
		private int allCount;
		private boolean isDoubletsCheck;
		private boolean isUpdateDoublets;

		private List<String> doubletMsgList = new LinkedList<String>();
		private List<String> successMsgList = new LinkedList<String>();
		private List<String> errorMsgList   = new LinkedList<String>();

		CardImportThread(ObjectId templateId, String templateName, List<List<ImportAttribute>> importCardAttributes, DataServiceBean serviceBean, int partSize, int currentPartIndex, int allCount, boolean isDoubletsCheck, boolean isUpdateDoublets) {

			this.templateId = templateId;
			this.templateName = templateName;
			this.importCardAttributes = importCardAttributes;
			this.serviceBean = serviceBean;
			this.partSize = partSize;
			this.currentPartIndex = currentPartIndex;
			this.allCount = allCount;
			this.isDoubletsCheck = isDoubletsCheck;
			this.isUpdateDoublets = isUpdateDoublets;
		}

		@Override
		public CardImportThread call() {
			for (int i = currentPartIndex*partSize; i < (currentPartIndex+1)*partSize && i < allCount; i++) {
				try{

					List<ImportAttribute> importCard = importCardAttributes.get(i);
					ImportCards importAction = new ImportCards();

					importAction.setTemplateId(templateId);
					importAction.setTemplateName(templateName);
					importAction.setCheckDoublets(isDoubletsCheck);
					importAction.setUpdateDoublets(isUpdateDoublets);

					importAction.setImportAttributes(importCard);
					importAction.setLineNumber(i+CardImportSettings.LINE_NUMBER_OFFSET);
					ImportResult importResult = (ImportResult)serviceBean.doAction(importAction);
					if (importResult.getResultType().equals(ImportResult.IMPORT_RESULT_TYPE.DOUBLET)){
						doubletMsgList.add(importResult.getResultMessage());
					}
					if (importResult.getResultType().equals(ImportResult.IMPORT_RESULT_TYPE.SUCCESS)){
						successMsgList.add(importResult.getResultMessage());
					}
				} catch (Exception ex){
					errorMsgList.add(MessageFormat.format("\n\t" + CARD_IMPORT_ERROR_MSG, new Object[]{i+CardImportSettings.LINE_NUMBER_OFFSET, ex.getMessage()}));
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

	class CardImportFutureTask extends FutureTask<CardImportThread> {

		private int fromIndex;
		private int toIndex;
		private int partIndex;

		public CardImportFutureTask(Callable callable) {
			super(callable);
		}

		public CardImportFutureTask(Callable callable, int partSize, int partIndex, int allCount) {
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
