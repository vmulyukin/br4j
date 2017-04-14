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

import com.aplana.agent.conf.AgentNodesMap;
import com.aplana.agent.conf.RouteTableConfiguration;
import com.aplana.agent.conf.routetable.Agent;
import com.aplana.agent.plugin.PluginFactory;

import org.apache.log4j.Logger;

/**
 * Main class for transport agent.
 */
public class TransportAgent {
	final static Logger logger = Logger.getLogger(TransportAgent.class);

	private TaskContainer tk;

	public TransportAgent() {
		logger.info("Initializing context");
		PluginFactory.initApplicationContext();
		prepareTasks();
	}

	public void activate() {
		tk.startExecuting();
	}

	public void deactivate() {
		tk.stopExecuting();
	}

	private void prepareTasks() {
		RouteTableConfiguration configuration = RouteTableConfiguration.getInstance();
		AgentNodesMap agentMap = configuration.getAgentNodesMap();

		tk = new TimerTaskContainer();
		for (Agent agent : agentMap.keySet()) {
			tk.registerTask(agent, agentMap.get(agent));
		}
	}
}
