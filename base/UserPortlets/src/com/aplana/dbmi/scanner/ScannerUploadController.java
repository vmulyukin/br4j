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

import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;

import com.aplana.dbmi.action.CreateCard;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.action.UploadFile;
import com.aplana.dbmi.card.AttributeEditor;
import com.aplana.dbmi.card.CardPortlet;
import com.aplana.dbmi.card.CardPortletSessionBean;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.util.AttrUtils;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.UserPrincipal;

/**
 * <code>Controller</code> to attach scanned documents.
 *
 * @author <a href="mailto:ogalkin@aplana.com">Oleg Galkin</a>
 */
public class ScannerUploadController extends AbstractCommandController {	
	private static final ObjectId FILE_TEMPLATE_ID =
		ObjectId.predefined(Template.class, "jbr.file");
	private static final ObjectId MATERIAL_NAME_ID =
			ObjectId.predefined(StringAttribute.class, "jbr.materialName");
	private static final ObjectId IS_PRIME_ID =
			ObjectId.predefined(ListAttribute.class, "jbr.prime");
	public static final ObjectId noId = ObjectId.predefined(ReferenceValue.class, "jbr.incoming.control.no");

	
	/**
	 * Command object for <code>MultipartFile</code>.
	 */
	public static class MultipartFileBean {
		private MultipartFile file;

		public MultipartFile getFile() {
			return file;
		}
		public void setFile(MultipartFile file) {
			this.file = file;
		}
	}

	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.mvc.AbstractCommandController#handle(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
	 */
	protected ModelAndView handle(HttpServletRequest request,	HttpServletResponse response,
					Object command, BindException errors) throws Exception {
		final String namespace = ServletRequestUtils.getRequiredStringParameter(request, "namespace");
		final String targetAttr = ServletRequestUtils.getRequiredStringParameter(request, "targetAttr");
		logger.info(command);
		final MultipartFileBean bean = (MultipartFileBean)command;
		CardPortletSessionBean sessionBean = CardPortlet.getSessionBean(request, namespace);
		Card card = sessionBean.getActiveCard();
		final DataServiceBean dataService = initDataService(request);
		Card fileCard = attachFile(sessionBean, card, bean.getFile(), dataService, targetAttr);
		response.setContentType("text/plain");
		response.getWriter().println("success");
		response.addHeader("cardId",((Long)fileCard.getId().getId()).toString());
		return null;
	}
	
	private Card attachFile(CardPortletSessionBean sessionBean, Card card, MultipartFile file, DataServiceBean dataService, String targetAttr)
					throws Exception {
		CardLinkAttribute docLinks = card.getCardLinkAttributeById(AttrUtils.getAttributeId(targetAttr));
		if (docLinks == null) {
			throw new IllegalArgumentException("Can't add link. Link attribute not found for card " +
							card.getId().getId());
		}
		final Card fileCard = createFile(file, dataService);
		docLinks.addLabelLinkedCard(fileCard);
		sessionBean.getActiveCardInfo().setAttributeEditorData(AttrUtils.getAttributeId(targetAttr), AttributeEditor.KEY_VALUE_CHANGED, Boolean.TRUE);
		return fileCard;
	}
	
	private Card createFile(MultipartFile file, DataServiceBean dataService) throws Exception {
		CreateCard createCardAction = new CreateCard();
		createCardAction.setTemplate(FILE_TEMPLATE_ID);
		Card fileCard = null;
		InputStream in = null;
		try {
			// create new file
			fileCard = (Card)dataService.doAction(createCardAction);
			
			// set file attributes
			StringAttribute name = (StringAttribute)fileCard.getAttributeById(Attribute.ID_NAME);
			name.setValue(file.getOriginalFilename());
			StringAttribute materialName = (StringAttribute)fileCard.getAttributeById(MATERIAL_NAME_ID);
			materialName.setValue(file.getOriginalFilename());
			ListAttribute primacy = (ListAttribute) fileCard.getAttributeById(IS_PRIME_ID);
    		ReferenceValue ref = new ReferenceValue();
    		ref.setId(noId);
    		primacy.setValue(ref);
			fileCard.setId((Long)dataService.saveObject(fileCard).getId());
			
			UploadFile uploadFileAction = new UploadFile();
			uploadFileAction.setCardId(fileCard.getId());
			uploadFileAction.setFileName(file.getOriginalFilename());
			uploadFileAction.setLength((int)file.getSize());
			in = file.getInputStream();
			uploadFileAction.setData(in);
			dataService.doAction(uploadFileAction);
		} finally {
			IOUtils.closeQuietly(in);
			if (fileCard != null && fileCard.getId() != null)
				dataService.doAction(new UnlockObject(fileCard));
		}
		return fileCard;
	}
	
	private DataServiceBean initDataService(HttpServletRequest request) {
		DataServiceBean dataService = new DataServiceBean(request.getSession().getId());
		if (request.getSession().getAttribute(DataServiceBean.USER_NAME) != null) {
		    dataService.setUser(new UserPrincipal((String) request.getSession().getAttribute("userName")));
		    dataService.setIsDelegation(true);
		    dataService.setRealUser(request.getUserPrincipal());
		} else {
		    dataService.setUser(request.getUserPrincipal());
		}
		dataService.setAddress(request.getRemoteAddr());
		return dataService;
	}
}
