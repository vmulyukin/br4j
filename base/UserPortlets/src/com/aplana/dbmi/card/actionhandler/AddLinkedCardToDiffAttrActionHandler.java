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
package com.aplana.dbmi.card.actionhandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.card.CardLinkPickerAttributeEditor;
import com.aplana.dbmi.card.CardPortletCardInfo;
import com.aplana.dbmi.card.CardPortletSessionBean;
import com.aplana.dbmi.card.CardPortletSessionBean.PortletMessage.PortletMessageType;
import com.aplana.dbmi.card.cardlinkpicker.descriptor.CardLinkPickerDescriptor;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.TypedCardLinkAttribute;
import com.aplana.dbmi.model.util.ObjectIdUtils;

/**
 * ��������� ������������� ����� �� �������� � ����������� � �����������
 * ���������. <br>
 * <b>anotherAttr</b> - ���� ���������� ����� ������� <b>diffTemplate</b><br>
 * ������������ ������ ������ � <b>useThisAttrInsteadOfCardId</b>
 * 
 */
public class AddLinkedCardToDiffAttrActionHandler extends
		AddLinkedCardActionHandler {
	private static Log logger = LogFactory
			.getLog(AddLinkedCardToDiffAttrActionHandler.class);
	public static final String ANOTHER_CARD_LINK = "anotherAttr";
	public static final String DIFF_TEMPLATE = "diffTemplate";

	private ObjectId anotheAttrId;
	private ObjectId diffTemplateId;

	protected static class TypedCardLinkItemCloseHandlerEx implements
			CardPortletCardInfo.CloseHandler {
		private ObjectId cardLinkId;
		private ObjectId idsToLinkId;
		private CardPortletSessionBean sessionBean;
		private ObjectId anotherCardLinkId;
		private ObjectId diffTemplateId;

		public TypedCardLinkItemCloseHandlerEx(ObjectId cardLinkId,
				ObjectId idsToLinkId, CardPortletSessionBean sessionBean,
				ObjectId anotherCardLinkId, ObjectId diffTemplate) {
			this.cardLinkId = cardLinkId;
			this.sessionBean = sessionBean;
			this.idsToLinkId = idsToLinkId;
			this.anotherCardLinkId = anotherCardLinkId;
			this.diffTemplateId = diffTemplate;

		}

		@SuppressWarnings("unchecked")
		public void afterClose(CardPortletCardInfo closedCardInfo,
				CardPortletCardInfo previousCardInfo) {
			if (idsToLinkId == null
					|| idsToLinkId.getType().equals(PersonAttribute.class)
					|| diffTemplateId == null || anotherCardLinkId == null) {
				new TypedCardLinkItemCloseHandler(cardLinkId, idsToLinkId,
						sessionBean).afterClose(closedCardInfo,
						previousCardInfo);
			} else {
				CardLinkAttribute idsToLinkAttr = (CardLinkAttribute) closedCardInfo
						.getCard().getAttributeById(idsToLinkId);
				List<ObjectId> linked = idsToLinkAttr.getIdsLinked();

				Search search = new Search();
				search.setByCode(true);
				SearchResult.Column col = new SearchResult.Column();
				col.setAttributeId(Card.ATTR_TEMPLATE);
				search.setColumns(Collections.singletonList(col));
				search.setWords(idsToLinkAttr.getLinkedIds());
				SearchResult result = null;
				List<Card> cards = new ArrayList<Card>();
				try {
					result = (SearchResult) sessionBean.getServiceBean()
							.doAction(search);
					cards = result.getCards();
				} catch (Exception e) {
					logger.error("Exception during fetching cards for attribute "
							+ idsToLinkAttr.getId().getId()
							+ ": "
							+ e.getMessage());
					e.printStackTrace();
				}
				List<ObjectId> cardIds = new ArrayList<ObjectId>();
				for (Card card : cards) {
					if (card.getTemplate().equals(diffTemplateId))
						cardIds.add(card.getId());
				}

				if (!cardIds.isEmpty()) {
					idsToLinkAttr.setIdsLinked(cardIds);
					new TypedCardLinkItemCloseHandler(anotherCardLinkId,
							idsToLinkId, sessionBean).afterClose(
							closedCardInfo, previousCardInfo);
					linked.removeAll(cardIds);
				}
				idsToLinkAttr.setIdsLinked(linked);
				new TypedCardLinkItemCloseHandler(cardLinkId, idsToLinkId,
						sessionBean).afterClose(closedCardInfo,
						previousCardInfo);

			}
			sessionBean.setEditorData(cardLinkId, CardLinkPickerAttributeEditor.KEY_CACHE_RESET, true);
		}
	}

	@Override
	protected void openNewCard() {
		CardPortletSessionBean sessionBean = getCardPortletSessionBean();
		try {
			sessionBean.openNestedCard(createCard(),
					new TypedCardLinkItemCloseHandlerEx(attribute.getId(),
							idsToLinkAttrId, sessionBean, anotheAttrId,
							diffTemplateId), true);
		} catch (Exception e) {
			logger.error("Can't redirect to card editing page", e);
			sessionBean.setMessageWithType("edit.link.error.create",
					new Object[] { e.getMessage() }, PortletMessageType.ERROR);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setParameter(String name, String value) {
		if (ANOTHER_CARD_LINK.equals(name)) {
			anotheAttrId = ObjectIdUtils.getObjectId(Arrays.asList(
					CardLinkAttribute.class, TypedCardLinkAttribute.class,
					PersonAttribute.class), CardLinkAttribute.class, value
					.trim(), false);
		} else if (DIFF_TEMPLATE.equals(name)) {
			diffTemplateId = ObjectIdUtils.getObjectId(Template.class,
					value.trim(), true);
		} else {
			super.setParameter(name, value);
		}

	}
}
