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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.portlet.PortletFileUpload;

import com.aplana.dbmi.action.UploadFile;
import com.aplana.dbmi.card.CardPortletSessionBean.PortletMessage.PortletMessageType;
import com.aplana.dbmi.crypto.SignatureData;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.HtmlAttribute;
import com.aplana.dbmi.model.MaterialAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.AsyncDataServiceBean;
import com.aplana.dbmi.service.AsyncDataServiceBean.ExecuteOption;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ServiceException;

public class MaterialAttributeEditor extends JspAttributeEditor implements PortletForm
{
	public static final String ID_URL = "_url";
	public static final String UPLOAD_FILE_ACTION = "upload";
	
	public static final String UPLOAD_FORM_JSP = "/WEB-INF/jsp/html/MaterialUpload.jsp";
	public static final String FILE_PATH_FIELD = "path";
	
	public MaterialAttributeEditor() {
		setParameter(PARAM_JSP, "/WEB-INF/jsp/html/attr/Material.jsp");
	}

	public boolean doesFullRendering(Attribute attr) {
		return true;
	}

	public boolean gatherData(ActionRequest request, Attribute attr)
			throws DataException {
		MaterialAttribute mattr = (MaterialAttribute) attr;
        String url = request.getParameter(CardPortlet.getAttributeFieldName(attr) + ID_URL);
        if (url != null) {
            mattr.setMaterialType(MaterialAttribute.MATERIAL_URL);
            mattr.setMaterialName(url);
        }
		return true;
	}

	public boolean processAction(ActionRequest request, ActionResponse response,
			Attribute attr) throws DataException {
        String action = request.getParameter(CardPortlet.ACTION_FIELD);
        if (!UPLOAD_FILE_ACTION.equals(action))
        	return false;

        CardPortletSessionBean sessionBean = CardPortlet.getSessionBean(request);
		DateFormat dateFormat = DateFormat.getDateTimeInstance(SimpleDateFormat.MEDIUM, SimpleDateFormat.MEDIUM, request.getLocale());
        List<CertificateInfo> certificateInfos = CertificateInfo.readCertificateInfo(
        		sessionBean.getActiveCard(), sessionBean.getServiceBean(), sessionBean.getResourceBundle(), dateFormat);

        boolean valid = true;
		for (CertificateInfo cerInfo : certificateInfos) {
			if (!cerInfo.isSignValid()){
				valid = false;
				break;
			}
		}
        
        
        if (certificateInfos != null && certificateInfos.size()>0 &&  valid) {
			boolean dialogOk = CardPortlet.DIALOG_ACTION_OK.equals(request.getParameter(CardPortlet.DIALOG_ACTION_FIELD));
			if (sessionBean.getDialog() == null && !dialogOk){
				CardPortletDialog dialog = new CardPortletDialog();
				dialog.setTitle(sessionBean.getResourceBundle().getString("upload.dsfound.title"));
				dialog.setMessage(sessionBean.getResourceBundle().getString("upload.dsfound.message"));
				dialog.setCardPortletAction(action);
				sessionBean.setDialog(dialog);
			}
			else {
				sessionBean.setDialog(null);
	        	sessionBean.openForm(this);
			}
        }
        else {
        	sessionBean.openForm(this);
        }
        
		return true;
	}

	public void doFormView(RenderRequest request, RenderResponse response)
			throws IOException, PortletException {
		PortletRequestDispatcher rd = request.getPortletSession().getPortletContext()
				.getRequestDispatcher(UPLOAD_FORM_JSP);
		rd.include(request, response);
	}

