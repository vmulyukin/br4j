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

import com.jenkov.prizetags.base.NamePropertyTag;
import com.jenkov.prizetags.tree.itf.ITree;
import com.jenkov.prizetags.tree.itf.ITreeIteratorElement;

import javax.servlet.jsp.JspException;
import javax.servlet.http.HttpServletRequest;
import java.util.Iterator;

/**
 * Todo implement a setScope() / getScope() so tree models can be stored in
 *      application context, page context and request attribute as well as
 *      in the session.
 */
public class TreeTag extends NamePropertyTag{

    protected String   tree             = null;
    protected String   node             = null;
    protected String   level            = null;
    protected Iterator treeIterator     = null;
    protected String   paramPrefix      = null;
    protected String   expandParam      = null;
    protected String   collapseParam    = null;
    protected String   expandAllParam   = null;
    protected String   collapseAllParam = null;
    protected String   selectParam      = null;
    protected String   selectOnlyParam  = null;
    protected String   unselectParam    = null;
    protected String   includeRootNode  = null;

    protected String   scope            = null;

//    protected String expandAncestorsParam          = "expandAncestorsParam";
//    protected String expandAncestorsAndSelfParam   = "expandAncestorsAndSelfParam";
//    protected String expandDescendantsParam        = "expandDescendantsParam";
//    protected String expandDescendantsAndSelfParam = "expandDescendantsAndSelfParam";
//    protected String expandParentParam             = "expandParentParam";
//    protected String expandParentAndSelfParam      = "expandParentAndSelfParam";
//    protected String expandChildrenParam           = "expandChildrenParam";
//    protected String expandChildrenAndSelfParam    = "expandChildrenAndSelfParam";
//
//    protected String collapseAncestors          = "collapseAncestors";
//    protected String collapseAncestorsAndSelf   = "collapseAncestorsAndSelf";
//    protected String collapseDescendants        = "collapseDescendants";
//    protected String collapseDescendantsAndSelf = "collapseDescendantsAndSelf";
//    protected String collapseParent             = "collapseParent";
//    protected String collapseParentAndSelf      = "collapseParentAndSelf";
//    protected String collapseChildren             = "collapseChildren";
//    protected String collapseChildrenAndSelf      = "collapseChildrenAndSelf";
//
//    protected String selectAncestors          = "selectAncestors";
//    protected String selectAncestorsAndSelf   = "selectAncestorsAndSelf";
//    protected String selectDescendants        = "selectDescendants";
//    protected String selectDescendantsAndSelf = "selectDescendantsAndSelf";
//    protected String selectParent             = "selectParent";
//    protected String selectParentAndSelf      = "selectParentAndSelf";
//    protected String selectChildren             = "selectChildren";
//    protected String selectChildrenAndSelf      = "selectChildrenAndSelf";
//
//    protected String unSelectAncestors          = "unSelectAncestors";
//    protected String unSelectAncestorsAndSelf   = "unSelectAncestorsAndSelf";
//    protected String unSelectDescendants        = "unSelectDescendants";
//    protected String unSelectDescendantsAndSelf = "unSelectDescendantsAndSelf";
//    protected String unSelectParent             = "unSelectParent";
//    protected String unSelectParentAndSelf      = "unSelectParentAndSelf";
//    protected String unSelectChildren             = "unSelectChildren";
//    protected String unSelectChildrenAndSelf      = "unSelectChildrenAndSelf";


    public String getParamPrefix() {
        if(this.paramPrefix == null) {
            return "";
        }
        return paramPrefix;
    }

    public void setParamPrefix(String paramPrefix) {
        this.paramPrefix = paramPrefix;
    }

    public String getSelectParam() {
        if(this.selectParam == null) return "select";
        return selectParam;
    }

    public void setSelectParam(String selectParam) {
        this.selectParam = selectParam;
    }

    public String getSelectOnlyParam() {
        if(this.selectOnlyParam == null){
            return "selectOnly";
        }
        return selectOnlyParam;
    }

    public void setSelectOnlyParam(String selectOnlyParam) {
        this.selectOnlyParam = selectOnlyParam;
    }

    public String getUnselectParam() {
        if(this.unselectParam == null) return "unSelect";
        return unselectParam;
    }

    public void setUnselectParam(String unselectParam) {
        this.unselectParam = unselectParam;
    }

    public String getTree(){
        return this.tree;
    }

