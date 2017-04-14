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
package com.aplana.dbmi.model;

import java.util.ArrayList;
import java.util.List;

public class YearPeriodAttribute extends DatePeriodAttribute {
	
	private int yearStart = 2011;
	//�������������� ����
	private int additionalYear=0;
	private List<Integer> years = new ArrayList<Integer>(3);
	private String fromYear=null;
	private String toYear=null;
	
	
	public YearPeriodAttribute(DateAttribute dateAttribute){
		super(dateAttribute);		
	}

	public int getYearStart() {
		return yearStart;
	}

	public void setYearStart(int yearStart) {
		this.yearStart = yearStart;
	}	
	
	public List<Integer> getYears() {
		return years;
	}

	public void setYears(List<Integer> years) {
		this.years = years;
	}	

	
	public int getAdditionalYear() {
		return additionalYear;
	}

	public void setAdditionalYear(int additionalYear) {
		this.additionalYear = additionalYear;
	}

	/**
	 * ��������� ���������� ����
	 * @return
	 */
	public String getFromYear() {
		return fromYear==null?"":fromYear;
	}

	/**
	 * ��������� ���������� ���� 
	 * @param fromYear
	 */
	public void setFromYear(String fromYear) {
		this.fromYear = fromYear;
	}
	
	/**
	 * ��������� ��������� ����
	 * @return
	 */
	public String getToYear() {
		return toYear==null?"":toYear;
	}

	/**
	 * ��������� ��������� ���� 
	 * @param fromYear
	 */
	public void setToYear(String toYear) {
		this.toYear = toYear;
	}
	
	@Override
	public void clear() {		
		setValueFrom(null);		
		setValueTo(null);		
	}


	
	


	
	
	
	
	

}
