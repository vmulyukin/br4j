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
package com.aplana.owriter.manager;

import com.aplana.owriter.ulteo.rpcclient.OvdAdminServiceLocator;
import com.aplana.owriter.ulteo.rpcclient.OvdAdminPortType;
import com.aplana.owriter.ulteo.rpcclient.OvdAdminBindingStub;
import com.aplana.owriter.ulteo.saajclient.OvdAdminSaajClient;
import org.apache.axis.client.Stub;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.xml.rpc.ServiceException;
import java.net.URL;

/**
 * ����� ��� ���������� �������� �������������� ��������
 *
 * @author Vlad Alexandrov
 * @version 1.0
 * @since   2014-10-03
 */
public class OWriterServiceClient{
	
	protected final Log logger = LogFactory.getLog(getClass());
	
	private OvdAdminPortType stub;
	private OvdAdminSaajClient saajClient;

	public OWriterServiceClient() throws Exception{
        
		OvdAdminServiceLocator service = new OvdAdminServiceLocator();
		String serviceUrl = OWriterSettings.getServiceBaseUrl() + "/service/admin";
		this.stub = service.getOvdAdminPort(new URL(serviceUrl));
		((OvdAdminBindingStub)stub).setUsername(OWriterSettings.getServiceLogin());
		((OvdAdminBindingStub)stub).setPassword(OWriterSettings.getServicePassword());
		
		this.saajClient = new OvdAdminSaajClient(serviceUrl, 
				OWriterSettings.getServiceLogin(), OWriterSettings.getServicePassword());

	}

	/**
	 * ����� �������� ������������� ���������� �������
	 * @return String serverId
	 * @throws Exception
	 */
	public String getServerId() throws Exception {
		String serverId = saajClient.getServerId();
		if (null == serverId){
			logger.error("Cannot get server id by service URL " + OWriterSettings.getServiceBaseUrl());
			throw new Exception("Cannot get server id by service URL " + OWriterSettings.getServiceBaseUrl());
		}
		return serverId;
	}

	/**
	 * ����� ��������� ���� ������������� ������ ������������ �� �������
	 * @return boolean true ���� ������ ����������, ����� false
	 * @throws Exception
	 */
	public boolean doesUserSessionExist(String userLogin, String serverId) throws Exception{
		boolean exists = false;
		if (null != serverId){
			String sessionId = saajClient.getUserSessionIdByServer(userLogin, serverId);
			if (null != sessionId){
				exists = true;
			}
		}
		return exists;
	}
	
	/**
	 * ����� ������� ���������������� ������ c �������� ����������� ������� � ���������� � 3 �������
	 * @return boolean true ���� ������ �������, ����� false
	 */
	public boolean removeUserSession(String userLogin, String serverId, int retryCount) {
		boolean killed = false;
		try {
			String sessionId = saajClient.getUserSessionIdByServer(userLogin, serverId);
			if (null != sessionId){
				for (int i = 0; i < retryCount; i++){
					killed = this.stub.session_kill(sessionId);
					if (killed) {
						break;
					}
					if (retryCount > 1)
						Thread.sleep(3000);
				}
			}
			if (!killed){
				logger.warn("User session " + userLogin + " cannot be killed on server " + serverId);
			}
		}catch (Exception e) {
			logger.error("Error during user " + userLogin + " session kill on server " + serverId, e);
		}
		return killed;
	}

	/**
	 * ����� ������� ������ ������������ ���� �� ��� �� ����������
	 * @return boolean true ���� ������������ ������ ��� ��� ����������, ����� false
	 */
	public boolean createUser(String userLogin){
		boolean result = true;
		try {
			if (!saajClient.doesUserExist(userLogin)){
				result = this.stub.user_add(userLogin, userLogin, "P@$$w0rd");
				if (result == false){
					logger.error("Cannot create user " + userLogin);
					return false;
				}
			}
			String groupId = saajClient.getGroupId(OWriterSettings.getUsersGroup());
			if (null != groupId){
				if (!saajClient.doesUserBelongToGroup(userLogin, groupId)){
					result = this.stub.users_group_add_user(userLogin, groupId);
					if (result == false){
						logger.error("Cannot add user " + userLogin + " to group " 
								+ OWriterSettings.getUsersGroup());
						return false;
					}
				}
			} else {
				logger.error("Users group " + OWriterSettings.getUsersGroup() + " does not exist. " +
						"Please check service configuration");
				return false;
			}
		}catch (Exception e) {
			logger.error("Error while creating user " + userLogin, e);
			result = false;
		}
		return result;
	}

	/**
	 * ����� ������� ����� ����������� ���������� � ��������� ��� � �������� ������ ����������
	 * @return boolean true ���� ���������� ������� � ��������� � ������, ����� false
	 */
	public String addWriterApp(String userLogin, String fileName, String serverId){
		String appId = null;
		boolean isAppAdded = false;
		boolean isAppPublished = false;	
		try {
			String appName = "Writer-" + userLogin;
			String remoteFileName = "'" + OWriterSettings.getServiceSharedDir() + "/" + userLogin + "/" + fileName + "'";
			appId = this.stub.application_static_add(appName, appName, "linux", "libreoffice " + remoteFileName);
			if (null == appId || appId.isEmpty()) {
				logger.error("Cannot add static application " + appName);
				return null;
			}
				
			isAppAdded = this.stub.server_add_static_application(appId, serverId);
			if (!isAppAdded){
				logger.error("Cannot add static application " + appName + " to server " + serverId);
			}else {
				String appGroupId = saajClient.getPublishedAppGroupId(OWriterSettings.getAppsGroup());
	
				if (null != appGroupId){
					isAppPublished = this.stub.applications_group_add_application(appId, appGroupId);
					if (!isAppPublished){
						logger.error("Cannot add static application " + appName + " to group " + OWriterSettings.getAppsGroup());
					}
				} else {
					logger.error("Application group " + OWriterSettings.getAppsGroup() + " is not published. "
						+ "Please check service configuration");
				}
			}
		} catch (Exception e) {
			logger.error("Error while adding static application", e);
		}finally {
			if (!isAppAdded || !isAppPublished){
				removeWriterApp(appId);
				appId = null;
			}
		}
		return appId;
	}

	/**
	 * ����� ������� ����������� ����������
	 * @return boolean true ���� ���������� �������, ����� false
	 */
	public boolean removeWriterApp(String appId){
		boolean result = false;
		try {
			result = this.stub.application_static_remove(appId);
			if (!result){
				logger.warn("Static application " + appId + " cannot be removed");
			}
		}catch (Exception e){
			logger.error("Error while removing static application " + appId, e);
		}
		return result;
    }

	/**
	 * ����� ���������� ������ ��� �������� ��������� �������� � ����� ���� ��������
	 * @return String URL
	 */
	public String generateAppLink(String userLogin, String userToken, String appId) {
		return OWriterSettings.getServiceBaseUrl() + "/external.php?token=" + userToken + "&login=" + userLogin 
				+ "&mode=applications&app=" + appId + "&language=ru";
	}

	/**
	 * ����� ���������� ������ ��������� �������
	 * @return String logs
	 */
	public String getServerLogs(String serverId){
		String serverLogs = null;
		try {
			serverLogs = saajClient.getServerLogs(serverId);
		}catch (Exception e){
			logger.error("Error while downloading server logs from serverId" + serverId, e);
		}
		return serverLogs;
	}
}
