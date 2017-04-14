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

-- Изменение статуса
INSERT INTO card_access(
	rec_id,
	permission_type,		object_id,		role_code,	
	person_attribute_code,	template_id
) 
	SELECT 
			nextval('seq_card_access_rec_id'), -- rec_id
			1, 					-- permission_type: 1=WFMOV/workflow move
			mov.wfm_id,			-- object_id: для 1=WFMOV это workflow move id
			role.role_code,		-- role_code: стат роль
			null,				-- Динамическая роль - код атрибута: не исп
			mov.template_id		-- шаблон 
	FROM	(	SELECT 	wfm.wfm_id as "wfm_id"
    					, t.template_id as "template_id"
				FROM workflow_move wfm
                		JOIN template t on t.workflow_id=wfm.workflow_id
				WHERE wfm.wfm_id in (9000000, 9000001, 9000002, 9000003, 9000004, 9000005, 9000006)   -- object_id (workflow move id)
				AND t.template_id = 324                                                               -- template_id
			) AS "mov",
			( 	SELECT sysrole.role_code as "role_code"
				FROM system_role sysrole
				WHERE sysrole.role_code in ('A', '_SYSTEM_')                                          -- role_code
			) AS "role"
	WHERE NOT EXISTS (
			SELECT 1 FROM card_access ca 
			WHERE ca.object_id=mov.wfm_id
					AND ca.permission_type = 1
					AND ca.role_code = role.role_code
					AND ca.template_id = mov.template_id
		)
;

-- Чтение-запись
INSERT INTO card_access(
	rec_id,
	permission_type,		object_id,		role_code,	
	person_attribute_code,	template_id
) 
	SELECT 
			nextval('seq_card_access_rec_id'), -- rec_id
			perm.access,    	-- permission_type: 2=READ, 3=WRITE
			status.id,			-- object_id: для READ/WRITE это статус
			role.role_code,		-- Cтат роль: role_code
			null,				-- Динамическая роль = код атрибута: не исп
			templ.id			-- шаблон 
	FROM	(SELECT 2 UNION SELECT 3) AS "perm"("access"),

			( 	SELECT sysrole.role_code 
				FROM system_role sysrole
				WHERE sysrole.role_code in ('A', '_SYSTEM_')
			) as "role" ("role_code"),

            (SELECT cs.status_id FROM card_status cs WHERE cs.status_id in (10001000)
            ) as "status"("id"),

            (SELECT t.template_id FROM template t WHERE t.template_id in (324)
            ) as "templ"("id")
            
	WHERE NOT EXISTS (
			SELECT 1 FROM card_access ca 
			WHERE 		ca.object_id=status.id
					AND ca.permission_type = perm.access
					AND ca.role_code = role.role_code
					AND ca.template_id = templ.id
                    AND ca.person_attribute_code is NULL
		)
;

-- Создание
INSERT INTO card_access(
	rec_id,
	permission_type,		object_id,		role_code,	
	person_attribute_code,	template_id
) 
	SELECT 
			nextval('seq_card_access_rec_id'), -- rec_id
			4,    	 			-- permission_type: 4=CREATE
			null,			    -- object_id
			role.role_code,		-- Cтат роль: role_code
			null,				-- Динамическая роль = код атрибута: не исп
			templ.id			-- шаблон 
	FROM	( 	SELECT sysrole.role_code 
				FROM system_role sysrole
				WHERE sysrole.role_code in ('A', '_SYSTEM_')                            -- role_code
			) AS "role" ("role_code"),
            (SELECT t.template_id FROM template t WHERE t.template_id IN (324)          -- template_id
            ) AS "templ"("id")            
	WHERE NOT EXISTS (
			SELECT 1 FROM card_access ca 
			WHERE 		ca.permission_type = 4
					AND ca.role_code = role.role_code
					AND ca.template_id = templ.id
                    AND ca.person_attribute_code IS NULL
		)
;