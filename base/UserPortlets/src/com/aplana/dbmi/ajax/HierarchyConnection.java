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
package com.aplana.dbmi.ajax;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.aplana.dbmi.card.hierarchy.HierarchicalCardList;
import com.aplana.dbmi.card.hierarchy.Hierarchy;
import com.aplana.dbmi.card.hierarchy.HierarchyItem;

public class HierarchyConnection {

	// (YNikitin, 2011/04/20) ���������� ���
	// (RuSA, 2011/04/24) ������ ����������, ��������, ���� ����� ���������
	// ������� ����� ��-�� ��������� ����������� ��������, � ���������
	// HierarchicalCardLinkAttributeEditor ����������.
	// ����� �� �������: BR4J00003083 ("40 ���������"), BR4J00003119 ("��������� ���� ��� ��������-�����������").
	public static final int ITEMS_FIRST_BLOCK_SIZE = 50;
	public static final float SCROLL_BOTTOM_POS = 0.7f;		// ������������� ��������� ������ ����� ��������� �� ���������� ���������
	public static final float SCROLL_BOTTOM_NEWPOS = 0.5f;	// ������������� ��������� ������ ����� ��������� ����� ���������� ���������
	public static final float MULTIPLIER = SCROLL_BOTTOM_POS/SCROLL_BOTTOM_NEWPOS - 1;

	private Hierarchy hierarchy;
	private Hierarchy mainHierarchy;

	private Iterator<HierarchicalCardList> roots;
	private Iterator<? extends HierarchyItem> rootItems;
	private int items_sent;	//������� ����� ��������� ���� ���������� (����� ���� ������� getNextTopLevelItems)
	private int next_block_size; // ������� ��������� � ��������� ���

	public HierarchyConnection(Hierarchy hierarchy) {
		mainHierarchy = hierarchy;
		reset();
	}

	public void reset() {
		reset(null);
	}

	public void reset(String query) {
		next_block_size = items_sent = 0;
		hierarchy = CardHierarchyServlet.filterHierarchy(mainHierarchy, query);
		rootItems = null;
		roots = null;
		if (hierarchy != null && hierarchy.getRoots() != null) {
			roots = hierarchy.getRoots().iterator();
			if (roots != null && roots.hasNext()) {
				rootItems = roots.next().getTopLevelItems().iterator();
			}
		}
	}

	public List<HierarchyItem> getNextTopLevelItems() {
		if (rootItems == null)
			return Collections.emptyList();
		int count = (next_block_size > 0) 
								? next_block_size
								: ITEMS_FIRST_BLOCK_SIZE;
		if (count > 0){
			next_block_size = Math.round(count * MULTIPLIER);
		} else
			next_block_size = 0;

		final List<HierarchyItem> result = new ArrayList<HierarchyItem>(next_block_size);
		while (true) {
			if (count==0)
				break;
			if (rootItems.hasNext()) {
				result.add(rootItems.next());
				++items_sent;
				count--;
				continue;
			}
			rootItems = null;
			if (roots.hasNext()) {
				rootItems = roots.next().getTopLevelItems().iterator();
			} else {
				break;
			}
		} // while
		return result;
	}

	public boolean hasNextTopLevelItems() {
		return (rootItems != null && rootItems.hasNext()) 
				|| ( roots != null && roots.hasNext());
	}

	public Hierarchy getHierarchy() {
		return hierarchy;
	}

	private Set<Long> notShownItems = Collections.emptySet();

	public Set<Long> getNotShownItems() {
		return notShownItems;
	}

	public void setNotShownItems(Set<Long> notShownItems) {
		this.notShownItems = notShownItems;
	}

	/**
	 * @return ���-�� ��������� ��� ��������� (���������) ������ ������, �������
	 * ���� ������� � ������ getNextTopLevelItems.
	 */
	public int getNext_block_size() {
		return this.next_block_size;
	}

	/**
	 * @param next_block_size: ���-�� ��������� ��� ��������� (���������)
	 * ������ ������, ������� ���� ������� � ������ getNextTopLevelItems.
	 */
	public void setNext_block_size(int next_block_size) {
		this.next_block_size = next_block_size;
	}


}
