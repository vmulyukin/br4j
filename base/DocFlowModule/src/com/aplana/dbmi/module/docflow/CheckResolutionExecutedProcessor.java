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
package com.aplana.dbmi.module.docflow;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.util.CollectionUtils;

import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.jbr.processors.ProcessCard;
import com.aplana.dbmi.jbr.util.CardUtils;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.LinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ObjectQueryBase;

public class CheckResolutionExecutedProcessor extends ProcessCard {
	private static final long serialVersionUID = 1L;

	public static final String PARAM_RESOLUTION_LINK_ATTR = "resolutionLinkAttr";
	public static final ObjectId RESOLUTION_TEMPLATE_ID = ObjectId.predefined(Template.class, "jbr.resolution");
	public static final ObjectId INDEPENDENT_RESOLUTION_TEMPLATE_ID = ObjectId.predefined(Template.class, "jbr.independent.resolution");
	public static final ObjectId WFM_FOR_RESOLUTION = ObjectId.predefined(WorkflowMove.class, "jbr.commission.finished");
	public static final ObjectId WFM_FOR_INDEPENDENT_RESOLUTION = ObjectId.predefined(WorkflowMove.class, "jbr.independent.resolution.execution.done");

	private static final ObjectId reportsAttrId = ObjectId.predefined(BackLinkAttribute.class, "jbr.reports");
	private static final ObjectId reportRatifiedStateId = ObjectId.predefined(CardState.class, "ratified");
	private static ObjectId executingDoneWfmId = null;

	private ObjectId resolutionLinkAttrId = null;

	@Override
	public Object process() throws DataException {
		
		Card card = getCard();
		final List<Card> resolutionCards = loadAllLinks(card.getId(), (CardLinkAttribute) card.getAttributeById(resolutionLinkAttrId), getUser());
		Card resolutionCard = null;
		if(!CollectionUtils.isEmpty(resolutionCards)) {
			resolutionCard = resolutionCards.get(0);
		} else {
			throw new IllegalArgumentException("attr resolutionLinkAttr do not contain card");
		}

		final LinkAttribute reportsAttr = (LinkAttribute)resolutionCard.getAttributeById(reportsAttrId);

		boolean isNonratifiedReportExists = false;
		Collection<Card> reportCardList;
		if(BackLinkAttribute.class.isAssignableFrom(reportsAttrId.getType())) {
			Search search = CardUtils.getFetchAction(resolutionCard.getId(), new ObjectId[] { reportsAttrId });
			final List<Card> actualBackLink = CardUtils.execSearchCards(search, getQueryFactory(), getDatabase(), getSystemUser());
			final BackLinkAttribute bLink = (BackLinkAttribute) actualBackLink.get(0).getAttributeById(reportsAttrId);
			search = CardUtils.getFetchAction(bLink, new ObjectId[] { Card.ATTR_STATE });
			reportCardList = CardUtils.execSearchCards(search, getQueryFactory(), getDatabase(), getSystemUser());
		} else {
			reportCardList = fetchCards(reportsAttr.getIdsLinked(), Collections.singleton(Card.ATTR_STATE), true);
		}
		for (Card reportCard : reportCardList) {
			if (!reportRatifiedStateId.equals(reportCard.getState())) {
				isNonratifiedReportExists = true;
				break;
			}
		}
		
		if (resolutionCard.getTemplate().equals(RESOLUTION_TEMPLATE_ID)) {
			executingDoneWfmId = WFM_FOR_RESOLUTION;
		} else if (resolutionCard.getTemplate().equals(INDEPENDENT_RESOLUTION_TEMPLATE_ID)) {
			executingDoneWfmId = WFM_FOR_INDEPENDENT_RESOLUTION;
		}

		if (!isNonratifiedReportExists && executingDoneWfmId != null) {

			ObjectQueryBase query = getQueryFactory().getFetchQuery(WorkflowMove.class);
			query.setAccessChecker(null);
			query.setId(executingDoneWfmId);
			WorkflowMove wfm = (WorkflowMove)getDatabase().executeQuery(getSystemUser(), query);		

			final ChangeState move = new ChangeState();
			move.setWorkflowMove( wfm );
			move.setCard(resolutionCard);
			execAction(move);
			logger.debug( "[CheckResolutionExecuted:" + resolutionCard.getId() 
					+ "] Resolution proceeded to the next stage by WorkFlowMove " 
					+ wfm.getId() + " '"+ wfm.getMoveName() + "'" 
					+ ", fromStatus=" + wfm.getFromState()
					+ ", toStatus=" + wfm.getToState()
				);
		}
		return null;
	}
	
	public void setParameter(String name, String value) {
		if (PARAM_RESOLUTION_LINK_ATTR.equalsIgnoreCase(name)) {
			resolutionLinkAttrId = IdUtils.smartMakeAttrId(value, BackLinkAttribute.class);
		} else {
			super.setParameter(name, value);
		}
	}

}