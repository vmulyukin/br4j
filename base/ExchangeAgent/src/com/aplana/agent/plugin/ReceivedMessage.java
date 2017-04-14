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
package com.aplana.agent.plugin;

import org.apache.log4j.Logger;

import java.util.List;

public class ReceivedMessage {

    protected final Logger logger = Logger.getLogger(getClass());

    private FileUnit document;

    private List<FileUnit> attachments;

    private String messageFolder;

    public ReceivedMessage(FileUnit document, List<FileUnit> attachments) {
        this.document = document;
        this.attachments = attachments;
    }


    public String getMessageFolder() {
        return messageFolder;
    }

    public void setMessageFolder(String messageFolder) {
        this.messageFolder = messageFolder;
    }

    public FileUnit getDocument() {
        return document;
    }

    public List<FileUnit> getAttachments() {
        return attachments;
    }


    @Override
    public String toString() {
        String name = document==null ? null : document.getName();
        return "{folder: " + messageFolder + ", document: " + name + "}";
    }

    public boolean validate() {
        if (!unitCheck(document)) return false;
        for (FileUnit attachment : attachments) {
            if (!unitCheck(attachment)) return false;
        }
        return true;
    }
    private boolean unitCheck(FileUnit unit) {
        if (unit == null) {
            logger.error("Unit is null");
            return false;
        }
        if (unit.getName() == null){
            logger.error("Unit name is null");
            return false;
        }
        if (unit.getContent() == null){
            logger.error("Unit content name is null");
            return false;
        }
        return true;
    }

    public static class FileUnit {
        private String name;
        private byte[] content;

        public FileUnit(String name, byte[] content) {
            this.name = name;
            this.content = content;
        }

        public String getName() {
            return name;
        }

        public byte[] getContent() {
            return content;
        }
    }
}
