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
package ru.datateh.jbr.iuh.parameters.other;

/**
 * @author etarakanov
 *         Date: 16.04.2015
 *         Time: 15:25
 */
public interface Dmsi
{
    /**
     *  Вид стандарта, по которому создано данное сообщение (сейчас используется значение "Стандарт системы управления документами")
     */
    String STANDART = "br4j.dbmi.dmsi.standart";
    /**
     * Версия стандарта (сейчас используется значение 1.0)
     */
    String VERSION = "br4j.dbmi.dmsi.version";
    /**
     * Уникальный служебный идентификационный номер системы
     */
    String SYS_ID = "br4j.dbmi.dmsi.sys_id";
    /**
     *  Наименование системы управления документами
     */
    String SYSTEM = "br4j.dbmi.dmsi.system";
    /**
     * Дополнительные данные о системе управления документами.
     */
    String SYSTEM_DETAILS = "br4j.dbmi.dmsi.system_details";
    /**
     * Обязательное для заполнение значение. Для него нужно указать card_id карточки Организации,
     * которая считается для данной СЭД организацией по умолчанию. Это означает, что если при выгрузке используется пользователь,
     * не привязанной к какой-то конкретной организации, то считается, что он относится к данной.
     */
    String DEFAULT_ORGANIZATION_ID = "br4j.dbmi.dmsi.default_organization_id";
    String IN_FOLDER = "br4j.dbmi.dmsi.inFolder";
    String IN_FOLDER_PROCESSED = "br4j.dbmi.dmsi.inFolderProcessed";
    String IN_FOLDER_DISCARDED = "br4j.dbmi.dmsi.inFolderDiscarded";

}
