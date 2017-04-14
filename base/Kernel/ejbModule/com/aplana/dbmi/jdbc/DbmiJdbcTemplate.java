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
package com.aplana.dbmi.jdbc;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.SqlProvider;

/**
 * �������������� JdbcTemplate ��� ����� ���������� �����������
 * @author larin
 */
public class DbmiJdbcTemplate extends JdbcTemplate{
	protected final Log logger = LogFactory.getLog("SQL_TRACE");

	public DbmiJdbcTemplate(DataSource dataSource) {
		super(dataSource);
	}

	private long getStartTime(){
		long start = 0;
		if (logger.isDebugEnabled()) {
			start = System.currentTimeMillis();
		}
		return start; 
	}
	
	private void debug(long start, String sql){
		if (logger.isDebugEnabled()) {
			long time = System.currentTimeMillis() - start;
			logger.debug("Executed SQL query [" + time + "][" + sql + "]");
		}		
	}
	
	public Object query(final String sql, final ResultSetExtractor rse) throws DataAccessException {
		Object result = 0;
		long start = getStartTime();
		result = super.query(sql, rse);
		debug(start, sql);
		return result;		
	}

	public void execute(final String sql) throws DataAccessException {
		long start = getStartTime();
		super.execute(sql);
		debug(start, sql);
	}

	public int update(final String sql) throws DataAccessException {
		int result = 0;
		long start = getStartTime();
		result = super.update(sql);
		debug(start, sql);
		return result;		
	}
	
	public int[] batchUpdate(final String[] sql) throws DataAccessException {
		int[] result = null;
		long start = getStartTime();
		result = super.batchUpdate(sql);
		for (int i=0; i<sql.length; i++){
			debug(start, sql[i]);			
		}
		return result;				
	}

	public Object execute(PreparedStatementCreator psc, PreparedStatementCallback action)
		throws DataAccessException {
		Object result = null;
		long start = getStartTime();
		result = super.execute(psc, action);
		debug(start, getSql(psc));
		return result;				
	}
	
	private String getSql(Object sqlProvider) {
		if (sqlProvider instanceof SqlProvider) {
			return ((SqlProvider) sqlProvider).getSql();
		}
		else {
			return null;
		}
	}
	
}
