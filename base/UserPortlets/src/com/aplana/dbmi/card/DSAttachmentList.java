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
import java.util.ArrayList;
import java.util.Iterator;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.SignAttachment;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.crypto.*;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.HtmlAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ServiceException;

public class DSAttachmentList extends CardPortlet implements PortletForm{
	
	public static final String UPLOAD_FORM_JSP = "/WEB-INF/jsp/attachmentsSelection.jsp";
	public static final String ACTION_FIELD = "MI_ACTION_FIELD";
	public static final String BACK_ACTION = "MI_BACK_ACTION";
	public static final String SIGN_AND_CLOSE_ACTION = "MI_SIGN_AND_CLOSE_ACTION";
	public static final ObjectId ATTR_DOC_ATTACH = ObjectId.predefined(CardLinkAttribute.class, "jbr.files");
	public static final ObjectId IS_PRIME = ObjectId.predefined(ListAttribute.class, "jbr.prime");	
	public static final ObjectId YES = ObjectId.predefined(ReferenceValue.class, "jbr.incoming.control.yes");
	public static final ObjectId ACTIVE = ObjectId.predefined(CardState.class, "active");
	
	private static final ObjectId ATTR_SIGNATURE = ObjectId.predefined(HtmlAttribute.class, "jbr.uzdo.signature");

	
    private Log logger = LogFactory.getLog(getClass());
    
    public void doFormView(RenderRequest request, RenderResponse response) throws IOException, PortletException {
    	buildInfoDoc(request, response);
		PortletRequestDispatcher rd = request.getPortletSession().getPortletContext().getRequestDispatcher(UPLOAD_FORM_JSP);
		rd.include(request, response);
	}

	public void processFormAction(ActionRequest request, ActionResponse response) throws IOException, PortletException {
		CardPortletSessionBean sessionBean = getSessionBean(request);
		Card card = sessionBean.getActiveCard();
		
		final String action = request.getParameter(ACTION_FIELD);	
		if (BACK_ACTION.equals(action)) {
			backActionHandler(request, response);
		} else if (SIGN_AND_CLOSE_ACTION.equals(action)) {
			try {
				String[] params = (String[]) request.getParameterValues("checkbox");
				if(null != params && params.length > 0) {
					boolean isLocked = false;
					
					if(CARD_VIEW_MODE.equals(sessionBean.getActiveCardInfo().getMode())) {
						sessionBean.getServiceBean().doAction(new LockObject(card.getId()));
						isLocked = true;
					}

		           	for (int i = 0; i < params.length; i++){
		        		ObjectId attachmentId = new ObjectId(Card.class, params[i]);
		        		Card attachmentCard = (Card) sessionBean.getServiceBean().getById(attachmentId);
		        		sessionBean.getServiceBean().doAction(new LockObject(attachmentCard.getId()));
		        		HtmlAttribute signatureAttribute = (HtmlAttribute) attachmentCard.getAttributeById(ATTR_SIGNATURE);
		        		String signature = signatureAttribute.getValue();
		        		if(signature == null) signature = "";
		        		//����������� ����� ����
						signatureAttribute.setValue(signature + request.getParameter(params[i] + "_Signature"));					
						SignAttachment signAttachmentAction = new SignAttachment();
						signAttachmentAction.setCard(attachmentCard);
						sessionBean.getServiceBean().doAction(signAttachmentAction);
						sessionBean.getServiceBean().doAction(new UnlockObject(attachmentCard.getId()));
		        	}
		           	
		           	if(isLocked) {
						sessionBean.getServiceBean().doAction(new UnlockObject(card.getId()));
					}
		           	setMessage(sessionBean, "ds.attachments.success.msg");
				}
				backActionHandler(request, response);
			} catch (DataException e) {
				logger.equals(e);
				setMessage(sessionBean, "db.side.error.msg");
			} catch (ServiceException e) {
				logger.equals(e);
				setMessage(sessionBean, "db.side.error.msg");
			}
		}
	}
	
