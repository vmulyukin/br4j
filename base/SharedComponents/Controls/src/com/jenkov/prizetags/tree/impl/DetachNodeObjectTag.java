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



package com.jenkov.prizetags.tree.impl;

import com.jenkov.prizetags.tree.itf.ITreeIteratorElement;
import com.jenkov.prizetags.tree.itf.ITreeNode;

import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.jsp.JspException;

public class DetachNodeObjectTag extends TagSupport{
    protected String detachedObject = null;
    protected String node           = null;

    public String getDetachedObject() {
        return detachedObject;
    }

    public void setDetachedObject(String detachedObject) {
        this.detachedObject = detachedObject;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }


    public int doStartTag() throws JspException {
        Object node = pageContext.getRequest().getAttribute(getNode());
        
        if(node == null){
            pageContext.getRequest().removeAttribute(getDetachedObject());
            return SKIP_BODY;
        }

        if(node instanceof ITreeIteratorElement){
            pageContext.getRequest().setAttribute(getDetachedObject(),
                    ((ITreeIteratorElement) node).getNode().getObject());
        } else if(node instanceof ITreeNode){
            pageContext.getRequest().setAttribute(getDetachedObject(),
                    ((ITreeNode) node).getObject());
        }

        return SKIP_BODY;
    }

}
