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

import java.util.Collections;
import java.util.UUID;

import com.aplana.dbmi.action.GetCardUUID;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.OverwriteCardAttributes;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.TextAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.ObjectQueryBase;

public class DoGetCardUUID extends ActionQueryBase implements WriteQuery {
	private static final long serialVersionUID = 1L;

	@Override
    public Object processQuery() throws DataException {
    	GetCardUUID action = (GetCardUUID)this.getAction();
    	ObjectId cardId = action.getCardId();
    	if (cardId==null)
    		throw new DataException("Input cardId is null.");
    	
    	try {
//	    	DataServiceBean serviceBean = getDataServiceBean();  
    		final ObjectQueryBase subQuery = getQueryFactory().getFetchQuery(Card.class);
    		subQuery.setId(cardId);
    		final Card card = (Card) getDatabase().executeQuery(getUser(), subQuery);
	    	Attribute attr = card.getAttributeById(GetCardUUID.JBR_UUID);
	    	if (attr == null||attr.getStringValue()==null||attr.getStringValue().equals("")){
	    		// ���������� ������������� ������� �������������
	    		LockObject lock = new LockObject(card.getId());
	    		ActionQueryBase q = getQueryFactory().getActionQuery(lock);
	    		q.setAction(lock);
	    		getDatabase().executeQuery(getUser(), q);
	    		try {
		    		if (attr == null){
						attr = new TextAttribute();
						attr.setId(GetCardUUID.JBR_UUID);
						card.getAttributes().add(attr);
		    		}
		    		// ������� �������� ��������, ����� ���� �������� ��������
		    		String uuid = UUID.randomUUID().toString();
		    		((TextAttribute)attr).setValue(uuid);
		    		
		    		final OverwriteCardAttributes act = new OverwriteCardAttributes();
		    		act.setCardId(cardId);
		    		act.setAttributes(Collections.singletonList(attr));
		    		act.setInsertOnly(true);
	
		    		final ActionQueryBase query = getQueryFactory().getActionQuery(act);
		    		query.setAction(act);
		    		getDatabase().executeQuery(getUser(), query);
	    		} finally {
	    			UnlockObject unlock = new UnlockObject(card.getId());
		    		q = getQueryFactory().getActionQuery(unlock);
		    		q.setAction(unlock);
		    		getDatabase().executeQuery(getUser(), q);
	    		}
	    		return attr.getStringValue();
	    	} else {
	    		return attr.getStringValue();
	    	}
    	} catch (DataException e) {
    		throw e;
    	} catch (Exception e){
    		throw new DataException(e);
    	}
    }    
}
