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

import javax.xml.bind.JAXBException;
import com.aplana.agent.conf.DocumentBodyReader.DocType;
import java.io.*;

public class DocumentBodyReaderFactory {

    public static DocumentBodyReader getDocumentBodyReader(final InputStream is, String name) throws JAXBException {
        if (DocType.MEDO_GOST.getFileName().equalsIgnoreCase(name)) {
            return new CommunicationBodyReader(is);
        } else if (DocType.REPLICATION.getFileName().equals(name)){
            return new ReplicationBodyReader(is);
        } else if (DocType.TICKET.getFileName().equals(name)) {
            return new DistributionLetterBodyReader(is);
        } else {
            throw new IllegalArgumentException("Unsupported main document " + name);
        }
    }
}