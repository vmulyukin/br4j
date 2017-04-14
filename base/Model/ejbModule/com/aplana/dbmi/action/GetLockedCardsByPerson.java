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

import com.aplana.dbmi.model.LockableObject;
import com.aplana.dbmi.model.ObjectId;

/**
 * ���� ������������ ��� �������� �������� �������� ��� ����, ����� ������� 
 * ��������� ��������������� �������� � ��� �� ������, ��� � �������� ��������.
 * ���������� ������ ���������� ��������������� ��������.
 * @author desu
 *
 */
public class GetLockedCardsByPerson implements ObjectAction {

	private static final long serialVersionUID = 1L;

	public GetLockedCardsByPerson(ObjectId id) {
		this.cardId = id;
	}
	
	public GetLockedCardsByPerson(LockableObject obj) {
		this.cardId = obj.getId();
	}
	
	private ObjectId cardId;
	
	public ObjectId getId(){
		return cardId;
	}
	
	public void setId(ObjectId id) {
		this.cardId = id;
	}
	
	@Override
	public Class getResultType() {
		return List.class;
	}

	@Override
	public ObjectId getObjectId() {
		return getId();
	}

}
