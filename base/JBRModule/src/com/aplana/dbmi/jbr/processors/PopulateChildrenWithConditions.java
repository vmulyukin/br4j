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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.CreateCard;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.jbr.util.AttributeSelector;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.jbr.util.IdUtils.IdPair;
import com.aplana.dbmi.jbr.util.PathAttributeDescriptior;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.LinkAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.TypedCardLinkAttribute;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.BasePropertySelector;
import com.aplana.dbmi.service.impl.SaveQueryBase;

/**
 * �������� ��������� ��� PopulateChildren, ������� ������� ����� ������� ��������
 * ������ ��� ���������� ������������ ������� � �������� �� ����������� �� ������ � ������� ��������, �� � � ������������ ��������� <br>
 *
 * ���������� ��� �������� ������� 3 ���������: <br>
 * 1. attr_condition - ������� ������� � ���� "<������� ������� ��������><�������� ���������><���������>" <br>
 * 2. attr_link_path - ������ ��������� ������� ����� ����������� '~', ������� ������� ���� � ������ ���������� �, �.�.
 * �������� �������� ��������� � ��� ������, ����� ����������� "������� 1" � "������� 2" ... � "������� N", ��� ���� ������ ��������� �������
 * ������� �� ������ ��������� ���������� ����� ����������� ';': ��������� ������� ��� ��������� �������� ��� �������� ������� <br>
 * ����������� ������ ��������� � ���, ��� ������ ��������� ������� ����� �������� ���� ������ �� ������ � ���, ��������, ��� ������� ����������
 * ���������, �� � � ��������, ���������� � ���������� ���������� ������ �� ���������� �������<br>
 * 3. attr_link_values - ������ �������� ��� �������� ��������� ���������� ���������
 * (���������, ������ ��������, ������ �� ������� ������� ��������). ������ ����������� '~', ������ �� ������� �������� ��������� ��������� ��������
 * ��� ��������� � ���������� ���������.<br>
 * ��������� ����� ������ ������ ��� TypedLink-���������, ��� ��� ������ ����������� ������ ��������� � �����������
// ToDo: ����������, ����� ����� ���� ������������ ��� ���� �������-��������� �������� �� objectids.properties<br>
//(21.09.2011)�������� ����� ������� ��������: attach_unique<br>
//(21.09.2011)�������� ����� ������� ��������: populate_if_false<br>
/*(21.09.2011)��������� ����������� �������� ���� TypedCardLink� ��� �������� �� BackLink.<br>
 * �� ���� �������������� ������� �� BackLink, � ����������� TypedCardLink, ��������� � ���� BackLink��:<br>
 * <pre>
 * {@code
 *  <parameter name="attr_link_path" value="backLink:jbr.doclinks.references@_CARDTYPE;"/>
 *  <parameter name="attr_link_values" value="=1502,1601;"/>
 *  }
 * </pre>
 *  @author ynikitin
 */
public class PopulateChildrenWithConditions extends PopulateChildren {

	private static final long serialVersionUID = 1L;

	/**
	 * ����������� ����������� ��������� ����������, ������� ��� � ��������: PARAM_XXX.
	 */
	private static final String MSG_CARD_0_DID_NOT_SATISFY_CONDITIONS = "card ''{0}'' did not satisfy conditions -> no more children populate";
	private static final String MSG_CARD_0_CREATE_ANYWAY = "card ''{0}'' did not satisfy conditions -> created empty child card: ''{1}''";
	protected static final String COND_REG_SEPARATOR = "[;]";

	protected static final String REG_MULTI_CONDITION_SEPARATOR = "[~]";
	protected static final String SET_SEPARATOR = "[=]";
	// ������� �������� � ����: "<objectid-�������� ��������>{.���� �
	// ��������}=��������"
	/**
	 * ������� ����������� ������� � ���� "������� ��������" (�������) = ��������,
	 * ��� ���� � �������� �������� ����������� ������������ ���������
	 * ������� �������� � ����: "<objectid-�������� ��������>{.���� � ��������}=��������"
	 **/
	protected static final String PARAM_ATTR_CONDITION = "attr_condition";
	/*
	 * ������� ����������� ������� (�������� �������) ������ ���� ������������ � ���� 2 ���������, �� ��������� ���� � ������ ��������
	 */

	/**
	 * ���� ��������� ��������� ��������
	 */
	protected static final String PARAM_ATTR_LINK_PATH = "attr_link_path";

	/**
	 * � ����� �� ����������� �������� ����������� �������� ��������
	 */
	protected static final String PARAM_ATTACH_DEST_CARD_INDEX = "attach_dest_card_index";

	/**
	 * ������� ����������� ������� = ��������
	 * (��������� ��� ������ �������� ��� ������������ �������� ������� ��������)
	 */
	protected static final String PARAM_ATTR_LINK_CONDITION = "attr_link_values";

	static final String PARAM_SET_ATTR = "set";

	/**
	 * ������ ������� ����, ��� � �������� ������������ ������ ���� ���
	 * ���������� ���� (�� ��������� false). �� ����, ���� � ��������
	 * {@link PopulateChildren#PARAM_ATTACH_ATTR attach} ��� ���� �����������
	 * ����� �����, ����� ����������� �� �����. ��������� ��� �� ��������� �
	 * ��������� �������� (����������� ������ ������ �� objectid.properties)
	 * �������� �������� ���������� ���������, �� ������ � ������
	 */
	private static final String PARAM_UNIQUE = "attach_unique";

	/**
	 * ������, ��������� ��� ��� ����� ��������� ������� ���� � ������ ������������� ��������.
	 * ���� true, �� ��������� ������ ����� (�� ��������� false). �������� ����� � ��������
	 * �������� ������ ���� � {@link PopulateChildren#PARAM_ATTACH_ATTR}
	 * ����� ������� ������� �����, � ��������� ������ ����� �� ���������.
	 */
	private static final String PARAM_POPULATE_IF_FALSE = "populate_if_false";

	/**
	 * ������ ������� ����, ��� � ����� ����� ����������� ������ ���������� (��
	 * ������������ � ������ ������������ ������) ������, ��������� �
	 * {@link PopulateChildren#PARAM_SPLIT_ATTR split}. �� ���� ���� ����������
	 * ��� ������������� ����� � ������ ������, ����� ����������� �� �����.
	 */
	private static final String PARAM_SPLIT_UNIQUE = "split_unique";

	/**
	 * ���������� ���������.
	 */
	protected static final String MSG_PARAMETER_ASSIGNED_3 = "assigned parameter ''{0}''=''{1}''  ->  ''{2}''";

	/**
	 * ������ ������� ������� (�������-��������) - �������� ������ � ���������� ������� �������������� ��������
	 */
	protected final List<BasePropertySelector> conditions = new ArrayList<BasePropertySelector>();