	/*
	private void signCardHandler(ActionRequest request) throws DataException, ServiceException {
		CardPortletSessionBean sessionBean = getSessionBean(request);
		Card card = sessionBean.getActiveCard();
		String msg = "������� �����������";
		boolean isLocked = false;
		
		try {
			if(CARD_VIEW_MODE.equals(sessionBean.getActiveCardInfo().getMode())) {
				sessionBean.getServiceBean().doAction(new LockObject(card.getId()));
				isLocked = true;
				card = (Card) sessionBean.getServiceBean().getById(sessionBean.getActiveCard().getId());
				HtmlAttribute signatureAttribute = (HtmlAttribute) card.getAttributeById(ATTR_SIGNATURE);
				signatureAttribute.setValue(request.getParameter(CardPortlet.getAttributeFieldName(signatureAttribute)));
			}
			
			
			ValidateMandatoryAttributes validationAction = new ValidateMandatoryAttributes();
			validationAction.setCard(card);
			sessionBean.getServiceBean().doAction(validationAction);
			
			SignCard signCardAction = new SignCard();
			signCardAction.setCard(card);
			sessionBean.getServiceBean().doAction(signCardAction);
			
			if(isLocked) {
				sessionBean.getServiceBean().doAction(new UnlockObject(card.getId()));
			}
		} catch (Exception e) {
			if(isLocked) {
				sessionBean.getServiceBean().doAction(new UnlockObject(card.getId()));
			}
			sessionBean.setMessage("db.side.error.msg");
		}
		sessionBean.setMessage(msg);
	}
	*/
	
	protected void backActionHandler(ActionRequest request, ActionResponse response) throws IOException {
		CardPortletSessionBean sessionBean = getSessionBean(request);
		sessionBean.getActiveCardInfo().getPortletFormManager().closeForm();
	}

	private void buildInfoDoc(RenderRequest request, RenderResponse response) {
  		ArrayList<AttachInfo> attInfo = new ArrayList<AttachInfo>(); 
  		CardPortletSessionBean sessionBeen = CardPortlet.getSessionBean(request);
        Card doc = sessionBeen.getActiveCard();
  		try {
  			CardLinkAttribute linkRes = (CardLinkAttribute) doc.getAttributeById(ATTR_DOC_ATTACH);
  			if (linkRes != null) {
  				Iterator<?> iterAttach = linkRes.getIdsLinked().iterator();  				
  				while (iterAttach.hasNext()) {  					
  					
  					ObjectId attachId = (ObjectId) iterAttach.next();
  	  				Card attachmentCard = (Card) sessionBeen.getServiceBean().getById(attachId);
  	  				if(attachmentCard.getState().equals(ACTIVE)) {
  	  				
	  	  				String attachName = ((StringAttribute) attachmentCard.getAttributeById(ATTR_NAME)).getValue();
	  	  				SignatureConfig sConf = new SignatureConfig(sessionBeen.getServiceBean(), attachmentCard);
	  	  				SignatureData sData = new SignatureData(sConf, attachmentCard);
	  	  				boolean isPrime = ((ListAttribute) attachmentCard.getAttributeById(IS_PRIME)).getValue().getId().equals(YES);
	  	  			
	  	  				AttachInfo attInfoObject= new AttachInfo();
	  	  				attInfoObject.setAttachText(attachName);
	  	  				attInfoObject.setAttId(attachId.getId().toString());
	  	  				attInfoObject.setAttrXML(sData.getAttrXML());
	  	  				attInfoObject.setHash(sData.getAttrValues(sessionBeen.getServiceBean(), true, null));
	  	  				attInfoObject.setData(sData.getAttrValues(sessionBeen.getServiceBean(), false, response.encodeURL(request.getContextPath() + "/MaterialDownloadServlet?" + MaterialDownloadServlet.PARAM_CARD_ID + "=")));
	  	  				attInfoObject.setPrime(isPrime);
	  	  				
	  	  				attInfo.add(attInfoObject);
  	  				}
  				}
  				request.setAttribute("resInfo", attInfo);

  			}
  		} catch (Exception e) {
  			logger.error("Error in run CourseExecutionHandler when get data from resolutions and reports", e);
  		}
  	}
 
	private void setMessage(CardPortletSessionBean sessionBean, String key) {
		sessionBean.setMessage(sessionBean.getResourceBundle().getString(key));
	}

}