    public void setTree(String tree){
        this.tree = tree;
    }

    public String getNode(){
        return this.node;
    }

    public void setNode(String node){
        this.node = node;
    }

    public String getExpandParam(){
        if(this.expandParam == null) return "expand";
        return this.expandParam;
    }

    public void setExpandParam(String expandParam){
        this.expandParam = expandParam;
    }

    public String getCollapseParam(){
        if(this.collapseParam == null) return "collapse";
        return this.collapseParam;
    }

    public void setCollapseParam(String collapseParam){
        this.collapseParam = collapseParam;
    }

    public String getExpandAllParam() {
        if(expandAllParam == null) return "all";
        return expandAllParam;
    }

    public void setExpandAllParam(String expandAllParam) {
        this.expandAllParam = expandAllParam;
    }

    public String getCollapseAllParam() {
        if(collapseAllParam == null) return "all";
        return collapseAllParam;
    }

    public void setCollapseAllParam(String collapseAllParam) {
        this.collapseAllParam = collapseAllParam;
    }

    public String getIncludeRootNode() {
        if(includeRootNode == null) return "true";
        return includeRootNode;
    }

    public void setIncludeRootNode(String includeRootNode) {
        this.includeRootNode = includeRootNode;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }


    protected void validateAttributes() throws JspException{
        if(getTree() == null) throw new JspException("attribute tree must not be null!");
        if(getNode() == null) throw new JspException("attribute node must not be null!");
    }

    protected boolean isTreeAvailable() throws JspException{
        if(findTree() == null){
            throw new JspException("There was no tree model found by key " + getTree() +
            ". Scope was " + scope + ". You must supply a valid tree model for the tree tags to work.");
            //return false;
        }
        return true;
    }


    protected ITree findTree() throws JspException {
        return (ITree) getBean(this.tree,  null, scope);
    }



    public int doStartTag() throws JspException{
        validateAttributes();

        if(!isTreeAvailable()){
            return SKIP_BODY;
        }

        ITree tree = findTree();

        if(!isTreeAlreadyUpdated()){
            TreeUpdater updater = new TreeUpdater((HttpServletRequest) pageContext.getRequest(), tree, getParamPrefix());
            updater.setExpandParam(getExpandParam());
            updater.setExpandAllParam(getExpandParam());
            updater.setCollapseParam(getCollapseParam());
            updater.setCollapseAllParam(getCollapseAllParam());
            updater.setSelectParam(getSelectParam());
            updater.setSelectOnlyParam(getSelectOnlyParam());
            updater.setUnSelectParam(getUnselectParam());
            updater.update();
        }

        this.treeIterator = tree.iterator(getIncludeRootNode().equals("true"));
        if(this.treeIterator.hasNext()){
                ITreeIteratorElement element = (ITreeIteratorElement) this.treeIterator.next();
                if(tree.getFilter()!= null){
                    while(!tree.getFilter().accept(tree, element) && this.treeIterator.hasNext()){
                        element = (ITreeIteratorElement) this.treeIterator.next();
                    }
                }
                if(tree.getFilter() == null || tree.getFilter().accept(tree, element)){
                    pageContext.getRequest().setAttribute(getNode(), element);
                    return EVAL_BODY_INCLUDE;
                }
        }
        return SKIP_BODY;
    }

    /**
     * If a TreeUpdater is attached to the request attributes it is a signal that this tree
     * has already been updated earlier during the processing of this request.
     * @return True if the tree has already been updated. False if not.
     */
    private boolean isTreeAlreadyUpdated() {
        if(pageContext.getRequest().getAttribute(getParamPrefix() + ".TreeUpdater") instanceof TreeUpdater){
            return true;
        }
        return false;
    }

    public int doAfterBody() throws JspException{
        ITree tree = findTree();
        if(this.treeIterator.hasNext()){
             ITreeIteratorElement element = (ITreeIteratorElement) this.treeIterator.next();
             if(tree.getFilter()!= null){
                 while(!tree.getFilter().accept(tree, element) && this.treeIterator.hasNext()){
                     element = (ITreeIteratorElement) this.treeIterator.next();
                 }
            }
            if(tree.getFilter() == null || tree.getFilter().accept(tree, element)){
                pageContext.getRequest().setAttribute(getNode(), element);
                return EVAL_BODY_AGAIN;
            }
        }
        return SKIP_BODY;
    }

}
