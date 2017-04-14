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
package com.aplana.dbmi.service.impl;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.jdbc.core.JdbcTemplate;

import com.aplana.dbmi.action.Action;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;

/**
 * Base class for implementing classes defining access permission 
 * checks on {@link QueryBase query} objects.<br> 
 * Every {@link QueryBase query} object could have assigned.<br>
 * Main permission check logic should be implemented in {@link #checkAccess()} method
 * of descendant classes. 
 * @see DatabaseBean#executeQuery(UserData, QueryBase)
 */
abstract public class AccessCheckerBase implements BeanFactoryAware, Serializable 
{
	private static final long serialVersionUID = 2l;
	protected Log logger = LogFactory.getLog(getClass());
	private transient BeanFactory factory;
	private UserData user;
	private transient JdbcTemplate jdbc;
	private DataObject object;
	private Action action;
	
	/**
	 * Gets JdbcTemplate used to perform database query
	 * @return JdbcTemplate used to perform database query
	 */
	public JdbcTemplate getJdbcTemplate() {
		return jdbc;
	}

	/**
	 * Sets JdbcTemplate to be used to perform database query
	 * @param jdbc JdbcTemplate to be used to perform database query
	 */
	public void setJdbcTemplate(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
	}

	/**
	 * Gets information about user whose access permissions should be checked
	 * @return information about user
	 */
	public UserData getUser() {
		return user;
	}

	/**
	 * Gets {@link Action} object being processed by query.
	 * Should be used for {@link ActionQueryBase} and its descendants only.
	 * @return {@link Action} object being processed by query.
	 */
	@SuppressWarnings("unchecked")
	public <T extends Action> T getAction() {
		return (T)action;
	}

	/**
	 * Sets {@link Action} object being processed by query.
	 * Should be used for {@link ActionQueryBase} and its descendants only.
	 * @param action {@link Action} object being processed by query.
	 */
	public void setAction(Action action) {
		this.action = action;
	}

	/**
	 * Sets information about user who performs query.
	 * @param user information about user who performs query.
	 */
	public void setUser(UserData user) {
		this.user = user;
	}
	
	/**
	 * Gets {@link DataObject}, being processed by query.
	 * Could be used in {@link SaveQueryBase} and its descendants only.
	 * @return object, being processed by query.
	 */
	public DataObject getObject() {
		return object;
	}

	/**
	 * Sets {@link DataObject}, being processed by query.
	 * @param object {@link DataObject}, being processed by query.
	 */
	public void setObject(DataObject object) {
		this.object = object;
	}

	/**
	 * Creates empty {@link DataObject} having only identifier field implemented,
	 * and sets it as a value of {@link #setObject(DataObject) object} property.
	 * @param id identifier of {@link DataObject}, being processed by query. 
	 */
	public void setObject(ObjectId id) {
		this.object = id == null ? null : DataObject.createFromId(id);
	}

	/**
	 * Main method. Descendant classes should implement
	 * access check logic here.
	 * @return true if access check passed, false otherwise
	 * @throws DataException if any error occurs
	 */
	abstract public boolean checkAccess() throws DataException;
	
	/**
	 * This method should be overridden by descendants if
	 * they want to make any filtering of query's result.
	 * It's called only when query has non-null result.
	 * @param result Result object. It's type depends on query.
	 * Can't be null. Method is free to make any changes in this object.
	 * @throws DataException if any error occurs
	 */
	public void filterResult(Object result) throws DataException {
		// Do nothing
	}
	
	/**
	 * Utility method to be used in descendant classes.
	 * Checks if given user have role with given identifier. 
	 * @param role string identifier of {@link com.aplana.dbmi.model.SystemRole}
	 * @return true if user have given role, false otherwise 
	 */
	public boolean hasRole(String role)
	{
		long roles = getJdbcTemplate().queryForLong(
				"SELECT COUNT(*) FROM person_role WHERE person_id=? AND role_code=?",
				new Object[] { getUser().getPerson().getId().getId(), role });
		return roles > 0;
	}

	protected BeanFactory getBeanFactory() {
		return factory;
	}

	/**
	 * Sets BeanFactory used by this Query object.
	 */
	public void setBeanFactory(BeanFactory factory) {
		this.factory = factory;
	}
}
