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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aplana.br4j.dynamicaccess.db_export.objects.AccessListDao;
import org.aplana.br4j.dynamicaccess.db_export.objects.ConnectionFactory;
import org.aplana.br4j.dynamicaccess.xmldef.AccessConfig;
import org.aplana.br4j.dynamicaccess.xmldef.Rule;
import org.aplana.br4j.dynamicaccess.xmldef.Template;

import javax.sql.DataSource;
import javax.swing.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class DoSaveAccessRules extends SwingWorker<Exception, Void> {

    public final static String NO_RULE = "NO_RULE";
    public final static String NO_STATUS = "NO_STATUS";

    public final static String READ = "R";
    public final static String WRITE = "W";
    public final static String CREATE = "C";

    public final static String CREATE_DESCR = "create";

    protected final Log logger = LogFactory.getLog(getClass());

    private Connection connection;
    private String url;
    private String username;
    private String password;
    private AccessConfig ac;
    private String currentTemplate;
    private boolean onlyChanges;

    public DoSaveAccessRules(String url, String username, String password, AccessConfig ac) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.ac = ac;
    }
    
    @Override
    protected Exception doInBackground() throws Exception {
        if (ac == null) throw new IllegalArgumentException("AccessConfig is null");

        setProgress(0); // for progress monitor
        try {
            establishConnection();
            connection.setAutoCommit(false);
            SaveAccessRulesManager accessRuleManager = new SaveAccessRulesManager(connection);
            accessRuleManager.setPartial(onlyChanges);
            Template[] templates = ac.getTemplate();
            if(!onlyChanges){
            	accessRuleManager.cleanUpTables();
            }

            if (templates != null) {
                for (int i = 0; i < templates.length; i++) {
                    if (isCancelled()) throw new DbException("������� �������� �������������");
                    Template template = templates[i];
                    currentTemplate = "������: " + template.getName();

                    logger.debug("Processing template " + template.getName() + "...");

                    if (template.getRules() == null || template.getRules().getRule() == null) {
                        continue;
                    }
                    
                	DataSource dataSource = ConnectionFactory.getDataSource(url, username, password);
                	
                    Rule[] rules = template.getRules().getRule();
                    Set<Rule> rulesToDelete = new HashSet<Rule>();
                    rulesToDelete.addAll(Arrays.asList(rules));
                    if(onlyChanges){
                        AccessListDao.getInstance(dataSource).clearRulesByRuleName(rulesToDelete, template, onlyChanges);
                    }
            		for (Rule rule : rules) {
            			accessRuleManager.processRule(rule, template);
            		}
                    setProgress( 100 * (i + 1) / templates.length); // for progress monitor
                }
            }
            connection.commit();
            logger.info("Access Rules configuration saved successfully.");
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

    public String getCurrentTemplate() {
        return currentTemplate;
    }

    private void establishConnection() throws DbException {
       	connection = ConnectionFactory.getConnection(url, username, password);
    	try{
    		connection.setAutoCommit(false);
    	}catch(SQLException ex){
    		throw new DbException(ex.getMessage());
    	}        
    }

	public void setOnlyChanges(boolean onlyChanges) {
		this.onlyChanges = onlyChanges;
	}

}