	/**
	 * ��������� ���� ������� ��� ��������, �������� ���������� ������� �
	 * ������������ ���� ������� ���������� �� �������� �������� ��������.
	 */
	protected final List<MultiCondition> multiConditions = new ArrayList<MultiCondition>();
	// ��������������� ������, ����������� �� ����� ���������� ����������
	protected final List<String> multiConditionsAttributes = new ArrayList<String>();
	protected final List<String> multiConditionsValues = new ArrayList<String>();
	protected List<ObjectId> childs = new ArrayList<ObjectId>();

	private List<IdPair> copyAttrPairs = new ArrayList<IdPair>();
	private List<IdPair> setAttrPairs = new ArrayList<IdPair>();

	private int attachDestCardIndex = 0;
	private HashSet<ObjectId> unique = new HashSet<ObjectId>();
	private boolean splitUnique = false;
	private boolean populateIfFalse = false;


	private String specialFlag = "";	// ����������� ���� (��� MEDO ��������� ����������� ������� ������ ���������� ������ ��������,
	// ���������� ������� � ��� ������ ���� = ������ �������� �� ����������� �������)
	@Override
	public Object process() throws DataException {
		if (templateId == null || attachAttrId == null)
			throw new IllegalStateException("Not all mandatory parameters are set");
		final Card source = loadCard(getCardId());
		if (!fillMultiConditions(source))
			return null;
		Card parent = source;
		//List<ObjectId> childs = new ArrayList<ObjectId>();
		if (pathAttrIds != null) {
			for (int i = 0; i < pathAttrIds.length; i++) {
				CardLinkAttribute link = (CardLinkAttribute) parent.getAttributeById(pathAttrIds[i]);
				if (link == null)
					throw new IllegalStateException("Attribute " + pathAttrIds[i].getId() +
							" not found in card " + parent.getId().getId());
				if (link.getLinkedCount() != 1)
					throw new IllegalStateException("Attribute " + pathAttrIds[i].getId() +
							" has " + link.getLinkedCount() + " links in card " + parent.getId().getId() +
							"; can't choose one");
				parent = loadCard(link.getSingleLinkedId());
			}
		}

		// �������� ������� �������
		if (	checkConditions(conditions, source)
				&& checkLinkConditions(source)) {
	//		if (unique) {
				for (MultiCondition mc : multiConditions) {
					final Set<Card> farCards = new HashSet<Card>();
					farCards.addAll(mc.getLinkFarCards());
					mc.getLinkFarCards().clear();
					mc.getLinkFarCards().addAll(farCards);
				}

	//		}
			if (splitAttrIds != null) {
				// ������� ����� � ����������� �� � ��������� ��������� ������������� ������������ �������
				for(Card sourceCard: multiConditions.get(attachDestCardIndex).getLinkFarCards()){
					final LinkAttribute attachAttr = getAttachAttr(sourceCard);
					Iterator<?> itr = null;

					if (splitAttrIds.sourceId() == null){	// �� ������ �������� ������
						if (splitAttrIds.sourceCardIndex() > -1){	// �� ������ ����� �������� �� �������
							ArrayList<ObjectId> ids = new ArrayList<ObjectId>();
							for(Card card: multiConditions.get(splitAttrIds.sourceCardIndex()).getLinkFarCards())
								ids.add(card.getId());
							itr = ids.iterator();
						}
					}else{ // ������ �������� ������
						Attribute splitAttr = null;
						List<Card> splitSourceCards = new ArrayList();
						if (splitAttrIds.sourceCardIndex() > -1){ // � ������ ����� �������� �� �������
							splitSourceCards.addAll(multiConditions.get(splitAttrIds.sourceCardIndex()).getLinkFarCards());
						}else{
							splitSourceCards.add(source);
						}
						// ���� ���������� ���������, �� ��������� �������� �� ������ �������, �� � ���� ���������
						List list = new ArrayList();
						for(Card splitSourceCard: splitSourceCards){
							splitAttr = splitSourceCard.getAttributeById(splitAttrIds.sourceId());
							if (splitAttr == null)
								throw new IllegalStateException("Either " + splitAttrIds.sourceId() +
										" not found in card " + splitSourceCard.getId().getId());
							list.addAll(splitAttributeValue2(splitAttr));
						}

						itr = list.iterator();
					}
					if (itr != null)
						uniq: while (itr.hasNext()){
							Object obj = itr.next();
							if (splitUnique) {
								Collection<ObjectId> values = getAttachIdsLinked(sourceCard);
								for (ObjectId val : values) {
									Card c = loadCard(val);
									//if (splitAttrIds.sourceId() == null) {
									if (((CardLinkAttribute)c.getAttributeById(splitAttrIds.destId())).getIdsLinked().contains(obj))
										continue uniq;
										/*}
										if (c.getAttributeById(splitAttrIds.destId()).equals(obj))
												//(((Card) obj).getAttributeById(splitAttrIds.sourceId())))
											continue uniq;
									*/
								}
							}
							createAndLinkChild(sourceCard, attachAttr, obj);
						}
					// ��������� ��� ��������, � ������� ��������� �����
					if(!attachReverse) {
						if ("CARD".equalsIgnoreCase(forceSaveMode)) {
							saveParent(sourceCard);
						} else if ("ATTRIBUTE".equalsIgnoreCase(forceSaveMode)) {
							updateAttribute(sourceCard.getId(), attachAttr);
						}
					}
				}
			} else {
				// ������� ����� � ����������� �� � ��������� ��������� ������������� ������������ �������
				if (attachDestCardIndex+1>multiConditions.size()){
					throw new DataException("AttachDestCardIndex " + attachDestCardIndex +
							" is most of multiCondition's size");
				}
				for(Card sourceCard: multiConditions.get(attachDestCardIndex).getLinkFarCards()){
					LinkAttribute attachAttr = getAttachAttr(sourceCard);
					createAndLinkChild(sourceCard, attachAttr, null);
					// ��������� ��� ��������, � ������� ��������� �����
					if(!attachReverse) { 
						if ("CARD".equalsIgnoreCase(forceSaveMode)) {
							saveParent(sourceCard);
						} else if ("ATTRIBUTE".equalsIgnoreCase(forceSaveMode)) {
							updateAttribute(sourceCard.getId(), attachAttr);
						}
					}
				}
			}

			if (forceSaveChilds){
				for(ObjectId c: childs){
					Card childCard = loadCard(c);
					saveParent(childCard);
				}
			}
			if (getAction()!=null && getAction() instanceof ChangeState)
				((ChangeState) getAction()).setCard(source);
		} else {
			if (populateIfFalse) {
				LinkAttribute attachAttr = (LinkAttribute) source.getAttributeById(attachAttrId);
				if (attachAttr != null) {
					splitAttrIds = null;
					setAttrPairs.clear();
					copyAttrPairs.clear();
					ObjectId emptyChild = createAndLinkChild(source, attachAttr, null);
					if(!attachReverse) {
						if ("CARD".equalsIgnoreCase(forceSaveMode)) {
							saveParent(source);
						} else if ("ATTRIBUTE".equalsIgnoreCase(forceSaveMode)) {
							updateAttribute(source.getId(), attachAttr);
						}
					}
				logger.info(MessageFormat.format( MSG_CARD_0_CREATE_ANYWAY, source.getId().getId(), emptyChild.getId()));
				} else
					logger.info(MessageFormat.format( MSG_CARD_0_DID_NOT_SATISFY_CONDITIONS, source.getId().getId()));
			} else {
				logger.info(MessageFormat.format( MSG_CARD_0_DID_NOT_SATISFY_CONDITIONS, source.getId().getId()));
			}

		}
		return null;
	}

