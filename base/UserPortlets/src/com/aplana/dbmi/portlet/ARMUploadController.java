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

import java.io.InputStream;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.springframework.validation.BindException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import com.aplana.dbmi.action.CreateCard;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.action.UploadFile;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.MaterialAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.UserPrincipal;

public class ARMUploadController extends SimpleFormController {	
	private static final ObjectId FILE_TEMPLATE_ID =
		ObjectId.predefined(Template.class, "jbr.file");
	private static final ObjectId DOCLINKS_ATTRIBUTE_ID =
		ObjectId.predefined(CardLinkAttribute.class, "jbr.files");
	
	private static final ObjectId materialNameId = ObjectId.predefined(StringAttribute.class, "jbr.materialName");
	private static final ObjectId primacyId = ObjectId.predefined(ListAttribute.class, "jbr.prime");
	public static final ObjectId noId = ObjectId.predefined(ReferenceValue.class, "jbr.incoming.control.no");
	
	private DataServiceBean dataService;
		
	/**
	 * Command object for <code>MultipartFile</code>.
	 */
	public static class MultipartFileBean { 
		private MultipartFile path;

		public MultipartFile getPath() {
			return path;
		}
		public void setPath(MultipartFile path) {
			this.path = path;
		}
	}

	/* (non-Javadoc)
	 * @see org.springframework.web.servlet.mvc.AbstractCommandController#handle(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, org.springframework.validation.BindException)
	 */
	protected ModelAndView onSubmit(HttpServletRequest request,	HttpServletResponse response,
					Object command, BindException errors) throws Exception {
		initDataService(request);
		final MultipartFileBean bean = (MultipartFileBean) command;
		
		String cardId = request.getParameter("cardId");
		Card card = null;
		if(null != cardId && cardId.length() > 0) {
			card = (Card)dataService.getById(new ObjectId(Card.class, Long.parseLong(cardId)));
		}
		
		if(null != card) {
			try {
				dataService.doAction(new LockObject(card));
			} catch (Exception e) {
				logger.error("Couldn't lock card " + card.getId().toString(), e);
				return null;
			}
		}
		
		Card fileCard = attachFile(card, bean.getPath());
		
		if(null != card) {
			try {
				dataService.saveObject(card);
				dataService.doAction(new UnlockObject(card));
			} catch (Exception e) {
				logger.error("Couldn't save or unlock card " + card.getId().toString(), e);
				return null;
			}
		}
		
		JSONObject result  = new JSONObject();
		result.put("id", fileCard.getId().getId());
		result.put("name", fileCard.getAttributeById(Attribute.ID_NAME).getStringValue());
		
		response.setContentType("text/javascript");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().print(result.toString());
		
		return null;
	} 
	
	private Card attachFile(Card card, MultipartFile file) throws Exception {
		CardLinkAttribute docLinks = null;
		if(null != card) {
			docLinks = card.getCardLinkAttributeById(DOCLINKS_ATTRIBUTE_ID);
			if (docLinks == null) {
				throw new IllegalArgumentException("Can't add link. Link attribute not found for card " +
							card.getId().getId());
			}
		}
		
		final Card fileCard = createFile(file);
		
		if(null != card) {
			docLinks.addLabelLinkedCard(fileCard);		
		}
		
		return fileCard;
	}
	
	private Card createFile(MultipartFile file) throws Exception {
		CreateCard createCardAction = new CreateCard();
		createCardAction.setTemplate(FILE_TEMPLATE_ID);
		Card fileCard = null;
		InputStream in = null;
		try {
			// create new file
			fileCard = (Card) dataService.doAction(createCardAction);
			
			// set file attributes
			StringAttribute name = (StringAttribute) fileCard.getAttributeById(Attribute.ID_NAME);
			String theFileName = URLDecoder.decode(file.getOriginalFilename(), "UTF-8");
			int lastIndexOfBackslash = theFileName.lastIndexOf("\\");
			if (lastIndexOfBackslash > -1) {
				theFileName = theFileName.substring(lastIndexOfBackslash + 1);
			}
			
			//String theFileName = file.getOriginalFilename();
			name.setValue(theFileName);
			
			StringAttribute materialName = (StringAttribute) fileCard.getAttributeById(materialNameId);
			materialName.setValue(theFileName);
			ListAttribute primacy = (ListAttribute) fileCard.getAttributeById(primacyId);
    		ReferenceValue ref = new ReferenceValue();
    		ref.setId(noId);
    		primacy.setValue(ref);
			
			fileCard.setId((Long) dataService.saveObject(fileCard).getId());
			
			UploadFile uploadFileAction = new UploadFile();
			uploadFileAction.setCardId(fileCard.getId());
			uploadFileAction.setFileName(theFileName);
			in = file.getInputStream();
			uploadFileAction.setData(in);
			dataService.doAction(uploadFileAction);
			
			MaterialAttribute attr = (MaterialAttribute)fileCard.getAttributeById(Attribute.ID_MATERIAL);
	        attr.setMaterialName(theFileName);
	        attr.setMaterialType(MaterialAttribute.MATERIAL_FILE);
	        dataService.saveObject(fileCard);
	        fileCard = (Card) dataService.getById(fileCard.getId());
		} finally {
			IOUtils.closeQuietly(in);
			if (fileCard != null && fileCard.getId() != null) {
				dataService.doAction(new UnlockObject(fileCard));
			}
		}
		return fileCard;
	}
	
	private void initDataService(HttpServletRequest request) {
		dataService = new DataServiceBean(request.getSession().getId());
		if (request.getSession().getAttribute(DataServiceBean.USER_NAME) != null) {
		    dataService.setUser(new UserPrincipal((String) request.getSession().getAttribute(DataServiceBean.USER_NAME)));
		    dataService.setIsDelegation(true);
		    dataService.setRealUser(request.getUserPrincipal());
		} else {
		    dataService.setUser(request.getUserPrincipal());
		}
		dataService.setAddress(request.getRemoteAddr());
	}
}