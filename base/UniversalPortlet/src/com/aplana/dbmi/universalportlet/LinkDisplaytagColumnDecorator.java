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
package com.aplana.dbmi.universalportlet;

import java.text.MessageFormat;
import java.util.List;

import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.servlet.jsp.PageContext;

import org.displaytag.decorator.DisplaytagColumnDecorator;
import org.displaytag.exception.DecoratorException;
import org.displaytag.properties.MediaTypeEnum;

/*
import com.ibm.portal.portlet.service.PortletServiceHome;
import com.ibm.portal.portlet.service.PortletServiceUnavailableException;
import com.ibm.portal.portlet.service.url.PortalURLGenerationService;
import com.ibm.portal.portlet.service.url.PortalURLWriter;
*/

public class LinkDisplaytagColumnDecorator implements DisplaytagColumnDecorator {
    private MessageFormat link;
    private int linkColumnIndex;

    public static final String EDIT_CARD_ID_FIELD = "MI_EDIT_CARD";

    public static final String BACK_URL_FIELD = "MI_BACK_URL_FIELD";

    public LinkDisplaytagColumnDecorator(RenderRequest request, RenderResponse response) {
        super();
    }

    public Object decorate(Object columnValue, PageContext pageContext, MediaTypeEnum mediaType) throws DecoratorException {
        String stringValue;
        if (columnValue != null) {
            stringValue = columnValue.toString().trim();
            if (stringValue.length() == 0) {
                stringValue = "-";
            }
        } else {
            stringValue = "-";
        }

        List columnValues = (List) pageContext.getAttribute("dataItem");

        String url = link.format(new Object[]{ columnValues.get(linkColumnIndex) });

        return "<a href=\"" + url + "\">" + stringValue + "</a>";
    }

    public MessageFormat getLink() {
        return link;
    }

    public void setLink(MessageFormat link) {
        this.link = link;
    }

    public int getLinkColumnIndex() {
        return linkColumnIndex;
    }

    public void setLinkColumnIndex(int linkColumnIndex) {
        this.linkColumnIndex = linkColumnIndex;
    }

}
