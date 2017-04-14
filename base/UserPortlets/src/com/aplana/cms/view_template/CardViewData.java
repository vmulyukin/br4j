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
package com.aplana.cms.view_template;

import com.aplana.cms.AppContext;

import java.util.List;

/**
 * Represents card attributes and linked cards data for views in right part of ARM
 * @author rmitenkov
 */
public class CardViewData {
    private long viewId;
    private List<ViewAttribute> attributes;
    private List<ColumnSortAttributes> sortColumns;

    public void setViewId(long viewId) {
        this.viewId = viewId;
    }

    public long getViewId() {
        return viewId;
    }

    public List<ViewAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<ViewAttribute> attributes) {
        this.attributes = attributes;
    }

    public List<ColumnSortAttributes> getSortColumns() {
		return sortColumns;
	}

	public void setSortColumns(List<ColumnSortAttributes> sortColumns) {
		this.sortColumns = sortColumns;
	}


	private final static String VIEW_ID_PREFIX = "view";

    public static CardViewData getBean(String viewId){
        if (containsBean(viewId)) {
            return (CardViewData) AppContext.getApplicationContext().getBean(buildViewId(viewId));
        }
        return null;
    }

    public static boolean containsBean(String viewId){
        if (AppContext.getApplicationContext() == null) return false;
        return AppContext.getApplicationContext().containsBean(buildViewId(viewId));
    }


    private static String buildViewId(String viewId) {
        return VIEW_ID_PREFIX + viewId;
    }

}
