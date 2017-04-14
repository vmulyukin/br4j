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
/**
 *
 */
package com.aplana.dmsi.types.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.aplana.dmsi.types.Department;

public class DepartmentAdapter extends XmlAdapter<String, Department> {

    @Override
    public String marshal(Department department) throws Exception {
	return department == null ? null : department.getFullName();
    }

    @Override
    public Department unmarshal(String departmentName) throws Exception {
	if (departmentName == null || "".equals(departmentName)) {
	    return null;
	}
	Department dep = new Department();
	dep.setFullName(departmentName);
	return dep;
    }

}