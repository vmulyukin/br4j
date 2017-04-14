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


---Для каждого пользователя определяем его управления---------------------------------------------
WITH RECURSIVE search_up(cycle_id, cycle_number, cycle_base) AS (
  select cast(c.card_id::bigint as numeric),1, c.card_id from card c
  where c.status_id in (4,7) and c.template_id = 484
  UNION ALL
  SELECT v.number_value, su.cycle_number+1, su.cycle_base
  FROM attribute_value v, search_up su
  WHERE v.attribute_code = 'JBR_DEPT_PARENT_LINK'
        AND v.card_id = su.cycle_id
        and exists (select 1 from attribute_value
  where attribute_code = 'JBR_DEPT_PARENT_LINK'
        and card_id = v.number_value)),
    departments as (select (max(ARRAY[cycle_number, cycle_id, cycle_base]))[3] as curDep,
                           (max(ARRAY[cycle_number, cycle_id, cycle_base]))[2] as depLvl2
                    from search_up
                    group by cycle_base)
select p.person_id, curDep, depLvl2, upper(av_name.string_value) = upper('Руководство') as isRuk
into temp table departments from
  person p
  join attribute_value av on p.card_id = av.card_id and av.attribute_code = 'JBR_PERS_DEPT_LINK'
  join departments d on av.number_value = d.curDep
  join attribute_value av_name on d.deplvl2 = av_name.card_id and av_name.attribute_code = 'JBR_DEPT_FULLNAME';

alter table departments
add constraint departments_pk PRIMARY KEY (person_id);
-----------------------------------------------------------------------------------------
----ВСЕ ОГ ЗА ПЕРИОД---------------------------------------------------------------------
select distinct c.card_id, status_id into temp table QR_FSIN_1_1
from card c
  join attribute_value reg_date on c.card_id = reg_date.card_id and reg_date.attribute_code = 'JBR_REGD_DATEREG'
  left join attribute_value av_reg on c.card_id = av_reg.card_id and av_reg.attribute_code = 'JBR_REGD_REGISTRAR'
  left join person p_reg on av_reg.number_value = p_reg.person_id
  left join attribute_value av_d on p_reg.card_id = av_d.card_id and av_d.attribute_code = 'JBR_PERS_ORG'
where c.template_id = 864 and c.status_id in (101,102,103,206,48909,104) and ({0} is null or av_d.number_value in ({0}))
      and (date_trunc('day',reg_date.date_value + interval '{1} hour') between '{2}' and '{3}');

alter table QR_FSIN_1_1
add constraint QR_FSIN_1_1_pk PRIMARY KEY (card_id);

-------------------------------------------------------------------------------------------
------ПОЛУЧАЕМ СВЯЗАННЫЕ ДОКИ--------------------------------------------------------------

select distinct c.card_id, d.depLvl2 into temp table QR_FSIN_1_otv
from QR_FSIN_1_1 c
  join attribute_value av_r on c.card_id = av_r.card_id and av_r.attribute_code = 'JBR_IMPL_ACQUAINT'
  join card c_r on av_r.number_value = c_r.card_id and c_r.status_id not in (34145,303990)
  join attribute_value av_main on av_r.number_value = av_main.card_id and av_main.attribute_code= 'JBR_RESPONS_CONSIDER'
                                  and av_main.value_id = 1449
  join attribute_value av_rassm on av_r.number_value = av_rassm.card_id and av_rassm.attribute_code= 'JBR_RASSM_PERSON'
  join departments d on av_rassm.number_value = d.person_id and d.isRuk = false
  where c.status_id <>  104;

insert into QR_FSIN_1_otv
  select distinct c.card_id, d.depLvl2
  from QR_FSIN_1_1 c
    join attribute_value_archive av_r on c.card_id = av_r.card_id and av_r.attribute_code = 'JBR_IMPL_ACQUAINT'
    join card_archive c_r on av_r.number_value = c_r.card_id and c_r.status_id not in (34145,303990)
    join attribute_value_archive av_main on av_r.number_value = av_main.card_id and av_main.attribute_code= 'JBR_RESPONS_CONSIDER'
                                            and av_main.value_id = 1449
    join attribute_value_archive av_rassm on av_r.number_value = av_rassm.card_id and av_rassm.attribute_code= 'JBR_RASSM_PERSON'
    join departments d on av_rassm.number_value = d.person_id and d.isRuk = false
  where c.status_id = 104;