	public void processFormAction(ActionRequest request, ActionResponse response)
			throws IOException, PortletException {
				
        if (PortletFileUpload.isMultipartContent(request)) {
            multipartContentHandle(request, response);
        } else {      	 	
        	
            final String action = request.getParameter(CardPortlet.ACTION_FIELD);
            CardPortletSessionBean sessionBean = CardPortlet.getSessionBean(request);            
        	if (CardPortlet.BACK_ACTION.equals(action)) {
        		sessionBean.closeForm();
        		// updating card because it may have been changed
        		final Card card = sessionBean.getActiveCard();
       			try {
       				final ObjectId cardId = card.getId();
       				if ((cardId != null)&&						 // (2009/12/10, RuSA) ������ ���� ���� id ����������� ��������
       					(!sessionBean.getActiveCardInfo().isOpenedInEditMode())) // (2010/03/19, MVA)� ������ ���� ��� �� � ������ �������������� 
       				{
       					// TODO: ��� ����� ����� ���-�� ������� ����� reloadCard
       					final Card reloadedCard = (Card) sessionBean.getServiceBean().getById(cardId);
       					sessionBean.getActiveCardInfo().setCard(reloadedCard);
       				}
       			} catch (Exception e) {
       				sessionBean.setMessageWithType("db.side.error.msg.param", new Object[] { e.getMessage() }, PortletMessageType.ERROR);
       			}
        	}
        }
	}

    private void multipartContentHandle(ActionRequest request, ActionResponse response) {
        FileItemFactory factory = new DiskFileItemFactory();
        PortletFileUpload upload = new PortletFileUpload(factory);
        String msg = "upload.error.msg";
        try {
            List items = upload.parseRequest(request);
            
            for (Iterator it = items.iterator(); it.hasNext();) {
                FileItem item = (FileItem) it.next();
                String name = item.getFieldName();                
                
                if (name.equalsIgnoreCase(FILE_PATH_FIELD)) {
                    if (item.getName() != null && !item.getName().equals("") && item.getSize() > 0) {
                        uploadFileHandler(request, item);
                        msg = "upload.success.msg";
                    }
                }else if(name.equalsIgnoreCase("fileSignature")){
                 	logger.debug("starting Signature");
                 	String signature = item.getString();
                 	logger.debug("Signature= " + signature);
                 	CardPortletSessionBean sessionBean = CardPortlet.getSessionBean(request);
                 	Card card = sessionBean.getActiveCard();                 
                 	HtmlAttribute attr = (HtmlAttribute) card.getAttributeById(ObjectId.predefined(HtmlAttribute.class, "jbr.uzdo.signature"));
                 	String stCardId = card.getId() == null ? "[NOT_DEFINED]" : card.getId().toString();  
                 	if(attr == null){
                         if ( logger.isDebugEnabled() ) {
                 		    logger.debug("attribute jbr.uzdo.signature not found in card " + stCardId);
                         }
                 	}else{	                 	
                 		//sessionBean.setAttributeEditorData(attr.getId(), SignatureData.AED_ATTACHSIGNATURE, signature);
                 		attr.setValue(signature);
                        if ( logger.isDebugEnabled() ) {
	                 	    logger.debug("saved signature in AttributeEditorData " + SignatureData.AED_ATTACHSIGNATURE);
                        }
                 	}
                }
            };
        } catch (Exception e) {
        	logger.error("Exception caught during processing of multipart request", e);
        }
        getCardPortletSessionBean(request).setMessage(msg, null);
    }

    private void uploadFileHandler(ActionRequest request, FileItem item) throws DataException, ServiceException, IOException {
        CardPortletSessionBean sessionBean = getCardPortletSessionBean(request);
        CardPortletCardInfo cardInfo = sessionBean.getActiveCardInfo();
        Card card = cardInfo.getCard();

        AsyncDataServiceBean serviceBean = sessionBean.getServiceBean();
        ObjectId cardId = serviceBean.saveObject(card, ExecuteOption.SYNC);            

        UploadFile uploadAction = new UploadFile();
        uploadAction.setCardId(cardId);
        String filePath = item.getName();
        String fileName = filePath.substring((filePath.lastIndexOf("\\") + 1));
        uploadAction.setFileName(fileName);
        uploadAction.setLength(Integer.parseInt("" + item.getSize()));
        uploadAction.setData(item.getInputStream());
        serviceBean.doAction(uploadAction);
        
        card = (Card)serviceBean.getById(cardId);
        cardInfo.setCard(card);
        cardInfo.setRefreshRequired(true);
    }

	protected Map getReferenceData(Attribute attr, PortletRequest request) {
		MaterialAttribute mattr = (MaterialAttribute)attr;
		Map result = new HashMap(1);
		result.put("fileMaterial", new Boolean(mattr.getMaterialType() != Card.MATERIAL_URL));
		return result;
	}
}
