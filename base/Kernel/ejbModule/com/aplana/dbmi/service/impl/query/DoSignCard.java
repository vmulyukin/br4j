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

import java.sql.Types;
import java.util.Date;

import com.aplana.dbmi.action.SignCard;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.HtmlAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;

public class DoSignCard extends ActionQueryBase implements WriteQuery {
	private static final long serialVersionUID = 1L;
	public static final String EVENT_ID_SIGN = "SIGN_CARD";
	public static final int THOUSAND = 1000;
	public String getEvent() {
		return EVENT_ID_SIGN;
	}

	public Object processQuery() throws DataException {		
		SignCard action = (SignCard) getAction();
		Card card = action.getCard();
		
		Date currentDate = new Date();
		Long dateSeconds = currentDate.getTime()/THOUSAND;
		((DateAttribute) card.getAttributeById(Attribute.ID_CHANGE_DATE)).setValue(currentDate);
		ObjectId signatureAttributeId = ObjectId.predefined(HtmlAttribute.class, "jbr.uzdo.signature");
		HtmlAttribute signatureAttribute = (HtmlAttribute) card.getAttributeById(signatureAttributeId);	
		
		getJdbcTemplate().update("delete from attribute_value where card_id = ? and attribute_code = ?", 
				new Object []{card.getId().getId(), Attribute.ID_CHANGE_DATE.getId()}, new int[] { Types.NUMERIC, Types.VARCHAR }
		);
		getJdbcTemplate().update("delete from attribute_value where card_id = ? and attribute_code = ?", 
				new Object []{card.getId().getId(), signatureAttributeId.getId()}, new int[] { Types.NUMERIC, Types.VARCHAR }
		);
		//EChirkov
		//������������ ���� � ���� UTC ���������� � postgres. 
		//dateSeconds - ����� � �������� (� UTC)
		//to_timestamp(?) - ������������ ������� � TIMESTAMP WITH TIMEZONE
		//at time zone 'UTC' - - �������� TIMESTAMP WITH TIMEZONE � TIMESTAMP WITHOUT TIMEZONE (� ���� UTC)
		getJdbcTemplate().update("insert into attribute_value (date_value, card_id, attribute_code) values(to_timestamp(?) at time zone 'UTC', ?, ?);",
					new Object[] { dateSeconds,  card.getId().getId(), Attribute.ID_CHANGE_DATE.getId()},
					new int[] { Types.NUMERIC, Types.NUMERIC, Types.VARCHAR }
				);
		getJdbcTemplate().update("insert into attribute_value (long_binary_value, card_id, attribute_code) values(?, ?, ?);",
					new Object[] { signatureAttribute.getValue().getBytes(),  card.getId().getId(), signatureAttributeId.getId()},
					new int[] { Types.BINARY, Types.NUMERIC, Types.VARCHAR }
			);
		return card;
	}
}