insert into QR_FSIN_1_otv
  with recursive all_res as (
    select distinct c.card_id, min(av_res1.card_id) as res
    from QR_FSIN_1_1 c
      join attribute_value av_r on c.card_id = av_r.card_id and av_r.attribute_code = 'JBR_IMPL_ACQUAINT'
      join card c_r on av_r.number_value = c_r.card_id and c_r.status_id not in (34145,303990)
      join attribute_value av_main on av_r.number_value = av_main.card_id and av_main.attribute_code= 'JBR_RESPONS_CONSIDER'
                                      and av_main.value_id = 1449
      join attribute_value av_rassm on av_r.number_value = av_rassm.card_id and av_rassm.attribute_code= 'JBR_RASSM_PERSON'
      join departments d on av_rassm.number_value = d.person_id and d.isRuk = true
      join attribute_value av_res1 on av_res1.number_value = c.card_id and av_res1.attribute_code = 'JBR_DOCB_BYDOC'
      join card c_res1 on av_res1.card_id = c_res1.card_id and c_res1.status_id not in (34145,303990)
      join attribute_value av_sign1 on av_sign1.card_id = av_res1.card_id and av_sign1.attribute_code = 'JBR_INFD_SGNEX_LINK'
                                       and av_sign1.number_value = av_rassm.number_value
    where c.status_id <>  104
    group by c.card_id
    union
    select ar.card_id, c_res.card_id
    from all_res ar
      join attribute_value av on ar.res = av.number_value and av.attribute_code = 'JBR_RIMP_PARASSIG'
      join card c_res on av.card_id = c_res.card_id and c_res.status_id not in (34145,303990))
  select ar.card_id, (min(ARRAY[ar.res, d.deplvl2]))[2] from all_res ar
    join attribute_value av on ar.res = av.card_id and av.attribute_code = 'JBR_INFD_EXEC_LINK'
    join departments d on av.number_value = d.person_id and d.isRuk = false
  group by ar.card_id;

insert into QR_FSIN_1_otv
  with recursive all_res as (
    select distinct c.card_id, min(av_res1.card_id) as res
    from QR_FSIN_1_1 c
      join attribute_value_archive av_r on c.card_id = av_r.card_id and av_r.attribute_code = 'JBR_IMPL_ACQUAINT'
      join card_archive c_r on av_r.number_value = c_r.card_id and c_r.status_id not in (34145,303990)
      join attribute_value_archive av_main on av_r.number_value = av_main.card_id and av_main.attribute_code= 'JBR_RESPONS_CONSIDER'
                                              and av_main.value_id = 1449
      join attribute_value_archive av_rassm on av_r.number_value = av_rassm.card_id and av_rassm.attribute_code= 'JBR_RASSM_PERSON'
      join departments d on av_rassm.number_value = d.person_id and d.isRuk = true
      join attribute_value_archive av_res1 on av_res1.number_value = c.card_id and av_res1.attribute_code = 'JBR_DOCB_BYDOC'
      join card_archive c_res1 on av_res1.card_id = c_res1.card_id and c_res1.status_id not in (34145,303990)
      join attribute_value_archive av_sign1 on av_sign1.card_id = av_res1.card_id and av_sign1.attribute_code = 'JBR_INFD_SGNEX_LINK'
                                               and av_sign1.number_value = av_rassm.number_value
    where c.status_id = 104
    group by c.card_id
    union
    select ar.card_id, c_res.card_id
    from all_res ar
      join attribute_value_archive av on ar.res = av.number_value and av.attribute_code = 'JBR_RIMP_PARASSIG'
      join card_archive c_res on av.card_id = c_res.card_id and c_res.status_id not in (34145,303990))
  select ar.card_id, (min(ARRAY[ar.res, d.deplvl2]))[2] from all_res ar
    join attribute_value_archive av on ar.res = av.card_id and av.attribute_code = 'JBR_INFD_EXEC_LINK'
    join departments d on av.number_value = d.person_id and d.isRuk = false
  group by ar.card_id;

create index QR_FSIN_1_otv_idx on QR_FSIN_1_otv using btree (card_id);

