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

import com.aplana.agent.conf.routetable.*;
import com.aplana.agent.conf.routetable.Resources.Resource;
import com.rits.cloning.Cloner;
import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

/**
 * Helper class to work with JAXB model
 */
public class RouteTableConfiguration {

	public final static String ROUTE_TABLE_FILE = "routeTable.xml";
	public final static String ROUTE_TABLE_SCHEMA_FILE = "routeTable.xsd";
	public final static String MODEL_PACKAGE = "com.aplana.agent.conf.routetable";

	public final static String DEFAULT_UUID = "default";

	public final static String DEFAULT_COLLECTOR = "default collector";

	protected final Logger logger = Logger.getLogger(getClass());

	private Unmarshaller unmarshaller;

	private RouteTable routeTable;
	private AgentNodesMap agentNodes = new AgentNodesMap();
	private HashMap<String, List<Node>> routeRulesMap = new HashMap<String, List<Node>>();
	Node defaultNode = null;

	private RouteTableConfiguration() {
		try {
			JAXBContext jc = JAXBContext.newInstance(MODEL_PACKAGE);
			unmarshaller = jc.createUnmarshaller();
			validateXML();
			routeTable = readFromXML();
			validate(routeTable);
			setDefaultCollector();
			buildAgentNodesMap();
			logger.info("We have " + agentNodes.size() + " distinct sub-agents.");
			buildRouteRules();
		} catch (JAXBException e) {
			logger.error("Unexpected error while loading RouteTable", e);
			throw new RuntimeException("Unexpected error while loading RouteTable", e);
		} catch (IOException e) {
			throw new RuntimeException("Unexpected error while loading RouteTable configuration", e);
		} catch (RouteTableException e) {
			throw new RuntimeException("Unexpected error while loading RouteTable configuration", e);
		} catch (SAXException e) {
			throw new RuntimeException("Unexpected error while validating RouteTable configuration", e);
		} catch (URISyntaxException e) {
			throw new RuntimeException("Unexpected error while getting RouteTable schema", e);
		}
	}

	private void validateXML() throws SAXException, IOException, URISyntaxException {
		logger.info("Validating RouteTable configuration...");
		SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema;
		schema = factory.newSchema(new File(ConfigService.getConfigFileUrl(ROUTE_TABLE_SCHEMA_FILE).toURI()));
		Validator validator = schema.newValidator();
		Source source = new StreamSource(ConfigService.loadConfigFile(ROUTE_TABLE_FILE));
		validator.validate(source);
		logger.info("RouteTable configuration successfully validated.");
	}

	private void validate(RouteTable routeTable) throws RouteTableException {
		/**
		 * 1. only first char may be '*'
		 * 2. other chars can not be '*'
		 */
		String validatePattern = "(\\*)?([^\\*]*)";
		for (Node node : routeTable.getNodes().getNode()) {
			for (Destination dest : node.getDestination()) {
				if (!dest.getUuid().matches(validatePattern)) {
					String message = "Wrong mask of destination UUID: " + dest.getUuid();
					throw new RouteTableException(message);
				}
			}
		}
	}

	private RouteTable readFromXML() throws JAXBException, IOException {
		return (RouteTable) unmarshaller.unmarshal(ConfigService.loadConfigFile(ROUTE_TABLE_FILE));
	}

	private static RouteTableConfiguration instance;

	public static synchronized RouteTableConfiguration getInstance() {
		if (instance == null) {
			instance = new RouteTableConfiguration();
		}
		return instance;
	}

	public String getTransportAgentName() {
		return routeTable.getTransportAgent().getName();
	}

	public String getTransportAgentUUID() {
		return routeTable.getTransportAgent().getUuid();
	}

	public List<Node> getNodes(String uuid) {
		List<Node> result = new ArrayList<Node>();

		for (Map.Entry<String, List<Node>> entry : routeRulesMap.entrySet()) {
			String uuidKey = entry.getKey().toUpperCase();
			String uuidPattern = uuidKey.replaceAll("\\*", "(.*)");
			if (uuid.toUpperCase().matches(uuidPattern)) {
				List<Node> value = entry.getValue();
				if (value != null) {
					result.addAll(entry.getValue());
				}
			}
		}

		if (result.isEmpty()) {    // ��� ������������ ���� ��� ������� ���������� ?
			result.add(defaultNode);
		}
		return result;
	}

	public AgentNodesMap getAgentNodesMap() {
		return agentNodes;
	}

	private boolean nullOrEmpty(String string) {
		return (string == null) || (string.isEmpty());
	}

	private boolean nullOrZero(Number num) {
		return (num == null) || (num.longValue() == 0);
	}

	private boolean hasDifferentConfiguration(Agent newAgent, Agent defAgent) {
		if (newAgent.getPeriod() != null) {
			if (defAgent.getPeriod() != null) {
				if (!newAgent.getPeriod().equals(defAgent.getPeriod())) {
					return true;
				}
			} else {
				return true;
			}
		}
		if (newAgent.getCron() != null) {
			if (defAgent.getCron() != null) {
				if (!newAgent.getCron().equals(defAgent.getCron())) {
					return true;
				}
			} else {
				return true;
			}
		}
		if (!(newAgent.getRetries() == defAgent.getRetries())) {
			return true;
		}
		if (newAgent.getConfig() != null) {
			if (defAgent.getConfig() != null) {
				if (!newAgent.getConfig().equals(defAgent.getConfig())) {
					return true;
				}
			} else {
				return true;
			}
		}
		return false;
	}

