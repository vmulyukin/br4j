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

INSERT INTO block_view_param (template_id, block_code, status_id, state_block)
SELECT t.template_id, tb.block_code, cs.status_id , 2
FROM 
(SELECT template_id FROM template WHERE template.template_id = 2264) AS t
INNER JOIN template_block tb ON (tb.template_id = t.template_id AND tb.block_code='IMPORTED_DOC')
LEFT JOIN card_status cs on status_id IN (10000101, 10000102)
WHERE	
	NOT EXISTS (
		SELECT 1 FROM block_view_param WHERE block_code = tb.block_code AND template_id=t.template_id AND status_id=cs.status_id);