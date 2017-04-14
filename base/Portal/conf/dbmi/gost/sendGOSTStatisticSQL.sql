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

with notif_subquery as 
(
	select
	c.card_id as elm_id,
	av_uuid.string_value as uuid,
	av_notif_created.date_value as notif_created, --Дата получения(создания) уведомлений о регистрациии и получении
	av_notif_type.value_id as notif_type

	from card c
	join attribute_value av on (av.card_id = c.card_id and av.attribute_code = 'ADMIN_222147' and av.value_id = 1581) --тип ГОСТ
	join attribute_value av_uuid on (av_uuid.card_id = c.card_id and av_uuid.attribute_code = 'JBR_DIST_UUID')
	join attribute_value av_date on (
		av_date.card_id = c.card_id and 
		av_date.attribute_code = 'CREATED' and
		(date_trunc('day',av_date.date_value) >  date_trunc('day',(CURRENT_TIMESTAMP - CAST((:days||' days') as interval))))
	)
	--Берем данные о доставке (1501)
	join attribute_value av_send_info_id on (
		av_send_info_id.card_id = c.card_id and 
		av_send_info_id.attribute_code = 'JBR_DIST_SEND_INFO'
	)
	--и из них дату создания и тип уведомления(о доставке, о регистрации)
	join attribute_value av_notif_created on (
		av_notif_created.card_id = av_send_info_id.number_value and
		--av_notif_created.card_id = av_notif_ids.card_id and
		av_notif_created.attribute_code = 'CREATED'
	)
	join attribute_value av_notif_type on (
		av_notif_type.card_id = av_send_info_id.number_value and
		--av_notif_type.card_id = av_notif_ids.card_id and
		av_notif_type.attribute_code = 'JBR_ACK_TYPE'
	)
	where c.template_id = 704
)

select 
	c.card_id as elm_id,
	av_uuid.string_value as uuid, 
	c.status_id as elm_status,
	cs.name_rus as elm_status_name,
	basedoc_card.template_id as basedoc_template,
	basedoc_card.status_id as basedoc_status,
	av_date.date_value as elm_created,
	av_default_org_fullname.string_value as default_org_fullname,
	av_signatory_org_fullname.string_value as sender_org_fullname,
	av_dest_org_fullname.string_value as dest_org_fullname,
	av_basedoc_regdate.date_value as basedoc_regdate,
	av_basedoc_regnumber.string_value as basedoc_regnumber,
	av_gost_created.date_value as gost_created,
	notif_received.notif_created as notif_received_created,
	notif_registered.notif_created as notif_registered_created

from card c
join card_status cs on (cs.status_id = c.status_id and c.status_id not in (1, 303990)) --исключаем ЭСР в статусах Черновик и Корзина
join attribute_value av on (av.card_id = c.card_id and av.attribute_code = 'ADMIN_222147' and av.value_id = 1581)
join attribute_value av_uuid on (av_uuid.card_id = c.card_id and av_uuid.attribute_code = 'JBR_DIST_UUID')
join attribute_value av_date on (
	av_date.card_id = c.card_id and 
	av_date.attribute_code = 'CREATED' and
	(date_trunc('day',av_date.date_value) >  date_trunc('day',(CURRENT_TIMESTAMP - CAST((:days||' days') as interval))))
)

--Получаем id ДО (это бэклинк, поэтому идем от ДО по кардлинку ЭРС)
join attribute_value av_basedoc on (
	av_basedoc.number_value = c.card_id and
	av_basedoc.attribute_code = 'ADMIN_222990' and
	av_basedoc.template_id in (364, 764)	
)

--Получаем карточку ДО
join card basedoc_card on (basedoc_card.card_id = av_basedoc.card_id)

--Из ДО получаем person_id подписанта
join attribute_value av_signatory_id on (
	av_signatory_id.card_id = av_basedoc.card_id and
	av_signatory_id.attribute_code = 'JBR_INFD_SIGNATORY'
)

--Добавляем имя организации по умолчанию, ID которой передаем из настроек
left join attribute_value av_default_org_fullname on (
	av_default_org_fullname.card_id = :defaultOrgId and
	av_default_org_fullname.attribute_code = 'JBR_DORG_FULLNAME'
)
--получаем id карточки подписанта шаблона Персона (Внутренний)
left join person person_signatory on (person_signatory.person_id = av_signatory_id.number_value)

--из карточки персоны подписанта берем id его организации
left join attribute_value av_signatory_org_id on (
	av_signatory_org_id.card_id = person_signatory.card_id and
	av_signatory_org_id.attribute_code = 'JBR_PERS_ORG'
)
--и из этой организации берем полное имя
left join attribute_value av_signatory_org_fullname on (
	av_signatory_org_fullname.card_id = av_signatory_org_id.number_value and
	av_signatory_org_fullname.attribute_code = 'JBR_DORG_FULLNAME'
)

--Получаем id организации получателя
left join attribute_value av_dest_org_id on (
	av_dest_org_id.card_id = c.card_id and
	av_dest_org_id.attribute_code = 'ADMIN_222015'
)
--и по нему получаем полное имя организации получателя
left join attribute_value av_dest_org_fullname on (
	av_dest_org_fullname.card_id = av_dest_org_id.number_value and
	av_dest_org_fullname.attribute_code = 'JBR_DORG_FULLNAME'
)

--Из ДО получаем дату регистрации
left join attribute_value av_basedoc_regdate on (
	av_basedoc_regdate.card_id = av_basedoc.card_id and
	av_basedoc_regdate.attribute_code = 'JBR_REGD_DATEREG'
)

--Из ДО получаем рег. номер
left join attribute_value av_basedoc_regnumber on (
	av_basedoc_regnumber.card_id = av_basedoc.card_id and
	av_basedoc_regnumber.attribute_code = 'JBR_REGD_REGNUM'
)

--Берем сообщение ГОСТ(если есть) 
left join attribute_value av_gost_id on (
	av_gost_id.card_id = c.card_id and
	av_gost_id.attribute_code = 'JBR_ELM_GOST_MSGS'
)
--и извлекаем из него дату создания
left join attribute_value av_gost_created on (
	av_gost_created.card_id = av_gost_id.number_value
	and av_gost_created.attribute_code = 'CREATED'
)

left join notif_subquery notif_received on (
	notif_received.uuid = av_uuid.string_value and
	notif_received.notif_type = 2900
)
left join notif_subquery notif_registered on (
	notif_registered.uuid = av_uuid.string_value and
	notif_registered.notif_type = 2901
)
