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
package com.aplana.soz.adapter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Iterator;
import java.io.File;

/**
 * Iterates given folder for *.xml files located in subfolders
 * given
 * folder1
 * file1.xml
 * folder2
 * file2.xml
 */
public class ExportDirIterator implements Iterator {

    protected final Log logger = LogFactory.getLog(getClass());

    private File outDir;
    private File outDirResultOk;
    private File outDirResultFail;
    private File currentFile;
    private File currentDir;

    public ExportDirIterator(File outDir, File outDirResultOk, File outDirResultFail) {
        this.outDir = outDir;
        this.outDirResultOk = outDirResultOk;
        this.outDirResultFail = outDirResultFail;
    }

    public boolean hasNext() {
        File[] directories = outDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return new File(dir, name).isDirectory();
            }
        });

        if (directories != null && directories.length > 0) {
            for (File dir : directories) {
                File[] files = dir.listFiles(new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        return name.endsWith(".xml");
                    }
                });
                for (File file : files) {
                    if (!file.isDirectory() && file.canWrite()) {
                        currentFile = file;
                        currentDir = dir;
                        return true;
                    }
                }

            }
        }
        return false;
    }

    public File next() {
        return currentFile;
    }

    public void remove() {
        deleteDir(currentDir);
    }

    public void moveToOk(){
        move(outDirResultOk);
    }

    public void moveToFail(){
        move(outDirResultFail);
    }

    private void move(File dest) {
        try {
            FileUtils.copyDirectory(currentDir, new File(dest, currentDir.getName()));
        } catch (IOException e) {
            logger.error("Failed to copy directory " + currentDir.getAbsolutePath() +
                    " to " + dest.getAbsolutePath()+ "!", e);
            throw new RuntimeException(e);
        }
        remove();
    }


    private void deleteDir(File currentDir) {
        assert currentDir.isDirectory();
        try {
            FileUtils.deleteDirectory(currentDir);
        } catch (IOException e) {
            logger.error("Failed to clean up output directory! Error while delete " + currentDir.getAbsolutePath(), e);
            throw new RuntimeException(e);
        }
    }
}
