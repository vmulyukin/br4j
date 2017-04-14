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
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

public class GenerateOGSpisokProsroch {

	public static JRDataSource generate(Connection conn, Timestamp startDate, Timestamp endDate, String dept, Long tzHour) {
		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
		Statement statement = null;
		try {
			conn.setAutoCommit(false);
			statement = conn.createStatement();

			String condition = getDateDepartmentConndition(startDate, endDate, dept, tzHour);
			statement.execute(createDepartmentTempTable());
			statement.execute(createOgCardsTempTable(condition));
			statement.execute(createArchivedOgCardsTempTable(condition));
			statement.execute(createIntermidTempTables());
			ResultSet rs = statement.executeQuery(getReportSql());

			conn.commit();
			conn.setAutoCommit(true);

			while(rs.next()) {
				Map<String, Object> line = new HashMap<String, Object>();
				line.put("card_id", rs.getLong("card_id"));
				line.put("reg_date", rs.getTimestamp("reg_date"));
				line.put("reg_num", rs.getString("reg_num"));
				line.put("reg_num_d", rs.getBigDecimal("reg_num_d"));
				line.put("author_og", rs.getString("author_og"));
				line.put("descr", rs.getString("descr"));
				line.put("quest", rs.getString("quest"));
				line.put("on_control_val", rs.getString("on_control_val"));
				line.put("rassmotritel", rs.getString("rassmotritel"));
				line.put("plandate", rs.getTimestamp("plandate"));
				line.put("factddate", rs.getTimestamp("factddate"));

				resultList.add(line);

			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (statement != null) {
					statement.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return new JRBeanCollectionDataSource(resultList);
	}

	public static String getDateDepartmentConndition(Timestamp startDate, Timestamp endDate, String dept, Long tzHour) {
		String deptCond = (dept != null ? "\t\t\t and (d.depLvl2 in (" + dept + "))":"");
		return	String.format(
				"\t\t\t and (date_trunc('day',reg_date.date_value + interval '%1$d hour') >= timestamp '%2$tF')\n" +
				"\t\t\t and (date_trunc('day',reg_date.date_value + interval '%1$d hour') <= timestamp '%3$tF')\n" + 
				deptCond, 
				tzHour, startDate, endDate);
	}

	//��������� ������� ������ �������������
	public static String createDepartmentTempTable() {
		return "drop table IF EXISTS  departments_temp_table;\n" +
				"with recursive\n" +
				"search_up(cycle_id, cycle_number, cycle_base) AS (\n" +
				"	select cast(c.card_id::bigint as numeric),1, c.card_id from card c\n" +
				"    where c.status_id in (4,7) and c.template_id = 484\n" +
				"	UNION ALL\n" +
				"    SELECT v.number_value, su.cycle_number+1, su.cycle_base\n" +
				"	FROM attribute_value v, search_up su\n" +
				"    WHERE v.attribute_code = 'JBR_DEPT_PARENT_LINK'\n" +
				"    AND v.card_id = su.cycle_id\n" +
				"    and exists (select 1 from attribute_value\n" +
				"		where attribute_code = 'JBR_DEPT_PARENT_LINK'\n" +
				"		and card_id = v.number_value)),\n" +
				"departments as (select (max(ARRAY[cycle_number, cycle_id, cycle_base]))[3] as curDep,\n" +
							"(max(ARRAY[cycle_number, cycle_id, cycle_base]))[2] as depLvl2\n" +
						"from search_up\n" +
				"		group by cycle_base)\n" +
				"select * into temp table departments_temp_table from departments;\n" + 
				"create index departments_temp_table_idx on departments_temp_table (curdep);\n";
	}

	//��������� ������� � ���������� ��
	public static String createOgCardsTempTable(String condition) {
		return  "drop table IF EXISTS og_cards_temp_table;\n" +
				"with \n" +
				"og_cards as (\n" +
				"select c_og.card_id\n" +
				"	from card c_og\n" +
				"	join attribute_value reg_date on reg_date.card_id = c_og.card_id\n" +
				"	and reg_date.attribute_code = 'JBR_REGD_DATEREG'\n" +
				"\n" +
				"	left join attribute_value av_rassm_d on c_og.card_id = av_rassm_d.card_id and av_rassm_d.attribute_code = 'JBR_IMPL_ACQUAINT'\n" +
				"	left join card c_r_d on c_r_d.card_id = av_rassm_d.number_value and c_r_d.status_id != 34145\n" +
				"	left join attribute_value av_rassm_p_d on av_rassm_p_d.card_id = c_r_d.card_id and av_rassm_p_d.attribute_code = 'JBR_RASSM_PERSON'\n" +
				"	left join person p_rassm on  av_rassm_p_d.number_value = p_rassm.person_id\n" +
				"	left join attribute_value av_dept on av_dept.card_id = p_rassm.card_id and av_dept.attribute_code = 'JBR_PERS_DEPT_LINK'\n" +
				"	left join departments_temp_table d on d.curDep =  av_dept.number_value\n" +
				"\n" +
				"	where c_og.template_id = 864 and c_og.status_id in (102,103,206,48909)\n" + condition +
				"\n" +
				"union\n" +
				"\n" +
				"select c_og.card_id\n" +
				"	from card c_og\n" +
				"	join attribute_value reg_date on reg_date.card_id = c_og.card_id\n" +
				"	and reg_date.attribute_code = 'JBR_REGD_DATEREG'\n" +
				"\n" +
				"	left join attribute_value av_all_res on av_all_res.number_value = c_og.card_id and av_all_res.attribute_code = 'JBR_MAINDOC'\n" +
				"	left join card c_all_res on av_all_res.card_id = c_all_res.card_id and c_all_res.status_id not in(1,34145,303990)\n" +
				"	left join attribute_value av_res_exec on c_all_res.card_id = av_res_exec.card_id and av_res_exec.attribute_code in ('JBR_INFD_EXEC_LINK','ADMIN_255974')\n" +
				"	left join person p_rassm on  av_res_exec.number_value = p_rassm.person_id\n" +
				"	left join attribute_value av_dept on av_dept.card_id = p_rassm.card_id and av_dept.attribute_code = 'JBR_PERS_DEPT_LINK'\n" +
				"	left join departments_temp_table d on d.curDep =  av_dept.number_value\n" +
				"\n" +
				"	where c_og.template_id = 864 and c_og.status_id in (102,103,206,48909)\n" + condition +
				"\n" +
				")\n" +
				"select * into temp table og_cards_temp_table from og_cards;\n" + 
				"create index og_cards_temp_table_idx on og_cards_temp_table (card_id);\n";
	}

	//��������� ������� � ���������� �� � ������� � ����
	public static String createArchivedOgCardsTempTable(String condition) {
		return  "drop table IF EXISTS og_cards_archive_temp_table;\n" +
				"with og_cards_archive as (\n" +
				"select c_og.card_id\n" +
				"	from card c_og\n" +
				"	join attribute_value reg_date on reg_date.card_id = c_og.card_id\n" +
				"		and reg_date.attribute_code = 'JBR_REGD_DATEREG'\n" +
				"\n" +
				"	left join attribute_value_archive av_rassm_d on c_og.card_id = av_rassm_d.card_id and av_rassm_d.attribute_code = 'JBR_IMPL_ACQUAINT'\n" +
				"	left join card_archive c_r_d on c_r_d.card_id = av_rassm_d.number_value and c_r_d.status_id != 34145\n" +
				"	left join attribute_value_archive av_rassm_p_d on av_rassm_p_d.card_id = c_r_d.card_id and av_rassm_p_d.attribute_code = 'JBR_RASSM_PERSON'\n" +
				"	left join person p_rassm on  av_rassm_p_d.number_value = p_rassm.person_id\n" +
				"	left join attribute_value av_dept on av_dept.card_id = p_rassm.card_id and av_dept.attribute_code = 'JBR_PERS_DEPT_LINK'\n" +
				"	left join departments_temp_table d on d.curDep =  av_dept.number_value\n" +
				"\n" +
				"	where c_og.template_id = 864 and c_og.status_id in (104)\n" + condition +
				"\n" +
				"union\n" +
				"\n" +
				"select c_og.card_id\n" +
				"	from card c_og\n" +
				"	join attribute_value reg_date on reg_date.card_id = c_og.card_id\n" +
				"		and reg_date.attribute_code = 'JBR_REGD_DATEREG'\n" +
				"\n" +
				"	left join attribute_value_archive av_all_res on av_all_res.number_value = c_og.card_id and av_all_res.attribute_code = 'JBR_MAINDOC'\n" +
				"	left join card_archive c_all_res on av_all_res.card_id = c_all_res.card_id and c_all_res.status_id not in(1,34145,303990)\n" +
				"	left join attribute_value_archive av_res_exec on c_all_res.card_id = av_res_exec.card_id and av_res_exec.attribute_code in ('JBR_INFD_EXEC_LINK','ADMIN_255974')\n" +
				"	left join person p_rassm on  av_res_exec.number_value = p_rassm.person_id\n" +
				"	left join attribute_value av_dept on av_dept.card_id = p_rassm.card_id and av_dept.attribute_code = 'JBR_PERS_DEPT_LINK'\n" +
				"	left join departments_temp_table d on d.curDep =  av_dept.number_value\n" +
				"\n" +
				"	where c_og.template_id = 864 and c_og.status_id in (104)\n" + condition +
				")\n" +
				"select * into temp table og_cards_archive_temp_table from og_cards_archive;\n" +
				"create index og_cards_archive_temp_table_idx on og_cards_archive_temp_table (card_id);";
	}

	public static String createIntermidTempTables(){
		StringBuilder query = new StringBuilder();
		query.append("drop table if exists res_couple; \n");
		query.append("with recursive \n");
		query.append("res_couple as ( \n");
		query.append("	select c_og.card_id as og, card_raasm.card_id as rassm, c_res1.card_id as level_res \n");
		query.append("	from og_cards_temp_table c_og \n");
		query.append("	left join attribute_value av_rassm on c_og.card_id = av_rassm.card_id \n");
		query.append("		and av_rassm.attribute_code = 'JBR_IMPL_ACQUAINT' \n");
		query.append("	join card card_raasm on card_raasm.card_id = av_rassm.number_value and card_raasm.status_id != 34145 \n");
		query.append("	join attribute_value av_rmain on av_rmain.card_id = card_raasm.card_id \n");
		query.append("		and av_rmain.attribute_code = 'JBR_RESPONS_CONSIDER' and av_rmain.value_id = 1449 \n");
		query.append("	join attribute_value av_rassm_p on av_rassm_p.card_id = card_raasm.card_id \n");
		query.append("		and av_rassm_p.attribute_code = 'JBR_RASSM_PERSON' \n");
		query.append(" \n");
		query.append("	join attribute_value av_res1 on av_res1.number_value = c_og.card_id \n");
		query.append("		and av_res1.attribute_code = 'JBR_DOCB_BYDOC' \n");
		query.append("	join card c_res1 on av_res1.card_id = c_res1.card_id and c_res1.status_id in (103,206) \n");
		query.append(" \n");
		query.append("	join attribute_value av_res_signer on av_res1.card_id = av_res_signer.card_id \n");
		query.append("		and av_res_signer.attribute_code = 'JBR_INFD_SGNEX_LINK' and av_res_signer.number_value = av_rassm_p.number_value \n");
		query.append(" \n");
		query.append("	UNION \n");
		query.append(" \n");
		query.append("	select t.og, t.rassm, c_link_res.card_id \n");
		query.append("	from res_couple t \n");
		query.append("	join attribute_value av_link_res on t.level_res = av_link_res.number_value and av_link_res.attribute_code = 'JBR_RIMP_PARASSIG' \n");
		query.append("	join card c_link_res on av_link_res.card_id = c_link_res.card_id and c_link_res.status_id in (103,206) \n");
		query.append(") select * into temp table res_couple from res_couple; \n");
		query.append("create index cpl_og_idx on res_couple using btree(og); \n");
		query.append("create index cpl_level_res_idx on res_couple using btree(level_res); \n");
		query.append("create index cpl_rassm_idx on res_couple using btree(rassm); \n");
		query.append(" \n");
		query.append(" \n");
		query.append("drop table if exists res_couple_archive ; \n");
		query.append("with recursive res_couple_archive as ( \n");
		query.append("	select c_og.card_id as og, card_raasm.card_id as rassm, c_res1.card_id as level_res \n");
		query.append("	from og_cards_archive_temp_table c_og \n");
		query.append("	left join attribute_value_archive av_rassm on c_og.card_id = av_rassm.card_id \n");
		query.append("		and av_rassm.attribute_code = 'JBR_IMPL_ACQUAINT' \n");
		query.append("	join card_archive card_raasm on card_raasm.card_id = av_rassm.number_value and card_raasm.status_id != 34145 \n");
		query.append("	join attribute_value_archive av_rmain on av_rmain.card_id = card_raasm.card_id \n");
		query.append("		and av_rmain.attribute_code = 'JBR_RESPONS_CONSIDER' and av_rmain.value_id = 1449 \n");
		query.append("	join attribute_value_archive av_rassm_p on av_rassm_p.card_id = card_raasm.card_id \n");
		query.append("		and av_rassm_p.attribute_code = 'JBR_RASSM_PERSON' \n");
		query.append(" \n");
		query.append("	join attribute_value_archive av_res1 on av_res1.number_value = c_og.card_id \n");
		query.append("		and av_res1.attribute_code = 'JBR_DOCB_BYDOC' \n");
		query.append("	join card_archive c_res1 on av_res1.card_id = c_res1.card_id and c_res1.status_id in (103,206) \n");
		query.append(" \n");
		query.append("	join attribute_value_archive av_res_signer on av_res1.card_id = av_res_signer.card_id \n");
		query.append("		and av_res_signer.attribute_code = 'JBR_INFD_SGNEX_LINK' and av_res_signer.number_value = av_rassm_p.number_value \n");
		query.append(" \n");
		query.append("	UNION \n");
		query.append(" \n");
		query.append("	select t.og, t.rassm, c_link_res.card_id \n");
		query.append("	from res_couple_archive t \n");
		query.append("	join attribute_value_archive av_link_res on t.level_res = av_link_res.number_value and av_link_res.attribute_code = 'JBR_RIMP_PARASSIG' \n");
		query.append("	join card_archive c_link_res on av_link_res.card_id = c_link_res.card_id and c_link_res.status_id in (103,206) \n");
		query.append(") select * into temp table res_couple_archive from res_couple_archive; \n");
		query.append("create index cpl_og_arch_idx on res_couple_archive using btree(og); \n");
		query.append("create index cpl_level_res_arch_idx on res_couple_archive using btree(level_res); \n");
		query.append("create index cpl_rassm_arch_idx on res_couple_archive using btree(rassm); \n");
		query.append(" \n");
		query.append("drop table if exists doc_couple; \n");
		query.append("select cpl.og, cpl.rassm, (max(ARRAY[(EXTRACT(EPOCH  FROM av_rel_reg_date.date_value)), av_relat.card_id]))[2] as relat \n");
		query.append("INTO TEMP TABLE doc_couple \n");
		query.append("from res_couple cpl \n");
		query.append("join attribute_value av_relat on av_relat.number_value = cpl.og and av_relat.attribute_code = 'JBR_DOCL_RELATDOC' and av_relat.value_id = 1502 \n");
		query.append("join card c_relat on av_relat.card_id = c_relat.card_id and c_relat.status_id in (101,104) and c_relat.template_id = 364 \n");
		query.append("join attribute_value av_exec on av_exec.card_id = av_relat.card_id and av_exec.attribute_code = 'JBR_INFD_EXECUTOR' \n");
		query.append("join attribute_value av_res_exec on cpl.level_res = av_res_exec.card_id and av_res_exec.attribute_code in ('JBR_INFD_EXEC_LINK','ADMIN_255974') \n");
		query.append("	and av_res_exec.number_value = av_exec.number_value \n");
		query.append("join attribute_value av_rel_reg_date on av_relat.card_id = av_rel_reg_date.card_id and av_rel_reg_date.attribute_code = 'JBR_REGD_DATEREG' \n");
		query.append("left join attribute_value av_test on cpl.level_res = av_test.number_value and av_test.attribute_code = 'JBR_RIMP_PARASSIG' \n");
		query.append("left join card c_test on av_test.card_id = c_test.card_id and c_test.status_id in (103,206) \n");
		query.append("where c_test.card_id is null \n");
		query.append("group by \n");
		query.append("cpl.og, cpl.rassm; \n");
		query.append(" \n");
		query.append("create index dc_og_idx on doc_couple using btree (og); \n");
		query.append("create index dc_rassm_idx on doc_couple using btree (rassm); \n");
		query.append("create index dc_relat_idx on doc_couple using btree (relat); \n");
		query.append(" \n");
		query.append("drop table if exists doc_couple_archive; \n");
		query.append("select cpl.og, cpl.rassm, (max(ARRAY[(EXTRACT(EPOCH  FROM av_rel_reg_date.date_value)), av_relat.card_id]))[2] as relat \n");
		query.append("INTO TEMP TABLE doc_couple_archive \n");
		query.append("from res_couple_archive cpl \n");
		query.append("join attribute_value av_relat on av_relat.number_value = cpl.og and av_relat.attribute_code = 'JBR_DOCL_RELATDOC' and av_relat.value_id = 1502 \n");
		query.append("join card c_relat on av_relat.card_id = c_relat.card_id and c_relat.status_id in (101,104) and c_relat.template_id = 364 \n");
		query.append("join attribute_value av_exec on av_exec.card_id = av_relat.card_id and av_exec.attribute_code = 'JBR_INFD_EXECUTOR' \n");
		query.append("join attribute_value_archive av_res_exec on cpl.level_res = av_res_exec.card_id and av_res_exec.attribute_code in ('JBR_INFD_EXEC_LINK','ADMIN_255974') \n");
		query.append("	and av_res_exec.number_value = av_exec.number_value \n");
		query.append("join attribute_value av_rel_reg_date on av_relat.card_id = av_rel_reg_date.card_id and av_rel_reg_date.attribute_code = 'JBR_REGD_DATEREG' \n");
		query.append("left join attribute_value_archive av_test on cpl.level_res = av_test.number_value and av_test.attribute_code = 'JBR_RIMP_PARASSIG' \n");
		query.append("left join card_archive c_test on av_test.card_id = c_test.card_id and c_test.status_id in (103,206) \n");
		query.append("where c_test.card_id is null \n");
		query.append("group by \n");
		query.append("cpl.og, cpl.rassm; \n");
		query.append(" \n");
		query.append("create index dc_og_arch_idx on doc_couple_archive using btree (og); \n");
		query.append("create index dc_rassm_arch_idx on doc_couple_archive using btree (rassm); \n");
		query.append("create index dc_relat_arch_idx on doc_couple_archive using btree (relat); \n");
		query.append(" \n");
		query.append(" \n");
		query.append("drop table if exists spiso_prosroch_result; \n");
		query.append("select c.card_id, reg_date.date_value as reg_date, \n");
		query.append("		coalesce(reg_num.string_value,'') as reg_num, \n");
		query.append("		reg_num_d.number_value as reg_num_d, \n");
		query.append("		coalesce(string_agg(distinct coalesce(substring(og_author_nm.string_value for (position('(' in og_author_nm.string_value))-1),''), '; '),'') as author_og, \n");
		query.append("		coalesce(descr.string_value,'') as descr, \n");
		query.append("		coalesce(string_agg(distinct quest_nm.string_value, '; '),'') as quest, \n");
		query.append("		on_control_val.value_rus as on_control_val, \n");
		query.append("		dept_short_name.string_value as rassmotritel, \n");
		query.append("		av_rassm_todate.date_value as planDate, \n");
		query.append("		av_reg_date.date_value as factDdate \n");
		query.append("into temp table spiso_prosroch_result \n");
		query.append(" from og_cards_temp_table c \n");
		query.append("	join attribute_value reg_date on reg_date.card_id = c.card_id and reg_date.attribute_code = 'JBR_REGD_DATEREG' \n");
		query.append("	join attribute_value reg_num on reg_num.card_id = c.card_id and reg_num.attribute_code = 'JBR_REGD_REGNUM' \n");
		query.append("	join attribute_value reg_num_d on reg_num_d.card_id = c.card_id and reg_num_d.attribute_code = 'JBR_REGD_REGNUM_D' \n");
		query.append("	left join attribute_value og_author on og_author.card_id = c.card_id and og_author.attribute_code = 'JBR_OG_REQ_AUTHOR' \n");
		query.append("	left join attribute_value og_author_nm on og_author_nm.card_id = og_author.number_value and og_author_nm.attribute_code = 'NAME' \n");
		query.append("	left join attribute_value descr on descr.card_id = c.card_id and descr.attribute_code = 'JBR_INFD_SHORTDESC' \n");
		query.append("	left join attribute_value quest on quest.card_id = c.card_id and quest.attribute_code = 'JBR_QUEST_THEMATIC_C' \n");
		query.append("	left join attribute_value quest_nm on quest_nm.card_id = quest.number_value and quest_nm.attribute_code = 'NAME' \n");
		query.append(" \n");
		query.append("	left join attribute_value on_control on on_control.card_id = c.card_id and on_control.attribute_code = 'JBR_IMPL_ONCONT' \n");
		query.append("	left join values_list on_control_val on on_control_val.value_id = on_control.value_id \n");
		query.append("	join attribute_value av_rassm on c.card_id = av_rassm.card_id and av_rassm.attribute_code = 'JBR_IMPL_ACQUAINT' \n");
		query.append("	left join attribute_value av_rmain on av_rmain.card_id = av_rassm.number_value and av_rmain.attribute_code = 'JBR_RESPONS_CONSIDER' \n");
		query.append("	left join card c_r on c_r.card_id = av_rassm.number_value \n");
		query.append("	left join doc_couple dc on av_rassm.number_value = dc.rassm \n");
		query.append("	left join attribute_value av_reg_date on av_reg_date.card_id = dc.relat and av_reg_date.attribute_code = 'JBR_REGD_DATEREG' \n");
		query.append("	left join attribute_value av_rassm_todate on av_rassm.number_value = av_rassm_todate.card_id and  av_rassm_todate.attribute_code = 'JBR_RASSM_TODATE' \n");
		query.append("	left join attribute_value av_rassm_person on av_rassm_person.card_id = av_rassm.number_value and av_rassm_person.attribute_code = 'JBR_RASSM_PERSON' \n");
		query.append("	left join person p_rassm on av_rassm_person.number_value = p_rassm.person_id \n");
		query.append("	left join attribute_value av_dept on av_dept.card_id = p_rassm.card_id and av_dept.attribute_code = 'JBR_PERS_DEPT_LINK' \n");
		query.append("	left join departments_temp_table d on d.curDep =  av_dept.number_value \n");
		query.append("	left join attribute_value dept_short_name on d.depLvl2 = dept_short_name.card_id and dept_short_name.attribute_code = 'JBR_DEPT_SHORTNAME' \n");
		query.append("where av_rmain.value_id = 1449 and c_r.status_id != 34145 \n");
		query.append("group by reg_date, reg_num, reg_num_d, descr, on_control_val, rassmotritel, planDate, factDdate, c.card_id; \n");
		query.append(" \n");
		query.append("drop table if exists spiso_prosroch_arch_result; \n");
		query.append("select c.card_id, reg_date.date_value as reg_date, \n");
		query.append("		coalesce(reg_num.string_value,'') as reg_num, \n");
		query.append("		reg_num_d.number_value as reg_num_d, \n");
		query.append("		coalesce(string_agg(distinct coalesce(substring(og_author_nm.string_value for (position('(' in og_author_nm.string_value))-1),''), '; '),'') as author_og, \n");
		query.append("		coalesce(descr.string_value,'') as descr, \n");
		query.append("		coalesce(string_agg(distinct quest_nm.string_value, '; '),'') as quest, \n");
		query.append("		on_control_val.value_rus as on_control_val, \n");
		query.append("		dept_short_name.string_value as rassmotritel, \n");
		query.append("		av_rassm_todate.date_value as planDate, \n");
		query.append("		av_reg_date.date_value as factDdate \n");
		query.append("into temp table spiso_prosroch_arch_result \n");
		query.append(" from og_cards_archive_temp_table c \n");
		query.append("	join attribute_value reg_date on reg_date.card_id = c.card_id and reg_date.attribute_code = 'JBR_REGD_DATEREG' \n");
		query.append("	join attribute_value reg_num on reg_num.card_id = c.card_id and reg_num.attribute_code = 'JBR_REGD_REGNUM' \n");
		query.append("	join attribute_value reg_num_d on reg_num_d.card_id = c.card_id and reg_num_d.attribute_code = 'JBR_REGD_REGNUM_D' \n");
		query.append("	left join attribute_value og_author on og_author.card_id = c.card_id and og_author.attribute_code = 'JBR_OG_REQ_AUTHOR' \n");
		query.append("	left join attribute_value og_author_nm on og_author_nm.card_id = og_author.number_value and og_author_nm.attribute_code = 'NAME' \n");
		query.append("	left join attribute_value descr on descr.card_id = c.card_id and descr.attribute_code = 'JBR_INFD_SHORTDESC' \n");
		query.append("	left join attribute_value quest on quest.card_id = c.card_id and quest.attribute_code = 'JBR_QUEST_THEMATIC_C' \n");
		query.append("	left join attribute_value quest_nm on quest_nm.card_id = quest.number_value and quest_nm.attribute_code = 'NAME' \n");
		query.append(" \n");
		query.append("	left join attribute_value on_control on on_control.card_id = c.card_id and on_control.attribute_code = 'JBR_IMPL_ONCONT' \n");
		query.append("	left join values_list on_control_val on on_control_val.value_id = on_control.value_id \n");
		query.append("	join attribute_value_archive av_rassm on c.card_id = av_rassm.card_id and av_rassm.attribute_code = 'JBR_IMPL_ACQUAINT' \n");
		query.append("	left join attribute_value_archive av_rmain on av_rmain.card_id = av_rassm.number_value and av_rmain.attribute_code = 'JBR_RESPONS_CONSIDER' \n");
		query.append("	left join card_archive c_r on c_r.card_id = av_rassm.number_value \n");
		query.append("	left join doc_couple_archive dc on av_rassm.number_value = dc.rassm \n");
		query.append("	left join attribute_value av_reg_date on av_reg_date.card_id = dc.relat and av_reg_date.attribute_code = 'JBR_REGD_DATEREG' \n");
		query.append("	left join attribute_value_archive av_rassm_todate on av_rassm.number_value = av_rassm_todate.card_id and  av_rassm_todate.attribute_code = 'JBR_RASSM_TODATE' \n");
		query.append("	left join attribute_value_archive av_rassm_person on av_rassm_person.card_id = av_rassm.number_value and av_rassm_person.attribute_code = 'JBR_RASSM_PERSON' \n");
		query.append("	left join person p_rassm on av_rassm_person.number_value = p_rassm.person_id \n");
		query.append("	left join attribute_value av_dept on av_dept.card_id = p_rassm.card_id and av_dept.attribute_code = 'JBR_PERS_DEPT_LINK' \n");
		query.append("	left join departments_temp_table d on d.curDep =  av_dept.number_value \n");
		query.append("	left join attribute_value dept_short_name on d.depLvl2 = dept_short_name.card_id and dept_short_name.attribute_code = 'JBR_DEPT_SHORTNAME' \n");
		query.append(" \n");
		query.append("where av_rmain.value_id = 1449 and c_r.status_id != 34145 \n");
		query.append("group by reg_date, reg_num, reg_num_d, descr, on_control_val, rassmotritel, planDate, factDdate, c.card_id; \n");
		return query.toString();
	}

	public static String getReportSql() {
		StringBuilder query = new StringBuilder();
		query.append("select * from spiso_prosroch_result where \n");
		query.append("(extract (DAY FROM (factDdate - planDate))::NUMERIC > 0 \n");
		query.append("OR factDdate is null AND planDate < (SELECT CURRENT_DATE)) \n");
		query.append("union \n");
		query.append("select * from spiso_prosroch_arch_result where \n");
		query.append("(extract (DAY FROM (factDdate - planDate))::NUMERIC > 0 \n");
		query.append("OR factDdate is null AND planDate < (SELECT CURRENT_DATE)) \n");
		query.append("order by reg_num_d; \n");
		return query.toString();
	}

}
