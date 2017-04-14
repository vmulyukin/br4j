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
package com.aplana.agent;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.map.MultiValueMap;

import com.aplana.agent.conf.routetable.Node;

public class RouteOrdersBean {
	private Map<URL, RouteOrder> routeOrders = new HashMap<URL, RouteOrder>();
	private MultiValueMap nodes = new MultiValueMap();

	public synchronized void putRouteOrder(RouteOrder routeOrder) {
		routeOrders.put(routeOrder.getLetter(), routeOrder);
		for (Node node : routeOrder.getNodes()) {
			this.nodes.put(node, routeOrder.getLetter());
		}
	}

	public synchronized void putRouteOrders(List<RouteOrder> routeOrders) {
		for (RouteOrder routeOrder : routeOrders) {
			putRouteOrder(routeOrder);
		}
	}

	public synchronized void removeLetter(URL letter) {
		RouteOrder removedRouteOrder = routeOrders.remove(letter);
		if (removedRouteOrder != null) {
			for (Node removedNode : removedRouteOrder.getNodes()) {
				nodes.remove(removedNode, letter);
			}
		}
	}

	public synchronized void clear() {
		routeOrders.clear();
		nodes.clear();
	}

	public synchronized List<RouteOrder> take(List<Node> nodes) throws RouterBeanException {
		Set<URL> lettersToOrder = new HashSet<URL>();
		for (Node node : nodes) {
			@SuppressWarnings("unchecked")
			List<URL> letters = (List<URL>) this.nodes.get(node);
			if (letters != null) {
				lettersToOrder.addAll(letters);        // ��� ���������� ������������ ��� ��� ���������� ����� � ��� �����������
			}
		}

		List<RouteOrder> orders = new ArrayList<RouteOrder>();
		for (URL letter : lettersToOrder) {
			RouteOrder routeOrder = routeOrders.get(letter);
			if (routeOrder == null) { // ���-�� ��������, ��������� �� ���������������� !
				throw new RouterBeanException("��������� ����� � ��� �� ����������������. ��������� ���! Letter=" + letter);
			}
			orders.add(routeOrder);
			removeLetter(letter);
		}
		return orders;
	}
}