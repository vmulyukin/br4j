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
package com.aplana.dbmi.jbr.processors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.aplana.dbmi.action.Action;
import com.aplana.dbmi.action.ListProject;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.IntegerAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.ObjectQueryBase;

public class DoGetAttachments extends ActionQueryBase {
	// ������������� �������� ����������
	private static final ObjectId SIGN_TEMPLATE_ID = ObjectId.predefined(
			Template.class, "jbr.sign");
	private static final ObjectId ATTR_ITERATION = ObjectId.predefined(IntegerAttribute.class, "jbr.version");

	// � �������� ��������� �������� ������� ���� �������� ���������� �� ��
	private static final ObjectId ATTR_LAST_ITERATION = ObjectId.predefined(IntegerAttribute.class, "jbr.visa.round");
	
	private static final ObjectId INCOMING_TEMPLATE_ID = ObjectId.predefined(
			Template.class, "jbr.incoming");
	private static final ObjectId OUTCOMING_TEMPLATE_ID = ObjectId.predefined(
			Template.class, "jbr.outcoming");
	private static final ObjectId NPA_TEMPLATE_ID = ObjectId.predefined(
			Template.class, "jbr.npa");
	private static final ObjectId OG_TEMPLATE_ID = ObjectId.predefined(
			Template.class, "jbr.incomingpeople");
	private static final ObjectId ORD_TEMPLATE_ID = ObjectId.predefined(
			Template.class, "jbr.ord");
	private static final ObjectId INTERNAL_TEMPLATE_ID = ObjectId.predefined(
			Template.class, "jbr.interndoc");

	// �������������� ��������� ��������
	private static final ObjectId SIGN_PARENT_DOCUMENT_BACKlINK_ID = ObjectId
			.predefined(BackLinkAttribute.class, "jbr.sign.parent");
	private static final ObjectId FILES_CARDLINK_ID = ObjectId.predefined(
			CardLinkAttribute.class, "jbr.files");

	private static final Map<ObjectId, ObjectId> parentLinks = ObjectIdUtils.stringToIdsMap(
			"jbr.sign=jbr.sign.parent,"
			+ "jbr.visa=jbr.visa.parent,"
			+ "jbr.examination=jbr.exam.parent",
			Template.class, true, BackLinkAttribute.class, false
	);
	
	/**
	 * ��������� �������� ���������
	 * 
	 * @param document
	 * @return
	 * @throws DataException
	 */
	private List<Card> getDocumentAttachments(Card document, boolean lastIterationOnly)
			throws DataException {
		List<Card> result = new ArrayList<Card>();
		final ObjectQueryBase query = getQueryFactory().getFetchQuery(
				Card.class);

		CardLinkAttribute filesAttribute = (CardLinkAttribute) document
				.getAttributeById(FILES_CARDLINK_ID);
		ObjectId[] fileIds = filesAttribute.getIdsArray();
		if(fileIds == null) return result;
		// ���� ����� �������� ���������� �����, �� ��������� �������� ������� �� ������ ����� ��������, ����� 0
		IntegerAttribute lastIterationAttribute = (IntegerAttribute) document.getAttributeById(ATTR_LAST_ITERATION);
		logger.info("Last iteration attribute for document "+document.getId()+" is "+lastIterationAttribute);
		int lastIteration = (lastIterationAttribute!=null)?lastIterationAttribute.getValue():0;
		logger.info("Value of last iteration attribute for document "+document.getId()+" is "+lastIteration);
		for (int i = 0; i < fileIds.length; i++) {
			query.setId(fileIds[i]);
			Card fileCard = (Card) getDatabase().executeQuery(getUser(), query);
			if(lastIterationOnly){
				int fileIteration = ((IntegerAttribute) fileCard.getAttributeById(ATTR_ITERATION)).getValue();
				logger.info("Value of iteration for filcard "+fileCard.getId()+" is "+fileIteration);
				if(fileIteration < lastIteration){ 
					continue;
				}
				else if(fileIteration == lastIteration){ 
					result.add(fileCard);
				}
			}
			else{
				result.add(fileCard);
			}
		}
		
		return result;
	}	

	/**
	 * ���������� ��������
	 * 
	 * @param action
	 * @return
	 * @throws DataException
	 */
	private Object execAction(Action action) throws DataException {
		final ActionQueryBase query = getQueryFactory().getActionQuery(action);
		query.setAction(action);
		return getDatabase().executeQuery(getUser(), query);
	}

	/**
	 * ������� ����� ������
	 */
	@Override
	public Object processQuery() throws DataException {
		GetAttachments action = (GetAttachments) getAction();

		// �������� �������� �� ��������������
		final ObjectQueryBase query = getQueryFactory().getFetchQuery(
				Card.class);
		query.setId(action.getCardId());
		Card card = (Card) getDatabase().executeQuery(getUser(), query);

		List<Card> result = new ArrayList<Card>();

		if (parentLinks.containsKey(card.getTemplate())) {
			// �������� �������� ���������
			final BackLinkAttribute documentBackLinkAttr = (BackLinkAttribute) card
					.getAttributeById(parentLinks.get(card.getTemplate()));
			ListProject listAction = new ListProject();
			listAction.setCard(card.getId());
			listAction.setAttribute(documentBackLinkAttr.getId());
			SearchResult execResult = (SearchResult) execAction(listAction);
			Card document = (Card) execResult.getCards().get(0);
			//�������������� ��������, ����� �� ��� �� ����� ����������
			query.setId(document.getId());
			document = (Card) getDatabase().executeQuery(getUser(), query);

			// �������� �������� ���������
			result = getDocumentAttachments(document, action.getOnlyFromLastIteration());
		} else if (card.getTemplate().equals(INCOMING_TEMPLATE_ID)
				|| card.getTemplate().equals(OUTCOMING_TEMPLATE_ID)
				|| card.getTemplate().equals(NPA_TEMPLATE_ID)
				|| card.getTemplate().equals(OG_TEMPLATE_ID)
				|| card.getTemplate().equals(ORD_TEMPLATE_ID)
				|| card.getTemplate().equals(INTERNAL_TEMPLATE_ID)) {
			// �������� �������� ���������
			result = getDocumentAttachments(card, action.getOnlyFromLastIteration());
		} else {
			throw new DataException(
					"Get attachments from card with template id = "
							+ card.getTemplateName() + " not supported");
		}
		return result;
	}

}
