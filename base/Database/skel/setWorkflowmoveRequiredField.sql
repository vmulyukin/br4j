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

INSERT INTO workflow_move_required_field(wfm_id, template_attr_id, must_be_set)
SELECT wfm.wfm_id, t_a.template_attr_id, 1
	FROM 
	(SELECT wfm_id FROM workflow_move WHERE wfm_id = 10000102) as wfm, -- wfm_id
	(SELECT template_attr_id 
		FROM template_attribute 
		WHERE attribute_code = 'JBR_IMP_DOC_DOC' 						-- attribute_code
			AND template_id = 2264									-- template_id
	) AS t_a
WHERE 
	NOT EXISTS (
		SELECT 1 FROM workflow_move_required_field
		WHERE workflow_move_required_field.wfm_id = wfm.wfm_id
			AND workflow_move_required_field.template_attr_id = t_a.template_attr_id
		)
;