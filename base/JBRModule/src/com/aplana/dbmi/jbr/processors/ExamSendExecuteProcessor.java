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

import java.sql.Types;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import com.aplana.dbmi.action.GetDeepChildren;
import com.aplana.dbmi.action.ListProject;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.jbr.util.CardUtils;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ContextProvider;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.SystemRole;
import com.aplana.dbmi.model.TextAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.UserData;

public class ExamSendExecuteProcessor extends ProcessCard{
	private static final long serialVersionUID = 1L;

	static final int depth = 20;

	// backlinkattribute.jbr.exam.parent=JBR_RASSM_PARENT_DOC = "��������-���������"
	static final ObjectId parentDocAttrId = ObjectId.predefined(BackLinkAttribute.class, "jbr.exam.parent");

	// personattribute.jbr.exam.person=JBR_RASSM_PERSON = "���������������"
	static final ObjectId examPersonAttrId = ObjectId.predefined(PersonAttribute.class, "jbr.exam.person");

	// textattribute.jbr.exam.comment=JBR_RASSM_COMMENT = "�����������"
	static final ObjectId examCommentAttrId = ObjectId.predefined(TextAttribute.class, "jbr.exam.comment");

	// backlinkattribute.jbr.resolutions=JBR_IMPL_RESOLUT = "���������"
	static final ObjectId rootAttrId = ObjectId.predefined(BackLinkAttribute.class, "jbr.resolutions");

	// backlinkattribute.jbr.linkedResolutions=JBR_RIMP_RELASSIG = "��������� ���������"
	static final ObjectId linkedAttrId = ObjectId.predefined(BackLinkAttribute.class, "jbr.linkedResolutions");

	// textattribute.jbr.resolutionText=JBR_GIPA_RESOLUT = "����� ���������"
	static final ObjectId resTextAttrId = ObjectId.predefined(TextAttribute.class, "jbr.resolutionText");

	// personattribute.jbr.AssignmentExecutor=JBR_INFD_EXEC_LINK = "�����������"
	static final ObjectId resExecutorAttrId = ObjectId.predefined(PersonAttribute.class, "jbr.AssignmentExecutor");

	// personattribute.jbr.CoExecutor =ADMIN_255974 = "�������������"
	static final ObjectId resCoExecutorsAttrId = ObjectId.predefined(PersonAttribute.class, "jbr.CoExecutor");

	// cardlinkattribute.jbr.Fyi=ADMIN_255979 = "� ��������"
	static final ObjectId resFyiAttrId = ObjectId.predefined(PersonAttribute.class, "jbr.Fyi");

	// cardlinkattribute.jbr.ExtExecutor =JBR_INFD_EXEC_EXT = "������� �����������"
	static final ObjectId resExtExecutorsAttrId = ObjectId.predefined(CardLinkAttribute.class, "jbr.ExtExecutor");

	// dateattribute.jbr.resolutionTerm=JBR_TCON_TERM = "����"
	static final ObjectId resDueDateAttrId = ObjectId.predefined(DateAttribute.class, "jbr.resolutionTerm");

	// AUTHOR = "����� ��������"
	static final ObjectId authorAttrId = new ObjectId(PersonAttribute.class, "AUTHOR");
	
	// NAME = "������������ ��������"
	static final ObjectId nameAttrId = new ObjectId(StringAttribute.class, "NAME");
	
	// �������
	private static final ObjectId MINISTER_ROLE_ID = ObjectId.predefined(SystemRole.class, "jbr.minister");

	static final ResourceBundle bundle = ResourceBundle.getBundle("jbr", ContextProvider.getContext().getLocale());
	
	@Override
	public Object process() throws DataException {
		final Card card = loadCardById(getCardId());
		execAction( new LockObject(card.getId()));
		try {		
			final PersonAttribute examPerson = (PersonAttribute)card.getAttributeById(examPersonAttrId);
		final PersonAttribute assistantsPerson = CardUtils.retrieveAssistantsByProfile(examPerson, getJdbcTemplate());
			final TextAttribute comment = (TextAttribute)card.getAttributeById(examCommentAttrId);
			
			final Card searchedDocCard = execListProject(getCardId(), parentDocAttrId, getSystemUser());
			final Card docCard = loadCardById(searchedDocCard.getId());
	
			/*
			 * �������� ������ ("���������") ...
			 */
			final Collection<ObjectId> roots = CardUtils.getCardIdsByBackLink(rootAttrId, docCard.getId(), 
					getQueryFactory(), getDatabase(), getSystemUser());
	
			/*
			 * ��������� ��� ��������� ("��������� ���������")...
			 */
			final Collection<ObjectId> ids = loadDeepChildren(roots);
	
			ids.addAll(roots); // (!) ���������� roots ����
			
			boolean isResolutionExist = false;
			StringBuilder commentBuilder = new StringBuilder();
			commentBuilder.append(comment.getStringValue());
			for ( ObjectId id: ids ) {
				Card resolutionCard = loadCardById(id);
				PersonAttribute author = (PersonAttribute)resolutionCard.getAttributeById(authorAttrId);
				if (author.intersectionValue(examPerson) || author.intersectionValue(assistantsPerson) ||
						hasStaticRole(author.getValues(), MINISTER_ROLE_ID)) {
					if (!isResolutionExist) {
						commentBuilder.append("\n" + bundle.getString("exam.comment.header.resolutions") + ": ");
						isResolutionExist = true;
					}
					appendResolutionDescription(commentBuilder, resolutionCard);
				}
			}
	
			if (isResolutionExist) {
				comment.setValue(commentBuilder.toString());
				saveCard(card, getSystemUser());
				reloadCard(getSystemUser());
			}
		} finally {
			execAction( new UnlockObject(card.getId()));
		}
		
		return null;
	}

