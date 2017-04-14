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
package com.aplana.dbmi.module.docflow;

import com.aplana.dbmi.action.*;
import com.aplana.dbmi.jbr.util.CardUtils;
import com.aplana.dbmi.model.*;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.client.CardLinkLoader;
import com.aplana.dbmi.service.impl.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * �����, ����������� ���� ������� � �������� ���� � � �������� ���������
 *
 */
public class NegotiationHistory extends ProcessorBase implements Parametrized {
	private static final long serialVersionUID = 1L;
	
	public static final ObjectId DOCUMENT_BACK_LINK_ATTRIBUTE_ID =	ObjectId.predefined(BackLinkAttribute.class, "jbr.visa.parent");
	public static final ObjectId CURRENT_RESOLUTION_ATTRIBUTE_ID =	ObjectId.predefined(TextAttribute.class, "jbr.visa.current.resolution");
	public static final ObjectId CURRENT_SIGN_RESOLUTION_ATTRIBUTE_ID = ObjectId.predefined(TextAttribute.class, "jbr.sign.current.resolution");
	public static final ObjectId VISA_HISTORY_ATTRIBUTE_ID =	ObjectId.predefined(HtmlAttribute.class, "jbr.visa.decision");
	public static final ObjectId SIGN_HISTORY_ATTRIBUTE_ID =	ObjectId.predefined(HtmlAttribute.class, "jbr.sign.comment");
	public static final ObjectId VISA_ROUND_ATTRIBUTE_ID =	ObjectId.predefined(IntegerAttribute.class, "jbr.visa.visa.round");
	public static final ObjectId SIGN_ROUND_ATTRIBUTE_ID =	ObjectId.predefined(IntegerAttribute.class, "jbr.sign.sign.round");
	public static final ObjectId VISA_ORDER_ATTRIBUTE_ID =	ObjectId.predefined(IntegerAttribute.class, "jbr.visa.number");
	public static final ObjectId SIGN_ORDER_ATTRIBUTE_ID =	ObjectId.predefined(IntegerAttribute.class, "jbr.sign.order");
	public static final ObjectId VISA_TEMPLATE_ID = ObjectId.predefined(Template.class, "jbr.visa");
	public static final ObjectId TASK_VISA_TEMPLATE_ID = ObjectId.predefined(Template.class, "jbr.taskVisa");
	public static final ObjectId SIGN_TEMPLATE_ID = ObjectId.predefined(Template.class, "jbr.sign");
	public static final ObjectId TEMPLATE = ObjectId.predefined(ListAttribute.class, "template.name");
	public static final ObjectId VISA_ROUND_MAIN_ID = ObjectId.predefined(IntegerAttribute.class, "jbr.visa.round");
	public static final ObjectId SIGN_DOCUMENT_PARENT =	ObjectId.predefined(BackLinkAttribute.class, "jbr.sign.parent");
	public static final ObjectId VISA_RESPONSIBLE_ID =	ObjectId.predefined(PersonAttribute.class, "jbr.visa.person");
	public static final ObjectId SIGN_RESPONSIBLE_ID =	ObjectId.predefined(PersonAttribute.class, "jbr.sign.person");
	public static final ObjectId PARENT_VISA_ID = ObjectId.predefined(BackLinkAttribute.class, "jbr.visa.parentVisa");
	public static final String CLEAR_CURRENT_RESOLUTION_PARAM = "clearCurrentResolution";
	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

	private boolean clearCurrentResolution = false;
	
