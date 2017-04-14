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
import java.util.HashSet;
import java.util.Set;

import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.util.ObjectIdUtils;

/**
 * @author NGaleev
 * �������� ����������� ��� ��������� ������ ����������.
 * ��������� ����������� ����� ������� ���� �� ����� � ������� ��������.
 * ������� ������ �������� ������� ��������
 */
public class TreeChooseDateFromChild extends GraphProcessorBase
{
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
	public boolean processNode() {

		if (this.getCurNodeId() == null) {
			logger.warn(" current node (card) id is null -> exiting ");
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

		// ������� ��������� ��������� � id ������� ��������
		final String 	sDstCardId 	= this.getCurNodeId().getId().toString();
		// ������� ��������� ��������� � ������ ��������� � �������� ��������� 
		final String 	srcAttrCode = this.srcAttrId.getId().toString();
		final String 	dstAttrCode = this.dstAttrId.getId().toString();

		//�������� ��� �������� �������� � ��������� �� id � ��������� ��������� 
		final String sAllChCardIds = ObjectIdUtils.numericIdsToCommaDelimitedString(this.getAllChildNodes());
		if (sAllChCardIds==null||sAllChCardIds.length()==0){
			logger.info( "List of source cards is empty, copy process is stop.");
			return true; // ���������� ��������� �����
		}
		logger.debug( "Processing tree with root card " + sDstCardId 
				+ ": copy attribute '" + srcAttrId+ "' into '" 
				+ dstAttrId + "' in root card from cards ["+ sAllChCardIds + "]" );

		final int countDel = getJdbcTemplate().update(
					"DELETE FROM attribute_value \n" +
					"WHERE attribute_code=? \n" +
					"	AND card_id="+ ObjectIdUtils.getIdLongFrom(sDstCardId),
					new Object[] { dstAttrCode },
					new int[] { Types.VARCHAR }
				);
		logger.debug( "Predelete "+ countDel +" attributes '"+ dstAttrCode +" under cards ["+ sDstCardId +"]");

		final int countCopied = getJdbcTemplate().update(
				"INSERT INTO attribute_value \n" +
				"( 	card_id, attribute_code, \n" +
				"	number_value, string_value, date_value, value_id, another_value) \n" +
				"SELECT c.card_id, ?, src.number_value, src.string_value, \n" +
				"	src.date_value, src.value_id, src.another_value \n" +
				"FROM card c, attribute_value src \n" +
				"WHERE c.card_id ="+  sDstCardId + " \n" +
				"		AND src.card_id in (" + sAllChCardIds + ") \n"+
				"		AND src.attribute_code= ? \n"+
				"ORDER BY src.date_value DESC LIMIT 1",
				new Object[] { dstAttrCode, srcAttrCode },
				new int[] { Types.VARCHAR, Types.VARCHAR}
			);
		logger.info( "Copied "+ countCopied +" attributes '" + srcAttrCode
				+ "' of cards "+ sAllChCardIds + " into '"+ dstAttrCode +" of cards ["+ sDstCardId +"]");

		return true; // ���������� ��������� �����
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