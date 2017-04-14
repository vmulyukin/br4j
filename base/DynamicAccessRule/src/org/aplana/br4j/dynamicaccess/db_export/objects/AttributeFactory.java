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
package org.aplana.br4j.dynamicaccess.db_export.objects;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.DatedTypedCardLinkAttribute;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.TypedCardLinkAttribute;

/**
 * Factory for {@link Attribute}.
 * @author atsvetkov
 *
 */
public class AttributeFactory {

	public static Attribute getAttribute(String attributeType, String attributeId) {
		if (Attribute.TYPE_CARD_LINK.equals(attributeType)) {
			CardLinkAttribute cardAttribute = new CardLinkAttribute();
			cardAttribute.setId(attributeId);
			return cardAttribute;
		} else if (Attribute.TYPE_TYPED_CARD_LINK.equals(attributeType)) {
			TypedCardLinkAttribute typedAttribute = new TypedCardLinkAttribute();
			typedAttribute.setId(attributeId);
			return typedAttribute;
		} else if (Attribute.TYPE_DATED_TYPED_CARD_LINK.equals(attributeType)) {
			DatedTypedCardLinkAttribute datedTypedAttribute = new DatedTypedCardLinkAttribute();
			datedTypedAttribute.setId(attributeId);
			return datedTypedAttribute;
		} else if (Attribute.TYPE_BACK_LINK.equals(attributeType)) {
			BackLinkAttribute backLinkAttribute = new BackLinkAttribute();
			backLinkAttribute.setId(attributeId);
			return backLinkAttribute;
/*		} else if (Attribute.TYPE_PERSON.equals(attributeType)) {
			PersonAttribute personAttribute = new PersonAttribute();
			personAttribute.setId(attributeId);
			return personAttribute;*/
		}

		return null;
	}
}
