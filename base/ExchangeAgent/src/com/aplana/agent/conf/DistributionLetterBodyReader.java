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

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import com.aplana.agent.conf.envelope.Letter;
import com.aplana.agent.conf.envelope.Letter.Attachments.Attachment;

/**
 * 
 * @author atsvetkov
 *
 */
public class DistributionLetterBodyReader extends DocumentBodyReader {

    private Letter letter;
        
    public DistributionLetterBodyReader(final InputStream is) throws JAXBException {
        this.letter = DistributionLetterParser.getInstance().unmarshal(is);
    }
    
    
    @Override
    public String toUuid() {
        return letter.getAddressee().getGuid();
    }

    @Override
    public String fromUuid() {
        return letter.getSender().getGuid();
    }
    
    @Override
    public String getId() {
    	return letter.getId();
    }
    
    protected boolean validateAttachedFiles(File dir) {
    	return true;
    }

    /**
     * Returns the attachments names listed in {@link Letter}.
     */
    @Override
    protected List<String> getAttachmentFiles(File dir) {
        List<String> ret = new ArrayList<String>();
        List<Attachment> attachments = letter.getAttachments().getAttachment();

        if (attachments != null) {
            for (Attachment attachment : attachments) {
                ret.add(attachment.getName());
            }
        } 
        return ret;
    }
}
