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
package ru.datateh.jbr.iuh.utils
import groovy.sql.Sql
import ru.datateh.jbr.iuh.msg.Message
import ru.datateh.jbr.iuh.msg.MessageType
import ru.datateh.jbr.iuh.parameters.CommonParameters

import java.sql.SQLException

/**
 * @author etarakanov
 * Date: 20.04.2015
 * Time: 15:13
 */

public class SQLUtils {
    /**
     * Выполнить указанный SQL запрос
     * @param parameters карта параметров
     * @param sql SQL запрос
     * @return сообщение об успешности или ошибочности операции
     */
    public static Message execSql(Map<String, String> parameters, String sql) {

        try {
            Sql conn = Sql.newInstance(parameters.get(CommonParameters.DataSources.DBMI_DB_URL), parameters.get(CommonParameters.DataSources.DBMI_DB_USER),
                    parameters.get(CommonParameters.DataSources.DBMI_DB_PASSWORD, parameters.get(CommonParameters.DataSources.DBMI_DB_DRIVER)));
            conn.execute(sql);
        } catch (SQLException e) {
            return new Message(MessageType.ERROR, "Error while perform SQL query: " + sql, e);
        }
        return new Message(MessageType.SUCCESS, "SQL query successfully executed")
    }
    /**
     * Выполнить SQL скрипт из указанного файла
     * @param parameters карата параметров
     * @param file файл содержащий SQL скрипт
     * @return сообщение об успешности или ошибочности операции
     */
    public static Message execSqlFromFile(Map<String, String> parameters, String file) {

        File sqlScriptFile = new File (parameters.get(CommonParameters.Iuh.CURRENT_SCRIPT_PATH) + File.separator + "data"  + File.separator + file);
        if(!FileUtils.checkFileExist(sqlScriptFile, false)){
            return new Message(MessageType.ERROR, "Error. File: " + sqlScriptFile + "does not exist");}

        StringBuilder sb = new StringBuilder()
        List<String> sqlScriptLines = FileUtils.readLines(sqlScriptFile);
        for (String sqlLine : sqlScriptLines)
        {
            sb.append(sqlLine);
        }

        return execSql(parameters, sb.toString());
    }
}
