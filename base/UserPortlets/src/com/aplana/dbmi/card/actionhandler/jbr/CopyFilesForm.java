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

import java.io.IOException;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import com.aplana.dbmi.action.SignAttachment;
import com.aplana.dbmi.card.CardPortlet;
import com.aplana.dbmi.card.CardPortletSessionBean;
import com.aplana.dbmi.card.CardPortletSessionBean.PortletMessage.PortletMessageType;
import com.aplana.dbmi.card.PortletForm;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.HtmlAttribute;
import com.aplana.dbmi.model.ObjectId;

public class CopyFilesForm implements PortletForm  {
	private static final String COPY_JSP = "copyFiles.jsp";
	
	public void doFormView(RenderRequest request, RenderResponse response)
			throws IOException, PortletException {
		PortletSession session = request.getPortletSession();	
		PortletRequestDispatcher rd =
			session.getPortletContext().getRequestDispatcher(CardPortlet.JSP_FOLDER + COPY_JSP);
		rd.include(request, response);
	}

	public void processFormAction(ActionRequest request, ActionResponse response)
			throws IOException, PortletException {
		try {
			CardPortletSessionBean sessionBean = CardPortlet.getSessionBean(request);
			if (request.getParameter("signature") != null){
				String signCards = request.getParameter("signIds");
				if (signCards != null && signCards.length()>0){
					response.setRenderParameter("checked.cards", signCards);
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
				response.setRenderParameter("forceDownload", "true");
			}
			if (request.getParameter("download") != null) {
				String checkedCards = request.getParameter("checkedCards");
				response.setRenderParameter("checked.cards", checkedCards);
				sessionBean.setMessageWithType(sessionBean.getResourceBundle().getString("download.with.sign.success"), PortletMessageType.EVENT);
			}
			String action = request.getParameter(CardPortlet.ACTION_FIELD);
			if (CardPortlet.BACK_ACTION.equals(action)) {		
				sessionBean.getActiveCardInfo().getPortletFormManager().closeForm();
			}
		} catch(Exception ex){
			new PortletException("Can not processFormAction", ex);
		}
	}

}