	protected LinkAttribute getAttachAttr(Card sourceCard) throws IllegalStateException {
		LinkAttribute attachAttr = (LinkAttribute) sourceCard.getAttributeById(attachAttrId);
		if (attachAttr == null)
			throw new IllegalStateException("Either " + attachAttrId.getId() + " not found in card "
					+ sourceCard.getId().getId());
		return attachAttr;
	}

	protected Collection<ObjectId> getAttachIdsLinked(Card sourceCard) throws DataException {
		if (attachReverse) {
			List<Card> linkedCards = super.getLinkedCards(sourceCard.getId(), attachAttrId, false);
			return ObjectIdUtils.getObjectIds(linkedCards);
		} else {
			CardLinkAttribute attachAttr = (CardLinkAttribute) getAttachAttr(sourceCard);
			Collection<ObjectId> values = attachAttr.getIdsLinked();
			return values;
		}
	}

	protected ObjectId createAndLinkChild(Card sourceCard, final LinkAttribute attachAttr, Object obj) throws DataException {
		if (attachReverse) {
			IdPair idPair = new IdPair();
			idPair.dest.setId(((BackLinkAttribute) attachAttr).getLinkSource());
			idPair.source.setId(sourceCard.getId());
			this.setAttrPairs.add(idPair);
			ObjectId child = createChild(sourceCard, obj);
			this.setAttrPairs.remove(idPair);
			if (child != null) {
				childs.add(child);
			}
			return child;
		} else {
			ObjectId child = createChild(sourceCard, obj);
			if (child != null) {
				attachAttr.addLinkedId(child);
				childs.add(child);
			}
			return child;
		}
	}

	/**
	 * ��������� ���������� �������� ��� �������� cardId.
	 * @param cardId
	 * @param attr
	 * @throws DataException
	 */
	private void updateAttribute(ObjectId cardId, Attribute attr)
		throws DataException
	{
		doOverwriteCardAttributes(cardId, attr);
	}

	@Override
	/**
	 * ������� ������� ��� ������� �������� � ������������ ������������ ��������� ��� �� ������� ��������,<br>
	 * ��� � �� ������ � ������ ��������� ������������� ������������ �������: <br>
	 * 1. ���� � ��������� �������� �� copyAttrPairs sourceCardIndex == -1, �� ���������� �� �������� ��������<br>
	 * 2. ���� � ��������� �������� �� copyAttrPairs sourceCardIndex != -1, �� ���������� �� ������ ��������� ��������
	 * � ������� �� multiConditions � conditionIndex == sourceCardIndex
	 */
	protected ObjectId createChild(Card parent, Object variant) throws DataException {
	    	Card sourceCard = parent;
		CreateCard create = new CreateCard(templateId);
		ActionQueryBase createQuery = getQueryFactory().getActionQuery(create);
		createQuery.setAction(create);
		Card child = (Card) getDatabase().executeQuery(getSystemUser(), createQuery);
		
		if (splitAttrIds != null) {
			setAttributeValue(child.getAttributeById(splitAttrIds.destId()), variant);
		}
		
		for (IdPair entry : copyAttrPairs) {
			try {
				if (entry.sourceCardIndex() != -1){
					List<Card> sourceCards = getFarCardsFromMultiConditions(entry.sourceCardIndex());
					parent = (sourceCards.size()>0)?sourceCards.get(0):null;
					copyAttrbiuteValue( (sourceCards.size()>0)
							? ( (sourceCards.get(0)!=null) ? parent.getAttributeById( entry.sourceId()) : null )
							:null
						, child.getAttributeById(entry.destId()) );
				} else
					copyAttrbiuteValue((parent!=null)?parent.getAttributeById( entry.sourceId()):null,
						child.getAttributeById(entry.destId()));
			} catch (Exception e) {
				logger.warn("Error copying attribute " + entry.source +
						" to " + entry.dest, e);
				// just skipping this attribute
			}
		}
		
		for (IdPair entry : setAttrPairs) {
			try {
				if (entry.sourceCardIndex() !=-1){
					List<Card> sourceCards = getFarCardsFromMultiConditions(entry.sourceCardIndex());
					parent = (sourceCards.size()>0)?sourceCards.get(0):null;
					Attribute attr = parent.getAttributeById(entry.destId());
					if (attr == null)
						attr = (Attribute)DataObject.createFromId(entry.destId());
					setAttributeValue(attr, entry.sourceId()==null ? null : entry.sourceId().getId());
				} else{
					Attribute attr = child.getAttributeById(entry.destId());
					if (attr == null)
						attr = (Attribute)DataObject.createFromId(entry.destId());
					setAttributeValue(attr, entry.sourceId()==null ? null : entry.sourceId().getId());
				}
			} catch (Exception e) {
				logger.warn("Error setting attribute " + entry.dest +
						" from " + entry.source, e);
				// just skipping this attribute
			}
		}
		
		if (!unique.isEmpty()) {
			boolean eq = false;
			ArrayList<Attribute> childAttrs = new ArrayList<Attribute>();
			for (ObjectId id : this.unique) {
				childAttrs.add(getAttributeById(child, id));
			}
			while (childAttrs.contains(null))
				childAttrs.remove(null);
			LinkAttribute attachAttr = (LinkAttribute) getAttributeById(sourceCard, attachAttrId);
			@SuppressWarnings("unchecked")
			Collection<ObjectId> values = attachAttr.getIdsLinked();

			block: for (ObjectId val : values) {
				Card c = loadCard(val);
				HashSet<Attribute> attrs = new HashSet<Attribute>(); // c.getAttributes();

				for (ObjectId id : this.unique) {
					attrs.add(getAttributeById(c, id));
				}
				if (attrs.remove(null))
					logger.warn("Card \"" + c.getId().getId()
							+ "\" does not contain the specified attribute");
				for (Attribute attr : attrs) {
					if (!childAttrs.contains(attr))
						continue;
					if (attr.equalValue(childAttrs.get(childAttrs.indexOf(attr))))
						eq = true;
					else {
						eq = false;
						continue block;
					}
				}
				// ���� �� ���� ���������� ��������� ������� ���� ���� ��������, �� �������� �����
				if (eq)
					break block;
			}
			if (eq)	// ���� ���� ���� ���� ��������, ������������� � ��������, � ������ �� ����������� ����������, ��� � ��������, �� �������� �� �����������
				return null;
		}

		SaveQueryBase saveQuery = getQueryFactory().getSaveQuery(child);
		saveQuery.setObject(child);
		ObjectId id = (ObjectId) getDatabase().executeQuery(getSystemUser(), saveQuery);
		UnlockObject unlock = new UnlockObject(id);
		ActionQueryBase unlockQuery = getQueryFactory().getActionQuery(unlock);
		unlockQuery.setAction(unlock);
		getDatabase().executeQuery(getSystemUser(), unlockQuery);
		return id;
	}

