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

﻿CREATE OR REPLACE FUNCTION dbm_install_script(prevision bigint, pscript text, ptag text) RETURNS pg_catalog.void AS $body$
DECLARE
script_body TEXT;
BEGIN
 
 --Выполнение скрипта
 
 IF (ptag is null) AND (prevision is not NULL) AND (pscript is not NULL) THEN	
	script_body = substring(pscript from '#"%#"@UNDO%' for '#');
	IF NOT EXISTS (SELECT revision FROM dbm_changelog WHERE revision=prevision) OR (SELECT COUNT(*) FROM dbm_changelog WHERE revision=prevision) % 2 = 0 THEN
		EXECUTE script_body;
		INSERT INTO dbm_changelog (revision, date_time, "user", "action", script) values (prevision, NOW(), USER, 'do', script_body);
		RAISE NOTICE 'Executed scripts from commit ''%''', prevision;
	ELSE
		RAISE NOTICE 'Scripts from commit ''%'' already installed', prevision;
	END IF;
 ELSE 	
	IF (prevision is NULL) AND (ptag is not NULL) AND (trim(ptag) <> '') THEN		
		IF NOT EXISTS (SELECT build FROM dbm_changelog WHERE build=ptag) THEN
			IF pscript is not NULL THEN
				EXECUTE pscript; 
				RAISE NOTICE 'Executed ''%'' script', ptag;
			END IF;
			INSERT INTO dbm_changelog (build, date_time, "user", "action", script) values (ptag, NOW(), USER, 'do', pscript);
		ELSE
			RAISE NOTICE 'Script ''%'' already installed', ptag;
		END IF;
	ELSE
		RAISE EXCEPTION 'ERROR. INPUT PARAMETERS WRONG';
	END IF;
 END IF;

END;
$body$
LANGUAGE 'plpgsql';


CREATE OR REPLACE FUNCTION dbm_install_script (prevision bigint, pscript text) RETURNS pg_catalog.void AS
$b$
BEGIN

 PERFORM dbm_install_script(prevision, pscript, null);  
 
END;
$b$
LANGUAGE 'plpgsql';


CREATE OR REPLACE FUNCTION dbm_install_script (ptag text, pscript text) RETURNS pg_catalog.void AS
$body$
BEGIN
 
 PERFORM dbm_install_script(null, pscript, ptag);
 
END;
$body$
LANGUAGE 'plpgsql';


CREATE OR REPLACE FUNCTION dbm_checking(bi bigint) RETURNS pg_catalog.void AS $body$

 DECLARE I text; CUR text; -- Variable names begin with a @

 BEGIN

 --Проверка наличия журнала инсталяции скриптов
 IF NOT EXISTS (SELECT relname FROM pg_class WHERE relname='dbm_changelog') THEN
 --Если журнала нет то создаем его
 CREATE TABLE dbmi_trunk.dbm_changelog (
	id SERIAL,
	revision BIGINT NULL,
	date_time TIMESTAMP NOT NULL,
	"user" VARCHAR(100) NOT NULL,
	"action" VARCHAR(4) NULL,
	script TEXT NULL,
	build TEXT NULL,
	CONSTRAINT id_primary PRIMARY KEY(id)
	);
 INSERT INTO dbmi_trunk.dbm_changelog (build, date_time, "user", "action", script) values ('0', NOW(), USER, 'do', null);
 END IF; 

 select build into I from dbm_changelog where id in (select max(id) from dbm_changelog  where build is not null and script is null);
 RAISE NOTICE 'Последняя сборка записанная в таблицу changelog ''%'' ', I;
 select substring(I from '(\d+).*') into I;
 IF (I is not null AND CAST (I AS bigint) >= 0) THEN 	RAISE NOTICE 'Таблица changelog ранее заполнялась';
 ELSE
	RAISE NOTICE 'Таблица changelog пуста либо заполнена неверно!';
	RAISE EXCEPTION 'Номер предыдущей сборки является null-значением.';
 END IF;

 select build into CUR from dbm_changelog where id in (select max(id) from dbm_changelog where build is not null and script is null);
 select substring(CUR from '(\d+).*') into CUR;
 IF ((CAST (CUR AS bigint) >= bi) OR (CAST (CUR AS bigint) = 0)) THEN	RAISE NOTICE 'Номер предыдущей сборки по базе: ''%'' ', CUR;
	RAISE NOTICE 'Начало текущей сборки: ''%'' ', bi;
	RAISE NOTICE 'Начинаем накат скриптов на базу...';
 ELSE
	RAISE EXCEPTION 'Номер предыдущей сборки по данным базы ''%'' не совпадает с номером начала текущей ''%''', CUR, bi;
 END IF;

 END;
 $body$ LANGUAGE 'plpgsql';

SELECT dbm_checking(_svn.begin_);

DROP FUNCTION dbm_checking(bi bigint);

GRANT ALL ON "dbmi_trunk"."dbm_changelog" TO "dbmi";
GRANT ALL ON "dbmi_trunk"."dbm_changelog" TO "portal";
GRANT ALL ON "dbmi_trunk"."dbm_changelog" TO "br4j_admin";

GRANT ALL ON "dbmi_trunk"."dbm_changelog_id_seq" TO "dbmi";
GRANT ALL ON "dbmi_trunk"."dbm_changelog_id_seq" TO "portal";
GRANT ALL ON "dbmi_trunk"."dbm_changelog_id_seq" TO "br4j_admin";

GRANT EXECUTE ON FUNCTION "dbmi_trunk"."dbm_install_script"(ptag text, pscript text) TO "dbmi";
GRANT EXECUTE ON FUNCTION "dbmi_trunk"."dbm_install_script"(prevision bigint, pscript text) TO "dbmi";
GRANT EXECUTE ON FUNCTION "dbmi_trunk"."dbm_install_script"(prevision bigint, pscript text, ptag text) TO "dbmi";

GRANT EXECUTE ON FUNCTION "dbmi_trunk"."dbm_install_script"(ptag text, pscript text) TO "portal";
GRANT EXECUTE ON FUNCTION "dbmi_trunk"."dbm_install_script"(prevision bigint, pscript text) TO "portal";
GRANT EXECUTE ON FUNCTION "dbmi_trunk"."dbm_install_script"(prevision bigint, pscript text, ptag text) TO "portal";

GRANT EXECUTE ON FUNCTION "dbmi_trunk"."dbm_install_script"(ptag text, pscript text) TO "br4j_admin";
GRANT EXECUTE ON FUNCTION "dbmi_trunk"."dbm_install_script"(prevision bigint, pscript text) TO "br4j_admin";
GRANT EXECUTE ON FUNCTION "dbmi_trunk"."dbm_install_script"(prevision bigint, pscript text, ptag text) TO "br4j_admin";

BEGIN TRANSACTION;
