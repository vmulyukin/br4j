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
package com.aplana.dbmi.service.impl.query;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.RowCallbackHandler;

import com.aplana.dbmi.action.GetNestedCardsByTemplate;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;

/**
 * ��������� �������� �������� �������� �������, 
 * ��������������� ��������� ��������
 * @author ppolushkin
 *
 */
public class DoGetNestedCardsByTemplate extends ActionQueryBase {

	private static final long serialVersionUID = 1L;
	
	protected static Log logger = LogFactory.getLog(DoGetNestedCardsByTemplate.class);

	@Override
	public Object processQuery() throws DataException {
		
		final GetNestedCardsByTemplate action = (GetNestedCardsByTemplate) getAction();
		
		final List<ObjectId> result = new ArrayList<ObjectId>();
		
		if(action != null && action.isCardId() && action.isTemplates()) {
			
			StringBuilder sql = new StringBuilder();
			sql.append("WITH RECURSIVE t(nv) as ( \n");
			sql.append("select t_temp.nv as nv from ( \n");
			sql.append("	select distinct \n");
			sql.append("	av.number_value as nv \n");
			sql.append("	from card c \n");
			sql.append("	join attribute_value av on c.card_id = av.card_id and av.attribute_code not in ({2})\n");
			sql.append("	join attribute a on av.attribute_code = a.attribute_code and a.data_type in (''C'', ''E'', ''F'') \n");
			sql.append("	join card c_c on av.number_value = c_c.card_id and c_c.template_id in ({0}) \n");
			sql.append("	where c.card_id in ({1}) \n");
			sql.append("	UNION \n");
			sql.append("	select distinct \n");
			sql.append("	av.card_id as nv \n");
			sql.append("	from card c \n");
			sql.append("	join attribute_value av on c.card_id = av.number_value and av.attribute_code not in ({2})\n");
			sql.append("	join attribute a on av.attribute_code = a.attribute_code and a.data_type in (''C'', ''E'', ''F'') \n");
			sql.append("	join card c_c on av.card_id = c_c.card_id and c_c.template_id in ({0}) \n");
			sql.append("	where c.card_id in ({1}) \n");
			sql.append(" ) as t_temp \n");	
			sql.append("UNION \n");
			sql.append("	select distinct \n");
			sql.append("	av.number_value as nv \n");
			sql.append("	from card c, t, attribute_value av, attribute a, card c_c \n");
			sql.append("	where t.nv = c.card_id and c.card_id = av.card_id and t.nv <> av.number_value \n");
			sql.append("	and av.attribute_code = a.attribute_code and a.data_type in (''C'', ''E'', ''F'') \n");
			sql.append("	and av.number_value = c_c.card_id and c_c.template_id in ({0}) \n");
			sql.append("	and av.attribute_code not in ({2})\n");
			sql.append(") select distinct nv from t ");
			
			String excludedLinksSqlParam = action.getExcludedLinks() != null ? IdUtils.makeIdCodesQuotedEnum(action.getExcludedLinks(), ",", "'") : "''";
			getJdbcTemplate().query(
					MessageFormat.format(sql.toString(), 
							IdUtils.makeIdCodesEnum(action.getUsedTemplates(),","),
							String.valueOf(action.getCardId().getId()),
							excludedLinksSqlParam
					),
					new RowCallbackHandler() {
						@SuppressWarnings("unused")
						public void processRow(ResultSet rs) throws SQLException {
							ObjectId number_value = new ObjectId(Card.class, rs.getLong(1));
							if(number_value == null) {
								logger.warn("number_values is null");
								return;
							}
							result.add(number_value);
						}
					}
				);
			
		} else {
			logger.warn("Action is not usable");
		}
		
		return result;
	}

}
