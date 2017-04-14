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
import java.io.IOException;
import java.util.Iterator;

/**
 * @author Jakob Jenkov - Copyright 2005 Jenkov Development
 */
public class FileDao implements ITreeDao {

    File rootDirectory = null;

    public FileDao(File rootDirectory) {
        if(!rootDirectory.exists()){
            throw new IllegalArgumentException("Directory " + rootDirectory.getAbsolutePath() + " doesn't exist");
        }
        this.rootDirectory = rootDirectory;
    }

    public void readRootAndChildren(ITree tree){
        ITreeNode root = null;
        try {
            root = new TreeNode(rootDirectory.getAbsolutePath(), rootDirectory.getCanonicalFile().getName(), "directory");
        } catch (IOException e) {
            throw new IllegalArgumentException("Error reading root node: "+ e.getMessage());
        }
        root.setObject(this.rootDirectory);
        readChildren(root);
        tree.setRoot(root);
    }

    public void readChildrenAndGrandchildren(ITree tree, ITreeNode node) {
        File file = (File) node.getObject();

        if(file.isDirectory()){
            readChildren(node);

            //read grandchildren
            Iterator children = node.getChildren().iterator();
            while(children.hasNext()){
                readChildren((ITreeNode) children.next());
            }
        }

    }

    protected void readChildren(ITreeNode node){
        //node.removeAllChildren();


        File file = (File) node.getObject();
        String[] files = file.list();
        if(files == null) return;
        for(int i=0; i<files.length; i++){
            File childFile = new File(file.getAbsolutePath() + File.separator + files[i]);
            if(!childFile.exists()) continue;
            String childType = childFile.isDirectory() ? "directory" : "file";
            ITreeNode child = new TreeNode(childFile.getAbsolutePath(), childFile.getName(), childType);
            child.setObject(childFile);
            node.addChild(child);
        }
    }
}
