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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletSecurityException;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.util.StringUtils;

import com.aplana.dbmi.action.ListProject;
import com.aplana.dbmi.card.FilesAndCommentsUtils;
import com.aplana.dbmi.card.FilesAndCommentsUtils.RoundData;
import com.aplana.dbmi.card.FilesAndCommentsUtils.RoundDataFiles;
import com.aplana.dbmi.card.FilesAndCommentsUtils.RoundVisaData;
import com.aplana.dbmi.card.util.ARMUtils;
import com.aplana.dbmi.card.util.CardAttributesComparator;
import com.aplana.dbmi.card.util.SearchUtils;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.IntegerAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.TypedCardLinkAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.PortletUtil;
import com.aplana.dbmi.service.ServiceException;

/**
 * Represents portlet for displaying document data details (attachments, print form, linked documents).
 * 
 * @author EStatkevich
 */
public class DocumentDataPortlet extends GenericPortlet {
		
	public static final String EMPTY_STRING = "";
	public static final String FIELD_CARD_ID = "cardId";
	public static final String VIEW = "/WEB-INF/jsp/documentData.jsp";
	public static final String SESSION_BEAN = "documentDataPortletSessionBean";
	
	private static final ObjectId ATTR_MATERIALS = ObjectId.predefined(CardLinkAttribute.class, "jbr.files");
	private static final ObjectId ATTR_INFO_MATERIALS = ObjectId.predefined(CardLinkAttribute.class, "jbr.info.files");
	private static final ObjectId ATTR_LINKED_FROM = ObjectId.predefined(BackLinkAttribute.class, "jbr.doclinks.references");
	private static final ObjectId ATTR_LINKED_TO = ObjectId.predefined(TypedCardLinkAttribute.class, "jbr.relatdocs");
	private static final ObjectId ATTR_IS_PRIME = ObjectId.predefined(ListAttribute.class, "jbr.prime");
	private static final ObjectId ATTR_VISA_RND = ObjectId.predefined(IntegerAttribute.class, "jbr.visa.round");
	private static final ObjectId ATTR_FILE_RND = ObjectId.predefined(IntegerAttribute.class, "jbr.version");
	private static final ObjectId deloState = ObjectId.predefined(CardState.class, "delo");
	private static final Long REF_VALUE_YES = Long.valueOf(1432l);
	
	//���� ����������� �������� ��������� ��������
	private static final boolean HIDE_LINKED_DOCS_ATTACHEMENTS = false;
	
	
	protected Log logger = LogFactory.getLog(getClass());
	
	@Override
	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, PortletSecurityException, IOException {
		DocumentDataPortletSessionBean sessionBean = getSessionBean(request);
		request.setAttribute("attachments", sessionBean.getAttachments() != null ? sessionBean.getAttachments() : new JSONArray());
		request.setAttribute("infomaterials", sessionBean.getInfoMaterials() != null ? sessionBean.getInfoMaterials() : new JSONArray());
		request.setAttribute("linkedFromDocs", sessionBean.getLinkedFromDocs() != null ? sessionBean.getLinkedFromDocs() : new JSONArray());
		request.setAttribute("linkedToDocs", sessionBean.getLinkedToDocs() != null ? sessionBean.getLinkedToDocs() : new JSONArray());
		request.setAttribute("template", sessionBean.getBaseCard() != null ? sessionBean.getBaseCard().getTemplateName() : EMPTY_STRING);
		response.setContentType("text/html");
		PortletRequestDispatcher rd = getPortletContext().getRequestDispatcher(VIEW);
		rd.include(request, response);
	}
	
	@Override
	public void processAction(ActionRequest request, ActionResponse response) throws PortletException, PortletSecurityException, IOException {
		DocumentDataPortletSessionBean sessionBean = null;		
		try {
			sessionBean = prepareSessionBean(request, response);
		} catch (DataException e) {
			sessionBean = getSessionBean(request);
			if (null != sessionBean) {
				String theMsg = e.getMessage();
				if (DataException.ID_GENERAL_ACCESS.equals(e.getMessageID())) {
					theMsg = getResourceBundle(request.getLocale()).getString("msg.noAccess");
				}
				sessionBean.setMessage(theMsg);
			}
			logger.error(e);
		} catch (ServiceException e) {
			sessionBean = getSessionBean(request);
			if (null != sessionBean) {
				sessionBean.setMessage(e.getMessage());
			}
			logger.error(e);
		}
	}

