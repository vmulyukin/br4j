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
package com.aplana.dbmi.module.notif;

import java.util.Collection;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;

import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.TreeAttribute;

public class ConditionalDevice implements NotificationDevice, BeanFactoryAware, InitializingBean {

	protected Log logger = LogFactory.getLog(getClass());
	
	public static final String STD_CHECKER_BEAN = "stdChecker";
	
	private PersonNotifyChecker checker;
	private NotificationDevice targetDevice;
	
	private BeanFactory beanFactory;
	private String checkField;
	private String checkFlag;
	
	public void setChecker(PersonNotifyChecker checker) {
		this.checker = checker;
	}

	public void setTargetDevice(NotificationDevice targetDevice) {
		this.targetDevice = targetDevice;
	}

	@Override
	public boolean sendNotification(Person recipient, NotificationObject object) {
		if (!checker.checkNotify(recipient, object))
			return false;
		return targetDevice.sendNotification(recipient, object);
	}

	@Override
	public boolean sendNotification(String recipient, NotificationObject object) {
		throw new NotImplementedException("Conditional message sending not implemented for external users");
	}
	
	@Override
	public int sendNotification(Collection<Person> recipients, NotificationObject object) {
		Collection<Person> targetPersons = checker.checkNotify(recipients, object);
		return targetDevice.sendNotification(targetPersons, object);
	}
	
	public void setCheckField(String checkField) {
		this.checkField = checkField;
	}
	
	public void setCheckFlag(String checkFlag) {
		this.checkFlag = checkFlag;
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (checkFlag != null) {
			PersonFlagChecker checker = (PersonFlagChecker) beanFactory.getBean(STD_CHECKER_BEAN);
			checker.setFlagId(ObjectId.predefined(ReferenceValue.class, checkFlag));
			if (checkField != null)
				checker.setFieldId(ObjectId.predefined(TreeAttribute.class, checkField));
			this.checker = checker;
		}
		if (checker == null || targetDevice == null)
			throw new IllegalStateException("Both checker and targetDevice must be set before use");
	}
}
