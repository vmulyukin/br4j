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

INSERT INTO attribute ( "attribute_code", 
	"attr_name_rus", "attr_name_eng", 
	"data_type", "block_code", "order_in_block", 
	"column_width", "display_length", "rows_number", 
	"ref_code", "locked_by", "lock_time", 
	"is_mandatory", "is_active", 
	"is_system", "is_readonly", "is_hidden"
)
	SELECT 
		anew.attr_code, 
		'Дата создания сообщения',                                 -- attr_name_rus
		'Message creation date',                                   -- attr_name_eng
		'D',                                                       -- "data_type
		'JBR_MEDO_INF',                                            -- block_code,		                                
		(SELECT COALESCE(1 + MAX(order_in_block), 0) 
		 FROM attribute a WHERE a.block_code = 'JBR_MEDO_INF'      -- block_code
		),                              
		20, NULL, 1,                                               -- "column_width", "display_length", "rows_number",
		NULL, NULL, NULL,                                          -- "ref_code", "locked_by", "lock_time", 
		0, 1,                                                      -- "is_mandatory", "is_active",
		0, 0, 0                                                    -- "is_system",    "is_readonly", "is_hidden"
	FROM (SELECT 
		cast('JBR_MEDO_CREATE_DATE' as VARCHAR)                    -- attribute code
		) AS anew (attr_code) 
	-- проверка дублирования
	WHERE 
		NOT EXISTS (
			SELECT 1 FROM attribute a WHERE a.attribute_code = anew.attr_code
		)
;