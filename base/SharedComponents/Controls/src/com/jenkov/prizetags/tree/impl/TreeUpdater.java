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

import com.jenkov.prizetags.tree.itf.ITree;
import com.jenkov.prizetags.tree.itf.ITreeNode;

import javax.servlet.http.HttpServletRequest;
import java.util.Iterator;

/**
 * The TreeUpdater updates an ITree instance based on the request parameters in
 * the current request. If your tree parameters are prefixed remember to set the
 * parameter prefix on the updater instance.
 *
 * <br/><br/>
 * If some of your tree request parameters
 * does not have standard names
 * remember to change the name of the request parameter by calling the appropriate
 * setExpand / setCollapse / setSelect / setUnSelect etc. methods with the name.
 * For instance if you use "expandNode" instead of "expand", call setExpand("expandNode")
 * on the TreeUpdater before calling update(). Do not include the tree parameter
 * prefix in this name.
 *
 *
 *
 * @author Jakob Jenkov - Copyright 2005 Jenkov Development
 */
public class TreeUpdater {

    protected HttpServletRequest request = null;
    protected ITree              tree    = null;

    protected String paramPrefix = "";

    protected String updateParam                     = "update";

    protected String expandParam                     = "expand";
    protected String expandAllParam                  = "all";
    protected String expandAncestorsParam            = "expandAncestors";
    protected String expandAncestorsAndSelfParam     = "expandAncestorsAndSelf";
    protected String expandDescendantsParam          = "expandDescendants";
    protected String expandDescendantsAndSelfParam   = "expandDescendantsAndSelf";
    protected String expandParentParam               = "expandParent";
    protected String expandParentAndSelfParam        = "expandParentAndSelf";
    protected String expandChildrenParam             = "expandChildren";
    protected String expandChildrenAndSelfParam      = "expandChildrenAndSelf";

    protected String collapseParam                   = "collapse";
    protected String collapseAllParam                = "all";
    protected String collapseAncestorsParam          = "collapseAncestors";
    protected String collapseAncestorsAndSelfParam   = "collapseAncestorsAndSelf";
    protected String collapseDescendantsParam        = "collapseDescendants";
    protected String collapseDescendantsAndSelfParam = "collapseDescendantsAndSelf";
    protected String collapseParentParam             = "collapseParent";
    protected String collapseParentAndSelfParam      = "collapseParentAndSelf";
    protected String collapseChildrenParam           = "collapseChildren";
    protected String collapseChildrenAndSelfParam    = "collapseChildrenAndSelf";

    protected String selectParam                     = "select";
    protected String selectOnlyParam                 = "selectOnly";
    protected String selectAncestorsParam            = "selectAncestors";
    protected String selectAncestorsAndSelfParam     = "selectAncestorsAndSelf";
    protected String selectDescendantsParam          = "selectDescendants";
    protected String selectDescendantsAndSelfParam   = "selectDescendantsAndSelf";
    protected String selectParentParam               = "selectParent";
    protected String selectParentAndSelfParam        = "selectParentAndSelf";
    protected String selectChildrenParam             = "selectChildren";
    protected String selectChildrenAndSelfParam      = "selectChildrenAndSelf";

    protected String unSelectParam                   = "unSelect";
    protected String unSelectAncestorsParam          = "unSelectAncestors";
    protected String unSelectAncestorsAndSelfParam   = "unSelectAncestorsAndSelf";
    protected String unSelectDescendantsParam        = "unSelectDescendants";
    protected String unSelectDescendantsAndSelfParam = "unSelectDescendantsAndSelf";
    protected String unSelectParentParam             = "unSelectParent";
    protected String unSelectParentAndSelfParam      = "unSelectParentAndSelf";
    protected String unSelectChildrenParam           = "unSelectChildren";
    protected String unSelectChildrenAndSelfParam    = "unSelectChildrenAndSelf";

    public TreeUpdater(HttpServletRequest request, ITree tree) {
        this.request = request;
        this.tree    = tree;
    }

    public TreeUpdater(HttpServletRequest request, ITree tree, String paramPrefix) {
        this.request = request;
        this.tree = tree;
        this.paramPrefix = paramPrefix;
    }

