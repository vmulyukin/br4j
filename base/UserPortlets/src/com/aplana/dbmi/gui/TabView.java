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

import com.aplana.dbmi.model.TabViewParam;

public class TabView extends TabViewParam {
	private static final long serialVersionUID = 1L;
	private AttributeContainer container;
	
	public TabView(){
		super();
	}
	
	public TabView(TabViewParam tvp){
		super();
		setId(tvp.getId());
		setLocker(tvp.getLocker());
		setLockTime(tvp.getLockTime());
		setTemplate(tvp.getTemplate());
		setOrder(tvp.getOrder());
		setNameRu(tvp.getNameRu());
		setNameEn(tvp.getNameEn());
	}
	
	public AttributeContainer getContainer() {
		return container;
	}
	public void setContainer(AttributeContainer container) {
		this.container = container;
	}
}