	@Override
	@SuppressWarnings("unchecked")
	public Object process() throws DataException {
		try {
			//�������� ��������
			ChangeState move = getAction();

			//�������� ������������
			Person curPerson = getUser().getPerson();

			//�������� ���������� ������������
			Person sysPerson = getDatabase().resolveUser(Database.SYSTEM_USER);

			//����� � �������, ������ ���� ������� ������������ �� �������, ����� ���������� ������
			if (curPerson.getId().equals(sysPerson.getId())){
				return null;
			}

			//�������� ����
			Card visa = move.getCard();
			ObjectId template = (visa.getTemplate() == null) ?
					((ListAttribute)visa.getAttributeById(TEMPLATE)).getReference():
						visa.getTemplate();
			Object block = visa.getAttributes().iterator().next();
			/* 17.02.2011, O.E.
			 * �������� ���� �������� ����� search, � �� ����� getCard,
			 *  ������ �������� ���������� ��������� �������������.
			 */
			ObjectId resolutionAttrId, roundAttributeId, orderAttributeId, historyAttrId, actionPerson, parentNegotiationId;
			if(VISA_TEMPLATE_ID.equals(template) || TASK_VISA_TEMPLATE_ID.equals(template)) {
				resolutionAttrId = CURRENT_RESOLUTION_ATTRIBUTE_ID;
				roundAttributeId = VISA_ROUND_ATTRIBUTE_ID;
				orderAttributeId = VISA_ORDER_ATTRIBUTE_ID;
				historyAttrId = VISA_HISTORY_ATTRIBUTE_ID;
				actionPerson = VISA_RESPONSIBLE_ID;
				parentNegotiationId = PARENT_VISA_ID;
			} else if(SIGN_TEMPLATE_ID.equals(template)) {
				resolutionAttrId = CURRENT_SIGN_RESOLUTION_ATTRIBUTE_ID;
				roundAttributeId = SIGN_ROUND_ATTRIBUTE_ID;
				orderAttributeId = SIGN_ORDER_ATTRIBUTE_ID;
				historyAttrId = SIGN_HISTORY_ATTRIBUTE_ID;
				actionPerson = SIGN_RESPONSIBLE_ID;
				parentNegotiationId = null;
			} else {
				throw new DataException("negotiation.history.invalid.template");
			}
			
			if(block instanceof Attribute) {
				ArrayList<ObjectId> temp = new ArrayList<ObjectId>();
				temp.add(visa.getId());
				visa = CardLinkLoader.loadCardsByIds
				(
					temp,
					new ObjectId[] { resolutionAttrId, roundAttributeId, orderAttributeId, historyAttrId, actionPerson},
					getSystemUser(),
					getQueryFactory(),
					getDatabase()
				).iterator().next();
			}
			//� ���� ��� ���������� �������� ���� ������� �������, ����� ����� ������������ � ������� ������������/����������
			TextAttribute resolutionAttr;
			if (SIGN_TEMPLATE_ID.equals(template)) {
				resolutionAttr = visa.getAttributeById(CURRENT_SIGN_RESOLUTION_ATTRIBUTE_ID);
			} else {
				resolutionAttr = visa.getAttributeById(CURRENT_RESOLUTION_ATTRIBUTE_ID);
			}
			IntegerAttribute roundAttribute = visa.getAttributeById(roundAttributeId);
			IntegerAttribute orderAttribute = visa.getAttributeById(orderAttributeId);
			PersonAttribute personAttribute = visa.getAttributeById(actionPerson);
			
			String resolutionValue = resolutionAttr == null ? "" : resolutionAttr.getValue();
			Integer round = roundAttribute == null ? -1 : roundAttribute.getValue();
			String order = orderAttribute == null ? "-1" : String.valueOf(orderAttribute.getValue());
			Person person = personAttribute == null ? null : personAttribute.getPerson();
			
			if (parentNegotiationId != null) {
				boolean isHasParent = true;
				ListProject listProject = new ListProject(visa.getId());
				while (isHasParent){
					listProject.setAttribute(parentNegotiationId);
					listProject.setColumns(Collections.singletonList(new SearchResult.Column(orderAttributeId)));
					SearchResult searchResult = execAction(listProject);
					if (!searchResult.getCards().isEmpty()) {
						Card parentCard = searchResult.getCards().iterator().next();
						IntegerAttribute orderAttributeparent =  parentCard.getAttributeById(orderAttributeId);
						if(orderAttributeparent != null && !orderAttributeparent.isEmpty()){
							order =String.valueOf(orderAttributeparent.getValue()) + "." + order ;
						}
						listProject.setCard(parentCard.getId());
					} else {
						isHasParent = false;
					}
				}
			}
			/*
			 * mainRound ������������ ��� ������������� �������� ���������� � ���������� ���-���������
			 * ���� ������� �������� �� "����������" ��� ������ �� ���� ��������, 
			 * �� ������� mainRound (�������� ���.���������) = round (�������� ���.��������)
			 */
			
			Integer mainRound = getRoundOfMainDoc(visa, template);
			
			if(mainRound == null)
				mainRound = round;
			
			//���������� ������� ������� � ������� ���� (� �������� �������� �������� �������� getMoveName(), �.�. ���� ����� ��� ����������� ���� �������� ��������, ���� ��� �������� �� ���������)
			addResolutionToHistory(resolutionValue, historyAttrId, visa, round, mainRound, order, 
					move.getWorkflowMove().getMoveName(), move.getWorkflowMove().getToState().getId().toString(), person);

			//���������� ������� ������� ���� ����� ���� ����� � ����������
			if (clearCurrentResolution && resolutionAttr != null){
				resolutionAttr.clear();
			}

			//��������� ����. ��������� ����������, ������ ��� ����� �� �� �������������� ����������� �������� �� ���� � ���� ���������. ���� �� �������� �������� �� �������� �������
			//store(visa);
			// ��������� ��������� ������� - ��� ������� � ������ ����� ��������

			OverwriteCardAttributes action = new OverwriteCardAttributes();
			action.setCardId(visa.getId());
			action.setAttributes(Collections.singletonList(visa.getAttributeById(historyAttrId)));
			action.setInsertOnly(false);
			execAction(action);

			return visa;
		}catch(DataException ex){
			throw ex;
		}catch(Exception ex){
			throw new DataException("Error on execute NegotiationHistory processor", ex);
		}
	}


