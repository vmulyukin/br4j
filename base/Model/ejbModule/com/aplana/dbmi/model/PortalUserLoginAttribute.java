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

/**
 * {@link Attribute} descendant, used to store portal user login string values.
 * In GUI shows as single text field 
 */
public class PortalUserLoginAttribute extends StringAttribute implements PseudoAttribute {
	private static final long serialVersionUID = 4L;
	/**
	 * @see Attribute#getType()
	 */
	public Object getType() {
		return TYPE_PORTAL_USER_LOGIN;
	}
}
