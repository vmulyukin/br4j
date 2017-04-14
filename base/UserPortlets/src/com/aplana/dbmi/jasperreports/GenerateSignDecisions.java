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
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
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

public class GenerateSignDecisions {
	public static final String TYPE_VISA = "visa";
	public static final String TYPE_SIGN = "sign";
	
	private XPathExpression recordExpression; 
	
	private Connection conn = null;
	private List/*<RecordDecision>*/ records = null;
	private XPath xpath;
	
	public GenerateSignDecisions() {
		XPathFactory factory = XPathFactory.newInstance();
		xpath = factory.newXPath();		
	}
	public JRDataSource generate(Connection conn, Long id) {
		this.conn = conn;
		
		records = new LinkedList();
		try {
			Statement stmt = this.conn.createStatement();
			ResultSet rs = stmt.executeQuery(getSql(id));
			
			while (rs.next()) {
				String type = rs.getString(1);
				ByteArrayInputStream decisions = (ByteArrayInputStream)rs.getBinaryStream(2);
				String sname = rs.getString(3);
				String org = rs.getString(4);
				String name = rs.getString(5);
				String mname = rs.getString(6);
				
				List record = getDecisions(type, sname, org, name, mname, decisions);
				records.addAll(record);
			}
			
			Collections.sort(records);
			//Collections.reverse(records);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return new JRBeanCollectionDataSource(records);
		}
	}
	
	private String getSql(Long id) {
		String sql = 
			"select	'sign' as type "+
			"	,av_decision.long_binary_value as decision "+
			"	,av_sname.string_value as sname "+
			"	,av_org.string_value as org "+
			"	,av_name.string_value as name "+
			"	,av_mname.string_value as mname "+
			"from	attribute_value av_base_visa, card cr_visa "+
			"left outer join "+
			"	attribute_value av_decision on (av_decision.card_id = cr_visa.card_id and av_decision.attribute_code = 'JBR_SIGN_COMMENT') "+
			"left outer join "+
			"	attribute_value av_visa_p on (av_visa_p.card_id = cr_visa.card_id and av_visa_p.attribute_code = 'JBR_SIGN_RESPONSIBLE') "+
			"left outer join "+
			"	person p_visa on (p_visa.person_id = av_visa_p.number_value) "+
			"left outer join "+
			"	attribute_value av_sname on (av_sname.card_id = p_visa.card_id and av_sname.attribute_code = 'JBR_PERS_SNAME') "+
			"left outer join "+
			"	attribute_value av_name on (av_name.card_id = p_visa.card_id and av_name.attribute_code = 'JBR_PERS_NAME') "+
			"left outer join "+
			"	attribute_value av_mname on (av_mname.card_id = p_visa.card_id and av_mname.attribute_code = 'JBR_PERS_MNAME') "+
			"left join attribute_value av_org_link on (av_org_link.card_id = p_visa.card_id and av_org_link.attribute_code = 'JBR_PERS_ORG') "+
			"left join attribute_value av_org on (av_org.card_id = av_org_link.number_value and av_org.attribute_code = 'JBR_DORG_SHORTNAME')" +	
			
			"where	av_base_visa.card_id = "+id.longValue()+" and av_base_visa.attribute_code = 'JBR_SIGN_SIGNING' "+
			"	and cr_visa.card_id = av_base_visa.number_value";
		return sql;
	}
	
	private List/*RecordDecision*/ getDecisions(String type, String sname, String org, String name, String mname, ByteArrayInputStream decisions) throws Exception {
		List result = new LinkedList();
		// ������ decisions
		if (decisions != null && decisions.available() != 0) {
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(decisions);
			recordExpression = xpath.compile("/report/part");
			NodeList recordNodes = (NodeList)recordExpression.evaluate(doc, XPathConstants.NODESET);
			for (int i=0; i < recordNodes.getLength(); i++) {
				Element recordEl = (Element)recordNodes.item(i);
				
				String timestamp = recordEl.getAttribute("timestamp");
				String visa_round = recordEl.getAttribute("round");
				String visa_fact_user = recordEl.getAttribute("fact-user");
				//String[] visa_fact_user_sname=visa_fact_user.split(" ");
				//visa_fact_user=visa_fact_user_sname[0];
				
				String decision = recordEl.getTextContent();
				
				result.add( new RecordDecision(type, sname, org, name, mname, timestamp, visa_round, visa_fact_user,decision,null,null,null,null,null,null, null));
				
			}
		}
		// ----------------
		return result;
	}
}
