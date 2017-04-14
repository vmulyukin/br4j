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
package com.aplana.dbmi.card.hierarchy.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.IntegerAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.TextAttribute;
import com.aplana.dbmi.model.TreeAttribute;
import com.aplana.dbmi.service.DataServiceBean;

public abstract class AttributeHandler {
	public static final AttributeHandler STRING;
	public static final AttributeHandler TEXT;
	public static final AttributeHandler INTEGER;
	public static final AttributeHandler DATE;
	public static final AttributeHandler LIST;
	public static final AttributeHandler TREE;
	public static final AttributeHandler PERSON;
	public static final AttributeHandler CARD_LINK;
	
	private static AttributeHandler[] types = new AttributeHandler[] { 
		STRING = new StringAttributeHandler("string", StringAttribute.class),
		TEXT = new StringAttributeHandler("text", TextAttribute.class),
		INTEGER = new IntegerAttributeHandler("number", IntegerAttribute.class),
		DATE = new DateAttributeHandler("date", DateAttribute.class),
		LIST = new ListAttributeHandler("list", ListAttribute.class),
		TREE = new TreeAttributeHandler("tree", TreeAttribute.class),
		PERSON = new PersonAttributeHandler("user", PersonAttribute.class),
		CARD_LINK = new CardLinkAttributeHandler("link", CardLinkAttribute.class),
	};

	protected Log logger = LogFactory.getLog(getClass());
	protected String xmlType;
	protected Class type;

	protected AttributeHandler(String value, Class clazz) {
		this.xmlType = value;
		this.type = clazz;
	}	
	public Class getType() {
		return type;
	}
	public String getXmlType() {
		return xmlType;
	}
	public abstract Object stringToValue(String st, DataServiceBean serviceBean) throws Exception;
	
	public ValuesRange parseValuesRange(String stRange, DataServiceBean serviceBean) throws Exception {
		throw new UnsupportedOperationException("Values range is not suported for attributes of type " + type.getName());
	}
	
	public boolean matchValue(Attribute attr, Object value) {
		throw new UnsupportedOperationException("Matching for attributes of type " + type.getName() + " is not supported.");
	}
	
	public static AttributeHandler getXmlType(Class clazz) {
		for (int i = 0; i < types.length; ++i) {
			if (types[i].type.equals(clazz)) {
				return types[i];
			}
		}
		throw new IllegalArgumentException("Unknown attribute type: '" + clazz.getName() + '\'');
	}
	
	public static AttributeHandler getXmlType(String type) {
		for (int i = 0; i < types.length; ++i) {
			if (types[i].xmlType.equals(type)) {
				return types[i];
			}
		}
		throw new IllegalArgumentException("Unknown attribute type alias: '" + type + '\'');
	}
}
