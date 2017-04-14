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
package com.aplana.cms.tags;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.cms.ContentProducer;
import com.aplana.cms.ContentRequest;
import com.aplana.cms.Tag;
import com.aplana.cms.TagProcessor;
import com.aplana.dbmi.action.Material;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;

/**
 * Tag shows HTML-fragment received by processing XML-file, attached to card
 * with given XSL-transformation
 */
public class XslViewTag implements TagProcessor {
	private static final String ATTR_XSL_URL = "xslurl";
	private static final String ATTR_XSL_CARD_ID = "xsl"; 
	private Log logger = LogFactory.getLog(getClass());
	private String content;
	public boolean prepareData(Tag tag, Card item, ContentProducer cms) throws Exception {
		String xslCardId = tag.getAttribute(ATTR_XSL_CARD_ID),
			xslUrl = tag.getAttribute(ATTR_XSL_URL);
		InputStream xslStream;
		if (xslCardId != null) {
			ObjectId id = new ObjectId(Card.class, Long.valueOf(xslCardId));
			xslStream = cms.getMaterialObject(id).getData();
			if (xslUrl != null) {
				logger.warn("Both '" + ATTR_XSL_CARD_ID + "' and '" + ATTR_XSL_URL + 
					"' attributes found. Value of '" + ATTR_XSL_URL + "' will be ignored");
			}
		} else if (xslUrl != null){
			xslStream = getXslUrl(xslUrl, cms.getRequest()).openStream();
		} else {
			throw new IllegalArgumentException("XSL tranformation not specified");
		}

		Material material = cms.getMaterialObject(item.getId());
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer transformer = tf.newTransformer(new StreamSource(xslStream));
		StringWriter writer = new StringWriter();
		transformer.transform(new StreamSource(material.getData()), new StreamResult(writer));
		content = writer.toString();
		return true;
	}
	
	private URL getXslUrl(String stXslUrl, ContentRequest request) throws IOException {
		logger.debug(ATTR_XSL_URL + " = " + stXslUrl);
		if (!stXslUrl.matches("^[a-zA-Z0-9]*://.*")) {
			StringBuffer prefix = new StringBuffer("http");
			if (request.isSecure()) {
				prefix.append('s'); 
			}
			prefix.append("://localhost:")
				.append(request.getServerPort());
			if (!stXslUrl.startsWith("/")) {
				prefix.append('/');
			}
			stXslUrl = prefix.append(stXslUrl).toString();
			logger.debug("Fully qualified " + ATTR_XSL_URL + " = " + stXslUrl);
		}
		return new URL(stXslUrl);
	}

	public void writeHtml(PrintWriter out, Tag tag, Card item,
			ContentProducer cms) {
		out.print(content);
	}
}
