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

import com.jenkov.prizetags.tree.itf.*;

import java.util.*;
import java.io.Serializable;            

public class Tree implements ITree, Serializable{
    protected ITreeNode   root                = null;

    protected boolean     singleSelectionMode = false;
    protected boolean     preserveSelectionOfInvisibleNodes = true;

    protected Set         expanded            = new TreeSet();
    protected Set         selected            = new TreeSet();
//    protected Set         preservedSelected   = new TreeSet();

    protected List        expandListeners     = new ArrayList();
    protected List        collapseListeners   = new ArrayList();
    protected List        selectListeners     = new ArrayList();
    protected List        unselectListeners   = new ArrayList();

    protected ITreeFilter filter              = new BaseTreeFilter();

    protected boolean     notifyOnChangeOnly  = true;
    protected ITreeDao    treeDao             = null;


    public Tree() {
    }

    /**
     * Creates a tree and sets the given tree node as the root.
     * @param root The root node of the tree.
     */
    public Tree(ITreeNode root) {
        this.root = root;
    }

    public ITreeDao getTreeDao() {
        return treeDao;
    }

    public void setTreeDao(ITreeDao treeDao) {
        this.treeDao = treeDao;
        if(this.root == null){
            this.treeDao.readRootAndChildren(this);
        }
    }

    public ITreeNode getRoot() {
        return this.root;
    }

    public void setRoot(ITreeNode node) {
        this.root = node;
    }

    public ITreeNode findNode(String treeNodeId) {
        return findNode(getRoot(), treeNodeId);
    }

    protected ITreeNode findNode(ITreeNode treeNode, String treeNodeId){
        if(treeNode.getId().equals(treeNodeId)){
            return treeNode;
        }

        Iterator children = treeNode.getChildren().iterator();
        while(children.hasNext()){
            ITreeNode child = (ITreeNode) children.next();
            ITreeNode match = findNode(child, treeNodeId);
            if( match != null){
                return match;
            }
        }
        return null;
    }

    public Set findNodes(Set treeNodeIds) {
        Set treeNodes = new HashSet();
        findNodes(getRoot(), treeNodeIds, treeNodes);
        return treeNodes;
    }

    protected void findNodes(ITreeNode treeNode, Set treeNodeIds, Set treeNodes){
        if(treeNodeIds.contains(treeNode.getId())){
            treeNodes.add(treeNode);
        }

        Iterator children = treeNode.getChildren().iterator();
        while(children.hasNext()){
            findNodes((ITreeNode) children.next(), treeNodeIds, treeNodes);
        }
    }

    public boolean isExpanded(String treeNodeId){
        return this.expanded.contains(treeNodeId);
    }

    // all expand methods ends by calling this method
    public void expand(String treeNodeId) {
        if(notifyOnChangeOnly && isExpanded(treeNodeId)) return;
        this.expanded.add(treeNodeId);
        if(this.treeDao != null){
            this.treeDao.readChildrenAndGrandchildren(this, findNode(treeNodeId));
        }
        notifyExpandListeners(treeNodeId);
    }

    public void expandAll() {
        if(this.root != null && this.root.hasChildren()){
            expand(this.root.getId());
            expandDescendants(this.root);
        }
    }

    public void expand(String[]   nodeIds) {
        for(int i=0; i < nodeIds.length; i++){
            expand(nodeIds[i]);
        }
    }

    public void expand(Collection nodeIds) {
        for(Iterator iterator = nodeIds.iterator(); iterator.hasNext(); ){
            expand((String) iterator.next());
        }
    }

    public void expandParent(String nodeId) {
        ITreeNode node = findNode(nodeId);
        if(node != null && !isRoot(node)){
            expand(node.getParent().getId());
        }
    }

    private boolean isRoot(ITreeNode node) {
        return node == this.root || node.getParent() == null;
    }

    public void expandParentAndSelf(String nodeId) {
        expand(nodeId);
        expandParent(nodeId);
    }

