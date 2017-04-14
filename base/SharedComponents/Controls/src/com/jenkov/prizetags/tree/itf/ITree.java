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

import java.util.Iterator;
import java.util.Set;
import java.util.Collection;

public interface ITree {


    /**
     * Returns the used tree dao.
     * @return The used tree dao. Null if no tree dao is set.
     */
    public ITreeDao getTreeDao();

    /**
     * Sets an ITreeDao (Data Access Object) on this tree. Using
     * this ITreeDao the tree can build itself dynamically and lazily, thus only
     * loading the visible nodes and their immediate children.
     *
     * @param dao
     */
    public void setTreeDao(ITreeDao dao);

    /**
     * Returns the root node of this tree.
     * @return The root node.
     */
    public ITreeNode getRoot();

    /**
     * Sets the root node of this tree.
     * @param node The node to set as root node for this tree.
     */
    public void      setRoot(ITreeNode node);

    /**
     * Returns the <code>ITreeNode</code> instance coresponding to the given treeNodeId. If
     * no node matches the given node id, null is returned.
     * Note: If more than one node has this node id, only the first one found is returned.
     * This shouldn't normally happen, since node id's are supposed to be unique within
     * the tree. But the tree doesn't enforce this uniqueness (as of now).
     * @param treeNodeId The id of the node to find.
     * @return The node with the id matching the treeNodeId parameter, or null if no node
     * in the tree matches the node.
     */
    public ITreeNode findNode  (String treeNodeId);


    /**
     * Returns the nodes matching the node id's in the passed set.
     * @param treeNodeIds A <code>Set</code> of node id's (<code>String</code> instances)
     * @return A <code>Set</code> of <code>ITreeNode</code> instances.
     */
    public Set findNodes(Set treeNodeIds);

    /**
     * Returns true if the node with the given node id is expanded.
     * @param treeNodeId The id of the node to check if is expanded.
     * @return True if the node is expanded, false if not.
     */
    public boolean   isExpanded(String treeNodeId);

    /**
     * Marks the node with the given node id as expanded, and notifies all expand listeners.
     * @param treeNodeId The id of the node to mark as expanded.
     */
    public void      expand    (String treeNodeId);

    /**
     * Expands all nodes currently in the tree.
     */
    public void      expandAll();


    /**
     * Removes the expand mark of the node with the given node id, and notifies all collapse
     * listeners.
     * @param treeNodeId The id of the node to remove the expand mark from.
     */
    public void      collapse  (String treeNodeId);

    /**
     * Collapses all nodes currently in the tree.
     */
    public void      collapseAll();

    /**
     * Returns the expanded nodes in this tree.
     * @return A <code>Set</code> of <code>ITreeNode</code> instances.
     */
    public Set       getExpandedNodes();

    /**
     * Adds an expand listener to this tree. All expand listeners are notified
     * whenever a node is expanded in this tree.
     * @param expandListener The <code>IExpandListener</code> instance to add.
     */
    public void      addExpandListener      (IExpandListener expandListener);

    /**
     * Removes an expand listener from this tree.
     * @param expandListener The expand listener to remove.
     */
    public void      removeExpandListener   (IExpandListener expandListener);

    /**
     * Adds a collapseListener to this three. All collapse listeners are notified
     * whenever a node is collapsed in this tree.
     * @param collapseListener The collapse listener to add.
     */
    public void      addCollapseListener    (ICollapseListener collapseListener);

    /**
     * Removes a collapse listener from this tree.
     * @param collapseListener The collapse listener to remove.
     */
    public void      removeCollapseListener (ICollapseListener collapseListener);

    /**
     * Returns true if the node with the given node id is active.
     * @param treeNodeId The id of the node to check if is active.
     * @return True if the node is active, false if not.
     */
    public boolean   isSelected(String treeNodeId);

    /**
     * Marks the node with the given node id as active, and notifies all select listeners.
     * A active node can
     * be used to render that node's text in bold (or whatever you choose).
     * The nodeMatch tag can match on active/unselected nodes.
     * @param treeNodeId The id of the node to mark as active.
     */
    public void      select    (String treeNodeId);

    /**
     * Like select(String nodeId) just for multiple nodes.
     * If the nodeId array is empty, nothing happens.
     *
     * <br/><br/>
     * NOTE: If the tree is in single-selection mode, and the array
     * contains more than one node id, this method will throw an
     * IllegalStateException.
     *
     * @param  treeNodeIds The id's of the nodes to mark as selected.
     * @throws IllegalStateException If the tree is in single-selection mode and
     *         the array contains more than one node id.
     */
    public void      select    (String[] treeNodeIds);


