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
import com.sun.xml.internal.bind.v2.TODO
import groovy.util.logging.Log4j
import ru.datateh.jbr.iuh.groovy.AbstractExecute
import ru.datateh.jbr.iuh.groovy.anno.Parameters
import ru.datateh.jbr.iuh.groovy.utils.FileUtils

/**
 * @author etarakanov
 * Date: 05.03.15
 * Time: 17:38
 */
@Log4j
public class CreateDB extends AbstractExecute {
    //TODO скорректирова SQL-скрипт, чтобы он не создавал db.main.account
    //TODO скорректировать SQL скрипт, чтобы он мог создвал db.account и portal_account с переданными паролями
    //TODO передавать в строку запуска db.account и portal_account с паролями
    //TODO не передавать в строку запуска db.main.account
    //TODO брать db.name db.host db.port из карты параметров
    //TODO использовать в качестве -U postgress, пользователя из внешнего источника


    private static final String IUH_PSQL_WINDOWS_PATH = "br4j.db.bin.path";
    private static final String IUH_DB_NAME = "br4j.db.name";
    private static final String IUH_DB_HOST_NAME = "br4j.db.host.name";
    private static final String IUH_DB_PORT_NUMBER = "br4j.db.port.number";

    private static final String IUH_DBMI_DB_USER = "br4j.dbmi.db.user.name";
    private static final String IUH_DBMI_DB_PASSWORD = "br4j.dbmi.db.user.password";
    private static final String IUH_PORTAL_DB_USER = "br4j.jboss.portal.db.user.name";
    private static final String IUH_PORTAL_DB_PASSWORD = "br4j.jboss.portal.db.user.password";
    private static final String IUH_DBMI_DB_MAIN_USER = "br4j.dbmi.db.main.user.name";

    private static final String IUH_PSQL_WINDOWS_PATH_DEFVAL = "C:"+ File.separator + "PROGRA~1" + File.separator +
            "PostgreSQL" + File.separator + "9.3" + File.separator + "bin";
    private static final String IUH_DB_NAME_DEFVAL = "br4j";
    private static final String IUH_DB_HOST_NAME_DEFVAL = "172.16.125.102";
    private static final String IUH_DB_PORT_NUMBER_DEFVAL = "5432";
    private static final String IUH_DBMI_DB_USER_DEFVAL = "dbmi";
    private static final String IUH_DBMI_DB_PASSWORD_DEFVAL = "dbmi";
    private static final String IUH_PORTAL_DB_USER_DEFVAL = "portal";
    private static final String IUH_PORTAL_DB_PASSWORD_DEFVAL = "portal";
    private static final String IUH_DBMI_DB_MAIN_USER_DEFVAL = "br4j_admin";

    private static final String DB_NAME = "cli_db_name";
    private static final String DBMI_DB_USER = "cli_br4j_account_name";
    private static final String DBMI_DB_PASSWORD = "cli_br4j_password";
    private static final String PORTAL_DB_USER = "cli_portal_account_name";
    private static final String PORTAL_DB_PASSWORD = "cli_portal_password";

    private static final String CREATE_USERS_SCRIPT = "1.create_users.sql";
    private static final String CREATE_DB_SCRIPT = "2.create_db.sql";
    private static final String GRANTS_SCRIPT = "3.grants.sql";

    private static String sqlScriptPath;

    Map<String, String> properties;

