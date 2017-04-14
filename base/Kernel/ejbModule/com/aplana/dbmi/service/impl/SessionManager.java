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
package com.aplana.dbmi.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Container for session hashes mapped by sessionId.
 * Filling and cleaning by class UserEventListener.
 * Using into EJB classes (for ex. {@link DataServiceBean}).
 * @author desu
 */
@ManagedResource(objectName="br4j:name=sessionManager", description="MBean for SessionManager")
public final class SessionManager {
	private Log logger = LogFactory.getLog(getClass());
	private Map<String, UserInfo> sessionsMap = new ConcurrentHashMap<String, UserInfo>();
	
	private static class UserInfo {
		private Integer sessionHash;
		private String userName;
		private Date logged;
		private Date lastAction;
		
		UserInfo(Integer s, String uname) {
			this.sessionHash = s;
			this.userName = uname;
			this.logged = new Date();
			this.lastAction = new Date();
		}
	}
		
	public void addSessionId(String sessId, Integer hash, String uname) {
		sessionsMap.put(sessId, new UserInfo(hash, uname));
	}
	
	public void removeSessionId(String sessId) {
		sessionsMap.remove(sessId);
	}
	
	public Integer getSessionHash(String sessId) {
		if (sessId == null) {
			return null;
		}
		UserInfo userInfo = sessionsMap.get(sessId);
		if (userInfo != null && userInfo.sessionHash != null) {
			userInfo.lastAction = new Date();
			return userInfo.sessionHash;
		}
		try {
			//case system set of sessionId (for ex. in tasks like MaterialSync)
			//where sessionId value set as the id of current task thread
			return Integer.valueOf(sessId);
		} catch (NumberFormatException e) {
			//case task did not set sessionId and there is no logged user in portal
			//with this sessionId
			logger.error("Undefined session id: " + sessId);
			return null;
		}
	}
	
	@ManagedOperation
	public String showInfo() {
		String format = "%-40s | %-40s | %-40s | %-20s | %-20s%n";
		StringBuilder sb = new StringBuilder(
				String.format(format, 
						"   Session ID", 
						"   Logged time", 
						"   Last action time", 
						"   User ID", 
						"   Session Hash")
		);
		sb.append("\n");
		
		for (Entry<String, UserInfo> entry : sessionsMap.entrySet()) {
			UserInfo ui = entry.getValue();
			sb.append(String.format(format, 
					entry.getKey(),
					ui.logged,
					ui.lastAction,
					ui.userName,
					ui.sessionHash));
		}
		
		return sb.toString();
	}

	@ManagedAttribute
	public int getCount() {
		return sessionsMap.size();
	}
}