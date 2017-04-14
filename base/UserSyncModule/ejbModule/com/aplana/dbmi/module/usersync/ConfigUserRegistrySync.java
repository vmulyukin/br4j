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
/**
 * 
 */
package com.aplana.dbmi.module.usersync;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.service.DataException;

/**
 * ������������ ��� UserRegistrySync.
 * @author RAbdullin
 */
public class ConfigUserRegistrySync {

	Log logger = LogFactory.getLog(getClass());

	final public static String SYNC_USERS_EN = "synch.users.enabled";
	final public static String SYNC_ROLES_EN = "synch.roles.enabled";
	final public static String SYNC_USERROLES_EN = "synch.userRoles.enabled";

	/**
	 * �������� ��������� �� ������� ���������� �������, ������� �� ���� 
	 * ���������������� �� ��. 
	 */
	final public static String LIST_USERROLES_SKIP = "userRoles.skip.portal.logins.list";

	/**
	 * �������� ��������� �� ������� ���������� �����, ������� ���� �������������
	 * (�������� ���� ���) ��� ������� ����������������� ������������.
	 */
	final public static String LIST_USERROLES_EVERYBODY = "userRoles.everybody.portal.role.list";

	/**
	 * ��������� ��� ����������, ������� ����� ��������� ������.
	 */
	final public static String SUFFIX_LIST = ".list";


	final private Properties config = new Properties();
	private String configName = "/userRegistrySync.properties";


	public String getConfigName() {
		return this.configName;
	}

	public void setConfigName(String path) {
		this.configName = path;
	}

	public void loadConfig() {
		if (configName == null) {
			config.clear();
		} else {
			try {
				config.load(getClass().getResourceAsStream(configName));
			} catch (IOException e) {
				logger.error("Configuration read error at file \'"+ configName + "\'", e);
			}
		}
	}


	public String getValue( final String paramName, final String defaultValue)
	{
		return config.getProperty(paramName, defaultValue);
	}

	/**
	 * �������� ������ ���������� �������� ����������������� ���������.
	 * @param paramName �������� ���������, ������ ����������� ������������ �� 
	 * ������� ������ SUFFIX_LIST.
	 * @return ������ ����� (���� ��� ���������, �� �������� ������ �� ���� ���������).
	 * @throws DataException
	 */
	public String[] getValues( final String paramName) 
		throws DataException 
	{
		// strictly checking suffix...
		if (paramName == null || !paramName.endsWith(SUFFIX_LIST))
			throw new DataException("factory.list", new Object[] {paramName} );

		final String value = config.getProperty(paramName);
		if (value == null) 
			return new String[0];
		final String[] result = value.trim().split(",;");
		return result;
	}

	/**
	 * �������� ������ ���������� �������� ����������������� ���������.
	 * @param paramName �������� ���������, ������ ����������� ������������ �� 
	 * ������� ������ SUFFIX_LIST.
	 * @return ������ ����� (���� ��� ���������, �� �������� ������ �� ���� ���������).
	 * @throws DataException
	 */
	public List /*<String>*/ getList( final String paramName) 
		throws DataException 
	{
		final List result = new ArrayList();
		Collections.addAll(result, getValues(paramName));
		return result;
	}

	/**
	 * �������� ���������� �������� ���������.
	 * @param paramName �������� ��������� ������������.
	 * @param bDefault �������� �� ���������, ���� ������������ ��� �� ������.
	 * @return �������� ���������� ����������� ��� bDefault, ���� ��� ���.
	 */
	public boolean getBoolValue( final String paramName, boolean bDefault){
		return stringToBool( config.getProperty(paramName), bDefault);
	}

	/**
	 * ���������� ���������� � ��� ���������.
	 * @param parameters
	 */
	public void putAll(Map parameters) {
		if (parameters != null)
			this.config.putAll(parameters);
	}

	static boolean stringToBool( String value, final boolean defaultValue)
	{
		if (value != null) value = value.trim();		
		return (value == null || value.length() == 0) 
				? defaultValue 
				: ( 	value.equalsIgnoreCase("true")
						||	value.equalsIgnoreCase("1")
						||	value.equalsIgnoreCase("+")
						||	value.equalsIgnoreCase("y")
						||	value.equalsIgnoreCase("yes")
						||	value.equalsIgnoreCase("�")
						||	value.equalsIgnoreCase("��")
				)
				; 
	}

}
