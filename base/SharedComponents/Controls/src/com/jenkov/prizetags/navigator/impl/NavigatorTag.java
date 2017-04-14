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



package com.jenkov.prizetags.navigator.impl;

import com.jenkov.prizetags.base.NamePropertyTag;
import com.jenkov.prizetags.navigator.itf.INavigator;

import javax.servlet.jsp.JspException;
import java.util.Iterator;

/**
 * @author Jakob Jenkov - Copyright 2005 Jenkov Development
 */
public class NavigatorTag extends NamePropertyTag {

    protected Iterator links = null;
    protected String   id    = null;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int doStartTag() throws JspException {
        INavigator navigator = (INavigator) getBean(getName(), getProperty(), getKey(), getKeyedObjectProperty(), getScope());
        if(navigator == null) {
            return SKIP_BODY;
        }

        this.links = navigator.getLinks().iterator();
        if(links.hasNext()){
            pageContext.getRequest().setAttribute(getId(), links.next());
            return EVAL_BODY_INCLUDE;
        }
        return SKIP_BODY;
    }

    public int doAfterBody() throws JspException {
        if(this.links.hasNext()){
            pageContext.getRequest().setAttribute(getId(), links.next());
            return EVAL_BODY_AGAIN;
        }
        return SKIP_BODY;
    }
}
