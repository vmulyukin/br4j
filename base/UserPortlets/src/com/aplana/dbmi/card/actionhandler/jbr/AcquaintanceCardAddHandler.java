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
package com.aplana.dbmi.card.actionhandler.jbr;

import com.aplana.dbmi.card.actionhandler.AddLinkedCardActionHandler;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ServiceException;

public class AcquaintanceCardAddHandler extends AddLinkedCardActionHandler
{
	private static final ObjectId ATTR_SRC_TERM =
		ObjectId.predefined(DateAttribute.class, "jbr.inform.date");
	private static final ObjectId ATTR_DEST_TERM =
		ObjectId.predefined(DateAttribute.class, "jbr.information.term");
	
	public AcquaintanceCardAddHandler() {
		setParameter(TEMPLATE_ID_PARAM, "jbr.inform");
	}

	protected Card createCard() throws DataException, ServiceException {
		Card card = super.createCard();
		DateAttribute srcAttr = (DateAttribute) getCardPortletSessionBean()
				.getActiveCard().getAttributeById(ATTR_SRC_TERM);
		DateAttribute dstAttr = (DateAttribute) card.getAttributeById(ATTR_DEST_TERM);
		dstAttr.setValue(srcAttr.getValue());
		return card;
	}

}
