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

INSERT INTO default_attribute_value(            template_attr_id, number_value, string_value,             date_value, value_id, long_binary_value)SELECT 			ta.template_attr_id,	1,                                            -- number_value	NULL,                                            -- string_value	NULL,                                            -- date_value	NULL,                                            -- value_id	NULL                                             -- long_binary_valueFROM template_attribute ta WHERE attribute_code = 'REPLIC_IN_PROCESS' AND template_id = 2400	AND NOT EXISTS (		SELECT 1 FROM default_attribute_value		WHERE default_attribute_value.template_attr_id = ta.template_attr_id	);