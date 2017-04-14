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

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * @author Jakob Jenkov,  Jenkov Development
 */
public class NodeIndentBaseTag extends TagSupport{

    protected String indentationTypeAttributeName = "indentationType";

    public String getIndentationType() throws JspException{
        return this.indentationTypeAttributeName;
    }

    public void setIndentationType(String name){
        this.indentationTypeAttributeName = name;
    }

    protected boolean getIndentationTypeAsBoolean() throws JspException{
        Boolean type = (Boolean)
                pageContext.getRequest().getAttribute(this.indentationTypeAttributeName);

        if(type == null) throw new JspException("No indentation type found for name: "
            + this.indentationTypeAttributeName);

        return type.booleanValue();
    }
}
