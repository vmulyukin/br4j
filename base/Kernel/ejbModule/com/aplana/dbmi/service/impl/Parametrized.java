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
package com.aplana.dbmi.service.impl;

/**
 * Interface to be implemented by parametrized processors
 * ({@link QueryBase}, {@link ProcessorBase} or {@link AccessCheckerBase} descendants).
 * <br>
 * Parametrized processor is a processors whose behavior could be changed depending
 * on values of one or more parameters.
 * To define value of parameter {@link #setParameter(String, String)} value should be
 * used.
 */
public interface Parametrized
{
	/**
	 * Sets value of parameter on this processor implementation
	 * @param name name of parameter
	 * @param value value of parameter
	 */
	public void setParameter(String name, String value);
}
