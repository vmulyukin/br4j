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
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.card.util.ARMUtils;
import com.aplana.dbmi.card.util.SearchUtils;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.TextAttribute;
import com.aplana.dbmi.model.filter.PersonCardIdFilter;
import com.aplana.dbmi.service.AsyncDataServiceBean;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.PortletUtil;
import com.aplana.dbmi.service.ServiceException;

/**
 * Represents portlet for handling common functionality of additional endorsement 
 * and sending acquaintances inside Supervisor/Minister Workstation.
 * 
 * @author EStatkevich
 */
public abstract class ResponsiblePersonsPortlet extends GenericPortlet {
		
	public static final String ACTION_INIT = "init";
	public static final String ACTION_DONE = "done";
	public static final String ACTION_CANCEL = "cancel";
	public static final String FIELD_ACTION = "formAction";
	public static final String FIELD_CARD_ID = "cardId";
	public static final String FIELD_NAMESPACE = "namespace";
	public static final String FIELD_BACK_URL = "backURL";
	public static final String FIELD_DONE_URL = "doneURL";
	public static final String FIELD_ENDORSERS = "endorserCardId";
	public static final String ENDORSERS = "endorsers";

	public static final ObjectId ATTR_COMPANY = ObjectId.predefined(PersonAttribute.class, "jbr.arm.company");
	public static final ObjectId ATTR_DOCLINKS = ObjectId.predefined(CardLinkAttribute.class, "jbr.files");
	public static final ObjectId ATTR_SHORT_DESC = ObjectId.predefined(TextAttribute.class, "jbr.document.title");
	public static final ObjectId ATTR_SHORT_CONTEXT = ObjectId.predefined(TextAttribute.class, "jbr.shortcontext");
	public static final ObjectId TEMPLATE_OG = ObjectId.predefined(Template.class, "jbr.incomingpeople");

	protected Log logger = LogFactory.getLog(getClass());

	protected abstract String getSessionBeanName();

	protected abstract String getView();

	protected abstract ResponsiblePersonsPortletSessionBean createSessionBean();

	protected abstract ResponsiblePersonsPortletSessionBean getSessionBean(PortletRequest request);

	protected abstract void prepareViewAttributes(RenderRequest request, RenderResponse response) throws DataException, ServiceException;

	protected abstract void processActionDone(ActionRequest request, ActionResponse response) throws DataException, ServiceException;

	@Override
	protected void doView(RenderRequest request, RenderResponse response) throws PortletException, PortletSecurityException, IOException {
		ResponsiblePersonsPortletSessionBean sessionBean = getSessionBean(request);

		if (sessionBean != null) {
			String msg = (null == sessionBean.getMessage()) ? "" : sessionBean.getMessage();
			try {
				prepareViewAttributes(request, response);
			} catch (DataException e) {
				msg += e.getMessage();
				logger.error(e);
			} catch (ServiceException e) {
				msg += e.getMessage();
				logger.error(e);
			} finally {
				sessionBean.setMessage((msg == null || "".equals(msg) ? null : msg));
			}

		}

		response.setContentType("text/html");
		PortletRequestDispatcher rd = getPortletContext().getRequestDispatcher(getView());
		rd.include(request, response);
	}
	
