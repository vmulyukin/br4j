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
import java.util.Collection;
import java.util.Iterator;
import java.util.ResourceBundle;

import org.apache.commons.io.IOUtils;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletRequest;

import com.aplana.dbmi.action.CreateCard;
import com.aplana.dbmi.action.DownloadFile;
import com.aplana.dbmi.action.Material;
import com.aplana.dbmi.action.UploadFile;
import com.aplana.dbmi.card.CardPortletSessionBean.PortletMessage.PortletMessageType;
import com.aplana.dbmi.card.FilesAndCommentsUtils.RoundDataFiles;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.MaterialAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.AsyncDataServiceBean;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.owriter.manager.OWriterServiceClient;
import com.aplana.owriter.manager.OWriterSessionManager;

public class FilesAndCommentsAttributeEditor extends CardLinkExAttributeEditor {
	
	ObjectId fileLinkAttributeId;
	boolean hideChildrenWhenDSP = false;
	boolean hideChildren = false;

	public static final String INIT_FILE_EDIT_ACTION = "INIT_FILE_EDIT_ACTION";
	public static final String FORCE_INIT_FILE_EDIT_ACTION = "FORCE_INIT_FILE_EDIT_ACTION";
	public static final String FINISH_FILE_EDIT_ACTION = "FILE_EDIT_FINISHED_ACTION";
	
	public static final String NEED_FILE_EDIT_CONFIRMATION = "NEED_FILE_EDIT_CONFIRMATION";
	public static final String OPEN_FILE_EDIT = "OPEN_FILE_EDIT";
	public static final String OPEN_HREF_PARAM = "OPEN_HREF_PARAM";

	public static final String KEY_FILE_EDIT = "fileEdit";
	public static final String PARAM_EDIT = "edit";
	
	public static final String PARAM_MULTIVALUED = "multiValued";

	private boolean modeEdit = false;
	private AttrType attrType = AttrType.UNDEFINED;
	
	private enum AttrType {
		UNDEFINED, SINGLEVALUED, MULTIVALUED
	}
	
	private ResourceBundle resourceBundle;

	public FilesAndCommentsAttributeEditor() {
		super();
		setParameter(PARAM_JSP, "/WEB-INF/jsp/html/attr/FilesAndCommentsEdit.jsp");
	}
	
	@Override
	public boolean processAction(ActionRequest request,	ActionResponse response, Attribute attr) throws DataException {
		if(attr instanceof CardLinkAttribute && attrType != AttrType.UNDEFINED)
			((CardLinkAttribute) attr).setMultiValued(attrType == AttrType.MULTIVALUED);
		
		final String action = request.getParameter(CardPortlet.ACTION_FIELD);
		resourceBundle = ResourceBundle.getBundle("com.aplana.dbmi.gui.nl.CardLinkEditResource", request.getLocale());

		final CardPortletSessionBean sessionBean = CardPortlet.getSessionBean(request);

		if (INIT_FILE_EDIT_ACTION.equals(action)) {
			String fileCardId = request.getParameter(FIELD_LINKED_ID);
			String userLogin = sessionBean.getServiceBean().getPerson().getLogin();
			try {
				if (null == fileCardId){
					logger.error("File card id is null");
					return false;
				}
				OWriterSessionManager sessionManager =  new OWriterSessionManager();
				sessionBean.setOWriterSessionManager(sessionManager);
				sessionManager.setUserLogin(userLogin);
				sessionManager.setFileCardId(Long.parseLong(fileCardId));
				
				if(sessionManager.getServiceClient().doesUserSessionExist(userLogin, sessionManager.getServerId())){
					response.setRenderParameter(NEED_FILE_EDIT_CONFIRMATION, Boolean.TRUE.toString());
					return true; // ������ �� �������� ���������������� �������
				}else {
					initFileEdit(sessionBean, response, false);
				}
			} catch (Exception e){
				sessionBean.setMessageWithType(resourceBundle.getString("file.edit.init.fail") , PortletMessageType.ERROR);
				logger.error("Error during file editor initialization for user " + userLogin + 
						" and file card " + fileCardId, e);
			}
		} else if (FORCE_INIT_FILE_EDIT_ACTION.equals(action)){
				initFileEdit(sessionBean, response, true);
		} else if (FINISH_FILE_EDIT_ACTION.equals(action)){
			finishFileEdit(sessionBean, request, response, attr);
		} else {
			return super.processAction(request, response, attr);
		}
		return true;
	}
	
