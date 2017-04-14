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
package com.aplana.distrmanager.exceptions;

import com.aplana.dbmi.service.DataException;

public class ChangeStateException extends DataException {
	private static final String DEFAULT_ID = "jbr.distrmanager.change.state";
	 
    public ChangeStateException() {
    	super(DEFAULT_ID);
        }

        public ChangeStateException(Throwable cause) {
    	super(DEFAULT_ID, cause);
        }

        public ChangeStateException(String message) {
    	super(message);
        }

        public ChangeStateException(String msgId, Object[] params) {
    	super(msgId, params);
        }

        public ChangeStateException(String msgId, Object[] params, Throwable cause) {
    	super(msgId, params, cause);
        }

        public ChangeStateException(String message, Throwable cause) {
    	super(message, cause);
        }

        @Override
        public String getLocalizedMessage() {
    	if (getCause() != null)
    	    return String.format("%s\n%s", super.getLocalizedMessage(),
    		    getCause().getLocalizedMessage());
    	return super.getLocalizedMessage();
        }
}