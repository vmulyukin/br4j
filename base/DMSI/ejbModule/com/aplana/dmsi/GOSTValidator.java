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
package com.aplana.dmsi;

import java.io.ByteArrayInputStream;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;

import com.aplana.dmsi.types.Header;
import com.aplana.dmsi.types.HeaderMessageEnumType;
import com.aplana.dmsi.util.XMLException;
import com.aplana.dmsi.util.XmlUtils;

public class GOSTValidator {

	private XPath xpath = XPathFactory.newInstance().newXPath();
	private static final String UNEXPECTED_EXCEPTION_CODE = "xml.validation.unexpectedException";

	public void validate(ByteArrayInputStream stream) throws GOSTException {
		Document doc = null;
		try {
			doc = XmlUtils.parseDocument(stream);
		} catch (XMLException ex) {
			throw new GOSTException("xml.bad", ex);
		}
		testMsgType(doc);
	}

	public void validate(Header header) throws GOSTException {
		testDocumentPresence(header);
		testAcknowledgementPresence(header);
	}

	private void testMsgType(Document doc) throws GOSTException {
		try {
			String result = xpath.evaluate("/Header/@msg_type", doc);
			if (result == null || "".equals(result.trim())) {
				throw new GOSTException("xml.validation.msg_type.incorrectValue");
			}
			byte value = Byte.parseByte(result);
			if (value < 0 || value > 4) {
				throw new GOSTException("xml.validation.msg_type.incorrectValue");
			}
		} catch (NumberFormatException ex) {
			throw new GOSTException("xml.validation.msg_type.incorrectValue", ex);
		} catch (XPathExpressionException ex) {
			throw new GOSTException(UNEXPECTED_EXCEPTION_CODE, ex);
		}
	}

	// ��� ����������� ������ ����������� �������, ����� ������������� ��������
	// �� ��������
	private void testDocumentPresence(Header header) throws GOSTException {
		if (HeaderMessageEnumType.DOCUMENT.equals(header.getMsgType())) {
			if (header.getDocument() == null) {
				throw new GOSTException("model.validation.document.absent");
			}
		}
	}

	// ��� ����������� ������ ����������� �������, ����� ������������� ��������
	// �� ��������
	private void testAcknowledgementPresence(Header header) throws GOSTException {
		if (HeaderMessageEnumType.ACKNOWLEDGEMENT.equals(header.getMsgType())) {
			if (header.getAcknowledgement() == null) {
				throw new GOSTException("model.validation.acknowledgement.absent");
			}
		}
	}
}
