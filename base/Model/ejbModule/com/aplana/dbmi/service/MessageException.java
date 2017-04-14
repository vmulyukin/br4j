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
package com.aplana.dbmi.service;

import java.util.ArrayList;
import java.util.List;

import com.aplana.dbmi.model.ObjectId;

/*
 * ������������ ��� ������ ��������� � ��������� ����������� ��������� (��. CardPortlet.java)
 */
public class MessageException extends DataException {

	private static final long serialVersionUID = 1L;

	/**
	 * @param cause
	 */
	public MessageException(Throwable cause) {
		super(cause);
	}

	public MessageException(String msg, List<ObjectId> container){
		super(msg);
		this.container = container;
	};

	/**
	 * @param msgId
	 * @param params
	 * @param cause
	 */
	public MessageException(String msgId, Object[] params, List<ObjectId> container, Throwable cause) {
		super(msgId, params, cause);
		this.container = container;
	}

	/**
	 * @param msgId
	 * @param params
	 */
	public MessageException(String msgId, Object[] params, List<ObjectId> container) {
		this(msgId, params, container, null);
	}

	/**
	 * @param msgId
	 * @param cause
	 */
	public MessageException(String msgId, List<ObjectId> container, Throwable cause) {
		this(msgId, null, container, cause);
	}

	protected List<ObjectId> container = new ArrayList<ObjectId>();

	/**
	 * 
	 * @return List<ObjectId>
	 */
	public List<ObjectId> getContainer(){
		return container;
	}
	
}
