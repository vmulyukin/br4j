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
package com.aplana.dbmi.replication.action;

import com.aplana.dbmi.action.Action;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.replication.packageconfig.ReplicationPackage;

public class HandlingPackage implements Action<Card> {
	private static final long serialVersionUID = 1L;
	private ReplicationPackage packageXml;

	private String replicationCardGuid;
	private String addressee;
	private String sender;
	
	public Class<?> getResultType() {
		return Card.class;
	}

	public ReplicationPackage getPackageXml() {
		return packageXml;
	}

	public void setPackageXml(ReplicationPackage packageXml) {
		this.packageXml = packageXml;
	}

	public String getReplicationCardGuid() {
		return replicationCardGuid;
	}

	public void setReplicationCardGuid(String replicationCardGuid) {
		this.replicationCardGuid = replicationCardGuid;
	}

	public String getAddressee() {
		return addressee;
	}

	public void setAddressee(String addressee) {
		this.addressee = addressee;
	}

	public String getSender() {
		return sender;
	}

	public void setSender(String sender) {
		this.sender = sender;
	}
}