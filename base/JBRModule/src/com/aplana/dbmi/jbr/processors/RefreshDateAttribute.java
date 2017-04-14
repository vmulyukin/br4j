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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.aplana.dbmi.jbr.util.AttributeSelector;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;

/**
 * ��������� �������� ���� ����
 *
 * ������ ������� ��������� dateOffset:
 *
 * ���� ���������� ���������� ��� ������� �� ������� ����
 * ����� - ���������� ������
 * ����� (h,d,m,y) - ����������� ����� �������
 * ������:
 * +7d ��������� 7 ����
 * -1h ������� ���� ���
 * @author larin
 *
 * (2012/10/05, YNikitin) ����� �������� updateIfLessCurDate - ��������� ���� ������ � ��� ������, ���� ��� ������ �������
 */
@SuppressWarnings("serial")
public class RefreshDateAttribute extends ProcessCard
{
	protected ObjectId dateAttributeId = null;   	// ������� � ������� ��������, ���� ������ ��������. ����������
	protected boolean  updateIfNullOnly = false; 	// ���� true, �� ��������� ����, ������ ���� �������� �����. �� ����������
	protected String   dateOffset = null;        	// �������� ����. �� ����������
	protected boolean  updateIfLessCurDate = false; // ��������� ������ � ��� ������, ���� ������ ������� ����, �� ����������

	protected ObjectId  conditionParentDateAttribute = null; 	// DateAttribute � ��������, ������� ���� ���������� � DateAttribute-�� � ������� �������� (�������� ������� �� ������ ProcessCard.PARAM_MAIN_DOC_BACKLINK_ATTR_ID ��������)
	protected ObjectId  conditionCurrentDateAttribute = null; 	// DateAttribute � ��������, ������� ���� ���������� � DateAttribute-�� � ������� ��������
	protected String  	condition = null; 						// �������, ������� ���� ���������

	@Override
	public Object process() throws DataException {

		final ObjectId cardId = getCardId();

		if (dateAttributeId == null) {
			logger.warn( "partameter dateAttrId is not set -> exiting");
			return null;
		}

		// �������� ���� � ������ ��������
		Date dt = getDateToRefresh();

		/* ���������� � �������� ��������... */
		final Card card = this.getCard();
		if (card == null) { logger.debug( "Card is null -> exiting "); return null; }

		Card c_parent = null;
		// ���� ������ ������������ �������, �� ��������� �� � ��������� ��
		if (isMainDocConditionSet() && (c_parent = validateAndCalculateParent(card)) == null) {
			return null;
		}
		if (!checkDateCondition(c_parent, card)){	// ���� �������������� ������� �� �����������, �� ������� � ��� � �������
			logger.info( "Condition: <parentCard."+conditionParentDateAttribute.toString()+condition+"currentCard+"+conditionCurrentDateAttribute+"> are not satisfied -> exiting" );
			return null;
		}
		DateAttribute dtAttr = (DateAttribute) card.getAttributeById(dateAttributeId);
		if (dtAttr == null) {
			logger.warn( "Active card with id="+ cardId +" does not contain date attribute '"+ dateAttributeId + "', only DB update will be performed");
			dtAttr = new DateAttribute();
			dtAttr.setId(dateAttributeId);
		}

		if (( !updateIfNullOnly || dtAttr.isEmpty())&&(!updateIfLessCurDate||dtAttr.isEmpty()||dtAttr.getValue().before(new Date()))) {
			logger.debug( "Active card with id="+cardId +": setting value of attribute \'"+ dateAttributeId+ "\' to " + dt.toString());
			dtAttr.setValue(dt);
		} else {
			logger.warn( "Active card with id="+cardId +": value of attribute \'"+ dateAttributeId
					+ "\' WAS NOT CHANGED due to (parameter 'updateIfNullOnly' is true and value is NOT empty) or (parameter 'updateIfLessCurDate' is true and value less than the current date)");
			return card;
		}

		// ���������� ��
		doOverwriteCardAttributes(cardId, getSystemUser(), dtAttr);
		logger.debug("Active card with id="+cardId +": value of attribute \'"+ dateAttributeId + "\' written into DB.");

		return card;
	}