	private Agent getSameAgent(Agent newAgent, AgentNodesMap accumulatedAgents) {
		for (Agent builtAgent : accumulatedAgents.keySet()) {
			if (!ObjectUtils.equals(newAgent.getConfig(), builtAgent.getConfig()) ||
					!ObjectUtils.equals(newAgent.getCron(), builtAgent.getCron()) ||
					!ObjectUtils.equals(newAgent.getPeriod(), builtAgent.getPeriod()) ||
					!ObjectUtils.equals(newAgent.getRetries(), builtAgent.getRetries())) {
				continue;
			}
			return builtAgent;
		}
		return null;
	}

	private void buildAgentNodesMap() throws RouteTableException {
		agentNodes.clear();
		Map<String, Agent> defaultAgents = new HashMap<String, Agent>();
		for (Agent agent : routeTable.getTransportAgent().getAgents().getAgent()) {
			defaultAgents.put(agent.getName(), agent);
		}
		Cloner cloner = new Cloner();
		for (Node node : routeTable.getNodes().getNode()) {
			Agent agent;
			Agent defaultAgent = defaultAgents.get(node.getAgent().getName());
			if (hasDifferentConfiguration(node.getAgent(), defaultAgent)) {
				// ���� ������ ���� ���� ����. �������� ��� ������, �� �������� ����� �����
				// � ������������� ���������� �� ���������� (� route-table/transport-agent/agents)

				agent = cloner.deepClone(defaultAgent);
				if (!nullOrEmpty(node.getAgent().getConfig())) {
					agent.setConfig(node.getAgent().getConfig());
				}
				if (!nullOrEmpty(node.getAgent().getCron())) {
					agent.setCron(node.getAgent().getCron());
				}
				if (!nullOrZero(node.getAgent().getPeriod())) {
					agent.setPeriod(node.getAgent().getPeriod());
				}
				if (!nullOrZero(node.getAgent().getRetries())) {
					agent.setRetries(node.getAgent().getRetries());
				}
				//����� ������� ��� �� ����� ������ �� ��� ���������� ������, ��������� �� ����������
				Agent sameAgent = getSameAgent(agent, agentNodes);
				agent = sameAgent != null ? sameAgent : agent;
			} else {
				agent = defaultAgent;
			}
			agentNodes.put(agent, node);
		}
	}

	private void setDefaultCollector() throws RouteTableException {    // ������������� ��������� ��������� (route-table/nodes/node@name="default collector")
		// �� ��� ���� ��� �� �������� ���� ���������
		Resource defaultCollectorResource = null;
		for (Node node : routeTable.getNodes().getNode()) {    // ������� ���� � ������ 'DEFAULT_COLLECTOR'
			if (DEFAULT_COLLECTOR.equals(node.getName())) {
				defaultCollectorResource = getCollector(node);
				break;
			}
		}
		if (defaultCollectorResource == null) {
			String message = "Route table has no default collector OR default collector node has no \'collector\' resource.";
			logger.error(message);
			throw new RouteTableException(message);
		}

		for (Node node : routeTable.getNodes().getNode()) {    // �������� �� ���� ����� � ���� ���� ��������� defaultCollectorResource
			if (getCollector(node) == null) {                // � ���� �� �������� ������ � ����� 'collector'
				node.getResources().getResource().add(defaultCollectorResource);
			}
		}
	}

	private Resource getCollector(Node node) {
		for (Resource resource : node.getResources().getResource()) {
			if (resource.getType().equals(ResourceType.COLLECTOR)) {
				return resource;
			}
		}
		return null;
	}

	private void buildRouteRules() throws RouteTableException {
		routeRulesMap.clear();
		for (Node node : routeTable.getNodes().getNode()) {    // �������� �� ���� ����� � ���� ���� ��������� defaultCollectorResource
			if (node.getType().equals(NodeType.OUT)) {
				for (Destination destination : node.getDestination()) {
					try {
						String uuidString = destination.getUuid();
						if (DEFAULT_UUID.equals(uuidString)) {    // ��������� ���� �� ���������
							if (defaultNode != null) {        // ���� �� ��������� ��� ��� ���������� ? - ������ !
								String message = "There are two or more \'default\' destinations in RouteTable !";
								logger.error(message);
								throw new RouteTableException(message);
							}
							defaultNode = node;
							continue;
						}

						List<Node> nodeList;
						if (routeRulesMap.containsKey(uuidString)) {
							nodeList = routeRulesMap.get(uuidString);
						} else {
							nodeList = new ArrayList<Node>();
						}
						nodeList.add(node);

						Collections.sort(nodeList, new Comparator<Node>() {
							@Override
							public int compare(Node node1, Node node2) {
								return node1.getOrder() - node2.getOrder();
							}
						});
						routeRulesMap.put(uuidString, nodeList);
					} catch (IllegalArgumentException e) {
						String message = "A destination of node " + node.getName() + " has invalid format of \'uuid\' parameter.";
						logger.error(message);
						throw new RouteTableException(message, e);
					}
				}
			}
		}
	}
}
