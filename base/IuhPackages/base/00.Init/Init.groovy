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
import ru.datateh.jbr.iuh.init.AbstractInit
import ru.datateh.jbr.iuh.parameters.CommonParameters
import ru.datateh.jbr.iuh.utils.FileUtils
import ru.datateh.jbr.iuh.utils.PropertiesUtils

import java.util.regex.Matcher

@Log4j
public class Init extends AbstractInit {
	def JBOSS_CONTROL_SCRIPT = "/etc/init.d/jboss"
	def BUILD_INFO_FILE = "conf/dbmi/build-info.properties"
	def BR4J_BUILD_PARAMETER = "build.number"
	
	void init() {
		def errMsg = force ? "There is FORCED mode, try continue" : "There is NOT FORCED mode, exitting"
		
	// Получаем системные свойства
		getSystemParams()
		
	// Получаем вычисляемые свойства
		log.info "Checking JBoss location..."
		if (!getJBossLocation()){
			log.error "No JBoss location specified ! That\'s CRITICAL ! ${errMsg}"
			if (!force){
				System.exit(1)
			}
		} else {
			log.info "...DONE!"
		}

	//Имя рабочей конфигурации JBoss
		log.info "Checking JBoss work configuration ..."
		if (!getJBossConfiguration()){
			log.error "No JBoss work configuration specified ! That\'s CRITICAL ! ${errMsg}"
			if (!force){
				System.exit(1)
			}
		} else {
			log.info "...DONE!"
		}
	//Полный путь до рабочей конфигурации JBoss
		map.put(CommonParameters.System.JBOSS_CONFIGURATION_PATH,
				"${map.get(CommonParameters.System.JBOSS_HOME)}/server/${map.get(CommonParameters.System.JBOSS_CONFIGURATION)}")

		// Версия выпуска BR4J
		log.debug "Getting BR4J version..."
		def buildNumber = getParameterFromPropertyFile(BR4J_BUILD_PARAMETER, "${map.get(CommonParameters.System.JBOSS_CONFIGURATION_PATH)}/${BUILD_INFO_FILE}")
		if (buildNumber == null){
			log.error "Couldn\'t find parameter \'${BR4J_BUILD_PARAMETER}\' in file ${BUILD_INFO_FILE}. That\'s CRITICAL ! ${errMsg}"
			if (!force){
				System.exit(1)
			}
		} else {	//параметр определён
			if (!buildNumber?.trim()){ // null or empty or blank
				log.error "Parameter \'${BR4J_BUILD_PARAMETER}\' in file ${BUILD_INFO_FILE} is empty. That\'s CRITICAL ! ${errMsg}"
				if (!force){
					System.exit(1)
				}
			} else {
				map.put(CommonParameters.System.DBMI_BUILD_NUMBER, buildNumber);
				log.info "...DONE"
			}
		}
		
	// Проверяем аккаунт пользователя под которым будем запускать наборы скриптов
		log.debug "Checking executor account..."
		String executor = map.get(CommonParameters.Iuh.EXECUTOR_USER_NAME);
		if (executor != null && !executor.equalsIgnoreCase("null")){
			log.debug "...use preset executor account: \'${executor}\'"
		} else {
			executor = map.get("iuh.starter.user.name")
			log.debug "...no preset executor account, use current: \'${executor}\'"
		}
		log.debug "...checking \'${executor}\' account existence"
		if (checkAccount(executor)){
			log.debug "...DONE"
		} else {
			log.error "Executor account not specified ! That\'s CRITICAL ! ${errMsg}"
			if (!force){
				System.exit(1)
			}
		}
		if (executor != null && !executor.equalsIgnoreCase("null")) {
			map.put(CommonParameters.Iuh.EXECUTOR_USER_NAME, executor)
        }

	// Распечатка карты параметров
		def out = new StringBuffer()
		out << "Current parameters:\n"
		map.sort{it.key}.each{ k, v -> out << "${k}:".padRight(35) << "${v}" << "\n"}
		log.info out.toString()
	}
	