    /**
     * Selects only the node ids in the given array. All nodes that were previously selected
     * but are not also in the node id array, will be unselected. This method is useful
     * when using checkboxes with the nodes in the tree. Only the nodes who's checkbox is checked
     * will be selected when calling the selectOnly method with the nodeId's of the selected nodes.
     *
     * <br/><br/>
     * Example:<br/><br/>
     * <code>
     * tree.selectOnly(request.getParameterValues("select"));
     *
     * <br/><br/>
     * NOTE: If the tree is in single-selection mode, and the array
     * contains more than one node id, this method will throw an
     * IllegalStateException.
     *
     * @param  treeNodeIds The id's of the nodes to mark as selected.
     * @throws IllegalStateException If the tree is in single-selection mode and
     *         the array contains more than one node id.
     */
    public void      selectOnly    (String[] treeNodeIds);



    /**
     * Removes the selected mark of the node with this node id, and notifies all
     * unSelect listeners.
     * @param treeNodeId The id of the node to remove the active mark from.
     */
    public void      unSelect  (String treeNodeId);

    /**
     * Unselects all nodes in this tree, and notifies all unSelect listeners.
     * This method is called by the Tree (ITree implementation) whenever
     * a node is active in single select mode, before marking the new node
     * as active.
     */
    public void      unSelectAll();

    /**
     * Returns the selected nodes in this tree.
     * @return  A <code>Set</code> of <code>ITreeNode</code> instances.
     */
    public Set       getSelectedNodes();

    /**
     * Adds a select listener to this tree. All select listeners are notified whenever
     * a node is active in this tree.
     * @param selectListener The select listener to add.
     */
    public void      addSelectListener      (ISelectListener selectListener);

    /**
     * Removes a select listener from this tree.
     * @param selectListener The select listener to remove.
     */
    public void      removeSelectListener   (ISelectListener selectListener);

    /**
     * Adds an unSelect listener to this tree. All unSelect listeners are notified whenever
     * a node is unselected in this tree.
     * @param unSelectListener The unSelect listener to add.
     */
    public void      addUnSelectListener    (IUnSelectListener unSelectListener);

    /**
     * Removes an unSelect listener form this tree.
     * @param unSelectListener The unSelect listener to remove.
     */
    public void      removeUnSelectListener (IUnSelectListener unSelectListener);

    /**
     * If you call setSingleSelectionMode(true) the tree will unSelect all
     * nodes before selecting a node. This way only one node can be
     * active at any time.
     * @param mode  True to set the tree in single selection mode, false to set
     *              the tree in multiple selection mode.
     */
    public void      setSingleSelectionMode(boolean mode);

    /**
     * Returns true if this tree is in single selection mode. The default is
     * that the tree is not in single selection mode.
     * @return True if this tree is in single selection mode, false if not.
     */
    public boolean   isSingleSelectionMode();

    /**
     * Returns whether the tree preserves the selection of invible nodes.
     * Default is true.
     * When having a checkbox in the JSP page for each node in the tree,
     * you cannot distinguish between a visible unchecked checkbox, or
     * a checkbox that is no longer part of the form because it's parent
     * has been collapsed. In effect collapsing a node results in unselection
     * of all its children and grandchildren. If preserveSelectionOfInvisibleNodes
     * is true the selectOnly call used by the checkbox tree JSP pages will
     * not change the selection state of invisible nodes.
     *
     * <br/><br/>
     * NOTE: This method only affects the selectOnly call. The other unselect methods
     * will ignore this setting.
     *
     * @return True if the tree will preserve selection of invisible nodes mode. False if not.
     */
    public boolean isPreserveSelectionOfInvisibleNodes();

   /**
    * Sets whether the tree preserves the selection of invible nodes.
    * Default is true.
    * When having a checkbox in the JSP page for each node in the tree,
    * you cannot distinguish between a visible unchecked checkbox, or
    * a checkbox that is no longer part of the form because it's parent
    * has been collapsed. In effect collapsing a node results in unselection
    * of all its children and grandchildren. Tree.If preserveSelectionOfInvisibleNodes
    * is true the selectOnly call used by the checkbox tree JSP pages will
    * not change the selection state of invisible nodes.
    *
    * <br/><br/>
    * NOTE: This method only affects the selectOnly call. The other unselect methods
    * will ignore this setting.
    *
    * @param preserveSelectionOfInvisibleNodes Set to true if the tree should
    * Preserve Selection of Invisible Nodes mode. False if not.
    */
    public void setPreserveSelectionOfInvisibleNodes(boolean preserveSelectionOfInvisibleNodes);


