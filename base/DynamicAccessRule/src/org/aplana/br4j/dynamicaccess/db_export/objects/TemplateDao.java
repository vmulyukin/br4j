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

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aplana.br4j.dynamicaccess.EditConfigMainForm;
import org.aplana.br4j.dynamicaccess.RulesUtility;
import org.aplana.br4j.dynamicaccess.db_export.BaseAccessDao;
import org.aplana.br4j.dynamicaccess.db_export.DbException;
import org.aplana.br4j.dynamicaccess.db_export.DoSaveAccessRules;
import org.aplana.br4j.dynamicaccess.xmldef.AttributeRule;
import org.aplana.br4j.dynamicaccess.xmldef.Operation;
import org.aplana.br4j.dynamicaccess.xmldef.Operations;
import org.aplana.br4j.dynamicaccess.xmldef.Permission;
import org.aplana.br4j.dynamicaccess.xmldef.Rule;
import org.aplana.br4j.dynamicaccess.xmldef.RuleDelegation;
import org.aplana.br4j.dynamicaccess.xmldef.RulePerson;
import org.aplana.br4j.dynamicaccess.xmldef.RuleProfile;
import org.aplana.br4j.dynamicaccess.xmldef.RuleRole;
import org.aplana.br4j.dynamicaccess.xmldef.Rules;
import org.aplana.br4j.dynamicaccess.xmldef.Status;
import org.aplana.br4j.dynamicaccess.xmldef.Template;
import org.aplana.br4j.dynamicaccess.xmldef.WFMoveType;
import org.aplana.br4j.dynamicaccess.xmldef.WfMove;
import org.aplana.br4j.dynamicaccess.xmldef.WfMoves;
import org.aplana.br4j.dynamicaccess.xmldef.types.OperationType;
import org.aplana.br4j.dynamicaccess.xmldef.types.PermissionCloner;
import org.aplana.br4j.dynamicaccess.xmldef.types.PermissionWrapper.RuleType;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

/**
 * Provides functionality for loading {@link Template}'s and data for them.
 * Provides caching of {@link PreparedStatement}'s uses to load Permission data.
 * So when loading list of {@link Template}'s we should one istance of this DAO.
 * 
 * @author atsvetkov
 * 
 */
public class TemplateDao extends BaseAccessDao {

	private static final String NULL_VALUE = "null";

	protected final Log logger = LogFactory.getLog(getClass());

    public final static String NO_RULE = "NO_RULE";
    public final static String NO_STATUS = "NO_STATUS";
    
    private static final String OPERATION_READ = "R";
    private static final String OPERATION_WRITE = "W";

	private static final String GET_ALL_TEMPLATE_SQL = "select template_name_rus, template_id from dbmi_trunk.template order by 2";

	private static final String GET_WORKFLOWS_SQL = "select coalesce(wm.name_rus, cs_to.default_move_name_rus), wm.wfm_id, cs_from.name_rus, "
        + " cs_to.name_rus, wm.from_status_id, wm.to_status_id "
        + " from workflow_move wm "
        + " inner join template t on t.workflow_id=wm.workflow_id "
        + " join card_status cs_from on cs_from.status_id = wm.from_status_id "
        + " join card_status cs_to on cs_to.status_id = wm.to_status_id  "
        + " where t.template_id= ? "
        + " order by 1";

	private static final String GET_STATUSES_SQL = "select distinct name_rus, status_id from card_status where status_id in ( "
        + " select wm.from_status_id from workflow_move wm "
        + " where workflow_id in (select workflow_id from template where template_id = ? ) "
        + " union select wm.to_status_id from workflow_move wm "
        + " where workflow_id in (select workflow_id from template where template_id = ? )) order by name_rus asc";

	
	private static final String GET_ATTRIBUTE_RULE_FOR_TEMPLATE_SQL = "select DISTINCT "
        + " t.template_name_rus, ab.block_name_rus, a.attribute_code, a.attr_name_rus, a.data_type "
        + " from template t "
        + " join template_attribute ta on ta.template_id = t.template_id "
        + " join attr_block ab on ab.block_code=ta.block_code"
        + " join attribute a on a.block_code = ab.block_code"
        + " where t.template_id = ? "
        // �������� �������������� ���������� �� �� � ���: ������������� ������������� ��������� � ����������� ����������� � LINK-��������
        + " and (data_type in ('C', 'U', 'E') "
        + "	or (data_type in ('B') and not exists(select 1 from attribute_option ao where ao.attribute_code = a.attribute_code and ao.option_code = 'LINK' and ao.option_value like '%;%')))"
        + " order by block_name_rus, a.attr_name_rus";

	private static final String GET_STATIC_ATTRIBUTE_RULE_FOR_TEMPLATE_SQL = "select null,'����������� ����',role_code, role_name_rus, 'STAT' from system_role order by role_name_rus";
	
	private static final String GET_PERSON_RULE_FOR_TEMPLATE_SQL = "select distinct person_attr_code, link_attr_code, intermed_attr_code "
        + ",description, linked_status_id, role_code, rule_hash "
        + " FROM dbmi_trunk.person_access_rule par "
        + " join attribute att on (att.attribute_code = par.person_attr_code) "
        + " where par.rule_id in (select rule_id from dbmi_trunk.access_rule where template_id = ? ) order by description asc";

