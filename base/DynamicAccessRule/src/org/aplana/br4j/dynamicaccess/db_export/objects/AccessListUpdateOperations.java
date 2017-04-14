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

import com.aplana.dbmi.model.AccessRule;
import com.aplana.dbmi.model.PersonAccessRule;
import com.aplana.dbmi.model.PersonProfileAccessRule;
import com.aplana.dbmi.service.impl.access.AccessRuleManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aplana.br4j.dynamicaccess.RulesUtility;
import org.aplana.br4j.dynamicaccess.db_export.DbException;
import org.aplana.br4j.dynamicaccess.db_export.SaveAccessRulesManager;
import org.aplana.br4j.dynamicaccess.db_export.StateObserver;
import org.aplana.br4j.dynamicaccess.xmldef.Rule;
import org.aplana.br4j.dynamicaccess.xmldef.Template;
import org.aplana.br4j.dynamicaccess.xmldef.types.PermissionWrapper;
import org.aplana.br4j.dynamicaccess.xmldef.types.RuleTypeUtility;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

/**
 * @author etarakanov
 *         Date: 06.05.2015
 *         Time: 15:17
 */
public class AccessListUpdateOperations
{
    private static final Log logger = LogFactory.getLog(AccessListUpdateOperations.class);

    private static final String UPDATE_ACCESS_LIST_SQL =
            // Resolving rules with direct links
            "INSERT INTO access_list(card_id, rule_id, person_id, source_value_id) " +
                    // Person access rules
                    AccessRuleManager.SELECT_PERSON_ACCESS_CARD_THIS +
                    " UNION ALL " + AccessRuleManager.SELECT_PERSON_ACCESS_CARD_LINKED +
                    //" UNION ALL " + AccessRuleManager.SELECT_PERSON_ACCESS_CARD_USER_LINKED +
                    " UNION ALL " + AccessRuleManager.SELECT_PERSON_ACCESS_CARD_BACKLINK +
                    //��� ������ ��������� ���� �� �������� �� ��������� ������������� �����
                    //��������� ��������, �.�. ��� ��� ����� ������������� ��������.
                    //��������� � ������������� ������ � ������� access_list
                    //" UNION ALL " + AccessRuleManager.SELECT_PERSON_ACCESS_LINKED_CARD +
                    //" UNION ALL " + AccessRuleManager.SELECT_PERSON_ACCESS_BACKLINK_CARD +
                    // Profile access rules
                    " UNION ALL " + AccessRuleManager.SELECT_PROFILE_ACCESS_CARD_THIS +
                    " UNION ALL " + AccessRuleManager.SELECT_PROFILE_ACCESS_CARD_LINKED +
                    //" UNION ALL " + AccessRuleManager.SELECT_PROFILE_ACCESS_CARD_USER_LINKED +
                    " UNION ALL " + AccessRuleManager.SELECT_PROFILE_ACCESS_CARD_BACKLINK;
    //�� �� �������, ��� � ����������� �������� ����
    //" UNION ALL " + AccessRuleManager.SELECT_PROFILE_ACCESS_LINKED_CARD +
    //" UNION ALL " + AccessRuleManager.SELECT_PROFILE_ACCESS_BACKLINK_CARD;


    /**
     * Updates access list using batch update.
     * @param cardIds {@link List} of card id to be updated in one batch
     * @return the number of rows updated in one batch
     * @throws DbException if batch update fails.
     */
    public static long updateAccessToCardInBatch(final List<Long> cardIds, DataSource dataSource) throws DbException {
        if (cardIds == null || cardIds.isEmpty()){
            throw new IllegalArgumentException("Not valid  card ID");
        }
        long rows = 0;
        long start = System.currentTimeMillis();
        int[] resultOfBatchUpdate;
        logger.info("Batch update for cards " + Arrays.toString(cardIds.toArray()));
        // ��������� ���� ��� ����������� ���������
        String updateSQL = UPDATE_ACCESS_LIST_SQL.replaceAll(":curCardId", "?");

        resultOfBatchUpdate = new JdbcTemplate(dataSource).batchUpdate(updateSQL, new BatchPreparedStatementSetter() {

            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Long cardId = cardIds.get(i);
                logger.info("Batch update for card " + cardId);
                ps.setLong(1, cardId);
                ps.setLong(2, cardId);
                ps.setLong(3, cardId);
                ps.setLong(4, cardId);
                ps.setLong(5, cardId);
                ps.setLong(6, cardId);
                //ps.setLong(7, cardId);
                //ps.setLong(8, cardId);
				/*ps.setLong(9, cardId);
				ps.setLong(10, cardId);
				ps.setLong(11, cardId);
				ps.setLong(12, cardId);*/
            }

            public int getBatchSize() {
                return cardIds.size();
            }
        });
        logger.debug("Batch size : " + cardIds.size());

