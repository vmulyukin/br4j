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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.action.ireferent.GetLayerSubsidiaries;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.ireferent.IReferentException;
import com.aplana.ireferent.card.GroupCard;
import com.aplana.ireferent.card.PersonCard;
import com.aplana.ireferent.types.WSOCollection;
import com.aplana.ireferent.types.WSOGroup;
import com.aplana.ireferent.types.WSOMGroup;
import com.aplana.ireferent.types.WSOMPerson;
import com.aplana.ireferent.util.ServiceUtils;

/**
 * @author PPanichev
 *
 */
@XmlTransient
public class CompletionGroup extends WSOGroup {

    /**
     *
     */
    private static final transient long serialVersionUID = -6673638229340896025L;

    private final transient ObjectId cardLinkAttrId = GroupCard.OVERHEAD_ORGANIZATION;
    private final transient Log logger = LogFactory.getLog(CompletionGroup.class);
    private final transient Set<ObjectId> templates = new HashSet<ObjectId>();
    private transient String parents;

    private transient GroupCard group_card = null;
    private transient WSOCollection childs = new WSOCollection();
    private transient Boolean includeAttachments = false;
    private transient Integer childsLevel = null;
    private transient Boolean isMObject = true;
    private transient DataServiceBean serviceBean = null;
    private transient HashMap<Long, Card> cashe3 = new HashMap<Long, Card>();
    private transient WSOCollection parentsGroup = null;

    public CompletionGroup() {

    }

    public CompletionGroup(GroupCard gc, Boolean includeAttachments,
	    Integer childsLevel, Boolean isMObject) throws DataException, ServiceException, IReferentException {
		this.group_card = gc;
		this.serviceBean = group_card.getServiceBean();
		this.templates.add(CompletionOrganizations.TEMPLATE_DEP_ID);
		parentsGroup = new WSOCollection();
		ObjectId depId = group_card.getDepartmentId();
		// ����� ���������� WSOMGroup
		if (depId != null && depId.getId() != null
			&& !"".equals(depId.getId().toString())) {
		    GroupCard groupCardIReferent = new GroupCard(depId.getId()
			    .toString(), serviceBean);
		    CompletionGroup cg = new CompletionGroup(groupCardIReferent, false,
			    -1, true);
		    parentsGroup.getData().add(cg);
		}
		this.includeAttachments = includeAttachments;
		this.childsLevel = childsLevel;
		this.isMObject = isMObject;
		this.setId(group_card.getId());
		this.setTitle(group_card.getFullName());
		this.setParents(parentsGroup);
		if (!isMObject) {
		    ObjectId leadId = group_card.getChiefId();
		    WSOMPerson persMO = new WSOMPerson();
		    if (leadId != null && !"".equals(leadId.getId().toString())) {
			PersonCard pers = new PersonCard(group_card.getChiefId().getId().toString(), serviceBean);
			CompletionPerson complPers = new CompletionPerson(pers, includeAttachments, true);
			persMO = complPers;
		    }
		    this.setLeader(persMO);
		    this.setType(WSOGroup.CLASS_TYPE);
		} else
		    this.setType(WSOMGroup.CLASS_TYPE);
		    this.setAttachments(new WSOCollection()); // � BR4J ���� ��������
								// ��� �����������
		this.setChilds(childs);
		if (childsLevel >= 0) {
		    // ������� childsLevel ������� �������� (��� ���: childsLevel == 0)
		    setChildsCards(group_card.getCardId());
		}
    }

    private HashMap<Long, Long> findLayerChildsCards() throws DataException, ServiceException {
	GetLayerSubsidiaries subsidiariesParenting = new GetLayerSubsidiaries(templates, parents, cardLinkAttrId);
	HashMap<Long, Long> ids = (HashMap<Long, Long>)serviceBean.doAction(subsidiariesParenting);
	logger.info(String.format("There was found %d cards", ids.size()));
	return ids;
    }

    /**
     * cardId - id ������� �������� cardLinkAttrId - id �������� ���� cardLink;
     * templateId - id ������������ �������;
     * @throws ServiceException
     * @throws DataException
     * @throws IReferentException
     */
    private void setChildsCards(Long cardId) throws DataException, ServiceException, IReferentException {

	final Map<Long, WSOGroup> parentOrgs = new HashMap<Long, WSOGroup>(); // ���� ��������

	Map<Long, Long> read = new HashMap<Long, Long>(); // ���� ��������/��������
	read.put(cardId, -1L); // ������ ������� �������� - ������
	parentOrgs.put(cardId, this);
	Integer level = childsLevel;
	do {
	    Set<Long> key = read.keySet(); // �������� ����
	    parents = ObjectIdUtils.numericIdsToCommaDelimitedString(key);
	    final HashMap<Long, Long> ids = findLayerChildsCards(); // �������� ���� ��������, ��� ��������: parents
	    //  ������ �� ������������ � ��������
	    for (Long k : parentOrgs.keySet()) ids.remove(k);
	    if (ids.isEmpty()) break;
	    read = new HashMap<Long, Long>(ids);
	    String words = ServiceUtils.LongToCommaDelimitedString(ids);
	    int separ = words.indexOf('#');
	    int size_words = words.length();
	    String key_pers = words.substring(0, separ);
	    String val_group = words.substring(separ+1, size_words);
	    Collection<Card> cardsGroup = ServiceUtils.getCards(serviceBean, key_pers, GroupCard.getColumnsGroupCard());
	    Collection<Card> cardsParent = ServiceUtils.getCards(serviceBean, val_group, GroupCard.getColumnsGroupCard());
	    for (Long k : ids.keySet()) {
		Long valGr = ids.get(k);
		for (Card c : cardsParent)
		    if ( valGr.equals((c.getId().getId())) )
			cashe3.put(k, c);
	    }
	    setChildGroupColl(cardsGroup, parentOrgs); // ������� ��������� � childs ��������, �������� ���� �������� � ��������.
	    					       // ��������� - ������� ���� ����� (��������)
	    level--;

	} while (level > 0 || childsLevel <= 0);
    }

    private void setChildGroupColl(Collection<Card> cards, Map<Long, WSOGroup> layerParent) {
	final Map<Long, WSOGroup> childsOrgs = new HashMap<Long, WSOGroup>();
	for (Card cardGroup: cards) {
	    try {
		Long valGroup = (Long)cardGroup.getId().getId();
		Card cardParent = cashe3.get(valGroup);
		Long valParent = (Long)cardParent.getId().getId();
		WSOGroup roundGroup = layerParent.get(valParent);
		 if (roundGroup != null) {
		     WSOCollection currentChilds = roundGroup.getChilds();
		     GroupCard groupCard = new GroupCard(cardGroup, serviceBean);
		     CompletionGroup complGroup = new CompletionGroup(groupCard, includeAttachments, -1, isMObject);
		     currentChilds.getData().add(complGroup);
		     childsOrgs.put(valGroup, complGroup);
		 } else
		     logger.info("com.aplana.ireferent.completion.cards.CompletionGroup.setChildGroupColl �� ������� ������������ ��������: " + valParent + ", ��� ��������: " + valGroup);
	    } catch (Exception e) {
		logger.error("com.aplana.ireferent.completion.cards.CompletionGroup.setChildGroupColl(Collection<Card> cards, Map<Long, WSOGroup> layerParent)", e);
		continue;
	    }
	}
	layerParent.clear();
	layerParent.putAll(childsOrgs);
    }
}
