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
package com.aplana.agent.util;

import static org.apache.commons.io.FileUtils.forceMkdir;
import static org.apache.commons.io.FileUtils.writeByteArrayToFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.agent.plugin.AccessDirException;
import com.aplana.agent.plugin.ReceivedMessage;
import com.aplana.ws.replication.Folder;

/**
 * Helper class works with file system
 */
public class FileUtility {
    protected final static Log logger = LogFactory.getLog(FileUtility.class);

    public final static String SEP = System.getProperty("file.separator");
    
    public static boolean lockFolder(File dir, Properties props) {
    	return lockFolder(dir, props, false);
    }

    public static boolean lockFolder(File dir, Properties props, boolean forceCreateFolder) {
    	if (forceCreateFolder) {
    		checkDirOrMakeIt(dir);
    	}
        markFolder(dir, props, FolderMark.LOCK_FILE);
        return true;
    }

    public static boolean unlockFolder(File dir) {
        demarkFolder(dir, FolderMark.LOCK_FILE);
        return true;
    }
    
    public static boolean markPartial(File dir) {
    	Properties props = getMarkProperties(dir, FolderMark.LOCK_FILE);
    	props = new FilePropsBuilder(props).set(FileProperties.PARTIAL, true).build();
    	markFolder(dir, props, FolderMark.LOCK_FILE);
    	return true;
    }

    public static boolean isLocked(File dir) {
        return isMarked(dir, FolderMark.LOCK_FILE);
    }

	public static boolean isLockedByCurrentAgent(File dir, String currentAgent, String currentTaId) {
		File lock = new File(dir, FolderMark.LOCK_FILE.toString());
		if (lock.exists()) {
			Properties fileProps = getMarkProperties(dir, FolderMark.LOCK_FILE);
			if (fileProps != null) {
				String agentId = fileProps.getProperty(FileProperties.TEXT.toString());
				String taId    = fileProps.getProperty(FileProperties.TA_UUID.toString());
				if (agentId != null && taId != null) {
					return currentAgent.equals(agentId) && currentTaId.equals(taId);
				}
			}
		}
		return false;
	}

    public static void markInQueue(File messageDir) {
        markFolder(messageDir, new FilePropsBuilder().set(FileProperties.TEXT, "placed in queue ").build(), FolderMark.QUEUE_FILE);
    }

    public static void markInQueue(File messageDir, Properties props) {
        markFolder(messageDir, props, FolderMark.QUEUE_FILE);
    }

    public static Properties removeInQueueMarker(File dir) {
        return demarkFolder(dir, FolderMark.QUEUE_FILE);
    }

    public static boolean isInQueue(File dir) {
        return isMarked(dir, FolderMark.QUEUE_FILE);
    }

    private static boolean markFolder(File dir, Properties props, FolderMark fileName) {
        if (!dir.canWrite() || !dir.isDirectory()) {
            throw new IllegalStateException("Can't create " + fileName + " in " + dir.getAbsolutePath());
        }
        if (props == null){
        	logger.warn("Some one pushed me a NULLED props !!! It's unfair play.");
        	props = new Properties();
        }
        try {
            FileWriter fw = new FileWriter(new File(dir, fileName.value));
            props.store(fw, "");
            fw.close();
        } catch (IOException e) {
            logger.error("failed to create "  + fileName + " in " + dir.getAbsolutePath(), e);
            throw new RuntimeException(e);
        }
        return true;
    }

    public static Properties demarkFolder(File dir, FolderMark fileName) {        
    	Properties fileProps = getMarkProperties(dir, fileName);
        try {
            FileUtils.forceDelete(new File(dir, fileName.value));
        }catch (FileNotFoundException fnf) {
            logger.warn("failed to delete " + fileName + " in " + dir.getAbsolutePath(), fnf);
            // don't raise exception
        }catch (IOException e) {
            logger.error("failed to delete "  + fileName + " in " + dir.getAbsolutePath(), e);
            throw new RuntimeException(e);
        }
        return fileProps;
    }
    
    public static Properties getMarkProperties(File dir, FolderMark fileName) {
    	Properties fileProps = new Properties();
        try {
            FileReader fr = new FileReader( new File (dir, fileName.value));
            try {
				fileProps.load(fr);
			} catch (Exception e) {	} // � ������ �������� ����� ������ ������ ����� ������
            fr.close();
        }catch (FileNotFoundException fnf) {
            logger.warn("failed to read props from " + fileName + " in " + dir.getAbsolutePath(), fnf);
            return null;
            // don't raise exception
        }catch (IOException e) {
            logger.error("failed to to read props from "  + fileName + " in " + dir.getAbsolutePath(), e);
            throw new RuntimeException(e);
        }
        return fileProps;
    }

