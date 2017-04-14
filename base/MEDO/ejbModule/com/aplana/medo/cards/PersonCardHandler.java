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
package com.aplana.medo.cards;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.aplana.dbmi.action.StrictSearch;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;

/**
 * Class provides methods that allows to find person or create external person
 * with given parameters.
 */
public class PersonCardHandler extends CardHandler {

	private static final ObjectId TEMPLATE_ID = ObjectId.predefined(
			Template.class, "jbr.externalPerson");
	private static final ObjectId[] SEARCH_SOURCE = {
			ObjectId.predefined(Template.class, "jbr.internalPerson"),
			TEMPLATE_ID };

	private static final ObjectId NAME_ID = ObjectId.predefined(
			StringAttribute.class, "jbr.person.firstName");
	private static final ObjectId MIDDLE_NAME_ID = ObjectId.predefined(
			StringAttribute.class, "jbr.person.middleName");
	private static final ObjectId LAST_NAME_ID = ObjectId.predefined(
			StringAttribute.class, "jbr.person.lastName");
	private static final ObjectId ORGANIZATION_ID = ObjectId.predefined(
			CardLinkAttribute.class, "jbr.person.organization");
	private static final ObjectId POSITION_ID = ObjectId.predefined(
			StringAttribute.class, "jbr.person.position");
	private static final ObjectId DEPARTMENT_ID = ObjectId.predefined(
			StringAttribute.class, "jbr.person.department");

	private String name = "";
	private String middlename = "";
	private String lastname = "";
	private long organizationId = -1;
	private String position = "";
	private String department = "";

	public PersonCardHandler() {
	}

	/**
	 * Creates instance of class with state allows to find person according to
	 * it first-name, middle-name, last-name and organization or create new one
	 * (external) with such parameters and given department and position.
	 * 
	 * @param name
	 *            - name of person
	 * @param middlename
	 *            - middle-name of person
	 * @param lastname
	 *            - last-name of person
	 * @param organizationId
	 *            - organization where person is worked or -1 if persons without
	 *            organization should be found
	 * @param department
	 *            - department where person is worked (used only during creation
	 *            of card)
	 * @param position
	 *            - position of person in organization (used only during
	 *            creation of card)
	 */
	public PersonCardHandler(String name, String middlename, String lastname,
			long organizationId, String department, String position) {
		this.name = name;
		this.middlename = middlename;
		this.lastname = lastname;
		this.organizationId = organizationId;
		this.department = department;
		this.position = position;
	}

	/**
	 * <p>
	 * Method is used in the {@link #getCardId()} and implements behavior of
	 * card id calculating.
	 * </p>
	 * <p>
	 * The following behavior is implemented:
	 * <ol>
	 * <li>Searches cards according to current state</li>
	 * <li>If there were found more than one cards returns ID of first of them</li>
	 * <li>If no cards were found creates new one according to values of
	 * parameters</li>
	 * </ol>
	 * </p>
	 * 
	 * @return ID of first of found cards or ID of created organization
	 * @throws CardException
	 */
	@Override
	protected long calculateCardId() throws CardException {
		List<ObjectId> cards = findCards();

		if (cards.size() > 1) {
			logger.warn("More than one person was found. First of them will be used");
		}
		if (!cards.isEmpty()) {
			return (Long) cards.get(0).getId();
		}

		// If person was not found, create it
		logger.info("Person was not found.");
		return createCard();
	}