	private static final String GET_ROLE_RULE_FOR_TEMPLATE_SQL = "select distinct role_code, description, rule_hash FROM dbmi_trunk.role_access_rule where "
        + "rule_id in (select rule_id from dbmi_trunk.access_rule where template_id = ? "
        + ") order by description asc";
	
	// ������� ��������� ������ �� ���������� ����������� ������ ��� ���� ������������� (NO_RULE)1
	private static final String GET_RULE_FOR_TEMPLATE_SQL = "select distinct role_code, description FROM dbmi_trunk.role_access_rule where "
        + "rule_id in (select rule_id from dbmi_trunk.access_rule where template_id = ? "
        + ") and description = '"+NO_RULE+"' order by description asc";

	private static final String GET_PROFILE_RULE_FOR_TEMPLATE_SQL = "select  distinct profile_attr_code, target_attr_code, link_attr_code, intermed_attr_code "
        + ", description, linked_status_id, role_code, rule_hash FROM dbmi_trunk.profile_access_rule where "
        + "rule_id in (select rule_id from dbmi_trunk.access_rule where template_id = ? "
        + ") order by description asc";
	        
	private static final String GET_DELEGATION_RULE_FOR_TEMPLATE_SQL = "select distinct link_attr_code, description, rule_hash FROM dbmi_trunk.delegation_access_rule where "
        + "rule_id in (select rule_id from dbmi_trunk.access_rule where template_id = ? "
        + ") order by description asc";
	
	
	private static final String GET_PERMISSION_FOR_TEMPLATE_SQL = "select  distinct status_id, coalesce(rar.description, par.description, prar.description, dar.description)  role_name " 
	 + " from access_rule ar " 
	 + " left outer join role_access_rule rar on ar.rule_id=rar.rule_id "
	 + " left outer join access_card_rule acr on ar.rule_id=acr.rule_id "
	 + " left outer join access_template_rule atr on ar.rule_id=atr.rule_id "
	 + " left outer join access_move_rule amr on ar.rule_id=amr.rule_id "
	 + " left outer join person_access_rule par on ar.rule_id=par.rule_id "
	 + " left outer join profile_access_rule prar on ar.rule_id=prar.rule_id "
	 + " left outer join delegation_access_rule dar on ar.rule_id=dar.rule_id "
	 + " where template_id=? and (atr.operation_code is not null or acr.operation_code is not null or amr.wfm_id is not null)"
	 + " order by status_id, role_name ";

	private static final String STATUS_ID = "status_id";	
	private static final String ROLE_NAME = "role_name";
	private static final String CREATE_PERM = "create_perm";
	private static final String RW_PERM = "rw_perm";
	private static final String WFM_ID = "wfm_id";
	private static final String RULE_ID = "rule_id";
	private static final String PERM_HASH = "perm_hash";
	private static final String RULE_HASH = "rule_hash";
	
	
	private static final String GET_PERMISSION_DATA_FOR_TEMPLATE_SQL = "select ar.rule_id " + RULE_ID + ", ar.status_id " + STATUS_ID + ", coalesce(rar.description, par.description, prar.description, dar.description) " + ROLE_NAME + ", " 
		 +	" atr.operation_code " + CREATE_PERM + ", acr.operation_code " + RW_PERM + ", amr.wfm_id " + WFM_ID + ", ar.perm_hash " + PERM_HASH + " "
		 + " from access_rule ar " 
		 + " left outer join role_access_rule rar on ar.rule_id=rar.rule_id "
		 + " left outer join access_card_rule acr on ar.rule_id=acr.rule_id "
		 + " left outer join access_template_rule atr on ar.rule_id=atr.rule_id "
		 + " left outer join access_move_rule amr on ar.rule_id=amr.rule_id "
		 + " left outer join person_access_rule par on ar.rule_id=par.rule_id "
		 + " left outer join profile_access_rule prar on ar.rule_id=prar.rule_id "
		 + " left outer join delegation_access_rule dar on ar.rule_id=dar.rule_id "
		 + " where template_id=? "
		 + " order by status_id, role_name, create_perm, rw_perm, wfm_id ";

	private static final String GET_PERMISSION_DATA_BY_RULE_NAME_STATUS_FOR_TEMPLATE_SQL = "select ar.rule_id " + RULE_ID + ", ar.status_id " + STATUS_ID + ", coalesce(rar.description, par.description, prar.description, dar.description) " + ROLE_NAME + ", " 
	 +	" atr.operation_code " + CREATE_PERM + ", acr.operation_code " + RW_PERM + ", amr.wfm_id " + WFM_ID + " "
	 + " from access_rule ar " 
	 + " left outer join role_access_rule rar on ar.rule_id=rar.rule_id "
	 + " left outer join access_card_rule acr on ar.rule_id=acr.rule_id "
	 + " left outer join access_template_rule atr on ar.rule_id=atr.rule_id "
	 + " left outer join access_move_rule amr on ar.rule_id=amr.rule_id "
	 + " left outer join person_access_rule par on ar.rule_id=par.rule_id "
	 + " left outer join profile_access_rule prar on ar.rule_id=prar.rule_id "
	 + " left outer join delegation_access_rule dar on ar.rule_id=dar.rule_id "
	 + " where template_id=? and (atr.operation_code is not null or acr.operation_code is not null or amr.wfm_id is not null) "
	 + " and ( (rar.description='?' or par.description='?' or prar.description='?' or dar.description='?' ) ) "
	 + " and status_id=? "
	 + " order by status_id, role_name, create_perm, rw_perm, wfm_id ";

