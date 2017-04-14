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
package com.aplana.medo.cards;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.medo.ServicesProvider;

/**
 * The base class for IEMDS export helpers. These classes performs required
 * business processing of cards in system.
 */
public abstract class ExportCardHandler {

	protected Log logger = LogFactory.getLog(getClass());
	protected static Log loggerSt = LogFactory.getLog(ExportCardHandler.class);
	protected Card card = null;
	protected DataServiceBean serviceBean;
	protected static DataServiceBean serviceBeanStatic;

	/**
	 * Returns id of card according to current state of class.
	 * 
	 * @return card id or -1 in case of error
	 * @throws CardException
	 */
	public abstract long getCardId() throws CardException;

	/**
	 * Returns string contained description of current class state.
	 * 
	 * @return parameter values log
	 */
	protected abstract String getParameterValuesLog();

	protected void saveCard()
		throws DataException, ServiceException 
	{
		final LockObject lock = new LockObject(card.getId());
		serviceBean.doAction(lock);
		try {
			serviceBean.saveObject(card);
			card = (Card)serviceBean.getById(card.getId());	// ������� ���������, ����� ������ ��������
		} finally {
			final UnlockObject unlock = new UnlockObject(card.getId());
			serviceBean.doAction(unlock);
		}
	}

	/**
	 * Returns {@link DataServiceBean} instance or throws {@link CardException}
	 * if it is impossible.
	 * 
	 * @return DataServiceBean
	 * @throws CardException
	 */
	protected DataServiceBean getServiceBean() throws CardException {
		try {
			serviceBean = ServicesProvider.serviceBeanInstance();
		} catch (ServiceException ex) {
			throw new CardException();
		}

		if (serviceBean == null) {
			throw new CardException();
		}

		return serviceBean;
	}

	/**
	 * Returns {@link DataServiceBean} instance or throws {@link CardException}
	 * if it is impossible.
	 * 
	 * @return DataServiceBean
	 * @throws CardException
	 */
	protected static DataServiceBean getServiceBeanStatic() throws CardException {
		try {
			serviceBeanStatic = ServicesProvider.serviceBeanInstance();
		} catch (ServiceException ex) {
			throw new CardException();
		}

		if (serviceBeanStatic == null) {
			throw new CardException();
		}

		return serviceBeanStatic;
	}

}