	/**
	 * Fills session bean with the necessary data. 
	 * 
	 * @param request
	 * @param response
	 * @return
	 * @throws DataException
	 * @throws ServiceException
	 * @throws PortletException
	 */
	protected DocumentDataPortletSessionBean prepareSessionBean(ActionRequest request, ActionResponse response) throws DataException, ServiceException, PortletException {
		final DocumentDataPortletSessionBean sessionBean = new DocumentDataPortletSessionBean();
		final PortletSession session = request.getPortletSession();
		session.setAttribute(SESSION_BEAN, sessionBean);

		final DataServiceBean serviceBean = PortletUtil.createService(request);
		sessionBean.setServiceBean(serviceBean);
		
		StringBuilder theHeader = new StringBuilder(getResourceBundle(request.getLocale()).getString("header"));
		
		String cardId = request.getParameter(FIELD_CARD_ID);
		if (StringUtils.hasLength(cardId)) {
			sessionBean.setDocId(cardId);
			final ObjectId baseCardId = new ObjectId(Card.class, Long.valueOf(cardId).longValue());
			final Card baseCard = (Card) sessionBean.getServiceBean().getById(baseCardId);
			sessionBean.setBaseCard(baseCard);
			
			StringAttribute  docNameAttr = (StringAttribute) baseCard.getAttributeById(ARMUtils.ATTR_NAME);
			if(docNameAttr != null && StringUtils.hasLength(docNameAttr.getValue())) {
				theHeader.append(": ");
				theHeader.append(docNameAttr.getValue());
			}
			
			//sessionBean.setAttachments(getAttachmentsJSONData(baseCard, serviceBean));
			
            JSONArray array = null;
            //array = getAttachmentsJSONDataOld(baseCard, serviceBean);
            array = getAttachmentsJSONData(baseCard, serviceBean, ATTR_MATERIALS);
            sessionBean.setAttachments(array);
            sessionBean.setInfoMaterials(getAttachmentsJSONData(baseCard, serviceBean, ATTR_INFO_MATERIALS));
            
            
			List<Card> linkedFromDocs = getLinkedDocsFromBackLink(baseCardId, ATTR_LINKED_FROM, serviceBean);
			sessionBean.setLinkedFromDocs(getLinkedDocsJSONData(sessionBean, linkedFromDocs, false));

			List<Card> linkedToDocs = getLinkedToDocs(sessionBean);
			sessionBean.setLinkedToDocs(getLinkedDocsJSONData(sessionBean, linkedToDocs, true));
			
			if(baseCard.getState().equals(deloState)){
				CardLinkAttribute docHistoryAttribute = (ObjectId.predefined(CardLinkAttribute.class, "jbr.docHistory")!=null)?
						(CardLinkAttribute)baseCard.getAttributeById(ObjectId.predefined(CardLinkAttribute.class, "jbr.docHistory")):null;
					if (docHistoryAttribute!=null&&!docHistoryAttribute.getIdsLinked().isEmpty()) {
						String report_card_id = docHistoryAttribute.getSingleLinkedId().getId().toString();
								
						String downloadServletPath = "/DBMI-UserPortlets/MaterialDownloadServlet?";
						String reportURL = downloadServletPath+"MI_CARD_ID_FIELD="+report_card_id+"&noname=1&pdf=1";
						sessionBean.setArchiveReportURL(reportURL);
					}
					sessionBean.setDeloState(true);
			} 
		}

		sessionBean.setHeader(theHeader.toString());
		return sessionBean;
	}