    /**
     * Returns an iterator of the nodes in the tree. The nodes are wrapped in
     * ITreeIteratorElements instances, that contain extra info about the nodes
     * iterated, for instance if it is expanded etc. The ITreeNode doesn't have
     * this info itself.
     *
     * <br/><br/>
     * The tree nodes are iterated in the same sequence they would be displayed in, meaning
     * depth first mode.
     *
     * <br/><br/>
     * This method is normally only used by the TreeTag class, to iterate the nodes in
     * the tree.
     *
     * @param includeRootNode True if you want to include the root node in this
     *          iterator. False if you want to exclude the root node.
     * @return An iterator containing ITreeIteratorElements wrapping the nodes in the tree.
     */
    public Iterator  iterator(boolean includeRootNode);


    /*
     * todo Implement a setFilter(ITreeNodeFilter filter) method that can filter
     *      the iterated nodes at runtime.
     */

    /**
     * Expands the nodes with the given node ids.
     * Node ids that doesn't have matching nodes in the tree are ignored.
     * @param nodeIds The node ids of the nodes to expand.
     */
    public void expand(String[]   nodeIds);

    /**
     * Collapses the nodes with the given node ids.
     * @param nodeIds The node ids of the nodes to collapse.
     */
    public void collapse(String[] nodeIds);

    /**
     * Expands the nodes with the given node ids.
     * @param nodeIds The node ids (as String's) to expand.
     */
    public void expand(Collection nodeIds);

    /**
     * Collapses the nodes with the given node ids.
     * @param nodeIds The node ids (as String's) to collapse.
     */
    public void collapse(Collection nodeIds);

    /**
     * Expands the parent, if any, of the node with the given node id.
     * If no node exists with that node id, or the node has no parent, nothing happens.
     * @param nodeId The id of the node to expand the parent of.
     */
    public void expandParent(String nodeId);

    /**
     * Collapses the parent, if any, of the node with the given node id.
     * If no node exists with that node id, or the node has no parent, nothing happens.
     * @param nodeId The id of the node to collapse the parent of.
     */
    public void collapseParent(String nodeId);

    /**
     * Expands the node with the given node id and its parent.
     * If no node exists with the given node id, nothing happens.
     * If the node has no parent, only the node itself is expanded.
     * @param nodeId The node id of the node to expand (plus its parent).
     */
    public void expandParentAndSelf(String nodeId);

    /**
     * Collapses the node with the given node id and its parent.
     * If no node exists with the given node id, nothing happens.
     * If the node has no parent, only the node itself is collapsed.
     * @param nodeId The node id of the node to collapse (plus its parent).
     */
    public void collapseParentAndSelf(String nodeId);

    /**
     * Expands the children, if any, of the node with the given node id.
     * If no node exists with that node id, or the node has no children, nothing happens.
     * @param nodeId The id of the node to expand the children of.
     */
    public void expandChildren(String nodeId);

    /**
     * Collapses the children, if any, of the node with the given node id.
     * If no node exists with that node id, or the node has no children, nothing happens.
     * @param nodeId The id of the node to collapse the children of.
     */
    public void collapseChildren(String nodeId);

    /**
     * Expands the node and its children, if any, of the node with the given node id.
     * If no node exists with that node id, nothing happens. If the node has no children
     * only the node itself is expanded.
     * @param nodeId The id of the node to expand + expand the children of.
     */
    public void expandChildrenAndSelf(String nodeId);


    /**
     * Collapses the node and its children, if any, of the node with the given node id.
     * If no node exists with that node id, nothing happens. If the node has no children
     * only the node itself is collapsed.
     * @param nodeId The id of the node to collapse + collapse the children of.
     */
    public void collapseChildrenAndSelf(String nodeId);


    /**
     * Expands all ancestors of the node with the given node id.
     * If no node exists with the given node id, nothing happens.
     * If the node has no ancestors nothing happens.
     * @param nodeId The node id of the node to expand all ancestors of.
     */
    public void expandAncestors(String nodeId);

    /**
     * Collapses all ancestors of the node with the given node id.
     * If no node exists with the given node id, nothing happens.
     * If the node has no ancestors nothing happens.
     * @param nodeId The node id of the node to collapse all ancestors of.
     */
    public void collapseAncestors(String nodeId);

