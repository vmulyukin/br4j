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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.CreateCard;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.IntegerAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.TextAttribute;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.service.AsyncDataServiceBean;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.dbmi.service.UserPrincipal;
import com.aplana.dbmi.service.AsyncDataServiceBean.ExecuteOption;

public class AdditionalEndorsementPortlet extends ResponsiblePersonsPortlet {
	
	public static final String VIEW = "/WEB-INF/jsp/additionalEndorsement.jsp";
	public static final String SESSION_BEAN = "additionalEndorsementPortletSessionBean";
	public static final String FIELD_ORDER = "order_";
	
	public static final ObjectId ATTR_LAST_NAME = ObjectId.predefined(
			StringAttribute.class, "jbr.person.lastName");
	public static final ObjectId ATTR_FIRST_NAME = ObjectId.predefined(
			StringAttribute.class, "jbr.person.firstName");
	public static final ObjectId ATTR_MIDDLE_NAME = ObjectId.predefined(
			StringAttribute.class, "jbr.person.middleName");

	public static final ObjectId TEMPLATE_VISA = ObjectId.predefined(
			Template.class, "jbr.visa");
	
	public static final ObjectId ATTR_VISA_RESPONSIBLE_PERSON = ObjectId.predefined(
			PersonAttribute.class, "jbr.visa.person");

	public static final ObjectId ATTR_VISA_ORDER = ObjectId.predefined(
			IntegerAttribute.class, "jbr.visa.order");
	
	public static final ObjectId ATTR_CHILD_VISAS = ObjectId.predefined(
			CardLinkAttribute.class, "jbr.visa.enclosedSet");
	public static final ObjectId ATTR_VISAS = ObjectId.predefined(
			CardLinkAttribute.class, "jbr.visa.set");
	public static final ObjectId ATTR_VISAS_HIDDEN = ObjectId.predefined(
			CardLinkAttribute.class, "jbr.visa.set.hidden");
	public static final ObjectId ATTR_VISA_CURRENT_RESOLUTION = ObjectId.predefined(
			TextAttribute.class, "jbr.visa.current.resolution");

	public static final ObjectId WFM_SEND_TO_ADDITIONAL_ENDORSEMENT = ObjectId.predefined(
			WorkflowMove.class, "jbr.visa.startEnclosed");
	public static final ObjectId WFM_ASSISTANT_SEND_TO_ADDITIONAL_ENDORSEMENT = ObjectId.predefined(
			WorkflowMove.class, "jbr.visa.startEnclosedBoss");

	public static final ObjectId WFM_DRAFT_TO_TRASH = ObjectId.predefined(
			WorkflowMove.class, "jbr.visa.draft.trash");
	public static final ObjectId WFM_AGREED_TO_TRASH = ObjectId.predefined(
			WorkflowMove.class, "jbr.visa.agreed.trash");
	public static final ObjectId WFM_CANCELLED_TO_TRASH = ObjectId.predefined(
			WorkflowMove.class, "jbr.visa.cancelled.trash");
	public static final ObjectId WFM_DISAGREED_TO_TRASH = ObjectId.predefined(
			WorkflowMove.class, "jbr.visa.disagreed.trash");
	public static final ObjectId WFM_ASSIGNED_TO_TRASH = ObjectId.predefined(
			WorkflowMove.class, "jbr.visa.assigned.trash");
	public static final ObjectId WFM_SKIPPED_TO_TRASH = ObjectId.predefined(
			WorkflowMove.class, "jbr.visa.skipped.trash");
	
