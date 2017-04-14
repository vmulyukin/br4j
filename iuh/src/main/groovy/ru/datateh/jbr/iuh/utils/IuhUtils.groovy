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
import com.lts.ipc.sharedmemory.SharedMemory
import groovy.util.logging.Log4j

import java.util.regex.Matcher
import java.util.regex.Pattern

@Log4j
public class IuhUtils {
	
	private static String fullFileName = System.getProperty('java.io.tmpdir') + File.separator + 'harness_param_map';
	
	private static int fileSize
	
	public static void mapToSharedFile(Map<String, String> map) {
		log.trace 'State of params\'s map before writting: ' + map
		StringBuilder sb = new StringBuilder()
		int offset = 0;
		map.keySet().each {
			sb.append(it).append('=').append(map.get(it)).append('\n')
		}
		String str = sb.toString()
		File checkFile = new File(fullFileName)
		if(checkFile.exists()) {
			checkFile.delete()
		}
		fileSize = str.getBytes().length
        File sharedFile = new File(fullFileName);
		SharedMemory sm = new SharedMemory(sharedFile, fileSize);
        sm.lock()
        sm.putString(offset, str)
        sm.unlock()
        FileUtils.changeFilePermission(sharedFile, FileUtils.Permission.WRITE, false);
    }
	
	public static Map<String, String> sharedFileToMap() {
		log.trace 'Reading in the params\'s map'
		Map<String, String> map = new HashMap<String, String>()
		SharedMemory sm = new SharedMemory()
		sm.connect(fullFileName, fileSize)
		File f = sm.getSegmentFile()
		BufferedReader br = new BufferedReader(new FileReader(f));
		String line
		//Pattern p = Pattern.compile('^(\\w+)=(.*)\$')
		Pattern p = Pattern.compile('^([-a-zA-Z0-9_\\.]+)=(.*)\$')
		Matcher m
		while((line = br.readLine()) != null) {
			//log.debug line
			m = p.matcher(line)
			if(m.find()) {
				//log.debug 'name: ' + m.group(1)
				//log.debug 'value: ' + m.group(2)
				//log.debug '<br>'
				map.put(m.group(1), m.group(2))
			} else {
				//log.debug line
			}
		}
		log.trace 'State of params\'s map after reading: ' + map
		return map
	}
	
	public static putParamToMap(String name, String value) {
		if(name == null) {
			return
		}
		Map<String, String> map = sharedFileToMap()
		map.put(name, value)
		mapToSharedFile(map)
	}
	
	public static removeParamFromMap(String name) {
		if(name == null) {
			return
		}
		Map<String, String> map = sharedFileToMap()
		map.remove(name)
		mapToSharedFile(map)
	}

}
