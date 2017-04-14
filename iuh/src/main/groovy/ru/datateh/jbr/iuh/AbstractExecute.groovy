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
import groovy.util.logging.Log4j
import org.apache.log4j.PropertyConfigurator
import ru.datateh.jbr.iuh.msg.HarnessException
import ru.datateh.jbr.iuh.msg.Message
import ru.datateh.jbr.iuh.msg.MessageType
import ru.datateh.jbr.iuh.parameters.CommonParameters
import ru.datateh.jbr.iuh.utils.AccessRulesUtils
import ru.datateh.jbr.iuh.utils.FileUtils
import ru.datateh.jbr.iuh.utils.IuhUtils
import ru.datateh.jbr.iuh.utils.PropertiesUtils
import ru.datateh.jbr.iuh.utils.SQLUtils

@Log4j
abstract class AbstractExecute implements Phases {
	
	protected static Map<String, String> map

    private static final String CODE_WORD = "2734b4f0-86ec-40bb-b62c-94681a7cc95c";
	
	protected boolean force
	protected String run
	
	protected Message msg
	

	public void init() {}
	
	protected void initScript() {
		try {

			init()
				
		} catch(Exception e) {
			if(msg == null) {
				msg = new Message(MessageType.ERROR, e)
			}
			if(!force) {
				throw new Exception(e);
			}
		}
		
	}
	

	public boolean check() {
		return true
	}
	

	protected boolean checkScript() {
		
		initScript()
		
		try {
			
			if(!check()) {
				return false
			}
			return true
		} catch(Exception e) {
			if(msg == null) {
				msg = new Message(MessageType.ERROR, e)
			}
			if(!force) {
				throw new Exception(e);
			}
			return false;
		}
		
	}
	

	public void install() {
		
	}
	

	protected void installScript() {
		
		if(!checkScript()) {
			msg = new Message(MessageType.REJECT, 'Script: ' + getParam(CommonParameters.Iuh.CURRENT_SCRIPT_PATH)
					+ this.getClass().getName() + ' has been rejected, maybe repeating')
			return
		}
		
		try {
			
			install()
				
		}
        catch (HarnessException e)
        {
            if(msg == null) {
                msg = e.getHarnessMessage();
            }
            if(!force) {
                throw new Exception(e);
            }
        }
        catch(Exception e) {
			if(msg == null) {
				msg = new Message(MessageType.ERROR, e)
			}
			if(!force) {
				throw new Exception(e);
			}
		}
		
	}
	

	public void uninstall() {}
	

	protected void uninstallScript() {
		
		checkScript()
		
		uninstall()
	}
	
	
	public void finalize() {
		if(msg != null) {
			setParam("msgKey" , msg.getState().getText());
			if(msg.text != null) {
				setParam('msgVal', msg.text.size() > 1024 ? msg.text.substring(0, 1024).replaceAll('\n', '') : msg.text.replaceAll('\n', ''))
			}
		}
		IuhUtils.mapToSharedFile(map)
	}
	
	
	protected void start() {
		// ���������� ������������
        def config = new ConfigSlurper().parse(FileUtils.getLogConfigFile().toURI().toURL());
		PropertyConfigurator.configure(config.toProperties())
		
		map = IuhUtils.sharedFileToMap()
		force = getParam(CommonParameters.Iuh.MODE_FORCE) ? Boolean.valueOf(getParam(CommonParameters.Iuh.MODE_FORCE)) : false
		run = getParam(CommonParameters.Iuh.MODE_RUN).trim();

		setParam(CommonParameters.Iuh.ANSWERS_PREFIX, getAnswerPrefix());
		setParam(CommonParameters.Iuh.CURRENT_SCRIPT_PATH, getScriptPath());
		
		try {
			if('install'.equalsIgnoreCase(run)) {
				installScript()
			} else if('check'.equalsIgnoreCase(run)) {
				checkScript()
			} else if('init'.equalsIgnoreCase(run)) {
				initScript()
			}  else {
				throw new IllegalArgumentException('Illegal mode value: ' + run)
			}
		} catch(Exception e) {
			log.error 'Error during the execution of the script: ' + e.getMessage()
			log.error 'Execution will continue if the parameter FORCE equals TRUE'
		}

		finalize()
	}

