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

import com.aplana.dbmi.model.web.AbstractControl;
import com.jenkov.prizetags.tree.itf.ITreeNode;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.Serializable;

public class TreeNode implements ITreeNode, Serializable{
	

	protected AbstractControl abstractControl;
    protected String       value              = "";
    protected String       name              = "";
    protected String       id                = "";
    protected String       parentId          = "";
    protected String       type              = "";
    protected String       toolTip           = "";
    protected String       expandedImageUrl  = "";
    protected String       collapsedImageUrl = "";
    protected Serializable object            = null;
    protected List         children          = new ArrayList();
    protected ITreeNode    parent            = null;
    private String nameRu;
    private String nameEn;

    public TreeNode(){
    }

    public TreeNode(ITreeNode parentNode){
        setParent(parentNode);
    }

    public TreeNode(String id, String name){
        setId(id);
        setName(name);
    }

    public TreeNode(AbstractControl abstractControl){
    	this.abstractControl  =abstractControl;
    	this.object = abstractControl;
    }
    
    public TreeNode(String id, String name, ITreeNode parentNode){
        setId(id);
        setName(name);
        setParent(parentNode);
    }

    public TreeNode(String id, String name, String type){
        setId(id);
        setName(name);
        this.type = type;
    }

    public TreeNode(String id, String name, String type, String value){
        this(id,name,type);
        this.value = value;
    }

    public TreeNode(String id, String name, String type, ITreeNode parentNode){
        setId(id);
        setName(name);
        this.type = type;
        setParent(parentNode);
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getToolTip() {
        return toolTip;
    }

    public void setToolTip(String text) {
        this.toolTip = text;
    }

    public String getExpandedImageUrl() {
        return expandedImageUrl;
    }

    public void setExpandedImageUrl(String expandedImageUrl) {
        this.expandedImageUrl = expandedImageUrl;
    }

    public String getCollapsedImageUrl() {
        return collapsedImageUrl;
    }

    public void setCollapsedImageUrl(String collapsedImageUrl) {
        this.collapsedImageUrl = collapsedImageUrl;
    }

    public Serializable getObject() {
        return this.object;
    }

    public void setObject(Serializable object) {
        this.object = object;
    }

    public void addChild(ITreeNode node) {
        addChildOnly(node);
        node.setParentOnly(this);
    }

    public void addChild(int index, ITreeNode node) {
        addChildOnly(index, node);
        node.setParentOnly(this);
    }

    public void addChildOnly(ITreeNode node) {
        if(this == node){
            throw new IllegalArgumentException("A TreeNode cannot have itself as a child");
        }
        this.children.add(node);
    }

    public void addChildOnly(int index, ITreeNode node) {
        if(this == node){
            throw new IllegalArgumentException("A TreeNode cannot have itself as a child");
        }
        this.children.add(index, node);
    }

    public void removeChild(ITreeNode node) {
        this.children.remove(node);
        node.setParentOnly(null);
    }

    public void removeChildOnly(ITreeNode node) {
        this.children.remove(node);
    }

    public void removeAllChildren() {
        Iterator iterator = this.children.iterator();
        while(iterator.hasNext()){
            ITreeNode node = (ITreeNode) iterator.next();
            node.setParentOnly(null);
        }
        this.children.clear();
    }

    public List getChildren() {
        return this.children;
    }

    public boolean hasChildren() {
        return getChildren().size() > 0;
    }

    public ITreeNode getParent() {
        return this.parent;
    }


    public void setParent(ITreeNode newParent) {
        if(this == newParent) {
            throw new IllegalArgumentException("A TreeNode cannot be it's own parent");
        }

        if(shouldRemoveSelfFromOldParent(newParent)){
            this.parent.removeChildOnly(this);
        }

        if(shouldAddSelfToNewParent(newParent)){
            newParent.addChildOnly(this);
        }

        this.parent = newParent;
        setParentId(this.parent.getId());
    }

    private boolean shouldAddSelfToNewParent(ITreeNode newParentNode) {
        return newParentNode != null && !newParentNode.getChildren().contains(this);
    }

    private boolean shouldRemoveSelfFromOldParent(ITreeNode newParentNode) {
        return this.parent != null && this.parent != newParentNode;
    }

    public void setParentOnly(ITreeNode newParent) {
        this.parent = newParent;
    }

    public int getAncestorCount() {
        int ancestorCount = 0;
        ITreeNode ancestor = getParent();
        while(ancestor != null){
            ancestorCount++;
            ancestor = ancestor.getParent();
        }
        return ancestorCount;
    }

    public int getChildCount() {
        return this.children.size();
    }

    public int getDescendantCount() {
        int descendantCount = this.children.size();
        for (Iterator iterator = children.iterator(); iterator.hasNext();) {
            ITreeNode iTreeNode = (ITreeNode) iterator.next();
            if(iTreeNode.hasChildren()){
                descendantCount += iTreeNode.getDescendantCount();
            }
        }
        return descendantCount;
    }

    public void copy(ITreeNode node) {
        setParentId(node.getParentId());
        setCollapsedImageUrl(node.getCollapsedImageUrl());
        setExpandedImageUrl(node.getExpandedImageUrl());
        setName(node.getName());
        setObject(node.getObject());
        setToolTip(node.getToolTip());
        setType(node.getType());
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int hashCode() {
        return this.id.hashCode();
    }

    public boolean equals(Object o){
        if( o == null)               { return false; }
        if(!(o instanceof ITreeNode)){ return false; }
        
        ITreeNode node = (ITreeNode) o;
        if(!this.id.equals(node.getId())){
            return false;
        }

        return true;
    }

    public String toString(){
        StringBuffer buffer = new StringBuffer();
        buffer.append("( id: ");
        buffer.append(getId());
        buffer.append(",    name: ");
        buffer.append(getName());
        buffer.append(",    type: ");
        buffer.append(getType());
        buffer.append(")");

        return buffer.toString();
    }

	public AbstractControl getAbstractControl() {
		return abstractControl;
	}

	public void setAbstractControl(AbstractControl abstractControl) {
		this.abstractControl = abstractControl;
	}


    public String getNameRu() {
        return nameRu;
    }

    public void setNameRu(String nameRu) {
        this.nameRu = nameRu;
    }

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }
}
