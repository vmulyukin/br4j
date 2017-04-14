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

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.workstation.AttributeValue;
import com.aplana.dbmi.service.impl.query.AttributeTypes;
import com.aplana.dbmi.service.impl.workstation.AttributeDef;


/**
 * Represents factory for {@link AbstractAttributeValueMapper} children
 * 
 * @author skashanski
 *
 */
public class AttributeValueMapperFactory {
	
	
	
	private static AttributeValueMapperFactory singleton = null;
	
	
	public static AttributeValueMapperFactory getFactory() {
		if (singleton == null)
			singleton = new AttributeValueMapperFactory();
		return singleton;
	}
	
	
	private AttributeValueMapperFactory() {
		
	}
	
	
	public AbstractAttributeValueMapper getAttributeValueMapper(AttributeValue attribute) {
		
		String attrType = AttributeDef.convertToString(attribute.getType());
		
		if (attribute.isAttributeFromLink()) {
			String attrRealType = AttributeDef.convertToString(attribute.getRealType());
			if(AttributeTypes.BACK_LINK.equals(attrRealType)) {
				return new BackLinkAttributeValueMapper();
			} else return new CardLinkAttributeValueMapper();
		} else if (AttributeTypes.TEXT.equals(attrType)) {
			return new TextAttributeValueMapper();
		}  else if (AttributeTypes.DATE.equals(attrType)) {
			return new DateAttributeValueMapper();
		}  else if (AttributeTypes.STRING.equals(attrType)) {
			return new StringAttributeValueMapper();
		} else if (AttributeTypes.INTEGER.equals(attrType)) {
			return new IntegerAttributeValueMapper();
		} else if (AttributeTypes.LIST.equals(attrType)){
			return new ListAttributeValueMapper();
		} else  if (AttributeTypes.CARD_LINK.equals(attrType)){
			return new CardLinkAttributeValueMapper();
		} else  if (AttributeTypes.TYPED_CLINK.equals(attrType)){
			return new TypedCardLinkAttributeValueMapper();
		} else  if (AttributeTypes.PERSON.equals(attrType)){
			return new PersonAttributeValueMapper();
		} else if (AttributeTypes.TREE.equals(attrType)){
            return new TreeAttributeValueMapper();
        } else if (AttributeTypes.HTML.equals(attrType)){
            return new HtmlAttributeValueMapper();
        } else if (AttributeTypes.BACK_LINK.equals(attrType)){
            return new BackLinkAttributeValueMapper();
        }

		throw new IllegalArgumentException("Unsupported attribute type : " + attrType);
		
	}	
	
	
	

}
