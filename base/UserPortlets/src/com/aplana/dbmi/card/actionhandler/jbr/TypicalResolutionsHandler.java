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
/*
 * BR4J00000867: 23.11.2010 - N.Zhegalin
 */
package com.aplana.dbmi.card.actionhandler.jbr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.TextAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;

/**
 * Class allows of getting values of typical resolutions for current user.
 */
public class TypicalResolutionsHandler implements ValuesGetter {

	private static final ObjectId CARD_STATE_PUBLISHED = ObjectId.predefined(
			CardState.class, "published");
	private static final ObjectId USER_STATE_ACTIVE = ObjectId.predefined(
			CardState.class, "user.active");

	private static final ObjectId BOSS_TEMPLATE_ID = ObjectId.predefined(
			Template.class, "boss.settings");
	private static final ObjectId PERSON_TEMPLATE_ID = ObjectId.predefined(
			Template.class, "jbr.internalPerson");

	private static final ObjectId ARM_OWNER_ID = ObjectId.predefined(
			PersonAttribute.class, "boss.owner");
	private static final ObjectId ARM_ASSISTANT_ID = ObjectId.predefined(
			PersonAttribute.class, "boss.assistant");
	private static final ObjectId PERS_OWNER_ID = ObjectId.predefined(
			PersonAttribute.class, "jbr.person.owner");
	private static final ObjectId PERS_SETTINGS_ID = ObjectId.predefined(
			CardLinkAttribute.class, "jbr.settings.card");
	
	private static final ObjectId ARM_RESOL_LIST_ID = ObjectId.predefined(
			CardLinkAttribute.class, "boss.resolutiontemplates");
	private static final ObjectId PERSON_RESOL_LIST_ID = ObjectId.predefined(
			CardLinkAttribute.class, "jbr.person.resolutiontemplates");

	private static final ObjectId ARM_RESOLUTION = ObjectId.predefined(
			TextAttribute.class, "jbr.typeResolution.resolution");

	private Log logger = LogFactory.getLog(getClass());

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.aplana.dbmi.card.actionhandler.jbr.ValuesGetter#getValues(com.aplana.dbmi.service.DataServiceBean)
	 */
	public List<String> getValues(DataServiceBean serviceBean) {
		List<String> values = null;
		try {
			// �� �������� �������� ��������� �� ���������� ��� �������������, 
			// ��� � �������� ��������� ��̻ ������ ������� ������������;
			values = getArmTypicalResolutValues(BOSS_TEMPLATE_ID, ARM_OWNER_ID, serviceBean);
			if (values.size() == 0 ) {
				// �� �������� �������� ��������� �� ���������� ��� �������������, 
				// ��� � �������� ��������� ������ ������� ������������
				values = getArmTypicalResolutValues(BOSS_TEMPLATE_ID, ARM_ASSISTANT_ID, serviceBean);
				if (values.size() == 0 ) {
					// �� �������� �������� ��������� �� ���������� ������������� 
					// ��� �������� ������������ � ��������� �������.
					values = getPersTypicalResolutValues(serviceBean);
				}
			}
		} catch (Exception e) {
			logger.error("Failed to load typical resolutions values", e);
		}
		return values;
	}

	private List<String> getArmTypicalResolutValues(ObjectId templateId, ObjectId personId, 
			DataServiceBean serviceBean) throws Exception
	{
		List<String> values = new ArrayList<String>();
		Search search = new Search();
		search.setByAttributes(true);
		search.setTemplates(Collections.singleton(DataObject
				.createFromId(templateId)));
		search.setStates(Collections.singleton(CARD_STATE_PUBLISHED));
		search.addPersonAttribute(personId, serviceBean.getPerson()
				.getId());
		search.setFetchLink(ARM_RESOL_LIST_ID);

		List<SearchResult.Column> columns = new ArrayList<SearchResult.Column>();
		SearchResult.Column column = new SearchResult.Column();
		column.setAttributeId(ARM_RESOLUTION);
		columns.add(column);
		search.setColumns(columns);

		@SuppressWarnings("unchecked")
		List<Card> cards = ((SearchResult) serviceBean.doAction(search))
		.getCards();
		for (Card card : cards) {
			TextAttribute resolutionTextAttribute = (TextAttribute) card
					.getAttributeById(ARM_RESOLUTION);
			values.add(resolutionTextAttribute.getValue());
		}
		return values;
	}

	private List<String> getPersTypicalResolutValues(DataServiceBean serviceBean) 
			throws Exception
	{
		List<String> values = new ArrayList<String>();
		Search search = new Search();
		search.setByAttributes(true);
		search.setTemplates(Collections.singleton(DataObject
				.createFromId(PERSON_TEMPLATE_ID)));
		search.setStates(Collections.singleton(USER_STATE_ACTIVE));
		search.addPersonAttribute(PERS_OWNER_ID, serviceBean.getPerson()
				.getId());
		search.setFetchLink(PERS_SETTINGS_ID);

		List<SearchResult.Column> columns = new ArrayList<SearchResult.Column>();
		SearchResult.Column column = new SearchResult.Column();
		column.setAttributeId(PERSON_RESOL_LIST_ID);
		columns.add(column);
		search.setColumns(columns);

		@SuppressWarnings("unchecked")
		List<Card> settingCards = ((SearchResult) serviceBean.doAction(search))
				.getCards();

		if (settingCards.size() > 0) {
			Card settingCard = settingCards.get(0);
			CardLinkAttribute resolListAttr = (CardLinkAttribute) settingCard
					.getAttributeById(PERSON_RESOL_LIST_ID);
			if (null != resolListAttr) {
				List<ObjectId> resolCardIds = resolListAttr.getIdsLinked();
				for (ObjectId resolCardId : resolCardIds) {
					Card resolCard = (Card) serviceBean.getById(resolCardId);
					TextAttribute resolutionTextAttribute = (TextAttribute) resolCard
							.getAttributeById(ARM_RESOLUTION);
					values.add(resolutionTextAttribute.getValue());
				}
			}
		}
		return values;
	}
}
