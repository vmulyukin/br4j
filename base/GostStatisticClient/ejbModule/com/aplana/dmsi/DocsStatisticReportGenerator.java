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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;

import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JExcelApiExporter;
import net.sf.jasperreports.engine.export.JExcelApiExporterParameter;
import net.sf.jasperreports.engine.util.FileBufferedOutputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.ws.docstatisticserviceproxy.DocsStatisticResponse;
import com.aplana.dbmi.ws.docstatisticserviceproxy.DocsStatisticService;
import com.aplana.dbmi.ws.docstatisticserviceproxy.DocsStatisticServiceImplService;
import com.aplana.dbmi.ws.docstatisticserviceproxy.OrganizationStatistic;
import com.aplana.dbmi.ws.docstatisticserviceproxy.TemplateStatistic;
import com.sun.xml.ws.client.BindingProviderProperties;

/**
 * Generates report by data retrieved from web services
 * @author akhasanov
 *
 */
public class DocsStatisticReportGenerator {

	private static final Log logger = LogFactory.getLog(DocsStatisticReportGenerator.class);
	
	private static final String JASPER_PATH = "dbmi/jasperReports/docsStatisticReport.jasper";
	
	private static final String ACCESS_ERROR = "�� ������� �������� ���������� � ������� ";
	
	private static final String ORGANIZATION_FIELD = "organization";
	private static final String TEMPLATE_FIELD = "template";
	private static final String COUNT_FIELD = "count";
	
	private static final String REPORT_SHEET_NAME = "���������� �� ����������";
	
	private String username;
	private String password;
	private Set<String> addresses;
	private long timeout;
	
	private int startInterval;
	private int endInterval;
	
	public DocsStatisticReportGenerator(
			String username,
			String password,
			Set<String> addresses,
			long timeout,
			int startInterval,
			int endInterval) {
		this.username = username;
		this.password = password;
		this.addresses = addresses;
		this.timeout = timeout;
		this.startInterval = startInterval;
		this.endInterval = endInterval;
	}
	
	private DocsStatisticService  getService(String address) {

		DocsStatisticServiceImplService service = new DocsStatisticServiceImplService(null, new QName("http://impl.ws.dbmi.aplana.com/", "DocsStatisticServiceImplService"));
		DocsStatisticService servicePort = service.getDocsStatisticServiceImplPort();
		BindingProvider bindingProvider = (BindingProvider) servicePort;
		bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, address + "/GostStatisticService/docsStatistic");
		
		bindingProvider.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, username);
		bindingProvider.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, password);
		if(timeout != 0) {
			bindingProvider.getRequestContext().put(BindingProviderProperties.REQUEST_TIMEOUT, timeout);
			bindingProvider.getRequestContext().put(BindingProviderProperties.CONNECT_TIMEOUT, timeout);
		}
		return servicePort;
	}
	
	/**
	 * Generates report
	 * @return jasper report stream
	 */
	public InputStream generateReport() {
		
		long startTime = System.currentTimeMillis();
		
		try {
			
			InputStream jasperFile = Portal.getFactory().getConfigService().loadConfigFile(JASPER_PATH);
			
			JExcelApiExporter xlsExporter = new JExcelApiExporter();

			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put(JRParameter.IS_IGNORE_PAGINATION, Boolean.TRUE);

			JasperPrint jrPrint = JasperFillManager.fillReport(jasperFile, params, new CustomDataSource(null, getData(addresses)));
			jrPrint.setProperty("net.sf.jasperreports.print.keep.full.text", Boolean.TRUE.toString());
			
			List<JasperPrint> list = new ArrayList<JasperPrint>();
			list.add(jrPrint);

			xlsExporter.setParameter(JExcelApiExporterParameter.JASPER_PRINT_LIST, list);
			xlsExporter.setParameter(JExcelApiExporterParameter.SHEET_NAMES, new String[]{REPORT_SHEET_NAME});

			FileBufferedOutputStream stream = new FileBufferedOutputStream();
			xlsExporter.setParameter(JRExporterParameter.OUTPUT_STREAM, stream);
			xlsExporter.exportReport();

			if (logger.isInfoEnabled()) {
				logger.info("generateReport(): report generating process completed successfully in " + (System.currentTimeMillis() - startTime + " milliseconds"));
			}
			return stream.getDataInputStream();
		} catch(Exception e) {
			logger.error(e);
		}
		return null;
	}
	
	private DocsStatisticResponse getResponse(DocsStatisticService service) throws DatatypeConfigurationException {
		GregorianCalendar c = new GregorianCalendar();
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.HOUR, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		
		XMLGregorianCalendar date = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
		return service.getDocumentsStatistic(date, startInterval, endInterval);
	}
	
	/**
	 * Retrieves data from web services by provided ip addresses
	 * @param addresses web service ip address
	 * @return
	 */
	private List<Map<String, Object>> getData(Set<String> addresses) {
		final List<DocsStatisticResponse> resultStatistics = new ArrayList<DocsStatisticResponse>();
		final List<String> failedIpList = new ArrayList<String>();
		
		for(String address : addresses) {
			try {
				logger.info("Trying to retrieve data from web service with ip: "+address);
				DocsStatisticService service = getService(address);
				if(service != null) {
					
					DocsStatisticResponse response = getResponse(service);
					if(response != null) {
						resultStatistics.add(response);
						logger.info("Data was retrieved successfully from web service with ip: "+address);
					} else throw new IllegalStateException();
				} else throw new IllegalStateException();
			} catch(IllegalStateException e) {
				failedIpList.add(address);
				logger.warn("Cannot retrieve data from "+address+" server");
			} catch(WebServiceException e) {
				logger.warn("Cannot access to "+address+" server");
				failedIpList.add(address);
			} catch (DatatypeConfigurationException e) {
				logger.error(e);
			}
		}
		
		String emptyTemplateField = "";
		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		for(DocsStatisticResponse resp : resultStatistics) {
			for(OrganizationStatistic orgStat : resp.getOrgStatistics()) {
				for(TemplateStatistic tempStat : orgStat.getTemplateStatistics()) {
					Map<String, Object> row = new HashMap<String, Object>();
					row.put(ORGANIZATION_FIELD, orgStat.getOrgName());
					
					String template = tempStat.getTemplate();
					if(("").equals(emptyTemplateField))
						emptyTemplateField = template;
					
					row.put(TEMPLATE_FIELD, template);
					row.put(COUNT_FIELD, tempStat.getCount());
					data.add(row);
				}
			}
		}
		
		for(String ip : failedIpList) {
			Map<String, Object> row = new HashMap<String, Object>();
			row.put(ORGANIZATION_FIELD, ACCESS_ERROR + ip);
			row.put(TEMPLATE_FIELD, emptyTemplateField);
			row.put(COUNT_FIELD, 0);
			data.add(row);
		}
		
		if(data.isEmpty()) {
			Map<String, Object> row = new HashMap<String, Object>();
			row.put(ORGANIZATION_FIELD,"");
			row.put(TEMPLATE_FIELD, "");
			row.put(COUNT_FIELD, 0);
			data.add(row);
		}
		return data;
	}

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

}
