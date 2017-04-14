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
package com.aplana.dbmi.gui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Tab;
import com.aplana.dbmi.model.TabViewParam;

public class TabsManager {
	private HashMap<ObjectId, TabViewParam> tabs = new HashMap<ObjectId, TabViewParam>();
	private TabViewParam activeTab = null;
	
	public List<TabViewParam> getTabs() {
		ArrayList<TabViewParam> al = new ArrayList<TabViewParam>(tabs.values());
		Collections.sort(al, new TabComparator());
		return al;
	}
	
	public List<TabViewParam> getVisibleTabs(){
		List<TabViewParam> list = new ArrayList<TabViewParam>(getTabs());
		if (list.size()==1){
			if ( list.get(0).getNameEn()==null && 
				 list.get(0).getNameRu()==null )
				list.clear();
		}
		return list;
	}
	
	public void setTabs(Collection<TabViewParam> tabs) {
		this.tabs.clear();
		for (Iterator<TabViewParam> i = tabs.iterator(); i.hasNext(); ){
			TabViewParam tab = i.next();
			this.tabs.put(tab.getId(), tab);
		}
//		setFirstActiveTab();
	}
	
	public void clear(){
		tabs.clear();
		activeTab = null;
	}
	
	public void addTab(TabViewParam tab){
		try {
			tabs.put(tab.getId(), tab);
		} catch (Exception e) {}
	}
	
	public void removeTab(TabViewParam tab){
		removeTab(tab.getId());
	}
	
	public TabViewParam getActiveTab() {
		return activeTab;
	}
	
	/**
	 * @return ������ ��������� ������ ���� �������� ������� (��������) ��������.
	 */
	public List<AttributeView> getActiveAttributeViews() {
		final List<AttributeView> result = new ArrayList<AttributeView>();
		final TabView activeTabView = (TabView) getActiveTab();
		if (activeTabView != null && activeTabView.getContainer() != null ) {
			final List<BlockView> blocks = activeTabView.getContainer().getAllRegions();
			if (blocks != null) {
				for(BlockView block: blocks) {
					if (block != null)
						result.addAll(block.getAttributeViews());
				}
			}
		}
		return result;
	}

	private void removeTab(ObjectId tabId){
		try {
			tabs.remove(tabId);
		} catch (Exception e) {}
	}

	private void setActiveTab(ObjectId aTab){
		try {
			if (tabs.containsKey(aTab))
				this.activeTab = tabs.get(aTab);
		} catch (Exception e) {}
	}
	
	public void setActiveTabId(ObjectId activeTabId) {
		if (activeTabId == null){
			setFirstActiveTab();
			return;
		}
		setActiveTab(activeTabId);
	}
	
	public boolean isTabActive(TabViewParam tab){
		if (tab.getId().equals(activeTab.getId()))
			return true;
		else return false;
	}
	
	private void setFirstActiveTab(){
		ArrayList<TabViewParam> al = new ArrayList<TabViewParam>(tabs.values());
		Collections.sort(al, new TabComparator());
		try {
			activeTab = al.get(0);
		} catch (Exception e) {
		}
	}
	
	public void setEmptyTabs(Collection<Long> tabs) {
		try {
			if (tabs.size() == 0)
				return;
			for (Iterator<Long> i = tabs.iterator(); i.hasNext();) {
				Long tab = i.next();
				removeTab(new ObjectId(Tab.class, tab.longValue()));
			}	
		} catch (Exception e) {}
		
	}

	class TabComparator implements Comparator<TabViewParam> {
		public int compare(TabViewParam o1, TabViewParam o2) {
			if (!(o1 instanceof TabViewParam)||!(o2 instanceof TabViewParam))
				throw new ClassCastException();
			
			return Integer.valueOf(o1.getOrder()).compareTo(Integer.valueOf(o2.getOrder()));
		}
	}
}