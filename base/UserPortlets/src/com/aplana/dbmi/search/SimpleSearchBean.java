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
package com.aplana.dbmi.search;

import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;

import com.aplana.dbmi.model.ContextProvider;
import com.aplana.dbmi.model.Template;

public class SimpleSearchBean {
	private String dateName;
	private Date dateFrom;
	private Date dateTo;
	private String dateFromStr;
	private String dateToStr;
	private Collection<Template> templates;
	private Object templId;
	private String searchQuery;
	
	public Date getDateFrom() {
		return dateFrom;
	}
	
	public void setDateFrom(Date dateFrom) {
		this.dateFrom = dateFrom;
		if (dateFrom==null)
			dateFromStr = "";
		else{
//			dateFromStr = String.format("%1$tY-%1$tm-%1$td", new Object[]{dateFrom});	
			dateFromStr = String.format("%1$td.%1$tm.%1$tY", new Object[]{dateFrom});	
		}
	}
	
	public Date getDateTo() {
		return dateTo;
	}
	
	public void setDateTo(Date dateTo) {
		this.dateTo = dateTo;
		if (dateTo==null)
			dateToStr = "";
		else{
			//dateToStr = String.format("%1$tY-%1$tm-%1$td", new Object[]{dateTo});	
			dateToStr = String.format("%1$td.%1$tm.%1$tY", new Object[]{dateTo});	
		}
	}
	
	public Collection<Template> getTemplates() {
		return templates;
	}
	
	public void setTemplates(Collection<Template> attributes) {
		this.templates = attributes;
	}
	
	public String getSearchQuery() {
		// ������� Locale.getDefault() �� ContextProvider.getContext().getLocale(), �.�. �� 94-� ������ ������� ������-�� ��������� � ���������� ������ (� ����� � ������ ������ � ��� ContextProvider.getContext().getLocale())
		ResourceBundle bundle = ResourceBundle.getBundle("nls.search", ContextProvider.getContext().getLocale());
//		System.out.print("ContextProvider.getContext().getLocale() = "+ContextProvider.getContext().getLocale().getDisplayLanguage());
//		System.out.print("Locale.getDefault() = "+Locale.getDefault().getDisplayLanguage());
		return (searchQuery != null && searchQuery.length() > 0) ? searchQuery : bundle.getString("search.document");
	}
	
	public void setSearchQuery(String searchQuery) {
		this.searchQuery = searchQuery;
	}
	
	public String getDateName() {
		return dateName;
	}
	
	public void setDateName(String dateName) {
		this.dateName = dateName;
	}
	
	public Object getTemplId() {
		return templId;
	}
	
	public void setTemplId(Object templId) {
		this.templId = templId;
	}
	
	public String getDateFromStr() {
		return dateFromStr;
	}
	
	public void setDateFromStr(String dateFromStr) {
		this.dateFromStr = dateFromStr;
	}
	
	public String getDateToStr() {
		return dateToStr;
	}
	
	public void setDateToStr(String dateToStr) {
		this.dateToStr = dateToStr;
	}
}
