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
package com.aplana.medo.processors;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import com.aplana.medo.CardImportClient;
import com.aplana.medo.XmlUtils;
import com.aplana.medo.cards.CardException;
import com.aplana.medo.cards.CardXMLBuilder;
import com.aplana.medo.cards.NotificationCardHandler;

/**
 * <code>NotificationProcessor</code> is <code>Processor</code> that allows
 * to process IEDMS <em>notification</em> according to required business
 * logic.
 */
public abstract class NotificationProcessor extends Processor {

    protected final Log logger = LogFactory.getLog(getClass());

    private static final String PROPERTIES_PREFIX = "code.notification.";

    private static final String ORIGINAL_SRC_KEY = "original";

    private static final String[] INFO_KEYS = { ORIGINAL_SRC_KEY };

    Map<String, String> values;

    /**
     * Creates new instance of <code>NotificationProcessor</code>
     *
     * @param properties -
     *                configuration that define code names of necessary
     *                attributes {@link #INFO_KEYS} in the following format:
     *                &lt;{@link #PROPERTIES_PREFIX}&gt;.&lt;{@link #INFO_KEYS}
     *                item&gt;
     */
    public NotificationProcessor(Properties properties) {
	super(properties);
    }

    /**
     * Imports XML data according to stored in <code>document</code>
     * representation:
     * <ol>
     * <li>Determine card id of 'Distribution item' card:</li>
     * <ul>
     * <li>from 'card' element 'id' attribute;</li>
     * <li>if one is not defined, try to find distribution item card according
     * to foundation of notification and source organization</li>
     * </ul>
     * <li>Create new notification according to XML.</li>
     * <li>Link created notification card to 'Distribution item' card.</li>
     * <ol>
     *
     * @param document -
     *                DOM document contained XML data for processing
     * @return id of imported card or -1 if it is impossible to process this XML
     *
     * @throws ProcessorException
     */
    @Override
    public long process(Document document) throws ProcessorException {
	ByteArrayOutputStream doc = XmlUtils.serialize(document);
	logger.info("Process XML: " + doc.toString());
	values = readValues(document);

	long notificationId = CardImportClient.callImportCardService(doc
		.toByteArray(), null);

	postProcess(notificationId);

	try {
	    // Force card saving processors
	    new NotificationCardHandler(notificationId).saveCard();
	} catch (CardException ex) {
	    throw new ProcessorException(ex);
	}
	return notificationId;
    }

    protected abstract void postProcess(long notificationId)
	    throws ProcessorException;

    /**
     * Read from XML document values of necessary attributes (<code>attribute</code>
     * elements that have code equals to value of <code>properties</code> with
     * key from {@link #INFO_KEYS}. Key has the following format: &lt;{@link #PROPERTIES_PREFIX}&gt;.&lt;{@link #INFO_KEYS}
     * item&gt;
     *
     * @param document
     * @return
     * @throws ProcessorException
     */
    private Map<String, String> readValues(Document document)
	    throws ProcessorException {
	Map<String, String> readedValues = new HashMap<String, String>();
	XPath xpath = CardXMLBuilder.newXPath();
	for (String infoKey : getRequiredInfoKeys()) {
	    try {
		String key = PROPERTIES_PREFIX + infoKey;
		String code = properties.getProperty(key);
		if (code == null) {
		    logger.error(String.format(
			    "Property with key='%s' should be defined", key));
		    throw new ProcessorException();
		}
		String value = (String) xpath.evaluate(String.format(
			"//%1$s:attribute[@code='%2$s']/%1$s:value",
			CardXMLBuilder.CardNamespaceContext.DEFAULT_PREFIX,
			code), document, XPathConstants.STRING);
		readedValues.put(infoKey, value);
	    } catch (XPathExpressionException ex) {
		logger.error(String.format(
			"Error during  read value of element with '%s' key",
			infoKey));
		throw new ProcessorException();
	    }
	}
	return readedValues;
    }

    protected Collection<String> getRequiredInfoKeys() {
	return Arrays.asList(INFO_KEYS);
    }
}
