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

import java.sql.Types;
import java.util.Collection;
import java.util.Date;

import org.springframework.jdbc.core.JdbcTemplate;

import com.aplana.dbmi.action.Action;
import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.jbr.util.CheckingAttributes;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.IntegerAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.TextAttribute;
import com.aplana.dbmi.model.TreeAttribute;
import com.aplana.dbmi.model.util.AttrUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.Database;
import com.aplana.dbmi.service.impl.DatabaseClient;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.Parametrized;
import com.aplana.dbmi.service.impl.ProcessorBase;
import com.aplana.dbmi.service.impl.SaveQueryBase;
import com.aplana.dbmi.service.impl.UserData;

/**
 * ��������� 
 * @author larin
 *
 */
//(Smirnov A. : 23.7.12)
public class ClearCardAttributePostProcessorEx extends ClearCardAttributePostProcessor {

	private static final String PARAM_CONDITION_ATTR = "condition";
	protected CheckingAttributes conditionAttrs;
	
	@Override
	public Object process() throws DataException {
		if (attrId == null){
			throw new DataException("Parameter attrId most be set");
		}

		Action action = getAction();
		if (action instanceof ChangeState){
			ChangeState changeStateAction = (ChangeState)action;
			Card card = changeStateAction.getCard();
			
			ObjectQueryBase query = getQueryFactory().getFetchQuery(Card.class);
			query.setId(card.getId());
			card = (Card)getDatabase().executeQuery(getSystemUser(), query);
			
			if(!((conditionAttrs != null && conditionAttrs.check(card, getUser()))||conditionAttrs==null)){
				return null;
			}
			
			if (templateId != null && !templateId.equals(card.getTemplate())) {
				return null;
			}

			Attribute attr = card.getAttributeById(attrId);
			
			// (Smirnov A. : 23.7.12)
			if (attr != null){
				attr.clear();
				doOverwriteCardAttributes(card.getId(), attr);
			}
		}
		return null;
	}

	public void setParameter(String name, String value) {
		if (PARAM_CONDITION_ATTR.equalsIgnoreCase(name)) {
			try {
				if (conditionAttrs == null) 
					conditionAttrs = new CheckingAttributes(getQueryFactory(), getDatabase(), getSystemUser());
				conditionAttrs.addCondition(value);
			} catch (Exception e) {
				logger.error("������ ��� �������� ���������", e);
			}
		} else
			super.setParameter(name, value);		
	}

	public void setValue (Attribute attr, Object value) {
		if (CardLinkAttribute.class.equals(attr.getId().getType()))
			((CardLinkAttribute) attr).setIdsLinked((Collection) value);
		else if (StringAttribute.class.equals(attr.getId().getType()))
			((StringAttribute) attr).setValue((String) value);
		else if (TextAttribute.class.equals(attr.getId().getType()))
			((TextAttribute) attr).setValue((String) value);
		else if (IntegerAttribute.class.equals(attr.getId().getType()))
			((IntegerAttribute) attr).setValue(((Number) value).intValue());
		else if (DateAttribute.class.equals(attr.getId().getType()))
			((DateAttribute) attr).setValue((Date) value);
		else if (ListAttribute.class.equals(attr.getId().getType()))
			((ListAttribute) attr).setValue((ReferenceValue) value);
		else if (TreeAttribute.class.equals(attr.getId().getType()))
			((TreeAttribute) attr).setValues((Collection) value);
		else if (PersonAttribute.class.equals(attr.getId().getType()))
			((PersonAttribute) attr).setValues((Collection) value);
		else
			throw new IllegalArgumentException("Unknown attribute type: " + attr.getId().getType());
	}

	/**
	 * ���������� ��������
	 * @param card
	 * @throws DataException
	 */
	private void store(Card card) throws DataException {		
		final SaveQueryBase query = getQueryFactory().getSaveQuery(card);
		query.setObject(card);
		getDatabase().executeQuery(getSystemUser(), query);
	}
}