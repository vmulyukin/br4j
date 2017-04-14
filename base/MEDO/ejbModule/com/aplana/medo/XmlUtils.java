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
package com.aplana.medo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

public final class XmlUtils {

    public static String W3C_XML_SCHEMA_NS_URI = "http://www.w3.org/2001/XMLSchema";

    protected static final Log logger = LogFactory.getLog(XmlUtils.class);

    private XmlUtils() {
    }

    /**
     * Checks whether given file correspond to given schema
     *
     * @param file -
     *                source file
     * @param schema -
     *                schema instance
     * @return whether file is valid
     * @throws SAXException
     */
    public static void validateFile(File file, Schema schema)
	    throws SAXException {
	try {
	    Validator validator = schema.newValidator();
	    validator.validate(new StreamSource(file));
	} catch (SAXException ex) {
	    logger.error("Input document is invalid", ex);
	    throw ex;
	} catch (IOException ex) {
	    logger.error("Error during source file validation", ex);
	    throw new IllegalStateException(ex);
	}
    }

    /**
     * Checks whether given file correspond to given schema
     *
     * @param stream -
     *                stream contained file
     * @param schema -
     *                schema instance
     * @return whether file is valid
     * @throws SAXException
     */
    public static void validateFile(InputStream stream, Schema schema)
	    throws SAXException {
	try {
	    Validator validator = schema.newValidator();
	    validator.validate(new StreamSource(stream));
	} catch (SAXException ex) {
	    logger.error("Input document is invalid", ex);
	    throw ex;
	} catch (IOException ex) {
	    logger.error("Error during source file validation", ex);
	    throw new IllegalStateException(ex);
	}
    }

    /**
     * Transforms given file according to given transformer.
     *
     * @param file -
     *                source file that should be transformed
     * @param fileTransformer -
     *                transformation according to that transformer will be done
     * @return result of transformation represented by stream
     */
    public static ByteArrayOutputStream transformFile(File file,
	    Transformer fileTransformer) {
	ByteArrayOutputStream out = new ByteArrayOutputStream();
	try {
	    fileTransformer.transform(new StreamSource(file), new StreamResult(
		    out));
	} catch (TransformerException ex) {
	    logger.error("Error during transformation of file "
		    + file.getName());
	    return null;
	}
	return out;
    }

    public static ByteArrayOutputStream transformFile(InputStream stream,
	    Transformer fileTransformer) {
	ByteArrayOutputStream out = new ByteArrayOutputStream();
	try {
	    fileTransformer.transform(new StreamSource(stream),
		    new StreamResult(out));
	} catch (TransformerException ex) {
	    logger.error("Error during transformation");
	    return null;
	}
	return out;
    }

    public static Document createDOMDocument(InputStream inputStream) {
	Document document = null;
	try {
	    DocumentBuilderFactory factory = DocumentBuilderFactory
		    .newInstance();
	    factory.setNamespaceAware(true);
	    document = factory.newDocumentBuilder().parse(inputStream);
	} catch (ParserConfigurationException ex) {
	    logger.error("Create DOM document error", ex);
	    return null;
	} catch (IOException ex) {
	    logger.error("Create DOM document error", ex);
	    return null;
	} catch (SAXException ex) {
	    logger.error("Create DOM document error", ex);
	    return null;
	} finally {
	    try {
		if (inputStream != null) {
		    inputStream.close();
		}
	    } catch (IOException ex) {
		logger.error("createDOMDocument: error during stream closing");
	    }
	}
	return document;
    }

    /**
     * Creates <code>Document</code> instance that represents DOM structure of
     * given source represented by stream.
     *
     * @param source -
     *                source file according to that <code>Document</code> will
     *                be created
     * @return Document instance
     */
    public static Document createDOMDocument(ByteArrayOutputStream source) {
	InputStream inputStream = new ByteArrayInputStream(source.toByteArray());
	return createDOMDocument(inputStream);
    }

    /**
     * Serializes given DOM Document to XML file.
     *
     * @param document -
     *                DOM document
     * @return stream containing XML file of given document
     */
    public static ByteArrayOutputStream serialize(Document document) {
	ByteArrayOutputStream cardStream = null;
	try {
	    cardStream = new ByteArrayOutputStream();
	    OutputFormat format = new OutputFormat("XML", "UTF-8", true);

	    XMLSerializer serializer = new XMLSerializer(cardStream, format);

	    serializer.serialize(document.getDocumentElement());
	} catch (IOException ex) {
	    logger.error("Error during serialization xml", ex);
	    return null;
	}
	return cardStream;
    }
}
