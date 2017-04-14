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
/**
 *  ���������� sql-������ �� ������� ������ ��� "������ �� �������� �������������� ��������� �������"
 */
package com.aplana.dbmi.jasperreports;

import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.card.CardPortlet;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

/**
 * @author ppolushkin
 *
 */
public class GenerateClassificatorOG {
	public static final String VALUE_ALL = "-1";
	
	protected static Log logger = LogFactory.getLog(GenerateClassificatorOG.class);
	
	private XPathExpression recordExpression;
	
	private Connection conn = null;
	private List<ClassificatorOG> records = null;
	private XPath xpath;
	
	private Long timeZone = null;
	
	public GenerateClassificatorOG() {
		XPathFactory factory = XPathFactory.newInstance();
		xpath = factory.newXPath();
		timeZone = new Long(Calendar.getInstance().getTimeZone().getRawOffset() / 3600000);
	}
	
	@SuppressWarnings("finally")
	public JRDataSource generate(Connection conn, Date from, Date to, String section, String subject, String theme, String question, String subquestion) {
		this.conn = conn;
		records = new LinkedList<ClassificatorOG>();
		
		if(section == null && subject == null && theme == null && question == null && subquestion == null)
			return new JRBeanCollectionDataSource(records);

		StringBuilder query = getQuery(from, to, section, subject, theme, question, subquestion);
		
		logger.info(query);
		
		try {
			Statement stmt = this.conn.createStatement();
			ResultSet rs = stmt.executeQuery(query.toString());
	
			while (rs.next()) {
				ClassificatorOG cog = new ClassificatorOG();
				
				cog.setCode(rs.getString(1));
				
				cog.setSectionId(rs.getLong(2));
				if(section != null){
					cog.setSectionName(rs.getString(3));
				} else
					cog.setSectionName("no_column");
				
				cog.setSubjectId(rs.getLong(4));
				if(subject != null){
					cog.setSubjectName(rs.getString(5));
				} else
					cog.setSubjectName("no_column");
				
				cog.setThemeId(rs.getLong(6));
				if(theme != null){
					cog.setThemeName(rs.getString(7));
				} else
					cog.setThemeName("no_column");
				
				cog.setQuestionId(rs.getLong(8));
				if(question != null){
					cog.setQuestionName(rs.getString(9));
				} else 
					cog.setQuestionName("no_column");
				
				cog.setSubquestionId(rs.getLong(10));
				if(subquestion != null){
					cog.setSubquestionName(rs.getString(11));
				} else 
					cog.setSubquestionName("no_column");
				
				cog.setKolvo(rs.getLong(12));
															
				records.add(cog);				
			}			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return new JRBeanCollectionDataSource(records);
		}		
	}
	
