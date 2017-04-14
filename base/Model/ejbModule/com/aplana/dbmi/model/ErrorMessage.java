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

import com.aplana.dbmi.service.DataException;

public class ErrorMessage extends LogEntry implements MessageEvent
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -306500854075092837L;
	private String message;
	private String description_message;
	private String code_message;
	private Long is_succes;
	
	public static final String MESSAGE_TYPE = "ERROR";
	
	public ErrorMessage(Throwable exception, DataException dataException, LogEntry event) {
		this.setAddress(event.getAddress());
		this.setEvent(event.getEvent());
		this.setId(event.getId());
		this.setObject(event.getObject());
		this.setTimestamp(event.getTimestamp());
		this.setUser(event.getUser());
		this.setParentUid(event.getParentUid());
		this.setUid(event.getUid());
		this.description_message = dataException.getLocalizedMessage();
		this.code_message = dataException.getMessageID();
		StringBuffer sb = new StringBuffer(exception.getClass().getName()).append(" ==> \r\n");
		final StackTraceElement[] st = exception.getStackTrace();
		for (StackTraceElement ste : st) {
			sb.append(ste.toString()).append("\r\n");
		}
		this.message = sb.toString();
	}
	
	public ErrorMessage(LogEntry event) {
		this.setAddress(event.getAddress());
		this.setEvent(event.getEvent());
		this.setId(event.getId());
		this.setObject(event.getObject());
		this.setTimestamp(event.getTimestamp());
		this.setUser(event.getUser());
	}
	
	public ErrorMessage() {

	}

	/**
	 * @return {@link #MESSAGE_TYPE}
	 */
	public String getMessageType() {
		return MESSAGE_TYPE;
	}

	public String getCodeMessage() {
		return code_message;
	}

	public void setCodeMessage(String code_message) {
		this.code_message = code_message;
	}

	public Long getSucces() {
		return is_succes;
	}

	public void isSucces(Long is_succes) {
		this.is_succes = is_succes;
	}

	/**
	 * Sets Message of logged action
	 * @param message message of logged action
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * Gets Message of logged action
	 * @return message message of logged action
	 */
	public String getMessage() {
		return message;
	}
	
	/**
	 * Sets description_message of logged action
	 * @param description_message description_message of logged action
	 */
	public void setDescriptionMessage(String description_message) {
		this.description_message = description_message;
	}

	/**
	 * Gets description_message of logged action
	 * @return description_message description_message of logged action
	 */
	public String getDescriptionMessage() {
		return description_message;
	}
}
