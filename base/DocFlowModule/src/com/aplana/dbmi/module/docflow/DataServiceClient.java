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
package com.aplana.dbmi.module.docflow;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.DataServiceBean;
import com.aplana.dbmi.service.impl.Database;
import com.aplana.dbmi.service.impl.QueryFactory;
import com.aplana.dbmi.service.impl.UserData;

public class DataServiceClient implements BeanFactoryAware {
	protected Log logger = LogFactory.getLog(getClass());
	
	private QueryFactory queryFactory;
	private Database database;
	private UserData user;

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.queryFactory = (QueryFactory) beanFactory.getBean(DataServiceBean.BEAN_QUERY_FACTORY);
		this.database = (Database) beanFactory.getBean(DataServiceBean.BEAN_DATABASE);
	}

	protected QueryFactory getQueryFactory() {
		return queryFactory;
	}

	protected Database getDatabase() {
		return database;
	}

	protected UserData getSystemUser() throws DataException {
		if (user == null) {
			user = new UserData();
			user.setPerson(database.resolveUser(Database.SYSTEM_USER));
			user.setAddress("internal");
		}
		return user;
	}
}
