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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aplana.br4j.dynamicaccess.db_export.BaseAccessDao;
import org.aplana.br4j.dynamicaccess.db_export.DbException;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.SystemRole;

/**
 * Dao for accessing attributes for rules by rule id. Uses caching of {@link PreparedStatement}s to improve performance.
 * @author atsvetkov
 *
 */
public class RuleAttributeDao extends BaseAccessDao {

	protected final Log logger = LogFactory.getLog(getClass());

	private static final String NULL_VALUE = "null";

	private static final String GET_LINK_STATE_PERSON_FOR_RULE_ID_SQL = "select par.linked_status_id from person_access_rule par "
		+ " where par.rule_id=? ";

	private static final String GET_LINK_STATE_PROFILE_FOR_RULE_ID_SQL = "select par.linked_status_id from profile_access_rule par "
		+ " where par.rule_id=? ";

	private static final String GET_ROLE_PERSON_FOR_RULE_ID_SQL = "select par.role_code from person_access_rule par "
			+ " where par.rule_id=? ";

	private static final String GET_ROLE_PROFILE_FOR_RULE_ID_SQL = "select par.role_code from profile_access_rule par "
			+ " where par.rule_id=? ";

	private static final String GET_LINK_ATTRIBUTE_PERSON_FOR_RULE_ID_SQL = "select a.attribute_code, a.data_type from person_access_rule par "
			+ " left outer join attribute a on a.attribute_code=par.link_attr_code " + " where par.rule_id=? ";

	private static final String GET_LINK_ATTRIBUTE_PROFILE_FOR_RULE_ID_SQL = "select a.attribute_code, a.data_type from profile_access_rule prar "
			+ " left outer join attribute a on a.attribute_code=prar.link_attr_code " + " where prar.rule_id=? ";

	private static final String GET_INTERMED_LINK_ATTRIBUTE_PERSON_FOR_RULE_ID_SQL = "select a.attribute_code, a.data_type from person_access_rule par "
		+ " left outer join attribute a on a.attribute_code=par.intermed_attr_code " + " where par.rule_id=? ";

	private static final String GET_INTERMED_LINK_ATTRIBUTE_PROFILE_FOR_RULE_ID_SQL = "select a.attribute_code, a.data_type from profile_access_rule prar "
		+ " left outer join attribute a on a.attribute_code=prar.intermed_attr_code " + " where prar.rule_id=? ";
	
	private static final String GET_RULE_ID_BY_PERM_HASH = "select rule_id from access_rule where perm_hash = ?";

	private PreparedStatement getRuleIdStatment;
	private PreparedStatement getLinkStateForPersonPrepStatement;
	private PreparedStatement getLinkStateForProfilePrepStatement;
	private PreparedStatement getRoleForPersonPrepStatement;
	private PreparedStatement getRoleForProfilePrepStatement;
	private PreparedStatement getLinkAttributeForPersonPrepStatement;
	private PreparedStatement getLinkAttributeForProfilePrepStatement;
	private PreparedStatement getIntermedLinkAttributeForPersonPrepStatement;
	private PreparedStatement getIntermedLinkAttributeForProfilePrepStatement;

    public RuleAttributeDao(DataSource dataSource) {
    	super(dataSource);
    }    

    public RuleAttributeDao(Connection connection, DataSource dataSource) {
    	super(connection, dataSource);
    }    
    
    public Long getRuleIdByPermHash(String permHash) throws DbException {
    	Long ruleId = null;
    	try {
	    	if(getRuleIdStatment == null){
	    		getRuleIdStatment = connection.prepareStatement(GET_RULE_ID_BY_PERM_HASH);
	    	}
	    	getRuleIdStatment.setString(1, permHash);	    	
	    	ResultSet resultSet = getRuleIdStatment.executeQuery();
			while(resultSet.next()){
				ruleId = resultSet.getLong(1);
	    	}	    	
		} catch (SQLException e) {
			logger.error("Exception occured: " + e.getMessage());
			throw new DbException(e.getMessage(), e);			
		} 
		return ruleId;
	}

