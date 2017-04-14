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
package com.aplana.dbmi.archive.export;

import org.w3c.dom.Document;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.service.impl.query.AttributeTypes;

/**
 * ������� ��� �������� ������� ����������� ��������� ���������
 * @author ppolushkin
 *
 */
public class AttributeXMLExporterFactory {
	

	private static AttributeXMLExporterFactory factory = new AttributeXMLExporterFactory();
	
	public static AttributeXMLExporterFactory getFactory() {
		return factory;
	}
	
	public AttributeXMLExporter getAttributeXMLExporter(Document doc, Card card, Attribute attr) {		
		String attrType = (String) attr.getType();
		
		if (AttributeTypes.TEXT.equals(attrType)) {
			return new StringAttributeXMLExporter(doc, attr);		
		}  else if (AttributeTypes.DATE.equals(attrType)) {
			return new DateAttributeXMLExporter(doc, attr);
		}  else if (AttributeTypes.STRING.equals(attrType)) {
			return new StringAttributeXMLExporter(doc, attr);
		} else if (AttributeTypes.INTEGER.equals(attrType)) {
			return new IntegerAttributeXMLExporter(doc, attr);
		} else if (AttributeTypes.LIST.equals(attrType)){
			return new ListAttributeXMLExporter(doc, attr);
		} else if (AttributeTypes.CARD_LINK.equals(attrType)){
			return new CardLinkAttributeXMLExporter(doc, attr);		
		} else if (AttributeTypes.TYPED_CLINK.equals(attrType)){
			return new CardLinkAttributeXMLExporter(doc, attr);
		} else if (AttributeTypes.DATED_TYPED_CLINK.equals(attrType)){
			return new CardLinkAttributeXMLExporter(doc, attr);
		} else if (AttributeTypes.PERSON.equals(attrType)){
			return new PersonAttributeXMLExporter(doc, attr);
		} else if (AttributeTypes.TREE.equals(attrType)){
			return new TreeAttributeXMLExporter(doc, attr);        
		} else if (AttributeTypes.HTML.equals(attrType)){
        	return new StringAttributeXMLExporter(doc, attr);
        } else if (AttributeTypes.BACK_LINK.equals(attrType)){
        	return new BackLinkAttributeXMLExporter(doc, card, attr);
        }
		return null;
	}

}
