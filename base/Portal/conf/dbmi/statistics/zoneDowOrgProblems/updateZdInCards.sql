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

CREATE OR REPLACE FUNCTION update_acl_by_profile_rule_and_cards(r_id NUMERIC, link_type VARCHAR, intermed_type VARCHAR, linked_state_id numeric, r_code VARCHAR) RETURNS integer AS
  $body$
DECLARE
	AFFECTED_ROWS BIGINT;
	DELETED_ROWS integer;
	sql_text VARCHAR;
	start_time VARCHAR;
	end_time VARCHAR;
	exec_time VARCHAR;
	PERSON_ALL_PROFILES CONSTANT VARCHAR DEFAULT
			'person_all_profiles as(
							select
								p.person_id,
								av.number_value,
								av.attribute_code,
								par.rule_id,
								par.link_attr_code,
								par.target_attr_code
							from
								profile_access_rule par
								{0}
								join person p
									on {1}
								join attribute_value av
									on av.attribute_code = par.profile_attr_code
									and av.template_id = 10
									and p.card_id = av.card_id
							where
								par.rule_id = $1
						)';

BEGIN
	RAISE NOTICE 'Пересчитываем матрицу прав для профильного правила №%: link_type = ''%'', intermed_type = ''%'', linked_state_id = %, r_code = ''%'' ', r_id, link_type, intermed_type, linked_state_id, r_code;
	SELECT clock_timestamp() INTO start_time;
	RAISE NOTICE 'Время старта - ''%''', start_time;
	--Проверка наличия правила в системе
	IF NOT EXISTS (SELECT 1 FROM access_rule WHERE rule_id=r_id) THEN
		RAISE NOTICE 'Правило не найдено, пропускаем...';
		RETURN 0;
	END IF;

	-- удаляем старые права из матрицы
	WITH a AS (
		DELETE from access_list al USING cards_for_recalc_zd_rules cr where al.rule_id=r_id and cr.card_id = al.card_id RETURNING 1
	) SELECT COUNT(*) INTO DELETED_ROWS from a;
	RAISE NOTICE 'Удалено % записей', DELETED_ROWS;

	sql_text := '';
	IF (link_type is NULL) THEN
		RAISE NOTICE 'Атрибут Связи отсутствует';
		sql_text :=
					'WITH b AS (
					WITH ';
		if (r_code IS NOT NULL) THEN
			sql_text := sql_text ||
								replace(replace(PERSON_ALL_PROFILES, '{0}',
								'					JOIN person_role sr '||
								'							ON par.role_code=sr.role_code '), '{1}', '(p.person_id=sr.person_id)');
		ELSE
			sql_text := sql_text ||
								replace(replace(PERSON_ALL_PROFILES, '{0}',
								''), '{1}', '(par.role_code IS NULL)');
		END IF;
		sql_text := sql_text ||
					', rules as (
					  select a1.template_id,
					    a1.status_id,
					    a1.rule_id,
					    a2.role_code,
					    a2.target_attr_code,
					    a2.profile_attr_code,
					    a3.card_id
					  from access_rule a1
					    join profile_access_rule a2
					      on a1.rule_id = $1 and a1.rule_id=a2.rule_id
					    join card a3
					      on (a3.template_id=a1.template_id)
					     and (a3.status_id=a1.status_id)
						 join cards_for_recalc_zd_rules cr on a3.card_id = cr.card_id
					)
						INSERT INTO access_list(card_id, rule_id, person_id, source_value_id)
						SELECT distinct
							r.card_id,
							r.rule_id,
							pv.person_id,
							tv.attr_value_id
						FROM
							rules r ';
		sql_text := sql_text||
					'		JOIN attribute_value tv
									ON r.card_id=tv.card_id
									AND r.target_attr_code=tv.attribute_code
							JOIN person_all_profiles pv
									ON tv.number_value=pv.number_value
									AND r.profile_attr_code=pv.attribute_code
						WHERE
							(1=1) ';

		sql_text := sql_text||
					'RETURNING 1
					) SELECT COUNT(*) FROM b';
	END IF;

	IF (sql_text = null or sql_text = '' or sql_text = '<NULL>') THEN
		RAISE NOTICE 'Тип атрибутов связи не корректный, пропускаем правило...';
		RETURN 0;
	ELSE
		RAISE NOTICE 'Запрос:
		%', sql_text;
		EXECUTE sql_text INTO AFFECTED_ROWS USING r_id;
		SELECT clock_timestamp() INTO end_time;
		RAISE NOTICE 'Добавлено % записей в матрицу прав', AFFECTED_ROWS;
		execute 'select timestamp '''||end_time||'''-timestamp '''||start_time||'''' into exec_time;
		RAISE NOTICE 'Время завершения - ''%''', end_time;
		RAISE NOTICE 'Время выполнения - ''%''', exec_time;
		RETURN AFFECTED_ROWS;
	END IF;
END;
$body$ LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION update_zd_rules(recalc_type int) RETURNS BIGINT AS
  $body$
DECLARE
	ALL_COUNT BIGINT;
	rec RECORD;
	rule_count INT;
	i INT;
BEGIN
	ALL_COUNT := 0;
	RAISE NOTICE 'НАЧИНАЕМ ОЧЕРЕДНОЙ ПЕРЕСЧЁТ МАТРИЦЫ ПРАВ!';
