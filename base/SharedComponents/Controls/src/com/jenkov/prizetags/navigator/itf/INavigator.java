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



package com.jenkov.prizetags.navigator.itf;

import java.util.List;

/**
 * Represents a navigator component which can be used to help a users navigation around the site.<br/>
 * Example:
 *
 * <br/><br/>
 * shop / products / computers / XYC 200 GhZ ATI 10000 PRO
 *
 * <br/><br/>
 * The navigator represents a hierarchy like a directory. Each entry in this hierarchy coresponds
 * to a navigational level. Each entry is also a link that can take the user back up the hierarchy
 * of your site. So, on the above example clicking on "products" will take the user back up to the
 * products main page.
 *
 * @author Jakob Jenkov - Copyright 2005 Jenkov Development
 */
public interface INavigator {

    /**
     * Adds the given link to this navigator. Please remember to
     * set the id of the link, and make sure the id is unique within
     * the navigator. If not, the links added may be lost.
     *
     * @param link The link to add to this navigator.
     */
    public void addLink(ILink link);

    /**
     * Adds the given link to this navigator if the link is not already
     * added to this navigator. Please remember to
     * set the id of the link, and make sure the id is unique within
     * the navigator. If not, the links added may be lost.
     *
     * @param link The link to add to this navigator.
     */
    public void addLinkIfNotExists(ILink link);


    /**
     * Removes the link that was added latest to this navigator.
     * It is the same principle as a pop operation on a stack.
     * The lowest link in the hierarchy is typically the link
     * added latest to the navigator. When moving up the hierarchy
     * you can just pop the last link off the navigator using this method.
     */
    public void removeLink();


    /**
     * Removes the link with the given linkId from this navigator.
     * For this to work the link ids of the links in the navigator
     * must be unique within that navigator.
     *
     * @param linkId The link id of the link to remove.
     */
    public void removeLink(String linkId);

    /**
     * Removes all links added after the link with the given node id.
     * This method is useful if you jump up the navigator hierarchy
     * to a certain point, and want to remove all links in the navigator
     * hierarchy below this point.
     *
     * @param linkId The id of the link that all links added later than
     *               are to be removed.
     *
     */
    public void removeLinksAfter(String linkId);


    /**
     * Sets the lowest position in the navigator to this link.
     * If the link is not already added it will be added. All
     * links below this link will be removed. Calling this method
     * is equivalent to calling addLinkIfNotExists(link)
     * and removeLinksAfter(link.getId()) in sequence.
     *
     * @param link The link that is to be the lowest position in this navigator.
     */
    public void setPosition(ILink link);

    /**
     * Returns the links stored inside this navigator. This method is used
     * by the navigator tag libs to iterate the links in order to display them.
     * @return A List of the links stored in this navigator.
     */
    public List getLinks();
}
