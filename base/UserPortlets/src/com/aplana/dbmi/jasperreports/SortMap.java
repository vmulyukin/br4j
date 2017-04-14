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

import java.util.Comparator;
import java.util.Date;
import java.util.Map;
/**
 * ����� ��� ���������� ���-�� �� ������ NegotiationListReportXMLDataSource
 * @author lyakin
 *
 */
public class SortMap implements Comparator<Map>{
	private String sField;
	SortMap(String sortField){
		sField=sortField;
	}
	public int compare(Map o1, Map o2) {
		if (sField.equals("byDate")){
			return((Date)o1.get("timestamp")).compareTo((Date)o2.get("timestamp")) ;
		}else{
			return o1.get("fact-user").toString().compareTo(o2.get("fact-user").toString()) ;
		}	
	}
}
