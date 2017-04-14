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
package com.aplana.dbmi.module.notif;

import java.util.Collection;
import java.util.Collections;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.action.NotifPersonFlagGroupChecker;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.TreeAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.ObjectQueryBase;

/**
 * �����, �������������� �������� �������� ������������ �� ������� ������������� �������� ���
 * �����������. ������������ ��� ������������� � ����� <code>beans.xml</code> � �������� ���������
 * {@link NotificationBean#setPersonNotifyChecker(PersonNotifyChecker)}.
 * ����� ����, ������ ���� ����� ������������ ��� ������������ �������� �������� � �������
 * {@link NotificationBean#setPersonNotifyFlag(String)} � {@link NotificationBean#setPersonNotifyFlagId(ObjectId)}.
 * 
 * ����� ��������� ������� <code>referencevalue.notification.events</code> �������� ������������
 * � ��������� ������� � �� ��������, �������������� ������� {@link #setFlagId(ObjectId)}.
 * �������� ����������� �����������, ���� ����� �������� �������.
 *  
 * @author apirozhkov
 */
public class PersonFlagChecker extends DataServiceClient implements PersonNotifyChecker {

	private static final ObjectId SETTINGS_CARD_ID = ObjectId.predefined(CardLinkAttribute.class, "jbr.settings.card");
	
	protected Log logger = LogFactory.getLog(getClass());
	private ObjectId flagId;
	private ObjectId fieldId = ObjectId.predefined(TreeAttribute.class, "notification.events");
	
	public void setFieldId(ObjectId fieldId) {
		if (fieldId == null || !TreeAttribute.class.equals(fieldId.getType()))
			throw new IllegalArgumentException("fieldId must be a tree attribute id");
		this.fieldId = fieldId;
	}

	/**
	 * ������������� ������������� ����������� �������� ({@link ReferenceValue}), �������������
	 * ������������� �������� ����������� � ������ ������.
	 * 
	 * @param flagId ������������� �������� �����������, ������������� ���������
	 * 		<code>referencevalue.notification.events</code>
	 */
	public void setFlagId(ObjectId flagId) {
		if (flagId == null || !ReferenceValue.class.equals(flagId.getType()))
			throw new IllegalArgumentException("flagId must be a reference value id");
		this.flagId = flagId;
	}

	@Override
	public boolean checkNotify(Person person, NotificationObject object) {
		if (flagId == null)
			throw new IllegalStateException("flagId must be set before use");
		if (person.getCardId() == null) {
			logger.warn("Person " + person.getFullName() + " (id " + person.getId().getId() + ") has no card");
			return false;
		}
		try {
			ObjectQueryBase query = getQueryFactory().getFetchQuery(Card.class);
			query.setId(person.getCardId());
			Card personCard = (Card) getDatabase().executeQuery(getSystemUser(), query);
			
			query = getQueryFactory().getFetchQuery(Card.class);
			query.setId(((CardLinkAttribute)personCard.getAttributeById(SETTINGS_CARD_ID)).getSingleLinkedId());
			Card settingsCard = (Card) getDatabase().executeQuery(getSystemUser(), query);
			
			TreeAttribute flags = (TreeAttribute) settingsCard.getAttributeById(fieldId);
			return flags != null && flags.hasValue(flagId);
		} catch (DataException e) {
			logger.error("Error fetching card " + person.getCardId().getId() +
					" for " + person.getFullName() + " (id " + person.getId().getId() + ")", e);
			return false;
		}
		//return false;
	}

	@Override
	public Collection<Person> checkNotify(Collection<Person> sourcePersons,
			NotificationObject object) {
		NotifPersonFlagGroupChecker notifPersonFlagGroupChecker = new NotifPersonFlagGroupChecker();
		notifPersonFlagGroupChecker.setPersons(sourcePersons);
		notifPersonFlagGroupChecker.setFieldId(fieldId);
		notifPersonFlagGroupChecker.setFlagId(flagId);
		try {
			ActionQueryBase actionQuery = getQueryFactory().getActionQuery(notifPersonFlagGroupChecker);
			actionQuery.setAction(notifPersonFlagGroupChecker);
			return (Collection<Person>)getDatabase().executeQuery(getSystemUser(), actionQuery);
		} catch (DataException e) {
			logger.error("Error group check notifs", e);
			return Collections.emptyList();
		}
		
	}
	
	
}