	@Override
	public void setParameter(String name, String value) {
		// ��������� ������� �������
		if (PARAM_ATTR_CONDITION.equals(name)){
			try {
				final AttributeSelector selector = AttributeSelector.createSelector(value);
				this.conditions.add( selector);
			} catch (DataException ex) {
				ex.printStackTrace();
			}
		} else if (PARAM_COPY_LIST.equals(name)) {
			String[] list = value.trim().split(COND_REG_SEPARATOR);
			for (int i = 0; i < list.length; i++) {
				final IdPair pair = IdPair.parseRule(list[i]);
				if (pair != null)
					copyAttrPairs.add(pair);
			}
		} else if (PARAM_ATTACH_DEST_CARD_INDEX.equals(name)){
			attachDestCardIndex = 0;
			try{
				attachDestCardIndex = Integer.parseInt(value);
			} catch (Exception e){
				attachDestCardIndex = 0;
			}
		} else if ("specialFlag".equals(name)){
			specialFlag = value;
		} else if (PARAM_ATTR_LINK_PATH.equals(name)){
			// ��������� ��������� ������� ��� ������� �������
			final String[] sIds = value.trim().split(REG_MULTI_CONDITION_SEPARATOR);
			if (sIds.length >0) {
				this.multiConditionsAttributes.clear();
				int i = 0;
				for (String s: sIds) {
					if (s == null || s.length() == 0) continue;
					this.multiConditionsAttributes.add(s);
					i++;
				}
			}
		}
		// ��������� �������� ������������� ��������� (�������� ����� ���� ������������ � )
		else if (PARAM_ATTR_LINK_CONDITION.equals(name)){
			// ��������� ��������� ��������� �������� ��� ������� �������
			final String[] sIds = value.trim().split(REG_MULTI_CONDITION_SEPARATOR);
			if (sIds.length >0) {
				this.multiConditionsValues.clear();
				int i = 0;
				for (String s: sIds) {
					if (s == null || s.length() == 0) continue;
					this.multiConditionsValues.add(s);
					i++;
				}
			}
			// ��������� �������� ������������� ��������� (�������� ����� ���� ������������ � )
		}else if (PARAM_SET_ATTR.equals(name)){
			String[] list = value.trim().split(REG_SEPARATOR);
			for (String setRule : list){
				String[] pairString = setRule.split(SET_SEPARATOR);
				if (pairString.length != 2){
					logger.warn("Broken SET rule: "+setRule+" Skipping.");
					continue;
				}
				final IdPair pair = new IdPair();
				pair.dest.setId( IdUtils.smartMakeAttrId( pairString[0].trim(),
						ListAttribute.class, false));
				if (pair.destId() == null){
					logger.warn("Broken SET rule: "+setRule+" Can not determine " +
							"destination attribute: "+pairString[0]+" Skipping.");
					continue;
				}
				if (!"NULL".equalsIgnoreCase(pairString[1])){
					pair.source.setId( IdUtils.smartMakeAttrId(pairString[1].trim(),
							ReferenceValue.class));
					if (pair.sourceId() == null){
						logger.warn("Broken SET rule: "+setRule+" Can not determine " +
								"source attribute/value: "+ pairString[1]+" Skipping.");
						continue;
					}
				}
				setAttrPairs.add(pair);
			}

		} else if (PARAM_UNIQUE.equals(name)) {
			// ��������� ������ ������ ������ ��������� ����������� �� ������������
			String[] attrs = value.split(COND_REG_SEPARATOR);
			for (String attr: attrs){
				this.unique.add( IdUtils.smartMakeAttrId(attr.trim(), CardLinkAttribute.class, false));
			}
		} else if (PARAM_SPLIT_UNIQUE.equals(name)) {
			this.splitUnique = Boolean.parseBoolean(value);
		} else if (PARAM_POPULATE_IF_FALSE.equals(name)) {
			this.populateIfFalse = Boolean.parseBoolean(value);
		} else
			super.setParameter(name, value);
	}

	private Attribute getAttributeById(Card card, ObjectId attributeId) {
		if (Card.ATTR_STATE.equals(attributeId)) {
			ListAttribute attr = new ListAttribute();
			attr.setId(Card.ATTR_STATE);
			attr.setValue((ReferenceValue) DataObject.createFromId(
				new ObjectId(ReferenceValue.class, card.getState().getId())));
			return attr;
		}
		return card.getAttributeById(attributeId);
	}

	/**
	 * @param s ������ ���� "{���:} id {@id2}",
	 * {x} = �������������� ����� x.
	 * ��������, "string: jbr.organization.shortName"
	 * 			 "back: jbr.sender: fullname"
	 * ���� ��� ������, �� ������ ���������������� �������� � ����� ������ �
	 * �����-���� �����, ���� ��� ������, �� ����������� �� "string".
	 * @return
	 * @throws DataException
	 */
	protected static PathAttributeDescriptior makeObjectId(final String s)	{
		return (s == null) ? null : new PathAttributeDescriptior(s);
	}

