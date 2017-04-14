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
import java.util.ArrayList;
import java.util.List;

import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.UserData;

/**
 * @author YNikitin
 * ��������� ��������� �������� ������(��) �� ������� �������� �� ���� 
 * ���������(��) �������� ����� Link � ����� ��������� ��������
 * ������:
 *		<specific property="workflowMove.id" value="jbr.commission.drop">
 *			<post-process class="RemoveLinkProcessor">
 *				<parameter name="linkAttrId" value="jbr.rimp.byrimp"/>
 *				<parameter name="listLinkedAttrIds" value="jbr.linkedResolutions , JBR_IMPL_RESOLUT"/>
 *			</post-process>
 *		</specific>
 */
public class RemoveLinkUniversalProcessor extends ProcessCard 
{
	/**
	 * <B>�������� {@value} ��������� ������� ��������� ����, � ������� �����
	 * ������� ������ �� �������.</B>
	 * <P>
	 * ������� ������������� ����� ",". ��������:
	 * 
	 * <pre>
	 * {@code
	 * <parameter name="selectTemplates" value=""/>	
	 * }
	 * </pre>
	 * 
	 * ���� �������� �� ������, ����� ��������� �� ���� ��������� ���������
	 * <P>
	 */
	public static final String PARAM_SELECT_TEMPLATES = "selectTemplates";

	/**
	 * ������ id ���������-������� � ��������� (��) ���������, �� ������� ���� 
	 * ������ ������ �� ������ ��������.
	 */ 
	private List<ObjectId> listLinkedAttrIds;
	
	/**
	 * ������ ��������, � ������� ���� ������� ������ �� ������� �������� 
	 */
	private List<ObjectId> listSelectTemplates;

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

		// ������ ��������� ����� ��� ��������...
		final String sCodes = IdUtils.makeIdCodesQuotedEnum(this.listLinkedAttrIds);
		logger.debug("attribute list inside linked cards is: ["+ sCodes + "]");
		// ������ �������� ��������
		final String sTemplates;
		if (listSelectTemplates!=null){
			sTemplates = IdUtils.makeIdCodesEnum(this.listSelectTemplates, ",");
		} else
			sTemplates = "";
		logger.debug("selected template list is: ["+ sTemplates + "]");

		// ����� ��������� �������, ������� ������ �� ��� � ������� ����
		cleanAccessListByLinkToCard(cardId, sCodes, sTemplates);
/*		int accessListCount = getJdbcTemplate().update(
				"DELETE FROM access_list ac " +
				"WHERE source_value_id in (SELECT attr_value_id FROM attribute_value av \n"+
				"WHERE av.attribute_code in ("+ sCodes + ")  \n"+
				"AND (av.number_value = ?) \n"+
				("".equals(sTemplates)?")":"\t\t and exists(select 1 from card c where c.card_id = av.card_id and c.template_id in ("+sTemplates+")) )\n"),
				new Object[] { cardId.getId() },
				new int[] { Types.NUMERIC }
			);
		logger.info( accessListCount + " value(s) delete from access_list before delete they from attribute_value");*/

		// ��������...
		final String sql = 
				"delete from attribute_value av \n"
			+	"where \n"
			+	"\t\t (av.attribute_code in ("+ sCodes + ") ) \n"
			+	"\t\t and (av.number_value = ?) \n" // ���� ������ �� ��������
			+   ("".equals(sTemplates)?"":"\t\t and exists(select 1 from card c where c.card_id = av.card_id and c.template_id in ("+sTemplates+")) \n")
			;

		final int processed = getJdbcTemplate().update(sql, 
					new Object[] {cardId.getId()},
					new int[] { Types.NUMERIC }
				);
		logger.info( processed + " removed record(s)");

		return null;
	}

	/**
	 * �������� ���������������� ���������.
	 * @return true ���� ���������������� ���������.
	 */
	private boolean configParameters() {

		listLinkedAttrIds = getAttrIdsListParameter( "listLinkedAttrIds", CardLinkAttribute.class);
		listSelectTemplates = getAttrIdsListParameter( PARAM_SELECT_TEMPLATES, Template.class);
		
		// 
		if (listLinkedAttrIds == null || listLinkedAttrIds.isEmpty()) {
			logger.warn("attributes inside linked card(s) not configured -> exiting");
			return false;
		}

		if (listSelectTemplates!=null&&!listSelectTemplates.isEmpty()){
			logger.info("Removing link to the current card only in templates: "+listSelectTemplates);
		}
		
		return true;
	}

}
