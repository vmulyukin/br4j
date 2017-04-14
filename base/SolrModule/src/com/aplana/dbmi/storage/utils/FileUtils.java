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
package com.aplana.dbmi.storage.utils;

public class FileUtils {
	/**
	 * replace illegal characters in a filename with "_"
	  * illegal characters :
	  *           : \ / * ? | < >
	  * forbidden file names:
	  * 			nul|prn|con|lpt[0-9]|com[0-9]
	  * 
	  * file name cannot start with dot (.)
	  * 
	  * @param file name
	  * 
	  * * @return
	  */
	public static String sanitizeFilename(String fileName) {
		String trimmedFileName = fileName.trim();
		// file name cannot start with dot (.)
		// forbidden characters: \ / : * ? " < > |
		// forbidden file names: nul|prn|con|lpt[0-9]|com[0-9].*
		String sanitizedName = trimmedFileName.replaceFirst("^\\.", "_")
			.replaceFirst("(nul|prn|con|lpt[0-9]|com[0-9])\\.", "_" + trimmedFileName.split("\\.")[0] + ".")
			.replaceFirst("(nul|prn|con|lpt[0-9]|com[0-9])$", "_" + trimmedFileName)
			.replaceAll("[:\\\\/*?|<>]", "_");
		return sanitizedName;
	}
}
