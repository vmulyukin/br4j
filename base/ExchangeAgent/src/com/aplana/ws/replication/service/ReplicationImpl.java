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
package com.aplana.ws.replication.service;

import com.aplana.agent.plugin.PluginFactory;
import com.aplana.agent.plugin.receiver.ReceiveException;
import com.aplana.agent.plugin.receiver.WebServiceReceiver;
import com.aplana.ws.replication.Folder;
import com.aplana.ws.replication.Replication;
import com.aplana.ws.replication.Status;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jws.WebService;

@WebService(endpointInterface = "com.aplana.ws.replication.Replication", targetNamespace = Replication.NAME_SPACE)
public class ReplicationImpl implements Replication {

    private final static String RECEIVE_PLUGIN = "replicationWsReceiver";

    protected final Log logger = LogFactory.getLog(getClass());

    public Status sendFolder(Folder files) {
/*        logger.info("Replication webservice called");
        if (!PluginFactory.containsBean(RECEIVE_PLUGIN)){
            throw new IllegalStateException("This WS require " + RECEIVE_PLUGIN + "in beans.xml");
        }

        WebServiceReceiver receiver = (WebServiceReceiver) PluginFactory.getBean(RECEIVE_PLUGIN);

        try {
            receiver.saveFolder(files);
        } catch (ReceiveException e) {
            logger.error(e);
            return Status.FAILURE(e);
        }

        logger.info("Replication webservice successfully finished");
*/        return Status.SUCCESS;
    }

}