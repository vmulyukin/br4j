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
package com.aplana.agent;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.agent.conf.DocumentBodyReader;
import com.aplana.agent.conf.DocumentBodyReader.DocType;
import com.aplana.agent.util.FileUtility;

/**
 * Iterates given folder for *.xml files located in subfolders
 * given
 * folder1
 * file1.xml
 * folder2
 * file2.xml
 */
public class ExportDirIterator implements Iterator<File> {
	protected final Log logger = LogFactory.getLog(getClass());
	private File nextDir;
	private Iterator<File> dirIterator;

	public ExportDirIterator(File outDir) {
		if (!outDir.exists()){ 
			throw new IllegalStateException("Outbound directory " + outDir.getAbsolutePath() + " doesn't exist!");
		}
		File[] directories = outDir.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return new File(dir, name).isDirectory();
			}
		});
		dirIterator = Arrays.asList(directories).iterator();
	}

	public boolean hasNext() {
		if (nextDir != null) {
			return true;
		}
		while (dirIterator.hasNext()) {
			File dir = dirIterator.next();
			if (FileUtility.isLocked(dir) || FileUtility.isInQueue(dir)) {
				continue;
			}
			
			File[] files = dir.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return DocumentBodyReader.isKnownMainDocument(name) != DocType.UNKNOWN;
				}
			});
			if (files == null){
				logger.error("Error when listing files in " + dir.getAbsolutePath());
				files = new File [0];
			}
		//First try to find (@link DocumentBodyReader.DISTRIBUTION_LETTER_FILENAME), then old format acceptable documents.
			if(getDistributionLetter(files) != null){
				nextDir = dir;
				return true;
			}
			for (File file : files) {
				if (isDocumentAcceptable(file)) {
					nextDir = dir;
					return true;
				}
			}
		}
		return false;
	}

	private boolean isDocumentAcceptable(File dir) {
		return !dir.isDirectory() && dir.canWrite();
	}

	private boolean isDistributionLetter(File file) {
		return DocType.TICKET.getFileName().equalsIgnoreCase(file.getName());
	}

	private File getDistributionLetter(File[] files) {
		for (File file : files) {
			if(isDistributionLetter(file)) {
				return file;
			}
		}
		return null;
	}

	/**
	*
	* @return current directory with message
	*/
	public File next() {
		if (hasNext()) {
			File next = nextDir;
			nextDir = null;
			return next;
		}
		return null;
	}

	public void remove() {}
}
