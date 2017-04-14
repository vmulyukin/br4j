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
package com.aplana.dbmi.card.util;

import java.io.File;
import java.util.Date;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class RepeatableReportXMLEditor extends ReportXMLEditor {

    private static final String RAW_PART_ELEMENT = "raw-part";

    public RepeatableReportXMLEditor(String text, File schema) {
	super(text, schema);
    }

    @Override
    public void appendPart(String round, Date date, String person, String text) {
	Element part = document.createElement(RAW_PART_ELEMENT);
	part.setTextContent(trimAndNewlineRight(text));
	document.getDocumentElement().appendChild(part);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.aplana.dbmi.card.util.ReportXMLEditor#popLastPart()
     */
    @Override
    public Element extractLastPart() {
	Element root = document.getDocumentElement();
	NodeList list = root.getElementsByTagName(RAW_PART_ELEMENT);
	StringBuilder content = new StringBuilder();
	for (int i = 0; i < list.getLength(); i++) {
	    Element edit = (Element) list.item(i);
	    content.append(edit.getTextContent());
	    root.removeChild(edit);
	}
	Element part = document.createElement(PART_ELEMENT);
	part.setTextContent(content.toString());
	return part;
    }

    public void appendPermanentPart(String round, Date date, String person, String text){
	super.appendPart(round, date, person, text);
    }
}
