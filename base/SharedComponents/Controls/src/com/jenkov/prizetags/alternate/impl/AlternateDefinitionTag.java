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
import java.util.StringTokenizer;

/**
 * @author Jakob Jenkov
 *         Copyright 2004 Jenkov Development
 */
public class AlternateDefinitionTag extends BaseTag{
    protected String altId     = null;
    protected String scope     = null;
    protected String from      = null;
    protected String to        = null;
    protected String altValues = null;
    protected String startAt   = null;

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

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getAltValues() {
        return altValues;
    }

    public void setAltValues(String altValues) {
        this.altValues = altValues;
    }

    public String getStartAt() {
        return startAt;
    }

    public void setStartAt(String startAt) {
        this.startAt = startAt;
    }

    public int doStartTag() throws JspException {
        validateAttributes();

        Object alternationBean   = getBean(getAltId(), null, getScope());

        if(alternationBean != null && !(alternationBean instanceof IAlternation)){
            throw new JspException("Bean found stored by altId key <" + getAltId() + "> is not an " +
                    "alternation definition (IAlternation)");
        }

        IAlternation alternation = null;

        if(alternationBean != null){
            alternation = (IAlternation) alternationBean;
        } else{
            alternation = new Alternation();

            if(getAltValues() != null){
                String[] listItems = splitAltValues();
                alternation.setListItems(listItems);
            }

            if(getFrom() != null && getTo() != null){
                alternation.setFrom(Integer.parseInt(getFrom()));
                alternation.setTo(Integer.parseInt(getTo()));
            }

            if(getFrom() != null){
                alternation.setIndex(Integer.parseInt(getFrom()));
            }

            if(getStartAt() != null){
                alternation.setIndex(Integer.parseInt(getStartAt()));
            }
        }

        setBean(getAltId(), getScope(), "request", alternation);

        return SKIP_BODY;
    }

    private void validateAttributes() throws JspException {
        if(getAltId() == null){
            throw new JspException("You MUST specify a name in the 'altId' attribute. This is the name of " +
                    "the request attribute the alternation will be stored under throughout the rest of this " +
                    "request.");
        }

        if(getFrom() != null && getTo() == null){
            throw new JspException("If you specify 'from' you must also specify 'to'.");
        }

        if(getFrom() == null && getTo() != null){
            throw new JspException("If you specify 'to' you must also specify 'from'.");
        }

        if(getFrom() != null && getAltValues() != null){
            throw new JspException("You cannot specify both 'from' and 'to', and an 'altValues' list " +
                    "to alternate between. Use either 'from' and 'to', or the 'altValues' attribute.");
        }

        if(getStartAt() != null){
            try{
                int startAt = Integer.parseInt(getStartAt());
            } catch (NumberFormatException e){
                throw new JspException("Attribute 'startAt' must be an integer.");
            }
        }
    }

    private String[] splitAltValues() throws JspException {
        StringTokenizer tokenizer = new StringTokenizer(getAltValues(), ", ");
        String[] listItems = new String[tokenizer.countTokens()];
        int i = 0;
        while(tokenizer.hasMoreTokens()){
            listItems[i] = tokenizer.nextToken();
            i++;
        }
        if(listItems.length == 0){
            throw new JspException("No values specified in the 'altValues' attribute. " +
                    "Separate values with comma and/or spaces. Must be at least one " +
                    "value, or leave out listItems attribute completely");
        }

        return listItems;
    }


}