	/**
	 * Retrieves link's type to the linked document. 
	 * 
	 * @param sessionBean
	 * @param linkedToDocId
	 * @return
	 * @throws DataException
	 * @throws ServiceException
	 */
	private String retrieveLinkType(DocumentDataPortletSessionBean sessionBean, ObjectId linkedToDocId) throws DataException, ServiceException {
		String theLinkType = EMPTY_STRING;
		DataServiceBean serviceBean = sessionBean.getServiceBean();
		TypedCardLinkAttribute linkedToDocsAttr = (TypedCardLinkAttribute) sessionBean.getBaseCard().getAttributeById(ATTR_LINKED_TO);
		ObjectId cardType = linkedToDocsAttr.getCardType((ObjectId) linkedToDocId);
		if (cardType != null) {
			ReferenceValue refVal = (ReferenceValue) serviceBean.getById(cardType);
			if (refVal != null) {
				theLinkType = refVal.getValueRu();
			}
		}
		return theLinkType;
	}
	
	private JSONArray getAttachmentsJSONData(Card baseCard, DataServiceBean serviceBean, ObjectId attachmentsAttrId) throws DataException, ServiceException{
		JSONArray attachmentsArray = new JSONArray();
		CardLinkAttribute materialsAttribute = (CardLinkAttribute) baseCard.getAttributeById(attachmentsAttrId);
		RoundDataFiles roundDataFiles = loadAttachmentsValues(baseCard, materialsAttribute, serviceBean);
		if(roundDataFiles!=null){
			sortRoundDataFiles(roundDataFiles);
			attachmentsArray = ARMUtils.getAttachmentsJSONData(roundDataFiles);
		}else{
			attachmentsArray = getAttachmentsJSONDataOld(baseCard, serviceBean,attachmentsAttrId);
		}		
		return attachmentsArray;
	} 
	
	/**
	 * Retrieves attachments of the given card.
	 * 
	 * @param baseCard
	 * @param serviceBean
	 * @return
	 * @throws DataException
	 * @throws ServiceException
	 */
	private JSONArray getAttachmentsJSONDataOld(Card baseCard, DataServiceBean serviceBean, ObjectId attachmentsAttrId) throws DataException, ServiceException {
		JSONArray attachmentsArray = new JSONArray();
		List<Card> attachments = getAttachments(baseCard, serviceBean,attachmentsAttrId);
		List<List<Card>> allFiles = new ArrayList<List<Card>>();
		parseAttachments(baseCard, allFiles, attachments);
		try{
			JSONObject jsonObject = null;
			for (List<Card> iterFiles : allFiles) {
				if (iterFiles.size() > 1) {
					sortCards(iterFiles);
				}
				jsonObject = new JSONObject();
				jsonObject.put("parrent", ARMUtils.getAttachmentsJSONData(iterFiles));
				attachmentsArray.put(jsonObject);
			}
		} catch (JSONException e) {
			logger.error("JSON error", e);
			throw new DataException(e);
		} 
		return attachmentsArray;
	}

	private void parseAttachments(Card baseCard, List<List<Card>> allFiles, List<Card> attachments) {
		int itersNr = 1;

		IntegerAttribute visaRoundAttr = (IntegerAttribute) baseCard.getAttributeById(ATTR_VISA_RND);
		if (visaRoundAttr != null) {
			if (visaRoundAttr.getValue() > itersNr) {
				itersNr = visaRoundAttr.getValue();
			}
		}

		allFiles.add(new ArrayList<Card>());
		for (int i = 0; i < itersNr - 1; i++) {
			allFiles.add(new ArrayList<Card>());
		}

		for (Card attachment : attachments) {
			IntegerAttribute fileRoundAttr = (IntegerAttribute) attachment.getAttributeById(ATTR_FILE_RND);
			if (fileRoundAttr == null || fileRoundAttr.getValue() == 0) {
				allFiles.get(0).add(attachment);
			} else {
				int fileRound = fileRoundAttr.getValue();
				if (fileRound <= itersNr) {
					allFiles.get(fileRound - 1).add(attachment);
				}
			}
		}
	}
	
