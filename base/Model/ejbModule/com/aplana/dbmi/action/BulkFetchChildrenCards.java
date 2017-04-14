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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.aplana.dbmi.action.SearchResult.Column;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;

/**
 * Represents an action that fetches children cards by provided parent card ids and link attribute
 *
 */
public class BulkFetchChildrenCards implements Action<BulkFetchChildrenCards.Result> {
	private static final long serialVersionUID = 1L;
	public static final String COLUMNS_KEY = "columns"; 
	
	private Collection<Column> columns;
	private ObjectId linkAttributeId;
	private boolean reverseLink;
	private Collection<ObjectId> childrenTemplates;
	private Collection<ObjectId> childrenStates;
	private Collection<ObjectId> parentCardIds;

	public Collection<Column> getColumns() {
		return columns;
	}

	public void setColumns(Collection<Column> columns) {
		this.columns = columns;
	}

	public ObjectId getLinkAttributeId() {
		return linkAttributeId;
	}

	public void setLinkAttributeId(ObjectId linkAttributeId) {
		this.linkAttributeId = linkAttributeId;
	}

	public boolean isReverseLink() {
		return reverseLink;
	}

	public void setReverseLink(boolean reverseLink) {
		this.reverseLink = reverseLink;
	}

	public Collection<ObjectId> getParentCardIds() {
		return parentCardIds;
	}

	public void setParentCardIds(Collection<ObjectId> parentCardIds) {
		this.parentCardIds = parentCardIds;
	}

	public static long getSerialVersionUID() {
		return serialVersionUID;
	}
	
	public Collection<ObjectId> getChildrenTemplates() {
		return childrenTemplates;
	}

	public void setChildrenTemplates(Collection<ObjectId> childrenTemplates) {
		this.childrenTemplates = childrenTemplates;
	}

	public Collection<ObjectId> getChildrenStates() {
		return childrenStates;
	}

	public void setChildrenStates(Collection<ObjectId> childrenStates) {
		this.childrenStates = childrenStates;
	}
	
	public Class<?> getResultType() {
		return BulkFetchChildrenCards.Result.class;
	}
	
	/**
	 * Represents the action result object
	 * contains found children cards and map of
	 * child cards list mapped by SearchResult label columns path
	 */
	public static class Result {
		
		private Map<ObjectId, List<Card>> cards;
		private Map<String, ArrayList<Card>> labelColumnsMap; // <SearchResult label column path, Child cards list found by label column attribute>
		
		public Map<ObjectId, List<Card>> getCards() {
			if (null != cards) {
				return cards;
			}
			return Collections.emptyMap();
		}
		public void setCards(Map<ObjectId, List<Card>> cards) {
			this.cards = cards;
		}
		public Map<String, ArrayList<Card>> getLabelColumnsMap() {
			if (null != labelColumnsMap) {
				return labelColumnsMap;
			}
			return Collections.emptyMap();
		}
		public void setLabelColumnsMap(Map<String, ArrayList<Card>> labelColumnsMap) {
			this.labelColumnsMap = labelColumnsMap;
		}
	}
}
