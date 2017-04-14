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
-------------------------------------- ЧАСТЬ 3. НАЧАЛО. ------------------------------------------------------ 
--**********************************************************************************************************--
--**********************************************************************************************************--
/*SELECT coalesce(:cli_db_name, :'custom_db_name', :'default_db_name') AS db_name;
\gset
SELECT coalesce(:cli_dbadmin_account_name, :'custom_dbadmin_account_name', :'default_dbadmin_account_name') AS dbadmin_account;
\gset
SELECT coalesce(:cli_br4j_account_name, :'custom_br4j_account_name', :'default_br4j_account_name') AS br4j_account;
\gset
SELECT coalesce(:cli_portal_account_name, :'custom_portal_account_name', :'default_portal_account_name') AS portal_account;
\gset*/

\set ON_ERROR_STOP 1

-- Установка свойств роли для приложения BR4J
ALTER SCHEMA "dbmi_trunk" OWNER TO :br4j_account;
-- Режим транзакции по умолчанию - изменения разрешены
ALTER USER :br4j_account SET default_transaction_read_only to FALSE;

-- Разрешение на возможность соединения с БД.
-- Обычно такое разрешение существует по умолчанию для новых ролей.
GRANT CONNECT, TEMPORARY
  ON DATABASE :db_name TO :br4j_account;
 -- Использование объектов в схеме public - только чтение для dbmi
GRANT USAGE ON SCHEMA public TO :br4j_account;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO :br4j_account;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO :br4j_account;
GRANT SELECT ON ALL SEQUENCES IN SCHEMA public TO :br4j_account;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON SEQUENCES TO :br4j_account;
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA public TO :br4j_account;

-- Использование объектов в схеме dbmi_trunk - изменение для dbmi
GRANT USAGE ON SCHEMA dbmi_trunk TO :br4j_account;
GRANT ALL ON ALL TABLES IN SCHEMA dbmi_trunk TO :br4j_account;
ALTER DEFAULT PRIVILEGES IN SCHEMA dbmi_trunk GRANT ALL ON TABLES TO :br4j_account;
GRANT ALL ON ALL SEQUENCES IN SCHEMA dbmi_trunk TO :br4j_account;
ALTER DEFAULT PRIVILEGES IN SCHEMA dbmi_trunk GRANT ALL ON SEQUENCES TO :br4j_account;
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA dbmi_trunk TO :br4j_account;

-- Разрешаем использование процедурного языка PL/PgSQL и создание функций на нём для dbmi
GRANT USAGE  ON LANGUAGE "plpgsql" TO :br4j_account;

-- Установка свойств роли для JBossPortal
ALTER SCHEMA "public" OWNER TO :portal_account;

-- Режим транзакции по умолчанию - изменения разрешены
ALTER USER :portal_account SET default_transaction_read_only to FALSE;

-- Разрешение на возможность соединения с БД.
-- Обычно такое разрешение существует по умолчанию для новых ролей.
GRANT CONNECT, TEMPORARY
  ON DATABASE :db_name TO :portal_account;

-- Использование объектов в схеме dbmi_trunk - только чтение для portal
GRANT USAGE ON SCHEMA dbmi_trunk TO :portal_account;
GRANT SELECT ON ALL TABLES IN SCHEMA dbmi_trunk TO :portal_account;
ALTER DEFAULT PRIVILEGES IN SCHEMA dbmi_trunk GRANT SELECT ON TABLES TO :portal_account;
GRANT SELECT ON ALL SEQUENCES IN SCHEMA dbmi_trunk TO :portal_account;
ALTER DEFAULT PRIVILEGES IN SCHEMA dbmi_trunk GRANT SELECT ON SEQUENCES TO :portal_account;
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA dbmi_trunk TO :portal_account;

-- Использование объектов в схеме public - изменение для public
GRANT USAGE ON SCHEMA "public" TO :portal_account;
GRANT ALL ON ALL TABLES IN SCHEMA "public" TO :portal_account;
ALTER DEFAULT PRIVILEGES IN SCHEMA "public" GRANT ALL ON TABLES TO :portal_account;
GRANT ALL ON ALL SEQUENCES IN SCHEMA "public" TO :portal_account;
ALTER DEFAULT PRIVILEGES IN SCHEMA "public" GRANT ALL ON SEQUENCES TO :portal_account;
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA "public" TO :portal_account;

-- Разрешаем использование процедурного языка PL/PgSQL и создание функций на нём
GRANT USAGE  ON LANGUAGE "plpgsql" TO :portal_account;


--**********************************************************************************************************--
--**********************************************************************************************************--
-------------------------------------- ЧАСТЬ 3. КОНЕЦ. ------------------------------------------------------- 
--**********************************************************************************************************--
--**********************************************************************************************************--


