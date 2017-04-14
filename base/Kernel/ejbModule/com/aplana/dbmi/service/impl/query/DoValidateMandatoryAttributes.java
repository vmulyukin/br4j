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
package com.aplana.dbmi.service.impl.query;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.aplana.dbmi.action.ValidateMandatoryAttributes;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.AttributeViewParam;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.TemplateBlock;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.ChildrenQueryBase;

public class DoValidateMandatoryAttributes extends ActionQueryBase{

	/**
	 * Performs checks if given {@link WorkflowMove} could be performed.
	 * If checks succeed then changes state of given {@link Card}.
	 * @return null
	 * @throws DataException if given {@link WorkflowMove} couldn't be performed.
	 */
	public Object processQuery() throws DataException
	{
		final ValidateMandatoryAttributes action = (ValidateMandatoryAttributes) getAction();
		final Card card = action.getCard();
		
		// Fetch view attributes ...
		final ChildrenQueryBase viewQuery = getQueryFactory().getChildrenQuery(Card.class, AttributeViewParam.class);
		viewQuery.setParent(card.getId());		
		final List attrViewParams = (List) getDatabase().executeQuery(getUser(), viewQuery);
		
		final Set mandatoryAttrIds = new HashSet();

		// ���������� ������������ ��������� �� view...
		for ( Iterator i = attrViewParams.iterator(); i.hasNext(); ) 
		{
			final AttributeViewParam rec = (AttributeViewParam)i.next();
			if (rec.isMandatory()) {
				mandatoryAttrIds.add(rec.getId().getId());
			}
		}
		
		// ������ �� ���� ��������� ���� �������� �������� � �������� ������������ 
		for(Iterator i = card.getAttributes().iterator(); i.hasNext(); ) 
		{
			final TemplateBlock block = (TemplateBlock)i.next();
			final Iterator j = block.getAttributes().iterator();
			while (j.hasNext()) {
				final Attribute attr = (Attribute)j.next();
				final String attrCode = (String)attr.getId().getId();

				final boolean isMandatory = mandatoryAttrIds.contains(attrCode);
				final boolean hasVal = !attr.isEmpty();

				if (isMandatory) {
					if (!hasVal) {
						throw new DataException("action.state.attrmandatory.cardinfo",
								new Object[] { attr.getNameRu(), card.getAttributeById(Attribute.ID_NAME).getStringValue() } );
					}
					mandatoryAttrIds.remove(attrCode); // ... ��� ���������
				}
			}
		}		
		
		return null;
	}

}
