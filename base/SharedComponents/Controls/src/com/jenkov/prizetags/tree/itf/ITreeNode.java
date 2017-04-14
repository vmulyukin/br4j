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

import java.io.Serializable;
import java.util.List;

/**
 * The ITreeNode interface represents a node in a tree.
 * @version $revision$
 * @author Jakob Jenkov
 */
public interface ITreeNode {

    /**
     * Returns the name of this node. Name is what is normally displayed by the tree tag
     * as the node text (although this is entirely up to you!).
     * @return The name of this node.
     */
    public String getName();

    /**
     * Sets the name of this node. Name is what is normally displayed by the tree tag
     * as the node text (although this is entirely up to you!).
     * @param name The name to set for this node.
     */
    public void   setName(String name);

    /**
     * Returns the id of this tree node. This id must be unique throughout the
     * tree.
     * @return The node id.
     */
    public String getId();

    /**
     * Sets the id of this tree node. This id must be unique throughout the
     * tree.
     * @param id The id to give to the node.
     */
    public void   setId(String id);

    /**
     * Returns the parent id of this tree node - the id of the parent node.
     * The parent id is rarely used directly by the programmer, or when
     * displaying the tree, but is useful
     * when connecting child nodes to parent nodes during loading in the
     * ITreeDao implementations. Sometimes the children of several nodes
     * are read simultanously, and the only way to determine what nodes
     * belong to what parents is if each child carries the id of the parent.
     *
     * @return The parent node id.
     */
    public String getParentId();

    /**
     * Sets the parent id of this tree node - the id of the parent node.
     * tree.
     * The parent id is rarely used directly by the programmer, or when
     * displaying the tree, but is useful
     * when connecting child nodes to parent nodes during loading in the
     * ITreeDao implementations. Sometimes the children of several nodes
     * are read simultanously, and the only way to determine what nodes
     * belong to what parents is if each child carries the id of the parent.
     *
     * @param id The parent id to give to the node.
     */
    public void   setParentId(String id);

    /**
     * Returns the type of this node. Type is normally used to determine what icon etc.
     * to display for this node (although this is entirely up to you!).
     * @return The type of this node.
     */
    public String getType();

    /**
     * Sets the type of this node. Type is normally used to determine what icon etc.
     * to display for this node (although this is entirely up to you!).
     * @param type The type to set for this node.
     */
    public void   setType(String type);

    /**
     * Returns the tool tip of this node. Tool tips can for instance
     * be displayed in the browser when the mouse is hovering over an image or a link.
     * @return The tool tip of this node.
     */
    public String getToolTip();

    /**
     * Sets the tool tip of this node. Tool tips can for instance
     * be displayed in the browser when the mouse is hovering over an image or a link.
     * @param text The tool tip text to set for this node.
     */
    public void   setToolTip(String text);


    /**
     * Returns the image url of this node in expanded state, if one is set.
     * Please note that the default way to associate
     * images with nodes is to use the tree:nodeMatch tag
     * which does *not* look at this node image url.
     * To use these image urls you have to actively
     * render the node image url using
     * the a standard img HTML tag + a tree:expandedImageUrl tag.
     *
     * @return The image url for this node, if any is set.
     */
    public String getExpandedImageUrl();

    /**
     * Sets the image url of this node to be used in expanded state.
     * Please note that the default way to associate
     * images with nodes is to use the tree:nodeMatch tag
     * which does *not* look at this node image url.
     * To use these image urls you have to actively
     * render the node image url using
     * the a standard img HTML tag + a tree:expandedImageUrl tag.
     *
     * @param imageUrl The url to associate with this node.
     */
    public void   setExpandedImageUrl(String imageUrl);

    /**
     * Returns the image url of this node in collapsed state, if one is set.
     * Please note that the default way to associate
     * images with nodes is to use the tree:nodeMatch tag
     * which does *not* look at this node image url.
     * To use these image urls you have to actively
     * render the node image url using
     * the a standard img HTML tag + a tree:collapsedImageUrl tag.
     *
     * @return The image url for this node, if any is set.
     */
    public String getCollapsedImageUrl();

