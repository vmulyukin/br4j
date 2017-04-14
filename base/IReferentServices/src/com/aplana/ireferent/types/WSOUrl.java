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
package com.aplana.ireferent.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.aplana.ireferent.IReferentMaterialDownloadServlet;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "WSO_URL", propOrder = {
	    "url"
	})
public class WSOUrl {
	
	 @XmlElement(name = "URL", required = true, nillable = true)
	 protected String url;
	 
	public static final String URI = "/IReferentServices/IReferentMaterialDownloadServlet?" + IReferentMaterialDownloadServlet.PARAM_CARD_ID +"=";
	public static final String PDF = "&" + IReferentMaterialDownloadServlet.PARAM_PDF + "=1";

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
