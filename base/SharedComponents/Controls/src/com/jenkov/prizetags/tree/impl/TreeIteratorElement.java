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
import com.jenkov.prizetags.tree.itf.ITreeNode;

import java.util.List;
import java.io.Serializable;

public class TreeIteratorElement implements ITreeIteratorElement, Serializable{

    protected ITreeNode node    = null;
    protected List      indentationProfile = null;
    protected boolean   isExpanded = false;
    protected boolean   isSelected = false;
    protected boolean   isFirstChild = false;
    protected boolean   isLastChild  = false;
    /**
     * @deprecated
     */
    protected int       level   = 0 ;

    /**
     * @deprecated
     * @param node
     * @param level
     * @param isExpanded
     * @param isSelected
     * @param isFirstChild
     * @param isLastChild
     */
    public TreeIteratorElement(ITreeNode node, int level,
                               boolean isExpanded  , boolean isSelected,
                               boolean isFirstChild, boolean isLastChild){
        this.node = node;
        this.level= level;
        this.isExpanded   = isExpanded;
        this.isSelected   = isSelected;
        this.isFirstChild = isFirstChild;
        this.isLastChild  = isLastChild;
    }

    public TreeIteratorElement(ITreeNode node, List indentationProfile,
                               boolean isExpanded  , boolean isSelected,
                               boolean isFirstChild, boolean isLastChild){
        this.node = node;
        this.indentationProfile = indentationProfile;
        this.isExpanded   = isExpanded;
        this.isSelected   = isSelected;
        this.isFirstChild = isFirstChild;
        this.isLastChild  = isLastChild;
    }

    public ITreeNode getNode() {
        return this.node;
    }

    public String getId() {
        return getNode().getId();
    }

    public String getName() {
        return getNode().getName();
    }

    public int childLevel() {
        return this.level;
    }

    public List getIndendationProfile() {
        return this.indentationProfile;
    }

    public boolean isExpanded(){
        return this.isExpanded;
    }

    public boolean isSelected() {
        return this.isSelected;
    }

    public boolean isFirstChild() {
        return this.isFirstChild;
    }

    public boolean isLastChild() {
        return this.isLastChild;
    }
}
