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

import static com.aplana.medo.cards.CardXMLBuilder.CARD_ID_ATTRIBUTE;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.transform.Transformer;
import javax.xml.validation.Schema;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.client.DataServiceFacade;
import com.aplana.medo.Importer.CardImporter;
import com.aplana.medo.Importer.Data;
import com.aplana.medo.cards.CardXMLBuilder;
import com.aplana.medo.cards.ImportedDocumentCardHandler;
import com.aplana.medo.converters.Converter;
import com.aplana.medo.converters.ConverterException;
import com.aplana.medo.converters.ConverterFactory;
import com.aplana.medo.processors.Processor;
import com.aplana.medo.processors.ProcessorException;
import com.aplana.medo.processors.ProcessorFactory;

public class MEDODocumentImporter implements CardImporter {

    public static final String PROCESSOR_ATTRIBUTE_NAME = "processor";
    public static final String DEFAULT_PROCESSOR_NAME = "default";

    private static final String CARD_ID_SRC_ATTRIBUTE = "src";
    private static final String CODE_XML_ATTR = "code";

    private static final ObjectId ORIGINAL_SOURCE_ATTRIBUTE_ID = ObjectId.predefined(CardLinkAttribute.class, "jbr.original.source");

	//TODO ��������� ��� �������� �� �������
	private static final List<String> MULTIVALUED_ATTRS = new ArrayList<String>();
	static {
		MULTIVALUED_ATTRS.add((String) ObjectId.predefined(CardLinkAttribute.class, "jbr.files").getId());
	}

    private Map<String, List<Converter>> convertersCache = new HashMap<String, List<Converter>>();

    protected final Log logger = LogFactory.getLog(getClass());

    private Config config;
    private DataServiceFacade serviceBean;

    public static class Config {
	private Transformer transformer = null;
	private Transformer importedDocTransformer = null;
	private Properties properties;
	private Schema schema = null;

	public Config() {
	}

	public Transformer getTransformer() {
	    return this.transformer;
	}

	public void setTransformer(Transformer transformer) {
	    this.transformer = transformer;
	}

	public Transformer getImportedDocTransformer() {
	    return this.importedDocTransformer;
	}

	public void setImportedDocTransformer(Transformer importedDocTransformer) {
	    this.importedDocTransformer = importedDocTransformer;
	}

	public Properties getProperties() {
	    return this.properties;
	}

	public void setProperties(Properties properties) {
	    this.properties = properties;
	}

	public Schema getSchema() {
	    return this.schema;
	}

	public void setSchema(Schema schema) {
	    this.schema = schema;
	}
    }

    public long importCard(Data data, long importedDocCardId) throws MedoException, DataException {
    	return importByMedo(data.getRawData(), importedDocCardId);
    }

    private long importByMedo(byte[] fileBytes, long importedDocCardId) throws DataException {
		convertersCache.clear();
		validateXML(fileBytes);
		updateImportedDocumentCard(fileBytes, importedDocCardId);
		Document mainCardDocument = transformFile(fileBytes);
		appendOriginalSourceAttribute(mainCardDocument, importedDocCardId);
		calculateCardIdIfNecessary(mainCardDocument);
		processElementsByConverters(mainCardDocument);
		return processResultXML(mainCardDocument);
    }

	private void appendOriginalSourceAttribute(Document mainCardDocument, long importedDocCardId) throws DOMException {
		Element cardRoot = mainCardDocument.getDocumentElement();
		Element originalSourceAttribute = CardXMLBuilder.createAttribute(
			mainCardDocument, ORIGINAL_SOURCE_ATTRIBUTE_ID.getId()
				.toString(), CardXMLBuilder.CARD_LINK_TYPE, String
				.valueOf(importedDocCardId));
		cardRoot.appendChild(originalSourceAttribute);
	}

    private long processResultXML(Document mainCardDocument)
	    throws DOMException, ProcessorException {
	Element cardRoot = mainCardDocument.getDocumentElement();
	long cardId = -1;
	String processorKey;
	if (cardRoot.hasAttribute(PROCESSOR_ATTRIBUTE_NAME)) {
	    processorKey = cardRoot.getAttribute(PROCESSOR_ATTRIBUTE_NAME);
	    cardRoot.removeAttribute(PROCESSOR_ATTRIBUTE_NAME);
	} else {
	    processorKey = DEFAULT_PROCESSOR_NAME;
	}

	Processor processor = ProcessorFactory.instance().createProcessor(
		config.getProperties(), processorKey);
	if (processor != null) {
	    cardId = processor.process(mainCardDocument);
	}
	return cardId;
    }

	private void processElementsByConverters(Document mainCardDocument) throws ConverterException, DOMException {

		Map<String, Element> multValuesElements = new HashMap<String, Element>();

		Element cardRoot = mainCardDocument.getDocumentElement();
		NodeList nodes = cardRoot.getChildNodes();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			if (node.getNodeType() != Node.ELEMENT_NODE) {
			continue;
			}

			Element element = (Element) node;
			String tagName = element.getTagName();
			List<Converter> converters = getConvertersOf(tagName);

			cardRoot.removeChild(element);

			if (converters.isEmpty()) {
				logger.warn(String.format("Converter for '%s' tag is not defined.", tagName));
			} else {
				for (Converter converter : converters) {
					Element attribute = converter.convert(mainCardDocument, element);
					if (attribute != null) {

						String attrCode = attribute.getAttribute(CODE_XML_ATTR);
						if (attrCode != null && MULTIVALUED_ATTRS.contains(attrCode)) {
							if (!multValuesElements.containsKey(attrCode)) {
								multValuesElements.put(attrCode, attribute);
							} else {
								Element elem = multValuesElements.get(attrCode);
								//��������� ��������� <value>
								elem.appendChild(attribute.getFirstChild());
							}
						} else {
							cardRoot.appendChild(attribute);
						}
					}
				}
			}
		}

