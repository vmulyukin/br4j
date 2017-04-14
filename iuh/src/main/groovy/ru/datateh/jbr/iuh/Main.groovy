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
package ru.datateh.jbr.iuh

import com.lts.ipc.sharedmemory.SharedMemory
import groovy.util.logging.Log4j
import org.apache.log4j.PropertyConfigurator
import ru.datateh.jbr.iuh.msg.HarnessException
import ru.datateh.jbr.iuh.msg.Message
import ru.datateh.jbr.iuh.msg.MessageType
import ru.datateh.jbr.iuh.parameters.CommonParameters
import ru.datateh.jbr.iuh.utils.FileUtils
import ru.datateh.jbr.iuh.utils.IuhUtils
import ru.datateh.jbr.iuh.utils.PropertiesUtils
import sun.misc.Signal
import sun.misc.SignalHandler

import java.util.prefs.Preferences
import java.util.regex.Matcher
import java.util.regex.Pattern

@Log4j
public class Main {

    static String CONFIG = "iuh.properties";
    static String ANSWER = "answer.properties";

    private static SharedMemory sm
    private static Map<String, String> map
    private static Scanner scanner = new Scanner(System.in);
    private static volatile InterruptedObject interruptedObject = InterruptedObject.getInstance()

    public static void main(String[] args) throws IOException {

        // Подключаем логгирование
        def config = new ConfigSlurper().parse(FileUtils.getLogConfigFile().toURI().toURL())
        Properties configProperties = config.toProperties();
        PropertyConfigurator.configure(configProperties)
        log.info 'Harness started.'
        log.debug 'Harness command line arguments: ' + args

        // Настраиваем обработку сигналов
        Signal.handle(new Signal("INT"), new HarnessSignalHandler())
        Signal.handle(new Signal("TERM"), new HarnessSignalHandler())

        // Создаем распределенный файл для работы из разных процессов
        map = new HashMap<String, String>();


        // Загружаем конфиг в распределенный файл
        getConfigParams()

        // Загружаем аргументы в распределенный файл
        getArgsParams(args);

        //Устанавливаем путь к файлу ответов
        setAnswerFilesParameters();

        log.trace 'map after parse run args and config params ' + map

        // Путь к набору
        String setPath = map.get(CommonParameters.Iuh.UPDATE_SET_PATH)

        if(setPath == null) {
            log.warn 'Path to current suite not found. Will be used current directory.'
            setPath = '.'
        }

        def force = map.get(CommonParameters.Iuh.MODE_FORCE) ? Boolean.valueOf(map.get(CommonParameters.Iuh.MODE_FORCE)) : false
        def interactive = map.get(CommonParameters.Iuh.MODE_INTERACTIVE) ? Boolean.valueOf(map.get(CommonParameters.Iuh.MODE_INTERACTIVE)) : false

        log.debug 'Run parameters:' + map

        log.debug 'Operating system: ' + System.getProperty('os.name') + ' ' + System.getProperty('os.version') + ' ' + System.getProperty('os.arch')
        log.debug 'Current user: ' + System.getProperty('user.name')
        log.debug 'Current directory: ' + System.getProperty('user.dir')

        if (!checkIsAdmin())
        {
            System.exit(1);
        }

        // Выгружаем проперти в распределенный файл
        IuhUtils.mapToSharedFile(map)


        // Создаем объект набора
        Suite suite = new Suite(setPath, force, interactive, interruptedObject)
        while(interruptedObject.isFlag()) {
            //interruptedObject.setFinished(true)
            sleep 100
        }
        //interruptedObject.setFinished(false)
        Message msg = suite.execute()

        if(msg.state == MessageType.ERROR) {
            log.error (log.isDebugEnabled() ? msg.getDetailedMessage() : msg)
            log.info 'Harness finished. '
            System.exit(1);
        } else {
            log.info (log.isDebugEnabled() ? msg.getDetailedMessage() : msg)
        }
        log.info 'Harness finished. '
    }

    public static boolean checkIsAdmin()
    {
        try{ //snippet from http://stackoverflow.com/questions/4350356/detect-if-java-application-was-run-as-a-windows-admin
            def currentUser = System.getProperty('user.name')
            log.debug "Checking administrative priveleges of current user \'${currentUser}\'..."
            Preferences prefs = Preferences.systemRoot();
            prefs.put("foo", "bar"); 	// SecurityException on Windows
            prefs.remove("foo");
            prefs.flush(); 				// BackingStoreException on Linux
            log.debug "...user \'${currentUser}\' has administrative priveleges"
            map.put(CommonParameters.Iuh.STARTER_USER_NAME, currentUser);
            log.debug "...DONE"
            return true;
        }catch(Exception ex){
            log.error 'Current user MUST HAVE administrative priveleges !'
            return false;
        }
    }