    /**
     * Updates the expand, collapse, select and unselect status of the tree depending on the
     * request parameters.
     *
     * <br/><br/>
     * The updater attaches itself to the request as a request attribute
     * so the tree tag knows not to repeat the update in case this update was
     * executed in a Struts action, Spring controller, Servlet or other (model 2) component
     * that executes before the JSP with the tree tag does. The key used to attach the
     * updater to the request attributes is the tree param prefix. That
     * way if you have several trees on a page their updaters will not conflict. Each
     * tree will have it's own tree param prefix anyways.
     */
    public void update(){
        selectUnselectNodes();
        unSelectNodes();
        expandNodes();
        collapseNodes();
        updateNodes();
        request.setAttribute(getParamPrefix() + ".TreeUpdater", this);
    }

    protected void updateNodes() {
        if("all".equals(request.getParameter(updateParam))){
           Iterator expandedNodesIterator = tree.getExpandedNodes().iterator();
           while(expandedNodesIterator.hasNext()){
               ITreeNode expandedNode = (ITreeNode) expandedNodesIterator.next();
               tree.getTreeDao().readChildrenAndGrandchildren(tree, expandedNode);
           }
        }
    }

    protected void expandNodes() {
        String[] expandId    = request.getParameterValues(getParamPrefix() + expandParam);
        if(expandId != null){
            for(int i=0; i< expandId.length; i++){
                if   ((getExpandAllParam()).equals(expandId[i]))  tree.expandAll();
                else                                               tree.expand(expandId[i]);
            }
        }

        String expandAncestorIds[] = request.getParameterValues(getParamPrefix() + expandAncestorsParam);
        if(expandAncestorIds!= null){
            for(int i=0; i< expandAncestorIds.length; i++){
                tree.expandAncestors(expandAncestorIds[i]);
            }
        }

        String expandAncestorAndSelfIds[] = request.getParameterValues(getParamPrefix() + expandAncestorsAndSelfParam);
        if(expandAncestorAndSelfIds != null){
            for(int i=0; i< expandAncestorAndSelfIds.length; i++){
                tree.expandAncestorsAndSelf(expandAncestorAndSelfIds[i]);
            }
        }

        String expandDescendantsIds[] = request.getParameterValues(getParamPrefix() + expandDescendantsParam);
        if(expandDescendantsIds != null){
            for(int i=0; i< expandDescendantsIds.length; i++){
                tree.expandDescendants(expandDescendantsIds[i]);
            }
        }

        String expandDescendantsAndSelfIds[] = request.getParameterValues(getParamPrefix() + expandDescendantsAndSelfParam);
        if(expandDescendantsAndSelfIds != null){
            for(int i=0; i< expandDescendantsAndSelfIds.length; i++){
                tree.expandDescendantsAndSelf(expandDescendantsAndSelfIds[i]);
            }
        }

        String expandParentIds[] = request.getParameterValues(getParamPrefix() + expandParentParam);
        if(expandParentIds != null){
            for(int i=0; i< expandParentIds.length; i++){
                tree.expandParent(expandParentIds[i]);
            }
        }

        String expandParentAndSelfIds[] = request.getParameterValues(getParamPrefix() + expandParentAndSelfParam);
        if(expandParentAndSelfIds != null){
            for(int i=0; i< expandParentAndSelfIds.length; i++){
                tree.expandParentAndSelf(expandParentAndSelfIds[i]);
            }
        }

        String expandChildrenIds[] = request.getParameterValues(getParamPrefix() + expandChildrenParam);
        if(expandChildrenIds != null){
            for(int i=0; i< expandChildrenIds.length; i++){
                tree.expandChildren(expandChildrenIds[i]);
            }
        }

        String expandChildrenAndSelfIds[] = request.getParameterValues(getParamPrefix() + expandChildrenAndSelfParam);
        if(expandChildrenAndSelfIds != null){
            for(int i=0; i< expandChildrenAndSelfIds.length; i++){
                tree.expandChildrenAndSelf(expandChildrenAndSelfIds[i]);
            }
        }
    }

