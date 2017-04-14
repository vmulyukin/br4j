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

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by echirkov on 31.08.2015.
 */
public class GenerateReportRecivedOgCount {
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	public static JRDataSource generate(Connection conn, Date startDate, Date endDate, String dept, Long tzHour) {
		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
		Statement statement = null;
		try {
			conn.setAutoCommit(false);
			statement = conn.createStatement();
			statement.execute(createTempTables(startDate, endDate, dept, tzHour));
			ResultSet rs = statement.executeQuery(getReportSql());

			conn.commit();
			conn.setAutoCommit(true);

			while(rs.next()) {
				Map<String, Object> line = new HashMap<String, Object>();
				line.put("reg_date", rs.getTimestamp("reg_date"));
				line.put("reg_num", rs.getString("reg_num"));
				line.put("reg_num_d", rs.getBigDecimal("reg_num_d"));
				line.put("author_og", rs.getString("author_og"));
				line.put("descr", rs.getString("descr"));
				line.put("quest", rs.getString("quest"));
				line.put("in_reg_date", rs.getTimestamp("in_reg_date"));
				line.put("in_reg_num", rs.getString("in_reg_num"));
				line.put("res_rassm", rs.getString("res_rassm"));
				line.put("status", rs.getString("status"));
				line.put("dept", rs.getString("dept"));

				resultList.add(line);

			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (statement != null) {
					statement.execute("drop table if exists departments, persons_in_dep, og_cards, " +
							"relation_doc,og_cards_archive, relation_doc_archive;");
					statement.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return new JRBeanCollectionDataSource(resultList);
	}

	private static String getReportSql() {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("select (reg_date.date_value+ interval '3 hour') as reg_date, \n")
				.append(" coalesce(reg_num.string_value,'') as reg_num, \n")
				.append(" reg_num_d.number_value as reg_num_d, \n")
				.append("   coalesce(string_agg(distinct coalesce(substring(og_author_nm.string_value for (position('(' in og_author_nm.string_value))-1),''), '; '),'') as author_og, \n")
				.append(" coalesce(descr.string_value,'') as descr, \n")
				.append(" coalesce(string_agg(distinct quest_nm.string_value, '; '),'') as quest, \n")
				.append(" (rd.in_reg_date+ interval '3 hour') as in_reg_date, \n")
				.append(" coalesce(rd.in_reg_num,'') as in_reg_num, \n")
				.append(" coalesce(rd.res_rassm,'') as res_rassm, \n")
				.append(" coalesce(cs.name_rus,'') as status, \n")
				.append(" coalesce(string_agg(distinct av_dept_nm.string_value,E';\n'),'') as dept, \n")
				.append(" c.card_id as card_id \n")
				.append(" from og_cards c \n")
				.append(" join card cc on c.card_id = cc.card_id \n")
				.append(" join card_status cs on cc.status_id = cs.status_id \n")
				.append(" join attribute_value reg_date on reg_date.card_id = c.card_id and reg_date.attribute_code = 'JBR_REGD_DATEREG' \n")
				.append(" join attribute_value reg_num on reg_num.card_id = c.card_id and reg_num.attribute_code = 'JBR_REGD_REGNUM' \n")
				.append(" join attribute_value reg_num_d on reg_num_d.card_id = c.card_id and reg_num_d.attribute_code = 'JBR_REGD_REGNUM_D' \n")
				.append(" left join attribute_value og_author on og_author.card_id = c.card_id and og_author.attribute_code = 'JBR_OG_REQ_AUTHOR' \n")
				.append(" left join attribute_value og_author_nm on og_author_nm.card_id = og_author.number_value and og_author_nm.attribute_code = 'NAME' \n")
				.append(" left join attribute_value descr on descr.card_id = c.card_id and descr.attribute_code = 'JBR_INFD_SHORTDESC' \n")
				.append(" left join attribute_value quest on quest.card_id = c.card_id and quest.attribute_code = 'JBR_QUEST_THEMATIC_C' \n")
				.append(" left join attribute_value quest_nm on quest_nm.card_id = quest.number_value and quest_nm.attribute_code = 'NAME' \n")
				.append(" left join relation_doc rd on c.card_id = rd.doc \n")
				.append(" left join attribute_value av_rassm on av_rassm.card_id = c.card_id and av_rassm.attribute_code = 'JBR_IMPL_ACQUAINT' \n")
				.append(" left join card c_rassm on av_rassm.number_value = c_rassm.card_id and c_rassm.status_id != 34145 \n")
				.append(" left join attribute_value av_rassm_p on av_rassm_p.card_id = c_rassm.card_id and av_rassm_p.attribute_code = 'JBR_RASSM_PERSON' \n")
				.append(" left join person p_rassm on av_rassm_p.number_value = p_rassm.person_id \n")
				.append(" left join attribute_value av_dept on av_dept.card_id = p_rassm.card_id and av_dept.attribute_code = 'JBR_PERS_DEPT_LINK' \n")
				.append(" left join departments d on d.curDep =  av_dept.number_value \n")
				.append(" left join attribute_value av_dept_nm on d.depLvl2 = av_dept_nm.card_id and av_dept_nm.attribute_code='JBR_DEPT_SHORTNAME' \n")
				.append(" group by reg_date, reg_num, descr, in_reg_date, in_reg_num, res_rassm, reg_num_d, status, c.card_id \n")
				.append(" union \n")
				.append(" select (reg_date.date_value+ interval '3 hour') as reg_date, \n")
				.append(" coalesce(reg_num.string_value,'') as reg_num, \n")
				.append(" reg_num_d.number_value as reg_num_d, \n")
				.append("   coalesce(string_agg(distinct coalesce(substring(og_author_nm.string_value for (position('(' in og_author_nm.string_value))-1),''), '; '),'') as author_og, \n")
				.append(" coalesce(descr.string_value,'') as descr, \n")
				.append(" coalesce(string_agg(distinct quest_nm.string_value, '; '),'') as quest, \n")
				.append(" (rd.in_reg_date+ interval '3 hour') as in_reg_date, \n")
				.append(" coalesce(rd.in_reg_num,'') as in_reg_num, \n")
				.append(" coalesce(rd.res_rassm,'') as res_rassm, \n")
				.append(" coalesce(cs.name_rus,'') as status, \n")
				.append(" coalesce(string_agg(distinct av_dept_nm.string_value,E';\n'),'') as dept, \n")
				.append(" c.card_id as card_id \n")
				.append(" from og_cards_archive c \n")
				.append(" join card cc on c.card_id = cc.card_id \n")
				.append(" join card_status cs on cc.status_id = cs.status_id \n")
				.append(" join attribute_value reg_date on reg_date.card_id = c.card_id and reg_date.attribute_code = 'JBR_REGD_DATEREG' \n")
				.append(" join attribute_value reg_num on reg_num.card_id = c.card_id and reg_num.attribute_code = 'JBR_REGD_REGNUM' \n")
				.append(" join attribute_value reg_num_d on reg_num_d.card_id = c.card_id and reg_num_d.attribute_code = 'JBR_REGD_REGNUM_D' \n")
				.append(" left join attribute_value og_author on og_author.card_id = c.card_id and og_author.attribute_code = 'JBR_OG_REQ_AUTHOR' \n")
				.append(" left join attribute_value og_author_nm on og_author_nm.card_id = og_author.number_value and og_author_nm.attribute_code = 'NAME' \n")
				.append(" left join attribute_value descr on descr.card_id = c.card_id and descr.attribute_code = 'JBR_INFD_SHORTDESC' \n")
				.append(" left join attribute_value quest on quest.card_id = c.card_id and quest.attribute_code = 'JBR_QUEST_THEMATIC_C' \n")
				.append(" left join attribute_value quest_nm on quest_nm.card_id = quest.number_value and quest_nm.attribute_code = 'NAME' \n")
				.append(" left join relation_doc_archive rd on c.card_id = rd.doc \n")
				.append(" left join attribute_value_archive av_rassm on av_rassm.card_id = c.card_id and av_rassm.attribute_code = 'JBR_IMPL_ACQUAINT' \n")
				.append(" left join card_archive c_rassm on av_rassm.number_value = c_rassm.card_id and c_rassm.status_id != 34145 \n")
				.append(" left join attribute_value_archive av_rassm_p on av_rassm_p.card_id = c_rassm.card_id and av_rassm_p.attribute_code = 'JBR_RASSM_PERSON' \n")
				.append(" left join person p_rassm on av_rassm_p.number_value = p_rassm.person_id \n")
				.append(" left join attribute_value av_dept on av_dept.card_id = p_rassm.card_id and av_dept.attribute_code = 'JBR_PERS_DEPT_LINK' \n")
				.append(" left join departments d on d.curDep =  av_dept.number_value \n")
				.append(" left join attribute_value av_dept_nm on d.depLvl2 = av_dept_nm.card_id and av_dept_nm.attribute_code='JBR_DEPT_SHORTNAME' \n")
				.append(" group by reg_date, reg_num, descr, in_reg_date, in_reg_num, res_rassm, reg_num_d, status, c.card_id \n")
				.append(" order by reg_num_d;");
		return stringBuffer.toString();
	}
	
	private static String createTempTables(Date startDate, Date endDate, String dept, Long tzHour){
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("WITH RECURSIVE search_up(cycle_id, cycle_number, cycle_base) AS ( \n")
				.append("    select cast(c.card_id::bigint as numeric),1, c.card_id from card c \n")
				.append("    where c.status_id in (4,7) and c.template_id = 484 \n")
				.append("    UNION ALL \n")
				.append("    SELECT v.number_value, su.cycle_number+1, su.cycle_base \n")
				.append("    FROM attribute_value v, search_up su \n")
				.append("    WHERE v.attribute_code = 'JBR_DEPT_PARENT_LINK' \n")
				.append("    AND v.card_id = su.cycle_id \n")
				.append("    and exists (select 1 from attribute_value \n")
				.append("		where attribute_code = 'JBR_DEPT_PARENT_LINK' \n")
				.append("		and card_id = v.number_value)), \n")
				.append("departments as (select (max(ARRAY[cycle_number, cycle_id, cycle_base]))[3] as curDep, \n")
				.append("			(max(ARRAY[cycle_number, cycle_id, cycle_base]))[2] as depLvl2 \n")
				.append("		from search_up \n")
				.append("		group by cycle_base) \n")
				.append("select curDep, depLvl2 into temp table departments from departments; \n")
				.append(" \n");
		if(dept != null && !dept.isEmpty()) {
			stringBuffer.append("select distinct person_id, av_dept_nm.string_value as dep_name into temp table persons_in_dep \n")
					.append("from person p \n")
					.append("join attribute_value av on p.card_id = av.card_id and av.attribute_code = 'JBR_PERS_DEPT_LINK' \n")
					.append("join departments d on d.curDep = av.number_value \n")
					.append("left join attribute_value av_dept_nm on d.depLvl2 = av_dept_nm.card_id and av_dept_nm.attribute_code='JBR_DEPT_SHORTNAME' \n")
					.append("where d.depLvl2 in (").append(dept).append("); \n")
					.append(" \n")
					.append("create index persons_in_dep_idx on persons_in_dep using btree(person_id); \n")
					.append("analyze persons_in_dep; \n")
					.append(" \n");
		}
		stringBuffer.append("select card_id into temp table og_cards from ( \n")
				.append("	select c.card_id \n")
				.append("	from card c \n")
				.append("	join attribute_value reg_date on reg_date.card_id = c.card_id and reg_date.attribute_code = 'JBR_REGD_DATEREG' \n");
		if(dept != null && !dept.isEmpty()) {
			stringBuffer.append("	join attribute_value av_rassm on av_rassm.card_id = c.card_id and av_rassm.attribute_code = 'JBR_IMPL_ACQUAINT' \n")
					.append("	join card c_r on c_r.card_id = av_rassm.number_value and c_r.status_id != 34145 \n")
					.append("	join attribute_value av_rassm_p on av_rassm_p.card_id = c_r.card_id and av_rassm_p.attribute_code = 'JBR_RASSM_PERSON' \n")
					.append("	join persons_in_dep p_rassm on av_rassm_p.number_value = p_rassm.person_id \n");
		}
		stringBuffer.append("	where  \n")
				.append("	(date_trunc('day',reg_date.date_value + interval '").append(tzHour).append(" hour') between '")
				.append(sdf.format(startDate)).append("' and '").append(sdf.format(endDate)).append("') \n")
				.append("	and c.status_id in (101,102,103,206,48909) and c.template_id = 864 \n");
		if(dept != null && !dept.isEmpty()) {
			stringBuffer.append(" \n")
					.append("	union \n")
					.append(" \n")
					.append("	select c.card_id \n")
					.append("	from card c \n")
					.append("	join attribute_value reg_date on reg_date.card_id = c.card_id and reg_date.attribute_code = 'JBR_REGD_DATEREG' \n")
					.append("	join attribute_value av_all_res on av_all_res.number_value = c.card_id and av_all_res.attribute_code = 'JBR_MAINDOC' \n")
					.append("	join card c_all_res on av_all_res.card_id = c_all_res.card_id and c_all_res.status_id in (103,206) \n")
					.append("	join attribute_value av_res_exec on c_all_res.card_id = av_res_exec.card_id and av_res_exec.attribute_code in ('JBR_INFD_EXEC_LINK','ADMIN_255974') \n")
					.append("	join persons_in_dep p_rassm on av_res_exec.number_value = p_rassm.person_id \n")
					.append("	where  \n")
					.append("	(date_trunc('day',reg_date.date_value + interval '").append(tzHour).append(" hour') between '")
					.append(sdf.format(startDate)).append("' and '").append(sdf.format(endDate)).append("') \n")
					.append("	and c.status_id in (101,102,103,206,48909) and c.template_id = 864 \n");
		}
		stringBuffer.append(") as s; \n")
				.append(" \n")
				.append("create index og_cards_idx on og_cards using btree(card_id); \n")
				.append("analyze og_cards; \n")
				.append(" \n")
				.append("select card_id into temp table og_cards_archive from ( \n")
				.append("	select c.card_id \n")
				.append("	from card c \n")
				.append("	join attribute_value reg_date on reg_date.card_id = c.card_id and reg_date.attribute_code = 'JBR_REGD_DATEREG' \n");
		if(dept != null && !dept.isEmpty()) {
			stringBuffer.append("	join attribute_value_archive av_rassm on av_rassm.card_id = c.card_id and av_rassm.attribute_code = 'JBR_IMPL_ACQUAINT' \n")
					.append("	join card_archive c_r on c_r.card_id = av_rassm.number_value and c_r.status_id != 34145 \n")
					.append("	join attribute_value_archive av_rassm_p on av_rassm_p.card_id = c_r.card_id and av_rassm_p.attribute_code = 'JBR_RASSM_PERSON' \n")
					.append("	join persons_in_dep p_rassm on av_rassm_p.number_value = p_rassm.person_id \n");
		}
		stringBuffer.append("	where  \n")
				.append("	(date_trunc('day',reg_date.date_value + interval '").append(tzHour).append(" hour') between '")
				.append(sdf.format(startDate)).append("' and '").append(sdf.format(endDate)).append("') \n")
				.append("	and c.status_id in (104) and c.template_id = 864 \n");
		if(dept != null && !dept.isEmpty()) {
			stringBuffer.append(" \n")
					.append("	union \n")
					.append(" \n")
					.append("	select c.card_id \n")
					.append("	from card c \n")
					.append("	join attribute_value reg_date on reg_date.card_id = c.card_id and reg_date.attribute_code = 'JBR_REGD_DATEREG' \n")
					.append("	join attribute_value_archive av_all_res on av_all_res.number_value = c.card_id and av_all_res.attribute_code = 'JBR_MAINDOC' \n")
					.append("	join card_archive c_all_res on av_all_res.card_id = c_all_res.card_id and c_all_res.status_id in (103,206) \n")
					.append("	join attribute_value_archive av_res_exec on c_all_res.card_id = av_res_exec.card_id and av_res_exec.attribute_code in ('JBR_INFD_EXEC_LINK','ADMIN_255974') \n")
					.append("	join persons_in_dep p_rassm on av_res_exec.number_value = p_rassm.person_id \n")
					.append("	where  \n")
					.append("	(date_trunc('day',reg_date.date_value + interval '").append(tzHour).append(" hour') between '")
					.append(sdf.format(startDate)).append("' and '").append(sdf.format(endDate)).append("') \n")
					.append("	and c.status_id in (104) and c.template_id = 864 \n");
		}
		stringBuffer.append(") as s; \n")
				.append(" \n")
				.append("create index og_cards_archive_idx on og_cards_archive using btree(card_id); \n")
				.append("analyze og_cards_archive; \n")
				.append(" \n")
				.append("with recursive res_couple as ( \n")
				.append("	select c_og.card_id as og, card_raasm.card_id as rassm, c_res1.card_id as level_res \n")
				.append("	from og_cards c_og \n")
				.append("	left join attribute_value av_rassm on c_og.card_id = av_rassm.card_id \n")
				.append("		and av_rassm.attribute_code = 'JBR_IMPL_ACQUAINT' \n")
				.append("	join card card_raasm on card_raasm.card_id = av_rassm.number_value and card_raasm.status_id != 34145 \n")
				.append("	join attribute_value av_rmain on av_rmain.card_id = card_raasm.card_id \n")
				.append("		and av_rmain.attribute_code = 'JBR_RESPONS_CONSIDER' and av_rmain.value_id = 1449 \n")
				.append("	join attribute_value av_rassm_p on av_rassm_p.card_id = card_raasm.card_id \n")
				.append("		and av_rassm_p.attribute_code = 'JBR_RASSM_PERSON' \n")
				.append(" \n")
				.append("	join attribute_value av_res1 on av_res1.number_value = c_og.card_id \n")
				.append("		and av_res1.attribute_code = 'JBR_DOCB_BYDOC' \n")
				.append("	join card c_res1 on av_res1.card_id = c_res1.card_id and c_res1.status_id in (103,206) \n")
				.append(" \n")
				.append("	join attribute_value av_res_signer on av_res1.card_id = av_res_signer.card_id \n")
				.append("		and av_res_signer.attribute_code = 'JBR_INFD_SGNEX_LINK' and av_res_signer.number_value = av_rassm_p.number_value \n")
				.append(" \n")
				.append("	UNION \n")
				.append(" \n")
				.append("	select t.og, t.rassm, c_link_res.card_id \n")
				.append("	from res_couple t \n")
				.append("	join attribute_value av_link_res on t.level_res = av_link_res.number_value and av_link_res.attribute_code = 'JBR_RIMP_PARASSIG' \n")
				.append("	join card c_link_res on av_link_res.card_id = c_link_res.card_id and c_link_res.status_id in (103,206) \n")
				.append("), relation_doc as ( \n")
				.append("select cpl.og as doc, (max(ARRAY[(EXTRACT(EPOCH  FROM av_rel_reg_date.date_value)), av_relat.card_id]))[2] as relat \n")
				.append("	from res_couple cpl \n")
				.append("	join attribute_value av_relat on av_relat.number_value = cpl.og and av_relat.attribute_code = 'JBR_DOCL_RELATDOC' and av_relat.value_id = 1502 \n")
				.append("	join card c_relat on av_relat.card_id = c_relat.card_id and c_relat.status_id in (101,104) and c_relat.template_id = 364 \n")
				.append("	join attribute_value av_exec on av_exec.card_id = av_relat.card_id and av_exec.attribute_code = 'JBR_INFD_EXECUTOR' \n")
				.append("	join attribute_value av_res_exec on cpl.level_res = av_res_exec.card_id and av_res_exec.attribute_code in ('JBR_INFD_EXEC_LINK','ADMIN_255974') \n")
				.append("		and av_res_exec.number_value = av_exec.number_value \n")
				.append("	join attribute_value av_rel_reg_date on av_relat.card_id = av_rel_reg_date.card_id and av_rel_reg_date.attribute_code = 'JBR_REGD_DATEREG' \n")
				.append("	left join attribute_value av_test on cpl.level_res = av_test.number_value and av_test.attribute_code = 'JBR_RIMP_PARASSIG' \n")
				.append("	left join card c_test on av_test.card_id = c_test.card_id and c_test.status_id in (103,206) \n")
				.append("	where c_test.card_id is null \n")
				.append("	group by \n")
				.append("	cpl.og, cpl.rassm) \n")
				.append("select sub.doc, sub.relat, av_reg_date.date_value as in_reg_date, av_reg_num.string_value as in_reg_num, \n")
				.append("string_agg(case when av_res_rassm_type.value_id = 6555 then vl_res_rassm.value_rus else (case when av_res_rassm_type.value_id = 6556 then vl_res_redir.value_rus end) end,', ') as res_rassm \n")
				.append("into temp table relation_doc \n")
				.append("from relation_doc sub \n")
				.append("join attribute_value av_reg_date on av_reg_date.card_id = sub.relat and av_reg_date.attribute_code = 'JBR_REGD_DATEREG' \n")
				.append("join attribute_value av_reg_num on sub.relat = av_reg_num.card_id and av_reg_num.attribute_code = 'JBR_REGD_REGNUM' \n")
				.append("left join attribute_value av_res_rassm on av_res_rassm.card_id = sub.relat and av_res_rassm.attribute_code = 'ADMIN_283926' \n")
				.append("left join attribute_value av_res_rassm_reslut on av_res_rassm_reslut.card_id = av_res_rassm.number_value and av_res_rassm_reslut.attribute_code ='JBR_DECISION_RES' \n")
				.append("left join attribute_value av_res_rassm_redir on av_res_rassm_redir.card_id = av_res_rassm.number_value and av_res_rassm_redir.attribute_code = 'JBR_REDIRECTION' \n")
				.append("left join attribute_value av_res_rassm_type on av_res_rassm_type.card_id = av_res_rassm.number_value and av_res_rassm_type.attribute_code = 'JBR_ACC_DECISION' \n")
				.append("left join values_list vl_res_rassm on av_res_rassm_reslut.value_id = vl_res_rassm.value_id \n")
				.append("left join values_list vl_res_redir on av_res_rassm_redir.value_id = vl_res_redir.value_id \n")
				.append("group by sub.doc, sub.relat, av_reg_date.date_value, av_reg_num.string_value; \n")
				.append(" \n")
				.append("create index relation_doc_rel on relation_doc using btree(doc); \n")
				.append("analyze relation_doc; \n")
				.append(" \n")
				.append("with recursive res_couple as ( \n")
				.append("	select c_og.card_id as og, card_raasm.card_id as rassm, c_res1.card_id as level_res \n")
				.append("	from og_cards_archive c_og \n")
				.append("	left join attribute_value_archive av_rassm on c_og.card_id = av_rassm.card_id \n")
				.append("		and av_rassm.attribute_code = 'JBR_IMPL_ACQUAINT' \n")
				.append("	join card_archive card_raasm on card_raasm.card_id = av_rassm.number_value and card_raasm.status_id != 34145 \n")
				.append("	join attribute_value_archive av_rmain on av_rmain.card_id = card_raasm.card_id \n")
				.append("		and av_rmain.attribute_code = 'JBR_RESPONS_CONSIDER' and av_rmain.value_id = 1449 \n")
				.append("	join attribute_value_archive av_rassm_p on av_rassm_p.card_id = card_raasm.card_id \n")
				.append("		and av_rassm_p.attribute_code = 'JBR_RASSM_PERSON' \n")
				.append(" \n")
				.append("	join attribute_value_archive av_res1 on av_res1.number_value = c_og.card_id \n")
				.append("		and av_res1.attribute_code = 'JBR_DOCB_BYDOC' \n")
				.append("	join card_archive c_res1 on av_res1.card_id = c_res1.card_id and c_res1.status_id in (103,206) \n")
				.append(" \n")
				.append("	join attribute_value_archive av_res_signer on av_res1.card_id = av_res_signer.card_id \n")
				.append("		and av_res_signer.attribute_code = 'JBR_INFD_SGNEX_LINK' and av_res_signer.number_value = av_rassm_p.number_value \n")
				.append(" \n")
				.append("	UNION \n")
				.append(" \n")
				.append("	select t.og, t.rassm, c_link_res.card_id \n")
				.append("	from res_couple t \n")
				.append("	join attribute_value_archive av_link_res on t.level_res = av_link_res.number_value and av_link_res.attribute_code = 'JBR_RIMP_PARASSIG' \n")
				.append("	join card_archive c_link_res on av_link_res.card_id = c_link_res.card_id and c_link_res.status_id in (103,206) \n")
				.append("), relation_doc as ( \n")
				.append("select cpl.og as doc, (max(ARRAY[(EXTRACT(EPOCH  FROM av_rel_reg_date.date_value)), av_relat.card_id]))[2] as relat \n")
				.append("	from res_couple cpl \n")
				.append("	join attribute_value av_relat on av_relat.number_value = cpl.og and av_relat.attribute_code = 'JBR_DOCL_RELATDOC' and av_relat.value_id = 1502 \n")
				.append("	join card c_relat on av_relat.card_id = c_relat.card_id and c_relat.status_id in (101,104) and c_relat.template_id = 364 \n")
				.append("	join attribute_value av_exec on av_exec.card_id = av_relat.card_id and av_exec.attribute_code = 'JBR_INFD_EXECUTOR' \n")
				.append("	join attribute_value_archive av_res_exec on cpl.level_res = av_res_exec.card_id and av_res_exec.attribute_code in ('JBR_INFD_EXEC_LINK','ADMIN_255974') \n")
				.append("		and av_res_exec.number_value = av_exec.number_value \n")
				.append("	join attribute_value av_rel_reg_date on av_relat.card_id = av_rel_reg_date.card_id and av_rel_reg_date.attribute_code = 'JBR_REGD_DATEREG' \n")
				.append("	left join attribute_value_archive av_test on cpl.level_res = av_test.number_value and av_test.attribute_code = 'JBR_RIMP_PARASSIG' \n")
				.append("	left join card_archive c_test on av_test.card_id = c_test.card_id and c_test.status_id in (103,206) \n")
				.append("	where c_test.card_id is null \n")
				.append("	group by \n")
				.append("	cpl.og, cpl.rassm) \n")
				.append("select sub.doc, sub.relat, av_reg_date.date_value as in_reg_date, av_reg_num.string_value as in_reg_num, \n")
				.append("string_agg(case when av_res_rassm_type.value_id = 6555 then vl_res_rassm.value_rus else (case when av_res_rassm_type.value_id = 6556 then vl_res_redir.value_rus end) end,', ') as res_rassm \n")
				.append("into temp table relation_doc_archive \n")
				.append("from relation_doc sub \n")
				.append("join attribute_value av_reg_date on av_reg_date.card_id = sub.relat and av_reg_date.attribute_code = 'JBR_REGD_DATEREG' \n")
				.append("join attribute_value av_reg_num on sub.relat = av_reg_num.card_id and av_reg_num.attribute_code = 'JBR_REGD_REGNUM' \n")
				.append("left join attribute_value av_res_rassm on av_res_rassm.card_id = sub.relat and av_res_rassm.attribute_code = 'ADMIN_283926' \n")
					.append("left join attribute_value av_res_rassm_reslut on av_res_rassm_reslut.card_id = av_res_rassm.number_value and av_res_rassm_reslut.attribute_code ='JBR_DECISION_RES' \n")
				.append("left join attribute_value av_res_rassm_redir on av_res_rassm_redir.card_id = av_res_rassm.number_value and av_res_rassm_redir.attribute_code = 'JBR_REDIRECTION' \n")
					.append("left join attribute_value av_res_rassm_type on av_res_rassm_type.card_id = av_res_rassm.number_value and av_res_rassm_type.attribute_code = 'JBR_ACC_DECISION' \n")
					.append("left join values_list vl_res_rassm on av_res_rassm_reslut.value_id = vl_res_rassm.value_id \n")
				.append("left join values_list vl_res_redir on av_res_rassm_redir.value_id = vl_res_redir.value_id \n")
					.append("group by sub.doc, sub.relat, av_reg_date.date_value, av_reg_num.string_value; \n")
					.append(" \n")
					.append("create index relation_doc_archive_rel on relation_doc_archive using btree(doc); \n")
				.append("analyze relation_doc_archive; \n");
		return stringBuffer.toString();
	}
}