    private static void getConfigParams() {
        // Подгружаем проперти, если файла
        File conf = new File(CONFIG)
        if(!conf.exists()) {
            log.warn 'Properties file ' + CONFIG + ' not found. Will be used only calculated parameters and run args.'
            //System.exit(1)
        } else {
            Properties iuh = new Properties()
            iuh = PropertiesUtils.readPropertiesFromFile(conf);
            log.debug 'Loaded run parameters from ' + CONFIG + ':' + iuh

            // Параметры из конфига выгружаем в мап
            iuh.stringPropertyNames().each {
                map.put(it, iuh.getProperty(it))
            }
        }
    }

    private static String getAnswerFilePath ()
    {
        String answerFileName;
        answerFileName = map.get(CommonParameters.Iuh.ANSWER_FILE);
        if (!((answerFileName != null && !answerFileName.isEmpty()) && FileUtils.checkFileExist(new File(answerFileName), false))){
            answerFileName = FileUtils.getCodeSourcePath() + File.separator + ANSWER;
            if(!FileUtils.checkFileExist(new File(answerFileName), false)){
                throw new HarnessException(new Message(MessageType.ERROR, "Answer file is missing"))
            }
        }
        return new File(answerFileName).getAbsolutePath();
    }

    private static String getUserAnswerFilePath ()
    {
        String userAnswerFileName;
        userAnswerFileName = map.get(CommonParameters.Iuh.USER_ANSWER_FILE);
        if (!((userAnswerFileName != null && !userAnswerFileName.isEmpty()) && FileUtils.checkFileExist(new File(userAnswerFileName), false))){
            return getAnswerFilePath();
        }
        return new File(userAnswerFileName).getAbsolutePath();
    }

    private static void setAnswerFilesParameters ()
    {
        String answerFile = getAnswerFilePath();
        String userAnswerFile = getUserAnswerFilePath();
        map.put(CommonParameters.Iuh.ANSWER_FILE, answerFile);
        map.put(CommonParameters.Iuh.USER_ANSWER_FILE, userAnswerFile);
        log.debug "answer permission changed = " + FileUtils.changeFilePermission(new File (answerFile), FileUtils.Permission.WRITE, false);
        log.debug "User answer permission changed = " + FileUtils.changeFilePermission(new File (userAnswerFile), FileUtils.Permission.WRITE, false);

    }

    private static void getArgsParams(String[] args) {
        Map<String, String> eligibleArgs = new HashMap<String, String>();
        // Если есть параметры командной строки - сохраняем их мап
        if(args != null) args.each {
            if(it != null && it.startsWith('-D')) {
                def cleanParam = it.replaceFirst('-D', '')
                //Pattern p = Pattern.compile('^(\\w+)=(.*)\$');
                Pattern p = Pattern.compile('^([-a-zA-Z0-9_\\.]+)=(.*)\$')
                Matcher matcher = p.matcher(cleanParam)
                if(matcher.find()) {
                    eligibleArgs.put(matcher[0][1], matcher[0][2])
                }
            }
        }
        log.debug 'Loaded run parameters from command line:' + eligibleArgs
        map.putAll(eligibleArgs);
    }

    static class HarnessSignalHandler implements SignalHandler {

        void handle(Signal sig) {
            interruptedObject.setFlag(true);
            sleep 1000
            /*while(!interruptedObject.isFinished()) {
                sleep 100
            }*/

            def suggestion = '\nExecution suspended. Enter \'n\' to continue execution or enter \'y\' to terminate execution definitively: '
            print suggestion
            String str;
            def variants = ['y', 'Y', 'n', 'N']
            while(!variants.contains (str = scanner.nextLine())) {
                print suggestion
            }
            if(str == variants[0] || str == variants[1]) {
                //Runtime.runtime.halt(0)
                while(!interruptedObject.isFinished()) {
                    sleep 100
                }
                System.exit(0)
            }
            if(str == variants[2] || str == variants[3]) {
                interruptedObject.setFlag(false);
                //System.out.print()
            }
        }
    }


    //-------------------------------------------------------------------------------

    /*private static void getInteractiveParam(String paramName, String prompt) {
        if(map.get(paramName) == null) {
            log.info 'Enter value of param ' + paramName + (prompt != null ? ' ' + prompt : '') + ': '
            map.put(paramName, scanner.nextLine())
        }
    }

    private static void getInteractiveParam(String paramName) {
        getInteractiveParam(paramName, null)
    }*/

}
