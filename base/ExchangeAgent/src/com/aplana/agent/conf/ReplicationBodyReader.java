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
package com.aplana.agent.conf;

import com.aplana.agent.conf.replication.PackageType;
import com.aplana.agent.conf.replication.ReplicationPackage;
import com.aplana.agent.util.FileUtility;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Reader for ReplicationPackage.xsd type
 */
public class ReplicationBodyReader extends DocumentBodyReader {

    private ReplicationPackage doc;

    ReplicationBodyReader(InputStream is) throws JAXBException {
        this.doc = ReplicationConfiguration.getInstance().unmarshal(is);
    }

    @Override
    public String toUuid() {
        return doc.getAddressee();
    }

    @Override
    public String fromUuid() {
        return doc.getSender();
    }
    
    @Override
    public String getId() {
    	PackageType type = doc.getPackageType();
    	switch (type) {
    		case CARD:
    		case RESPONSE:
    		case COLLISION:
    			return doc.getCard().getGuid();
    		case STATUS:
    			return doc.getStatus().getGuid();
    		case REQUEST:
    			return doc.getIncompleteCards().getGuid();
    		default:
    			return doc.getAddressee();
    	}
    	
    }

    /**
     * returns all files in directory except main replication file and special files aka LOCK, QUEUE etc.
     * @param dir base package dir
     * @return list of short names
     */
    @Override
    protected List<String> getAttachmentFiles(File dir) {
        List<File> fileList = FileUtility.getListOfFilesExcept(dir, new File(DocType.REPLICATION.getFileName()));

        List<String> ret = new ArrayList<String>(fileList.size());
        for (File file : fileList) {
            ret.add(file.getName());
        }

        return ret;
    }
}
