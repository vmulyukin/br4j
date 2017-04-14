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

public class ComissionConditionalDevice implements NotificationDevice  {

	protected Log logger = LogFactory.getLog(getClass());
	
	public static final String STD_CHECKER_BEAN = "stdChecker";
	
	private PersonNotifyChecker checker;
	private NotificationDevice targetDevice;
	
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
	public int sendNotification(Collection<Person> recipients,
			NotificationObject object) {
		int send = 0;
		for(Person p: recipients){
			if(sendNotification(p,object)){
				send ++;
			} else {
				logger.error("Unable to sent message throw " + targetDevice.getClass() + " to " + p.getId() );
			}
		}
		return send;
	}

}
