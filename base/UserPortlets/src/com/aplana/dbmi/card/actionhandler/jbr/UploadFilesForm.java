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
package com.aplana.dbmi.card.actionhandler.jbr;

import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.portlet.PortletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.action.CreateCard;
import com.aplana.dbmi.action.OverwriteCardAttributes;
import com.aplana.dbmi.action.SignAttachment;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.action.UploadFile;
import com.aplana.dbmi.card.AttributeEditor;
import com.aplana.dbmi.card.CardPortlet;
import com.aplana.dbmi.card.CardPortletSessionBean;
import com.aplana.dbmi.card.CardPortletSessionBean.PortletMessage.PortletMessageType;
import com.aplana.dbmi.card.JspAttributeEditor;
import com.aplana.dbmi.card.PortletForm;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.HtmlAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.MaterialAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.TextAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.dbmi.service.AsyncDataServiceBean.ExecuteOption;

public class UploadFilesForm implements PortletForm {
	private static final Log logger = LogFactory.getLog(UploadFilesForm.class);
	private static final String UPLOAD_JSP = "UploadFiles.jsp";
	
	private static final ObjectId TEMPLATE_FILE =
		ObjectId.predefined(Template.class, "jbr.file");

	public static final String ATTR_KEY_NAMES_FILES = "namesFiles";
	
	public static final String ATTR_KEY_MODE = "mode";
	public static final String MODE_SELECTED = "modeSelected";
	public static final String MODE_RESULT = "modeResult";
	
	public static final String FILENAME_PARAM = "file";
	public static final String PRIMACY_PARAM = "isPrime";
	public static final String MATERIALNAME_PARAM = "materialName";
	
	public static final ObjectId materialNameId = ObjectId.predefined(StringAttribute.class, "jbr.materialName");
	public static final ObjectId primacyId = ObjectId.predefined(ListAttribute.class, "jbr.prime");
	public static final ObjectId descrId = ObjectId.predefined(TextAttribute.class, "descr");
	public static final ObjectId yesId = ObjectId.predefined(ReferenceValue.class, "jbr.incoming.control.yes");
	public static final ObjectId noId = ObjectId.predefined(ReferenceValue.class, "jbr.incoming.control.no");
	
	private Attribute attr;
	
	public UploadFilesForm(Attribute attr, CardPortletSessionBean sessionBean) {
		this.attr = attr;
		setConfigParameter(ATTR_KEY_MODE, MODE_SELECTED, this.attr.getId(), sessionBean);
	}

	public void doFormView(RenderRequest request, RenderResponse response)
			throws IOException, PortletException {
		PortletSession session = request.getPortletSession();		
		//request.getParameter("");
		request.setAttribute(JspAttributeEditor.ATTR_ATTRIBUTE, attr);
		
		PortletRequestDispatcher rd =
			session.getPortletContext().getRequestDispatcher(CardPortlet.JSP_FOLDER + UPLOAD_JSP);
		
		rd.include(request, response);
		
	}

	public void processFormAction(ActionRequest request, ActionResponse response)
			throws IOException, PortletException {
		try{
			CardPortletSessionBean sessionBean = CardPortlet.getSessionBean(request);
			String action = request.getParameter(CardPortlet.ACTION_FIELD);
			if (CardPortlet.BACK_ACTION.equals(action)) {	
					sessionBean.getActiveCardInfo().getPortletFormManager().closeForm();
			} else if (request.getParameter("signature") != null){
				String signCards = request.getParameter("signCards");
				if (signCards != null && signCards.length()>0){
					String[] signCardIds = signCards.split(";");
					String[] signs = request.getParameter("signature").split(";");
					for (int i=0; i<signCardIds.length; i++){
						ObjectId signatureAttributeId = ObjectId.predefined(HtmlAttribute.class, "jbr.uzdo.signature");
						Card attachmentCard = (Card) sessionBean.getServiceBean().getById(new ObjectId(Card.class, Long.parseLong(signCardIds[i])));
						HtmlAttribute signAttr = (HtmlAttribute)attachmentCard.getAttributeById(signatureAttributeId);
						signAttr.setValue(signs[i]);
						SignAttachment signAttachmentAction = new SignAttachment();
						signAttachmentAction.setCard(attachmentCard);
						sessionBean.getServiceBean().doAction(signAttachmentAction);
					}
					sessionBean.setMessageWithType(sessionBean.getResourceBundle().getString("ds.attachments.success.msg"), PortletMessageType.EVENT);
				}
				sessionBean.getActiveCardInfo().getPortletFormManager().closeForm();
			}else if (PortletFileUpload.isMultipartContent(request)) {
				List<String> addedAttachments = multipartContentHandle(request, response);
				setConfigParameter(ATTR_KEY_MODE, MODE_RESULT, attr.getId(), sessionBean);
				final boolean desable_ds = request.getParameter("desable_ds")==null? false : request.getParameter("desable_ds").equals("true");
				//������������� ���� ������������� ���������� ���
				if (addedAttachments.size() > 0 && sessionBean.isDsSupport(request) && !desable_ds){
					String attachmentIdsAsString = "";
					for (int i=0; i<addedAttachments.size(); i++){
						if (i>0){
							attachmentIdsAsString += ";";
						}
						attachmentIdsAsString += addedAttachments.get(i); 
					}
					response.setRenderParameter("ds.need.sign", attachmentIdsAsString);
				}
			} /*else {
				String action = request.getParameter(CardPortlet.ACTION_FIELD);
				if (CardPortlet.BACK_ACTION.equals(action)) {		
					sessionBean.getActiveCardInfo().getPortletFormManager().closeForm();
				}
			}*/
			if (!sessionBean.isDsSupport(request)) {
			    response.setRenderParameter("showAlert", "true");
			}
		}catch(Exception ex){
			new PortletException("Can not processFormAction", ex);
		}
	}
	
