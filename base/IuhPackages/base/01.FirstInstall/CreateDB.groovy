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
import groovy.util.logging.Log4j
import ru.datateh.jbr.iuh.AbstractExecute
import ru.datateh.jbr.iuh.anno.Parameters
import ru.datateh.jbr.iuh.msg.HarnessException
import ru.datateh.jbr.iuh.msg.Message
import ru.datateh.jbr.iuh.msg.MessageType
import ru.datateh.jbr.iuh.parameters.CommonParameters
import ru.datateh.jbr.iuh.utils.FileUtils
/**
 * @author etarakanov
 * Date: 05.03.15
 * Time: 17:38
 */
@Log4j
public class CreateDB extends AbstractExecute {

    private static final IUH_PSQL_WINDOWS_PATH = "br4j.db.bin.path";

    private static final IUH_PSQL_WINDOWS_PATH_DEFVAL = "C:"+ File.separator + "PROGRA~1" + File.separator +
            "PostgreSQL" + File.separator + "9.3" + File.separator + "bin";

    private static final DB_NAME = "cli_db_name";
    private static final DB_ACCOUNT_NAME = "cli_br4j_account_name";
    private static final DB_MAIN_ACCOUNT_NAME = "cli_dbadmin_account_name";
    private static final PORTAL_ACCOUNT_NAME = "cli_portal_account_name";

    private static final CREATE_USERS_SCRIPT = "1.create_users.sql";
    private static final CREATE_DB_SCRIPT = "2.create_db.sql";
    private static final GRANTS_SCRIPT = "3.grants.sql";

    private static String sqlScriptPath;

    Map<String, String> properties;

    public void install() {
        log.info "CreateDB is running... "
        sqlScriptPath = getParam(CommonParameters.Iuh.CURRENT_SCRIPT_PATH) + File.separator + 'data' + File.separator;
        FileUtils.checkFileExist(new File(sqlScriptPath + CREATE_USERS_SCRIPT), true);
        FileUtils.checkFileExist(new File(sqlScriptPath + CREATE_DB_SCRIPT), true);
        FileUtils.checkFileExist(new File(sqlScriptPath + GRANTS_SCRIPT), true);

        String prntCmd = getPrintCommand();
        String psqlCmd = getPsqlCommand();

        log.debug prntCmd + " | " + psqlCmd;

        int execResult = executeCmd(prntCmd, psqlCmd);
        if (execResult > 0){
            msg = new Message(MessageType.ERROR, "Error while execute commands. Exit code: " + execResult);
            throw new HarnessException (msg);
        }

        log.info "CreateDB successfully finished... ";
    }

    private int executeCmd (String[] cmd)
    {
        List<String> commands = Arrays.asList(cmd);

        Process process1 = commands.inject( null ) { previous, next ->
            if(previous)
                previous | next.execute()
            else
                next.execute()
        } as Process

        InputStreamReader reader = new InputStreamReader(process1.getInputStream());
        InputStreamReader errReader = new InputStreamReader(process1.getErrorStream());
        Scanner scan = new Scanner(reader);
        Scanner errScan = new Scanner(errReader);

        new Thread(new Runnable() {
            public void run() {
                while (scan.hasNextLine()) {
                    log.debug scan.nextLine();
                }
            }
        }).start();

        new Thread(new Runnable() {
            public void run() {
                while (errScan.hasNextLine()) {
                    log.debug errScan.nextLine();
                }
            }
        }).start();

        return process1.waitFor();
    }

    private Map<String, String> collectProperties ()
    {
        if (properties == null)
        {
            properties = new HashMap<String, String>();
            //заполнение карты свойств
            if (getParam(CommonParameters.System.OS_NAME).startsWith("Win")) {
                properties.put(IUH_PSQL_WINDOWS_PATH,
                        getPropertyValue(IUH_PSQL_WINDOWS_PATH, IUH_PSQL_WINDOWS_PATH_DEFVAL));
            }
            properties.put(CommonParameters.DataSources.DB_NAME,
                    getPropertyValue(CommonParameters.DataSources.DB_NAME, null));
            properties.put(CommonParameters.DataSources.DB_HOST_NAME,
                    getPropertyValue(CommonParameters.DataSources.DB_HOST_NAME, null));
            properties.put(CommonParameters.DataSources.DB_PORT_NUMBER,
                    getPropertyValue(CommonParameters.DataSources.DB_PORT_NUMBER, null));
            properties.put(CommonParameters.DataSources.DBMI_DB_USER,
                    getPropertyValue(CommonParameters.DataSources.DBMI_DB_USER, null));
            properties.put(CommonParameters.Iuh.EXECUTOR_USER_NAME,
                    getPropertyValue(CommonParameters.Iuh.EXECUTOR_USER_NAME, null));
            properties.put(CommonParameters.DataSources.PORTAL_DB_USER,
                    getPropertyValue(CommonParameters.DataSources.PORTAL_DB_USER, null));
        }
        return properties
    }

    private static String getPrintCommand()
    {
        StringBuilder result = new StringBuilder();
        result.append(sqlScriptPath + CREATE_USERS_SCRIPT + " ");
        result.append(sqlScriptPath + CREATE_DB_SCRIPT + " ");
        result.append(sqlScriptPath + GRANTS_SCRIPT + " ");

        if (getParam(CommonParameters.System.OS_NAME).startsWith("Win"))
        {
            return "cmd /C type " + result.toString();
        }
        return "cat " + result.toString();
    }

    private String getPsqlCommand ()
    {
        if (getParam(CommonParameters.System.OS_NAME).startsWith("Win"))
        {
            return collectProperties().get(IUH_PSQL_WINDOWS_PATH) + File.separator + "psql" + " " + getUserParams() + getSystemParams();
        }
        return "psql"  + " " + getUserParams() + getSystemParams();
    }

    private String getUserParams()
    {
        StringBuilder result = new StringBuilder();
        result.append("-v " + DB_NAME + "=" + collectProperties().get(CommonParameters.DataSources.DB_NAME) + " ");
        result.append("-v " + DB_ACCOUNT_NAME + "=" + collectProperties().get(CommonParameters.DataSources.DBMI_DB_USER) + " ");
        result.append("-v " + DB_MAIN_ACCOUNT_NAME + "=" + collectProperties().get(CommonParameters.Iuh.EXECUTOR_USER_NAME) + " ");
        result.append("-v " + PORTAL_ACCOUNT_NAME + "=" + collectProperties().get(CommonParameters.DataSources.PORTAL_DB_USER) + " ");
        return result.toString();
    }

    private String getSystemParams()
    {
        StringBuilder result = new StringBuilder();
        result.append("-h " + collectProperties().get(CommonParameters.DataSources.DB_HOST_NAME) + " ");
        result.append("-p " + collectProperties().get(CommonParameters.DataSources.DB_PORT_NUMBER) + " ");
        result.append("-d " + "postgres" + " ");
        result.append("-U " + "postgres" + " ");
        return result.toString();
    }

    public static void main(String[] args) {
        new CreateDB().start()
    }

}
