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

import java.util.ArrayList;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.json.JSONArray;
import org.springframework.util.StringUtils;

import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.CreateCard;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.TextAttribute;
import com.aplana.dbmi.model.TreeAttribute;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.model.util.AttrUtils;
import com.aplana.dbmi.service.AsyncDataServiceBean;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.dbmi.service.AsyncDataServiceBean.ExecuteOption;
import com.aplana.dbmi.util.AttributeUtil;

/**
 * Represents portlet for handling sending acquaintances inside Supervisor/Minister Workstation.
 * 
 * @author EStatkevich
 */
public class AcquaintancePortlet extends ResponsiblePersonsPortlet {
	
	public static final String VIEW = "/WEB-INF/jsp/acquaintance.jsp";
	public static final String SESSION_BEAN = "acquaintancePortletSessionBean";

	private static final ObjectId ATTR_INFORMATION_PERSON = ObjectId.predefined(PersonAttribute.class, "jbr.information.person");
	private static final ObjectId DOCUMENT_INFORMATION_CARDLINK_ID = ObjectId.predefined(CardLinkAttribute.class, "jbr.inform.list");
	private static final ObjectId JBR_INFD_TYPEDOC = ObjectId.predefined(CardLinkAttribute.class, "jbr.reg.doctype");
	private static final ObjectId JBR_REGD_REGNUM = ObjectId.predefined(StringAttribute.class, "regnumber");
	private static final ObjectId TEMPLATE_ACQUAINTANCE = ObjectId.predefined(Template.class, "jbr.inform");
	private static final ObjectId WFM_SEND_ACQUAINTANCE = ObjectId.predefined(WorkflowMove.class, "jbr.info.send");
	private static final ObjectId INCOMING_PEOPLE_ID = ObjectId.predefined(Template.class, "jbr.incomingpeople");
	
	private static final ObjectId AC_COMMENT = ObjectId.predefined(TextAttribute.class, "jbr.inf.comment");
	
	private static final ObjectId ATTR_NAME = ObjectId.predefined(StringAttribute.class, "name");
	
	public static final String PARAM_COMMENT="commentTextarea";
	

	@Override
	protected ResponsiblePersonsPortletSessionBean createSessionBean() {
		return new AcquaintancePortletSessionBean();
	}

	@Override
	protected AcquaintancePortletSessionBean getSessionBean(PortletRequest request) {
		PortletSession session = request.getPortletSession();
		return (AcquaintancePortletSessionBean) session.getAttribute(SESSION_BEAN);
	}