	/**
	 * Retrieves attachments of the given card.
	 * 
	 * @param baseCard
	 * @param serviceBean
	 * @return
	 * @throws DataException
	 * @throws ServiceException
	 */
	private List<Card> getAttachments(Card baseCard, DataServiceBean serviceBean, ObjectId attachmentsAttrId) throws DataException, ServiceException {
		List<Card> attachmentsList = new ArrayList<Card>();
		CardLinkAttribute materialsAttribute = (CardLinkAttribute) baseCard.getAttributeById(attachmentsAttrId);
		//loadAttachmentsValues(baseCard, materialsAttribute, serviceBean);
		if (materialsAttribute != null) {
			for(Object attachmentId : materialsAttribute.getIdsLinked()) {
				Card attachment = null;
				try{
					attachment = (Card) serviceBean.getById((ObjectId) attachmentId);
				} catch (DataException e){
					continue;
				}
				
				attachmentsList.add(attachment);
			}
		}
		return attachmentsList;
	}
	
	public RoundDataFiles loadAttachmentsValues(Card baseCard, Attribute attr, DataServiceBean serviceBean) throws DataException{
		RoundDataFiles roundDataFiles = null;		
		try {
			FilesAndCommentsUtils utils = new FilesAndCommentsUtils(baseCard, attr, serviceBean);
			if(!utils.isRoundExists()){
				return null;
			}
			roundDataFiles = utils.loadLinkedData(HIDE_LINKED_DOCS_ATTACHEMENTS);
			logger.info("������ ��������");
		} catch (Exception e) {			
			logger.error(e);	
			throw new DataException(e);
		}
		return roundDataFiles;
	}
	
	/**
	 * Finds Id of the primary attachment.
	 * 
	 * @param baseCard
	 * @param serviceBean
	 * @return prime attachment id, or the id of the first element, or "" if there is no attachments.
	 * @throws DataException
	 * @throws ServiceException
	 */
	private String getPrimeAttachmentId(Card baseCard, DataServiceBean serviceBean, ObjectId materialsAttrId) throws DataException, ServiceException {
		String theId = EMPTY_STRING;
		List<Card> attachmentsList = getAttachments(baseCard, serviceBean,materialsAttrId);
		if (attachmentsList != null && attachmentsList.size() > 0) {
			for (Card attachment : attachmentsList) {
				if (!StringUtils.hasLength(theId)) {
					theId = attachment.getId().getId().toString();
				}
				ListAttribute primacy = (ListAttribute) attachment.getAttributeById(ATTR_IS_PRIME);
				if (primacy != null) {
					ReferenceValue primacyRefValue = primacy.getValue();
					if (primacyRefValue != null) {
						if (REF_VALUE_YES.equals(primacyRefValue.getId().getId())) {
							theId = attachment.getId().getId().toString();
							break;
						}
					}
				}
			}
		}
		return theId;
	}
	
	/**
	 * Retrieves cards that refer to the given one.
	 * 
	 * @param cardId
	 * @param attrId
	 * @param serviceBean
	 * @return List<Card>
	 */
	private List<Card> getLinkedDocsFromBackLink(ObjectId cardId, ObjectId attrId, DataServiceBean serviceBean) {
		List<Card> cards = new ArrayList<Card>();
		try {
			final ListProject search = new ListProject();
			search.setAttribute(attrId);
			search.setCard(cardId);

			List<Card> foundCards = SearchUtils.execSearchCards(search, serviceBean);
			if (foundCards != null && foundCards.size() > 0) {
				for (Card theCard : foundCards) {
					fetchCard(serviceBean, cards, theCard.getId());
				}
			}
		} catch (Exception e) {
			logger.error("Error in get cardId from backlink: " + e);
		}
		return cards;
	}