	public static final ObjectId VISA_CARDSTATE_DRAFT = ObjectId.predefined(
			CardState.class, "draft");
	public static final ObjectId VISA_CARDSTATE_AGREED = ObjectId.predefined(
			CardState.class, "jbr.visa.agreed");
	public static final ObjectId VISA_CARDSTATE_CANCELLED = ObjectId.predefined(
			CardState.class, "jbr.visa.cancelled");
	public static final ObjectId VISA_CARDSTATE_DISAGREED = ObjectId.predefined(
			CardState.class, "jbr.visa.disagreed");
	public static final ObjectId VISA_CARDSTATE_ASSIGNED = ObjectId.predefined(
			CardState.class, "jbr.visa.assigned");
	public static final ObjectId VISA_CARDSTATE_SKIPPED = ObjectId.predefined(
			CardState.class, "jbr.visa.skipped");
	public static final ObjectId VISA_CARDSTATE_ASSISTENT = ObjectId.predefined(
			CardState.class, "jbr.visa.assistent");
	public static final ObjectId VISA_CARDSTATE_AGREEMENT = ObjectId.predefined(
			CardState.class, "jbr.visa.waiting");
	public static final ObjectId CARDSTATE_TRASH = ObjectId.predefined(
			CardState.class, "trash");
	
	
	private ObjectId[] stateIds = {
			CARDSTATE_TRASH	
	};
	
	@Override
	protected ResponsiblePersonsPortletSessionBean createSessionBean() {
		return new AdditionalEndorsementPortletSessionBean();
	}
	
	@Override
	protected AdditionalEndorsementPortletSessionBean getSessionBean(PortletRequest request){
	    PortletSession session = request.getPortletSession();
        AdditionalEndorsementPortletSessionBean sessionBean = (AdditionalEndorsementPortletSessionBean) session.getAttribute(SESSION_BEAN);
        String userName = (String) request.getPortletSession().getAttribute(DataServiceBean.USER_NAME, PortletSession.APPLICATION_SCOPE);
        if (userName != null) {
            sessionBean.getServiceBean().setUser(new UserPrincipal(userName));
            sessionBean.getServiceBean().setIsDelegation(true);
            sessionBean.getServiceBean().setRealUser(request.getUserPrincipal());
        } else {
            sessionBean.getServiceBean().setUser(request.getUserPrincipal());
            sessionBean.getServiceBean().setIsDelegation(false);
        }
        return sessionBean;
	}

	@Override
	protected String getSessionBeanName() {
		return SESSION_BEAN;
	}

	@Override
	protected String getView() {
		return VIEW;
	}
	
	@Override
	protected AdditionalEndorsementPortletSessionBean prepareSessionBean(PortletRequest request) throws DataException, ServiceException, PortletException {
		AdditionalEndorsementPortletSessionBean sessionBean = (AdditionalEndorsementPortletSessionBean) super.prepareSessionBean(request);
		setCurrentPerson(sessionBean);
		setVisaCard(request, sessionBean);

		final List<Card> childVisasCards = new LinkedList<Card>();
		sessionBean.setChildVisas(childVisasCards);

		final CardLinkAttribute childVisasAttribute = sessionBean.getVisaCard().getCardLinkAttributeById(ATTR_CHILD_VISAS);
		if (null != childVisasAttribute) {
			final Collection<ObjectId> childVisasIds = childVisasAttribute.getIdsLinked();
			for (ObjectId childVisaId : childVisasIds) {
				try {
					final Card childVisa = (Card) sessionBean.getServiceBean().getById(childVisaId);
					if(!isCardState(childVisa, stateIds)){
						childVisasCards.add(childVisa);
					}
				} catch (Exception e) {
					logger.error("Error by get visacard " + childVisaId + " on attribute " + ATTR_CHILD_VISAS, e);
				}
			}
		}
		
		sessionBean.setHeader(getResourceBundle(request.getLocale()).getString("header"));
		sessionBean.setWorkflowRequired(false);
		return sessionBean;
	}
	
	/**
	 * ��������� �������������� ������� ��������(<b>card</b>) � �������� �� ������(<b>stateIds</b>)
	 * @param card
	 * @param stateIds
	 * @return
	 */
	private boolean isCardState(Card card, ObjectId[] stateIds){
		ObjectId cardStateId = card.getState();
		for (ObjectId stateId : stateIds) {
			if(cardStateId.equals(stateId)){
				return true;
			}
		}
		return false;
	}

