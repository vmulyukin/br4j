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
package com.aplana.dbmi.card.hierarchy;
import java.util.List;

/**
 * ������������� ������ ��������
 * �������� ������ ��������, ��������������� �� ������ ��� ���������� ���������
 * ������������ ����������� �� ��������� ���� list � tree
 */
public class HierarchicalCardList {
	private String alias;
	private List topLevelItems;
	private List cardItems;
	private boolean stored;
	public String getAlias() {
		return alias;
	}
	public HierarchicalCardList(String alias) {
		this.alias = alias;
	}	
	public boolean isStored() {
		return stored;
	}
	public void setStored(boolean stored) {
		this.stored = stored;
	}	
	public List getTopLevelItems() {
		return topLevelItems;
	}
	public void setTopLevelItems(List topLevelItems) {
		this.topLevelItems = topLevelItems;
	}
	public List getCardItems() {
		return cardItems;
	}	
	public void setCardItems(List cardItems) {
		this.cardItems = cardItems;
	}
}