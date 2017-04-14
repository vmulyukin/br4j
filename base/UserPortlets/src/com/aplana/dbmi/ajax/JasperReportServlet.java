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
package com.aplana.dbmi.ajax;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.jasperreports.CreatorChart;
import com.aplana.dbmi.jasperreports.DefReport;
import com.aplana.dbmi.jasperreports.IGettingXMLDataSource;
import com.aplana.dbmi.jasperreports.JasperReportFileNameFormer;
import com.aplana.dbmi.service.DataServiceBean;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRXmlDataSource;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JExcelApiExporter;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.remoting.samples.chat.exceptions.DatabaseException;
import org.w3c.dom.Document;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class JasperReportServlet extends HttpServlet {
	protected final Log logger = LogFactory.getLog(getClass());
	public static final String CONFIG_FILE_PREFIX = "dbmi/jasperReports/";
	public static final String PARAM_NAME_CONFIG = "nameConfig"; // ��� ��������� ������� ��������� ��� ����. ������
	public static final String CONFIG_BOUND_CARDS = "reportChartBoundCards";
	public static final String PARAM_CARDID = "cardId";
	public static final String PARAM_EXPORT_FORMAT = "exportFormat";
	public static final String PARAM_FILE_NAME = "fileName";
	public static final String OVER_PARAM_EXPORT_FORMAT = "overExportFormat";
	public static final String REPORT_ERROR_PARAM = "reportError";
	public static final String ERROR_MESSAGE_PARAM = "errorMessage";
	//��� ������ � ������� ����������� Data Source
	//������ ������ - ���_���������_������:�����_���_���������_���������_������:���������:����_�_����_�������_�����_�������
	public static final String CONFIG_DATA_SOURCE = "dataSource";
	
	public static final String PREFIX_TYPEDATA_STRING = "S_";
	public static final String PREFIX_TYPEDATA_DATE = "D_";
	public static final String PREFIX_TYPEDATA_LONG = "L_";
	public static final String PREFIX_TYPEDATA_SELECTKIT = "K_";
	public static final String PREFIX_TYPEDATA_PERSONS = "P_";
	public static final String PREFIX_TYPEDATA_VALUESREF = "R_";
	public static final String PREFIX_TYPEDATA_BOOLEAN = "B_";
	public static final String PREFIX_TYPEDATA_CARDS = "Cs_";
	public static final String PREFIX_TYPEDATA_RADIO = "Ra_";
	
	public static final String DATE_FORMAT = "yyyy-MM-dd";
	
	public static final String EXPORT_PDF = "PDF";
	public static final String EXPORT_DOCX = "DOCX";
	public static final String EXPORT_XLS = "XLS";
	public static final String EXPORT_XLSX = "XLSX";
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		ServletOutputStream out = resp.getOutputStream();
		try {
			writeReportToStream(req, resp, out);
		} finally {
			IOUtils.closeQuietly(out);
		}
	}
	
	protected void writeReportToStream(HttpServletRequest req, HttpServletResponse resp, OutputStream out) 
			throws ServletException, IOException  {
		//���� ����� ������ ���� � ��������� ����, ��� ����� ������������ ���������� ��-�� ����� ���� ������ ���������� �����
		String reportError = req.getParameter(REPORT_ERROR_PARAM);
		if(reportError != null && "true".equalsIgnoreCase(reportError)) {
			String messageError = req.getParameter(ERROR_MESSAGE_PARAM);
			if(messageError != null) {
				throw new ServletException(messageError);
			} else {
				throw new ServletException("Executing Report Error. Call to Technical Support please.");
			}
		}
		// ��� ����. �����
		String configName = req.getParameter(PARAM_NAME_CONFIG);
		// ��� ������� ��������
		String exportFormat;
		if(req.getParameter(OVER_PARAM_EXPORT_FORMAT)==null)
			exportFormat = req.getParameter(PARAM_EXPORT_FORMAT);
		else
			exportFormat = req.getParameter(OVER_PARAM_EXPORT_FORMAT).substring(PREFIX_TYPEDATA_RADIO.length());
		// Data Source
		String dSource = req.getParameter(CONFIG_DATA_SOURCE);
		// ��������� �������
		Map<String, Object> params = getParamsReport(req);
		
		// �������� ���� � UserPortlets
		/*String serverPath = (req.isSecure() ? "https" : "http") + "://" + req.getServerName() + ":" + req.getServerPort();	
		String path = serverPath+req.getContextPath()+"/jasper/";
		params.put("path", path);*/
		
		//�������� ������ ��������� ���� � �������� ���������� � ��������� http �� jndi
		URL jasperUri = req.getSession().getServletContext().getResource("/jasper");
		params.put("path", jasperUri.toString() + "/");
		
		//����� ������������, ������������ ����� (���������� � ��������� �������)
		if (req.getSession().getAttribute(DataServiceBean.USER_NAME) != null) {
		    params.put("user", req.getSession().getAttribute(DataServiceBean.USER_NAME));
		} else {
		    params.put("user", req.getUserPrincipal().getName());
		}
		// �������� ��������� ������������� �������� ��������� ������
		DefReport defRC = new DefReport();
		try {
			InputStream stream = Portal.getFactory().getConfigService().loadConfigFile(
					CONFIG_FILE_PREFIX+configName+".xml");
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream);
			defRC.setConfig(doc);
		} catch (Exception e) {
			logger.error("�� ������� �������� ���������������� ���� " + configName, e);
		}
		
		// ������ ������ ��������� � ��������� �� � ���������
		Connection conn = null;
		try {
			Map<String, String> namesCharts = defRC.getNamesCharts(); // ��� ���������� ��������� - ��� ���������
			conn = getConnection();
			for (Map.Entry<String, String> entry : namesCharts.entrySet()) {
				String varChart = entry.getKey();
				String nameChart = entry.getValue();
				InputStream stream = Portal.getFactory().getConfigService().loadConfigFile(
						CONFIG_FILE_PREFIX + nameChart + ".xml");
				Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream);
				BufferedImage chart = CreatorChart.createChart(doc, params, conn);
				params.put(varChart, chart);
			}
		} catch (Exception e) {
			logger.error("������ � ���������� ���������",e);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					logger.error("������ ��� �������� ���������� � ��",e);
				}
			}
		}
		
		// ��������� ������ JasperReport
		InputStream stream =Portal.getFactory().getConfigService().loadConfigFile(
				CONFIG_FILE_PREFIX+defRC.getNameReport()+".jasper");
		conn = null;
		try {
			JasperPrint jasperPrint;
			if (defRC.isQuery()) {
				conn = getConnection();
				jasperPrint = getJasperPrint(stream, params, dSource,conn,req );
			} else {
				jasperPrint = JasperFillManager.fillReport( stream, params, new JREmptyDataSource() );
			}
			
			
			JRExporter exporter = setOutType(exportFormat, resp);
			
			setFileName(req.getParameter(PARAM_FILE_NAME), exportFormat, resp);
			
			exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
			exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, out);
			exporter.exportReport();
		} catch (Exception e) {
			logger.error("Error during jasper report exporting", e);
		} 
		finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					logger.error("������ ��� �������� ���������� � ��",e);
				}
			}
		}
	}
	private void setFileName(String fileName, String exportFormat, HttpServletResponse resp) {
		if(fileName != null) {
			JasperReportFileNameFormer jrfnf = new JasperReportFileNameFormer(exportFormat);
			String contentDisposition = jrfnf.getContentDisposition(fileName);
			resp.setHeader("Content-Disposition", contentDisposition);
		}
	}

	private JRExporter setOutType(String exportFormat, HttpServletResponse resp) {
		if (EXPORT_DOCX.equals(exportFormat)) {
			resp.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
			return new JRDocxExporter();
		} else if (EXPORT_XLS.equals(exportFormat)) {
			resp.setContentType("application/vnd.ms-excel");
			return new JExcelApiExporter();
		} else if (EXPORT_XLSX.equals(exportFormat)) {
			resp.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
			return new JRXlsxExporter();
		} else if (EXPORT_PDF.equals(exportFormat)) {
			resp.setContentType("application/pdf");
			return new JRPdfExporter();
		} else {
			resp.setContentType("application/pdf");
			return new JRPdfExporter();
		}
	}

	private Map<String, Object> getParamsReport(HttpServletRequest req) {
		Map<String, Object> pars = new HashMap<String, Object>();
		Enumeration en = req.getParameterNames();
		while (en.hasMoreElements()) {
			String key = (String) en.nextElement();
			if (!key.equals(PARAM_NAME_CONFIG) && !key.equals(PARAM_EXPORT_FORMAT)&&!key.equals(OVER_PARAM_EXPORT_FORMAT)) {
				Object objValuePar = getObjectParameter(req.getParameter(key));
				pars.put(key, objValuePar);
				// ��������-����, ����, ��� �������� key �����
				pars.put(key+"_ISNULL", Boolean.FALSE);
			}
		}
		return pars;
	}

	private Object getObjectParameter(String strValue) {
		if (strValue == null || "".equals(strValue)) {
			return null;
		}
		if (strValue.startsWith(PREFIX_TYPEDATA_STRING)) {
			return strValue.substring(PREFIX_TYPEDATA_STRING.length());
		} else if (strValue.startsWith(PREFIX_TYPEDATA_DATE)) {
			String strDate = strValue.substring(PREFIX_TYPEDATA_DATE.length());
			try {
				if (strDate.equals("")) {
					return null;
				} else {
					long time = (new SimpleDateFormat(DATE_FORMAT)).parse(strDate).getTime(); 
					return new Timestamp(time);
				}
			} catch (ParseException e) {
				logger.error(e.getMessage(), e);
			}
		} else if (strValue.startsWith(PREFIX_TYPEDATA_LONG)) {
			String strLong = strValue.substring(PREFIX_TYPEDATA_LONG.length());
			return Long.valueOf(strLong.trim());
		} else if (strValue.startsWith(PREFIX_TYPEDATA_SELECTKIT)) {
			String choice = strValue.substring(PREFIX_TYPEDATA_SELECTKIT.length());
			return choice;
		} else if (strValue.startsWith(PREFIX_TYPEDATA_PERSONS)) {
			String persons = strValue.substring(PREFIX_TYPEDATA_PERSONS.length());
			return persons;
		} else if (strValue.startsWith(PREFIX_TYPEDATA_VALUESREF)) {
			String valuesRef = strValue.substring(PREFIX_TYPEDATA_VALUESREF.length());
			return valuesRef;
		} else if (strValue.startsWith(PREFIX_TYPEDATA_CARDS)) {
			String cardsIds = strValue.substring(PREFIX_TYPEDATA_CARDS.length());
			return cardsIds;
		} else if (strValue.startsWith(PREFIX_TYPEDATA_BOOLEAN)) {
			String valuesRef = strValue.substring(PREFIX_TYPEDATA_BOOLEAN.length());
			return Boolean.valueOf(valuesRef);
		} else if(strValue.startsWith(PREFIX_TYPEDATA_RADIO)){
			String valuesRef = strValue.substring(PREFIX_TYPEDATA_RADIO.length());
			return Long.parseLong(valuesRef);
		}
		return null;
	}
	/**
	 *
	 * @throws SQLException
	 * @throws NamingException
	 */
	public static Connection getConnection() throws SQLException, NamingException {
		InitialContext context = new InitialContext();
		DataSource dataSource = (DataSource) context.lookup("java:/jdbc/DBMIDS");
		Connection con = dataSource.getConnection();
		return con;
	}
	/**
	 * ���������� data source � ���� xml �����(���������)
	 * @return JRXmlDataSource
	 * @throws JRException
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws DatabaseException 
	 */
	public JRXmlDataSource getXMLConnection(String dSource, HttpServletRequest req) throws JRException, ClassNotFoundException, InstantiationException, IllegalAccessException, DatabaseException {
		Class xmlDataSourceClass = this.getClass().getClassLoader().loadClass(dSource);
		IGettingXMLDataSource xmlDataSource = (IGettingXMLDataSource) xmlDataSourceClass.newInstance();
		xmlDataSource.setParameters(getParamsReport(req));
		JRXmlDataSource jrxmlds = new JRXmlDataSource(xmlDataSource.getXML(),xmlDataSource.getRecordPath());
		return jrxmlds;
	}

	/**
	 * ������� ���������� ������ jasperPrint � ����������� �� data source
	 * @return jasperPrint
	 * @throws JRException
	 * @throws SQLException
	 * @throws NamingException
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws ClassNotFoundException 
	 * @throws DatabaseException 
	 */
	public JasperPrint getJasperPrint(InputStream stream,Map<String, Object> params,String dSource,Connection conn, HttpServletRequest req  ) throws JRException, SQLException, NamingException, ClassNotFoundException, InstantiationException, IllegalAccessException, DatabaseException{
		JasperPrint jasperPrint;
		params.put("locale", req.getLocale());
		params.put(JRParameter.REPORT_LOCALE, req.getLocale());
		if (dSource!=null){
			jasperPrint = JasperFillManager.fillReport( stream, params, getXMLConnection(dSource,req) );
		}
		//���� sourceType = xml, �� �������� jasperPrint � ��������������� �����������
		else {
			jasperPrint = JasperFillManager.fillReport( stream, params, conn );
		}	
		return jasperPrint;
	}
}
