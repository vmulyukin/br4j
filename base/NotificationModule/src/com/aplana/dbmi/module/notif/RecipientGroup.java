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
package com.aplana.dbmi.module.notif;

import java.util.Collection;

/**
 * This interface should be implemented by any object intended for use in
 * {@link com.aplana.dbmi.module.notif.NotificationBean#setRecipients(Collection) NotificationBean.recipients}
 * collection.
 * 
 * @author apirozhkov
 */
public interface RecipientGroup
{
	/**
	 * Returns collection of {@link com.aplana.dbmi.model.Person} objects
	 * depending on implementing object's state and specific notification object.
	 *  
	 * @param object A notification object
	 * @return Collection of {@link com.aplana.dbmi.model.Person} objects
	 */
	public Collection discloseRecipients(NotificationObject object);
}
