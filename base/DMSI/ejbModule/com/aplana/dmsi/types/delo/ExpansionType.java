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
package com.aplana.dmsi.types.delo;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;

import com.aplana.dmsi.types.File;
import com.aplana.dmsi.types.Rubric;

public class ExpansionType extends com.aplana.dmsi.types.ExpansionType {

	@XmlElement(name = "RC")
	protected String rc;
	@XmlElement(name = "File")
	protected List<File> files;
	@XmlElement(name = "Rubric")
	protected List<Rubric> rubric;
	@XmlElement(name = "AdvInfo")
	protected String advInfo;
	@XmlElement(name = "EAddress")
	protected String eAddress;

	public String getRc() {
		return this.rc;
	}

	public void setRc(String rc) {
		this.rc = rc;
	}

	public List<File> getFiles() {
		if (this.files == null) {
			this.files = new ArrayList<File>();
		}
		return this.files;
	}

	public List<Rubric> getRubric() {
		if (this.rubric == null) {
			this.rubric = new ArrayList<Rubric>();
		}
		return this.rubric;
	}

	public String getAdvInfo() {
		return this.advInfo;
	}

	public void setAdvInfo(String advInfo) {
		this.advInfo = advInfo;
	}

	public String getEAddress() {
		return this.eAddress;
	}

	public void setEAddress(String address) {
		this.eAddress = address;
	}

}