	@Override
	protected void prepareViewAttributes(RenderRequest request, RenderResponse response) throws DataException, ServiceException {
		final JSONArray endorsers = new JSONArray();
		request.setAttribute(ENDORSERS, endorsers);
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
	protected ResponsiblePersonsPortletSessionBean prepareSessionBean(PortletRequest request) throws DataException, ServiceException, PortletException {
		AcquaintancePortletSessionBean sessionBean = (AcquaintancePortletSessionBean) super.prepareSessionBean(request);
		final Card baseCard = sessionBean.getBaseCard();
		if (baseCard != null) {
			final PersonAttribute acquaintedPersonsAttribute = (PersonAttribute) baseCard.getAttributeById(ATTR_INFORMATION_PERSON);
			if (acquaintedPersonsAttribute != null && !acquaintedPersonsAttribute.isEmpty()) {
				for (Object person : acquaintedPersonsAttribute.getValues()) {
					sessionBean.getAcquaintedPersons().add((Long) ((Person) person).getCardId().getId());
				}
			}

			StringBuilder theHeader = new StringBuilder(30);
			if(!baseCard.getTemplate().equals(INCOMING_PEOPLE_ID)){
				CardLinkAttribute typeDocLinkAttr = (CardLinkAttribute) baseCard.getAttributeById(JBR_INFD_TYPEDOC);
				if(typeDocLinkAttr.getSingleLinkedId()!=null){
					Card typeDocCard = (Card) sessionBean.getServiceBean().getById(typeDocLinkAttr.getSingleLinkedId());
					StringAttribute typeDocAttrName = (StringAttribute) typeDocCard.getAttributeById(ATTR_NAME);
					theHeader.append(typeDocAttrName.getValue());
				}
			} else {
				theHeader.append(" ��������� ����������");
			}


			StringAttribute regNumAttr = (StringAttribute) baseCard.getAttributeById(JBR_REGD_REGNUM);
			theHeader.append(" �").append(regNumAttr.getValue());
			sessionBean.setHeader(theHeader.toString());
		}
		return sessionBean;
	}

	/**
	 * @param request
	 * @param response
	 * @throws DataException
	 * @throws ServiceException
	 */
	protected void processActionDone(ActionRequest request, ActionResponse response) throws DataException, ServiceException {
		final AcquaintancePortletSessionBean sessionBean = getSessionBean(request);
		final DataServiceBean serviceBean = sessionBean.getServiceBean();
		List<ObjectId> newAcquaintanceIds = storeNewAcquaintances(request, sessionBean);
		linkAcquaintancesWithMainDoc(request, serviceBean, sessionBean, newAcquaintanceIds);
		sendCreatedAcquaintances(serviceBean, newAcquaintanceIds);
	}
	
	/**
	 * Stores new acquaintance cards for selected persons.
	 * 
	 * @param request
	 * @param sessionBean
	 * @return
	 * @throws DataException
	 * @throws ServiceException
	 */
	private List<ObjectId> storeNewAcquaintances(ActionRequest request, AcquaintancePortletSessionBean sessionBean) throws DataException, ServiceException {
		final DataServiceBean serviceBean = sessionBean.getServiceBean();
		final String[] responsiblePersonsCardIds = request.getParameterValues(FIELD_ENDORSERS);
		final String ac_comment = request.getParameter(PARAM_COMMENT);
		
		if (null == responsiblePersonsCardIds) {
			return new ArrayList<ObjectId>(0);
		}
		
		final List<ObjectId> newAcquaintanceIds = new ArrayList<ObjectId>(responsiblePersonsCardIds.length);
		
		
		

		for (String responsiblePersonCardIdStr : responsiblePersonsCardIds) {
			Person responsiblePerson = getPersonByCardId(responsiblePersonCardIdStr, serviceBean);
			CreateCard createCard = new CreateCard(TEMPLATE_ACQUAINTANCE);
			createCard.setLinked(true);
			Card createdAcquaintance = (Card) serviceBean.doAction(createCard);
			
			if(!checkEmptyString(ac_comment)){
				TextAttribute textAttribute = (TextAttribute) getCreateAttribute(createdAcquaintance, AC_COMMENT);
				textAttribute.setValue(ac_comment);
			}
			
			PersonAttribute responsiblePersonAttribute = (PersonAttribute) getCreateAttribute(createdAcquaintance, ATTR_INFORMATION_PERSON);
			 /* (PersonAttribute) createdAcquaintance.getAttributeById(ATTR_INFORMATION_PERSON);
			if (null == responsiblePersonAttribute) {
				responsiblePersonAttribute = new PersonAttribute();
				responsiblePersonAttribute.setId(ATTR_INFORMATION_PERSON);
				createdAcquaintance.getAttributes().add(responsiblePersonAttribute);
			}*/
			responsiblePersonAttribute.setPerson(responsiblePerson.getId());
			ObjectId acquaintanceCardId = ((AsyncDataServiceBean) serviceBean).saveObject(createdAcquaintance, ExecuteOption.SYNC);
			serviceBean.doAction(new UnlockObject(acquaintanceCardId));
			newAcquaintanceIds.add(acquaintanceCardId);
		}
		return newAcquaintanceIds;
	}
	
	/**
	 * ���������� ������� �� �������� ���� �� ����.
	 * ���� ������� �� ����������, �� ������ ����� � ����� ��� � ��������
	 * @param card
	 * @param attrId
	 * @return
	 */
	private Attribute getCreateAttribute(Card card, ObjectId attrId){
		 Attribute attribute = getAttribute(card, attrId);
		 if(null==attribute){
			 attribute = createAttribute(attrId);
			 card.getAttributes().add(attribute);
		 }
		 return attribute;
	}
	
	private Attribute createAttribute(ObjectId attrId){
		return AttrUtils.createAttribute(attrId);
	}
	
	private Attribute getAttribute(Card card, ObjectId attrId){
		return card.getAttributeById(attrId);		
	}
	
	private boolean checkEmptyString(String value){
		if(null==value || value.isEmpty()){
			return true;
		}
		return false;
	}
	
	
	
	/**
	 * Performs linking of main card with just created acquaintance cards.
	 * 
	 * @param request
	 * @param serviceBean
	 * @param sessionBean
	 * @param newAcquaintances
	 */
	private void linkAcquaintancesWithMainDoc(ActionRequest request, DataServiceBean serviceBean, AcquaintancePortletSessionBean sessionBean, List<ObjectId> newAcquaintances)  
		throws DataException, ServiceException {
		String parentCardId = sessionBean.getCardId();
		if (StringUtils.hasLength(parentCardId) && newAcquaintances.size() > 0) {
			final ObjectId cardId = new ObjectId(Card.class, Integer.parseInt(parentCardId));
			boolean locked = false;
			try {
				serviceBean.doAction(new LockObject(cardId));
				locked = true;
				final Card mainCard = (Card) serviceBean.getById(cardId);
				CardLinkAttribute attr = (CardLinkAttribute) mainCard.getAttributeById(DOCUMENT_INFORMATION_CARDLINK_ID);
				if (attr == null) {
					attr = new CardLinkAttribute();
					attr.setId((String) DOCUMENT_INFORMATION_CARDLINK_ID.getId());
					mainCard.getAttributes().add(attr);
				}
				attr.addIdsLinked(newAcquaintances);
				//��������� � ���������� ���������� ���-��� � ����� � ���������� ��� ���������� 
				//�������� ������������
				((AsyncDataServiceBean) serviceBean).saveObject(mainCard, ExecuteOption.SYNC);
				//serviceBean.saveObject(mainCard);
			} catch (DataException de) {
				logger.error("Error updating parent card:", de);
				throw de;
			} catch (ServiceException se) {
				logger.error("Error updating parent card:", se);
				throw se;
			} finally {
				if (locked) {
					unlockObject(cardId, serviceBean);
				}
			}
		}
	}

	/**
	 * Performs workflow move to send acquaintance card to its recipient.
	 * 
	 * @param serviceBean
	 * @param newAcquaintances
	 * @throws DataException
	 * @throws ServiceException
	 */
	private void sendCreatedAcquaintances(DataServiceBean serviceBean, List<ObjectId> newAcquaintances) throws DataException, ServiceException {
		for (ObjectId theId : newAcquaintances) {
			final Card acquaintanceCard = (Card) serviceBean.getById(theId);
			final WorkflowMove wfm = (WorkflowMove) serviceBean.getById(WFM_SEND_ACQUAINTANCE);
			final ChangeState actionChange = new ChangeState();
			actionChange.setCard(acquaintanceCard);
			actionChange.setWorkflowMove(wfm);
			//�������� ������������ ���� �������� ���������
			((AsyncDataServiceBean) serviceBean).doAction(actionChange, ExecuteOption.SYNC);
		}
	}
}