	@Override
	public void initEditor(PortletRequest request, Attribute attr) throws DataException{
		super.initEditor(request, attr);
		if (isModeEdit()){
			CardPortlet.getSessionBean(request).getActiveCardInfo().setAttributeEditorData(attr.getId(), KEY_FILE_EDIT, fileLinkAttributeId);
		}
		CardPortlet.getSessionBean(request).getActiveCardInfo().setAttributeEditorData(attr.getId(), FilesAndCommentsUtils.PARAM_FILE_LINK, fileLinkAttributeId);
	}
	
	@Override
	public void loadAttributeValues(Attribute attr, PortletRequest request) {
		
		CardPortletSessionBean sessionBean = CardPortlet.getSessionBean(request);
		CardPortletCardInfo cardInfo = sessionBean.getActiveCardInfo();	
		Card documentCard = cardInfo.getCard();

		
		FilesAndCommentsUtils utils = new FilesAndCommentsUtils(documentCard, attr, sessionBean.getServiceBean());
		
		if (!utils.isRoundExists()) {
			super.loadAttributeValues(attr, request);
		}
		else {
			if (utils.getCurrentRound() == 0 || hideChildren) {
				super.loadAttributeValues(attr, request);
				@SuppressWarnings("unchecked")
				Collection<Card> cards = (Collection<Card>) cardInfo.getAttributeEditorData(attr.getId(), LinkedCardUtils.ATTR_LINK_CARDS_LIST);
				Iterator<Card> i = cards.iterator();
				while(i.hasNext()){
					Card c = i.next();
					try{
						sessionBean.getServiceBean().getById(c.getId());
					} catch (DataException e){
						i.remove();
					} catch (ServiceException e) {
						logger.error(e);
					}
				}
			}
			else {
				try {
					RoundDataFiles roundDataFiles = utils.loadLinkedData((hideChildrenWhenDSP  && utils.isDSP(documentCard)) || hideChildren);
					saveRoundDataFiles(roundDataFiles, cardInfo, attr);					
				} catch (Exception e) {
					if (sessionBean != null)
						sessionBean.setMessageWithType("edit.link.error.create", new Object[] { e.getMessage() }, PortletMessageType.ERROR);
				}
			}
		}
	}
	
	/**
	 * ���������� ���������� � ��������� � cardInfo.
	 * @param roundDataFiles
	 * @param cardInfo
	 * @param attr
	 */
	private void saveRoundDataFiles(RoundDataFiles roundDataFiles, CardPortletCardInfo cardInfo, Attribute attr){
		cardInfo.setAttributeEditorData(attr.getId(), FilesAndCommentsUtils.ROUND_DATA_ARRAY, roundDataFiles.getRoundDatas());
		cardInfo.setAttributeEditorData(attr.getId(), LinkedCardUtils.ATTR_LINK_COLUMNS_LIST, roundDataFiles.getColumns());
	}
	