    public void install() {
        log.info "CreateDB is running... "
        sqlScriptPath = getScriptPath();
        FileUtils.checkFileExist(new File(sqlScriptPath + CREATE_USERS_SCRIPT), true);
        FileUtils.checkFileExist(new File(sqlScriptPath + CREATE_DB_SCRIPT), true);
        FileUtils.checkFileExist(new File(sqlScriptPath + GRANTS_SCRIPT), true);

        String prntCmd = getPrintCommand();
        String psqlCmd = getPsqlCommand();

        log.debug prntCmd + " | " + psqlCmd;

        int execResult = executeCmd(prntCmd, psqlCmd);
        if (execResult > 0){
            System.exit(execResult);
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
            if (getParam("os.name").startsWith("Win")) {
                properties.put(IUH_PSQL_WINDOWS_PATH,
                        getPropertyValue(IUH_PSQL_WINDOWS_PATH, IUH_PSQL_WINDOWS_PATH_DEFVAL));
            }
            properties.put(IUH_DB_NAME,
                    getPropertyValue(IUH_DB_NAME, IUH_DB_NAME_DEFVAL));
            properties.put(IUH_DB_HOST_NAME,
                    getPropertyValue(IUH_DB_HOST_NAME, IUH_DB_HOST_NAME_DEFVAL));
            properties.put(IUH_DB_PORT_NUMBER,
                    getPropertyValue(IUH_DB_PORT_NUMBER, IUH_DB_PORT_NUMBER_DEFVAL));
            properties.put(IUH_DBMI_DB_USER,
                    getPropertyValue(IUH_DBMI_DB_USER, IUH_DBMI_DB_USER_DEFVAL));
            properties.put(IUH_DBMI_DB_PASSWORD,
                    getPropertyValue(IUH_DBMI_DB_PASSWORD, IUH_DBMI_DB_PASSWORD_DEFVAL));
            properties.put(IUH_PORTAL_DB_USER,
                    getPropertyValue(IUH_PORTAL_DB_USER, IUH_PORTAL_DB_USER_DEFVAL));
            properties.put(IUH_PORTAL_DB_PASSWORD,
                    getPropertyValue(IUH_PORTAL_DB_PASSWORD, IUH_PORTAL_DB_PASSWORD_DEFVAL));
            properties.put(IUH_DBMI_DB_MAIN_USER,
                    getPropertyValue(IUH_DBMI_DB_MAIN_USER, IUH_DBMI_DB_MAIN_USER_DEFVAL));
        }
        return properties
    }

    private String getPrintCommand()
    {
        StringBuilder result = new StringBuilder();
        result.append(sqlScriptPath + CREATE_USERS_SCRIPT + " ");
        result.append(sqlScriptPath + CREATE_DB_SCRIPT + " ");
        result.append(sqlScriptPath + GRANTS_SCRIPT + " ");

        if (getParam("os.name").startsWith("Win"))
        {
            return "cmd /C type " + result.toString();
        }
        return "cat " + result.toString();
    }

    private String getPsqlCommand ()
    {
        if (getParam("os.name").startsWith("Win"))
        {
            return collectProperties().get(IUH_PSQL_WINDOWS_PATH) + File.separator + "psql" + " " + getUserParams() + getSystemParams();
        }
        return "psql"  + " " + getUserParams() + getSystemParams();
    }

    private String getUserParams()
    {
        StringBuilder result = new StringBuilder();
        result.append("-v " + DB_NAME + "=" + "'"+ collectProperties().get(IUH_DB_NAME) + "'" + " ");
        result.append("-v " + DBMI_DB_USER + "=" + "'"+ collectProperties().get(IUH_DBMI_DB_USER) + "'" + " ");
        result.append("-v " + DBMI_DB_PASSWORD + "=" + "'"+ collectProperties().get(IUH_DBMI_DB_PASSWORD) + "'" + " ");
        result.append("-v " + PORTAL_DB_USER + "=" + "'"+ collectProperties().get(IUH_PORTAL_DB_USER) + "'" + " ");
        result.append("-v " + PORTAL_DB_PASSWORD + "=" + "'"+ collectProperties().get(IUH_PORTAL_DB_PASSWORD) + "'" + " ");
        return result.toString();
    }

    private String getSystemParams()
    {
        StringBuilder result = new StringBuilder();
        result.append("-h " + collectProperties().get(IUH_DB_HOST_NAME) + " ");
        result.append("-p " + collectProperties().get(IUH_DB_PORT_NUMBER) + " ");
        result.append("-d " + "postgres" + " ");
        result.append("-U " + IUH_DBMI_DB_MAIN_USER_DEFVAL + " ");
        return result.toString();
    }

    private String getScriptPath ()
    {
        File scriptPath = new File (this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
        return scriptPath.getAbsolutePath()  + File.separator + 'data' + File.separator;
    }

    public static void main(String[] args) {
        new CreateDB().start()
    }

}
