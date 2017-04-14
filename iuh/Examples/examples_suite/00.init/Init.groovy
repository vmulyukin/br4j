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
import ru.datateh.jbr.iuh.groovy.init.AbstractInit;
import static ru.datateh.jbr.iuh.groovy.utils.StringUtils.*;

import groovy.io.FileType
import groovy.util.logging.*;

import java.io.File;
import java.net.*
import java.util.Map;
import java.util.regex.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.*

import org.apache.log4j.*
import org.w3c.dom.Document

@Log4j
public class Init extends AbstractInit {
	def JBOSS_CONTROL_SCRIPT = "/etc/init.d/jboss"
	
	def FILE_STORAGE_CONFIG = "conf/dbmi/solr/solrModule.properties"
	def PDF_CONVERTOR_CONFIG = "conf/dbmi/openoffice/pdfConvertor.properties"
	
	def FILESTORAGE_CACHE_NAME = "cache"
	def CONVERTOR_CACHE_PARAMETER = "convertor.cache.storage"
	def FILESTORAGE_ROOT_PARAMETER = "store.rootLocation"
	def FILESTORAGE_CACHE_PARAMETER = "store.cacheRootLocation"
	def BUILD_INFO_FILE = "conf/dbmi/build-info.properties"
	def SOLR_PARAMETER = "solrDir"
	def PDF_CACHE_PARAMETER = "pdfCacheDir"
	
	def BR4J_BUILD_PARAMETER = "build.number"
	
	final static String EXECUTOR_PARAMETER = "iuh.executor.user.name"
	def DBMI_DATASOURCE_NAME = "jdbc/DBMIDS"
	def DBMI_EVENT_DATASOURCE_NAME = "jdbc/DBMIDS_EVENT"
	def PORTAL_DATASOURCE_NAME = "PortalDS"
	
	void init() {
//        log.info "force = " + force +  " iuh.mode.force = " + map.get('iuh.mode.force')
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
		map.put('br4j.jboss.configuration.path', "${map.get('br4j.jboss.home')}/server/${map.get('br4j.jboss.configuration')}")
		
		
/*	//Параметры файлового хранилища BR4J
		log.info "Checking BR4J file storage locations..."
		if (!setFilestoreParameters()){
			log.error "There are errors in BR4J file storage configuration ! That\'s CRITICAL ! ${errMsg}"
			if (!force){
				System.exit(1)
			}
		} else {
			log.info "...DONE!"
		}*/

		// Версия выпуска BR4J
		log.debug "Getting BR4J version..."
		def buildNumber = getParameterFromPropertyFile(BR4J_BUILD_PARAMETER, "${map.get('br4j.jboss.configuration.path')}/${BUILD_INFO_FILE}")
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
				map.put('br4j.dbmi.build.number', buildNumber)
				log.info "...DONE"
			}
		}

//		setDBParameters(force)
		
	// Проверяем аккаунт пользователя под которым будем запускать наборы скриптов
		log.debug "Checking executor account..."
		String executor = map.get(EXECUTOR_PARAMETER);
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
            map.put(EXECUTOR_PARAMETER, executor)
        }

	// Распечатка карты параметров
		def out = new StringBuffer()
		out << "Current parameters:\n"
		map.sort{it.key}.each{ k, v -> out << "${k}:".padRight(35) << "${v}" << "\n"}
		log.info out.toString()
		
