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
package com.aplana.crypto;

import java.security.cert.Certificate;
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import com.aplana.dbmi.Portal;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class CheckSignature  {
	
	private final Log logger = LogFactory.getLog(getClass());
	private static final String TAG_CARD = "card";
	private static final String TAG_COLUMN = "column";
	private static final String TAG_VALUE = "value";
	private static final String TAG_SIGNATURE = "signature";
	private static final String TAG_CERTIFICATE = "certificate";

	public boolean verifyString(InputStream is) {
		Certificate certificate = null;
		boolean result = false;
		try {
			Document doc = initFromXml(is);
			String values = getAttrValues(doc);
			String signature = getSignature(doc);
			certificate = getCertificate(doc);
			logger.debug("verifyString values: " + values);
			logger.debug("verifyString signature: " + signature);
			result = CryptoLayer.getInstance(Portal.getFactory().getConfigService()).checkStringContentSignature(values, signature, certificate);
		} catch (Exception e) {
			logger.error("verifyString: ", e);
		}
		return result;
	}

	private Document initFromXml(InputStream is) {
		Document doc = null;
		try {
			doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
		} catch (Exception e) {
			logger.error("Error initializing signature attributes configuration", e);
		}
		return doc;
	}

	private String getAttrValues(Document doc) {
		
		String result = "";
		Element root = doc.getDocumentElement();
		NodeList cardsList = root.getElementsByTagName(TAG_CARD);
		for (int a = 0; a < cardsList.getLength(); a++) {
			Element cardNode = (Element) cardsList.item(a);
			NodeList columnList = cardNode.getElementsByTagName(TAG_COLUMN);
			for (int t = 0; t < columnList.getLength(); t++) {
				Element columnNode = (Element) columnList.item(t);
				NodeList valList = columnNode.getElementsByTagName(TAG_VALUE);
				for (int vv = 0; vv < valList.getLength(); vv++) {
					Element valNode = (Element) valList.item(vv);
					if (result.length() > 0) {
						result += ";";
					}
					
					result += valNode.getTextContent();
				}
			}
		}
		return Base64.byteArrayToBase64(result.getBytes());
	}
	
	private String getSignature(Document doc) {
		Element root = doc.getDocumentElement();
		Node signatureNode = ((NodeList) root.getElementsByTagName(TAG_SIGNATURE)).item(0);
		String signature = signatureNode.getTextContent();
		return signature;
	}

	private Certificate getCertificate(Document doc) {
		Element root = doc.getDocumentElement();
		Node certNode = ((NodeList) root.getElementsByTagName(TAG_CERTIFICATE)).item(0);
		String certs = certNode.getTextContent();
		Certificate certificate = null;
		if (certs != null && certs.length() > 0) {
			try {
				certificate = CryptoLayer.getInstance(Portal.getFactory().getConfigService()).getCertFromStringBase64(certs);
			} catch(Exception e) {
				logger.error("getCerticate: ", e);
			}
		}
		return certificate;
	}
}
