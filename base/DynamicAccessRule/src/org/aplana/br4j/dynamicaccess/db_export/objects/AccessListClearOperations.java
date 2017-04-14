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
import org.aplana.br4j.dynamicaccess.xmldef.*;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

/**
 * @author etarakanov
 *         Date: 05.05.2015
 *         Time: 18:47
 */
public class AccessListClearOperations
{
    private static final Log logger = LogFactory.getLog(AccessListClearOperations.class);

    private static final String clearAccessList = "DELETE FROM access_list al USING access_rule ar where ar.rule_id = al.rule_id AND ar.perm_hash in (?)";
    private static final String clearAccessAttr = "DELETE FROM access_attr_rule par USING access_rule ar where ar.rule_id = par.rule_id AND ar.perm_hash in (?)";
    private static final String clearAccessCard = "DELETE FROM access_card_rule par USING access_rule ar where ar.rule_id = par.rule_id AND ar.perm_hash in (?)";
    private static final String clearAccessTemplate = "DELETE FROM access_template_rule par USING access_rule ar where ar.rule_id = par.rule_id AND ar.perm_hash in (?)";
    private static final String clearAccessMove = "DELETE FROM access_move_rule par USING access_rule ar where ar.rule_id = par.rule_id AND ar.perm_hash in (?)";
    private static final String clearAccessRuleList = "DELETE FROM access_rule par USING access_rule ar where ar.rule_id = par.rule_id AND ar.perm_hash in (?)";
    private static final String clearPersonAccess = "DELETE FROM person_access_rule par USING access_rule ar where ar.rule_id = par.rule_id AND ar.perm_hash in (?)";
    private static final String clearProfileAccess = "DELETE FROM profile_access_rule par USING access_rule ar where ar.rule_id = par.rule_id AND ar.perm_hash in (?)";
    private static final String clearRoleAccess = "DELETE FROM role_access_rule t USING access_rule ar where ar.rule_id = t.rule_id AND ar.perm_hash in (?)";
    private static final String clearDelegationAccess = "DELETE FROM delegation_access_rule par USING access_rule ar where ar.rule_id = par.rule_id AND ar.perm_hash in (?)";

    private static final int parameterIndex = 1;

    public static void clearAccessList(DataSource dataSource) {
        try {
            new JdbcTemplate(dataSource).execute("TRUNCATE  access_list");
        } catch (DataAccessException e) {
            logger.error("Exception occured" + e.getMessage());
            throw  e;
        }
    }

    public static void clearAccessList(DataSource dataSource, Long rule_id) {
        try {
            new JdbcTemplate(dataSource).execute("DELETE from access_list where rule_id = " + rule_id);
        } catch (DataAccessException e) {
            logger.error("Exception occured" + e.getMessage());
            throw  e;
        }
    }

    public static void clearRulesByRuleName(Set<Rule> rulesToDelete, Template template, DataSource dataSource, boolean partial) throws DbException {

        Connection connection;
        Map<String, PreparedStatement> statements;
        try {
            connection =  dataSource.getConnection();
            statements = getPrepareStatements(connection);
        } catch (SQLException e) {
            throw new DbException(e.getMessage());
        }
        for(Rule rule : rulesToDelete){
            if(rule!= null){
                // get rule ids from model by rule name
                Set<String> permHashes = getPermHashesByRuleNameFromModel(rule.getName(), template, partial);
                if(!partial){
                    Set<String> permHashesInDB = getRuleIdsByRuleNameFromDatabase(rule.getName(), template, dataSource);
                    permHashes.addAll(permHashesInDB);
                }
                clearRulesByRulePermHashes(permHashes, statements);
            }
        }
        try {
            connection.close();
        } catch (SQLException e) {
            throw new DbException(e.getMessage());
        }

    }

