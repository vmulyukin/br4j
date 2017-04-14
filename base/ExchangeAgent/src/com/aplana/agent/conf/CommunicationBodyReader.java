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

import com.aplana.ws.soz.model.communication.AssociatedFile;
import com.aplana.ws.soz.model.communication.Communication;

import org.apache.log4j.Logger;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Read useful information from document body for IEDMS.xsd type
 */
class CommunicationBodyReader extends DocumentBodyReader {

    protected final Logger logger = Logger.getLogger(getClass());

    private Communication doc;


    CommunicationBodyReader(InputStream is) throws JAXBException {
        this.doc = CommunicationConfiguration.getInstance().unmarshal(is);
    }

    public String toUuid() {
        return doc.getHeader().getUid();
    }

    public String fromUuid() {
        return doc.getHeader().getSource().getUid();
    }
    
    @Override
    public String getId() {
    	return doc.getDocument().getId();
    }

    @Override
    protected List<String> getAttachmentFiles(File dir) {
        Communication.Files files = doc.getFiles();

        if (files != null && files.getFile() != null && files.getFile().size() > 0) {
            List<String> ret = new ArrayList<String>(files.getFile().size());

            for (AssociatedFile associatedFile : files.getFile()) {
                ret.add(associatedFile.getLocalName());
            }
            return ret;
        } else {
            return null;
        }
    }
}
