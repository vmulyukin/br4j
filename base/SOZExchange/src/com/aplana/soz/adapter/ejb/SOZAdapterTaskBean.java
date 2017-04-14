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
package com.aplana.soz.adapter.ejb;

import com.aplana.soz.SOZException;
import com.aplana.soz.adapter.Result;
import com.aplana.soz.adapter.SOZAdapter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.access.ContextSingletonBeanFactoryLocator;
import org.springframework.ejb.support.AbstractStatelessSessionBean;

import javax.ejb.CreateException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;

import java.util.Map;

public class SOZAdapterTaskBean extends AbstractStatelessSessionBean
        implements SessionBean {

    private static final long serialVersionUID = 1L;
    protected final static Log logger = LogFactory.getLog(SOZAdapterTaskBean.class);

    private static Boolean working = false;

    @Override
    protected void onEjbCreate() throws CreateException {
        working = false;
    }
    
    @Override
	public void setSessionContext(SessionContext sessionContext) {
        super.setSessionContext(sessionContext);
        setBeanFactoryLocator(ContextSingletonBeanFactoryLocator.getInstance());
        setBeanFactoryLocatorKey("businessBeanFactory");
    }

    public void process(Map parameters) {
        synchronized (logger) {
            if (working) {
                logger.warn("SOZAdapterTaskBean is already working. Skipping.");
                return;
            }
            working = true;
        }
        try {
            SOZAdapter sozAdapter = new SOZAdapter();
            sozAdapter.initConfiguration();
            Result result = sozAdapter.runExport();
            logger.info(result.toString());
        } catch (SOZException e) {
            logger.error(e);
            e.printStackTrace();
        } finally {
            synchronized (logger) {
                working = false;
                logger.info("SOZAdapterTaskBean finished");
            }
        }

    }
}

