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

import com.jenkov.prizetags.tree.itf.ITree;
import com.jenkov.prizetags.tree.itf.ITreeIteratorElement;
import com.jenkov.prizetags.tree.itf.ITreeNode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TreeIterator implements Iterator, Serializable{


    protected ITree         tree            = null;
    protected TreeNodeStack stack           = null;
    protected int           level           = 0;

//    public TreeIterator (ITree tree, boolean includeRootNode){
//        this(tree, includeRootNode, null);
//    }

    public TreeIterator(ITree tree, boolean includeRootNode) {
        this.stack = new TreeNodeStack();
        this.tree = tree;
        if(includeRootNode){
            ITreeIteratorElement root = new TreeIteratorElement(tree.getRoot(), new ArrayList()
                    , tree.isExpanded(tree.getRoot().getId())
                    , tree.isSelected(tree.getRoot().getId())
                    , true, true);
            this.stack.push(root);
        } else {
            pushChildren(new TreeIteratorElement(tree.getRoot(), new ArrayList()
                    , tree.isExpanded(tree.getRoot().getId())
                    , tree.isSelected(tree.getRoot().getId())
                    , true, true
                    ), false);
        }
    }

    public boolean hasNext(){
        return this.stack.size() > 0;
    }

    public Object next(){
        ITreeIteratorElement element = (ITreeIteratorElement) this.stack.pop();
        if(this.tree.isExpanded(element.getNode().getId())){
            pushChildren(element, true);
        }
        return element;
    }

    public void remove() {
        //not implemented
    }

    protected void pushChildren(ITreeIteratorElement element, boolean indent){
        List indentationProfile = copyIndentationProfile(element);

        if(indent){
            indentationProfile.add(new Boolean(element.isLastChild()));
        }

        List children           = element.getNode().getChildren();
        for(int i=0; i < children.size(); i++){
            ITreeNode node = (ITreeNode) children.get(children.size()-i-1);
            this.stack.push(
                    new TreeIteratorElement(
                              node
                            , indentationProfile
                            , this.tree.isExpanded(node.getId())
                            , this.tree.isSelected(node.getId())
                            , i == children.size() - 1
                            , i == 0
            ));
        }
    }

    protected List copyIndentationProfile(ITreeIteratorElement element){
        List copy = new ArrayList();
        Iterator iterator = element.getIndendationProfile().iterator();
        while(iterator.hasNext()){
            copy.add(iterator.next());
        }
        return copy;
    }








}