with all_person as (
  select distinct c.card_id, av_p.number_value
  from QR_FSIN_1_1 c
    join attribute_value av on c.card_id = av.number_value and av.attribute_code = 'JBR_MAINDOC'
    join card c_av on av.card_id = c_av.card_id and c_av.status_id not in (303990)
    join attribute_value av_p on av.card_id = av_p.card_id and av_p.attribute_code in ('JBR_INFD_EXEC_LINK','ADMIN_255974')
  where c.status_id <>  104
  union
  select distinct c.card_id, av_p.number_value
  from QR_FSIN_1_1 c
    join attribute_value_archive av on c.card_id = av.number_value and av.attribute_code = 'JBR_MAINDOC'
    join card_archive c_av on av.card_id = c_av.card_id and c_av.status_id not in (303990)
    join attribute_value_archive av_p on av.card_id = av_p.card_id and av_p.attribute_code in ('JBR_INFD_EXEC_LINK','ADMIN_255974')
  where c.status_id = 104
  union
  select distinct c.card_id, av_p.number_value
  from QR_FSIN_1_1 c
    join attribute_value av on c.card_id = av.card_id and av.attribute_code = 'JBR_IMPL_ACQUAINT'
    join card c_av on c_av.card_id = av.number_value and c_av.status_id != 34145
    join attribute_value av_p on c_av.card_id = av_p.card_id and av_p.attribute_code = 'JBR_RASSM_PERSON'
  where c.status_id <>  104
  union
  select distinct c.card_id, av_p.number_value
  from QR_FSIN_1_1 c
    join attribute_value_archive av on c.card_id = av.card_id and av.attribute_code = 'JBR_IMPL_ACQUAINT'
    join card_archive c_av on c_av.card_id = av.number_value and c_av.status_id != 34145
    join attribute_value_archive av_p on c_av.card_id = av_p.card_id and av_p.attribute_code = 'JBR_RASSM_PERSON'
  where c.status_id =  104
)
select distinct ap.card_id, d.depLvl2 into temp table QR_FSIN_1_neotv
from all_person ap
  join departments d on ap.number_value = d.person_id and d.isRuk = false;

delete from QR_FSIN_1_neotv nv
where exists (select 1 from QR_FSIN_1_otv ov where ov.card_id = nv.card_id and ov.depLvl2 = nv.depLvl2);

create index QR_FSIN_1_neotv_idx on QR_FSIN_1_neotv using btree(card_id);

--получили ответственные и не ответственные управления

with all_relat as (
  select c.card_id, c_relat.card_id as relat, c.deplvl2 from
    QR_FSIN_1_otv c
    join attribute_value relat on c.card_id = relat.number_value and relat.attribute_code = 'JBR_DOCL_RELATDOC' and relat.value_id = 1502
    join card c_relat on relat.card_id = c_relat.card_id and c_relat.template_id = 364 and c_relat.status_id in (101,104)
  union
  select c.card_id, c_relat.card_id as relat, c.deplvl2 from
    QR_FSIN_1_otv c
    join attribute_value relat on c.card_id = relat.card_id and relat.attribute_code = 'JBR_DOCL_RELATDOC' and relat.value_id = 1502
    join card c_relat on relat.number_value = c_relat.card_id and c_relat.template_id = 364 and c_relat.status_id in (101,104)
)
select distinct ar.card_id, (max(ARRAY[extract(epoch from regdate.date_value), ar.relat]))[2] relat into temp table QR_FSIN_1_rel_out
from all_relat ar
  join attribute_value av_exec on av_exec.card_id = ar.relat and av_exec.attribute_code = 'JBR_INFD_EXECUTOR'
  join departments d on av_exec.number_value = d.person_id
  join attribute_value regdate on ar.relat = regdate.card_id and regdate.attribute_code = 'JBR_REGD_DATEREG'
  join attribute_value res_rassm on ar.relat = res_rassm.card_id and res_rassm.attribute_code = 'ADMIN_283926'
where d.deplvl2 = ar.deplvl2
group by ar.card_id;

create index QR_FSIN_1_rel_out_og on QR_FSIN_1_rel_out using btree(card_id);
create index QR_FSIN_1_rel_out_rel on QR_FSIN_1_rel_out using btree(card_id);