	public void appendResolutionDescription(StringBuilder commentBuilder, Card resolutionCard) throws DataException {
		commentBuilder.append("\n" + resolutionCard.getAttributeById(resTextAttrId).getStringValue());
		commentBuilder.append("\n" + bundle.getString("exam.comment.header.executor") + ": ");
		
		PersonAttribute personAttribute = (PersonAttribute)resolutionCard.getAttributeById(resExecutorAttrId);
			// �������� ���� �� � ��������� �����������:
		if (personAttribute != null && personAttribute.getPerson() != null) {
			final Card executorCard = loadCardById( personAttribute.getPerson().getCardId() );
			commentBuilder.append(executorCard.getAttributeById(nameAttrId).getStringValue());
		} else {
			commentBuilder.append("-"); // ���� � �������� ��� �����������
		}
		
		final Collection<Person> coexecutorPersons = ((PersonAttribute)resolutionCard.getAttributeById(resCoExecutorsAttrId)).getValues();
		if (coexecutorPersons.size() > 0) {
			commentBuilder.append("\n" + bundle.getString("exam.comment.header.coexecutors") + ": ");
			//commentBuilder.append(resolutionCard.getAttributeById(resCoExecutorsAttrId).getStringValue());
			for (Iterator<Person> personIterator = coexecutorPersons.iterator(); personIterator.hasNext();) {
				final Card coexecutorCard = loadCardById( personIterator.next().getCardId() );
				commentBuilder.append(coexecutorCard.getAttributeById(nameAttrId).getStringValue());
				if (personIterator.hasNext()) {
					commentBuilder.append(", ");
				}
			}
		}
		final Collection<Person> fyiIds = ((PersonAttribute)resolutionCard.getAttributeById(resFyiAttrId)).getValues();
		if (fyiIds != null && !fyiIds.isEmpty()) {
			commentBuilder.append("\n" + bundle.getString("exam.comment.header.fyi") + ": ");
			for (Iterator<Person> currentFyiId = fyiIds.iterator(); currentFyiId.hasNext(); ){ //int i = 0; i < fyiIds.length; i++) {
				Card fyiCard = loadCardById(currentFyiId.next().getCardId());
				commentBuilder.append(fyiCard.getAttributeById(nameAttrId).getStringValue());
                if (currentFyiId.hasNext()) {
					commentBuilder.append(", ");
				}
			}
		}
		ObjectId[] extExecutorsIds = ((CardLinkAttribute)resolutionCard.getAttributeById(resExtExecutorsAttrId)).getIdsArray();
		if (extExecutorsIds != null && extExecutorsIds.length > 0) {
			commentBuilder.append("\n" + bundle.getString("exam.comment.header.extexecutors") + ": ");
			for (int i = 0; i < extExecutorsIds.length; i++) {
				if (i > 0) {
					commentBuilder.append(", ");
				}
				Card extExecutorCard = loadCardById(extExecutorsIds[i]);
				commentBuilder.append(extExecutorCard.getAttributeById(nameAttrId).getStringValue());
			}
		}
        	// ����
        	final DateAttribute term = (DateAttribute) resolutionCard.getAttributeById(resDueDateAttrId);
        	//term.setShowTime(false);
	        term.setTimePattern(DateAttribute.defaultTimePattern);
	        final String termStr = term.getStringValue();
	        if ((termStr != null) && termStr.length() > 0) {
	            commentBuilder.append("\n" + bundle.getString("exam.comment.header.duedate")).append(": ");
	            commentBuilder.append(termStr);
	        }
		commentBuilder.append("");
	}

	Card execListProject( ObjectId cardId, ObjectId backLinkAttrId, 
			UserData user) throws DataException
	{
		final ListProject action = new ListProject();
		action.setCard( cardId);
		action.setAttribute( backLinkAttrId);

		final SearchResult rs = (SearchResult) super.execAction(action, user);
		if (rs == null) return null;

		final List<Card> cards = rs.getCards();
		return (cards == null || cards.isEmpty()) ? null : cards.get(0);
	}
	
	/**
	 * �������� �� �� ���� ����� (�� ������� this.depth), ������� � �������� 
	 * ��������, ������ �� �������� this.cres.
	 * @param roots: ������ �������� ��������.
	 * @return ������ �����.
	 * @throws DataException
	 */
	@SuppressWarnings("unchecked")
	private Collection<ObjectId> loadDeepChildren(
			final Collection<ObjectId> roots
		) throws DataException 
	{
		final GetDeepChildren action = new GetDeepChildren();
		action.setDepth(depth);
		action.setChildTypeId(linkedAttrId);
		action.setRoots(roots);
		final ActionQueryBase aqb = getQueryFactory().getActionQuery(action);
		aqb.setAction(action);
		final Collection<ObjectId> ids = (Collection<ObjectId>)getDatabase().executeQuery(getSystemUser(), aqb);
		return ids;
	}
	
	private boolean hasStaticRole(Collection<Person> personIds, ObjectId roleAttrId){
		final String sql = 
			"select count(*) from person_role pr where pr.person_id in ("
			+IdUtils.makeIdCodesEnum(personIds, ",")
			+") and pr.role_code = ?"; 
		final long cnt = getJdbcTemplate().queryForLong( sql,
				new Object[] {roleAttrId.getId()},
				new int[] {Types.VARCHAR}
		);
		return cnt > 0;
	}
}
