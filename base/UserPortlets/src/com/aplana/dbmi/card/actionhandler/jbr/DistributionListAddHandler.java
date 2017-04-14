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
package com.aplana.dbmi.card.actionhandler.jbr;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import com.aplana.dbmi.action.CreateCard;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.card.PortletForm;
import com.aplana.dbmi.card.PortletFormManager;
import com.aplana.dbmi.card.PortletFormManagerAware;
import com.aplana.dbmi.card.actionhandler.CardPortletAttributeEditorActionHandler;
import com.aplana.dbmi.gui.FilteredCards;
import com.aplana.dbmi.gui.IListEditor;
import com.aplana.dbmi.gui.LinkChooser;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.dbmi.service.AsyncDataServiceBean.ExecuteOption;

public class DistributionListAddHandler extends CardPortletAttributeEditorActionHandler
		implements PortletFormManagerAware
{
	private static final ObjectId TEMPLATE_LIST =
		ObjectId.predefined(Template.class, "jbr.distributionList");
	private static final ObjectId ATTR_RECIPIENTS =
		ObjectId.predefined(CardLinkAttribute.class, "jbr.distributionList.recipinets");
	
	private static final ObjectId TEMPLATE_ELEMENT =
		ObjectId.predefined(Template.class, "jbr.DistributionListElement");
	private static final ObjectId ATTR_RECIPIENT =
		ObjectId.predefined(CardLinkAttribute.class, "jbr.distributionItem.recipient");
	private static final ObjectId ATTR_DATE =
		ObjectId.predefined(DateAttribute.class, "jbr.distributionItem.sendDate");
	private static final ObjectId ATTR_METHOD =
		ObjectId.predefined(ListAttribute.class, "jbr.distributionItem.method");

	private static final ObjectId DEFAULT_SENDING_METHOD =
			ObjectId.predefined(ReferenceValue.class, "jbr.distributionItem.method.email");
	
	private static final ObjectId SENDING_METHOD_ATTR =
			ObjectId.predefined(ListAttribute.class, "jbr.sending.method");
	
	private PortletFormManager manager;
	
	protected void process(Attribute attr, List cardIds, ActionRequest request,
			ActionResponse response) throws DataException {
		manager.openForm(new ListChooseForm((CardLinkAttribute) attr));
	}

	public void setPortletFormManager(PortletFormManager manager) {
		this.manager = manager;
	}
	
	public class ListChooseForm implements PortletForm
	{
		private CardLinkAttribute attr;
		private LinkChooser chooser;
		
		public ListChooseForm(CardLinkAttribute attr) {
			this.attr = attr;
			this.chooser = new LinkChooser();

			try {
				Search filter = new Search();
				filter.setByAttributes(true);
				filter.setTemplates(Collections.singletonList(DataObject.createFromId(TEMPLATE_LIST)));
				filter.setWords("");
				chooser.setDataProvider(new FilteredCards(serviceBean, filter, "***** Distribution list"));
			} catch (Exception e) {
				logger.error("Error retrieving list of distribution lists", e);
			}
		}
		
		public void doFormView(RenderRequest request, RenderResponse response)
				throws IOException, PortletException {
	    	request.setAttribute(LinkChooser.ATTR_INSTANCE, chooser);
			if (!chooser.doView(request, response))
				manager.closeForm();
		}

		public void processFormAction(ActionRequest request, ActionResponse response)
				throws IOException, PortletException {
			if (!chooser.processAction(request, response))
				manager.closeForm();

			String action = request.getParameter(IListEditor.FIELD_ACTION);
			if (IListEditor.ACTION_SAVE.equals(action)) {
				List selected = chooser.getDataProvider().getSelectedListData();
				if (selected == null || selected.size() == 0)
					return;
				
				try {
					ObjectId listId = (ObjectId) selected.iterator().next();
					Card list = (Card) serviceBean.getById(listId);
					CardLinkAttribute recipients = (CardLinkAttribute) list.getAttributeById(ATTR_RECIPIENTS);
					for (Iterator itr = recipients.getIdsLinked().iterator(); itr.hasNext(); ) {
						ObjectId recipientId = (ObjectId) itr.next();
						addListElement(recipientId, null, getSendingMethodId(recipientId));
					}
				} catch (Exception e) {
					getCardPortletSessionBean().setMessage(e.getMessage());
				}
				manager.closeForm();
			}
		}

		private ObjectId getSendingMethodId(ObjectId recipientId) {
			ObjectId methodId = DEFAULT_SENDING_METHOD;

			try {
				if (recipientId != null) {
					Search search = new Search();
					search.setByCode(true);
					search.setWords(recipientId.getId().toString());
					SearchResult.Column col = new SearchResult.Column();
					col.setAttributeId(SENDING_METHOD_ATTR);
					search.setColumns(Collections.singletonList(col));

					SearchResult result = serviceBean.doAction(search);
					if (result != null && result.getCards() != null && result.getCards().size() == 1) {
						Card organizationCard = result.getCards().get(0);
						ListAttribute method = organizationCard.getAttributeById(SENDING_METHOD_ATTR);
						if (method != null && method.getValue() != null) {
							methodId = method.getValue().getId();
						}
					}
				}
			} catch (Exception e) {
				logger.error("Error retrieving sending method from organization", e);
			}

			return methodId;
		}

		private void addListElement(ObjectId recipient, Date date, ObjectId method) throws DataException, ServiceException {
			CreateCard create = new CreateCard(TEMPLATE_ELEMENT);
			Card element = (Card) serviceBean.doAction(create);
			CardLinkAttribute cAttr = (CardLinkAttribute) element.getAttributeById(ATTR_RECIPIENT);
			cAttr.addLinkedId(recipient);
			DateAttribute dAttr = (DateAttribute) element.getAttributeById(ATTR_DATE);
			dAttr.setValue(date);
			ListAttribute lAttr = (ListAttribute) element.getAttributeById(ATTR_METHOD);
			lAttr.setValue((ReferenceValue) DataObject.createFromId(method));
			ObjectId elementId = serviceBean.saveObject(element, ExecuteOption.SYNC);
			serviceBean.doAction(new UnlockObject(elementId));
			attr.addLinkedId(elementId);
		}
	}
}
