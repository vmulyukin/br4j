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

import java.util.Iterator;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.TemplateBlock;
import com.aplana.dbmi.service.DataException;

/**
 * �������� ���������, ������������ ��� ����������.
 */

public class CheckRequiredAttributes extends ProcessCard  {

	@Override
	public Object process() throws DataException {
		final Card card = super.getCard();
		if (card == null)
			return null;
		
		// ������ �� ���� ��������� �������� � ��������
		// ������������ ��� �������...  
		for(Iterator i = card.getAttributes().iterator(); i.hasNext(); ) 
		{
			final TemplateBlock block = (TemplateBlock)i.next();
			final Iterator j = block.getAttributes().iterator();
			while (j.hasNext()) {
				Attribute attr = (Attribute)j.next();
				if (attr.isMandatory() && !attr.isHidden() && !attr.isReadOnly()) {
					if (attr.isEmpty()) {
						throw new DataException("action.state.attrmandatory",
								new Object[] { attr.getNameRu(), attr.getNameEn() } );
					}
				}

			}
		}
		return null;
	}

}
