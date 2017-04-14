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

/* Доработаны скрипты для АС и АПС беклинков: учтено, что по свойству LINK в таблице attribute_option в колонке option_value может быть перечисление атрибутов через ';'
*/
-- пересчитываем матрицу прав для персональных и профильных правил - аналог кнопки Загрузить в БД утилиты
-- при составлении запросов учитывется наличие/отсутствие целевого статуса и наличие/отсутсвие роли в правиле
CREATE OR REPLACE FUNCTION recalculate_access_list_for_person_rule(r_id NUMERIC, link_type VARCHAR, intermed_type VARCHAR, linked_state_id numeric, r_code VARCHAR) RETURNS integer AS 
$body$
DECLARE
	AFFECTED_ROWS BIGINT;
	DELETED_ROWS integer;
	sql_text VARCHAR;
	start_time VARCHAR;
	end_time VARCHAR;
	exec_time VARCHAR;
	main_select VARCHAR;
	-- запрос на извлечение основных атрибутов для таблицы access_list из полученного дерева карточек
	SELECT_MAIN_ATTRIBUTE_FROM_CARD_TREE_FOR_PERSON_RULES CONSTANT VARCHAR DEFAULT 
		'select distinct
			ct.card_id, 
			ct.rule_id, 
			pv.number_value as person_id,  
			pv.attr_value_id 
		from card_tree ct
		{0}
			JOIN attribute_value pv ON ct.link_card_id=pv.card_id
			AND ct.person_attr_code=pv.attribute_code
		{1}	'; 
		
	/* Запрос для извлечения самых верхних родителей на основе backlink-атрибута link_attr_code во входном персональном правиле
	 для backlink-атрибута link_attr_code */
	SELECT_UPPER_PARENT_CARD_FOR_PERSON_RULE CONSTANT VARCHAR DEFAULT 
		'			( 
						SELECT  
							c.card_id,  
							r.rule_id,  
							functionbacklink(c.card_id, aoU.option_value, aoL.option_value) as link_card_id,      
							pr.linked_status_id, 
							pr.role_code, 
							pr.intermed_attr_code, 
							pr.person_attr_code 
						FROM card c 
							JOIN access_rule r  	
									ON 	(c.template_id=r.template_id)  
									AND (c.status_id=r.status_id) 
							JOIN person_access_rule pr 
									ON 	r.rule_id=pr.rule_id  
							JOIN attribute_option aoL  
									ON pr.link_attr_code=aoL.attribute_code  
									AND aoL.option_code=''LINK'' 
							JOIN attribute_option aoU  
									ON pr.link_attr_code=aoU.attribute_code  
									AND aoU.option_code=''UPLINK'' 
						WHERE r.rule_id = $1 
						UNION ALL 
						SELECT  
							c.card_id,  
							r.rule_id,  
							av.card_id as link_card_id,      
							pr.linked_status_id, 
							pr.role_code, 
							pr.intermed_attr_code, 
							pr.person_attr_code 
						FROM card c 
							JOIN access_rule r  	
									ON 	(c.template_id=r.template_id)  
									AND (c.status_id=r.status_id) 
							JOIN person_access_rule pr 
									ON 	r.rule_id=pr.rule_id  
							JOIN attribute_option aoL  
									ON pr.link_attr_code=aoL.attribute_code  
									AND aoL.option_code=''LINK'' 
							LEFT JOIN attribute_option aoU  
									ON pr.link_attr_code=aoU.attribute_code  
									AND aoU.option_code=''UPLINK'' 
							JOIN attribute_value av 
									ON av.attribute_code = aoL.option_value
									and av.number_value = c.card_id 
						WHERE r.rule_id = $1 
							and aoU.option_value is NULL 
					) as c '; 	

	/* запрос на извлечение детей первого уровня на основе cardlink-атрибута link_attr_code во входном персональном правиле
	 для cardlink-атрибута link_attr_code */
	SELECT_FIRST_CHILD_CARD_FOR_PERSON_RULE  CONSTANT VARCHAR DEFAULT 
		'			( 
						SELECT  
							c.card_id,  
							r.rule_id,  
							lv.number_value as link_card_id,      
							pr.linked_status_id, 
							pr.role_code, 
							pr.intermed_attr_code, 
							pr.person_attr_code 
						FROM card c 
							JOIN access_rule r 	
									ON (c.template_id=r.template_id) 
									AND (c.status_id=r.status_id) 
							JOIN person_access_rule pr 
									ON r.rule_id=pr.rule_id 
							JOIN attribute_value lv 
									ON c.card_id=lv.card_id 
									AND pr.link_attr_code=lv.attribute_code 
						WHERE  
							r.rule_id=$1
					) as c '; 

	/* список колонок, которые надо извлечь из предыдущих карточек, чтобы изспользовать полученный список в качестве первых родительских
	для backlink-атрибута intermed_attr_code */
	COLUMN_LIST_FIRST_PARENT_CARD_OF_PREV_CARD_FOR_PERSON_RULE CONSTANT VARCHAR DEFAULT 
		'			c.card_id, 
					c.rule_id, 
					lv.card_id as link_card_id, 
					c.linked_status_id, 
					c.role_code, 
					c.person_attr_code, 
					o.option_value as link_attr_code ';

	/* список колонок, которые надо извлечь из предыдущих карточек, чтобы изспользовать полученный список в качестве первых дочерних
	 для cardlink-атрибута intermed_attr_code */
	COLUMN_LIST_FIRST_CHILD_CARD_OF_PREV_CARD_FOR_PERSON_RULE CONSTANT VARCHAR DEFAULT 
		'			c.card_id, 
					c.rule_id, 
					lv.number_value as link_card_id, 
					c.linked_status_id, 
					c.role_code, 
					c.person_attr_code, 
					c.intermed_attr_code as link_attr_code ';

	/* запрос на извлечение родителей первого уровня в персональных правилах для вычесленных ввиде дерева карточек  
	для backlink-атрибута intermed_attr_code */
	SELECT_FIRST_PARENT_CARD_OF_PREV_CARD_FOR_PERSON_RULE CONSTANT VARCHAR DEFAULT 
		'	select 
				ct.card_id, 
				ct.rule_id, 
				lv.card_id as link_card_id, 
				ct.linked_status_id, 
				ct.role_code, 
				ct.person_attr_code, 
				ct.link_attr_code 
			from 
				card_tree ct 
				JOIN attribute_value lv 
						ON ct.link_card_id=lv.number_value 
						AND lv.attribute_code = ct.link_attr_code ';

	/* запрос на извлечение детей первого уровня в персональных правилах для вычисленных в виде дерева карточек  
	 для cardlink-атрибута intermed_attr_code */
	SELECT_FIRST_CHILD_CARD_OF_PREV_CARD_FOR_PERSON_RULE CONSTANT VARCHAR DEFAULT 
		'	select 
				ct.card_id, 
				ct.rule_id, 
				lv.number_value as link_card_id, 
				ct.linked_status_id, 
				ct.role_code, 
				ct.person_attr_code, 
				ct.link_attr_code 
			from 
				card_tree ct 
				JOIN attribute_value lv 
						ON ct.link_card_id=lv.card_id 
						AND lv.attribute_code = ct.link_attr_code ';


	/* подзапросы для вычисления первых родительских карточек для найденных ранее карточек, используется совместно с COLUMN_LIST_FIRST_PARENT_CARD_OF_PREV_CARD_FOR_PERSON_RULE
	 для backlink-атрибута intermed_attr_code */
	JOIN_FIRST_PARENT_CARD_OF_PREV_CARD_FOR_PERSON_RULE CONSTANT VARCHAR DEFAULT 
		'			JOIN attribute_option o 
							ON c.intermed_attr_code=o.attribute_code 
							AND o.option_code=''LINK'' 
					JOIN attribute_value lv 
							ON c.link_card_id=lv.number_value 
							AND o.option_value=lv.attribute_code ';
		
	/* подзапросы для вычисления первых дочерних карточек для найденных ранее карточек, используется совместно с COLUMN_LIST_FIRST_CHILD_CARD_OF_PREV_CARD_FOR_PERSON_RULE */
	JOIN_FIRST_CHILD_CARD_OF_PREV_CARD_FOR_PERSON_RULE CONSTANT VARCHAR DEFAULT 
		'			JOIN attribute_value lv 
							ON c.link_card_id=lv.card_id 
							AND c.intermed_attr_code=lv.attribute_code ';
		