	private static final String GET_RULE_IDS_BY_RULE_NAME_FOR_TEMPLATE_SQL = "select ar.rule_id " + RULE_ID + ", ar.status_id " + STATUS_ID + ", coalesce(rar.description, par.description, prar.description, dar.description) " + ROLE_NAME + ", rar.description, par.description, prar.description, dar.description" 
	 +	" atr.operation_code " + CREATE_PERM + ", acr.operation_code " + RW_PERM + ", amr.wfm_id " + WFM_ID + " "
	 + " from access_rule ar " 
	 + " left outer join role_access_rule rar on ar.rule_id=rar.rule_id "
	 + " left outer join access_card_rule acr on ar.rule_id=acr.rule_id "
	 + " left outer join access_template_rule atr on ar.rule_id=atr.rule_id "
	 + " left outer join access_move_rule amr on ar.rule_id=amr.rule_id "
	 + " left outer join person_access_rule par on ar.rule_id=par.rule_id "
	 + " left outer join profile_access_rule prar on ar.rule_id=prar.rule_id "
	 + " left outer join delegation_access_rule dar on ar.rule_id=dar.rule_id "
	 + " where template_id=? and (rar.description='?' or par.description='?' or prar.description='?' or dar.description='?') and (atr.operation_code is not null or acr.operation_code is not null or amr.wfm_id is not null) "
	 + " ";		
	
	public final static String READ = "R";
    public final static String WRITE = "W";
    public final static String CREATE = "C";
    public final static String CREATE_FOR_ID = DoSaveAccessRules.CREATE_DESCR;
    
    private PreparedStatement getWfMoveTypesPrepStatement;
    private PreparedStatement getStatusesPrepStatement;
    private PreparedStatement getAttributeRulesPrepStatement;
    private PreparedStatement getStaticAttributeRulesPrepStatement;
    private PreparedStatement getRoleRulesPrepStatement;
    private PreparedStatement getRulesPrepStatement;
    private PreparedStatement getPersonRulesPrepStatement;
    private PreparedStatement getProfileRulesPrepStatement;
    private PreparedStatement getDelegationRulesPrepStatement;
    private PreparedStatement getPermissionsPrepStatement;
    
    public TemplateDao(DataSource dataSource) {
    	super(dataSource);
    }    
    	
    public static Template createTemplate(String name, String templateId) {
        Template template = new Template();
    	Rules rules = new Rules();
        Rule rule = new Rule();                
        rule.setName(NO_RULE);
        Status status = new Status();
        status.setName(NO_STATUS);
        status.setStatus_id(NO_STATUS);
        template.setRules(rules);
        template.getRules().addRule(rule);
        template.addStatus(status);
        template.setName(name);
        template.setTemplate_id(templateId);
		return template;
    }
    
    /**
     * Gets list of {@link WFMoveType} for given {@link Template}
     * @param template
     * @return
     */
    public List<WFMoveType> getWorkflowsForTemplate(Template template) throws DbException {
    	List<WFMoveType> wfMoveTypes = new ArrayList<WFMoveType>();
    	try {
	    	if(getWfMoveTypesPrepStatement == null){
					getWfMoveTypesPrepStatement = getJdbcTemplate().getDataSource().getConnection().prepareStatement(GET_WORKFLOWS_SQL);
	    	}
	    	getWfMoveTypesPrepStatement.setLong(1, Long.parseLong(template.getTemplate_id()));
	    	
	    	ResultSet resultSet = getWfMoveTypesPrepStatement.executeQuery();
	    	while(resultSet.next()){
	    		WFMoveType wfMoveType = mapToWFMoveType(resultSet);
	    		if(wfMoveType != null){
	    			wfMoveTypes.add(wfMoveType);
	    		}
	    	}
	    	
		} catch (SQLException e) {
			logger.error("Exception occured: " + e.getMessage());
			throw new DbException(e.getMessage(), e);
		}
		return wfMoveTypes;		
    }

