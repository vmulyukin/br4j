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
 *         Time: 13:54
 */
public interface DataSources
{
    /**
     * Имя файла содержащего настройки источников данных
     */
    String DS_FILE_NAME = "br4j.dbmi.ds.filename";
    /**
     * Имя БД
     */
    String DB_NAME = "br4j.db.name";
    /**
     * Имя сервера БД
     */
    String DB_HOST_NAME = "br4j.db.host.name";
    /**
     * Номер порта
     */
    String DB_PORT_NUMBER = "br4j.db.port.number";

    /**
     * jndi имя источника данных DBMI
     */
    String JNDI_NAME_DBMIDS = "br4j.dbmi.ds.jndi-name.DBMIDS";
    /**
     * jndi имя источника данных DBMI_EVENT
     */
    String JNDI_NAME_DBMIDS_EVENT = "br4j.dbmi_event.ds.jndi-name.DBMIDS_EVENT";
    /**
     * jndi имя источника данных PORTAL
     */
    String JNDI_NAME_PORTAL = "br4j.jboss.portal.ds.jndi-name.PortalDS";

    /**
     * URL к БД DBMI
     */
    String DBMI_DB_URL = "br4j.dbmi.db.url";
    /**
     * Дривер для доступа к БД DBMI
     */
    String DBMI_DB_DRIVER = "br4j.dbmi.db.driver";
    /**
     * Учетная запись пользователя для доступа к БД DBMI
     */
    String DBMI_DB_USER = "br4j.dbmi.db.user.name";
    /**
     * Пароль учетной записи пользователя для доступа к БД DBMI
     */
    String DBMI_DB_PASSWORD = "br4j.dbmi.db.user.password";
    /**
     * Минимальный размер пула запросов к БД DBMI
     */
    String DBMI_DB_POOLSIZE_MIN = "br4j.dbmi.db.poolsize.min";
    /**
     * Максимальный размер пула запросов к БД DBMI
     */
    String DBMI_DB_POOLSIZE_MAX = "br4j.dbmi.db.poolsize.max";
    /**
     * SQL запрос для диагностики подключения к БД DBMI
     */
    String DBMI_DB_CHECK_SQL = "br4j.dbmi.db.check_sql";

    /**
     * URL к БД DBMI_EVENT
     */
    String DBMI_EVENT_DB_URL = "br4j.dbmi_event.db.url";
    /**
     * Дривер для доступа к БД DBMI_EVENT
     */
    String DBMI_EVENT_DB_DRIVER = "br4j.dbmi_event.db.driver";
    /**
     * Учетная запись пользователя для доступа к БД DBMI_EVENT
     */
    String DBMI_EVENT_DB_USER = "br4j.dbmi_event.db.user.name";
    /**
     * Пароль учетной записи пользователя для доступа к БД DBMI_EVENT
     */
    String DBMI_EVENT_DB_PASSWORD = "br4j.dbmi_event.db.user.password";
    /**
     * Минимальный размер пула запросов к БД DBMI_EVENT
     */
    String DBMI_EVENT_DB_POOLSIZE_MIN = "br4j.dbmi_event.db.poolsize.min";
    /**
     * Максимальный размер пула запросов к БД DBMI_EVENT
     */
    String DBMI_EVENT_DB_POOLSIZE_MAX = "br4j.dbmi_event.db.poolsize.max";
    /**
     * SQL запрос для диагностики подключения к БД DBMI_EVENT
     */
    String DBMI_EVENT_DB_CHECK_SQL = "br4j.dbmi_event.db.check_sql";

    /**
     * URL к БД PORTAL
     */
    String PORTAL_DB_URL = "br4j.jboss.portal.db.url";
    /**
     * Дривер для доступа к БД PORTAL
     */
    String PORTAL_DB_DRIVER = "br4j.jboss.portal.db.driver";
    /**
     * Учетная запись пользователя для доступа к БД PORTAL
     */
    String PORTAL_DB_USER = "br4j.jboss.portal.db.user.name";
    /**
     * Пароль учетной записи пользователя для доступа к БД PORTAL
     */
    String PORTAL_DB_PASSWORD = "br4j.jboss.portal.db.user.password";
    /**
     * Минимальный размер пула запросов к БД PORTAL
     */
    String PORTAL_DB_POOLSIZE_MIN = "br4j.jboss.portal.db.poolsize.min";
    /**
     * Максимальный размер пула запросов к БД PORTAL
     */
    String PORTAL_DB_POOLSIZE_MAX = "br4j.jboss.portal.db.poolsize.max";
    /**
     * SQL запрос для диагностики подключения к БД PORTAL
     */
    String PORTAL_DB_CHECK_SQL = "br4j.jboss.portal.db.check_sql";

    /**
     * Тип отображения типов данных БД
     */
    String TYPE_MAPPING = "br4j.db.type-mapping";
}
