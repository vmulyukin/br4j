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

import org.aplana.br4j.dynamicaccess.db_export.DBOperationUtil
import org.aplana.br4j.dynamicaccess.xmldef.AccessConfig
import ru.datateh.jbr.iuh.msg.Message
import ru.datateh.jbr.iuh.msg.MessageType
import ru.datateh.jbr.iuh.parameters.CommonParameters;

/**
 * @author etarakanov
 * Date: 09.04.2015
 * Time: 16:34
 */
class AccessRulesUtils {
    /**
     * Выполняет частичное обновление прав доступа
     * @param parameters карта параметров
     * @param accessRulesFileName имя файля, содержащего информацию об обновляемых правах
     * @return сообщение об успешности или ошибочности операции
     */
    public static Message performPartialUpdate(Map<String, String> parameters, String accessRulesFileName)
    {
        Exception result;
        AccessConfig accessConfig = loadAccessConfig(parameters, accessRulesFileName)
        if (accessConfig != null && checkDBConnectParameters(parameters)) {
            DBOperationUtil util = new DBOperationUtil();
            //частичное обновление правил
            result = util.doUpdatePartial(parameters.get(CommonParameters.DataSources.DBMI_DB_URL), parameters.get(CommonParameters.DataSources.DBMI_DB_USER),
                    parameters.get(CommonParameters.DataSources.DBMI_DB_PASSWORD), accessConfig);
        }
        else
        {
            return new Message(MessageType.ERROR, "Error. DB connection parameters or file name is invalid")
        }
        if (result != null)
        {
            return new Message(MessageType.ERROR, "Error while perform partial update rules", result);
        }
        return new Message(MessageType.SUCCESS, "Partial update rules successfully finished")
    }

    private static AccessConfig loadAccessConfig (Map<String, String> parameters, String filePath) throws Exception {
        if (filePath != null) {
            File file = new File (parameters.get(CommonParameters.Iuh.CURRENT_SCRIPT_PATH) + File.separator + "data"  + File.separator + filePath);
            if (FileUtils.checkFileExist(file, false))
            {
                InputStreamReader reader = new InputStreamReader(new FileInputStream(file.getAbsolutePath()), "UTF-8");
                return  (AccessConfig) AccessConfig.unmarshal(reader);
            }
        }
        return null;
    }

    private static boolean checkDBConnectParameters (Map<String, String> parameters)
    {
        if (parameters != null) {
            if (parameters.get(CommonParameters.DataSources.DBMI_DB_URL) == null || parameters.get(CommonParameters.DataSources.DBMI_DB_URL).isEmpty())
                return false;
            if (parameters.get(CommonParameters.DataSources.DBMI_DB_USER) == null || parameters.get(CommonParameters.DataSources.DBMI_DB_USER).isEmpty())
                return false;
            if (parameters.get(CommonParameters.DataSources.DBMI_DB_PASSWORD) == null || parameters.get(CommonParameters.DataSources.DBMI_DB_PASSWORD).isEmpty())
                return false;
        } else { return false; }
        return true;
    }


}
