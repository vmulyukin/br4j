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
package com.aplana.dbmi.storage.impl.beans;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.jboss.wsf.common.utils.UUIDGenerator;

import com.aplana.dbmi.storage.ContentStorage;
import com.aplana.dbmi.storage.FileStoreSchema;
import com.aplana.dbmi.storage.impl.StorageConst;

/**
 * Default implementation of {@link FileStoreSchema} that generates a new unique content 
 * URL based on the current date/time, like:
 * 		protocol://YYYY/MM/DD/HH/MM/UID.dat
 * 
 * @author <a href="mailto:ogalkin@aplana.com">Oleg Galkin</a>
 */
public class DefaultFileStoreSchema implements FileStoreSchema 
{
	private static final String PATH_SEPARATOR = StorageConst.DELIMITER_URL_LEVELS; // File.pathSeparator; // "/";
	private static final String DEFAULT_FILE_EXT = ".dat";

	private String fileExt = DEFAULT_FILE_EXT;

	/**
	 * Generates a new unique content URL based on the current date/time and UUID.
	 */
	public String createUniqueUrl( ContentStorage storage) {
		final Calendar calendar = new GregorianCalendar();
		final int 
				year = calendar.get(Calendar.YEAR), 
				month = calendar.get(Calendar.MONTH) + 1,  // 0-based
				day = calendar.get(Calendar.DAY_OF_MONTH),
				hour = calendar.get(Calendar.HOUR_OF_DAY),
				 minute = calendar.get(Calendar.MINUTE)
			;

		final StringBuilder sb = new StringBuilder();
		final String protocol = (storage != null)
								? storage.getSupportedProtocols()[0] 
								 : FileContentStorageBean.STORE_PROTOCOL;
		sb.append(protocol)
			.append(StorageConst.TAIL_PROTOCOL)
			.append(StorageConst.ROOT_OF_STORAGE)
			.append( String.format("%04d", year))	.append(PATH_SEPARATOR)
			.append( String.format("%02d", month))	.append(PATH_SEPARATOR)
			.append( String.format("%02d", day))	.append(PATH_SEPARATOR)
			.append( String.format("%02d", hour))	.append(PATH_SEPARATOR)
			.append( String.format("%02d", minute))	.append(PATH_SEPARATOR)
			.append(UUIDGenerator.generateRandomUUIDString())
			.append(this.fileExt);

		return sb.toString();
	}

	/**
	 * @return the current file extensions of generating url paths.
	 */
	public String getFileExt() {
		return this.fileExt;
	}

	/**
	 * @param fileExt the current file extensions for newly generated url paths.
	 * the NULL value is used to set {@link DEFAULT_FILE_EXT}
	 */
	public void setFileExt(String fileExt) {
		this.fileExt = (fileExt == null) ? DEFAULT_FILE_EXT : fileExt;
	}
}
