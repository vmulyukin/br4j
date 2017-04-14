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
import com.aplana.dbmi.ws.zdproblemsserviceproxy.*;
import com.sun.xml.ws.client.BindingProviderProperties;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JExcelApiExporter;
import net.sf.jasperreports.engine.export.JExcelApiExporterParameter;
import net.sf.jasperreports.engine.util.FileBufferedOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceException;
import java.io.InputStream;
import java.util.*;

/**
 * Generates report by data retrieved from web ZoneDowProblemsService
 * @author echirkov
 *
 */
public class ZoneDowProblemOrgsReportGenerator {

	private static final Log logger = LogFactory.getLog(ZoneDowProblemOrgsReportGenerator.class);

	private static final String JASPER_PATH = "dbmi/jasperReports/zdProblemOrgsReport.jasper";

	private static final String ACCESS_ERROR = "�� ������� �������� ���������� � �������";

	private static final String REPORT_SHEET_NAME = "����������� ��� \"���� ���\"";

	private String username;
	private String password;
	private Set<String> addresses;
	private long timeout;

	public ZoneDowProblemOrgsReportGenerator(
			String username,
			String password,
			Set<String> addresses,
			long timeout) {
		this.username = username;
		this.password = password;
		this.addresses = addresses;
		this.timeout = timeout;
	}
	
	private ZoneDowProblemsService  getService(String address) {
		ZoneDowProblemsServiceImplService service = new ZoneDowProblemsServiceImplService(null, new QName("http://impl.ws.dbmi.aplana.com/", "DocsStatisticServiceImplService"));
		ZoneDowProblemsService servicePort = service.getZoneDowProblemsServiceImplPort();
		BindingProvider bindingProvider = (BindingProvider) servicePort;
		bindingProvider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, address + "/GostStatisticService/zdProblemOrgs");

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

			if(addresses == null) {
				return null;
			}
			List<Map<String,Object>> rows = getData(addresses);
			if(rows.isEmpty()){
				logger.info("No data for report, return");
				return null;
			}
			JasperPrint jrPrint = JasperFillManager.fillReport(jasperFile, params, new JRBeanCollectionDataSource(rows));
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

	/**
	 * Retrieves data from web services by provided ip addresses
	 * @param addresses web service ip address
	 * @return
	 */
	private List<Map<String, Object>> getData(Set<String> addresses) {
		final List<Map<String,Object>> resultStatistics = new LinkedList<Map<String,Object>>();
		final List<String> failedIpList = new ArrayList<String>();
		
		for(final String address: addresses) {
			try {
				logger.info("Trying to retrieve data from web service with ip: "+address);
				ZoneDowProblemsService service = getService(address);
				if(service != null) {
					
					ZoneDowProblemsResponse response = service.getZDProblemOrgsStat();
					if(response != null) {
						resultStatistics.add(new HashMap<String, Object>());
						resultStatistics.add(new HashMap<String, Object>(){{
							put("name", address);
						}});
						for(ZdOrganisation zdOrganisation: response.getOrganisationsWithProblems()){
							resultStatistics.add(transformZdOrganisation(zdOrganisation));
						}
						logger.info("Data was retrieved successfully from web service with ip: "+address);
					} else {
						logger.info("There is no data from server " + address);
					}
				} else throw new IllegalStateException();
			} catch(IllegalStateException e) {
				failedIpList.add(address);
				logger.error("Cannot retrieve data from " + address + " server");
			} catch(WebServiceException e) {
				logger.error("Cannot access to " + address + " server");
				failedIpList.add(address);
			} catch (Exception e) {
				logger.error(e.getMessage() + " address = " + address);
				failedIpList.add(address);
			}
		}

		if(!failedIpList.isEmpty()) {
			resultStatistics.add(new HashMap<String, Object>() {{
				put("name", "������ �� �������� �");
			}});
		}
		for (final String ip: failedIpList){
			resultStatistics.add(new HashMap<String, Object>(){{
				put("name", ip);
			}});
		}
		return resultStatistics;
	}

	private Map<String, Object> transformZdOrganisation(final ZdOrganisation zdOrganisation){
		return new HashMap<String, Object>() {{
			put("card_id", zdOrganisation.getCardId());
			put("template", zdOrganisation.getTemplate());
			put("status", zdOrganisation.getStatus());
			put("name", zdOrganisation.getName());
		}};
	}

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

}