	private StringBuilder getQuery(Date from, Date to, String section, String subject, String theme, String question, String subquestion){
		StringBuilder sb = new StringBuilder();
		boolean unionFlag = false;
				
		if(section != null){
			getSection(sb, from, to, section);
			unionFlag = true;
		}
		
		if(unionFlag && subject != null){
			sb.append(" UNION \n");
			unionFlag = false;
		}
		
		if(subject != null){
			getSubject(sb, from, to, subject, section);
			unionFlag = true;
		}
		
		if(unionFlag && theme != null){
			sb.append(" UNION \n");
			unionFlag = false;
		}
		
		if(theme != null){
			getTheme(sb, from, to, theme, subject, section);
			unionFlag = true;
		}
		
		if(unionFlag && question != null){
			sb.append(" UNION \n");
			unionFlag = false;
		}
		
		if(question != null){
			getQuestion(sb, from, to, question, theme, subject, section);
			unionFlag = true;
		}
		
		if(unionFlag && subquestion != null){
			sb.append(" UNION \n");
			unionFlag = false;
		}
		
		if(subquestion != null){
			getSubquestion(sb, from, to, subquestion, question, theme, subject, section);
		}
		
		sb.append("order by section_name, subject_name, theme_name, que_name, subq_name; \n");
		
		return sb;
	}
	
private void getSection(StringBuilder sb, Date from, Date to, String section){
		
		sb.append("select \n");
		sb.append("av_code.string_value as code, \n");
		sb.append("c_sec.card_id as section_id, \n");
		sb.append("av_sec.string_value as section_name, \n");
		sb.append("0 as subject_id, \n");
		sb.append("'' as subject_name, \n");
		sb.append("0 as theme_id, \n");
		sb.append("'' as theme_name, \n");
		sb.append("0 as que_id, \n");
		sb.append("'' as que_name, \n");
		sb.append("0 as subq_id, \n");
		sb.append("'' as subq_name, \n");
		sb.append("( \n");
		sb.append("		select count(*) \n");
		sb.append("		from ( \n");
		
		sb.append(" 	select og_que.card_id as nv \n");
		sb.append("		from card c \n");
		sb.append("		join attribute_value og_date_reg on (og_date_reg.card_id = c.card_id and og_date_reg.attribute_code = 'JBR_REGD_DATEREG') \n");
		sb.append("		join attribute_value og_que on (og_que.card_id = c.card_id and og_que.attribute_code = 'ADMIN_277251') \n");
		sb.append("		join attribute_value og_que_back on (og_que_back.card_id = og_que.number_value and og_que_back.attribute_code = 'ADMIN_278045') \n");
		sb.append("		join attribute_value og_the_back on (og_the_back.card_id = og_que_back.number_value and og_the_back.attribute_code = 'ADMIN_275720') \n");
		sb.append("		join attribute_value og_sub_back on (og_sub_back.card_id = og_the_back.number_value and og_sub_back.attribute_code = 'ADMIN_275722') \n");
		sb.append("		join card c_opt on c_opt.card_id = og_que.number_value \n");
		sb.append("		where (1=1) \n");		
		filterByTemplateStatus(sb);		
		filterByRegDate(sb, from, to);
		sb.append("		and c_opt.template_id = 884 \n");
		sb.append("		and og_sub_back.number_value = c_sec.card_id \n");
		sb.append(" 	UNION \n\n");
		sb.append(" 	select og_subq.card_id as nv \n");
		sb.append("		from card c \n");
		sb.append("		join attribute_value og_date_reg on (og_date_reg.card_id = c.card_id and og_date_reg.attribute_code = 'JBR_REGD_DATEREG') \n");
		sb.append("		join attribute_value og_subq on (og_subq.card_id = c.card_id and og_subq.attribute_code = 'ADMIN_277251') \n");
		sb.append("		join attribute_value og_que on (og_que.card_id = og_subq.number_value and og_que.attribute_code = 'JBR_QUESTION') \n");
		sb.append("		join attribute_value og_que_back on (og_que_back.card_id = og_que.number_value and og_que_back.attribute_code = 'ADMIN_278045') \n");
		sb.append("		join attribute_value og_the_back on (og_the_back.card_id = og_que_back.number_value and og_the_back.attribute_code = 'ADMIN_275720') \n");
		sb.append("		join attribute_value og_sub_back on (og_sub_back.card_id = og_the_back.number_value and og_sub_back.attribute_code = 'ADMIN_275722') \n");
		sb.append("		join card c_opt on c_opt.card_id = og_subq.number_value \n");
		sb.append("		where (1=1) \n");		
		filterByTemplateStatus(sb);		
		filterByRegDate(sb, from, to);
		sb.append("		and c_opt.template_id = 885 \n");
		sb.append("		and og_sub_back.number_value = c_sec.card_id \n");
		
		sb.append("		) as inner_sec \n");
		sb.append(" ) as kolvo \n");

		sb.append("from card c_sec \n");

		sb.append("join attribute_value av_sec on av_sec.card_id = c_sec.card_id and av_sec.attribute_code = 'NAME' \n");

		sb.append("join attribute_value av_code on av_code.card_id = c_sec.card_id and av_code.attribute_code = 'ADMIN_275723' \n");

		sb.append("where c_sec.template_id = 802 \n");
		
		if(!VALUE_ALL.equals(section)){
			sb.append("and c_sec.card_id in ( ");
			sb.append(section);
			sb.append(" ) \n");
		}
}
	
