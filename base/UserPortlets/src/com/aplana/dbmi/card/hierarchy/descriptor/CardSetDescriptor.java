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
package com.aplana.dbmi.card.hierarchy.descriptor;

import java.util.LinkedList;
import java.util.List;

import com.aplana.dbmi.card.hierarchy.CardFilterCondition;
import com.aplana.dbmi.model.LocalizedString;
import com.aplana.dbmi.model.ObjectId;

public class CardSetDescriptor extends HierarchyItemDescriptor {
	private String alias;
	private ObjectId labelAttr;
	private ObjectId secondaryLabelAttr;
	private ObjectId linkedLabelAttr;
	private LocalizedString labelFormat;
	private int labelMaxLength = 0;
	private boolean labelAsLink;
	private boolean labelAsDownloadLink; // �������� ����� ��������
	private ObjectId sortAttr;
	private List grouping;
	private List childrenLinks;
	private List infoLinks;
	private List<LinkDescriptor> parentLinks;
	private boolean stored;
	private String columnsKey;
	private CardFilterCondition condition;
	private String group;
	private boolean checkChildren;
	private boolean terminalNodesOnly;
	private List<ObjectId> sortOrderByParentAttr;
	private ObjectId parentAttrLink;
	private boolean parentAttrLinkReversed;
	private boolean showOrg;
	private boolean isCheckAll;

	public CardFilterCondition getCondition() {
		return condition;
	}
	public void setCondition(CardFilterCondition condition) {
		this.condition = condition;
	}
	public String getAlias() {
		return alias;
	}
	public void setAlias(String alias) {
		this.alias = alias;
	}
	public ObjectId getLabelAttr() {
		return labelAttr;
	}
	public void setLabelAttr(ObjectId labelAttr) {
		this.labelAttr = labelAttr;
	}
	public ObjectId getSortAttr() {
		return sortAttr;
	}

	public void setSortAttr(ObjectId sortAttr) {
		this.sortAttr = sortAttr;
	}
	public List getGrouping() {
		return grouping;
	}
	public void setGrouping(List grouping) {
		this.grouping = grouping;
	}
	public List getChildrenLinks() {
		return childrenLinks;
	}
	public void setChildrenLinks(List childrenLinks) {
		this.childrenLinks = childrenLinks;
	}

	
	public int getLabelMaxLength() {
		return labelMaxLength;
	}
	public void setLabelMaxLength(int labelMaxLength) {
		this.labelMaxLength = labelMaxLength;
	}
	public List getInfoLinks() {
		return infoLinks;
	}
	public void setInfoLinks(List infoLinks) {
		this.infoLinks = infoLinks;
	}
	public boolean isStored() {
		return stored;
	}
	public void setStored(boolean stored) {
		this.stored = stored;
	}
	public List<LinkDescriptor> getParentLinks() {
		return parentLinks;
	}
	public void setParentLinks(List<LinkDescriptor> parentLinks) {
		this.parentLinks = parentLinks;
	}
	public ObjectId getSecondaryLabelAttr() {
		return secondaryLabelAttr;
	}
	public void setSecondaryLabelAttr(ObjectId secondaryLabelAttr) {
		this.secondaryLabelAttr = secondaryLabelAttr;
	}		
	public ObjectId getLinkedLabelAttr() {
		return linkedLabelAttr;
	}
	public void setLinkedLabelAttr(ObjectId linkedLabelAttr) {
		this.linkedLabelAttr = linkedLabelAttr;
	}
	public LocalizedString getLabelFormat() {
		return labelFormat;
	}
	public void setLabelFormat(LocalizedString labelFormat) {
		this.labelFormat = labelFormat;
	}
	public boolean isLabelAsLink() {
		return labelAsLink;
	}
	public void setLabelAsLink(boolean labelAsLink) {
		this.labelAsLink = labelAsLink;
	}
	public String getColumnsKey() {
		return columnsKey;
	}
	public void setColumnsKey(String columnsKey) {
		this.columnsKey = columnsKey;
	}
	public String getGroup() {
		return group;
	}
	public void setGroup(String group) {
		this.group = group;
	}
	public boolean isCheckChildren(){
		return checkChildren;
	}
	public void setCheckChildren(boolean checkChildren){
		this.checkChildren = checkChildren;
	}
	public boolean isTerminalNodesOnly() {
		return terminalNodesOnly;
	}
	public void setTerminalNodesOnly(boolean terminalNodesOnly) {
		this.terminalNodesOnly = terminalNodesOnly;
	}
	
	public List<ObjectId> getSortOrderByParentAttr() {
		return sortOrderByParentAttr;
	}
	public void setSortOrderByParentAttr(List<ObjectId> sortOrderByParentAttr) {
		this.sortOrderByParentAttr = sortOrderByParentAttr;
	}
	public ObjectId getParentAttrLink() {
		return parentAttrLink;
	}
	public void setParentAttrLink(ObjectId parentAttrLink) {
		this.parentAttrLink = parentAttrLink;
	}
	public boolean isParentAttrLinkReversed() {
		return parentAttrLinkReversed;
	}
	public void setParentAttrLinkReversed(boolean parentAttrLinkReversed) {
		this.parentAttrLinkReversed = parentAttrLinkReversed;
	}
	public boolean isShowOrg() {
		return showOrg;
	}
	public void setShowOrg(boolean showOrg) {
		this.showOrg = showOrg;
	}
	public boolean isCheckAll() {
		return isCheckAll;
	}
	public void setCheckAll(boolean isCheckAll) {
		this.isCheckAll = isCheckAll;
	}
	public boolean isLabelAsDownloadLink() {
		return labelAsDownloadLink;
	}
	public void setLabelAsDownloadLink(boolean labelAsDownloadLink) {
		this.labelAsDownloadLink = labelAsDownloadLink;
	}
}