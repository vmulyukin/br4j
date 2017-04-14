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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aplana.br4j.dynamicaccess.RulesUtility;
import org.aplana.br4j.dynamicaccess.db_export.BaseAccessDao;
import org.aplana.br4j.dynamicaccess.xmldef.types.PermissionWrapper.RuleType;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlTypeValue;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * DAO for accessing rule information.
 * @author ynikitin
 *
 */
public class AccessRuleDao extends BaseAccessDao {
	
	protected final Log logger = LogFactory.getLog(getClass());

	private static final String GET_ACCESS_RULE_IDS_SQL = 
			"select  \n"+
			"	rule_id, \n"+
			"	rule_count, \n"+
			"    rule_type, \n"+
			"    rule_name \n"+
			"from \n"+
			"  ( \n"+
			"  Select  \n"+
			"      r.rule_id,  \n"+
			"      (select count(r1.rule_id) from  \n"+
			"                access_rule r1  \n"+
			"            where  \n"+
			"                EXISTS(select 1 from person_access_rule par1 where par1.rule_id = r1.rule_id and par1.link_attr_code is null) \n"+
			"                and exists(select 1 from card c1 where c1.template_id = r1.template_id and c1.status_id = r1.status_id) \n"+
			"      ) as rule_count,  \n"+
			"      'Person' as rule_type, \n"+
			"      '������������ �������' as rule_name \n"+
			"  from  \n"+
			"      access_rule r  \n"+
			"  where  \n"+
			"      EXISTS(select 1 from person_access_rule par where par.rule_id = r.rule_id and par.link_attr_code is null) \n"+
			"      and exists(select 1 from card c where c.template_id = r.template_id and c.status_id = r.status_id) \n"+
			"  union all \n"+
			"  Select  \n"+
			"      r.rule_id,  \n"+
			"      (select count(r1.rule_id) from  \n"+
			"                access_rule r1  \n"+
			"            where  \n"+
			"                EXISTS(select 1 from person_access_rule par1 where par1.rule_id = r1.rule_id and not(par1.link_attr_code is null) and (par1.intermed_attr_code is null)) \n"+
			"                and exists(select 1 from card c1 where c1.template_id = r1.template_id and c1.status_id = r1.status_id) \n"+
			"      ) as rule_count,  \n"+
			"      'Person' as rule_type, \n"+
			"      '������������ ������� � ��' as rule_name \n"+
			"  from  \n"+
			"      access_rule r  \n"+
			"  where  \n"+
			"      EXISTS(select 1 from person_access_rule par where par.rule_id = r.rule_id and not(par.link_attr_code is null) and (par.intermed_attr_code is null)) \n"+
			"      and exists(select 1 from card c where c.template_id = r.template_id and c.status_id = r.status_id) \n"+
			"  union all \n"+
			"  Select  \n"+
			"      r.rule_id,  \n"+
			"      (select count(r1.rule_id) from  \n"+
			"                access_rule r1  \n"+
			"            where  \n"+
			"                EXISTS(select 1 from person_access_rule par1 where par1.rule_id = r1.rule_id and not(par1.link_attr_code is null) and not(par1.intermed_attr_code is null)) \n"+
			"                and exists(select 1 from card c1 where c1.template_id = r1.template_id and c1.status_id = r1.status_id) \n"+
			"      ) as rule_count,  \n"+
			"      'Person' as rule_type, \n"+
			"      '������������ ������� � �� � ���' as rule_name \n"+
			"  from  \n"+
			"      access_rule r  \n"+
			"  where  \n"+
			"      EXISTS(select 1 from person_access_rule par where par.rule_id = r.rule_id and not(par.link_attr_code is null) and not(par.intermed_attr_code is null)) \n"+
			"      and exists(select 1 from card c where c.template_id = r.template_id and c.status_id = r.status_id) \n"+
			"  union all \n"+
			"  --��������� ����� ������������ � ���������� ������-- \n"+
			"  Select  \n"+
			"      r.rule_id,  \n"+
			"      (select count(r1.rule_id) from  \n"+
			"                access_rule r1  \n"+
			"            where  \n"+
			"                EXISTS(select 1 from profile_access_rule par1 where par1.rule_id = r1.rule_id and par1.link_attr_code is null) \n"+
			"                and exists(select 1 from card c1 where c1.template_id = r1.template_id and c1.status_id = r1.status_id) \n"+
			"      ) as rule_count,  \n"+
			"      'Profile' as rule_type, \n"+
			"      '���������� �������' as rule_name \n"+
			"  from  \n"+
			"      access_rule r  \n"+
			"  where  \n"+
			"      EXISTS(select 1 from profile_access_rule par where par.rule_id = r.rule_id and par.link_attr_code is null) \n"+
			"      and exists(select 1 from card c where c.template_id = r.template_id and c.status_id = r.status_id) \n"+
			"  union all \n"+
			"  Select  \n"+
			"      r.rule_id,  \n"+
			"      (select count(r1.rule_id) from  \n"+
			"                access_rule r1  \n"+
			"            where  \n"+
			"                EXISTS(select 1 from profile_access_rule par1 where par1.rule_id = r1.rule_id and not(par1.link_attr_code is null) and (par1.intermed_attr_code is null)) \n"+
			"                and exists(select 1 from card c1 where c1.template_id = r1.template_id and c1.status_id = r1.status_id) \n"+
			"      ) as rule_count,  \n"+
			"      'Profile' as rule_type, \n"+
			"      '���������� ������� � ��' as rule_name \n"+
			"  from  \n"+
			"      access_rule r  \n"+
			"  where  \n"+
			"      EXISTS(select 1 from profile_access_rule par where par.rule_id = r.rule_id and not(par.link_attr_code is null) and (par.intermed_attr_code is null)) \n"+
			"      and exists(select 1 from card c where c.template_id = r.template_id and c.status_id = r.status_id) \n"+
			"  union all \n"+
			"  Select  \n"+
			"      r.rule_id,  \n"+
			"      (select count(r1.rule_id) from  \n"+
			"                access_rule r1  \n"+
			"            where  \n"+
			"                EXISTS(select 1 from profile_access_rule par1 where par1.rule_id = r1.rule_id and not(par1.link_attr_code is null) and not(par1.intermed_attr_code is null)) \n"+
			"                and exists(select 1 from card c1 where c1.template_id = r1.template_id and c1.status_id = r1.status_id) \n"+
			"      ) as rule_count,  \n"+
			"      'Profile' as rule_type, \n"+
			"      '���������� ������� � �� � ���' as rule_name \n"+
			"  from  \n"+
			"      access_rule r  \n"+
			"  where  \n"+
			"      EXISTS(select 1 from profile_access_rule par where par.rule_id = r.rule_id and not(par.link_attr_code is null) and not(par.intermed_attr_code is null)) \n"+
			"      and exists(select 1 from card c where c.template_id = r.template_id and c.status_id = r.status_id) \n"+
			"  ) as rules \n"+
			"order by \n"+
			"	rules.rule_type, \n"+
			"   rules.rule_name, \n"+
			"   rules.rule_id \n";	
	