	private <T> T execAction(ObjectAction<T> action)
			throws DataException {
		final ActionQueryBase query = getQueryFactory().getActionQuery(action);
		query.setAction(action);
		return getDatabase().executeQuery(getSystemUser(), query);
	}
	
	
	/**
	 * ����� ����� �������� ��������� ��������� (�� �� ����� �������� ���� - ��� ���� ���������)
	 * @param currentCard - ������� ��������
	 * @param template - ������ ������� ��������
	 * @return �������� ��������� ��������� ��� null
	 */
	private Integer getRoundOfMainDoc(Card currentCard, ObjectId template) 
			throws DataException {
		
		final BackLinkAttribute parentAttr = SIGN_TEMPLATE_ID.equals(template) ? (BackLinkAttribute) currentCard.getAttributeById(SIGN_DOCUMENT_PARENT) : null;
		final ObjectId parentLinkId = parentAttr != null ? parentAttr.getLinkSource() : null;
		if (parentLinkId != null) {
			
			final List<Card> cards = getMainDocForIterationNumber(parentLinkId, currentCard.getId());
			
			if (cards != null && !cards.isEmpty()) {
				if (cards.size() == 1) {
					return assignRoundOfMainDoc(currentCard, cards.get(0));
				} else {
					logger.warn("Card " + currentCard.getId().getId() + " have a few parent cards");
				}
			} else {
				logger.warn("Card " + currentCard.getId().getId() + " no have a parent card");
			}		
		}
		
		return null;
	}
	
	
	/**
	 * ������� �������� �������� �� ���������� �������� ���� ��������, ���� ��� null
	 * @param currentCard
	 * @param mainDoc
	 * @return ����� �������� �������� ��� null
	 */
	private Integer assignRoundOfMainDoc(Card currentCard, Card mainDoc) {
		if (mainDoc != null) {
			final IntegerAttribute iteration = mainDoc.getAttributeById(VISA_ROUND_MAIN_ID);
			final Integer iter = iteration != null ? iteration.getValue() : null;
			if (iter != null) {
				return iter;
			}
		} else {
			logger.warn("Parent card for card " + currentCard.getId().getId() + " are not loaded");
		}
		return null;
	}
	
	
	/**
	 * �������� ������������ �������� ���� �� �������� �� �������,
	 * � ��� �������� ������ ������� �������� ����� ��������.
	 * @param attr - �������� ������� ������������ ��������
	 * @param value - �������� ��������� (id ������� ��������)
	 * @return ������ ������������ �������� � ������� �� ������� �� �������� attr
	 */
	private List<Card> getMainDocForIterationNumber(ObjectId attr, ObjectId value) 
		throws DataException {
		
		Search search = new Search();
		search.setByAttributes(true);
		search.addCardLinkAttribute(attr, value);
		
		search.setColumns(Collections.singletonList(CardUtils.createColumn(VISA_ROUND_MAIN_ID)));

		return CardUtils.execSearchCards(search, getQueryFactory(), getDatabase(), getSystemUser());
	}

