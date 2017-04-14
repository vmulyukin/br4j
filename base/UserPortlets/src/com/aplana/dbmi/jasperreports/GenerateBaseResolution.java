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

import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.JRDataSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GenerateBaseResolution {
	private XPathExpression recordExpression; 
	private Connection conn = null;
	private List <BaseResolution> records = null;
	private XPath xpath;
	
	public GenerateBaseResolution() {
		XPathFactory factory = XPathFactory.newInstance();
		xpath = factory.newXPath();
		records = new LinkedList();
	}
	
	
	
	public JRDataSource generate(Connection conn, Long id) {
		this.conn = conn;
		try {
			Statement stmt = this.conn.createStatement();
			String sql = "select string_value from attribute_value " +
			"where attribute_code='ADMIN_221090' and card_id="+id;
			ResultSet rs = stmt.executeQuery(sql);
			
			while (rs.next()) {
				ByteArrayInputStream resolutions = (ByteArrayInputStream)rs.getBinaryStream(1);
				List record = getResolutions(resolutions);
				records.addAll(record);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return new JRBeanCollectionDataSource(records);
		}
	}
	
	private List <BaseResolution> getResolutions (ByteArrayInputStream resolutions) throws Exception {
		List<BaseResolution> result = new LinkedList();
		if (resolutions != null && resolutions.available() != 0) {
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(resolutions);
			recordExpression = xpath.compile("./document/Task");
			NodeList recordNodes = (NodeList)recordExpression.evaluate(doc, XPathConstants.NODESET);
			for (int i=0; i < recordNodes.getLength(); i++) {
				Element recordEl = (Element)recordNodes.item(i);
				String text = recordEl.getAttribute("task_text");
				String term = recordEl.getAttribute("deadline");
				XPathExpression creationDateExp = xpath.compile("./TaskNumber");
				Element  creationDateElem = (Element)creationDateExp.evaluate(recordEl, XPathConstants.NODE);
				String creationDate = creationDateElem.getAttribute("taskDate");
				String author = new String();
				XPathExpression authorExp = xpath.compile("./Author/Organization");
				Element  authorExpElem = (Element)authorExp.evaluate(recordEl, XPathConstants.NODE);
				author = author + authorExpElem.getAttribute("shortname") +": ";
				authorExp = xpath.compile("./Author/Organization/OfficialPerson/Name");
				authorExpElem = (Element)authorExp.evaluate(recordEl, XPathConstants.NODE);
				author = author + authorExpElem.getTextContent();
				authorExp = xpath.compile("./Author/Organization/OfficialPerson/Official");
				authorExpElem = (Element)authorExp.evaluate(recordEl, XPathConstants.NODE);
				author = author + " " + authorExpElem.getAttribute("post");
				recordExpression = xpath.compile("./Executor");
				NodeList executorsNodes = (NodeList)recordExpression.evaluate(recordEl, XPathConstants.NODESET);
				String executor = new String();
				for (int j=0; j < executorsNodes.getLength(); j++) {
					Element recordExecutor = (Element)executorsNodes.item(j);
					XPathExpression executorExp = xpath.compile("./Organization");
					Element executorElem = (Element)executorExp.evaluate(recordExecutor, XPathConstants.NODE);
					executor = executor + executorElem.getAttribute("fullname") +": ";
					executorExp = xpath.compile("./Organization/OfficialPerson/Name");
					executorElem = (Element)executorExp.evaluate(recordExecutor, XPathConstants.NODE);
					executor = executor + executorElem.getTextContent();
					executorExp = xpath.compile("./Organization/OfficialPerson/Official");
					executorElem = (Element)executorExp.evaluate(recordExecutor, XPathConstants.NODE);
					executor = executor + " " + executorElem.getAttribute("post") +"; ";
				}
				result.add( new BaseResolution(author, text, creationDate, term, executor));
			}
		}
		return result;
	}

}
