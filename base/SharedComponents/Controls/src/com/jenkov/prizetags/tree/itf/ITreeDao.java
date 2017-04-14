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
 * Represents a DAO (Data Access Object) that is capable of loading the nodes of a tree dynamically.
 * Implement this interface and set an instance of the implementation on the ITree instance, using
 * the ITree.setTreeDao(treeDaoImpl).
 *
 * <br/><br/>
 * The com.jenkov.prizetags.tree.impl.FileDao is an example implementation that can build a
 * tree dynamically from files and directories. Look at this class to see what an implementation
 * requires.
 *
 * @author Jakob Jenkov - Copyright 2005 Jenkov Development
 */
public interface ITreeDao {

    /**
     * Reads the root of the tree and all children of the root.
     *
     * @param tree The tree to read the nodes of
     */
    public void readRootAndChildren(ITree tree);

    /**
     * Reads the children of the given node, and its grandchildren. The reason
     * the grandchildren should be loaded as well is, that by default expand
     * handles are only shown for nodes that have children. If only the immediate
     * children of the expanded node are loaded, the children will be visible, but
     * they will not have any expand handles. You can also cheat and just add a
     * dummy node to each children instead of reading the grandchildren, but then
     * all the children will appear with an expand handle, not just the ones that
     * really have children.
     *
     * @param tree The tree the expanded node belongs to.
     * @param node The expanded node to load the children and grandchildren of.
     */
    public void readChildrenAndGrandchildren(ITree tree, ITreeNode node);
}