BEGIN
	RAISE NOTICE 'Пересчитываем матрицу прав для персонального правила №%: link_type = ''%'', intermed_type = ''%'', linked_state_id = %, r_code = ''%'' ', r_id, link_type, intermed_type, linked_state_id, r_code;	
	SELECT clock_timestamp() INTO start_time;
	RAISE NOTICE 'Время старта - ''%''', start_time;
	--Проверка наличия правила в системе
	IF NOT EXISTS (SELECT 1 FROM access_rule WHERE rule_id=r_id) THEN
		RAISE NOTICE 'Правило не найдено, пропускаем...';
		RETURN 0;
	END IF; 

	-- удаляем старые права из матрицы
	WITH a AS (
		DELETE from access_list where rule_id=r_id RETURNING 1
	) SELECT COUNT(*) INTO DELETED_ROWS from a;
	RAISE NOTICE 'Удалено % записей', DELETED_ROWS;	
	
	sql_text := '';
	IF (link_type is NULL) THEN
		RAISE NOTICE 'Атрибут Связи отсутствует';
		sql_text := 
					'WITH b AS ( 
						INSERT INTO access_list(card_id, rule_id, person_id, source_value_id) 
						SELECT 
							c.card_id, 
							r.rule_id, 
							pv.number_value, 
							pv.attr_value_id 
						FROM 
							card c 
							JOIN access_rule r 
								ON (c.template_id=r.template_id) 
								AND (c.status_id=r.status_id) 
							JOIN person_access_rule pr 
								ON r.rule_id=pr.rule_id ';
		-- если роль задана, то тобавляем фильтрацию
		IF (r_code IS NOT NULL) THEN
			sql_text := sql_text||
					'		JOIN person_role sr 
								ON pr.role_code=sr.role_code ';
		END IF;
		sql_text := sql_text||
					'		JOIN attribute_value pv 
								ON c.card_id=pv.card_id 
								AND pr.person_attr_code=pv.attribute_code 
						WHERE 
							(1=1) ';
		-- если роль задана, то тобавляем фильтрацию
		IF (r_code IS NOT NULL) THEN
			sql_text := sql_text||
					'		AND (pv.number_value=sr.person_id) ';
		ELSE
			sql_text := sql_text||
					'		AND (pr.role_code is NULL) ';
		END IF;
		sql_text := sql_text||
					'		AND r.rule_id=$1';
		sql_text := sql_text||
					'RETURNING 1 
					) SELECT COUNT(*) FROM b';
	ELSIF (link_type = 'B')	THEN
		RAISE NOTICE 'Атрибут Связи - беклинк';
		IF (intermed_type = 'B') THEN
			RAISE NOTICE 'Атрибут Промежуточной Связи - бэклинк';
			sql_text := 
						'WITH b AS ( 
						WITH RECURSIVE card_tree(card_id, rule_id, link_card_id, linked_status_id, role_code, person_attr_code, link_attr_code) AS (
								SELECT '||
									COLUMN_LIST_FIRST_PARENT_CARD_OF_PREV_CARD_FOR_PERSON_RULE ||
						'		FROM ';
			sql_text := sql_text || SELECT_UPPER_PARENT_CARD_FOR_PERSON_RULE;
			sql_text := sql_text || JOIN_FIRST_PARENT_CARD_OF_PREV_CARD_FOR_PERSON_RULE;
			sql_text := sql_text || '	UNION ALL ';
			sql_text := sql_text || SELECT_FIRST_PARENT_CARD_OF_PREV_CARD_FOR_PERSON_RULE;

			sql_text := sql_text ||
						') '||
						'	INSERT INTO access_list(card_id, rule_id, person_id, source_value_id) ';

			main_select := SELECT_MAIN_ATTRIBUTE_FROM_CARD_TREE_FOR_PERSON_RULES;
			IF (linked_state_id is NOT NULL) THEN
				main_select := replace(main_select, '{0}', 'join card lc ON ct.link_card_id=lc.card_id and (ct.linked_status_id=lc.status_id)');
			ELSE
				main_select := replace(main_select, '{0}', '');
			END IF;
			
			IF (r_code IS NOT NULL) THEN
				main_select := replace(main_select, '{1}', 'join person_role pr on pv.number_value=pr.person_id 
																													and pr.role_code = ct.role_code ');
			ELSE 
				main_select := replace(main_select, '{1}', '');
			END IF;
			
			sql_text := sql_text || main_select;
			
			sql_text := sql_text||
					' RETURNING 1 
					) SELECT COUNT(*) FROM b';
						
		ELSIF (intermed_type = 'C' or intermed_type = 'E' or intermed_type = 'F') THEN
			RAISE NOTICE 'Атрибут Промежуточной Связи - кардлинк или типизированный кардлинк';
			sql_text := 
						'WITH b AS ( 
						WITH RECURSIVE card_tree(card_id, rule_id, link_card_id, linked_status_id, role_code, person_attr_code, link_attr_code) AS (
								SELECT '||
									COLUMN_LIST_FIRST_CHILD_CARD_OF_PREV_CARD_FOR_PERSON_RULE ||
						'		FROM ';
						
			sql_text := sql_text || SELECT_UPPER_PARENT_CARD_FOR_PERSON_RULE;
			sql_text := sql_text || JOIN_FIRST_CHILD_CARD_OF_PREV_CARD_FOR_PERSON_RULE;
			sql_text := sql_text || '	UNION ALL ';
			sql_text := sql_text || SELECT_FIRST_CHILD_CARD_OF_PREV_CARD_FOR_PERSON_RULE;

			sql_text := sql_text ||
						') '||
						'	INSERT INTO access_list(card_id, rule_id, person_id, source_value_id) ';
			
			main_select := SELECT_MAIN_ATTRIBUTE_FROM_CARD_TREE_FOR_PERSON_RULES;
			IF (linked_state_id is NOT NULL) THEN
				main_select := replace(main_select, '{0}', 'join card lc ON ct.link_card_id=lc.card_id and (ct.linked_status_id=lc.status_id)');
			ELSE
				main_select := replace(main_select, '{0}', '');
			END IF;
			
			IF (r_code IS NOT NULL) THEN
				main_select := replace(main_select, '{1}', 'join person_role pr on pv.number_value=pr.person_id 
																													and pr.role_code = ct.role_code ');
			ELSE 
				main_select := replace(main_select, '{1}', '');
			END IF;
			
			sql_text := sql_text || main_select;
			
			sql_text := sql_text||
					'RETURNING 1 '||
					') SELECT COUNT(*) FROM b';
		ELSE 
			RAISE NOTICE 'Атрибут Промежуточной Связи отсутствует';
			sql_text := 
						'WITH b AS ( 
							INSERT INTO access_list(card_id, rule_id, person_id, source_value_id) 
								select 
									c.card_id, 
									c.rule_id, 
									pv.number_value as person_id, 
									pv.attr_value_id 
								FROM ';
			sql_text := sql_text || SELECT_UPPER_PARENT_CARD_FOR_PERSON_RULE;
			sql_text := sql_text ||
						'			JOIN card lc '|| 
						'					ON lc.card_id = c.link_card_id ';
			-- если статус свсязанных карточек задан, то тобавляем фильтрацию
			IF not(linked_state_id is NULL) THEN
				sql_text := sql_text ||
						'					AND (c.linked_status_id=lc.status_id) ';
			END IF;
			sql_text := sql_text ||
						'			JOIN attribute_value pv 
											ON lc.card_id=pv.card_id 
											AND c.person_attr_code=pv.attribute_code ';
			IF (r_code IS NOT NULL) THEN
				sql_text := sql_text || 
					'join person_role pr on pv.number_value=pr.person_id and pr.role_code = c.role_code ';
			END IF;	
			sql_text := sql_text||
					'RETURNING 1 
					) SELECT COUNT(*) FROM b';
		END IF;
	ELSIF (link_type = 'C' or link_type = 'E' or link_type = 'F')	THEN
		RAISE NOTICE 'Атрибут Связи - кардлинк или типизированный кардлинк';
		IF (intermed_type = 'B') THEN
			RAISE NOTICE 'Атрибут Промежуточной Связи - бэклинк';
			sql_text := 
						'WITH b AS ( 
						WITH RECURSIVE card_tree(card_id, rule_id, link_card_id, linked_status_id, role_code, person_attr_code, link_attr_code) AS (
							SELECT '||
								COLUMN_LIST_FIRST_PARENT_CARD_OF_PREV_CARD_FOR_PERSON_RULE||
						'	FROM ';
						
			sql_text := sql_text || SELECT_FIRST_CHILD_CARD_FOR_PERSON_RULE;
			sql_text := sql_text || JOIN_FIRST_PARENT_CARD_OF_PREV_CARD_FOR_PERSON_RULE;
			sql_text := sql_text || '	UNION ALL ';
			sql_text := sql_text || SELECT_FIRST_PARENT_CARD_OF_PREV_CARD_FOR_PERSON_RULE;
			
			sql_text := sql_text ||
						') '||
						'	INSERT INTO access_list(card_id, rule_id, person_id, source_value_id) ';
						
			main_select := SELECT_MAIN_ATTRIBUTE_FROM_CARD_TREE_FOR_PERSON_RULES;
			IF (linked_state_id is NOT NULL) THEN
				main_select := replace(main_select, '{0}', 'join card lc ON ct.link_card_id=lc.card_id and (ct.linked_status_id=lc.status_id)');
			ELSE
				main_select := replace(main_select, '{0}', '');
			END IF;
			
			IF (r_code IS NOT NULL) THEN
				main_select := replace(main_select, '{1}', 'join person_role pr on pv.number_value=pr.person_id 
																													and pr.role_code = ct.role_code ');
			ELSE 
				main_select := replace(main_select, '{1}', '');
			END IF;
			
			sql_text := sql_text || main_select;
			
			sql_text := sql_text||
					'RETURNING 1 '||
					') SELECT COUNT(*) FROM b';
		ELSIF (intermed_type = 'C' or intermed_type = 'E' or intermed_type = 'F') THEN
			RAISE NOTICE 'Атрибут Промежуточной Связи - кардлинк или типизированный кардлинк';
			sql_text := 
						'WITH b AS ( 
						WITH RECURSIVE card_tree(card_id, rule_id, link_card_id, linked_status_id, role_code, person_attr_code, link_attr_code) AS (
							SELECT '||
								COLUMN_LIST_FIRST_CHILD_CARD_OF_PREV_CARD_FOR_PERSON_RULE||
						'	FROM ';
			
			sql_text := sql_text || SELECT_FIRST_CHILD_CARD_FOR_PERSON_RULE;
			sql_text := sql_text || JOIN_FIRST_CHILD_CARD_OF_PREV_CARD_FOR_PERSON_RULE;
			sql_text := sql_text || '	UNION ALL ';
			sql_text := sql_text || SELECT_FIRST_CHILD_CARD_OF_PREV_CARD_FOR_PERSON_RULE;
			
			sql_text := sql_text ||
						') 
							INSERT INTO access_list(card_id, rule_id, person_id, source_value_id) ';

			main_select := SELECT_MAIN_ATTRIBUTE_FROM_CARD_TREE_FOR_PERSON_RULES;
			IF (linked_state_id is NOT NULL) THEN
				main_select := replace(main_select, '{0}', 'join card lc ON ct.link_card_id=lc.card_id and (ct.linked_status_id=lc.status_id)');
			ELSE
				main_select := replace(main_select, '{0}', '');
			END IF;
			
			IF (r_code IS NOT NULL) THEN
				main_select := replace(main_select, '{1}', 'join person_role pr on pv.number_value=pr.person_id 
																													and pr.role_code = ct.role_code ');
			ELSE 
				main_select := replace(main_select, '{1}', '');
			END IF;
			
			sql_text := sql_text || main_select;
			
			sql_text := sql_text||
					'RETURNING 1 
					) SELECT COUNT(*) FROM b';
		ELSE 
			RAISE NOTICE 'Атрибут Промежуточной Связи отсутствует';
			sql_text := 
						'WITH b AS ( 
							INSERT INTO access_list(card_id, rule_id, person_id, source_value_id) 
							SELECT 
								c.card_id, 
								c.rule_id, 
								pv.number_value, 
								pv.attr_value_id 
							FROM ';
			sql_text := sql_text || SELECT_FIRST_CHILD_CARD_FOR_PERSON_RULE;
			sql_text := sql_text ||
						'		JOIN card lc '||
						'			ON c.link_card_id=lc.card_id ';
			-- если статус свсязанных карточек задан, то тобавляем фильтрацию
			IF not(linked_state_id is NULL) THEN
				sql_text := sql_text ||
						'					AND (c.linked_status_id=lc.status_id) ';
			END IF;
			sql_text := sql_text ||
						'		JOIN attribute_value pv 
									ON lc.card_id=pv.card_id 
									AND c.person_attr_code=pv.attribute_code ';

			IF (r_code IS NOT NULL) THEN
				sql_text := sql_text || 
					'join person_role pr on pv.number_value=pr.person_id and pr.role_code = c.role_code ';
			END IF;
			sql_text := sql_text||
					'RETURNING 1 
					) SELECT COUNT(*) FROM b';

		END IF;
	END IF;

	IF (sql_text = '') THEN
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

CREATE OR REPLACE FUNCTION recalculate_access_list_for_profile_rule(r_id NUMERIC, link_type VARCHAR, intermed_type VARCHAR, linked_state_id numeric, r_code VARCHAR) RETURNS integer AS 
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
	-- запрос на извлечение основных атрибутов для таблицы access_list из полученного дерева карточек
	SELECT_MAIN_ATTRIBUTE_FROM_CARD_TREE_FOR_PROFILE_RULES CONSTANT VARCHAR DEFAULT 
		'select distinct
			ct.card_id, 
			ct.rule_id, 
			pv.person_id as person_id,  
			tv.attr_value_id
		from card_tree ct
		{0}
			JOIN attribute_value tv
				ON ct.link_card_id=tv.card_id
				AND ct.target_attr_code=tv.attribute_code
			JOIN person_all_profiles pv
				ON tv.number_value=pv.number_value
				AND ct.profile_attr_code=pv.attribute_code '; 
		
	/* Запрос для извлечения самых верхних родителей на основе backlink-атрибута link_attr_code во входном профильном правиле
	 для backlink-атрибута link_attr_code */
	SELECT_UPPER_PARENT_CARD_FOR_PROFILE_RULE CONSTANT VARCHAR DEFAULT 
		'			( 
						SELECT  
							c.card_id,  
							r.rule_id,  
							functionbacklink(c.card_id, aoU.option_value, aoL.option_value) as link_card_id,      
							pr.linked_status_id, 
							{0}, 
							pr.role_code, 
							pr.intermed_attr_code, 
							pr.profile_attr_code, 
							pr.target_attr_code 
						FROM card c 
							JOIN access_rule r  	
									ON 	(c.template_id=r.template_id)  
									AND (c.status_id=r.status_id) 
							JOIN profile_access_rule pr 
									ON 	r.rule_id=pr.rule_id  
							{1}  
							JOIN attribute_option aoL  
									ON pr.link_attr_code=aoL.attribute_code  
									AND aoL.option_code=''LINK'' 
							JOIN attribute_option aoU  
									ON pr.link_attr_code=aoU.attribute_code  
									AND aoU.option_code=''UPLINK'' 
						WHERE r.rule_id = $1 
						UNION ALL 
						SELECT  
							c.card_id,  
							r.rule_id,  
							av.card_id as link_card_id,      
							pr.linked_status_id, 
							{0}, 
							pr.role_code, 
							pr.intermed_attr_code, 
							pr.profile_attr_code, 
							pr.target_attr_code 
						FROM card c 
							JOIN access_rule r  	
									ON 	(c.template_id=r.template_id)  
									AND (c.status_id=r.status_id) 
							JOIN profile_access_rule pr 
									ON 	r.rule_id=pr.rule_id  
							{1}  
							JOIN attribute_option aoL  
									ON pr.link_attr_code=aoL.attribute_code  
									AND aoL.option_code=''LINK'' 
							LEFT JOIN attribute_option aoU  
									ON pr.link_attr_code=aoU.attribute_code  
									AND aoU.option_code=''UPLINK'' 
							JOIN attribute_value av 
									ON av.attribute_code = aoL.option_value
									and av.number_value = c.card_id 
						WHERE r.rule_id = $1 
							and aoU.option_value is NULL 
					) as c '; 
 	

	/* запрос на извлечение детей первого уровня на основе cardlink-атрибута link_attr_code во входном профильном правиле
	 для cardlink-атрибута link_attr_code */
	SELECT_FIRST_CHILD_CARD_FOR_PROFILE_RULE CONSTANT VARCHAR DEFAULT 
		'			( 
						SELECT  
							c.card_id,  
							r.rule_id,  
							lv.number_value as link_card_id,      
							{0}, 
							pr.linked_status_id, 
							pr.role_code, 
							pr.profile_attr_code, 
							pr.target_attr_code, 
							pr.intermed_attr_code 
						FROM 	
							card c 
							JOIN access_rule r 
									ON (c.template_id=r.template_id) 
									AND (c.status_id=r.status_id) 
							JOIN profile_access_rule pr 
									ON r.rule_id=pr.rule_id 
							{1} 
							JOIN attribute_value lv 
									ON c.card_id=lv.card_id 
									AND pr.link_attr_code=lv.attribute_code 
						WHERE  
							r.rule_id=$1
					) as c ';

	/* список колонок, которые надо извлечь из предыдущих карточек, чтобы изспользовать полученный список в качестве первых родительских
	для backlink-атрибута intermed_attr_code */
	COLUMN_LIST_FIRST_PARENT_CARD_OF_PREV_CARD_FOR_PROFILE_RULE CONSTANT VARCHAR DEFAULT 
		'		c.card_id, 
				c.rule_id, 
				lv.card_id as link_card_id, 
				c.linked_status_id, 
				NULL as role_code, 
				c.profile_attr_code, 
				c.target_attr_code, 
				o.option_value as link_attr_code ';

	/* список колонок, которые надо извлечь из предыдущих карточек, чтобы изспользовать полученный список в качестве первых дочерних
	 для cardlink-атрибута intermed_attr_code */
	COLUMN_LIST_FIRST_CHILD_CARD_OF_PREV_CARD_FOR_PROFILE_RULE CONSTANT VARCHAR DEFAULT 
		'			c.card_id, 
					c.rule_id, 
					lv.number_value as link_card_id,
					c.linked_status_id, 
					NULL as role_code, 
					c.profile_attr_code, 
					c.target_attr_code, 
					c.intermed_attr_code as link_attr_code ';
		
	/* запрос на извлечение родителей первого уровня в профильных правилах для вычесленных ввиде дерева карточек  
	 для backlink-атрибута intermed_attr_code */
	SELECT_FIRST_PARENT_CARD_OF_PREV_CARD_FOR_PROFILE_RULE  CONSTANT VARCHAR DEFAULT 
		'	select 
				ct.card_id, 
				ct.rule_id, 
				lv.card_id as link_card_id, 
				ct.linked_status_id, 
				ct.role_code, 
				ct.profile_attr_code, 
				ct.target_attr_code, 
				ct.link_attr_code 
			from 
				card_tree ct 
				JOIN attribute_value lv 
						ON ct.link_card_id=lv.number_value 
						AND lv.attribute_code = ct.link_attr_code
						 ';

	/* запрос на извлечение детей первого уровня в профильных правилах для вычисленных в виде дерева карточек  
	 для cardlink-атрибута intermed_attr_code */
	SELECT_FIRST_CHILD_CARD_OF_PREV_CARD_FOR_PROFILE_RULE CONSTANT VARCHAR DEFAULT 
		'	select 
				ct.card_id, 
				ct.rule_id, 
				lv.number_value as link_card_id, 
				ct.linked_status_id, 
				ct.role_code, 
				ct.profile_attr_code, 
				ct.target_attr_code, 
				ct.link_attr_code 
			from 
				card_tree ct 
				JOIN attribute_value lv 
						ON ct.link_card_id=lv.card_id 
						AND ct.link_attr_code=lv.attribute_code 
						 ';

	/* подзапросы для вычисления первых родительских карточек для найденных ранее карточек, используется совместно с COLUMN_LIST_FIRST_PARENT_CARD_OF_PREV_CARD_FOR_PROFILE_RULE
	 для backlink-атрибута intermed_attr_code */
	JOIN_FIRST_PARENT_CARD_OF_PREV_CARD_FOR_PROFILE_RULE CONSTANT VARCHAR DEFAULT 
		'			JOIN attribute_option o 
							ON c.intermed_attr_code=o.attribute_code 
							AND o.option_code=''LINK'' 
					JOIN attribute_value lv 
							ON c.link_card_id=lv.number_value 
							AND o.option_value=lv.attribute_code
							 ';
		
	/* подзапросы для вычисления первых дочерних карточек для найденных ранее карточек, используется совместно с COLUMN_LIST_FIRST_CHILD_CARD_OF_PREV_CARD_FOR_PROFILE_RULE */
	JOIN_FIRST_CHILD_CARD_OF_PREV_CARD_FOR_PROFILE_RULE CONSTANT VARCHAR DEFAULT 
		'			JOIN attribute_value lv 
							ON c.link_card_id=lv.card_id 	
							AND c.intermed_attr_code=lv.attribute_code 
							 ';
		
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
		DELETE from access_list where rule_id=r_id RETURNING 1
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
	ELSIF (link_type = 'B')	THEN
		RAISE NOTICE 'Атрибут Связи - беклинк';
		IF (intermed_type = 'B') THEN
			RAISE NOTICE 'Атрибут Промежуточной Связи - бэклинк';
			sql_text := 
						'WITH b AS ( 
							WITH RECURSIVE ';
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
						', card_tree(card_id, rule_id, link_card_id, linked_status_id, role_code, profile_attr_code, target_attr_code, link_attr_code) AS (
								SELECT '||
									COLUMN_LIST_FIRST_PARENT_CARD_OF_PREV_CARD_FOR_PROFILE_RULE ||
						'		FROM ';
			sql_text := sql_text ||
								replace(replace(SELECT_UPPER_PARENT_CARD_FOR_PROFILE_RULE, '{0}', 'NULL'), '{1}', '');
			
			sql_text := sql_text || JOIN_FIRST_PARENT_CARD_OF_PREV_CARD_FOR_PROFILE_RULE;
			sql_text := sql_text || '   UNION ALL ';
			sql_text := sql_text || SELECT_FIRST_PARENT_CARD_OF_PREV_CARD_FOR_PROFILE_RULE;
			
			sql_text := sql_text ||
						') '||
						'	INSERT INTO access_list(card_id, rule_id, person_id, source_value_id) ';
						
			IF (linked_state_id IS NOT NULL) THEN
				sql_text := sql_text || 
						replace(SELECT_MAIN_ATTRIBUTE_FROM_CARD_TREE_FOR_PROFILE_RULES, '{0}', 'join card lc ON ct.link_card_id=lc.card_id 
																								and (ct.linked_status_id=lc.status_id) ');
			ELSE 
				sql_text := sql_text || 
						replace(SELECT_MAIN_ATTRIBUTE_FROM_CARD_TREE_FOR_PROFILE_RULES, '{0}', '');
			END IF;
			sql_text := sql_text||
					'RETURNING 1 '||
					') SELECT COUNT(*) FROM b';
						
		ELSIF (intermed_type = 'C' or intermed_type = 'E' or intermed_type = 'F') THEN
			RAISE NOTICE 'Атрибут Промежуточной Связи - кардлинк или типизированный кардлинк';
			sql_text := 
						'WITH b AS ( 
							WITH RECURSIVE ';
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
						', card_tree(card_id, rule_id, link_card_id, linked_status_id, role_code, profile_attr_code, target_attr_code, link_attr_code) AS (
								SELECT '||
									COLUMN_LIST_FIRST_CHILD_CARD_OF_PREV_CARD_FOR_PROFILE_RULE ||
						'		FROM ';
			sql_text := sql_text ||
									replace(replace(SELECT_UPPER_PARENT_CARD_FOR_PROFILE_RULE, '{0}', 'NULL'), '{1}', '');
			-- если статус свсязанных карточек задан, то тобавляем фильтрацию
			sql_text := sql_text || JOIN_FIRST_CHILD_CARD_OF_PREV_CARD_FOR_PROFILE_RULE;
			sql_text := sql_text || '	UNION ALL ';
			sql_text := sql_text || SELECT_FIRST_CHILD_CARD_OF_PREV_CARD_FOR_PROFILE_RULE;
			
			sql_text := sql_text ||
						') '||
						'	INSERT INTO access_list(card_id, rule_id, person_id, source_value_id) ';

			IF (linked_state_id IS NOT NULL) THEN
				sql_text := sql_text || 
						replace(SELECT_MAIN_ATTRIBUTE_FROM_CARD_TREE_FOR_PROFILE_RULES, '{0}', 'join card lc ON ct.link_card_id=lc.card_id 
																								and (ct.linked_status_id=lc.status_id) ');
			ELSE 
				sql_text := sql_text || 
						replace(SELECT_MAIN_ATTRIBUTE_FROM_CARD_TREE_FOR_PROFILE_RULES, '{0}', '');
			END IF;
			
			sql_text := sql_text ||
					'RETURNING 1 
					) SELECT COUNT(*) FROM b';
		ELSE 
			RAISE NOTICE 'Атрибут Промежуточной Связи отсутствует';
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
			sql_text := sql_text||
							'INSERT INTO access_list(card_id, rule_id, person_id, source_value_id) 
								select 
									c.card_id,  
									c.rule_id, 
									pv.person_id, 
									tv.attr_value_id 
								FROM '||
									replace(replace(SELECT_UPPER_PARENT_CARD_FOR_PROFILE_RULE, '{0}', 'NULL'), '{1}', '');
			
			sql_text := sql_text ||
			'			JOIN card lc '|| 
						'					ON lc.card_id = c.link_card_id ';
			-- если статус свсязанных карточек задан, то тобавляем фильтрацию
			IF not(linked_state_id is NULL) THEN
				sql_text := sql_text ||
						'					AND (c.linked_status_id=lc.status_id) ';
			END IF;
			sql_text := sql_text ||
						'			JOIN attribute_value tv 
											ON lc.card_id=tv.card_id 
											AND c.target_attr_code=tv.attribute_code 
									JOIN person_all_profiles pv 
											ON tv.number_value=pv.number_value 
											AND c.profile_attr_code=pv.attribute_code ';
			sql_text := sql_text||
					'RETURNING 1 '||
					') SELECT COUNT(*) FROM b';
		END IF;
	ELSIF (link_type = 'C' or link_type = 'E' or link_type = 'F')	THEN
		RAISE NOTICE 'Атрибут Связи - кардлинк или типизированный кардлинк';
		IF (intermed_type = 'B') THEN
			RAISE NOTICE 'Атрибут Промежуточной Связи - бэклинк';
			sql_text := 
						'WITH b AS ( 
							WITH RECURSIVE ';
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
						', card_tree(card_id, rule_id, link_card_id, linked_status_id, role_code, profile_attr_code, target_attr_code, link_attr_code) AS (
							SELECT '||
								COLUMN_LIST_FIRST_PARENT_CARD_OF_PREV_CARD_FOR_PROFILE_RULE||
						'	FROM ';
			sql_text := sql_text ||
									replace(replace(SELECT_FIRST_CHILD_CARD_FOR_PROFILE_RULE, '{0}', 'NULL'), '{1}', '');
			sql_text := sql_text || JOIN_FIRST_PARENT_CARD_OF_PREV_CARD_FOR_PROFILE_RULE;
			sql_text := sql_text || '	UNION ALL ';
			sql_text := sql_text || SELECT_FIRST_PARENT_CARD_OF_PREV_CARD_FOR_PROFILE_RULE;
																	
			sql_text := sql_text ||
						') 
							INSERT INTO access_list(card_id, rule_id, person_id, source_value_id) ';
			IF (linked_state_id IS NOT NULL) THEN
				sql_text := sql_text || 
						replace(SELECT_MAIN_ATTRIBUTE_FROM_CARD_TREE_FOR_PROFILE_RULES, '{0}', 'join card lc ON ct.link_card_id=lc.card_id 
																								and (ct.linked_status_id=lc.status_id) ');
			ELSE 
				sql_text := sql_text || 
						replace(SELECT_MAIN_ATTRIBUTE_FROM_CARD_TREE_FOR_PROFILE_RULES, '{0}', '');
			END IF;
			
			sql_text := sql_text ||
					'RETURNING 1 
					) SELECT COUNT(*) FROM b';
		ELSIF (intermed_type = 'C' or intermed_type = 'E' or intermed_type = 'F') THEN
			RAISE NOTICE 'Атрибут Промежуточной Связи - кардлинк или типизированный кардлинк';
			sql_text := 
						'WITH b AS ( 
							WITH RECURSIVE ';
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
						', card_tree(card_id, rule_id, link_card_id, linked_status_id, role_code, profile_attr_code, target_attr_code, link_attr_code) AS (
							SELECT '||
								COLUMN_LIST_FIRST_CHILD_CARD_OF_PREV_CARD_FOR_PROFILE_RULE||
						'	FROM ';
			sql_text := sql_text ||
									replace(replace(SELECT_FIRST_CHILD_CARD_FOR_PROFILE_RULE, '{0}', 'NULL'), '{1}', '');
			sql_text := sql_text || JOIN_FIRST_CHILD_CARD_OF_PREV_CARD_FOR_PROFILE_RULE;
			sql_text := sql_text || '	UNION ALL ';
			sql_text := sql_text || SELECT_FIRST_CHILD_CARD_OF_PREV_CARD_FOR_PROFILE_RULE;
			
			sql_text := sql_text ||
						') 
							INSERT INTO access_list(card_id, rule_id, person_id, source_value_id) ';
							
			IF (linked_state_id IS NOT NULL) THEN
				sql_text := sql_text || 
						replace(SELECT_MAIN_ATTRIBUTE_FROM_CARD_TREE_FOR_PROFILE_RULES, '{0}', 'join card lc ON ct.link_card_id=lc.card_id 
																								and (ct.linked_status_id=lc.status_id) ');
			ELSE 
				sql_text := sql_text || 
						replace(SELECT_MAIN_ATTRIBUTE_FROM_CARD_TREE_FOR_PROFILE_RULES, '{0}', '');
			END IF;
			
			sql_text := sql_text||
					'RETURNING 1 '||
					') SELECT COUNT(*) FROM b';
		ELSE 
			RAISE NOTICE 'Атрибут Промежуточной Связи отсутствует';
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

			sql_text :=  sql_text||
						'	INSERT INTO access_list(card_id, rule_id, person_id, source_value_id) 
							SELECT 
								c.card_id, 
								c.rule_id, 
								pv.person_id, 
								tv.attr_value_id 
							FROM ';
			sql_text := sql_text ||
									replace(replace(SELECT_FIRST_CHILD_CARD_FOR_PROFILE_RULE, '{0}', 'NULL'), '{1}', '');
			sql_text := sql_text ||
						'		JOIN card lc '||
						'				ON c.link_card_id=lc.card_id ';
			-- если статус свсязанных карточек задан, то тобавляем фильтрацию
			IF not(linked_state_id is NULL) THEN
				sql_text := sql_text ||
						'					AND (c.linked_status_id=lc.status_id) ';
			END IF;
			sql_text := sql_text ||
						'		JOIN attribute_value tv 
										ON lc.card_id=tv.card_id 
										AND c.target_attr_code=tv.attribute_code 
								JOIN person_all_profiles pv 
										ON tv.number_value=pv.number_value 
										AND c.profile_attr_code=pv.attribute_code ';
			sql_text := sql_text||
					'RETURNING 1 
					) SELECT COUNT(*) FROM b';

		END IF;
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

CREATE OR REPLACE FUNCTION dbm_recalc_person_rules(recalc_type int) RETURNS BIGINT AS
$body$
DECLARE
	ALL_COUNT BIGINT;
	rec RECORD;
	rule_count INT;
	i INT;
BEGIN
	ALL_COUNT := 0;
	RAISE NOTICE 'НАЧИНАЕМ ОЧЕРЕДНОЙ ПЕРЕСЧЁТ МАТРИЦЫ ПРАВ!';
--	RAISE NOTICE 'Откиньтесь на спинку кресла и налейте себе второй бокал коньяка, путешествие в мир прав и беспредела продолжается...';
	-- пересчитывать будем права на правила, сначала на персональные, потом на профильные
	RAISE NOTICE '-----------------Персональные правила-------------------';
	-- если надо пересчитать права для правил c АПС
	if ((recalc_type is NULL) or (recalc_type=1)) THEN
		RAISE NOTICE '-----------------Персональные правила с АС и АПС-------------------';
		SELECT count(*) INTO rule_count from person_access_rule pr where not(pr.intermed_attr_code is null) and exists(select 1 from card c join access_rule ar on ar.status_id = c.status_id and ar.template_id = c.template_id where ar.rule_id = pr.rule_id);
		RAISE NOTICE 'Всего персональных правил с непустым атрибутом промежуточной связи, для которых есть карточки - %', rule_count;
		i := 0;
		FOR /*r_id, link_type, intermed_type, linked_state_id, r_code*/ rec IN SELECT pr.rule_id as r_id, al.data_type as link_type, ai.data_type as intermed_type, pr.linked_status_id as linked_state_id, pr.role_code as r_code from person_access_rule pr join attribute al on al.attribute_code = pr.link_attr_code join attribute ai on ai.attribute_code = pr.intermed_attr_code where not(pr.intermed_attr_code is null) and exists(select 1 from card c join access_rule ar on ar.status_id = c.status_id and ar.template_id = c.template_id where ar.rule_id = pr.rule_id) order by 1 asc
		loop
			i := i+1;
			RAISE NOTICE '';
			RAISE NOTICE '----------Правило %/%----------', i, rule_count;
			SELECT recalculate_access_list_for_person_rule(rec.r_id, rec.link_type, rec.intermed_type, rec.linked_state_id, rec.r_code)+ALL_COUNT INTO ALL_COUNT;
			RAISE NOTICE '----------Правило %/%----------', i, rule_count;
		end loop;
		RAISE NOTICE '-----------------Персональные правила с АС и АПС-------------------';
	END IF;
	-- если надо пересчитать права для правил без АПС
	if ((recalc_type is NULL) or (recalc_type=2)) THEN
		RAISE NOTICE '-----------------Персональные правила с АС-------------------';
		SELECT count(*) INTO rule_count from person_access_rule pr where not(pr.link_attr_code is null) and (pr.intermed_attr_code is null) and exists(select 1 from card c join access_rule ar on ar.status_id = c.status_id and ar.template_id = c.template_id where ar.rule_id = pr.rule_id);
		RAISE NOTICE 'Всего персональных правил с непустым атрибутом связи, для которых есть карточки - %', rule_count;
		i := 0;
		FOR /*r_id, link_type, intermed_type, linked_state_id, r_code*/ rec IN SELECT pr.rule_id as r_id, al.data_type as link_type, NULL as intermed_type, pr.linked_status_id as linked_state_id, pr.role_code as r_code from person_access_rule pr join attribute al on al.attribute_code = pr.link_attr_code where (pr.intermed_attr_code is null) and not(pr.link_attr_code is null) and exists(select 1 from card c join access_rule ar on ar.status_id = c.status_id and ar.template_id = c.template_id where ar.rule_id = pr.rule_id) order by 1 asc
		loop
			i := i+1;
			RAISE NOTICE '';
			RAISE NOTICE '----------Правило %/%----------', i, rule_count;
			SELECT recalculate_access_list_for_person_rule(rec.r_id, rec.link_type, NULL, rec.linked_state_id, rec.r_code)+ALL_COUNT INTO ALL_COUNT;
			RAISE NOTICE '----------Правило %/%----------', i, rule_count;
		end loop;
		RAISE NOTICE '-----------------Персональные правила с АС-------------------';
	END IF;
	-- если надо пересчитать права для правил без АС и АПС
	if ((recalc_type is NULL) or (recalc_type=3)) THEN
		RAISE NOTICE '-----------------Персональные правила без АС-------------------';
		SELECT count(*) INTO rule_count from person_access_rule pr where (pr.link_attr_code is null) and (pr.intermed_attr_code is null) and exists(select 1 from card c join access_rule ar on ar.status_id = c.status_id and ar.template_id = c.template_id where ar.rule_id = pr.rule_id);
		RAISE NOTICE 'Всего персональных правил с пустыми атрибутами связи, для которых есть карточки - %', rule_count;
		i := 0;
		FOR /*r_id, link_type, intermed_type, linked_state_id, r_code*/ rec IN SELECT pr.rule_id as r_id, NULL as link_type, NULL as intermed_type, pr.linked_status_id as linked_state_id, pr.role_code as r_code from person_access_rule pr where (pr.intermed_attr_code is null) and (pr.link_attr_code is null) and exists(select 1 from card c join access_rule ar on ar.status_id = c.status_id and ar.template_id = c.template_id where ar.rule_id = pr.rule_id) order by 1 asc
		loop
			i := i+1;
			RAISE NOTICE '';
			RAISE NOTICE '----------Правило %/%----------', i, rule_count;
			SELECT recalculate_access_list_for_person_rule(rec.r_id, NULL, NULL, rec.linked_state_id, rec.r_code)+ALL_COUNT INTO ALL_COUNT;
			RAISE NOTICE '----------Правило %/%----------', i, rule_count;
		end loop;
		RAISE NOTICE '-----------------Персональные правила без АС-------------------';
	END IF;

	RAISE NOTICE '-----------------Персональные правила-------------------';

	RAISE NOTICE 'ПЕРЕСЧЁТ ПРАВ ЗАВЕРШЕН, ДОБАВЛЕНО ВСЕГО ''%'' ЗАПИСЕЙ!', ALL_COUNT;
	RETURN ALL_COUNT;
END;
$body$ LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION dbm_recalc_profile_rules(recalc_type int) RETURNS BIGINT AS
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
	-- если надо пересчитать права для правил c АПС
	if ((recalc_type is NULL) or (recalc_type=1)) THEN
		RAISE NOTICE '-----------------Профильные правила с АС и АПС-------------------';
		SELECT count(*) INTO rule_count from profile_access_rule pr where not(pr.intermed_attr_code is null) and exists(select 1 from card c join access_rule ar on ar.status_id = c.status_id and ar.template_id = c.template_id where ar.rule_id = pr.rule_id);
		RAISE NOTICE 'Всего профильных правил с непустым атрибутом промежуточной связи, для которых есть карточки - %', rule_count;
		i := 0;
		FOR /*r_id, link_type, intermed_type, linked_state_id, r_code*/ rec IN SELECT pr.rule_id as r_id, al.data_type as link_type, ai.data_type as intermed_type, pr.linked_status_id as linked_state_id, pr.role_code as r_code from profile_access_rule pr join attribute al on al.attribute_code = pr.link_attr_code join attribute ai on ai.attribute_code = pr.intermed_attr_code where not(pr.intermed_attr_code is null) and exists(select 1 from card c join access_rule ar on ar.status_id = c.status_id and ar.template_id = c.template_id where ar.rule_id = pr.rule_id) order by 1 asc
		loop
			i := i+1;
			RAISE NOTICE '';
			RAISE NOTICE '----------Правило %/%----------', i, rule_count;
			SELECT recalculate_access_list_for_profile_rule(rec.r_id, rec.link_type, rec.intermed_type, rec.linked_state_id, rec.r_code)+ALL_COUNT INTO ALL_COUNT;
			RAISE NOTICE '----------Правило %/%----------', i, rule_count;
		end loop;
		RAISE NOTICE '-----------------Профильные правила с АС и АПС-------------------';
	END IF;
	-- если надо пересчитать права для правил без АПС
	if ((recalc_type is NULL) or (recalc_type=2)) THEN
		RAISE NOTICE '-----------------Профильные правила с АС-------------------';
		SELECT count(*) INTO rule_count from profile_access_rule pr where not(pr.link_attr_code is null) and (pr.intermed_attr_code is null) and exists(select 1 from card c join access_rule ar on ar.status_id = c.status_id and ar.template_id = c.template_id where ar.rule_id = pr.rule_id);
		RAISE NOTICE 'Всего профильных правил с непустым атрибутом связи, для которых есть карточки - %', rule_count;
		i := 0;
		FOR /*r_id, link_type, intermed_type, linked_state_id, r_code*/ rec IN SELECT pr.rule_id as r_id, al.data_type as link_type, NULL as intermed_type, pr.linked_status_id as linked_state_id, pr.role_code as r_code from profile_access_rule pr join attribute al on al.attribute_code = pr.link_attr_code where (pr.intermed_attr_code is null) and not(pr.link_attr_code is null) and exists(select 1 from card c join access_rule ar on ar.status_id = c.status_id and ar.template_id = c.template_id where ar.rule_id = pr.rule_id) order by 1 asc
		loop
			i := i+1;
			RAISE NOTICE '';
			RAISE NOTICE '----------Правило %/%----------', i, rule_count;
			SELECT recalculate_access_list_for_profile_rule(rec.r_id, rec.link_type, NULL, rec.linked_state_id, rec.r_code)+ALL_COUNT INTO ALL_COUNT;
			RAISE NOTICE '----------Правило %/%----------', i, rule_count;
		end loop;
		RAISE NOTICE '-----------------Профильные правила с АС-------------------';
	end if;
	-- если надо пересчитать права для правил без АС
	if ((recalc_type is NULL) or (recalc_type=3)) THEN
		RAISE NOTICE '-----------------Профильные правила без АС-------------------';
		SELECT count(*) INTO rule_count from profile_access_rule pr where (pr.link_attr_code is null) and (pr.intermed_attr_code is null) and exists(select 1 from card c join access_rule ar on ar.status_id = c.status_id and ar.template_id = c.template_id where ar.rule_id = pr.rule_id);
		RAISE NOTICE 'Всего профильных правил с пустыми атрибутами связи, для которых есть карточки - %', rule_count;
		i := 0;
		FOR /*r_id, link_type, intermed_type, linked_state_id, r_code*/ rec IN SELECT pr.rule_id as r_id, NULL as link_type, NULL as intermed_type, pr.linked_status_id as linked_state_id, pr.role_code as r_code from profile_access_rule pr where (pr.intermed_attr_code is null) and (pr.link_attr_code is null) and exists(select 1 from card c join access_rule ar on ar.status_id = c.status_id and ar.template_id = c.template_id where ar.rule_id = pr.rule_id) order by 1 asc
		loop
			i := i+1;
			RAISE NOTICE '';
			RAISE NOTICE '----------Правило %/%----------', i, rule_count;
			SELECT recalculate_access_list_for_profile_rule(rec.r_id, NULL, NULL, rec.linked_state_id, rec.r_code)+ALL_COUNT INTO ALL_COUNT;
			RAISE NOTICE '----------Правило %/%----------', i, rule_count;
		end loop;
		RAISE NOTICE '-----------------Профильные правила без АС-------------------';
	END IF;
	
	RAISE NOTICE '-----------------Профильные правила-------------------';
	RAISE NOTICE 'ПЕРЕСЧЁТ ПРАВ ЗАВЕРШЕН, ДОБАВЛЕНО ВСЕГО ''%'' ЗАПИСЕЙ!', ALL_COUNT;
	RETURN ALL_COUNT;
END;
$body$ LANGUAGE 'plpgsql';

CREATE OR REPLACE FUNCTION update_seq_rule_id() RETURNS INT AS
$body$
DECLARE
	CURRENT_SEQ_RULE_ID BIGINT;
	MAX_RULE_ID BIGINT;
	UPDATE_QUERY VARCHAR;
BEGIN
	SELECT max(rule_id) INTO MAX_RULE_ID from access_rule;
	RAISE NOTICE 'Максимальный rule_id = ''%''', MAX_RULE_ID;
		
	SELECT nextval('seq_rule_id') INTO CURRENT_SEQ_RULE_ID;
	RAISE NOTICE 'Текущий seq_rule_id = ''%''', CURRENT_SEQ_RULE_ID;
	
	if (CURRENT_SEQ_RULE_ID>=MAX_RULE_ID) THEN
		RAISE NOTICE 'Замена не нужна, выходим';
		RETURN 0;
	ELSE
		RAISE NOTICE 'Необходимо обновить seq_rule_id';
		CURRENT_SEQ_RULE_ID = MAX_RULE_ID+1;
		-- Sequence: seq_rule_id
		UPDATE_QUERY = 
		'DROP SEQUENCE seq_rule_id;

		CREATE SEQUENCE seq_rule_id
		  INCREMENT 1
		  MINVALUE 1
		  MAXVALUE 9223372036854775807
		  START '||CURRENT_SEQ_RULE_ID||
		'  CACHE 20;
		ALTER TABLE seq_rule_id
		  OWNER TO br4j_admin;
		GRANT ALL ON SEQUENCE seq_rule_id TO br4j_admin;
		GRANT ALL ON SEQUENCE seq_rule_id TO dbmi;
		GRANT SELECT ON SEQUENCE seq_rule_id TO portal;';
		RAISE NOTICE 'Запрос:
		%', UPDATE_QUERY;
		EXECUTE UPDATE_QUERY;
		RETURN 1;
	END IF;

END;
$body$ LANGUAGE 'plpgsql';

GRANT EXECUTE ON FUNCTION recalculate_access_list_for_person_rule(r_id NUMERIC, link_type VARCHAR, intermed_type VARCHAR, linked_state_id numeric, r_code VARCHAR) TO "br4j_admin";
GRANT EXECUTE ON FUNCTION recalculate_access_list_for_profile_rule(r_id NUMERIC, link_type VARCHAR, intermed_type VARCHAR, linked_state_id numeric, r_code VARCHAR) TO "br4j_admin";
GRANT EXECUTE ON FUNCTION dbm_recalc_person_rules(recalc_type int) TO "br4j_admin";
GRANT EXECUTE ON FUNCTION dbm_recalc_profile_rules(recalc_type int) TO "br4j_admin";
GRANT EXECUTE ON FUNCTION update_seq_rule_id() TO "br4j_admin";

BEGIN TRANSACTION;
	SELECT dbm_recalc_person_rules(3);
END TRANSACTION;
BEGIN TRANSACTION;
	SELECT dbm_recalc_person_rules(2);
END TRANSACTION;
BEGIN TRANSACTION;
	SELECT dbm_recalc_person_rules(1);
END TRANSACTION;
BEGIN TRANSACTION;
	SELECT dbm_recalc_profile_rules( 3);
END TRANSACTION;
BEGIN TRANSACTION;
	SELECT dbm_recalc_profile_rules(2);
END TRANSACTION;
BEGIN TRANSACTION;
	SELECT dbm_recalc_profile_rules(1);
END TRANSACTION;
-- обновляем seq_rule_id после полного пересчёта (в идеале надо это делать после полной заливки в утилите)
BEGIN TRANSACTION;
	select update_seq_rule_id();
END TRANSACTION;

DROP FUNCTION dbm_recalc_person_rules(recalc_type int);
DROP FUNCTION dbm_recalc_profile_rules(recalc_type int);
DROP FUNCTION update_seq_rule_id();