	/**
	 * ���������� ������ ������� �� ������ ������ ����������� ��������� � ����������� ��������
	 */
	protected boolean fillMultiConditions(Card card){
		// ������� ������� ������� � �������� 0, ��������� � �������� ����� �������� �������� ������� ������ �� �������
		{
			final MultiCondition ms = new MultiCondition(0, 0);
			ms.getStartCardsList().add(card);
			ms.getLinkFarCards().add(card);
			multiConditions.add(ms);
		}

		if (multiConditionsAttributes.isEmpty()||multiConditionsValues.isEmpty()){
			logger.warn( "MultiConditions is empty, PopulateChildren can be configured instead of this processor");
			return true;
		}
		if (multiConditionsAttributes.size() != multiConditionsValues.size()){
			logger.warn( "multiConditionsAttributes size is not equal for multiConditionsValues -> populate children break");
			return false;
		}

		// ��������� ������ multiConditions
		int size = multiConditionsAttributes.size();
		for (int i=0; i<size; i++){
			final String attrIds = multiConditionsAttributes.get(i);
			final String[] sIds = attrIds.trim().split(COND_REG_SEPARATOR);
			final MultiCondition ms = new MultiCondition(i+1, 0);
			multiConditions.add(ms);
			if (sIds.length >0) {
				ms.getAttrIdList().clear();
/*				if (sIds[0].substring(0, 1).equals(REG_NUMBER_SYMBOL)){
					int startCardIndex = 0;
					try{

						if
						startCardIndex = Integer.parseInt();
					} catch (Exception e){
						startCardIndex = 0;
					}
					ms.setStartCardsIndex(startCardIndex);
				}*/
				// ��������� ������ ��������� ��������
				int startCardIndex = 0;
				sIds[0] = sIds[0].trim();
				final String sCardIndex = getStartIndex(sIds[0]);
				if ( sCardIndex != null && sCardIndex.length()>0){
					sIds[0] = clearStartIndex(sIds[0], sCardIndex);
					try{
						startCardIndex = Integer.parseInt(sCardIndex);
					} catch (Exception e){
						startCardIndex = 0;
					}
				}
				ms.setStartCardsIndex(startCardIndex);

				// ��������� ������ ������������� ���������
				for (String s: sIds) {
					if (s == null || s.length() == 0) continue;
					final PathAttributeDescriptior id = makeObjectId(s);
					ms.getAttrIdList().add( id);
				}
			}
			// ����������� ������ ����������� �������� ���������
			final String value = multiConditionsValues.get(i);
			final String[] ids = value.split(COND_REG_SEPARATOR);
			if (ids.length > 0) {
				ms.getStringConditions().clear();
				for (String s: ids) {
					ms.getStringConditions().add(s);
				}
			}
		}
		return true;
	}

	protected boolean checkConditions(List<BasePropertySelector> conds, Card c) throws DataException
	{
		/*
		 * ������ �������� ������:
		 * 		attrList = (ListAttribute) card.getAttributeById( ((AttributeSelector) cond).attrId);
		 * 		attrList.getReference() = ObjectId(id='ADMIN_26973', type=com.apana.dbmi.model.Reference);
		 * 		attrList.getValue() = ReferenceValue(
		 *			id=ObjectId(id=1433, type=com.apana.dbmi.model.ReferenceValue),
		 *			active=false, children=null, ..., valueEn="No", valueRu="���")
		 */
		if (conds == null || c == null || conds.size()==0) return true;
		boolean cardFetched = false;
		for (BasePropertySelector cond: conds)
		{
			if (!cardFetched) {
				// ���������� �������� ���� ��� �� ������...
				if ( 	(cond instanceof AttributeSelector)
						&& null == c.getAttributeById( ((AttributeSelector) cond).getAttrId() ) )
				{
					c = loadCard(c.getId());
					cardFetched = true;
				}
			}
			final boolean ok = cond.satisfies(c);
			if (!ok) {
				logger.info( "Card "+ c.getId().getId() +" did not satisfies codition {" + cond+ "}  -> check is negative");
				return false;
			}
		}
		return true;
	}

	/** �������� �������� �������, �.�. ����������� ������ ��������� ��������, ���������������
	 * ���� ��������
	 *
	 * @param c - ��������, ��� ������� ��������� �������
	 * @return true - ���� ���������� ��������, false - ���
	 * @throws DataException
	 */
	protected boolean checkLinkConditions(Card c) throws DataException
	{
		/*
		 * ������ �������� ������:
		 * 		attrList = (ListAttribute) card.getAttributeById( ((AttributeSelector) cond).attrId);
		 * 		attrList.getReference() = ObjectId(id='ADMIN_26973', type=com.apana.dbmi.model.Reference);
		 * 		attrList.getValue() = ReferenceValue(
		 *			id=ObjectId(id=1433, type=com.apana.dbmi.model.ReferenceValue),
		 *			active=false, children=null, ..., valueEn="No", valueRu="���")
		 */

		// ��������� ��� ������� (����� �������) ��������� ...
		// ���� ������� �� ����������� -> ����� ��������� �������� � false
		for(int i=1; i < multiConditions.size(); i++){
			generateStartCardsList(multiConditions.get(i));
			final boolean ok = processLinkAttributes(multiConditions.get(i), c);
			if (!ok) {
				logger.info( "link_codition #"+multiConditions.get(i).getConditionIndex()+" for cards "+ multiConditions.get(i).getStartCardsList()+" is not execute -> populate children break");
				return false;
			}
		}
		return true;
	}

