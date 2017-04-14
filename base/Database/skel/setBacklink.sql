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

INSERT INTO attribute_option (attribute_code, option_code, option_value) 
	SELECT 
		'JBR_IMP_DOC_DOC', -- attribute_code
		'LINK', 
		'JBR_ORIG_SRC'     -- cardlink attribute_code
	FROM ( SELECT 
				cast( 'JBR_IMP_DOC_DOC' as varchar), -- attribute_code
				cast( 'LINK' as varchar)
			) AS src ( attr_code, opt_code)
	WHERE NOT EXISTS(
			SELECT 1 FROM attribute_option ao
			WHERE 	ao.attribute_code = src.attr_code
				AND ao.option_code = src.opt_code
		)
;