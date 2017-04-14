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

--Скрипт для поиска организаций без Зоны ДОУ на всех серверах, кроме ЦА
select c.card_id, t.template_name_rus as template, cs.name_rus as status, name.string_value as name
from card c
  join template t on c.template_id = t.template_id
  join card_status cs on cs.status_id = c.status_id
  left join attribute_value zds on c.card_id = zds.card_id and zds.attribute_code = 'JBR_ZONES_DOW'
  join attribute_value ext on ext.card_id = c.card_id and ext.attribute_code = 'JBR_ORG_IS_EXT' and ext.value_id = 1449
  join attribute_value meth on meth.card_id = c.card_id and meth.attribute_code = 'JBR_SENDING_METHOD' and meth.value_id = 1581
  join attribute_value name on name.card_id = c.card_id and name.attribute_code = 'NAME'
where c.template_id = 222 and c.status_id in (4,7) and zds.card_id is null
      and exists (select 1 from attribute_value t where t.number_value = c.card_id and t.attribute_code = 'JBR_PERS_ORG' and t.template_id = 10);