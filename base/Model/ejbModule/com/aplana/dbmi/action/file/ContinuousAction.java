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
package com.aplana.dbmi.action.file;

import com.aplana.dbmi.action.Action;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataService;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.dbmi.service.User;

/**
 * This interface should be used to implement actions which requires extra initialization
 * (see {@link #beforeMainAction()}) or finalization
 * (see {@link #afterMainAction(Object)}) steps. <br>
 * Each ContinousAction implementation could have a reference to {@link DataServiceBean}
 * object initialized with credentials of user who performed action.
 * This DataServiceBean instance could be used in initialization/finalization steps
 * <br>NOTE: all actions implementing ContinuousAction interface must be executed via
 * {@link com.aplana.dbmi.service.DataServiceBean#doAction(Action)} call.
 */
public interface ContinuousAction
{
	/**
	 * Creates new {@link DataServiceBean} object initialized with given
	 * EJB reference and user information. This DataServiceBean will be used in
	 * in {@link #beforeMainAction()} and {@link #afterMainAction(Object)} methods
	 * @param service {@link DataService} EJB remote interface instance
	 * @param user information about user, who performs this action
	 */
	public void setService(DataService service, User user);
	/**
	 * Sets ActionPerformer to be used in {@link #beforeMainAction()} and
	 * {@link #afterMainAction(Object)} methods
	 * @param service {@link ActionPerformer} correctly initialized service
	 * that can do actions
	 */
	public void setService(ActionPerformer service);
	/**
	 * Initialization method to be called before execution of action itself.
	 * If this method returns false, then execution of action will be terminated
	 * and null be returned as the result of action.
	 * @return true if action could be proceeded or false otherwise
	 * @throws DataException in case of business-logic error
	 * @throws ServiceException in case of communication error
	 */
	public boolean beforeMainAction() throws DataException, ServiceException;
	/**
	 * Finalization method to be called after execution of action itself.
	 * @throws DataException in case of business-logic error
	 * @throws ServiceException in case of communication error
	 */
	public void afterMainAction(Object result) throws DataException, ServiceException;
}