	@Override
	protected void prepareViewAttributes(RenderRequest request, RenderResponse response) throws DataException, ServiceException {
		/*
		 * 29455
		 * ������� �������������� ���������� ����� ��������������� ������������ 
		 * ������������ �� ���������� �������������� ���������� ���� ����
		 * ppolushkin
		 * 
		 * final AdditionalEndorsementPortletSessionBean sessionBean = getSessionBean(request);
		 * final DataServiceBean serviceBean = sessionBean.getServiceBean();
		*/

		final JSONArray endorsers = new JSONArray();

		/*
		 * 29455
		 * ������� �������������� ���������� ����� ��������������� ������������ 
		 * ������������ �� ���������� �������������� ���������� ���� ����
		 * ppolushkin
		 * 
		 * if (sessionBean.getChildVisas() != null) {
		 *	for (Card childVisa : sessionBean.getChildVisas()) {
		 *		final IntegerAttribute childVisaOrder = (IntegerAttribute) childVisa.getAttributeById(ATTR_VISA_ORDER);
		 *		int order = null != childVisaOrder ? childVisaOrder.getValue() : 1;
		 *		if (order <= 0) {
		 *			order = 1;
		 *		}
		 *
		 *		final PersonAttribute childVisaResponsiblePerson = (PersonAttribute) childVisa.getAttributeById(ATTR_VISA_RESPONSIBLE_PERSON);
		 *		if (null != childVisaResponsiblePerson) {
		 *			final Person additionalEndorser = childVisaResponsiblePerson.getPerson();
		 *			try {
		 *				final JSONObject endorser = new JSONObject();
		 *				endorser.put("cardId", additionalEndorser.getCardId().getId());
		 *				endorser.put("name", getPersonName(additionalEndorser.getCardId(), serviceBean));
		 *				endorser.put("order", order);
		 *				endorsers.put(endorser);
		 *			} catch (JSONException e) {
		 *				logger.error(e);
		 *			}
		 *		}
		 *	}
		 * }
		 */
		request.setAttribute(ENDORSERS, endorsers);
	}

