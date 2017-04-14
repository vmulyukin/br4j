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
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.aplana.dbmi.action.GetDeepChildren;
import com.aplana.dbmi.action.ListProject;
import com.aplana.dbmi.jbr.util.CardUtils;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.service.DataException;

public class ReplaceAssistentToHeadForReview extends ReplaceAssistentToHead {
	/* ��� ������� ��������� ������������, ������� ��� ��������� � ��� ���������, �����
	 * �������� ���������. ���� ������� ������������ �������� �������������, �� � ���
	 * ����������, ��� � �������� �������� �������� attr ������ ��� ��������, ���������������
	 * ������� ������������
	 */
	// �������� "���������"
	private static final ObjectId ATTR_RES_TO_RES = ObjectId.predefined(BackLinkAttribute.class, "jbr.linkedResolutions");
	// ������� "������������"
	private static final ObjectId ATTR_REVIEW_TO_BASE = ObjectId.predefined(BackLinkAttribute.class, "jbr.exam.parent");
	// ������� ���. ���������
	private static final ObjectId ATTR_BASE_TO_RES = ObjectId.predefined(BackLinkAttribute.class, "jbr.resolutions");

	@Override
	public Object process() throws DataException {
		final List<ObjectId> assistants = getAssistants();
		if (assistants == null)
			return null;

		// �������� �������� ���������
		final Card source = loadCard(getCardId());
		final ListProject searchBase = new ListProject();
		searchBase.setAttribute(ATTR_REVIEW_TO_BASE);
		searchBase.setCard(source.getId());

		final List<Card> list = CardUtils.execSearchCards(searchBase, getQueryFactory(), getDatabase(), getSystemUser());
		final Card baseCard = (list != null) ? list.get(0) : null;

		// ������� ��� ��������� ����������� � ������� ��������� ���������
		final Set<ObjectId> resolutionIds = getResolutionIds(baseCard.getId());

		// ��� ������� ��������� ����������, ��� ���������, � ���� ��� ��������,
		// ������ ������������
		final Person curPerson = getUser().getPerson();
		for (ObjectId resId : resolutionIds) {
			replaceSigner(resId, assistants, curPerson);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private Set<ObjectId> getResolutionIds( ObjectId cardId
			) throws DataException
	{
		final GetDeepChildren actionRes = new GetDeepChildren();
		actionRes.setDepth(10);

		final Collection<ObjectId> roots = new ArrayList<ObjectId>(1);
		roots.add(cardId);

		actionRes.setRoots(roots);
		actionRes.setChildTypeId(ATTR_BASE_TO_RES);
		actionRes.setSecondChildTypeId(ATTR_RES_TO_RES);

		return (Set<ObjectId>)execAction(actionRes);
	}
}
