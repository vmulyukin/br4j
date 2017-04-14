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
package com.aplana.dbmi.gui;

import java.util.List;

import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ObjectId;

public interface ListDataProvider
{
	public String getListTitle();
	public String getSelectedListTitle();

	public List getColumns();

	public String getColumnTitle(String column);
	public int getColumnWidth(String column);
	
	/**
	 * Returns true if given column is "Linked"
	 */
	public boolean isColumnLinked(String column);


	public List getListData();
	// public String getValue(DataObject item, String column);
	public String getValue(ObjectId item, String column);

	// since (2010/02) List<ObjectId>
	public List /*ObjectId[]*/ getSelectedListData();
	public void setSelectedList( List /*ObjectId[]*/ data);

}
