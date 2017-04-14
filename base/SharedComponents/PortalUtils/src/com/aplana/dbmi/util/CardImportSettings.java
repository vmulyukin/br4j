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
package com.aplana.dbmi.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.Portal;

public class CardImportSettings {
	private static Log logger = LogFactory.getLog(CardImportSettings.class);

	private static final String CONFIG = "dbmi/cardImport/card_import_config.properties";
	private static final String THREAD_COUNT_PROPERTY = "threadCount";
	private static final String CARDS_COUNT_THRESHOLD_PROPERTY = "cardsCountThreshold";
	

	public static final int CHAR_COUNT_IN_LINE_LIMIT = 50;	// ������������ ���������� ������������ �������� � ������ ������ ���������� �������
	public static final int LINE_COUNT_LIMIT = 20;			// ������������ ���������� ������������ ����� � ��������� � ����������� ������� 
	//����������� ���������� ������������ �������, ������������� ��������, � ���������� ����������� � �������
	private static final float THREAD_COUNT_RATIO = 0.5f;

	//���� ����� ����������� � ������� ������ ����� ������, �� ����� ������������ ���������� ������� �� ���������
	private static final int THREAD_THRESHOLD = 4;

	//���� ����� ����������� ������ ������, �� ������� ��� �������� ������� �� ���������
	private static final int DEFAULT_THREAD_COUNT = 1;

	//����� �� ���������, �� �������� ���������������� �����������
	private static final int DEFAULT_CARDS_COUNT_THRESHOLD = 50;

	//���������� �����������, �� ������� �������� ������� ����� � �������� ������ ���������� ���������� �������
	public static final int THREAD_SLEEP_VALUE = 1000;

	//�������� ������ ������������� �������� � �������������� �� ������ � ������ �����.
	//�� ������ ������ ������������� ����� �������, �� ������ ������ ������ ��������� � ��� ������� � ������� ������ 
	//��������������� ���� ������ ������������� ��������
	public static final int LINE_NUMBER_OFFSET = 3;

	private static Properties props;
	private static Object synch = new Object();
	
	public static Properties getProps() {
		synchronized  (synch) { // (PdfConvertorSettings.class)
			if (props == null) {			
				try {
					final InputStream is = Portal.getFactory().getConfigService().getConfigFileUrl(CONFIG).openStream();
					try {
					final Properties p = new Properties();
					p.load(is);
					props = p;
					} finally {
						IOUtils.closeQuietly(is);
					}
				} catch (IOException e) {
					logger.error("Couldn't read settings file " + CONFIG, e);
				}
			}
		}

		return props;
	}

	public static int getThreadCount() {
		Properties props = getProps();
		if (props != null) {
			String propertyValue = props.getProperty(THREAD_COUNT_PROPERTY);
			if ( propertyValue != null && !propertyValue.isEmpty()) {
				try {
					int value = Integer.valueOf(propertyValue);
					if (value > 0) {
						return value;
					}
				} catch (Exception e) {
					logger.warn("Unable to read threadCount property. Default value will be used.", e);
				}
			}
		}
		/*
		 * ���� ���������� ������� �� ���������������� � ����� ����������� � ������� �� ������ ���������� ������, �� 
		 * ������ ��� ������� �������� ����� �����������, � ��������� ������ �������� �� ���������
		 */
		int currentCPUCount = Runtime.getRuntime().availableProcessors();
		if (currentCPUCount >= THREAD_THRESHOLD) {
			return (int)(currentCPUCount * THREAD_COUNT_RATIO);
		}
		return DEFAULT_THREAD_COUNT;
	}

	public static int getCardsCountThreshold() {
		Properties props = getProps();
		if (props != null) {
			String propertyValue = props.getProperty(CARDS_COUNT_THRESHOLD_PROPERTY);
			if ( propertyValue != null && !propertyValue.isEmpty()) {
				try {
					return Integer.valueOf(propertyValue);
				} catch (Exception e) {
					logger.warn("Unable to read cardsCountThreshold property. Default value will be used.", e);
				}
			}
		}

		return DEFAULT_CARDS_COUNT_THRESHOLD;
	}
}
