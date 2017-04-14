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
import java.util.Arrays;

import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.SystemRole;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.UserData;

/**
 * ����� ���������� ���������� ��������� �� ����������� � ������ "���������� ������� ����".
 * (����������� ������� ��������� ���������)
 * - ������� ������������ ������� �� ������� ����������� � ������ � �����������,
 * ���� �������� �������� ��������� ��� ��� ����������; 
 * - ������� ������������ ������� �� ������� ����������� � ��������� ���������� ��������,
 *  ���� �������� �������� ������������ �������� ��� ��� ����������; 
 * - ������� ������������ ������� �� ������� ����������� � ����������� ������� ����, 
 * ���� �������� �������� ���������� ������������ ��� ��� ����������. 
 * 
 * ��� ����� �� "���������� ������� ����" � "�����������" �������� ����� �������, � ���� 
 * ��������� ������������� ��������� �������� �� �����������, ���� ����� ����������� ���
 * ���� ���� � ����� "�������" (+ ���������) ��� "����������� ��������" (+���������);
 * 
 * ���� ������� ����������� ��������� ����������, ������� ������������ �������� �������.
 * 
 * @author DSultanbekov
 */
public class OutcomingAfterSignProcessor extends AbstractCardProcessor
{

	// �������
	private static final ObjectId MINISTER_ROLE_ID = 
			ObjectId.predefined(SystemRole.class, "jbr.minister");
	// (�����) �������� ��������
	private static final ObjectId MINISTER_HELPER_ROLE_ID = 
			ObjectId.predefined(SystemRole.class, "jbr.minister.helper");
	// "��������� �������� - �������" (������������ ����)
	//   personattribute.jbr.hidden.minister.assistants=ASST_MNSTR_HID
	private static final ObjectId HELPER_MINISTER_ATTR_ID = 
			ObjectId.predefined(PersonAttribute.class, "jbr.hidden.minister.assistants");
	/* ������ "���������" (364) �������� ��������� ��� ��� ����������: 
	 *   == ������� 399274 "�� ������� ���������� (��������� ��������� ��� ���������� ��������)"
	 * 		�� ������� "���������� ������� ����" (355554)  ->  "�����������" (200)
	 * workflowmove.jbr.outcoming.filling-in-file-index.before-registration=399274
	 */
	private static final ObjectId MOVEID_PROCEED_TO_REGISTRATION = 
			ObjectId.predefined(WorkflowMove.class, "jbr.outcoming.filling-in-file-index.before-registration");


	// ��� ��������
	private static final ObjectId DEPUTY_MINISTER_ROLE_ID = 
			ObjectId.predefined(SystemRole.class, "jbr.deputyMinister");
	// ��������� ������������ ���������- �������
	//   personattribute.jbr.hidden.zam.minister.assistants=ASST_ZAM_MNSTR_HID
	private static final ObjectId HELPER_DEP_MINISTER_ATTR_ID = 
			ObjectId.predefined(PersonAttribute.class, "jbr.hidden.zam.minister.assistants");
	/* 364	��������� �������� ����� �������� ��� ��� ����������: 
	 *   == ������� 355557 '��� �����'
	 * 		�� ������� "���������� ������� ����" (355554)  ->  "�������� ���������� ��������" (355555)
	 * workflowmove.jbr.outcoming.sign.deputy-minister-check=355557
	 */
	private static final ObjectId MOVEID_DEP_MINISTER_CHECK =
			ObjectId.predefined( WorkflowMove.class, "jbr.outcoming.sign.deputy-minister-check");


//	// �������� ������������
//	// systemrole.jbr.director.department=JBR_DEP_DIR
//	private static final ObjectId DEPART_DIRECTOR_ROLE_ID = 
//			ObjectId.predefined(SystemRole.class, "jbr.director.department");
//	// ��������� ���������� ������������ - �������
//	//   personattribute.jbr.hidden.dep.director.assistants=ASST_DRCT_DEP_HID
//	private static final ObjectId HELPER_DEPART_DIRECTOR_ATTR_ID = 
//			ObjectId.predefined(PersonAttribute.class, "jbr.hidden.dep.director.assistants");


	private static final ObjectId SIGNING_LIST_ATTR_ID = ObjectId.predefined(CardLinkAttribute.class, "jbr.sign.set");
	private static final ObjectId SIGNERS_ATTR_ID = ObjectId.predefined(PersonAttribute.class, "jbr.sign.person");

	// personattribute.jbr.outcoming.signatory=JBR_INFD_SIGNATORY
	// private static final ObjectId THESIGNER_ATTR_ID = ObjectId.predefined(PersonAttribute.class, "jbr.outcoming.signatory");


//	private JdbcTemplate jdbcTemplate;

