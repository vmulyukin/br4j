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
package com.aplana.distrmanager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StringUtils;

public class ExceptionToFileLogger {

	private File destinationFolder;
	private String fileName;
	private Log logger = LogFactory.getLog(getClass());

	public File getDestinationFolder() {
		return this.destinationFolder;
	}

	public void setDestinationFolder(File destinationFolder) {
		this.destinationFolder = destinationFolder;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public void logException(Exception exception) {
		try {
			String errorMessage = createErrorMessage(exception);
			createLogFile(errorMessage, exception);
		} catch (IOException ex) {
			logger.error("Error during log file creation", ex);
		} catch (RuntimeException ex) {
			logger.error("Unexpected exception during log file creation", ex);
		}
	}

	private String createErrorMessage(Exception ex) {
		StringBuilder errorMessage = new StringBuilder();
		Throwable cause = ex;
		errorMessage.append(cause.getMessage());
		while ((cause = cause.getCause()) != null) {
			errorMessage.append("\n Caused by: \n");
			errorMessage.append(cause.getMessage());
		}
		return errorMessage.toString();
	}

	private void createLogFile(String errorMessage, Exception exception) throws IOException {
		if (destinationFolder == null || fileName == null || !StringUtils.hasText(fileName)) {
			return;
		}
		File logFile = new File(destinationFolder, fileName + ".log");
		Writer writer = null;
		try {
			writer = new FileWriter(logFile);
			IOUtils.write(errorMessage, writer);
			IOUtils.write("\n", writer);
			exception.printStackTrace(new PrintWriter(writer, true));
		} finally {
			IOUtils.closeQuietly(writer);
		}
	}

}