    public void expandChildren(String nodeId) {
        ITreeNode node = findNode(nodeId);
        if(node != null && node.getChildren().size() > 0){
            Iterator iterator = node.getChildren().iterator();
            while(iterator.hasNext()){
                ITreeNode child = (ITreeNode) iterator.next();
                expand(child.getId());
            }

        }
    }
    public void expandChildrenAndSelf(String nodeId){
        expand(nodeId);
        expandChildren(nodeId);
    }

    public void expandAncestors(String nodeId) {
        ITreeNode node = findNode(nodeId);
        if(node != null){
            while(!isRoot(node)){
                expand(node.getParent().getId());
                node = node.getParent();
            }
        }
    }

    public void expandAncestorsAndSelf(String nodeId) {
        expand(nodeId);
        expandAncestors(nodeId);
    }

    public void expandDescendants(String nodeId) {
        ITreeNode node = findNode(nodeId);
        expandDescendants(node);
    }

    public void expandDescendantsAndSelf(String nodeId) {
        expand(nodeId);
        expandDescendants(nodeId);
    }

    private void expandDescendants(ITreeNode node) {
        if(node == null) return;
        Iterator iterator = node.getChildren().iterator();
        while(iterator.hasNext()){
            ITreeNode child = (ITreeNode) iterator.next();
            if(child.hasChildren()){
                expand(child.getId());
                expandDescendants(child);
            }
        }
    }

    public void collapse(String treeNodeId) {
        if(notifyOnChangeOnly && !isExpanded(treeNodeId)) return;
        this.expanded.remove(treeNodeId);
        notifyCollapseListeners(treeNodeId);
    }

    public void collapseAll() {
        if(this.root != null){
            collapse(this.root.getId());
            collapseDescendants(this.root);
        }
    }

    public void collapse(String[] nodeIds) {
        for(int i=0; i<nodeIds.length; i++){
            collapse(nodeIds[i]);
        }
    }

    public void collapse(Collection nodeIds) {
        for(Iterator iterator= nodeIds.iterator(); iterator.hasNext();){
            collapse((String) iterator.next());
        }
    }

    public void collapseParent(String nodeId) {
        ITreeNode node = findNode(nodeId);
        if(node != null && !isRoot(node)){
            collapse(node.getParent().getId());
        }
    }

    public void collapseParentAndSelf(String nodeId) {
        collapse(nodeId);
        collapseParent(nodeId);
    }

    public void collapseChildren(String nodeId) {
        ITreeNode node = findNode(nodeId);
        if(node != null && node.getChildren().size() > 0){
            Iterator iterator = node.getChildren().iterator();
            while(iterator.hasNext()){
                ITreeNode child = (ITreeNode) iterator.next();
                collapse(child.getId());
            }

        }
    }

    public void collapseChildrenAndSelf(String nodeId){
        collapse(nodeId);
        collapseChildren(nodeId);
    }

    public void collapseAncestors(String nodeId) {
        ITreeNode node = findNode(nodeId);
        if(node != null){
            while(!isRoot(node)){
                collapse(node.getParent().getId());
                node = node.getParent();
            }
        }
    }

    public void collapseAncestorsAndSelf(String nodeId) {
        collapse(nodeId);
        collapseAncestors(nodeId);
    }

    public void collapseDescendants(String nodeId) {
        ITreeNode node = findNode(nodeId);
        collapseDescendants(node);
    }

    public void collapseDescendantsAndSelf(String nodeId) {
        collapse(nodeId);
        collapseDescendants(nodeId);
    }

    private void collapseDescendants(ITreeNode node) {
        if(node == null) return;
        Iterator iterator = node.getChildren().iterator();
        while(iterator.hasNext()){
            ITreeNode child = (ITreeNode) iterator.next();
            collapse(child.getId());
            collapseDescendants(child);
        }
    }