with all_relat as (
  select c.card_id, c_relat.card_id as relat, c.deplvl2 from
    QR_FSIN_1_otv c
    join attribute_value relat on c.card_id = relat.number_value and relat.attribute_code = 'JBR_DOCL_RELATDOC' and relat.value_id = 1502
    join card c_relat on relat.card_id = c_relat.card_id and c_relat.template_id = 784 and c_relat.status_id in (101,102,103,104,48909,206)
  union
  select c.card_id, c_relat.card_id as relat, c.deplvl2 from
    QR_FSIN_1_otv c
    join attribute_value relat on c.card_id = relat.card_id and relat.attribute_code = 'JBR_DOCL_RELATDOC' and relat.value_id = 1502
    join card c_relat on relat.number_value = c_relat.card_id and c_relat.template_id = 784 and c_relat.status_id in (101,102,103,104,48909,206)
  union
  select c.card_id, c_relat.card_id as relat, c.deplvl2 from
    QR_FSIN_1_neotv c
    join attribute_value relat on c.card_id = relat.number_value and relat.attribute_code = 'JBR_DOCL_RELATDOC'
    join card c_relat on relat.card_id = c_relat.card_id and c_relat.template_id = 784 and c_relat.status_id in (101,102,103,104,48909,206)
  union
  select c.card_id, c_relat.card_id as relat, c.deplvl2 from
    QR_FSIN_1_neotv c
    join attribute_value relat on c.card_id = relat.card_id and relat.attribute_code = 'JBR_DOCL_RELATDOC'
    join card c_relat on relat.number_value = c_relat.card_id and c_relat.template_id = 784 and c_relat.status_id in (101,102,103,104,48909,206)
), ruk as (select card_id from attribute_value where attribute_code = 'JBR_DEPT_FULLNAME'
                                                     and upper(string_value) = upper('Руководство') and template_id = 484)
select distinct ar.card_id, (max(ARRAY[extract(epoch from regdate.date_value), ar.relat]))[2] relat into temp table QR_FSIN_1_rel_int
from all_relat ar
  join attribute_value av_exec on av_exec.card_id = ar.relat and av_exec.attribute_code = 'JBR_INFD_EXECUTOR'
  join departments d on av_exec.number_value = d.person_id and d.deplvl2 = ar.deplvl2
  join attribute_value regdate on ar.relat = regdate.card_id and regdate.attribute_code = 'JBR_REGD_DATEREG'
  join attribute_value res_rassm on ar.relat = res_rassm.card_id and res_rassm.attribute_code = 'ADMIN_283926'
  join QR_FSIN_1_otv otv on ar.card_id = otv.card_id
  join attribute_value addr on ar.relat = addr.card_id and addr.attribute_code = 'JBR_INFD_RECEIVER'
  join departments d_addr on addr.number_value = d_addr.person_id and d_addr.deplvl2 in (otv.deplvl2, (select card_id from ruk))
group by ar.card_id, d_addr.deplvl2;

