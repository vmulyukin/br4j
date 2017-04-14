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
package com.aplana.dbmi.card.graph;

import java.util.List;
import java.util.Map;
import com.aplana.dbmi.model.ObjectId;

import com.aplana.dbmi.card.hierarchy.Messages;

public class GraphDescriptor {
	int depth;
	String nameFirstCardSet;
	Map cardSets; // ������ CardSet-�� alias -> CardSet
	private Messages messages;
	
	public Messages getMessages() {
		return messages;
	}
	public void setMessages(Messages messages) {
		this.messages = messages;
	}
	public void setDepth(int depth) {
		this.depth = depth;
	}
	public int getDepth() {
		return depth;
	}
	
	public CardSet getCardSet(String name) {
		return (CardSet)cardSets.get(name);
	}
	
	static class CardSet {
		private String type;
		private String labelType;
		Map links; // ������ ������ �� �������� �������. linkAttr -> target. Map<ObjectId, String>
		Map attrs; // ��������������� ����� id ��������� (ObjectId) � ��� ������(String) 
		Map params; // ������������ ����� ������� ���������(String) � ������ ����������(Map<���_���������(String) -> ��������_���������(String)>)
	
		public Map getLinks() {
			return links;
		}
		public void setLinks(Map links) {
			this.links = links;
		}
		public String getTarget(ObjectId linkAttr) {
			return (String)links.get(linkAttr);
		}
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		public String getLabelType() {
			return labelType;
		}
		public void setLabelType(String labelType) {
			this.labelType = labelType;
		}
		public Map getAttrs() {
			return attrs;
		}
		public void setAttrs(Map attrs) {
			this.attrs = attrs;
		}
		public Map getParams() {
			return params;
		}
		public void setParams(Map params) {
			this.params = params;
		}
		public Map getAttrParams(String nameAttr) {
			return (Map)params.get(nameAttr);
		}
	}

	public Map getCardSets() {
		return cardSets;
	}
	public void setCardSets(Map cardSets) {
		this.cardSets = cardSets;
	}
	public String getNameFirstCardSet() {
		return nameFirstCardSet;
	}
	public void setNameFirstCardSet(String nameFirstCardSet) {
		this.nameFirstCardSet = nameFirstCardSet;
	}
	
}