    public Set getExpandedNodes() {
        return findNodes(this.expanded);
    }

    public void addExpandListener(IExpandListener expandListener) {
        this.expandListeners.add(expandListener);
    }

    public void removeExpandListener(IExpandListener expandListener) {
        this.expandListeners.remove(expandListener);
    }

    public void addCollapseListener(ICollapseListener collapseListener) {
        this.collapseListeners.add(collapseListener);
    }

    public void removeCollapseListener(ICollapseListener collapseListener) {
        this.collapseListeners.remove(collapseListener);
    }

    public boolean isSelected(String treeNodeId) {
        return this.selected.contains(treeNodeId);
    }

    public void select(String treeNodeId) {
        if(notifyOnChangeOnly && isSelected(treeNodeId)) return;
        if(isSingleSelectionMode()){
            unSelectAll();
        }
        this.selected.add(treeNodeId);

        notifySelectListeners(treeNodeId);
    }

    public void selectAll() {
        select(root.getId());
        selectDescendants(root);
    }

    public void select(String[] treeNodeIds) {
        multipleSelectVsSelectionModeValidation(treeNodeIds);
        for (int i = 0; i < treeNodeIds.length; i++) {
            select(treeNodeIds[i]);
        }
    }

    public void select(Collection nodeIds) {
        for (Iterator iterator = nodeIds.iterator(); iterator.hasNext();) {
            select((String) iterator.next());
        }
    }

    public void selectOnly(String[] treeNodeIds){
        multipleSelectVsSelectionModeValidation(treeNodeIds);
        Set selectOnlySet = new HashSet();
        for (int i = 0; i < treeNodeIds.length; i++) {
            selectOnlySet.add(treeNodeIds[i]);
        }
        Set selected = copy(this.selected);
        Set unselect = copy(this.selected);

        //unselect all nodes that were selected but are not selected anymore.
        unselect.removeAll(selectOnlySet);

        //check if any of the nodes to unselect are invisible and if these should be preserved
        if(isPreserveSelectionOfInvisibleNodes()){
            for(Iterator iterator = unselect.iterator(); iterator.hasNext();){
                String nodeId = (String) iterator.next();
//                System.out.println("node id = " +  nodeId);
                //if visible then do remove the node.
                if(isInvisible(nodeId)){
//                    System.out.println("node is visible: " + nodeId);
                    iterator.remove();  //don't unselect this node because it is not visible.
                }
            }
        }
        unSelect(unselect);

        //select all nodes that were not selected before.
        selectOnlySet.removeAll(selected);
        select(selectOnlySet);
    }

    public boolean isInvisible(String nodeId) {
        ITreeNode node   = findNode(nodeId);
        if(node == null) return true;
        ITreeNode parent = node.getParent();
        while(parent != null){
            if(!isExpanded(parent.getId())) return true;
            parent = parent.getParent();
        }
        return false;
    }

    public void selectDescendants(String nodeId) {
        ITreeNode node = findNode(nodeId);
        selectDescendants(node);
    }

    public void selectDescendantsAndSelf(String nodeId) {
        select(nodeId);
        selectDescendants((nodeId));
    }

    public void selectParent(String nodeId) {
        ITreeNode node = findNode(nodeId);
        if(!isRoot(node)){
            select(node.getParent().getId());
        }
    }

    public void selectParentAndSelf(String nodeId) {
        select(nodeId);
        selectParent(nodeId);
    }

    public void selectAncestors(String nodeId) {
        ITreeNode node = findNode(nodeId);
        if(node == null) return;
        while(!isRoot(node)){
            select(node.getParent().getId());
            node = node.getParent();
        }
    }

    public void selectAncestorsAndSelf(String nodeId) {
        select(nodeId);
        selectAncestors(nodeId);
    }

    public void selectChildren(String nodeId) {
        ITreeNode node = findNode(nodeId);
        if(node != null && node.getChildren().size() > 0){
            Iterator iterator = node.getChildren().iterator();
            while(iterator.hasNext()){
                ITreeNode child = (ITreeNode) iterator.next();
                select(child.getId());
            }
        }
    }

