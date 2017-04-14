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

Требования: В системе должен быть установлен Groovy версии 2.2.2

Запуск оснастки:
Linux
sh starter.sh
Windows
starter.bat

Пример запуска оснастки с дополнительными параметрами:
sh starter.sh -Diuh.update.set.path=Examples/examples_suite_unix

Полный список дополнительных параметров смотреть в файле: parametersList.txt.

Требования:
    Для корректной работы оснастки в ее домашней папке должны находится следующие файлы:
iuh.properties - файл свойств остастки
answer.properties - файл ответов для запускаемых скриптов
log4j.groovy - файл настроек логирования
В папке IUH_HOME/lib должны находится следующие библиотеке:
clips.jar
log4j.jar
postgresql-9.3-1101.jdbc4.jar
DynamicAccessRules.jar

    При запуске оснастки должен быть установлен параметр "iuh.update.set.path" либо в файле "iuh.properties",
либо в параметре командной строки. Если параметры указаы в файле "iuh.properties" и в коммандной строке,
то значения параметров, указанные в командной строке будут иметь приоритет.