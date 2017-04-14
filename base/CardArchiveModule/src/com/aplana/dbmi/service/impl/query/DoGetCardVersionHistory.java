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
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.RowCallbackHandler;

import com.aplana.dbmi.action.GetCardVersionHistory;
import com.aplana.dbmi.archive.export.AttributeValueHist;
import com.aplana.dbmi.archive.export.CardVersionHist;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;

/**
 * ��������� �������� ������ �� ������� card_version � attribute_value_hist
 * ��� ���������� ��������
 * @author ppolushkin
 *
 */
public class DoGetCardVersionHistory extends ActionQueryBase {

	private static final long serialVersionUID = 1L;
	
	protected static Log logger = LogFactory.getLog(DoGetCardVersionHistory.class);

	@Override
	public Object processQuery() throws DataException {
		
		final GetCardVersionHistory action = (GetCardVersionHistory) getAction();
		
		final List<CardVersionHist> result = new ArrayList<CardVersionHist>();
		
		if(action != null && action.isCardId()) {
			
			StringBuilder sql = new StringBuilder();
			sql.append("select cv.version_id, cv.version_date, cv.parent_card_id, cv.status_id, cv.file_name, cv.file_store_url, \n");
			sql.append("avh.attribute_code, avh.number_value, avh.string_value, avh.date_value, avh.value_id, avh.another_value, \n");
			sql.append("(select convert_from( \n");
			sql.append("		(select a.long_binary_value from attribute_value_hist a where a.card_id = cv.card_id and a.version_id = cv.version_id \n");
			sql.append("			and a.attribute_code = avh.attribute_code limit 1), ''UTF8'')) as long_binary_value, action_log_id \n");
			sql.append("from card_version cv \n");
			sql.append("join attribute_value_hist avh on cv.card_id = avh.card_id and cv.version_id = avh.version_id \n");
			sql.append("where cv.card_id = {0} \n");
			sql.append("order by cv.version_id, cv.version_date ");
			
			getJdbcTemplate().query(
					MessageFormat.format(sql.toString(),
							String.valueOf(action.getCardId().getId())),
					new RowCallbackHandler() {
						public void processRow(ResultSet rs) throws SQLException {
							Long l = rs.getLong(1);
							if(rs.wasNull())
									return;
							CardVersionHist cvh = getCardVersionHist(result, l);
							Date d;
							String str;
							if(cvh == null) {
								cvh = new CardVersionHist();
								cvh.setId(action.getCardId());
								cvh.setVersionId(l);
								d = rs.getTimestamp(2);
								cvh.setVersionDate(d != null ? d : null);
								l = rs.getLong(3);
								cvh.setParentId(!rs.wasNull() ? new ObjectId(Card.class, l) : null);
								l = rs.getLong(4);
								cvh.setState(!rs.wasNull() ? new ObjectId(CardState.class, l) : null);
								str = rs.getString(5);
								cvh.setFileName(str != null ? str : null);
								str = rs.getString(6);
								cvh.setUrl(str != null ? str : null);
								cvh.setActionLogId(rs.getLong(14));
								result.add(cvh);
							}
							
							AttributeValueHist av = new AttributeValueHist();
							str = rs.getString(7);
							av.setAttributeCode(str != null ? new ObjectId(Attribute.class, str) : null);
							l = rs.getLong(8);
							av.setNumberValue(!rs.wasNull() ? l : null);
							str = rs.getString(9);
							av.setStringValue(str != null ? str : null);
							d = rs.getTimestamp(10);
							av.setDateValue(d != null ? d : null);
							l = rs.getLong(11);
							av.setValueId(!rs.wasNull() ? l : null);
							str = rs.getString(12);
							av.setAnotherValue(str != null ? str : null);
							str = rs.getString(13);
							av.setLongBinaryValue(str != null ? str : null);
							
							cvh.getAvh().add(av);
						}
					}
				);
			
		} else {
			logger.warn("Action is not usable");
		}
		
		return result;
	}
	
	private CardVersionHist getCardVersionHist(List<CardVersionHist> list, Long versionId) {
		for(Iterator<CardVersionHist> it = list.iterator(); it.hasNext();) {
			CardVersionHist obj = it.next();
			if(obj.getVersionId().equals(versionId)) {
				return obj;
			}
		}
		return null;
	}

}
