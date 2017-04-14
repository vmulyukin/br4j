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
package com.aplana.medo.cards;

import java.io.ByteArrayOutputStream;
import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import com.aplana.medo.XmlUtils;

/**
 * Class allows to create DOM structure of card and serialize that to XML
 */
public class CardXMLBuilder {

    public static final String CARD_TAG = "card";
    public static final String TEMPLATE_ID_ATTRIBUTE = "templateId";
    public static final String CARD_ID_ATTRIBUTE = "id";
    public static final String ATTRIBUTE_TAG = "attribute";
    public static final String CODE_ATTRIBUTE = "code";
    public static final String TYPE_ATTRIBUTE = "type";
    public static final String DESCRIPTION_ATTRIBUTE = "description";
    public static final String EDS_ATTRIBUTE = "edsTemp"; // Temporary
    public static final String VALUE_TAG = "value";

    public static final String CARD_LINK_TYPE = "cardLink";
    public static final String STRING_TYPE = "string";
    public static final String TEXT_TYPE = "text";
    public static final String DATE_TYPE = "date";
    public static final String HTML_TYPE = "html";
    public static final String LIST_TYPE = "list";

    public static final String NS = "http://aplana.com/dbmi/exchange/model/Card";

    private Document doc;
    private Element root;

    /**
     * Creates instance by initializing new DOM document.
     */
    public CardXMLBuilder() {
	DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
	DocumentBuilder db;
	try {
	    db = f.newDocumentBuilder();
	} catch (ParserConfigurationException ex) {
	    throw new IllegalStateException(
		    "Error during document builder initialization");
	}
	doc = db.newDocument();
	createRoot();
    }

    /**
     * Creates instance using existent DOM document.
     *
     * @param doc
     *                existent DOM document
     */
    public CardXMLBuilder(Document doc) {
	this.doc = doc;
	createRoot();
    }

    /**
     * Creates root element of DOM document.
     */
    private void createRoot() {
	root = doc.getDocumentElement();
	if (root == null) {
	    doc.appendChild(doc.createElementNS(NS, CARD_TAG));
	    root = doc.getDocumentElement();
	}
    }

    /**
     * Sets template Id of card.
     *
     * @param templateId -
     *                template Id of card
     */
    public void setTemplateId(long templateId) {
	root.setAttribute(TEMPLATE_ID_ATTRIBUTE, String.valueOf(templateId));
    }

    /**
     * Sets id XML attribute of card
     *
     * @param cardId -
     *                id of card
     */
    public void setCardId(long cardId) {
	root.setAttribute(CARD_ID_ATTRIBUTE, String.valueOf(cardId));
    }

    /**
     * Appends to card new attribute represented by DOM element.
     *
     * @param attribute
     *                DOM element that represents card attribute
     *
     * @see #createAttribute(Document, String, String, String)
     * @see #appendAttribute(String, String, String)
     */
    public void appendAttribute(Element attribute) {
	root.appendChild(attribute);
    }

    /**
     * Appends to card new attribute with given parameters.
     *
     * @param codeAttr
     *                code of attribute
     * @param typeAttr
     *                type of attribute
     * @param value
     *                value of attribute
     * @see #createAttribute(Document, String, String, String)
     */
    public void appendAttribute(String codeAttr, String typeAttr, String value) {
	Element element = createAttribute(doc, codeAttr, typeAttr, value);
	root.appendChild(element);
    }

    /**
     * Serializes current DOM structure to XML file.
     *
     * @return stream containing XML file of created card
     * @see #serialize(Document)
     */
    public ByteArrayOutputStream serialize() {
	return XmlUtils.serialize(doc);
    }

    /**
     * Creates new attribute DOM element by given parameters.
     *
     * @param document -
     *                DOM document used to create element
     * @param codeAttr -
     *                code of attribute
     * @param typeAttr -
     *                type of attribute
     * @param value -
     *                value of attribute
     * @return 'attribute' element
     */
    public static Element createAttribute(Document document, String codeAttr,
	    String typeAttr, String value) {

	Element attribute = document.createElementNS(NS, ATTRIBUTE_TAG);

	if (codeAttr != null && !codeAttr.equals("")) {
	    attribute.setAttribute(CODE_ATTRIBUTE, codeAttr);
	}
	if (codeAttr != null && !typeAttr.equals("")) {
	    attribute.setAttribute(TYPE_ATTRIBUTE, typeAttr);
	}

	Element valueElement = document.createElementNS(NS, VALUE_TAG);
	Text valueText = document.createTextNode(value);

	valueElement.appendChild(valueText);
	attribute.appendChild(valueElement);
	return attribute;
    }
    
    /**
     * Creates new attribute DOM element by given parameters.
     *
     * @param document -
     *                DOM document used to create element
     * @param codeAttr -
     *                code of attribute
     * @param typeAttr -
     *                type of attribute
     * @param descriptionAttr -
     *                description of attribute
     * @param value -
     *                value of attribute
     * @return 'attribute' element
     */
    public static Element createAttributeValueDescription(Document document, String codeAttr,
	    String typeAttr, String descriptionAttr, String value) {

	Element attribute = document.createElementNS(NS, ATTRIBUTE_TAG);

	if (codeAttr != null && !codeAttr.equals("")) {
	    attribute.setAttribute(CODE_ATTRIBUTE, codeAttr);
	}
	if (codeAttr != null && !typeAttr.equals("")) {
	    attribute.setAttribute(TYPE_ATTRIBUTE, typeAttr);
	}

	Element valueElement = document.createElementNS(NS, VALUE_TAG);
	
	if (descriptionAttr != null && !descriptionAttr.equals("")) {
		valueElement.setAttribute(DESCRIPTION_ATTRIBUTE, descriptionAttr);
	}
	Text valueText = document.createTextNode(value);

	valueElement.appendChild(valueText);
	attribute.appendChild(valueElement);
	return attribute;
    }
    
    //Temporary
    public static void createAttributeEdsInValue_Tag(Document document, Element valueElement, String edsAttr)
    {
    	valueElement.setAttribute(EDS_ATTRIBUTE, edsAttr);
    }
    //

    /**
     * Creates new XPath instance with set namespace context. The
     * {@link CardNamespaceContext#DEFAULT_PREFIX} should be used in XPath
     * expressions.
     *
     * @return new XPath instance with set namespace context
     */
    public static XPath newXPath() {
	XPath xpath = XPathFactory.newInstance().newXPath();
	xpath.setNamespaceContext(new CardXMLBuilder.CardNamespaceContext());
	return xpath;
    }

    public static class CardNamespaceContext implements NamespaceContext {

	public static final String DEFAULT_PREFIX = "jbr";

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.xml.namespace.NamespaceContext#getNamespaceURI(java.lang.String)
	 */
	public String getNamespaceURI(String prefix) {
	    if (prefix == null)
		throw new IllegalArgumentException("Prefix cannot be null");
	    else if (prefix.equals(XMLConstants.DEFAULT_NS_PREFIX))
		return CardXMLBuilder.NS;
	    else if (prefix.equals(DEFAULT_PREFIX))
		return CardXMLBuilder.NS;
	    else
		return "";
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.xml.namespace.NamespaceContext#getPrefix(java.lang.String)
	 */
	public String getPrefix(String arg0) {
	    return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.xml.namespace.NamespaceContext#getPrefixes(java.lang.String)
	 */
	public Iterator<String> getPrefixes(String arg0) {
	    return null;
	}
    }
}
