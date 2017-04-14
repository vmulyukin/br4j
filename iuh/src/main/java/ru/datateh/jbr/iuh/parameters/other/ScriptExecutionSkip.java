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
 *         Date: 01.10.2015
 *         Time: 17:36
 */
public interface ScriptExecutionSkip
{
    /**
     * Вкл/выкл выполнение скрипта разыменования файлов транспортного агента в "/conf/dbmi/transportAgent/*.properties.example"
     */
    String TRANSPORT_AGENT_SKIP = "br4j.dbmi.transportAgent.skip";

    /**
     * Вкл/выкл выполнение скрипта разыменования файлов в "/conf/dbmi/mail/*.properties.example"
     */
    String MAIL_SKIP = "br4j.dbmi.mail.skip";

    /**
     * Вкл/выкл выполнение скрипта разыменования файла "/conf/dbmi/WorkCalendar.xml.example"
     */
    String WORK_CALENDAR_SKIP = "br4j.dbmi.workcalendar.skip";

    /**
     * Вкл/выкл выполнение скрипта разыменования файла в "/conf/dbmi/massSaveTask/query.sql.example"
     */
    String MASS_SAVE_TASK_SKIP = "br4j.dbmi.masssavetask.skip";

    /**
     * Вкл/выкл выполнение скрипта разыменования файла в "/conf/dbmi/AutoUnlockConfig.xml.example"
     */
    String AUTO_UNLOCK_SKIP = "br4j.dbmi.autoulock.skip";

    /**
     * Вкл/выкл выполнение скрипта разыменования файлов функционала автоутверждения неконтрольных поручений
     * "/conf/dbmi/transportAgent/flags.properties.example" и "/conf/dbmi/transportAgent/pagesVisibility.properties.example"
     */
    String AUTO_APPROVE_NONCONTROL_REPORTS = "br4j.dbmi.autoapprove.skip";
}
