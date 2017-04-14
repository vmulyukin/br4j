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
package org.aplana.br4j.dynamicaccess.db_export.subjects;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aplana.br4j.dynamicaccess.RulesUtility;
import org.aplana.br4j.dynamicaccess.db_export.AccessRule;
import org.aplana.br4j.dynamicaccess.db_export.BaseAccessDao;
import org.aplana.br4j.dynamicaccess.db_export.DoSaveAccessRules;
import org.aplana.br4j.dynamicaccess.xmldef.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class PermissionSubjectDao extends BaseAccessDao {

    protected final Log logger = LogFactory.getLog(getClass());

    private Rule subject;

    public PermissionSubjectDao(Rule subject, Connection connection) {
        super(connection);
        this.subject = subject;
    }

    public void insertSubject() throws SQLException {
        if (subject.getRulePerson() != null) {
            insertPersonAccessRule(subject.getRulePerson());
        } else if (subject.getRuleRole() != null) {
            insertRoleAccessRule(subject.getRuleRole());
        } else if (DoSaveAccessRules.NO_RULE.equals(subject.getName())) {
            RuleRole emptyRole = new RuleRole();
            emptyRole.setName(DoSaveAccessRules.NO_RULE);
            emptyRole.setRoleCode(null);
            insertRoleAccessRule(emptyRole);
        } else if (subject.getRuleDelegation() != null) {
            insertDelegationAccessRule(subject.getRuleDelegation());
        } else if (subject.getRuleProfile() != null) {
            insertProfileAccessRule(subject.getRuleProfile());
        } else {
            throw new IllegalStateException("Unknown subject: " + subject);
        }
        logger.debug("Subject added " + subject);
    }

    private void insertRoleAccessRule(RuleRole rp) throws SQLException {
    	PreparedStatement updateNameStatment = connection.prepareStatement(
    			"update role_access_rule rr set description =  ? ||  \n"+ 
    			"\t coalesce(? || (regexp_split_to_array(rr.description, ?))[2],'')\n" +
    			"FROM access_rule ar \n"+
    			"where rr.rule_hash = ? and rr.rule_id = ar.rule_id and ar.template_id = ?");
    	updateNameStatment.setString(1, rp.getName().split(RulesUtility.RULE_NAME_SUFFIX)[0]);
    	updateNameStatment.setString(2,RulesUtility.RULE_NAME_SUFFIX);
    	updateNameStatment.setString(3,RulesUtility.RULE_NAME_SUFFIX);
    	updateNameStatment.setString(4, rp.getRuleHash());
    	updateNameStatment.setLong(5, ar.getTemplateId());
    	updateNameStatment.execute();
    	
        PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO role_access_rule (rule_id, role_code, description, rule_hash) values (?, ?, ?, ?)");
        statement.setLong(1, ar.getRuleId());
        if (DoSaveAccessRules.NO_RULE.equals(rp.getName())) {
            statement.setNull(2, Types.VARCHAR);
        } else {
            statement.setString(2, rp.getRoleCode());
        }
        statement.setString(3, rp.getName());
        setPrepStmString(statement, 4, rp.getRuleHash());
        statement.executeUpdate();
    }

    private void insertPersonAccessRule(RulePerson rp) throws SQLException {
    	PreparedStatement updateNameStatment = connection.prepareStatement(
    			"update person_access_rule rr set description =  ? ||  \n"+ 
    			"\t coalesce(? || (regexp_split_to_array(rr.description, ?))[2],'')\n" +
    			"FROM access_rule ar \n"+
    			"where rr.rule_hash = ? and rr.rule_id = ar.rule_id and ar.template_id = ?");
    	updateNameStatment.setString(1, rp.getName().split(RulesUtility.RULE_NAME_SUFFIX)[0]);
    	updateNameStatment.setString(2,RulesUtility.RULE_NAME_SUFFIX);
    	updateNameStatment.setString(3,RulesUtility.RULE_NAME_SUFFIX);
    	updateNameStatment.setString(4, rp.getRuleHash());
    	updateNameStatment.setLong(5, ar.getTemplateId());
    	updateNameStatment.execute();
        PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO person_access_rule (rule_id, person_attr_code, link_attr_code" +
                        ", intermed_attr_code, description" +
                        ", linked_status_id, role_code, rule_hash) values (?, ?, ?, ?, ?, ?, ?, ?)");
        statement.setLong(1, ar.getRuleId());
        setPrepStmString(statement, 2, rp.getPersonAttributeCode());
        setPrepStmString(statement, 3, rp.getLink());
        setPrepStmString(statement, 4, rp.getIntermedAttributeCode());
        statement.setString(5, rp.getName());
        setPrepStmInt(statement, 6, rp.getLinkedStatusId());
        setPrepStmString(statement, 7, rp.getRoleCode());
        setPrepStmString(statement, 8, rp.getRuleHash());
        statement.executeUpdate();
    }


    private void insertProfileAccessRule(RuleProfile rp) throws SQLException {
    	PreparedStatement updateNameStatment = connection.prepareStatement(
    			"update profile_access_rule rr set description =  ? ||  \n"+ 
    			"\t coalesce(? || (regexp_split_to_array(rr.description, ?))[2],'')\n" +
    			"FROM access_rule ar \n"+
    			"where rr.rule_hash = ? and rr.rule_id = ar.rule_id and ar.template_id = ?");
    	updateNameStatment.setString(1, rp.getName().split(RulesUtility.RULE_NAME_SUFFIX)[0]);
    	updateNameStatment.setString(2,RulesUtility.RULE_NAME_SUFFIX);
    	updateNameStatment.setString(3,RulesUtility.RULE_NAME_SUFFIX);
    	updateNameStatment.setString(4, rp.getRuleHash());
    	updateNameStatment.setLong(5, ar.getTemplateId());
    	updateNameStatment.execute();
    	
        PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO profile_access_rule (rule_id, profile_attr_code, target_attr_code" +
                        ", link_attr_code, intermed_attr_code, description" +
                        ", linked_status_id,  role_code, rule_hash) values (?, ?, ?, ?, ?, ?, ?, ?, ?)");
        statement.setLong(1, ar.getRuleId());
        setPrepStmString(statement, 2, rp.getProfileAttributeCode());
        setPrepStmString(statement, 3, rp.getTargetAttributeCode());
        setPrepStmString(statement, 4, rp.getLinkAttributeCode());
        setPrepStmString(statement, 5, rp.getIntermedAttributeCode());
        statement.setString(6, rp.getName());
        setPrepStmInt(statement, 7, rp.getLinkedStatusId());
        setPrepStmString(statement, 8, rp.getRoleCode());
        setPrepStmString(statement,9, rp.getRuleHash());
        statement.executeUpdate();
    }

    private void insertDelegationAccessRule(RuleDelegation rp) throws SQLException {
    	PreparedStatement updateNameStatment = connection.prepareStatement(
    			"update delegation_access_rule rr set description =  ? ||  \n"+ 
    			"\t coalesce(? || (regexp_split_to_array(rr.description, ?))[2],'')\n" +
    			"FROM access_rule ar \n"+
    			"where rr.rule_hash = ? and rr.rule_id = ar.rule_id and ar.template_id = ?");
    	updateNameStatment.setString(1, rp.getName().split(RulesUtility.RULE_NAME_SUFFIX)[0]);
    	updateNameStatment.setString(2,RulesUtility.RULE_NAME_SUFFIX);
    	updateNameStatment.setString(3,RulesUtility.RULE_NAME_SUFFIX);
    	updateNameStatment.setString(4, rp.getRuleHash());
    	updateNameStatment.setLong(5, ar.getTemplateId());
    	updateNameStatment.execute();
    	
        PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO delegation_access_rule (rule_id, link_attr_code, description, rule_hash) values (?, ?, ?, ?)");
        statement.setLong(1, ar.getRuleId());
        setPrepStmString(statement, 2, rp.getLinkAttributeCode());
        statement.setString(3, rp.getName());
        setPrepStmString(statement, 4, rp.getRuleHash());
        statement.executeUpdate();
    }

    public String getName() {
        return subject.getName();
    }
}
