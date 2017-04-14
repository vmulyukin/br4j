--
--   Licensed to the Apache Software Foundation (ASF) under one or more
--   contributor license agreements.  See the NOTICE file distributed with
--   this work for additional information regarding copyright ownership.
--   The ASF licenses this file to you under the Apache License, Version 2.0
--   (the "License"); you may not use this file except in compliance with
--   the License.  You may obtain a copy of the License at
--
--       http://www.apache.org/licenses/LICENSE-2.0
--
--   Unless required by applicable law or agreed to in writing, software
--   distributed under the License is distributed on an "AS IS" BASIS,
--   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
--   See the License for the specific language governing permissions and
--   limitations under the License.
--

INSERT INTO attribute_view_param(template_attr_id, status_id, role_code, is_mandatory, is_hidden, is_readonly, person_attribute_code)
    SELECT
		t_attr.template_attr_id,
		st.status_id,
		role.role_code,
		0,
		0,
		0,
		null	
	FROM 
		(SELECT template_attr_id FROM template_attribute WHERE template_id = 2344 AND attribute_code='') AS t_attr(template_attr_id),
		(SELECT status_id FROM card_status WHERE status_id IN ()) AS st(status_id),
		(SELECT role_code FROM system_role WHERE role_code in ('A', '_SYSTEM_')) as role(role_code)
	WHERE NOT EXISTS (
		SELECT 1 FROM attribute_view_param a_v_p
		WHERE a_v_p.template_attr_id = t_attr.template_attr_id
			AND a_v_p.status_id = st.status_id
			AND a_v_p.role_code = role.role_code
	)
;