    /**
     * Возвращает значение свойства.<p>
     * Последовательность поиска значения:<p>
     * 1. карта свойств;<p>
     * 2. общий файл;<p>
     * 3. если включен режим интерактивности, запрашивает у пользователя и сохраняет его в общий файл;<p>
     * 4. значение по умолчанию.<p>
     * @param propertyName - имя свойства
     * @param defaultValue - значение свойства по умолчанию
     * @return значение свойства, если не найдено генерируется исключение {@link ru.datateh.jbr.iuh.msg.HarnessException}
     */
    protected static String getPropertyValue(String propertyName, String defaultValue)
    {
		boolean isInteractive = Boolean.parseBoolean(getParam(CommonParameters.Iuh.MODE_INTERACTIVE));
        String result = getParam(propertyName);
        if (result != null && !result.isEmpty()){return result;}
        result = getValue(propertyName);
        if (result != null && !result.isEmpty()){return result;}
        if (isInteractive) {
            result = getUserResponse(propertyName);
            if (result != null && !result.isEmpty()){
                putValue(propertyName, result);
                return result;
            }
        }
        result = defaultValue;
        if (result != null && !result.isEmpty()){return result;}
        log.error("Can not find parameter: " + propertyName);
        throw new HarnessException(new Message(MessageType.ERROR, "Can not find parameter: " + propertyName));
    }

    /**
     * Запрашивает значение параметра у пользователя
     * @param parameterName - имя параметра
     * @return - значение параметра
     * @throws java.io.IOException
     */
    protected static String getUserResponse (String parameterName) throws IOException {
            System.out.println(CODE_WORD + ": " + parameterName);
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String parameterValue = br.readLine();
            return  parameterValue;
    }

    /**
     * Проверяет значение указанного свойства в файле свойств, в случае отличия запрашивает у пользователя разрешение
     * на изменение значения, и изменяет при положительном ответе.
     * @param propertyFile - файл свойств
     * @param propertyName - имя свойства
     * @param propertyNewValue - новое значение свойства
     * @param comment - комментарий добавляемый при изменении значения свойства
     * @return true если значение свойства было изменено, иначе false
     */
    protected static boolean  updatePropertyWithConfirm (File propertyFile, String propertyName, String propertyNewValue, String comment)
    {
        String propertyCurrentValue = PropertiesUtils.readPropertyFromFile(propertyFile, propertyName);
        if (!propertyCurrentValue.equals(propertyNewValue))
        {
            String confirm = getUserResponse("Current property: " + propertyName + " has value: " + propertyCurrentValue
            + ". Do you want update this property with value: " + propertyNewValue + "? y/n?");
            if ("y".equalsIgnoreCase(confirm))
            {
                PropertiesUtils.updatePropertyAndSave(propertyFile, propertyName ,propertyNewValue, comment);
                return true;
            }
        }
        return false;
    }

    /**
     * Проверяет наличие указанного свойства в файле свойств, в случае отсутствия запрашивает у пользователя разрешение
     * на добаление свойства, и добавляет при положительном ответе.
     * @param propertyFile файл свойств
     * @param propertyName имя свойства
     * @param propertyValue значение свойства
     * @param comment комментарий добавляемый при добавлении свойства
     * @return true если значение свойства было изменено, иначе false
     */
    protected static boolean  createPropertyWithConfirm (File propertyFile, String propertyName, String propertyValue, String comment)
    {
        String propertyCurrentValue = PropertiesUtils.readPropertyFromFile(propertyFile, propertyName);
        if (propertyCurrentValue == null)
        {
            String confirm = getUserResponse("Property: " + propertyName + " is missing in file: " + propertyFile
                    + ". Do you want add this property with value: " + propertyValue + "? y/n?");
            if ("y".equalsIgnoreCase(confirm))
            {
                PropertiesUtils.addPropertyAndSave(propertyFile, propertyName ,propertyValue, comment);
                return true;
            }
        }
        return false;
    }

    /**
     * Проверяет наличие указанного свойства в файле свойств, в случае наличия запрашивает у пользователя разрешение
     * на удаление свойства, и удаляет при положительном ответе.
     * @param propertyFile файл свойств
     * @param propertyName имя свойства
     * @param comment строка комментарий, которая будет удалена из файла свойств, если будет найдена
     * @return true если значение свойства было изменено, иначе false
     */
    protected static boolean  deletePropertyWithConfirm (File propertyFile, String propertyName, String comment)
    {
        String propertyCurrentValue = PropertiesUtils.readPropertyFromFile(propertyFile, propertyName);
        if (propertyCurrentValue != null)
        {
            String confirm = getUserResponse("Property: " + propertyName + " is present in file: " + propertyFile
                    + ". Do you want delete this property? y/n?");
            if ("y".equalsIgnoreCase(confirm))
            {
                PropertiesUtils.deletePropertyAndSave(propertyFile, propertyName, comment);
                return true;
            }
        }
        return false;
    }

