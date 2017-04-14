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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.owriter.token.SecureTokenGenerator;

/**
 * ����� ��� ���������� ������� ������ � �������� �������������� ��������
 *
 * @author Vlad Alexandrov
 * @version 1.0
 * @since   2014-10-03
 */

public class OWriterSessionManager {
	protected final Log logger = LogFactory.getLog(getClass());
	protected final Log srvLogger = LogFactory.getLog("OWriterServiceLog");
	
	private String userLogin;
	private String userHome;
	private String fileName;
	private long lastModified;
	private Long fileCardId;
	private String serverId;
	private String appId;
	private InputStream origFileData;
	
	private OWriterServiceClient srvClient;
	
	public OWriterSessionManager() throws Exception {
		srvClient = new OWriterServiceClient();
		serverId = srvClient.getServerId();
	}
	
	public OWriterServiceClient getServiceClient() {
		return srvClient;
	}

	public void setServiceClient(OWriterServiceClient srvClient) {
		this.srvClient = srvClient;
	}

	public void setUserLogin(String userLogin) {
		this.userLogin = userLogin;
	}

	public void setUserHome(String userHome) {
		this.userHome = userHome;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public void setFileCardId(Long fileCardId) {
		this.fileCardId = fileCardId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}
	
	public void setOrigFileData(InputStream origFileData) {
		this.origFileData = origFileData;
	}

	public String getUserLogin() {
		return userLogin;
	}

	public String getUserHome() {
		return userHome;
	}

	public String getFileName() {
		return fileName;
	}

	public Long getFileCardId() {
		return fileCardId;
	}

	public String getServerId() {
		return serverId;
	}

	public String getAppId() {
		return appId;
	}
	
	public InputStream getOrigFileData() {
		return origFileData;
	}

	public InputStream getNewFileData() throws IOException {
		return new FileInputStream(userHome + fileName);
	}

	/**
	 * ����� ���������� ������ ��� �������� ��������� �������� � ����� ���� ��������
	 * @return String URL
	 * @throws Exception
	 */
	public String generateAppLink() throws Exception {
		String uid = UUID.randomUUID().toString();
		SecureTokenGenerator stg = new SecureTokenGenerator(uid);
		String userToken = stg.generateToken(userLogin);
		if (null == userToken) {
			logger.error("User token is null for user " + userLogin + " and random uid " + uid);
			return null;
		}else {
			return srvClient.generateAppLink(userLogin, userToken, appId);
		}
	}

	/**
	 * ����� ������� ������������, ���������� � �������������� �������� ������� ������������
	 * @return String URL
	 * @throws Exception
	 */
	public boolean initSession(){
		boolean result = false;
		if (null == userLogin || null == serverId
				|| null == fileName || null == origFileData){
			logger.error(MessageFormat.format("Writer session is not configured properly: " +
					"userLogin=''{0}'', serverId=''{1}'', fileName=''{2}'', origFileData=''{3}''",
					userLogin, serverId, fileName, origFileData));
			return false;
		}
		if (srvClient.createUser(userLogin)){
			appId = srvClient.addWriterApp(userLogin, fileName, serverId);
			if (null != appId){
				userHome = createUserHome(userLogin);
				if (null != userHome){
					result = putFileToUserHome(userHome, fileName, origFileData);
				}
				if (!result) {
					srvClient.removeWriterApp(appId);
				}
			}
		}
		
		if (null == appId){
			srvLogger.error(srvClient.getServerLogs(serverId));
		}
		return result;
	}

	/**
	 * ����� ������� ���������������� ������ � ����������� ����������
	 * @return void
	 */
	public void closeSession(){
		boolean isUserSessionRemoved = false;
		try {
			if (srvClient.doesUserSessionExist(userLogin, serverId))
				isUserSessionRemoved = srvClient.removeUserSession(userLogin, serverId, 1);
		}catch (Exception e){
			logger.warn("Error while checking whether user session exists or not for " + userLogin 
					+ " on server " + serverId, e);
		}

		if (!srvClient.removeWriterApp(appId)){
			srvLogger.warn(srvClient.getServerLogs(serverId));
		}

		try {
			if (!isUserSessionRemoved && srvClient.doesUserSessionExist(userLogin, serverId))
				if (!srvClient.removeUserSession(userLogin, serverId, 10)){
					srvLogger.error(srvClient.getServerLogs(serverId));
				}
		}catch (Exception e){
			logger.error("Error while checking whether user session exists or not for " + userLogin + 
					" on server " + serverId, e);
			srvClient.removeUserSession(userLogin, serverId, 1); // try to remove user session anyway
			srvLogger.warn(srvClient.getServerLogs(serverId));
		}
		// cleanup user home
		try {
			FileUtils.cleanDirectory(new File(userHome));
		} catch (Exception e){
			logger.warn("Error while cleaning up user home directory " + userHome, e);
		}
    }

	/**
	 * ����� ��������� ��� �� ���� ������� � �������� ��������������
	 * @return boolean true ���� ���, ����� false
	 */
	public boolean isFileChanged(){
		File newFile = new File(userHome + fileName);
		if (newFile.lastModified() > lastModified)
			return true;
		else
			return false;
	}

	/**
	 * ����� ������� �������� ������� ������������ (���� ��� �� ����������) � ������� ���
	 * @return String user home dir
	 */
	private String createUserHome(String userLogin){
		String fileSeparator = System.getProperty("file.separator");
		String userHome = OWriterSettings.getLocalSharedDir() + fileSeparator + userLogin + fileSeparator;

		File userDir = new File(userHome);
		// if the directory does not exist, create it
		if (!userDir.exists()) {
			if (logger.isDebugEnabled()){
				logger.debug("Creating directory: " + userHome);
			}
			boolean result = false;
			try{
				result = userDir.mkdir();
			} catch(SecurityException se){
				logger.error(se.getMessage(), se);
			}
			if(!result) {
				logger.error("Cannot create user home directory: " + userHome);
				return null;
			}else if (logger.isDebugEnabled()){
				logger.debug("Directory " + userHome + "created");
			}
		}
		// cleanup user home
		try {
			FileUtils.cleanDirectory(new File(userHome));
		}catch (IOException e){
			logger.warn("Error while cleaning up user home directory " + userHome);
		}
		return userHome;
	}

	/**
	 * ����� �������� ���� � �������� ������� ������������
	 * @return boolean true ���� ���� ��� ����������, ����� false
	 */
	private boolean putFileToUserHome(String userHome, String fileName, InputStream fileData){
		boolean result = true;

		File userDir = new File(userHome);
		if (!userDir.exists()) {
			logger.error("Cannot put file to user home directory because user home does not exist: " + userHome);
			return false;
		}
			
		File newFile = new File(userHome + fileName);
		OutputStream os = null;
		try {
			os = new FileOutputStream(newFile);
			IOUtils.copy(fileData, os);
		}catch (Exception e) {
			logger.error("Error while copying file " + fileName + " to user home " + userHome);
			result = false;
		}finally {
			IOUtils.closeQuietly(fileData);
			IOUtils.closeQuietly(os);
			if (result){
				newFile.setWritable(true, false);
				lastModified = newFile.lastModified();
			}
		}
		return result;
	}
}
