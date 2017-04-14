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
import com.aplana.dbmi.replication.packageconfig.PackageType;

import java.util.Set;

public class CreateReplicationPackage implements Action<Set<Card>> {
	private static final long serialVersionUID = 1L;
	private Card card;
	private String ReplicationCardGuid;
	private String Addressee;
	private PackageType packageType;
	private boolean updateVersion = true;
	
	public Class<?> getResultType() {
		return Set.class;
	}

	public Card getCard() {
		return card;
	}

	public void setCard(Card card) {
		this.card = card;
	}

	public String getReplicationCardGuid() {
		return ReplicationCardGuid;
	}

	public void setReplicationCardGuid(String replicationCardGuid) {
		ReplicationCardGuid = replicationCardGuid;
	}

	public String getAddressee() {
		return Addressee;
	}

	public void setAddressee(String addressee) {
		Addressee = addressee;
	}

	public PackageType getPackageType() {
		return packageType;
	}

	public void setPackageType(PackageType packageType) {
		this.packageType = packageType;
	}

	public boolean isUpdateVersion() {
		return updateVersion;
	}

	public void setUpdateVersion(boolean updateVersion) {
		this.updateVersion = updateVersion;
	}
}