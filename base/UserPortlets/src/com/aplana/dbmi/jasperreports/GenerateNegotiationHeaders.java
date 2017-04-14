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
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class GenerateNegotiationHeaders {
	
	private XPathExpression recordExpression; 
	
	private Connection conn = null;
	private static final String MSG_ALL = "���";
	
	public GenerateNegotiationHeaders() {
		}

	public String generateString(Connection conn, String ids) {
		this.conn = conn;
		if((ids==null)||"".equals(ids)||"null".equals(ids)){
			return MSG_ALL;
		}
		String result = "";
		try {
			Statement stmt = this.conn.createStatement();
			ResultSet rs = stmt.executeQuery(getSql(ids));

			while (rs.next()) {
				String full_name = rs.getString(1);
				result = result + full_name + ", ";
			}
			if(result.length()>2){
				result = result.substring(0,result.length()-2);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return result;
		}
	}

	
	private String getSql(String ids) {
		String sql = "select string_value from attribute_value where card_id in (" + ids + ") "+
				"and attribute_code = 'NAME' "+
				"order by string_value asc";
		return sql;
	}
}