	@Override
	protected void processActionDone(ActionRequest request, ActionResponse response) throws DataException, ServiceException {
		final AdditionalEndorsementPortletSessionBean sessionBean = getSessionBean(request);
		final AsyncDataServiceBean serviceBean = sessionBean.getServiceBean();

		final Card visaCard = sessionBean.getVisaCard();
		serviceBean.doAction(new LockObject(visaCard.getId()));
		try {
			// deprecated
			// final String resolution = request.getParameter(FIELD_TEXT_RESOLUTION);
			// if (null != resolution && resolution.trim().length() > 0) {
			// 		final HtmlAttribute attr = ((HtmlAttribute)
			//		visaCard.getAttributeById(ATTR_VISA_DECISION));
			// 		final ReportXMLEditor editor = new RepeatableReportXMLEditor(attr.getValue(), null);
			// 		editor.appendPart("", null, "", resolution);
			// 		attr.setValue(editor.serialize());
			// }

			final List<ObjectId> childResolutionIds = processChildVisas(request, sessionBean);

			CardLinkAttribute childVisasAttribute = visaCard.getCardLinkAttributeById(ATTR_CHILD_VISAS);
			if (null == childVisasAttribute) {
				childVisasAttribute = new CardLinkAttribute();
				childVisasAttribute.setId(ATTR_CHILD_VISAS);
				visaCard.getAttributes().add(childVisasAttribute);
			}
			// (YNikitin, 2011/04/13) �������� ������� ������ ��������� ��� >>>>
			Collection idsLinked = childVisasAttribute.getIdsLinked();
			// ��������� �� ������ ��������� ��� � ������������ ��������� � ������ "�������"
			for (Iterator iterator = idsLinked.iterator(); iterator.hasNext();) {
				final Object item = iterator.next();
				Card childVisaCard = null;
				ObjectId cardId = null;
				try {
					if (item == null)
						continue;
					if (item instanceof Card) {
						childVisaCard = (Card) item;
						cardId = childVisaCard.getId();
					} else {
						cardId = (ObjectId) item;
						childVisaCard = (Card) serviceBean.getById(cardId);
					}
					// ��������� � ������ ������� ����������� ����
					if (!childResolutionIds.contains(childVisaCard.getId())) {
						// ToDo: �������� Action ��� ������ ������� WorkFlowMove
						// �� ������ ������� �������� � ��������� �������
						WorkflowMove wfm = null;
						if (childVisaCard.getState().equals(VISA_CARDSTATE_DRAFT))
							wfm = (WorkflowMove) serviceBean.getById(WFM_DRAFT_TO_TRASH);
						else if (childVisaCard.getState().equals(VISA_CARDSTATE_AGREED))
							wfm = (WorkflowMove) serviceBean.getById(WFM_AGREED_TO_TRASH);

						else if (childVisaCard.getState().equals(VISA_CARDSTATE_CANCELLED))
							wfm = (WorkflowMove) serviceBean.getById(WFM_CANCELLED_TO_TRASH);
						else if (childVisaCard.getState().equals(VISA_CARDSTATE_DISAGREED))
							wfm = (WorkflowMove) serviceBean.getById(WFM_DISAGREED_TO_TRASH);
						else if (childVisaCard.getState().equals(VISA_CARDSTATE_ASSIGNED))
							wfm = (WorkflowMove) serviceBean.getById(WFM_ASSIGNED_TO_TRASH);
						else if (childVisaCard.getState().equals(VISA_CARDSTATE_SKIPPED))
							wfm = (WorkflowMove) serviceBean.getById(WFM_SKIPPED_TO_TRASH);

						serviceBean.doAction(new LockObject(childVisaCard));
						try {
							if (wfm == null)
								throw new Exception("Destination WorkFlowMove is not found");
							final ChangeState actionChange = new ChangeState();
							actionChange.setCard(childVisaCard);
							actionChange.setWorkflowMove(wfm);
							serviceBean.doAction(actionChange);
						} finally {
							serviceBean.doAction(new UnlockObject(childVisaCard));
						}
					}
				} catch (Exception e) {
					logger.error("Error by changeState for card " + cardId + ": " + e);
				}
			}
			// childVisasAttribute.clear();
			// <<<<
			// ��������� ����� ��������� � ������ ��������� ���
			// (CardLinkAttribute ��� ����������� �� ���������� ������)
			childVisasAttribute.addIdsLinked(childResolutionIds);
			
			// ��������� ���� "������� �������"
			TextAttribute attr = ((TextAttribute) visaCard.getAttributeById(ATTR_VISA_CURRENT_RESOLUTION));
			if(attr == null) {
				attr = new TextAttribute();
				visaCard.getAttributes().add(attr);
			}
			attr.setValue(getResourceBundle(request.getLocale()).getString("message.currentDecision"));
			
			serviceBean.saveObject(visaCard, ExecuteOption.SYNC);
			performWorkflowMove(sessionBean);
		} finally {
			serviceBean.doAction(new UnlockObject(visaCard.getId()));
		}
	}
	
