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
package com.aplana.dmsi.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

public final class XmlUtils {

    private static final String SUB_ELEMENT_XPATH_FORMAT = "./%s";
    private static XPath xpath = XPathFactory.newInstance().newXPath();

    private XmlUtils() {
    }

    public static Document createDocument() throws XMLException {
	try {
	    DocumentBuilder builder = DocumentBuilderFactory.newInstance()
		    .newDocumentBuilder();
	    return builder.newDocument();
	} catch (ParserConfigurationException ex) {
	    throw new XMLException(ex.getMessage(), ex);
	}
    }

    public static Transformer createTransformer(InputStream stream)
	    throws XMLException {
	return createTransformer(new StreamSource(stream));
    }

    public static Transformer createTransformer(Source source)
	    throws XMLException {
	try {
	    TransformerFactory factory = TransformerFactory.newInstance();
	    return factory.newTransformer(source);
	} catch (TransformerConfigurationException ex) {
	    throw new XMLException(ex.getMessage(), ex);
	}
    }

    public static Document parseDocument(InputStream stream)
	    throws XMLException {
	try {
	    DocumentBuilder builder = DocumentBuilderFactory.newInstance()
		    .newDocumentBuilder();
	    return builder.parse(stream);
	} catch (SAXException ex) {
	    throw new XMLException(ex.getMessage(), ex);
	} catch (IOException ex) {
	    throw new XMLException(ex.getMessage(), ex);
	} catch (ParserConfigurationException ex) {
	    throw new XMLException(ex.getMessage(), ex);
	}
    }

    public static ByteArrayOutputStream serialize(Document document)
	    throws XMLException {
	ByteArrayOutputStream cardStream = null;
	try {
	    cardStream = new ByteArrayOutputStream();
	    OutputFormat format = new OutputFormat("XML", "UTF-8", false);
	    XMLSerializer serializer = new XMLSerializer(cardStream, format);
	    serializer.serialize(document.getDocumentElement());
	} catch (IOException ex) {
	    throw new XMLException(ex.getMessage(), ex);
	}
	return cardStream;
    }

    public static Element getSingleSubElement(Element parentElement,
	    String subElementName) {
	try {
	    String subElementExpression = String.format(
		    SUB_ELEMENT_XPATH_FORMAT, subElementName);
	    return (Element) xpath.evaluate(subElementExpression,
		    parentElement, XPathConstants.NODE);
	} catch (XPathExpressionException ex) {
	    throw new IllegalStateException(ex);
	}
    }

    public static NodeList getSubElements(Element parentElement,
	    String subElementName) {
	try {
	    String subElementExpression = String.format(
		    SUB_ELEMENT_XPATH_FORMAT, subElementName);
	    return (NodeList) xpath.evaluate(subElementExpression,
		    parentElement, XPathConstants.NODESET);
	} catch (XPathExpressionException ex) {
	    throw new IllegalStateException(ex);
	}
    }

    public static NodeList getAllSubElements(Element parentElement) {
	return getSubElements(parentElement, "*");
    }
}