    public static void clearRulesByRuleNameAndStatus(Set<Rule> rulesToDelete, String statusId, Template template, DataSource dataSource, boolean partial) throws DbException {
        logger.info("Clear rules by rule name and status id");
        Connection connection;
        Map<String, PreparedStatement> statements;
        try {
            connection =  dataSource.getConnection();
            statements = getPrepareStatements(connection);
        } catch (SQLException e) {
            throw new DbException(e.getMessage());
        }
        for(Rule rule : rulesToDelete){
            if(rule!= null){
                Set<String> permHashes = getHashesByRuleNameAndStatusFromModel(rule.getName(), statusId, template, partial);
                if(!partial){
                    Set<String> permHashesInDB = getHashesByRuleNameAndStatusFromDatabase(rule.getName(), statusId, template, dataSource);
                    permHashes.addAll(permHashesInDB);
                }
                clearRulesByRulePermHashes(permHashes, statements);
            }
        }
        try {
            connection.close();
        } catch (SQLException e) {
            throw new DbException(e.getMessage());
        }
    }

    private static void clearRulesByRulePermHashes(final Set<String> permHashes, Map<String, PreparedStatement> statements) throws DbException {
        if(permHashes.isEmpty()){
            return;
        }
        logger.info("Clearing access rules for: " + permHashes);
        try {
            int deletedRows = clearAccessListBypermHashes(permHashes, statements.get("clearAccessList"));
            deletedRows += clearPersonRuleBypermHashes(permHashes, statements.get("clearPersonAccess"));
            deletedRows += clearProfileRuleBypermHashes(permHashes, statements.get("clearProfileAccess"));
            deletedRows += clearRoleRuleBypermHashes(permHashes, statements.get("clearRoleAccess"));
            deletedRows += clearDelegationRuleBypermHashes(permHashes, statements.get("clearDelegationAccess"));

            deletedRows += clearAccessAttrListBypermHashes(permHashes, statements.get("clearAccessAttr"));
            deletedRows += clearAccessCardRuleBypermHashes(permHashes, statements.get("clearAccessCard"));
            deletedRows += clearAccessTemplateRuleBypermHashes(permHashes, statements.get("clearAccessTemplate"));
            deletedRows += clearAccessMoveRuleBypermHashes(permHashes, statements.get("clearAccessMove"));
            deletedRows += clearAccessRuleBypermHashes(permHashes, statements.get("clearAccessRuleList"));
            logger.info("Rows deleted: " + deletedRows);

        } catch (SQLException ex) {
            throw new DbException(ex.getMessage());
        }
    }

    private static Map<String, PreparedStatement> getPrepareStatements (Connection connection) throws SQLException {
        Map<String, PreparedStatement> result = new HashMap<String, PreparedStatement>();
        try {
            result.put("clearAccessList", connection.prepareStatement(clearAccessList));
            result.put("clearAccessAttr", connection.prepareStatement(clearAccessAttr));
            result.put("clearAccessCard", connection.prepareStatement(clearAccessCard));
            result.put("clearAccessTemplate", connection.prepareStatement(clearAccessTemplate));
            result.put("clearAccessMove", connection.prepareStatement(clearAccessMove));
            result.put("clearAccessRuleList", connection.prepareStatement(clearAccessRuleList));
            result.put("clearPersonAccess", connection.prepareStatement(clearPersonAccess));
            result.put("clearProfileAccess", connection.prepareStatement(clearProfileAccess));
            result.put("clearRoleAccess", connection.prepareStatement(clearRoleAccess));
            result.put("clearDelegationAccess", connection.prepareStatement(clearDelegationAccess));
        } catch (SQLException e) {
            logger.error("Error while prepare statements", e);
            throw e;
        }
        return result;
    }

    /**
     * Returns rule ids for the given rule name.
     *
     * @param ruleName fully qualified name of the rule (with suffix SYSTEM if exists).
     * @param template template
     * @return set of rules id
     */
    private static Set<String> getRuleIdsByRuleNameFromDatabase(String ruleName, Template template, DataSource dataSource) {
        TemplateDao templateDao = new TemplateDao(dataSource);
        //get rule ids from database
        return templateDao.getRuleIdsByRuleName(ruleName, template);
    }

