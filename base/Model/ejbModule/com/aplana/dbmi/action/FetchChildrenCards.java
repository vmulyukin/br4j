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

import java.util.List;
import java.util.Set;

import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;

/**
 * Action used to fetch children cards of given {@link Card} object
 * Parent-Children relation is specified by providing appropriate {@link CardLinkAttribute} identifier.
 * Result is returning in form of {@link SearchResult} object.
 * @author DSultanbekov
 * @see FetchParentCards
 */
public class FetchChildrenCards implements Action {
	private static final long serialVersionUID = 2L;
	private ObjectId cardId;
	private ObjectId linkAttributeId;
	private Set<ObjectId> linkAttrTypes;
	private boolean reverseLink;
	private List columns;

	/**
	 * Gets identifier of parent {@link Card} object
	 * @return identifier of parent {@link Card} object
	 */
	public ObjectId getCardId() {
		return cardId;
	}

	/**
	 * Sets identifier of parent {@link Card} object
	 * @param cardId identifier of parent {@link Card} object
	 */
	public void setCardId(ObjectId cardId) {
		this.cardId = cardId;
	}
	
	/**
	 * Gets columns of resulting {@link SearchResult} object
	 * @return list of {@Link SearchResult.Column} objects
	 */
	public List getColumns() {
		return columns;
	}

	/**
	 * Sets desired set of {@link SearchResult} columns
	 * @param columns list of {@Link SearchResult.Column} objects
	 */
	public void setColumns(List columns) {
		this.columns = columns;
	}

	/**
	 * Gets identifier of {@link CardLinkAttribute} object which specifies 
	 * parent-children relationship between cards
	 * @return identifier of {@link CardLinkAttribute} object which specifies 
	 * parent-children relationship between cards
	 */
	public ObjectId getLinkAttributeId() {
		return linkAttributeId;
	}

	/**
	 * Sets identifier of {@link CardLinkAttribute} object which specifies 
	 * parent-children relationship between cards 
	 * @param linkAttributeId identifier of {@link CardLinkAttribute} object which specifies 
	 * parent-children relationship between cards
	 * @throws IllegalArgumentException if linkAttributeId is null or if it have wrong type
	 */
	public void setLinkAttributeId(ObjectId linkAttributeId) {
		if (linkAttributeId == null || !CardLinkAttribute.class.equals(linkAttributeId.getType())) {
			throw new IllegalArgumentException("CardLinkAttribute identifier is required");
		}
		this.linkAttributeId = linkAttributeId;
	}

	/**
	 * Checks if given {@link #getLinkAttributeId() linkAttribute} specifies link from parent to children
	 * or vice versa
	 * @return true if {@link #getLinkAttributeId() linkAttributeId} represents singlevalued {@link CardLinkAttribute}
	 * in children {@link Card cards} pointing to single parent {@link Card},<br> 
	 * false if {@link #getLinkAttributeId() linkAttributeId} represents multivalued {@link CardLinkAttribute}
	 * in parent {@link Card} pointing to several children {@link Card cards}   
	 */
	public boolean isReverseLink() {
		return reverseLink;
	}

	/**
	 * Sets direction of given {@link #getLinkAttributeId() linkAttribute} is it specifies relation from
	 * children to parent or from parent to children
	 * @param reverse  true if {@link #getLinkAttributeId() linkAttributeId} represents singlevalued {@link CardLinkAttribute}
	 * in children {@link Card cards} pointing to single parent {@link Card},<br> 
	 * false if {@link #getLinkAttributeId() linkAttributeId} represents multivalued {@link CardLinkAttribute}
	 * in parent {@link Card} pointing to several children {@link Card cards}
	 */
	public void setReverseLink(boolean reverse) {
		this.reverseLink = reverse;
	}

	/**
	 * Always returns SearchResult.class;
	 * @return SearchResult.class;
	 */	
	public Class getResultType() {
		return SearchResult.class;
	}

	public Set<ObjectId> getLinkAttrTypes() {
		return linkAttrTypes;
	}

	public void setLinkAttrTypes(Set<ObjectId> linkAttrTypes) {
		this.linkAttrTypes = linkAttrTypes;
	}
}
