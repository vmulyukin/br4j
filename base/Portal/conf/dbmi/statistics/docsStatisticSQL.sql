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

with persons as (
select
 p.person_id
 , av_p2name.string_value as Organization
from
 person p 
 join attribute_value av_p 
  on p.card_id = av_p.card_id 
  and av_p.attribute_code = 'JBR_PERS_ORG'
 join attribute_value av_p2name 
  on av_p.number_value = av_p2name.card_id 
  and av_p2name.attribute_code = 'JBR_DORG_FULLNAME'
)
select 
 p.Organization, 
 t.template_name_rus as template, 
 count(c.card_id) as count 
from 
 card c
 join template t 
  on t.template_id = c.template_id
 join attribute_value av_r 
  on c.card_id = av_r.card_id 
  and av_r.attribute_code = 'JBR_REGD_REGISTRAR'
 join attribute_value av_c
  on c.card_id = av_c.card_id 
  and av_c.attribute_code = 'CREATED'
  and av_c.date_value >= :startDate
  and av_c.date_value <= :endDate
 join persons p 
  on p.person_id = av_r.number_value
where 
 c.status_id not in (34145,106,303990,1,302,301) 
 and c.template_id in (224,364,864,1226,764,784)
group by template, Organization
order by Organization,template