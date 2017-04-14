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

import com.jenkov.prizetags.tree.itf.ITreeNode;

import java.util.Comparator;
import java.util.Collections;
import java.util.Iterator;

/**
 * @author Jakob Jenkov - Copyright 2005 Jenkov Development
 */
public class TreeSorter {

    private static final int ASCENDING  = 1;
    private static final int DESCENDING = -1;

    public static void sort(ITreeNode node, Comparator comparator){
        Collections.sort(node.getChildren(), comparator);
    }

    public static void sortRecursive(ITreeNode node, Comparator comparator){
        sort(node, comparator);
        Iterator iterator = node.getChildren().iterator();
        while(iterator.hasNext()){
            ITreeNode child = (ITreeNode) iterator.next();
            sortRecursive(child, comparator);
        }
    }

    public static void sortById(ITreeNode node){
        sort(node, createIdComparator(ASCENDING));
    }

    public static void sortByIdDescending(ITreeNode node){
        sort(node, createIdComparator(DESCENDING));
    }

    public static void sortRecursiveById(ITreeNode node){
        sortRecursive(node, createIdComparator(ASCENDING));
    }

    public static void sortRecursiveByIdDescending(ITreeNode node){
        sortRecursive(node, createIdComparator(DESCENDING));
    }

    public static void sortByName(ITreeNode node){
        sort(node, createNameComparator(ASCENDING));
    }

    public static void sortByNameDescending(ITreeNode node){
        sort(node, createNameComparator(DESCENDING));
    }

    public static void sortRecursiveByName(ITreeNode node){
        sortRecursive(node, createNameComparator(ASCENDING));
    }

    public static void sortRecursiveByNameDescending(ITreeNode node){
        sortRecursive(node, createNameComparator(DESCENDING));
    }

    public static void sortByType(ITreeNode node){
        sort(node, createTypeComparator(ASCENDING));
    }

    public static void sortByTypeDescending(ITreeNode node){
        sort(node, createTypeComparator(DESCENDING));
    }

    public static void sortRecursiveByType(ITreeNode node){
        sortRecursive(node, createTypeComparator(ASCENDING));
    }

    public static void sortRecursiveByTypeDescending(ITreeNode node){
        sortRecursive(node, createTypeComparator(DESCENDING));
    }


    private static Comparator createIdComparator(final int sortDirection) {
        return new Comparator(){
            public int compare(Object o1, Object o2) {
                return sortDirection * ((ITreeNode)o1).getId().compareTo(((ITreeNode)o2).getId());
            }
        };
    }

    private static Comparator createNameComparator(final int sortDirection) {
        return new Comparator(){
            public int compare(Object o1, Object o2) {
                return sortDirection * ((ITreeNode)o1).getName().compareTo(((ITreeNode)o2).getName());
            }
        };
    }

    private static Comparator createTypeComparator(final int sortDirection) {
        return new Comparator(){
            public int compare(Object o1, Object o2) {
                return sortDirection * ((ITreeNode)o1).getType().compareTo(((ITreeNode)o2).getType());
            }
        };
    }

}
