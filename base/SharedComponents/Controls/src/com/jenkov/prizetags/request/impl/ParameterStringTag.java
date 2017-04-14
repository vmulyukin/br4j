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



package com.jenkov.prizetags.request.impl;

import com.jenkov.prizetags.base.BaseTag;

import javax.servlet.jsp.JspException;
import java.util.Set;
import java.util.Iterator;
import java.util.HashSet;
import java.util.StringTokenizer;

/**
 * @author Jakob Jenkov - Copyright 2005 Jenkov Development
 */
public class ParameterStringTag extends BaseTag {

    protected String suffix = null;
    protected String ignore = "";

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getIgnore() {
        return ignore;
    }

    public void setIgnore(String ignore) {
        this.ignore = ignore;
    }


    public int doStartTag() throws JspException {
        Set parameterNames = filterOutIgnoredParameters();

        if(parameterNames.size() > 0){
            write("?");
        }

        Iterator iterator = parameterNames.iterator();
        while(iterator.hasNext()){
            String parameterName = (String) iterator.next();
            write(parameterName);
            write("=");
            write(pageContext.getRequest().getParameter(parameterName));
            if(iterator.hasNext()){
                write("&");
            }
        }

        if("true".equals(getSuffix())){
            if(parameterNames.size() > 0) write("&");
            else                          write("?");
        }

        return SKIP_BODY;
    }

    private Set filterOutIgnoredParameters() {
        Set ignoredParameters = new HashSet();
        StringTokenizer tokenizer = new StringTokenizer(getIgnore(), " ,");
        while(tokenizer.hasMoreElements()){
            String ignoredParameter = tokenizer.nextToken();
            ignoredParameters.add(ignoredParameter);
        }

        Set activeParameters = new HashSet();
        Set requestParameters = pageContext.getRequest().getParameterMap().keySet();
        Iterator iterator = requestParameters.iterator();
        while(iterator.hasNext()){
            String parameter = (String) iterator.next();
            if(!ignoredParameters.contains(parameter)){
                activeParameters.add(parameter);
            }
        }
        return activeParameters;
    }
}
