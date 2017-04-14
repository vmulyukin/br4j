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
package com.aplana.dbmi.card.actionhandler.multicard;

import java.util.List;

import com.aplana.dbmi.card.CardPortletSessionBean;
import com.aplana.dbmi.model.Attribute;

/**
 * ��������� ��������� �����������
 * ������������ � {@link AddTemplateGeneratedCardsActionHandler}
 * @author dstarostin
 *
 */
public interface MappingProcedure {

	/**
	 * ������������� ���������. ���������� ���� ��� �� �������� ������� ��������.
	 * ����� ���������� ����� ������, ��������� ����������� ������ ���� ������ ������������ ������ {@link #execute(Attribute)}
	 * ��� ����������� ������ �����������,
	 * ��� ������������� checked exceptions ������ ����������� �� � {@link MappingUserException}
	 * @param templateCard ��������-������
	 * @param args ������ ��������� ���������� �� �������. ��. {@link AddTemplateGeneratedCardsActionHandler#PARAM_MAPPING}.
	 * @throws MappingUserException ���� ������ ������ ��������� checked exception
	 */
	void init(CardPortletSessionBean session, String[] args) throws MappingUserException, MappingSystemException;
	
	/**
	 * ����� ���������. ���������� ��� ��������� ������ �������� ��������.
	 * ������ �������� �������� ����������� ��� ��������. 
	 * ��� ������������� unchecked exceptions ������ ����������� �� ������,
	 * ��� ������������� checked exceptions ������ ����������� �� � {@link MappingUserException}
	 * @param attr ������� ������� ����� ��������
	 * @return true ���� ��������� �����������, false ���� ������ ������ ���
	 * @throws MappingUserException ���� �� ������� ���������� ���������
	 */
	boolean execute(List<Attribute> attr) throws MappingSystemException;
	
}
