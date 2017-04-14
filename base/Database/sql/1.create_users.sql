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

--**********************************************************************************************************--
--**********************************************************************************************************--
-------------------------------------- ЧАСТЬ 1. НАЧАЛО. ------------------------------------------------------ 
--**********************************************************************************************************--
--**********************************************************************************************************--

\set default_br4j_account_name 'dbmi'
\set default_portal_account_name 'portal'
\set default_dbadmin_account_name 'br4j_admin'
\set default_db_name 'br4j'

\set custom_br4j_account_name 
\set custom_portal_account_name 
\set custom_dbadmin_account_name 
\set custom_db_name 

\set ON_ERROR_STOP 0

-------------------------------------------------------------------------------------------------------------
-------------------------------- ИНТЕРАКТИВНЫЙ БЛОК. НАЧАЛО. -------------------------------------------------
---------Расскоментируйте строки в этом блоке чтобы включить интерактивный ввод параметров--------------------
--------------------------------------------------------------------------------------------------------------
/*******
\echo 'Необходимо ввести несколько параметров. Если Вас устраивают значения по умолчанию, указанные в скобках [], то просто нажмине ENTER.'
\prompt 'Для продолжения нажмине ENTER.' temp_var
\prompt 'Имя создаваемой БД приложения BR4J [':default_db_name']: ' custom_db_name
\prompt 'Имя аккаунта для приложения BR4J [':default_br4j_account_name']: ' custom_br4j_account_name
\prompt 'Имя аккаунта для портальной приложения BR4J [':default_portal_account_name']: ' custom_portal_account_name
\prompt 'Имя аккаунта для администратора БД приложения BR4J [':default_dbadmin_account_name']: ' custom_dbadmin_account_name
*******/
---------------------------------------------------------------------------------------------------------------
-------------------------------- ИНТЕРАКТИВНЫЙ БЛОК. КОНЕЦ. ---------------------------------------------------
---------------------------------------------------------------------------------------------------------------



CREATE OR REPLACE FUNCTION db_check(db_name varchar) RETURNS INTEGER AS $$
DECLARE 
   cont INTEGER;
BEGIN
    SELECT COUNT(1) FROM pg_database WHERE datname=db_name INTO cont;
    IF (cont > 0) THEN 
        RAISE EXCEPTION 'БД с именем % уже существует. Продолжение работы невозможно.', db_name
        USING ERRCODE = 'BR4J1';
    END IF;
    RETURN 0;
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION str_coalesce(custom_value varchar, default_value varchar) RETURNS varchar AS $$
DECLARE 
   actual_value varchar;
BEGIN
    custom_value := trim(custom_value);
--    custom_acc_name := trim('\\t' from custom_acc_name);
    IF (custom_value = '') THEN
       actual_value := default_value;
    ELSE
       actual_value := custom_value;
    END IF;
    RETURN actual_value;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION str_coalesce(cli_value varchar, custom_value varchar, default_value varchar) RETURNS varchar AS $$
DECLARE 
   temp_value varchar;
   result_value varchar;
BEGIN
	cli_value := trim(cli_value);
  custom_value := trim(custom_value);
    IF (cli_value = '') THEN
       temp_value := custom_value;
    ELSE
       temp_value := cli_value;
    END IF;
	IF (temp_value = '') THEN
       temp_value := default_value;
    END IF;
	result_value := temp_value;
  RETURN result_value;
END;
$$ LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION create_account(acc_name varchar, search_path varchar, superuser_flag boolean) RETURNS INTEGER AS $$
DECLARE 
   cont INTEGER;
   superuser_mark varchar;
