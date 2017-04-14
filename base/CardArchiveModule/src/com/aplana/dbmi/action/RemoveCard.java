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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.TypedCardLinkAttribute;

/**
 * This action used to remove {@link com.aplana.dbmi.model.Card Card} and its
 * descendants from the database.<br>
 * The action removes {@link CardLinkAttribute linked} cards <b>specified in:</b>:
 * <ul>
 * <li>cards and its descendants that are defined by <b>templates</b> which specified in
 * {@link #setTemplateIds(Set)}</li>
 * <li><b>cards</b> (but not its descendants) which specified in {@link #setIgnoredCardIds(Set) }</li>
 * <li>cards that <b>refer to attributes</b> on the cards which
 * specified in {@link #setLinkAttrIds(Set)}</li> 
 * </ul>
 * and all attributes on the root card <b>except</b>:
 * <ul>
 * <li><b>attributes</b> which specified in {@link #setSavedAttrIds(Set)}.</li>
 * </ul>
 * Also action will verify that <b>no
 * one else</b> is referred to the cards which will be removed.<br>
 * Files of all the cards, which are defined by file <b>templates</b> ,specified in
 * {@link #setFileTemplateIds(Set)}, will be removed by
 * {@link com.aplana.dbmi.action.RemoveFile RemoveFile} action. By default this template is
 * '<b>File</b>' (284).<br>
 * Returns removed card ids.
 */
public class RemoveCard implements ObjectAction {

	private static final long serialVersionUID = 1L;
	// cardId ��������� �������� -- ������� ������
	private ObjectId cardId;
	// ����� �-�������� ������������/�� ������������ ��� ���������� ������ 
	private Set<ObjectId> linkAttrIds = new HashSet<ObjectId>();
	// �������� ����� �������� ��������/�� �������� � ������ 
	private Set<ObjectId> templateIds = new HashSet<ObjectId>();
	// ����� �������� ������� �� ������� �� attribute_value
	private Set<ObjectId> rootSavedAttrIds = new HashSet<ObjectId>();
	// ����� �������� �� �������� � ������
	private Set<ObjectId> ignoredCardIds = new HashSet<ObjectId>();
	// ���������, ����� �� �������� ������ ������ ����� �� ��������
	private boolean checkLinks = true;
	// true - ������������ ��� ��������� Link-�������� ����� ��������� (linkAttrIds)��� ���������� ������.
	// false - ������������ ������ ��������� (linkAttrIds) Link-�������� ��� ���������� ������. 
	private boolean excludeLinkAttrIds = true;
	// true - �������� ������ ��������� (templateIds) �������� �� �������� � ������.
	// false - �������� �������� ������ ��������� (templateIds) �������� � ������. 
	private boolean excludeTemplateIds = true;
	private boolean removeRootCardHistosy = false;
	
	// ������ ������� ��������
	private Set<ObjectId> fileTemplateIds = new HashSet<ObjectId>();
	{
		this.fileTemplateIds.add(ObjectId.predefined(Template.class, "jbr.file"));
	}
	// ������� ��� ��� �������� �����
	private boolean removeRootCard = true;
	/**
	 * This method sets identifier of {@link com.aplana.dbmi.model.Card Card}
	 * which will be removed
	 * 
	 * @param cardId identifier of {@link com.aplana.dbmi.model.Card Card}
	 */
	public void setCardId(ObjectId cardId) {
		this.cardId = cardId;
	}

	/**
	 * Returns the identifier of {@link com.aplana.dbmi.model.Card Card} which
	 * will be removed
	 * 
	 * @return identifier of {@link com.aplana.dbmi.model.Card Card}
	 */
	public ObjectId getCardId() {
		return cardId;
	}

	/**
	 * This method sets identifiers of {@link CardLinkAttribute} on cards which
	 * will be ignored <b>or</b> will be used to construct cards tree
	 * 
	 * @param linkAttrIds
	 *            identifiers of {@link CardLinkAttribute}
	 * @see {@link #setExcludeLinkAttrIds(boolean)}
	 *            
	 * 
	 */
	public void setLinkAttrIds(Set<ObjectId> linkAttrIds) {
		for (ObjectId linkAttrId : linkAttrIds) {
			if (linkAttrId == null
					|| !(CardLinkAttribute.class.equals(linkAttrId.getType()) || TypedCardLinkAttribute.class.equals(linkAttrId.getType()))) {

				throw new IllegalArgumentException("CardLinkAttribute or TypedCardLinkAttribute identifier is required");
			}
		}
		this.linkAttrIds = linkAttrIds;
	}	
	
