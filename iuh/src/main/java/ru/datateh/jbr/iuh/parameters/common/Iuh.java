/**
 *   Licensed to the Apache Software Foundation (ASF) under one or more
 *   contributor license agreements.  See the NOTICE file distributed with
 *   this work for additional information regarding copyright ownership.
 *   The ASF licenses this file to you under the Apache License, Version 2.0
 *   (the "License"); you may not use this file except in compliance with
 *   the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package ru.datateh.jbr.iuh.parameters.common;

/**
 * @author etarakanov
 *         Date: 16.04.2015
 *         Time: 13:26
 */
public interface Iuh
{
    /**
     * Путь к jar-файлу оснастки. Данный параметр используется только для тестирования.
     */
    String CODE_SOURCE = "iuh.code.source.path";
    /**
     * Путь к набору обнавления
     */
    String UPDATE_SET_PATH = "iuh.update.set.path";
    /**
     * Путь к пакету скрипта, который исполняется в данный момент
     */
    String CURRENT_SCRIPT_PATH = "iuh.current.script.path";
    /**
     * Форсированный режим
     */
    String MODE_FORCE = "iuh.mode.force";
    /**
     * Интерактивный режим
     */
    String MODE_INTERACTIVE = "iuh.mode.interactive";
    /**
     * Учетная запись пользователя, запустившего оснастку
     */
    String STARTER_USER_NAME = "iuh.starter.user.name";
    /**
     * Путь к файлу ответов
     */
    String ANSWER_FILE = "iuh.answers.file";

    /**
     * Путь к пользовательскому файлу ответов
     */
    String USER_ANSWER_FILE = "iuh.user.answers.file";
    /**
     * Режим прогона
     */
    String MODE_RUN = "iuh.mode.run";
    /**
     * Учетная запись пользователя, запустившего скприпт
     */
    String EXECUTOR_USER_NAME = "iuh.executor.user.name";
    /**
     * Префикс перед названием параметра в файле ответов, включающий в себя имя набора, имя пакета, имя скрипта в
     * формате - SuiteName.PackageName.ScriptName.parameterName, например: "update.00.Init.OpenOffice.br4j.dbmi.convertor.temp.dir=data/ooconverter"
     */
    String ANSWERS_PREFIX = "iuh.answers.prefix";
}
