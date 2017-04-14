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

import java.text.MessageFormat;
import java.util.Date;
import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.DatedTypedCardLinkAttribute;
import com.aplana.dbmi.model.IntegerAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.TypedCardLinkAttribute;
import com.aplana.dbmi.model.util.DateUtils;
import com.aplana.dbmi.service.DataException;

/**
 * 
 * @author ppolushkin
 *
 */
public class OgSendToAddresseeForConsideration extends SendToAddresseeForConsideration {

	protected static final long serialVersionUID = 1L;
	
	protected static final String PARAM_CONSIDERATION_RESPONSIBLE = "considerationResponsible";
	
	protected ObjectId ATTR_CONSIDERATION_RESPONSIBLE =
			// personattribute.jbr.incoming.addressee=JBR_INFD_RECEIVER
			ObjectId.predefined(ListAttribute.class, "jbr.responsibility.consider");
	
	protected CardLinkAttribute addressee;

	@Override
	public void setParameter(String name, String value) {
		if (name == null || value == null) return;

		boolean assigned = true;
		Object result = null;
		if (PARAM_CONSIDERATION_RESPONSIBLE.equals(name)) {
			this.ATTR_CONSIDERATION_RESPONSIBLE = IdUtils.smartMakeAttrId(value, ListAttribute.class);
			result = this.ATTR_CONSIDERATION_RESPONSIBLE;
		}  else {
			super.setParameter(name, value);
			return;
		}

		logSettingParam(assigned, name, value, result);
	}
	
	@Override
	public Object process() throws DataException 
	{
		final ChangeState move = (ChangeState) getAction();
		
		final Card doc = reloadCard(move);
		
		if(!initWorkSets(doc)) {
			return null;
		}
		
		addressee = 
				(CardLinkAttribute) doc.getAttributeById(ATTR_ADDRESSES);
		if (addressee == null)
			return false;
		
		/*
		 * ������ �� ���������, ��������� � ������ ������������...
		 */
		boolean isDocUpdated = false; // true, ���� ���� ���������
		for (Person person : personsToProcess) {
			if (existsPersonsIds.contains(person.getId()) ) 
			{
				logger.info("Consideration card for user '" + person.getFullName() + "' already exists");
				continue;
			}
			
			/*
			 * �������� ����� ��������... 
			 */
			final Card consideration = createNewConsideration();
			
			// ������ ����������������... 
			final PersonAttribute examiner = (PersonAttribute) 
				consideration.getAttributeById(ATTR_CONSIDERATION_EXAMINER);
			examiner.setPerson(person);

			// ���������� �����������...
			final IntegerAttribute order = (IntegerAttribute) 
				consideration.getAttributeById(ATTR_VISA_ORDER);
			order.setValue(0);
			
			// ������ ���� � ��������������� ���� �����
			if(TypedCardLinkAttribute.class.equals(addressee.getClass())
					|| DatedTypedCardLinkAttribute.class.equals(addressee.getClass())) {
				
				TypedCardLinkAttribute a = (TypedCardLinkAttribute) addressee;
				
				final ReferenceValue val;
				final Long valueLong = (Long) a.getTypes().get(person.getCardId().getId());
				if(valueLong != null) {
					final ObjectId valueId = new ObjectId(ReferenceValue.class, valueLong);
					val = new ReferenceValue();
					val.setId(valueId);
					
					final ListAttribute resp = (ListAttribute)
							consideration.getAttributeById(ATTR_CONSIDERATION_RESPONSIBLE);
					
					resp.setValue(val);
				}
			}
			
			final DateAttribute term = (DateAttribute)
					consideration.getAttributeById(ATTR_CONSIDERATION_ENDDATE);
			
			if(DatedTypedCardLinkAttribute.class.equals(addressee.getClass())) {
				
				DatedTypedCardLinkAttribute a = (DatedTypedCardLinkAttribute) addressee;
				
				final Date valueDate = (Date) a.getDates().get(person.getCardId().getId());
				if(valueDate != null) {
					term.setValue(valueDate);
				} else {
					term.setValue(DateUtils.addDaysToCurrent(29));
				}
			} else {
				term.setValue(new Date());
			}

			// ���������� ����� ��������...
			final ObjectId considerationId = saveCardByUser(consideration, getSystemUser());
			considerations.addLinkedId(considerationId);
			isDocUpdated = true;

			logger.info(MessageFormat.format(MSG_CONSIDERATION_SAVED_FOR_CARD_2,
					doc.getId(), considerations.getId(), considerationId ));
		}
		
		/*
		 * ���������� �������� ��������
		 */
		if (isDocUpdated) {
			saveCardByUser(doc, getSystemUser());
			move.setCard(doc);
			logger.info(MessageFormat.format(MSG_CARD_SAVED_1, doc.getId()));
		} else {
			logger.info(MessageFormat.format(MSG_CARD_NOT_SAVED_1, doc.getId()));
		}
		return (isDocUpdated) ? doc : null;
	}

}