	/**
	 * Returns the identifiers of {@link CardLinkAttribute} on cards which
	 * will be ignored or used to construct cards tree 
	 * (see boolean parameter on {@link #setLinkAttrIds(Set, boolean...)} )
	 * 
	 * @return identifiers of {@link CardLinkAttribute}
	 */
	public Set<ObjectId> getLinkAttrIds() {
		return this.linkAttrIds;
	}

	/**
	 * This method sets identifiers of {@link com.aplana.dbmi.model.Template
	 * Templates} whose cards will not be removed or whose cards will be removed
	 * @see #setExcludeTemplateIds(boolean)
	 * @param templateIds identifiers of {@link com.aplana.dbmi.model.Template Template} objects
	 */
	
	public void setTemplateIds(Set<ObjectId> templateIds) {
		this.templateIds = templateIds;
	}
	
	/**
	 * Returns identifiers of {@link com.aplana.dbmi.model.Template Template} objects
	 * whose cards will not be removed or whose cards will be removed  
	 * @see #getExclusions()
	 * @return identifiers of {@link com.aplana.dbmi.model.Template Template} objects
	 */
	public Set<ObjectId> getTemplateIds() {
		return this.templateIds;
	}

	/**
	 * This method sets identifiers of {@link com.aplana.dbmi.model.Attribute Attribute}
	 * objects on root card which will not be removed
	 * 
	 * @param savedAttrIds identifiers of {@link com.aplana.dbmi.model.Attribute Attribute} objects
	 */
	public void setSavedAttrIds(Set<ObjectId> savedAttrIds) {
		this.rootSavedAttrIds = savedAttrIds;
	}

	/**
	 * Returns identifiers of {@link com.aplana.dbmi.model.Attribute Attribute} objects
	 * on root card which will not be removed
	 * 
	 * @return identifiers of {@link com.aplana.dbmi.model.Attribute Attribute} objects
	 */
	public Set<ObjectId> getSavedAttrIds() {
		return this.rootSavedAttrIds;
	}

	/**
	 * This method sets identifiers of {@link com.aplana.dbmi.model.Card Card},
	 * which will not be removed
	 * 
	 * @param ignoredCardIds identifiers of {@link com.aplana.dbmi.model.Card Card}
	 */
	public void setIgnoredCardIds(Set<ObjectId> ignoredCardIds) {
		this.ignoredCardIds = ignoredCardIds;
	}

	/**
	 * Returns identifiers of {@link com.aplana.dbmi.model.Card Card}, which
	 * will not be removed
	 * 
	 * @return identifiers of {@link com.aplana.dbmi.model.Card Card}
	 */
	public Set<ObjectId> getIgnoredCardIds() {
		return this.ignoredCardIds;
	}
/*
	/**
	 * This method determines whether or not to check that no one refers to the
	 * cards which will be removed. By default is true
	 * 
	 * @param checkLinks <b>true</b> if validation is needed, otherwise <b>false</b>
	 
	public void setCheckLinks(boolean checkLinks) {
		this.checkLinks = checkLinks;
	}
*/
	/**
	 * 16.11.11 - returns always 1 because the check is always performed.<br>
	 * Returns <b>true</b> if validation of that no one refers to the cards
	 * which will be removed is needed, otherwise <b>false</b>. By default is true
	 * 
	 * @return true or false
	 */
	public boolean isCheckLinks() {
		return this.checkLinks;
	}
	/** Returns flag that define how to use <i>linkAttrIds</i> parameter of the action.<br> 
	 * If <i>excludeLinkAttrIds</i> is <b>true</b>, any available {@link CardLinkAttribute}
	 * and its descendants will be used to build removed cards tree except those in <i>linkAttrIds</i>, 
	 * <br>otherwise then only <i>linkAttrIds</i>
	 * will be used to build removed cards tree.<br> 
	 * Default value is {@code true}.
	 * @return flag
	 */
	public boolean isExcludeLinkAttrIds() {
		return excludeLinkAttrIds;
	}