    public void selectChildrenAndSelf(String nodeId){
        select(nodeId);
        selectChildren(nodeId);
    }


    private Set copy(Set selectedNodes) {
        Set copy = new HashSet();
        for (Iterator iterator = selectedNodes.iterator(); iterator.hasNext();) {
            copy.add(iterator.next());
        }
        return copy;
    }


    public void unSelect(String treeNodeId) {
        if(notifyOnChangeOnly && !isSelected(treeNodeId)) return;
        this.selected.remove(treeNodeId);
        notifyUnselectListeners(treeNodeId);
    }

    public void unSelectAll() {
        Iterator iterator =  this.selected.iterator();
        while(iterator.hasNext()){
            String nodeId = (String) iterator.next();
            iterator.remove();
            notifyUnselectListeners(nodeId);
        }
    }

    public void unSelect(String[] nodeIds) {
        for(int i=0; i<nodeIds.length; i++){
            unSelect(nodeIds[i]);
        }
    }

    public void unSelect(Collection nodeIds) {
        for (Iterator iterator = nodeIds.iterator(); iterator.hasNext();) {
            unSelect((String) iterator.next());
        }
    }

    public void unSelectDescendants(String nodeId) {
        ITreeNode node = findNode(nodeId);
        unSelectDescendants(node);
    }

    private void selectDescendants(ITreeNode node){
        if(node == null) return;
        for (Iterator iterator = node.getChildren().iterator(); iterator.hasNext();) {
            ITreeNode treeNode = (ITreeNode) iterator.next();
            select(treeNode.getId());
            if(treeNode.hasChildren()){
                selectDescendants(treeNode);
            }
        }
    }

    private void unSelectDescendants(ITreeNode node){
        if(node == null) return;
        for (Iterator iterator = node.getChildren().iterator(); iterator.hasNext();) {
            ITreeNode treeNode = (ITreeNode) iterator.next();
            unSelect(treeNode.getId());
            if(treeNode.hasChildren()){
                unSelectDescendants(treeNode);
            }
        }
    }

    public void unSelectDescendantsAndSelf(String nodeId) {
        unSelect(nodeId);
        unSelectDescendants(nodeId);
    }

    public void unSelectParent(String nodeId) {
        ITreeNode node = findNode(nodeId);
        if(node != null && !isRoot(node)){
            unSelect(node.getParent().getId());
        }
    }

    public void unSelectParentAndSelf(String nodeId) {
        unSelect(nodeId);
        unSelectParent(nodeId);
    }

    public void unSelectAncestors(String nodeId) {
        ITreeNode node = findNode(nodeId);
        if(node == null) return;
        while(!isRoot(node)){
            unSelect(node.getParent().getId());
            node = node.getParent();
        }
    }

    public void unSelectAncestorsAndSelf(String nodeId) {
        unSelect(nodeId);
        unSelectAncestors(nodeId);
    }

    public void unSelectChildren(String nodeId) {
        ITreeNode node = findNode(nodeId);
        if(node != null && node.getChildren().size() > 0){
            Iterator iterator = node.getChildren().iterator();
            while(iterator.hasNext()){
                ITreeNode child = (ITreeNode) iterator.next();
                unSelect(child.getId());
            }
        }
    }

    public void unSelectChildrenAndSelf(String nodeId){
        unSelect(nodeId);
        unSelectChildren(nodeId);
    }


    public Set getSelectedNodes() {
        return findNodes(this.selected);
    }

    public void setSingleSelectionMode(boolean mode) {
        this.singleSelectionMode = mode;
    }

    public boolean isSingleSelectionMode(){
        return this.singleSelectionMode;
    }

    public boolean isPreserveSelectionOfInvisibleNodes() {
        return preserveSelectionOfInvisibleNodes;
    }