    private WFMoveType mapToWFMoveType(ResultSet resultSet) throws SQLException {
		WFMoveType wfMoveType = new WFMoveType();

		String workflowName = resultSet.getString(1);
		String workflowId = resultSet.getString(2);
		String workflowFrom = resultSet.getString(3);
		String res_workflow_to = resultSet.getString(4);
		String res_workflow_from_status = resultSet.getString(5);
		String res_workflow_to_status = resultSet.getString(6);
		String wflNameToDisplay;
		if (workflowName == null || workflowName.equals("")) {
			wflNameToDisplay = ("no_name_workflow-" + (workflowId));
		} else {
			wflNameToDisplay = (workflowName + "-" + workflowId);
		}
		wfMoveType.setName(wflNameToDisplay);
		wfMoveType.setWfm_from(workflowFrom);
		wfMoveType.setWfm_to(res_workflow_to);
		wfMoveType.setWfm_id(workflowId);
		wfMoveType.setWfm_from_status(res_workflow_from_status);
		wfMoveType.setWfm_to_status(res_workflow_to_status);
		
		
		return wfMoveType;
	}
    /**
     * Gets the {@link List} of all tempalates in database.
     * @return {@link List} of {@link Tempalte}
     */
    public List<Template> getAllTempaltes() {

    	return getJdbcTemplate().query(GET_ALL_TEMPLATE_SQL, new RowMapper<Template>(){
    		
    		public Template mapRow(ResultSet resultSet, int line) throws SQLException {
               
                String templateName = resultSet.getString(1);
                String templateId = resultSet.getString(2);
                if (templateName.equalsIgnoreCase("") || templateName == null) {
                	templateName = "no_name_template" + (line);
                }

    			return createTemplate(templateName, templateId);
    		}
    	});    	
    }

    public List<Status> getStatusesForTemplate(Template template) throws DbException {
    	List<Status> statuses = new ArrayList<Status>();
    	try {
	    	if(getStatusesPrepStatement == null){
	    		getStatusesPrepStatement = getJdbcTemplate().getDataSource().getConnection().prepareStatement(GET_STATUSES_SQL);
	    	}
	    	getStatusesPrepStatement.setLong(1, Long.parseLong(template.getTemplate_id()));
	    	getStatusesPrepStatement.setLong(2, Long.parseLong(template.getTemplate_id()));
	    	
	    	ResultSet resultSet = getStatusesPrepStatement.executeQuery();
	    	while(resultSet.next()){
	    		Status status = mapToStatus(resultSet);
	    		if(status != null){
	    			statuses.add(status);
	    		}
	    	}
	    	
		} catch (SQLException e) {
			logger.error("Exception occured: " + e.getMessage());
			throw new DbException(e.getMessage(), e);		
		}
		return statuses;
		
    }

    private Status mapToStatus(ResultSet resultSet) throws SQLException {
		String statusName = resultSet.getString(1);
		String statusId = resultSet.getString(2);
		Status status = new Status();
		if (statusId == null || statusId.equals("") || statusId.equals(NULL_VALUE)) {
			status.setStatus_id(NO_STATUS);
		} else {
			status.setStatus_id(statusId);
		}
		status.setName(statusName);
		return status;
	}
    
    class AttributeRuleExtractor implements ResultSetExtractor<AttributeRule>{
		public AttributeRule extractData(ResultSet resultSet) throws SQLException, DataAccessException {
			String res_blockNameRus = resultSet.getString(2);
            String attributeCode = resultSet.getString(3);
            String attrNameRus = resultSet.getString(4);
            String dataType = resultSet.getString(5);

            AttributeRule attributeRule = new AttributeRule();

            attributeRule.setBlock_name_rus(res_blockNameRus);
            attributeRule.setAttribute_code(attributeCode);
            attributeRule.setAttr_name_rus(attrNameRus);
            attributeRule.setData_type(dataType);
			return attributeRule;
		}    		    		
	}
    
    public List<AttributeRule> getAttributeRuleForTemplate(Template template) throws DbException { 
    	
    	List<AttributeRule> attrubuteRuleList = new ArrayList<AttributeRule>();
    	try {
	    	if(getAttributeRulesPrepStatement == null){
	    		getAttributeRulesPrepStatement = getJdbcTemplate().getDataSource().getConnection().prepareStatement(GET_ATTRIBUTE_RULE_FOR_TEMPLATE_SQL);
	    	}
	    	getAttributeRulesPrepStatement.setLong(1, Long.parseLong(template.getTemplate_id()));
	    	
	    	ResultSet resultSet = getAttributeRulesPrepStatement.executeQuery();
			AttributeRuleExtractor attributeRuleExtractor = new AttributeRuleExtractor();
	    	while(resultSet.next()){
	    		AttributeRule attributeRule = attributeRuleExtractor.extractData(resultSet);
	    		if(attributeRule != null){
	    			attrubuteRuleList.add(attributeRule);
	    		}
	    	}
	    	
		} catch (SQLException e) {
			logger.error("Exception occured: " + e.getMessage());
			throw new DbException(e.getMessage(), e);			
		}
		
		List<AttributeRule> staticAttrubuteRuleList = getStaticAttributeRuleList();
				
		attrubuteRuleList.addAll(staticAttrubuteRuleList);
		return attrubuteRuleList;		
    }


	private List<AttributeRule> getStaticAttributeRuleList() throws DbException {
		List<AttributeRule> staticAttrubuteRuleList = new ArrayList<AttributeRule>();
    	try {
	    	if(getStaticAttributeRulesPrepStatement == null){
	    		getStaticAttributeRulesPrepStatement = getJdbcTemplate().getDataSource().getConnection().prepareStatement(GET_STATIC_ATTRIBUTE_RULE_FOR_TEMPLATE_SQL);
	    	}	    	
	    	ResultSet resultSet = getStaticAttributeRulesPrepStatement.executeQuery();
			AttributeRuleExtractor attributeRuleExtractor = new AttributeRuleExtractor();
	    	while(resultSet.next()){
	    		AttributeRule attributeRule = attributeRuleExtractor.extractData(resultSet);
	    		if(attributeRule != null){
	    			staticAttrubuteRuleList.add(attributeRule);
	    		}
	    	}
	    	
		} catch (SQLException e) {
			logger.error("Exception occured: " + e.getMessage());
			throw new DbException(e.getMessage(), e);			
		}
		return staticAttrubuteRuleList;
	}
    