	/** ���������������� ��� ������� ������� � ������� ������� ��������
	 *
	 * @param card - ������� ��������
	 * @return true - �� ����������� ���� �������� ������� ���� �� ���� ��������, false - �� ����� �������� �� �������, ��� ����
	 * ���� � �������� ����� � ���������� �������� ���� ������� �� ����� �������� �� ����� �������, �� � ������ ������� ���� ���� �� ����
	 * � ��������� �� �����������, �� ��������� = true
	 * @throws DataException
	 */
	protected boolean processLinkAttributes(MultiCondition ms, Card processCard) throws DataException
	{
/*		final String tag = "(cards=" + (( != null) ? card.getId() : "null") + ")";
		logger.debug("preparing arguments values for "+ tag + "...");
*/
		if (ms.getStartCardsList().size() < 1) {
			logger.debug( "start card list is empty -> exiting");
			return false;
		}

		if (ms.getAttrIdList().size() < 1) {
			logger.debug( " args list is empty -> exiting");
			return false;
		}

		final List<LinkCondition> linkConditions = createLinkConditionList(ms.getStringConditions());
		boolean isConditionsContainNeq = false;
		int prevFinalCardsCount = 0;
		// ������������ ������ ������������� ��������
		ms.getLinkFarCards().addAll(ms.getStartCardsList());	// ������ ������� ������������� ��� ������ ��������� ��������, �� ������ �������� � ����������� ������ ������������� ��������
		// ����������� �� ���� ������������� ��������� � ��������� ������ ������������� ��������
		for (PathAttributeDescriptior item: ms.getAttrIdList()) {
			isConditionsContainNeq = containNeqOperand(linkConditions);
			// ���� � �������� �������� ����� ����� �� ������� �� ����� ��������������� ��������, �� �������� ���� ���� ��������� attrIdList
			if (ms.getLinkFarCards().isEmpty())
				return isConditionsContainNeq;
			//this.attrIdList.indexOf(item);
			List<Card> foundFarCards;

			// ��������� ������� � ������ ���������� ������������� ������, �������� � typedlink:jbr.relatdocs@_CARDTYPE ��� _CARDTYPE
			Attribute finalAttr = null;
			Iterator<ObjectId> iterIds = item.getAttrIds().iterator();
			// ����� �� ���� ��������� (��� ����� ���������� - �����)
			final List<Card> finalCards = new ArrayList<Card>();
			while (iterIds.hasNext()) {
				foundFarCards = new ArrayList<Card>();
				final ObjectId attrId = iterIds.next();
				if (iterIds.hasNext()){
					finalCards.clear();
					// �������� �� ���� ��������� ��������� ������ ��� �������� �����
					for(Card analizeCard: ms.getLinkFarCards()){
						finalAttr = analizeCard.getAttributeById(attrId);
						if (finalAttr == null) {
							logger.warn( MessageFormat.format(MSG_CARD_0_HAS_NO_ATTRIBUTE_1, analizeCard.getId(), attrId));
							return false;
						};
						foundFarCards = loadAllLinkedCardsByAttr(analizeCard.getId(), finalAttr);
						if (foundFarCards!=null)
							finalCards.addAll(foundFarCards);
						foundFarCards = finalCards;
					}
					if (finalCards.isEmpty() ) {
						logger.warn( MessageFormat.format(MSG_CARD_0_HAS_EMPTY_ATTR_1, ms.getLinkFarCards().toString(), attrId));
						return isConditionsContainNeq;
					};
				}
				else {
					// ���� �� ���� ��������� �� link-�� ������������ ������ ��������, ��������� �����, ��� finalCards = �����
					// �� ������ ����� ���������� �������� ��������, ��������� �����
					if (finalCards.size()==0){
						finalCards.addAll(ms.getLinkFarCards());
					}
					prevFinalCardsCount = finalCards.size();
					foundFarCards.clear();
					// �������� ������� ��� �������� � ���������� ��������� (� ��������� �� ������ ��� �� � ������� ��������������)
					final LinkCondition lk = (LinkCondition)linkConditions.toArray()[0];
					final boolean isOperEQ = AttributeSelector.CompareOperation.EQ.equals(lk.getCompOper());
					final boolean isOperNEQ = AttributeSelector.CompareOperation.NEQ.equals(lk.getCompOper());
					linkConditions.remove(0);
					List<ObjectId> referenceValues = IdUtils.stringToAttrIds(lk.getValues(), ReferenceValue.class);
					// ���� ���������� �������������� �������� ������� � ���� �������� ��� �����
					if ("_CARDTYPE".equals(attrId.getId())&&(finalAttr!=null)&&(finalAttr instanceof TypedCardLinkAttribute)){
						// ��������� �� ��������, ������� �� ������������� ������������ ���� �����
						for (Card finalCard: finalCards){
							final ObjectId finalCardType = ((TypedCardLinkAttribute)finalAttr).getCardType(finalCard.getId());
							if (finalCardType == null) // ������������� ��� ����� - ����������
								continue;
							if (isOperNEQ){	// ��� ������� ����� ��������� �������� � ������ ��������� � ������ �� � �������� ������, ���� ��� �����
								foundFarCards.add(finalCard);
							}
							for(ObjectId rv:referenceValues){
								// �������� ��������� �������� ����� �����
								final boolean isIdEquals = rv.getId().toString().equals( finalCardType.getId().toString() );
								if (isIdEquals) {
									if (isOperEQ) {
										foundFarCards.add(finalCard);
										break;
									}
									if (isOperNEQ){
										foundFarCards.remove(finalCard);
										break;
									}
								}
							}
						}
						//TODO: ������� �� �������� � �������������� ������� ������������� ������� �������� � ���� ���������
					} else if ("_CARDTYPE".equals(attrId.getId())&&(finalAttr!=null)&&(finalAttr instanceof BackLinkAttribute)){
						// ��������� �� ��������, ������� �� ������������� ������������ ���� �����
						ObjectId temp = new ObjectId(TypedCardLinkAttribute.class, ((BackLinkAttribute)finalAttr).getLinkSource().getId().toString());

						for (Card finalCard: finalCards){
							TypedCardLinkAttribute attr = (TypedCardLinkAttribute)finalCard.getAttributeById(temp);
							if (attr == null){	// ���� ������� �� ��� ������, �� ���������� �� ���� ����������, ������� ������ ��� �������
								Collection<ObjectId> linkSources = ((BackLinkAttribute)finalAttr).getLinkSources();
								if (linkSources!=null){
									for(ObjectId linkSourceId: linkSources){
										temp = new ObjectId(TypedCardLinkAttribute.class, linkSourceId.getId().toString());
										attr = (TypedCardLinkAttribute)finalCard.getAttributeById(temp);
										if (attr!=null)
											break;
									}
								}
							}
							if (attr == null){
								continue;

							}
							final ObjectId finalCardType = attr.getCardType(processCard.getId());
							if (finalCardType == null) // ������������� ��� ����� - ����������
								continue;
							if (isOperNEQ){	// ��� ������� ����� ��������� �������� � ������ ��������� � ������ �� � �������� ������, ���� ��� �����
								foundFarCards.add(finalCard);
							}
							for(ObjectId rv : referenceValues){
								if (isOperEQ &&
									rv.getId().toString().equals(finalCardType.getId().toString()))
								{	// �������� ��������� �������� ����� �����
									foundFarCards.add(finalCard);
									break;
								}
								if (isOperNEQ &&
									rv.getId().toString().equals(finalCardType.getId().toString()))
								{	// �������� ��������� �������� ����� �����
									foundFarCards.remove(finalCard);
									break;
								}
							}
						}
					} else if ("_TEMPLATE".equals(attrId.getId())){
						// ��������� �� ��������, ������� �� ������������� ��������
						for (Card finalCard: finalCards){ // �������� ��������� �������� ��������
							final ObjectId templateType = finalCard.getTemplate();
							if (isOperNEQ){	// ��� ������� ����� ��������� �������� � ������ ��������� � ������ �� � �������� ������, ���� ��� �� ���
								foundFarCards.add(finalCard);
							}
							for(ObjectId rv:referenceValues){
								final boolean isIdEquals = rv.getId().toString().equals( templateType.getId().toString() );
								if (isIdEquals) {
									if (isOperEQ) {
										foundFarCards.add(finalCard);
										break;
									}
									if (isOperNEQ){
										foundFarCards.remove(finalCard);
										break;
									}
								}
							}
						}
					} else if ("_STATE".equals(attrId.getId())){
						// ��������� �� ��������, ������� �� ������������� ���������
						for (Card finalCard: finalCards){
							final ObjectId stateType = finalCard.getState();
							if (lk.getCompOper().equals(AttributeSelector.CompareOperation.NEQ)){	// ��� ������� ����� ��������� �������� � ������ ��������� � ������ �� � �������� ������, ���� ��� �� ���
								foundFarCards.add(finalCard);
							}
							for(ObjectId rv:referenceValues){ 	// �������� �������� ...
								final boolean isIdEquals = rv.getId().toString().equals(stateType.getId().toString());
								if (isIdEquals) {
									if (isOperEQ){
										foundFarCards.add(finalCard);
										break;
									}
									if (isOperNEQ){
										foundFarCards.remove(finalCard);
										break;
									}
								}
							}
						}
					} else {
						// ���������� �� ���� ��������� � �������� ���������
						for (Card finalCard: finalCards){
							finalAttr = finalCard.getAttributeById(attrId);
							lk.setSrcCard(processCard);	// ������ �������� � ������� ��������, � �� � �������������
							lk.setDstAttrId(attrId);
							final List<BasePropertySelector> cardConditions = lk.generateConditions();
							boolean ok = false;
							if (cardConditions!=null){
								for (BasePropertySelector ps: cardConditions){
									ps.setBeanFactory(getBeanFactory());
									ok = ok | checkConditions(Collections.singletonList(ps), finalCard);
									if (ok) break; // ��� ��� true -> ������ ����� �� ���������
								}
								// ���� ���� �� ���� ������� �����������, �� ��������� �������� �������� � ������ ���������
								if (ok)
									foundFarCards.add(finalCard);
							}
						}
					}
				}
				ms.getLinkFarCards().clear();
				// ����� ���� ���, ������ �� ���� ��������� �� ����� ���������, � ��������� ��� ��� ��������� �������, ������ �������� ������ ��������� ��������
				if (foundFarCards != null)
					ms.getLinkFarCards().addAll(foundFarCards);
			}
		}
		// ���� ��������� ����������� ������� � �������� ���� ��� ����
		if ( (ms.getConditionIndex() == this.multiConditions.size()-1) && "MEDO".equals(specialFlag) ){
			return (ms.getLinkFarCards().size()==prevFinalCardsCount);	// ���������� �������� � ���������� �������� ���������� ������� ������ ��������� � ����������� �������� � ������������� �������
		}
		return !ms.getLinkFarCards().isEmpty();	// �������� ���� (� ��� ������� ���������� ����� ���� ��������) ������� ������� � ��� ������, ���� �������� ������ ��������� �������� �� ������
	} // for
	/** �������� ������ LinkCondition ��� ����������� �������
	 *
	 * @param list - ������ ��������� ��� �������� � ����� �����
	 * @return ���� LinkCondition
	 */
	protected List<LinkCondition> createLinkConditionList(List<String> list){
		if (list == null || list.isEmpty())
			return null;
		final List<LinkCondition> result = new ArrayList<LinkCondition>();
		for(String cond: list)
			result.add(new LinkCondition(cond));
		return result;
	}

