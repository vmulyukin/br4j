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
package com.aplana.crypto;

import java.text.MessageFormat;
import java.util.ResourceBundle;

public class AppletException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private static final String DEFAULT_KEY = "default";
    private static final String MESSAGES_NAME = "com.aplana.crypto.nls.exceptions";

    public AppletException() {
	this(DEFAULT_KEY);
    }

    public AppletException(String message, Object... parameters) {
	this(message, null, parameters);
    }

    public AppletException(String message, Throwable cause,
	    Object... parameters) {
	super(MessageFormat.format(ResourceBundle.getBundle(MESSAGES_NAME)
		.getString(message), parameters), cause);
    }

    public AppletException(Throwable cause) {
	this(DEFAULT_KEY, cause);
    }

    @Override
    public String getMessage() {
	StringBuilder messageBuilder = new StringBuilder(super.getMessage());
	Throwable ex = this;
	while (ex.getCause() != null) {
	    ex = ex.getCause();
	    messageBuilder.append("\n" + ex.getMessage());
	}
	return messageBuilder.toString();
    }

    @Override
    public String toString() {
	return getMessage();
    }
}