    public List<Rule> getPersonRuleForTemplate(Template template) throws DbException {
    	
    	List<Rule> personRules = new ArrayList<Rule>();
    	try {
	    	if(getPersonRulesPrepStatement == null){
	    		getPersonRulesPrepStatement = getJdbcTemplate().getDataSource().getConnection().prepareStatement(GET_PERSON_RULE_FOR_TEMPLATE_SQL);
	    	}
	    	getPersonRulesPrepStatement.setLong(1, Long.parseLong(template.getTemplate_id()));
	    	
	    	ResultSet resultSet = getPersonRulesPrepStatement.executeQuery();
			while(resultSet.next()){
	    		Rule personRule = mapToPersonRule(resultSet);
	    		if(personRule != null){
	    			personRules.add(personRule);
	    		}
	    	}
	    	
		} catch (SQLException e) {
			logger.error("Exception occured: " + e.getMessage());
			throw new DbException(e.getMessage(), e);		
		}
		return personRules;		
  }
    
    /**
     * Maps {@link ResultSet} to {@link Rule} of type Person
     * @param resultSet used to extract data
     * @return {@link Rule}
     * @throws SQLException
     */
    private Rule mapToPersonRule(ResultSet resultSet) throws SQLException {
		String personAttrCode = resultSet.getString(1);
        String linkAttrCode = resultSet.getString(2);
        String intermedAttrCode = resultSet.getString(3);
        String descrName = resultSet.getString(4);
        String linkedStatusId = resultSet.getString(5);
        String roleCode = resultSet.getString(6);
        String ruleHash = resultSet.getString(7);
        Rule rule = new Rule();
        // rule name should be equal to the description of person access rule
        rule.setName(descrName);
        RulePerson rulePerson = new RulePerson();
        rulePerson.setName(descrName);
        rulePerson.setPersonAttributeCode(personAttrCode);
        rulePerson.setLink(linkAttrCode);
        rulePerson.setIntermedAttributeCode(intermedAttrCode);
        rulePerson.setLinkedStatusId(linkedStatusId);
        rulePerson.setRoleCode(roleCode);
        rulePerson.setRuleHash(ruleHash);
        if(!rulePerson.getRuleHash().equals(rulePerson.generateRuleHash())){
        	throw new SQLException("Database md5-hash not equals model md5-hash: " + descrName);
        }
        rule.setRulePerson(rulePerson);
		return rule;
	}
    
    public List<Rule> getRoleRuleForTemplate(Template template) throws DbException {
    	List<Rule> roleRules = new ArrayList<Rule>();
    	try {
	    	if(getRoleRulesPrepStatement == null){
	    		getRoleRulesPrepStatement = getJdbcTemplate().getDataSource().getConnection().prepareStatement(GET_ROLE_RULE_FOR_TEMPLATE_SQL);
	    	}
	    	getRoleRulesPrepStatement.setLong(1, Long.parseLong(template.getTemplate_id()));
	    	
	    	ResultSet resultSet = getRoleRulesPrepStatement.executeQuery();
			while(resultSet.next()){
	    		Rule roleRule = mapToRoleRule(resultSet);
	    		if(roleRule != null){
	    			roleRules.add(roleRule);
	    		}
	    	}
	    	
		} catch (SQLException e) {
			logger.error("Exception occured: " + e.getMessage());
			throw new DbException(e.getMessage(), e);			
		}
		return roleRules;		    	    
    }
    
    private Rule mapToRoleRule(ResultSet resultSet) throws SQLException {
		String roleCode = resultSet.getString(1);
		String roleDesc = resultSet.getString(2);
		String ruleHash = resultSet.getString(3);
		if (roleCode != null) {
			Rule rule = new Rule();
			rule.setName(roleDesc);
			RuleRole ruleRole = new RuleRole();
			ruleRole.setName(roleDesc);
			ruleRole.setRoleCode(roleCode);
			ruleRole.setRuleHash(ruleHash);
	        if(!ruleRole.getRuleHash().equals(ruleRole.generateRuleHash())){
	        	throw new SQLException("Database md5-hash not equals model md5-hash: " + roleDesc);
	        }
			rule.setRuleRole(ruleRole);
			return rule;
		}
		return null;
	}
    
