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

import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.distrmanager.exceptions.*;
import com.aplana.distrmanager.util.UtilsWorkingFiles;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class LoadDoc {

	protected static final List<String> PASSPORT_FILE_NAMES = Arrays.asList("document.xml");
	protected static final List<String> LETTER_FILE_NAMES = Arrays.asList("distributionletter.xml");

	private static DateFormat PREFIX_FILE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmssSSS");

	private static final String IN_FOLDER_KEY = "inFolder";
	private static final String IN_FOLDER_PROCESSED_KEY = "inFolderProcessed";
	private static final String IN_FOLDER_DISCARDED_KEY = "inFolderDiscarded";

	private File inFolder;
	private File inProcessedFolder;
	private File inDiscardedFolder;
	private String destFolderName;

	private boolean isInterrupt = false;

	private DocumentLoader documentLoader;

	private Log logger = LogFactory.getLog(getClass());

	public void setOptions(Properties options) {
		initializeFolders(options);
	}

	private void initializeFolders(Properties options) {
		this.inFolder = initializeFolder(options.getProperty(IN_FOLDER_KEY, ""));
		this.inProcessedFolder = initializeFolder(options.getProperty(IN_FOLDER_PROCESSED_KEY, ""));
		this.inDiscardedFolder = initializeFolder(options.getProperty(IN_FOLDER_DISCARDED_KEY, ""));
	}

	private File initializeFolder(String name) {
		File folder = new File(name);
		if (!folder.isAbsolute()) {
			throw new ConfigurationException("Absolute path should be defined for " + name);
		}
		if (!folder.exists()) {
			throw new ConfigurationException("Directory " + name + " does not exist");
		}
		if (!folder.isDirectory()) {
			throw new ConfigurationException("File " + name + " is not directory");
		}
		return folder;
	}

	public File getInDiscardedFolder() {
		return this.inDiscardedFolder;
	}

	public String getDestFolderName() {
		return this.destFolderName;
	}

	public void setDocumentLoader(DocumentLoader documentLoader) {
		this.documentLoader = documentLoader;
	}

	public void uploadDocuments() {
		destFolderName = PREFIX_FILE_FORMAT.format(new Date());

		if (logger.isInfoEnabled()) {
			logger.info("Documents loading is started from [" + inFolder.getAbsolutePath() + "]");
		}

		if (!inFolder.canRead()) {
			if (logger.isErrorEnabled()) {
				logger.error("It is impossible to read from " + inFolder.getAbsolutePath());
			}
			return;
		}

		File[] subDirectories = inFolder.listFiles(new FileFilter() {
			public boolean accept(File file) {
				return file.isDirectory() && !UtilsWorkingFiles.isDirectoryLocked(file);
			}
		});

		if (subDirectories == null) {
			logger.warn("It is some strange. Subdirectories were not get.");
			return;
		}

		Arrays.sort(subDirectories, new Comparator<File>() {
			public int compare(File o1, File o2) {
				return o1.compareTo(o2);
			}
		});

		for (File processingDirectory : subDirectories) {
			if (!isInterrupt) {
				if (logger.isInfoEnabled()) {
					logger.info("Loading from [" + processingDirectory + "] is started");
				}

				if (logger.isDebugEnabled()) {
					File[] allFiles = processingDirectory.listFiles(new FilenameFilter() {
						public boolean accept(File directory, String name) {
							return true;
						}
					});
					StringBuilder builder = new StringBuilder();
					builder.append("Folder ").append(processingDirectory).append(" contains following files: [\n");
					for (File file : allFiles) {
						builder.append(file.getName()).append("\n");
					}
					builder.append("]\n");
					logger.debug(builder.toString());
				}

				destFolderName = String.format("%s_%s", PREFIX_FILE_FORMAT.format(new Date()), processingDirectory.getName());

				File[] passportFiles = processingDirectory.listFiles(new FilenameFilter() {
					public boolean accept(File directory, String name) {
						return isPassportFile(name);
					}
				});

				if (passportFiles == null) {
					if (logger.isWarnEnabled()) {
						logger.warn("It is some strange. Files (passport) were not get from [" + processingDirectory + "]");
					}
					continue;
				}

				if (passportFiles.length > 1 && logger.isWarnEnabled()) {
					logger.warn("More than one passport file were found in [" + processingDirectory
							+ "]. First of them will be used");
				}

				File[] letterFiles = processingDirectory.listFiles(new FilenameFilter() {
					public boolean accept(File directory, String name) {
						return isLetterFile(name);
					}
				});

				if (letterFiles == null || letterFiles.length == 0) {
					if (logger.isWarnEnabled()) {
						logger.warn("It is some strange. Files (letter) were not get from [" + processingDirectory + "]");
					}

					if (letterFiles == null) {
						continue;
					}
				}

				if (letterFiles.length > 1 && logger.isWarnEnabled()) {
					logger.warn("More than one letter file were found in [" + processingDirectory
							+ "]. First of them will be used");
				}

				File destFolder = getResultProcessedFolder(destFolderName);
				if (passportFiles.length > 0) {
					Exception exception = null;
					try {
						documentLoader.load(passportFiles[0], letterFiles.length > 0 ? letterFiles[0] : null);
					} catch (PrematureAcknowledgementException ex) {
						if (logger.isWarnEnabled()) {
							logger.warn("Directory [" + processingDirectory + "] is skipped", ex);
						}
						destFolder = processingDirectory;
					} catch (DataException ex) {
						exception = ex;
					} catch (IOException ex) {
						exception = ex;
					} catch (RuntimeException ex) {
						exception = ex;
					} catch (LoadingException ex) {
						exception = ex;
					}
					if (exception != null) {
						destFolder = getResultDiscardedFolder(destFolderName);
						if (logger.isErrorEnabled()) {
							logger.error("Error during processing [" + processingDirectory + "]", exception);
						}
					}
				} else {
					if (logger.isInfoEnabled()) {
						logger.info("There is no passport file in the [" + processingDirectory
								+ "] directory. Continue working.");
					}
					continue;
				}

				try {
					if (!processingDirectory.equals(destFolder)) {
						String name = destFolder.getParentFile().getName();
						UtilsWorkingFiles.markDirectory(processingDirectory, name);
						FileUtils.copyDirectory(processingDirectory, destFolder);
						FileUtils.deleteQuietly(processingDirectory);
						UtilsWorkingFiles.unmarkDirectory(destFolder, name);
						if (logger.isInfoEnabled()) {
							logger.info("Directory [" + processingDirectory + "] is moved to [" + destFolder + "]");
						}
					}
				} catch (IOException ex) {
					if (logger.isErrorEnabled()) {
						logger.error("Unable to move directory [" + processingDirectory + "] to [" + destFolder + "].", ex);
					}
					FileUtils.deleteQuietly(destFolder);
				}
			} else {
				logger.info("Scanning subdirectories: " + Arrays.toString(subDirectories) + " was interrupted. Possibly JBOSS Server shutting down.");
				break;
			}
		}

		if (logger.isInfoEnabled()) {
			logger.info("Documents loading from [" + inFolder.getAbsolutePath() + "] is finished");
		}
	}

	public void interrupt() {
		logger.info("LoadDoc bean will be interrupted");
		this.isInterrupt = true;
	}

	public static boolean isPassportFile(String name) {
		return PASSPORT_FILE_NAMES.contains(name.toLowerCase());
	}

	public static boolean isLetterFile(String name) {
		return LETTER_FILE_NAMES.contains(name.toLowerCase());
	}

	private File getResultProcessedFolder(String folderName) {
		return new File(inProcessedFolder, folderName);
	}

	public File getResultDiscardedFolder(String folderName) {
		return new File(inDiscardedFolder, folderName);
	}

	protected interface DocumentLoader {
		ObjectId load(File passportFile, File letterFile) throws DataException, IOException, LoadingException;
	}
}
