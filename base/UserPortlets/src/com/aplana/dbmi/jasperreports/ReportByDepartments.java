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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ReportByDepartments {
	Connection conn;
	private static Log logger = LogFactory.getLog(ReportByDepartments.class);
	
	public ReportByDepartments(Connection conn) {
		this.conn = conn;
	}
	
	public String prefixSQL(Date fromDate, Date toDate, String templateIds, String departmentIds) throws SQLException {
		String nullDepartmentIds = null;
		if(departmentIds!=null){
			nullDepartmentIds="'"+departmentIds+"'";			
		}
			
		StringBuilder sql = new StringBuilder();
		
		sql.append("-- ������ ������������� ���������� �������������� ������������� \n");
		sql.append("with head_dep (person_id) as ( \n");
		sql.append("	select distinct p_head.person_id \n");
		sql.append("	from card dep \n");
		sql.append("	inner join attribute_value av_head on \n");
		sql.append("		av_head.card_id = dep.card_id and av_head.attribute_code = 'JBR_DEPT_CHIEF' \n");
		sql.append("	inner join person p_head on p_head.card_id = av_head.number_value \n");
		sql.append("	where dep.template_id = 484 and dep.status_id = 4 \n");
		sql.append("), \n");
		sql.append("-- ������ �������� ������� ������������� ������ ���� �������� \n");
		sql.append("doc_constr (card_id) as ( \n");
		sql.append("	select cr.card_id \n");
		sql.append("	from card cr \n");
		sql.append("	inner join attribute_value av_dreg on  \n");
		sql.append("		av_dreg.card_id = cr.card_id and av_dreg.attribute_code = 'JBR_REGD_DATEREG' \n");
		sql.append("	where 	cr.template_id in (");
		sql.append(templateIds);
		sql.append(") \n");
		sql.append("		and cr.status_id not in (1,106,107,108,301,302,6092498,303990,101,102,10000120,200) -- all before 103 \n");
		sql.append("		and date_trunc('day', (av_dreg.date_value + interval '6 hour')) >= '");
		sql.append(new SimpleDateFormat("yyyy-MM-dd").format(fromDate));
		sql.append("' \n");
		sql.append("		and date_trunc('day', (av_dreg.date_value + interval '6 hour')) < (cast('");
		sql.append(new SimpleDateFormat("yyyy-MM-dd").format(toDate));
		sql.append("' as date) + interval '1 day') \n");
		sql.append("), \n");
		sql.append("-- ���-��������� � ��� ��������� \n");
		sql.append("resolutions_all (card_id, res_id) as ( \n");
		sql.append("		with recursive res_of_doc (card_id, res_id) as ( \n");
		sql.append("			select av_res1.number_value, av_res1.card_id \n");
		sql.append("			from attribute_value av_res1 \n");
		sql.append("			inner join card c1 on c1.card_id = av_res1.card_id \n");
		sql.append("			where av_res1.attribute_code = 'JBR_DOCB_BYDOC' \n");
		sql.append("				and av_res1.number_value in (select card_id from doc_constr) \n");
		sql.append("				and c1.status_id not in (1,34145,303990,107,10001050) \n");
		sql.append("			union \n");
		sql.append("			select res_parent.card_id, av_res2.card_id \n");
		sql.append("			from res_of_doc res_parent \n");
		sql.append("			inner join attribute_value av_res2 on \n");
		sql.append("				av_res2.number_value = res_parent.res_id \n");
		sql.append("				and av_res2.attribute_code = 'JBR_RIMP_PARASSIG' \n");
		sql.append("			inner join card c2 on c2.card_id = av_res2.card_id \n");
		sql.append("				and c2.status_id not in (1,34145,303990,107,10001050) \n");
		sql.append("		) \n");
		sql.append("		select card_id, res_id \n");
		sql.append("		from res_of_doc \n");
		sql.append("), \n");
		sql.append("-- ���-��������� � ������ �� ���������� \n");
		sql.append("target_res (card_id, res_id, number_value, date_value) as ( \n");
		sql.append("		select ra.card_id, ra.res_id, av_exec.number_value, av_dcr.date_value \n");
		sql.append("		from resolutions_all ra \n");
		sql.append("			inner join attribute_value av_exec on \n");
		sql.append("				av_exec.card_id = ra.res_id and av_exec.attribute_code = 'JBR_INFD_EXEC_LINK' \n");
		sql.append("								and av_exec.number_value in (select person_id from head_dep) \n");
		sql.append("			inner join attribute_value av_dcr on \n");
		sql.append("				av_dcr.card_id = ra.res_id and av_dcr.attribute_code = 'CREATED' \n");
		sql.append("), \n");
		sql.append("-- ������ �������� (��������������� ������ ���� ��������) � �� ������������� \n");
		sql.append("doc_exec (card_id, exec_id) as ( \n");
		sql.append("		select dc.card_id, \n");
		sql.append("				(select number_value from target_res tr where tr.card_id = dc.card_id order by tr.date_value limit 1) as exec_id \n");
		sql.append("		from doc_constr dc \n");
		sql.append(") \n");
		sql.append("select doc.card_id, av_dep.number_value \n");
		sql.append("from doc_exec doc \n");
		sql.append("left join person p_exec on p_exec.person_id = doc.exec_id \n");
		sql.append("left join attribute_value av_dep on \n");
		sql.append("	av_dep.card_id = p_exec.card_id and av_dep.attribute_code = 'JBR_PERS_DEPT_LINK' \n");
		sql.append(" --�������� �� ���������� ���������� �� ������������ \n");
		sql.append("where av_dep.number_value is null or av_dep.number_value in (");
		sql.append(departmentIds);
		sql.append(") or ");
		sql.append(nullDepartmentIds);
		sql.append(" is null");
		//"	or exists (select 1 from dep_all_parent dp where dp.id = av_dep.number_value and dp.pid in ("+ departmentIds +"))";
		logger.info("prefixSQL is executing now...");
		ResultSet rs = conn.createStatement().executeQuery(sql.toString());
		logger.info(sql.toString());
		StringBuilder cardIdsNoHead = new StringBuilder();
		
		StringBuilder sb = new StringBuilder();		
		sb.append("select 1, 1 where false \n");
		//if (!rs.isLast())
			
		while (rs.next()) {
			
			if(rs.getLong(2)==0){
				if(cardIdsNoHead.length()>0){
					cardIdsNoHead.append(",");
				}
				cardIdsNoHead.append(rs.getLong(1));
				continue;
			}
			sb.append("union \n");
			sb.append("select "+rs.getLong(1)+" as docId, "+rs.getLong(2)+" as depId \n");
			/*if (!rs.isLast())
				sb.append("union \n");*/
		}
		
		sb.append(prefixSqlNoHead(cardIdsNoHead.toString(), conn, departmentIds, nullDepartmentIds));
		
		return sb.toString();
	}
	
	
	private String prefixSqlNoHead(String cardIds, Connection connection, String departmentIds, String nullDepartmentIds) throws SQLException{
		
		if(cardIds==null || cardIds.isEmpty()){
			return "";
		}
		
		StringBuilder sql = new StringBuilder();
		
		sql.append("select t.c_id, t.r_id, t.d_id \n");
		sql.append("from ( \n");
		
		sql.append("select  av.card_id as c_id, min(av.number_value) as r_id,  min(av_dep.number_value) as d_id \n");
		sql.append(" from \n");
		sql.append(" attribute_value av \n");
		sql.append(" join card c on c.card_id = av.card_id \n");
		sql.append(" join attribute_value av_res on av.card_id=av_res.card_id and av_res.attribute_code = 'JBR_INFD_EXEC_LINK' \n");
		sql.append(" join person p on av_res.number_value=p.person_id \n");
		sql.append(" join attribute_value av_dep on av_dep.card_id = p.card_id and av_dep.attribute_code = 'JBR_PERS_DEPT_LINK' \n");  
		sql.append(" where av.attribute_code = 'JBR_DOCB_BYDOC' \n");
		sql.append(" 	and av.number_value in (");
		sql.append(cardIds);
		sql.append(") 	and c.status_id not in (1,34145,303990,107,10001050) \n");
		sql.append(" GROUP BY av.card_id");
		
		sql.append(") as t \n");
		sql.append(" where (t.d_id in (");
		sql.append(departmentIds);
		sql.append(") or ");
		sql.append(nullDepartmentIds);
		sql.append(" is null)");		
		
		logger.info("prefixSqlNoHead is executing now...");
		ResultSet rs = connection.createStatement().executeQuery(sql.toString());
		logger.info(sql.toString());
		StringBuilder result = new StringBuilder();
		while(rs.next()){
			result.append("union \n");
			result.append("select "+rs.getLong(1)+" as docId, "+rs.getLong(3)+" as depId \n");
		}
		return result.toString();
		
	}
}