		//������ ��������� ��� ���� ������������ ��������
		for (Element elem : multValuesElements.values()) {
			cardRoot.appendChild(elem);
		}

	}

    private void calculateCardIdIfNecessary(Document mainCardDocument)
	    throws MedoException, ConverterException, DOMException {
	Element cardRoot = mainCardDocument.getDocumentElement();
	String cardIdTag = config.getProperties().getProperty("tag.cardId");
	if (cardIdTag == null || "".equals(cardIdTag)) {
	    logger.error("The 'tag.cardId' property should be set");
	    throw new MedoException();
	}
	NodeList cardIdElementList = cardRoot.getElementsByTagName(cardIdTag);
	if (cardIdElementList.getLength() > 1) {
	    logger.error(String.format(
		    "There should be exactly one %s element in XML file",
		    cardIdTag));
	    throw new MedoException();
	}
	if (cardIdElementList.getLength() == 1) {
	    Element cardIdElement = (Element) cardIdElementList.item(0);
	    String cardIdType = cardIdElement
		    .getAttribute(CARD_ID_SRC_ATTRIBUTE);
	    if (cardIdType == null || "".equals(cardIdType)) {
		logger.error(String.format(
			"%s element should have non-empty '%s' attribute.",
			cardIdTag, CARD_ID_SRC_ATTRIBUTE));
		throw new MedoException();
	    }
	    List<Converter> cardIdConverters = getConvertersOf(String.format(
		    "%s.%s", cardIdElement.getTagName(), cardIdType));
	    if (cardIdConverters.isEmpty()) {
		logger.error(String.format(
			"Converter for %s tag with src='%s' is not defined.",
			cardIdTag, cardIdType));
		throw new MedoException();
	    }

	    Element cardIdValueElement = cardIdConverters.get(0).convert(
		    mainCardDocument, cardIdElement);

	    if (cardIdValueElement == null) {
		logger.warn("Card id was not calculated");
	    } else {
		String valueOfCardId = cardIdValueElement.getTextContent();
		cardRoot.setAttribute(CARD_ID_ATTRIBUTE, valueOfCardId);
	    }
	}
    }

    private void updateImportedDocumentCard(byte[] fileBytes,
	    long importedDocCardId) throws DataException {
	ByteArrayOutputStream importedDocXML = XmlUtils.transformFile(
		new ByteArrayInputStream(fileBytes), config
			.getImportedDocTransformer());
	log("Transformed XML for imported document", importedDocXML
		.toByteArray());
	new ImportedDocumentCardHandler(importedDocCardId).updateCard(XmlUtils
		.createDOMDocument(importedDocXML));
    }

    private Document transformFile(byte[] fileBytes) throws MedoException {
	if (config.getTransformer() == null
		|| config.getImportedDocTransformer() == null) {
	    logger.error("One of transformers was not set for import");
	    throw new MedoException();
	}

	ByteArrayOutputStream transformedFile = XmlUtils.transformFile(
		new ByteArrayInputStream(fileBytes), config.getTransformer());
	log("Transformed XML", transformedFile.toByteArray());

	Document mainCardDocument = XmlUtils.createDOMDocument(transformedFile);
	return mainCardDocument;
    }

    private void validateXML(byte[] fileBytes) throws MedoException {
	if (config.getSchema() != null) {
	    try {
		XmlUtils.validateFile(new ByteArrayInputStream(fileBytes),
			config.getSchema());
	    } catch (SAXException ex) {
		throw new MedoException("jbr.medo.importer.invalidXml");
	    }
	}
    }

    private void log(String prefix, byte[] data) {
	try {
	    logger.info(prefix + ":\n" + new String(data, "UTF-8"));
	} catch (UnsupportedEncodingException ex) {
	    throw new IllegalStateException(ex);
	}
    }

    /**
     * Returns list of predefined in properties converters by <code>name</code>
     *
     * @param name -
     *                the name, converters for which should be returned
     * @return list of predefined converters
     * @throws ConverterException
     */
    private List<Converter> getConvertersOf(String name)
	    throws ConverterException {
	List<Converter> converters;
	if (convertersCache.containsKey(name)) {
	    converters = convertersCache.get(name);
	} else {
	    converters = new ArrayList<Converter>();
	    Converter converter = null;

	    int n = 0;
	    do {
		String keySuffix = String.format("%s_%d", name, n++);
		converter = ConverterFactory.instance().createConverter(
			config.getProperties(), keySuffix);
		if (converter != null) {
		    converter.setServiceBean(serviceBean);
		    converters.add(converter);
		}
	    } while (converter != null);
	    convertersCache.put(name, converters);
	}
	return converters;
    }

    public Config getConfig() {
	return this.config;
    }

    public void setConfig(Config config) {
	this.config = config;
    }

    public void setDataServiceBean(DataServiceFacade serviceFacade) {
	this.serviceBean = serviceFacade;
}

}
