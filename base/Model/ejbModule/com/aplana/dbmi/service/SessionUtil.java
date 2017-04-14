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
package com.aplana.dbmi.service;

import java.lang.reflect.Field;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.portal.api.PortalRuntimeContext;
import org.jboss.portal.api.session.PortalSession;

public class SessionUtil {
	private static final Log logger = LogFactory.getLog(SessionUtil.class);
	
	public static Integer initSessionId(PortalRuntimeContext portalRuntime) {
		Integer sessionId = null;
		if (portalRuntime != null) {
			PortalSession session = portalRuntime.getSession();
			if (session != null) {
				try {
					//��� ����� ��������. ������ ������� ���� �� ���� �������.
					//������� ��������� ������ ����-������ �� ���������� ������.
					//���������� ��� ��������� ������� �������� ������ (creationTime). 
					Field f = session.getClass().getDeclaredField("session");
					f.setAccessible(true);
					Object sessionFacade = f.get(session);
					f = sessionFacade.getClass().getDeclaredField("session");
					f.setAccessible(true);
					HttpSession httpSession = (HttpSession) f.get(sessionFacade);
					f = httpSession.getClass().getDeclaredField("creationTime");
					f.setAccessible(true);
					long time = (Long)f.get(httpSession);
					String id = httpSession.getId();
				
					sessionId = (id + time).hashCode();
				} catch (Exception ex) {
					logger.warn("Can't get access to private HttpSession\n" + ex);
				}
			}
		}
		return sessionId;
	}
	
	public static String initSession(PortalRuntimeContext portalRuntime) {
		String id = null;
		if (portalRuntime != null) {
			PortalSession session = portalRuntime.getSession();
			if (session != null) {
				id = session.getId();
			} else {
				logger.warn("DataServiceBean with PortalRuntimeContext but without session (session == null)");
			}
		} else {
			logger.warn("DataServiceBean without RortalRuntimeContext");
		}
		return id;
	}
}
