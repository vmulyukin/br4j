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

import java.util.ResourceBundle;

import com.aplana.dbmi.model.ContextProvider;

/**
 * ������ ���������� ������������ ��� ����������� ���������� ������� ������� �
 * �������� ������ ������ ��������-��������. �������� ������������ ��� ���������
 * �������� �� ������.
 * @author nzhegalin
 */
public class DMSIException extends Exception {
    private static final long serialVersionUID = 1L;

    private static final String DEFAULT_MESSAGE = "Error is occurred. Check logs please.";
	private static final String RESOURCE_BUNDLE = "nls.dmsi_exceptions";

    public DMSIException() {
    	super(DEFAULT_MESSAGE);
    }

    public DMSIException(String message) {
    	super(message);
    }

    public DMSIException(Throwable cause) {
    	super(DEFAULT_MESSAGE, cause);
    }

    public DMSIException(String message, Throwable cause) {
    	super(message, cause);
    }

	@Override
	public String getMessage() {
		String message = super.getMessage();
		String messageById = tryGetMessageById(message);
		return messageById == null ? message : messageById;
}

	private String tryGetMessageById(String message) {
		try {
			ResourceBundle bundle = ResourceBundle.getBundle(RESOURCE_BUNDLE, ContextProvider.getContext().getLocale());
			return bundle.getString(message);
		} catch (RuntimeException ex) {
			return null;
		}
	}
}
