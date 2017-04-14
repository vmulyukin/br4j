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
import java.util.Iterator;

import com.aplana.dbmi.action.GetResponsibleDowRecipients;
import com.aplana.dbmi.action.ListProject;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.TypedCardLinkAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.ObjectQueryBase;

/**
 * ����������� ������ ����������� - ������������� �� ���.
 *  �.�. ������ ������� (������ ������ �����������), �� ��������� ������� �������� �� ������������ - ����� ����������� ����.
 *
 * @author echirkov
 */
public class ResponsibleDowSpecialRecipients extends DataServiceClient implements RecipientGroup
{
	private String attribute;

	public Collection discloseRecipients(NotificationObject object) {
		GetResponsibleDowRecipients responsibleDowRecipients = new GetResponsibleDowRecipients();
		ObjectId cardId = ((SingleCardNotification) object).getCard().getId();
		responsibleDowRecipients.setDocument(cardId);
		try {
			ActionQueryBase actionQueryBase = getQueryFactory().getActionQuery(responsibleDowRecipients);
			actionQueryBase.setAction(responsibleDowRecipients);
			actionQueryBase.setAccessChecker(null);
			return (Collection<Person>)getDatabase().executeQuery(getSystemUser(), actionQueryBase);
		} catch (DataException e) {
			// TODO Auto-generated catch block
			logger.warn("Error getting card ResponsibleDowRecipients " + cardId.getId() + "; skipped", e);
			return Collections.emptyList();
		}
	}
}
