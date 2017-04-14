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

INSERT INTO template
("template_id", "template_name_rus", "template_name_eng", "is_active", "is_system", "workflow_id", "show_in_createcard", "show_in_search")
SELECT 
	anew.template_id,
	'Импортированный документ', -- template_name_rus
	'Imported document',        -- template_name_eng
	1,                          -- is_active
	0,                          -- is_system
	wfl.workflow_id,            -- workflow_id
	1,                          -- show_in_createcard
	1                           -- show_in_search
FROM 
	(
		SELECT 
			2264                    -- template_Id
	) AS anew (template_id),
	(
		SELECT workflow_id
		FROM workflow
		WHERE workflow_id = 1500100  -- workflow_id
	) AS wfl			
WHERE NOT EXISTS (SELECT 1 FROM template WHERE template_id = anew.template_id);