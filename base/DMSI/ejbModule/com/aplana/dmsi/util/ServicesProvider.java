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
package com.aplana.dmsi.util;

import java.rmi.RemoteException;
import java.security.Principal;

import javax.ejb.CreateException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataService;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.DataServiceHome;
import com.aplana.dbmi.service.ServiceException;

public final class ServicesProvider {

    protected final static Log logger = LogFactory
	    .getLog(ServicesProvider.class);

    /**
     * Returns DataServiceBean instance
     *
     * @return DataServiceBean instance or null if some error was occurred
     *         during initialization
     * @throws ServiceException
     */
    public static DataServiceBean serviceBeanInstance(Principal user)
	    throws ServiceException {

		DataServiceBean serviceBean;
	
		InitialContext context = null;
		DataServiceHome home = null;
		try {
		    context = new InitialContext();
		    home = (DataServiceHome) PortableRemoteObject.narrow(context
			    .lookup("ejb/dbmi"), DataServiceHome.class);
		} catch (NamingException ex) {
		    throw new ServiceException(
			    "Error during DataServiceHome context initialization", ex);
	
		}
		if (home == null)
		    return null;
	
		try {
		    DataService service = home.create();
		    serviceBean = new DataServiceBean();
		    serviceBean.setService(service, service.authUser((user),
			    "127.0.0.1"));
		} catch (RemoteException ex) {
		    throw new ServiceException(ex);
		} catch (CreateException ex) {
		    throw new ServiceException(ex);
		} catch (DataException ex) {
		    throw new ServiceException(ex);
		}
	
		return serviceBean;
    }
}
