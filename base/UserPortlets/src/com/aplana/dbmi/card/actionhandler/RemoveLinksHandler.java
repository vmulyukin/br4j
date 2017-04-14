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
package com.aplana.dbmi.card.actionhandler;

import java.util.Collection;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;

import com.aplana.dbmi.card.Parametrized;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.service.DataException;

public class RemoveLinksHandler extends CardPortletAttributeEditorActionHandler
		implements Parametrized
{
	//public static final String PARAM_TARGET_STATE = "moveTo";

	//private ObjectId targetStateId;

	public static final String PARAM_TEMPLATE = "templates";
	public static final String PARAM_STATES = "states";

	private Collection<ObjectId> templates;
	private Collection<ObjectId> states;

	@Override
	protected void process(Attribute attr, List<ObjectId> cardIds, ActionRequest request,
			ActionResponse response) throws DataException {
		if (cardIds == null) return;
		final CardLinkAttribute links = (CardLinkAttribute) attr;
		for (ObjectId id: cardIds ) {
			final boolean removed = links.removeLinkedId(id);
			if (!removed)
				logger.warn("Card " + id.getId() + " is not linked to this card");
		}
	}

	@Override
	public boolean isApplicableForUser() {
		Card card = getCardPortletSessionBean().getActiveCard();
		boolean isTemplateApplicable = templates == null || templates.contains(card.getTemplate());
		boolean isStateApplicable = states == null || states.contains(card.getState());
		return isTemplateApplicable && isStateApplicable;
	}

	public void setParameter(String name, String value) {
		if (PARAM_TEMPLATE.equals(name)) {
			this.templates = IdUtils.stringToAttrIds(value, Template.class, true, false);
		} else if (PARAM_STATES.equals(name)) {
			this.states = IdUtils.stringToAttrIds(value, CardState.class, true, false);
		} else {
			throw new IllegalArgumentException("Unknown parameter: " + name);
		}
	}


}
