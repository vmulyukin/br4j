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
import com.jenkov.prizetags.tree.itf.ITreeDao;
import com.jenkov.prizetags.tree.itf.ITreeNode;

import java.util.*;

/**
 * @author Jakob Jenkov - Copyright 2004-2006 Jenkov Development
 */
public abstract class DaoBase implements ITreeDao {

    public void readRootAndChildren(ITree tree) {
        ITreeNode rootNode = readRoot(tree);

        readAndUpdateChildren(rootNode);
    }

    protected abstract ITreeNode readRoot(ITree tree);


    public void readChildrenAndGrandchildren(ITree tree, ITreeNode parentNode) {

        if(parentNode == null) return;

        readAndUpdateChildren(parentNode);


        Map parentMap         = new HashMap();
        Map parentChildSetMap = new HashMap();
        Iterator iterator = parentNode.getChildren().iterator();
        while(iterator.hasNext()){
            ITreeNode parent = (ITreeNode) iterator.next();
            parentMap.put(parent.getId(), parent);
            parentChildSetMap.put(parent.getId(), new HashSet());
        }


        Set grandChildren = readGrandChildren(parentNode);

        Iterator grandChildIterator = grandChildren.iterator();
        while(grandChildIterator.hasNext()){
            ITreeNode grandChild = (ITreeNode) grandChildIterator.next();
            ((Set) parentChildSetMap.get(grandChild.getParentId())).add(grandChild);
        }


        Iterator parentIdIterator = parentMap.keySet().iterator();
        while(parentIdIterator.hasNext()){
            String    id       = (String   ) parentIdIterator.next();
            ITreeNode parent   = (ITreeNode) parentMap.get        (id);
            Set       childSet = (Set      ) parentChildSetMap.get(id);
            updateNodeChildren(parent, childSet);
            childSet.clear();
        }
        parentMap.clear();
        parentChildSetMap.clear();

    }


    /**
     * Reads all grand children of the given parent node. You should iterate
     * the children of the parent node, and read all children of these children
     * (the grandchildren of the parent node). Make sure you set both the
     * id and the parentId on all grandchildren, since the parent id is used
     * to attach the grandchild node to its corresponding parent node. You
     * should not do this. This is done automatically in the
     * readChildrenAndGrandChildren() method. Just read the grandchildren
     * into the set and return the set.
     *
     * @param parentNode The node to read all grandchildren of.
     * @return A set containing all grand children of the parent node, in no particular order.
     */
    protected abstract Set readGrandChildren(ITreeNode parentNode) ;


    protected void readAndUpdateChildren(ITreeNode parentNode){
        Set               nodesFromDatabase = readChildren(parentNode);
        updateNodeChildren(parentNode, nodesFromDatabase);
        nodesFromDatabase.clear();
    }

    /**
     * Reads all children of the given parent node. Do not
     * connect the children to the parent node. This will be done automatically
     * by the readAndUpdateChildren method.
     * @param parentNode The node to read all children of.
     * @return A set containing all children of the parent node.
     */
    protected abstract Set readChildren(ITreeNode parentNode);


    protected void updateNodeChildren(ITreeNode parentNode, Set nodesFromSource) {
        Set childset = copyCurrentChildSet(parentNode);

        //add nodes that exist in source, but not in currently loaded child set.
        Iterator childrenFromDatabaseIterator = nodesFromSource.iterator();
        while(childrenFromDatabaseIterator.hasNext()){
            ITreeNode nodeFromSource = (ITreeNode) childrenFromDatabaseIterator.next();
            if(!childset.contains(nodeFromSource)){
                parentNode.addChild(nodeFromSource);
            } else {
                Iterator childsetIterator = childset.iterator();
                while(childsetIterator.hasNext()){
                    ITreeNode childNode = (ITreeNode) childsetIterator.next();
                    if(childNode.getId().equals(nodeFromSource.getId())){
                        childNode.copy(nodeFromSource);
                        break;
                    }
                }
            }
        }


        //remove nodes that exist in currently loaded child set, but doesn't exist in the source (anymore).
        Iterator childIterator = childset.iterator();
        while(childIterator.hasNext()){
            ITreeNode currentChild = (ITreeNode) childIterator.next();
            if(!nodesFromSource.contains(currentChild)){
                parentNode.removeChild(currentChild);
            }
        }

    }

    protected Set copyCurrentChildSet(ITreeNode parentNode) {
        Set  childset = new HashSet();
        Iterator childIterator = parentNode.getChildren().iterator();
        while(childIterator.hasNext()){
            childset.add(childIterator.next());
        }
        return childset;
    }

}
