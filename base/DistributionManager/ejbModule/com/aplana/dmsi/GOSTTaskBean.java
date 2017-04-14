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
package com.aplana.dmsi;

import com.aplana.distrmanager.LoadSaveDocFacade;
import org.springframework.beans.BeansException;
import org.springframework.context.access.ContextSingletonBeanFactoryLocator;
import org.springframework.ejb.support.AbstractStatelessSessionBean;

import javax.ejb.CreateException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import java.util.Map;

public class GOSTTaskBean extends AbstractStatelessSessionBean
        implements SessionBean {

    private static final long serialVersionUID = 1L;

    private final static String GOST_INTERCHANGER_BEAN = "gostInterchanger";

    private LoadSaveDocFacade gostInterchanger;
    private static Boolean working = false;

    @Override
	public void setSessionContext(SessionContext sessionContext) {
        super.setSessionContext(sessionContext);
        setBeanFactoryLocator(ContextSingletonBeanFactoryLocator.getInstance());
        setBeanFactoryLocatorKey("businessBeanFactory");
    }

    @Override
    protected void onEjbCreate() throws CreateException {
        try {
            LoadSaveDocFacade interchanger = (LoadSaveDocFacade) getBeanFactory()
                    .getBean(GOST_INTERCHANGER_BEAN);
            setGostInterchanger(interchanger);
        } catch (BeansException ex) {
            logger.error("Unable to load beans", ex);
            throw new CreateException(ex.getMessage());
        }
    }

    public void process(Map<?, ?> parameters) {
        synchronized (GOSTTaskBean.class) {
            if (working)
                return;
            working = true;
        }
        try {
            gostInterchanger.uploadDocuments();
        } finally {
            synchronized (GOSTTaskBean.class) {
                working = false;
            }
        }
    }

    public void interrupt()
    {
        logger.info("GOSTTaskBean will be interrupted");
        if (gostInterchanger != null)
        {
            gostInterchanger.interrupt();
        }
    }

    public LoadSaveDocFacade getGostInterchanger() {
        return this.gostInterchanger;
    }

    public void setGostInterchanger(LoadSaveDocFacade gostInterchanger) {
        this.gostInterchanger = gostInterchanger;
    }
}
