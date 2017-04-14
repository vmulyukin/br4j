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
package com.aplana.dbmi.universalportlet;

import java.text.MessageFormat;

public class ColumnDescription {
    private String name;
    private String displayName;
    private boolean sortable;
    private boolean hidden;
    private MessageFormat link;
    private String linkColumn;
    private int linkColumnIndex;
    private int width;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public boolean isSortable() {
        return sortable;
    }

    public void setSortable(boolean sortable) {
        this.sortable = sortable;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public MessageFormat getLink() {
        return link;
    }

    public void setLink(MessageFormat link) {
        this.link = link;
    }

    public String getLinkColumn() {
        return linkColumn;
    }

    public void setLinkColumn(String linkColumn) {
        this.linkColumn = linkColumn;
    }

    public int getLinkColumnIndex() {
        return linkColumnIndex;
    }

    public void setLinkColumnIndex(int linkColumnIndex) {
        this.linkColumnIndex = linkColumnIndex;
    }

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}
}
