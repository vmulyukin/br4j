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
package com.jenkov.prizetags.tree.itf;

import java.util.List;

/**
 * Instances of this interface wraps the <code>ITreeNode</code> instances, when
 * the TreeIterator iterates the tree, from within the TreeTag.
 *
 * <br/><br/>
 * This interface is primarily used by the TreeIterator and the TreeTag classes when
 * iterating the tree nodes. During normal work with the ITree interface and related
 * classes, you will not come across this interface.
 */
public interface ITreeIteratorElement {

    /**
     * Returns the node wrapped by this <code>ITreeIteratorElement</code> instance.
     * @return A <code>ITreeNode</code> instance.
     */
    public ITreeNode getNode();

    /**
     * Returns the altId of the wrapped node.
     * @return The altId of the wrapped node.
     */
    public String    getId();

    /**
     * Returns the name of the wrapped node
     * @return The name of the wrapped node.
     */
    public String    getName();

    /**
     * Returns the child level of this node. The child level tells the indentation
     * tag how much this node should be indented. Thus, if a node is a child of the
     * root node, it's child level is 1.
     * @return The child level (indentation level) of this node.
     * @deprecated Use the getIndendationProfile() method instead.
     */
    public int       childLevel();

    /**
     * Returns the indentation profile of the wrapped node. The indendation profile
     * is a <code>List</code> of <code>Boolean</code> instances, containing one
     * instance for each ancestor of the wrapped node.
     * The <code>Boolean</code> instances tells whether that particular ancestor
     * was the last child of it's parent or not. If the ancestor was not the
     * last child of it's parent, a vertical line can be drawn from this ancestor
     * to it's next sibling. Else a blank space can be drawn.
     *
     * @return A <code>List</code> of <code>Boolean</code> instances, telling whether to
     * connect ancestors of the wrapped node, to succeding ancestor sibblings with vertical
     * lines.
     */
    public List      getIndendationProfile();


    /**
     * Returns true if the wrapped node is expanded, false if not.
     * @return True if the wrapped node is expanded, false if not.
     */
    public boolean   isExpanded();

    /**
     * Returns true if the wrapped node is active, false if not.
     * @return True if the wrapped node is active, false if not.
     */
    public boolean   isSelected();

    /**
     * Returns true if the wrapped node is the first child of it's parent,
     * false if not.
     * @return True if the wrapped node is the first child of it's parent,
     * false if not.
     */
    public boolean   isFirstChild();

    /**
     * Returns true if the wrapped node is the last child of it's parent,
     * false if not.
     * @return True if the wrapped node is the last child of it's parent,
     * false if not.
     */
    public boolean   isLastChild();
}
