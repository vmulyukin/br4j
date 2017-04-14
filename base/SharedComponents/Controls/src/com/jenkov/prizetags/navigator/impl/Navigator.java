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



package com.jenkov.prizetags.navigator.impl;

import com.jenkov.prizetags.navigator.itf.INavigator;
import com.jenkov.prizetags.navigator.itf.ILink;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Jakob Jenkov - Copyright 2005 Jenkov Development
 */
public class Navigator implements INavigator {

    protected List links = new ArrayList();

    public Navigator() {
    }

    public Navigator(List links) {
        this.links = links;
    }

    public Navigator(ILink[] links) {
        if( links !=  null){
            for(int i=0; i<links.length; i++){
                this.links.add(links[i]);
            }
        }
    }

    public Navigator(String[] linkIds){
        if( linkIds !=  null){
            for(int i=0; i<linkIds.length; i++){
                this.links.add(new Link(linkIds[i]));
            }
        }

    }



    public void addLink(ILink link) {
        links.add(link);
    }

    public void addLinkIfNotExists(ILink link) {
        if(!links.contains(link)){
            links.add(link);
        }
    }

    public void removeLink() {
        if(links.size() == 0) return;
        links.remove(links.size()-1);
    }

    public void removeLink(String linkId) {
        if(linkId == null) throw new NullPointerException("Cannot remove link. Link id parameter was null.");
        Iterator iterator = this.links.iterator();
        while(iterator.hasNext()){
            ILink link = (ILink) iterator.next();
            if(linkId.equals(link.getId())){
                iterator.remove();
            }
        }
    }

    public void removeLinksAfter(String linkId) {
        if(linkId == null) throw new NullPointerException("Cannot remove link. Link id parameter was null.");
        Iterator iterator = this.links.iterator();
        boolean linkFound = false;
        while(iterator.hasNext()){
            ILink link = (ILink) iterator.next();
            if(linkFound) iterator.remove();
            else if(linkId.equals(link.getId())){
                linkFound = true;
            }
        }
    }

    public void setPosition(ILink link) {
        addLinkIfNotExists(link);
        removeLinksAfter(link.getId());
    }

    public List getLinks() {
        return links;
    }
}
