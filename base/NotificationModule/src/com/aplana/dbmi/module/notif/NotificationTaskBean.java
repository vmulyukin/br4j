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
import java.util.Iterator;
import java.util.Map;

import javax.ejb.CreateException;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.access.ContextSingletonBeanFactoryLocator;

/**
 * Bean implementation class for Enterprise Bean: NotificationScheduler
 */
public class NotificationTaskBean extends org.springframework.ejb.support.AbstractStatelessSessionBean implements javax.ejb.SessionBean {
	
	protected final Log logger = LogFactory.getLog(getClass());

    static final long serialVersionUID = 3206093459760846163L;
    public static final String PARAM_DELIVERY = "deliveryBean";
    private String deliveryBean;

	public NotificationTaskBean()	{
		setBeanFactoryLocator(ContextSingletonBeanFactoryLocator.getInstance());
		setBeanFactoryLocatorKey("businessBeanFactory");
	}
	
    protected void onEjbCreate() throws CreateException {
        try {
        	deliveryBean = (String) new InitialContext().lookup("java:comp/env/" + PARAM_DELIVERY);
        	logger.info("NotificationTaskBean: EJB created for delivery " + deliveryBean);
		} catch (NamingException e) {
			logger.error("Error initializing NotificationTaskBean", e);
		}
    }

    public void process(Map parameters)
    {
    	if (deliveryBean == null)
    		throw new IllegalStateException("Delivery source should be defined");
        logger.info("NotificationTaskBean: Task started (" + deliveryBean + ")");
        BeanFactory beanFactory = this.getBeanFactory();
        DeliverySource source = (DeliverySource) beanFactory.getBean(deliveryBean);
        Collection<NotificationObject> notifications = source.listDeliveries();
		int sent = 0;
		for (Iterator<NotificationObject> itr = notifications.iterator(); itr.hasNext(); ) {
			NotificationObject delivery = itr.next();
			if (!source.buildDelivery(delivery))
				continue;
			NotificationBean notifier = source.getNotifier(delivery);
			notifier.setObject(delivery);
			sent += notifier.sendNotifications();
		}
        logger.info("NotificationTaskBean: Task completed (" + deliveryBean + "); " +
        		sent + " notification message(s) sent");
    }
}