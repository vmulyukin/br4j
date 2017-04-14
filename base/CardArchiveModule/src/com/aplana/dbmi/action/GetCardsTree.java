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
package com.aplana.dbmi.action;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.DatedTypedCardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.TypedCardLinkAttribute;
/**
 * <b>Action used to construct {@link com.aplana.dbmi.model.Card cards} tree</b><br>
 * Returns <b>Set</b> of card IDs in Long as result.
 * Methods:<br>
 * <ul>
 * <li>{@link #setCardId(ObjectId)}</li>
 * <li>{@link #setIgnoredCards(Set)}</li>
 * <li>{@link #setLinkAttrs(Set)}</li>
 * <li>{@link #setTemplates(Set)}</li>
 * <li>{@link #setReverse(Fields, boolean)}</li>
 * <li>{@link #setNesting(int)}</li>
 * </ul>
 *
 */ 
public class GetCardsTree implements ObjectAction {

	private static final long serialVersionUID = 1L;

	// cardId ������� ������
	private ObjectId cardId;
	// ����� �������� ������� �� ��������� � ������
	private Set<ObjectId> linkAttrIds = new HashSet<ObjectId>();
	// ����� ����� �������� ��������� �� ������
	private Set<ObjectId> templateIds = new HashSet<ObjectId>();
	// ����� ����� �� �������� � ������
	private Set<ObjectId> ignoredCardIds = new HashSet<ObjectId>();
	// ������� ������, � �������� ������� ��������� ����� (0 - ������� �� ������) 
	private int nesting = -1;

	// ��� ������� ����, ��������� �� �� �� ������������ ��������� ����������
	// ��� ��������� ����������� ���������
	/**
	 * "Fields.LINKATTRS" - used to determine the parameters specified in the
	 * {@link com.aplana.dbmi.action.GetCardsTree#setLinkAttrs(Set)
	 * setLinkAttrs(Set)} <br>
	 * "Fields.TEMPLATES" - used to determine the parameters specified in the
	 * {@link com.aplana.dbmi.action.GetCardsTree#setLinkAttrs(Set)
	 * setTemplates(Set)}
	 */
	public static enum Fields {
		LINKATTRS, TEMPLATES
	}

	// �� ��������� �� ��������� ��������� �� ���� ��������� ���������
	private Map<Fields, Boolean> reverse;
	{
		reverse = new HashMap<Fields, Boolean>();
		reverse.put(Fields.LINKATTRS, true);
		reverse.put(Fields.TEMPLATES, true);
	}

	/**
	 * {@link #setLinkAttrs(Set)} This method sets, how to use parameters of the
	 * action. If <i>reverse</i> is <b>true</b>, then parameter <i>field</i>
	 * will be excluded. <br>
	 * For example, if the "LINKATTR" will be "<b>true</b>", the attributes
	 * specified in the {@link #setLinkAttrs(Set)} will be excluded from all
	 * possible attributes that tree is constructed, otherwise the tree will be
	 * constructed only on those attributes. <br>
	 * By default all parameters will be <b>excluded</b> (then there is all
	 * reverse parameters set to true)<br>
	 * Correspondence between the parameter and it's string value specified in
	 * the {@link Fields}.
	 * 
	 * @see Fields
	 * @param field {@link Fields} object
	 * @param reverse <b>true</b> or <b>false</b>
	 */
	public void setReverse(Fields field, boolean reverse) {
		this.reverse.put(field, reverse);
	}

	public boolean isReverse(Fields field) {
		return reverse.get(field);
	}

	/**
	 * This method sets identifier of {@link com.aplana.dbmi.model.Card Card}
	 * which begins tree
	 * 
	 * @param cardId identifier of {@link com.aplana.dbmi.model.Card Card}
	 */
	public void setCardId(ObjectId cardId) {
		this.cardId = cardId;
	}

	/**
	 * Returns the identifier of {@link com.aplana.dbmi.model.Card Card} which
	 * begins tree
	 * 
	 * @return identifier of {@link com.aplana.dbmi.model.Card Card}
	 */
	public ObjectId getCardId() {
		return cardId;
	}

	/**
	 * This method sets identifiers of {@link CardLinkAttribute} on cards which
	 * will be <i>ignored</i> <b>or</b> <i>used to</i> build a tree cards
	 * 
	 * @see #setReverse(Fields, boolean)
	 * 
	 * @param linkAttrs identifiers of {@link CardLinkAttribute}
	 */
	public void setLinkAttrs(Set<ObjectId> linkAttrs) {
		for (ObjectId linkAttr : linkAttrs) {
			if (linkAttr == null
					|| !(CardLinkAttribute.class.equals(linkAttr.getType()) || TypedCardLinkAttribute.class.equals(linkAttr.getType())
							|| DatedTypedCardLinkAttribute.class.equals(linkAttr.getType()))) {

				throw new IllegalArgumentException("CardLinkAttribute or TypedCardLinkAttribute identifier is required");
			}
		}
		this.linkAttrIds = linkAttrs;
	}

	/**
	 * Returns identifiers of {@link CardLinkAttribute} on cards which will be
	 * ignored <b>or</b> used to build a tree cards
	 * 
	 * @see #setReverse(Fields, boolean)
	 * @return identifiers of {@link CardLinkAttribute}
	 */
	public Set<ObjectId> getLinkAttrs() {
		return this.linkAttrIds;
	}

	/**
	 * This method sets identifiers of {@link com.aplana.dbmi.model.Template
	 * Templates}, whose cards will <i>not be</i> included in a tree, <b>or</b>
	 * whose cards will <i>be</i> included in a tree
	 * 
	 * @see #setReverse(Fields, boolean)
	 * 
	 * @param templates identifiers of {@link com.aplana.dbmi.model.Template
	 *  Templates}
	 */
	public void setTemplates(Set<ObjectId> templates) {
		this.templateIds = templates;
	}

	/**
	 * Returns identifiers of {@link com.aplana.dbmi.model.Template Templates},
	 * whose cards will <i>not be</i> included in a tree, <b>or</b> whose cards
	 * will <i>be</i> included in a tree
	 * 
	 * @see #setReverse(Fields, boolean)
	 * 
	 * @return identifiers of {@link com.aplana.dbmi.model.Template Templates}
	 */
	public Set<ObjectId> getTemplates() {
		return this.templateIds;
	}

	/**
	 * This method sets identifiers of {@link com.aplana.dbmi.model.Card Card},
	 * which will not be included on the tree
	 * 
	 * @param ignoredCards identifiers of {@link com.aplana.dbmi.model.Card Card}
	 */
	public void setIgnoredCards(Set<ObjectId> ignoredCards) {
		this.ignoredCardIds = ignoredCards;
	}

	/**
	 * Returns identifiers of {@link com.aplana.dbmi.model.Card Card}, which
	 * will not be included on the tree
	 * 
	 * @return identifiers of {@link com.aplana.dbmi.model.Card Card}
	 */
	public Set<ObjectId> getIgnoredCards() {
		return this.ignoredCardIds;
	}
	
	/**
	 * Sets the depth of the tree from which return a result. <br> Default is '-1' (all tree).
	 */
	public void setNesting(int nesting) {
		this.nesting = nesting;
	}
	
	/**
	 * Returns the depth of the tree from which return a result. <br> Default is '-1' (all tree).
	 */
	public int getNesting() {
		return this.nesting;
	}
	
	public Class<?> getResultType() {
		return Set.class;
	}

	public ObjectId getObjectId() {
		return cardId;
	}

}
