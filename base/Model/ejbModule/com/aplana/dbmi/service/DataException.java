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

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.model.ContextProvider;

/**
 * Base class for all exception thrown in system.
 * Message should contain human-readable message which will be shown to user in case of error.
 * Usually used to wrap other caught exception of other types,
 * or to notify user of some error in business-logic level<br>
 * Messages shown by this exception is internationalized and should be defined through
 * properties files (see com/aplana/dbmi/nls/exceptions_*.properties files).<br>
 * Messages in properties file could be simple strings as well as message patterns with
 * placeholders for parameters.
 */
public class DataException extends Exception
{
	public static final String RESOURCE_PREFIX = "@ID:";
	public static final String ID_GENERAL_ACCESS = "general.access";
	public static final String ID_GENERAL_RUNTIME = "general.runtime";
	
	private static final long serialVersionUID = 1L;
	private static final String RESOURCE_BUNDLE = "nls.exceptions";
	private static final String ID_GENERAL = "general";
	private static final Log logger = LogFactory.getLog(DataException.class);
	
	private Object[] params;

	/**
	 * Default constructor.
	 * Creates exception object with message found in properties-file under key "general"
	 */
	public DataException() {
		super(ID_GENERAL);
	}
	

	/**
	 * Creates exception with given message  
	 * @param msgId key of the required message in nls *.properties file
	 */
	public DataException(String msgId) {
		super(msgId != null ? msgId : ID_GENERAL);
	}

	/**
	 * Takes a set of objects and inserts the formatted strings
	 * into the given message pattern at the appropriate places
	 * ({like @link java.text.MessageFormat#format(String, Object[])})
	 * Resulting string is used as the exception message 
	 * @param msgId key of the required message pattern in nls *.properties file.
	 * This pattern should have several placeholders for parameters 
	 * @param params set of object to replace placeholders in message string.
	 * If one of parameters is a string starting with {@link #RESOURCE_PREFIX} then 
	 * value of this parameter will be replaced with a string stored in 
	 * properties file under key equals to parameter's ending   
	 */
	public DataException(String msgId, Object[] params) {
		super(msgId != null ? msgId : ID_GENERAL);
		this.params = params;
	}
	
	/**
	 * Wraps Throwable object with DataException.
	 * For a message uses message defined in properties-file under key "general"
	 * @param cause Throwable object to be wrapped
	 */
	public DataException(Throwable cause) {
		super(ID_GENERAL, cause);
	}

	/**
	 * Wraps Throwable object with DataException and sets given message for it.
	 * @param msgId key of the required message in nls *.properties file
	 * @param cause Throwable object to be wrapped
	 */
	public DataException(String msgId, Throwable cause) {
		super(msgId != null ? msgId : ID_GENERAL, cause);
	}
	
	/**
	 * Wraps Throwable object with DataException.
	 * For the message uses formatted string received after replacing all placeholders
	 * in given message pattern with given parameters. 
	 * @param msgId key of the required message pattern in nls *.properties file.
	 * This message should have several placeholders for parameters
	 * @param params set of object to replace placeholders in message pattern string
	 * If one of parameters is a string starting with {@link #RESOURCE_PREFIX} then 
	 * value of this parameter will be replaced with a string stored in 
	 * properties file under key equals to parameter's ending   
	 * @param cause Throwable object to be wrapped
	 */
	public DataException(String msgId, Object[] params, Throwable cause) {
		super(msgId != null ? msgId : ID_GENERAL, cause);
		this.params = params;
	}
	
	/**
	 * Returns key of the message pattern used by this exception object
	 * @return key of the message pattern in properties file
	 */
	public String getMessageID() {
		return super.getMessage();
	}

	/**
	 * @see #getMessage() 
	 */
	public String getLocalizedMessage() {
		return getMessage();
	}

	/**
	 * Returns localized message of exception using given message key and set of parameters
	 * Resource bundle to use is defined by 
	 * (@link com.aplana.dbmi.model.ContextProvider  caller's locale context}
	 */
	public String getMessage()
	{
		try {
			ResourceBundle rb = getResourceBundle();
			if (params == null)
				return rb.getString(super.getMessage());
			Object[] translated = new Object[params.length];
			for (int i = 0; i < params.length; i++) {
				translated[i] = params[i];
				if (params[i] != null && params[i].toString().startsWith(RESOURCE_PREFIX))
					try {
						translated[i] = rb.getString(params[i].toString().substring(RESOURCE_PREFIX.length()));
					} catch (MissingResourceException e) {
						logger.warn("Missing resource: " + params[i].toString().substring(RESOURCE_PREFIX.length()));
					}
			}
			return MessageFormat.format(rb.getString(super.getMessage()), translated);
		} catch (MissingResourceException e) {
			logger.warn("Missing resource: " + super.getMessage());
			return super.getMessage();
		}
	}

	protected ResourceBundle getResourceBundle() {
		return ResourceBundle.getBundle(RESOURCE_BUNDLE, ContextProvider.getContext().getLocale());		
	}
}
