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

import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

// ����� ������ ���������� � ����� ������, � � ������ ��������� �������� � ������ � ����� ��������� �� �������������
// , � ����� � ��������� xslt ����������������
// �������� ����� readConfig ��� ���������� ���������� �� ����������������� �����
public class DefReport {
	private String report;
	private Map charts; // ��� ��������� -> ��� ���������
	private boolean isQuery; // ���� ����, ��� ����. ���� jasper ������ �������� sql ������
	private Map images; // ��� ����������� -> �������� ����� �����������
	
	public DefReport() throws XPathExpressionException {
		charts = new HashMap();
		isQuery = true;
		images = new HashMap();
	}
	
	public void setConfig(Document doc) throws XPathExpressionException {
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		XPathExpression reportExpression = xpath.compile("/reportChart/report");
		XPathExpression chartsExpression = xpath.compile("/reportChart/chart");
		XPathExpression imagesExpression = xpath.compile("/reportChart/image");
		
		Element reportEl = (Element)reportExpression.evaluate(doc, XPathConstants.NODE);
		report = reportEl.getAttribute("name");
		String strIsQuery = reportEl.getAttribute("isQuery");
		if (strIsQuery != null && strIsQuery.equals("false")) {
			isQuery = false;
		}
		
		NodeList chartNodes = (NodeList)chartsExpression.evaluate(doc, XPathConstants.NODESET);
		for (int i=0; i < chartNodes.getLength(); i++) {
			Element chartEl = (Element)chartNodes.item(i);
			charts.put(chartEl.getAttribute("parameter"), chartEl.getAttribute("name"));
		}
		
		NodeList imageNodes = (NodeList)imagesExpression.evaluate(doc, XPathConstants.NODESET);
		for (int i=0; i < imageNodes.getLength(); i++) {
			Element imageEl = (Element)imageNodes.item(i);
			images.put(imageEl.getAttribute("parameter"), imageEl.getAttribute("name"));
		}
	}
	
	public String getNameReport() {
		return report;
	}
	public Map getNamesCharts() {
		return charts;
	}

	public boolean isQuery() {
		return isQuery;
	}

	public Map getNamesImages() {
		return images;
	}
}