    public List<Rule> getProfileRuleForTemplate(Template template) throws DbException {
    
    	List<Rule> profileRules = new ArrayList<Rule>();
    	try {
	    	if(getProfileRulesPrepStatement == null){
	    		getProfileRulesPrepStatement = getJdbcTemplate().getDataSource().getConnection().prepareStatement(GET_PROFILE_RULE_FOR_TEMPLATE_SQL);
	    	}
	    	getProfileRulesPrepStatement.setLong(1, Long.parseLong(template.getTemplate_id()));
	    	
	    	ResultSet resultSet = getProfileRulesPrepStatement.executeQuery();
			while(resultSet.next()){
	    		Rule profileRule = mapToProfileRule(resultSet);
	    		if(profileRule != null){
	    			profileRules.add(profileRule);
	    		}
	    	}
	    	
		} catch (SQLException e) {
			logger.error("Exception occured: " + e.getMessage());
			throw new DbException(e.getMessage(), e);			
		}
		return profileRules;	
    }
    
    public List<Rule> getRuleForTemplate(Template template) throws DbException {
    	List<Rule> rules = new ArrayList<Rule>();
    	try {
	    	if(getRulesPrepStatement == null){
	    		getRulesPrepStatement = getJdbcTemplate().getDataSource().getConnection().prepareStatement(GET_RULE_FOR_TEMPLATE_SQL);
	    	}
	    	getRulesPrepStatement.setLong(1, Long.parseLong(template.getTemplate_id()));
	    	
	    	ResultSet resultSet = getRulesPrepStatement.executeQuery();
			while(resultSet.next()){
                Rule rule = new Rule();                
                rule.setName(NO_RULE);
	    		if(rule != null){
	    			rules.add(rule);
	    		}
	    	}
	    	
		} catch (SQLException e) {
			logger.error("Exception occured: " + e.getMessage());
			throw new DbException(e.getMessage(), e);			
		}
		return rules;		    	    
    }

    /**
     * Returns rules for template of specified {@link RuleType}.
     * @param template {@link Template}
     * @param ruleType {@link RuleType}
     * @return list of {@link Rule}
     * @throws DbException
     */
	public List<Rule> getRulesForTemplate(Template template, RuleType ruleType) throws DbException {
		if (ruleType == null) {
			throw new IllegalArgumentException("RuleType is unddefined");
		}
    	switch(ruleType){
    		case Person: return getPersonRuleForTemplate(template);
    		case Role: return getRoleRuleForTemplate(template);
    		case Profile: return getProfileRuleForTemplate(template);
    		case Delegation: return getDelegationRuleForTemplate(template);
    		case Undefined: return getRuleForTemplate(template);
    		default: throw new IllegalArgumentException("RuleType is unddefined");

    	}
    }
    
    
	private Rule mapToProfileRule(ResultSet resultSet) throws SQLException {
		String profileAttrCode = resultSet.getString(1);
		String res_target_attr_code = resultSet.getString(2);
		String linkAttrCode = resultSet.getString(3);
		String res_intermedAttrCode = resultSet.getString(4);
		String desc = resultSet.getString(5);
		String linkedStatusId = resultSet.getString(6);
		String roleCode = resultSet.getString(7);
		String ruleHash = resultSet.getString(8);

		Rule rule = new Rule();
		rule.setName(desc);

		RuleProfile ruleproFile = new RuleProfile();
		ruleproFile.setName(desc);
		ruleproFile.setProfileAttributeCode(profileAttrCode);
		ruleproFile.setTargetAttributeCode(res_target_attr_code);
		ruleproFile.setLinkAttributeCode(linkAttrCode);
		ruleproFile.setIntermedAttributeCode(res_intermedAttrCode);
		ruleproFile.setLinkedStatusId(linkedStatusId);
		ruleproFile.setRoleCode(roleCode);
		ruleproFile.setRuleHash(ruleHash);
        if(!ruleproFile.getRuleHash().equals(ruleproFile.generateRuleHash())){
        	throw new SQLException("Database md5-hash not equals model md5-hash: " + desc);
        }
		rule.setRuleProfile(ruleproFile);
		return rule;
	}
    
    public List<Rule> getDelegationRuleForTemplate(Template template) throws DbException {
    	List<Rule> delegationRules = new ArrayList<Rule>();
    	try {
	    	if(getDelegationRulesPrepStatement == null){
	    		getDelegationRulesPrepStatement = getJdbcTemplate().getDataSource().getConnection().prepareStatement(GET_DELEGATION_RULE_FOR_TEMPLATE_SQL);
	    	}
	    	getDelegationRulesPrepStatement.setLong(1, Long.parseLong(template.getTemplate_id()));
	    	
	    	ResultSet resultSet = getDelegationRulesPrepStatement.executeQuery();
			while(resultSet.next()){
	    		Rule delegationRule = mapToDelegationRule(resultSet);
	    		if(delegationRule != null){
	    			delegationRules.add(delegationRule);
	    		}
	    	}
	    	
		} catch (SQLException e) {
			logger.error("Exception occured: " + e.getMessage());
			throw new DbException(e.getMessage(), e);			
		}
		return delegationRules;				
        
    }

	public List<Rule> getDelegationRuleForTemplateSpring(Template template) {
		return getJdbcTemplate().query(GET_DELEGATION_RULE_FOR_TEMPLATE_SQL,
				new Object[] { Long.parseLong(template.getTemplate_id()) }, new int[] { Types.NUMERIC },
				new RowMapper<Rule>() {

					public Rule mapRow(ResultSet resultSet, int line) throws SQLException {

						return mapToDelegationRule(resultSet);
					}

				});
	}
    
