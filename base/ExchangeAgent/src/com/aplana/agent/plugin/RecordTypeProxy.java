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

import com.aplana.agent.conf.delivery.*;
import com.aplana.agent.util.FileUtility;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class RecordTypeProxy {
	RecordType record;

	public static RecordTypeProxy getInstance(){
		return new RecordTypeProxy();
	}

	private RecordTypeProxy() {
		record = new RecordType();
	}

	public RecordTypeProxy setDate(Date date) throws DatatypeConfigurationException {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		record.setDate(DatatypeFactory.newInstance().newXMLGregorianCalendar((GregorianCalendar) cal));
		return this;
	}

	public RecordTypeProxy setTransportAgent(String name, String uuid){
		TransportAgentType transport = new TransportAgentType();
		transport.setName(name);
		transport.setUuid(uuid);
		record.setTransportAgent(transport);
		return this;
	}

	public RecordTypeProxy setTransportAgentName(String name){
		TransportAgentType transport;
		if (record.getTransportAgent() == null){
			transport = new TransportAgentType();
		} else {
			transport = record.getTransportAgent();
		}
		transport.setName(name);
		record.setTransportAgent(transport);
		return this;
	}

	public RecordTypeProxy setTransportAgentUuid(String uuid){
		TransportAgentType transport;
		if (record.getTransportAgent() == null){
			transport = new TransportAgentType();
		} else {
			transport = record.getTransportAgent();
		}
		transport.setUuid(uuid);
		record.setTransportAgent(transport);
		return this;
	}

	public RecordTypeProxy setAgentName(String name){
		AgentType agent;
		if (record.getAgent() == null){
			agent = new AgentType();
		} else {
			agent = record.getAgent();
		}
		agent.setName(name);
		record.setAgent(agent);
		return this;
	}

	public RecordTypeProxy setActionType(IdType type){
		ActionType action;
		if (record.getAction() == null){
			action = new ActionType();
		} else {
			action = record.getAction();
		}
		action.setId(type);
		record.setAction(action);
		return this;
	}

	public RecordTypeProxy setToURL(File toDir) throws MalformedURLException{
		ToType to;
		if (record.getTo() == null){
			to = new ToType();
		} else {
			to = record.getTo();
		}
		to.setUrl(toDir.toURI().toURL().toString());
		record.setTo(to);
		return this;
	}

	public RecordTypeProxy setToURL(URL toUrl){
		ToType to;
		if (record.getTo() == null){
			to = new ToType();
		} else {
			to = record.getTo();
		}
		to.setUrl(toUrl.toString());
		record.setTo(to);
		return this;
	}

	public RecordTypeProxy setToURL(String toUrl){
		ToType to;
		if (record.getTo() == null){
			to = new ToType();
		} else {
			to = record.getTo();
		}
		to.setUrl(toUrl);
		record.setTo(to);
		return this;
	}

	public RecordTypeProxy setToNodeName(String name){
		ToType to;
		if (record.getTo() == null){
			to = new ToType();
		} else {
			to = record.getTo();
		}
		to.setNodeName(name);
		record.setTo(to);
		return this;
	}

	public RecordTypeProxy setFromURL(File fromDir) throws MalformedURLException{
		FromType from;
		if (record.getFrom() == null){
			from = new FromType();
		} else {
			from = record.getFrom();
		}
		from.setUrl(fromDir.toURI().toURL().toString());
		record.setFrom(from);
		return this;
	}

	public RecordTypeProxy setFromURL(URL fromUrl){
		FromType from;
		if (record.getFrom() == null){
			from = new FromType();
		} else {
			from = record.getFrom();
		}
		from.setUrl(fromUrl.toString());
		record.setFrom(from);
		return this;
	}

	public RecordTypeProxy setFromURL(String fromUrl){
		FromType from;
		if (record.getFrom() == null){
			from = new FromType();
		} else {
			from = record.getFrom();
		}
		from.setUrl(fromUrl);
		record.setFrom(from);
		return this;
	}

	public RecordTypeProxy setFromNodeName(String name){
		FromType from;
		if (record.getFrom() == null){
			from = new FromType();
		} else {
			from = record.getFrom();
		}
		from.setNodeName(name);
		record.setFrom(from);
		return this;
	}

	public RecordTypeProxy setPacketName(String name){
		PacketType packet;
		if (record.getPacket() == null){
			packet = new PacketType();
		} else {
			packet = record.getPacket();
		}
		packet.setName(name);
		record.setPacket(packet);
		return this;
	}

	public RecordTypeProxy setPacketId(String id){
		PacketType packet;
		if (record.getPacket() == null){
			packet = new PacketType();
		} else {
			packet = record.getPacket();
		}
		packet.setMessageId(id);
		record.setPacket(packet);
		return this;
	}

	public RecordTypeProxy setLetterContent(File letterLocation) throws IOException {
		PacketType packet;
		if (record.getPacket() == null) {
			packet = new PacketType();
			record.setPacket(packet);
		} else {
			packet = record.getPacket();
		}

		processDirMD5(letterLocation, packet, "");

		return this;
	}

	private void processDirMD5(File dir, PacketType packet, String accumulatedPath) throws IOException {
		final File[] files = dir.listFiles();
		if (files == null) {
			return;
		}
		for(File listItem : files) {
			if (listItem.isDirectory()) {
				processDirMD5(listItem, packet, accumulatedPath + listItem.getName() + "/");
			} else {
				if (!FileUtility.isSpecialFile(listItem)) {
					FileType file = new FileType();
					file.setName(accumulatedPath + listItem.getName());
					file.setSize(listItem.length());

					InputStream is = new FileInputStream(listItem);
					try {
						String md5 = DigestUtils.md5Hex(is);
						file.setMd5(md5);
					} finally {
						IOUtils.closeQuietly(is);
					}

					packet.getFile().add(file);
				}
			}
		}
	}

	public RecordType getRecord(){
		return record;
	}
}
