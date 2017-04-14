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

public class GenerateResolutionDecisions {
	public static final String TYPE_VISA = "visa";
	public static final String TYPE_SIGN = "sign";
	
	private XPathExpression recordExpression; 
	
	private Connection conn = null;
	private List/*<RecordDecision>*/ records = null;
	private XPath xpath;
	private List/*<RecordDecision>*/ res_ids = null;
	
	public GenerateResolutionDecisions() {
		XPathFactory factory = XPathFactory.newInstance();
		xpath = factory.newXPath();
		records = new LinkedList();
		res_ids = new LinkedList();
		}
	//not need in BR4J00016005
	/*static Comparator<RecordDecision> snorderer = new Comparator<RecordDecision>() {

     	public int compare(RecordDecision rd1, RecordDecision rd2) {
			// TODO Auto-generated method stub
			 return rd1.getSname().compareTo(rd2.getSname());
		}
    };*/

	public JRDataSource generate(Connection conn, Long id) {
		this.conn = conn;
		try {
			Statement stmt = this.conn.createStatement();
			ResultSet rs = stmt.executeQuery(getSql(id));
			
	
			while (rs.next()) {
				Long id_res = rs.getLong(1);
				
				ByteArrayInputStream decisions = (ByteArrayInputStream)rs.getBinaryStream(2);
				String sname = rs.getString(3);
				String org = rs.getString(4);
				
				String status = rs.getString(5);
							
				List record = getDecisions(sname, sname, org, null, null, decisions, status, rs.getLong(6), rs.getLong(8));
				records.addAll(record);
				/*if(!res_ids.contains(id_res)){
				generate(conn,id_res);	
				}
				res_ids.add(id_res);*/
			}
			//BR4J00016005 Sorting by Sname moved to SQL 
			//Collections.sort(records, snorderer);
			//Collections.reverse(records);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return new JRBeanCollectionDataSource(records);
		}
	}
	
