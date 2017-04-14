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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author ppolushkin
 * TODO: �������������, �������� ���������� ��� ���������� ������� ������ ���� �� ����������� �������, ��������, @currentDate:yyyy-MM-dd@, @currentDate:dd.MM.yyyy@ 
 * ������, ��� ����� �������� ����� ������������ �� ������ ��� ����.
 *
 */
public class JasperReportFileNameFormer {
	
	//patterns for name generating
	private static final String CURRENT_DATE_PATTERN1 = "@currentDate_yyyy-MM-dd@";
	private static final String CURRENT_DATE_PATTERN2 = "@currentDate_dd.MM.yyyy@";
	
	//map for holding patterns and their substitutes
	private final Map<String, String> patterns = new HashMap<String, String>();
	
	//map for holding file type's extensions
	private final Map<String, String> extensions = new HashMap<String, String>();
	
	{
		//filing pattern's map
		patterns.put(CURRENT_DATE_PATTERN1, new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
		patterns.put(CURRENT_DATE_PATTERN2, new SimpleDateFormat("dd.MM.yyyy").format(new Date()));
		
		//filing extension's map
		extensions.put("PDF", ".pdf");
		extensions.put("DOCX", ".docx");
		extensions.put("XLS", ".xls");
		extensions.put("XLSX", ".xlsx");
	}
	
	private String exportType;

	public JasperReportFileNameFormer(String exportType) {
		if(exportType != null) {
			this.exportType = exportType;
		} else {
			this.exportType = "";
		}
	}
	
	
	public String getFileName(String mainPart) {
		if(mainPart == null) {
			return "";
		}
		String result = mainPart;
		for(Map.Entry<String, String> entry : patterns.entrySet()) {
			result = result.replaceAll(entry.getKey(), entry.getValue());
		}
		return result.concat(extensions.get(exportType));
	}
	
	public String getContentDisposition(String mainPart) {
		return "inline; filename=" + getFileName(mainPart);
	}
	
	
	public String getExportType() {
		return exportType;
	}

	public void setExportType(String exportType) {
		this.exportType = exportType;
	}
	
}