create index QR_FSIN_1_rel_int_og on QR_FSIN_1_rel_int using btree(card_id);
create index QR_FSIN_1_rel_int_rel on QR_FSIN_1_rel_int using btree(card_id);
------------------------------------------------------------------------------------------------------------------------------
with table_1_quest as (
  select c.card_id from card c
    join attribute_value av on c.card_id = av.card_id and av.attribute_code = 'JBR_THEME_ID'
  where c.template_id = 850 and av.string_value in
                                ('Вопросы пенсионного обеспечения сотрудников',
                                 'Вопросы ресурсного обеспечения сотрудников',
                                 'Вопросы труда и заработной платы сотрудников',
                                 'Жилищные вопросы',
                                 'Вопросы здравоохранения и медицинского обслуживания сотрудников',
                                 'Работа с кадрами УИС',
                                 'О нарушении законности сотрудниками УИС',
                                 'О неправильных действиях сотрудников ИУ',
                                 'Вопросы социальной защиты сотрудников УИС')
  union
  select c.card_id from card c
    join attribute_value av on c.card_id = av.card_id and av.attribute_code = 'JBR_THEMATIC_ID'
  where c.template_id = 851 and av.string_value in
                                ('Вопросы материальной помощи',
                                 'Выплата единовременных пособий',
                                 'Назначение пенсий',
                                 'Разъяснение по пенсионному вопросу',
                                 'Невыплата компенсации за вещевое имущество',
                                 'Обеспечение вещевым и материальным довольствием',
                                 'Выплата надбавки за выслугу',
                                 'Выплата районного коэффициента',
                                 'О несвоевременной выплате денежного довольствия, заработной платы',
                                 'Оплата труда',
                                 'Упорядочение зарплаты',
                                 'Жилищные условия пенсионеров',
                                 'Жилищные условия сотрудников',
                                 'Получение ГЖС',
                                 'Вопросы медицинского обслуживания сотрудников УИС и их семей',
                                 'Вопросы протезирования',
                                 'Вопросы санитарно-эпидемического обеспечения',
                                 'Выделение путевок в санатории и дома отдыха',
                                 'Направление на лечение',
                                 'О категории годности к военной службе ВВК',
                                 'О компенсации на санаторно-курортное лечение',
                                 'Восстановление на службе в УИС',
                                 'Изменение формулировки приказа об увольнении',
                                 'Награждения, поощрения',
                                 'Направление в образовательные учреждения ФСИН России',
                                 'Неправильное увольнение из УИС',
                                 'О снятии ограничений в пенсионном обеспечении',
                                 'Перевод в другие органы УИС',
                                 'Прием на работу в УИС',
                                 'Присвоение званий',
                                 'Снятие дисциплинарного взыскания',
                                 'Справки о работе в УИС',
                                 'Стаж службы в органах УИС',
                                 'О выплате пособий участникам ЧАЭС',
                                 'О выплате страховых сумм',
                                 'Злоупотребление сл. полож. руководителями УИС',
                                 'О коррупционных действиях работников УИС',
                                 'Незаконное лишение или предоставление прав осужденным',
                                 'Несвоевременное освобождение из ИГУ',
                                 'Рукоприкладство',
                                 'Недостойное поведение в быту',
                                 'Халатное и неправильное отношение к служебным обязанностям',
                                 'О социально-правовой защите сотрудников УИС и членов их семей')
) select * into temp table  QR_FSIN_1_table_1_quest
  from table_1_quest;

with table_2_quest as (
  select c.card_id from card c
    join attribute_value av on c.card_id = av.card_id and av.attribute_code = 'JBR_THEME_ID'
  where c.template_id = 850 and av.string_value in
                                ('Благодарность сотрудникам УИС',
                                 'Вопросы, не относящиеся к компетенции ФСИН России',
                                 'Вопросы, связанные с отбыванием наказания в ИТУ',
                                 'О недостатках, связанных с пребыванием осужденных и подследственных в СИЗО и тюрьмах',
                                 'Другие вопросы, относящиеся к компетенции ФСИН России',
                                 'О медицинском обеспечении осужденных',
                                 'Вопросы отбывания наказания условно-осужденными',
                                 'О переводе в целях личной безопасности',
                                 'Предложения по улучшению работы УИС'
                                )
  union
  select c.card_id from card c
    join attribute_value av on c.card_id = av.card_id and av.attribute_code = 'JBR_THEMATIC_ID'
  where c.template_id = 851 and av.string_value in
                                ('О ходатайстве по уголовным делам',
                                 'О необеспечении положенным довольствием',
                                 'О предоставлении свиданий',
                                 'О взыскании по исполнительным листам с осужденных',
                                 'О денежных расчетах с осужденными',
                                 'Коммунально-бытовые вопросы',
                                 'Нарушения при конвоировании',
                                 'О конфликтах между осужденными',
                                 'О нарушении режима содержания осужденных',
                                 'О получении полиса обязательного медицинского страхования',
                                 'О профессионально-техническом обучении',
                                 'О розыске осужденных и их родственников',
                                 'О страховании в системе обязательного пенсионного  страхования',
                                 'Об изменении режима заключения',
                                 'Перевод осужденных ближе к м/ж',
                                 'О получении паспорта',
                                 'Вопросы по рациону питания',
                                 'О розыске личных вещей и денежных средств',
                                 'Справки по вопросам отбывания наказания',
                                 'О трудоустройстве',
                                 'Об условно-досрочном освобождении и помиловании',
                                 'Неудовлетворительное мед. обслуживание',
                                 'О направлении на МСЭК',
                                 'Об освобождении от наказания по состоянию здоровья',
                                 'Об установлении группы инвалидности',
                                 'Перевод в другое ИТУ (по состоянию здоровья)',
                                 'Вопросы труда и зарплаты',
                                 'Неправильное отношение к условно-осужденным и условно-освобожденным',
                                 'Об условно-досрочном освобождении',
                                 'Перевод в другое ИТУ (конфликт с администрацией)',
                                 'О незаконном применении спецсредств',
                                 'Незаконное водворение в ШИЗО',
                                 'Нарушение прав осужденных в ИК',
                                 'Перевод в другое ИТУ (конфликт с осужденными)')
) select * into temp table  QR_FSIN_1_table_2_quest
  from table_2_quest;

