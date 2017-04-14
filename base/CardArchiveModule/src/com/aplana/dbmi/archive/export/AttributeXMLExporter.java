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

import java.util.Iterator;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;

import java.util.Collections;

/**
 * ����� ��� �������� ��������� � XML
 * @author ppolushkin
 *
 */
public abstract class AttributeXMLExporter implements AttributeExporter {
	
	protected static Log logger = LogFactory.getLog(AttributeXMLExporter.class);
	
	protected Document doc;
	protected Card card;
	protected Attribute attr;
	protected CardXMLExporter cexp;

	public AttributeXMLExporter(Document doc, Attribute attr) {
		this.doc = doc;
		this.attr = attr;
	}
	
	public Attribute getAttr() {
		return attr;
	}

	public void setAttr(Attribute attr) {
		this.attr = attr;
	}
	
	public Card getCard() {
		return card;
	}

	public void setCard(Card card) {
		this.card = card;
	}

	public void setDoc(Document doc) {
		this.doc = doc;
	}
	
	public CardXMLExporter getCexp() {
		return cexp;
	}

	public void setCexp(CardXMLExporter cexp) {
		this.cexp = cexp;
	}
	
	public Object export() throws DOMException, ParserConfigurationException {
		if(attr != null && !attr.isEmpty()) {
			Element attrElem = addAttr();
			String value = (String) getValue();
			addValues(attrElem, Collections.singletonList(value));
			return attrElem;
		}
		return null;
	}
	
	public abstract Object getValue();
	
	protected Element addAttr() throws DOMException, ParserConfigurationException {
		Element elemAttr = getDoc().createElement("attr");
		String attrCode = (String) attr.getId().getId();
	    if (attrCode != null) {
	    	elemAttr.setAttribute("code", attrCode);
	    }
	    String attrType = (String) attr.getType();
	    if (attrType != null) {
	    	elemAttr.setAttribute("type", attrType);
	    }
	    /*String attrName = (String) attr.getName();
	    if (attrName != null) {
	    	elemAttr.setAttribute("name", attrName);
	    }*/
	    //elem.appendChild(elemAttr);
		return elemAttr;
	};
	
	protected void addValues(Element elem, List<String> values) throws DOMException, ParserConfigurationException {
		Element current = addValuesWrap(elem);
		for(Iterator<String> it = values.iterator(); it.hasNext();) {
			String value = it.next();
			Element child = getDoc().createElement("value");
			child.setTextContent(value);
			current.appendChild(child);
		}
	}
	
	protected Element addValuesWrap(Element elem) throws DOMException, ParserConfigurationException {
		Element valuesWrap = getDoc().createElement("values");
		elem.appendChild(valuesWrap);
		return valuesWrap;
	}
	
	protected Document getDoc() throws ParserConfigurationException {
		if(doc == null) {
			DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
		    DocumentBuilder db = f.newDocumentBuilder();
		    this.doc = db.newDocument();
		}
		return doc;
	}

}