	private String getSql(Long id) {
		String sql = 
				//executor
				"(select	c.card_id as c_res, "+
			"av_rep_xml.long_binary_value as rep_xml, "+
			"('���. '||coalesce(av_sname_aut.string_value, p_exec.full_name)) as sname_aut, " +
			"av_org.string_value as org," +
			"c_rep_status.name_rus as status, " +
			"av_res_rep.card_id as rep_id, " +
			"1 as number, "+ //number to sorting
			"c_rep_status.status_id as rep_status_id "+
			"from 	card c "+
			"left outer join attribute_value av_res_rep on "+
				"(av_res_rep.number_value = c.card_id and av_res_rep.attribute_code ='ADMIN_702311') "+
			"left outer join attribute_value av_res_user on "+ 
				"(av_res_user.card_id = c.card_id and av_res_user.attribute_code ='JBR_INFD_EXEC_LINK') "+
			"left join card rep_c on rep_c.card_id = av_res_rep.card_id " +
			"left join card_status c_rep_status on (c_rep_status.status_id = rep_c.status_id) "+
			"join attribute_value av_rep_aut on "+
				"(av_rep_aut.card_id = av_res_rep.card_id and av_rep_aut.attribute_code ='ADMIN_702335') "+
			"left outer join person p_exec on (p_exec.person_id = av_rep_aut.number_value) "+
			"left outer join attribute_value av_sname_aut on (av_sname_aut.card_id = p_exec.card_id and av_sname_aut .attribute_code = 'JBR_PERS_SNAME_NM') "+
			"left join attribute_value av_org_link on (av_org_link.card_id = p_exec.card_id and av_org_link.attribute_code = 'JBR_PERS_ORG') "+
			"left join attribute_value av_org on (av_org.card_id = av_org_link.number_value and av_org.attribute_code = 'JBR_DORG_SHORTNAME')" +	
			"left outer join attribute_value av_rep_xml on "+
			"(av_rep_xml.card_id =av_res_rep.card_id and av_rep_xml.attribute_code = 'ADMIN_702354') "+
			"where 	c.card_id = "+id.longValue()+ " and av_rep_aut.number_value = av_res_user.number_value " +
			" UNION "+
			//co-executor
			"select	c.card_id as c_res, "+
			"av_rep_xml.long_binary_value as rep_xml, "+
			"coalesce(av_sname_aut.string_value, p_exec.full_name) as sname_aut, " +
			"av_org.string_value as org," +
			"c_rep_status.name_rus as status, " +
			"av_res_rep.card_id as rep_id, " +
			"2 as number, "+ //number to sorting
			"c_rep_status.status_id as rep_status_id "+
			"from 	card c "+
			"left outer join attribute_value av_res_rep on "+
				"(av_res_rep.number_value = c.card_id and av_res_rep.attribute_code ='ADMIN_702311') "+
			"left join attribute_value av_res_user on "+ 
				"(av_res_user.card_id = c.card_id and av_res_user.attribute_code ='ADMIN_255974') "+
			"left join card rep_c on rep_c.card_id = av_res_rep.card_id " +
			"left join card_status c_rep_status on (c_rep_status.status_id = rep_c.status_id) "+
			"join attribute_value av_rep_aut on "+
				"(av_rep_aut.card_id = av_res_rep.card_id and av_rep_aut.attribute_code ='ADMIN_702335') "+
			"left outer join person p_exec on (p_exec.person_id = av_rep_aut.number_value) "+
			"left outer join attribute_value av_sname_aut on (av_sname_aut.card_id = p_exec.card_id and av_sname_aut .attribute_code = 'JBR_PERS_SNAME_NM') "+
			"left join attribute_value av_org_link on (av_org_link.card_id = p_exec.card_id and av_org_link.attribute_code = 'JBR_PERS_ORG') "+
			"left join attribute_value av_org on (av_org.card_id = av_org_link.number_value and av_org.attribute_code = 'JBR_DORG_SHORTNAME')" +	
			"left outer join attribute_value av_rep_xml on "+
				"(av_rep_xml.card_id =av_res_rep.card_id and av_rep_xml.attribute_code = 'ADMIN_702354') "+
			"where 	c.card_id = "+id.longValue()+ " and av_rep_aut.number_value = av_res_user.number_value "+
			" UNION "+
			//outside executor
			"select c.card_id as c_res, "+
			"av_rep_xml.long_binary_value as rep_xml, "+
			"av_sname_aut.string_value as sname_aut, "+
			"null as org," +
			"c_rep_status.name_rus as status, " +
			"av_res_rep.card_id as rep_id, " + 
			"3 as number, "+ //number to sorting
			"c_rep_status.status_id as rep_status_id "+
			"from 	card c "+
			"left outer join attribute_value av_res_rep on "+
				"(av_res_rep.number_value = c.card_id and av_res_rep.attribute_code='ADMIN_702600') "+
			"left join card rep_c on rep_c.card_id = av_res_rep.card_id " +
			"left join card_status c_rep_status on (c_rep_status.status_id = rep_c.status_id) "+
			"join attribute_value av_rep_aut on "+
				"(av_rep_aut.card_id = av_res_rep.card_id and av_rep_aut.attribute_code='ADMIN_702598') "+
			"left outer join attribute_value av_sname_aut on  "+
			"(av_sname_aut.card_id = av_rep_aut.number_value and av_sname_aut.attribute_code = 'NAME') "+
			"left outer join attribute_value av_rep_xml on "+
				"(av_rep_xml.card_id =av_res_rep.card_id and av_rep_xml.attribute_code = 'ADMIN_702354') "+
			"where 	c.card_id ="+id.longValue()+") "+
			"order by number asc, rep_id asc"; //first sorting by numder, second - by sname_aut 
		return sql;
	}
	
	private List/*RecordDecision*/ getDecisions(String type, String sname, String org, String name, String mname, ByteArrayInputStream decisions, String status, Long reportId, Long reportStatusId) throws Exception {
		List result = new LinkedList();
		// ������ decisions
		if (decisions != null && decisions.available() != 0) {
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(decisions);
			recordExpression = xpath.compile("/report/part");
			NodeList recordNodes = (NodeList)recordExpression.evaluate(doc, XPathConstants.NODESET);
			for (int i=0; i < recordNodes.getLength(); i++) {
				Element recordEl = (Element)recordNodes.item(i);
				
				String timestamp = recordEl.getAttribute("timestamp");
				String decision = recordEl.getTextContent();
				
				// ���� ����� �����������, �� ���������� ������ ��������� 
				if (i == recordNodes.getLength()-1)
					result.add( new RecordDecision(type, sname, org, name, mname, timestamp, null, null,decision,reportId,null,reportStatusId,status,null,null, null));
			}
		}
		else 
		{ 
			result.add( new RecordDecision(type, sname, org, name, mname, null, null, null,"����� �� �����������",reportId,null,reportStatusId,status,null,null, null));
		}
		 //----------------
		return result;
	}
}
