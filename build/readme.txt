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

1. Обновление приложения: скопировать содержимое папки 'App' в ${JBOSS_HOME}/server/default
2. Обновление БД: выполнить скрипт из папки DB_Update
3. Выполнение доп. инструкций: инструкции лежат в папке Instructions в файле readme_*release_name*.txt, доп. файлы (например, новые библиотеки) лежат в соотвествующих подкаталогах
4. В папке Change_List лежит список изменений в новой версии приложения
5. В папке ACL_Tool лежит последняя версия утилиты для редактирования прав
5. Информация о совместимости сборок и патчей смотреть в passport.txt (блок ancestors)
