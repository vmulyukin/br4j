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
package com.aplana.dbmi.model;

import java.util.Date;
import java.util.HashMap;

public class Message extends DataObject {

	private static final long serialVersionUID = 1L;
	
	private Date sendTime;
	private Date readTime;
	private Person recipient;
	private Person sender;
	private String text;
	private MessageGroup group;
	private ObjectId messageEventCardId;
	
	public Message setId(long id) {
		super.setId(new ObjectId(Message.class, id));
		return this;
	}

	public Date getSendTime() {
		return sendTime;
	}
	
	public Message setSendTime(Date sendTime) {
		this.sendTime = sendTime;
		return this;
	}
	
	public Date getReadTime() {
		return readTime;
	}
	
	public Message setReadTime(Date readTime) {
		this.readTime = readTime;
		return this;
	}
	
	public boolean isRead() {
		return readTime == null;
	}
	
	public Message setRead() {
		if (readTime == null)
			readTime = new Date();
		return this;
	}
	
	public Person getRecipient() {
		return recipient;
	}
	
	public Message setRecipient(Person recipient) {
		this.recipient = recipient;
		return this;
	}
	
	public Person getSender() {
		return sender;
	}
	
	public Message setSender(Person sender) {
		this.sender = sender;
		return this;
	}

	public String getText() {
		return text;
	}
	
	public Message setText(String text) {
		this.text = text;
		return this;
	}

	public MessageGroup getGroup() {
		return group;
	}

	public Message setGroup(MessageGroup group) {
		this.group = group;
		return this;
	}

	public ObjectId getMessageEventCardId() {
		return messageEventCardId;
	}

	public void setMessageEventCardId(ObjectId messageEventCardId) {
		this.messageEventCardId = messageEventCardId;
	}

}