	private void getSubject(StringBuilder sb, Date from, Date to, String subject, String section){
		
		sb.append("select \n");
		sb.append("av_code.string_value as code, \n");
		sb.append("c_sec.card_id as section_id, \n");
		sb.append("av_sec.string_value as section_name, \n");
		sb.append("c_sub.card_id as subject_id, \n");
		sb.append("av_sub.string_value as subject_name, \n");
		sb.append("0 as theme_id, \n");
		sb.append("'' as theme_name, \n");
		sb.append("0 as que_id, \n");
		sb.append("'' as que_name, \n");
		sb.append("0 as subq_id, \n");
		sb.append("'' as subq_name, \n");
		sb.append("( \n");
		sb.append("		select count(*) \n");
		sb.append("		from ( \n");
		
		sb.append(" 	select og_que.card_id as nv \n");
		sb.append("		from card c \n");
		sb.append("		join attribute_value og_date_reg on (og_date_reg.card_id = c.card_id and og_date_reg.attribute_code = 'JBR_REGD_DATEREG') \n");
		sb.append("		join attribute_value og_que on (og_que.card_id = c.card_id and og_que.attribute_code = 'ADMIN_277251') \n");
		sb.append("		join attribute_value og_que_back on (og_que_back.card_id = og_que.number_value and og_que_back.attribute_code = 'ADMIN_278045') \n");
		sb.append("		join attribute_value og_the_back on (og_the_back.card_id = og_que_back.number_value and og_the_back.attribute_code = 'ADMIN_275720') \n");
		sb.append("		join card c_opt on c_opt.card_id = og_que.number_value \n");
		sb.append("		where (1=1) \n");		
		filterByTemplateStatus(sb);		
		filterByRegDate(sb, from, to);
		sb.append("		and c_opt.template_id = 884 \n");
		sb.append("		and og_the_back.number_value = c_sub.card_id \n");
		sb.append(" 	UNION \n");
		sb.append(" 	select og_subq.card_id as nv \n");
		sb.append("		from card c \n");
		sb.append("		join attribute_value og_date_reg on (og_date_reg.card_id = c.card_id and og_date_reg.attribute_code = 'JBR_REGD_DATEREG') \n");
		sb.append("		join attribute_value og_subq on (og_subq.card_id = c.card_id and og_subq.attribute_code = 'ADMIN_277251') \n");
		sb.append("		join attribute_value og_que on (og_que.card_id = og_subq.number_value and og_que.attribute_code = 'JBR_QUESTION') \n");
		sb.append("		join attribute_value og_que_back on (og_que_back.card_id = og_que.number_value and og_que_back.attribute_code = 'ADMIN_278045') \n");
		sb.append("		join attribute_value og_the_back on (og_the_back.card_id = og_que_back.number_value and og_the_back.attribute_code = 'ADMIN_275720') \n");
		sb.append("		join card c_opt on c_opt.card_id = og_subq.number_value \n");
		sb.append("		where (1=1) \n");		
		filterByTemplateStatus(sb);		
		filterByRegDate(sb, from, to);
		sb.append("		and c_opt.template_id = 885 \n");
		sb.append("		and og_the_back.number_value = c_sub.card_id \n");
		
		sb.append("		) as inner_subj \n");
		sb.append(" ) as kolvo \n");
		

		sb.append("from card c_sub \n");

		sb.append("join attribute_value av_sub on av_sub.card_id = c_sub.card_id and av_sub.attribute_code = 'NAME' \n");
		sb.append("join attribute_value av_sub_back on av_sub_back.card_id = c_sub.card_id and av_sub_back.attribute_code = 'ADMIN_275722' \n");

		sb.append("join attribute_value av_code on av_code.card_id = c_sub.card_id and av_code.attribute_code = 'ADMIN_275723' \n");

		sb.append("join card c_sec on c_sec.card_id = av_sub_back.number_value \n");
		sb.append("join attribute_value av_sec on av_sec.card_id = c_sec.card_id and av_sec.attribute_code = 'NAME' \n");

		sb.append("where c_sub.template_id = 844 \n");
		
		if(!VALUE_ALL.equals(subject)){
			sb.append("and c_sub.card_id in ( ");
			sb.append(subject);
			sb.append(" ) \n");
		}
		
		if(VALUE_ALL.equals(subject) && !VALUE_ALL.equals(section) && section != null){
			sb.append("and c_sec.card_id in ( ");
			sb.append(section);
			sb.append(" ) \n");
		}
		
		sb.append("and c_sec.template_id = 802 \n");
	}
	
	private void getTheme(StringBuilder sb, Date from, Date to, String theme, String subject, String section){
		
		sb.append("select \n");
		sb.append("av_code.string_value as code, \n");
		sb.append("c_sec.card_id as section_id, \n");
		sb.append("av_sec.string_value as section_name, \n");
		sb.append("c_sub.card_id as subject_id, \n");
		sb.append("av_sub.string_value as subject_name, \n");
		sb.append("c_the.card_id as theme_id, \n");
		sb.append("av_the.string_value as theme_name, \n");
		sb.append("0 as que_id, \n");
		sb.append("'' as que_name, \n");
		sb.append("0 as subq_id, \n");
		sb.append("'' as subq_name, \n");
		sb.append("( \n");
		sb.append(" 	select count(*) \n");
		sb.append("		from ( \n");
		
		sb.append(" 	select og_que.card_id as nv \n");
		sb.append(" 	from card c \n");
		sb.append(" 	join attribute_value og_date_reg on (og_date_reg.card_id = c.card_id and og_date_reg.attribute_code = 'JBR_REGD_DATEREG') \n");
		sb.append("		join attribute_value og_que on (og_que.card_id = c.card_id and og_que.attribute_code = 'ADMIN_277251') \n");
		sb.append("		join attribute_value og_que_back on (og_que_back.card_id = og_que.number_value and og_que_back.attribute_code = 'ADMIN_278045') \n");
		sb.append("		join card c_opt on c_opt.card_id = og_que.number_value \n");
		sb.append("		where (1=1) \n");
		filterByTemplateStatus(sb);
		filterByRegDate(sb, from, to);
		sb.append("		and c_opt.template_id = 884 \n");
		sb.append(" 	and og_que_back.number_value = c_the.card_id \n");
		sb.append(" 	UNION \n");
		sb.append(" 	select og_subq.card_id as nv \n");
		sb.append(" 	from card c \n");
		sb.append(" 	join attribute_value og_date_reg on (og_date_reg.card_id = c.card_id and og_date_reg.attribute_code = 'JBR_REGD_DATEREG') \n");
		sb.append("		join attribute_value og_subq on (og_subq.card_id = c.card_id and og_subq.attribute_code = 'ADMIN_277251') \n");
		sb.append("		join attribute_value og_que on (og_que.card_id = og_subq.number_value and og_que.attribute_code = 'JBR_QUESTION') \n");
		sb.append("		join attribute_value og_que_back on (og_que_back.card_id = og_que.number_value and og_que_back.attribute_code = 'ADMIN_278045') \n");
		sb.append("		join card c_opt on c_opt.card_id = og_subq.number_value \n");
		sb.append("		where (1=1) \n");
		filterByTemplateStatus(sb);
		filterByRegDate(sb, from, to);
		sb.append("		and c_opt.template_id = 885 \n");
		sb.append(" 	and og_que_back.number_value = c_the.card_id \n");
		
		sb.append("		) as inner_the \n");
		sb.append(" ) as kolvo \n");
		

		sb.append("from card c_the \n");

		sb.append("join attribute_value av_the on av_the.card_id = c_the.card_id and av_the.attribute_code = 'NAME' \n");
		sb.append("join attribute_value av_the_back on av_the_back.card_id = c_the.card_id and av_the_back.attribute_code = 'ADMIN_275720' \n");

		sb.append("join attribute_value av_code on av_code.card_id = c_the.card_id and av_code.attribute_code = 'ADMIN_275723' \n");

		sb.append("join card c_sub on c_sub.card_id = av_the_back.number_value \n");
		sb.append("join attribute_value av_sub on av_sub.card_id = c_sub.card_id and av_sub.attribute_code = 'NAME' \n");
		sb.append("join attribute_value av_sub_back on av_sub_back.card_id = c_sub.card_id and av_sub_back.attribute_code = 'ADMIN_275722' \n");

		sb.append("join card c_sec on c_sec.card_id = av_sub_back.number_value \n");
		sb.append("join attribute_value av_sec on av_sec.card_id = c_sec.card_id and av_sec.attribute_code = 'NAME' \n");

		sb.append("where c_the.template_id = 803 \n");
		
		if(!VALUE_ALL.equals(theme)){
			sb.append("and c_the.card_id in ( ");
			sb.append(theme);
			sb.append(" ) \n");
		}
		
		if(VALUE_ALL.equals(theme) && !VALUE_ALL.equals(subject) && subject != null){
			sb.append("and c_sub.card_id in ( ");
			sb.append(subject);
			sb.append(" ) \n");
		}
		
		if(VALUE_ALL.equals(theme) && VALUE_ALL.equals(subject) 
				&& !VALUE_ALL.equals(section) && section != null){
			sb.append("and c_sec.card_id in ( ");
			sb.append(section);
			sb.append(" ) \n");
		}
		
		sb.append("and c_sub.template_id = 844 \n");
		sb.append("and c_sec.template_id = 802 \n");
	}
	
