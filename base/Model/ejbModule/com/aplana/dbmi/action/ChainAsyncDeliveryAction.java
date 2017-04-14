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

import java.util.List;

import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.ReferenceValue;

public class ChainAsyncDeliveryAction implements Action<Void> {

	private static final long serialVersionUID = 1L;
	
	// �������� ��
	private Card card;
	// ������ ��� ��������� ��������, ������� ���������� ������� ���� ���������, ���� � �� � ������
	private ObjectId template;
	// ������� ���� ����� ��������� ��������� �������� � ��
	private ObjectId attach;
	// ����������
	private ObjectId recipient;
	// ������� �� ��������� �������� ���� ����� ������������ ����������
	private ObjectId targetAttr;
	// ������� ��������� ��������, �� ������� ��� ������ ���� ���������� � ������ ������
	private List<ObjectId> statesForSend;
	// �������, ������� ���������� ����������� �� ��������� ��������, ���� ��� � ������ ������� (statesForSend)
	private ObjectId workflowMove;
	// ������� �� ��������� �������� ��� �������� ������������ ������� (��� ��� - ����� ��������)
	private ObjectId filterListAttr;
	// ������ �������� ��� �������� ������������ ������� (filterListAttr)
	private List<ReferenceValue> filterValue;
	// ������� � �� �� ������� �����������, �� ������� ���� �������/��������� ��������� �������� (JBR_DIST_CREATE_LIST)
	private ObjectId unservedAttr;
	
	public Card getCard() {
		return card;
	}

	public void setCard(Card card) {
		this.card = card;
	}

	public ObjectId getTemplate() {
		return template;
	}

	public void setTemplate(ObjectId template) {
		this.template = template;
	}

	public ObjectId getAttach() {
		return attach;
	}

	public void setAttach(ObjectId attach) {
		this.attach = attach;
	}

	public ObjectId getRecipient() {
		return recipient;
	}

	public void setRecipient(ObjectId recipient) {
		this.recipient = recipient;
	}

	public ObjectId getTargetAttr() {
		return targetAttr;
	}

	public void setTargetAttr(ObjectId targetAttr) {
		this.targetAttr = targetAttr;
	}

	public List<ObjectId> getStatesForSend() {
		return statesForSend;
	}

	public void setStatesForSend(List<ObjectId> statesForSend) {
		this.statesForSend = statesForSend;
	}

	public ObjectId getWorkflowMove() {
		return workflowMove;
	}

	public void setWorkflowMove(ObjectId workflowMove) {
		this.workflowMove = workflowMove;
	}

	public ObjectId getFilterListAttr() {
		return filterListAttr;
	}

	public void setFilterListAttr(ObjectId filterListAttr) {
		this.filterListAttr = filterListAttr;
	}

	public List<ReferenceValue> getFilterValue() {
		return filterValue;
	}

	public void setFilterValue(List<ReferenceValue> filterValue) {
		this.filterValue = filterValue;
	}

	public ObjectId getUnservedAttr() {
		return unservedAttr;
	}

	public void setUnservedAttr(ObjectId unservedAttr) {
		this.unservedAttr = unservedAttr;
	}
	
	@Override
	public Class<?> getResultType() {
		return null;
	}

}
