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
package com.aplana.dbmi.card.actionhandler;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.action.CheckLock;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.UnlockObject;
import com.aplana.dbmi.card.CardPortlet;
import com.aplana.dbmi.card.CardPortletSessionBean;
import com.aplana.dbmi.card.CardPortletSessionBean.PortletMessage.PortletMessageType;
import com.aplana.dbmi.card.Parametrized;
import com.aplana.dbmi.card.PortletForm;
import com.aplana.dbmi.card.PortletFormManager;
import com.aplana.dbmi.card.PortletFormManagerAware;
import com.aplana.dbmi.card.hierarchy.CardFilterCondition;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.IntegerAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.Reference;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ObjectLockedException;
import com.aplana.dbmi.service.ObjectNotLockedException;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.dbmi.util.IdUtils;

public class ChangeOrderAction extends CardPortletAttributeEditorActionHandler implements PortletFormManagerAware,
		Parametrized {
	private final static ObjectId PERSON_NM_ATTR_ID =  ObjectId.predefined(StringAttribute.class, "jbr.person.lastnameNM");
	private final static ObjectId PERS_ORG_ATTR_ID =  ObjectId.predefined(CardLinkAttribute.class, "jbr.incoming.organization");
	private final static ObjectId ORG_SHORTNAME_ATTR_ID =  ObjectId.predefined(StringAttribute.class, "jbr.organization.shortName");


	public final static String PARAM_COLUMNS = "columns";
	public final static String PARAM_ORDER_ATTR = "orderAttribute";
	public final static String PARAM_REF_ID = "referenceId";

	private PortletFormManager formManager;
	private ChangeOrderForm changeOrderForm = new ChangeOrderForm();

	public void setParameter(String name, String value) {
		if (PARAM_COLUMNS.equals(name)) {
			changeOrderForm.setColumnIds(IdUtils.stringToAttrIds(value, null));
		} else if (PARAM_REF_ID.equals(name)) {
			changeOrderForm.setReferenceId(ObjectIdUtils.getObjectId(Reference.class, value, false));
		} else if (PARAM_ORDER_ATTR.equals(name)) {
			changeOrderForm.setOrderAttributeId(IdUtils.smartMakeAttrId(value, IntegerAttribute.class));
		}
	}

	public void setPortletFormManager(PortletFormManager portletFormManager) {
		formManager = portletFormManager;
	}

	@Override
	protected void process(Attribute attr, List cardIds, ActionRequest request, ActionResponse response)
			throws DataException {
		changeOrderForm.setCardIds(((CardLinkAttribute) attr).getIdsLinked());
		formManager.openForm(changeOrderForm);
	}

	@Override
	public boolean isApplicableForUser() {
		final CardFilterCondition condition = getCondition();
		return super.isApplicableForUser()
				&& (condition == null || condition.check(getCardPortletSessionBean().getActiveCard()));
	}

	public static class ChangeOrderForm implements PortletForm {

		public static final String ACTION_CHANGE = "CHANGE_ORDER";
		public static final String VIEW_NAME = "ChangeOrderForm.jsp";

		private List<ObjectId> cardIds = Collections.emptyList();
		private CardPortletSessionBean sessionBean;
		private List<ObjectId> columnIds = Collections.emptyList();
		private ObjectId referenceId;
		private ObjectId orderAttributeId;
		private Log logger = LogFactory.getLog(getClass());

		private List<ReferenceValue> references = null;
		private List<String> columnNames = null;

		public ChangeOrderForm() {
		}

		public void doFormView(RenderRequest request, RenderResponse response) throws IOException, PortletException {
			setSessionBean(CardPortlet.getSessionBean(request));
			try {
				initializeReferences();
				initializeColumnNames();
				request.setAttribute("tableHead", columnNames);

				List<TableRow> tableRows = new ArrayList<TableRow>(cardIds.size());
				for (ObjectId cardId : getCardIds()) {
					Card linkedCard = (Card) getServiceBean().getById(cardId);
					TableRow row = new TableRow();
					try {
						getServiceBean().doAction(new CheckLock(cardId));
					} catch (ObjectLockedException ex) {
						row.setDisabled(true);
					} catch (ObjectNotLockedException ex) {
					}
					row.setId(linkedCard.getId().getId().toString());
					row.setItems(references);
					String[] rowValues = new String[getColumnIds().size()];
					for (int i = 0; i < getColumnIds().size(); ++i) {
						ObjectId columnId = getColumnIds().get(i);
						Attribute columnAttr = linkedCard.getAttributeById(columnId);
						if(columnAttr != null && columnAttr instanceof PersonAttribute){
							PersonAttribute personAttribute = (PersonAttribute)columnAttr;
							if(!personAttribute.isEmpty()){
								Person person = (Person) getServiceBean().getById(personAttribute.getValue());
								Card personCard = (Card) getServiceBean().getById(person.getCardId());
								Card organisationCard = (Card) getServiceBean().getById(personCard.getCardLinkAttributeById(PERS_ORG_ATTR_ID).getSingleLinkedId());
								rowValues[i] = personCard.getAttributeById(PERSON_NM_ATTR_ID).getStringValue()+", "+
										organisationCard.getAttributeById(ORG_SHORTNAME_ATTR_ID).getStringValue();
							}
						} else {
							rowValues[i] = columnAttr == null ? "" : columnAttr.getStringValue();
						}
					}
					row.setColumnValues(rowValues);

					IntegerAttribute orderAttribute = (IntegerAttribute) linkedCard.getAttributeById(orderAttributeId);
					int order = orderAttribute.getValue();
					row.setOrder(order);
					for (ReferenceValue ref : references) {
						String value = ref.getValue();
						if (Integer.valueOf(value).equals(order)) {
							row.setSelectedItem(ref);
							break;
						}
					}
					tableRows.add(row);
				}
				Collections.sort(tableRows);
				request.setAttribute("tableRows", tableRows);
			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
				String msg = getSessionBean().getResourceBundle().getString("db.side.error.msg.param");
				String errorMessage = MessageFormat.format(msg, ex.getMessage());
				request.setAttribute("errorMessage", errorMessage);
			}
			PortletSession session = request.getPortletSession();
			PortletRequestDispatcher rd = session.getPortletContext().getRequestDispatcher(
					CardPortlet.JSP_FOLDER + VIEW_NAME);
			rd.include(request, response);
		}

		protected void initializeReferences() throws DataException, ServiceException {
			if (references == null) {
				Collection<ReferenceValue> result = getServiceBean().listChildren(getReferenceId(),
						ReferenceValue.class);
				references = (List<ReferenceValue>) result;
			}
		}

		protected void initializeColumnNames() throws DataException, ServiceException {
			if (columnNames == null) {
				columnNames = new ArrayList<String>(getColumnIds().size() + 1);
				for (ObjectId columnId : getColumnIds()) {
					Attribute attr = (Attribute) getServiceBean().getById(columnId);
					columnNames.add(attr.getName());
				}
				Attribute orderAttr = (Attribute) getServiceBean().getById(orderAttributeId);
				columnNames.add(orderAttr.getName());
			}
		}

		public void processFormAction(ActionRequest request, ActionResponse response) throws IOException,
				PortletException {
			try {
				setSessionBean(CardPortlet.getSessionBean(request));
				String action = request.getParameter(CardPortlet.ACTION_FIELD);

				if (ACTION_CHANGE.equals(action)) {
					processChange(request, response);
				}
				sessionBean.getActiveCardInfo().getPortletFormManager().closeForm();
			} catch (Exception ex) {
				logger.error(ex.getMessage(), ex);
				getSessionBean().setMessageWithType("db.side.error.msg.param", new Object[] { ex.getMessage() },
						PortletMessageType.ERROR);
			}
		}

		protected void processChange(ActionRequest request, ActionResponse response) throws IllegalArgumentException,
				NumberFormatException, DataException, ServiceException {
			List<ObjectId> lockedCards = new ArrayList<ObjectId>();
			for (ObjectId cardId : getCardIds()) {
				String currValue = request.getParameter("curr_value_" + cardId.getId());
				String prevValue = request.getParameter("prev_value_" + cardId.getId());
				if (currValue == null || currValue.equals(prevValue)) {
					continue;
				}

				ReferenceValue value = new ReferenceValue();
				value.setId(Long.valueOf(currValue));
				int index = references.indexOf(value);
				DataServiceBean serviceBean = getServiceBean();
				if (index > -1) {
					int intValue = Integer.valueOf(references.get(index).getValue());
					boolean isLocked = false;
					try {
						serviceBean.doAction(new LockObject(cardId));
						isLocked = true;
						Card linkedCard = (Card) serviceBean.getById(cardId);
						IntegerAttribute orderAttribute = (IntegerAttribute) linkedCard
								.getAttributeById(orderAttributeId);
						orderAttribute.setValue(intValue);
						serviceBean.saveObject(linkedCard);
					} catch (ObjectLockedException ex) {
						lockedCards.add(cardId);
					} finally {
						if (isLocked) {
							serviceBean.doAction(new UnlockObject(cardId));
						}
					}
				}
			}
			if (lockedCards.size() > 0) {
				getSessionBean().setMessageWithType("form.changeOrder.locked",
						new Object[] { ObjectIdUtils.numericIdsToCommaDelimitedString(lockedCards) },
						PortletMessageType.ERROR);
			}
		}

		public List<ObjectId> getCardIds() {
			return this.cardIds;
		}

		public void setCardIds(List<ObjectId> cardIds) {
			if (cardIds == null) {
				this.cardIds = Collections.emptyList();
			} else {
				this.cardIds = cardIds;
			}
		}

		public CardPortletSessionBean getSessionBean() {
			return this.sessionBean;
		}

		public void setSessionBean(CardPortletSessionBean sessionBean) {
			this.sessionBean = sessionBean;
		}

		public DataServiceBean getServiceBean() {
			return getSessionBean().getServiceBean();
		}

		public List<ObjectId> getColumnIds() {
			return this.columnIds;
		}

		public void setColumnIds(List<ObjectId> columnIds) {
			this.columnIds = columnIds;
		}

		public ObjectId getReferenceId() {
			return this.referenceId;
		}

		public void setReferenceId(ObjectId referenceId) {
			this.referenceId = referenceId;
		}

		public ObjectId getOrderAttributeId() {
			return this.orderAttributeId;
		}

		public void setOrderAttributeId(ObjectId orderAttributeId) {
			this.orderAttributeId = orderAttributeId;
		}

		public static class TableRow implements Comparable<TableRow> {
			private String id;
			private String[] columnValues;
			private Collection<ReferenceValue> items;
			private ReferenceValue selectedItem;
			private int order;
			private boolean isDisabled;

			public String getId() {
				return this.id;
			}

			public void setId(String id) {
				this.id = id;
			}

			public String[] getColumnValues() {
				return this.columnValues;
			}

			public void setColumnValues(String[] columnValues) {
				this.columnValues = columnValues;
			}

			public Collection<ReferenceValue> getItems() {
				return this.items;
			}

			public void setItems(Collection<ReferenceValue> items) {
				this.items = items;
			}

			public ReferenceValue getSelectedItem() {
				return this.selectedItem;
			}

			public void setSelectedItem(ReferenceValue selectedItem) {
				this.selectedItem = selectedItem;
			}

			public int getOrder() {
				return this.order;
			}

			public void setOrder(int order) {
				this.order = order;
			}

			public int compareTo(TableRow o) {
				return this.order - o.order;
			}

			public boolean isDisabled() {
				return isDisabled;
			}

			public void setDisabled(boolean isDisabled) {
				this.isDisabled = isDisabled;
			}
		}

	}

}