    public void setPreserveSelectionOfInvisibleNodes(boolean preserveSelectionOfInvisibleNodes) {
        this.preserveSelectionOfInvisibleNodes = preserveSelectionOfInvisibleNodes;
    }

    public void addSelectListener(ISelectListener selectListener) {
        this.selectListeners.add(selectListener);
    }

    public void removeSelectListener(ISelectListener selectListener) {
        this.selectListeners.remove(selectListener);
    }

    public void addUnSelectListener(IUnSelectListener unSelectListener) {
        this.unselectListeners.add(unSelectListener);
    }

    public void removeUnSelectListener(IUnSelectListener unSelectListener) {
        this.unselectListeners.remove(unSelectListener);
    }

    public Iterator iterator(boolean includeRootNode) {
        return new TreeIterator(this, includeRootNode);
    }

    public ITreeNode[] getNodePath(String nodeId) {
        ITreeNode node = findNode(nodeId);
        if(node == null) return new ITreeNode[0];

        NodePath nodePath = new NodePath();
        getNodePath(node, nodePath);
        return nodePath.nodePath;
    }


    public ITreeFilter getFilter() {
        return this.filter;
    }

    public void setFilter(ITreeFilter filter) {
        this.filter = filter;
    }

    public void setNotifyOnChangeOnly(boolean notifyOnChangeOnly) {
        this.notifyOnChangeOnly = notifyOnChangeOnly;
    }

    public boolean isNotifyOnChangeOnly() {
        return this.notifyOnChangeOnly;
    }

    private void getNodePath(ITreeNode node, NodePath nodePath) {
        if(!isRoot(node)){
            nodePath.nodeCount++;
            getNodePath(node.getParent(), nodePath);
            nodePath.nodePath[nodePath.currentNode++] = node;
        } else {
            nodePath.nodePath = new ITreeNode[nodePath.nodeCount];
            nodePath.nodePath[0] = node;
            nodePath.currentNode++;
        }
    }


    private void multipleSelectVsSelectionModeValidation(String[] treeNodeIds) {
        if(isSingleSelectionMode() && treeNodeIds.length > 1){
            throw new IllegalStateException("You cannot select more than one node when the tree " +
                    "is in single-selection mode. ");
        }
    }

    private void notifyCollapseListeners(String treeNodeId) {
        if(this.collapseListeners.size() > 0){
            ITreeNode collapsedNode = findNode(treeNodeId);
            Iterator iterator = this.collapseListeners.iterator();
            while(iterator.hasNext()){
                ((ICollapseListener) iterator.next()).nodeCollapsed(collapsedNode, this);
            }
        }
    }

    private void notifySelectListeners(String treeNodeId) {
        if(this.selectListeners.size() > 0){
            ITreeNode selectedNode = findNode(treeNodeId);
            Iterator iterator = this.selectListeners.iterator();
            while(iterator.hasNext()){
                ((ISelectListener) iterator.next()).nodeSelected(selectedNode, this);
            }
        }
    }

    private void notifyUnselectListeners(String treeNodeId) {
        if(this.unselectListeners.size() > 0){
            ITreeNode unselectedNode = findNode(treeNodeId);
            Iterator iterator = this.unselectListeners.iterator();
            while(iterator.hasNext()){
                ((IUnSelectListener) iterator.next()).nodeUnselected(unselectedNode, this);
            }
        }
    }

    private void notifyExpandListeners(String treeNodeId) {
/*        if(this.expandListeners.size() > 0){
            ITreeNode expandedNode = findNode(treeNodeId);
            Iterator iterator = this.expandListeners.iterator();
            while(iterator.hasNext()){
                ((IExpandListener) iterator.next()).nodeExpanded(expandedNode, this);
            }
        } */
    }

    private class NodePath{
        public int currentNode = 0;
        public int nodeCount   = 1;
        public ITreeNode[] nodePath = null;
    }

}
