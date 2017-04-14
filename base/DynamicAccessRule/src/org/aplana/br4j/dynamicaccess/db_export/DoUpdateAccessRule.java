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
import org.aplana.br4j.dynamicaccess.xmldef.Template;

import javax.sql.DataSource;
import javax.swing.*;
import java.util.List;

public class DoUpdateAccessRule extends SwingWorker<Exception, Void> {

    protected final Log logger = LogFactory.getLog(getClass());

    private String url;
    private String username;
    private String password;
    private boolean partial;
    /**
     * ��� �������, ��� ������� � ������� (��� ���������). 
     */
    private List<String> ruleNames;
    private Template template;

    public DoUpdateAccessRule(String url, String username, String password, List<String> ruleNames, Template template) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.ruleNames = ruleNames;
        this.template = template;
    }

    @Override
    protected Exception doInBackground() throws Exception {
        if (ruleNames == null || ruleNames.isEmpty() || template == null) {
        	throw new IllegalArgumentException("Rule name or template is not specified");
        }

        setProgress(1); // for progress monitor	
        try {            
        	if (isCancelled()) {
				throw new CancelException("������� access list ����������� �������������");
			}
        	
			updateAccessRule();
			
        } catch (Exception e) {
        	JOptionPane.showMessageDialog(null, "Error occured: " + e.getMessage(), "Info",
                    JOptionPane.WARNING_MESSAGE);
		
            logger.error(e.getMessage());
            e.printStackTrace();
            setProgress(100);
            return e;
        } 
        return null;
    }

	protected void updateAccessRule() throws DbException {
		DataSource dataSource = ConnectionFactory.getDataSource(url, username, password);
		AccessListDao accessListDao = AccessListDao.getInstance(dataSource);
		for(String ruleName : ruleNames){
			accessListDao.updateAccessByRule(ruleName, template, new UpdateAccessRuleStateObserver(this), partial);			
		}
	}

    public String getUrl() {
		return url;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public List<String> getRuleNames() {
		return ruleNames;
	}

	public Template getTemplate() {
		return template;
	}
	
	public void setPartial(boolean partial) {
		this.partial = partial;
	}

	class UpdateAccessRuleStateObserver implements StateObserver{    	
    	protected DoUpdateAccessRule workerThread;
		
    	
    	public UpdateAccessRuleStateObserver(DoUpdateAccessRule workerThread) {
			this.workerThread = workerThread;
		}

		public void stateChanged(int progress) {
			workerThread.setProgress(progress);			
		}
		
		public boolean isCancelled(){
			return workerThread.isCancelled();
		}
    }
	
	class UpdateMultipleAccessRuleStateObserver extends UpdateAccessRuleStateObserver{
		
		private int currentProgress;
		private int numberOfRules;
		
		public UpdateMultipleAccessRuleStateObserver(DoUpdateAccessRule workerThread, int numberOfRules) {
			super(workerThread);
			this.numberOfRules = numberOfRules;
		}
		@Override
		public void stateChanged(int progress) {
			currentProgress = currentProgress + progress/numberOfRules;
			workerThread.setProgress(currentProgress);			
		}
	}
}
