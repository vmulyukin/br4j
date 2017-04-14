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
package com.aplana.agent.plugin;

import com.aplana.agent.*;
import com.aplana.agent.conf.DistributionLetterParser;
import com.aplana.agent.conf.DocumentBodyReader.DocType;
import com.aplana.agent.conf.RouteTableConfiguration;
import com.aplana.agent.conf.envelope.Letter;
import com.aplana.agent.conf.envelope.LetterType;
import com.aplana.agent.conf.routetable.Node;
import com.aplana.agent.conf.routetable.ResourceType;
import com.aplana.agent.conf.routetable.Resources.Resource;
import com.aplana.agent.plugin.sender.SendStatus;
import com.aplana.agent.util.FileProperties;
import com.aplana.agent.util.FilePropsBuilder;
import com.aplana.agent.util.FileUtility;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

/**
 * Executes plugins
 */
public class PluginExecutor implements StatefulJob {
	protected final Log logger = LogFactory.getLog(getClass());
	protected Plugin plugin;
	protected String pluginName;
	protected String pluginId;

	public PluginExecutor() {}

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		String defThreadName = Thread.currentThread().getName();
		try{
			TaskDefinition td = (TaskDefinition)context.getJobDetail().getJobDataMap().get(TaskContainer.TASK_DEF);
			pluginName = td.getAgentName();
			pluginId = td.getTaskName();
			Thread.currentThread().setName(td.getTaskName());
			
			if (StringUtils.isBlank(pluginName)) {
				throw new IllegalArgumentException("Bean " + pluginName + " is empty.");
			}
			if (!PluginFactory.containsBean(pluginName)) {
				throw new IllegalArgumentException("Unknown bean " + pluginName + " in Route Table. Up to date it with beans.xml");
			}
			if (logger.isDebugEnabled()){
				logger.debug("Initializing plugin " + pluginName);
			}
			plugin = PluginFactory.getBean(pluginName);
			
			setRuntimeConfiguration(plugin, td);
			
			cleanResources(td);
			
			if (!processGetMail(td)){
				return;
			}
			if (!processSendMail(td)){
				return;
			}
			
			td.setIteration(td.getIteration()+1);
			td.setNextStartTime(context.getNextFireTime());
		} catch (JobExecutionException e){		// ���� ���� ����������������� ����������, �� ���� �������� ��� �������
			e.setUnscheduleAllTriggers(false);
			throw e;
		} catch(Exception e) {
			throw new JobExecutionException(e, false);
		} finally {
			Thread.currentThread().setName(defThreadName);
		}
	}
	
	private void cleanResources(TaskDefinition td) {
		for (Node inNode : td.getNodes().getInNodes()) {
			try {
				//������ ������ - ������� �������� �� �������� ������ � �����
				if (td.getIteration() == 0) {
					if (plugin instanceof Router) {
						plugin.cleanResources(inNode);
					}
				}
			} catch (Exception e) {
				logger.error("Plugin " +plugin+ " can't cleanup resources.", e);
			}
		}
		for (Node outNode : td.getNodes().getOutNodes()) {
			try {
				//������ ������ - ������� �������� �� �������� ������ � �����
				if (td.getIteration() == 0) {
					if (!(plugin instanceof Router)) {
						plugin.cleanResources(outNode);
					}
				}
			} catch (Exception e) {
				logger.error("Plugin " +plugin+ " can't cleanup resources.", e);
			}
		}
	}
	
	private boolean processGetMail(TaskDefinition td) {
		for (Node inNode : td.getNodes().getInNodes()) {
			if (logger.isDebugEnabled()) {
				logger.debug("Agent \'" + pluginId + "\' is about to get mail from node \'" + inNode.getName() + "\'");
			}
			try {
				if (plugin.getMail(inNode)) {
					if (logger.isInfoEnabled()) {
						logger.info("Agent \'" + pluginId + "\' got mail from node \'" + inNode.getName() + "\'");
					}
				}else{
					if (logger.isInfoEnabled()) {
						logger.warn("Agent \'" + pluginId + "\' could not get mail from node \'" + inNode.getName()+"\'. Check route table configuration.");
					}
				}
			} catch (GetMailException e) {
				logger.warn("Agent \'" + pluginId + "\' could not get mail from node \'" + inNode.getName() + "\'. Shall try next time.");
			} catch (PluginException e) {
				logger.error("There is an error while executing \'getMail\' of agent \'" + pluginId 
						+ "\'. Stopping executing this agent.", e);
				return false;
			}
		}
		return true;
	}

	private boolean processSendMail(TaskDefinition td) throws JobExecutionException {
		RouteOrdersBean routeOrdersBean = (RouteOrdersBean)PluginFactory.getOrdinalBean("routeOrdersBean");
		// �������� � ���������� � ��������� ��� ����� ������� (������� ����: ��� ������ ��������� � ��� ����� ��), 
		// ������� ��������� � ����� ������. ��� ������� ������� ������� �������.
		List<RouteOrder> routeOrders;
		try {
			routeOrders = routeOrdersBean.take(td.getNodes().getOutNodes());
		} catch (RouterBeanException rbe) {
			throw new JobExecutionException(rbe);
		}
		if (logger.isInfoEnabled()){
			logger.info("Agent \'" + pluginId + "\' has " + routeOrders.size() + " orders to proceed.");
		}
		
		ListIterator<RouteOrder> ordersIterator = routeOrders.listIterator();
		try {
			while (ordersIterator.hasNext()) {
				RouteOrder routeOrder = ordersIterator.next();
				// ��������� ������ �� � ������� ��� ��������� ��������� ���������
				Collections.sort(routeOrder.getNodes(), new Comparator<Node>(){
					@Override
					public int compare(Node node1, Node node2) {
						return node1.getOrder() - node2.getOrder();
					}
				});
				// ������������ ������� �������� �������, � ����� ��������� �� ������� (node@order) ���� ����� ������
				Iterator<Node> itr = routeOrder.getNodes().iterator();
				Node currentOutNode = itr.next(); // �������� �������� ������� ��� ���������
				if (currentOutNode == null) {
					logger.warn("Agent \'" + pluginId + "\'. There is not any node route for letter " + routeOrder.getLetter() 
							+ "Very strange, check code.");
					break; // ��������� � ���������� ������� �� ������ ����� �� ������.
				}
				
				File letterFolder;
				try {
					URI letterURI = routeOrder.getLetter().toURI();
					letterFolder = new File(letterURI);
				} catch (URISyntaxException use) {
					String message = "There is an error while executing \'sendMail\' of agent \'" + pluginId 
							+ "\'. Target message " + routeOrder.getLetter() + " has invalid URL syntax. Stopping execution of this agent.";
					logger.error(message, use);
					return false;
				}
				
				if (!letterFolder.exists()) {
					ordersIterator.remove();
					logger.error("Directory " + letterFolder.getName() + " does not exists!. Ignoring this folder.");
					return false;
				}
				
				// ������ ��������� ����� ������� �������������� ����� ���� � ������������ ����� ��������
				Properties savedProps = new FilePropsBuilder().set(FileProperties.ATTEMPT, 1).build();
				try {
					if (FileUtility.isLocked(letterFolder)){ // ���������, �� ������������� �� ������� ��������� ���-�� ���
						String message = "There is an error while executing \'sendMail\' of agent \'" + pluginId 
								+ "\'. Target message " + routeOrder.getLetter() + " is locked. Stopping execution of this agent.";
						logger.error(message);
						throw new PluginException(message);
					}

					Properties props = new FilePropsBuilder().build();
					FileUtility.lockFolder(letterFolder, props); // ������ ��������� ��, ����� ����� �� ����������
					// ���������� �������� �������������� ����� ��� ��������� ����������� ������� � ������� ���
					savedProps = FileUtility.removeInQueueMarker(letterFolder);
					// ������ ��������� ��� ������ �� ����� �������
					Letter envelope = getEnvelope(routeOrder.getEnvelopeFile());
					
					if (!checkMaxRetries(savedProps, td.getRetries())) { // ���������� ������� �������� ��������� ?
						if (sendToTrash(letterFolder, currentOutNode)) { 	// ���������� ��������� � ������� ��, ���� ����
							ordersIterator.remove(); // ������� ��������� - ������� �� ���������� ������
							generateTicket(letterFolder, envelope, routeOrder.getLetterType(), currentOutNode, SendStatus.FAILURE);
						} else {		// ����������� � "�������" �� ������� �� ��� ��� ���� ��������
							ordersIterator.remove(); // ������� ���� ��������� - ������� �� ���������� ������, ����� ��������� ������ ������
							FileUtility.unlockFolder(letterFolder);	// ������������ ������
						}
						return true;
					}
					boolean res = false;
					while (!res) {	// �������� �� ��������� � ��������� ���������
						res = sendMail(routeOrder.getLetter(), currentOutNode);
						if (res) {
							ordersIterator.remove(); // ������� ��������� - ������� �� ���������� ������
							logger.info("Agent \'" + pluginId + "\'. Successfully sent letter " + routeOrder.getLetter() 
									+ " via node " + currentOutNode.getName() + " with order " + currentOutNode.getOrder());
							break;					// � ��������� ��������� ����� ������
						}
						logger.warn("Agent \'" + pluginId + "\'. Could not send letter " + routeOrder.getLetter() 
								+ " via node " + currentOutNode.getName() + " with order " + currentOutNode.getOrder());
						if (itr.hasNext()) {										// ��������� ��������� ��. (���� ����).
							currentOutNode = itr.next();
							logger.warn("Agent \'" + pluginId +"\'. Trying to send  letter " + routeOrder.getLetter() 
									+ " via reserve node " + currentOutNode.getName() + " with order " + currentOutNode.getOrder());
						} else {												// ��� ���������� ��. ���������� ��������� ����� �������
							logger.warn("Agent \'" + pluginId +"\'. No more reserve nodes for letter " + routeOrder.getLetter() 
									+ ". Shall try next time.");
							break;
						}
					}
					if (!res) {	// ��������� � ���� ��� �� �������
						savedProps.setProperty("ta.date.modified", new Date().toString()); //��������� ����� ����������� �������������� ������� �������
						FileUtility.markInQueue(letterFolder, savedProps);	// � ���������� ��� �� �����
						FileUtility.unlockFolder(letterFolder);				// � ������� ���� ����������
					} else {
						generateTicket(letterFolder, envelope, routeOrder.getLetterType(), 
								currentOutNode, SendStatus.SUCCESS);
					}
				} catch (SendMailException e) {
					logger.warn("Agent \'" + pluginId + "\' could not send mail to node \'" + currentOutNode.getName() + "\'. Shall try next time.");
					savedProps.setProperty("ta.date.modified", new Date().toString()); //��������� ����� ����������� �������������� ������� �������
					FileUtility.markInQueue(letterFolder, savedProps);	// � ���������� ��� �� �����
					FileUtility.unlockFolder(letterFolder);		// � ������� ���� ����������
				} catch (PluginException e) {
					logger.error("There is an error while executing \'sendMail\' of agent \'" + pluginId 
							+ "\'. Stopping execution of this agent.", e);
					savedProps.setProperty("ta.date.modified", new Date().toString()); //��������� ����� ����������� �������������� ������� �������
					FileUtility.markInQueue(letterFolder, savedProps);	// � ���������� ��� �� �����
					FileUtility.unlockFolder(letterFolder);		// � ������� ���� ����������
				} catch(Exception e) {
					throw new JobExecutionException(e, false);
				}
			}
		} finally {
			//���������� ������� ������������� ������� (���� ��� ����) ��� ���� ����� � ����. ��� ���������� �����
			routeOrdersBean.putRouteOrders(routeOrders);
		}
		return true;
	}

	private Letter getEnvelope(File envelopeFile) {
		if (envelopeFile == null) {
			return null;
		}
		try {
			Letter envelopeDocument = DistributionLetterParser.getInstance().unmarshal(
					new BufferedInputStream(new FileInputStream(envelopeFile)));
			return envelopeDocument;
		} catch (FileNotFoundException e) {
			logger.error("No such file " + envelopeFile, e);
		} catch (JAXBException e) {
			logger.error("Error while parsing file " + envelopeFile, e);
		} catch (NullPointerException e) { // � ��� ���������� ��������
		}
		return null;
	}

	// ����������� � �������, ���� ��� ����� �� ���� �� �����-�� �������� ��� �������
	private boolean sendToTrash(File letterFile, Node node) throws PluginException {
		try {
			Resource trashResource = getResource(node, ResourceType.TRASH);
			if (trashResource != null){
				URL trashURL = new URL(trashResource.getUrl());
				File trashFolder = FileUtility.checkDirOrMakeIt(new File(trashURL.toURI()));
				logger.warn("Moving letter " + letterFile + " to \'trash\' folder " + trashFolder.getAbsolutePath() + " of node \'" + node.getName() + "\'");
				FileUtility.copyDirectory(letterFile, trashFolder);
				FileUtility.silentDeleteDir(letterFile);
				//TODO �������� ���������� ������������� ������ ??
				FileUtility.unlockFolder(new File(trashFolder, letterFile.getName()));
				return true;
			} else {
				logger.warn("No \'trash\' resource defined for node " + node.getName() + ". Message " + letterFile + " stay in collector.");
				return false;
			}
		} catch (Exception e) {
			String message = "Could\'nt move letter " + letterFile + "to \'trash\' resourse of node \'" + node.getName() + "\'";
			logger.error(message, e);
		}
		return false;
	}

	/**
	 * ��������� ������� ���������� ������� �������� �, ���� �� ����������� ������������� � 1
	 * @param savedProps Properties, ��� ���������� ������� ������� ��������
	 * @param maxRetries ����������� �� ���������� �������
	 * @return true ����� ������� �� ���������� ��� �� ��������� �����������, false � ��������� ������.
	 */
	private boolean checkMaxRetries(Properties savedProps, Long maxRetries) {
		Integer attempt = 0;
		String attemptStr = savedProps.getProperty(FileProperties.ATTEMPT.toString());
		try {
			attempt = Integer.decode(attemptStr);
		} catch (NumberFormatException e1) {
		} catch (NullPointerException e1) {
		}
		savedProps.setProperty(FileProperties.ATTEMPT.toString(), (++attempt).toString());
		return attempt <= maxRetries;
	}

	protected boolean sendMail(URL letter, Node destination) throws PluginException{
		try {
			if (logger.isInfoEnabled()){
				logger.info("Agent \'" + pluginId + "\' is about to send mail "+ letter + " to node \'" + destination.getName() + "\'");
			}
			boolean res = plugin.sendMail(letter, destination);
			if (res){
				if (logger.isInfoEnabled()){
					logger.info("Agent \'" + pluginId + "\' successfully sent mail to node " + destination.getName() + "\'");
				}
			}else{
				if (logger.isInfoEnabled()){
					logger.info("Agent \'" + pluginId + "\' could not send mail to node " + destination.getName()+". Node type is not supported.");
				}
			}
			return res;
		} catch (SendMailException e) {
			logger.warn("Agent \'" + pluginId + "\' could not send mail to node " + destination.getName());
			return false;
		}
	}

	/**
	 * Generates ticket based on the status of sent message. 
	 * @param letter
	 * @param sendStatus
	 * @throws PluginException 
	 */
	public void generateTicket(File letter, Letter envelopeDocument, DocType type, Node node, SendStatus sendStatus) throws PluginException {
		if (envelopeDocument == null){	// ������� ��������������� ������: ���� ����� ��� ���� �� ������
			return;		// TODO ���� �� ������ ����� ��� ��������������� ������ ?
		}
		
		if (type.equals(DocType.TICKET)){
			 return;		// �� ��������/��������� ������ ������ ������ ��������� �� ����.
		}
		try {
			URL currentCollectorURL = null;
			File currentCollectorFolder = null;
			for (Resource resource : node.getResources().getResource()){
				if (resource.getType().equals(ResourceType.COLLECTOR)){
					try {
						currentCollectorURL = new URL(resource.getUrl());
						currentCollectorFolder = new File(currentCollectorURL.toURI());
						break;
					} catch (MalformedURLException e) { // ����� ����� �� ���������, ��������� ����
					} catch (URISyntaxException e) { //��������� ����
					}
				}
			}
			if ((currentCollectorURL == null) || (currentCollectorFolder == null)) {
				String message = "There is an error while executing \'sendMail\' of agent \'" + pluginId 
						+ "\'. Target message name " + letter + " has invalid URL syntax. Stopping execution of this agent.";
				logger.error(message);
				throw new PluginException(message);
			}
			
			modifyLetter(sendStatus, envelopeDocument);

			UUID uuid = UUID.randomUUID();
			File ticketSubfolder = new File(currentCollectorFolder, uuid.toString());

			String pluginId = plugin.getClass().getSimpleName();
			if (plugin.getEnvironment() != null ){
				pluginId = plugin.getEnvironment().getProperty(Plugin.AGENT_ID);
			}
			Properties props = new FilePropsBuilder()
				.set(FileProperties.TEXT, pluginId + "-ticket_generation")
				.set(FileProperties.PARTIAL, true)
				.build();
			FileUtility.lockFolder(ticketSubfolder, props, true);
			File ticketFile = new File(ticketSubfolder, DocType.TICKET.getFileName());
			DistributionLetterParser.getInstance().marshal(envelopeDocument, ticketFile);
			FileUtility.unlockFolder(ticketSubfolder);
			logger.info("Ticket was generated for message: " + letter + " in collector");
		} catch (JAXBException e) {
			logger.error("Error while parsing/saving distribution letter: " + e.getMessage(), e);
		} catch (DatatypeConfigurationException e) {
			logger.error("No DataTypeFactory configuration found. Please check it.", e);
		}
	}

	private void modifyLetter(SendStatus sendStatus, Letter letter) throws DatatypeConfigurationException {
		letter.setType(convertStatus(sendStatus));
		String sederName = letter.getAddressee().getName();
		String senderGuid = letter.getAddressee().getGuid();
		letter.getAddressee().setName(letter.getSender().getName());
		letter.getAddressee().setGuid(letter.getSender().getGuid());
		letter.getSender().setName(sederName);
		letter.getSender().setGuid(senderGuid);
		letter.setId(UUID.randomUUID().toString());
		letter.setDate(DatatypeFactory.newInstance().newXMLGregorianCalendar((GregorianCalendar)Calendar.getInstance()));
	}

	/**
	 * Converts from {@link SendStatus} to {@link LetterType}.
	 * @author atsvetkov
	 *
	 */
	public LetterType convertStatus(SendStatus sendStatus) {
		switch (sendStatus) {
			case SUCCESS: return LetterType.SENT;
			case FAILURE: return LetterType.NOT_SENT;
			default: return LetterType.NOT_SENT;
		}
	}

	private void setRuntimeConfiguration(Plugin plugin, TaskDefinition td) {
		plugin.setEnvironment(getRuntimeEnvironment());
		
		if ((td.getConfiguration() != null)
				&& !td.getConfiguration().isEmpty()){
			plugin.setConfiguration(new File(td.getConfiguration()));
		}
		if (logger.isDebugEnabled()){
			logger.debug("Agent \'" + pluginId + "\' initialized and configured.");
		}
	}

	private Properties getRuntimeEnvironment(){
		RouteTableConfiguration rtc = RouteTableConfiguration.getInstance();

		Properties pluginRuntimeEnvironment = new Properties();
		pluginRuntimeEnvironment.put(Plugin.AGENT_NAME, pluginName);
		pluginRuntimeEnvironment.put(Plugin.AGENT_ID, pluginId);
		pluginRuntimeEnvironment.put(Plugin.TRANSPORT_AGENT_NAME, rtc.getTransportAgentName());
		pluginRuntimeEnvironment.put(Plugin.TRANSPORT_AGENT_UUID, rtc.getTransportAgentUUID());
		
		return pluginRuntimeEnvironment;
	}
	
	private Resource getResource(Node node, ResourceType nodeType) {
		for (Resource res : node.getResources().getResource()) {
			if (res.getType() == nodeType) {
				return res;
			}
		}
		return null;
	}

}
