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

INSERT INTO reference_list (ref_code, description)
SELECT anew.ref_code, 'Values of DMSI message type' 
FROM (SELECT cast('DMSI_MSG_TYPE_VAL' as varchar)) AS anew(ref_code)
WHERE NOT EXISTS (SELECT 1 FROM reference_list WHERE ref_code = anew.ref_code)
;
INSERT INTO values_list ("value_id", "ref_code", "value_rus", "value_eng", "order_in_level", "is_active", "parent_value_id")
SELECT src.value_id, 'DMSI_MSG_TYPE_VAL', 'Уведомление', 'Acknowledgement', 1, 1, NULL 
FROM ( SELECT 2259 ) as src(value_id)
WHERE NOT EXISTS (SELECT 1 FROM values_list WHERE value_id = src.value_id)
;