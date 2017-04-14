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
public class MigrateSQL extends AbstractExecute {

    private static final IUH_PSQL_WINDOWS_PATH = "br4j.db.bin.path";

    private static final IUH_PSQL_WINDOWS_PATH_DEFVAL = "C:"+ File.separator + "PROGRA~1" + File.separator +
            "PostgreSQL" + File.separator + "9.3" + File.separator + "bin";

    private static final MIGRATE_SCRIPT = "migrate.sql";

    private static final MODIFIED_MIGRATE_SCRIPT = "modified_migrate.sql";

    private static String modified_sqlScriptPath = System.getProperty('java.io.tmpdir') + File.separator;

    private static String sqlScriptPath;

    Map<String, String> properties;

    public void install() {
        log.info "MigrateSql is running... "
        sqlScriptPath = getParam(CommonParameters.Iuh.CURRENT_SCRIPT_PATH) + File.separator + 'data' + File.separator;
        File sqlScriptFile = new File(sqlScriptPath + MIGRATE_SCRIPT);
        if (FileUtils.checkFileExist(sqlScriptFile, false)) {
            addOnErrorStopParameter(sqlScriptFile);

            String prntCmd = getPrintCommand();
            String psqlCmd = getPsqlCommand();

            log.debug prntCmd + " | " + psqlCmd;

            int execResult = executeCmd(prntCmd, psqlCmd);
            if (execResult > 0){
                msg = new Message(MessageType.ERROR, "Error while execute commands. Exit code: " + execResult);
                throw new HarnessException (msg);
            }
        }
        else
        {
            log.warn "File: " + sqlScriptFile + " not found. Execution will not continued.";
        }

        log.info "MigrateSql successfully finished... ";
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
            properties.put(CommonParameters.Iuh.EXECUTOR_USER_NAME,
                    getPropertyValue(CommonParameters.Iuh.EXECUTOR_USER_NAME, null));
        }
        return properties
    }

    private static String getPrintCommand()
    {
        StringBuilder result = new StringBuilder();
        result.append(modified_sqlScriptPath + MODIFIED_MIGRATE_SCRIPT + " ");

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
            return collectProperties().get(IUH_PSQL_WINDOWS_PATH) + File.separator + "psql" + " " + getSystemParams();
        }
        return "psql"  + " " + getSystemParams();
    }

    private String getSystemParams()
    {
        StringBuilder result = new StringBuilder();
        result.append("-h " + collectProperties().get(CommonParameters.DataSources.DB_HOST_NAME) + " ");
        result.append("-p " + collectProperties().get(CommonParameters.DataSources.DB_PORT_NUMBER) + " ");
        result.append("-d " + collectProperties().get(CommonParameters.DataSources.DB_NAME) + " ");
        result.append("-U " + collectProperties().get(CommonParameters.Iuh.EXECUTOR_USER_NAME) + " ");
        return result.toString();
    }

    private static void addOnErrorStopParameter (File sqlScriptFile)
    {
        List<String> scriptLines = FileUtils.readLines(sqlScriptFile);
        scriptLines.add(0, "\\set ON_ERROR_STOP 1");
        File modified_sqlScriptFile = new File(modified_sqlScriptPath + MODIFIED_MIGRATE_SCRIPT);
        if (FileUtils.checkFileExist(modified_sqlScriptFile, false))
        {
            if (FileUtils.deleteFile(modified_sqlScriptPath + MODIFIED_MIGRATE_SCRIPT)) {
                storeFile(modified_sqlScriptFile, scriptLines);
            } else {
                throw new HarnessException (new Message(MessageType.ERROR, "Error while deleting file: " + modified_sqlScriptPath + MODIFIED_MIGRATE_SCRIPT));
            }
        } else {
            storeFile(modified_sqlScriptFile, scriptLines);
        }
    }

    private static boolean storeFile (File storedFile, List<String> storedLines)
    {
        try {
            if (storedFile.createNewFile()) {
                FileUtils.storeLines(storedFile, storedLines);
                log.debug "File: " + storedFile + "was created";
                return true;
            } else {
                throw new HarnessException (new Message(MessageType.ERROR, "Error while creating file: " + storedFile));
            }
        } catch (IOException e) {
            throw new HarnessException (new Message(MessageType.ERROR, e));
        }
    }

    public static void main(String[] args) {
        new MigrateSQL().start()
    }

}
