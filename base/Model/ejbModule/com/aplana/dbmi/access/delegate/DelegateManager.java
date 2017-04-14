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
package com.aplana.dbmi.access.delegate;

import java.util.Set;

import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;

/**
 * ��������� ��� ������ � ��������������� ������������.
 * @author RAbdullin
 */
public interface DelegateManager {

	/**
	 * �������� ������ ������������� ���������� ���������� ������.
	 * @param bossId: ������������� ������������, ������������� ����������.
	 * @return ����� ��������� ��� null, ���� ��� �� ������ ��������.
	 * � ���� ��������� ��������� ����� .bossPersonId = bossId
	 */
	Set /*<PermissionDelegate>*/ getDelegatesFromPerson( ObjectId personId)
		throws DataException;


	/**
	 * �������� ������ ���� ������������� ���������� ���������� �� ������� ������.
	 * @param personId: ������������� ������������, ����������� ����������.
	 * @return ����� ��������� ��� null, ���� ��� �� ������ ������������� ��� 
	 * ������� ������������. 
	 * � ���� ��������� ��������� ����� .toPersonId == personId.
	 */
	Set /*<PermissionDelegate>*/ getDelegatesToPerson( ObjectId personId)
		throws DataException;


	/**
	 * �������� ������ �����, ������� ��������� bossId ����������� ���������� 
	 * ����� ����� ����������.
	 * @param bossId: ������������� ������������, ������������� ����������.
	 * @param permissions: ����� ����������, ������� ������ ����� ���������
	 * (��� ������������ ����������� ���), �������� null = ��� (�����) ����������, 
	 * �.�. ����� ������� ������ ����, ��� ����������� ���-���� ������� 
	 * ������������.
	 * @return ����� ��������� ��� null, ���� ��� �� ������.
	 */
	Set /*<PersonId>*/ getDelegatedPersons( ObjectId bossId, Set /*<CardAccess>*/ permissions)
		throws DataException;


	/**
	 * ��������� ������ ������, �� ����� ������� ��������� personId �����
	 * ��������� � ������ ������ ��� �������� �� ��������� ������ (��������, 
	 * ��� ��������� ������� ��������).
	 * @param personId
	 * @param permissions
	 * @return
	 */
	Set /*<PersonId>*/ getPersonsCanDoAs( ObjectId personId, Set /*<CardAccess>*/ permissions)
	 	throws DataException;


	/**
	 * �������� ������ ���� ��������� ������������ �����.
	 * @return
	 * @throws DataException 
	 */
	Set /* PermissionSet */ getAllPermissionSets() throws DataException;


	// void saveDelegates( List /*<PermissionDelegate>*/ delegates) throws DataException;
}