	private static final String GET_ACCESS_RULE_IDS_BY_HASHES_SQL = 
			"select ac.rule_id, 'Person' \n" +
					"from access_rule ac \n" +
					"join person_access_rule par on ac.rule_id = par.rule_id \n" +
					"where ac.perm_hash in (:permHashes) \n" +
					"union all  \n" +
					"select ac.rule_id, 'Profile' \n" +
					"from access_rule ac \n" +
					"join profile_access_rule par on ac.rule_id = par.rule_id \n" +
					"where ac.perm_hash in (:permHashes) \n";
    
	public AccessRuleDao(DataSource dataSource) {
    	super(dataSource);
    }

    public List<Long> getRuleIds(){
    	return getJdbcTemplate().query(GET_ACCESS_RULE_IDS_SQL, new RowMapper<Long>(){
    		
    		public Long mapRow(ResultSet resultSet, int line) throws SQLException {
    			return resultSet.getLong(1);
    		}
    	});    	
    }

    public List<RecalculateAccessRule> getRules(){
    	return getJdbcTemplate().query(GET_ACCESS_RULE_IDS_SQL, new RowMapper<RecalculateAccessRule>(){
    		
    		public RecalculateAccessRule mapRow(ResultSet resultSet, int line) throws SQLException {
    			RecalculateAccessRule accessRule = new RecalculateAccessRule();
    			accessRule.setRule_id(resultSet.getLong(1));
    			accessRule.setRule_count(resultSet.getInt(2));
    			accessRule.setRule_type(RuleType.valueOf(resultSet.getString(3)));
    			accessRule.setRule_name(resultSet.getString(4));
    			return accessRule;
    		}
    	});    	
    }
    