	private void getQuestion(StringBuilder sb, Date from, Date to, String question, String theme, String subject, String section){
		
		sb.append("select \n"); 
		sb.append("av_code.string_value as code, \n");
		sb.append("c_sec.card_id as section_id, \n");
		sb.append("av_sec.string_value as section_name, \n");
		sb.append("c_sub.card_id as subject_id, \n");
		sb.append("av_sub.string_value as subject_name, \n");
		sb.append("c_the.card_id as theme_id, \n");
		sb.append("av_the.string_value as theme_name, \n");
		sb.append("c_que.card_id as que_id, \n");
		sb.append("av_que.string_value as que_name, \n");
		sb.append("0 as subq_id, \n");
		sb.append("'' as subq_name, \n");
		sb.append("( \n");
		sb.append("		select count(*) \n");
		sb.append("		from ( \n");
		
		sb.append("		select og_que.card_id as nv \n");
		sb.append("		from card c \n");
		sb.append("		join attribute_value og_date_reg on (og_date_reg.card_id = c.card_id and og_date_reg.attribute_code = 'JBR_REGD_DATEREG') \n");
		sb.append("		join attribute_value og_que on (og_que.card_id = c.card_id and og_que.attribute_code = 'ADMIN_277251') \n");
		sb.append("		join card c_opt on c_opt.card_id = og_que.number_value \n");
		sb.append("		where (1=1) \n");		
		filterByTemplateStatus(sb);
		filterByRegDate(sb, from, to);		
		sb.append("		and c_opt.template_id = 884 \n");
		sb.append("		and og_que.number_value = c_que.card_id \n");
		sb.append("		UNION \n");
		sb.append("		select og_subq.card_id as nv \n");
		sb.append("		from card c \n");
		sb.append("		join attribute_value og_date_reg on (og_date_reg.card_id = c.card_id and og_date_reg.attribute_code = 'JBR_REGD_DATEREG') \n");
		sb.append("		join attribute_value og_subq on (og_subq.card_id = c.card_id and og_subq.attribute_code = 'ADMIN_277251') \n");
		sb.append("		join attribute_value og_que on (og_que.card_id = og_subq.number_value and og_que.attribute_code = 'JBR_QUESTION') \n");
		sb.append("		join card c_opt on c_opt.card_id = og_subq.number_value \n");
		sb.append("		where (1=1) \n");		
		filterByTemplateStatus(sb);		
		filterByRegDate(sb, from, to);
		sb.append("		and c_opt.template_id = 885 \n");
		sb.append("		and og_que.number_value = c_que.card_id \n");
		
		sb.append("		) as inner_que \n");		
		sb.append(" ) as kolvo \n");
		

		sb.append("from card c_que \n");
		sb.append("join attribute_value av_que on av_que.card_id = c_que.card_id and av_que.attribute_code = 'NAME' \n");
		sb.append("join attribute_value av_que_back on av_que_back.card_id = c_que.card_id and av_que_back.attribute_code = 'ADMIN_278045' \n");

		sb.append("join attribute_value av_code on av_code.card_id = c_que.card_id and av_code.attribute_code = 'ADMIN_275723' \n");

		sb.append("join card c_the on c_the.card_id = av_que_back.number_value \n");
		sb.append("join attribute_value av_the on av_the.card_id = c_the.card_id and av_the.attribute_code = 'NAME' \n");
		sb.append("join attribute_value av_the_back on av_the_back.card_id = c_the.card_id and av_the_back.attribute_code = 'ADMIN_275720' \n");

		sb.append("join card c_sub on c_sub.card_id = av_the_back.number_value \n");
		sb.append("join attribute_value av_sub on av_sub.card_id = c_sub.card_id and av_sub.attribute_code = 'NAME' \n");
		sb.append("join attribute_value av_sub_back on av_sub_back.card_id = c_sub.card_id and av_sub_back.attribute_code = 'ADMIN_275722' \n");

		sb.append("join card c_sec on c_sec.card_id = av_sub_back.number_value \n");
		sb.append("join attribute_value av_sec on av_sec.card_id = c_sec.card_id and av_sec.attribute_code = 'NAME' \n");

		sb.append("where c_que.template_id = 884 \n");
		
		if(!VALUE_ALL.equals(question)){
			sb.append("and c_que.card_id in ( ");
			sb.append(question);
			sb.append(" ) \n");
		}
		
		if(VALUE_ALL.equals(question) && !VALUE_ALL.equals(theme) && theme != null){
			sb.append("and c_the.card_id in ( ");
			sb.append(theme);
			sb.append(" ) \n");
		}
		
		if(VALUE_ALL.equals(question) && VALUE_ALL.equals(theme) 
				&& !VALUE_ALL.equals(subject) && subject != null){
			sb.append("and c_sub.card_id in ( ");
			sb.append(subject);
			sb.append(" ) \n");
		}
		
		if(VALUE_ALL.equals(question) && VALUE_ALL.equals(theme) 
				&& VALUE_ALL.equals(subject) && !VALUE_ALL.equals(section) && section != null){
			sb.append("and c_sec.card_id in ( ");
			sb.append(section);
			sb.append(" ) \n");
		}
		
		sb.append("and c_the.template_id = 803 \n");
		sb.append("and c_sub.template_id = 844 \n");
		sb.append("and c_sec.template_id = 802 \n");	
		
	}
	
	
	private void getSubquestion(StringBuilder sb, Date from, Date to, String subquestion, String question, String theme, String subject, String section){
		
		sb.append("select \n");
		sb.append("av_code.string_value as code, \n");
		sb.append("c_sec.card_id as section_id, \n");
		sb.append("av_sec.string_value as section_name, \n");
		sb.append("c_sub.card_id as subject_id, \n");
		sb.append("av_sub.string_value as subject_name, \n");
		sb.append("c_the.card_id as theme_id, \n");
		sb.append("av_the.string_value as theme_name, \n");
		sb.append("c_que.card_id as que_id, \n");
		sb.append("av_que.string_value as que_name, \n");
		sb.append("c_subq.card_id as subq_id, \n");
		sb.append("av_subq.string_value as subq_name, \n");
		sb.append("( \n");
		sb.append("		select count(*) \n");
		sb.append("		from card c \n");
		sb.append("		join attribute_value og_date_reg on (og_date_reg.card_id = c.card_id and og_date_reg.attribute_code = 'JBR_REGD_DATEREG') \n");
		sb.append("		join attribute_value og_subq on (og_subq.card_id = c.card_id and og_subq.attribute_code = 'ADMIN_277251') \n");
		sb.append("		join card c_opt on c_opt.card_id = og_subq.number_value \n");
		sb.append("		where (1=1) \n");
		filterByTemplateStatus(sb);		
		filterByRegDate(sb, from, to);
		sb.append("		and c_opt.template_id = 885 \n");
		sb.append("		and og_subq.number_value = c_subq.card_id \n");
		sb.append(" ) as kolvo \n");
		
		sb.append("from card c_subq \n");
		sb.append("join attribute_value av_subq on av_subq.card_id = c_subq.card_id and av_subq.attribute_code = 'NAME' \n");
		sb.append("join attribute_value av_subq_back on av_subq_back.card_id = c_subq.card_id and av_subq_back.attribute_code = 'JBR_QUESTION' \n");
		
		sb.append("join attribute_value av_code on av_code.card_id = c_subq.card_id and av_code.attribute_code = 'ADMIN_275723' \n");
		
		sb.append("join card c_que on c_que.card_id = av_subq_back.number_value \n");
		sb.append("join attribute_value av_que on av_que.card_id = c_que.card_id and av_que.attribute_code = 'NAME' \n");
		sb.append("join attribute_value av_que_back on av_que_back.card_id = c_que.card_id and av_que_back.attribute_code = 'ADMIN_278045' \n");
		
		sb.append("join card c_the on c_the.card_id = av_que_back.number_value \n");
		sb.append("join attribute_value av_the on av_the.card_id = c_the.card_id and av_the.attribute_code = 'NAME' \n");
		sb.append("join attribute_value av_the_back on av_the_back.card_id = c_the.card_id and av_the_back.attribute_code = 'ADMIN_275720' \n");

		sb.append("join card c_sub on c_sub.card_id = av_the_back.number_value \n");
		sb.append("join attribute_value av_sub on av_sub.card_id = c_sub.card_id and av_sub.attribute_code = 'NAME' \n");
		sb.append("join attribute_value av_sub_back on av_sub_back.card_id = c_sub.card_id and av_sub_back.attribute_code = 'ADMIN_275722' \n");
		
		sb.append("join card c_sec on c_sec.card_id = av_sub_back.number_value \n");
		sb.append("join attribute_value av_sec on av_sec.card_id = c_sec.card_id and av_sec.attribute_code = 'NAME' \n");
		
		sb.append("where c_subq.template_id = 885 \n");
		
		if(!VALUE_ALL.equals(subquestion)){
			sb.append("and c_subq.card_id in ( ");
			sb.append(subquestion);
			sb.append(" ) \n");
		}
		
		if(VALUE_ALL.equals(subquestion) && !VALUE_ALL.equals(question) && question != null){
			sb.append("and c_que.card_id in ( ");
			sb.append(question);
			sb.append(" ) \n");
		}
		
		if(VALUE_ALL.equals(subquestion) && VALUE_ALL.equals(question) 
				&& !VALUE_ALL.equals(theme) && theme != null){
			sb.append("and c_the.card_id in ( ");
			sb.append(theme);
			sb.append(" ) \n");
		}
		
		if(VALUE_ALL.equals(subquestion) && VALUE_ALL.equals(question) 
				&& VALUE_ALL.equals(theme) && !VALUE_ALL.equals(subject) && subject != null){
			sb.append("and c_sub.card_id in ( ");
			sb.append(subject);
			sb.append(" ) \n");
		}
		
		if(VALUE_ALL.equals(subquestion) && VALUE_ALL.equals(question) 
				&& VALUE_ALL.equals(theme) && VALUE_ALL.equals(subject) 
				&& !VALUE_ALL.equals(section) && section != null){
			sb.append("and c_sec.card_id in ( ");
			sb.append(section);
			sb.append(" ) \n");
		}
		
		sb.append("and c_que.template_id = 884 \n");
		sb.append("and c_the.template_id = 803 \n");
		sb.append("and c_sub.template_id = 844 \n");
		sb.append("and c_sec.template_id = 802 \n");
		
	}
	
	
	@SuppressWarnings("finally")
	public Long getSummary(Connection conn, Date from, Date to, String section, String subject, String theme, String question, String subquestion){
		
		this.conn = conn;
		
		if(section == null && subject == null && theme == null && question == null && subquestion == null)
			return new Long(0);
		
		StringBuilder sb = new StringBuilder();
		boolean unionFlag = false;
		Long result = new Long(0);
		
		sb.append("select distinct count(*) \n");
		sb.append("from \n");

		sb.append("( \n");
		
		if(subquestion != null){
			getSubquestionSummary(sb, from, to, subquestion);
			unionFlag = true;
		}
		
		if(unionFlag && question != null){
			sb.append(" UNION \n");
			unionFlag = false;
		}
		
		if(question != null){
			getQuestionSummary(sb, from, to, question);
			unionFlag = true;
		}
		
		if(unionFlag && theme != null){
			sb.append(" UNION \n");
			unionFlag = false;
		}
		
		if(theme != null){
			getThemeSummary(sb, from, to, theme);
			unionFlag = true;
		}
		
		if(unionFlag && subject != null){
			sb.append(" UNION \n");
			unionFlag = false;
		}
		
		if(subject != null){
			getSubjectSummary(sb, from, to, subject);
			unionFlag = true;
		}

		if(unionFlag && section != null){
			sb.append(" UNION \n");
			unionFlag = false;
		}
		
		if(section != null){
			getSectionSummary(sb, from, to, section);
		}
		
		sb.append(" ) as all_cards; \n");
		
		logger.info(sb);
		
		try {
			Statement stmt = this.conn.createStatement();
			ResultSet rs = stmt.executeQuery(sb.toString());			
	
			while (rs.next()) {
				
				result = rs.getLong(1);							
			}			
		} catch (Exception e) {
			e.printStackTrace();
			result = -1L;
		} finally {
			return result;
		}
	}
	
