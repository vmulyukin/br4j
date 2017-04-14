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
package ru.datateh.jbr.iuh.msg;

import java.util.Arrays;

public class Message {
	
	private MessageType state = MessageType.UNDEFINED;
	private String text;
	private Throwable exception;
	
	public Message(MessageType state, String text) {
		this.state = state;
		this.text = text;
	}
	
	public Message(MessageType state, Throwable exception) {
		this.state = state;
		this.text = exception.getMessage();
		this.exception = exception;
	}

	public Message(MessageType state, String text, Throwable exception) {
		this.state = state;
		this.text = text;
		this.exception = exception;
	}

	public Message(String text) {
		this.text = text;
	}
	
	public MessageType getState() {
		return state;
	}
	
	public void setState(MessageType state) {
		this.state = state;
	}
	
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}

	@Override
	public String toString() {
		return text;
	}
	
	public String getFullString() {
		return "State: " + state.getText()
				+ ", Message: " + text;
	}
	
	public String getDetailedMessage(){
		if (exception == null){
			return this.toString();
		} else {
			return this.getFullString() + '\n' + Arrays.toString(exception.getStackTrace());
		}
	}

}
