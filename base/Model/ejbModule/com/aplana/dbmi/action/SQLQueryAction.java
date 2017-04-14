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
package com.aplana.dbmi.action;

import java.sql.Types;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * {@link Action} implementation used in UniversalPortlet to execute given sql query
 * with given set of parameters.
 * <br>
 * Returns collection of database rows returned by query. Each row is represented by {@link java.util.Map} object
 * containing all columns of the row mapped by column name.
 */
public class SQLQueryAction implements Action<List<Map>>
{
	/**
	 * Constant used to represent string parameter type.
	 * This type is mapped to java.sql.Types.VARCHAR data type
	 */
	public static final int PARAM_STRING = Types.VARCHAR;
	/**
	 * Constant used to represent date parameter type.
	 * This type is mapped to java.sql.Types.DATE data type
	 */
	public static final int PARAM_DATE = Types.DATE;
	/**
	 * Constant used to represent numeric parameter type.
	 * This type is mapped to java.sql.NUMERIC data type
	 */
	public static final int PARAM_NUMBER = Types.NUMERIC;
	
	private static final long serialVersionUID = 2L;
	private String sql;
	private MapSqlParameterSource params;
	private boolean onlyUpdate;

	/**
	 * Gets array of parameters values
	 * @return array of parameters values
	 */
	public MapSqlParameterSource getParams() {
		return params;
	}

	/**
	 * Sets array of parameters values
	 * @param params desired parameters values
	 */
	public void setParams(MapSqlParameterSource params) {
		this.params = params;
	}

	/**
	 * Gets sql query to be executed
	 * @return sql query to be executed
	 */
	public String getSql() {
		return sql;
	}

	/**
	 * Sets sql query to be executed
	 * @param sql desired sql query to be executed
	 */
	public void setSql(String sql) {
		this.sql = sql;
	}

	/**
	 * @see Action#getResultType()
	 */
	public Class getResultType() {
		return Collection.class;
	}

	public boolean isOnlyUpdate() {
		return onlyUpdate;
	}

	public void setOnlyUpdate(boolean onlyUpdate) {
		this.onlyUpdate = onlyUpdate;
	}
}