    /**
     * Returns rule ids for the given rule name and status id.
     * @param ruleName fully qualified name of the rule (with suffix SYSTEM if exists).
     * @param statusId status id used to find corresponding rule ids.
     * @param template {@link Template} containing specified rule ids.
     * @return {@link Set} of rule ids.
     */
    private static Set<String> getHashesByRuleNameAndStatusFromDatabase(String ruleName, String statusId, Template template, DataSource dataSource) {
        TemplateDao templateDao = new TemplateDao(dataSource);
        //get rule ids from database
        return templateDao.getRuleIdsByRuleNameAndStatus(ruleName, statusId, template);
    }

    private static Set<String> getPermHashesByRuleNameFromModel(String ruleName, Template template, boolean partial) {
        List<Permission> permissions = getPermissionsByRuleNameFromModel(ruleName, template);
        return getPermHashesFromPermissions(permissions, partial);
    }

    private static Set<String> getHashesByRuleNameAndStatusFromModel(String ruleName, String statusId, Template template, boolean partial) {
        List<Permission> permissions = statusId!=null?getPermissionsByRuleNameAndStatusFromModel(ruleName, statusId, template):getPermissionsByRuleNameFromModel(ruleName, template);
        return getPermHashesFromPermissions(permissions, partial);
    }

    private static List<Permission> getPermissionsByRuleNameFromModel(String ruleName, Template template) {
        Permission[] permissions = template.getPermission();
        List<Permission> foundPermissions = new ArrayList<Permission>();
        if (permissions != null) {
            for (Permission permission : permissions) {
                if (ruleName.equals(permission.getRule())) {
                    foundPermissions.add(permission);
                }
            }
        }
        return foundPermissions;
    }

    private static List<Permission> getPermissionsByRuleNameAndStatusFromModel(String ruleName, String statusId, Template template) {
        Permission[] permissions = template.getPermission();
        List<Permission> foundPermissions = new ArrayList<Permission>();

        if (permissions != null) {
            for (Permission permission : permissions) {
                if (ruleName.equals(permission.getRule())&&statusId.equals(permission.getStatus())) {
                    foundPermissions.add(permission);
                }
            }
        }
        return foundPermissions;
    }

    private static Set<String> getPermHashesFromPermissions(List<Permission> permissions, boolean partial) {
        Set<String> permHashes = new HashSet<String>();
        for(Permission permission : permissions) {
            if(permission.getOperations() != null){
                for(Operation operation : permission.getOperations().getOperations()){
                    if(operation.getPermHash() != null && (!partial || operation.getAction() != null) ){
                        permHashes.add(operation.getPermHash());
                    }
                }
            }
            if(permission.getWfMoves() != null){
                for(WfMove wfMove : permission.getWfMoves().getWfMove()){
                    if(wfMove.getPermHash() != null && (!partial || wfMove.getAction() != null)){
                        permHashes.add(wfMove.getPermHash());
                    }
                }
            }
        }
        return permHashes;
    }

    private static int clearAccessListBypermHashes(final Set<String> permHashes, PreparedStatement statement) throws SQLException {

        for (String permHashe : permHashes){
            statement.setString(parameterIndex, permHashe);
            statement.addBatch();
        }
        int[] resultOfBatchUpdate = statement.executeBatch();

        int deletedRows = getUpdatedRowCount(resultOfBatchUpdate);
        logger.debug("Access_list deleted rows: " + deletedRows);

        return deletedRows;
    }

    private static int clearAccessAttrListBypermHashes(final Set<String> permHashes, PreparedStatement statement) throws SQLException {

        for (String permHashe : permHashes){
            statement.setString(parameterIndex, permHashe);
            statement.addBatch();
        }
        int[] resultOfBatchUpdate = statement.executeBatch();
        int deletedRows = getUpdatedRowCount(resultOfBatchUpdate);
        logger.debug("access_attr_rule deleted rows: " + deletedRows);
        return deletedRows;

    }

