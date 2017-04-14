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

import java.util.Collection;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.BackLinkAttribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;

/**
 * {@link Action} implementation used to get list of all cards referencing
 * given {@link Card} via 
 * {@link com.aplana.dbmi.model.CardLinkAttribute card link attributes}.<br>
 * To use this action it is necessary to define {@link #setCard(ObjectId) card} object whose
 * children should be fetched and {@link BackLinkAttribute}
 * defining parent-children relation between cards.
 * <br>
 * Returns {@link SearchResult} object containing information about all children cards
 * of {@link #getCard() given card} object.  
 */
public class ListProject implements ObjectAction<SearchResult> 
{
	private static final long serialVersionUID = 3L;
	private ObjectId card;
	private ObjectId attribute = Attribute.ID_CHILDREN;
	private Collection columns;
	private boolean rememberLastParent = false;
	
	/**
	 * Default constructor
	 * By default {@link #setAttribute attribute} property is set to {@link Attribute#ID_CHILDREN} value
	 */
	public ListProject() {
	}
	
	/**
	 * Creates new action instance and initializes its {@link #setCard(ObjectId) card} property}
	 * with given value.<br>
	 * Value of {@link #setAttribute attribute} property is set to {@link Attribute#ID_CHILDREN}.  
	 * @param card identifier of parent card whose children needs to be fetched
	 * @throws IllegalArgumentException if given identifier is not a {@link Card} identifier 
	 */
	public ListProject(ObjectId card) {
		setCard(card);
	}

	/**
	 * Gets identifier of {@link Card} object whose children needs to be fetched
	 * @return identifier of {@link Card} object whose children needs to be fetched
	 */
	public ObjectId getCard() {
		return card;
	}

	/**
	 * Sets identifier of {@link Card} object whose children needs to be fetched
	 * @param card identifier of {@link Card} object whose children needs to be fetched
	 */
	public void setCard(ObjectId card) {
		if (card == null || !Card.class.equals(card.getType()))
			throw new IllegalArgumentException("Not a card id");
		this.card = card;
	}

	/**
	 * Gets identifier of {@link BackLinkAttribute} defining parent-children relation
	 * between cards
	 * @return identifier of {@link BackLinkAttribute} defining parent-children relation
	 * between cards
	 */
	public ObjectId getAttribute() {
		return attribute;
	}

	/**
	 * Sets identifier of {@link BackLinkAttribute} defining parent-children relation
	 * between cards 
	 * @param attribute identifier of {@link BackLinkAttribute} defining parent-children relation
	 * between cards
	 * @throws IllegalArgumentException if given identifier is not a 
	 * {@link BackLinkAttribute} identifier
	 */
	public void setAttribute(ObjectId attribute) {
		if (!BackLinkAttribute.class.equals(attribute.getType()))
			throw new IllegalArgumentException("Not a back link attribute id " + attribute);
		this.attribute = attribute;
	}
	
	/**
	 * See {@link Search#getColumns() Search.getColumns()} for description
	 * @return Collection of {@link SearchResult.Column columns}
	 * @since v.3
	 */
	public Collection getColumns() {
		return columns;
	}

	/**
	 * See {@link Search#setColumns(Collection) Search.setColumns(Collection)} for description
	 * @param columns Collection of {@link SearchResult.Column columns}
	 * @since v.3
	 */
	public void setColumns(Collection columns) {
		this.columns = columns;
	}

	/**
	 * {@link Card} identifier
	 */
	public ObjectId getObjectId() {
		return getCard();
	}

	/**
	 * @return {@link SearchResult}
	 * @see Action#getResultType()
	 */
	public Class getResultType() {
		return SearchResult.class;
	}
	
	/**
	 * Set flag 'rememberLastParent'
	 * @param remember
	 */
	public void setRememberLastParent(boolean remember) {
		this.rememberLastParent = remember;
	}
	
	/**
	 * Get the flag 'rememberLastParent'
	 * If it is true, then Query will save the last card ID from backlink as parent card ID.
	 * Else, Query will work by the old logic (without saving last card ID).
	 * @return boolean value which define the variant of work of query
	 */
	public boolean isRememberLastParent() {
		return this.rememberLastParent;
	}
}
