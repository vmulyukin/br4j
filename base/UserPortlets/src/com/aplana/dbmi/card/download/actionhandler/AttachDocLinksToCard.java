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
package com.aplana.dbmi.card.download.actionhandler;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;

/**
 * Attaches given file card IDs to target card as DOCLINKS (attachments).
 * Parameters string: ?cardId=01234567&doclinks=123:456:789
 * 
 * @author aminnekhanov
 * 
 */
public class AttachDocLinksToCard extends FileActionHandler {
	public static final String PARAM_CARD_ID = "cardId";
	public static final String PARAM_DOCLINKS = "doclinks";
	public final static ObjectId ATTR_DOCLINKS = new ObjectId(
			CardLinkAttribute.class, "DOCLINKS");

	@Override
	public void process(HttpServletRequest request, HttpServletResponse response)
			throws DataException {
		String target_cardId = request.getParameter(PARAM_CARD_ID);
		String doclinksStr = request.getParameter(PARAM_DOCLINKS);
		if (target_cardId == null || doclinksStr == null) {
			String s = "Ivalid, incorrect or missing parameters: ";
			s += PARAM_CARD_ID + "=" + target_cardId;
			s += "; " + PARAM_DOCLINKS + "=" + doclinksStr;
			logger.error(s);
			throw new DataException(s);
		}
		String doclinks[] = doclinksStr.split(":");

		if (logger.isDebugEnabled())
			logger.debug("Attaching DOCLIKS to card_id " + target_cardId + ": "
					+ doclinksStr);

		DataServiceBean ds = this.getServiceBean();
		ds.setSessionId(request.getSession().getId());
		ObjectId oidCard = new ObjectId(Card.class,
				Long.parseLong(target_cardId));
		try {
			Card c = (Card) ds.getById(oidCard);
			CardLinkAttribute attr_doclinks = c
					.getCardLinkAttributeById(ATTR_DOCLINKS);
			int count_added = 0;
			if (attr_doclinks != null) {
				for (String doclink : doclinks) {
					long attachment_card_id = Long.parseLong(doclink);
					if (attachment_card_id > 0) {
						attr_doclinks.addLinkedId(attachment_card_id);
						count_added++;
					}
				}
			}
			if (count_added > 0) {
				// lock > save > unlock
				LockObject lockAction = new LockObject(c);
				ds.doAction(lockAction);
				ds.saveObject(c);
				UnlockObject unlockAction = new UnlockObject(c);
				ds.doAction(unlockAction);
			}

			response.setContentType("text/plain");
			PrintWriter out = null;
			out = response.getWriter();
			out.write("" + count_added + " attachments added.");
		} catch (IOException e) {
			logger.error("Got IOException", e);
			throw new DataException(e);
		} catch (ServiceException se) {
			logger.error("GetDocLinksAction got ServiceException: ", se);
			throw new DataException(se);
		}
	}
}
