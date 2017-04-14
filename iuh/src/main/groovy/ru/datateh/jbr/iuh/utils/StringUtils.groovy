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
import groovy.util.logging.Log4j

@Log4j
class StringUtils {
	
	static String collectionToString(Collection<String> col) {
		log.debug 'Convert collection ' + col + ' to string'
		StringBuilder sb = new StringBuilder()
		for(Iterator<String> iter = col.iterator(); iter.hasNext();) {
			sb.append(iter.next())
			if(iter.hasNext()) {
				sb.append(',')
			}
		}
		return sb.toString()
	}
	
	static boolean isEmpty(String str) {
		if(str == null
			|| str.isEmpty()) {
			return true;
		}
		return false;
	}
}
