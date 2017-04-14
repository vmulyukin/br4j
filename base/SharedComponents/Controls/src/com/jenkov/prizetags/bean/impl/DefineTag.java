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



package com.jenkov.prizetags.bean.impl;

import com.jenkov.prizetags.base.NamePropertyTag;

import javax.servlet.jsp.JspException;

/**
 * @author Jakob Jenkov - Copyright 2005 Jenkov Development
 */
public class DefineTag extends NamePropertyTag {

    protected String id      = null;
    protected String idScope = "request";

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdScope() {
        return idScope;
    }

    public void setIdScope(String idScope) {
        this.idScope = idScope;
    }

    public int doStartTag() throws JspException {
        Object bean = getBean();

        if("request".equals(getIdScope())){
            pageContext.getRequest().setAttribute(getId(), bean);
        } else if("session".equals(getIdScope())){
            pageContext.getSession().setAttribute(getId(), bean);
        } else if("application".equals(getIdScope())){
            pageContext.getServletContext().setAttribute(getIdScope(), bean);
        } else if ("page".equals(getIdScope())){
            pageContext.setAttribute(getIdScope(), bean);
        }

        return SKIP_BODY;
    }
}
