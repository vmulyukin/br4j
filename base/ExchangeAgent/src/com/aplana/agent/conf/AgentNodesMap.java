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
package com.aplana.agent.conf;

import java.util.HashMap;

import com.aplana.agent.conf.routetable.Agent;
import com.aplana.agent.conf.routetable.Node;

public class AgentNodesMap extends HashMap<Agent, NodeList>{
	private static final long serialVersionUID = 1L;

	public NodeList put(Agent key, Node value) throws RouteTableException {
		if (!containsKey(key)){
			NodeList nodeList = new NodeList();
			nodeList.add(value);
			return super.put(key, nodeList);
		} else {
			NodeList nodes = get(key);
			nodes.add(value);
			return super.put(key, nodes);
		}
	}
}
