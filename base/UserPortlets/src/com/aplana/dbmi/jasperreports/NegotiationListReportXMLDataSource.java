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
import java.io.FileWriter;
import java.io.InputStream;
import java.io.StringWriter;
import java.lang.reflect.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.collections.map.HashedMap;
import org.jboss.remoting.samples.chat.exceptions.DatabaseException;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
/**
 * ����� �������� ������� ������� �� ���� ��� ��� ���������,id �������� ������� � ���������� � 
 * ���������� xml-�������� �� ������� ���� �������, 
 * ������������ ������������, ����� �������, ������� ��������
 * @author lyakin
 *
 */
public class NegotiationListReportXMLDataSource extends GettingXMLDataSource{

	private Map params;
	private ArrayList solutions = new ArrayList();
	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy");
	private static final String NEGOTIATION_RESULT = "��������, ���������� � �����������, �� ���������, ���������, �� �������������� ������������";
	
	public Document getXML() throws DatabaseException {
		Connection con = null;
		try {
			Document newXmldoc=null;
			con = getConnection();
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = dbf.newDocumentBuilder();
			//�������� ���� ������� �� �������� ���. ���� ������� � ���� ���
			PreparedStatement ps = con.prepareStatement("SELECT"+
														" av_visa_solution.long_binary_value as sol,"+
														" c_person.full_name as resp_sn"+
														" FROM card c_doc"+
														" JOIN attribute_value av_visa on (av_visa.card_id = c_doc.card_id and av_visa.attribute_code = 'JBR_VISA_VISA')"+
														" JOIN attribute_value av_visa_solution on (av_visa_solution.card_id = av_visa.number_value and av_visa_solution.attribute_code = 'JBR_VISA_SOLUTION')"+
														" LEFT OUTER JOIN attribute_value av_visa_resp on (av_visa_resp.card_id = av_visa.number_value and av_visa_resp.attribute_code = 'JBR_VISA_RESPONSIBLE')"+
														" LEFT OUTER JOIN person c_person on ( c_person.person_id = av_visa_resp.number_value)"+
														" LEFT OUTER JOIN attribute_value av_pers_sname on (av_pers_sname.card_id = c_person.card_id and av_pers_sname.attribute_code = 'NAME')"+ //JBR_PERS_SNAME_NM
														" WHERE c_doc.card_id="+ params.get("card_id"));
			ResultSet resultSet = ps.executeQuery();
			//���� �� ���� ��������
			while (resultSet.next()){
				Document xmldoc;
				//����������� ���� � ���-��������
				InputStream inputStream = resultSet.getBinaryStream("sol");
				if (inputStream.available()!=0){
					xmldoc = builder.parse(inputStream);
					NodeList children =  xmldoc.getElementsByTagName("part");
					//��������� ���-��������
					 for (int i = 0; i < children.getLength(); i++) {
						      Node part = children.item(i);
						      NamedNodeMap attributes = part.getAttributes();
						      //�������� �������� ����
						      Node nodeFactUser = attributes.getNamedItem("fact-user");
						      Node nodeTimestamp = attributes.getNamedItem("timestamp");
						      Node nodeAction = attributes.getNamedItem("action");
						      Node nodeRound = attributes.getNamedItem("round");
						      //���������� �������� ��������� � ������
						      String solution = part.getTextContent();
						      String factUser = nodeFactUser.getNodeValue();
						      String str_timestamp = nodeTimestamp.getNodeValue();
						      SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
						      Date timestamp = (Date)formatter.parse(str_timestamp);
						      String action = nodeAction.getNodeValue();
						      String round = nodeRound.getNodeValue();
						      if (NEGOTIATION_RESULT.contains(action)){
							      //������� ��� � ���������� �������� ��������� � ����
							      Map data = new HashedMap();
							      data.put("fact-user", factUser);
							      data.put("timestamp", timestamp);
							      data.put("action", action);
							      data.put("round", round);
							      data.put("solution", solution);
							      //�������� ��� � ������
							      solutions.add(data);
						      }
					 }
				}
				else {
					 /* Map data = new HashedMap();
					  data.put("fact-user", resultSet.getString("resp_sn"));
				      data.put("timestamp", null);
				      data.put("action", "");
				      //data.put("round", round);
				      data.put("solution", "");
				      //�������� ��� � ������
				      solutions.add(data);*/
				}
				
			}
			//��������� ������ ���-��
			Map[] solutionsMap = (Map[]) solutions.toArray(new Map[solutions.size()]);
			Arrays.sort(solutionsMap, new SortMap((String) params.get("sort")));
			System.out.println(solutionsMap);
			//������� ����� ���-��������
			newXmldoc = builder.parse(new ByteArrayInputStream("<?xml version=\"1.0\" encoding=\"UTF-8\"?><report/>".getBytes("UTF-8")));
			Element root = newXmldoc.getDocumentElement();
			//���������� � ����� ���-�������� ��� ��������������� ������
			for(int i=0;i<=solutionsMap.length-1;i++){
				final Element part = newXmldoc.createElement("part");
				if (solutionsMap[i].get("round")!= null)
					part.setAttribute("round", solutionsMap[i].get("round").toString());
				if (solutionsMap[i].get("timestamp")!= null)
					part.setAttribute("timestamp",(solutionsMap[i].get("timestamp") == null) ? "-" : DATE_FORMAT.format(solutionsMap[i].get("timestamp")));
				if (solutionsMap[i].get("fact-user")!= null)
					part.setAttribute("fact-user", solutionsMap[i].get("fact-user").toString());
				if (solutionsMap[i].get("action")!= null)
					part.setAttribute("action", solutionsMap[i].get("action").toString());
					part.setTextContent(solutionsMap[i].get("solution").toString());
				root.appendChild(part);
			}
			/*���������� ��������� � ����
				final FileWriter ftw = new FileWriter("C:\\data.xml");
				final Transformer serializer = TransformerFactory.newInstance().newTransformer();
				serializer.transform(new DOMSource(newXmldoc), new StreamResult(ftw));
			*/
			return newXmldoc;
			
			
			
			
		} catch (Exception e) {
			System.out.println("������ � ��������� XML");
			e.printStackTrace();
		}
		finally {
			if (con != null) {
				try {
					con.close();
				} catch (Exception e) {
					throw new DatabaseException();
				}
			}
		}
		// TODO Auto-generated method stub
		return null;
		
	}

	public void setParameters(Map parameters) {
		// TODO Auto-generated method stub
		params=parameters;
	}

	public String getRecordPath() {
	return "/report/part";
	}
	





	  

}
