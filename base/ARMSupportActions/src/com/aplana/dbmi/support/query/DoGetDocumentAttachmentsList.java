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
import java.util.HashMap;
import java.util.Map;

import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.query.DoSearch;
import com.aplana.dbmi.support.action.GetDocumentAttachmentsList;

public class DoGetDocumentAttachmentsList extends ActionQueryBase {
	private static final long serialVersionUID = 1L;

	@Override
	public Object processQuery() throws DataException {
		GetDocumentAttachmentsList action = getAction();
		StringBuffer selectSqlBuffer = new StringBuffer();
		Search.Filter filter = new Search().getFilter();
		filter.setCurrentUserRestrict(Search.Filter.CU_READ_PERMISSION);
		
		selectSqlBuffer.append("select cards.number_value as card_id, av_name.string_value as name, av_file.string_value, 'DOCLINKS' = cards.attribute_code as files from \n" +
				"(--������������ \n" +
				"select av_parent.card_id as parent, c.card_id, av.number_value, av.attribute_code from card c  \n" +
				"join attribute_value av_parent on av_parent.number_value = c.card_id and av_parent.attribute_code in ('JBR_IMPL_ACQUAINT','JBR_SIGN_SIGNING')  \n" +
				"join attribute_value av on av_parent.card_id = av.card_id and av.attribute_code in ('DOCLINKS','INFO_DOCLINKS') \n" +
				"where c.card_id in (" + action.getObjectId().getId() + ") \n" +
					"UNION \n" +
				"--�� \n" +
				"select c.card_id as parent, c.card_id, av.number_value, av.attribute_code from card c  \n" +
				"join attribute_value av on c.card_id = av.card_id and av.attribute_code in ('DOCLINKS','INFO_DOCLINKS') \n" +
				"where c.template_id in (224,364,784,864,1226,764,1255,1600) and c.card_id in (" + action.getObjectId().getId() + ") \n" +
					"UNION \n" +
				"--������������ \n" +
				"select couple.parent, couple.card_id, av.number_value, av.attribute_code from  \n" +
				"(select c.card_id, functionbacklink(c.card_id,'ADMIN_713517','JBR_INFORM_LIST') as parent from card c \n" +
				"where c.card_id in (" + action.getObjectId().getId() + ")) as couple \n" +
				"join attribute_value av on couple.parent = av.card_id and av.attribute_code in ('DOCLINKS','INFO_DOCLINKS') \n" +
					"UNION \n" +
				"--������ �� ����������/����� �� ���������� ������� ������������/������������ � ���������� \n" +
				"select av_parent.number_value as parent, c.card_id, av.number_value, av.attribute_code from card c  \n" +
				"join attribute_value av_parent on av_parent.card_id = c.card_id and av_parent.attribute_code in ('ADMIN_702604','ADMIN_702602','ADMIN_726877')  \n" +
				"join attribute_value av on av_parent.number_value = av.card_id and av.attribute_code in ('DOCLINKS','INFO_DOCLINKS') \n" +
				"where c.card_id in (" + action.getObjectId().getId() + ")  \n" +
					"UNION \n" +
				"--��������� \n" +
				"select av_parent.number_value as parent, c.card_id, av.number_value, av.attribute_code from card c  \n" +
				"join attribute_value av_parent on av_parent.card_id = c.card_id and av_parent.attribute_code in ('JBR_MAINDOC')  \n" +
				"join attribute_value av on av_parent.number_value = av.card_id and av.attribute_code in ('DOCLINKS','INFO_DOCLINKS') \n" +
				"where c.card_id in (" + action.getObjectId().getId() + ")  \n" +
					"UNION \n" +
				"--������������ \n" +
				"select couple.parent, couple.card_id, av.number_value, av.attribute_code from  \n" +
				"(select c.card_id, functionbacklink(c.card_id,'ADMIN_6814498','JBR_VISA_VISA') as parent from card c \n" +
				"where c.card_id in (" + action.getObjectId().getId() + ")) as couple \n" +
				"join attribute_value av on couple.parent = av.card_id and av.attribute_code in ('DOCLINKS','INFO_DOCLINKS') \n" +
				") as cards \n" +
				"join card cc on cards.parent = cc.card_id \n" +
				"join attribute_value av_name on cards.number_value = av_name.card_id and av_name.attribute_code = 'NAME' \n" +
				"join attribute_value av_file on cards.number_value = av_file.card_id and av_file.attribute_code = 'MATERIAL_NAME' \n" +
				"left join attribute_value av_prime on cards.number_value = av_prime.card_id and av_prime.attribute_code = 'IS_PRIME' \n" +
				"where 1=1 \n"); 
		
		DoSearch.emmitPermissionWhere(selectSqlBuffer, filter, (Long)getUser().getPerson().getId().getId(), "cc");
		selectSqlBuffer.append(" order by av_prime.value_id, cards.attribute_code, cards.number_value");
		
		return getJdbcTemplate().query(selectSqlBuffer.toString(),
				new RowMapper(){
					@Override
					public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
						Map<String,String> row = new HashMap<String,String>();
						row.put(GetDocumentAttachmentsList.ATTR_ID,new Long(rs.getLong(1)).toString());
						row.put(GetDocumentAttachmentsList.ATTR_NAME,rs.getString(2));
						row.put(GetDocumentAttachmentsList.ATTR_FILE,rs.getString(3));
						row.put(GetDocumentAttachmentsList.ATTR_ISFILE,rs.getBoolean(4)?"true":"false");
						return row;
					}
					
				}
			);
	}

}
