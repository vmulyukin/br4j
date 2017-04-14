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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;

import com.aplana.dbmi.action.ExportCardToXml;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.Database;
import com.aplana.dbmi.service.impl.ProcessorBase;
import com.aplana.dbmi.service.impl.QueryFactory;
import com.aplana.dbmi.service.impl.UserData;

public class CardPassport extends LinkedAttachments {

	public static final String OBJ_DOCUMENT = "doc";
	
    private Map attachments = new HashMap();
    private SingleCardNotification notification = null;

    @Override
    public Collection getAttachments(NotificationObject object) {
		Map cards = new HashMap();
		notification = (SingleCardNotification) object;
		loadCards(notification, cards);
		ObjectId recipientId = notification.getCard().getId();
		ArrayList materials = new ArrayList(cards.size());
		if (cards.size() != 1) {
		    throw new IllegalStateException("Exactly one document is possible here but in fact there are " + cards.size());
		}
	
		Card card = (Card) cards.values().iterator().next();
		byte[] passportData = initializeData(card.getId(), recipientId);
		materials.add(new CardPassportDataSource(passportData));
		notification.addInfo(OBJ_DOCUMENT, card);
	
		for (Iterator itr = attachments.entrySet().iterator(); itr.hasNext();) {
		    Entry fileEntry = (Entry) itr.next();
		    ObjectId cardId = (ObjectId) fileEntry.getKey();
		    String fileName = (String) fileEntry.getValue();
		    materials.add(new MaterialDataSource(cardId, getDefaultCaller(), fileName));
		}
		return materials;
    }

    private byte[] initializeData(ObjectId cardId, ObjectId recipientId) {
		try {
		    attachments.clear();
		    CardPassportCaller caller = new CardPassportCaller();
		    ExportCardToXml exportAction = new ExportCardToXml();
		    exportAction.setCardId(cardId);
		    exportAction.setRecipientId(recipientId);
		    ActionQueryBase actionQuery = caller.getQueryFactory()
			    .getActionQuery(exportAction);
		    actionQuery.setAction(exportAction);
		    ExportCardToXml.Result result = (ExportCardToXml.Result) caller.getDatabase().
		    		executeQuery(caller.getUser(), actionQuery);
		    notification.addInfo(result.getInfos());
		    InputStream data = result.getData();
		    attachments.putAll(result.getFiles());
		    ByteArrayOutputStream out = new ByteArrayOutputStream();
		    IOUtils.copy(data, out);
		    return out.toByteArray();
		} catch (DataException e) {
		    throw new IllegalStateException("Error loading passport from card "
			    + cardId.getId(), e);
		} catch (IOException e) {
		    throw new IllegalStateException("Error loading passport from card "
			    + cardId.getId(), e);
		}
    }

    private class CardPassportCaller extends ProcessorBase {
		public CardPassportCaller() {
		}
	
		@Override
		public Object process() throws DataException {
		    throw new UnsupportedOperationException("Should never be called");
		}
	
		@Override
		public Database getDatabase() {
		    return CardPassport.this.getReadonlyDatabase();
		}
	
		@Override
		public QueryFactory getQueryFactory() {
		    return CardPassport.this.getQueryFactory();
		}
	
		@Override
		public UserData getUser() {
		    try {
			return getSystemUser();
		    } catch (DataException e) {
			logger.error("Error authenticating system", e);
			return null;
		    }
		}

    }
}
