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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.DatedTypedCardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.TypedCardLinkAttribute;
import com.aplana.dbmi.model.util.DateUtils;

/**
 * ������� � XML CardLink, TypedCardLink � DatedTypedCardLink ���������
 * ��� ���������� � XML ��������� ��������.
 * @author ppolushkin
 *
 */
public class CardLinkAttributeXMLExporter extends LinkAttributeXMLExporter {

	public CardLinkAttributeXMLExporter(Document doc, Attribute attr) {
		super(doc, attr);
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public List<ObjectId> getValue() {
		final Collection col = ((CardLinkAttribute) attr).getIdsLinked();
		List<ObjectId> list = new ArrayList<ObjectId>();
		Iterator it = col.iterator();
		while (it.hasNext())
		{
			ObjectId objId = (ObjectId) it.next();
			list.add(objId);
		}
		return list;
	}
	
	@Override
	protected void addLinkValue(ObjectId value, Element elem) throws DOMException, ParserConfigurationException {
		Element child = getDoc().createElement("value");
		child.setTextContent(String.valueOf(value.getId()));
		if(TypedCardLinkAttribute.class.equals(attr.getClass())) {
			TypedCardLinkAttribute a = (TypedCardLinkAttribute) attr;
			Long typed = (Long) a.getTypes().get(value);
			if(typed != null) {
				child.setAttribute("typed", String.valueOf(typed));
			}
		}
		if(DatedTypedCardLinkAttribute.class.equals(attr.getClass())) {			
			DatedTypedCardLinkAttribute a = (DatedTypedCardLinkAttribute) attr;
			Date dated = a.getDates().get(value);
			if(dated != null) {
				dated = DateUtils.toUTC(dated);
				child.setAttribute("dated", String.valueOf(dated.getTime()));
			}
		}
		elem.appendChild(child);
	}
}
