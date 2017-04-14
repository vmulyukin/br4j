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

INSERT INTO workflow_move
(wfm_id, workflow_id, name_rus, name_eng, from_status_id, to_status_id, need_confirmation, action_code)
SELECT 
	anew.wfm_id,
	wfl.workflow_id,                 
	'Карточка обработана',   -- name_rus
	'Card is processed',     -- name_eng
	from_st.status_id,
	to_st.status_id,  
	0,                       -- need_confirmation
	'CHG_STATUS'             -- action_code
FROM 
	(
		SELECT 10000100                   -- wfm_id
	) AS anew (wfm_id),
	(
		SELECT workflow_id
		FROM workflow
		WHERE workflow_id = 1500100   -- workflow_id
	) AS wfl,
	(
		SELECT status_id
		FROM card_status
		WHERE status_id = 10000100 -- from_status_id
	) AS from_st,
	(
		SELECT status_id
		FROM card_status
		WHERE status_id = 10000101 -- to_status_id
	) AS to_st
WHERE NOT EXISTS (SELECT 1 FROM workflow_move WHERE wfm_id = anew.wfm_id)
;