	/**
	 * Выполняет частичное обновление прав доступа
	 * @param fileName имя файля, содержащего информацию об обновляемых правах
	 * @return сообщение об успешности или ошибочности операции
	 */
	protected static Message performPartialUpdateAccessRules (String fileName)
	{
		Message result =  AccessRulesUtils.performPartialUpdate(map, fileName);
		if (result.getState() != MessageType.SUCCESS)
		{
			throw new HarnessException(result);
		}
		return result;
	}
	
	protected static void setParam(String name, String value) {
		map.put(name, value)
	}
	
	
	protected static String getParam(String name) {
		map.get(name)
	}

	/**
	 * Записывает пару ключ значение в файл ответов
	 * @param parameterName имя параметра
	 * @param parameterValue значение параметра
	 */
	protected static void putValue(String parameterName, String parameterValue) {
		String userAnswersFileName = getParam(CommonParameters.Iuh.USER_ANSWER_FILE);
		File answerFile = new File(userAnswersFileName);
		log.debug ("Write parameter " + parameterName + " with value " + parameterValue + " in file " + userAnswersFileName);
		boolean isInteractive = Boolean.parseBoolean(getParam(CommonParameters.Iuh.MODE_INTERACTIVE));
		if (isInteractive && (userAnswersFileName != null && !userAnswersFileName.isEmpty()))
		{
			if (FileUtils.checkFileExist(answerFile, false)) {
				log.debug("Write parameter : " + parameterName + " with value: " + parameterValue + " in answer file: " + userAnswersFileName);
				PropertiesUtils.addPropertyAndSave(answerFile, getParam(CommonParameters.Iuh.ANSWERS_PREFIX) + "." + parameterName, parameterValue, null);
				log.debug ("Writing parameter success")
			}
		}
	}

	/**
	 * Читает значение указанного параметра из файла ответов
	 * @param parameterName имя параметра
	 * @return значение параметра
	 */
	protected static String getValue(String parameterName) {

        File answerFile = new File(getParam(CommonParameters.Iuh.ANSWER_FILE));
        File userAnswerFile = new File(getParam(CommonParameters.Iuh.USER_ANSWER_FILE));
        log.debug ("Read parameter " + getParam(CommonParameters.Iuh.ANSWERS_PREFIX) + "." + parameterName + " from file: " + answerFile);
        String answerValue = PropertiesUtils.readPropertyFromFile(answerFile, getParam(CommonParameters.Iuh.ANSWERS_PREFIX) + "." + parameterName);
        log.debug ("Read parameter " + getParam(CommonParameters.Iuh.ANSWERS_PREFIX) + "." + parameterName + " from file: " + userAnswerFile);
        String userAnswerValue =  PropertiesUtils.readPropertyFromFile(userAnswerFile, getParam(CommonParameters.Iuh.ANSWERS_PREFIX) + "." + parameterName);

        if (userAnswerValue != null && !userAnswerValue.isEmpty()){
            return userAnswerValue;
        }
        if (answerValue != null && !answerValue.isEmpty()){
            return answerValue;
        }
        return null;
	}

	/**
	 * Выполнение sql-запроса
	 * @param sql sql-запрос
	 * @return сообщение об успешности или ошибочности операции
	 */
	protected static Message execSql(String sql) {
		Message result =  SQLUtils.execSql(map, sql);
		if (result.getState() != MessageType.SUCCESS)
		{
			throw new HarnessException(result);
		}
		return result;
	}

	/**
	 * Выполнение sql-скрипта из файла
	 * @param file имя файла. Файл должен находтся в папке packageName/data
	 * @return сообщение об успешности или ошибочности операции
	 */
	protected static Message execSqlFromFile(String file) {
		return SQLUtils.execSqlFromFile(map, file);
	}

    private String getAnswerPrefix ()
    {
        String className = this.getClass().getName();
        File scriptPath = new File (this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
        File suitePath = new File (scriptPath.getParent());
        String packageName = scriptPath.getName();
        String suiteName = suitePath.getName();
        String prefix = suiteName + "." + packageName + "." + className;
        return prefix;
    }

	private String getScriptPath ()
	{
		File scriptPath = new File (this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
		return scriptPath.getAbsolutePath();
	}

}