	/**
	 * �������� ������� ��������� ��������� � ������������ ������ �������
	 * @param list
	 * @return
	 */
	protected static boolean containOperand(List<LinkCondition> list,
			AttributeSelector.CompareOperation oper )
	{
		if (list != null && oper != null) {
			for(LinkCondition cond: list) {
				if (oper.equals(cond.getCompOper()))
					return true;
			} // for
		}
		return false;
	}

	/**
	 * �������� ������� ����������� � ������������ ������ �������
	 * @param list
	 * @return
	 */
	protected static boolean containNeqOperand( List<LinkCondition> list)
	{
		return containOperand(list, AttributeSelector.CompareOperation.NEQ);
	}

	/**
	 * ������ ������ ��������� �������� � ������� � sourceCardIndex
	 * @param sourceCardIndex - ������ �������
	 * @return
	 */
	protected List<Card> getFarCardsFromMultiConditions(int sourceCardIndex){
		if (multiConditions.isEmpty()||multiConditions.size()<sourceCardIndex)
			return null;
		if (multiConditions.get(sourceCardIndex).getConditionIndex()==sourceCardIndex)
			return multiConditions.get(sourceCardIndex).getLinkFarCards();
		for(MultiCondition mc: multiConditions){
			if(mc.getConditionIndex()==sourceCardIndex)
				return mc.getLinkFarCards();
		}
		return null;
	}

	protected void generateStartCardsList(MultiCondition ms){
		if (ms.startCardsIndex >= ms.conditionIndex)
			return;
		if (ms.startCardsIndex>multiConditions.size())
			return;
		if (multiConditions.get(ms.startCardsIndex).getConditionIndex()==ms.startCardsIndex){
			ms.getStartCardsList().addAll(multiConditions.get(ms.startCardsIndex).getLinkFarCards());
			return;
		}
		for (MultiCondition m: multiConditions){
			if (m.getConditionIndex()==ms.getStartCardsIndex())
			{
				ms.getStartCardsList().addAll(m.getLinkFarCards());
				return;
			}
		}
	}
	/**
	 * ��������������� ����� ��� ��������� ������ PropertySelector �������� �� ������� ����������
	 * <�������� ��������>, <������ �������>
	 * ���������, ����� � ���������� ��� ������������ �������� ������� ��� ������� ������������� �������� ���������
	 * ������������� �������� �������� � ������� ���������
	 * @author ynikitin
	 *
	 */
	protected class LinkCondition{
		private String values;		// ������ ����������� �������� (� ����� ���������) ����� ������� � ���������� ��������� ����� ������ ���������
		private Card srcCard;		// ��������, � ������� ������ �������� ����������� ��������� (���� ���� ��������� �� �� ���������, � �� ������������� �������� � ��������
		private ObjectId dstAttrId;	// ������� � ������������� �������� ��� ���������
		private AttributeSelector.CompareOperation compOper = AttributeSelector.CompareOperation.EQ;
		private String compOperSymbol;
		public LinkCondition(){
			super();
		}

		public LinkCondition(String values, Card srcCard, ObjectId dstAttrId) {
			this(values);
			this.srcCard = srcCard;
			this.dstAttrId = dstAttrId;
		}

		public LinkCondition(String values) {
			super();
			this.values = values.trim();
			this.compOperSymbol = AttributeSelector.containOper(values);				// �������� ��������� (�������� ������ �������� � ������ ������� ��� ���������)
			this.compOper = AttributeSelector.findOper(compOperSymbol);					// ���������� �������� ��������� (������������)
			if (compOperSymbol != null)
				this.values = this.values.replaceAll(compOperSymbol, "");					// ������� ���� ��������� �� ���� ������ ��������
		}

		public AttributeSelector.CompareOperation getCompOper() {
			return compOper;
		}

