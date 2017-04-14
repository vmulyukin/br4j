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
package com.aplana.dbmi.card.hierarchy.descriptor;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;

import com.aplana.dbmi.actionhandler.descriptor.ActionsDescriptor;
import com.aplana.dbmi.card.hierarchy.Messages;
import com.aplana.dbmi.card.util.SearchUtils;
import com.aplana.dbmi.model.Card;

public class HierarchyDescriptor {
	public static final String COLUMNS_MAIN = "main";

	private Map columns = new HashMap();
	private List cardSets;
	private List styles;
	private ActionsDescriptor actionsDescriptor;
	private Messages messages;
	private CardItemsMergeMode parentMergeMode;
	private int cacheReloadTime;
	private Boolean noColumns = false;
	private boolean isReadonly = true;
	private String hierarchySQL;
	private boolean terminalNodesOnly;

	private int id;
	public HierarchyDescriptor(int id) {
		this.id = id;
	}
	public int getId() {
		return id;
	}

	public CardItemsMergeMode getParentMergeMode() {
		return parentMergeMode;
	}
	public void setParentMergeMode(CardItemsMergeMode parentMergeMode) {
		this.parentMergeMode = parentMergeMode;
	}
	public List getColumns(String key) {
		return (List) columns.get(key);
	}
	public void setColumns(String key, List columns) {
		this.columns.put(key, columns);
	}
	public List getCardSets() {
		return cardSets;
	}
	public void setCardSets(List cardSets) {
		this.cardSets = cardSets;
	}
	public List getStyles() {
		return styles;
	}
	public void setStyles(List styles) {
		this.styles = styles;
	}
	public Messages getMessages() {
		return messages;
	}
	public void setMessages(Messages messages) {
		this.messages = messages;
	}
	public void setActionsDescriptor(ActionsDescriptor actions) {
		this.actionsDescriptor = actions;
	}
	public ActionsDescriptor getActionsDescriptor() {
		return actionsDescriptor;
	}

	public boolean isReadonly() {
		return this.isReadonly;
	}
	public void setReadonly(boolean isReadonly) {
		this.isReadonly = isReadonly;
	}
	public Boolean getNoColumns() {
		return noColumns;
	}
	public void setNoColumns(Boolean noColumns) {
		this.noColumns = noColumns;
	}
	public CardSetDescriptor getStoredDescriptor() {
		Iterator i = cardSets.iterator();
		while (i.hasNext()) {
			CardSetDescriptor d = (CardSetDescriptor)i.next();
			if (d.isStored()) {
				return d;
			}
		}
		throw new IllegalStateException("No stored cardSet defined");
	}

	public CardSetDescriptor getCardSetDescriptor(String alias) {
		Iterator i = cardSets.iterator();
		while (i.hasNext()) {
			CardSetDescriptor d = (CardSetDescriptor)i.next();
			if (alias.equals(d.getAlias())) {
				return d;
			}
		}
		throw new IllegalStateException("No cardSet with given alias " + alias + " found");
	}

	public StylingDescriptor getStylingDescriptor(Card c) {
		Iterator i = getStyles().iterator();
		while (i.hasNext()) {
			StylingDescriptor d = (StylingDescriptor)i.next();
			if (d.getCondition().check(c)) {
				return d;
			}
		}
		return null;
	}

	public boolean hasInfoBlocks() {
		for (Iterator i = getCardSets().iterator(); i.hasNext(); ) {
			CardSetDescriptor cd = (CardSetDescriptor) i.next();
			if (cd.getInfoLinks() != null && cd.getInfoLinks().size() > 0)
				return true;
		}
		return false;
	}

	public JSONArray getColumnsJSON(String key) throws JSONException {
		return SearchUtils.getColumnsJSON(getColumns(key));
	}

	public int getCacheReloadTime() {
		return cacheReloadTime;
	}

	public void setCacheReloadTime(int cacheReloadTime) {
		this.cacheReloadTime = cacheReloadTime;
	}
	
	public String getHierarchySQL() {
		return hierarchySQL;
	}
	public void setHierarchySQL(String hierarchySQL) {
		this.hierarchySQL = hierarchySQL;
	}
	public boolean isTerminalNodesOnly() {
		return terminalNodesOnly;
	}
	public void setTerminalNodesOnly(boolean terminalNodesOnly) {
		this.terminalNodesOnly = terminalNodesOnly;
	}
	
}