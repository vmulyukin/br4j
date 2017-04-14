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
package com.aplana.medo.types.adapters;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.aplana.medo.types.IncomeRequestXMLPacket;
import com.aplana.medo.types.IncomeResponseXMLPacket;
import com.aplana.medo.types.XMLPacket;

public class XMLPacketAdapter extends XmlAdapter<XMLPacket, XMLPacket> {

    @Override
    public XMLPacket marshal(XMLPacket packet) throws Exception {
	return packet;
    }

    @Override
    public XMLPacket unmarshal(XMLPacket packet) throws Exception {
	switch (packet.getTypePacket()) {
	case REQUEST:
	    return new IncomeRequestXMLPacket(packet);
	case REPLY:
	    return new IncomeResponseXMLPacket(packet);
	}
	return null;
    }
}
