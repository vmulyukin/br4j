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

INSERT INTO xml_data (xml_type, xml_data, is_system, description) 
SELECT 
	'SEARCH',
	'<?xml version="1.0" encoding="UTF-8"?>
	<search>
	<template>222</template>
	<column id="NAME" width="100"/>
	<column id="_STATE" width="80"/>
	</search>', 
	0, 
	'Filter for JBR_DORG_SUPER_ORG' -- descr
	WHERE NOT EXISTS 
		(
		SELECT 1 FROM xml_data 
		WHERE 
			description='Filter for JBR_DORG_SUPER_ORG' -- descr
			AND xml_type='SEARCH'
		)
;

INSERT INTO attribute_option (attribute_code, option_code, option_value) 
	SELECT 
		src.attr_code,
		src.opt_code, 
		(
		SELECT xml_data_id FROM xml_data
		WHERE 
			description = 'Filter for JBR_DORG_SUPER_ORG' -- descr
			AND xml_type = 'SEARCH'
		)
	FROM ( SELECT 
				cast( 'JBR_DORG_SUPER_ORG' as varchar), -- attribute_code
				cast( 'FILTER' as varchar)
			) AS src ( attr_code, opt_code)
	WHERE NOT EXISTS(
			SELECT 1 FROM attribute_option ao
			WHERE 	ao.attribute_code = src.attr_code
				AND ao.option_code = src.opt_code
		)
;