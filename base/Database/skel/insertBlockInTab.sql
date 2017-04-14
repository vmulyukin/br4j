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

INSERT INTO tab_block 
	( block_code, tab_id, layout )
	SELECT
		b.block_code,
		t.tab_id,
		(SELECT COALESCE(1 + MAX(layout), 300) FROM tab_block WHERE tab_id = t.tab_id AND layout >= 300)
	FROM 
		(SELECT block_code FROM attr_block WHERE block_code='JBR_MEDO_INF') AS b, -- block_code
		(SELECT tab_id FROM tab WHERE tab_id = 760) AS t                          -- tab_id
	WHERE
		NOT EXISTS(
			SELECT 1
			FROM tab_block tb1
			WHERE tb1.block_code = b.block_code
				AND tb1.tab_id = t.tab_id
		)
	LIMIT 1
;