    private static int clearAccessCardRuleBypermHashes(final Set<String> permHashes, PreparedStatement statement) throws SQLException {

        for (String permHashe : permHashes){
            statement.setString(parameterIndex, permHashe);
            statement.addBatch();
        }
        int[] resultOfBatchUpdate = statement.executeBatch();
        int deletedRows = getUpdatedRowCount(resultOfBatchUpdate);
        logger.debug("access_card_rule deleted rows: " + deletedRows);
        return deletedRows;
    }

    private static int clearAccessTemplateRuleBypermHashes(final Set<String> permHashes, PreparedStatement statement)throws SQLException {

        for (String permHashe : permHashes){
            statement.setString(parameterIndex, permHashe);
            statement.addBatch();
        }
        int[] resultOfBatchUpdate = statement.executeBatch();
        int deletedRows = getUpdatedRowCount(resultOfBatchUpdate);
        logger.debug("access_template_rule deleted rows: " + deletedRows);
        return deletedRows;
    }

    private static int clearAccessMoveRuleBypermHashes(final Set<String> permHashes, PreparedStatement statement) throws SQLException {

        for (String permHashe : permHashes){
            statement.setString(parameterIndex, permHashe);
            statement.addBatch();
        }
        int[] resultOfBatchUpdate = statement.executeBatch();
        int deletedRows = getUpdatedRowCount(resultOfBatchUpdate);
        logger.debug("access_move_rule deleted rows: " + deletedRows);
        return deletedRows;
    }

    private static int clearAccessRuleBypermHashes(final Set<String> permHashes, PreparedStatement statement) throws SQLException {

        for (String permHashe : permHashes){
            statement.setString(parameterIndex, permHashe);
            statement.addBatch();
        }

        int[] resultOfBatchUpdate = statement.executeBatch();
        int deletedRows = getUpdatedRowCount(resultOfBatchUpdate);
        logger.debug("access_rule deleted rows: " + deletedRows);
        return deletedRows;

    }

    private static int clearPersonRuleBypermHashes(final Set<String> permHashes, PreparedStatement statement) throws SQLException {

        for (String permHashe : permHashes){
            statement.setString(parameterIndex, permHashe);
            statement.addBatch();
        }
        int[] resultOfBatchUpdate = statement.executeBatch();
        int deletedRows = getUpdatedRowCount(resultOfBatchUpdate);
        logger.debug("person_access_rule deleted rows: " + deletedRows);
        return deletedRows;
    }

    private static int clearProfileRuleBypermHashes(final Set<String> permHashes, PreparedStatement statement) throws SQLException {

        for (String permHashe : permHashes){
            statement.setString(parameterIndex, permHashe);
            statement.addBatch();
        }
        int[] resultOfBatchUpdate = statement.executeBatch();
        int deletedRows = getUpdatedRowCount(resultOfBatchUpdate);
        logger.debug("profile_access_rule deleted rows: " + deletedRows);
        return deletedRows;
    }

    private static int clearRoleRuleBypermHashes(final Set<String> permHashes, PreparedStatement statement) throws SQLException {

        for (String permHashe : permHashes){
            statement.setString(parameterIndex, permHashe);
            statement.addBatch();
        }
        int[] resultOfBatchUpdate = statement.executeBatch();
        int deletedRows = getUpdatedRowCount(resultOfBatchUpdate);
        logger.debug("role_access_rule deleted rows: " + deletedRows);
        return deletedRows;
    }

    private static int clearDelegationRuleBypermHashes(final Set<String> permHashes, PreparedStatement statement) throws SQLException {

        for (String permHashe : permHashes){
            statement.setString(parameterIndex, permHashe);
            statement.addBatch();
        }
        int[] resultOfBatchUpdate = statement.executeBatch();
        int deletedRows = getUpdatedRowCount(resultOfBatchUpdate);
        logger.debug("delegation_access_rule deleted rows: " + deletedRows);
        return deletedRows;
    }

    private static int getUpdatedRowCount(int[] resultOfBatchUpdate) {
        int rows = 0;
        for (int element : resultOfBatchUpdate) {
            rows += element;
        }
        return rows;
    }

}