        rows += getUpdatedRowCount(resultOfBatchUpdate);

        long duration = System.currentTimeMillis() - start;
        logger.info(" Direct rules resolved in: " + duration + "ms");
        logger.info(rows + " permission(s) added in (" + duration + "ms)");
        return rows;
    }


    /**
     * Updates access to specified rule. if this method should be executed in one transaction put its calling in transaction.
     * @param ruleName the name of rule to be updated.
     * @param template the {@link Template} containing rule.
     * @param partial true if need to perform partial update access, else false
     * @throws DbException if operation fails.
     */
    public static void updateAccessByRule(String ruleName, Template template, StateObserver callback, DataSource dataSource, boolean partial) throws DbException {
        final DataSourceTransactionManager txManager = new DataSourceTransactionManager();
        txManager.setDataSource(dataSource);

        final DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
        transactionDefinition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        final TransactionStatus transactionStatus = txManager.getTransaction(transactionDefinition);
        Connection connection = null;
        try {
			connection = DataSourceUtils.getConnection(dataSource);
            PermissionWrapper.RuleType ruleType = RuleTypeUtility.getRuleTypeByRuleName(ruleName, template);

            Set<Rule> newAndDeletesRules = getRulesByRuleName(ruleName, template, dataSource);
            AccessListClearOperations.clearRulesByRuleName(newAndDeletesRules, template, dataSource, partial);

            Map<Rule, Set<String>> permHashesForRules= new HashMap<Rule, Set<String>>();
            List<Rule> rulesToUpdate = getRulesByRuleNameFromModel(ruleName, template);
            for(Rule rule : rulesToUpdate) {
                Set<String> permHashesForRule = saveRule(template, rule, connection, partial);
                permHashesForRules.put(rule, permHashesForRule);
            }

            txManager.commit(transactionStatus);
            // refresh the list of ruleIds, because if the rule was new after saving it the list of ruleIds will not be empty
            // ruleIds is used farther to update the table access_list

            for(Rule rule : newAndDeletesRules) {
                Set<String> permHashes;
                if(partial){
                    permHashes = permHashesForRules.get(rule);
                } else {
                    permHashes = new TemplateDao(dataSource).getRuleIdsByRuleName(rule.getName(), template);
                }

                updateAccessListForRules(callback, ruleType, permHashes, dataSource);
            }

            callback.stateChanged(StateObserver.MAX_PROGRESS);

            updatePermissionsByRuleName(template, ruleName, partial, null, dataSource);

        } catch (SQLException e) {
            txManager.rollback(transactionStatus);
            logger.error("Exception occured" + e.getMessage());
            throw new DbException(e.getMessage());
        }
        finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                logger.error("Exception occured" + e.getMessage());
                throw new DbException(e.getMessage());
            }
        }
    }

    /**
     * Updates access to specified rule. if this method should be executed in one transaction put its calling in transaction.
     * @param ruleName the name of rule from the Application (without suffix SYSTEM).
     * @param template the {@link Template} containing rule.
     * @param partial true if need to perform partial update access, else false
     * @throws DbException if operation fails.
     * ������������� �� ��, ����� ������� ������ ������� �� ������� ������� � ��������� �������������� �������
     */
    public static void updateAccessByRuleAndStatus(String ruleName, String statusId, Template template, StateObserver callback,  DataSource dataSource, boolean partial) throws DbException {
        final DataSourceTransactionManager txManager = new DataSourceTransactionManager();
        txManager.setDataSource(dataSource);

        final DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
        transactionDefinition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        final TransactionStatus transactionStatus = txManager.getTransaction(transactionDefinition);
        Connection connection = null;
        try {
			connection = DataSourceUtils.getConnection(dataSource);
            //delete all rules (new and stored in DB)
            Set<Rule> allRules = getRulesByRuleName(ruleName, template, dataSource);
            AccessListClearOperations.clearRulesByRuleNameAndStatus(allRules, statusId, template, dataSource, partial);
            //save only rules from Model
            List<Rule> rulesToUpdate = getRulesByRuleNameFromModel(ruleName, template);

            Map<Rule, Set<String>> permHashesForRules= new HashMap<Rule, Set<String>>();
            for(Rule rule : rulesToUpdate) {
                Set<String> permHashesForRule = saveRule(template, rule, statusId, connection, partial);
                permHashesForRules.put(rule, permHashesForRule);
            }

            txManager.commit(transactionStatus);

            // refresh the list of ruleIds, because if the rule was new after saving it the list of ruleIds will not be empty
            // ruleIds is used farther to update the table access_list
            PermissionWrapper.RuleType ruleType = RuleTypeUtility.getRuleTypeByRuleName(ruleName, template);
            for(Rule rule : rulesToUpdate) {
                Set<String> permHashes;
                if(partial){
                    permHashes = permHashesForRules.get(rule);
                } else {
                    permHashes = new TemplateDao(dataSource).getRuleIdsByRuleNameAndStatus(rule.getName(), statusId, template);
                }

                updateAccessListForRules(callback, ruleType, permHashes, dataSource);
            }

            callback.stateChanged(StateObserver.MAX_PROGRESS);

            //updatePermissionsByRuleName(template, ruleName);

        } catch (SQLException e) {
            txManager.rollback(transactionStatus);
            logger.error("Exception occured" + e.getMessage());
            throw new DbException(e.getMessage());
        }
        finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                logger.error("Exception occured" + e.getMessage());
                throw new DbException(e.getMessage());
            }
        }
    }

    public static long updateAccessListForRule(PermissionWrapper.RuleType ruleType, Long ruleId, DataSource dataSource)
            throws DbException {
        AccessRule accessRule = null;
        boolean isUpdateAccessList = false;

        Connection connection;
        try {
            connection = dataSource.getConnection();
        } catch (SQLException e) {
            logger.error("Exception occured" + e.getMessage());
            throw new DbException(e.getMessage());
        }

        RuleAttributeDao ruleAttributeDao = new RuleAttributeDao(connection, dataSource);
        if (ruleType.isPersonType()) {
            accessRule = getPersonAccessRule(ruleId, ruleAttributeDao);
            isUpdateAccessList = true;
        } else if (ruleType.isProfileType()) {
            accessRule = getProfileAccessRule(ruleId, ruleAttributeDao);
            isUpdateAccessList = true;
        }

        long rows=0;
        if (isUpdateAccessList) {
            // update access list
            AccessRuleManager accessRuleManager = new AccessRuleManager(new JdbcTemplate(dataSource));
            rows += accessRuleManager.applyNewRule(accessRule);
        }

        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            logger.error("Exception occured" + e.getMessage());
            throw new DbException(e.getMessage());
        }

        logger.info("Loaded rule id: " + ruleId + ", accessRules: " + accessRule);
        return rows;
    }

    /**
     * ���������� ������ ������ �� ������ � �� �� �� ����� �������, ���������� � ������� (��� �������� SYSTEM).
     * @param ruleName ��� �������
     * @param template ������
     * @return ������ ������
     * @throws DbException
     */

    public static Set<Rule> getRulesByRuleName(String ruleName, Template template, DataSource dataSource) throws DbException {
        List<Rule> rulesFromDataBase = getRulesByRuleNameFromDB(ruleName, template, dataSource);
        List<Rule> rulesFromModel = getRulesByRuleNameFromModel(ruleName, template);
        Set<Rule> rulesToUpdate = new HashSet<Rule>();
        rulesToUpdate.addAll(rulesFromModel);
        rulesToUpdate.addAll(rulesFromDataBase);
        return rulesToUpdate;
    }

    /**
     * ���������� ������ ������ �� ������ �� ����� �������, ���������� � ������� (��� �������� SYSTEM).
     * @param ruleName ��� �������
     * @param template ������
     * @return ������ ������
     */

    private static List<Rule> getRulesByRuleNameFromModel(String ruleName, Template template) {
        List<Rule> rulesList = Arrays.asList(template.getRules().getRule());
        return RulesUtility.getRulesByRuleName(ruleName, rulesList);
    }

    /**
     * ���������� ������ ������ �� �� �� ����� �������, ���������� � ������� (��� �������� SYSTEM).
     * @param ruleName ��� �������
     * @param template ������
     * @return ������ ������
     * @throws DbException
     */
    private static List<Rule> getRulesByRuleNameFromDB(String ruleName, Template template, DataSource dataSource) throws DbException {
        TemplateDao templateDao = new TemplateDao(dataSource);
        PermissionWrapper.RuleType ruleType = RuleTypeUtility.getRuleTypeByRuleName(ruleName, template);
        List<Rule> rulesFromDataBase = templateDao.getRulesForTemplate(template, ruleType);

        return RulesUtility.getRulesByRuleName(ruleName, rulesFromDataBase);
    }

    private static long updateAccessListForRules(StateObserver callback, PermissionWrapper.RuleType ruleType, Set<String> permHashes, DataSource dataSource)
            throws DbException {
        List<AccessRule> accessRules = null;
        boolean isUpdateAccessList = false;
        RuleAttributeDao ruleAttributeDao = new RuleAttributeDao(dataSource);
        if (ruleType.isPersonType()) {
            accessRules = getPersonAccessRules(permHashes, ruleAttributeDao);
            isUpdateAccessList = true;
        } else if (ruleType.isProfileType()) {
            accessRules = getProfileAccessRules(permHashes, ruleAttributeDao);
            isUpdateAccessList = true;
        }

        long rows = 0;
        if (isUpdateAccessList) {
            // update access list
            int index = 0;
            for (AccessRule accessRule : accessRules) {
                if (callback.isCancelled()) {
                    logger.debug("Cancelled by user");
                    break;
                }
                callback.stateChanged(StateObserver.MAX_PROGRESS * (index + 1) / accessRules.size());
                AccessRuleManager accessRuleManager = new AccessRuleManager(new JdbcTemplate(dataSource));
                rows+=accessRuleManager.applyNewRule(accessRule);
                index++;
            }
        }

        logger.info("Loaded rule ids: " + permHashes + ", accessRules: " + accessRules);
        return rows;
    }

    private static Set<String> saveRule(Template template, Rule rule, Connection connection, boolean partial) throws DbException, SQLException {
        SaveAccessRulesManager saveAccessRulesManager = new SaveAccessRulesManager(connection);
        saveAccessRulesManager.setPartial(partial);
        return saveAccessRulesManager.processRule(rule, template);
    }

    private static Set<String> saveRule(Template template, Rule rule, String statusId, Connection connection, boolean partial) throws DbException, SQLException {
        SaveAccessRulesManager saveAccessRulesManager = new SaveAccessRulesManager(connection);
        saveAccessRulesManager.setUniqueStatusId(statusId);
        saveAccessRulesManager.setPartial(partial);
        return saveAccessRulesManager.processRule(rule, template);
    }

    public static void updatePermissionsByRuleName(Template template, String ruleName, boolean partial, List<String> statuses, DataSource dataSource) throws DbException {
        TemplateDao templateDao = new TemplateDao(dataSource);
        templateDao.updatePermissionByRuleName(template, ruleName, partial, statuses);
    }

    private static AccessRule getProfileAccessRule(Long ruleId, RuleAttributeDao ruleAttributeDao) throws DbException {
        PersonProfileAccessRule profileRule = new PersonProfileAccessRule();
        profileRule.setId(ruleId);
        com.aplana.dbmi.model.Attribute linkAttribute = ruleAttributeDao.getLinkAttributeProfileByRuleId(ruleId);
        com.aplana.dbmi.model.Attribute intermedLinkAttribute = ruleAttributeDao.getIntermedLinkAttributeProfileByRuleId(ruleId);
        profileRule.setLinkAttribute(linkAttribute);
        profileRule.setIntermediateLinkAttribute(intermedLinkAttribute);
        profileRule.setLinkedStateId(ruleAttributeDao.getLinkStateIdProfileByRuleId(ruleId));
        profileRule.setRoleId(ruleAttributeDao.getRoleCodeProfileByRuleId(ruleId));
        return profileRule;
    }

    private static List<AccessRule> getProfileAccessRules(Set<String> permHashes, RuleAttributeDao ruleAttributeDao) throws DbException {
        List<AccessRule> accessRules = new ArrayList<AccessRule>();
        for(String permHashe : permHashes){
            accessRules.add(getProfileAccessRule(ruleAttributeDao.getRuleIdByPermHash(permHashe), ruleAttributeDao));
        }
        return accessRules;
    }

    private static AccessRule getPersonAccessRule(Long ruleId, RuleAttributeDao ruleAttributeDao) throws DbException {
        PersonAccessRule personRule = new PersonAccessRule();
        personRule.setId(ruleId);
        com.aplana.dbmi.model.Attribute linkAttribute = ruleAttributeDao.getLinkAttributePersonByRuleId(ruleId);
        com.aplana.dbmi.model.Attribute intermedLinkAttribute = ruleAttributeDao.getIntermedLinkAttributePersonByRuleId(ruleId);
        personRule.setLinkAttribute(linkAttribute);
        personRule.setIntermediateLinkAttribute(intermedLinkAttribute);
        personRule.setLinkedStateId(ruleAttributeDao.getLinkStateIdPersonByRuleId(ruleId));
        personRule.setRoleId(ruleAttributeDao.getRoleCodePersonByRuleId(ruleId));
        return personRule;
    }

    private static List<AccessRule> getPersonAccessRules(Set<String> permHashes, RuleAttributeDao ruleAttributeDao) throws DbException {
        List<AccessRule> accessRules = new ArrayList<AccessRule>();
        for(String permHashe : permHashes){
            accessRules.add(getPersonAccessRule(ruleAttributeDao.getRuleIdByPermHash(permHashe), ruleAttributeDao));
        }
        return accessRules;
    }

    private static int getUpdatedRowCount(int[] resultOfBatchUpdate) {
        int rows = 0;
        for (int element : resultOfBatchUpdate) {
            rows += element;
        }
        return rows;
    }
}
