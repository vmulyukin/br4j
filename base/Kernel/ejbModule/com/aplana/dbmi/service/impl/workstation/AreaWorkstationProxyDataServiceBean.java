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
package com.aplana.dbmi.service.impl.workstation;

import java.beans.Expression;
import java.lang.reflect.UndeclaredThrowableException;


import javax.ejb.CreateException;
import javax.ejb.SessionContext;

import org.springframework.context.access.ContextSingletonBeanFactoryLocator;
import org.springframework.ejb.support.AbstractStatelessSessionBean;

import com.aplana.dbmi.service.ServiceException;
import com.aplana.dbmi.service.workstation.AreaWorkstationDataServiceInterface;

/**
 * Represents AreaWorkstation Proxy EJB Session Bean.
 * 
 *
 */
public class AreaWorkstationProxyDataServiceBean extends
		AbstractStatelessSessionBean {
	
	
	@Override
	protected void onEjbCreate() throws CreateException {
		
	}
	
	

	private AreaWorkstationDataServiceInterface getAreaWorkStationDataService(String serviceName) {
		
		return (AreaWorkstationDataServiceInterface) getBeanFactory().getBean(serviceName);
		
	}
	
	
	private Object getService(String serviceName) {
		
		return  getBeanFactory().getBean(serviceName);
		
	}	
	
	

	@Override
	public void setSessionContext(SessionContext sessionContext) {
		
		super.setSessionContext(sessionContext);
		//uses singleton spring factory loader to avoid multiple spring configuration files loadings
		setBeanFactoryLocator(ContextSingletonBeanFactoryLocator.getInstance());
		setBeanFactoryLocatorKey("businessBeanFactory");
	}
	
	
	
    public Object invoke(String serviceName, String methodName, Object[] args) throws Exception
    {
        
        Object serviceToInvoke = getService(serviceName);

        Object result;

        try
        {
            Expression expression = new Expression(serviceToInvoke, methodName, args);
            result = expression.getValue();
        }
        catch(UndeclaredThrowableException undeclaredEx)
        {
            Exception convertedEx;
            
            if(undeclaredEx.getCause() instanceof Exception)
                convertedEx  = (Exception) undeclaredEx.getCause();
            else
                convertedEx = new ServiceException(undeclaredEx.getCause());
            
            throw convertedEx;
        }

        return result;
    }
	

	
	
	
	
	

}