	private void getSubquestionSummary(StringBuilder sb, Date from, Date to, String subquestion){
		
		sb.append("select distinct c.card_id as card_id \n");
		sb.append("from card c \n");
		sb.append("join attribute_value og_date_reg on (og_date_reg.card_id = c.card_id and og_date_reg.attribute_code = 'JBR_REGD_DATEREG') \n");
		sb.append("join attribute_value subq on (subq.card_id = c.card_id and subq.attribute_code = 'ADMIN_277251') \n");
		sb.append("join card c_opt on c_opt.card_id = subq.number_value \n");
		sb.append("where (1=1) \n");
		filterByTemplateStatus(sb);
		filterByRegDate(sb, from, to);
		sb.append("and c_opt.template_id = 885 \n");
		
		if(!VALUE_ALL.equalsIgnoreCase(subquestion)){
			sb.append("and subq.number_value in ( ");
			sb.append(subquestion);
			sb.append(" ) \n");			
		}
	}
	
	private void getQuestionSummary(StringBuilder sb, Date from, Date to, String question){
		
		sb.append("select distinct c.card_id as card_id \n");
		sb.append("from card c \n");
		sb.append("join attribute_value og_date_reg on (og_date_reg.card_id = c.card_id and og_date_reg.attribute_code = 'JBR_REGD_DATEREG') \n");
		sb.append("join attribute_value que on (que.card_id = c.card_id and que.attribute_code = 'ADMIN_277251') \n");
		sb.append("join card c_opt on c_opt.card_id = que.number_value \n");
		sb.append("where (1=1) \n");
		filterByTemplateStatus(sb);
		filterByRegDate(sb, from, to);
		sb.append("and c_opt.template_id = 884 \n");
		
		if(!VALUE_ALL.equalsIgnoreCase(question)){
			sb.append("and que.number_value in ( ");
			sb.append(question);
			sb.append(" ) \n");			
		}
		
		sb.append(" UNION \n");
		
		sb.append("select distinct c.card_id as card_id \n");
		sb.append("from card c \n");
		sb.append("join attribute_value og_date_reg on (og_date_reg.card_id = c.card_id and og_date_reg.attribute_code = 'JBR_REGD_DATEREG') \n");
		sb.append("join attribute_value subq on (subq.card_id = c.card_id and subq.attribute_code = 'ADMIN_277251') \n");
		sb.append("join attribute_value que on (que.card_id = subq.number_value and que.attribute_code = 'JBR_QUESTION') \n");
		sb.append("join card c_opt on c_opt.card_id = subq.number_value \n");
		sb.append("where (1=1) \n");
		filterByTemplateStatus(sb);
		filterByRegDate(sb, from, to);
		sb.append("and c_opt.template_id = 885 \n");
		
		if(!VALUE_ALL.equalsIgnoreCase(question)){
			sb.append("and que.number_value in ( ");
			sb.append(question);
			sb.append(" ) \n");			
		}
	}
	
