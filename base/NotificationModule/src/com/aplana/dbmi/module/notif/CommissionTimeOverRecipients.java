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
import java.util.HashMap;

import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ObjectQueryBase;

public class CommissionTimeOverRecipients extends CommissionRemindRecipients {

	private static final ObjectId FLAG_EXECUTOR_CTL =
			ObjectId.predefined(ReferenceValue.class, "notification.event.commission.overdue.my.ctl");
	private static final ObjectId FLAG_EXECUTOR_NONCTL =
			ObjectId.predefined(ReferenceValue.class, "notification.event.commission.overdue.my.nonctl");
	private static final ObjectId FLAG_SUPERVISOR_CTL =
			ObjectId.predefined(ReferenceValue.class, "notification.event.commission.overdue.supervised.ctl");
	private static final ObjectId FLAG_SUPERVISOR_NONCTL =
			ObjectId.predefined(ReferenceValue.class, "notification.event.commission.overdue.supervised.nonctl");
	
	@Override
	public Collection discloseRecipients(NotificationObject object) {
		if (!SingleCardNotification.class.isAssignableFrom(object.getClass()))
			throw new IllegalArgumentException("This recipient group can only be used for card notifications");
		
		try {
			Card commission = ((SingleCardNotification) object).getCard();
			if (commission.getTemplate() == null) {		// seems that card is not loaded
				ObjectQueryBase cardQuery = getCardQuery();
				cardQuery.setId(commission.getId());
				commission = (Card) getDatabase().executeQuery(getSystemUser(), cardQuery);
			}
			final HashMap<ObjectId, Person> recipients = new HashMap<ObjectId, Person>();
			
			PersonAttribute executor = (PersonAttribute) commission.getAttributeById(
					ObjectId.predefined(PersonAttribute.class, "jbr.AssignmentExecutor"));
			addFilteredPersons(recipients, executor.getValues(),
					isCommissionOnControl(commission) ? FLAG_EXECUTOR_CTL : FLAG_EXECUTOR_NONCTL,
					null);
			addFilteredPersons(recipients, getAssistants(executor.getValues()),
					isCommissionOnControl(commission) ? FLAG_EXECUTOR_CTL : FLAG_EXECUTOR_NONCTL,
					null);
			
			PersonAttribute coexecutors = (PersonAttribute) commission.getAttributeById(
					ObjectId.predefined(PersonAttribute.class, "jbr.CoExecutor"));
			if (coexecutors.getValues().size() > 0) {
				addFilteredPersons(recipients, coexecutors.getValues(),
						isCommissionOnControl(commission) ? FLAG_EXECUTOR_CTL : FLAG_EXECUTOR_NONCTL,
						null);
				addFilteredPersons(recipients, getAssistants(coexecutors.getValues()),
						isCommissionOnControl(commission) ? FLAG_EXECUTOR_CTL : FLAG_EXECUTOR_NONCTL,
						null);
			}
			
			Card executorProfile = getPersonProfile(executor.getValue());
			CardLinkAttribute departmentLink = (CardLinkAttribute) executorProfile.getAttributeById(
					ObjectId.predefined(CardLinkAttribute.class, "jbr.personInternal.department"));
			try {
				if (departmentLink.getLinkedCount() > 0) {
					ObjectQueryBase cardQuery = getCardQuery();
					cardQuery.setId(departmentLink.getIdsArray()[0]);
					Card department = (Card) getDatabase().executeQuery(getSystemUser(), cardQuery);
					
					PersonAttribute supervisor = (PersonAttribute) department.getAttributeById(
							ObjectId.predefined(PersonAttribute.class, "jbr.department.curator"));
					if (supervisor.getValues().size() > 0) {
						addFilteredPersons(recipients, supervisor.getValues(),
								isCommissionOnControl(commission) ? FLAG_SUPERVISOR_CTL : FLAG_SUPERVISOR_NONCTL,
								null);
						addFilteredPersons(recipients, getAssistants(supervisor.getValues()),
								isCommissionOnControl(commission) ? FLAG_SUPERVISOR_CTL : FLAG_SUPERVISOR_NONCTL,
								null);
					}
				}
			} catch (DataException e) {
				logger.warn("Error fetching person's department card", e);
			}
			
			return recipients.values();
		} catch (DataException e) {
			logger.warn("Error fetching notification data", e);
			return Collections.emptyList();
		} finally {
			clearProfileCache();
		}
	}

}
