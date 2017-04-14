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
package com.aplana.dbmi.card;

import java.util.Date;

import javax.portlet.PortletRequest;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.aplana.dbmi.model.Attribute;

public class XSLTRepeatableReportAttributeEditor extends XSLTReportAttributeEditor {

	protected Element createPart(Document xml, String text, Date date) {
		Element part = xml.createElement("raw-part");
		part.setTextContent(trimAndNewlineRight(text));
		return part;
	}
	
	@Override
	protected String getXSLTOutput(Document xml, PortletRequest request, Attribute attr)
			throws Exception {
		NodeList list = xml.getDocumentElement().getElementsByTagName("raw-part");
		String content ="";
		for (int i = 0; i < list.getLength(); i++){
			Element edit = (Element) list.item(i);
			content += edit.getTextContent();
			xml.getDocumentElement().removeChild(edit);
		}
		cardInfo.setAttributeEditorData(attr.getId(), KEY_EDITOR_DATA, content);
		return transform(xml, request.getPortletSession().getPortletContext().getRealPath(xsltLocation));
	}

}
