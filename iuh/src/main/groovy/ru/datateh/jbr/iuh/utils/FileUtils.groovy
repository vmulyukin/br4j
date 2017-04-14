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
import ru.datateh.jbr.iuh.GroovyWrapper
import ru.datateh.jbr.iuh.msg.HarnessException
import ru.datateh.jbr.iuh.msg.Message
import ru.datateh.jbr.iuh.msg.MessageType

import java.nio.charset.Charset

public class FileUtils {

    public static enum Permission {
        READ,
        WRITE;
    }

    static String LOG_CONFIG = "log4j.groovy";

    /**
     * Удаляет указанный файл
     * @param fileName имя файла
     * @return возвращает true, если операция была успешна, иначе false
     */
	public static boolean deleteFile(String fileName) {
		File f = new File(fileName);
		if(f.isFile()) {
			return f.delete()
		}
        return false;
	}

    /**
     * Изменяет владельца файла логирования на POSIX системах
     * @param owner новый владелец файла
     * @return true, если владелец успешно изменен, иначе false
     */
    public static boolean changeLogFileOwnerOnNixSystem (String owner)
    {
        if ((owner != null && !owner.equalsIgnoreCase("null")) || (owner != null && !owner.isEmpty())) {
            try {
                Properties configProperties = new ConfigSlurper().parse(getLogConfigFile().toURI().toURL()).toProperties()
                String logFileName = configProperties.getProperty("log4j.appender.scrlog.file");
                if (logFileName != null) {
                    String touch = "touch ${logFileName}";
                    String chown = "chown ${owner} ${logFileName}";
                    String osName = System.getProperty('os.name');
                    if (!osName.startsWith('Win'))
                    {
                        Process cmd_touch = Runtime.getRuntime().exec(touch);
                        cmd_touch.waitFor();
                        Process cmd_chown = Runtime.getRuntime().exec(chown);
                        cmd_chown.waitFor();
                    }
                    return true;
                }
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    public static boolean changeFilePermission (File file, Permission permission, boolean forOwner)
    {
        checkFileExist(file, true);
        boolean result;
        switch (permission)
        {
            case Permission.READ:
                result = file.setReadable(true, forOwner);
                break;
            case Permission.WRITE:
                result = file.setWritable(true, forOwner);
                break;
        }
        return result;
    }

    /**
     * копирует исходный файл, если он присутствует, в файл назначения
     * @param sourceFile исходный файл
     * @param destinationFile файл назначения
     */
    public static void copyFile(File sourceFile, File destinationFile)
    {
        InputStream is = null;
        OutputStream os = null;
        checkFileExist(sourceFile, true);
        try {
            is = new FileInputStream(sourceFile);
            os = new FileOutputStream(destinationFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } finally {
            is.close();
            os.close();
        }
    }

    /**
     * Проверяет существует ли указанный файл
     * @param file файл
     * @param throwException если true, генерирует исключение {@link ru.datateh.jbr.iuh.msg.HarnessException}
     * @return возвращает true, если файл существует и false если файл не существует и throwException = false, или параметр file = null
     */
    public static boolean checkFileExist (File file, boolean throwException)
    {
        if (file != null) {
            if (!file.isFile() && throwException)
            {
                throw new HarnessException(new Message(MessageType.ERROR, "Can not find file: " + file));
            }
            else if (!file.isFile() && !throwException)
            {
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Проверяет существует ли указанная директория
     * @param directory директория
     * @param throwException если true, генерирует исключение {@link ru.datateh.jbr.iuh.msg.HarnessException}
     * @return возвращает true, если директория существует и false если директория не существует и throwException = false, или параметр file = null
     */
    public static boolean checkDirectoryExist (File directory, boolean throwException)
    {
        if (directory != null) {
            if (!directory.isDirectory() && throwException)
            {
                throw new HarnessException(new Message(MessageType.ERROR, "Can not find directory: " + directory));
            }
            else if (!directory.isDirectory() && !throwException)
            {
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * читает указанный файл в кодироваке UTF-8 и возвращает список строк
     * @param file файл
     * @return список строк файла
     */
    public static List<String> readLines(File file)
    {
        checkFileExist(file, true);
        List<String> result = new ArrayList<String>();
        String line;
        try {
            InputStream fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"));
            BufferedReader br = new BufferedReader(isr);
            while ((line = br.readLine()) != null) {
                result.add(line);
            }
        } catch (Exception e) {
            throw new HarnessException(new Message(MessageType.ERROR, "Error while reading file " + file, e));
        }
        return result;
    }

    /**
     * Сохраняет список строк в кодировке UTF-8 в файл
     * @param file имя файла
     * @param list список строк
     */
    public static void storeLines(File file, List<String> list)
    {
        PrintWriter writer = new PrintWriter(file, "UTF-8");
        for (String line : list)
        {
            writer.println(line);
        }
        writer.close();
    }

    /**
     * Возвращает файл настроек логирования
     * @return файл настроек логирования
     */
    public static File getLogConfigFile ()
    {
        File logConfig = new File(LOG_CONFIG);
        if (checkFileExist(logConfig, false))
        {
            return logConfig;
        }
        logConfig = new File(getCodeSourcePath() + File.separator + LOG_CONFIG);
        if (checkFileExist(logConfig, false))
        {
            return logConfig;
        }
        return null;
    }

    /**
     * Возвращает путь к папке в которой расположен jar-файл оснастки
     * @return путь к папке в которой расположен jar-файл оснастки
     */
    public static String getCodeSourcePath ()
    {
        GroovyWrapper wrapper = new GroovyWrapper();
        File sourcePath = new File (wrapper.getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
        return sourcePath.getParent();
    }

}