	private void getSystemParams() {
		log.debug "Collecting OS parameters..."
	// Модель ОС
		map.put(CommonParameters.System.OS_NAME, System.getProperty('os.name'))
	// Версия ОС
		map.put(CommonParameters.System.OS_VERSION, System.getProperty('os.version'))
	// Архитектура ОС
		map.put(CommonParameters.System.OS_ARCH, System.getProperty('os.arch'))
	// IP сервера
		log.debug "...collect all inet addresses on all inet intefaces..."
		def tmpS = new StringBuffer() 
		NetworkInterface.getNetworkInterfaces().each{ iface ->  // по всем сетевым интерфейсам...
			iface.inetAddresses.each{ addr -> //... берём каждый адрес...
				if (addr in java.net.Inet4Address){ // берём только IPv4 адреса
					tmpS << addr.hostAddress << ' ' //...собираем в строку с пробелами
				}
			}
		}
		map.put(CommonParameters.System.OS_HOST_ADRESS, tmpS.toString().trim())
		log.trace "......done"
	// Имя сервера. Берётся только hostname на loopback так как брать все имена на всех интерфейсах - оч. долго
		map.put(CommonParameters.System.OS_HOST_NAME, InetAddress.getLocalHost().getHostName())
		log.debug "...DONE"
	}

	private boolean getJBossLocation() {
	// Определяем расположение JBoss 3-мя способами последовательно:
	// 1.Установлена переменная br4j.jboss.home в файле конфигурации оснастки или в командной строке
	// 2.На основе данных управляющего скрипта (только для Linux-Unix)
	// 3.На основе данных переменной JBOSS_HOME
		if(!map.get(CommonParameters.System.JBOSS_HOME)) {
			log.debug "...${CommonParameters.System.JBOSS_HOME} was not set in file iuh.properties nor via command line"
	// ---- Способ №2 -----
			if(!map.get(CommonParameters.System.OS_NAME).startsWith('Win')) { // не только лишь для Windows
				log.debug "...checking file ${JBOSS_CONTROL_SCRIPT}"
				File f = new File(JBOSS_CONTROL_SCRIPT);
				if(f.isFile()){
					log.debug "...file ${JBOSS_CONTROL_SCRIPT} found"
					Matcher m = f.getText('UTF-8') =~ '(?m)^[^#].*JBOSS_HOME:-"([-a-zA-Z0-9_/.]+)"}'
					if(m.find()) {
						map.put(CommonParameters.System.JBOSS_HOME, m.group(1))
						log.debug "...found \'JBOSS_HOME\' in file ${JBOSS_CONTROL_SCRIPT}!"
					} else {
						log.warn "...\'JBOSS_HOME\' not found in ${JBOSS_CONTROL_SCRIPT}!"
					}
				} else {
					log.debug "...file ${JBOSS_CONTROL_SCRIPT} not found"
				}
			}
			if (!map.get(CommonParameters.System.JBOSS_HOME)){  // 2 предыдущих способо не сработали
	// ---- Способ №3 -----
				log.debug "...checking  environment variable \'JBOSS_HOME\'..."
				if (!System.getenv('JBOSS_HOME')){
					map.put(CommonParameters.System.JBOSS_HOME, System.getenv('JBOSS_HOME'))
					log.debug "......got \'JBOSS_HOME\': ${map.get(CommonParameters.System.JBOSS_HOME)}"
				} else {
					log.debug "......not found  environment variable \'JBOSS_HOME\'!"
				}
			}
		} else {
			log.debug "...use preset value for parameter ${CommonParameters.System.JBOSS_HOME}"
		}
		if (!map.get(CommonParameters.System.JBOSS_HOME)){ // ни один из способов определения не сработал
			log.debug "...couldn\'t discover JBoss server location !"
			return false
		}
		return true;
	}

