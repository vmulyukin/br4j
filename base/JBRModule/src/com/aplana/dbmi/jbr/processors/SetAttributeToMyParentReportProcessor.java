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

import java.util.LinkedList;
import java.util.List;

import com.aplana.dbmi.action.ParentReportAction;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ObjectQueryBase;

public class SetAttributeToMyParentReportProcessor extends SetAttributeValueProcessor{

	/**
	 * 
	 */
	private static final long serialVersionUID = 18476675L;
	
	private ObjectId attributeId = null;
	private String value = null;

	@Override
	public Object process() throws DataException {
		List<ObjectId> cards = new LinkedList<ObjectId>();
		cards.add(getCardId());
		ParentReportAction parentReportAction = new ParentReportAction();
		parentReportAction.setChildResolutionCardIds(cards);
		List<Long> reportIds = (List<Long>) execAction(parentReportAction);
		if(reportIds.size()==1){
			for(Long reportid: reportIds){
				Card card = fetchCard( new ObjectId(Card.class, reportid));
				Attribute destAttr = card.getAttributeById(attributeId);
				if(setValue(card, destAttr, value)){
					saveAttribute(card, destAttr);
				}
			}
		}
		return null;
	}
	
	@Override
	public void setParameter(String name, String value) {
		if (DEST_ATTRIBUTE_ID_PARAM.equalsIgnoreCase(name)) {
			this.attributeId = IdUtils.smartMakeAttrId(value, StringAttribute.class);
		} else if (DEST_VALUE_PARAM.equalsIgnoreCase(name)) {
			this.value = value;
		} else {
			super.setParameter( name, value);
		}
	}

}
