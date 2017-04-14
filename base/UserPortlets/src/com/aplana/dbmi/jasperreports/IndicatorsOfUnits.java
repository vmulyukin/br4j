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
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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

public class IndicatorsOfUnits {
	
	public static String YEAR = "year";
	public static String DAY = "day";
	
	private Connection conn = null;
	
	
	public IndicatorsOfUnits() {
		}

	public Integer getIncomingCount(Connection conn, Date date, String unitId, String type, Boolean rb) {
		this.conn = conn;
		
		Integer result = null;
		try {
			Statement stmt = this.conn.createStatement();
			ResultSet rs = null;
			if(YEAR.equals(type)){
				rs = stmt.executeQuery(getInCountingSql(getCurYearDate(date),getNextYearDate(date), unitId, rb));
			} else if (DAY.equals(type)){
				rs = stmt.executeQuery(getInCountingSql(getCurDay(date),getNextDay(date), unitId, rb));
			}
			while (rs.next()) {
				result = rs.getInt(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return result;
		}
	}
	
	
	private String getInCountingSql(String from_date, String to_date, String id, Boolean rb) {
		String sql = "SELECT COUNT(*) incoming FROM card c "+
		"JOIN attribute_value av_n on c.card_id = av_n.card_id and av_n.attribute_code = 'JBR_REGD_REGNUM' "+
		"JOIN attribute_value av_d on c.card_id = av_d.card_id and av_d.attribute_code = 'JBR_REGD_DATEREG' "+
		"JOIN attribute_value av_rec on c.card_id = av_rec.card_id and av_rec.attribute_code = 'JBR_INFD_RECEIVER' "+ 
		"JOIN person p on av_rec.number_value = p.person_id "+
		 "JOIN attribute_value av_recCard on p.card_id = av_recCard.card_id and av_recCard.attribute_code = 'JBR_PERS_DEPT_LINK' "; 
		if(rb){
			sql = sql + 
					"JOIN attribute_value av_rb on c.card_id = av_rb.card_id and av_rb.attribute_code = 'JBR_INFD_SENDER' ";
		}
		sql = sql +
		"WHERE " +
		"c.template_id = 224 " +
		"and c.status_id not in (1,301,302,303990) " +
		"and av_recCard.number_value in (" + id + ") " +
		"and av_d.date_value >= " + from_date +
		"and av_d.date_value < " + to_date;
		if (rb){
			sql = sql +
				"and av_rb.number_value in (select c1.card_id from card c1 " +
				"join attribute_value av on c1.card_id = av.card_id and attribute_code = 'JBR_DORG_FULLNAME' "+
					"where c1.template_id = 222 "+
					"and av.string_value in ('������������� ���������� ������������','������������� ���������� ���������� ������������') )";
		}
		System.out.println(sql);
		return sql;
	}
	
	public Integer getOgCount(Connection conn, Date date, String unitId, String type, Boolean rb) {
		this.conn = conn;
		
		Integer result = null;
		try {
			Statement stmt = this.conn.createStatement();
			ResultSet rs = null;
			if(YEAR.equals(type)){
				rs = stmt.executeQuery(getOgCountingSql(getCurYearDate(date),getNextYearDate(date), unitId, rb));
			} else if (DAY.equals(type)){
				rs = stmt.executeQuery(getOgCountingSql(getCurDay(date),getNextDay(date), unitId, rb));
			}
			while (rs.next()) {
				result = rs.getInt(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return result;
		}
	}
	
	private String getOgCountingSql(String from_date, String to_date, String id, Boolean rb) {
		String sql = "SELECT COUNT(distinct c.card_id) incoming FROM card c "+
		"JOIN attribute_value av_n on c.card_id = av_n.card_id and av_n.attribute_code = 'JBR_REGD_REGNUM' "+
		"JOIN attribute_value av_d on c.card_id = av_d.card_id and av_d.attribute_code = 'JBR_REGD_DATEREG' "+
		"JOIN attribute_value av_rassm on c.card_id = av_rassm.card_id and av_rassm.attribute_code = 'JBR_IMPL_ACQUAINT' "+
        "	JOIN attribute_value av_rec on av_rassm.number_value = av_rec.card_id and av_rec.attribute_code = 'JBR_RASSM_PERSON' "+
        "    	JOIN person p_rec on av_rec.number_value = p_rec.person_id "+
        "       	JOIN attribute_value av_recCard on p_rec.card_id = av_recCard.card_id and av_recCard.attribute_code = 'JBR_PERS_DEPT_LINK' ";
		if(rb){
			sql = sql + 
					"JOIN attribute_value av_rb on c.card_id = av_rb.card_id and av_rb.attribute_code = 'JBR_INFD_SENDER' ";
		}
		sql = sql +
		"WHERE " +
		"c.template_id = 864 " +
		"and c.status_id not in (1,301,302,303990) " +
		"and av_recCard.number_value in (" + id + ") " +
		"and av_d.date_value >= " + from_date +
		"and av_d.date_value < " + to_date;
		if (rb){
			sql = sql +
				"and av_rb.number_value in (select c1.card_id from card c1 " +
				"join attribute_value av on c1.card_id = av.card_id and attribute_code = 'JBR_DORG_FULLNAME' "+
					"where c1.template_id = 222 "+
					"and av.string_value in ('������������� ���������� ������������','������������� ���������� ���������� ������������') )";
		}
		System.out.println(sql);
		return sql;
	}
	
	public Integer getRbCount(Connection conn, Date date, String unitId, String type) {
		this.conn = conn;
		
		Integer resultIn = null;
		Integer resultOg = null;
		try {
			Statement stmt = this.conn.createStatement();
			ResultSet rsIn = null;
			ResultSet rsOg = null;
			if(YEAR.equals(type)){
				rsIn = stmt.executeQuery(getInCountingSql(getCurYearDate(date),getNextYearDate(date), unitId, true));
			} else if (DAY.equals(type)){
				rsIn = stmt.executeQuery(getInCountingSql(getCurDay(date),getNextDay(date), unitId, true));
			}
			while (rsIn.next()) {
				resultIn = rsIn.getInt(1);
			}
			if(YEAR.equals(type)){
				rsOg = stmt.executeQuery(getOgCountingSql(getCurYearDate(date),getNextYearDate(date), unitId, true));
			} else if (DAY.equals(type)){
				rsOg = stmt.executeQuery(getOgCountingSql(getCurDay(date),getNextDay(date), unitId, true));
			}
			while (rsOg.next()) {
				resultOg = rsOg.getInt(1);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return (resultIn+resultOg);
		}
	}
	
	private String getInternalSql(String from_date, String to_date, String id) {
		String sql = "SELECT COUNT(*) incoming FROM card c "+
		"JOIN attribute_value av_n on c.card_id = av_n.card_id and av_n.attribute_code = 'JBR_REGD_REGNUM' "+
		"JOIN attribute_value av_d on c.card_id = av_d.card_id and av_d.attribute_code = 'JBR_REGD_DATEREG' "+
		 "JOIN attribute_value av_rec on c.card_id = av_rec.card_id and av_rec.attribute_code = 'JBR_INFD_RECEIVER' "+ 
		"JOIN person p on av_rec.number_value = p.person_id "+
		 "JOIN attribute_value av_recCard on p.card_id = av_recCard.card_id and av_recCard.attribute_code = 'JBR_PERS_DEPT_LINK' "+
		 "JOIN attribute_value doc_type on c.card_id = doc_type.card_id and doc_type.attribute_code = 'JBR_INFD_TYPEDOC' "+
		 "JOIN attribute_value doc_name on doc_type.number_value = doc_name.card_id and doc_name.attribute_code = 'NAME' "+
		 
		"WHERE " +
		"c.template_id = 784 " +
		"and c.status_id not in (106,107,108,200,303990,6092498) " +
		"and av_recCard.number_value in (" + id + ") " +
		"and av_d.date_value >= " + from_date +
		"and av_d.date_value < " + to_date +
		"and doc_name.string_value like '��������� �������%'";
		System.out.print(sql);
		return sql;
	}
	
	public Integer getInternalCount(Connection conn, Date date, String unitId, String type) {
		this.conn = conn;
		
		Integer result = null;
		try {
			Statement stmt = this.conn.createStatement();
			ResultSet rs = null;
			if(YEAR.equals(type)){
				rs = stmt.executeQuery(getInternalSql(getCurYearDate(date), getNextYearDate(date), unitId));
			} else if (DAY.equals(type)){
				rs = stmt.executeQuery(getInternalSql(getCurDay(date), getNextDay(date), unitId));
			}
			while (rs.next()) {
				result = rs.getInt(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return result;
		}
	}
	
	private String getOutcomingSql(String from_date, String to_date, String id) {
		String sql = "SELECT COUNT(*) incoming FROM card c "+
		"JOIN attribute_value av_n on c.card_id = av_n.card_id and av_n.attribute_code = 'JBR_REGD_REGNUM' "+
		"JOIN attribute_value av_d on c.card_id = av_d.card_id and av_d.attribute_code = 'JBR_REGD_DATEREG' "+
		 "JOIN attribute_value av_rec on c.card_id = av_rec.card_id and av_rec.attribute_code = 'JBR_INFD_EXECUTOR' "+ 
		"JOIN person p on av_rec.number_value = p.person_id "+
		 "JOIN attribute_value av_recCard on p.card_id = av_recCard.card_id and av_recCard.attribute_code = 'JBR_PERS_DEPT_LINK' "+
		"WHERE " +
		"c.template_id = 364 " +
		"and c.status_id in (101,104) " +
		"and av_recCard.number_value in (" + id + ") " +
		"and av_d.date_value >= " + from_date +
		"and av_d.date_value < " + to_date;
		System.out.print(sql);
		return sql;
	}
	
	public Integer getOutcomingCount(Connection conn, Date date, String unitId, String type) {
		this.conn = conn;
		Integer result = null;
		try {
			Statement stmt = this.conn.createStatement();
			ResultSet rs = null;
			if(YEAR.equals(type)){
				rs = stmt.executeQuery(getOutcomingSql(getCurYearDate(date), getNextYearDate(date), unitId));
			} else if (DAY.equals(type)){
				rs = stmt.executeQuery(getOutcomingSql(getCurDay(date), getNextDay(date), unitId));
			}
			while (rs.next()) {
				result = rs.getInt(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return result;
		}
	}
	
	private String getNPASql(String from_date, String to_date, String id) {
		String sql = "SELECT COUNT(*) incoming FROM card c "+
		"JOIN attribute_value av_n on c.card_id = av_n.card_id and av_n.attribute_code = 'JBR_REGD_REGNUM' "+
		"JOIN attribute_value av_d on c.card_id = av_d.card_id and av_d.attribute_code = 'JBR_REGD_DATEREG' "+
		 "JOIN attribute_value av_rec on c.card_id = av_rec.card_id and av_rec.attribute_code = 'JBR_INFD_EXECUTOR' "+ 
		"JOIN person p on av_rec.number_value = p.person_id "+
		 "JOIN attribute_value av_recCard on p.card_id = av_recCard.card_id and av_recCard.attribute_code = 'JBR_PERS_DEPT_LINK' "+
		 "JOIN attribute_value doc_type on c.card_id = doc_type.card_id and doc_type.attribute_code = 'JBR_INFD_TYPEDOC' "+
		 "JOIN attribute_value doc_name on doc_type.number_value = doc_name.card_id and doc_name.attribute_code = 'NAME' "+
		 
		"WHERE " +
		"c.template_id = 1226 " +
		"and c.status_id in (101,103,206,48909,104) " +
		"and av_recCard.number_value in (" + id + ") " +
		"and av_d.date_value >= " + from_date +
		"and av_d.date_value < " + to_date +
		"and doc_name.string_value like '������ �� �������� ������������%'";
		System.out.print(sql);
		return sql;
	}
	
	public Integer getNPACount(Connection conn, Date date, String unitId, String type) {
		this.conn = conn;
		
		Integer result = null;
		try {
			Statement stmt = this.conn.createStatement();
			ResultSet rs = null;
			if(YEAR.equals(type)){
				rs = stmt.executeQuery(getNPASql(getCurYearDate(date), getNextYearDate(date), unitId));
			} else if (DAY.equals(type)){
				rs = stmt.executeQuery(getNPASql(getCurDay(date), getNextDay(date), unitId));
			}
			while (rs.next()) {
				result = rs.getInt(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return result;
		}
	}
	
	private String getInOutNPASql(String from_date, String to_date, String id) {
		String sql = "SELECT COUNT(*) incoming FROM card c "+
		"JOIN attribute_value av_n on c.card_id = av_n.card_id and av_n.attribute_code = 'JBR_REGD_REGNUM' "+
		"JOIN attribute_value av_d on c.card_id = av_d.card_id and av_d.attribute_code = 'JBR_REGD_DATEREG' "+
		 "JOIN attribute_value av_rec on c.card_id = av_rec.card_id and av_rec.attribute_code in ('JBR_INFD_EXECUTOR','JBR_INFD_RECEIVER') "+ 
		"JOIN person p on av_rec.number_value = p.person_id "+
		 "JOIN attribute_value av_recCard on p.card_id = av_recCard.card_id and av_recCard.attribute_code = 'JBR_PERS_DEPT_LINK' "+
		 "JOIN attribute_value doc_type on c.card_id = doc_type.card_id and doc_type.attribute_code = 'JBR_INFD_TYPEDOC' "+
		 "JOIN attribute_value doc_name on doc_type.number_value = doc_name.card_id and doc_name.attribute_code = 'NAME' "+
		 
		"WHERE " +
		"c.template_id in (224,364) " +
		"and av_recCard.number_value in (" + id + ") " +
		"and av_d.date_value >= " + from_date +
		"and av_d.date_value < " + to_date +
		"and doc_name.string_value in ('�������� (���)','��������� (���)')";
		System.out.print(sql);
		return sql;
	}
	
	public Integer getInOutNPACount(Connection conn, Date date, String unitId, String type) {
		this.conn = conn;
		
		Integer result = null;
		try {
			Statement stmt = this.conn.createStatement();
			ResultSet rs = null;
			if(YEAR.equals(type)){
				rs = stmt.executeQuery(getInOutNPASql(getCurYearDate(date), getNextYearDate(date), unitId));
			} else if (DAY.equals(type)){
				rs = stmt.executeQuery(getInOutNPASql(getCurDay(date), getNextDay(date), unitId));
			}
			while (rs.next()) {
				result = rs.getInt(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return result;
		}
	}
	
	
	private String getCurYearDate(Date date){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
		return  "'"+sdf.format(date)+"-01-01'";
	}
	
	private String getNextYearDate(Date date){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
		Integer nextYear = Integer.valueOf(sdf.format(date)) + 1;
		return  "'"+nextYear.toString()+"-01-01'";
	}
	
	private String getCurDay(Date date){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return  "'"+sdf.format(date)+"'";
	}
	
	private String getNextDay(Date date){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return  "'"+sdf.format(new Date(date.getTime()+86400000))+"'";
	}
}
