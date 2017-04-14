====
      Licensed to the Apache Software Foundation (ASF) under one or more
      contributor license agreements.  See the NOTICE file distributed with
      this work for additional information regarding copyright ownership.
      The ASF licenses this file to you under the Apache License, Version 2.0
      (the "License"); you may not use this file except in compliance with
      the License.  You may obtain a copy of the License at

          http://www.apache.org/licenses/LICENSE-2.0

      Unless required by applicable law or agreed to in writing, software
      distributed under the License is distributed on an "AS IS" BASIS,
      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
      See the License for the specific language governing permissions and
      limitations under the License.
====

Скрипт создания БД "с нуля" состоит из трёх частей-файлов. Такая разбивка выполнена для удобства использования и сопровождения.
Скрипт создания БД предназначен только для использования вместе с утилитой "psql" из комплекта "PostgreSQL client".
Скрипт должен быть выполнен от имени администратора СУБД.
Скрипт должен выполняться как одно целое. Для этого можно применить один из методов: 
	1) перед выполнением его части должны быть соединены в один файл, в порядке, предусмотренным их именами;
	2) соединить содержимое файлов прямо во время исполнения, к примеру:
		cat 1.create_users.sql  2.create_db.sql  3.grants.sql | psql -U postgres -d postgres (в Linux)
		type 1.create_users.sql  2.create_db.sql  3.grants.sql | psql -U postgres -d postgres (в Windows)
	3) при запуске вручную из коммандной строки для корректной работы скрипта "1.create_users.sql" необходимо указать следующие дополнительные параметры:
		-v cli_db_name=  -v cli_br4j_account_name=  -v cli_dbadmin_account_name=  -v cli_portal_account_name= .
		Если планируется использовать значения по умолчанию для этих параметров, то значения можно не указывать, но ключи должны быть указаны обязательно, как в приведенном примере.

Часть 1. Файл "1.create_users.sql". 
Создаёт 3 пользовательские учётные записи (если они ещё не созданы) в СУБД (имена можно менять в заголовке):
	dbmi		учётная запись для работы приложения BR4J;
	portal		учётная запись для работы портала JBoss Portal;
	br4j_admin	учётная запись для администрирования данной БД: установка свойств и режимов работы БД, распределение элементов БД, выполнение скриптов обновления.
В данном скрипте задаётся имя создаваемой БД, по умолчанию: "br4j".
В данной части есть возможность включить интерактивный режим ввода используемых параметров. Для этого необходимо раскомментировать соответствующий блок.
Также в этой части расположена команда создания БД: "CREATE DATABASE".

Часть 2. Файл "2.create_db.sql".
Создаёт все необходимые структуры БД, а также заливает минимальные данные, необходимые для работы приложения BR4J.
Фактически является результатом получения дампа соответствующей БД со следующими особенностями:
	нет оператора создания БД
	нет определения владельца БД
	нет привелегии пользователей
Пример консольной команды для получения такого дампа:
    pg_dump --host [hostname] --username "[username]" --no-password --format plain --no-owner --no-privileges --no-tablespaces --verbose --no-unlogged-table-data --file "[some.file]" "[db-name]"

Часть 3. Файл "3.grants.sql".
Устанавливает правила доступа для пользователей БД, созданных в первой части на объекты БД, созданные во второй части.



