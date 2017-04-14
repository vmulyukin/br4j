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
package com.aplana.dbmi.card.extra;

import com.aplana.dbmi.card.JspAttributeEditor;
import com.aplana.dbmi.model.Attribute;

import javax.portlet.PortletException;

/**
 */
public class DownloadCardsJSBuilder extends ExtraJavascriptBuilder{
    @Override
    public void addJavascript(Attribute attr, StringBuilder stringBuilder) throws PortletException {

        String attrId = JspAttributeEditor.getAttrHtmlId(attr);
        String jsFunction = "\n function " + attrId + "_DownloadCardsJSAction(ref){ \n     "+attrId+"_DownloadCards(ref); \n }";
        stringBuilder.append(jsFunction);

    }

    public String getEntryPoint(Attribute attr) {

        String attrId = JspAttributeEditor.getAttrHtmlId(attr);
        return attrId+"_DownloadCardsJSAction(this)";

    }

}
