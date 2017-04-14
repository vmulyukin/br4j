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
package org.aplana.br4j.dynamicaccess.db_export.objects;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aplana.br4j.dynamicaccess.db_export.DbException;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * Utility class to provide connection to database
 * @author atsvetkov
 *
 */
public class ConnectionFactory {

	private static final Log logger = LogFactory.getLog(ConnectionFactory.class);
	private static final String DRIVER_CLASS_NAME = "org.postgresql.Driver";
	
	public static DataSource getDataSource(String url, String userName, String password) throws DbException {
		DriverManagerDataSource dataSource = new DriverManagerDataSource();
		dataSource.setDriverClassName(DRIVER_CLASS_NAME);
		dataSource.setUrl(url);
		dataSource.setUsername(userName);
		dataSource.setPassword(password);
		return dataSource;		
	}
	
	public static Connection getConnection(String url, String userName, String password) throws DbException {
		Connection connection = null;
		try {
            //���������� ����������� �������� - �� ������� ������ ������������ ������� PostgreSQL
            Class.forName(DRIVER_CLASS_NAME);
        } catch (java.lang.ClassNotFoundException e) {
            //� ������ ���������� ����������� ������� ��������� �� ������
            logger.error(e.getMessage());
            throw new DbException("PostgreSQL driver not found");
        }
        try {
            Properties connectionProps = new Properties();
            connectionProps.put("user", userName);
            connectionProps.put("password", password);
            connection = DriverManager.getConnection(url, connectionProps);
            logger.info("Connected to database");
        } catch (SQLException e) {
            e.printStackTrace();
            logger.error("Can't establish SQL connection");
            throw new DbException("Can't establish SQL connection", e);
        }        
        return connection;
	}
}
