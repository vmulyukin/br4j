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
package com.aplana.dmsi.object;

import java.util.Collection;

import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.IntegerAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Template;
import com.aplana.dmsi.DMSIException;
import com.aplana.dmsi.config.ClassConfig;
import com.aplana.dmsi.config.ClassConfigManager;
import com.aplana.dmsi.types.DMSIObject;

/**
 * ������� ��� �������� JAXB ������ ��������������� ��������.
 * ��� ������ ��� ����, ��� �� ��������� �������� ������������ �� ������ �� �������, �� � �� �������� 
 * �������� 'jbr.outcoming.destinationTemplate', ���� ����� �����. 
 * ���� ������� ��� ����������� �������� ���������� ��������� �� ����� ��� �� � ������ ������ BR4J00039039
 * 
 * @author Alex
 *
 */
public class DocumentFactory extends EntityFactory {

	private static final ObjectId DESTINATION_TEMPLATE_ATTR_ID = ObjectId.predefined(IntegerAttribute.class, "jbr.outcoming.destinationTemplate");

	@Override
	protected Collection<ObjectId> getRequiredAttributes() {
		Collection<ObjectId> result = super.getRequiredAttributes();
		result.add(DESTINATION_TEMPLATE_ATTR_ID);
		return result;
	}

	@Override
	protected Object newDMSIObject(Card card) throws DMSIException {
		this.processingCard = card;
		ObjectId templateId = null;
		IntegerAttribute templateAttrId = card.getAttributeById(DESTINATION_TEMPLATE_ATTR_ID);
		if (templateAttrId != null && !templateAttrId.isEmpty() && templateAttrId.getValue() != 0) {
			templateId = new ObjectId(Template.class, templateAttrId.getValue());
		} else {
			templateId = card.getTemplate();
		}
		Class<? extends DMSIObject> clazz = getObjectClassByTemlate(templateId);
		ClassConfig config = ClassConfigManager.instance().getConfigByClass(
			clazz);
		return newObject(config);
	}
}