    private Rule mapToDelegationRule(ResultSet resultSet) throws SQLException {
		String delegationCode = resultSet.getString(1);
		String desc = resultSet.getString(2);
		String ruleHash = resultSet.getString(3);
		if (delegationCode != null) {
			Rule rule = new Rule();
			rule.setName(desc);
			RuleDelegation ruledelegation = new RuleDelegation();
			ruledelegation.setName(desc);
			ruledelegation.setLinkAttributeCode(delegationCode);
			ruledelegation.setRuleHash(ruleHash);		
	        if(!ruledelegation.getRuleHash().equals(ruledelegation.generateRuleHash())){
	        	throw new SQLException("Database md5-hash not equals model md5-hash: " + desc);
	        }
			rule.setRuleDelegation(ruledelegation);
			return rule;
		}
		return null;
	}
    
    public List<Permission> getPermissionsForTemplate(Template template) throws DbException {
		
    	List<Permission> permissions = new ArrayList<Permission>();
    	try {
	    	if(getPermissionsPrepStatement == null){
	    		getPermissionsPrepStatement = getJdbcTemplate().getDataSource().getConnection().prepareStatement(GET_PERMISSION_FOR_TEMPLATE_SQL);
	    	}
	    	getPermissionsPrepStatement.setLong(1, Long.parseLong(template.getTemplate_id()));
	    	
	    	ResultSet resultSet = getPermissionsPrepStatement.executeQuery();
			while(resultSet.next()){
				Permission permission = mapToPermission(resultSet);
	    		if(permission != null){
	    			permissions.add(permission);
	    		}
	    	}
	    	
		} catch (SQLException e) {
			logger.error("Exception occured: " + e.getMessage());
			throw new DbException(e.getMessage(), e);			
		}  	
		
				
    	List<Map<String, Object>> queryResult = getJdbcTemplate().queryForList(GET_PERMISSION_DATA_FOR_TEMPLATE_SQL,
				new Object[] { Long.parseLong(template.getTemplate_id()) }, new int[] { Types.NUMERIC });
    			
		for (Permission permission : permissions) {
			loadPermissionData(template, permission, queryResult);
		}
		return permissions;
	}


	private void loadPermissionData(Template template, Permission permission, List<Map<String, Object>> queryResult) {
		List<Map<String, Object>> queryDataForPermision = getQueryDataForPermission(queryResult, permission);
		if (queryDataForPermision.size() == 0) {
			logger.error("No Permissions found for: " + permission);
		}
		loadOperations(queryDataForPermision, permission);

		loadWfMoves(queryDataForPermision, permission, template);
		
	}	

	/**
	 * Updates data in {@link Permission}s having the specified rule name.
	 * @param template {@link Template} containing permission.
	 * @param ruleName the name of rule used to find permissions to update them.
	 * @param partial 
	 */
	public void updatePermissionByRuleName(Template template, String ruleName, boolean partial, List<String> statuses) {
    	List<Map<String, Object>> queryResult = getJdbcTemplate().queryForList(GET_PERMISSION_DATA_FOR_TEMPLATE_SQL,
				new Object[] { Long.parseLong(template.getTemplate_id()) }, new int[] { Types.NUMERIC });

		for (Permission permission : template.getPermission()) {
			if(permission != null && ruleName.equals(permission.getRule().split(RulesUtility.RULE_NAME_SUFFIX)[0]) 
					&& (statuses == null || statuses.contains(permission.getStatus()))) {
				loadPermissionData(template, permission, queryResult);
				if(EditConfigMainForm.allBasePermissions
					.get(Long.parseLong(template.getTemplate_id())) != null){
					EditConfigMainForm.allBasePermissions
					.get(Long.parseLong(template.getTemplate_id())).remove(permission);
				}
			}			
		}

	}
	
    private Permission mapToPermission(ResultSet resultSet) throws SQLException {
		Permission permission = new Permission();
		String statusId = resultSet.getString(1);
		if(statusId == null || NULL_VALUE.equals(statusId)) {
			statusId = NO_STATUS;
		}
		String ruleName = resultSet.getString(2);						
		permission.setStatus(statusId);
		permission.setRule(ruleName);
		return permission;
	}
    
    /**
     * Loads work flow moves in empty {@link Permission}.
     * @param queryDataForPermision
     * @param permission
     * @param template
     */
	private void loadWfMoves(List<Map<String, Object>> queryDataForPermision, Permission permission, Template template) {
		//clear workflow moves before filling in
		permission.setWfMoves(new WfMoves());
		for(Map<String, Object> entry : queryDataForPermision) {
			if(isWfm(entry)) {				
				if(permission.getWfMoves() == null){
					permission.setWfMoves(new WfMoves());
				}
				String wfmId = String.valueOf(entry.get(WFM_ID));        			
				WfMove wfMove = new WfMove();
		        wfMove.setWfm_id(wfmId);
		        String permHash = String.valueOf(entry.get(PERM_HASH));  
		        wfMove.setPermHash(permHash);
		        String wfmName = getWfmNamebyWfmId(template, wfmId);
		        if(wfmName == null || wfmName.length() == 0){
		        	logger.error("WfmName is null for " + wfmId + ", template: " + template);
		        }
		        wfMove.setName(wfmName);
		        permission.getWfMoves().addWfMove(wfMove);
			}
		}
	}

