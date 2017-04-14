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
package com.aplana.dbmi.model;

public class TabBlockViewParam extends LockableObject {
	private static final long serialVersionUID = 1L;

	private ObjectId tab;
	private Object layout;
	
	public ObjectId getTab() {
		return tab;
	}
	public void setTab(ObjectId tab) {
		this.tab = tab;
	}

	public void setTab(long tab) {
		this.tab = new ObjectId(Tab.class, tab);
	}
	
	public Object getLayout() {
		return layout;
	}
	
	public void setLayout(Object layout) {
		this.layout = layout;
	}
	
	public void setId(String id) {
		super.setId(new ObjectId(TabBlockViewParam.class, id));
	}
}