    private void collapseNodes() {
        String collapseId[]  = request.getParameterValues(getParamPrefix() + collapseParam);
        if(collapseId != null){
            for(int i=0; i<collapseId.length; i++){
                if((getCollapseAllParam()).equals(collapseId[i])) tree.collapseAll();
                else                                            tree.collapse(collapseId[i]);
            }
        }

        String collapseAncestorIds[] = request.getParameterValues(getParamPrefix() + collapseAncestorsParam);
        if(collapseAncestorIds!= null){
            for(int i=0; i< collapseAncestorIds.length; i++){
                tree.collapseAncestors(collapseAncestorIds[i]);
            }
        }

        String collapseAncestorAndSelfIds[] = request.getParameterValues(getParamPrefix() + collapseAncestorsAndSelfParam);
        if(collapseAncestorAndSelfIds != null){
            for(int i=0; i< collapseAncestorAndSelfIds.length; i++){
                tree.collapseAncestorsAndSelf(collapseAncestorAndSelfIds[i]);
            }
        }

        String collapseDescendantsIds[] = request.getParameterValues(getParamPrefix() + collapseDescendantsParam);
        if(collapseDescendantsIds != null){
            for(int i=0; i< collapseDescendantsIds.length; i++){
                tree.collapseDescendants(collapseDescendantsIds[i]);
            }
        }

        String collapseDescendantsAndSelfIds[] = request.getParameterValues(getParamPrefix() + collapseDescendantsAndSelfParam);
        if(collapseDescendantsAndSelfIds != null){
            for(int i=0; i< collapseDescendantsAndSelfIds.length; i++){
                tree.collapseDescendantsAndSelf(collapseDescendantsAndSelfIds[i]);
            }
        }

        String collapseParentIds[] = request.getParameterValues(getParamPrefix() + collapseParentParam);
        if(collapseParentIds != null){
            for(int i=0; i< collapseParentIds.length; i++){
                tree.collapseParent(collapseParentIds[i]);
            }
        }

        String collapseParentAndSelfIds[] = request.getParameterValues(getParamPrefix() + collapseParentAndSelfParam);
        if(collapseParentAndSelfIds != null){
            for(int i=0; i< collapseParentAndSelfIds.length; i++){
                tree.collapseParentAndSelf(collapseParentAndSelfIds[i]);
            }
        }

        String collapseChildrenIds[] = request.getParameterValues(getParamPrefix() + collapseChildrenParam);
        if(collapseChildrenIds != null){
            for(int i=0; i< collapseChildrenIds.length; i++){
                tree.collapseChildren(collapseChildrenIds[i]);
            }
        }

        String collapseChildrenAndSelfIds[] = request.getParameterValues(getParamPrefix() + collapseChildrenAndSelfParam);
        if(collapseChildrenAndSelfIds != null){
            for(int i=0; i< collapseChildrenAndSelfIds.length; i++){
                tree.collapseChildrenAndSelf(collapseChildrenAndSelfIds[i]);
            }
        }
    }

    protected void selectUnselectNodes() {
        String[] selectIds     = request.getParameterValues(getParamPrefix() + selectParam);
        String[] selectOnlyIds = request.getParameterValues(getParamPrefix() + selectOnlyParam  );

        if(selectOnlyIds != null){
            tree.selectOnly(selectOnlyIds);
        }
        if(selectIds != null){
           tree.select(selectIds);
        }

        String selectAncestorsIds[] = request.getParameterValues(getParamPrefix() + selectAncestorsParam);
        if(selectAncestorsIds != null){
            for(int i=0; i< selectAncestorsIds.length; i++){
                tree.selectAncestors(selectAncestorsIds[i]);
            }
        }

        String selectAncestorsAndSelfIds[] = request.getParameterValues(getParamPrefix() + selectAncestorsAndSelfParam);
        if(selectAncestorsAndSelfIds != null){
            for(int i=0; i< selectAncestorsAndSelfIds.length; i++){
                tree.selectAncestorsAndSelf(selectAncestorsAndSelfIds[i]);
            }
        }

        String selectDescendantsIds[] = request.getParameterValues(getParamPrefix() + selectDescendantsParam);
        if(selectDescendantsIds != null){
            for(int i=0; i< selectDescendantsIds.length; i++){
                tree.selectDescendants(selectDescendantsIds[i]);
            }
        }

        String selectDescendantsAndSelfIds[] = request.getParameterValues(getParamPrefix() + selectDescendantsAndSelfParam);
        if(selectDescendantsAndSelfIds != null){
            for(int i=0; i< selectDescendantsAndSelfIds.length; i++){
                tree.selectDescendantsAndSelf(selectDescendantsAndSelfIds[i]);
            }
        }

        String selectParentIds[] = request.getParameterValues(getParamPrefix() + selectParentParam);
        if(selectParentIds != null){
            for(int i=0; i< selectParentIds.length; i++){
                tree.selectParent(selectParentIds[i]);
            }
        }

        String selectParentAndSelfIds[] = request.getParameterValues(getParamPrefix() + selectParentAndSelfParam);
        if(selectParentAndSelfIds != null){
            for(int i=0; i< selectParentAndSelfIds.length; i++){
                tree.selectParentAndSelf(selectParentAndSelfIds[i]);
            }
        }

        String selectChildrenIds[] = request.getParameterValues(getParamPrefix() + selectChildrenParam);
        if(selectChildrenIds != null){
            for(int i=0; i< selectChildrenIds.length; i++){
                tree.selectChildren(selectChildrenIds[i]);
            }
        }

        String selectChildrenAndSelfIds[] = request.getParameterValues(getParamPrefix() + selectChildrenAndSelfParam);
        if(selectChildrenAndSelfIds != null){
            for(int i=0; i< selectChildrenAndSelfIds.length; i++){
                tree.selectChildrenAndSelf(selectChildrenAndSelfIds[i]);
            }
        }
    }

