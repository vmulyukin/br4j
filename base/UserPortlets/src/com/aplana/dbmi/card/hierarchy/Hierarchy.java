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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.aplana.dbmi.card.hierarchy.descriptor.HierarchyDescriptor;

public class Hierarchy {
	private List roots;
	private HierarchyDescriptor descriptor;
	private Map items;
	private volatile long currId;
	
	public Hierarchy(HierarchyDescriptor hd) {
		this.descriptor = hd;
		items = new HashMap();
		currId = 0;
	}
	
	public List getRoots() {
		return roots;
	}

	public void setRoots(List roots) {
		this.roots = roots;
	}

	public HierarchyDescriptor getDescriptor() {
		return descriptor;
	}
	public long addItem(HierarchyItem item) {
		synchronized(items) {
			long id = ++currId;
			items.put(new Long(id), item);
			return id;
		}
	}
}