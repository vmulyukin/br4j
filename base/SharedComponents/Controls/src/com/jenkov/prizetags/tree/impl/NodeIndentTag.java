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
/*
    Copyright 2004 Jenkov Development

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/



/**
 * @version $revision$
 * @author Jakob Jenkov
 */
package com.jenkov.prizetags.tree.impl;

import com.jenkov.prizetags.tree.itf.ITreeIteratorElement;

import javax.servlet.jsp.JspException;
import java.util.Iterator;
import java.util.ArrayList;

public class NodeIndentTag extends NodeBaseTag{

    protected Iterator  indentationProfileIterator   = null;
    protected String    indentationTypeAttributeName = "indentationType";

    public String getIndentationType() throws JspException{
        return this.indentationTypeAttributeName;
    }

    public void setIndentationType(String name){
        this.indentationTypeAttributeName = name;
    }




    public int doStartTag() throws JspException{
        this.indentationProfileIterator = getElement().getIndendationProfile().iterator();

        if(this.indentationProfileIterator.hasNext()){
            pageContext.getRequest().setAttribute(getIndentationType(),
                    this.indentationProfileIterator.next());
            return EVAL_BODY_INCLUDE;
        }
        return SKIP_BODY;
    }


    public int doAfterBody() throws JspException{
        if(this.indentationProfileIterator.hasNext()){
            pageContext.getRequest().setAttribute(getIndentationType(),
                    this.indentationProfileIterator.next());

            return EVAL_BODY_AGAIN;
        }

        return SKIP_BODY;
    }
}
