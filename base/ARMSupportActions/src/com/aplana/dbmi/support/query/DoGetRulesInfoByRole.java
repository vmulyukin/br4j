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
package com.aplana.dbmi.support.query;

import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.support.action.GetRulesInfoByRole;

public class DoGetRulesInfoByRole extends ActionQueryBase {

	@Override
	public Object processQuery() throws DataException {
		GetRulesInfoByRole action = getAction();
		String roleCodeString = action.getRoleId().getId().toString();
		String sql = "select t.template_name_rus as template, '�����������' as rule_type, pr.description as rname,  \n" +
				"null as target, null as profile, null as link, null as intermid \n" + 
				"from system_role sr \n" +
				"join role_access_rule pr on sr.role_code = pr.role_code \n" +
				"join access_rule ar on pr.rule_id = ar.rule_id  \n" +
				"join template t on ar.template_id = t.template_id \n" +
				"where sr.role_code = ?" + 
				"group by t.template_name_rus, rname \n" +
				"UNION \n" +
				"select t.template_name_rus as template, '������������' as rule_type, (regexp_split_to_array(pr.description,'_SYSTEM_'))[1] as rname,  \n" +
				"at.attr_name_rus as target, null as profile, al.attr_name_rus as link, ai.attr_name_rus as intermid \n" + 
				"from system_role sr \n" +
				"join person_access_rule pr on sr.role_code = pr.role_code \n" +
				"join access_rule ar on pr.rule_id = ar.rule_id  \n" +
				"join template t on ar.template_id = t.template_id \n" +
				"join attribute at on at.attribute_code = pr.person_attr_code \n" +
				"left join attribute al on al.attribute_code = pr.link_attr_code \n" +
				"left join attribute ai on ai.attribute_code = pr.intermed_attr_code \n" +
				"where sr.role_code = ?" + 
				"group by t.template_name_rus, pr.person_attr_code, pr.link_attr_code, pr.intermed_attr_code, \n" +
				"at.attr_name_rus, al.attr_name_rus, ai.attr_name_rus, rname \n" +
				"UNION \n" +
				"select t.template_name_rus as template, '����������' as rule_type, (regexp_split_to_array(pr.description,'_SYSTEM_'))[1] as rname, \n" +
				"at.attr_name_rus as target,  ap.attr_name_rus as profile, al.attr_name_rus as link, ai.attr_name_rus as intermid \n" +
				"from system_role sr \n" +
				"join profile_access_rule pr on sr.role_code = pr.role_code \n" +
				"join access_rule ar on pr.rule_id = ar.rule_id  \n" +
				"join template t on ar.template_id = t.template_id \n" +
				"join attribute at on at.attribute_code = pr.target_attr_code \n" +
				"join attribute ap on ap.attribute_code = pr.profile_attr_code \n" +
				"left join attribute al on al.attribute_code = pr.link_attr_code \n" +
				"left join attribute ai on ai.attribute_code = pr.intermed_attr_code \n" +
				"where sr.role_code = ?" + 
				"group by t.template_name_rus, pr.target_attr_code, pr.profile_attr_code, pr.link_attr_code, pr.intermed_attr_code, \n" +
			    "at.attr_name_rus, ap.attr_name_rus, al.attr_name_rus, ai.attr_name_rus, rname \n" +
				"order by template, rule_type desc \n";
		return getJdbcTemplate().queryForList(sql, new Object[]{roleCodeString,roleCodeString,roleCodeString});
	}

}