	@Override
	public void setParameter(String name, String value){
		if (name.equalsIgnoreCase(FilesAndCommentsUtils.PARAM_FILE_LINK)) {
			fileLinkAttributeId = ObjectIdUtils.getAttrObjectId(value, ":");
		}else if (name.equalsIgnoreCase(FilesAndCommentsUtils.PARAM_HIDE_WHEN_DSP)) {
			hideChildrenWhenDSP = Boolean.parseBoolean(value);
		}else if (name.equalsIgnoreCase(FilesAndCommentsUtils.PARAM_HIDE_CHILDREN)) {
			hideChildren = Boolean.parseBoolean(value);
		}else if (name.equalsIgnoreCase(PARAM_EDIT)) {
			modeEdit = Boolean.parseBoolean(value);
		}else if(name.equalsIgnoreCase(PARAM_MULTIVALUED)) {
			attrType = (Boolean.parseBoolean(value.trim())) ? AttrType.MULTIVALUED : AttrType.SINGLEVALUED;
		} else {
			super.setParameter(name, value);
		}
	}
	
	public boolean isModeEdit() {
		return modeEdit;
	}
	
	public void initFileEdit(CardPortletSessionBean sessionBean, ActionResponse response, boolean forceKill) {
		OWriterSessionManager sessionManager = sessionBean.getOWriterSessionManager();
		OWriterServiceClient srvClient =  sessionManager.getServiceClient();
		String userLogin = sessionManager.getUserLogin();
		try {
			if (forceKill){
				boolean removed = srvClient.removeUserSession(userLogin, sessionManager.getServerId(), 1);
				if (!removed){
					sessionBean.setMessageWithType(resourceBundle.getString("file.edit.session.remove.fail") , PortletMessageType.ERROR);
					logger.error("Cannot kill previous session for user " + sessionManager.getUserLogin() 
							+ " while initializing editor for file card" + sessionManager.getFileCardId());
					return;
				}
			}
			Material material = getMaterial(sessionBean, sessionManager.getFileCardId());
			sessionManager.setFileName(material.getName());
			sessionManager.setOrigFileData(material.getData());
	
			boolean result = sessionManager.initSession();
			if (!result){
				sessionBean.setMessageWithType(resourceBundle.getString("file.edit.init.fail") , PortletMessageType.ERROR);
				logger.error("Cannot initialize writer application session for user " + sessionManager.getUserLogin() 
						+ " and file card" + sessionManager.getFileCardId());
			}else {
				String appLink = sessionManager.generateAppLink();
				if (null != appLink){
					response.setRenderParameter(OPEN_FILE_EDIT, Boolean.TRUE.toString());
					response.setRenderParameter(OPEN_HREF_PARAM, appLink);
				}else {
					sessionBean.setMessageWithType(resourceBundle.getString("file.edit.init.fail") , PortletMessageType.ERROR);
					logger.error("Cannot generate writer application link for user " + sessionManager.getUserLogin() 
							+ " and file card" + sessionManager.getFileCardId());
				}
			}
		}catch (Exception e){
			sessionBean.setMessageWithType(resourceBundle.getString("file.edit.init.fail") , PortletMessageType.ERROR);
			logger.error("Error during file editor initialization for user " + sessionManager.getUserLogin() + 
					" and file card " + sessionManager.getFileCardId(), e);
		}
	}

	public void finishFileEdit(CardPortletSessionBean sessionBean, ActionRequest request, 
				ActionResponse response, Attribute attr) {
		OWriterSessionManager sessionManager = sessionBean.getOWriterSessionManager();
		try {
			if(sessionManager.isFileChanged()){
				Card newFileCard = createNewFileCard(sessionBean, sessionManager);
				addFileCardToDocument(sessionManager, request, newFileCard, attr);
				sessionBean.setMessageWithType(resourceBundle.getString("file.edit.finish.success") , PortletMessageType.EVENT);
			}else {
				sessionBean.setMessageWithType(resourceBundle.getString("file.edit.not.changed") , PortletMessageType.INFO);
			}
		}catch (Exception e) {
			sessionBean.setMessageWithType(resourceBundle.getString("file.edit.finish.fail") , PortletMessageType.ERROR);
			logger.error("Cannot finish file edit operation properly for user " + sessionManager.getUserLogin()
					+ " and file card " + sessionManager.getFileCardId() + ". File will not be changed", e);
		}finally {
			sessionManager.closeSession();
		}
	}
	
