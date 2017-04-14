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
package com.aplana.dmsi.expansion;

import java.util.List;

import com.aplana.dmsi.expansion.fsin.Application;
import com.aplana.dmsi.expansion.fsin.Category;
import com.aplana.dmsi.expansion.fsin.FSIN0100ExpansionType;
import com.aplana.dmsi.expansion.fsin.Pages;
import com.aplana.dmsi.expansion.fsin.RegHistory;
import com.aplana.dmsi.expansion.fsin.Rubric;
import com.aplana.dmsi.types.ExpansionType;

public class FSIN0100ExpansionAdapter implements SpecializedExpansionAdapter {

	public ExpansionType createCommon(ExpansionType expansion) {
		FSIN0100ExpansionType fsinExpansion = (FSIN0100ExpansionType) expansion;
		ExpansionType commonExpansion = new ExpansionType();
		commonExpansion.setOrganization(fsinExpansion.getOrganization());
		commonExpansion.setExpVer(fsinExpansion.getExpVer());
		commonExpansion.getEcontact().addAll(fsinExpansion.getEcontact());
		List<Object> anyData = commonExpansion.getAny();

		for (Rubric rubric : fsinExpansion.getRubric()) {
			anyData.add(rubric);
		}
		if (fsinExpansion.getPages() != null) {
			anyData.add(fsinExpansion.getPages());
		}
		if (fsinExpansion.getCategory() != null) {
			anyData.add(fsinExpansion.getCategory());
		}
		if (fsinExpansion.getRegHistory() != null) {
			anyData.add(fsinExpansion.getRegHistory());
		}
		if (fsinExpansion.getApplication() != null) {
			anyData.add(fsinExpansion.getApplication());
		}

		return commonExpansion;
	}

	public ExpansionType createSpecialized(ExpansionType expansion) {
		FSIN0100ExpansionType fsinExpansion = new FSIN0100ExpansionType();
		fsinExpansion.setOrganization(expansion.getOrganization());
		fsinExpansion.setExpVer(expansion.getExpVer());
		fsinExpansion.getEcontact().addAll(expansion.getEcontact());
		for (Object extension : expansion.getAny()) {
			if (extension instanceof Rubric) {
				fsinExpansion.getRubric().add((Rubric) extension);
			} else if (extension instanceof Pages) {
				fsinExpansion.setPages((Pages) extension);
			} else if (extension instanceof Category) {
				fsinExpansion.setCategory((Category) extension);
			} else if (extension instanceof RegHistory) {
				fsinExpansion.setRegHistory((RegHistory) extension);
			} else if (extension instanceof Application) {
				fsinExpansion.setApplication((Application) extension);
			}
		}
		return fsinExpansion;
	}
}