--	RAISE NOTICE 'Коньяк скорее всего закончился, нервы наверное уже тоже, поэтому просто поспите, остался последний рывок...';
	-- пересчитывать будем права на профильные правила
	RAISE NOTICE '-----------------Профильные правила-------------------';
	-- если надо пересчитать права для правил без АС
	if ((recalc_type is NULL) or (recalc_type=3)) THEN
		RAISE NOTICE '-----------------Профильные правила для 224 864 без атрибута связи по зонам ДОУ-------------------';
		SELECT count(*) INTO rule_count from access_rule ar
		join profile_access_rule pr on ar.rule_id = pr.rule_id and pr.target_attr_code = 'JBR_ZONE_DOW' and pr.link_attr_code is null
		where ar.template_id in (224,864) and ar.status_id = 301;
		RAISE NOTICE 'Всего профильных правил с пустыми атрибутами связи, для которых есть карточки - %', rule_count;
		i := 0;
		FOR /*r_id, link_type, intermed_type, linked_state_id, r_code*/ rec IN SELECT pr.rule_id as r_id, NULL as link_type, NULL as intermed_type, pr.linked_status_id as linked_state_id, pr.role_code as r_code from profile_access_rule pr where (pr.intermed_attr_code is null) and (pr.link_attr_code is null) and exists(select 1 from card c join access_rule ar on ar.status_id = c.status_id and ar.template_id = c.template_id where ar.rule_id = pr.rule_id) and pr.rule_id in (select ar.rule_id from access_rule ar join profile_access_rule pr on ar.rule_id = pr.rule_id and pr.target_attr_code = 'JBR_ZONE_DOW' and pr.link_attr_code is null
			where ar.template_id in (224,864) and ar.status_id = 301) order by 1 asc
		loop
			i := i+1;
			RAISE NOTICE '';
			RAISE NOTICE '----------Правило %/%----------', i, rule_count;
			SELECT update_acl_by_profile_rule_and_cards(rec.r_id, NULL, NULL, rec.linked_state_id, rec.r_code)+ALL_COUNT INTO ALL_COUNT;
			RAISE NOTICE '----------Правило %/%----------', i, rule_count;
		end loop;
		RAISE NOTICE '-----------------Профильные правила без АС-------------------';
	END IF;

	RAISE NOTICE '-----------------Профильные правила-------------------';
	RAISE NOTICE 'ПЕРЕСЧЁТ ПРАВ ЗАВЕРШЕН, ДОБАВЛЕНО ВСЕГО ''%'' ЗАПИСЕЙ!', ALL_COUNT;
	RETURN ALL_COUNT;
END;
$body$ LANGUAGE 'plpgsql';

GRANT EXECUTE ON FUNCTION update_acl_by_profile_rule_and_cards(r_id NUMERIC, link_type VARCHAR, intermed_type VARCHAR, linked_state_id numeric, r_code VARCHAR) TO "dbmi";
GRANT EXECUTE ON FUNCTION update_zd_rules(recalc_type int) TO "dbmi";

BEGIN TRANSACTION;
drop table if exists cards_for_recalc_zd_rules;
--удаляем нулевые ЗД
delete from attribute_value where attribute_code = 'JBR_ZONE_DOW' and number_value is null and template_id in (224,864);

--сохраняем id карточек
select c.card_id into table cards_for_recalc_zd_rules
from card c
  left join attribute_value av on c.card_id = av.card_id and av.attribute_code = 'JBR_ZONE_DOW'
where 	av.card_id is null
       and c.status_id = 301
       and c.template_id in (224,864);

--вставляем JBR_ZONE_DOW
insert into attribute_value(card_id, attribute_code, number_value)
--по адресату
  select c.card_id, 'JBR_ZONE_DOW', zd.number_value from card c
    left join attribute_value av on c.card_id = av.card_id and av.attribute_code = 'JBR_ZONE_DOW'
    join attribute_value av_adr on c.card_id = av_adr.card_id and av_adr.attribute_code = 'JBR_INFD_RECEIVER'
    left join person p on av_adr.number_value  = p.person_id
    left join attribute_value zd on zd.card_id = p.card_id and zd.attribute_code = 'JBR_ZONE_DOW'
  where 	av.card_id is null
         and c.status_id = 301
         and c.template_id in (224,864)

  union
  --если по адресату не получилось, то по Организации-получателю
  select c.card_id, 'JBR_ZONE_DOW', reciever_zd.number_value  from card c
    left join attribute_value av on c.card_id = av.card_id and av.attribute_code = 'JBR_ZONE_DOW'
    left join attribute_value av_adr on c.card_id = av_adr.card_id and av_adr.attribute_code = 'JBR_INFD_RECEIVER'
    left join person p on av_adr.number_value  = p.person_id
    left join attribute_value zd on zd.card_id = p.card_id and zd.attribute_code = 'JBR_ZONE_DOW'

    join attribute_value reciever on reciever.card_id = c.card_id and reciever.attribute_code = 'JBR_RECEIVER_ACTL'
    join attribute_value reciever_zd on reciever_zd.card_id = reciever.number_value and reciever_zd.attribute_code = 'JBR_ZONES_DOW'
  where 	av.card_id is null and zd.number_value is null
         and c.status_id = 301
         and c.template_id in (224,864);
--пересчитываем права для карточек по правилам на ЗД
SELECT update_zd_rules(3);
--удаляем таблицу с id обновленных карточек
drop table if exists cards_for_recalc_zd_rules;

END TRANSACTION;

DROP FUNCTION update_acl_by_profile_rule_and_cards(r_id NUMERIC, link_type VARCHAR, intermed_type VARCHAR, linked_state_id numeric, r_code VARCHAR);
DROP FUNCTION update_zd_rules(recalc_type int);