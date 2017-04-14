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

import com.jenkov.prizetags.tree.itf.ITreeDao;
import com.jenkov.prizetags.tree.itf.ITree;
import com.jenkov.prizetags.tree.itf.ITreeNode;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * @author Jakob Jenkov - Copyright 2004-2006 Jenkov Development
 */
public class DatabaseDao implements ITreeDao {

    protected DataSource       dataSource = null;
    protected TreeTableMapping mapping    = null;

    protected String readRootSql          = null;
    protected String readChildrenSql      = null;
    protected String readGrandChildrenSql = null;

    protected Long   rootId               = null;

    public DatabaseDao(DataSource dataSource, TreeTableMapping mapping, long rootId) {
        this.dataSource = dataSource;
        this.mapping    = mapping;
        this.rootId     = new Long(rootId);

        this.readRootSql            = "select * from " + mapping.getTableName()
                                    + " where " + mapping.getIdColumn() + " = ? ";
        this.readChildrenSql        = "select * from " + mapping.getTableName()
                                    + " where " + mapping.getParentIdColumn() + " = ? "
                                    + " and   " + mapping.getParentIdColumn() + " <> " + mapping.getIdColumn();
        this.readGrandChildrenSql   = "select * from " + mapping.getTableName()
                                    + " where " + mapping.getParentIdColumn() + " in ";
    }

    public DatabaseDao(DataSource dataSource, TreeTableMapping mapping) {
        this.dataSource = dataSource;
        this.mapping    = mapping;
        this.readRootSql            = "select * from " + mapping.getTableName()
                                    + " where " + mapping.getParentIdColumn() + " is null "
                                    + " or "    + mapping.getParentIdColumn() + " = " + mapping.getIdColumn();
        this.readChildrenSql        = "select * from " + mapping.getTableName()
                                    + " where " + mapping.getParentIdColumn() + " = ? "
                                    + " and   " + mapping.getParentIdColumn() + " <> " + mapping.getIdColumn();
        this.readGrandChildrenSql   = "select * from " + mapping.getTableName()
                                    + " where " + mapping.getParentIdColumn() + " in ";
    }

    public void readRootAndChildren(ITree tree) {
        Connection        connection = null;
        PreparedStatement statement  = null;
        ResultSet         result     = null;
        ITreeNode         rootNode   = null;
        try{
            connection = this.dataSource.getConnection();
            statement  = connection.prepareStatement(this.readRootSql);
            if(this.rootId != null){
                statement.setLong(1, this.rootId.longValue());
            }

            result = statement.executeQuery();
            if(result.next()){
                rootNode = new TreeNode();
                readNode(rootNode, result);
                tree.setRoot(rootNode);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(result);
            close(statement);
            close(connection);
        }

        readChildren(rootNode);
    }


    public void readChildrenAndGrandchildren(ITree tree, ITreeNode parentNode) {
        if(parentNode == null) return;

        readChildren(parentNode);
        StringBuffer buffer = generateIdString(parentNode);

        Map parentMap         = new HashMap();
        Map parentChildSetMap = new HashMap();
        Iterator iterator = parentNode.getChildren().iterator();
        while(iterator.hasNext()){
            ITreeNode parent = (ITreeNode) iterator.next();
            parentMap.put(parent.getId(), parent);
            parentChildSetMap.put(parent.getId(), new HashSet());
        }



        String sql = this.readGrandChildrenSql + buffer.toString();

        Connection        connection = null;
        PreparedStatement statement  = null;
        ResultSet         result     = null;
        try {
            connection = this.dataSource.getConnection();
            statement  = connection.prepareStatement(sql);
            result     = statement.executeQuery();
            while(result.next()){
                ITreeNode grandChild = new TreeNode();
                readNode(grandChild, result);
                Set childSet = (Set) parentChildSetMap.get(result.getString(this.mapping.getParentIdColumn()));
                childSet.add(grandChild);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(result);
            close(statement);
            close(connection);
        }

        Iterator parentIdIterator = parentMap.keySet().iterator();
        while(parentIdIterator.hasNext()){
            String    id       = (String   ) parentIdIterator.next();
            ITreeNode parent   = (ITreeNode) parentMap.get        (id);
            Set       childSet = (Set      ) parentChildSetMap.get(id);
            updateNodeChildren(parent, childSet);
            childSet.clear();
        }
        parentMap.clear();
        parentChildSetMap.clear();
    }


    private void readChildren(ITreeNode parentNode){
        //parentNode.removeAllChildren();

        Connection        connection = null;
        PreparedStatement statement  = null;
        ResultSet         result     = null;
        Set               nodesFromDatabase = new HashSet();
        try{
            connection = this.dataSource.getConnection();
            statement  = connection.prepareStatement(this.readChildrenSql);
            statement.setLong(1, Long.parseLong(parentNode.getId()));

            result = statement.executeQuery();
            while(result.next()){
                ITreeNode node = new TreeNode();
                readNode(node, result);
                nodesFromDatabase.add(node);
            }

            updateNodeChildren(parentNode, nodesFromDatabase);
            nodesFromDatabase.clear();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(result);
            close(statement);
            close(connection);
        }
    }

    private void updateNodeChildren(ITreeNode parentNode, Set nodesFromDatabase) {
        Set childset = copyCurrentChildren(parentNode);

        //add nodes that exist in DB, but not in currently loaded child set.
        Iterator childrenFromDatabaseIterator = nodesFromDatabase.iterator();
        while(childrenFromDatabaseIterator.hasNext()){
            ITreeNode nodeFromDatabase = (ITreeNode) childrenFromDatabaseIterator.next();
            if(!childset.contains(nodeFromDatabase)){
                parentNode.addChild(nodeFromDatabase);
            }
        }

        //remove nodes that exist in currently loaded child set, but doesn't exist in the database (anymore).
        Iterator childIterator = childset.iterator();
        while(childIterator.hasNext()){
            ITreeNode currentChild = (ITreeNode) childIterator.next();
            if(!nodesFromDatabase.contains(currentChild)){
                parentNode.removeChild(currentChild);
            }
        }
    }

    private Set copyCurrentChildren(ITreeNode parentNode) {
        Set  childset = new HashSet();
        Iterator childIterator = parentNode.getChildren().iterator();
        while(childIterator.hasNext()){
            childset.add(childIterator.next());
        }
        return childset;
    }

    private void readNode(ITreeNode rootNode, ResultSet result) throws SQLException {
        rootNode.setId  (result.getString(this.mapping.getIdColumn()));
        rootNode.setName(result.getString(this.mapping.getNameColumn()));
        rootNode.setType(result.getString(this.mapping.getTypeColumn()));
        if(this.mapping.getToolTipColumn() != null){
            rootNode.setToolTip(result.getString(this.mapping.getToolTipColumn()));
        }
    }

    private StringBuffer generateIdString(ITreeNode parentNode) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(" (");
        Iterator iterator = parentNode.getChildren().iterator();
        while(iterator.hasNext()){
            ITreeNode child = (ITreeNode) iterator.next();
            buffer.append(child.getId());
            if(iterator.hasNext()){
                buffer.append(", ");
            }
        }
        buffer.append(") ");
        return buffer;
    }

    private void close(Connection connection) {
        try {
            if(connection != null) connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void close(PreparedStatement statement) {
        try {
            if(statement != null) statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void close(ResultSet result) {
        try {
            if(result != null) result.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