	/**
	 * ����� ��� �������� ����� �������� �������� �� ������ ������
	 * @return Card newFileCard
	 */
	private Card createNewFileCard(CardPortletSessionBean sessionBean, OWriterSessionManager sessionManager) 
			throws IOException, DataException, ServiceException
	{
		AsyncDataServiceBean serviceBean = sessionBean.getServiceBean();
		Card newFileCard = null;
		InputStream fileData = null;
		try{
			fileData = sessionManager.getNewFileData();
			// ��������� �� �� ������� "����"
			CreateCard createCardAction = new CreateCard();
			createCardAction.setTemplate(ObjectId.predefined(Template.class, "jbr.file"));
	
			Card oldFileCard = (Card)serviceBean.getById(new ObjectId(Card.class, sessionManager.getFileCardId()));
			newFileCard = serviceBean.doAction(createCardAction);

			StringAttribute name = (StringAttribute)newFileCard.getAttributeById(Attribute.ID_NAME);
			if (null != name){
				name.setValue("file");
			}
			
			MaterialAttribute materialAttr = (MaterialAttribute)newFileCard.getAttributeById(Attribute.ID_MATERIAL);
			if (null != materialAttr)
			{
				materialAttr.setMaterialName(sessionManager.getFileName());
				materialAttr.setMaterialType(MaterialAttribute.MATERIAL_FILE);
			}

			StringAttribute materialNameAttr = (StringAttribute) newFileCard.getAttributeById(ObjectId.predefined(StringAttribute.class, "jbr.materialName"));
			if (null != materialNameAttr){
				materialNameAttr.setValue(sessionManager.getFileName());
			}

			StringAttribute nameAttr = (StringAttribute)newFileCard.getAttributeById(Attribute.ID_NAME);
			if (null != nameAttr){
				nameAttr.setValue(sessionManager.getFileName());
			}

			// �������� ������� "�������� ��������"
			final ObjectId primacyId = ObjectId.predefined(ListAttribute.class, "jbr.prime");
			ListAttribute oldPrimacyAttr = (ListAttribute) oldFileCard.getAttributeById(primacyId);
			ListAttribute newPrimacyAttr = (ListAttribute) newFileCard.getAttributeById(primacyId);
			
			if (null != newPrimacyAttr && null != oldPrimacyAttr)
			{
				newPrimacyAttr.setValueFromAttribute(oldPrimacyAttr);
			}
	
			// ��������� ����� �� ����
			ObjectId newFileCardId = serviceBean.saveObject(newFileCard);
			newFileCard.setId(newFileCardId);
			
			// ��������� ����������������� ���� � ����� �� ����
			UploadFile uploadAction = new UploadFile();
		    uploadAction.setCardId(newFileCardId);
		    String fileName = sessionManager.getFileName();
		    uploadAction.setFileName(fileName);
		    uploadAction.setData(fileData);
		    serviceBean.doAction(uploadAction);
		}finally {
			IOUtils.closeQuietly(fileData);
		}
		return newFileCard;
	}

	/**
	 * ����� ��� ���������� ����� �������� ����� �� �������� ��������� ������ ������
	 * @param Card fileCard
	 */
	private void addFileCardToDocument(OWriterSessionManager sessionManager, 
			ActionRequest request, Card fileCard, Attribute attr) {
		removeLink((CardLinkAttribute) attr, new ObjectId(Card.class, sessionManager.getFileCardId()));
		addLink((CardLinkAttribute) attr, fileCard.getId());
		loadAttributeValues(attr, request);
	}
	
	private Material getMaterial(CardPortletSessionBean sessionBean, Long fileCardId)
			throws DataException, ServiceException{
		Material material = null;
		DownloadFile action = new DownloadFile();
		action.setCardId(new ObjectId(Card.class, fileCardId));
		material = (Material) sessionBean.getServiceBean().doAction(action);
		return material;
	}
}
