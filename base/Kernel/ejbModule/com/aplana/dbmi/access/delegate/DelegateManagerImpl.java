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

import org.apache.commons.lang.Validate;
import org.springframework.beans.factory.BeanFactory;

import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;

/**
 * ���������������� ������ � �������������� ����������.
 * processQuery ������ ���������� DelegateManager.
 * @author RAbdullin
 */
public class DelegateManagerImpl implements DelegateManager 
{

	final private BeanFactory beanFactory;

	public DelegateManagerImpl(BeanFactory factory) {
		this.beanFactory = factory;
	}


	public BeanFactory  getBeanFactory(){
		return this.beanFactory;
	}


//	public Object processQuery() throws DataException {
//		final DelegateManager result = this;
//		return result;
//	}


	public final static String BEAN_DELEGATOR = "delegateManager"; 
	DelegateManager mgrBean = null;
	public DelegateManager getDelegator() {
		if (mgrBean == null) {
			mgrBean = (DelegateManager) getBeanFactory().getBean(BEAN_DELEGATOR, DelegateManager.class);
			Validate.notNull( mgrBean, "No delegate manager bean '" + BEAN_DELEGATOR+ "'");
		}
		return mgrBean;
	}


	/**
	 * �������� ������ ������������� ���������� ���������� ������.
	 * @param bossId: ������������� ������������, ������������� ����������.
	 * @return ����� ��������� ��� null, ���� ��� �� ������ ��������.
	 * � ���� ��������� ��������� ����� .bossPersonId = bossId
	 */
	public Set /*<PermissionDelegate>*/ getDelegatesFromPerson(ObjectId personId) 
		throws DataException {
		return getDelegator().getDelegatesFromPerson(personId);
	}


	public Set getDelegatesToPerson(ObjectId personId) 
		throws DataException {
		return getDelegator().getDelegatesToPerson(personId);
	}


	public Set getDelegatedPersons(ObjectId bossId, Set permissions) 
		throws DataException {
		return getDelegator().getDelegatedPersons( bossId, permissions);
	}


	public Set getPersonsCanDoAs(ObjectId personId, Set permissions) 
		throws DataException {
		return getDelegator().getPersonsCanDoAs( personId, permissions);
	}


	public Set getAllPermissionSets() throws DataException {
		return getDelegator().getAllPermissionSets();
	}

}
