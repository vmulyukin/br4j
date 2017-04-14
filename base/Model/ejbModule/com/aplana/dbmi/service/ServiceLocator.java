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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import com.aplana.dbmi.service.proxy.AreaWorkstationProxy;

/**
 * Represents Service locator for returning Business interface of EJB
 * 
 * @author skashanski
 * 
 */
public class ServiceLocator {

	private static ServiceLocator me;
	private InitialContext context = null;
	
	private Map cache; //holds references to EJBHome


	public static final String JNDI_PREFIX = "ejb/";
	
	private static final String AREA_WORKSTATION_BEAN = "areaWorkstationBean-local";

	private ServiceLocator() throws ServiceException {

		try {

			context = new InitialContext();
			cache = Collections.synchronizedMap(new HashMap());


		} catch (NamingException e) {

			throw new ServiceException(e);

		}

	}

	public static ServiceLocator getInstance() throws ServiceException {

		if (me == null)
			me = new ServiceLocator();

		return me;
	}

	public String getJNDIName(String jndiName) {

		return JNDI_PREFIX + jndiName;

	}

	public Object getLocalEJB(String jndiName) throws ServiceException {

		try {
			
			Object home = null;
			
			if (cache.containsKey(jndiName)) {
				//gets from cache
				home = cache.get(jndiName);
				
			} else {
				
				home = context.lookup(getJNDIName(jndiName));
				cache.put(jndiName, home);
				
			}
			
			
			Class[] parameterTypes = new Class[0];
			Object[] parameters = new Object[0];
			
			Method createMethod = home.getClass().getMethod("create",
					parameterTypes);
			
			return createMethod.invoke(home, parameters);
			
		} catch (NamingException e) {
			
			throw new ServiceException(e);
			
		} catch (NoSuchMethodException e) {
			
			throw new ServiceException(e);
			
		} catch (InvocationTargetException e) {
			
			throw new ServiceException(e);
			
		} catch (IllegalAccessException e) {
			
			throw new ServiceException(e);
			
		}  

	}
	
	public <T> T getKernelService(String serviceName, Class interfaceClass) throws ServiceException {
		
		AreaWorkstationProxy serviceProxy = new AreaWorkstationProxy(serviceName, interfaceClass);
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class[]{interfaceClass}, serviceProxy);
	}

}
