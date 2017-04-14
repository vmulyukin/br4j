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

import javax.servlet.jsp.JspException;
import java.util.StringTokenizer;

/**
 * @author Jakob Jenkov
 *         Copyright 2004 Jenkov Development
 */
public class AlternateMatchTag extends AlternateBaseTag{

    protected String altValues = null;

    public String getAltValues() {
        return altValues;
    }

    public void setAltValues(String altValues) {
        this.altValues = altValues;
    }

    public int doStartTag() throws JspException {
        IAlternation alternation = getAlternation();

        String[] matchValues = splitAltValues();

        if(alternation.getListItems() != null){
            for(int i=0; i<matchValues.length; i++){
                if(matchValues[i].equals(alternation.getListItem())){
                    return EVAL_BODY_INCLUDE;
                }
            }
        }
        else {
            for(int i=0; i<matchValues.length; i++){
                int indexToMatch = Integer.parseInt(matchValues[i]);
                if(alternation.getIndex() == indexToMatch){
                    return EVAL_BODY_INCLUDE;
                }
            }
        }

        return SKIP_BODY;
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