	/**
	 * Retrieves card from DB for the given Id, if the user can access this card (if not - just skip it), and adds it to the result list.
	 * 
	 * @param serviceBean
	 * @param cards
	 * @param anId
	 */
	private void fetchCard(DataServiceBean serviceBean, List<Card> cards, ObjectId anId) {
		try {
			// add fetched card to list if access allowed, otherwise skip it
			cards.add((Card) serviceBean.getById(anId));
		} catch (DataException e) {
			logger.error("Error in getting linked document with id=" + anId.getId() + " Exception:" + e);
		} catch (ServiceException e) {
			logger.error("Error in getting linked document with id=" + anId.getId() + " Exception:" + e);
		}
	}
	
	/**
	 * Retrieves cards that are referenced by the given one.
	 * 
	 * @param sessionBean
	 * @return List<Card>
	 * @throws DataException
	 * @throws ServiceException
	 */
	private List<Card> getLinkedToDocs(DocumentDataPortletSessionBean sessionBean) throws DataException, ServiceException {
		List<Card> cards = new ArrayList<Card>();
		DataServiceBean serviceBean = sessionBean.getServiceBean();
		TypedCardLinkAttribute linkedToDocsAttr = (TypedCardLinkAttribute) sessionBean.getBaseCard().getAttributeById(ATTR_LINKED_TO);
		for (Object docId : linkedToDocsAttr.getIdsLinked()) {
			fetchCard(serviceBean, cards, (ObjectId) docId);
		}
		return cards;
	}
	
	/**
	 * Retrieves session bean from session.
	 * 
	 * @param request
	 * @return DocumentDataPortletSessionBean
	 */
	protected DocumentDataPortletSessionBean getSessionBean(PortletRequest request) {
		PortletSession session = request.getPortletSession();
		return (DocumentDataPortletSessionBean) session.getAttribute(SESSION_BEAN);
	}
	
	/**
	 * Collects data from linked documents into array.
	 * 
	 * @param sessionBean
	 * @param linkedDocs
	 * @param isLinkedTo
	 * @return JSONArray
	 * @throws DataException
	 * @throws ServiceException
	 */
	public JSONArray getLinkedDocsJSONData(DocumentDataPortletSessionBean sessionBean, List<Card> linkedDocs, boolean isLinkedTo) throws DataException, ServiceException {
		JSONArray docsArray = new JSONArray();
		if (linkedDocs != null && linkedDocs.size() > 0) {
			try {
				for (Card card : linkedDocs) {
					JSONObject linkedDoc = new JSONObject();
					if (isLinkedTo) {
						linkedDoc.put("linkType", retrieveLinkType(sessionBean, card.getId()));
					}
					StringAttribute nameAttribute = (StringAttribute) card.getAttributeById(ARMUtils.ATTR_NAME);
					if (nameAttribute != null) {
						linkedDoc.put("name", nameAttribute.getValue());
					}
					linkedDoc.put("cardId", card.getId().getId().toString());
					linkedDoc.put("primeAttachmentId", getPrimeAttachmentId(card, sessionBean.getServiceBean(),ATTR_MATERIALS));
					docsArray.put(linkedDoc);
				}
			} catch (JSONException e) {
				logger.error("JSON error", e);
				throw new DataException(e);
			}
		}
		return docsArray;
	}
	
	private void sortCards(List<Card> iterFiles){
		List<ObjectId> sortAttributes = new LinkedList<ObjectId>();
		sortAttributes.add(ATTR_IS_PRIME);
//		sortAttributes.add(ARMUtils.ATTR_MATERIAL_NAME);
		Comparator comparator = new CardAttributesComparator(sortAttributes, false);
		Collections.sort(iterFiles, comparator);
	}
	
	private void sortRoundDataFiles(RoundDataFiles roundDataFiles){
		RoundData[] roundDatas = roundDataFiles.getRoundDatas();
		for (RoundData roundData : roundDatas) {
			sortCards(roundData.fileCardList);
			if (roundData.visaDataList != null && roundData.visaDataList.size() > 0) {
				for (RoundVisaData roundVisaData : roundData.visaDataList) {
					if(roundVisaData.fileCardList.size()<1){
						continue;
					}
					sortCards(roundVisaData.fileCardList);
				}
			}
		}
	}
}
