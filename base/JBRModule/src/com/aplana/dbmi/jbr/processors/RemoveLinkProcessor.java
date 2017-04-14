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
/**
 * 
 */
package com.aplana.dbmi.jbr.processors;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.UserData;

/**
 * @author RAbdullin
 * ��������� ��������� �������� ������(��) �� ������� �������� �� ���� 
 * ���������(��) ����� backLink/cardLink/typedLink.
 * ������:
 *		<specific property="workflowMove.id" value="jbr.commission.drop">
 *			<post-process class="RemoveLinkProcessor">
 *				<parameter name="linkAttrId" value="jbr.rimp.byrimp"/>
 *				<parameter name="listLinkedAttrIds" value="jbr.linkedResolutions , JBR_IMPL_RESOLUT"/>
 *			</post-process>
 *		</specific>
 */
public class RemoveLinkProcessor extends ProcessCard 
{
	private static final long serialVersionUID = 1L;

	/**
	 * BackLink/CardLink/TypedLink ������� � ������� �������� ��� ���������
	 * ����� ��� ������ ��������� �������� ��� �������� ������ �� ������
	 *  �������� �� ���������-�������, ������������� � listLinkedAttrIds.
	 */
	private ObjectId linkAttrId;

	/**
	 * ������ id ���������-������� � ��������� (��) ���������, �� ������� ���� 
	 * ������ ������ �� ������ ��������.
	 */ 
	private List<ObjectId> listLinkedAttrIds;

	/** 
	 * @see com.aplana.dbmi.service.impl.ProcessorBase#process()
	 */
	@Override
	public Object process() throws DataException {

		if (!configParameters())
			return null;

		final ObjectId cardId = getCardId();
		if (cardId == null) {
			logger.warn("no cardId -> exiting");
			return null;
		}

		// ��������� id-������ �������� ...
		final UserData operUser = getSystemUser();
		final List<ObjectId> list = new ArrayList<ObjectId>(5);
		if (!getLinkedList(list, cardId, operUser))
			return null;
		final String sIds = ObjectIdUtils.numericIdsToCommaDelimitedString(list).trim();
		logger.debug("card "+ cardId.getId() + " has "+ list.size() +" linked card(s) with ids : [" + sIds + "]");

		// ������ ��������� ����� ��� ��������...
		final String sCodes = IdUtils.makeIdCodesQuotedEnum(this.listLinkedAttrIds);
		logger.debug("attribute list inside linked cards is: ["+ sCodes + "]");

		// ��������...
		final String sql = 
				"delete from attribute_value av \n"
			+	"where \n"
			+	"\t\t (av.card_id in ("+ sIds+ ") ) \n"
			+	"\t\t and (av.attribute_code in ("+ sCodes + ") ) \n"
			+	"\t\t and (av.number_value = ?) \n" // ���� ������ �� ��������
			;

		ArrayList<ObjectId> lockedCards = new ArrayList<ObjectId>(list.size());
		try {
			for (ObjectId objId : list) {
				execAction(new LockObject(objId));
				lockedCards.add(objId);
			}
			final int processed = getJdbcTemplate().update(sql, 
					new Object[] {cardId.getId()},
					new int[] { Types.NUMERIC }
			);
			logger.info( processed + " removed record(s)");
		} finally {
			for (ObjectId objId : lockedCards) {
				execAction(new UnlockObject(objId));
			}
		}
		return null;
	}

	/**
	 * �������� ������ ��������� ��������� ��������. 
	 * @param result: ������� ������, � ������� ����� ��������� ��������� id.
	 * @param cardId: ��������, ��� ������� ���� ������ ���������.
	 * @param operUser: ������������ �� ����� �������� ��������� ������ � ��.
	 * @return true, ���� � result �������� ���� �� ���� �������.
	 * @throws DataException
	 */
	private boolean getLinkedList( List<ObjectId> result, 
			ObjectId cardId, UserData operUser ) 
			throws DataException
	{
		final Card card = loadCardById(cardId, operUser);
		if (card == null) {
			logger.warn("null card loaded -> exiting");
			return false;
		}

		final Attribute attr= card.getAttributeById( this.linkAttrId);
		if (attr == null) {
			logger.warn( "card "+ card.getId() +" did not contains attribute "+ this.linkAttrId + " -> exiting");
			return false;
		}

		final List<ObjectId> list = super.getAllLinkedIdsByAttr(cardId, attr, operUser);
		if (list == null || list.isEmpty()) {
			logger.warn( "card "+ card.getId() +", attribute "+ this.linkAttrId + " has empty linked list-> exiting");
			return false;
		}

		logger.info( "card "+ card.getId() +", attribute "+ this.linkAttrId + " has linked list counter " + list.size());

		result.addAll(list);

		return true;
	}

	/**
	 * �������� ���������������� ���������.
	 * @return true ���� ���������������� ���������.
	 */
	private boolean configParameters() {

		linkAttrId = getAttrIdParameter( "linkAttrId", CardLinkAttribute.class, false);
		listLinkedAttrIds = getAttrIdsListParameter( "listLinkedAttrIds", BackLinkAttribute.class);

		if (linkAttrId == null) {
			logger.warn("link attribute not configured -> exiting");
			return false;
		}

		if (listLinkedAttrIds == null || listLinkedAttrIds.isEmpty()) {
			logger.warn("attributes inside linked card(s) not configured -> exiting");
			return false;
		}

		return true;
	}

}