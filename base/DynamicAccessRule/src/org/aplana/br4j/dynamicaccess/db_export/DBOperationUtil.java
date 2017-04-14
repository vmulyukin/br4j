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

import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aplana.br4j.dynamicaccess.db_export.objects.AccessListDao;
import org.aplana.br4j.dynamicaccess.db_export.objects.AccessRuleDao;
import org.aplana.br4j.dynamicaccess.db_export.objects.ConnectionFactory;
import org.aplana.br4j.dynamicaccess.xmldef.*;
import org.aplana.br4j.dynamicaccess.xmldef.types.*;
import org.aplana.br4j.dynamicaccess.xmldef.types.Action;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.sql.DataSource;
import javax.swing.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.*;

/**
 * @author etarakanov
 *         Date: 26.02.15
 *         Time: 13:21
 */

/**
 * ����� ��� ����������� �������� �������� �� �������� � �������������� ������ � ���� �������.
 * ���������� ��������� ������ doInBackground � �������:
 * {@link org.aplana.br4j.dynamicaccess.db_export.DoSaveAccessRules}
 * {@link org.aplana.br4j.dynamicaccess.db_export.DoSaveTemplate}
 * {@link org.aplana.br4j.dynamicaccess.db_export.DoUpdateAccessList}
 * {@link org.aplana.br4j.dynamicaccess.db_export.DoUpdateAccessRule}
 * {@link org.aplana.br4j.dynamicaccess.db_export.DoUpdateAccessRuleByStatus}
 */
public class DBOperationUtil
{
    private int progress;
    private Template currentTemplate;
    private boolean isCancel;
    private String ruleNote;

    private final static int ERROR_LIMIT = 100;
    private int errorsCount = 0;

    private DataSource dataSource;

    private final Log logger = LogFactory.getLog(getClass());

    /**
     * ��������� ���������� ������ � �� � �������� Access List
     * @param url - ���� ����������� � ��
     * @param username - ������� ������ ������������ ��
     * @param password - ������ ������������ ��
     * @param ac - ������������ ���� ������� {@link org.aplana.br4j.dynamicaccess.xmldef.AccessConfig}
     * @return - null ���� �������� �������, ����� Exception
     * @throws Exception
     */
    public Exception doUpdatePartial(String url, String username, String password, AccessConfig ac) throws Exception {
        logger.info("Partial update Access Rules...");
        if (ac == null) throw new IllegalArgumentException("AccessConfig is null");
	    AccessConfig filteredAccessConfig = removeExistsPermissions(url, username, password, ac);
        Exception result = doSaveAccessRules(url, username, password, filteredAccessConfig);
        if (result == null) {
            result = doUpdateAccessList(url, username, password, filteredAccessConfig, true);
        }
        logger.info("Partial update Access Rules completed successfully.");
        return result;
    }

	/**
	 * ���������� {@link org.aplana.br4j.dynamicaccess.xmldef.AccessConfig} ������ � ������ �������
	 * (����������� ������ ����� �� ����������, ����� �� ������� �������� ��� ���������).
	 * @param url - ���� ����������� � ��
	 * @param username - ������� ������ ������������ ��
	 * @param password - ������ ������������ ��
	 * @param originalAC - ������������ ���� ������� {@link org.aplana.br4j.dynamicaccess.xmldef.AccessConfig}
	 * @return
	 * @throws Exception
	 */
	public AccessConfig removeExistsPermissions(String url, String username, String password, AccessConfig originalAC) throws Exception {
		AccessConfig filteredAC =  SerializationUtils.clone(originalAC);
		DataSource dataSource = ConnectionFactory.getDataSource(url, username, password);
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		for(Template t: filteredAC.getTemplateList()){
			logger.info("Start filter template rules, template = " + t.getTemplate_id());
			for(Permission p: t.getPermissionList()){
				logger.info("Start filter permission: rule = " + p.getRule() + ", status = " + p.getStatus());
				Iterator<Operation> io = p.getOperations().getOperationList().iterator();
				while (io.hasNext()){
					Operation operation = io.next();
					if(operation.getPermHash() == null || operation.getPermHash().isEmpty()) {
						throw new  Exception("Operation doesn't have permHash");
					}
					if(Action.ADD.equals(operation.getAction()) &&
							checkPermissionExists(jdbcTemplate, operation.getPermHash(), p.getRule())){
						io.remove();
						logger.info("Operation with permHash = " + operation.getPermHash() + "has alredy exixsts");
					}
				}
				Iterator<WfMove> iwfm = p.getWfMoves().getWfMoveList().iterator();
				while (iwfm.hasNext()){
					WfMove wfMove = iwfm.next();
					if(wfMove.getPermHash() == null || wfMove.getPermHash().isEmpty()) {
						throw new  Exception("WfMove doesn't have permHash");
					}
					if(Action.ADD.equals(wfMove.getAction()) &&
							checkPermissionExists(jdbcTemplate, wfMove.getPermHash(), p.getRule())){
						iwfm.remove();
					}
				}
			}
		}
		jdbcTemplate.getDataSource().getConnection().close();
		return filteredAC;
	}

