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
import java.sql.Types;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.action.GetDeepChildren;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;

public class DoGetDeepChildren extends ActionQueryBase {
	private int maxDepth = 1;
	private int currentDepth = 0;
	private String sql;
	private String sqlForBackLink;
	private String sqlForCardLink;
	private Set globalSet;

	public Object processQuery() throws DataException {
		GetDeepChildren action = (GetDeepChildren)getAction();
		maxDepth = action.getDepth();
		
		sqlForBackLink = "select distinct \n" +
			"CASE \n" +
			"	WHEN upLink.option_value is null \n" +
			"		THEN avLinkFrom.card_id \n" +
			"	WHEN upLink.option_value is not null \n" +
			"		THEN functionbacklink(c.card_id, upLink.option_value, link.option_value) \n" +
			"	ELSE NULL \n" +
			"      END \n" +
			"FROM card c \n" +
			"LEFT OUTER JOIN attribute_option link on link.attribute_code = ? \n" +
			"	and link.option_code='LINK' \n" +
			"LEFT OUTER JOIN attribute_option upLink on upLink.attribute_code = ? \n" +
			"	and upLink.option_code='UPLINK' \n" +
			"JOIN attribute_value avLinkFrom \n" +
			"	on avLinkFrom.number_value=c.card_id \n" +
			"	and avLinkFrom.attribute_code=( \n" +
			"			select o.option_value \n" +
			"			from attribute_option o \n" +
			"			where o.attribute_code=? \n" +
			"			and o.option_code='LINK' \n" +
			"			) \n" +
			"WHERE c.card_id=?";
		sqlForCardLink = "SELECT distinct number_value \n" +
			"FROM attribute_value \n" +
			"WHERE attribute_code=? AND card_id=?";
		globalSet = new HashSet();
		globalSet.add(action.getRoots());
		for (Iterator i = action.getRoots().iterator(); i.hasNext();) {
			ObjectId id = (ObjectId) i.next();
			getLL(id, action.getChildTypeId());
		}
		globalSet.remove(action.getRoots());
		if (action.getSecondChildTypeId() != null) {
			ArrayList newRoots = new ArrayList(globalSet);
			Iterator iter = newRoots.iterator();
			while (iter.hasNext()) {
				ObjectId id = (ObjectId) iter.next();
				getLL(id, action.getSecondChildTypeId());
			}
		}
		return globalSet;
	}
	
	private void getLL(ObjectId card, ObjectId typeId){
		if (currentDepth == maxDepth)
			return;
		Object[] params = null;
		if(CardLinkAttribute.class.isAssignableFrom(typeId.getType())) {
			sql = sqlForCardLink;
			params = new Object[]{typeId.getId(), card.getId()};
		} else if(BackLinkAttribute.class.isAssignableFrom(typeId.getType())) {
			sql = sqlForBackLink;
			params = new Object[]{typeId.getId(), typeId.getId(), typeId.getId(), card.getId()};
		} else {
			throw new IllegalArgumentException("Child as " + typeId.getType() + " is not unacceptable.");
		}
		currentDepth++;
		try {
			Set cSet = new HashSet();
			List cList = getJdbcTemplate().query(sql,
					params,
					new int[] { Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.NUMERIC },
					new RowMapper(){
						public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
							ObjectId id = new ObjectId(Card.class, rs.getInt(1));
							return id;
						}
			});
			if (cList != null)
				cSet.addAll(cList);
			cSet.removeAll(globalSet);
			globalSet.addAll(cSet);
			
			for (Iterator i = cSet.iterator(); i.hasNext();) {
				ObjectId id = (ObjectId) i.next();
				getLL(id, typeId);
			}
		} catch (DataAccessException e) {
			e.printStackTrace();
		}
		currentDepth--;
	}
}
