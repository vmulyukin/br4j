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
package com.aplana.dbmi.actionhandler.descriptor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.aplana.dbmi.card.hierarchy.Messages;

public class ActionsDescriptor {

	private final Map<String, ActionHandlerDescriptor> items = new LinkedHashMap<String, ActionHandlerDescriptor>();
	private Messages messages;
	private Comparator<ActionHandlerDescriptor> comp;

	public ActionHandlerDescriptor getActionHandlerDescriptor(String actionId) {
		return items.get(actionId);
	}

	public Set<String> getActionIds() {
		return items.keySet();
	}

	public Comparator<ActionHandlerDescriptor> getComp() {
		return comp;
	}

	public void setComp(Comparator<ActionHandlerDescriptor> comp) {
		this.comp = comp;
	}

	/**
	 * �������� ������ id ��������, ������������� �� ��������������� ��������.
	 * @return
	 */
	public List<String> getSortedActionIds()
	{
		final List<ActionHandlerDescriptor> list = new ArrayList<ActionHandlerDescriptor>();
		if (items != null && items.values() != null) {
			list.addAll(items.values());
			// ��������� ������
			if(comp == null)
				comp = new ActionsDescriptorTitleComparator();
			Collections.sort(list, comp);
		}
		final List<String> result = new ArrayList<String>(list.size());
		for (ActionHandlerDescriptor desc : list) {
			result.add(desc.getId());
		}
		return result;
	}

	public void addItem(ActionHandlerDescriptor actionHandlerDescriptor) {
		items.put(actionHandlerDescriptor.getId(), actionHandlerDescriptor);
	}
	
	public void removeItem(String id) {
		for(ActionHandlerDescriptor ahd : items.values()){
			if(ahd.getId().equals(id)){
				items.remove(id);
				break;
			}
		}
	}

	public Messages getMessages() {
		return messages;
	}

	public void setMessages(Messages messages) {
		this.messages = messages;
	}
}