	/**
	 * ���������� �������� ������������� ������ �� ���� � �������� �������. �������� �� �������� ����� ��� ����,
	 * ����� �� ������������ ������ � ����� ����� � ������ ��������� (����� ��������� ���).
	 * @param jdbcTemplate
	 * @param permHash - ��� �����
	 * @param description - �������� �������.
	 * @return true, ���� ����� � ����� ����� � ��������� ����������, false � ��������� �������.
	 */
	public Boolean checkPermissionExists(JdbcTemplate jdbcTemplate, String permHash, String description){
		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append("select count(1) > 0 \n")
				.append("from access_rule ar \n")
				.append("join (select rule_id, description from person_access_rule \n")
				.append("union select rule_id, description from profile_access_rule) as rules \n")
				.append("on ar.rule_id = rules.rule_id \n")
				.append("where ar.perm_hash = ? and rules.description = ?");
		return jdbcTemplate.queryForObject(sqlBuilder.toString(), Boolean.class, new Object[] {permHash, description});
	}

    /**
     * ���������� Access List � �� {@link org.aplana.br4j.dynamicaccess.db_export.DoSaveAccessRules#doInBackground()}
     * @param url - ���� ����������� � ��
     * @param username - ������� ������ ������������ ��
     * @param password - ������ ������������ ��
     * @param ac - ������������ ���� ������� {@link org.aplana.br4j.dynamicaccess.xmldef.AccessConfig}
     * @return - null ���� �������� �������, ����� Exception
     * @throws Exception
     */
    public Exception doSaveAccessRules(String url, String username, String password, AccessConfig ac) throws Exception {
        if (ac == null) throw new IllegalArgumentException("AccessConfig is null");

        Connection connection = getConnection(url, username, password);
        setProgress(0); // for progress monitor
        try {
            connection.setAutoCommit(false);
            SaveAccessRulesManager accessRuleManager = new SaveAccessRulesManager(connection);
            accessRuleManager.setPartial(ac.getPartial());
            Template[] templates = ac.getTemplate();
            if(!ac.getPartial()){
                accessRuleManager.cleanUpTables();
            }

            if (templates != null) {
                for (int i = 0; i < templates.length; i++) {
                    if (isCancel()) throw new DbException("������� �������� �������������");
                    Template template = templates[i];
                    currentTemplate = template;

                    logger.debug("Processing template " + template.getName() + "...");

                    if (template.getRules() == null || template.getRules().getRule() == null) {
                        continue;
                    }

                    DataSource dataSource = ConnectionFactory.getDataSource(url, username, password);

                    Rule[] rules = template.getRules().getRule();
                    Set<Rule> rulesToDelete = new HashSet<Rule>();
                    rulesToDelete.addAll(Arrays.asList(rules));
                    if(ac.getPartial()){
                        AccessListDao.getInstance(dataSource).clearRulesByRuleName(rulesToDelete, template, ac.getPartial());
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

    /**
     * ���������� ������� � �� {@link org.aplana.br4j.dynamicaccess.db_export.DoSaveTemplate#doInBackground()}
     * @param url - ���� ����������� � ��
     * @param username - ������� ������ ������������ ��
     * @param password - ������ ������������ ��
     * @param template - ������������ ���� ��� ������� �������� {@link org.aplana.br4j.dynamicaccess.xmldef.Template}
     * @return - null ���� �������� �������, ����� Exception
     * @throws Exception
     */
    public Exception doSaveTemplate(String url, String username, String password, Template template) throws Exception {
        if (template == null) {
            throw new IllegalArgumentException("template is null");
        }

        Connection connection = getConnection(url, username, password);

        try {
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

    /**
     * �������� ���� {@link org.aplana.br4j.dynamicaccess.db_export.DoUpdateAccessList#doInBackground()}
     * @param url - ���� ����������� � ��
     * @param username - ������� ������ ������������ ��
     * @param password - ������ ������������ ��
     * @return - null ���� �������� �������, ����� Exception
     * @throws Exception
     */
    public Exception doUpdateAccessList(String url, String username, String password, AccessConfig ac, boolean onlyChange) throws Exception {
        if (onlyChange && ac == null) throw new IllegalArgumentException("AccessConfig is null");
        final DataSourceTransactionManager txManager = new DataSourceTransactionManager();
        txManager.setDataSource(getDataSource(url, username, password));

        final DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
        transactionDefinition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        final TransactionStatus transactionStatus = txManager.getTransaction(transactionDefinition);
        long start = System.currentTimeMillis();
        long updatedRows = 0;
        progress = 1;
        try {
            Connection connection = getConnection(url, username, password);
            List<AccessRuleDao.RecalculateAccessRule> rules = getRules(getDataSource(url, username, password), ac, onlyChange);
            if(!onlyChange){ //������ �������� ������ ���� ������ �������� ACL
                logger.info("Clearing old data in access list");
                AccessListDao.getInstance(getDataSource(url, username, password)).clearAccessList();
            }

            int iterIndex = 0;
            String iterName = (rules.size()>0)?rules.get(0).getRule_name():null;
            for(int index = 0; index < rules.size(); index ++) {
				/* ��� ����������� �������� ��������� ���������, ��� ������� ������� ����������� ����� �� 6-� ��������:
				 * 1. ������������ ������� ��� �� � ��� (������������ ��� ������������ ������� �123 (1/200) - 1/2000 � �������)
				 * 2. ������������ ������� � �� � ��� ��� (������������ ��� ������������ ������� c �� �123 (1/200) - 1/2000 � �������)
				 * 3. ������������ ������� � �� � ��� (������������ ��� ������������ ������� c �� � ��� �123 (1/200) - 1/2000 � �������)
				 * 4. ���������� ������� ��� �� � ��� (������������ ��� ���������� ������� �123 (1/200) - 1/2000 � �������)
				 * 5. ���������� ������� � �� � ��� ��� (������������ ��� ���������� ������� c �� �123 (1/200) - 1/2000 � �������)
				 * 6. ���������� ������� � �� � ��� (������������ ��� ���������� ������� c �� � ��� �123 (1/200) - 1/2000 � �������)
				 */
                AccessRuleDao.RecalculateAccessRule currRule = rules.get(index);
                // ��������� ������� � ����� �������� ������ �������� ������� � ������� ������� ������� � ��������
                if (!currRule.getRule_name().equals(iterName)){
                    iterIndex = 0;
                    iterName = currRule.getRule_name();
                }
                long toIndex = index + 1;
                iterIndex++;
                ruleNote = MessageFormat.format("{0} �{1} ({2}/{3}) - {4}/{5} ", currRule.getRule_name(), currRule.getRule_id(), iterIndex, currRule.getRule_count(), toIndex, rules.size());

                StringBuffer str = new StringBuffer("Updating Access List for rule: "+ruleNote);
                logger.info(str);
                if (isCancel()) {
                    throw new CancelException("������� access list ����������� �������������");
                }
                if(onlyChange){ //�������� ���� ������ ��� ��������������� �������
                    logger.info("Clearing old data by rule_id = " + currRule.getRule_id());
                    AccessListDao.getInstance(getDataSource(url, username, password)).clearAccessList(currRule.getRule_id());
                }
                updatedRows+= updateAccessToRule(connection, getDataSource(url, username, password), currRule.getRule_type(), currRule.getRule_id());
                //Do not commit explicitly as JdbcTemplate use autoCommit option
                //connection.commit();

                setProgress( 100 * (index + 1) / rules.size()); // for progress monitor

            }
            setProgress(100);
            txManager.commit(transactionStatus);
            logger.info("Updating Access List has finished successfully.");

        } catch (Exception e) {
            txManager.rollback(transactionStatus);

            logger.error(e.getMessage());

            e.printStackTrace();
            // we are using batch update, no need to rollback current
            // transaction because the new run update access list will clear the
            // old data first.

            return e;
        } finally {
            long end = System.currentTimeMillis();
            logger.info(" Updating access list finished in : " + (end - start) + "ms, updated rows: " + updatedRows);
        }
        return null;
    }

    /**
     * ��������� ��������� ������� ������� � �� � ����������� �����
     * {@link org.aplana.br4j.dynamicaccess.db_export.DoUpdateAccessRule#doInBackground()}
     * @param url - ���� ����������� � ��
     * @param username - ������� ������ ������������ ��
     * @param password - ������ ������������ ��
     * @param ruleNames - ������ ���� ������
     * @param template - ������������ ���� ��� ������� �������� {@link org.aplana.br4j.dynamicaccess.xmldef.Template}
     * @return - null ���� �������� �������, ����� Exception
     * @throws Exception
     */
    public Exception doUpdateAccessRule(String url, String username, String password, List<String> ruleNames, Template template) throws Exception {
        if (ruleNames == null || ruleNames.isEmpty() || template == null) {
            throw new IllegalArgumentException("Rule name or template is not specified");
        }

        progress = 1; // for progress monitor
        try {
            if (isCancel) {
                throw new CancelException("������� access list ����������� �������������");
            }

            updateAccessRule(url, username, password, ruleNames, template);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error occured: " + e.getMessage(), "Info",
                    JOptionPane.WARNING_MESSAGE);

            logger.error(e.getMessage());
            e.printStackTrace();
            progress = 100;
            return e;
        }
        return null;
    }

    /**
     * ��������� ��������� ������� ��� ��������� �������� ������� � �� � ����������� �����
     * {@link org.aplana.br4j.dynamicaccess.db_export.DoUpdateAccessRuleByStatus#doInBackground()}
     * @param url - ���� ����������� � ��
     * @param username - ������� ������ ������������ ��
     * @param password - ������ ������������ ��
     * @param ruleNames - ������ ���� ������
     * @param statusIds - ������ �������� ��������
     * @param template - ������������ ���� ��� ������� ��������
     * @return - null ���� �������� �������, ����� Exception
     * @throws Exception
     */
    public Exception doUpdateAccessRuleByStatus(String url, String username, String password, List<String> ruleNames,
                                                List<String> statusIds, Template template) throws Exception {
        if (ruleNames == null || ruleNames.isEmpty() || template == null) {
            throw new IllegalArgumentException("Rule name or template is not specified");
        }

        progress = 1; // for progress monitor
        try {
            if (isCancel) {
                throw new CancelException("������� access list ����������� �������������");
            }

            updateAccessRule(url, username, password, ruleNames, template, statusIds);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error occured: " + e.getMessage(), "Info",
                    JOptionPane.WARNING_MESSAGE);

            logger.error(e.getMessage());
            e.printStackTrace();
            progress = 100;
            return e;
        }
        return null;
    }

    public boolean isCancel() {
        return isCancel;
    }

    public void setCancel(boolean isCancel) {
        this.isCancel = isCancel;
    }

    public Template getCurrentTemplate() {
        return currentTemplate;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getProgress() {
        return progress;
    }

    public String getRuleNote() {
        return ruleNote;
    }

    private Connection getConnection(String url, String username, String password) throws DbException
    {
        Connection connection = ConnectionFactory.getConnection(url, username, password);
        try{
            connection.setAutoCommit(false);
        }catch(SQLException ex){
            throw new DbException(ex.getMessage());
        }
        return connection;
    }

    private DataSource getDataSource(String url, String username, String password) throws DbException {
        if (dataSource == null) {
            dataSource = ConnectionFactory.getDataSource(url, username, password);
        }
        return dataSource;
    }

    private List<AccessRuleDao.RecalculateAccessRule> getRules(DataSource dataSource, AccessConfig ac, boolean onlyChanges) {
        AccessRuleDao ruleDao = new AccessRuleDao(dataSource);
        if(onlyChanges){
            List<String> permHashes = new ArrayList<String>();
            for(Template template: ac.getTemplate()){
                for(Permission permission : template.getPermission()){
                    for(Operation operation: permission.getOperations().getOperations()){
                        if(org.aplana.br4j.dynamicaccess.xmldef.types.Action.ADD.equals(operation.getAction())){
                            if(operation.getPermHash() == null){
                                throw new RuntimeException("perm hash for rule " + permission.getRule() + " is null");
                            }
                            permHashes.add(operation.getPermHash());
                        }
                    }
                    for(WfMove wfMove: permission.getWfMoves().getWfMove()){
                        if(org.aplana.br4j.dynamicaccess.xmldef.types.Action.ADD.equals((wfMove.getAction()))){
                            if(wfMove.getPermHash() == null){
                                throw new RuntimeException("perm hash for rule " + permission.getRule() + " is null");
                            }
                            permHashes.add(wfMove.getPermHash());
                        }
                    }
                }
            }
            if(!permHashes.isEmpty()){
                return ruleDao.getRulesByHashes(permHashes);
            } else {
                return new ArrayList<AccessRuleDao.RecalculateAccessRule>();
            }

        } else {
            return ruleDao.getRules();
        }
    }

    private void updateAccessRule(String url, String username, String password, List<String> ruleNames,  Template template) throws DbException {
        DataSource dataSource = ConnectionFactory.getDataSource(url, username, password);
        AccessListDao accessListDao = AccessListDao.getInstance(dataSource);
        for(String ruleName : ruleNames){
            accessListDao.updateAccessByRule(ruleName, template, new DBOperationStateObserver(this), false);
        }
    }

    private void updateAccessRule(String url, String username, String password, List<String> ruleNames,
                                    Template template, List<String> statusIds) throws DbException {
        DataSource newDataSource = ConnectionFactory.getDataSource(url, username, password);
        DBOperationStateObserver stateObserver = new DBOperationStateObserver(this);
        int totalCount = ruleNames.size() * statusIds.size();
        int count = 0;
        for (String ruleName : ruleNames) {
            for (String statusId : statusIds) {
                // each saving of rules should be performed in a separate
                // transaction. That's why we provide a new datasource for each operation.
                AccessListDao.getInstance(newDataSource).updateAccessByRuleAndStatus(ruleName, statusId, template, stateObserver, false);

                setProgress((count + 1) * StateObserver.MAX_PROGRESS / totalCount);
                count++;
            }
            // ���������� ������ �� �� ������ ����� ����, ��� ��� ����� ��� ���� �������� ����� ��������� � ��
            AccessListDao.getInstance(newDataSource).updatePermissionsByRuleName(template, ruleName, false, statusIds);
        }
    }

    private long updateAccessToRule(Connection connection, DataSource dataSource, PermissionWrapper.RuleType ruleType, Long ruleId) throws Exception {
        try {
            return AccessListDao.getInstance(dataSource).updateAccessListForRule(ruleType, ruleId);
        } catch (Exception e) {
            if (++errorsCount > ERROR_LIMIT) {
                logger.error(e);
                logger.error("Update canceled!!! Errors limit " + ERROR_LIMIT +" reached");
                throw e;
            } else {
                logger.error(e);
                logger.error("Update failed " + errorsCount + "times. Continue...");
                return 0;
            }
        }
    }

    public static class DBOperationStateObserver implements StateObserver{
        private final DBOperationUtil workerThread;


        public DBOperationStateObserver(DBOperationUtil workerThread) {
            this.workerThread = workerThread;
        }

        public void stateChanged(int progress) {
            workerThread.setProgress(progress);
        }

        public boolean isCancelled(){
            return workerThread.isCancel();
        }
    }
}
