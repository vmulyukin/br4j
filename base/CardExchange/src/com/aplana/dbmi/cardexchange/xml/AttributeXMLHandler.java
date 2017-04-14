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
package com.aplana.dbmi.cardexchange.xml;

import java.util.List;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.HtmlAttribute;
import com.aplana.dbmi.model.IntegerAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.TextAttribute;
import com.aplana.dbmi.model.TreeAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ServiceException;

public abstract class AttributeXMLHandler {
	protected String xmlType;
	protected Class type;

	public abstract AttributeXMLValue[] getValue(Attribute attr) throws DataException, ServiceException;

	public abstract void setValues(List values, Attribute attr) throws Exception;
	
	public boolean matchXmlValue(Attribute attr, AttributeXMLValue value) {
		throw new UnsupportedOperationException("Matching for attributes of type " + type.getCanonicalName() + " is not supported.");
	}

	public static final AttributeXMLHandler STRING;
	public static final AttributeXMLHandler TEXT;
	public static final AttributeXMLHandler INTEGER;
	public static final AttributeXMLHandler DATE;
	public static final AttributeXMLHandler LIST;
	public static final AttributeXMLHandler TREE;
	public static final AttributeXMLHandler PERSON;
	public static final AttributeXMLHandler CARD_LINK;
	public static final AttributeXMLHandler HTML;	
	
	private static AttributeXMLHandler[] types = new AttributeXMLHandler[] { 
		STRING = new StringAttributeXMLHandler("string", StringAttribute.class),
		TEXT = new StringAttributeXMLHandler("text", TextAttribute.class),
		INTEGER = new IntegerAttributeXMLHandler("integer", IntegerAttribute.class),
		DATE = new DateAttributeXMLHandler("date", DateAttribute.class),
		LIST = new ListAttributeXMLHandler("list", ListAttribute.class),
		TREE = new TreeAttributeXMLHandler("tree", TreeAttribute.class),
		PERSON = new PersonAttributeXMLHandler("person", PersonAttribute.class),
		CARD_LINK = new CardLinkAttributeXMLHandler("cardLink", CardLinkAttribute.class),
		HTML = new StringAttributeXMLHandler("html", HtmlAttribute.class)
	};

	protected AttributeXMLHandler(String value, Class clazz) {
		this.xmlType = value;
		this.type = clazz;
	}	
	
	public static AttributeXMLHandler getXmlType(Class clazz) {
		for (int i = 0; i < types.length; ++i) {
			if (types[i].type.equals(clazz)) {
				return types[i];
			}
		}
		return null;
	}
	
	public static AttributeXMLHandler getXmlType(String type) {
		for (int i = 0; i < types.length; ++i) {
			if (types[i].xmlType.equalsIgnoreCase(type)) {
				return types[i];
			}
		}
		return null;		
	}
	public Class getType() {
		return type;
	}
	public String getXmlType() {
		return xmlType;
	}
}