	private ArrayList<String> multipartContentHandle(ActionRequest request, ActionResponse response) {
		// �������� ������������ �������� 
		CardPortletSessionBean sessionBean = CardPortlet.getSessionBean(request);
		Card parentCard = sessionBean.getActiveCard();
		
		FileItemFactory factory = new DiskFileItemFactory();
	    PortletFileUpload upload = new PortletFileUpload(factory);
	       
	    List<String> nameFiles = null;
	    ArrayList<String> attachmentCardIds = new ArrayList<String>();
	    
	    try {
	    	List<FileItem> items = upload.parseRequest(request);
	    	
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
	    	nameFiles = new ArrayList<String>(items.size());
	    	for (Map<String, FileItem> map : data) {
	    		FileItem item = map.get(FILENAME_PARAM);
	    		String name = item.getName();
    			nameFiles.add(name);
	    		Card fileCard = createFileCard(sessionBean);
	    		ListAttribute primacy = (ListAttribute) fileCard.getAttributeById(primacyId);
	    		ReferenceValue ref = new ReferenceValue();

	    		ref.setId(map.containsKey(PRIMACY_PARAM) ? yesId : noId);
	    		primacy.setValue(ref);
	    		
	    		if(map.containsKey(MATERIALNAME_PARAM)){
		    		StringAttribute materialName = (StringAttribute) fileCard.getAttributeById(materialNameId);
		    		if (materialName!=null)
		    			materialName.setValue(map.get(MATERIALNAME_PARAM).getString("UTF-8"));
	    		}
	    		
	    		TextAttribute descr = (TextAttribute) fileCard.getAttributeById(descrId);
	    		descr.setValue(attr.getId().getId().toString());
	    		
	    		addFileToFileCard(fileCard, item, sessionBean);
	    		addFileCardToParent(parentCard, fileCard, sessionBean);
	    		
	    		attachmentCardIds.add(fileCard.getId().getId().toString());
	    		item.delete();
	    	}
	    	setConfigParameter(ATTR_KEY_NAMES_FILES, nameFiles, attr.getId(), sessionBean);
	    	return attachmentCardIds;
	    } catch (Exception e) {
			logger.error("Can not upload files", e);
			return attachmentCardIds;
	    }   
	}

	private void addFileCardToParent(Card parentCard, Card fileCard, CardPortletSessionBean sessionBean) {
       	CardLinkAttribute docLinks = parentCard.getCardLinkAttributeById(attr.getId());
		if (docLinks == null) {
			throw new IllegalArgumentException("Can't add link. Link attribute not found for card " +
					parentCard.getId().getId());
		}			
		docLinks.addLabelLinkedCard(fileCard);
		sessionBean.getActiveCardInfo().setAttributeEditorData(attr.getId(), AttributeEditor.KEY_VALUE_CHANGED, Boolean.TRUE);
	}

	private void addFileToFileCard(Card fileCard, FileItem item, CardPortletSessionBean sessionBean) throws IOException, DataException, ServiceException {
		UploadFile uploadAction = new UploadFile();
        uploadAction.setCardId(fileCard.getId());
        String filePath = item.getName();
        String fileName = filePath.substring((filePath.lastIndexOf("\\") + 1));
        uploadAction.setFileName(fileName);
        InputStream is = item.getInputStream();
        uploadAction.setData(is);

        try {
	        sessionBean.getServiceBean().doAction(uploadAction);        
	        MaterialAttribute attr = (MaterialAttribute)fileCard.getAttributeById(Attribute.ID_MATERIAL);
	        attr.setMaterialName(fileName);
	        attr.setMaterialType(MaterialAttribute.MATERIAL_FILE);
	        
	        StringAttribute name = (StringAttribute)fileCard.getAttributeById(Attribute.ID_NAME);
			name.setValue(fileName);		
			sessionBean.getServiceBean().saveObject(fileCard);
			
		} catch(Exception e) {
			logger.error("Can not add file to card " + fileCard.getId(), e);
		} finally {
			IOUtils.closeQuietly(is);
			sessionBean.getServiceBean().doAction(new UnlockObject(fileCard));
		}
	}

	private Card createFileCard(CardPortletSessionBean sessionBean) {
		CreateCard createCardAction = new CreateCard();
		createCardAction.setTemplate(TEMPLATE_FILE);
		Card fileCard = null;
			
		try {
			fileCard = (Card)sessionBean.getServiceBean().doAction(createCardAction);
			StringAttribute name = (StringAttribute)fileCard.getAttributeById(Attribute.ID_NAME);
			name.setValue("file");
			ObjectId fileCardId = sessionBean.getServiceBean().saveObject(fileCard, ExecuteOption.SYNC);
			fileCard.setId((Long)fileCardId.getId());	
		}catch(Exception e){
			logger.error("error creating new file card", e);
	    }
		return fileCard;
	}

	public static Object getConfigParameter(String key, ObjectId attrId, CardPortletSessionBean sessionBean) {
		return sessionBean.getActiveCardInfo().getAttributeEditorData(attrId, key);
	}
	
	public static void setConfigParameter(String key, Object value, ObjectId attrId, 
			CardPortletSessionBean sessionBean) {
		sessionBean.getActiveCardInfo().setAttributeEditorData(attrId, key, value);
	}
}
