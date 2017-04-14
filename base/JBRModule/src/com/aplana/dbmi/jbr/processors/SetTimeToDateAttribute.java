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
package com.aplana.dbmi.jbr.processors;

import java.util.Calendar;
import java.util.Date;

import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;

/**
 * (2013/06/14, YNikitin)
 * ������ ��������� ������� � ������� ���� ����
 *
 * ��������� ��� ���������: hours, minutes, seconds
 * ������:
 * hours = 23
 * minutes = 59
 * seconds = 59
 * @author ynikitin
 */
@SuppressWarnings("serial")
public class SetTimeToDateAttribute extends ProcessCard {
	protected ObjectId dateAttributeId = null;	// ������� � ������� ��������, ���� ������ ��������. ����������
	protected String hours = null;				// ���� ��� ���������
	protected String minutes = null;			// ������ ��� ���������
	protected String seconds = null;			// ������� ��� ���������

	@Override
	public Object process() throws DataException {

		final ObjectId cardId = getCardId();

		if (dateAttributeId == null) {
			logger.warn( "parameter dateAttrId is not set -> exiting");
			return null;
		}

		/* ���������� � �������� ��������... */
		final Card card = this.getCard();
		
		if (card == null) { logger.debug( "Card is null -> exiting "); return null; }

		DateAttribute dtAttr = (DateAttribute) card.getAttributeById(dateAttributeId);
		if (dtAttr == null) {
			logger.warn( "Active card with id="+ cardId +" does not contain date attribute '"+ dateAttributeId + "'");
			return null;
		}

		Date dt = dtAttr.getValue();
		
		if(dt == null) {
			logger.warn( "Date attribute '"+ dateAttributeId + "' in the card with id:"+ cardId +" is empty.");
			return null;
		}

		dt = updateTimeInDate(dt, hours, minutes, seconds);
		
		dtAttr.setValue(dt);

		// ���������� ��
		doOverwriteCardAttributes(cardId, getSystemUser(), dtAttr);
		logger.debug("Active card with id="+cardId +": value of attribute \'"+ dateAttributeId + "\' written into DB.");

		return getResult();
	}

	@Override
	public void setParameter(String name, String value) {
		if (name.equals("dateAttributeId")) {
			dateAttributeId = ObjectIdUtils.getObjectId(DateAttribute.class, value, false);
		} else if (name.equals("hours")) {
			hours = value;
		} else if (name.equals("minutes")) {
			minutes = value;
		} else if (name.equals("seconds")) {
			seconds = value;
		} else {
			super.setParameter( name, value );
		}
	}
	
	private Date updateTimeInDate(Date dt, String hours, String minutes, String seconds){
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(dt.getTime());
		int hrs;
		int mnts;
		int scnds;
		
		try{
			hrs = Integer.parseInt(hours);
		} catch (Exception e){
			hrs = calendar.get(Calendar.HOUR_OF_DAY);
		}

		try{
			mnts = Integer.parseInt(minutes);
		} catch (Exception e){
			mnts = calendar.get(Calendar.MINUTE);
		}

		try{
			scnds = Integer.parseInt(seconds);
		} catch (Exception e){
			scnds = calendar.get(Calendar.SECOND);
		}
		calendar.set(Calendar.HOUR_OF_DAY, hrs);
		calendar.set(Calendar.MINUTE, mnts);
		calendar.set(Calendar.SECOND, scnds);
/*		int day = calendar.get(Calendar.DAY_OF_MONTH);
		int month = calendar.get(Calendar.MONTH);
		int year = calendar.get(Calendar.YEAR);*/
		
		Date date = new java.sql.Date(calendar.getTimeInMillis());
		return date;
	}
}