    private void unSelectNodes() {
        String   unselectId    = request.getParameter      (getParamPrefix() + unSelectParam);

        if(unselectId != null){
           tree.unSelect(unselectId);
        }

        String unSelectAncestorsIds[] = request.getParameterValues(getParamPrefix() + unSelectAncestorsParam);
        if(unSelectAncestorsIds != null){
            for(int i=0; i< unSelectAncestorsIds.length; i++){
                tree.unSelectAncestors(unSelectAncestorsIds[i]);
            }
        }

        String unSelectAncestorsAndSelfIds[] = request.getParameterValues(getParamPrefix() + unSelectAncestorsAndSelfParam);
        if(unSelectAncestorsAndSelfIds != null){
            for(int i=0; i< unSelectAncestorsAndSelfIds.length; i++){
                tree.unSelectAncestorsAndSelf(unSelectAncestorsAndSelfIds[i]);
            }
        }

        String unSelectDescendantsIds[] = request.getParameterValues(getParamPrefix() + unSelectDescendantsParam);
        if(unSelectDescendantsIds != null){
            for(int i=0; i< unSelectDescendantsIds.length; i++){
                tree.unSelectDescendants(unSelectDescendantsIds[i]);
            }
        }

        String unSelectDescendantsAndSelfIds[] = request.getParameterValues(getParamPrefix() + unSelectDescendantsAndSelfParam);
        if(unSelectDescendantsAndSelfIds != null){
            for(int i=0; i< unSelectDescendantsAndSelfIds.length; i++){
                tree.unSelectDescendantsAndSelf(unSelectDescendantsAndSelfIds[i]);
            }
        }

        String unSelectParentIds[] = request.getParameterValues(getParamPrefix() + unSelectParentParam);
        if(unSelectParentIds != null){
            for(int i=0; i< unSelectParentIds.length; i++){
                tree.unSelectParent(unSelectParentIds[i]);
            }
        }

        String unSelectParentAndSelfIds[] = request.getParameterValues(getParamPrefix() + unSelectParentAndSelfParam);
        if(unSelectParentAndSelfIds != null){
            for(int i=0; i< unSelectParentAndSelfIds.length; i++){
                tree.unSelectParentAndSelf(unSelectParentAndSelfIds[i]);
            }
        }

        String unSelectChildrenIds[] = request.getParameterValues(getParamPrefix() + unSelectChildrenParam);
        if(unSelectChildrenIds != null){
            for(int i=0; i< unSelectChildrenIds.length; i++){
                tree.unSelectChildren(unSelectChildrenIds[i]);
            }
        }

        String unSelectChildrenAndSelfIds[] = request.getParameterValues(getParamPrefix() + unSelectChildrenAndSelfParam);
        if(unSelectChildrenAndSelfIds != null){
            for(int i=0; i< unSelectChildrenAndSelfIds.length; i++){
                tree.unSelectChildrenAndSelf(unSelectChildrenAndSelfIds[i]);
            }
        }
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    public ITree getTree() {
        return tree;
    }

    public void setTree(ITree tree) {
        this.tree = tree;
    }

    public String getParamPrefix() {
        if( paramPrefix == null) return "";
        return paramPrefix;
    }

    public void setParamPrefix(String paramPrefix) {
        this.paramPrefix = paramPrefix;
    }

    public String getExpandParam() {
        return expandParam;
    }

    public void setExpandParam(String expandParam) {
        this.expandParam = expandParam;
    }

    public String getExpandAllParam() {
        return expandAllParam;
    }

    public void setExpandAllParam(String expandAllParam) {
        this.expandAllParam = expandAllParam;
    }

    public String getExpandAncestorsParam() {
        return expandAncestorsParam;
    }

    public void setExpandAncestorsParam(String expandAncestorsParam) {
        this.expandAncestorsParam = expandAncestorsParam;
    }

    public String getExpandAncestorsAndSelfParam() {
        return expandAncestorsAndSelfParam;
    }

    public void setExpandAncestorsAndSelfParam(String expandAncestorsAndSelfParam) {
        this.expandAncestorsAndSelfParam = expandAncestorsAndSelfParam;
    }

    public String getExpandDescendantsParam() {
        return expandDescendantsParam;
    }

    public void setExpandDescendantsParam(String expandDescendantsParam) {
        this.expandDescendantsParam = expandDescendantsParam;
    }

    public String getExpandDescendantsAndSelfParam() {
        return expandDescendantsAndSelfParam;
    }

    public void setExpandDescendantsAndSelfParam(String expandDescendantsAndSelfParam) {
        this.expandDescendantsAndSelfParam = expandDescendantsAndSelfParam;
    }

    public String getExpandParentParam() {
        return expandParentParam;
    }

    public void setExpandParentParam(String expandParentParam) {
        this.expandParentParam = expandParentParam;
    }

    public String getExpandParentAndSelfParam() {
        return expandParentAndSelfParam;
    }

    public void setExpandParentAndSelfParam(String expandParentAndSelfParam) {
        this.expandParentAndSelfParam = expandParentAndSelfParam;
    }

    public String getExpandChildrenParam() {
        return expandChildrenParam;
    }

    public void setExpandChildrenParam(String expandChildrenParam) {
        this.expandChildrenParam = expandChildrenParam;
    }

    public String getExpandChildrenAndSelfParam() {
        return expandChildrenAndSelfParam;
    }

    public void setExpandChildrenAndSelfParam(String expandChildrenAndSelfParam) {
        this.expandChildrenAndSelfParam = expandChildrenAndSelfParam;
    }

    public String getCollapseParam() {
        return collapseParam;
    }

    public void setCollapseParam(String collapseParam) {
        this.collapseParam = collapseParam;
    }

    public String getCollapseAllParam() {
        return collapseAllParam;
    }

    public void setCollapseAllParam(String collapseAllParam) {
        this.collapseAllParam = collapseAllParam;
    }

    public String getCollapseAncestorsParam() {
        return collapseAncestorsParam;
    }

    public void setCollapseAncestorsParam(String collapseAncestorsParam) {
        this.collapseAncestorsParam = collapseAncestorsParam;
    }

    public String getCollapseAncestorsAndSelfParam() {
        return collapseAncestorsAndSelfParam;
    }

    public void setCollapseAncestorsAndSelfParam(String collapseAncestorsAndSelfParam) {
        this.collapseAncestorsAndSelfParam = collapseAncestorsAndSelfParam;
    }

    public String getCollapseDescendantsParam() {
        return collapseDescendantsParam;
    }

    public void setCollapseDescendantsParam(String collapseDescendantsParam) {
        this.collapseDescendantsParam = collapseDescendantsParam;
    }

    public String getCollapseDescendantsAndSelfParam() {
        return collapseDescendantsAndSelfParam;
    }

    public void setCollapseDescendantsAndSelfParam(String collapseDescendantsAndSelfParam) {
        this.collapseDescendantsAndSelfParam = collapseDescendantsAndSelfParam;
    }

    public String getCollapseParentParam() {
        return collapseParentParam;
    }

    public void setCollapseParentParam(String collapseParentParam) {
        this.collapseParentParam = collapseParentParam;
    }

    public String getCollapseParentAndSelfParam() {
        return collapseParentAndSelfParam;
    }

    public void setCollapseParentAndSelfParam(String collapseParentAndSelfParam) {
        this.collapseParentAndSelfParam = collapseParentAndSelfParam;
    }

    public String getCollapseChildrenParam() {
        return collapseChildrenParam;
    }

    public void setCollapseChildrenParam(String collapseChildrenParam) {
        this.collapseChildrenParam = collapseChildrenParam;
    }

    public String getCollapseChildrenAndSelfParam() {
        return collapseChildrenAndSelfParam;
    }

    public void setCollapseChildrenAndSelfParam(String collapseChildrenAndSelfParam) {
        this.collapseChildrenAndSelfParam = collapseChildrenAndSelfParam;
    }

    public String getSelectParam() {
        return selectParam;
    }

    public void setSelectParam(String selectParam) {
        this.selectParam = selectParam;
    }

    public String getSelectOnlyParam() {
        return selectOnlyParam;
    }

    public void setSelectOnlyParam(String selectOnlyParam) {
        this.selectOnlyParam = selectOnlyParam;
    }

    public String getSelectAncestorsParam() {
        return selectAncestorsParam;
    }

    public void setSelectAncestorsParam(String selectAncestorsParam) {
        this.selectAncestorsParam = selectAncestorsParam;
    }

    public String getSelectAncestorsAndSelfParam() {
        return selectAncestorsAndSelfParam;
    }

    public void setSelectAncestorsAndSelfParam(String selectAncestorsAndSelfParam) {
        this.selectAncestorsAndSelfParam = selectAncestorsAndSelfParam;
    }

    public String getSelectDescendantsParam() {
        return selectDescendantsParam;
    }

    public void setSelectDescendantsParam(String selectDescendantsParam) {
        this.selectDescendantsParam = selectDescendantsParam;
    }

    public String getSelectDescendantsAndSelfParam() {
        return selectDescendantsAndSelfParam;
    }

    public void setSelectDescendantsAndSelfParam(String selectDescendantsAndSelfParam) {
        this.selectDescendantsAndSelfParam = selectDescendantsAndSelfParam;
    }

    public String getSelectParentParam() {
        return selectParentParam;
    }

    public void setSelectParentParam(String selectParentParam) {
        this.selectParentParam = selectParentParam;
    }

    public String getSelectParentAndSelfParam() {
        return selectParentAndSelfParam;
    }

    public void setSelectParentAndSelfParam(String selectParentAndSelfParam) {
        this.selectParentAndSelfParam = selectParentAndSelfParam;
    }

    public String getSelectChildrenParam() {
        return selectChildrenParam;
    }

    public void setSelectChildrenParam(String selectChildrenParam) {
        this.selectChildrenParam = selectChildrenParam;
    }

    public String getSelectChildrenAndSelfParam() {
        return selectChildrenAndSelfParam;
    }

    public void setSelectChildrenAndSelfParam(String selectChildrenAndSelfParam) {
        this.selectChildrenAndSelfParam = selectChildrenAndSelfParam;
    }

    public String getUnSelectParam() {
        return unSelectParam;
    }

    public void setUnSelectParam(String unSelectParam) {
        this.unSelectParam = unSelectParam;
    }

    public String getUnSelectAncestorsParam() {
        return unSelectAncestorsParam;
    }

    public void setUnSelectAncestorsParam(String unSelectAncestorsParam) {
        this.unSelectAncestorsParam = unSelectAncestorsParam;
    }

    public String getUnSelectAncestorsAndSelfParam() {
        return unSelectAncestorsAndSelfParam;
    }

    public void setUnSelectAncestorsAndSelfParam(String unSelectAncestorsAndSelfParam) {
        this.unSelectAncestorsAndSelfParam = unSelectAncestorsAndSelfParam;
    }

    public String getUnSelectDescendantsParam() {
        return unSelectDescendantsParam;
    }

    public void setUnSelectDescendantsParam(String unSelectDescendantsParam) {
        this.unSelectDescendantsParam = unSelectDescendantsParam;
    }

    public String getUnSelectDescendantsAndSelfParam() {
        return unSelectDescendantsAndSelfParam;
    }

    public void setUnSelectDescendantsAndSelfParam(String unSelectDescendantsAndSelfParam) {
        this.unSelectDescendantsAndSelfParam = unSelectDescendantsAndSelfParam;
    }

    public String getUnSelectParentParam() {
        return unSelectParentParam;
    }

    public void setUnSelectParentParam(String unSelectParentParam) {
        this.unSelectParentParam = unSelectParentParam;
    }

    public String getUnSelectParentAndSelfParam() {
        return unSelectParentAndSelfParam;
    }

    public void setUnSelectParentAndSelfParam(String unSelectParentAndSelfParam) {
        this.unSelectParentAndSelfParam = unSelectParentAndSelfParam;
    }

    public String getUnSelectChildrenParam() {
        return unSelectChildrenParam;
    }

    public void setUnSelectChildrenParam(String unSelectChildrenParam) {
        this.unSelectChildrenParam = unSelectChildrenParam;
    }

    public String getUnSelectChildrenAndSelfParam() {
        return unSelectChildrenAndSelfParam;
    }

    public void setUnSelectChildrenAndSelfParam(String unSelectChildrenAndSelfParam) {
        this.unSelectChildrenAndSelfParam = unSelectChildrenAndSelfParam;
    }
}
