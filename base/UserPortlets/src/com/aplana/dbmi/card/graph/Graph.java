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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletURL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.aplana.dbmi.model.ObjectId;

public class Graph {
	private long latestNodeId = -1;
	private Map nodes; // ������ ����� ����� ���� Node;  nodeId (Long) -> Node
	private Long root; // �������� nodeId ����������� ��������
	
	static class Node {
		String type; // ��� ����
		String label;
		Long cardId;
		List links; // ������ ������ � ������� ���������
		Map attrs; // �������� ���������. name (String) -> (������������ ��� � ��������) (Field)
		public Map getAttrs() {
			return attrs;
		}
		public void setAttrs(Map attrs) {
			this.attrs = attrs;
		}
		public Long getCardId() {
			return cardId;
		}
		public void setCardId(Long cardId) {
			this.cardId = cardId;
		}
		public List getLinks() {
			return links;
		}
		public void setLinks(List links) {
			this.links = links;
		}
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		public String getLabel() {
			return label;
		}
		public void setLabel(String label) {
			this.label = label;
		}
	}
	
	static class Link {
		private ObjectId idLink; // id �����
		private Long targetCardId;
		private Long targetNodeId;
		public ObjectId getIdLink() {
			return idLink;
		}
		public void setIdLink(ObjectId idLink) {
			this.idLink = idLink;
		}
		public Long getTargetCardId() {
			return targetCardId;
		}
		public void setTargetCardId(Long targetCardId) {
			this.targetCardId = targetCardId;
		}
		public Long getTargetNodeId() {
			return targetNodeId;
		}
		public void setTargetNodeId(Long targetNodeId) {
			this.targetNodeId = targetNodeId;
		}
	}
	
	// ������������ ������������� ��������, ��� � ��������
	static class Field {
		String title;
		String value;
		
		public Field(String title, String value) {
			this.title = title;
			this.value = value;
		}
	}
	
	public void addNode(Long nodeId, Node node) {
		if (nodes == null) {
			nodes = new HashMap();
		}
		if (nodes.size() == 0) {
			root = nodeId;
		}
		nodes.put(nodeId, node);
	}
	
	protected Long nextNodeId() {
		latestNodeId++;
		return new Long(latestNodeId);
	}
	
	public JSONObject getJSONTree(PortletURL linkURL, String parCardId, String prefixId) throws JSONException {
		return getJSONNode(root, null, linkURL, parCardId, prefixId);
	}
	
	// linkURL (PortletURL) - url �������� �������� ��� ����� �� �������������� ����,
	// ���������� ��������� ���������� �� ����������� ��������� ����������� ����� ��������
	// parCardId - ��� ��������� portletURL ���������� ����� ��������
	// prefixId - ���������� ������� id �� ��������
	private JSONObject getJSONNode(Long nodeId, String typeLink, PortletURL linkURL, String parCardId, String prefixId) throws JSONException {
		JSONObject jsonNode = new JSONObject();
		Node node = (Node)nodes.get(nodeId);
		if(node == null){
			return null;
		}
		jsonNode.put("id", prefixId+nodeId.toString());
		jsonNode.put("name", node.getLabel());
		
		JSONObject data = new JSONObject();
		data.put("cardId", node.getCardId().toString());
		data.put("typeNode", node.getType());
		if (typeLink != null) {
			data.put("typeLink", typeLink);
		}
		linkURL.setParameter(parCardId, node.getCardId().toString());
		data.put("linkURL", linkURL.toString());
		
		JSONObject attrs = new JSONObject();
		Map valueAttrs = node.getAttrs();
		if (valueAttrs != null) {
			Iterator iter = valueAttrs.keySet().iterator();
			while (iter.hasNext()) {
				String key = (String) iter.next();

				JSONObject show = new JSONObject();
				Graph.Field field = (Graph.Field)valueAttrs.get(key);
				show.put("title", field.title);
				show.put("value",field.value);

				attrs.put(key, show);
			}
		}
		data.put("attrs", attrs);

		jsonNode.put("data", data);

		final JSONArray children = new JSONArray();
		if (node.getLinks() != null) {
			Iterator iter = node.getLinks().iterator();
			while (iter.hasNext()) {
				Link link = (Link)iter.next();
				JSONObject o = getJSONNode(link.targetNodeId, (String)link.getIdLink().getId(), linkURL, parCardId, prefixId);
				if (o!=null){
					children.put(o);
				}
			}
		}
		jsonNode.put("children", children);
		return jsonNode;
	}
}
