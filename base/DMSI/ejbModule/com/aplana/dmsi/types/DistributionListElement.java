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
package com.aplana.dmsi.types;

import javax.xml.bind.annotation.XmlTransient;

@XmlTransient
public class DistributionListElement extends DMSIObject {

    protected Organization recipient;
    protected String uid;
	protected HeaderMessageEnumType messageType;

    public Organization getRecipient() {
	return recipient;
    }

    public void setRecipient(Organization recipient) {
	this.recipient = recipient;
    }

    public String getUid() {
	return this.uid;
    }

    public void setUid(String uid) {
	this.uid = uid;
    }

	public HeaderMessageEnumType getMessageType() {
		return this.messageType;
}

	public void setMessageType(HeaderMessageEnumType messageType) {
		this.messageType = messageType;
	}

}