    public List<RecalculateAccessRule> getRulesByHashes(List<String> permHashes) {
    	final NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(getJdbcTemplate());
    	final MapSqlParameterSource args = new MapSqlParameterSource();
		args.addValue("permHashes", permHashes);
		return namedParameterJdbcTemplate.query(GET_ACCESS_RULE_IDS_BY_HASHES_SQL, args, new RowMapper<RecalculateAccessRule>(){
    		
    		public RecalculateAccessRule mapRow(ResultSet resultSet, int line) throws SQLException {
    			RecalculateAccessRule accessRule = new RecalculateAccessRule();
    			accessRule.setRule_id(resultSet.getLong(1));
    			accessRule.setRule_count(resultSet.getFetchSize());
    			accessRule.setRule_type(RuleType.valueOf(resultSet.getString(2)));
    			accessRule.setRule_name("���������� �������");
    			return accessRule;
    		}
    	});
    }
    
    public void renameRuleByRuleHash(String ruleHash, String newName, Long templateId) throws SQLException{
    	final NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(getJdbcTemplate());
    	final MapSqlParameterSource args = new MapSqlParameterSource();
    	args.addValue("newName", newName.split(RulesUtility.RULE_NAME_SUFFIX)[0], Types.VARCHAR);
    	args.addValue("ruleHash", ruleHash, Types.VARCHAR);
    	args.addValue("templateId", templateId, Types.NUMERIC);
    	
    	
    	namedParameterJdbcTemplate.update(
    			"update role_access_rule rr set description =  :newName \n" +
    			"FROM access_rule ar \n"+
    			"where rr.rule_hash = :ruleHash and rr.rule_id = ar.rule_id and ar.template_id = :templateId",
    			args);	
    	namedParameterJdbcTemplate.update(
    			"update person_access_rule rr set description =  :newName \n" +
    			"FROM access_rule ar \n"+
    			"where rr.rule_hash = :ruleHash and rr.rule_id = ar.rule_id and ar.template_id = :templateId",
    			args);	
    	namedParameterJdbcTemplate.update(
    			"update profile_access_rule rr set description =  :newName \n" +
				"FROM access_rule ar \n"+
				"where rr.rule_hash = :ruleHash and rr.rule_id = ar.rule_id and ar.template_id = :templateId",
				args);	
    	namedParameterJdbcTemplate.update(
    			"update delegation_access_rule rr set description =  :newName \n" +
				"FROM access_rule ar \n"+
				"where rr.rule_hash = :ruleHash and rr.rule_id = ar.rule_id and ar.template_id = :templateId",
				args);	
    }
    
    public class RecalculateAccessRule{
    	private long rule_id;
    	private int rule_count;
    	private RuleType rule_type;
    	private String rule_name;
		public long getRule_id() {
			return rule_id;
		}
		public void setRule_id(long rule_id) {
			this.rule_id = rule_id;
		}
		public int getRule_count() {
			return rule_count;
		}
		public void setRule_count(int rule_count) {
			this.rule_count = rule_count;
		}
		public RuleType getRule_type() {
			return rule_type;
		}
		public void setRule_type(RuleType rule_type) {
			this.rule_type = rule_type;
		}
		public String getRule_name() {
			return rule_name;
		}
		public void setRule_name(String rule_name) {
			this.rule_name = rule_name;
		}
		
		public String toString(){
			return MessageFormat.format("{0} - {1} (����� - {2})", new Object[]{rule_name, rule_id, rule_count});
		}
    }
}
