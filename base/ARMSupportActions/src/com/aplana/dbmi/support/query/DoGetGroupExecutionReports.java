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
package com.aplana.dbmi.support.query;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.model.AccessCard;
import com.aplana.dbmi.jbr.action.GetBoss;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.query.DoSearch;
import com.aplana.dbmi.support.action.GetGroupExecutionReports;

public class DoGetGroupExecutionReports extends ActionQueryBase {

	private static final long serialVersionUID = 1L;

	@Override
	public Object processQuery() throws DataException {
		GetGroupExecutionReports action = (GetGroupExecutionReports) getAction();
		Search.Filter filter = new Search().getFilter();
		filter.setCurrentUserRestrict(Search.Filter.CU_READ_PERMISSION);
		
		StringBuffer selectSqlBuffer = new StringBuffer();
		
		List <ObjectId> personIds = null;
		if(action.isAsistantMode()){
			GetBoss getBossAction = new GetBoss();
			getBossAction.setAssistantIds(Collections.singletonList(getUser().getPerson().getId()));
			ActionQueryBase query = (ActionQueryBase)getQueryFactory().getActionQuery(getBossAction);
			query.setAction(getBossAction);
			personIds = (List<ObjectId>)getDatabase().executeQuery(getUser(), query);
		} else {
			personIds = Collections.singletonList(getUser().getPerson().getId());
		}
		String sPersonIds = ObjectIdUtils.numericIdsToCommaDelimitedString(personIds);
		selectSqlBuffer.append("select c.card_id, av_res_res.string_value, av_res_deadline.date_value, signer_pers.full_name,  \n" +
				"	av_doc_regnum.string_value, av_doc_regdate.date_value, av_doc_desc.string_value from card c \n" +
				"join attribute_value executor on c.card_id = executor.card_id and executor.attribute_code = 'ADMIN_702335' and executor.number_value in ("+sPersonIds+") \n" +
				"join attribute_value av_res on av_res.card_id = c.card_id and av_res.attribute_code = 'ADMIN_702311' \n" +
				"join attribute_value av_doc on av_doc.card_id = c.card_id and av_doc.attribute_code = 'ADMIN_702604' \n" +
				"left join attribute_value av_res_res on av_res_res.card_id = av_res.number_value and av_res_res.attribute_code = 'JBR_GIPA_RESOLUT' \n" +
				"left join attribute_value av_res_deadline on av_res_deadline.card_id = av_res.number_value and av_res_deadline.attribute_code in ('JBR_TCON_TERM','JBR_IMPL_DEADLINE') \n" +
				"join attribute_value av_res_signer on av_res_signer.card_id = av_res.number_value and av_res_signer.attribute_code ='JBR_INFD_SGNEX_LINK' \n" +
				"join person signer_pers on signer_pers.person_id = av_res_signer.number_value \n" +
				"left join attribute_value av_doc_regnum on av_doc_regnum.card_id = av_doc.number_value and av_doc_regnum.attribute_code = 'JBR_REGD_REGNUM' \n" +
				"left join attribute_value av_doc_regdate on av_doc_regdate.card_id = av_doc.number_value and av_doc_regdate.attribute_code = 'JBR_REGD_DATEREG' \n" +
				"left join attribute_value av_doc_desc on av_doc_desc.card_id = av_doc.number_value and av_doc_desc.attribute_code = 'JBR_INFD_SHORTDESC' \n" +
				"where c.template_id = 1044 and c.status_id = 702239 \n");
		
		DoSearch.emmitPermissionWhere(selectSqlBuffer, filter, (Long)getUser().getPerson().getId().getId(), "c");
				
		return getJdbcTemplate().query(selectSqlBuffer.toString(),
				new RowMapper(){
					@Override
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
						Map<String,String> row = new HashMap<String,String>();
						row.put("id",new Long(rs.getLong(1)).toString());
						row.put("resolution",rs.getString(2));
						row.put("deadline",rs.getDate(3) == null ? null : simpleDateFormat.format(rs.getDate(3)));
						row.put("signer",rs.getString(4));
						row.put("regnum",rs.getString(5));
						row.put("regdate",rs.getDate(6) == null ? null : simpleDateFormat.format(rs.getDate(6)));
						row.put("descr",rs.getString(7));
						return row;
					}
					
				}
			);
		
	}
}