	/**
	 * @param request
	 * @param sessionBean
	 * @return
	 * @throws DataException
	 * @throws ServiceException
	 */
	private List<ObjectId> processChildVisas(ActionRequest request, AdditionalEndorsementPortletSessionBean sessionBean) throws DataException, ServiceException {
		final AsyncDataServiceBean serviceBean = sessionBean.getServiceBean();
		final String[] responsiblePersonsCardIds = request.getParameterValues(FIELD_ENDORSERS);

		if (null == responsiblePersonsCardIds) {
			return new ArrayList<ObjectId>(0);
		}

		final List<ObjectId> childResolutionIds = new ArrayList<ObjectId>(responsiblePersonsCardIds.length);

		for (String responsiblePersonCardIdStr : responsiblePersonsCardIds) {
			Person responsiblePerson = getPersonByCardId(responsiblePersonCardIdStr, serviceBean);
			Card childVisa = findChildVisaByResponsible(responsiblePerson, sessionBean);

			if (null == childVisa) {
				CreateCard createCard = new CreateCard(TEMPLATE_VISA);
				createCard.setLinked(true);
				childVisa = (Card) serviceBean.doAction(createCard);
				ObjectId childVisaCardId = serviceBean.saveObject(childVisa, ExecuteOption.SYNC);
				childVisa.setId(childVisaCardId);

				PersonAttribute responsiblePersonAttribute = (PersonAttribute) childVisa.getAttributeById(ATTR_VISA_RESPONSIBLE_PERSON);
				if (null == responsiblePersonAttribute) {
					responsiblePersonAttribute = new PersonAttribute();
					responsiblePersonAttribute.setId(ATTR_VISA_RESPONSIBLE_PERSON);
					childVisa.getAttributes().add(responsiblePersonAttribute);
				}
				responsiblePersonAttribute.setPerson(responsiblePerson.getId());
			} else {
				serviceBean.doAction(new LockObject(childVisa.getId()));
			}

			String orderString = request.getParameter(FIELD_ORDER + responsiblePersonCardIdStr);
			int order = Integer.parseInt(orderString);

			IntegerAttribute visaOrderAttribute = (IntegerAttribute) childVisa.getAttributeById(ATTR_VISA_ORDER);
			if (null == visaOrderAttribute) {
				visaOrderAttribute = new IntegerAttribute();
				visaOrderAttribute.setId(ATTR_VISA_ORDER);
				childVisa.getAttributes().add(visaOrderAttribute);
			}
			visaOrderAttribute.setValue(order);

			serviceBean.saveObject(childVisa);
			serviceBean.doAction(new UnlockObject(childVisa.getId()));

			childResolutionIds.add(childVisa.getId());
		}
		return childResolutionIds;
	}

	/**
	 * @param sessionBean
	 * @throws DataException
	 * @throws ServiceException
	 */
	private void performWorkflowMove(AdditionalEndorsementPortletSessionBean sessionBean) throws DataException, ServiceException {
		final DataServiceBean serviceBean = sessionBean.getServiceBean();
		final Card visaCard = sessionBean.getVisaCard();
		final WorkflowMove wfm = (WorkflowMove) serviceBean.getById(WFM_SEND_TO_ADDITIONAL_ENDORSEMENT);
		final ChangeState actionChange = new ChangeState();
		actionChange.setCard(visaCard);
		actionChange.setWorkflowMove(wfm);
		serviceBean.doAction(actionChange);
	}

	/**
	 * @param cardId
	 * @param serviceBean
	 * @return
	 * @throws DataException
	 * @throws ServiceException
	 */
	private String getPersonName(ObjectId cardId, DataServiceBean serviceBean) throws DataException, ServiceException {
		final Card card = (Card) serviceBean.getById(cardId);

		final StringBuffer name = new StringBuffer();
		Attribute attr = card.getAttributeById(ATTR_LAST_NAME);
		if (attr != null && attr.getStringValue() != null) {
			name.append(attr.getStringValue()).append(' ');
		}
		attr = card.getAttributeById(ATTR_FIRST_NAME);
		if (attr != null && attr.getStringValue() != null) {
			name.append(attr.getStringValue()).append(' ');
		}
		attr = card.getAttributeById(ATTR_MIDDLE_NAME);
		if (attr != null && attr.getStringValue() != null) {
			name.append(attr.getStringValue());
		}

		return name.toString();
	}

	/**
	 * @param responsiblePerson
	 * @param sessionBean
	 * @return
	 */
	private Card findChildVisaByResponsible(Person responsiblePerson, AdditionalEndorsementPortletSessionBean sessionBean) {
		for (Card childVisa : sessionBean.getChildVisas()) {
			PersonAttribute childVisaResponsiblePerson = (PersonAttribute) childVisa.getAttributeById(ATTR_VISA_RESPONSIBLE_PERSON);

			if (responsiblePerson.getId().equals(childVisaResponsiblePerson.getPerson().getId())
					&& VISA_CARDSTATE_DRAFT.equals(childVisa.getState())) {
				return childVisa;
			}
		}
		return null;
	}

