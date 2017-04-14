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

import java.io.File;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

/**
 * @author Jakob Jenkov - Copyright 2005 Jenkov Development
 */
public class FileDao2 extends DaoBase implements ITreeDao {

    File rootDirectory = null;

    public FileDao2(File rootDirectory) {
        if(!rootDirectory.exists()){
            throw new IllegalArgumentException("Directory " + rootDirectory.getAbsolutePath() + " doesn't exist");
        }
        this.rootDirectory = rootDirectory;
    }


    protected ITreeNode readRoot(ITree tree) {
        tree.setRoot(readNode(this.rootDirectory));
        return tree.getRoot();
    }

    protected Set readChildren(ITreeNode parentNode) {
        Set children = new HashSet();

        File parentDir = (File) parentNode.getObject();
        String[] files = parentDir.list();
        if(files == null) return children;
        for(int i=0; i<files.length; i++){
            File childFile = new File(parentDir.getAbsolutePath() + File.separator + files[i]);
            ITreeNode child = readNode(childFile);
            child.setParentId(parentNode.getId());
            if(!childFile.exists()) continue;
            children.add(child);
        }

        return children;
    }


    protected Set readGrandChildren(ITreeNode parentNode) {
        Set grandChildren = new HashSet();

        Iterator children = parentNode.getChildren().iterator();
        while(children.hasNext()){
            ITreeNode child = (ITreeNode) children.next();
            grandChildren.addAll(readChildren(child));
        }

        return grandChildren;
    }


    protected ITreeNode readNode(File file){
        if(!file.exists()) return null;
        String childType = file.isDirectory() ? "directory" : "file";
        ITreeNode node = new TreeNode(file.getAbsolutePath(), file.getName() , childType);
        node.setObject(file);
        return node;
    }

}