    public static boolean isMarked(File dir, final FolderMark file) {
        File[] files = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.equalsIgnoreCase(file.value);
            }
        });

        return files != null && files.length > 0;
    }

    public static File copyDirectory(File src, File dest) {
        return copyDirectory(src, dest, null);
    }
    
    public static File copyDirectory(File src, File dest, IOFileFilter filter) {
        File res;
        try {
            res = new File(dest, src.getName());
            FileUtils.copyDirectory(src, res, filter);
        } catch (IOException e) {
            logger.error("Failed to copy directory " + src.getAbsolutePath() +
                    " to " + dest.getAbsolutePath() + "!", e);
            throw new RuntimeException(e);
        }
        return res;
    }

    public static void silentDeleteDir(File dir) {
        try {
            deleteDir(dir);
        } catch (IOException e) {
            // only mark folder
            logger.warn("Skipping undeleted folder " + dir);
            markFolder(dir, new FilePropsBuilder().set(FileProperties.TEXT, "Delete this folder ").build(), FolderMark.DELME_FILE);
        }
    }


    public static void deleteDir(File dir) throws IOException {
        if (!dir.canWrite() || !dir.isDirectory()) {
            throw new IllegalStateException("Can't delete " + dir.getAbsolutePath());
        }
        try {
        	for (File inner : dir.listFiles()) {
        		if (!inner.getName().equals(FolderMark.LOCK_FILE.toString())) {
        			if (inner.isDirectory()) {
        				FileUtils.deleteDirectory(inner);
        			} else if (inner.isFile()) {
        				inner.delete();
        			}
        		}
        	}
        	FileUtils.deleteDirectory(dir);
        } catch (IOException e) {
            logger.error("Failed to clean up output directory! Error while delete " + dir.getAbsolutePath(), e);
            throw (e);
        }
    }

    public static File checkDirOrMakeIt(String path) {
        File dir= new File(path);
        return checkDirOrMakeIt(dir);
    }

    public static File checkDirOrMakeIt(File dir) {
        if (!dir.exists()){
            try {
                forceMkdir(dir);
            } catch (IOException e) {
                logger.error("Failed to create " + dir.getAbsolutePath(), e);
                throw new AccessDirException("Can not create directory: " + dir.getAbsolutePath(), e);
            }
        }
        if (!dir.canWrite() || !dir.isDirectory()) {
            throw new IllegalStateException("Can't work with " + dir.getAbsolutePath());
        }
        return dir;
    }

    public static File writeFile(File dir, ReceivedMessage.FileUnit unit) throws IOException {
        File file = new File(dir, unit.getName());
        writeByteArrayToFile(file, unit.getContent());
        return file;
    }

    public static List<File> getListOfFilesExcept(File dir, File doc) {
        List<File> result = new ArrayList<File>();
        Collection<?> collection = FileUtils.listFiles(dir, null, false);
        if (collection != null){
            for (Object o : collection) {
                File inList = (File) o;
                if (!doc.getName().equalsIgnoreCase(inList.getName())
                        && !isSpecialFile(inList)
                        ){
                    result.add(inList);
                }
            }
        }
        return result;
    }

    public static boolean isSpecialFile(File inList) {
        return FolderMark.isValueOf(inList.getName());
    }

    public static Folder.File readFile(File file) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("File is null");
        }
        if (!file.exists()){
            throw new IllegalArgumentException("File " + file.getAbsolutePath() + " does not exist");
        }
        if (!file.isFile()){
            throw new IllegalArgumentException("File " + file.getAbsolutePath() + " is not a file");
        }

        byte[] bytes = FileUtils.readFileToByteArray(file);

        return new Folder.File(bytes, file.getName());
    }
    
	public static void moveDirectory(File srcDir, File destDir, boolean appendDirContent) throws IOException {
		if (!appendDirContent){
			FileUtils.moveDirectory(srcDir, destDir);
			return;
		}
		if (srcDir == null) {
			throw new NullPointerException("Source must not be null");
		}
		if (!srcDir.exists()) {
			throw new FileNotFoundException("Source '" + srcDir + "' does not exist");
		}
		if (!srcDir.isDirectory()) {
			throw new IOException("Source '" + srcDir + "' is not a directory");
		}
	
		FileUtils.copyDirectory( srcDir, destDir );
		FileUtils.deleteDirectory( srcDir );
		if (srcDir.exists()) {
			throw new IOException("Failed to delete original directory '" + srcDir +
					"' after copy to '" + destDir + "'");
		}
	}

	public static void moveDirectory(File srcDir, File destDir, IOFileFilter filter) throws IOException {
		if (srcDir == null) {
			throw new NullPointerException("Source must not be null");
		}
		if (!srcDir.exists()) {
			throw new FileNotFoundException("Source '" + srcDir + "' does not exist");
		}
		if (!srcDir.isDirectory()) {
			throw new IOException("Source '" + srcDir + "' is not a directory");
		}

		FileUtils.copyDirectory( srcDir, destDir, filter );
		Properties lockProps = getMarkProperties(srcDir, FolderMark.LOCK_FILE);
		lockProps = new FilePropsBuilder(lockProps).set(FileProperties.PARTIAL, true).build();
		markFolder(srcDir, lockProps, FolderMark.LOCK_FILE);
		FileUtils.deleteDirectory( srcDir );
		if (srcDir.exists()) {
			throw new IOException("Failed to delete original directory '" + srcDir +
					"' after copy to '" + destDir + "'");
		}
	}
}