	private void getThemeSummary(StringBuilder sb, Date from, Date to, String theme){
		
		sb.append("select distinct c.card_id as card_id \n");
		sb.append("from card c \n");
		sb.append("join attribute_value og_date_reg on (og_date_reg.card_id = c.card_id and og_date_reg.attribute_code = 'JBR_REGD_DATEREG') \n");
		sb.append("join attribute_value que on (que.card_id = c.card_id and que.attribute_code = 'ADMIN_277251') \n");
		sb.append("join attribute_value theme on (theme.card_id = que.number_value and theme.attribute_code = 'ADMIN_278045') \n");
		sb.append("join card c_opt on c_opt.card_id = que.number_value \n");
		sb.append("where (1=1) \n");
		filterByTemplateStatus(sb);
		filterByRegDate(sb, from, to);
		sb.append("and c_opt.template_id = 884 \n");
		
		if(!VALUE_ALL.equalsIgnoreCase(theme)){
			sb.append("and theme.number_value in ( ");
			sb.append(theme);
			sb.append(" ) \n");			
		}
		
		sb.append(" UNION \n");
		
		sb.append("select distinct c.card_id as card_id \n");
		sb.append("from card c \n");
		sb.append("join attribute_value og_date_reg on (og_date_reg.card_id = c.card_id and og_date_reg.attribute_code = 'JBR_REGD_DATEREG') \n");
		sb.append("join attribute_value subq on (subq.card_id = c.card_id and subq.attribute_code = 'ADMIN_277251') \n");
		sb.append("join attribute_value que on (que.card_id = subq.number_value and que.attribute_code = 'JBR_QUESTION') \n");
		sb.append("join attribute_value theme on (theme.card_id = que.number_value and theme.attribute_code = 'ADMIN_278045') \n");
		sb.append("join card c_opt on c_opt.card_id = subq.number_value \n");
		sb.append("where (1=1) \n");
		filterByTemplateStatus(sb);
		filterByRegDate(sb, from, to);
		sb.append("and c_opt.template_id = 885 \n");
		
		if(!VALUE_ALL.equalsIgnoreCase(theme)){
			sb.append("and theme.number_value in ( ");
			sb.append(theme);
			sb.append(" ) \n");			
		}
	}
	
