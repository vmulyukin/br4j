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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.jbr.util.CardUtils;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;


/**
 * Sets specified {@link DateAttribute} in the current {@link Card} = to {@link DateAttribute} from parent {@link Card}
 * + some specified dateOffset, if any
 *
 * @author aminnekhanov
 *
 */
@SuppressWarnings("serial")
public class RefreshDateFromAttribute extends RefreshDateAttribute
{
	protected ObjectId backlinkAttributeId    = null; // ������� �� �������� ������ ��������. ����������
	protected ObjectId parent_dateAttributeId = null; // ������� ��������, ��� �������� ����� ��� ���������. ����������

	@Override
	public Object process() throws DataException
	{
		// checks
		if( dateAttributeId == null ) {
			logger.warn( "partameter dateAttributeId is not set -> exiting" );
			return null;
		}
		if( backlinkAttributeId == null ) {
			logger.warn( "partameter backlinkAttributeId is not set -> exiting" );
			return null;
		}
		if( parent_dateAttributeId == null ) {
			logger.warn( "partameter parent_dateAttributeId is not set -> exiting" );
			return null;
		}

		// get currently processing card id
		ObjectId cardId = null;
		Card c_this = this.getCard();
		cardId = c_this.getId();
		
		Card c_parent = null;

		if ((c_parent = validateAndCalculateParent(c_this)) == null) {
			return null;
		}

		if (!checkDateCondition(c_parent, c_this)){	// ���� �������������� ������� �� �����������, �� ������� � ��� � �������
			logger.info( "Condition: <parentCard."+conditionParentDateAttribute.toString()+condition+"currentCard+"+conditionCurrentDateAttribute+"> are not satisfied -> exiting" );
			return null;
		}
		// get source date attribute from parent
		DateAttribute srcDateAttr = null;
		if (c_parent != null) {
			srcDateAttr = (DateAttribute) c_parent.getAttributeById(parent_dateAttributeId);
		}

		if(srcDateAttr != null) {
			Date srcDate = srcDateAttr.getValue(); // get date value from parent card
			Date newDate = addDateOffset( srcDate ); // calc new value
			DateAttribute c_attr = (DateAttribute)c_this.getAttributeById( dateAttributeId );
			if(c_attr == null) {
				c_attr = new DateAttribute();
				c_attr.setId( dateAttributeId );
			}
			// value is not empty AND update if null only?
			if( !c_attr.isEmpty() && updateIfNullOnly ) {
				logger.warn( "Active card " + String.valueOf(cardId) + ": value of attribute \'"+ dateAttributeId +
					"\' WAS NOT CHANGED due to parameter 'updateIfNullOnly' is true and value is NOT empty" );
			} else {// either new attribute  OR  existing value which we can change
				c_attr.setValue( newDate ); // set new value
				doOverwriteCardAttributes( cardId, getSystemUser(), c_attr ); // update DB
			}
		} else {
			logger.warn("Parent card " + c_parent.getId() + " doesn't have date attribute " +
				parent_dateAttributeId.toString() + ", skipping" );
		}

		return c_this;
	}


	@Override
	public void setParameter( String name, String value )
	{
		if( name.equals("backlinkAttributeId") ) {
			backlinkAttributeId = ObjectIdUtils.getObjectId( BackLinkAttribute.class, value, false );
		} else if( name.equals("parent_dateAttributeId") ) {
			parent_dateAttributeId = ObjectIdUtils.getObjectId( DateAttribute.class, value, false );
		} else {
			super.setParameter( name, value );
		}
	}


	/**
	 * ������ ������� ������ �������� ��� ������� ��� ������ ListProject
	 * @return ������ ��������
	 */
	List<SearchResult.Column> createSearchColumnsList()
	{
		final List<SearchResult.Column> columns = new ArrayList<SearchResult.Column>();
		columns.add( CardUtils.createColumn( IdUtils.smartMakeAttrId( "NAME", StringAttribute.class ) ) );
		columns.add( CardUtils.createColumn( Card.ATTR_TEMPLATE ) );
		columns.add( CardUtils.createColumn( parent_dateAttributeId ) ); // ������ ��� ������� �� ��������
		return columns;
	}
}