	/**
	 * ��������� ���� ������� ������� � �������
	 * @return �������������� ����
	 */
	private Date getDateToRefresh()
	{
		Date result = addDateOffset( new Date() );
		return result;
	}


	/**
	 * ��������� ���� ������� ������� � �������. ������ �������� dateOffset ����������.
	 * @param startDate - ��������� ����/�����, �� ������� ��������� �������� (���� ������ null - ������� ����/�����)
	 * @return �������������� ����
	 */
	protected Date addDateOffset( Date startDate )
	{
		Date result = (startDate != null) ? startDate : new Date();
		// ��������� �������� ���� �� ������, ��������� � ��������� dateOffset
		if( dateOffset != null )
		{
			Pattern pattern = Pattern.compile( "([\\+\\-])(\\d+)([h,d,m,y])" );
			Matcher matcher = pattern.matcher( dateOffset );
			if( matcher.find() )
			{
				String offsetDirection = matcher.group( 1 );
				int offsetSize = Integer.valueOf( matcher.group( 2 ) );
				String offsetType = matcher.group( 3 );

				// ��������� ����������� ��������
				if( offsetDirection.equals("-") ) {
					offsetSize = offsetSize * (-1);
				}

				int amount = Calendar.HOUR_OF_DAY;
				if( offsetType.equals("d") ) {
					amount = Calendar.DAY_OF_MONTH;
				} else if( offsetType.equals("m") ) {
					amount = Calendar.MONTH;
				} else if( offsetType.equals("y") ) {
					amount = Calendar.YEAR;
				}

				Calendar calendar = Calendar.getInstance();
				calendar.setTime( result );
				calendar.add( amount, offsetSize );
				result = calendar.getTime();
			}
		}
		return result;
	}


	@Override
	public void setParameter(String name, String value) {
		if (name.equalsIgnoreCase("dateAttributeId")) {
			dateAttributeId = ObjectIdUtils.getObjectId(DateAttribute.class, value, false);
		} else if (name.equalsIgnoreCase("updateIfNullOnly")) {
			updateIfNullOnly =  value.equals("true");
		} else if (name.equalsIgnoreCase("dateOffset")) {
			dateOffset =  value;
		} else if (name.equalsIgnoreCase("updateIfLessCurDate")) {
			updateIfLessCurDate =  value.equals("true");
		} else if (name.equalsIgnoreCase("conditionParentDateAttribute")){
			conditionParentDateAttribute = ObjectIdUtils.getObjectId(DateAttribute.class, value, false);
		} else if (name.equalsIgnoreCase("conditionCurrentDateAttribute")){
			conditionCurrentDateAttribute = ObjectIdUtils.getObjectId(DateAttribute.class, value, false);
		} else if (name.equalsIgnoreCase("condition")){
			condition = value.trim();
		} else {
			super.setParameter( name, value );
		}
	}

	/**
	 * �������� ���� ��� - �� ������������ � ������� �������� (������ �������, ����������)
	 * @param parentCard - ������������ ��������
	 * @param currCard - ������� ��������
	 * @return
	 */
	protected boolean checkDateCondition(Card parentCard, Card currCard){
		if (condition==null||condition.length()==0||conditionParentDateAttribute==null||conditionCurrentDateAttribute==null)
			return true;
		try {
			DateAttribute currAttr = (DateAttribute)currCard.getAttributeById(conditionCurrentDateAttribute);
			if (currAttr==null||currAttr.getValue()==null){
				logger.info( conditionCurrentDateAttribute+" did not existing in currentCard " +currCard + " or empty");
				return true;
			}
			final DateFormat suffix_date = new SimpleDateFormat("yyyy-MM-dd");
			String currAttrValue = suffix_date.format(currAttr.getValue());
			String mixedValue = "date:"+conditionParentDateAttribute.getId().toString()+condition+"date:"+currAttrValue;
			AttributeSelector selector = AttributeSelector.createSelector(mixedValue);

			return selector.satisfies(parentCard);
		} catch (DataException ex) {
			ex.printStackTrace();
			return false;
		}
	}
}