	private void getSubjectSummary(StringBuilder sb, Date from, Date to, String subject){
		
		sb.append("select distinct c.card_id as card_id \n");
		sb.append("from card c \n");
		sb.append("join attribute_value og_date_reg on (og_date_reg.card_id = c.card_id and og_date_reg.attribute_code = 'JBR_REGD_DATEREG') \n");
		sb.append("join attribute_value que on (que.card_id = c.card_id and que.attribute_code = 'ADMIN_277251') \n");
		sb.append("join attribute_value theme on (theme.card_id = que.number_value and theme.attribute_code = 'ADMIN_278045') \n");
		sb.append("join attribute_value subj on (subj.card_id = theme.number_value and subj.attribute_code = 'ADMIN_275720') \n");
		sb.append("join card c_opt on c_opt.card_id = que.number_value \n");
		sb.append("where (1=1) \n");
		filterByTemplateStatus(sb);
		filterByRegDate(sb, from, to);
		sb.append("and c_opt.template_id = 884 \n");
		
		if(!VALUE_ALL.equalsIgnoreCase(subject)){
			sb.append("and subj.number_value in ( ");
			sb.append(subject);
			sb.append(" ) \n");			
		}
		
		sb.append(" UNION \n");
		
		sb.append("select distinct c.card_id as card_id \n");
		sb.append("from card c \n");
		sb.append("join attribute_value og_date_reg on (og_date_reg.card_id = c.card_id and og_date_reg.attribute_code = 'JBR_REGD_DATEREG') \n");
		sb.append("join attribute_value subq on (subq.card_id = c.card_id and subq.attribute_code = 'ADMIN_277251') \n");
		sb.append("join attribute_value que on (que.card_id = subq.number_value and que.attribute_code = 'JBR_QUESTION') \n");
		sb.append("join attribute_value theme on (theme.card_id = que.number_value and theme.attribute_code = 'ADMIN_278045') \n");
		sb.append("join attribute_value subj on (subj.card_id = theme.number_value and subj.attribute_code = 'ADMIN_275720') \n");
		sb.append("join card c_opt on c_opt.card_id = subq.number_value \n");
		sb.append("where (1=1) \n");
		filterByTemplateStatus(sb);
		filterByRegDate(sb, from, to);
		sb.append("and c_opt.template_id = 885 \n");
		
		if(!VALUE_ALL.equalsIgnoreCase(subject)){
			sb.append("and subj.number_value in ( ");
			sb.append(subject);
			sb.append(" ) \n");			
		}
	}
	
