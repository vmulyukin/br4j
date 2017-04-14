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
package com.aplana.dbmi.action;

import java.util.List;

import com.aplana.dbmi.model.ContextProvider;

public class CardsImportResult {
	private int successCount;
	private int doubletCount;
	private int errorCount;
	
	private StringBuilder doubletMsg = new StringBuilder();
	private StringBuilder successMsg = new StringBuilder();
	private StringBuilder errorMsg   = new StringBuilder(ContextProvider.getContext().getLocaleMessage("card.import.found.trouble.object"));

	public void addSuccessMessages(List<String> messages) {
		for (String message : messages) {
			successMsg.append(message);
			successCount++;
		}
	}

	public void addDoubletMessages(List<String> messages) {
		for (String message : messages) {
			doubletMsg.append(message);
			doubletCount++;
		}
	}

	public void addErrorMessages(List<String> messages) {
		for (String message : messages) {
			errorMsg.append(message);
			errorCount++;
		}
	}

	public int getSuccessCount() {
		return successCount;
	}

	public int getDubletCount() {
		return doubletCount;
	}

	public int getErrorCount() {
		return errorCount;
	}
	
	public String getDoubletMsg() {
		return doubletMsg.toString();
	}

	public String getSuccessMsg() {
		return successMsg.toString();
	}

	public String getErrorMsg() {
		return errorMsg.toString();
	}
}
