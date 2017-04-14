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



package com.jenkov.prizetags.alternate.impl;

import com.jenkov.prizetags.alternate.itf.IAlternation;

/**
 * @author Jakob Jenkov
 *         Copyright 2004 Jenkov Development
 */
public class Alternation implements IAlternation{

    protected int      from      = 0;
    protected int      to        = 1;
    protected int      index     = 0;
    protected String[] listItems = null;

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public int getTo() {
        return to;
    }

    public void setTo(int to) {
        this.to = to;
    }

    public String[] getListItems() {
        return listItems;
    }

    public void setListItems(String[] listItems) {
        this.listItems = listItems;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getListItem() {
        if(isEmptyList())     return null;
        return listItems[index % listItems.length];
    }

    public void alternate() {
        index++;
        if(isEmptyList()){
            index %= (to + 1);
            if(index < from) {
                index = from;
            }
        } else {
            index %= listItems.length;
        }
    }

    protected boolean isEmptyList(){
        if(listItems == null)     return true;
        if(listItems.length == 0) return true;
        return false;
    }



}