	/**
	 * Create card that have given parameters (see
	 * {@link #PersonCardHandler(String, String, String, long, String, String)})
	 * and returns ID of that.
	 * 
	 * @return ID of created card
	 * @throws CardException
	 */
	private long createCard() throws CardException {
		logger.info("Trying to create person card according to current state: "
				+ getParameterValuesLog());

		Map<ObjectId, Object> attributeValues = new HashMap<ObjectId, Object>();
		attributeValues.put(NAME_ID, name);
		attributeValues.put(MIDDLE_NAME_ID, middlename);
		attributeValues.put(LAST_NAME_ID, lastname);
		attributeValues.put(POSITION_ID, position);
		attributeValues.put(DEPARTMENT_ID, department);

		if (organizationId != -1) {
			attributeValues.put(ORGANIZATION_ID, organizationId);
		}
		return createCard(TEMPLATE_ID, attributeValues, "jbr.medo.card.person.creationFailed");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aplana.medo.cards.CardHandler#getParameterValuesLog()
	 */
	@Override
	protected String getParameterValuesLog() {
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append(String.format("firstName='%s', ", name));
		logBuilder.append(String.format("middleName='%s', ", middlename));
		logBuilder.append(String.format("lastname='%s', ", lastname));
		logBuilder.append(String.format("organizationId='%d', ", organizationId));
		logBuilder.append(String.format("department='%d', ", organizationId));
		logBuilder.append(String.format("position='%d'", organizationId));
		return logBuilder.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aplana.medo.cards.CardHandler#search()
	 */
	@Override
	protected List<ObjectId> search() throws CardException {
		DataServiceBean serviceBean = getServiceBean();

		StrictSearch search = new StrictSearch();

		search.addStringAttribute(NAME_ID, name);
		search.addStringAttribute(LAST_NAME_ID, lastname);

		if (middlename != null && !middlename.trim().isEmpty()) {
			search.addStringAttribute(MIDDLE_NAME_ID, middlename);
		}

		if (organizationId != -1) {
			search.addCardLinkAttribute(ORGANIZATION_ID, new ObjectId(
					Card.class, organizationId));
		}

		List<Template> templates = new ArrayList<Template>();
		for (ObjectId id : SEARCH_SOURCE) {
			templates.add((Template) DataObject.createFromId(id));
		}
		search.setTemplates(templates);

		try {
			List<ObjectId> cards = serviceBean.doAction(search);
			// If organization should be empty, remove from list persons with
			// non-empty organization value
			if (organizationId == -1) {
				for (Iterator<ObjectId> iterator = cards.iterator(); iterator
						.hasNext();) {
					ObjectId cardId = iterator.next();
					Card card = (Card) serviceBean.getById(cardId);
					CardLinkAttribute organizationAttribute = (CardLinkAttribute) card
							.getAttributeById(ORGANIZATION_ID);
					if (!organizationAttribute.getIdsLinked().isEmpty()) {
						iterator.remove();
					}
				}
			}
			return cards;
		} catch (DataException ex) {
			throw new CardException("jbr.medo.card.person.searchFailed", ex);
		} catch (ServiceException ex) {
			throw new CardException("jbr.medo.card.person.searchFailed", ex);
		}
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the middlename
	 */
	public String getMiddlename() {
		return this.middlename;
	}

	/**
	 * @param middlename
	 *            the middlename to set
	 */
	public void setMiddlename(String middlename) {
		this.middlename = middlename;
	}

	/**
	 * @return the lastname
	 */
	public String getLastname() {
		return this.lastname;
	}

	/**
	 * @param lastname
	 *            the lastname to set
	 */
	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	/**
	 * @return the organizationId
	 */
	public long getOrganizationId() {
		return this.organizationId;
	}

	/**
	 * @param organizationId
	 *            the organizationId to set
	 */
	public void setOrganizationId(long organizationId) {
		this.organizationId = organizationId;
	}

	/**
	 * @return the department
	 */
	public String getDepartment() {
		return this.department;
	}

	/**
	 * @param department
	 *            the department to set
	 */
	public void setDepartment(String department) {
		this.department = department;
	}

	/**
	 * @return the position
	 */
	public String getPosition() {
		return this.position;
	}

	/**
	 * @param position
	 *            the position to set
	 */
	public void setPosition(String position) {
		this.position = position;
	}
}