	/**
	 * ���������� ��������
	 * @param card
	 * @throws DataException
	 */
	/*private void store(Card card) throws DataException {
		final SaveQueryBase query = getQueryFactory().getSaveQuery(card);
		query.setObject(card);
		getDatabase().executeQuery(getUser(), query);
	}*/

	/**
	 * ���������� ��������� � �������
	 * @param resolution
	 * @param documentHistoryAttributeId
	 * @param card
	 * @throws TransformerException
	 * @throws TransformerFactoryConfigurationError
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws UnsupportedEncodingException
	 */
	private void addResolutionToHistory(String resolution,
			ObjectId documentHistoryAttributeId, Card card, int round, int mainRound, String order, String actionName, String toStateId, Person planPerson) throws UnsupportedEncodingException, ParserConfigurationException, SAXException, IOException, TransformerFactoryConfigurationError, TransformerException {
		//�������� �������, ��� �������� �������
		HtmlAttribute historyAttr = card.getAttributeById(documentHistoryAttributeId);

		UserData userData = getPrimaryQuery().getRealUser() != null ?
				getPrimaryQuery().getRealUser() : getPrimaryQuery().getUser();

		String newValue = formHistoryAttrValue(historyAttr.getValue(),
				String.valueOf(round), String.valueOf(mainRound), order, userData.getPerson().getFullName(), 
				resolution, actionName, toStateId, planPerson != null ? planPerson.getFullName() : "");

		historyAttr.setValue(newValue);
	}

	String formHistoryAttrValue(String originalValue, String round, String mainRound, String order, String person, String resolution, String actionName, String toStateId, String planPerson) throws ParserConfigurationException, UnsupportedEncodingException, SAXException, IOException, TransformerFactoryConfigurationError, TransformerException {

		Document xmldoc;
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = dbf.newDocumentBuilder();
		// ������ XML �� ������
		if (originalValue == null || originalValue.length() == 0 || originalValue.charAt(0) != '<'){
			xmldoc = builder.parse(new ByteArrayInputStream("<?xml version=\"1.0\" encoding=\"UTF-8\"?><report/>".getBytes("UTF-8")));
		}else{
			xmldoc = builder.parse(new ByteArrayInputStream(originalValue.getBytes("UTF-8")));
		}
		Element root = xmldoc.getDocumentElement();

		final Date date = new Date();
		final Element part = xmldoc.createElement("part");
		part.setAttribute("plan-user", planPerson);
		part.setAttribute("round", round);
		part.setAttribute("main-round", mainRound);
		part.setAttribute("order", order);
		part.setAttribute("timestamp", DATE_FORMAT.format(date));
		part.setAttribute("fact-user", person);
		part.setAttribute("action", actionName);
		part.setAttribute("to-state", toStateId);
		part.setTextContent(resolution);
		root.appendChild(part);

		final StringWriter stw = new StringWriter();
		final Transformer serializer = TransformerFactory.newInstance().newTransformer();
		serializer.transform(new DOMSource(xmldoc), new StreamResult(stw));

		return stw.toString();
	}

	public void setParameter(String name, String value) {
		if (CLEAR_CURRENT_RESOLUTION_PARAM.equalsIgnoreCase(name)){
			clearCurrentResolution = Boolean.parseBoolean(value);
		}
	}
}
