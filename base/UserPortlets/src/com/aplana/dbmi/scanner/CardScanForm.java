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
package com.aplana.dbmi.scanner;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

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
import org.w3c.dom.Attr;

import com.aplana.dbmi.card.CardPortletSessionBean.PortletMessage.PortletMessageType;
import com.aplana.dbmi.crypto.SignatureData;
import com.aplana.dbmi.action.CreateCard;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.action.UploadFile;
import com.aplana.dbmi.action.SignAttachment;
import com.aplana.dbmi.card.AttributeEditor;
import com.aplana.dbmi.card.CardPortlet;
import com.aplana.dbmi.card.CardPortletSessionBean;
import com.aplana.dbmi.card.PortletForm;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.MaterialAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.HtmlAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.util.AttrUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ServiceException;



public class CardScanForm implements PortletForm {
	public static final String CARD_SCAN_JSP = "CardScan.jsp";
	private CardPortletSessionBean sessionBean;
	private static final String FILE_PATH_FIELD = "path";
	private String renderJSP = CARD_SCAN_JSP;
	public static final String ATTR_KEY_MODE = "mode";
	public static final String MODE_SELECTED = "modeSelected";
	public static final String TARGET_ATTR = "targetAttr";
	public static final String SRC_CARD_ID = "srcCardId";
	
	private static final ObjectId FILE_TEMPLATE_ID =
		ObjectId.predefined(Template.class, "jbr.file");
	private static final ObjectId DOCLINKS_ATTRIBUTE_ID =
		ObjectId.predefined(CardLinkAttribute.class, "jbr.files");
	
	ObjectId targetAttrId = DOCLINKS_ATTRIBUTE_ID;
	private ObjectId nameSrcAttr;
	
	public void doFormView(RenderRequest request, RenderResponse response)
			throws IOException, PortletException {
		PortletSession session = request.getPortletSession();
		request.getPortletSession().setAttribute(TARGET_ATTR, AttrUtils.getAttrTypeString(targetAttrId.getType())+":"+targetAttrId.getId());
		PortletRequestDispatcher rd =
			session.getPortletContext().getRequestDispatcher(CardPortlet.JSP_FOLDER + renderJSP);
		sessionBean = CardPortlet.getSessionBean(request);
		if(sessionBean.getActiveCard().getId() == null){
			if(nameSrcAttr != null){
				Attribute srcAttr = sessionBean.getActiveCard().getAttributeById(nameSrcAttr);
				if(srcAttr instanceof CardLinkAttribute){
					if(!srcAttr.isEmpty()){
						ObjectId singleId = ((CardLinkAttribute)srcAttr).getSingleLinkedId();
						request.getPortletSession().setAttribute(SRC_CARD_ID, ((CardLinkAttribute)srcAttr).getSingleLinkedId().getId().toString());
					}
				} else {
					if(!srcAttr.isEmpty()){
						request.getPortletSession().setAttribute(SRC_CARD_ID, ((CardLinkAttribute)srcAttr).getStringValue());
					}
				}
			} else {				
				request.getPortletSession().setAttribute(SRC_CARD_ID, "no_id");
			}
		} else {
			request.getPortletSession().setAttribute(SRC_CARD_ID, sessionBean.getActiveCard().getId().getId().toString());
		}
		rd.include(request, response);

	}
	
	public void setRenderJSP(String newValue){
		renderJSP = newValue;
	}
	
	public void setTargetAttrId(ObjectId targetAttrId) {
		this.targetAttrId = targetAttrId;
	}

	public void processFormAction(ActionRequest request, ActionResponse response)
			throws IOException, PortletException {
		try{
			sessionBean = CardPortlet.getSessionBean(request);
			
			String action = request.getParameter(CardPortlet.ACTION_FIELD);
			if (action != null && CardPortlet.BACK_ACTION.equals(action)) {
				CardPortlet.getSessionBean(request).closeForm();
			}else if (PortletFileUpload.isMultipartContent(request)) {
		            multipartContentHandle(request, response);
			} else if (request.getParameter("signature") != null){
				String signCards = request.getParameter("signCards");
				if (signCards != null && signCards.length()>0){
					String[] signCardIds = signCards.split(";");
					String[] signs = request.getParameter("signature").split(";");
					for (int i=0; i<signCardIds.length; i++){
						ObjectId signatureAttributeId = ObjectId.predefined(HtmlAttribute.class, "jbr.uzdo.signature");
						Card attachmentCard = (Card) sessionBean.getServiceBean().getById(new ObjectId(Card.class, signCardIds[i]));
						HtmlAttribute signAttr = (HtmlAttribute)attachmentCard.getAttributeById(signatureAttributeId);
						signAttr.setValue(signs[i]);
						SignAttachment signAttachmentAction = new SignAttachment();
						signAttachmentAction.setCard(attachmentCard);
						sessionBean.getServiceBean().doAction(signAttachmentAction);
					}
					sessionBean.setMessageWithType(sessionBean.getResourceBundle().getString("ds.attachments.success.msg"), PortletMessageType.EVENT);
				}
				sessionBean.getActiveCardInfo().getPortletFormManager().closeForm();
			} else if (request.getParameter("cardId") != null) {
				if (sessionBean.isDsSupport(request)){
					response.setRenderParameter("ds.need.sign", request.getParameter("cardId"));
				} else {
					CardPortlet.getSessionBean(request).closeForm();
				}
			}
		}catch(Exception ex){
			new PortletException("Can not processFormAction", ex);
		}
	}
	