	private void getSectionSummary(StringBuilder sb, Date from, Date to, String section){
		
		sb.append("select distinct c.card_id as card_id \n");
		sb.append("from card c \n");
		sb.append("join attribute_value og_date_reg on (og_date_reg.card_id = c.card_id and og_date_reg.attribute_code = 'JBR_REGD_DATEREG') \n");
		sb.append("join attribute_value que on (que.card_id = c.card_id and que.attribute_code = 'ADMIN_277251') \n");
		sb.append("join attribute_value theme on (theme.card_id = que.number_value and theme.attribute_code = 'ADMIN_278045') \n");
		sb.append("join attribute_value subj on (subj.card_id = theme.number_value and subj.attribute_code = 'ADMIN_275720') \n");
		sb.append("join attribute_value sect on (sect.card_id = subj.number_value and sect.attribute_code = 'ADMIN_275722') \n");
		sb.append("join card c_opt on c_opt.card_id = que.number_value \n");
		sb.append("where (1=1) \n");
		filterByTemplateStatus(sb);
		filterByRegDate(sb, from, to);
		sb.append("and c_opt.template_id = 884 \n");
		
		if(!VALUE_ALL.equalsIgnoreCase(section)){
			sb.append("and sect.number_value in ( ");
			sb.append(section);
			sb.append(" ) \n");
		}
		
		sb.append(" UNION \n");
		
		sb.append("select distinct c.card_id as card_id \n");
		sb.append("from card c \n");
		sb.append("join attribute_value og_date_reg on (og_date_reg.card_id = c.card_id and og_date_reg.attribute_code = 'JBR_REGD_DATEREG') \n");
		sb.append("join attribute_value subq on (subq.card_id = c.card_id and subq.attribute_code = 'ADMIN_277251') \n");
		sb.append("join attribute_value que on (que.card_id = subq.number_value and que.attribute_code = 'JBR_QUESTION') \n");
		sb.append("join attribute_value theme on (theme.card_id = que.number_value and theme.attribute_code = 'ADMIN_278045') \n");
		sb.append("join attribute_value subj on (subj.card_id = theme.number_value and subj.attribute_code = 'ADMIN_275720') \n");
		sb.append("join attribute_value sect on (sect.card_id = subj.number_value and sect.attribute_code = 'ADMIN_275722') \n");
		sb.append("join card c_opt on c_opt.card_id = subq.number_value \n");
		sb.append("where (1=1) \n");
		filterByTemplateStatus(sb);
		filterByRegDate(sb, from, to);
		sb.append("and c_opt.template_id = 885 \n");
		
		if(!VALUE_ALL.equalsIgnoreCase(section)){
			sb.append("and sect.number_value in ( ");
			sb.append(section);
			sb.append(" ) \n");
		}
	}
	
	private void filterByTemplateStatus(StringBuilder sb) {
		
		sb.append("and c.template_id = 864 and c.status_id not in (301,302,303990,1) \n");
		
	}
	
	private void filterByRegDate(StringBuilder sb, Date from, Date to) {
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
		
		sb.append("		and date_trunc('day', og_date_reg.date_value) <= '");
		sb.append(sdf.format(to));
		sb.append("' \n");
		sb.append("		and date_trunc('day', og_date_reg.date_value) >= '");
		sb.append(sdf.format(from));
		sb.append("' \n");
		
	}

}
