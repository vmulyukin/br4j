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

with notif_table as (
	select 
	c.card_id as id,
	av_uuid.string_value as uuid,
	av_date.date_value as created,
	av_elm_id.number_value as elm_id,
	av_elm_created.date_value as elm_created,
	av_gost.number_value as gost_id,
	av_gost_notif.number_value as gost_notification_id,
	av_notif_type.value_id as notification_type,
	av_elm_uuid.string_value as notification_uuid

	from card c
	join attribute_value av_date on (
		av_date.card_id = c.card_id and 
		av_date.attribute_code = 'CREATED' and
		(date_trunc('day',av_date.date_value) >  date_trunc('day',(CURRENT_TIMESTAMP - CAST((:days||' days') as interval))))
	)
	join attribute_value av_uuid on (
		av_uuid.card_id = c.card_id and 
		av_uuid.attribute_code = 'JBR_DISTR_SRC_UUID'
	)
	--ЭСР,фактически квиток о доставке и регистрации
	join attribute_value av_elm_id on (
		av_elm_id.card_id = c.card_id and
		av_elm_id.attribute_code = 'ADMIN_222990'
	)
	--Получаем дату создания квитка
	left join attribute_value av_elm_created on (
		av_elm_created.card_id = av_elm_id.number_value and
		av_elm_created.attribute_code = 'CREATED'
	)
	--uuid ЭСРa, в котором будет отправлено уведомление
	left join attribute_value av_elm_uuid on (
		av_elm_uuid.card_id = av_elm_id.number_value and
		av_elm_uuid.attribute_code = 'JBR_DIST_UUID'
	)
	left join attribute_value av_gost on (
		av_gost.card_id = av_elm_id.number_value and
		av_gost.attribute_code = 'JBR_ELM_GOST_MSGS'
	)
	left join attribute_value av_gost_notif on (
		av_gost_notif.card_id = av_gost.number_value and
		av_gost_notif.attribute_code = 'JBR_MSG_ACKS'
	)
	left join attribute_value av_notif_type on (
		av_notif_type.card_id = av_gost_notif.number_value and
		av_notif_type.attribute_code = 'JBR_ACK_TYPE'
	)
	where c.template_id = 2424
),

all_status_date_table as (
	select 
	av_uuid.string_value as uuid,
	c.status_id as status,
	av_date.date_value as created
	from card c
	join attribute_value av on (av.card_id = c.card_id and av.attribute_code = 'JBR_DISTR_DLV_METH' and av.value_id = 1522)
	join attribute_value av_date on (
		av_date.card_id = c.card_id and 
		av_date.attribute_code = 'CREATED' and
		(date_trunc('day',av_date.date_value) >  date_trunc('day',(CURRENT_TIMESTAMP - CAST((:days||' days') as interval))))
	)
	join attribute_value av_uuid on (
		av_uuid.card_id = c.card_id and 
		av_uuid.attribute_code = 'JBR_DISTR_SRC_UUID'
	)
	where c.template_id = 2424
),

max_status_table as (
	select all_status_date_table.uuid as uuid,
	max(all_status_date_table.status) as status
	from all_status_date_table
	GROUP BY uuid
),

max_status_max_date_table as (
	select
	max_status_table.*,
	max(all_status_date_table.created) as created
	from max_status_table
	left join all_status_date_table on (max_status_table.uuid = all_status_date_table.uuid and max_status_table.status = all_status_date_table.status)
	group by max_status_table.uuid, max_status_table.status
 )

select 
max_status_max_date_table.*,

notif_received.elm_created as notif_received_created,
notif_received.notification_uuid as notif_received_uuid,
notif_registered.elm_created as notif_registered_created,
notif_registered.notification_uuid as notif_registered_uuid,

av_incom_created.date_value as incoming_created,
av_incom_regnum.string_value as incoming_regnum,
av_incom_regdate.date_value as incoming_regdate,
av_incom_reason.string_value as incoming_reason

from max_status_max_date_table
--Получаем card_id по уникальным наборам (uuid, status, created)
join attribute_value av_uuid on (
	av_uuid.attribute_code = 'JBR_DISTR_SRC_UUID' and
	av_uuid.string_value = max_status_max_date_table.uuid and
	av_uuid.template_id = 2424
)
join attribute_value av_date on (
	av_date.card_id = av_uuid.card_id and 
	av_date.attribute_code = 'CREATED' and
	av_date.date_value = max_status_max_date_table.created
)
--Присоединяем данные из уведомления о доставке
left join notif_table notif_received on (
	notif_received.id = av_uuid.card_id and
	notif_received.notification_type = 2900
)
--Присоединяем дату создания уведомления о регистрации
left join notif_table notif_registered on (
	notif_registered.id = av_uuid.card_id and
	notif_registered.notification_type = 2901
)
--Берем связанный входящий
left join attribute_value av_incom_doc_id on (
	av_incom_doc_id.number_value = av_uuid.card_id and
	av_incom_doc_id.attribute_code = 'JBR_ORIG_SRC' and
	av_incom_doc_id.template_id in (224)
)
--и получаем его дату создания
left join attribute_value av_incom_created on (
	av_incom_created.card_id = av_incom_doc_id.card_id and
	av_incom_created.attribute_code = 'CREATED'
)
--получаем его регистрационный номер
left join attribute_value av_incom_regnum on (
	av_incom_regnum.card_id = av_incom_doc_id.card_id and
	av_incom_regnum.attribute_code = 'JBR_REGD_REGNUM'
)
--и дату регистрации
left join attribute_value av_incom_regdate on (
	av_incom_regdate.card_id = av_incom_doc_id.card_id and
	av_incom_regdate.attribute_code = 'JBR_REGD_DATEREG'
)
--и причину в отказе регистрации
left join attribute_value av_incom_reason on (
	av_incom_reason.card_id = av_incom_doc_id.card_id and
	av_incom_reason.attribute_code = 'JBR_MEDO_REASON_RFS'
)