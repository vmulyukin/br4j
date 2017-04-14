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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Jakob Jenkov,  Jenkov Development
 */
public class TreeNodeStack implements Serializable{

    protected List        stackContents = new ArrayList();
//    protected ITreeFilter filter        = null;
//    protected ITree       tree          = null;

//    public TreeNodeStack(ITree tree, ITreeFilter filter) {
//        this.filter = filter == null? new BaseTreeFilter(): filter;
//        this.filter.init(tree);
//        this.tree = tree;
//    }


    public int size(){
        return this.stackContents.size();
    }

    public void push(Object node){
        this.stackContents.add(node);
    }

    public Object pop(){
        Object node = this.stackContents.get(this.stackContents.size()-1);
        this.stackContents.remove(this.stackContents.size()-1);
        return node;
    }

    public Object top(){
        return this.stackContents.get(this.stackContents.size()-1);
    }
}
