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

import org.aplana.br4j.dynamicaccess.db_export.objects.AccessListDao;
import org.aplana.br4j.dynamicaccess.db_export.objects.ConnectionFactory;
import org.aplana.br4j.dynamicaccess.xmldef.Template;

import javax.sql.DataSource;
import java.util.List;

/**
 * Saves edited access rules and updates access_list table for specified rules and statuses.
 * @author atsvetkov
 * 
 */
public class DoUpdateAccessRuleByStatus extends DoUpdateAccessRule {

	private List<String> statusIds;
	private boolean partial;

	public DoUpdateAccessRuleByStatus(String url, String username, String password, List<String> ruleNames,
			List<String> statusIds, Template template) {
		super(url, username, password, ruleNames, template);
		this.statusIds = statusIds;
	}

	@Override
	protected void updateAccessRule() throws DbException {
		DataSource newDataSource = ConnectionFactory.getDataSource(getUrl(), getUsername(), getPassword());
		UpdateAccessRuleByStatusStateObserver stateObserver = new UpdateAccessRuleByStatusStateObserver(this);
		int totalCount = getRuleNames().size() * statusIds.size();
		int count = 0;
		for (String ruleName : getRuleNames()) {
			for (String statusId : statusIds) {
				// each saving of rules should be performed in a separate
				// transaction. That's why we provide a new datasource for each operation.
				AccessListDao.getInstance(newDataSource).updateAccessByRuleAndStatus(ruleName, statusId, getTemplate(), stateObserver, partial);

				setProgress((count + 1)*StateObserver.MAX_PROGRESS / totalCount);
				count++;
			}
			// ���������� ������ �� �� ������ ����� ����, ��� ��� ����� ��� ���� �������� ����� ��������� � ��
			AccessListDao.getInstance(newDataSource).updatePermissionsByRuleName(getTemplate(), ruleName, partial, statusIds);
		}
	}

	public List<String> getStatusIds() {
		return statusIds;
	}
	
	class UpdateAccessRuleByStatusStateObserver implements StateObserver{    	
    	protected DoUpdateAccessRuleByStatus workerThread;

    	public UpdateAccessRuleByStatusStateObserver(DoUpdateAccessRuleByStatus workerThread) {
			this.workerThread = workerThread;
		}

    	public void stateChanged(int progress) {
		}

		public boolean isCancelled(){
			return workerThread.isCancelled();
		}
    }

	public void setPartial(boolean b) {
		this.partial = b;
		
	}
}
