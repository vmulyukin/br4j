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
package com.aplana.dbmi.model;

import java.io.Serializable;

/**
 * Abstract class used as ancestor for all classes representing objects stored in database
 * All such classes should have {@link #getId()} method, returning 
 * information of record type and primary key (see {@link ObjectId}).
 * <br>
 * In most cases identifier of DataObject should be immutable. So in most cases creation of 
 * DataObject descendant should looks like follows: <br>
 * <pre>
 * ObjectId objId = new ObjectId(DataObjectChild.class, idValue);
 * DataObjectChild obj = DataObject.createFromId(objId);
 * // initializing of other properties
 * ...
 * // working with object
 * </pre> 
 */
abstract public class DataObject implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	private ObjectId id;
	
	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (!(obj instanceof DataObject)) return false;
		final DataObject other = (DataObject) obj;
		if (this.id == null)
			return false; // null != null
		return this.id.equals(other.getId());
	}
	
	@Override
	public int hashCode() {
		return  (this.id == null) ? 0xABCDEF : this.id.hashCode();
	}
	
	/**
	 * Gets object identifier
	 * @return object identifier
	 */
	public ObjectId getId() {
		return id;
	}
	
	/**
	 * temporary makes it public...Should be used for internal purpose only 
	 */
	public void setId(ObjectId id) {
		//if (!id.getType().isAssignableFrom(getClass()))
		//	throw new IllegalArgumentException("Illegal identifier");
		this.id = id;
	}
	
	/**
	 * Create empty object by given id
	 * Create new instance of DataObject descendant and initialize it's identifier field.
	 * All other fields remains empty (if not initialized in default constructor)
	 * @param id database object identifier
	 * @return empty data object with given Id
	 */
	@SuppressWarnings("unchecked")
	public static <T extends DataObject> T createFromId(ObjectId id)
	{
		try {
			T obj = (T) id.getType().newInstance();
			obj.setId(id);
			return obj;
		} catch (Exception e) {
			throw new RuntimeException("Error creating object by id " + id, e);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends DataObject> T createFromId(Class<T> type, String code)
	{
		try {
			ObjectId id = ObjectId.predefined(type, code);
			T obj = (T) id.getType().newInstance();
			obj.setId(id);
			return obj;
		} catch (Exception e) {
			throw new RuntimeException("Error creating object by code " + code, e);
		}
	}
}
