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

import com.aplana.dmsi.types.ExpansionType;
import com.aplana.dmsi.types.File;
import com.aplana.dmsi.types.Rubric;

public class EOSExpansionAdapter implements SpecializedExpansionAdapter {

	public ExpansionType createCommon(ExpansionType expansion) {
		com.aplana.dmsi.types.delo.ExpansionType deloExpansion = (com.aplana.dmsi.types.delo.ExpansionType) expansion;
		ExpansionType commonExpansion = new ExpansionType();
		commonExpansion.setOrganization(deloExpansion.getOrganization());
		commonExpansion.setExpVer(deloExpansion.getExpVer());
		commonExpansion.getEcontact().addAll(deloExpansion.getEcontact());
		List<Object> anyData = commonExpansion.getAny();
		for (File file : deloExpansion.getFiles()) {
			anyData.add(file);
		}
		for (Rubric rubric : deloExpansion.getRubric()) {
			anyData.add(rubric);
		}
		return commonExpansion;
	}

	public ExpansionType createSpecialized(ExpansionType expansion) {
		com.aplana.dmsi.types.delo.ExpansionType deloExpansion = new com.aplana.dmsi.types.delo.ExpansionType();
		deloExpansion.setOrganization(expansion.getOrganization());
		deloExpansion.setExpVer(expansion.getExpVer());
		deloExpansion.getEcontact().addAll(expansion.getEcontact());
		for (Object extension : expansion.getAny()) {
			if (extension instanceof File) {
				deloExpansion.getFiles().add((File) extension);
			} else if (extension instanceof Rubric) {
				deloExpansion.getRubric().add((Rubric) extension);
			}
		}
		return deloExpansion;
	}
}
