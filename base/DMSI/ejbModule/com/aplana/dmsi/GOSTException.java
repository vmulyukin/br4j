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
package com.aplana.dmsi;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dmsi.Configuration.GOSTMessage;
import com.aplana.dmsi.config.ConfigurationException;

/**
 * ������ ���������� ������������ ��� ����������� ������� ��������-��������,
 * ������� ����� ������� �������� ������������
 * @author nzhegalin
 */
public class GOSTException extends DMSIException {

	private static final long serialVersionUID = 1L;

	private Log logger = LogFactory.getLog(getClass());
	private static final String DEFAULT_CODE = "general";
	private GOSTMessage gostMessage = null;

	public GOSTException() {
		super(DEFAULT_CODE);
	}

	public GOSTException(Throwable cause) {
		super(DEFAULT_CODE, cause);
	}

	public GOSTException(String message) {
		super(message);
	}

	public GOSTException(String message, Throwable cause) {
		super(message, cause);
	}

	public Long getErrorCode() {
		return getGOSTMessage().getCode();
	}

	@Override
	public String getMessage() {
		return getGOSTMessage().getMessage();
	}

	private GOSTMessage getGOSTMessage() {
		if (this.gostMessage == null) {
			String messageCode = super.getMessage();
			try {
				this.gostMessage = Configuration.instance().getGOSTMessageByCode(messageCode);
			} catch (ConfigurationException ex) {
				if (logger.isWarnEnabled()) {
					logger.warn("There is no message with alias " + messageCode + ". Default will be used.", ex);
				}
				this.gostMessage = new GOSTMessage(getDefaultErrorCode(), getDefaultMessage());
			}
		}
		return this.gostMessage;
	}

	private Long getDefaultErrorCode() {
		return Long.valueOf(-1);
	}

	private String getDefaultMessage() {
		return "Unexpected error is occurred: " + super.getMessage();
	}
}
