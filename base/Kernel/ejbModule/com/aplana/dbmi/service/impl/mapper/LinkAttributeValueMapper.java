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
package com.aplana.dbmi.service.impl.mapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.HtmlAttribute;
import com.aplana.dbmi.model.LinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.TextAttribute;
import com.aplana.dbmi.model.workstation.AttributeValue;
import com.aplana.dbmi.service.impl.query.AttributeTypes;
import com.aplana.dbmi.service.impl.workstation.AttributeDef;

public abstract class LinkAttributeValueMapper<T extends LinkAttribute> extends
		AbstractAttributeValueMapper<LinkAttribute> {

	protected abstract T createAttribute();

	@Override
	protected void init(AttributeValue attr, LinkAttribute attrCreated) {
		super.init(attr, attrCreated);

		if (attr.isAttributeFromLink()) {
			final Class<?> attrType = AttributeTypes.getAttributeClass(AttributeDef.convertToString(attr.getType()));
			ObjectId labelAttrId = new ObjectId(attrType, attr.getLinkedCode());
			attrCreated.setLabelAttrId(labelAttrId);
		}
	}
	
	@Override
	protected void setValue(LinkAttribute attr, Object value) {

		Collection linkedIds = (Collection) value;
		Iterator linkedIterator = linkedIds.iterator();

		ObjectId labelAttrId = attr.getLabelAttrId();

		List cardIds = new ArrayList();
		int i =0;
		while (linkedIterator.hasNext()) {
			if (attr.getLabelAttrId() != null) {
				i++;				
				Card linkedCard = new Card();
				// generate pseudo unique identifier
				ObjectId linkedCardId = new ObjectId(Card.class, attr.getId().getId().toString() + "_" + labelAttrId.getId().toString() + "_" +i);
				linkedCard.setId(linkedCardId);
				
				try {
					final Class<?> attrType = attr.getLabelAttrId().getType();
					Attribute cardLinkAttrValue = (Attribute) attrType.newInstance();
					cardLinkAttrValue.setId(labelAttrId);
					if(StringAttribute.class.equals(attrType)) {
						((StringAttribute) cardLinkAttrValue).setValue((String) linkedIterator.next());
					} else if (DateAttribute.class.equals(attrType)) {
						((DateAttribute) cardLinkAttrValue).setValue((Date) linkedIterator.next());
					} else if (TextAttribute.class.equals(attrType)) {
						((TextAttribute) cardLinkAttrValue).setValue((String) linkedIterator.next());
					} else if (HtmlAttribute.class.equals(attrType)) {
						((HtmlAttribute) cardLinkAttrValue).setValue((String) linkedIterator.next());
					} else {
						logger.warn("The value hasn't been set for: " + linkedCardId);
						// to avoid infinite loop just retrieving the next element in the iteration
						linkedIterator.next();
					}
					// TODO: extend this for other attributes if required
					
					List attributes = new ArrayList();
					attributes.add(cardLinkAttrValue);
					linkedCard.setAttributes(attributes);
				} catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				cardIds.add(linkedCard);

			} else {

				Long id = (Long) linkedIterator.next();
				ObjectId cardId = new ObjectId(Card.class, id);
				Card linkedCard = new Card();
				linkedCard.setId(id);
				cardIds.add(linkedCard);
			}
		}
		attr.setIdsLinked(cardIds);
	}

}
