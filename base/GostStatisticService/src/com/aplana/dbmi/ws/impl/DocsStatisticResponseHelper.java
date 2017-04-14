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
package com.aplana.dbmi.ws.impl;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.action.SQLQueryAction;
import com.aplana.dbmi.service.DataService;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.DataServiceHome;
import com.aplana.dbmi.service.SystemUser;
import com.aplana.dbmi.ws.docstatisticservice.DocsStatisticResponse;
import com.aplana.dbmi.ws.docstatisticservice.OrganizationStatistic;
import com.aplana.dbmi.ws.docstatisticservice.TemplateStatistic;

public class DocsStatisticResponseHelper {

	private static final String DOCS_STATISTIC_SQL = "dbmi/statistics/docsStatisticSQL.sql";
	
	private static final Log logger = LogFactory.getLog(DocsStatisticResponseHelper.class);
	
	private static final String START_PARAM = "startDate";
	private static final String END_PARAM = "endDate";
	
	private static final String TEMPLATE_FIELD = "template";
	private static final String ORG_FIELD = "organization";
	private static final String COUNT_FIELD = "count";

	private static DataServiceBean serviceBean;
	
	public static DocsStatisticResponse getDocsStatisticResponse(DataServiceBean serviceBean, Date startPoint, Integer startInterval, Integer endInterval) {
		InputStream sqlFileStream = null;
		try {
			sqlFileStream = Portal.getFactory().getConfigService().loadConfigFile(DOCS_STATISTIC_SQL);

			String sql = convertStreamToString(sqlFileStream);
			SQLQueryAction action = new SQLQueryAction();
			action.setSql(sql);
			
			final Date start = resetTimeZone(convertToPeriodDate(startPoint, startInterval, true));
			final Date end = resetTimeZone(convertToPeriodDate(startPoint, endInterval, false));
			
			final MapSqlParameterSource params = new MapSqlParameterSource();
			params.addValue(START_PARAM, start, SQLQueryAction.PARAM_DATE);
			params.addValue(END_PARAM, end, SQLQueryAction.PARAM_DATE);
			action.setParams(params);

			final List<Map> result = (List<Map>) serviceBean.doAction(action);
			
			return buildDocsStatistic(result);
		} catch(Exception  e) {
			logger.error(e);
		} finally {
			IOUtils.closeQuietly(sqlFileStream);
		}
		return null;
	}
	
	/**
	 * @param startPoint
	 * @param interval - day interval 
	 * @param start - 	true - ������ ������� (+ interval �� startPoint + 00:00:01)
	 * 					false - ����� ������� (+ interval �� startPoint + 23:59:59)
	 * @return
	 */
	private static Date convertToPeriodDate(Date startPoint, int interval, boolean start) {
		
		final Calendar cal = Calendar.getInstance();
		cal.setTime(startPoint);
		cal.add(Calendar.DAY_OF_MONTH, interval);
		int hours = start? 0 : 23;
		cal.set(Calendar.HOUR_OF_DAY, hours);
		cal.set(Calendar.HOUR, hours);
		int minutes = start? 0 : 59;
		cal.set(Calendar.MINUTE, minutes);
		int seconds = start? 1 : 59;
		cal.set(Calendar.SECOND, seconds);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}
	
	private static Date resetTimeZone(Date dt) {
		if (dt != null) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(dt.getTime());
			calendar.set(Calendar.ZONE_OFFSET, 0);
			calendar.set(Calendar.DST_OFFSET, 0);
			return dt = calendar.getTime();
		}
		return null;
	}
	
	private static DocsStatisticResponse buildDocsStatistic(List<Map> resultRows) {
		final DocsStatisticResponse stat = new DocsStatisticResponse();
		final Map<String, OrganizationStatistic> orgStatistics = new HashMap<String, OrganizationStatistic>();
		for(Map row : resultRows) {
			
			TemplateStatistic templateStat = new TemplateStatistic();
			templateStat.setTemplate(row.get(TEMPLATE_FIELD).toString());
			String countStr = row.get(COUNT_FIELD).toString();
			int count = (countStr!= null && !countStr.equals("")) ? Integer.parseInt(countStr) : 0 ;
			templateStat.setCount(count);
			
			String orgName = row.get(ORG_FIELD).toString();
			if(!orgStatistics.containsKey(orgName)) {
				OrganizationStatistic orgStat = new OrganizationStatistic();
				orgStat.setOrgName(orgName);
				
				List<TemplateStatistic> templateStats = new ArrayList<TemplateStatistic>();
				templateStats.add(templateStat);
				
				orgStat.setTemplateStatistics(templateStats);
				
				orgStatistics.put(orgName, orgStat);
			} else {
				OrganizationStatistic orgStat = orgStatistics.get(orgName);
				orgStat.getTemplateStatistics().add(templateStat);
			}
			
		}
		stat.setOrgStatistics(orgStatistics.values());
		return stat;
	}
	
	private static String convertStreamToString(InputStream is) {
	    java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
	    return s.hasNext() ? s.next() : "";
	}
}