	 /**
	 * Sets flag that define how to use <i>linkAttrIds</i> parameter of the action.<br> 
	 * If <i>excludeLinkAttrIds</i> is <b>true</b>, any available {@link CardLinkAttribute}
	 * and its descendants will be used to build removed cards tree except those in <i>linkAttrIds</i>, 
	 * <br>otherwise then only <i>linkAttrIds</i>
	 * will be used to build removed cards tree.<br> 
	 * Default value is {@code true}.
	 */	
	public void setExcludeLinkAttrIds(boolean excludeLinkAttrIds) {
		this.excludeLinkAttrIds = excludeLinkAttrIds;
	}
	/**
	 * Returns flag that define how to use <i>templateIds</i> parameter of the action. 
	 * If <i>excludeTemplateIds</i> is {@code true}, then only cards with <i>templateIds</i>
	 * will not be included in removing cards tree, otherwise in removing cards tree, will be 
	 * included only cards with <i>templateIds</i>. <br>
	 * Default value is {@code true}.
	 * @return flag
	 */
	public boolean isExcludeTemplateIds() {
		return excludeTemplateIds;
	}

	 /**
	 * Sets flag that define how to use <i>templateIds</i> parameter of the action. 
	 * If <i>excludeTemplateIds</i> is {@code true}, then only cards with <i>templateIds</i>
	 * will not be included in removing cards tree, otherwise in removing cards tree, will be 
	 * included only cards with <i>templateIds</i>. Default value is {@code true}.
	 */
	public void setExcludeTemplateIds(boolean excludeTemplateIds) {
		this.excludeTemplateIds = excludeTemplateIds;
	}

	/**
	 * This method sets identifiers of {@link com.aplana.dbmi.model.Template
	 * Templates} used to <b>file</b> cards. By default use '<b>File</b>' (284)
	 * template.
	 * 
	 * @param fileTemplateIds identifiers of 
	 * {@link com.aplana.dbmi.model.Template Template} objects
	 */
	public void setFileTemplateIds(Set<ObjectId> fileTemplateIds) {
		if (!fileTemplateIds.isEmpty())
			this.fileTemplateIds = fileTemplateIds;
	}

	/**
	 * Returns identifiers of {@link com.aplana.dbmi.model.Template Template} objects
	 * used to <b>file</b> cards (by default '<b>File</b>' (284))
	 * 
	 * @return identifiers of {@link com.aplana.dbmi.model.Template Template} objects	 *
	 */
	public Set<ObjectId> getFileTemplateIds() {
		return this.fileTemplateIds;
	}
	/**
	 * Returns exclusion types [attributes exclusion, templates exclusion]. 
	 * By default both exclusions is <i>true</i>
	 * @return [attributes exclusion, templates exclusion]
	 */
	public boolean[] getExclusions(){
		return new boolean[] {this.excludeLinkAttrIds, this.excludeTemplateIds};
	}
	
	/**
	 * Sets flag that define remove {@link #setCardId(ObjectId) root} card with
	 * descendants or remove only descendants.<br>
	 * If <i>removeRootCard</i> is {@code false}, then only descendants will be
	 * removed.<br>
	 * Default value is {@code true}.
	 * 
	 * @param removeRootCard
	 *            false if only descendants removing is needed.
	 */
	public void setRemoveRootCard(boolean removeRootCard) {
		this.removeRootCard = removeRootCard;
	}

	/**
	 * Returns flag that define remove {@link #setCardId(ObjectId) root} card
	 * with descendants or remove only descendants.<br>
	 * If <i>removeRootCard</i> is {@code false}, then only descendants will be
	 * removed.<br>
	 * Default value is {@code true}.
	 * 
	 * @return false if only descendants removing is needed.
	 */
	public boolean isRemoveRootCard() {
		return removeRootCard;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Class<Set> getResultType() {
		return Set.class;
	}

	@Override
	public ObjectId getObjectId() {
		return cardId;
	}

	public boolean isRemoveRootCardHistosy() {
		return removeRootCardHistosy;
	}

	public void setRemoveRootCardHistosy(boolean removeRootCardHistosy) {
		this.removeRootCardHistosy = removeRootCardHistosy;
	}
}