	@Override
	public void processAction(ActionRequest request, ActionResponse response) throws PortletException, PortletSecurityException, IOException {
		ResponsiblePersonsPortletSessionBean sessionBean = null;
		String action = request.getParameter(FIELD_ACTION);

		try {
			if (ACTION_INIT.equals(action)) {
				sessionBean = prepareSessionBean(request);
			} else if (ACTION_CANCEL.equals(action)) {
				sessionBean = getSessionBean(request);
				request.getPortletSession().removeAttribute(getSessionBeanName());
				response.sendRedirect(sessionBean.getBackUrl());
			} else if (ACTION_DONE.equals(action)) {
				sessionBean = getSessionBean(request);
				processActionDone(request, response);
				request.getPortletSession().removeAttribute(getSessionBeanName());
				response.sendRedirect(sessionBean.getDoneUrl());
			}
		} catch (DataException e) {
			sessionBean = getSessionBean(request);
			if (null != sessionBean) {
				sessionBean.setMessage(e.getMessage());
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

	@SuppressWarnings("unchecked")
	protected Person getPersonByCardId(String cardIdStr, DataServiceBean serviceBean) throws DataException, ServiceException {
		final ObjectId cardId = new ObjectId(Card.class, Integer.parseInt(cardIdStr));

		final List<ObjectId> filterCardIds = new ArrayList<ObjectId>(1);
		filterCardIds.add(cardId);

		final PersonCardIdFilter filter = new PersonCardIdFilter();
		filter.setCardIds(filterCardIds);

		final Collection<Person> persons = serviceBean.filter(Person.class, filter);
		final Person responsiblePerson = persons.iterator().next();
		return responsiblePerson;
	}

	protected ResponsiblePersonsPortletSessionBean prepareSessionBean(PortletRequest request) throws DataException, ServiceException, PortletException {
		final ResponsiblePersonsPortletSessionBean sessionBean = createSessionBean();

		// �������� ��� �����, ����� ����� ���� ��������� ������
		final PortletSession session = request.getPortletSession();
		session.setAttribute(getSessionBeanName(), sessionBean);

		final AsyncDataServiceBean serviceBean = PortletUtil.createService(request);
		sessionBean.setServiceBean(serviceBean);

		sessionBean.setBackUrl(request.getParameter(FIELD_BACK_URL));
		sessionBean.setDoneUrl(request.getParameter(FIELD_DONE_URL));
		sessionBean.setCardId(request.getParameter(FIELD_CARD_ID));

		// �������� ��������� �����������
		retrieveImmediateEmployees(sessionBean, serviceBean);
		
		final ObjectId baseCardId = new ObjectId(Card.class, Integer.parseInt(sessionBean.getCardId()));
		final Card baseCard = (Card) sessionBean.getServiceBean().getById(baseCardId);
		sessionBean.setBaseCard(baseCard);
		
		TextAttribute theShortDescrAttr = null;
		if (TEMPLATE_OG.equals(baseCard.getTemplate())) {
			theShortDescrAttr = (TextAttribute) baseCard.getAttributeById(ATTR_SHORT_CONTEXT);
		} else {
			theShortDescrAttr = (TextAttribute) baseCard.getAttributeById(ATTR_SHORT_DESC);
		}

		if (theShortDescrAttr != null) {
			sessionBean.setShortDescription(theShortDescrAttr.getValue());
		}
		
		CardLinkAttribute docLinks = baseCard.getCardLinkAttributeById(ATTR_DOCLINKS);
		
		Map<ObjectId, String> attachedFilesMap = ARMUtils.getAttachedFilesMap(serviceBean, docLinks);
		
		sessionBean.setBaseCardAttachedFiles(attachedFilesMap);

		sessionBean.setSwitchNavigatorLink(ARMUtils.retrieveSwitchNavigatorButton(request, serviceBean));
		
		return sessionBean;
	}
	
	/**
	 * @param sessionBean
	 * @param serviceBean
	 */
	private void retrieveImmediateEmployees(final ResponsiblePersonsPortletSessionBean sessionBean, final DataServiceBean serviceBean) {
		Card armCard = ARMUtils.getArmSettings(serviceBean);
		final List<Long> cardIds = new ArrayList<Long>();
		final List<ObjectId> immediateCardIds = new ArrayList<ObjectId>();
		try {
			final Collection<Person> companyPersons = SearchUtils.getAttrPersons(armCard, ATTR_COMPANY, false);
			if (companyPersons != null) {
				for (final Person person : companyPersons) {
					final ObjectId cardId = person.getCardId();
					immediateCardIds.add(cardId);
					cardIds.add((Long) cardId.getId());
				}
			}
		} catch (DataException ex) {
			logger.error("Error getting persons from attribute " + ATTR_COMPANY + " of card " + (armCard == null ? "null" : armCard.getId()), ex);
		}

		final Map<Long, String> names = ARMUtils.getNameByCardIds(serviceBean, cardIds);
		final Map<ObjectId, String> immediate = new LinkedHashMap<ObjectId, String>();
		for (final ObjectId cardId : immediateCardIds) {
			immediate.put(cardId, StringEscapeUtils.escapeHtml(names.get(cardId.getId())));
		}
		sessionBean.setImmediateEmployees(immediate);
	}
	
	protected void unlockObject(ObjectId objectId, DataServiceBean serviceBean) {
		try {
			serviceBean.doAction(new UnlockObject(objectId));
		} catch (Exception e) {
			logger.error("Couldn't unlock object " + objectId.toString(), e);
		}
	}
}