//		setParam('dbConn', 'jdbc:postgresql://127.0.0.1:5432/72')
//		setParam('dbLogin', 'enterprisedb')
//		setParam('dbPass', '123')
	}
	
	private void getSystemParams() {
		log.debug "Collecting OS parameters..."
	// Модель ОС
		map.put('os.name', System.getProperty('os.name'))
	// Версия ОС
		map.put('os.version', System.getProperty('os.version'))
	// Архитектура ОС
		map.put('os.arch', System.getProperty('os.arch'))
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
		map.put('os.host.address', tmpS.toString().trim())
		log.trace "......done"
	// Имя сервера. Берётся только hostname на loopback так как брать все имена на всех интерфейсах - оч. долго
		map.put('os.host.name', InetAddress.getLocalHost().getHostName())
		log.debug "...DONE"
	}

	private boolean getJBossLocation() {
	// Определяем расположение JBoss 3-мя способами последовательно:
	// 1.Установлена переменная br4j.jboss.home в файле конфигурации оснастки или в командной строке
	// 2.На основе данных управляющего скрипта (только для Linux-Unix)
	// 3.На основе данных переменной JBOSS_HOME
		if(!map.get('br4j.jboss.home')) {
			log.debug "...\'br4j.jboss.home\' was not set in file iuh.properties nor via command line"
	// ---- Способ №2 -----
			if(!map.get('os.name').startsWith('Win')) { // не только лишь для Windows
				log.debug "...checking file ${JBOSS_CONTROL_SCRIPT}"
				File f = new File(JBOSS_CONTROL_SCRIPT);
				if(f.isFile()){
					log.debug "...file ${JBOSS_CONTROL_SCRIPT} found"
					Matcher m = f.getText('UTF-8') =~ '(?m)^[^#].*JBOSS_HOME:-"([-a-zA-Z0-9_/.]+)"}'
					if(m.find()) {
						map.put('br4j.jboss.home', m.group(1))
						log.debug "...found \'JBOSS_HOME\' in file ${JBOSS_CONTROL_SCRIPT}!"
					} else {
						log.warn "...\'JBOSS_HOME\' not found in ${JBOSS_CONTROL_SCRIPT}!"
					}
				} else {
					log.debug "...file ${JBOSS_CONTROL_SCRIPT} not found"
				}
			}
			if (!map.get('br4j.jboss.home')){  // 2 предыдущих способо не сработали
	// ---- Способ №3 -----
				log.debug "...checking  environment variable \'JBOSS_HOME\'..."
				if (!System.getenv('JBOSS_HOME')){
					map.put('br4j.jboss.home', System.getenv('JBOSS_HOME'))
					log.debug "......got \'JBOSS_HOME\': ${map.get('br4j.jboss.home')}"
				} else {
					log.debug "......not found  environment variable \'JBOSS_HOME\'!"
				}
			}
		} else {
			log.debug "...use preset value for parameter \'br4j.jboss.home\'"
		}
		if (!map.get('br4j.jboss.home')){ // ни один из способов определения не сработал
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
		if(!map.get('br4j.jboss.configuration')) {
			log.debug "...\'br4j.jboss.configuration\' was not set in file iuh.properties nor via command line"
	// ---- Способ №2 -----
			if(!map.get('os.name').startsWith('Win')) { // не только лишь для Windows
				log.debug "...checking file ${JBOSS_CONTROL_SCRIPT}"
				File f = new File(JBOSS_CONTROL_SCRIPT);
				if(f.isFile()){
					log.debug "...file ${JBOSS_CONTROL_SCRIPT} found"
					Matcher m = f.getText('UTF-8') =~ '(?m)^[^#].*JBOSS_CONF:-"([-a-zA-Z0-9_.]+)"}'
					if(m.find()) {
						map.put('br4j.jboss.configuration', m.group(1))
						log.debug "...found \'JBOSS_CONF\' in file ${JBOSS_CONTROL_SCRIPT}!"
					} else {
						log.warn "...\'JBOSS_CONF\' not found in ${JBOSS_CONTROL_SCRIPT}!"
					}
				} else {
					log.debug "...file ${JBOSS_CONTROL_SCRIPT} not found"
				}
			}
			if (!map.get('br4j.jboss.configuration')){  // 2 предыдущих способо не сработали
	// ---- Способ №3 -----
				log.debug "...setting \'br4j.jboss.configuration\' to default value: \'default\'"
				map.put('br4j.jboss.configuration', 'default')
			}
		} else {
			log.debug "...use preset value for parameter \'br4j.jboss.configuration\'"
		}
		return true
	}

/*	private boolean setFilestoreParameters(force) {
		boolean retValue = true
	// Местоположение основного (root) раздела файлового хранилища
		if (!map.get(SOLR_PARAMETER)){
			log.debug "...getting parameter \'${FILESTORAGE_ROOT_PARAMETER}\'..."
			def currPath = getParameterFromPropertyFile(FILESTORAGE_ROOT_PARAMETER, 
					"${map.get('br4j.jboss.configuration.path')}/${FILE_STORAGE_CONFIG}")
			if (currPath == null){ 
				log.warn "...couldn\'t find parameter \'${FILESTORAGE_ROOT_PARAMETER}\'. That\'s CRITICAL ! ${errMsg}"
				if (!force) 
					return false
				retValue = false
			} else {	//параметр определён
				if (!currPath?.trim()){ // null or empty or blank
					log.warn "...parameter \'${FILESTORAGE_ROOT_PARAMETER}\' is empty. That\'s CRITICAL ! ${errMsg}"
				if (!force) 
					return false
					retValue = false
				} else {
					log.debug "...validating path \'${currPath}\'"
					if (!(new File("${currPath}").isDirectory())){ //проверка на валидность каталога
						log.warn "...there is invalid path: \'${currPath}\' for run parameter \'${presetParam}\'. . That\'s CRITICAL ! ${errMsg}"
						if (!force) 
							return false
						retValue = false
					} else {
						log.debug "...valid!"
					}
					map.put(SOLR_PARAMETER, currPath)
				}
			}
		} else {
			log.debug "...use preset value for parameter \'${SOLR_PARAMETER}\'"
		}

	// Проверка настройки PDF-конвертора: указание кэша сгенерированных PDF (PDF_CONVERTOR_CONFIG:convertor.cache.storage=cache)
		log.debug "...checking run parameter ${CONVERTOR_CACHE_PARAMETER}..."
		def cacheStorage = getParameterFromPropertyFile(CONVERTOR_CACHE_PARAMETER, 
				"${map.get('br4j.jboss.configuration.path')}/${PDF_CONVERTOR_CONFIG}")
		if (cacheStorage == null){ 
			log.warn "...couldn\'t find parameter \'${CONVERTOR_CACHE_PARAMETER}\'. That\'s CRITICAL ! ${errMsg}"
			if (!force) 
				return false
			retValue = false
		} else {	//параметр определён
			if (!cacheStorage?.trim()){ // null or empty or blank
				log.warn "...parameter \'${CONVERTOR_CACHE_PARAMETER}\' is empty. That\'s CRITICAL ! ${errMsg}"
				if (!force) 
					return false
				retValue = false
			} else {
				log.debug "...validating path \'${cacheStorage}\'"
				if (!cacheStorage.equals(FILESTORAGE_CACHE_NAME)){
					log.warn "...BR4j configuration ${file.getAbsolutePath()} has invalid settings (convertor.cache.storage:\'${cacheStorage}\')! ${errMsg}"
					if (!force) 
						return false
					retValue = false
				}
			}
		}
	// Местоположение хранилища кэшированных PDF-файлов
		if (!map.get(PDF_CACHE_PARAMETER)){
			log.debug "...getting parameter \'${FILESTORAGE_CACHE_PARAMETER}\'..."
			def currPath = getParameterFromPropertyFile(FILESTORAGE_CACHE_PARAMETER, 
					"${map.get('br4j.jboss.configuration.path')}/${FILE_STORAGE_CONFIG}")
			if (currPath == null){ 
				log.warn "...couldn\'t find parameter \'${FILESTORAGE_CACHE_PARAMETER}\'. That\'s CRITICAL ! ${errMsg}"
				if (!force) 
					return false
				retValue = false
			} else {	//параметр определён
				if (!currPath?.trim()){ // null or empty or blank
					log.warn "...parameter \'${FILESTORAGE_CACHE_PARAMETER}\' is empty. That\'s CRITICAL ! ${errMsg}"
					if (!force) 
						return false
					retValue = false
				} else {
					log.debug "...validating path \'${currPath}\'"
					if (!(new File("${currPath}").isDirectory())){ //проверка на валидность каталога
						log.warn "...there is invalid path: \'${currPath}\' for run parameter \'${presetParam}\'. . That\'s CRITICAL ! ${errMsg}"
						if (!force) 
							return false
						retValue = false
					} else {
						log.debug "...valid!"
					}
					map.put(PDF_CACHE_PARAMETER, currPath)
				}
			}
		} else {
			log.debug "...use preset value for parameter \'${PDF_CACHE_PARAMETER}\'"
		}
		return retValue
	}

	boolean setDBParameters(boolean force){ 
	// Параметры подключения к БД
		def dsResult = false
		log.debug "Getting BR4J DataSource parameters..."
		def deployDir = new File("${map.get('br4j.jboss.configuration.path')}/deploy")
		[ "${DBMI_DATASOURCE_NAME}"			:["application", "dbmi"], 
		  "${DBMI_EVENT_DATASOURCE_NAME}"	:["application event log", "dbmi_event"],
		  "${PORTAL_DATASOURCE_NAME}"		:["JBoss Portal", "jboss.portal"]
		].each { k, v ->
			log.debug "...searching ${deployDir} DataSource files for ${v[0]} parameters..."
			deployDir.traverse(
				type:			FileType.FILES,
				maxDepth:		0,
				nameFilter:		~/.*-ds\.xml$/
			){ fileIter ->
				log.debug "......found ${fileIter.name}"
				Map res = getDSParams(fileIter, k, force)
				if (res){
					res.each{ a, b ->
						map.put("br4j.${v[1]}.db.${a}", b) // добавляем префикс к ключу при записи в карту параметров
					}
					dsResult = true
					return groovy.io.FileVisitResult.TERMINATE
				}
			}
			if (!dsResult){ // что-то не получилось при парсинге: не найден подход. файл, нет либо неполная инфа в нём
				log.error "There is an error while reading DBMI parameters in ${deployDir}. ${errMsg}"
				if (!force) { // если форсированный режим - продолжаем работу
					System.exit(1)
				}
			}
		}
		if (dsResult){
			log.debug "...DONE"
		} else {
			log.error "Configuration in DataSourse file ${it.getAbsolutePath()} is not valid! That\'s CRITICAL !"
			System.exit(1)
		}
		
		// Если не все необходимые параметры были переданы, то в случае интерактивного режима мы можем попросить пользователя их добавить
		*//*if(map.get('interactive') != null && 'true'.equalsIgnoreCase(map.get('interactive'))) {
			getInteractiveParam('dbConn')
			getInteractiveParam('dbLogin')
			getInteractiveParam('dbPass')
			getInteractiveParam('dbDriver')
		}*//*
	}*/
	
	private boolean checkAccount(login) {
		if(map.get('os.name').startsWith('Win')) {
			def process = ("runas /savecred /user:${map.get('hostName')}\\${login} \"echo 1\"").execute()
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
				map.put('iuh.executor.user.name', login)
				return true
			}else{
				log.error "User account ${login} does not exists!"
				return false
			}
		}
	}
	
	String getParameterFromPropertyFile(paramName, fileName){
		def file = new File(fileName)
		log.debug "...getting property \'${paramName}\' from file ${fileName}..."
		if (!file.isFile()){
			log.error "...file ${fileName} not found!"
			return null // CRITICAL
		}
		log.debug "...file found"
		def iuh = new Properties()
		file.withInputStream{ iuh.load(it) }
		if (!(iuh."${paramName}")?.trim()){
			log.warn "...parameter \'${paramName}\' not found in file ${fileName}!"
			return null // CRITICAL
		}
		return iuh."${paramName}"
	}
	
	/*Map getDSParams(File file, String dsName, boolean forceMode) {
		Map returnValue = [:]
		try {
			def parser = new XmlParser()
			def root = parser.parseText(file.getText("UTF-8"))
			if (root.name() != 'datasources'){
				log.debug "......not found section \'datasources\' in file ${file.getAbsolutePath()}"
				if (!forceMode) {
					return [:]  // отсутствует обязательный элемент, поэтому считаем конфигурацию полностью непригодной
				}
			}
			def ds = root.'local-tx-datasource'.find{it.'jndi-name'.text() == dsName} //вернёт 1 или 0 найденных секций (вследствие особенностей работы find)
			if (!ds){
				log.debug "......not found section \'${root.name()}->${dsName}\' in file ${file.getAbsolutePath()}"
				if (!forceMode) {
					return [:]  // отсутствует обязательный элемент, поэтому считаем конфигурацию полностью непригодной
				}
			}
			def node = ds.'connection-url'
			if (!node){
				log.debug "......not found section \'${root.name()}->${dsName}->connection-url\' in file ${file.getAbsolutePath()}"
				if (!forceMode) {
					return [:]  // отсутствует обязательный элемент, поэтому считаем конфигурацию полностью непригодной
				}
			}
			returnValue.put('url', node.text())

			node = ds.'driver-class'
			if (!node){
				log.debug "......not found section \'${root.name()}->${dsName}->driver-class\' in file ${file.getAbsolutePath()}"
				if (!forceMode) {
					return [:]  // отсутствует обязательный элемент, поэтому считаем конфигурацию полностью непригодной
				}
			}
			returnValue.put('driver', node.text())
			
			node = ds.'user-name'
			if (!node){
				log.debug "......not found section \'${root.name()}->${dsName}->user-name\' in file ${file.getAbsolutePath()}"
				if (!forceMode) {
					return [:]  // отсутствует обязательный элемент, поэтому считаем конфигурацию полностью непригодной
				}
			}
			returnValue.put('user.name', node.text())
			
			node = ds.'password'
			if (!node){
				log.debug "......not found section \'${root.name()}->${dsName}->password\' in file ${file.getAbsolutePath()}"
				if (!forceMode) {
					return [:]  // отсутствует обязательный элемент, поэтому считаем конфигурацию полностью непригодной
				}
			}
			returnValue.put('user.password', node.text())
			
			node = ds.'min-pool-size'
			if (!node){
				log.debug "......not found section \'${root.name()}->${dsName}->min-pool-size\' in file ${file.getAbsolutePath()}"
				if (!forceMode) {
					return [:]  // отсутствует обязательный элемент, поэтому считаем конфигурацию полностью непригодной
				}
			}
			returnValue.put('poolsize.min', node.text())
			
			node = ds.'max-pool-size'
			if (!node){
				log.debug "......not found section \'${root.name()}->${dsName}->max-pool-size\' in file ${file.getAbsolutePath()}"
				if (!forceMode) {
					return [:]  // отсутствует обязательный элемент, поэтому считаем конфигурацию полностью непригодной
				}
			}
			returnValue.put('poolsize.max', node.text())
			
			node = ds.'check-valid-connection-sql'
			if (!node){
				log.debug "......not found section \'${root.name()}->${dsName}->check-valid-connection-sql\' in file ${file.getAbsolutePath()}"
			} else {
				returnValue.put('check_sql', node.text())
			}
			
		} catch(Exception e) {
			errorHandling(e.getMessage())
				if (!forceMode) {
					return [:]  // были ошибки, поэтому считаем конфигурацию полностью непригодной
				}
		}
		log.debug "......OK"
		return returnValue
	}*/
	
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