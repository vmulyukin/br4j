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

import java.util.Iterator;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

import com.aplana.dbmi.card.Parametrized;
import com.aplana.dbmi.card.actionhandler.CardPortletAttributeEditorActionHandler;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.util.AttrUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ServiceException;

public class DistributionListRemoveLinksHandler extends CardPortletAttributeEditorActionHandler implements Parametrized
{
	//public static final String PARAM_TARGET_STATE = "moveTo";
	
	//private ObjectId targetStateId;
	
	public static final String PARAM_MUST_BE_EMPTY_ID = "mustBeEmptyId";
	
	private ObjectId mustBeEmptyId;
	
	protected void process(Attribute attr, List cardIds, ActionRequest request,
			ActionResponse response) throws DataException {
		CardLinkAttribute links = (CardLinkAttribute) attr;
		for (Iterator itr = cardIds.iterator(); itr.hasNext(); ) {
			ObjectId id = (ObjectId) itr.next();
			try {
				Card distr = (Card)serviceBean.getById(id);
				Attribute distrAttr = distr.getAttributeById(mustBeEmptyId);
				if (!distrAttr.isEmpty()) {
					logger.warn("Can't remove sent element " + id.getId());
				} else {
					boolean removed = links.removeLinkedId(id);
					if (!removed)
						logger.warn("Card " + id.getId() + " is not linked to this card");
				}
			} catch (ServiceException e) {
				logger.error(e);
			}
		}
	}

	public void setParameter(String name, String value) {
		/*if (PARAM_TARGET_STATE.equalsIgnoreCase(name)) {
			targetStateId = ObjectIdUtils.getObjectId(CardState.class, value, true);
		} else*/if (PARAM_MUST_BE_EMPTY_ID.equals(name)) {
			mustBeEmptyId = AttrUtils.getAttributeId(value);
		} else 
			throw new IllegalArgumentException("Unknown parameter: " + name);
	}
}
