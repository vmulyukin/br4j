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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ValidateDepartmentHierarchy { 

	protected static Log logger = LogFactory.getLog(ValidateDepartmentHierarchy.class);
	
	public static Boolean validate(Connection conn) {
		try {
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(getSql());

			rs.next();
			boolean isValid = rs.getBoolean(1);
			if(!isValid){
				logger.error("Fixated department hierarchy.");
			}
			return isValid;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	
	private static String getSql() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("WITH RECURSIVE search_up(cycle_id, cycle_number, cycle_base) AS ( \n")
			    		.append("select cast(c.card_id::bigint as numeric),1, c.card_id from card c\n")
			    		.append("where c.status_id in (4,7) and c.template_id = 484\n")
			    		.append("UNION ALL\n")
			    		.append("SELECT v.number_value, su.cycle_number+1, su.cycle_base\n")
			    		.append("FROM attribute_value v, search_up su\n")
			    		.append("WHERE v.attribute_code = 'JBR_DEPT_PARENT_LINK'\n")
			    		.append("AND v.card_id = su.cycle_id and su.cycle_number < 322\n")
			    		.append("and exists (select 1 from attribute_value\n")
			    		.append("where attribute_code = 'JBR_DEPT_PARENT_LINK'\n")
			    		.append("and card_id = v.number_value))\n")
			    		.append("select max(cycle_number) < 322 from search_up\n");
		return stringBuilder.toString();
	}
}
