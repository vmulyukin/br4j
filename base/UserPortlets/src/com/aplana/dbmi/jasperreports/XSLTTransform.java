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
package com.aplana.dbmi.jasperreports;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import com.aplana.dbmi.Portal;

public class XSLTTransform {
	public static final String CONFIG_FILE_XSLT = "dbmi/xslt/";
	private static final Log logger = LogFactory.getLog(XSLTTransform.class);
	private Transformer trans = null;
	private DocumentBuilder builder = null;
	
	public XSLTTransform(String nameXslt) {
		try {
			InputStream xsltStream = Portal.getFactory().getConfigService().loadConfigFile(
					CONFIG_FILE_XSLT+nameXslt);
			Source xslt = new StreamSource(xsltStream);
			
		    TransformerFactory transFact = TransformerFactory.newInstance();
			trans = transFact.newTransformer(xslt);
			
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		} catch (Exception e) {
			logger.error("������ ��� ������������� XSLTTransform", e);
		}
	}
	
	public String transform(InputStream xmlStream) {
		try {			
			Document doc = builder.parse(xmlStream);
			Source xml = new DOMSource(doc);
			
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			trans.transform( xml, new StreamResult(outStream));
			return outStream.toString("UTF-8");
		} catch (Exception e) {
			logger.error("������ ��� xslt ������������� � �������� ���������� ������ JasperReports", e);
			return "";
		}
	}
	
	public static String transform(InputStream xmlStream, String nameXslt) {
		try {
			InputStream xsltStream = Portal.getFactory().getConfigService().loadConfigFile(
					CONFIG_FILE_XSLT+nameXslt);
			Source xslt = new StreamSource(xsltStream);
			
			Transformer trans = null;
		    TransformerFactory transFact = TransformerFactory.newInstance();
			trans = transFact.newTransformer(xslt);
			
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(xmlStream);
			Source xml = new DOMSource(doc);
			
			ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			trans.transform( xml, new StreamResult(outStream));
			return outStream.toString("UTF-8");
		} catch (Exception e) {
			logger.error("������ ��� xslt ������������� � �������� ���������� ������ JasperReports", e);
			return "";
		}
	}
}
