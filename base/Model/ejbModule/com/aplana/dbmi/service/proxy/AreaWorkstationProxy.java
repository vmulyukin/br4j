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
package com.aplana.dbmi.service.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import com.aplana.dbmi.service.ServiceLocator;
import com.aplana.dbmi.service.workstation.AreaWorkstationProxyDataServiceLocal;

/**
 * Represents Local Proxy implementation
 * It is used for execution services from ejb-module
 * It locates {@link AreaWorkstationProxyDataServiceBean} and delegates all passed calls to this EJB  
 * 
 *
 */
public class AreaWorkstationProxy implements InvocationHandler  {

	
    private static final String AREA_WORKSTATION_BEAN_LOCAL = "areaWorkstationBean-local";

	private String serviceName = null;

    private Class interfacename = null;

	
	

	public AreaWorkstationProxy(String serviceName, Class interfacename) {
		super();
		this.serviceName = serviceName;
		this.interfacename = interfacename;
	}



	
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		
		AreaWorkstationProxyDataServiceLocal areaWorkstationProxyDataService = 
			(AreaWorkstationProxyDataServiceLocal)ServiceLocator.getInstance().getLocalEJB(AREA_WORKSTATION_BEAN_LOCAL);
		
        String methodName = method.getName();
		
		return areaWorkstationProxyDataService.invoke(serviceName, methodName, args);
		
	}
	
	
	
	

}
