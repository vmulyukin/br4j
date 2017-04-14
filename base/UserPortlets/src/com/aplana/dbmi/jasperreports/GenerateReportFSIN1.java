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

import com.aplana.dbmi.Portal;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by echirkov on 31.08.2015.
 */
public class GenerateReportFSIN1 {
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	public static final String CONFIG_FOLDER = "dbmi/jasperReports/reportSQL";
	public static final String CONFIG_FILE = CONFIG_FOLDER + "/FSIN1.sql";

	public static JRDataSource generate(Connection conn, Date startDate, Date endDate, String org, Long tzHour) {
		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
		Statement statement = null;
		InputStream is = null;
		try {
			conn.setAutoCommit(false);
			is = Portal.getFactory().getConfigService().loadConfigFile(CONFIG_FILE);
			String tempTablesSQL = IOUtils.toString(is, "UTF-8");
			tempTablesSQL = MessageFormat.format(tempTablesSQL.replaceAll("'", "''"),
					org, tzHour, sdf.format(startDate), sdf.format(endDate));
			statement = conn.createStatement();
			statement.execute(tempTablesSQL);
			statement.execute(generateResRassmTempTable("QR_FSIN_1_3_7"));
			statement.execute(generateResRassmTempTable("QR_FSIN_1_26_30"));
			ResultSet rs = statement.executeQuery(getQuerySQL(tzHour));

			while(rs.next()) {
				Map<String, Object> line = new HashMap<String, Object>();
				line.put("_1", rs.getLong("_1"));
				line.put("_2", rs.getLong("_2"));
				line.put("_3", rs.getLong("_3"));
				line.put("_4", rs.getLong("_4"));
				line.put("_5", rs.getLong("_5"));
				line.put("_6", rs.getLong("_6"));
				line.put("_7", rs.getLong("_7"));
				line.put("_8", rs.getLong("_8"));
				line.put("_22", rs.getLong("_22"));
				line.put("_23", rs.getLong("_23"));
				line.put("_24", rs.getLong("_24"));
				line.put("_25", rs.getLong("_25"));
				line.put("_26", rs.getLong("_26"));
				line.put("_27", rs.getLong("_27"));
				line.put("_28", rs.getLong("_28"));
				line.put("_29", rs.getLong("_29"));
				line.put("_30", rs.getLong("_30"));
				line.put("_31", rs.getLong("_31"));
				line.put("_50", rs.getLong("_50"));
				line.put("_51", rs.getLong("_51"));
				line.put("_52", rs.getLong("_52"));
				resultList.add(line);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(is);
			try {
				if (statement != null) {
					statement.execute("drop table if exists departments, QR_FSIN_1_1, QR_FSIN_1_otv, " +
							"QR_FSIN_1_neotv,QR_FSIN_1_rel_out, QR_FSIN_1_rel_int,QR_FSIN_1_2,QR_FSIN_1_3_7,QR_FSIN_1_26_30," +
							"qr_fsin_1_table_1_quest,qr_fsin_1_3_7_rr,qr_fsin_1_26_30_rr,qr_fsin_1_table_2_quest," +
							"QR_FSIN_1_25,QR_FSIN_1_rassm_to_date,QR_FSIN_1_26_30,QR_FSIN_1_26_30_rr;");
					statement.close();
				}
				conn.commit();
				conn.setAutoCommit(true);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return new JRBeanCollectionDataSource(resultList);
	}

	private static String getQuerySQL(Long tzHour) {
		StringBuilder sb = new StringBuilder();
		sb.append("select (select count(1) _1 from QR_FSIN_1_1), \n");
		sb.append("	(select count(1) _2 from QR_FSIN_1_2), \n");
		sb.append("	(select count(1) _3 from QR_FSIN_1_3_7_rr where res_rassm = 1240), \n");
		sb.append("	(select count(1) _4 from QR_FSIN_1_3_7_rr where res_rassm = 1243), \n");
		sb.append("	(select count(1) _5 from QR_FSIN_1_3_7_rr where res_rassm = 1242), \n");
		sb.append("	(select count(1) _6 from QR_FSIN_1_3_7_rr where redirect = 6557), \n");
		sb.append("	(select count(1) _7 from QR_FSIN_1_3_7_rr where redirect = 6558), \n");
		sb.append("	(select count(1) _8 from QR_FSIN_1_2 c \n");
		sb.append("	join attribute_value av_q on c.card_id = av_q.card_id and av_q.attribute_code = 'JBR_QUEST_THEMATIC_C'), \n");
		sb.append("	(select count(distinct c.card_id) _22 \n");
		sb.append("	from QR_FSIN_1_2 c \n");
		sb.append("	join QR_FSIN_1_rel_out c_out using(card_id) \n");
		sb.append("	join QR_FSIN_1_rassm_to_date to_date using(card_id) \n");
		sb.append("	join attribute_value av_rel_reg_date on c_out.relat = av_rel_reg_date.card_id and av_rel_reg_date.attribute_code = 'JBR_REGD_DATEREG' \n");
		sb.append("	where date_trunc('day',to_date.rassm_to_date + interval '"+tzHour+" hour') < date_trunc('day',av_rel_reg_date.date_value + interval '"+tzHour+" hour')),  \n");
		sb.append("	(select count(1) _23 from qr_fsin_1_2 c \n");
		sb.append("	join attribute_value control_type on c.card_id = control_type.card_id  \n");
		sb.append("	and control_type.attribute_code = 'JBR_IMPL_TYPECONT' and control_type.value_id in (3429,2193)), \n");
		sb.append("	(select count(distinct c.card_id) _24 \n");
		sb.append("		from QR_FSIN_1_2 c \n");
		sb.append("		join (select * from QR_FSIN_1_rel_out union select * from  QR_FSIN_1_rel_int) c_out using(card_id) \n");
		sb.append("		join attribute_value av_res_rassm on c_out.relat = av_res_rassm.card_id and av_res_rassm.attribute_code = 'ADMIN_283926' \n");
		sb.append("		join attribute_value av_outside on av_outside.card_id = av_res_rassm.number_value and av_outside.attribute_code = 'JBR_DECISION_OUT' and av_outside.value_id = 1449), \n");
		sb.append("	(select count(1) _25 from QR_FSIN_1_25), \n");
		sb.append("	(select count(1) _26 from QR_FSIN_1_26_30_rr where res_rassm = 1240), \n");
		sb.append("	(select count(1) _27 from QR_FSIN_1_26_30_rr where res_rassm = 1243), \n");
		sb.append("	(select count(1) _28 from QR_FSIN_1_26_30_rr where res_rassm = 1242), \n");
		sb.append("	(select count(1) _29 from QR_FSIN_1_26_30_rr where redirect = 6557), \n");
		sb.append("	(select count(1) _30 from QR_FSIN_1_26_30_rr where redirect = 6558), \n");
		sb.append("	(select count(1) _31 from QR_FSIN_1_25 c \n");
		sb.append("	join attribute_value av_q on c.card_id = av_q.card_id and av_q.attribute_code = 'JBR_QUEST_THEMATIC_C'), \n");
		sb.append("	(select count(distinct c.card_id) _50 \n");
		sb.append("	from QR_FSIN_1_25 c \n");
		sb.append("	join QR_FSIN_1_rel_out c_out using(card_id) \n");
		sb.append("	join QR_FSIN_1_rassm_to_date to_date using(card_id) \n");
		sb.append("	join attribute_value av_rel_reg_date on c_out.relat = av_rel_reg_date.card_id and av_rel_reg_date.attribute_code = 'JBR_REGD_DATEREG' \n");
		sb.append("	where date_trunc('day',to_date.rassm_to_date + interval '"+tzHour+" hour') < date_trunc('day',av_rel_reg_date.date_value + interval '"+tzHour+" hour')),  \n");
		sb.append("	(select count(1) _51 from qr_fsin_1_25 c \n");
		sb.append("	join attribute_value control_type on c.card_id = control_type.card_id  \n");
		sb.append("	and control_type.attribute_code = 'JBR_IMPL_TYPECONT' and control_type.value_id in (3429,2193)), \n");
		sb.append("	(select count(distinct c.card_id) _52 \n");
		sb.append("		from QR_FSIN_1_25 c \n");
		sb.append("		join (select * from QR_FSIN_1_rel_out union select * from  QR_FSIN_1_rel_int) c_out using(card_id) \n");
		sb.append("		join attribute_value av_res_rassm on c_out.relat = av_res_rassm.card_id and av_res_rassm.attribute_code = 'ADMIN_283926' \n");
		sb.append("		join attribute_value av_outside on av_outside.card_id = av_res_rassm.number_value and av_outside.attribute_code = 'JBR_DECISION_OUT' and av_outside.value_id = 1449);");
		return sb.toString();
	}
	
	private static String generateResRassmTempTable(String cardAlias){
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("with dat as ( \n");
		stringBuilder.append("	select c.card_id, av_res.value_id res_rassm, av_redir.value_id redirect from " + cardAlias +" c \n");
		stringBuilder.append("	join QR_FSIN_1_rel_out otv using(card_id) \n");
		stringBuilder.append("	join attribute_value av_rr on otv.relat = av_rr.card_id and av_rr.attribute_code = 'ADMIN_283926' \n");
		stringBuilder.append("	join attribute_value av_q on av_rr.number_value = av_q.card_id and av_q.attribute_code = 'JBR_QUESTIONS_RES' \n");
		stringBuilder.append("	left join attribute_value av_res on av_rr.number_value = av_res.card_id and av_res.attribute_code = 'JBR_DECISION_RES' \n");
		stringBuilder.append("	left join attribute_value av_redir on av_rr.number_value = av_redir.card_id and av_redir.attribute_code = 'JBR_REDIRECTION' \n");
		stringBuilder.append("	union \n");
		stringBuilder.append("	select c.card_id, av_res.value_id res, av_redir.value_id redir from " + cardAlias + " c \n");
		stringBuilder.append("	join QR_FSIN_1_rel_int otv using(card_id) \n");
		stringBuilder.append("	join attribute_value av_rr on otv.relat = av_rr.card_id and av_rr.attribute_code = 'ADMIN_283926' \n");
		stringBuilder.append("	left join attribute_value av_q on av_rr.number_value = av_q.card_id and av_q.attribute_code = 'JBR_QUESTIONS_RES' \n");
		stringBuilder.append("	left join attribute_value av_res on av_rr.number_value = av_res.card_id and av_res.attribute_code = 'JBR_DECISION_RES' \n");
		stringBuilder.append("	left join attribute_value av_redir on av_rr.number_value = av_redir.card_id and av_redir.attribute_code = 'JBR_REDIRECTION' \n");
		stringBuilder.append(") select * into temp table " + cardAlias + "_rr from dat; \n");
		return stringBuilder.toString();
	}
}