    public Attribute getIntermedLinkAttributePersonByRuleId(Long ruleId) throws DbException {
    	Attribute attribute = null;
    	try {
	    	if(getIntermedLinkAttributeForPersonPrepStatement == null){
	    		getIntermedLinkAttributeForPersonPrepStatement = connection.prepareStatement(GET_INTERMED_LINK_ATTRIBUTE_PERSON_FOR_RULE_ID_SQL);
	    	}
	    	getIntermedLinkAttributeForPersonPrepStatement.setLong(1, ruleId);	    	
	    	ResultSet resultSet = getIntermedLinkAttributeForPersonPrepStatement.executeQuery();
			while(resultSet.next()){
				String attributeCode = resultSet.getString(1);
				String attributeType = resultSet.getString(2);
				if(isNotNull(attributeType)) {
					attribute = AttributeFactory.getAttribute(attributeType, attributeCode);
				}				
	    	}	    	
		} catch (SQLException e) {
			logger.error("Exception occured: " + e.getMessage());
			throw new DbException(e.getMessage(), e);			
		} 
		return attribute;
	}
	
	public Attribute getLinkAttributePersonByRuleId(Long ruleId) throws DbException {
    	Attribute attribute = null;
    	try {
	    	if(getLinkAttributeForPersonPrepStatement == null){
	    		getLinkAttributeForPersonPrepStatement = connection.prepareStatement(GET_LINK_ATTRIBUTE_PERSON_FOR_RULE_ID_SQL);
	    	}
	    	getLinkAttributeForPersonPrepStatement.setLong(1, ruleId);	    	
	    	ResultSet resultSet = getLinkAttributeForPersonPrepStatement.executeQuery();
			while(resultSet.next()){
				String attributeCode = resultSet.getString(1);
				String attributeType = resultSet.getString(2);
				if(isNotNull(attributeType)) {
					attribute = AttributeFactory.getAttribute(attributeType, attributeCode);
				}				
	    	}	    	
		} catch (SQLException e) {
			logger.error("Exception occured: " + e.getMessage());
			throw new DbException(e.getMessage(), e);			
		} 
		return attribute;
	}

	public Attribute getIntermedLinkAttributeProfileByRuleId(Long ruleId) throws DbException {
    	Attribute attribute = null;
    	try {
	    	if(getIntermedLinkAttributeForProfilePrepStatement == null) {
	    		getIntermedLinkAttributeForProfilePrepStatement = connection.prepareStatement(GET_INTERMED_LINK_ATTRIBUTE_PROFILE_FOR_RULE_ID_SQL);
	    	}
	    	getIntermedLinkAttributeForProfilePrepStatement.setLong(1, ruleId);	    	
	    	ResultSet resultSet = getIntermedLinkAttributeForProfilePrepStatement.executeQuery();
			while(resultSet.next()){
				String attributeCode = resultSet.getString(1);				
				String attributeType = resultSet.getString(2);
				if(isNotNull(attributeType)) {
					attribute = AttributeFactory.getAttribute(attributeType, attributeCode);
				}				
	    	}	    	
		} catch (SQLException e) {
			logger.error("Exception occured: " + e.getMessage());
			throw new DbException(e.getMessage(), e);			
		} 
		return attribute;
	}
	
	public ObjectId getLinkStateIdPersonByRuleId(Long ruleId) throws DbException {
    	ObjectId stateId = null;
    	try {
	    	if(getLinkStateForPersonPrepStatement == null) {
	    		getLinkStateForPersonPrepStatement = connection.prepareStatement(GET_LINK_STATE_PERSON_FOR_RULE_ID_SQL);
	    	}
	    	getLinkStateForPersonPrepStatement.setLong(1, ruleId);	    	
	    	ResultSet resultSet = getLinkStateForPersonPrepStatement.executeQuery();
			while(resultSet.next()){
				Long state = resultSet.getLong(1);				
				if(isNotNull(state)) {
					stateId = new ObjectId(CardState.class, state);
				}				
	    	}	    	
		} catch (SQLException e) {
			logger.error("Exception occured: " + e.getMessage());
			throw new DbException(e.getMessage(), e);			
		} 
		return stateId;
	}

