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

INSERT INTO attr_block(block_code, block_name_rus, block_name_eng, is_active, is_system)
	SELECT
		bl_new.block_code, 
		'Информация из МЭДО',       -- block_name_rus
		'Information of IEDMS',     -- block_name_eng
		1,                          -- is_active
		0                           -- is_system
	FROM (
		SELECT CAST(
			'JBR_MEDO_INF' AS VARCHAR) -- block_code
		) 
		AS bl_new(block_code)
	WHERE NOT EXISTS (SELECT 1 FROM attr_block WHERE attr_block.block_code = bl_new.block_code)
;

