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
package com.aplana.dbmi.module.notif.selector;

import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.DatedTypedCardLinkAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.module.notif.DataServiceClient;
import com.aplana.dbmi.service.impl.Selector;

public class MainConsiderationSelector extends DataServiceClient implements
		Selector {
	
	private ObjectId newConsidirationStateAttrId = ObjectId.predefined(DatedTypedCardLinkAttribute.class, "jbr.request.new");
	private ObjectId requestTypeAttrId = ObjectId.predefined(ListAttribute.class, "jbr.request.type");
	private ObjectId changeMainConsId = ObjectId.predefined(ReferenceValue.class, "jbr.considerator.change.respon");
	private ObjectId yesId = ObjectId.predefined(ReferenceValue.class, "jbr.commission.control.yes");
	
	public boolean satisfies(Object object) {
		Card card = ((ChangeState) object).getCard();
		ListAttribute requestType = card.getAttributeById(requestTypeAttrId);
		if(changeMainConsId.equals(requestType.getValue().getId())){
			DatedTypedCardLinkAttribute newConsidirationState = card.getAttributeById(newConsidirationStateAttrId);
			for(ObjectId id : newConsidirationState.getIdsLinked()){
				if(yesId.equals(newConsidirationState.getCardType(id))){
					return true;
				}
			}
		}
		return false;
	}
}
