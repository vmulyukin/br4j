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
package org.aplana.br4j.dynamicaccess.db_export;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;

public abstract class BaseAccessDao {
    protected AccessRule ar;
    protected Connection connection;
	// used to manipulate directly with connection if necessary. For example set autoCommit to false and later perform commits.
    protected DataSource dataSource;
    protected JdbcTemplate jdbcTemplate;

    //TODO get rid of this method
    protected BaseAccessDao(Connection connection) {
        this.connection = connection;
    }
    /**
     * Constructs {@link JdbcTemplate} inside it.
     * @param dataSource {@link DataSource} which is used to create JdbcTemplate.
     */
    protected BaseAccessDao(DataSource dataSource) {
    	this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        try {
			this.connection =  this.jdbcTemplate.getDataSource().getConnection();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public BaseAccessDao(Connection connection, DataSource dataSource) {
    	this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    	this.connection = connection;
    }    

    public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}
    
	public DataSource getDataSource() {
		return dataSource;
	}
	
	public void setAr(AccessRule ar) {
        this.ar = ar;
    }

    protected void setPrepStmString(PreparedStatement statement, int indx, String str) throws SQLException {
        String normStr = str;
        if (str != null && str.trim().length() == 0) normStr = null;
        statement.setString(indx, normStr);
    }

    protected void setPrepStmInt(PreparedStatement statement, int indx, String str) throws SQLException {
        if (str != null && str.trim().length() > 0){
            statement.setInt(indx, Integer.parseInt(str));
        } else {
            statement.setNull(indx, Types.NUMERIC);
        }
    }
	public Connection getConnection() {
		return connection;
	}
}