	private boolean getJBossConfiguration() {
	// Определяем расположение рабочей конфигурации JBoss 3-мя способами последовательно:
	// 1.Установлена переменная br4j.jboss.configuration в файле конфигурации оснастки или в командной строке
	// 2.На основе данных управляющего скрипта (только для Linux-Unix)
	// 3.Устанавливаем значение 'server/default' -- беспроигрышный вариант )
		if(!map.get(CommonParameters.System.JBOSS_CONFIGURATION)) {
			log.debug "...${CommonParameters.System.JBOSS_CONFIGURATION} was not set in file iuh.properties nor via command line"
	// ---- Способ №2 -----
			if(!map.get(CommonParameters.System.OS_NAME).startsWith('Win')) { // не только лишь для Windows
				log.debug "...checking file ${JBOSS_CONTROL_SCRIPT}"
				File f = new File(JBOSS_CONTROL_SCRIPT);
				if(f.isFile()){
					log.debug "...file ${JBOSS_CONTROL_SCRIPT} found"
					Matcher m = f.getText('UTF-8') =~ '(?m)^[^#].*JBOSS_CONF:-"([-a-zA-Z0-9_.]+)"}'
					if(m.find()) {
						map.put(CommonParameters.System.JBOSS_CONFIGURATION, m.group(1))
						log.debug "...found \'JBOSS_CONF\' in file ${JBOSS_CONTROL_SCRIPT}!"
					} else {
						log.warn "...\'JBOSS_CONF\' not found in ${JBOSS_CONTROL_SCRIPT}!"
					}
				} else {
					log.debug "...file ${JBOSS_CONTROL_SCRIPT} not found"
				}
			}
			if (!map.get(CommonParameters.System.JBOSS_CONFIGURATION)){  // 2 предыдущих способо не сработали
	// ---- Способ №3 -----
				log.debug "...setting ${CommonParameters.System.JBOSS_CONFIGURATION} to default value: \'default\'"
				map.put(CommonParameters.System.JBOSS_CONFIGURATION, 'default')
			}
		} else {
			log.debug "...use preset value for parameter ${CommonParameters.System.JBOSS_CONFIGURATION}"
		}
		return true
	}
	
	private boolean checkAccount(login) {
		if(map.get(CommonParameters.System.OS_NAME).startsWith('Win')) {
			def process = ("runas /savecred /user:${map.get(CommonParameters.System.OS_HOST_NAME)}\\${login} \"echo 1\"").execute()
			process.waitFor()
			def err = process.err.text
			def out = process.text
			def exitCode = process.exitValue() // echo %errorlevel%
			log.debug "...exit code:${exitCode}, process out:\'${out}\'"
			if(exitCode == 0) {
				return true
			}else{
				log.error "User account ${login} does not exists!"
				return false
			}
		} else {  // Unix-like OS
			def cmd = "getent passwd ${login}"
			log.debug "...executing \'${cmd}\'"
			def process = cmd.execute()
			process.waitFor()
			def out = process.text
			def exitCode = process.exitValue() // echo $?
			log.debug "...exit code:${exitCode}, process out:\'${out}\'"
			if(exitCode == 0) {
				return true
			}else{
				log.error "User account ${login} does not exists!"
				return false
			}
		}
	}
	
	String getParameterFromPropertyFile(String paramName, String fileName){
		File file = new File(fileName)
		log.debug "...getting property \'${paramName}\' from file ${fileName}..."
		if (!FileUtils.checkFileExist(file, false)){
			log.error "...file ${fileName} not found!"
			return null // CRITICAL
		}
		log.debug "...file found"
		String paramValue = PropertiesUtils.readPropertyFromFile(file,paramName);
		if (!(paramValue)?.trim()){
			log.warn "...parameter \'${paramName}\' not found in file ${fileName}!"
			return null // CRITICAL
		}
		return paramValue;
	}
	
	void errorHandling(String msg) {
		if(!force) {
			log.error msg
			System.exit(1)
		}
		log.warn msg
	}
	
	void errorHandling(int msg) {
		errorHandling(String.valueOf(msg))
	}
	
	static void main(String[] args) {
		new Init().start()
	}
}