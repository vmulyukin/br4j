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

/**
 * @author Jakob Jenkov - Copyright 2005 Jenkov Development
 */
public class TreeTableMapping {

    private String tableName      = null;

    private String idColumn       = null;
    private String parentIdColumn = null;
    private String treeIdColumn   = null;
    private String nameColumn     = null;
    private String typeColumn     = null;
    private String toolTipColumn  = null;

    public TreeTableMapping(String tableName, String idColumn, String parentIdColumn, String treeIdColumn,
                            String nameColumn, String typeColumn, String toolTipColumn) {
        this.tableName = tableName;
        this.idColumn = idColumn;
        this.parentIdColumn = parentIdColumn;
        this.treeIdColumn = treeIdColumn;
        this.nameColumn = nameColumn;
        this.typeColumn = typeColumn;
        this.toolTipColumn = toolTipColumn;
    }

    public TreeTableMapping(String idColumn, String parentIdColumn, String treeIdColumn, String nameColumn,
                            String typeColumn, String toolTipColumn) {
        this.idColumn = idColumn;
        this.parentIdColumn = parentIdColumn;
        this.treeIdColumn = treeIdColumn;
        this.nameColumn = nameColumn;
        this.typeColumn = typeColumn;
        this.toolTipColumn = toolTipColumn;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getIdColumn() {
        return idColumn;
    }

    public void setIdColumn(String idColumn) {
        this.idColumn = idColumn;
    }

    public String getParentIdColumn() {
        return parentIdColumn;
    }

    public void setParentIdColumn(String parentIdColumn) {
        this.parentIdColumn = parentIdColumn;
    }

    public String getTreeIdColumn() {
        return treeIdColumn;
    }

    public void setTreeIdColumn(String treeIdColumn) {
        this.treeIdColumn = treeIdColumn;
    }

    public String getNameColumn() {
        return nameColumn;
    }

    public void setNameColumn(String nameColumn) {
        this.nameColumn = nameColumn;
    }

    public String getTypeColumn() {
        return typeColumn;
    }

    public void setTypeColumn(String typeColumn) {
        this.typeColumn = typeColumn;
    }

    public String getToolTipColumn() {
        return toolTipColumn;
    }

    public void setToolTipColumn(String toolTipColumn) {
        this.toolTipColumn = toolTipColumn;
    }


}
