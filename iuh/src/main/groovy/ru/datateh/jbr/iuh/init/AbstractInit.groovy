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
package ru.datateh.jbr.iuh.init

import groovy.util.logging.Log4j
import org.apache.log4j.PropertyConfigurator
import ru.datateh.jbr.iuh.AbstractExecute
import ru.datateh.jbr.iuh.msg.Message
import ru.datateh.jbr.iuh.msg.MessageType
import ru.datateh.jbr.iuh.parameters.CommonParameters
import ru.datateh.jbr.iuh.utils.FileUtils
import ru.datateh.jbr.iuh.utils.IuhUtils

@Log4j
public abstract class AbstractInit extends AbstractExecute {
	
	@Override
	@Deprecated
	final public boolean check() {}

	@Override
	@Deprecated
	final public void install() {}

	@Override
	@Deprecated
	final public void uninstall() {}
	
	public void start() {
		
		// ���������� ������������
        def config = new ConfigSlurper().parse(FileUtils.getLogConfigFile().toURI().toURL());
		PropertyConfigurator.configure(config.toProperties())
		
		map = IuhUtils.sharedFileToMap()
		force = getParam(CommonParameters.Iuh.MODE_FORCE) ? Boolean.valueOf(getParam(CommonParameters.Iuh.MODE_FORCE)) : false
		
		try {
			
			//if(preRun) {
				initScript()
			/*	postInit()
			}*/
			
		} catch(Exception e) {
			log.error 'Error during the execution of the script: ' + e.getMessage()
			log.error 'Execution will continue if the parameter FORCE equals TRUE'
		}
		
		finalize()
	}
	
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

}
