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

import com.jenkov.prizetags.tree.itf.ITree;

import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.jsp.JspException;


public class UnselectedNodeTag extends TagSupport{
    protected String tree           = null;
    protected String unselectParam    = null;
    protected String unselectedNode   = null;

    public String getTree() {
        return tree;
    }

    public void setTree(String tree) {
        this.tree = tree;
    }

    public String getUnselectParam() {
        if(unselectParam == null) return "unSelect";
        return unselectParam;
    }

    public void setUnselectParam(String unselectParam) {
        this.unselectParam = unselectParam;
    }

    public String getUnselectedNode() {
        return unselectedNode;
    }

    public void setUnselectedNode(String unselectedNode) {
        this.unselectedNode = unselectedNode;
    }


    public int doStartTag() throws JspException {
        String unselectId = pageContext.getRequest().getParameter(getUnselectParam());
        if(unselectId != null){
            ITree tree = (ITree) pageContext.getSession().getAttribute(getTree());
            pageContext.getRequest().setAttribute(getUnselectedNode(), tree.findNode(unselectId));
        }

        return SKIP_BODY;
    }



}
