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

INSERT INTO template_attribute(	template_id,	attribute_code,	block_code,	is_mandatory, order_in_list,	column_width, is_hidden,	is_readonly) 	SELECT 		t.template_id,		a.attribute_code, a.block_code,		a.is_mandatory, 		a.order_in_block,		a.column_width, 		a.is_hidden,		a.is_readonly	FROM attribute a		JOIN template_block tb on tb.block_code=a.block_code		JOIN template t on t.template_id = tb.template_id 	WHERE (1=1) 		AND (t.template_id = 224)                             -- template_id		AND (a.attribute_code = 'JBR_MEDO_CREATE_DATE')       -- attribute_code		AND (a.block_code = 'JBR_MEDO_INF')                   -- block_code		AND NOT EXISTS (			SELECT 1 FROM template_attribute ta			WHERE ta.template_id = t.template_id 				AND ta.attribute_code=a.attribute_code		);-- Скрыть для остальныхINSERT INTO template_attribute (		template_id,		block_code,			attribute_code,		is_mandatory,		order_in_list,		column_width,		is_hidden,		is_readonly)	SELECT 		t.template_id,		a.block_code, 		a.attribute_code,		a.is_mandatory, 	a.order_in_block,		a.column_width, 		1, -- a.is_hidden,		a.is_readonly	FROM attribute a		-- JOIN attr_block ab on ab.block_code=a.block_code		JOIN template_block tb on tb.block_code=a.block_code		JOIN template t on t.template_id = tb.template_id 	WHERE (1=1)		AND (t.template_id <> 224)                             -- template_id		AND (a.attribute_code = 'JBR_MEDO_FORM_DATE')		AND NOT EXISTS (			SELECT 1 FROM template_attribute ta			WHERE ta.template_id = t.template_id 				AND ta.attribute_code=a.attribute_code		);