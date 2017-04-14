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
 *         Time: 13:16
 */
public interface System
{
    /**
     * Расположение JBoss
     */
    String JBOSS_HOME = "br4j.jboss.home";
    /**
     * Расположение рабочей конфигурации JBoss
     */
    String JBOSS_CONFIGURATION = "br4j.jboss.configuration";
    /**
     * Полный путь до рабочей конфигурации JBoss
     */
    String JBOSS_CONFIGURATION_PATH = "br4j.jboss.configuration.path";
    /**
     * Версия выпуска BR4J
     */
    String DBMI_BUILD_NUMBER = "br4j.dbmi.build.number";
    /**
     * Модель ОС
     */
    String OS_NAME = "os.name";
    /**
     * Версия ОС
     */
    String OS_VERSION = "os.version";
    /**
     * Архитектура ОС
     */
    String OS_ARCH = "os.arch";
    /**
     * IP сервера
     */
    String OS_HOST_ADRESS = "os.host.address";
    /**
     * Имя сервера. Берётся только hostname на loopback так как брать все имена на всех интерфейсах - оч. долго
     */
    String OS_HOST_NAME = "os.host.name";
}
