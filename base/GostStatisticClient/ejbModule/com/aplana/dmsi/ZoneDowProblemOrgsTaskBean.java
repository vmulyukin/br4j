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

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.action.SQLQueryAction;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.ReferenceValue;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.InputStream;
import java.util.Calendar;
import java.util.Map;
import java.util.Properties;

/**
 * EJB для задачника поиска органищаций и документов без Зон ДОУ. Формирует отчет по организациям и проставляет
 * Зону ДОУ в документы.
 */
public class ZoneDowProblemOrgsTaskBean extends AbstractStatisticTaskBean {

	private static final long serialVersionUID = 1L;
	private Log logger = LogFactory.getLog(getClass());
	
	private static final String CONFIG_FILE = "dbmi/statistics/zoneDowOrgProblems/zd_problem_orgs_config.properties";
	private static final String UPDATE_ZD_SQL = "dbmi/statistics/zoneDowOrgProblems/updateZdInCards.sql";
	private static final String BASE_REPORT_NAME = "Orgs_bez_DOW_";
	
	private static final String TIMEOUT_PROPERTY = "timeout";
	
	private static Boolean working = false;
	
	private long wsTimeout; // ws connection timeout (5 minutes by default)
	
	private int startInterval;
	private int endInterval;
	
	@Override
	public void process(Map parameters) {
		logger.info("ZoneDowProblemOrgsTask is started");
		
		synchronized (ZoneDowProblemOrgsTaskBean.class) {
		    if (working) {
			    logger.info("ZoneDowProblemOrgsTask is already started. Skip this");
		    	return;
		    }
		    working = true;
		}
		
		Exception exception = null;
		Card newMaterialCard = null;
		InputStream updateZdSqlStream = null;
		try {
			logger.info("Start update ZoneDOW in documents. SQL = " + UPDATE_ZD_SQL);
			updateZdSqlStream = Portal.getFactory().getConfigService().loadConfigFile(UPDATE_ZD_SQL);
			String updateZdSql = IOUtils.toString(updateZdSqlStream);
			SQLQueryAction sqlQueryAction = new SQLQueryAction();
			sqlQueryAction.setSql(updateZdSql);
			sqlQueryAction.setOnlyUpdate(true);
			getServiceBean().doAction(sqlQueryAction);

			readProperties(CONFIG_FILE);

			ZoneDowProblemOrgsReportGenerator reportGenerator = new ZoneDowProblemOrgsReportGenerator(
					getUsername(),
					getPassword(), 
					getAddresses(), 
					getWsTimeout());
			InputStream reportStream  = reportGenerator.generateReport();
			if(reportStream == null){
				logger.info("Report is null, return");
				synchronized (ZoneDowProblemOrgsTaskBean.class) {
					working = false;
				}
				return;
			}
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
			if(newMaterialCard != null) {
				createStatisticCard(newMaterialCard, exception);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			IOUtils.closeQuietly(updateZdSqlStream);
		    synchronized (ZoneDowProblemOrgsTaskBean.class) {
				working = false;
			}
		}
		
	}
	
	protected void setParameters(Properties options) {
		super.setParameters(options);
		String timeoutProp = options.getProperty(TIMEOUT_PROPERTY);
		if(timeoutProp != null && !timeoutProp.equals(""))
			setWsTimeout(Long.parseLong(timeoutProp));
		setStatisticsType(ObjectId.predefined(ReferenceValue.class, "zd.problem.orgs"));
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
}