	@Override
	public Object process() throws DataException 
	{

		try {
			final ChangeState move = (ChangeState) getAction();
			final Long cardId = (Long)move.getCard().getId().getId();

			logger.debug("OutcomingAfterSignProcessor started with move " 
					+ move + "(wfm="+ 
						(move.getWorkflowMove() == null ? null: move.getWorkflowMove().getId() ) 
					+") for card "+ cardId+ "" );

			final boolean isMinister = hasStaticOrDynamicRole(cardId, 
					SIGNERS_ATTR_ID,
					SIGNING_LIST_ATTR_ID,
					new ObjectId[] {MINISTER_ROLE_ID, MINISTER_HELPER_ROLE_ID},
					HELPER_MINISTER_ATTR_ID);
			final boolean isDepMinister = hasStaticOrDynamicRole( cardId, 
					SIGNERS_ATTR_ID,
					SIGNING_LIST_ATTR_ID, 
					new ObjectId[] {DEPUTY_MINISTER_ROLE_ID}, 
					HELPER_DEP_MINISTER_ATTR_ID);
			//final boolean isDirectorDepart = hasStaticOrDynamicRole( cardId, SIGNERS_ATTR_ID, 
			//		SIGNING_LIST_ATTR_ID, DEPART_DIRECTOR_ROLE_ID, HELPER_DEPART_DIRECTOR_ATTR_ID);

			if (!isMinister && !isDepMinister) // && !isDirectorDepart
				// (!?) _����� �� �������� 
				return null;

			ObjectId moveId = null; // id ��������...
			if (isMinister) { // && !isDepMinister && !isDirectorDepart
				// ���� ������� �������� ... 
				moveId = MOVEID_PROCEED_TO_REGISTRATION;
				logger.info( "Minister's or his helper sign found for card  "+ cardId+ ". Proceeding to the next stage by " + moveId);
			} else if (isDepMinister) { // !isDirectorDepart
				// ... ������������� �� ����������� ������� ���� � ������ ��������� ���������� ��������
				moveId = MOVEID_DEP_MINISTER_CHECK;
				logger.info( "Deputy minister or his helper sign found for card  "+ cardId+ ". Proceeding to the next stage by " + moveId);
			} else {
				// ������ �� ������ -> ������� � ��� ����������� ...
			}

			if (moveId == null) {
				logger.info( "Card "+ cardId+ " do not chage state automatically cause it's not signed by minister, his deputy or thier helpers" );
			} else {
				final UserData user = getSystemUser();
				final ObjectQueryBase q = getQueryFactory().getFetchQuery(WorkflowMove.class);
				q.setId(moveId);
				final WorkflowMove proceedWfm = (WorkflowMove) getDatabase().executeQuery(user, q);

				final ChangeState proceedMove = new ChangeState();
				proceedMove.setCard(move.getCard());			
				proceedMove.setWorkflowMove(proceedWfm);
				super.execAction(proceedMove, user);

				logger.info("Card "+ cardId+ " moved successfully to the next wfm step by " 
						+ moveId + " '"+ proceedWfm.getMoveName()+ "' "
						+ proceedWfm.getToState());
			}
		} catch (Throwable ex) {
			logger.error("processor failed with error", ex);
			throw new DataException(ex);
		}
		return null;
	}


	private boolean hasStaticOrDynamicRole( Long longCardId, 
				ObjectId signerAttrId, 
				ObjectId listSignersOfMainCardAttrId,
				ObjectId[] objectIds,
				ObjectId helperMinisterAttrId
			)
	{
		final String sql = 
				"select count(*) \n" +
				"from attribute_value av_signer \n" +

				// ��� ������� ������...
				"where av_signer.attribute_code = ? \n" +

				"and ( \n" +
				"\t ( \n" +
					// �������� ����, ��� ��� ������� ���� � ��������� ���������...
				"\t\t exists (select 1 from attribute_value av_signing where av_signing.attribute_code = ? and av_signing.number_value = av_signer.card_id and av_signing.card_id = ?) \n"+

					// �������� ����������� ����...
				"\t\t and exists (select 1 from person_role pr where pr.person_id = av_signer.number_value and pr.role_code in ( " 
					+ IdUtils.makeIdCodesQuotedEnum( Arrays.asList(objectIds))
					+ " )) \n" +
				"\t ) \n" +

				// ��� �������� ����� ���� - �������� � ������ hidden-������ ��������� ...
				"	or exists ( \n" +
				"		select 1 from attribute_value av_signing \n" +
				"		where av_signing.attribute_code = ? \n" +
				"			and av_signing.card_id = ? \n" +
				"			and av_signing.number_value = av_signer.number_value \n" + // ���������� �� ���� ������� av_signer.number_value
				"	) \n" +
				") \n "
			;
		final long cnt = getJdbcTemplate().queryForLong( sql,
				new Object[] {
					signerAttrId.getId(),

					listSignersOfMainCardAttrId.getId(),
					longCardId,

					helperMinisterAttrId.getId(),
					longCardId
				},
				new int[] {
					Types.VARCHAR, 

					Types.VARCHAR, 
					Types.NUMERIC,

					Types.VARCHAR, 
					Types.NUMERIC
				}
			);
		return cnt > 0;
	}

}