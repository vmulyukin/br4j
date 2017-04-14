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
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aplana.br4j.dynamicaccess.db_export.objects.ConnectionFactory;
import org.aplana.br4j.dynamicaccess.xmldef.AccessConfig;
import org.aplana.br4j.dynamicaccess.xmldef.Template;

import javax.swing.SwingWorker;

public class DoSaveTemplate extends SwingWorker<Exception, Void> {

	protected final Log logger = LogFactory.getLog(getClass());

    private Connection connection;
    private String url;
    private String username;
    private String password;
    private AccessConfig ac;
    private Template template;

	public DoSaveTemplate(String url, String username, String password, AccessConfig ac, Template template) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.ac = ac;
        this.template = template;
	}

	@Override
	protected Exception doInBackground() throws Exception {
        if (ac == null) {
        	throw new IllegalArgumentException("AccessConfig is null");
        } else if (template == null) {
        	throw new IllegalArgumentException("template is null");
        }

        try {
            establishConnection();

            SaveAccessRulesManager accessRuleManager = new SaveAccessRulesManager(connection);
            accessRuleManager.cleanUpRuleTablesForTemplate(template.getTemplate_id());

            accessRuleManager.processTemplate(template);
            connection.commit();
            logger.info("Template " + template.getName() + " was saved successfully");
        } catch (Exception e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            if (connection != null) connection.rollback();
            return e;
        } finally {
            if (connection != null) connection.close();
        }
        return null;
	}

    private void establishConnection() throws DbException {
       	connection = ConnectionFactory.getConnection(url, username, password);
    	try{
    		connection.setAutoCommit(false);
    	}catch(SQLException ex){
    		throw new DbException(ex.getMessage());
    	}        
    }
}