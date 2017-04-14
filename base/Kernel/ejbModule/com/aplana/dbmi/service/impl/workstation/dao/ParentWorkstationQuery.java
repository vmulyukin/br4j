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
package com.aplana.dbmi.service.impl.workstation.dao;

import java.util.Iterator;

import org.springframework.jdbc.core.support.JdbcDaoSupport;

import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.impl.locks.LockManagement;

public abstract class ParentWorkstationQuery extends JdbcDaoSupport{
	
	private LockManagement lockManagement;

	public LockManagement getLockManagement() {
		return lockManagement;
	}

	public void setLockManagement(LockManagement lockManagement) {
		this.lockManagement = lockManagement;
	}
	
	public String excludeCardsOnService(){
		StringBuilder result = new StringBuilder("");
		if(lockManagement!=null){
			result.append("(-1");
			Iterator<ObjectId> it = lockManagement.getCardIdsOnService().iterator();
			if(it.hasNext()){
				result.append(",");
			}
			while(it.hasNext()){
				ObjectId id = it.next();
				result.append(id.getId());
				if(it.hasNext()){
					result.append(",");
				}
			}
			result.append(")");
			return result.toString();
		} else {
			return result.toString();
		}
	}
}
