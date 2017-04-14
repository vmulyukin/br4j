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

INSERT INTO tab_template ("template_id", "tab_id", "order_lr")
SELECT 
	t.template_id,
	tb.tab_id,
	1                                                                           -- order_lr
FROM 
	(SELECT template_id FROM template WHERE template_id = 2264) AS t,           -- tempalte_id
	(SELECT tab_id FROM tab WHERE tab_id = 700) as tb                           -- tab_id
WHERE NOT EXISTS (SELECT 1 FROM tab_template WHERE tab_id = tb.tab_id AND template_id = t.template_id)
;