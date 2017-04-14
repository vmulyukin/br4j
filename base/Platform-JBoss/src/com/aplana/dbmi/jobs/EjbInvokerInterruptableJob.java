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
package com.aplana.dbmi.jobs;

import org.quartz.*;
import org.quartz.jobs.ee.ejb.EJBInvokerJob;

import javax.ejb.EJBHome;
import javax.ejb.EJBMetaData;
import javax.ejb.EJBObject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.Hashtable;

/**
 * @author etarakanov
 *         Date: 12.05.2015
 *         Time: 12:46
 */
public class EjbInvokerInterruptableJob extends EJBInvokerJob implements InterruptableEjbJob{

    private JobExecutionContext context;
    private static final String INTERRUPT_METHOD_NAME = "interrupt";

    @Override
    public void interrupt() throws UnableToInterruptJobException {
        JobDetail detail = context.getJobDetail();

        JobDataMap dataMap = detail.getJobDataMap();

        String ejb = dataMap.getString("ejb");
        String method = INTERRUPT_METHOD_NAME;
        if (ejb == null) {
            throw new UnableToInterruptJobException("Unable to get task ejb name");
        }
        InitialContext jndiContext = null;
        try
        {
            jndiContext = getInitialContext(dataMap);
        }
        catch (NamingException ne)
        {
            throw new UnableToInterruptJobException(ne);
        }
        Object value = null;
        try
        {
            value = jndiContext.lookup(ejb);
        }
        catch (NamingException ne)
        {
            throw new UnableToInterruptJobException(ne);
        }
        finally
        {
            if (jndiContext != null) {
                try
                {
                    jndiContext.close();
                }
                catch (NamingException e) {}
            }
        }
        EJBHome ejbHome = (EJBHome) PortableRemoteObject.narrow(value, EJBHome.class);

        EJBMetaData metaData = null;
        try
        {
            metaData = ejbHome.getEJBMetaData();
        }
        catch (RemoteException re)
        {
            throw new UnableToInterruptJobException(re);
        }
        Class homeClass = metaData.getHomeInterfaceClass();

        Class remoteClass = metaData.getRemoteInterfaceClass();

        ejbHome = (EJBHome)PortableRemoteObject.narrow(ejbHome, homeClass);

        Method methodCreate = null;
        try
        {
            methodCreate = homeClass.getDeclaredMethod("create", (Class[])null);
        }
        catch (NoSuchMethodException nsme)
        {
            throw new UnableToInterruptJobException(nsme);
        }
        EJBObject remoteObj = null;
        try
        {
            remoteObj = (EJBObject)methodCreate.invoke(ejbHome, (Object[])null);
        }
        catch (IllegalAccessException iae)
        {
            throw new UnableToInterruptJobException(iae);
        }
        catch (InvocationTargetException ite)
        {
            throw new UnableToInterruptJobException(ite);
        }
        Method methodInterrupt = null;
        try
        {
            methodInterrupt = remoteClass.getMethod(method);
        }
        catch (NoSuchMethodException nsme)
        {
            throw new UnableToInterruptJobException(nsme);
        }
        try
        {
            methodInterrupt.invoke(remoteObj);
        }
        catch (IllegalAccessException iae)
        {
            throw new UnableToInterruptJobException(iae);
        }
        catch (InvocationTargetException ite)
        {
            throw new UnableToInterruptJobException(ite);
        }

    }

    private InitialContext getInitialContext(JobDataMap jobDataMap)
            throws NamingException
    {
        Hashtable params = new Hashtable(2);

        String initialContextFactory = jobDataMap.getString("java.naming.factory.initial");
        if (initialContextFactory != null) {
            params.put("java.naming.factory.initial", initialContextFactory);
        }
        String providerUrl = jobDataMap.getString("java.naming.provider.url");
        if (providerUrl != null) {
            params.put("java.naming.provider.url", providerUrl);
        }
        String principal = jobDataMap.getString("java.naming.security.principal");
        if (principal != null) {
            params.put("java.naming.security.principal", principal);
        }
        String credentials = jobDataMap.getString("java.naming.security.credentials");
        if (credentials != null) {
            params.put("java.naming.security.credentials", credentials);
        }
        if (params.size() == 0) {
            return new InitialContext();
        }
        return new InitialContext(params);
    }

    @Override
    public void setJobExecutionContext(JobExecutionContext context) {
        this.context = context;
    }
}
