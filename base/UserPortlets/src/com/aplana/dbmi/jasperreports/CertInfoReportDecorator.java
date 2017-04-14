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
package com.aplana.dbmi.jasperreports;

import java.util.ResourceBundle;

import com.aplana.dbmi.card.CertificateInfo;
import com.aplana.dbmi.model.ContextProvider;

public class CertInfoReportDecorator  {
	
	private String templateName = null;
	private CertificateInfo certificateInfo = null;
	private ResourceBundle bundle = null;
	
	public CertInfoReportDecorator(String templateName, CertificateInfo certificateInfo){
		this.certificateInfo = certificateInfo;
		this.templateName = templateName;
		init();
	}
	
	private void init(){
		bundle = ResourceBundle.getBundle("com.aplana.dbmi.archive.CardArchiveResource",ContextProvider.LOCALE_RUS);
	}

	public String getTemplateName() {
		return templateName;
	}
	
	public String getTimeSign(){
		return certificateInfo.getTime();
	}
	
	public String getOwner(){
		StringBuilder text  = new StringBuilder();
		text.append(!certificateInfo.getOwner().isEmpty()?certificateInfo.getOwner():"");
		text.append(!certificateInfo.getOrganization().isEmpty()? ",\n"+certificateInfo.getOrganization():"");
		text.append(!certificateInfo.getOrganizationUnit().isEmpty()?",\n"+certificateInfo.getOrganizationUnit():"");
		text.append(!certificateInfo.getOrganizationPost().isEmpty()?",\n"+certificateInfo.getOrganizationPost():"");
		return text.toString();
	}
	
	public String getCertificationCenter(){
		return certificateInfo.getCertificationCenter();
	}
	
	public String getValidDate(){
		StringBuilder builder = new StringBuilder();
		builder.append(bundle.getString("ds.showinfo.from")).append(" ");
		builder.append(certificateInfo.getValidFromDate()).append(" ");
		builder.append(bundle.getString("ds.showinfo.to")).append(" ");
		builder.append(certificateInfo.getValidToDate());		
		return builder.toString();
	}
	
	public String getSerialNumber(){
		return certificateInfo.getSerialNumber().toString();
	}
	
	public String getKeyState(){
		return certificateInfo.getState();
	}
	
	public String getSignState(){
		return certificateInfo.getSignState();
	}

	

}
