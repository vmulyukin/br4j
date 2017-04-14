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
package com.aplana.dmsi.expansion.fsin;

import java.util.ArrayList;
import java.util.List;

public class FSIN0100ExpansionType extends com.aplana.dmsi.types.ExpansionType {

	private List<Rubric> rubric;
	private Pages pages;
	private Category category;
	private RegHistory regHistory;
	private Application application;

	public List<Rubric> getRubric() {
		if (this.rubric == null) {
			this.rubric = new ArrayList<Rubric>();
		}
		return this.rubric;
	}

	public Pages getPages() {
		return this.pages;
	}

	public void setPages(Pages pages) {
		this.pages = pages;
	}

	public Category getCategory() {
		return this.category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public RegHistory getRegHistory() {
		return this.regHistory;
	}

	public void setRegHistory(RegHistory regHistory) {
		this.regHistory = regHistory;
	}

	public Application getApplication() {
		return application;
	}

	public void setApplication(Application application) {
		this.application = application;
	}

}
