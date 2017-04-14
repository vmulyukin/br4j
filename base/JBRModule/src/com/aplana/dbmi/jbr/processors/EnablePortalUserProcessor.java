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
package com.aplana.dbmi.jbr.processors;

import com.aplana.dbmi.action.GetPersonByLogin;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.service.impl.SaveQueryBase;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.PortalException;
import com.aplana.dbmi.PortalUser;
import com.aplana.dbmi.action.GetPersonByCard;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.PortalUserProcessException;

/**
 * Processor used for enabling / disabling user in portal
 * depending on 'isEnabled' param
 *
 * @author Vlad Alexandrov
 * @version 1.0
 * @since   2014-11-26
 */

public class EnablePortalUserProcessor extends ProcessCard
{
	private static final long serialVersionUID = 1L;

	private static final String PARAM_IS_ENABLED = "isEnabled";
	private boolean isEnabled = true;

	@Override
	public Object process() throws DataException {
		final ObjectId cardId = getCardId();
		final Card card = getCard();
		if (null == cardId || null == card) {
			logger.warn("Impossible to execute processor until card is saved -> exiting");
			return null;
		}

		//1. Get existing user by card
		GetPersonByCard personByCardAction = new GetPersonByCard(card.getId());
		final Person person = execAction(personByCardAction);
		if (null == person) {
			logger.error("Cannot find user in person table by cardId: " + card.getId().getId());
			throw new DataException("user.not.found");
		}
		person.setActive(isEnabled);
		execAction(new LockObject(person));

		try {
			final SaveQueryBase saveQuery = getQueryFactory().getSaveQuery(person);
			saveQuery.setObject(person);
			getDatabase().executeQuery( this.getUser(), saveQuery);
		} catch (DataException e) {
			logger.error("An unexpected exception occurred while trying to SAVE person with ID: " + person.getId().getId(), e);
			throw e;
		} finally {
			execAction(new UnlockObject(person));
		}

		try {
			final PortalUser pUser = Portal.getFactory().getUserService().getByLogin(person.getLogin());
			if (null != pUser) {
				if (TransactionSynchronizationManager.isSynchronizationActive()) {
					TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
						@Override
						public void beforeCommit(boolean readOnly) {
							try{
								Portal.getFactory().getUserService().enableUser(pUser, isEnabled);
							} catch (PortalException e) {
								if (isEnabled == true){
									logger.error("Cannot enable user " + person.getLogin(), e);
									throw new PortalUserProcessException("user.enable.fail");
								}else {
									logger.error("Cannot disable user " + person.getLogin(), e);
									throw new PortalUserProcessException("user.disable.fail");
								}
							}
						}
					});
				}
			}
		} catch (PortalException e) {
			logger.error("Cannot check user presence in portal database by login: " + person.getLogin(), e);
			throw new DataException("user.login.check.fail");
		}
		return getResult();
	}
	
	@Override
	public void setParameter(String name, String value) {
		if (PARAM_IS_ENABLED.equalsIgnoreCase(name)) {
			this.isEnabled = Boolean.parseBoolean(value);
		} else
			super.setParameter(name, value);
	}
}