	private void loadOperations(List<Map<String, Object>> queryDataForPermision, Permission permission) {
		Operations operations = new Operations();
		permission.setOperations(operations);
		for(Map<String, Object> entry : queryDataForPermision) {
			if(!isOperationPerm(entry)) {
				continue;
			}
			Operation operation = new Operation();
			String permHash = String.valueOf(entry.get(PERM_HASH));
			operation.setPermHash(permHash);
			
			String createPerm = (String)entry.get(CREATE_PERM);
			String rwPerm = (String)entry.get(RW_PERM);
	    	
	    	//TODO use C instead of create in id description
			operation.setOperationType(getOperationPerm(createPerm, rwPerm));
			        		
		    permission.getOperations().addOperation(operation);

		}
	}

	private boolean isNotNull(String value) {
		return value != null && value != "" && !NULL_VALUE.equals(value);
	}
	
	private String getWfmNamebyWfmId(Template template, String wfmId) {
		if(wfmId == null){
			return null;
		}
		for(WFMoveType wfMoveType : template.getWFMoveType()) {
			if(wfmId.equals(wfMoveType.getWfm_id())) {
				return wfMoveType.getName();
			}
		}
		return null;
		
	}

	private boolean isWfm(Map<String, Object> entry) {
		String wfmId = String.valueOf(entry.get(WFM_ID));		
		if (isNotNull(wfmId)) {
			return true;
		}
		return false;
	}

	private OperationType getOperationPerm(String createPerm, String rwPerm) {
		if (isNotNull(createPerm)) {
			return OperationType.CREATE;
		} else if (isNotNull(rwPerm)) {
			if(rwPerm.equals(OPERATION_READ)){
				return OperationType.READ;
			} else 	if(rwPerm.equals(OPERATION_WRITE)){
				return OperationType.WRITE;
			}
		}
		return null;
	}


	private boolean isOperationPerm(Map<String, Object> entry) {
		String createPerm = (String)entry.get(CREATE_PERM);
		String rwPerm = (String)entry.get(RW_PERM);		
		if(isNotNull(createPerm) || isNotNull(rwPerm)){
			return true;
		}
		return false;		
	}

	private List<Map<String, Object>> getQueryDataForPermission(List<Map<String, Object>> queryAllResult, Permission permission) {
		List<Map<String, Object>> result  = new ArrayList<Map<String, Object>>();
		for(Map<String, Object> entry : queryAllResult) {
			
			String statusId = String.valueOf(entry.get(STATUS_ID));
			if(statusId == null || NULL_VALUE.equals(statusId)) {
				statusId = NO_STATUS;
			}
			String roleName = (String)entry.get(ROLE_NAME);
			
			if (permission.getStatus() != null && permission.getStatus().equals(statusId)
					&& permission.getRule() != null && permission.getRule().equals(roleName)) {
				result.add(entry);
			}
			
		}
		return result;
	}
	
	public Set<String> getRuleIdsByRuleName(String ruleName, Template template) {
		Set<String> permHashes = new HashSet<String>();
		
    	List<Map<String, Object>> queryResult = getJdbcTemplate().queryForList(GET_PERMISSION_DATA_FOR_TEMPLATE_SQL,
				new Object[] { Long.parseLong(template.getTemplate_id()) }, new int[] { Types.NUMERIC });

    	for(Map<String, Object> entry : queryResult) {
	    	if(ruleName.equals(String.valueOf(entry.get(ROLE_NAME)))) {
	    		permHashes.add(((String)entry.get(PERM_HASH)));
	    	}
    	}
    	return permHashes;    	
	}

	/**
	 * Returns rule ids for the given rule and status.
	 * @param ruleName name of the rule used to find corresponding rule ids.
	 * @param statusId status id used to find corresponding rule ids.
	 * @param template {@link Template} containing specified rule ids.
	 * @return {@link Set} of rule ids.
	 */
	public Set<String> getRuleIdsByRuleNameAndStatus(String ruleName, String statusId, Template template) {
		Set<String> permHashes = new HashSet<String>();
		
    	List<Map<String, Object>> queryResult = getJdbcTemplate().queryForList(GET_PERMISSION_DATA_FOR_TEMPLATE_SQL,
				new Object[] { Long.parseLong(template.getTemplate_id()) }, new int[] { Types.NUMERIC });

    	for(Map<String, Object> entry : queryResult) {
    		String entryStatusId = String.valueOf(entry.get(STATUS_ID));
    		if(ruleName.equals(String.valueOf(entry.get(ROLE_NAME))) && statusId.equals(entryStatusId==null||entryStatusId.equals("null")?NO_STATUS:entryStatusId)) {
    			permHashes.add(((String)entry.get(PERM_HASH)));
	    		logger.info("Rule id for rule " + ruleName + " and statusId " + statusId + " found : " + entry.get(RULE_ID).toString());
	    	}
    	}
    	return permHashes;    	
	}

}