select distinct c.card_id into temp table QR_FSIN_1_2
from QR_FSIN_1_1 c
  join attribute_value av on c.card_id = av.card_id and av.attribute_code = 'JBR_QUEST_THEMATIC_C'
  join QR_FSIN_1_table_1_quest on av.number_value = QR_FSIN_1_table_1_quest.card_id;

select distinct c.card_id into temp table QR_FSIN_1_25
from QR_FSIN_1_1 c
  join attribute_value av on c.card_id = av.card_id and av.attribute_code = 'JBR_QUEST_THEMATIC_C'
  join QR_FSIN_1_table_2_quest on av.number_value = QR_FSIN_1_table_2_quest.card_id;

with filtered as(
  select c.card_id from QR_FSIN_1_2 c
    join attribute_value av on c.card_id = av.card_id and av.attribute_code = 'JBR_DOCL_RELATDOC'
  union
  select c.card_id from QR_FSIN_1_2 c
    join attribute_value av on c.card_id = av.number_value and av.attribute_code = 'JBR_DOCL_RELATDOC'
) select card_id into temp table QR_FSIN_1_3_7
  from filtered;

alter table QR_FSIN_1_3_7
add constraint QR_FSIN_1_3_7_pk PRIMARY KEY (card_id);

with filtered as(
  select c.card_id from QR_FSIN_1_25 c
    join attribute_value av on c.card_id = av.card_id and av.attribute_code = 'JBR_DOCL_RELATDOC'
  union
  select c.card_id from QR_FSIN_1_25 c
    join attribute_value av on c.card_id = av.number_value and av.attribute_code = 'JBR_DOCL_RELATDOC'
) select card_id into temp table QR_FSIN_1_26_30
  from filtered;

alter table QR_FSIN_1_26_30
add constraint QR_FSIN_1_26_30_pk PRIMARY KEY (card_id);
--получаем срок рассмотрения
with dat as (
  select c.card_id, max(av_rassm_todate.date_value) rassm_to_date from QR_FSIN_1_1 c
    join attribute_value av_rassm on c.card_id = av_rassm.card_id and av_rassm.attribute_code = 'JBR_IMPL_ACQUAINT'
    join card c_rassm on c_rassm.card_id = av_rassm.number_value and c_rassm.status_id not in (1,34145)
    join attribute_value av_rmain on av_rmain.card_id = av_rassm.number_value and av_rmain.attribute_code = 'JBR_RESPONS_CONSIDER' and av_rmain.value_id = 1449
    join attribute_value av_rassm_todate on av_rassm_todate.card_id = av_rassm.number_value and av_rassm_todate.attribute_code = 'JBR_RASSM_TODATE'
  where c.status_id <> 104
  group by c.card_id
  union
  select c.card_id, max(av_rassm_todate.date_value) rassm_to_date from QR_FSIN_1_1 c
    join attribute_value_archive av_rassm on c.card_id = av_rassm.card_id and av_rassm.attribute_code = 'JBR_IMPL_ACQUAINT'
    join card_archive c_rassm on c_rassm.card_id = av_rassm.number_value and c_rassm.status_id not in (1,34145)
    join attribute_value_archive av_rmain on av_rmain.card_id = av_rassm.number_value and av_rmain.attribute_code = 'JBR_RESPONS_CONSIDER' and av_rmain.value_id = 1449
    join attribute_value_archive av_rassm_todate on av_rassm_todate.card_id = av_rassm.number_value and av_rassm_todate.attribute_code = 'JBR_RASSM_TODATE'
  where c.status_id =  104
  group by c.card_id
) select * into QR_FSIN_1_rassm_to_date from dat;
alter table QR_FSIN_1_rassm_to_date
add constraint QR_FSIN_1_rassm_to_date_pk PRIMARY KEY (card_id);