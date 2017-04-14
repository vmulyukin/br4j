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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aplana.br4j.dynamicaccess.db_export.DbException;
import org.aplana.br4j.dynamicaccess.db_export.StateObserver;
import org.aplana.br4j.dynamicaccess.xmldef.Rule;
import org.aplana.br4j.dynamicaccess.xmldef.Template;
import org.aplana.br4j.dynamicaccess.xmldef.types.PermissionWrapper;

import javax.sql.DataSource;
import java.util.List;
import java.util.Set;


/**
 * DAO for access list entity.
 * @author atsvetkov
 *
 */
public class AccessListDao {

	private static AccessListDao instance;
	private final Log logger = LogFactory.getLog(getClass());
	private DataSource dataSource;

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public static AccessListDao getInstance(DataSource dataSource)
	{
		if (instance == null) {
			instance = new AccessListDao();
		}
		instance.setDataSource(dataSource);
		return instance;
	}

	/**
	 * Clears table access_list before importing new data.
	 */
	public void clearAccessList()
	{
		AccessListClearOperations.clearAccessList(dataSource);
	}

	public void clearAccessList(Long rule_id)
	{
		AccessListClearOperations.clearAccessList(dataSource, rule_id);
	}

	/**
	 * Deletes all permissions from database having the specified rule name.
	 * @param rulesToDelete {@link Rule} set rules name
	 * @param template {@link Template} containing permissions to be deleted
	 * @param partial {@link Template} containing permissions to be deleted
	 * @throws DbException
	 */
	public void clearRulesByRuleName(Set<Rule> rulesToDelete, Template template, boolean partial) throws DbException
	{
		AccessListClearOperations.clearRulesByRuleName(rulesToDelete, template, dataSource, partial);
	}

	/**
	 * Deletes all permissions from database having the specified rule name and status.
	 * @param rulesToDelete {@link Rule} set rules name
	 * @param statusId status id
	 * @param template {@link Template} containing permissions to be deleted
	 * @param partial {@link Template} containing permissions to be deleted
	 * @throws DbException
	 */
	public void clearRulesByRuleNameAndStatus(Set<Rule> rulesToDelete, String statusId, Template template, boolean partial) throws DbException
	{
		AccessListClearOperations.clearRulesByRuleNameAndStatus(rulesToDelete, statusId, template, dataSource, partial);
	}

	/**
	 * Updates access list using batch update.
	 * @param cardIds {@link List} of card id to be updated in one batch
	 * @return the number of rows updated in one batch
	 * @throws DbException if batch update fails.
	 */
	public long updateAccessToCardInBatch(final List<Long> cardIds) throws DbException
	{
		return AccessListUpdateOperations.updateAccessToCardInBatch(cardIds, dataSource);
	}

	/**
	 * Updates access to specified rule. if this method should be executed in one transaction put its calling in transaction.
	 * @param ruleName the name of rule to be updated.
	 * @param template the {@link Template} containing rule.
	 * @param partial true if need to perform partial update access, else false
	 * @throws DbException if operation fails.
	 */
	public void updateAccessByRule(String ruleName, Template template, StateObserver callback, boolean partial) throws DbException
	{
		AccessListUpdateOperations.updateAccessByRule(ruleName, template, callback, dataSource, partial);
	}

	/**
	 * ���������� ������ ������ �� ������ � �� �� �� ����� �������, ���������� � ������� (��� �������� SYSTEM).
	 * @param ruleName ��� �������
	 * @param template ������
	 * @return ������ ������
	 * @throws DbException
	 */
	public Set<Rule> getRulesByRuleName(String ruleName, Template template) throws DbException
	{
		return AccessListUpdateOperations.getRulesByRuleName(ruleName, template, dataSource);
	}

	/**
	 * Updates access to specified rule. if this method should be executed in one transaction put its calling in transaction.
	 * @param ruleName the name of rule from the Application (without suffix SYSTEM).
	 * @param template the {@link Template} containing rule.
	 * @param partial true if need to perform partial update access, else false
	 * @throws DbException if operation fails.
	 * ������������� �� ��, ����� ������� ������ ������� �� ������� ������� � ��������� �������������� �������
	 */
	public void updateAccessByRuleAndStatus(String ruleName, String statusId, Template template, StateObserver callback, boolean partial) throws DbException
	{
		AccessListUpdateOperations.updateAccessByRuleAndStatus(ruleName, statusId, template, callback, dataSource, partial);
	}

	public long updateAccessListForRule(PermissionWrapper.RuleType ruleType, Long ruleId) throws DbException
	{
		return AccessListUpdateOperations.updateAccessListForRule(ruleType, ruleId, dataSource);
	}

	public void updatePermissionsByRuleName(Template template, String ruleName, boolean partial, List<String> statuses) throws DbException
	{
		AccessListUpdateOperations.updatePermissionsByRuleName(template, ruleName, partial, statuses, dataSource);
	}

}