	private void multipartContentHandle(ActionRequest request, ActionResponse response) {
       FileItemFactory factory = new DiskFileItemFactory();
       PortletFileUpload upload = new PortletFileUpload(factory);
        
        String msg = "upload.error.msg";
                       
        try {
        	Card fileCard = sessionBean.getActiveCard();
        	if(fileCard.getAttributeById(Attribute.ID_MATERIAL) == null){
            	//������� �� � ������� ��������, � ������� ����� �������� ����� � ����� � �������
            	fileCard = createFileCard();
            }
        	
            List items = upload.parseRequest(request);
            
            for (Iterator it = items.iterator(); it.hasNext();) {
                FileItem item = (FileItem) it.next();
                String name = item.getFieldName();                
                          
                if (name.equalsIgnoreCase(FILE_PATH_FIELD)) {
                    if (item.getName() != null && !item.getName().equals("") && item.getSize() > 0) {
                        
                        uploadFileHandler(request, item, fileCard);
                        msg = "upload.success.msg";
                    }
                }else if(name.equalsIgnoreCase("fileSignature")){
                 
                }
               
            };
            
            if (fileCard != null && fileCard.getId() != null 
            		&& fileCard.getId().equals(sessionBean.getActiveCard().getId())==false)
            	sessionBean.getServiceBean().doAction(new UnlockObject(fileCard));
            
         
        } catch (Exception e) {           
            e.printStackTrace();
        }      

    }
	
	private void uploadFileHandler(ActionRequest request, FileItem item, Card card) throws DataException, ServiceException, IOException {
                     
       	attachFile(card, item);        
       	Card activeCard = sessionBean.getActiveCard();
       	
       	if(card.getId().equals(activeCard.getId())==false){
	       	CardLinkAttribute docLinks = activeCard.getCardLinkAttributeById(DOCLINKS_ATTRIBUTE_ID);
			if (docLinks == null) {
				throw new IllegalArgumentException("Can't add link. Link attribute not found for card " +
						activeCard.getId().getId());
			}			
			docLinks.addLabelLinkedCard(card);
			sessionBean.getActiveCardInfo().setAttributeEditorData(DOCLINKS_ATTRIBUTE_ID, AttributeEditor.KEY_VALUE_CHANGED, Boolean.TRUE);
       	}
    }
	
	private void attachFile(Card card, FileItem item) throws DataException, ServiceException, IOException {
		ObjectId cardId = card.getId(); 
	    if (cardId == null) {
	         cardId = sessionBean.getServiceBean().saveObject(card);            
	         card.setId(((Long)cardId.getId()).longValue());
	    };   
	        
    	UploadFile uploadAction = new UploadFile();
        uploadAction.setCardId(card.getId());
        String filePath = item.getName();
        String fileName = filePath.substring((filePath.lastIndexOf("\\") + 1));
        uploadAction.setFileName(fileName);
        uploadAction.setLength(Integer.parseInt("" + item.getSize()));
        uploadAction.setData(item.getInputStream());

        sessionBean.getServiceBean().doAction(uploadAction);        
        MaterialAttribute attr = (MaterialAttribute)card.getAttributeById(Attribute.ID_MATERIAL);
        attr.setMaterialName(fileName);
        attr.setMaterialType(MaterialAttribute.MATERIAL_FILE);
        
        StringAttribute name = (StringAttribute)card.getAttributeById(Attribute.ID_NAME);
		name.setValue(fileName);		
		sessionBean.getServiceBean().saveObject(card);
    }
    
    private Card createFileCard() throws Exception {
		CreateCard createCardAction = new CreateCard();
		createCardAction.setTemplate(FILE_TEMPLATE_ID);
		Card fileCard = null;
		
		try {
			fileCard = (Card)sessionBean.getServiceBean().doAction(createCardAction);
			StringAttribute name = (StringAttribute)fileCard.getAttributeById(Attribute.ID_NAME);
			name.setValue("file");
			fileCard.setId((Long)sessionBean.getServiceBean().saveObject(fileCard).getId());								
		}catch(Exception e){
			e.printStackTrace();
			//logger.error("error creating new file card", e);
    	}
		return fileCard;
	}

	public void setNameSrcAttr(ObjectId nameSrcAttr) {
		// TODO Auto-generated method stub
		this.nameSrcAttr = nameSrcAttr;
	}
}