    /**
     * Sets the image url of this node to be used in expanded state.
     * Please note that the default way to associate
     * images with nodes is to use the tree:nodeMatch tag
     * which does *not* look at this node image url.
     * To use these image urls you have to actively
     * render the node image url using
     * the a standard img HTML tag + a tree:collapsedImageUrl tag.
     *
     * @param imageUrl The url to associate with this node.
     */
    public void   setCollapsedImageUrl(String imageUrl);

    /**
     * Returns an attached object. An attached object can be handy if you want
     * to display information from this object in the tree in the JSP page
     * @return The object attached to this tree node instance using setObject(Object object).
     */
    public Serializable getObject();

    /**
     * Attaches an object to this tree node instance. An attached object can be handy if you want
     * to display information from this object in the tree in the JSP page.
     * @param object The object to attach. Object must be Serializable!
     */
    public void setObject(Serializable object);

    /**
     * Adds a child node to this node.
     * This will also set the parent node of the child,
     * by calling <code>child.setParentOnly(this);</code>
     * @param node The node to add as child.
     */
    public void   addChild(ITreeNode node);


    /**
     * Adds a child node to this node as n'th child, n being the index.
     * This will also set the parent node of the child by
     * calling <code>child.setParentOnly(this)</code>
     * @param index The index to add the child as.
     * @param node  The node to add as child.
     */
    public void   addChild(int index, ITreeNode node);

    /**
     * Adds a child node to this node.
     * This will NOT set the parent node of the child.
     * @param node The node to add as child.
     */
    public void   addChildOnly(ITreeNode node);

    /**
     * Adds a child node to this node as n'th child, n being the index.
     * This will NOT set the parent node of the child.
     * @param index The index to add the child as.
     * @param node  The node to add as child.
     */
    public void   addChildOnly(int index, ITreeNode node);

    /**
     * Removes a child node from this node.
     * This will also clear the parent node from the removed child,
     * by calling <code>child.setParentOnly(null);</code>
     * @param node The child node to remove.
     */
    public void   removeChild(ITreeNode node);


    /**
     * Removes a child node from this node.
     * This will NOT clear the parent node from the removed child.
     * @param node The child node to remove.
     */
    public void   removeChildOnly(ITreeNode node);


    /**
     * Removes all children from this node.
     * This will also remove the parent node of each of the removed nodes.
     */
    public void removeAllChildren();


    /**
     * Returns the listItems of children for this node.
     * @return A <code>List</code> of <code>ITreeNode</code> instances.
     */
    public List   getChildren();

    /**
     * Returns true if this node has any children.
     * @return True if this node has children, false if not.
     */
    public boolean hasChildren();


    /**
     * Returns the parent node of this node, meaning the node this node is a child of.
     * If the node has no parent null is returned.
     * @return The parent node of this node, or null if the node has no parent.
     */
    public ITreeNode getParent();


    /**
     * Sets the parent node of this node, meaning the node this node is a child of.
     *
     * <br/><br/>
     * The node will call the parent.addChildOnly(this) as well, if this node is
     * not already a child of the new parent.
     *
     * <br/><br/>
     * If you call this method with a value
     * that is not null, and is not the same parent as the current,
     * then the current parent, if any, will have this node removed from it. This
     * is done by calling the parent.removeChildOnly(this);
     */
    public void setParent(ITreeNode parent);


    /**
     * Sets the parent node of this node, meaning the node this node is a child of.
     * Does NOT call any of the parent.addChild() or parent.addChildOnly() methods.
     */
    public void setParentOnly(ITreeNode parent);



    public int getAncestorCount();

    public int getChildCount();

    public int getDescendantCount();

    public void copy(ITreeNode node);

    public String getValue();
    public void setValue(String value);

    public String getNameRu();

    public void setNameRu(String nameRu);

    public String getNameEn();

    public void setNameEn(String nameEn);


}
