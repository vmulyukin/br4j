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

import java.util.ArrayList;

import javax.portlet.ActionRequest;
import javax.portlet.RenderRequest;

import com.aplana.dbmi.gui.ListEditor;

/**
 * Represents editor subordinate reports
 * It allows select 
 * @author skashanski
 *
 */
public class SubReportCardAttachmentListEditor extends ListEditor {

	private final ArrayList alreadyAttached =  new ArrayList();

	@Override
	protected void changeSearchDisplayParameters(RenderRequest request) {
	}

	@Override
	public void setDataProvider(ListDataProvider dataProvider) {
		this.dataProvider = dataProvider;
		this.selectedIds.clear();

		if (dataProvider != null && (dataProvider.getSelectedListData() != null)) {
			alreadyAttached.clear();
			alreadyAttached.addAll( dataProvider.getSelectedListData() );
		}
	}

	@Override
	protected void processSave(ActionRequest request) {
		ArrayList commonList = new ArrayList(alreadyAttached.size() + selectedIds.size());
		commonList.addAll(alreadyAttached);
		commonList.addAll(selectedIds);
		dataProvider.setSelectedList(commonList);
	}

}
