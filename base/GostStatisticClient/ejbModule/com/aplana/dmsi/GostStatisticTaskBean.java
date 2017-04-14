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
package com.aplana.dmsi;

import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.ReferenceValue;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

/**
 * ����� <code>GostStatisticTaskBean</code> ������������ ����� ����������� ��������� ������, ������� ���������� Excel ���� ����
 * ��������� � ��������� ��� � �������.
 *
 * @author aklyuev
 *
 */
public class GostStatisticTaskBean extends AbstractStatisticTaskBean {

	private static final long serialVersionUID = 1L;
	private Log logger = LogFactory.getLog(getClass());

	public static final String BASE_REPORT_NAME = "Monitoring_";

	private static final String CONFIG_FILE = "dbmi/gost/gost_statistic_config.properties";
	private static final String PERIOD_PROPERTY = "period";

	private static final String DEFAULT_PERIOD = "7";

	private static Boolean working = false;

	private int period;

	public void process(Map<?, ?> parameters) {
		logger.info("GostStatisticTask is started");

		synchronized (GostStatisticTaskBean.class) {
			if (working) {
				logger.debug("GostStatisticTask is already started. Skip this");
				return;
			}
			working = true;
		}

		Exception exception = null;
		Card newMaterialCard = null;

		try {
			readProperties(CONFIG_FILE);
			GostStatisticReportGenerator reportGenerator = new GostStatisticReportGenerator(period, getAddresses(), getUsername(), getPassword());
			InputStream reportStream  = reportGenerator.generateReport();

			Date currentTime = Calendar.getInstance().getTime();
			String reportName = BASE_REPORT_NAME + DATE_FORMATTER.format(currentTime) + REPORT_EXTENSION;

			newMaterialCard = createNewMaterialCard(reportStream, reportName);
		} catch (Exception e) {
			exception = e;
			logger.error(e.getMessage(), e);
		}

		try {
			createStatisticCard(newMaterialCard, exception);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
		    synchronized (GostStatisticTaskBean.class) {
				working = false;
				logger.info("GostStatisticTask is finished");
			}
		}

	}

	protected void setParameters(Properties options) {
		super.setParameters(options);
		setPeriod(Integer.valueOf(options.getProperty(PERIOD_PROPERTY, DEFAULT_PERIOD)));
		setStatisticsType(ObjectId.predefined(ReferenceValue.class, "statistic.gost"));
	}

	public int getPeriod() {
		return period;
	}

	public void setPeriod(int period) {
		this.period = period;
	}
}
