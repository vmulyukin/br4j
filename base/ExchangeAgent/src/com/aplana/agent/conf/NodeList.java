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

import com.aplana.agent.conf.routetable.Node;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class NodeList {
	protected final Logger logger = Logger.getLogger(getClass());
	
	List<Node> allNodes = new ArrayList<Node>();
	List<Node> inNodes = new ArrayList<Node>();
	List<Node> outNodes = new ArrayList<Node>();

	public boolean add(Node e) throws RouteTableException {
		switch (e.getType()){
			case IN :	inNodes.add(e);
						break;
			case OUT:	outNodes.add(e);
						break;
		}
		return allNodes.add(e);
	}

	public void clear() {
		inNodes.clear();
		outNodes.clear();
		allNodes.clear();
	}

	public boolean addAll(Collection<? extends Node> nodes) throws RouteTableException {
		for (Node node : nodes){
			this.add(node);
		}
		return true;
	}
	
	public List<Node> getInNodes(){
		return inNodes;
	}

	public List<Node> getOutNodes(){
		return outNodes;
	}
}
