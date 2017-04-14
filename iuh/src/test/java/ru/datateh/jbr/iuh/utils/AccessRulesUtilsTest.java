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
package ru.datateh.jbr.iuh.utils;

import mockit.Expectations;
import mockit.Mocked;
import mockit.Tested;
import mockit.Verifications;
import org.aplana.br4j.dynamicaccess.db_export.DBOperationUtil;
import org.aplana.br4j.dynamicaccess.xmldef.AccessConfig;
import org.junit.Test;
import ru.datateh.jbr.iuh.parameters.CommonParameters;

import java.util.HashMap;
import java.util.Map;

import static mockit.Deencapsulation.invoke;

/**
 * @author etarakanov
 *         Date: 09.04.2015
 *         Time: 17:26
 */
public class AccessRulesUtilsTest {

    @Mocked
    DBOperationUtil util;

    private static final String DBMI_DB_URL = "br4j.dbmi.db.url";
    private static final String DBMI_DB_USER = "br4j.dbmi.db.user.name";
    private static final String DBMI_DB_PASSWORD = "br4j.dbmi.db.user.password";

    private static final String DBMI_DB_URL_VAL = "url";
    private static final String DBMI_DB_USER_VAL = "user";
    private static final String DBMI_DB_PASSWORD_VAL = "password";

    @Tested
    AccessRulesUtils accessRulesUtils;

    @Test
    public void testPerformPartialUpdateSuccess() throws Exception {

        System.out.println("Perform test");
        new Expectations(accessRulesUtils){{
            invoke(accessRulesUtils, "loadAccessConfig", withAny(HashMap.class), anyString); result = new AccessConfig();
            DBOperationUtil util = new DBOperationUtil();
            util.doUpdatePartial(DBMI_DB_URL_VAL, DBMI_DB_USER_VAL, DBMI_DB_PASSWORD_VAL, (AccessConfig) any); result = null;
        }};

        AccessRulesUtils.performPartialUpdate(getParameters(),"testFileName");
    }

    @Test
    public void testPerformPartialUpdateError() throws Exception {

        System.out.println("Perform test");
        new Expectations(accessRulesUtils){{
            invoke(accessRulesUtils, "loadAccessConfig", withAny(HashMap.class), anyString); result = new AccessConfig();
        }};

        AccessRulesUtils.performPartialUpdate(null, "testFileName");
        Map<String, String> parameters = getParameters();
        parameters.remove(DBMI_DB_URL);
        AccessRulesUtils.performPartialUpdate(parameters, "testFileName");
        parameters = getParameters();
        parameters.remove(DBMI_DB_USER);
        AccessRulesUtils.performPartialUpdate(parameters, "testFileName");
        parameters = getParameters();
        parameters.remove(DBMI_DB_PASSWORD);
        AccessRulesUtils.performPartialUpdate(parameters, "testFileName");

        new Verifications(){{
            util.doUpdatePartial(anyString, anyString, anyString, (AccessConfig) any); times = 0;
        }};
    }

    private  Map<String, String> getParameters ()
    {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put(CommonParameters.DataSources.DBMI_DB_URL, DBMI_DB_URL_VAL);
        parameters.put(CommonParameters.DataSources.DBMI_DB_USER, DBMI_DB_USER_VAL);
        parameters.put(CommonParameters.DataSources.DBMI_DB_PASSWORD, DBMI_DB_PASSWORD_VAL);
        parameters.put(CommonParameters.Iuh.CURRENT_SCRIPT_PATH, "D:/temp");
        return parameters;
    }
}