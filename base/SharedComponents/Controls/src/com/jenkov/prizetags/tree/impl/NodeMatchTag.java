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
import com.jenkov.prizetags.tree.itf.INodeMatcher;
import com.jenkov.prizetags.tree.itf.ITreeNode;

import javax.servlet.jsp.JspException;

public class NodeMatchTag extends NodeBaseTag{

    protected String type        = null;
    protected String name        = null;
    protected String id          = null;
    protected String expanded    = null;
    protected String selected    = null;
    protected String hasChildren = null;
    protected String isFirstChild= null;
    protected String isLastChild = null;
    protected String matcherName  = null;
    protected String matcherClass = null;
    protected String isRoot = null;
    private String isAfterRoot = null;




    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType(){
        return this.type;
    }

    public void setType(String type){
        this.type = type;
    }

    public String getExpanded(){
        return this.expanded;
    }

    public void setExpanded(String expanded){
        this.expanded = expanded;
    }

    public String getSelected() {
          return selected;
    }

    public void setSelected(String selected) {
        this.selected = selected;
    }

    public String getHasChildren() {
        return hasChildren;
    }

    public void setHasChildren(String hasChildren) {
        this.hasChildren = hasChildren;
    }

    public String getIsFirstChild(){
        return this.isFirstChild;
    }

    public void setIsFirstChild(String isFirstChild){
        this.isFirstChild = isFirstChild;
    }

    public String getIsLastChild(){
        return this.isLastChild;
    }

    public void setIsLastChild(String isLastChild){
        this.isLastChild = isLastChild;
    }

    public boolean attributeNotSet(String attributeValue){
        return attributeValue == null;
    }
    public boolean valueNotSet(String attributeValue){
        return attributeValue == null;
    }

    public String getMatcherName() {
        return matcherName;
    }

    public void setMatcherName(String matcherName) {
        this.matcherName = matcherName;
    }

    public String getMatcherClass() {
        return matcherClass;
    }

    public void setMatcherClass(String matcherClass) {
        this.matcherClass = matcherClass;
    }

    protected boolean matchesWithWildCard(String attribute, String value){
        int index = attribute.indexOf("*");
        if(index == -1) return false;
        if(attribute.equals("*")) return true;

        if(attribute.startsWith("*")){
            if(value.endsWith(attribute.substring(1, attribute.length()))) return true;
        }
        if(attribute.endsWith("*")){
            if(value.startsWith(attribute.substring(0,attribute.length() -1))) return true;
        }
        String start = attribute.substring(0,index);
        String end   = attribute.substring(index + 1, attribute.length());
        if(value.startsWith(start) && value.endsWith(end)) return true;

        return false;
    }

    protected boolean matches(String attribute, String value){
        if(attributeNotSet(attribute)) return true;
        if(valueNotSet(value))         return false;
        if(attribute.equals(value))    return true;
        if(matchesWithWildCard(attribute, value)) return true;
        return false;
    }

    protected boolean matchesBoolean(String attribute, boolean booleanValue) throws JspException{
        if(attribute == null) return true;
        if(!attribute.equals("true") && !attribute.equals("false")){
            throw new JspException("boolean values must be either true or false (lower case only)");
        }
        if(attribute.equals("true")  && booleanValue == true)   return true;
        if(attribute.equals("false") && booleanValue == false) return true;

        return false;
    }

    public int doStartTag() throws JspException{
        ITreeIteratorElement element = getElement();
        if(element == null) throw new JspException("null element");
        if(element.getNode() == null) throw new JspException("null node");
        //if(element.getNode().getType() == null) throw new JspException("null type");
        if(this.matcherClass != null && this.matcherName != null){
            throw new JspException("You cannot provide both a matcherName and a matcherClass attribute. Only one" +
                    " at a time is allowed.");
        }

        if(!matches(getType(), element.getNode().getType()))    return SKIP_BODY;
        if(!matches(getId()  , element.getNode().getId()))      return SKIP_BODY;
        if(!matches(getName(), element.getNode().getName()))    return SKIP_BODY;

        if(!matchesBoolean(getExpanded()    , element.isExpanded()) )  return SKIP_BODY;
        if(!matchesBoolean(getSelected()    , element.isSelected()) )  return SKIP_BODY;
        if(!matchesBoolean(getHasChildren() , element.getNode().hasChildren()) ) return SKIP_BODY;
        if(!matchesBoolean(getIsFirstChild(), element.isFirstChild())){ return SKIP_BODY;}
        if(!matchesBoolean(getIsLastChild() , element.isLastChild())) { return SKIP_BODY;}

        if(!matchesBoolean(this.getIsAfterRoot(), this.getChildLevel(element.getNode()) > 1)){
             return SKIP_BODY;
        }
        if(!matchesBoolean(this.getIsRoot(), this.getChildLevel(element.getNode()) == 0)){
             return SKIP_BODY;
        }

        INodeMatcher matcher = null;
        if(this.matcherClass != null){
            matcher = instantiateMatcher();
            if(!matcher.matches(element)){
                return SKIP_BODY;
            }
        }
        if(this.matcherName != null){
            matcher = (INodeMatcher) getBean(this.matcherName, null, null);
            if(matcher == null){
                throw new JspException("No INodeMatcher found (was null) at matcherName " + this.matcherName);
            }
            if(!matcher.matches(element)){
                return SKIP_BODY;
            }
        }
        return EVAL_BODY_INCLUDE;
    }

    private INodeMatcher instantiateMatcher() throws JspException {
        try {
            return (INodeMatcher) Class.forName(this.matcherClass).newInstance();
        } catch (InstantiationException e) {
            throw new JspException("Error instantiating matcher of class: " + this.matcherClass, e);
        } catch (IllegalAccessException e) {
            throw new JspException("Error instantiating matcher of class: " + this.matcherClass, e);
        } catch (ClassNotFoundException e) {
            throw new JspException("Error instantiating matcher of class: " + this.matcherClass, e);
        }
    }

    public String getIsRoot() {
        return isRoot;
    }

    public void setIsRoot(String root) {
        isRoot = root;
    }

    public String getIsAfterRoot() {
        return isAfterRoot;
    }

    public void setIsAfterRoot(String afterRoot) {
        isAfterRoot = afterRoot;
    }
    
    public long getChildLevel(ITreeNode node){
    	long res = 0;
    	ITreeNode  parent = node.getParent(); 

    	while(parent != null){
    		++ res;
    		parent = parent.getParent();
    	}
    	return res;    	
    }
}
