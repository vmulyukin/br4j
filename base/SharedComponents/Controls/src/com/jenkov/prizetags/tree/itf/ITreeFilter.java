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



package com.jenkov.prizetags.tree.itf;

/**
 * If you do not want to show all visible nodes in a tree, but don't want to
 * remove the nodes from the tree either, use a ITreeFilter. The filter
 * tells the TreeIterator which nodes should be included / excluded from the
 * displayed tree.
 *
 * @author Jakob Jenkov - Copyright 2005 Jenkov Development
 */
public interface ITreeFilter {

    /**
     * Initialize the filter. If for instance you need to traverse the tree
     * in a different order to determine which nodes are visible, this
     * method is the place to do so. Then save the information and return
     * it in the accept-calls.
     *
     * @param tree The tree to initialize this filter to.
     */
    public void init(ITree tree);

    /**
     * Return true if the node is to be included when iterating the visible nodes of a tree.
     * False if the node should be ignored, and thus not displayed in the JSP tree.
     * @param tree The tree the node belongs to.
     * @param node The node to tell if should be included in the displayed tree.
     * @return
     */
    public boolean accept(ITree tree, ITreeIteratorElement node);
}
    