    /**
     * Expands all ancestors + the node itself of the node with the given node id.
     * If no node exists with the given node id, nothing happens.
     * If the node has no ancestors only the node itself is expanded.
     * @param nodeId The node id of the node to expand + expand all ancestors of.
     */
    public void expandAncestorsAndSelf(String nodeId);

    /**
     * Collapses all ancestors + the node itself of the node with the given node id.
     * If no node exists with the given node id, nothing happens.
     * If the node has no ancestors only the node itself is collapsed.
     * @param nodeId The node id of the node to collapse plus collapse all ancestors of.
     */
    public void collapseAncestorsAndSelf(String nodeId);

    /**
     * Expands all descendants of the node with the given node id.
     * If no node with the given node id exists, or the node has no
     * descendants, nothing happens.
     * Only descendants that have children are expanded.
     * @param nodeId The node id of the node to expand all descendants of.
     */
    public void expandDescendants(String nodeId);

    /**
     * Collapses all descendants of the node with the given node id.
     * If no node with the given node id exists, or the node has no
     * descendants, nothing happens.
     * Only descendants that have children are collapsed.
     * @param nodeId The node id of the node to collapse all descendants of.
     */
    public void collapseDescendants(String nodeId);

    /**
     * Expands the node with the given node id plus all it's descendants.
     * If node node exists with the given node id, nothing happens.
     * If the node has no descendants, only the node itself is expanded.
     * Only descendants that have children are expanded.
     * @param nodeId The node to expand plus expand all descendants of.
     */
    public void expandDescendantsAndSelf(String nodeId);

    /**
     * Collapses the node with the given node id plus all it's descendants.
     * If node node exists with the given node id, nothing happens.
     * If the node has no descendants, only the node itself is collapsed.
     * Only descendants that have children are collapsed.
     * @param nodeId The node to collapse plus collapse all descendants of.
     */
    public void collapseDescendantsAndSelf(String nodeId);

    /**
     * Unselects the nodes with the given node ids. Non-existing node ids are ignored.
     * @param nodeIds The node ids of the nodes to unselect.
     */
    public void unSelect(String[] nodeIds);

    /**
     * Selects the nodes with the given node ids. Non-existing node ids are ignored.
     * @param nodeIds The node ids of the nodes to select.
     */
    public void select(Collection nodeIds);

    /**
     * Unselects the nodes with the given node ids. Non-existing node ids are ignored.
     * @param nodeIds The node ids of the nodes to unselect.
     */
    public void unSelect(Collection nodeIds);

    /**
     * Selects all nodes in the tree.
     */
    public void selectAll();

    /**
     * Selects all descendants of a given node.
     * If no node exists with the given node id, nothing happens.
     * If the given node has node descendants, nothing happens.
     * @param nodeId The node id of the node to expand the descendants of.
     */
    public void selectDescendants(String nodeId);

    /**
     * Unselects all descendants of a given node.
     * If no node exists with the given node id, nothing happens.
     * If the given node has node descendants, nothing happens.
     * @param nodeId The node id of the node to unselect the descendants of.
     */
    public void unSelectDescendants(String nodeId);

    /**
     * Selects the node plus all its descendants.
     * If no node exists with the given node id, nothing happens.
     * If the node has no descendants only the node itself is selected.
     * @param nodeId The id of the node to select plus select all descendants of.
     */
    public void selectDescendantsAndSelf(String nodeId);

    /**
     * Unselects the node plus all its descendants.
     * If no node exists with the given node id, nothing happens.
     * If the node has no descendants only the node itself is unselected.
     * @param nodeId The id of the node to unselect plus unselect all descendants of.
     */
    public void unSelectDescendantsAndSelf(String nodeId);

    /**
     * Selects the parent of the node with the given node id.
     * If no node exists with the given node id, or the node has
     * no parent, nothing happens.
     * @param nodeId The node id of the node to select the parent of.
     */
    public void selectParent(String nodeId);

    /**
     * Unselects the parent of the node with the given node id.
     * If no node exists with the given node id, or the node has
     * no parent, nothing happens.
     * @param nodeId The node id of the node to unselect the parent of.
     */
    public void unSelectParent(String nodeId);

    /**
     * Selects the node with the given node id plus its parent.
     * If no node exists with the given node id nothing happens.
     * If the node has no parent only the node itself is selected.
     * @param nodeId The id of the node to select plus select its parent.
     */
    public void selectParentAndSelf(String nodeId);

