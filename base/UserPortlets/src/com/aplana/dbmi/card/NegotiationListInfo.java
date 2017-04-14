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
package com.aplana.dbmi.card;

public class NegotiationListInfo {
	
	
	private String listName;
	private String listDesc;
	private String listId;
	private String listTemplate;
	private String listDocType;
	private String listUrgancy;
	private String listAuthor;
	
	public String getListName(){return listName;}
	public void setListName(String negotiationListName){listName=negotiationListName;}
	public String getListDesc(){return listDesc;}
	public void setListDesc(String negotiationListDesc){listDesc=negotiationListDesc;}
	
	public String getListTemplateType(){return listTemplate;}
	public void setListTemplateType(String negotiationListTemplateType){listTemplate=negotiationListTemplateType;}
	public String getListDocType(){return listDocType;}
	public void setListDocType(String negotiationListDocumentType){listDocType=negotiationListDocumentType;}
	public String getListUrgancyCategory(){return listUrgancy;}
	public void setListUrgancyCategory(String negotiationListUrgancyCategory){listUrgancy=negotiationListUrgancyCategory;}
	public String getListId(){return listId;}
	public void setListId(String negotiationListId){listId=negotiationListId;}
	public String getListAuthor(){return listAuthor;}
	public void setListAuthor(String negotiationListAuthor){listAuthor=negotiationListAuthor;}
}
