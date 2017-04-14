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

import com.aplana.dbmi.model.Attribute;

public abstract class AttributeContainer {
	private HashMap<String, List<BlockView>> regions = new HashMap<String, List<BlockView>>();
	
	public int getRegionsCount(){
		return regions.size();
	}
	
	public void clearAll(){
		regions.clear();
	}
	
	public void setComponents(Collection<BlockView> components){
		clearAll();
		if (components == null)
			return;
		for (Iterator<BlockView> i = components.iterator(); i.hasNext(); )
			addComponent(i.next());
	}
	
	public List<BlockView> getAllRegions(){
		ArrayList<BlockView> al = new ArrayList<BlockView>();
		for (Iterator<List<BlockView>> i = regions.values().iterator(); i.hasNext();) {
			List<BlockView> list = (List<BlockView>) i.next();
			al.addAll(list);
		}
		return al;
	}
	
	public AttributeView getAttributeViewFor(Attribute attr){
		List<BlockView> blocks = getAllRegions();
		for (Iterator<BlockView> i = blocks.iterator(); i.hasNext();) {
			BlockView block = i.next();
			List<AttributeView> attrs = block.getAttributeViews();
			for (Iterator<AttributeView> j = attrs.iterator(); j.hasNext();) {
				AttributeView attrv = j.next();
				if (attrv.getAttribute().getId().equals(attr.getId()))
					return attrv;
			}
		}
		return null;
	}
	
	protected void clearRegion(String regionName){
		if (regions.containsKey(regionName))
			regions.get(regionName).clear();
	}
	
	protected void addRegion(String regionName){
		if (!regions.containsKey(regionName))
			regions.put(regionName, new ArrayList<BlockView>());
	}
	
	protected List<BlockView> getRegion(String regionName){
		ArrayList<BlockView> al = new ArrayList<BlockView>();
		
		try {
			if (regionName.equals("ALL"))
				return getAllRegions();
			al.addAll(regions.get(regionName));
		} catch (Exception e) {}
		Collections.sort(al, new ComponentsComparator());
		return al;
	}
	
	protected int getRegionSize(String regionName){
		if (regions.containsKey(regionName))
			return regions.get(regionName).size();
		return 0;
	}
	
	protected void addComponent(String regionName, BlockView component){
		if ((regionName == null)||(component == null))
			return;
		addRegion(regionName);
		List<BlockView> list = regions.get(regionName);
		list.add(component);
	}
	
	private class ComponentsComparator implements Comparator<Object> {
		public int compare(Object o1, Object o2) {
			return compareComponents(o1, o2);
		}
	}
	
	public abstract void addComponent(Object component);

	public abstract void addRegion(Object regionID);

	public abstract void clearRegion(Object regionID);

	public abstract int getRegionSize(Object regionID);
	
	public abstract List<BlockView> getRegion(Object regionID);
	
	protected abstract int compareComponents (Object o1, Object o2);
}