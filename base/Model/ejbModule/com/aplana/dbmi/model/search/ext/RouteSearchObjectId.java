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
package com.aplana.dbmi.model.search.ext;

import java.util.ArrayList;
import java.util.List;

import com.aplana.dbmi.action.xml.SearchXmlHelper;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.util.ObjectIdUtils;

public class RouteSearchObjectId extends ObjectId {
	/*
	 * ObjectId ��� ������������� � ������ �� ���������� (����������� �����).
	 * ��������� ������ �������� �� ��������� ���������.
	 * 
	 * �������� routeSource � ������������ �������� ��� ������ ����� � ���������, ��������:
	 * "jbr.examby->jbr.exam.person;jbr.incoming.addressee"
	 * 2 �������� ����������� "->"
	 * 2 ���� ����������� ";" 
	 * ������ ���� � ���� ������ ����� ��������� ������� ��������� ��������, ���������� � ���� ����
	 * ��������:jbr.examby#draft,execution
	 * ������ �������� �������� ������������� ����� �������. 
	 * ������ ������ ����:
	 * jbr.examby#draft,execution->jbr.exam.person;jbr.incoming.addressee
	 */
	private static final long serialVersionUID = 1L;
	protected List<List<RouteSearchNode>> routes;
	
	public List<List<RouteSearchNode>> getRoutes() {
		return routes;
	}

	public RouteSearchObjectId(Class type, Object id, String routeSource) {
		super(type, id);
		this.routes = parseRouteSource(routeSource);
	}
	
	protected List<List<RouteSearchNode>> parseRouteSource(String routeSource){
		List<List<RouteSearchNode>> routes = new ArrayList<List<RouteSearchNode>>();
		for(String sortAttrPath: routeSource.split(";")){
			List<RouteSearchNode> sortAttrPathList = new ArrayList<RouteSearchNode>();
			RouteSearchNode node = null;
			for(String sortAttrNode: sortAttrPath.trim().split(Attribute.LABEL_ATTR_PARTS_SEPARATOR)){
				if(sortAttrNode.trim().length()>0){
					node = new RouteSearchNode(sortAttrNode.trim());
					sortAttrPathList.add(node);
				}
			}
			if(sortAttrPathList.size()>0){
				node.setLastNodeFlag(true);
				//�������� ��������� Node, ��� �� ��������� �������������� �������
				routes.add(sortAttrPathList);
			}
		}
		return routes;
	}
	
	/**
	 * @author echirkov
	 *
	 */
	public class RouteSearchNode {
		public static final String STATUS_SEPARATOR = "#"; 
		private ObjectId linkAttr;
		private List<ObjectId> validStatuses;
		private boolean lastNodeFlag = false;
		
		RouteSearchNode(String routeNodeString){
			String[] splittedNode = routeNodeString.split(STATUS_SEPARATOR);
			linkAttr = SearchXmlHelper.safeMakeId(splittedNode[0]);
			if(splittedNode.length == 2){
				validStatuses = ObjectIdUtils.commaDelimitedStringToIds(splittedNode[1], CardState.class);
			}
		}

		public ObjectId getLinkAttr() {
			return linkAttr;
		}

		public List<ObjectId> getValidStatuses() {
			return validStatuses;
		}
		
		public boolean hasValidStatuses(){
			return validStatuses != null && validStatuses.size() > 0;
		}

		public boolean isLastNodeFlag() {
			return lastNodeFlag;
		}

		public void setLastNodeFlag(boolean isLast) {
			this.lastNodeFlag = isLast;
		}
	}

}