BEGIN
    IF (superuser_flag) THEN
       superuser_mark := 'SUPERUSER';
    ELSE
       superuser_mark := 'NOSUPERUSER';
    END IF;

    SELECT COUNT(1) FROM pg_roles WHERE rolname=acc_name INTO cont; 
    IF (cont > 0) THEN 
       RAISE INFO 'Аккаунт % уже существует', acc_name;
    ELSE 
		EXECUTE 'CREATE ROLE '||acc_name||' LOGIN
		UNENCRYPTED PASSWORD '''||acc_name||''' '
		||superuser_mark||' INHERIT CREATEDB CREATEROLE NOREPLICATION;';
		EXECUTE 'ALTER ROLE '||acc_name||' SET search_path = '||search_path||';';
       RAISE INFO 'Аккаунт % создан с паролем [%]', acc_name, acc_name;
    END IF;

    RETURN 0;
END;
$$ LANGUAGE plpgsql;


SELECT str_coalesce (:'cli_db_name', :'custom_db_name', :'default_db_name') AS db_name;
\gset 
SELECT str_coalesce (:'cli_dbadmin_account_name', :'custom_dbadmin_account_name', :'default_dbadmin_account_name') AS dbadmin_account;
\gset 
SELECT str_coalesce (:'cli_br4j_account_name', :'custom_br4j_account_name', :'default_br4j_account_name') AS br4j_account;
\gset 
SELECT str_coalesce (:'cli_portal_account_name', :'custom_portal_account_name', :'default_portal_account_name') AS portal_account;
\gset 

select create_account(:'br4j_account', 'dbmi_trunk, public', false);
select create_account(:'portal_account', 'public', false);
select create_account(:'dbadmin_account', 'dbmi_trunk, public', true);
select db_check(:'db_name');

DROP FUNCTION create_account(varchar, varchar, boolean);
DROP FUNCTION str_coalesce(varchar, varchar, varchar);
DROP FUNCTION str_coalesce(varchar, varchar);
DROP FUNCTION db_check(varchar);

--Защита от искажения уже существующей БД
\set ON_ERROR_STOP 1

-- Создание БД с определённым именем и владельцем, параметры LC_COLLATE и LC_CTYPE устанавливаются по умолчанию:
-- они определены при установке сервера или при инициализации кластера БД
CREATE DATABASE :db_name WITH OWNER = :dbadmin_account TEMPLATE = template0 ENCODING = 'UTF8';

-- Подсоединение к созданной БД под административным аккаунтом для заливки дампа
\connect :db_name :dbadmin_account

--**********************************************************************************************************--
--**********************************************************************************************************--
-------------------------------------- ЧАСТЬ 1. КОНЕЦ. ------------------------------------------------------- 
--**********************************************************************************************************--
--**********************************************************************************************************--


--**********************************************************************************************************--
--**********************************************************************************************************--
-------------------------------------- ЧАСТЬ 2. НАЧАЛО. ------------------------------------------------------ 
--**********************************************************************************************************--
--**********************************************************************************************************--

--------------------------------------------------------------------------------------------------------------
-------------------------------- НАПОЛНЕНИЕ БД. НАЧАЛО. ------------------------------------------------------
/******
 Вставьте в этот блок дамп БД в текстовом виде либо используйте дамп из следующего файла за текущим. 
 Дамп не должен содержать: 
     оператора создания БД
     определения владельца БД
     привелегии пользователей
 Пример консольной команды для получения такого дампа:
    pg_dump --host [hostname] --port 5432 --username "[username]"
    --no-password  --format plain --no-owner --no-privileges
    --no-tablespaces --verbose --no-unlogged-table-data
    --file "[some.file]" "[db-name]"
******/
--------------------------------------------------------------------------------------------------------------

-- ДАМП БД --

--------------------------------------------------------------------------------------------------------------
-------------------------------- НАПОЛНЕНИЕ БД. КОНЕЦ. -------------------------------------------------------
--------------------------------------------------------------------------------------------------------------

--**********************************************************************************************************--
--**********************************************************************************************************--
-------------------------------------- ЧАСТЬ 2. КОНЕЦ. ------------------------------------------------------- 
--**********************************************************************************************************--
--**********************************************************************************************************--

