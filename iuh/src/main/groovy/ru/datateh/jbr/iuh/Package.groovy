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
import ru.datateh.jbr.iuh.utils.FileUtils
import ru.datateh.jbr.iuh.enums.ScriptTypes
import ru.datateh.jbr.iuh.msg.Message
import ru.datateh.jbr.iuh.msg.MessageType

@Log4j
public class Package {

	private static final String SCRIPT_ORDER_FILE_NAME = "scriptOrder";
	private String path;
	private SortedSet<Script> scripts = new TreeSet<Script>();
	private boolean force;
	private volatile InterruptedObject iObj
	
	@Override
	public String toString() {
		return path;
	}

	public Package(String path, boolean force, InterruptedObject iObj) {
		this.path = path;
		this.force = force;
		this.iObj = iObj;
	}
	
	public Message execute() {

		Message result = buildScripts();
		if(result.getState() == MessageType.ERROR) {
			if(!force) {
				return result;
			} else {
				log.error result
			}
		} else {
			log.debug result
		}

        String packageName = (new File (path)).getName();
		log.info "---->Executing package \"" + packageName + "\"..."
		Collection<Script> orderedScripts = getScripts();

		for(Script script : orderedScripts) {
			while(iObj.isFlag()) {
				iObj.setFinished(true)
				sleep 100
			}
			iObj.setFinished(false)
			result = script.execute();
			if(result != null) {
				if(result.getState() == MessageType.ERROR) {
					if(!force) {
						return result;
					} else {
						log.error result
					}
				} else {
					log.debug result
				}
			}
		}
		log.debug "...package \"" + packageName + "\" executed"
		
		return new Message(MessageType.SUCCESS, "<----Package \"" + packageName + "\" executed !")
	}
	
	private Message buildScripts() {
		
		// ��������� ������� ������
		File pack = new File(path);
		if(!pack.exists()) {
			return new Message(MessageType.ERROR, "Package \"" + path + "\" does not exist.")
		}
		
		log.debug "Preparing package: \"" + path + "\"..."
		def matcher
		ScriptTypes st
		
		for(File file : pack.listFiles()) {
			if(file.isFile()) {
				matcher = file.getName() =~ /(.+)\.(\w+)$/
				if(matcher.find() && (st = ScriptTypes.getScriptType(matcher.group(2))) != null) {
					log.debug '... found script ' + file + ' type is ' + st
					scripts.add(new Script(matcher.group(1), matcher.group(2), path, st));
				}
			}
		}
		return new Message(MessageType.SUCCESS, "Package \"" + path + "\" prepared successfully.");
	}

	/**
	 * Возвращает коллекцию скриптов, содержащихся в пакете, либо коллекцию скриптов, имена которых указаыны в файле
	 * имя_набора/имя_пакета/scriptOrder, в том порядке, в котором они перечислены
	 * @return коллекция скриптов
	 */
	public Collection<Script> getScripts() {
		File scriptOrderFile  =  new File(path + File.separator + SCRIPT_ORDER_FILE_NAME)
		List<String> scriptOrder = getScriptExecuteOrder(scriptOrderFile);
		if (scriptOrder.isEmpty()) {
			return scripts;
		} else {
			return reArrangeScriptOrder(this.scripts, scriptOrder);
		}
	}

	/**
	 * Возвращает список строк из файла, если файл существует
	 * @param orderFile имя файла
	 * @return список строк в файле, иначе пустой список
	 */
	private static List<String> getScriptExecuteOrder(File orderFile)
	{
		if (FileUtils.checkFileExist(orderFile, false)) {
			return FileUtils.readLines(orderFile);
		}
		return new ArrayList<String>();
	}

	/**
	 * Возвращает список скриптов отсортированных в соответствии с порядком, указанным в списке scriptOrder
	 * @param sortedScripts исходный отсортированный список скриптов
	 * @param scriptOrder новый требуемый порядок скриптов
	 * @return список скриптов отсортированный в порядке, указанном в scriptOrder
	 */
	private static List<Script> reArrangeScriptOrder(SortedSet<Script> sortedScripts, List<String> scriptOrder) {
		List<Script> reOrderedScripts = new ArrayList<Script>();
		for (String scriptName : scriptOrder)
		{
			Script script = getScriptByName(sortedScripts, scriptName.trim());
			if (script != null) {
				reOrderedScripts.add(script);
			}
		}
		return reOrderedScripts;
	}

	/**
	 * Возвращает скрипт с указанным именем
	 * @param scripts список скриптов
	 * @param scriptName имя скрипта
	 * @return скрипт с указанным именем
	 */
	private static Script getScriptByName(SortedSet<Script> scripts, String scriptName) {
		Iterator<Script> scriptIterator = scripts.iterator();
		while (scriptIterator.hasNext())
		{
			Script script = scriptIterator.next();
			if (script.getName().equalsIgnoreCase(scriptName))
			{
				return script;
			}

		}
		return null;
	}
}
