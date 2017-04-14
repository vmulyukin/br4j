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

import java.io.InputStream;
import java.util.Calendar;
import java.util.Map;
import java.util.Properties;
import java.util.Date;

import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.ReferenceValue;

/**
 * Represents a standart task that retrieves document statistics from provided servers and generates an Excel file
 * @author ahasanov
 *
 */
public class DocsStatisticTaskBean extends AbstractStatisticTaskBean {

	private static final long serialVersionUID = 1L;
	private Log logger = LogFactory.getLog(getClass());
	
	private static final String CONFIG_FILE = "dbmi/statistics/docs_statistic_config.properties";
	private static final String BASE_REPORT_NAME = "Statistic_document_";
	
	private static final String TIMEOUT_PROPERTY = "timeout";
	private static final String START_INTERVAL_PARAM = "startInterval";
	private static final String END_INTERVAL_PARAM = "endInterval";
	private static final String YEAR = "YEAR";
	private static final String MONTH = "MONTH";
	
	private static Boolean working = false;
	
	private long wsTimeout; // ws connection timeout (5 minutes by default)
	
	private int startInterval;
	private int endInterval;
	
	@Override
	public void process(Map parameters) {
		logger.info("DocsStatisticTask is started");
		
		synchronized (DocsStatisticTaskBean.class) {
		    if (working) {
		    	if (logger.isDebugEnabled()) {
		    		logger.debug("DocsStatisticTask is already started. Skip this");
		    	}
		    	return;
		    }
		    working = true;
		}
		
		Exception exception = null;
		Card newMaterialCard = null;

		try {
			readProperties(CONFIG_FILE);
			
			DocsStatisticReportGenerator reportGenerator = new DocsStatisticReportGenerator(
					getUsername(),
					getPassword(), 
					getAddresses(), 
					getWsTimeout(),
					getStartInterval(),
					getEndInterval());
			InputStream reportStream  = reportGenerator.generateReport();
			
			newMaterialCard = createNewMaterialCard(
					reportStream,
					BASE_REPORT_NAME + DATE_FORMATTER.format(Calendar.getInstance().getTime()) + REPORT_EXTENSION
			);
		} catch (Exception e) {
			exception = e;
			if (logger.isErrorEnabled()) {
				logger.error(e.getMessage(), e);
			}
		}

		try {
			createStatisticCard(newMaterialCard, exception);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
		    synchronized (DocsStatisticTaskBean.class) {
				working = false;
			}
		}
		
	}
	
	protected void setParameters(Properties options) {
		super.setParameters(options);
		String timeoutProp = options.getProperty(TIMEOUT_PROPERTY);
		if(timeoutProp != null && !timeoutProp.equals(""))
			setWsTimeout(Long.parseLong(timeoutProp));
		setStatisticsType(ObjectId.predefined(ReferenceValue.class, "statistic.docs"));
		setStartInterval(discloseInterval(options.get(START_INTERVAL_PARAM).toString()));
		setEndInterval(discloseInterval(options.get(END_INTERVAL_PARAM).toString()));
	}

	public long getWsTimeout() {
		return wsTimeout;
	}

	public void setWsTimeout(long wsTimeout) {
		this.wsTimeout = wsTimeout;
	}

	public int getStartInterval() {
		return startInterval;
	}

	public void setStartInterval(int startInterval) {
		this.startInterval = startInterval;
	}

	public int getEndInterval() {
		return endInterval;
	}

	public void setEndInterval(int endInterval) {
		this.endInterval = endInterval;
	}
	
	private int discloseInterval(String interval){
		if(YEAR.equals(interval)){
			return 1 - (int)DateUtils.getFragmentInDays(new Date(), Calendar.YEAR);
		} else if(MONTH.equals(interval)){
			return 1 - (int)DateUtils.getFragmentInDays(new Date(), Calendar.MONTH);
		} else {
			return Integer.parseInt(interval);
		}
	}

}
