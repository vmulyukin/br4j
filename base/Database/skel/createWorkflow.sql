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

INSERT INTO workflow
("workflow_id", "initial_status_id", "name_rus", "name_eng", "is_active")
	SELECT 
		anew.workflow_id,
		st.status_id,                   
		'Обработка загрузки из МЭДО',      -- name_rus
		'Load from IEDMS handling',        -- name_eng
		1 
	FROM 
		(
			SELECT 99999                   -- workflow_id
		) AS anew (workflow_id),
		(
			SELECT status_id
			FROM card_status
			WHERE status_id = 10000100     -- initial_status_id
		) AS st
	WHERE NOT EXISTS 
		(
			SELECT workflow_id 
			FROM workflow
			WHERE workflow_id = anew.workflow_id
		)
;