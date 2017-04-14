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
package com.aplana.dbmi.jbr.processors.docgraph;

import java.sql.Types;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;

/**
 * @author RAbdullin
 * �������� ����������� ��� ��������� ������� ����������.
 * ��������� ����������� ������ �������� �� �����-������ (��������� ���������) 
 * � ������ � �������� ����.
 * ��������, "���������" ���� ����� �� ��������� � ��������� �� ��� ��������.
 */
public class copyAttribute extends GraphProcessorBase
{
	private static final long serialVersionUID = 1L;

	/**
	 * �������� ������� ��� �����������.
	 */
	private ObjectId srcAttrId;

	/**
	 * ������� ������� ��� �����������.
	 */
	private ObjectId dstAttrId;

	/* (non-Javadoc)
	 * @see com.aplana.dbmi.jbr.model.GraphProcessor#process()
	 */
	@Override
	public boolean processNode() throws DataException {

		if (this.getOriginNodeId() == null) {
			logger.warn(" origin node (card) id is null -> exiting ");
			return true;
		}

		if (this.srcAttrId == null) {
			logger.warn(" source attribute is null -> exiting ");
			return true;
		}

		if (this.dstAttrId == null) {
			logger.warn(" destination attribute is null -> exiting ");
			return true;
		}

		final ObjectId srcCardId = this.getOriginNodeId();
		final String srcAttrCode = this.srcAttrId.getId().toString();
		final String dstAttrCode = this.dstAttrId.getId().toString();

		/* 
		 * ������� "������������" ���� ������������ � �������� �����...
		 */
		final Set<ObjectId> allNodes = new HashSet<ObjectId>();

		// �������� � ���� ������ - ����, ���� ����� � ���������...
		if (this.getCurNodeId() != null)
			allNodes.add(this.getCurNodeId());
		if (this.getAllChildNodes() != null)
			allNodes.addAll(this.getAllChildNodes());
		if (this.getAllParentNodes() != null)
			allNodes.addAll(this.getAllParentNodes());
		if (this.getCopyToNodes() != null)
			allNodes.addAll(this.getCopyToNodes());

		// ������ ������� ����� (�� ���������� �������� ��������, ���� �������� 
		// �������� �� ��������)
		final Set<ObjectId> destNodes = new HashSet<ObjectId>(allNodes);
		if (srcAttrCode.equals(dstAttrCode)) // ��������� "�����������" ������������ �������� ...
			destNodes.remove( srcCardId); // ��������� �������� ��������, ����� �� ����������� ���� �� ...

		if (destNodes.isEmpty()) {
			logger.info( "Nothing copied from source attributes '" + srcAttrCode
					+ "' of card "+ srcCardId.getId() + " into '"+ dstAttrCode +"', cause dest cards list is empty");
			return true; // true="������������ �����"
		}

		// ����������� ������ id ������� �������� ...
		final String sDestCardIds = ObjectIdUtils.numericIdsToCommaDelimitedString(destNodes);

		logger.debug( "processing tree with root card " + srcCardId.getId() 
				+ " copy attribute '" + srcAttrId+ "' into '" 
				+ dstAttrId+ "' of cards ["+ sDestCardIds + "]" );

		// ��������� ������ ��� ��������� attribute-value-������ {0} � {1]
		final String compare_av_2 = 
			"						AND ( (({0}.number_value is null) and ({1}.number_value is null)) or ({0}.number_value = {1}.number_value) ) \n" +
			"						AND ( (({0}.string_value is null) and ({1}.string_value is null)) or ({0}.string_value = {1}.string_value) ) \n" +
			"						AND ( (({0}.date_value is null) and ({1}.date_value is null)) or ( date_trunc( ''second'', {0}.date_value) = date_trunc( ''second'', {1}.date_value)) ) \n" +
			"						AND ( (({0}.value_id is null) and ({1}.value_id is null)) or ({0}.value_id = {1}.value_id) ) \n" +
			"						AND ( (({0}.another_value is null) and ({1}.another_value is null)) or ({0}.another_value = {1}.another_value) ) \n" +
			"						AND ( (({0}.long_binary_value is null) and ({1}.long_binary_value is null)) or ({0}.long_binary_value = {1}.long_binary_value) ) \n"
			;
		/* �������� ������� ��������� ... */
		ArrayList<ObjectId> lockedCards = new ArrayList<ObjectId>(destNodes.size());
		try	{
			for (ObjectId objId : destNodes) {
				execAction(new LockObject(objId));
				lockedCards.add(objId);
			}
			
			final long msStartPredel = System.currentTimeMillis();
			// (!) ���� �������� �������� ���� � ������ dest, �� � �� ���� �������,
			// ����� ������ ����� ����������.
			final int countDel = getJdbcTemplate().update(
					"DELETE FROM attribute_value avEx \n" +
					"WHERE \n" +
					"	avEx.card_id in ("+  sDestCardIds + ") \n" +
					"	AND avEx.attribute_code = ? \n" +
					"	AND NOT EXISTS( \n" +
					"		SELECT 1 FROM attribute_value src \n" +
					"		WHERE \n" +
					"			src.card_id = ? \n"+
					"			AND src.attribute_code= ? \n" +
					"			-- �������� ������������� ������ ������ � ����� ������ ... \n" +
								MessageFormat.format(compare_av_2, "avEx", "src") +
					" 	) -- /AND NOT EXISTS \n",
					new Object[] { dstAttrCode, srcCardId.getId(), srcAttrCode },
					new int[] { Types.VARCHAR, Types.NUMERIC, Types.VARCHAR }
			);
			if (logger.isDebugEnabled()) {
				final long msDuration = System.currentTimeMillis() - msStartPredel;
				logger.debug( "Predelete in "+ msDuration+ " msec " + countDel +" attributes '"+ dstAttrCode +
						"' of cards ["+ sDestCardIds +"]" );
			}

			/* ������� ����� ��������� ... */

			final long msStart = System.currentTimeMillis();
			final int countCopied = getJdbcTemplate().update(
					"INSERT INTO attribute_value \n" +
					"	( 	card_id, attribute_code, number_value, string_value, date_value, value_id, another_value) \n" +
					"SELECT DISTINCT cDest.card_id, aDest.attribute_code, src.number_value, src.string_value, \n" +
					"		src.date_value, src.value_id, src.another_value \n" +
					"FROM card cDest, attribute_value src, attribute aDest \n" +
					"WHERE 	cDest.card_id in ("+  sDestCardIds + ") \n" +
					"		AND src.card_id = ? \n"+
					"		AND src.attribute_code= ? \n" +
					"		AND aDest.attribute_code = ? \n" +
					"		-- �������� ������������ ������ ����� ������ ... \n" +
					"		AND NOT EXISTS( \n" +
					"				select 1 from attribute_value avEx \n" +
					"				where 	avEx.card_id = cDest.card_id \n" +
					"						and avEx.attribute_code =  aDest.attribute_code \n" +
											MessageFormat.format(compare_av_2, "avEx", "src") +
					" 		) -- /AND NOT EXISTS \n",
					new Object[] { srcCardId.getId(), srcAttrCode, dstAttrCode },
					new int[] { Types.NUMERIC, Types.VARCHAR, Types.VARCHAR}
			);
			if (logger.isDebugEnabled()) {
				final long msDuration = System.currentTimeMillis() - msStart;
				logger.debug( "Copied in "+ msDuration + " msec "+ countCopied +" source attributes '" + srcAttrCode +
					"' of card "+ srcCardId.getId() + " into '"+ dstAttrCode +
					"' of card(s) ["+ sDestCardIds +"]");
			}
		} finally {
			for (ObjectId objId : lockedCards) {
				execAction(new UnlockObject(objId));
			}
		}

		return true; // true="������������ �����"
	}

	/* (non-Javadoc)
	 * @see com.aplana.dbmi.jbr.processors.ProcessCard#setParameter(java.lang.String, java.lang.String)
	 */
	@Override
	public void setParameter(String name, String value) {
		if (name == null || "".equals(name)) return;
		super.setParameter(name, value);

		if ("srcAttrId".equalsIgnoreCase(name)) {
			this.srcAttrId = IdUtils.smartMakeAttrId(value, Attribute.class);
		} else if ("dstAttrId".equalsIgnoreCase(name)) {
			this.dstAttrId = IdUtils.smartMakeAttrId(value, Attribute.class);
		} else {
			logger.warn( "Unknown parameter: '"+ name + "'='"+ value + "' -> ignored");
		}
	}

}