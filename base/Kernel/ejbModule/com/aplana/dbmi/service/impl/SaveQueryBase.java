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

import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ObjectLockedException;
import com.aplana.dbmi.service.ObjectNotLockedException;
import com.aplana.dbmi.service.impl.query.WriteQuery;

/**
 * {@link QueryBase} descendant representing base class for queries used to
 * save {@link DataObject} instances in database. <br>
 * @see DataServiceBean#saveObject(com.aplana.dbmi.service.User, DataObject) 
 */
abstract public class SaveQueryBase extends QueryBase implements WriteQuery {
	private static final long serialVersionUID = 1L;
	
	private DataObject object;
	private boolean newObj;

	/**
	 * Gets object being saved
	 * @return object being saved
	 */
	public DataObject getObject() {
		return object;
	}

	/**
	 * Sets object being saved
	 * @param object object being saved
	 */
	public void setObject(DataObject object) {
		this.object = object;
		if (getAccessChecker() != null)
			getAccessChecker().setObject(object);
		this.newObj = object.getId() == null;
	}
	
	protected void initProcessor(ProcessorBase processor) {
		super.initProcessor(processor);
		processor.setObject(getObject());
	}

	/**
	 * Returns identifier of saved object
	 */
	public ObjectId getEventObject() {
		return getObject().getId();
	}

	/**
	 * This method is used to save newly created object in database.<br>
	 * By default object is recognized as 'new' if it have null value as {@link DataObject#getId() identifier}.
	 * This behaviour could be changed by overriding {@link #isNew()} method in descendant.
	 * <br>
	 * Method saves object and returns identifier of newly created record in database<br>
	 * @return identifier of newly created record in database
	 * @throws DataException if object saving fails
	 */
	abstract protected ObjectId processNew() throws DataException;
	/**
	 * This method is used to update object which is already present database (object which is not 'new').<br>
	 * By default object is recognized as 'new' if it have null value as {@link DataObject#getId() identifier}.
	 * This behaviour could be changed by overriding {@link #isNew()} method in descendant.
	 * @throws DataException if object saving fails.
	 */
	abstract protected void processUpdate() throws DataException;

	/**
	 * Saves object in database.
	 * @return {@link ObjectId} identifier of saved object.
	 * @throws DataException if saving failed
	 */
	public Object processQuery() throws DataException
	{
		validate();
		if (isNew()) {
			return processNew();
		} else {
			processUpdate();
			return getObject().getId();
		}
	}
	
	/**
	 * Checks if object being saved already present in database.
	 * Object is recognized as 'new' if it have null value as {@link DataObject#getId() identifier}.
	 * This behaviour could be changed by overriding this method in descendant.
	 * @return true if object was not saved in database before, false if it was 
	 */
	protected boolean isNew()
	{
		return newObj;
	}

	protected void checkLock() throws ObjectLockedException, ObjectNotLockedException, DataException
	{
		super.checkLock(getObject().getId());
	}
	
	/**
	 * Retrieve next value from the database sequence with given name.
	 * @param sequence name of sequence
	 * @return next value of sequence
	 */
	protected long generateId(String sequence)
	{
		// (2010/03) POSGRE
		// OLD: return getJdbcTemplate().queryForLong("SELECT " + sequence + ".nextval FROM dual");
		return getJdbcTemplate().queryForLong( "SELECT nextval('" + sequence + "')");
	}

	/**
	 * Generates unique string identifier. This value could be used as primary
	 * key for tables with string primary keys.<br>
	 * Main advantage of this method is
	 * that generated value contains other characters besides digits, so
	 * generated value couldn't be misinterpreted as a Long identifier 
	 * by {@link ObjectId#predefined(Class, String)} method.
	 * @param table this argument is not used.
	 * @return unique string identifier.
	 */
	protected String generateStringId(String table)
	{
		final int PREFIX_LEN = 6;
		String prefix = getUser().getPerson().getLogin();
		if (prefix.length() > PREFIX_LEN)
			prefix = prefix.substring(0, PREFIX_LEN);
		return (String) getJdbcTemplate().queryForObject(
				// (2010/03) OLD: "SELECT '" + prefix.toUpperCase() + "_'|| seq_system_id.nextval FROM dual",
				"SELECT '" + prefix.toUpperCase() + "_'|| nextval('seq_system_id')",
				String.class);
	}
	
	@Override
	public int hashCode() {
		int hash = super.hashCode();
		hash = hash ^ ((object != null) ? object.hashCode() : 87756453) ^ Boolean.valueOf(newObj).hashCode();
		return hash;
	}
}
