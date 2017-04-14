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
package com.aplana.quartz.utils;

import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.service.DataException;

/**
 * ��������� ���������������� ����������.
 *   - ����������� ��� "properties".
 *   - ����� ����������� �� ������ (��� ����� �� ����������� �����).  
 * @author RAbdullin
 */
public class Configuration {

	/**
	 * ��������� �� ��������� ��� ����������, ������� ����� ��������� ������.
	 */
	final public static String DEFAULT_LIST_SUFFIX = ".list";

	/**
	 * �������-����������� �� ��������� ��� �������� � �������.
	 */
	final public static String DEFAULT_LIST_DELIMITERS = "[,;]";

	final Log logger = LogFactory.getLog(getClass());

	/**
	 * ������� �������� ����������������� properties-����� ������ ��������
	 * <portal>/conf/. ��� �������� ������������
	 * Portal.getFactory().getConfigService().loadConfigFile(xxx). ������������
	 * ����������� � config.
	 */
	private String configName;

	/**
	 * ������� ������������. �������� �� ����� {@link configName}. ������
	 * ������ {@link process} ����� ������������� ����������� (���������)
	 * ��������� �� {@link process().args}
	 */
	final protected Properties config = new Properties();


	public Configuration() {
	}

	public Configuration(String cfgName) {
		this();
		this.configName = cfgName;
	}

	/**
	 * ������� ���������������� ���������.
	 * @return the config
	 */
	public Properties getConfig() {
		return this.config;
	}

	/**
	 * ������� ������� �������� ����������������� ����� (������������
	 * <portal>/conf)
	 * 
	 * @return
	 */
	public String getConfigName() {
		return this.configName;
	}

	/**
	 * ������ �������� ����������������� ����� (������������ <portal>/conf)
	 * 
	 * @return
	 */
	public void setConfigName(String path) {
		this.configName = (path != null) ? path.trim() : null;
	}

	/**
	 * ��������� �������� ������������ �� ����� � ��������� {@link configName}
	 */
	public void loadConfig() {
		try {
			logger.debug(MessageFormat.format(
					"loading current config ''{0}'' ...", configName));
			if (configName == null || configName.length() == 0) {
				config.clear();
				logger.info("config data cleared");
			} else {
				/*
				 * �������� ������������ ������������ jar-������: final
				 * InputStream stm = getClass().getResourceAsStream(configName);
				 */

				/* �������� ������������ <portal>/conf */
				final InputStream stm = Portal.getFactory().getConfigService().loadConfigFile(configName);
				try {
					config.load(stm);
				} finally {
					IOUtils.closeQuietly(stm);
				}
				logger.info(MessageFormat.format(
						"config loaded successfully from file ''{0}''", 
						Portal.getFactory().getConfigService().getConfigFileUrl(configName)));
			}
		} catch (Exception ex) {
			logger.error("Configuration load error from config \'" + configName
					+ "\'", ex);
		}
	}

	public String getValue( final String paramName, final String defaultValue)
	{
		return config.getProperty(paramName, defaultValue);
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
	 * �������� ������ ���������� �������� ����������������� ���������.
	 * @param paramName �������� ���������, 
	 * (!) ���� ����� suffix, �� �������� ������ ����������� ������������ �� 
	 * ���� ������� ������ suffix.
	 * @param suffix ���� �� ����� - ������� ������, ������� ������ ������������� 
	 * �������� ���������, ����� ������� ��� ��� ������ ��������,
	 * ���� null (��� �����) - �������� ��������� ����� ���� �����.
	 * @param delimiters ������ ��������-������������ ��� ������
	 * @return ������ ����� (���� ��� ���������, �� �������� ������ �� ���� ���������).
	 * @throws DataException
	 */
	public String[] getValues( final String paramName, final String suffix, final String delimiters) 
		throws DataException 
	{
		final boolean hasSuffix = (suffix != null) && (suffix.length() > 0);

		// strictly checking suffix...
		if (paramName == null 
				|| ( hasSuffix && !paramName.endsWith(suffix) )
			)
			throw new DataException("factory.list", new Object[] {paramName} );

		final String value = config.getProperty(paramName);
		if (value == null) 
			return new String[0];
		final String[] result = value.trim().split(delimiters);
		return result;
	}

	/**
	 * �������� ������ �������� �� ��������� � ��������� ���������, �������� 
	 * ������ ����������� �� ��������-����������� "," ��� ";". 
	 * @param paramName
	 * @param suffix
	 * @return
	 * @throws DataException
	 */
	public String[] getValues( final String paramName, final String suffix) 
		throws DataException
	{
		return getValues(paramName, suffix, DEFAULT_LIST_DELIMITERS);
	} 

	/**
	 * �������� ������ �������� �� ��������� � ��������� ���������, �������� 
	 * ������ ����������� �� ��������-����������� "," ��� ";", � �������� 
	 * ��������� ������ ������������ �� {@link SUFFIX_LIST=".list"}. 
	 * 
	 * @param paramName
	 * @return
	 * @throws DataException
	 */
	public String[] getValues( final String paramName) throws DataException
	{
		return getValues(paramName, DEFAULT_LIST_SUFFIX);
	} 

	/**
	 * �������� ������ ���������� �������� ����������������� ���������.
	 * @param paramName �������� ���������, ������ ����������� ������������ �� 
	 * ������� ������ SUFFIX_LIST.
	 * @return ������ ����� (���� ��� ���������, �� �������� ������ �� ���� ���������).
	 * @throws DataException
	 */
	public List<String> getList( final String paramName) 
		throws DataException 
	{
		final List<String> result = new ArrayList<String>();
		Collections.addAll(result, getValues(paramName));
		return result;
	}

	/**
	 * ���������� ���������� � ��� ���������.
	 * @param parameters
	 */
	public void putAll(Map<String, ?> parameters) {
		if (parameters != null)
			this.config.putAll(parameters);
	}

	static public boolean stringToBool( String value, final boolean defaultValue)
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
