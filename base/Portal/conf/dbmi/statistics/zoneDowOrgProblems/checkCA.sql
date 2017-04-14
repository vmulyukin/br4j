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

--SQL для проверки явялеяется ли текущий стенд - сервером ЦА
select avo.string_value ilike ('%ГОСТ ФСИН РФ%') isCA
from person p
  join attribute_value av using (card_id)
  join attribute_value avo on av.number_value = avo.card_id and avo.attribute_code = 'NAME'
where av.attribute_code = 'JBR_PERS_ORG'
group by avo.string_value
order by count(p.person_id) desc
limit 1;