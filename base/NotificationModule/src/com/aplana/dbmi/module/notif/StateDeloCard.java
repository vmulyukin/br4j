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

import com.aplana.dbmi.service.DataException;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.action.CreateCard;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.IntegerAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.TextAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;



public class StateDeloCard {
	
		private static final ObjectId RESULT_PROCESSING = ObjectId.predefined(
				TextAttribute.class, "resultProcessing");

		private static final ObjectId TEMPLATE_ID = ObjectId.predefined(
				Template.class, "jbr.ProcessingDistribution");

		private long card_id;
		private StringBuffer result_processing;
		private Card card = null;
		private DataServiceBean serviceBean = null;
		private Log logger = LogFactory.getLog(getClass());

		public StateDeloCard() {
			serviceBean = ServicesProvider.serviceBeanInstance();
			logger.info("Create object StateMedoCard.");
		}

		public long getCardId() {
			return card_id;
		}

		public StringBuffer getResultProcessing() {
			return this.result_processing;
		}

		public void setResultProcessing(StringBuffer result_processing) {
			this.result_processing = result_processing;
		}

		public long createCard() throws DataException {
			logger.info(String.format("Trying to create StateMedo card."));

			if (serviceBean == null)
				throw new DataException("DataServiceBean was not initialized");

			final CreateCard createCard = new CreateCard(TEMPLATE_ID);

			try {
				card = (Card) serviceBean.doAction(createCard);
				if (card == null) {
					throw new DataException(
							"Card was not created by unspecifed reason.");
				}

				final TextAttribute resultProcessing = (TextAttribute) card.getAttributeById(RESULT_PROCESSING);
				resultProcessing.setValue(result_processing.toString());

				final ObjectId cardId = saveCardDELO();
				logger.info(String.format("Card with '%s' id was created", cardId.getId().toString()));
				card_id = ((Long) cardId.getId()).longValue();
				return card_id;
			} catch (DataException ex) {
				throw new DataException("jbr.medo.statemedocard.dataexception", ex);
			} catch (ServiceException ex) {
				throw new DataException("jbr.medo.statemedocard.serviceexception", ex);
			} catch (Exception e) {
				throw new DataException("jbr.medo.statemedocard.exception", e);
			}

		}

		private ObjectId saveCardDELO() throws DataException, ServiceException 
		{
			final ObjectId id_c = serviceBean.saveObject(card);
			final UnlockObject unlock = new UnlockObject(id_c);
			serviceBean.doAction(unlock);
			return id_c;
		}
}