    /**
     * Unselects the node with the given node id plus its parent.
     * If no node exists with the given node id nothing happens.
     * If the node has no parent only the node itself is unselected.
     * @param nodeId The id of the node to unselect plus unselect its parent.
     */
    public void unSelectParentAndSelf(String nodeId);

    /**
     * Selects all ancestors of the node with the given node id.
     * If no node exists with the given node id, or the node
     * has no ancestors, nothing happens.
     * @param nodeId The node id of the node to select all ancestors of.
     */
    public void selectAncestors(String nodeId);

    /**
     * Unselects all ancestors of the node with the given node id.
     * If no node exists with the given node id, or the node
     * has no ancestors, nothing happens.
     * @param nodeId The node id of the node to unselect all ancestors of.
     */
    public void unSelectAncestors(String nodeId);

    /**
     * Selects the node plus all ancestors of the node with the given node id.
     * If no node exists with the given node id, nothing happens.
     * If the node has no ancestors only the node itself is selected.
     * @param nodeId The id of the node to select plus select all ancestors of.
     */
    public void selectAncestorsAndSelf(String nodeId);

    /**
     * Unselects the node plus all ancestors of the node with the given node id.
     * If no node exists with the given node id, nothing happens.
     * If the node has no ancestors only the node itself is unselected.
     * @param nodeId The id of the node to unselect plus unselect all ancestors of.
     */
    public void unSelectAncestorsAndSelf(String nodeId);


    /**
     * Selects the children of the node with the given node id.
     * If no node exists with the given node id, or the node has
     * no children, nothing happens.
     * @param nodeId The id of the node to select the children of.
     */
    public void selectChildren(String nodeId);

    /**
     * Unselects the children of the node with the given node id.
     * If no node exists with the given node id, or the node has
     * no children, nothing happens.
     * @param nodeId The id of the node to unselect the children of.
     */
    public void unSelectChildren(String nodeId);

    /**
     * Selects the node and the children of the node with the given node id.
     * If no node exists with the given node id nothing happens.
     * If the node has no children only the node itself is selected.
     * @param nodeId The id of the node to select the children of.
     */
    public void selectChildrenAndSelf(String nodeId);

    /**
     * Unselects the node and the children of the node with the given node id.
     * If no node exists with the given node id nothing happens.
     * If the node has no children only the node itself is unselected.
     * @param nodeId The id of the node to unselect the children of.
     */
    public void unSelectChildrenAndSelf(String nodeId);


    /**
     * Returns an ITreeNode array of all the nodes from the node with the given node id and up to the root
     * (or from the root down to the node).
     * The root node has index 0, and the node with the given node id will have index nodepath.length-1
     * (last node in the array).
     * @param nodeId The node id of the node to get the node path of.
     * @return The path as nodes from the node and up to the root (or from the root down to the node).
     */
    ITreeNode[] getNodePath(String nodeId);

    /**
     * Sets the filter to use on this tree. An ITreeFilter can filter out nodes
     * that you do not want displayed in the tree, but for practical reasons do
     * not want to remove from the tree entirely.
     * @param filter The filter to use with this tree.
     */
    public void setFilter(ITreeFilter filter);

    /**
     * Returns the filter to use on this tree. An ITreeFilter can filter out nodes
     * that you do not want displayed in the tree, but for practical reasons do
     * not want to remove from the tree entirely.
     * @return The currently used tree filter.
     */
    public ITreeFilter getFilter();


    /**
     * Sets whether or not event listeners are notified if a select / expand etc. method is
     * called with a node id of a node that is already expanded. Set notifyOnChangeOnly to true
     * if you only want the listeners notified when a node is expanded / selected etc. that was not
     * already expanded. False if all calls to expand / select should notify the listeners even if
     * the node was already expanded / selected. Default is true.
     *
     * @param notifyOnChangeOnly True to notify listeners only when tree changes status.
     *          False to notify whenever expand / selecte etc. is called regardless of whether
     * the given node was already expanded / selected.
     */
    public void setNotifyOnChangeOnly(boolean notifyOnChangeOnly);

    /**
     * Returns whether the tree is in notifyOnChangeOnly mode or not. See
     * setNotifyOnChangeOnly for more info.
     * @see   setNotifyOnChangeOnly(boolean notifyOnChangeOnly);
     * @return True if the tree is in notifyOnChangeOnly mode. False if not.
     */
    public boolean isNotifyOnChangeOnly();
  }
