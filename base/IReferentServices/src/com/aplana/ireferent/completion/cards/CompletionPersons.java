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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.action.ireferent.GetLayerSubsidiariesPerson;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.ireferent.IReferentException;
import com.aplana.ireferent.card.GroupCard;
import com.aplana.ireferent.card.PersonCard;
import com.aplana.ireferent.types.WSOCollection;
import com.aplana.ireferent.types.WSOMPerson;
import com.aplana.ireferent.util.ServiceUtils;

/**
 * @author PPanichev
 *
 */
@XmlTransient
public class CompletionPersons extends WSOCollection {

    private transient Log logger = LogFactory.getLog(getClass());
    private transient Boolean includeAttachments = false;
    private transient Integer childsLevel = null;
    private transient Boolean isMObject = true;
    private transient GroupCard group_card = null;
    private transient String parents;

    private final transient ObjectId cardLinkAttrOverId = GroupCard.OVERHEAD_ORGANIZATION;
    private final transient Set<ObjectId> cardLinkAttrOrgIds = new HashSet<ObjectId>();
    private final transient ObjectId templatePersId = PersonCard.TEMPLATE_PERSON_ID;
    private final transient Set<ObjectId> templateOrgIds = new HashSet<ObjectId>();
    private transient DataServiceBean serviceBean = null;
    private transient HashMap<Long, Card> cashe3 = new HashMap<Long, Card>();

    public CompletionPersons() {

    }

    public CompletionPersons(GroupCard gc, Boolean includeAttachments,
	    Integer childsLevel, Boolean isMObject) throws DataException, ServiceException, IReferentException {
	this.group_card = gc;
	this.serviceBean = group_card.getServiceBean();
	this.includeAttachments = includeAttachments;
	this.childsLevel = childsLevel;
	this.isMObject = isMObject;
	cardLinkAttrOrgIds.add(PersonCard.DEPARTMENT);
	templateOrgIds.add(CompletionOrganizations.TEMPLATE_DEP_ID);
	setChildsCards(group_card.getCardId());
    }

    private HashMap<Long, Long> findLayerChildsCards() throws DataException, ServiceException {
	GetLayerSubsidiariesPerson subsidiariesPerson = new GetLayerSubsidiariesPerson(templateOrgIds, cardLinkAttrOrgIds, parents, templatePersId, cardLinkAttrOverId);
	HashMap<Long, Long> ids = (HashMap<Long, Long>)serviceBean.doAction(subsidiariesPerson);
	logger.info(String.format("There was found %d cards", ids.size()));
	return ids;
    }

    private Collection<Card> findChildsGroupHead() throws DataException, ServiceException, IReferentException {
	final Search search_cards = new Search();
	search_cards.setTemplates(Collections.singleton(DataObject.createFromId(PersonCard.TEMPLATE_PERSON_ID)));
	CardLinkAttribute a = (CardLinkAttribute) serviceBean.getById(PersonCard.DEPARTMENT);
	search_cards.addCardLinkAttribute(a.getId(), group_card.getCard().getId());
	search_cards.setByAttributes(true);
	search_cards.setColumns(PersonCard.getColumnsPersonCard());
	try {
	    @SuppressWarnings("unchecked")
	    SearchResult cardsSR = (SearchResult)serviceBean
		    .doAction(search_cards);
	    Collection<Card> cards = cardsSR.getCards();
	    return cards;
	} catch (DataException ex) {
	    throw new IReferentException("com.aplana.ireferent.completion.cards.CompletionPersons.findChildsGroupHead()",
		    ex);
	} catch (ServiceException ex) {
	    throw new IReferentException("com.aplana.ireferent.completion.cards.CompletionPersons.findChildsGroupHead()",
		    ex);
	}
    }

    /**
     * cardId - id ������� �������� cardLinkAttrId - id �������� ���� cardLink;
     * templateId - id ������������ �������;
     * @throws ServiceException
     * @throws DataException
     * @throws IReferentException
     */
    private void setChildsCards(Long cardId) throws DataException, ServiceException, IReferentException {

		// ����� ������, �������� ��������������� � �������� �����������
		Collection<Card> headPers = findChildsGroupHead();
		for (Card pers : headPers) {
		    cashe3.put((Long)pers.getId().getId(), group_card.getCard());
		}
		setChildGroupColl(headPers);
		if (childsLevel.intValue() >= 0) {
			Map<Long, Long> read = new HashMap<Long, Long>(); // ���� ��������/��������
			read.put(-1L, cardId); // ������ ������� �������� - ������
			int level = childsLevel.intValue();
			do {
			    Collection<Long> val_read = read.values(); // ������� ����
			    parents = ObjectIdUtils.numericIdsToCommaDelimitedString(val_read);
			    final HashMap<Long, Long> ids = findLayerChildsCards(); // �������� ���� ��������, ��� �������� read (sIds)
			    //  ������ �� ������������ � ��������
			    for (Long k : read.keySet()) ids.remove(k);
			    if (ids.isEmpty()) break;
			    read = new HashMap<Long, Long>(ids);
			    String words = ServiceUtils.LongToCommaDelimitedString(ids);
			    int separ = words.indexOf('#');
			    int size_words = words.length();
			    String key_pers = words.substring(0, separ);
			    String val_group = words.substring(separ+1, size_words);
			    Collection<Card> cardsPers = ServiceUtils.getCards(serviceBean, key_pers, PersonCard.getColumnsPersonCard());
			    Collection<Card> cardsGroup = ServiceUtils.getCards(serviceBean, val_group, GroupCard.getColumnsGroupCard());
			    for (Long k : ids.keySet()) {
				Long valGr = ids.get(k);
				for (Card c : cardsGroup)
				    if ( valGr.equals((c.getId().getId())) )
					cashe3.put(k, c);
			    }
			    // ������� ��������� � childs ��������, �������� ���� ��������.
			    // ��������� - ������� ���� ����� (��������)
			    setChildGroupColl(cardsPers);
			    level--;
			} while (level > 0 || childsLevel.intValue() <= 0);
		}
    }

    private void setChildGroupColl(Collection<Card> cards) {
	for (Card cardPers: cards) {
	    CompletionPerson complPers;
	    try {
		PersonCard pers = new PersonCard(cardPers, serviceBean);
		Long c_id = pers.getCardId();
		Card cardGroup = cashe3.get(c_id);
		GroupCard group = new GroupCard(cardGroup, serviceBean);
		complPers = new CompletionPerson(pers, group, includeAttachments, isMObject);
	    } catch (Exception e) {
		logger.error("com.aplana.ireferent.completion.cards.CompletionPersons.setChildGroupColl(Card cards)", e);
		continue;
	    }
	    if (isMObject) {
		WSOMPerson wsMPers = complPers;
		this.getData().add(wsMPers);
	    }
	    else
		this.getData().add(complPers);
	}
    }
}
