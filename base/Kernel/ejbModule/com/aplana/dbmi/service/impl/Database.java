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

import java.util.Date;

import com.aplana.dbmi.PortalUser;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.service.DataException;

/**
 * Interface for spring transaction proxy created for {@link DatabaseBean} object.
 */
public interface Database
{
	/**
	 * Login used by system user (person_id = 0)
	 */
	public static final String SYSTEM_USER = "__system__";
	
	/**
	 * Fetches {@link Person} object with given login from database.<br>
	 * Only active users could be fetched by this method 
	 * (only exception is {@link com.aplana.dbmi.service.SystemUser} who is always inactive).
	 * @return {@link Person} object representing user with given login or null
	 * if there is no active users with given login, or if several users were found.
	 * @throws DataException if any error occurs 
	 */	
	public Person resolveUser(String name) throws DataException;
	
	/**
	 * Executes given query object.
	 * Will perform all access checks, pre- and post-processing if they specified
	 * for query.
	 * @param user user who performs query
	 * @param query query object to be executed
	 * @return result of query
	 * @throws DataException if access check fails or if any other error occurs. 
	 */	
	public <T> T executeQuery(UserData user, QueryBase query) throws DataException;
	/**
	 * Performs access checks, implemented by given {@link AccessCheckerBase} descendant.<br>
	 * Initializes given access checker object with proper jdbcTemplate and user information
	 * and executes its {@link AccessCheckerBase#checkAccess() checkAccess} method.
	 * @return result returned by {@link AccessCheckerBase#checkAccess() checkAccess} method of given access checker.
	 * @throws DataException if any error occurs
	 */	
	public boolean checkAccess(UserData user, AccessCheckerBase accessChecker) throws DataException;
	
	/**
	 * Syncs information about given {@link PortalUser} with database.<br>
	 * This method updates information about user in PERSON table with
	 * information from {@link PortalUser} object if such record exists.
	 * This could involve creation of new records, deactivating or reactivating
	 * of some records in PERSON table.<br>
	 * Anyway, after execution of this method there should be exactly one
	 * {@link Person#isActive() active} record in PERSON table, representing
	 * given {@link PortalUser} object.
	 * @throws DataException if any error occurs 
	 */	
	public void syncUser(PortalUser person) throws DataException;
	
	/**
	 * Marks all users who were synchronized with Portal for the last time
	 * before given date as inactive.
	 * @param threshold threshold date
	 * @throws DataException if any error occurs
	 */	
	public void clearUsers(Date threshold) throws DataException;
	
	/**
	 * Validate given query object. 
	 * @param user user who performs query
	 * @param query query object to be valdate
	 * @throws DataException  if any error occurs 
	 */
	public void validate(UserData user, QueryBase query) throws DataException;
}