	public ObjectId getLinkStateIdProfileByRuleId(Long ruleId) throws DbException {
    	ObjectId stateId = null;
    	try {
	    	if(getLinkStateForProfilePrepStatement == null) {
	    		getLinkStateForProfilePrepStatement = connection.prepareStatement(GET_LINK_STATE_PROFILE_FOR_RULE_ID_SQL);
	    	}
	    	getLinkStateForProfilePrepStatement.setLong(1, ruleId);	    	
	    	ResultSet resultSet = getLinkStateForProfilePrepStatement.executeQuery();
			while(resultSet.next()){
				Long state = resultSet.getLong(1);				
				if(isNotNull(state)) {
					stateId = new ObjectId(CardState.class, state);
				}				
	    	}	    	
		} catch (SQLException e) {
			logger.error("Exception occured: " + e.getMessage());
			throw new DbException(e.getMessage(), e);			
		} 
		return stateId;

	}
	
	public ObjectId getRoleCodePersonByRuleId(Long ruleId) throws DbException {
    	ObjectId roleCodeId = null;
    	try {
	    	if(getRoleForPersonPrepStatement == null) {
	    		getRoleForPersonPrepStatement = connection.prepareStatement(GET_ROLE_PERSON_FOR_RULE_ID_SQL);
	    	}
	    	getRoleForPersonPrepStatement.setLong(1, ruleId);	    	
	    	ResultSet resultSet = getRoleForPersonPrepStatement.executeQuery();
			while(resultSet.next()){
				String roleCode = resultSet.getString(1);				
				if(isNotNull(roleCode)) {
					roleCodeId = new ObjectId(SystemRole.class, roleCode);
				}				
	    	}	    	
		} catch (SQLException e) {
			logger.error("Exception occured: " + e.getMessage());
			throw new DbException(e.getMessage(), e);			
		} 
		return roleCodeId;
	}

	public ObjectId getRoleCodeProfileByRuleId(Long ruleId) throws DbException {
    	ObjectId roleCodeId = null;
    	try {
	    	if(getRoleForProfilePrepStatement == null) {
	    		getRoleForProfilePrepStatement = connection.prepareStatement(GET_ROLE_PROFILE_FOR_RULE_ID_SQL);
	    	}
	    	getRoleForProfilePrepStatement.setLong(1, ruleId);	    	
	    	ResultSet resultSet = getRoleForProfilePrepStatement.executeQuery();
			while(resultSet.next()){
				String roleCode = resultSet.getString(1);				
				if(isNotNull(roleCode)) {
					roleCodeId = new ObjectId(SystemRole.class, roleCode);
				}				
	    	}	    	
		} catch (SQLException e) {
			logger.error("Exception occured: " + e.getMessage());
			throw new DbException(e.getMessage(), e);			
		} 
		return roleCodeId;
	}
	
	public Attribute getLinkAttributeProfileByRuleId(Long ruleId) throws DbException {
    	Attribute attribute = null;
    	try {
	    	if(getLinkAttributeForProfilePrepStatement == null) {
	    		getLinkAttributeForProfilePrepStatement = connection.prepareStatement(GET_LINK_ATTRIBUTE_PROFILE_FOR_RULE_ID_SQL);
	    	}
	    	getLinkAttributeForProfilePrepStatement.setLong(1, ruleId);	    	
	    	ResultSet resultSet = getLinkAttributeForProfilePrepStatement.executeQuery();
			while(resultSet.next()){
				String attributeCode = resultSet.getString(1);				
				String attributeType = resultSet.getString(2);
				if(isNotNull(attributeType)) {
					attribute = AttributeFactory.getAttribute(attributeType, attributeCode);
				}				
	    	}	    	
		} catch (SQLException e) {
			logger.error("Exception occured: " + e.getMessage());
			throw new DbException(e.getMessage(), e);			
		} 
		return attribute;
	}
	
	private boolean isNotNull(String value) {
		return value != null && value != "" && !NULL_VALUE.equals(value);
	}

	private boolean isNotNull(Long value) {
		return value != null && value.intValue() != 0 && !NULL_VALUE.equals(value);
	}
}
