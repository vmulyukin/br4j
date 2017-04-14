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

import java.util.Collection;

import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.TypedCardLinkAttribute;

public class SearchRelatedDocsForReport implements Action{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public final static ObjectId LINKED_DOCS_ID = ObjectId.predefined(TypedCardLinkAttribute.class, "jbr.report.result");
	public final static ObjectId DOC_DOCS_ID = ObjectId.predefined(TypedCardLinkAttribute.class, "jbr.relatdocs");
	
	public final static ObjectId REPORT_ID = ObjectId.predefined(CardLinkAttribute.class, "jbr.report.int.parent");
	public final static ObjectId DOC_ID = ObjectId.predefined(CardLinkAttribute.class, "jbr.report.int.document");
	public final static ObjectId TOP_RESOLUTION_ID = ObjectId.predefined(BackLinkAttribute.class, "jbr.resolutions");
	public final static ObjectId SUB_RESOLUTION_ID = ObjectId.predefined(BackLinkAttribute.class, "jbr.linkedResolutions");
	
	public final static ObjectId EXEC_RESP_ID = ObjectId.predefined(PersonAttribute.class, "jbr.AssignmentExecutor");
	public final static ObjectId EXEC_REPORT_ID = ObjectId.predefined(PersonAttribute.class, "jbr.report.int.executor");
	
	public enum Scope{
		SUBREPORTS,		//���������, ������� ������ �������������� ��� �������� ���������, ���� ������� ����������� - �������������
		WHOLE_DOCUMENT	//������ ��� ���� ��������� ��������� (����� �������� ������) + ��������� ��������� ���������
	}
	
	private Scope scope;
	//�������� ������
	private Card card;

	@SuppressWarnings("rawtypes")
	public Class getResultType() {
		return Collection.class;
	}
	
	public Scope getScope(){
		return scope;
	}
	
	public void setScope(Scope scope){
		this.scope = scope;
	}
	
	public Card getCard(){
		return card;
	}
	
	public void setCard(Card card){
		this.card = card;
	}
}
