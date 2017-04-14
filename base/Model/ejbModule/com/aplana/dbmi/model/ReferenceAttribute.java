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

import java.util.Collection;

/**
 * Abstract {@link Attribute} class descendant.
 * Should be used as the base class for attributes
 * which values are taken from {@link Reference} dictionaries
 * See {@link ListAttribute}, {@link TreeAttribute}   
 */
public abstract class ReferenceAttribute extends Attribute implements ReferenceConsumer {
	private static final long serialVersionUID = 1L;
	private ObjectId reference;
	private Collection<ReferenceValue> referenceValues;
	private boolean restrictedList;

	/**
	 * Gets identifier of dictionary used by this attribute instance
	 * @return identifier of {@link Reference} object used by this attribute
	 */
	public ObjectId getReference() {
		return reference;
	}

	/**
	 * Gets list of available values
	 * @return list of {@link ReferenceValue} object that could be used as a values 
	 * for this attribute instance
	 */
	public Collection<ReferenceValue> getReferenceValues() {
		return referenceValues;
	}

	/**
	 * Sets identifier of dictionary to be used as the source of available values
	 * for this attribute instance 
	 */
	public void setReference(ObjectId reference) {
		this.reference = reference;
	}

	/**
	 * Sets list of available values for this attribute instance
	 */
	public void setReferenceValues(Collection<ReferenceValue> referenceValues) {
		this.referenceValues = referenceValues;
	}

	/**
	 * Checks if this attribute could have only values already specified
	 * in associated dictionary.
	 * If value of isRestrictedList flag is false,
	 * then user have an option to specify his own value 
	 * (see {@link ReferenceValue#newAnotherValue(String)}
	 * @return true if only already specified values allowed, false otherwise
	 */
	public boolean isRestrictedList() {
		return restrictedList;
	}

	/**
	 * Sets isRestrictedList flag of attribute instance
	 * If value of isRestrictedList flag is false,
	 * then user have an option to specify his own value 
	 * (see {@link ReferenceValue#newAnotherValue(String)}
	 * @param restrictedList desired value of isRestrictedList flag
	 */
	public void setRestrictedList(boolean restrictedList) {
		this.restrictedList = restrictedList;
	}
}
