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
import ru.datateh.jbr.iuh.enums.ScriptTypes
import ru.datateh.jbr.iuh.msg.Message
import ru.datateh.jbr.iuh.msg.MessageType
import ru.datateh.jbr.iuh.utils.ExecUtils

@Log4j
class Script implements Comparable<Script> {
	
	private String name;
	private String extention;
	private String packagePath;
	private ScriptTypes type;

	Script(String name, String extention, String packagePath, ScriptTypes type) {
		this.name = name;
		this.extention = extention;
		this.packagePath = packagePath;
		this.type = type;
	}
	
	@Override
	public String toString() {
		return 'Script [name=' + name + ', extention=' + extention + ', packagePath=' + packagePath + ', type=' + type.getType() + ']';
	}

	public Message execute() {
		
		// ��������� ������� �������
		File script = new File(packagePath + File.separator + name + '.' + extention);
		log.debug 'Checking script ' + packagePath + File.separator + name + '.' + extention + '...'
		if(!script.exists()) {
			return new Message(MessageType.ERROR, 'Script ' + script.getAbsolutePath() + ' not exists')
		}
		log.debug '...checked script ' + packagePath + File.separator + name + '.' + extention
		
		if(type == ScriptTypes.GROOVY) {
			return ExecUtils.execGroovyScript(packagePath, name, extention);
		} else if(type == ScriptTypes.SH) {
			return ExecUtils.execShScript(packagePath, name, extention);
		}
		return null;
	}

	@Override
	public int compareTo(Script o) {
		return this.name.compareToIgnoreCase(o.name);
	}

	String getName() {
		return name
	}

	String getExtention() {
		return extention
	}

	String getPackagePath() {
		return packagePath
	}

	ScriptTypes getType() {
		return type
	}
}
