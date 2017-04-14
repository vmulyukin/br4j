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

import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.ListProject;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.jbr.util.CardUtils;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ObjectLockedException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.Database;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.QueryFactory;
import com.aplana.dbmi.service.impl.UserData;
import com.aplana.dbmi.service.impl.query.GetCard;

/**
 * ������ ��������� ����������� ��� ��������� ������� ��������� � ��������� "����������" ��� "��������"
 * ���� � ���������-��������� ��� ��������� �������� ������ ��������� � ��������� "����������" ��� "��������",
 * �� ��������-��������� ����������� � ��������� "���������".
 * ��������� ������� ������������ �� ����� ��������� ������� ������ - ����� �� ���� ������� � �������.
 * @author DSultanbekov
 */
public class ExecuteCommissionParentDoc extends AbstractCardProcessor {
	private static final long serialVersionUID = 1L;

	// TODO: ��������! �������� ������ � �����������
	private static final ObjectId PARENT_DOC_CARDLINK_ID = ObjectId.predefined(CardLinkAttribute.class, "jbr.main.doc");
	private static final ObjectId PARENT_COMMISSION_CARDLINK_ID = ObjectId.predefined(CardLinkAttribute.class, "jbr.rimp.byrimp");
	private static final ObjectId DOCUMENT_COMMISSIONS_BACKLINK_ID = ObjectId.predefined(BackLinkAttribute.class, "jbr.resolutions");
	private static final ObjectId COMMISSION_RATIFIED_STATE_ID = ObjectId.predefined(CardState.class, "ratified");
	private static final ObjectId COMMISSION_CANCELLED_STATE_ID = ObjectId.predefined(CardState.class, "poruchcancelled");
	private static final ObjectId PARENT_DOC_EXECUTED_STATE_ID = ObjectId.predefined(CardState.class, "done");

	private static final ObjectId PARENT_DOC_AUTOEXECUTED_STATE_ID = ObjectId.predefined(CardState.class, "execution");
	private static final ObjectId TEMPLATE_ID_INCOMING = ObjectId.predefined(Template.class, "jbr.incoming");
	private static final ObjectId TEMPLATE_ID_INTERNAL = ObjectId.predefined(Template.class, "jbr.interndoc");
	
	@Override
	public Object process() throws DataException {

		final UserData userData = new UserData();
		userData.setAddress("127.0.0.1");
		userData.setPerson(((Person)DataObject.createFromId(Person.ID_SYSTEM)));

		final QueryFactory qf = getQueryFactory();
		final Database d = getDatabase();
		logger.debug("Checking if parent document should be moved to 'Executed' state.");

		// ���������, ��� ��������� �������� ���������� �������� ������
		final List<Card> commisions = getLinkedCards(getCardId(), PARENT_COMMISSION_CARDLINK_ID, false);
		if (commisions == null) {
			logger.debug("This is not a top-level commission. No actions required.");
			return null;
		}

		// �������� ���������� � ���������-���������
		final List<Card> parents = getLinkedCards(getCardId(), PARENT_DOC_CARDLINK_ID, false);

		ObjectId docId;
		if ( (parents == null) || parents.isEmpty() ) {
			logger.warn("Couldn't find parent document. Exiting.");
			return null;
		}
		if (parents.size() != 1) {
			logger.warn("Several parent documents found for commission card with id " + getCardId().getId() + ". Exiting.");
			return null;
		}
		// here parents.size() == 1 
		docId = parents.iterator().next().getId();

		ActionQueryBase aq = null;
		try {
			LockObject lock = new LockObject(docId);
			aq = qf.getActionQuery(lock);
			aq.setAction(lock);			
			d.executeQuery(userData, aq);
		} catch (ObjectLockedException e) {
			throw new DataException("jbr.parentDocument.locked", new Object[] {e.getLocker().getFullName()});
		}
		logger.debug("Parent document locked");

		try {
			// �������� ��������-���������
			ObjectQueryBase fq = qf.getFetchQuery(Card.class);
			fq.setId(docId);
			final Card doc = (Card)d.executeQuery(userData, fq);
			logger.debug("Found parent document: " + doc.getId().getId());

			// �������� ���������� � �������� ��������� �������� ������
			final ListProject lp = new ListProject();
			lp.setAttribute(DOCUMENT_COMMISSIONS_BACKLINK_ID);
			lp.setCard(doc.getId());

			final List<SearchResult.Column> columns = new ArrayList<SearchResult.Column>();
			columns.add( CardUtils.createColumn(Card.ATTR_STATE));
			lp.setColumns(columns);

			final List<Card> list = CardUtils.execSearchCards(lp, getQueryFactory(), getDatabase(), userData);

			// ��������� ��� ��� ��������� �������� ������ ��������� � ��������� "��������" ��� "����������"
			if (list != null)
				for(final Card commission: list ) {
					final ObjectId stateId = commission.getState();
					if (!COMMISSION_CANCELLED_STATE_ID.equals(stateId) 
							&& !COMMISSION_RATIFIED_STATE_ID.equals(stateId)) {
						logger.debug("Found not finished top-level commission: " + commission.getId().getId() + ". Exiting.");
						return null;
					}
				}
			logger.debug("All top-level commissions are finished. Ready to move parent document in 'Executed' state.");
	
			// ���������� ����������� ��� �������� ������������� ��������� � ��������� "��������" -> "��������"

			// TODO: ������ hardcode ��� ����������� � ��������� (�� � ������ ���� ����� ���������������)
			final ObjectId docTemplate = doc.getTemplate();
			ObjectId destState = null;
			if ( TEMPLATE_ID_INCOMING.equals(docTemplate) || TEMPLATE_ID_INTERNAL.equals(docTemplate)) {
				destState = PARENT_DOC_AUTOEXECUTED_STATE_ID;
			} else {
				destState = PARENT_DOC_EXECUTED_STATE_ID;
			}
			// ���������� ������� ��� �������� ������������� ��������� � ��������� "��������"
			final WorkflowMove wfm = CardUtils.findWorkFlowMove( docId, destState, qf, getDatabase(), userData);
			if (wfm == null) {
				logger.warn("Couldn't find workflow move to 'Executed' state for given parent document " + docId.getId());
				return null;
			}

			final ChangeState changeState = new ChangeState();
			changeState.setCard(doc);
			changeState.setWorkflowMove(wfm);
			aq = qf.getActionQuery(changeState);
			aq.setAction(changeState);
			d.executeQuery(userData, aq);
			logger.error("Parent document moved to next state");

		} finally {
			try {
				UnlockObject unlock = new UnlockObject(docId);
				aq = qf.getActionQuery(unlock);
				aq.setAction(unlock);
				d.executeQuery(userData, aq);
				logger.debug("Parent document unlocked");
			} catch (Exception e) {
				logger.error("Failed to unlock parent document object", e);
			}
		}
		return null;
	}

	List<Card> loadProjects(ObjectId linkAttrId, UserData userData) 
		throws DataException
	{
		final ListProject listProject = new ListProject();
		listProject.setCard(getCardId());
		listProject.setAttribute(linkAttrId);

		final List<Card> list = CardUtils.execSearchCards(listProject, getQueryFactory(), getDatabase(), userData);
		return (list == null || list.isEmpty()) ? null : list;
	}
}
