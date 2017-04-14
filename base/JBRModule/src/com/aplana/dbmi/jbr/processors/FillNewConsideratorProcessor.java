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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.DatedTypedCardLinkAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.service.DataException;

/**
 * ���������, ������� ��������� ������������ ���������� �������� "����� ���������������" � �� "������ �� ��������� ����������������".
 * ���� �� ������ "��� ������������", �� � ������ ���� ������� "������ �� ����� �������������� ����������������" ���������� �������� "��",
 * ����� "���".
 * ��� ��, ���� �� ������� ���� ������������, �� �� �������� ������� �� ��������� �������� ������������.
 * 
 * @author aklyuev
 *
 */
public class FillNewConsideratorProcessor extends SetAttributeValueProcessor {
	private static final long serialVersionUID = 3L;

	public static final ObjectId NEW_RASSM_ATTR_ID = ObjectId.predefined(DatedTypedCardLinkAttribute.class, "jbr.request.new");
	public static final ObjectId REQUEST_TYPE_ATTR_ID = ObjectId.predefined(ListAttribute.class, "jbr.request.type");
	public static final ObjectId CONSIDERATOR_CHANGE_RESPON = ObjectId.predefined(ReferenceValue.class, "jbr.considerator.change.respon");
	public static final ObjectId REQUEST_CHANGE_TEMPLATE_ID = ObjectId.template("jbr.request.change");
	public static final ObjectId YES = ObjectId.predefined(ReferenceValue.class, "jbr.commission.control.yes");
	public static final ObjectId NO = ObjectId.predefined(ReferenceValue.class, "jbr.commission.control.no");
	public static final ObjectId PREV_CONS_ATTR_ID = ObjectId.predefined(CardLinkAttribute.class, "jbr.request.cons");
	public static final ObjectId EXAM_TERM_ATTR_ID = ObjectId.predefined(DateAttribute.class, "jbr.exam.term"); //"����������� ��" � �� "������������"
	public static final DateFormat DATE_FORMAT = new SimpleDateFormat(DateAttribute.defaultTimePattern);
	protected final Log logger = LogFactory.getLog(getClass());

	@Override
	public Object process() throws DataException {
		final Card card = this.getCard();
		boolean saveAttr = false;

		Date defaultDate = getDateFromLastConsiderator(card.getId());

		//������������ ������ �������� ������� "������ �� ����� ����������������"
		if (card != null && REQUEST_CHANGE_TEMPLATE_ID.getId().equals(card.getTemplate().getId()) ){

			ListAttribute reqType = card.getAttributeById(REQUEST_TYPE_ATTR_ID);
			DatedTypedCardLinkAttribute newRassmPerson = card.getAttributeById(NEW_RASSM_ATTR_ID);
			if (newRassmPerson != null) {
				for (ObjectId personObjectId : newRassmPerson.getIdsLinked()) {
					//���� � ������ ��������������� �� ����� ���, �� ���������� 
					//�������� �� ��������� � ����������� �� ���� �������
					Long personId = (Long) personObjectId.getId();
					if (newRassmPerson.getType(personId) == null) {
						saveAttr = true;
						//���� ��� ������� �� ����� �������������� ����������������, �� ���������� �������� "��" (1449)
						if (reqType!= null && reqType.getValue() != null && CONSIDERATOR_CHANGE_RESPON.equals(reqType.getValue().getId())) {
							newRassmPerson.getTypes().put(personId, (Long)YES.getId());
						} else {
							newRassmPerson.getTypes().put(personId, (Long)NO.getId());
						}
					}

					if (newRassmPerson.getDate(personId) == null && defaultDate != null) {
						saveAttr = true;
						newRassmPerson.getDates().put(personId, defaultDate);
					}
				}
			}

			if (saveAttr) {
				saveAttribute(card, newRassmPerson);
			}
		} else {
			logger.error("Processor is not aplicable for following Template: " + card != null ? card.getTemplateName() : "null");
		}
		return null;
	}

	/**
	 * ����� ���������� �������� �������� "���� ������������" �� ������� �� "������������"
	 * @param requestCardId - ObjectId �������� �� "������ �� ��������� ���������������"
	 */
	public Date getDateFromLastConsiderator(ObjectId requestCardId) {
		if (requestCardId != null && requestCardId.getId() != null) {

			Search search = new Search();
			search.setByCode(true);
			search.setWords(String.valueOf(requestCardId.getId()));

			SearchResult.Column col = new SearchResult.Column();
			col.setAttributeId(PREV_CONS_ATTR_ID);
			col.setLabelAttrId(EXAM_TERM_ATTR_ID);
			col.setPathToLabelAttr(null);

			search.setColumns(Collections.singletonList(col));

			try {
				SearchResult result = execAction(search);
				if (result != null && result.getCards() != null) {
					if (!result.getCards().isEmpty() && result.getCards().size() == 1) {
						Card rassmCard = result.getCards().get(0);
						CardLinkAttribute attr = rassmCard.getAttributeById(PREV_CONS_ATTR_ID);
						String dateStringValue = (String) attr.getLabelLinkedMap().get(attr.getLastIdLinked());
						Date resDate = DATE_FORMAT.parse(dateStringValue);
						return resDate;
					} else {
						if (logger.isWarnEnabled()) {
							logger.warn("Unable to retreive valid consideration card");
						}
					}
				}
			} catch (Exception e) {
				if (logger.isWarnEnabled()) {
					logger.warn("Unable to retreive date from consideration due to  " + e.getMessage(), e);
				}
			}
		}
		return null;
	}
}