	/**
	 * @param sessionBean
	 * @throws DataException
	 * @throws ServiceException
	 */
	private void setCurrentPerson(AdditionalEndorsementPortletSessionBean sessionBean) throws DataException, ServiceException {
		final Person curPerson = (Person) sessionBean.getServiceBean().getById(Person.ID_CURRENT);
		sessionBean.setCurrentPerson(curPerson);
	}

	/**
	 * ����� ����� ������ visaListAttrId � �������� parentCard �������� ����, � 
	 * ������� ������������ personId �������� ������ ������ personAttrId.
	 * @param parentCard
	 * @param visaListAttrId
	 * @param personAttrId
	 * @param personId
	 * @return
	 * @throws ServiceException 
	 * @throws DataException 
	 */
	private Card findVisaCardInTheListByPerson(ObjectId personId, DataServiceBean serviceBean, Card parentCard, ObjectId visaListAttrId, ObjectId personAttrId) throws DataException, ServiceException {
		final CardLinkAttribute visasAttribute = parentCard.getCardLinkAttributeById(visaListAttrId);
		if (visasAttribute == null || personAttrId == null)
			return null;

		for (ObjectId visaCardId : (Collection<ObjectId>) visasAttribute.getIdsLinked()) {
			// (YNikitin, 2011/04/08) �������� ����������� ������ �� ������
			// ��������� ���� �� ������������ �������� >>>>
			try {
				final Card visaCard = (Card) serviceBean.getById(visaCardId);
				ObjectId visaCardState = visaCard.getState();
				//�������� ��� ����������� ���.������������ ������ ���� � �������� "������������" ��� "��������� ����������"
				if(!visaCardState.equals(VISA_CARDSTATE_AGREEMENT) && !visaCardState.equals(VISA_CARDSTATE_ASSISTENT)){
					continue;
				}
				final PersonAttribute responsibleAttribute = (PersonAttribute) visaCard.getAttributeById(personAttrId);
				if (responsibleAttribute != null && responsibleAttribute.getPerson() != null && personId.equals(responsibleAttribute.getPerson().getId())) {
					return visaCard;
				}
			} catch (Exception e) {
				logger.error("Error for card " + visaCardId.getId() + ": " + e);
				continue;
			}
			// <<<<
		}
		return null;
	}

	private void setVisaCard(PortletRequest request, AdditionalEndorsementPortletSessionBean sessionBean) throws DataException, ServiceException {
		final DataServiceBean serviceBean = sessionBean.getServiceBean();
		final Long parentCardId = Long.parseLong(request.getParameter(FIELD_CARD_ID));
		final Card parentCard = (Card) serviceBean.getById(new ObjectId(Card.class, parentCardId));
		final ObjectId personId = sessionBean.getCurrentPerson().getId();

		// ���� ������� � ������ "�������� ����������� �������", ����� "����"...
		Card visaCard = findVisaCardInTheListByPerson(personId, serviceBean, parentCard, ATTR_VISAS_HIDDEN, ATTR_VISA_RESPONSIBLE_PERSON);
		if (visaCard == null) {
			visaCard = findVisaCardInTheListByPerson(personId, serviceBean, parentCard, ATTR_VISAS, ATTR_VISA_RESPONSIBLE_PERSON);
		}

		if (visaCard == null) {
			throw new DataException(MessageFormat.format("Cannot find visa at card {0} inside ''{1}''/''{2}'' corresponding to user {3} via attribute ''{4}''", new Object[] { parentCardId,
					ATTR_VISAS, ATTR_VISAS_HIDDEN, personId, ATTR_VISA_RESPONSIBLE_PERSON }));
		}
		sessionBean.setVisaCard(visaCard);
	}
}
