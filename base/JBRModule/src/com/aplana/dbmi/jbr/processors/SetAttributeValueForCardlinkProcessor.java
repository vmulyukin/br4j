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

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.jbr.util.CardUtils;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.service.DataException;

/**
 * ������������� ���������, ������������ c ����� ��������� 
 * ��������-������� �������� �� ����� (��� �������� �� ID ��������).
 * ���������� ��������� ������������ ��� �������� �������� ������� ����������� ��������� (1255)
 * ��� ���������� ��������� ������ ����������� � ��� ��������� ���������� �� ���������.
 * �������� ��� ���������� �������� ������ �� ��������� �� ���� NAME � �� �������, ������� ������� �� ����������.
 * @author desu
 *
 */
public class SetAttributeValueForCardlinkProcessor extends SetAttributeValueProcessor {

	protected static final String TEMPLATE_PARAM = "templateId";
	
	private static final long serialVersionUID = 1L;
	private String value = null;
	private ObjectId attributeId = null;
	private ObjectId templateId = null;
	
	@Override
	public Object process() throws DataException {		
		if (attributeId == null || value == null || templateId == null) {
			logger.error("Not all mandatory parameters were set");
			throw new DataException("jbr.card.configfail");
		}

		Card card = getCard();
		final Attribute destAttr = card.getAttributeById(attributeId);
		if (destAttr == null) {
			logger.warn( "Card "+ card.getId() + " has no attribute "+ destAttr);
			return null;
		}
		
		Search search = new Search();
		search.setByAttributes(true);
		search.setTemplates(Collections.singletonList(DataObject.createFromId(templateId)));
		search.addStringAttribute(IdUtils.smartMakeAttrId("NAME", StringAttribute.class), value);
		final List<Card> cards = CardUtils.execSearchCards(search, getQueryFactory(), getDatabase(), getSystemUser()); 
			
		if (cards != null) {
			for (Card foundedCard : cards) {
				String numberValue = foundedCard.getId().getId().toString();
				if( setValue( card, destAttr, numberValue) == false)
					logger.info( MessageFormat.format("Attribute ''{0}'' of card ''{1}'' not changed", destAttr.getId(), card.getId() ));
			}
		}
		return null;
	}
	
	@Override
	public void setParameter(String name, String value) {
		if (DEST_ATTRIBUTE_ID_PARAM.equalsIgnoreCase(name)) {
			this.attributeId = IdUtils.smartMakeAttrId(value, CardLinkAttribute.class);
		} else if (DEST_VALUE_PARAM.equalsIgnoreCase(name)) {
			this.value = value;
		} else if (TEMPLATE_PARAM.equalsIgnoreCase(name)) {
			this.templateId = IdUtils.smartMakeAttrId(value, Template.class);
		} else {
			super.setParameter( name, value);
		}
	}
}
