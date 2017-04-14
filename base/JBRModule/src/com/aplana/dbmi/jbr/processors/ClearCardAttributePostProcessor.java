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

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.util.AttrUtils;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.QueryBase.QueryExecPhase;

/**
 * @author DSultanbekov
 * @comment AbdullinR
 * ��������� ������� ���������� ��������. 
 * ���������:
 *    attrId: id �������� � ���� ���: ���
 *    templateId: ���� id �������, ���� �����, �� �����������, ����� ��������
 * ����� ������ ���� ������.
 */
public class ClearCardAttributePostProcessor extends ProcessCard {
	private static final long serialVersionUID = 1L;
	
	protected ObjectId attrId;
	protected ObjectId templateId;

	@Override
	public Object process() throws DataException {
		Card card = getCard();

		if (templateId != null && !templateId.equals(card.getTemplate())) {
			return null;
		}

		if (attrId == null) {
			throw new IllegalStateException("Identifier of attribute to be cleared is not specified");
		}

		if (changeAttributes!=null){
			boolean someAttributesChanged = false;
			for(ObjectId changeAttributeCode: changeAttributes){
				final Attribute changeAttribute = card.getAttributeById(changeAttributeCode);
				if (!super.isAttributeChanged(changeAttribute, card)){
					if(isMultiplicationChangeAttributeOption){
						logger.warn("Attribute "+changeAttribute.getId().toString()+" in card " + (card.getId()!=null?card.getId().toString():null) + " is not change => exit");
						return null;
					}
				} else {
					someAttributesChanged = true;
				}
			}
			if(!someAttributesChanged){
				logger.warn("No attribute changed in card " + (card.getId()!=null?card.getId().toString():null) + " => exit");
				return null;
			}
		}
		final Attribute attr = card.getAttributeById(attrId);
		if (attr == null) {
			logger.warn("No attribute with id = '" + attrId.toString() + "' found in card " + card.getId());
		} else {
			logger.debug("Clearing value of attribute with id = '" + attrId.toString() + "' of card " + card.getId());
			attr.clear();
			if (this.getCurExecPhase().equals(QueryExecPhase.POSTPROCESS)){
				cleanAccessListByAttributeAndCard(attr.getId(), card.getId());
				/*int accessListCount = getJdbcTemplate().update(
						"DELETE FROM access_list ac " +
						"WHERE source_value_id in (SELECT attr_value_id FROM attribute_value "+
						"WHERE attribute_code=? AND card_id=?)",
						new Object[] { attr.getId().getId(), card.getId().getId() },
						new int[] { Types.VARCHAR, Types.NUMERIC });
				logger.info( accessListCount + " value(s) delete from access_list before delete they from attribute_value");*/
				doOverwriteCardAttributes(card.getId(), attr);
			}
		}
		return null;
	}

	public void setParameter(String name, String value) {
		if ("attrId".equalsIgnoreCase(name)) {
			this.attrId = AttrUtils.getAttributeId(value);
		} else if ("templateId".equalsIgnoreCase(name)){
			this.templateId = ObjectIdUtils.getObjectId(Template.class, value, true);
		} else
			super.setParameter(name, value);
	}
}
