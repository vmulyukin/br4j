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



/**
 * @version $revision$
 * @author Jakob Jenkov
 */
package com.jenkov.prizetags.tree.impl;

import com.jenkov.prizetags.tree.itf.ITreeIteratorElement;
import com.jenkov.prizetags.base.BaseTag;

import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.jsp.JspException;
import java.io.IOException;

public class NodeBaseTag extends BaseTag {

    protected String node = null;

     public String getNode(){
         return this.node;
     }

     public void setNode(String node){
         this.node = node;
     }

     protected void validateAttributes() throws JspException{
         if(getNode() == null)
             throw new JspException("Attribute node must not be null");
     }

     protected ITreeIteratorElement getElement() throws JspException{
         validateAttributes();
         ITreeIteratorElement element =
                 (ITreeIteratorElement) pageContext.getRequest().getAttribute(getNode());
         if(element == null){
             throw new JspException("Node retrieved from request was null");
         }
         return element;
     }

}