		/**
		 * ��������� ������ PropertySelector, ������� � ���������� ����� �������������� ��� ������� ������������ ��������
		 * @return
		 * @throws DataException
		 */
		@SuppressWarnings({ "null", "unchecked" })
		public List<BasePropertySelector> generateConditions() throws DataException{
			if ((this.values == null) || (this.values.length()==0)) {
				return null;
			}
			if (this.dstAttrId == null) {
				return null;
			}
			if (this.srcCard == null)
				return null;

			final List<BasePropertySelector> result = new ArrayList<BasePropertySelector>();	// ������������ ������ �������
			final String[] sIds = values.split("[,]");
			if (sIds.length > 0) {
				result.clear();
				for (String value: sIds) {
					if (value == null || value.length() == 0) continue;
					try {
						String attrValue = value.trim();
						// ���� attrValue - ������� � ��������� ��������
						if (attrValue.contains(PathAttributeDescriptior.REG_ATTR_SEPARATOR)) {
							// ����� ���������� �������� �������� ���������� �������� � ��������� � ������� ��������
							final PathAttributeDescriptior id = makeObjectId(attrValue);
							List<Attribute> foundAttributes = getAttributeForDescriptor(id);
							if (foundAttributes==null||foundAttributes.isEmpty())
								return null;
							for(Attribute a: foundAttributes){
								if (a.getStringValue()!=""){
									attrValue = compOperSymbol+a.getStringValue();
									final AttributeSelector selector = AttributeSelector.createSelector(dstAttrId.getId() + attrValue, dstAttrId.getType());
									result.add(selector);
								}
							}
							continue;
						}
						final ObjectId nameAttrId = IdUtils.smartMakeAttrId(attrValue, Attribute.class);
						final Attribute attrId = srcCard.getAttributeById(nameAttrId);
						if (attrId != null)
							attrValue = compOperSymbol+ attrId.getStringValue();
						else
							attrValue = compOperSymbol+ attrValue;
						if (attrValue!=null && attrValue.length()>0){
							final AttributeSelector selector = AttributeSelector.createSelector(dstAttrId.getId() + attrValue, dstAttrId.getType());
							result.add(selector);
						}
					} catch (DataException ex) {
						throw new DataException(ex);
					}
				}
				return result;
			}
			return null;
		}

		/**
		 * ���������� ������� ����������� �� ���� ��������, ��� ��������� ������� ����������� ������� ��������
		 * @param id
		 * @return ������ ����������� ��������� ���������
		 * @throws DataException
		 */
		@SuppressWarnings({ "null", "synthetic-access" })
		private List<Attribute> getAttributeForDescriptor(PathAttributeDescriptior id)
			throws DataException
		{
			if (id==null) {
				return null;
			}

			final List<Attribute> result = new ArrayList<Attribute>();
			// result.clear();

			// ������������ ������ ��������� ��������
			final List<Card> linkCards = new ArrayList<Card>(10);
			linkCards.add(srcCard);	// ��������� �������� -  �������

			List<Card> foundFarCards = new ArrayList<Card>(10);
			// foundFarCards.clear();

			final Iterator<ObjectId> iterIds = id.getAttrIds().iterator();
			Attribute finalAttr = null;
			final List<Card> finalCards = new ArrayList<Card>();
			// ����� �� ����� ���� ����������
			while (iterIds.hasNext()) {
				final ObjectId attrId = iterIds.next();
				if (iterIds.hasNext()){ // ��� �� ����� -> �������� ������  ��������...
					// �������� �� ���� ��������� ��������� ������ ��� �������� �����
					finalCards.clear();
					for(Card analizeCard: linkCards){
						finalAttr = analizeCard.getAttributeById(attrId);
						if (finalAttr == null) {
							logger.warn( MessageFormat.format(MSG_CARD_0_HAS_NO_ATTRIBUTE_1, analizeCard.getId(), attrId));
							return null;
						};
						foundFarCards = loadAllLinkedCardsByAttr(analizeCard.getId(), finalAttr);
						if (foundFarCards!=null)
							finalCards.addAll(foundFarCards);
						foundFarCards = finalCards;
						if ( finalCards == null || finalCards.isEmpty() ) {
							logger.warn( MessageFormat.format(MSG_CARD_0_HAS_EMPTY_ATTR_1, analizeCard.getId(), attrId));
							return result;
						};
					}
				}
				else {
					// ���� �� ���� ��������� �� link-�� ������������ ������ ��������, ��������� �����, ��� finalCards = �����
					// �� ������ ����� ������������� �������� ��������, ��������� �����
					if (finalCards.size()==0){
						finalCards.addAll(linkCards);
					}

					// ���������� �� ���� ��������� � ������� ������ ��������
					for (Card finalCard: finalCards){
						finalAttr = finalCard.getAttributeById(attrId);
						if (finalAttr!=null)
							result.add(finalAttr);
					}
				}
				linkCards.clear();
				// ����� ���� ���, ������ �� ���� ��������� �� ����� ��������� ������ �������� ������ ��������� ��������
				linkCards.addAll(foundFarCards);
			}
			return result;
		}

		public String getCompOperSymbol() {
			return compOperSymbol;
		}

		public void setSrcCard(Card srcCard) {
			this.srcCard = srcCard;
		}

		public void setDstAttrId(ObjectId dstAttrId) {
			this.dstAttrId = dstAttrId;
		}

		public String getValues() {
			return values;
		}
	}

	/**
	 * ��������������� ����� ��� �������� ������ n-�� �������, ������ �������� ��������� ������ ���������� � ����
	 * <%������� � ������� ��� ��������� ��������%><%�������� ���������%><%������������ �������� ��� ������� ��������, ��� ������� ���������� ���������%>
	 * @author ynikitin
	 *
	 */
	protected class MultiCondition{
		/**
		 * ������ �������� ������� ()
		 */
		int conditionIndex;

		/**
		 * ������ �������, ������ ��������� �������� �������� ������� �� ��������� ������ �������� �������� �������
		 */
		int startCardsIndex = 0;
		/**
		 * ��������� ������ ��������, ��� �������� ����������� ������ ��������
		 */
		private List<Card> startCardsList;

		/**
		 * ������ ��������� ��� ������� (��� � �������, ��� � � ��������� ���������)
		 */
		private final List<PathAttributeDescriptior> attrIdList;

		/**
		 * ��� �������, ������� ���� ��������� �� ��������� ������ �������� ��������� ��������
		 */
		private final List<String> stringConditions;

		/**
		 * ������ ��������, ��������� � �������� ������� �������
		 */
		private List<Card> linkFarCards;		// ��������, ��������� � �������� ����������� ��������������� �� ���� ��������

		public MultiCondition(int conditionIndex, int startCardsIndex) {
			super();
			this.conditionIndex = conditionIndex;
			this.startCardsIndex = startCardsIndex;
			this.linkFarCards = new ArrayList<Card>();
			this.stringConditions = new ArrayList<String>();
			this.attrIdList = new ArrayList<PathAttributeDescriptior>();
			this.startCardsList = new ArrayList<Card>();
		}

		public int getStartCardsIndex() {
			return this.startCardsIndex;
		}

		public List<BasePropertySelector> getConditions() {
			return conditions;
		}

		public int getConditionIndex() {
			return conditionIndex;
		}

		public void setConditionIndex(int conditionIndex) {
			this.conditionIndex = conditionIndex;
		}

		public List<Card> getStartCardsList() {
			return startCardsList;
		}

		public List<PathAttributeDescriptior> getAttrIdList() {
			return attrIdList;
		}

		public List<String> getStringConditions() {
			return stringConditions;
		}

		public List<Card> getLinkFarCards() {
			return linkFarCards;
		}

		public void setStartCardsIndex(int startCardsIndex) {
			this.startCardsIndex = startCardsIndex;
		}

	}
}
