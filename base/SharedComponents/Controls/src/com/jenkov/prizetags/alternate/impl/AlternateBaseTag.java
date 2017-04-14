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



package com.jenkov.prizetags.alternate.impl;

import com.jenkov.prizetags.alternate.itf.IAlternation;
import com.jenkov.prizetags.base.BaseTag;

import javax.servlet.jsp.JspException;

/**
 * @author Jakob Jenkov - Copyright 2005 Jenkov Development
 */
public class AlternateBaseTag extends BaseTag{

    protected String altId = null;
    protected String scope = null;

    public String getAltId() {
        return altId;
    }

    public void setAltId(String altId) {
        this.altId = altId;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }


    protected IAlternation getAlternation() throws JspException {
        if(altId == null){
            throw new JspException("You must specify the altId of the alternation to match.");
        }
        Object alt = getBean(getAltId(), null, getScope());
        if(alt == null){
            throw new JspException("No alternation found in the request attributes under the key: " + altId
            + " and scope = " + getScope());
        }

        if(! (alt instanceof IAlternation)){
            throw new JspException("The object found under key " + altId +
                    " and scope " + getScope() + " is not an alternation. Check your altId attribute again.");
        }

        return (IAlternation) alt;
    }

}
