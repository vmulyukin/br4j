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
/**
 *
 */
package com.aplana.ireferent.completion.cards;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.action.ireferent.GetParents;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.ireferent.IReferentException;
import com.aplana.ireferent.card.GroupCard;
import com.aplana.ireferent.types.WSOCollection;
import com.aplana.ireferent.types.WSOMGroup;
import com.aplana.ireferent.util.ServiceUtils;

/**
 * @author PPanichev
 *
 */
@XmlTransient
public class CompletionOrganizations extends WSOCollection{

    public static final transient ObjectId TEMPLATE_ORG_ID = ObjectId.predefined(
	    Template.class, "jbr.organization");
    public static final transient ObjectId TEMPLATE_DEP_ID = ObjectId.predefined(
	    Template.class, "jbr.department");
    public static final transient ObjectId CARD_STATE_ID = ObjectId.predefined(
	    CardState.class, "published");

    private transient Log logger = LogFactory.getLog(getClass());
    private transient Set<ObjectId> templates = new HashSet<ObjectId>();
    private transient DataServiceBean serviceBean = null;

    public CompletionOrganizations() {

    }

    public CompletionOrganizations(Boolean includeAttachments, Boolean isMObject, DataServiceBean serviceBeanCO) throws DataException, ServiceException, IReferentException {

	this.serviceBean = serviceBeanCO;
	this.templates.add(TEMPLATE_DEP_ID);
	List<Long> organizationIds = findParentsCards();
	String listCards = ObjectIdUtils.numericIdsToCommaDelimitedString(organizationIds);
	Collection<Card> cardsGroup = ServiceUtils.getCards(serviceBean, listCards, GroupCard.getColumnsGroupCard());
	for(Card cardGroup : cardsGroup) {
	    try {
		GroupCard group = new GroupCard(cardGroup, serviceBean);
		CompletionGroup complGroup = new CompletionGroup(group, includeAttachments, -1, isMObject);
		if (isMObject) {
		    WSOMGroup mGroup = complGroup;
		    this.getData().add(mGroup);
		} else {
		    this.getData().add(complGroup);
		}
	    } catch (Exception e) {
		logger.error("com.aplana.ireferent.completion.cards.CompletionOrganizations.GroupCard.error: " + cardGroup.getId().getId(), e);
	    }
	}
	/*for(Long org :organizationIds) {
	    try {
		String groupID = new ObjectId(Card.class, org).getId().toString();
		GroupCard groupCardIReferent = new GroupCard(groupID, serviceBean);
		CompletionGroup complGroup = new CompletionGroup(groupCardIReferent, includeAttachments, -1, isMObject);
		if (isMObject) {
		    WSOMGroup mGroup = complGroup;
		    this.getData().add(mGroup);
		} else {
		    this.getData().add(complGroup);
		}
	    } catch (Exception e) {
		logger.error("com.aplana.ireferent.completion.cards.CompletionOrganizations.GroupCard.error: " + org, e);
	    }
	} */
    }

    private List<Long> findParentsCards() throws DataException, ServiceException {
	GetParents parents = new GetParents(templates, CARD_STATE_ID, GroupCard.OVERHEAD_ORGANIZATION);
	List<Long> ids = (List<Long>)serviceBean.doAction(parents);
	logger.info(String.format("There was found %d cards", ids.size()));
	return ids;
    }
}
