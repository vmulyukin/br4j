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
package com.aplana.dbmi.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;


public class SearchByTemplatePeriodNameAction implements Action {
	
	private static final long serialVersionUID = -9118915950371293664L;
	
	private Date startPeriod = null;
	
	private Date endPeriod = null;
	
	private long[] permissionTypes = null;
	
	private int page = 1;
	
	private int pageSize = 50;
	
	private String name = null;
	
	private boolean checkPermission = false; 
	
	protected Collection templates = new ArrayList();
	
	private List<String> regNums = new ArrayList<String>();
	private boolean regNumStrictSearch = true;
	
	
	private List<String> outNumbers = new ArrayList<String>();
	private boolean outNumbersStrictSearch = true;
	
	private Collection<Integer> projectNumbers;
	private boolean projectNumbersStrictSearch = true;
	
	private Card card = null;
	
	private String OGAuthor = null;
	private boolean OGAuthorStrictSearch = true;
	
	private Collection<ObjectId> ignoredCards;
	
	/*
	 * ������������� ������ ��� ������� ��������� ���������� ��� ������ �� ����������.
	 * ����� ����� �������� ��� ������ ��������, ���� �����������.
	 */
	private SearchRelatedDocsForReport filterAction = null;

	public Class getResultType() {

		return List.class;
	}

	public boolean isCheckPermission(){
		return checkPermission;
	}
	
	public void setCheckPermission(boolean doCheckPermission){
		checkPermission = doCheckPermission;
	}
	
	public Collection getTemplates() {
		return templates;
	}


	public void setTemplates(Collection templates) {
		this.templates = templates;
	}
	

	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public Date getStartPeriod() {
		return startPeriod;
	}


	public void setStartPeriod(Date startPeriod) {
		this.startPeriod = startPeriod;
	}


	public Date getEndPeriod() {
		return endPeriod;
	}


	public void setEndPeriod(Date endPeriod) {
		this.endPeriod = endPeriod;
	}


	public long[] getPermissionTypes() {
		return permissionTypes;
	}


	public void setPermissionTypes(long[] permissionTypes) {
		this.permissionTypes = permissionTypes;
	}


	public int getPage() {
		return page;
	}


	public void setPage(int page) {
		this.page = page;
	}


	public int getPageSize() {
		return pageSize;
	}


	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
	
	
	public void setCard(Card card){
		this.card = card;
	}
	
	public Card getCard(){		
		return card;
	}
	
	public void setRegNums(List<String> regNums){
		this.regNums = regNums;				
	}
	
	public List<String> getRegNums(){
		return regNums;
	}
	
	public SearchRelatedDocsForReport getFilterAction(){
		return filterAction;
	}
	
	public void setFilterAction(SearchRelatedDocsForReport filterAction){
		this.filterAction = filterAction;
	}
	
	public Collection<Integer> getProjectNumbers(){
		return projectNumbers;
	}
	
	public void setProjectNumbers(Collection<Integer> projectNumbers){
		this.projectNumbers = projectNumbers;
	}
	
	public List<String> getOutNumbers() {
		return outNumbers;
	}
	
	public void setOutNumbers(List<String> outNumbers) {
		this.outNumbers = outNumbers;
	}
	
	public String getOGAuthor() {
		return OGAuthor;
	}
	
	public void setOGAuthor(String oGAuthor) {
		OGAuthor = oGAuthor;
	}

	public boolean isRegNumStrictSearch() {
		return regNumStrictSearch;
	}

	public void setRegNumStrictSearch(boolean regNumStrictSearch) {
		this.regNumStrictSearch = regNumStrictSearch;
	}

	public boolean isOutNumbersStrictSearch() {
		return outNumbersStrictSearch;
	}

	public void setOutNumbersStrictSearch(boolean outNumbersSrictSearch) {
		this.outNumbersStrictSearch = outNumbersSrictSearch;
	}

	public boolean isProjectNumbersStrictSearch() {
		return projectNumbersStrictSearch;
	}

	public void setProjectNumbersStrictSearch(boolean projectNumbersStrictSearch) {
		this.projectNumbersStrictSearch = projectNumbersStrictSearch;
	}

	public boolean isOGAuthorStrictSearch() {
		return OGAuthorStrictSearch;
	}

	public void setOGAuthorStrictSearch(boolean oGAuthorStrictSearch) {
		OGAuthorStrictSearch = oGAuthorStrictSearch;
	}

	public Collection<ObjectId> getIgnoredCards() {
		return ignoredCards;
	}

	public void setIgnoredCards(Collection<ObjectId> ignoredCards) {
		this.ignoredCards = ignoredCards;
	}
}
