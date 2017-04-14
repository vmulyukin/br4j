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


public class SelectedNodeTag extends TagSupport{
    protected String tree           = null;
    protected String selectParam    = null;
    protected String selectedNode   = null;

    public String getTree() {
        return tree;
    }

    public void setTree(String tree) {
        this.tree = tree;
    }

    public String getSelectParam() {
        if(selectParam == null) return "select";
        return selectParam;
    }

    public void setSelectParam(String selectParam) {
        this.selectParam = selectParam;
    }

    public String getSelectedNode() {
        return selectedNode;
    }

    public void setSelectedNode(String selectedNode) {
        this.selectedNode = selectedNode;
    }


    public int doStartTag() throws JspException {
        String selectId = pageContext.getRequest().getParameter(getSelectParam());
        if(selectId != null){
            ITree tree = (ITree) pageContext.getSession().getAttribute(getTree());
            pageContext.getRequest().setAttribute(getSelectedNode(), tree.findNode(selectId));
        }

        return SKIP_BODY;
    }



}
