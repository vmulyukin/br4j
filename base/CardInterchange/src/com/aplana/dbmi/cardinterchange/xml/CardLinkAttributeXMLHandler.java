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
package com.aplana.dbmi.cardinterchange.xml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.util.ObjectIdUtils;

public class CardLinkAttributeXMLHandler extends AttributeXMLHandler {

	public CardLinkAttributeXMLHandler(String value, Class clazz) {
		super(value, clazz);
	}

	public AttributeXMLValue[] getValue(Attribute attr) {
		Collection values = ((CardLinkAttribute)attr).getValues();
		AttributeXMLValue[] result = new AttributeXMLValue[values.size()];
		int index = 0;
		Iterator i = values.iterator();
		while (i.hasNext()) {
			Card val = (Card)i.next();
			String name;
			try {
				name = ((StringAttribute)val.getAttributeById(Attribute.ID_NAME)).getValue();
			} catch (RuntimeException e) {
				name = null;
			}
			result[index] = new AttributeXMLValue(val.getId().getId().toString(), name);
			++index;
		}
		return result;
	}

	public void setValues(List values, Attribute attr) {
		CardLinkAttribute a = (CardLinkAttribute)attr;
		List cards = new ArrayList(values.size());
		for (int i = 0; i < values.size(); ++i) {
			ObjectId cardId = ObjectIdUtils.getObjectId(Card.class, ((AttributeXMLValue)values.get(i)).getValue(), true);
			cards.add(DataObject.createFromId(cardId));
			if (!attr.isMultiValued()) {
				break;
			}
		}
		a.setValues(cards);
	}

}
