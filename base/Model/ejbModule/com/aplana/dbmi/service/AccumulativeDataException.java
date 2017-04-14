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
import java.util.Iterator;

/**
 * Represents exception that accumulates {@link DataException}-s and concatenates their messages in "getMessage" method.
 */
public class AccumulativeDataException extends Exception {

	private static final long serialVersionUID = 1L;
	private ArrayList<DataException> exceptionsList = new ArrayList<DataException>();

	/**
	 * Default constructor. Creates default exception object.
	 */
	public AccumulativeDataException() {
	}

	/**
	 * Appends a new exception to the existing ones.
	 * 
	 * @param exc
	 */
	public void addException(DataException exc) {
		exceptionsList.add(exc);
	}

	/**
	 * Returns message, concatenated from accumulated exceptions messages.
	 */
	public String getMessage() {
		StringBuilder theMsg = new StringBuilder();
		if (exceptionsList.size() > 0) {
			for (Iterator<DataException> iterator = exceptionsList.iterator(); iterator.hasNext();) {
				DataException exc = iterator.next();
				theMsg.append(exc.getMessage());
				if(iterator.hasNext()) {
					theMsg.append("\n");
				}
			}
		}
		return theMsg.toString();
	}

	/**
	 * Returns accumulated exceptions quantity.
	 * 
	 * @return accumulated exceptions quantity
	 */
	public int getExceptionsQuantity() {
		return exceptionsList.size();
	}
}
