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
import java.util.*;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.model.*;
import com.aplana.dbmi.module.docflow.GenerateVisaFromRout;

import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.PortletUtil;
import com.aplana.dbmi.service.ServiceException;

public class SavedNegotiationRoutes extends CardPortlet implements PortletForm{
	public static final String xmlNegotiationList = "dbmi/NegotiationList.xml";
	public static final String UPLOAD_FORM_JSP = "/WEB-INF/jsp/savedNegotiationRoutes.jsp";
    public static final ObjectId NEGOTIATION_LIST_TEMPLATE = ObjectId.predefined(Template.class, "jbr.negotiationlist.template");
    public static final ObjectId NEGOTIATION_LIST_NAME = ObjectId.predefined(StringAttribute.class, "name");
    public static final ObjectId NEGOTIATION_LIST_URGENCY_CATEGORY = ObjectId.predefined(ListAttribute.class, "jbr.negotiationlist.urgencyCategory");
    public static final ObjectId NEGOTIATION_LIST_DOCUMENT_TYPE = ObjectId.predefined(CardLinkAttribute.class, "jbr.negotiationlist.documentType");
    public static final ObjectId DOC_TYPE = ObjectId.predefined(CardLinkAttribute.class, "jbr.reg.doctype");
    public static final String NEGOTIATION_LIST_ID = "negotiation_list_id";
    public static final String NEGOTIATION_LIST_SUBMIT = "negotiation_list_submit";
    
    public void doFormView(RenderRequest request, RenderResponse response) throws PortletException, IOException {
    	try{
    		List<NegotiationListInfo> listInfo = new ArrayList<NegotiationListInfo>();
    		getNegotiationRoutesList(request, listInfo);
    		request.setAttribute("listInfo", listInfo);
    		PortletRequestDispatcher rd = request.getPortletSession().getPortletContext()
    		.getRequestDispatcher(UPLOAD_FORM_JSP);
    		rd.include(request, response);
    	
    	} catch (DataException e) {
    		logger.error(e.getMessage(), e);
		} catch (ServiceException e) {
			logger.error(e.getMessage(), e);
		}
	}
  
	public void processFormAction(ActionRequest request, ActionResponse response)throws IOException, PortletException {
    	final String action = request.getParameter(ACTION_FIELD);	
    	if (BACK_ACTION.equals(action)) {
    		CardPortletSessionBean sessionBeen = CardPortlet.getSessionBean(request);
    		//���������� ���������� ��������, �.�. ��������� �� ������������� �� ��
    		sessionBeen.getActiveCardInfo().setRefreshRequired(true);
    		backActionHandler(request, response);
    	} 
    	else if (NEGOTIATION_LIST_SUBMIT.equals(action)) {
    		try {
    			final ObjectId listId =  new  ObjectId(Card.class, Long.parseLong(request.getParameter(NEGOTIATION_LIST_ID)));
    			DataServiceBean serviceBean = PortletUtil.createService(request);
    			CardPortletSessionBean sessionBeen = CardPortlet.getSessionBean(request);
    			Card doc = sessionBeen.getActiveCard();
    			GenerateVisaFromRout gVisa = new GenerateVisaFromRout();
    			gVisa.setDocId(doc.getId());
    			gVisa.setRoutId(listId);
    			try{
    				serviceBean.doAction(gVisa);
    			}
    			catch (DataException e){
    				sessionBeen.setMessage(e.getMessage());
    			}
      		
    			//��������� ��������
    			CardPortletCardInfo parent = sessionBeen.getActiveCardInfo();
    			parent.setCard((Card) serviceBean.getById(doc.getId()));
    			parent.setRefreshRequired(true);
      		
    			backActionHandler(request, response);
    		} catch (DataException e) {
    			logger.error(e.getMessage(), e);
			} catch (ServiceException e) {
				logger.error(e.getMessage(), e);
			}
    	}
    }

	protected void backActionHandler(ActionRequest request, ActionResponse response)
			throws IOException {
		CardPortletSessionBean sessionBean = getSessionBean(request);
		sessionBean.getActiveCardInfo().getPortletFormManager().closeForm();
	}

	@SuppressWarnings("unchecked")
	public void getNegotiationRoutesList(RenderRequest request, List<NegotiationListInfo> listInfo) throws DataException, ServiceException, IOException {
		Search actionSearch = new Search();
  		DataServiceBean serviceBean 	= PortletUtil.createService(request);
  		CardPortletSessionBean sessionBeen = CardPortlet.getSessionBean(request);
  		Card doc = sessionBeen.getActiveCard();
 		Person curUser = new Person();
 		curUser = getSessionBean(request).getServiceBean().getPerson();
		actionSearch.clearAttributes();
		actionSearch.initFromXml(Portal.getFactory().getConfigService().loadConfigFile(xmlNegotiationList));
		
		
		CardLinkAttribute docTypeAttr = (CardLinkAttribute) doc.getAttributeById(DOC_TYPE);
		ObjectId docTypeValue = docTypeAttr == null ? null : docTypeAttr.getSingleLinkedId();
		
		if (docTypeValue != null){
			actionSearch.addCardLinkAttribute(NEGOTIATION_LIST_DOCUMENT_TYPE, docTypeValue);
		}
		//�.�. 20.12.2010. ���������, ���� �� ����� �� ������.
		actionSearch.getFilter().setCurrentUserRestrict(Search.Filter.CU_READ_PERMISSION);
		//
		SearchResult result = (SearchResult) serviceBean.doAction(actionSearch);
  		List<Card> cards = result.getCards();
  		Iterator<Card> iter = cards.iterator();
  		
  		String cardName;
  		String cardUrgancyCategory;
  		String cardDocumentType;
  		while (iter.hasNext()) {
  			NegotiationListInfo listInfoObject = new NegotiationListInfo();
  			Card negotiationListCard = (Card) iter.next();
  			cardName = ((StringAttribute) negotiationListCard.getAttributeById(NEGOTIATION_LIST_NAME)).getValue(); 
  			ListAttribute listCardUrgancyCategory = (ListAttribute) negotiationListCard.getAttributeById(NEGOTIATION_LIST_URGENCY_CATEGORY);
  			if (listCardUrgancyCategory !=null ){
  				cardUrgancyCategory = listCardUrgancyCategory.getStringValue();
  			} else {
  				cardUrgancyCategory = null;
  			}
  			CardLinkAttribute listCardDocumentType = (CardLinkAttribute) negotiationListCard.getAttributeById(NEGOTIATION_LIST_DOCUMENT_TYPE);
  			if (listCardDocumentType !=null ){
  				cardDocumentType = listCardDocumentType.getStringValue();
  			} else {
  				cardDocumentType = null;
  			}
  			listInfoObject.setListName(cardName);
  			listInfoObject.setListUrgancyCategory(cardUrgancyCategory);
  			listInfoObject.setListDocType(cardDocumentType);
  			listInfoObject.setListId(negotiationListCard.getId().getId().toString());
  			listInfo.add(listInfoObject);
  	      
  		}
  	}
 

}
