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

import com.aplana.dbmi.action.CheckLock;
import com.aplana.dbmi.action.CreateCard;
import com.aplana.dbmi.action.LockObject;
import com.aplana.dbmi.action.OverwriteCardAttributes;
import com.aplana.dbmi.card.*;
import com.aplana.dbmi.card.CardPortletSessionBean.PortletMessage.PortletMessageType;
import com.aplana.dbmi.model.*;
import com.aplana.dbmi.model.filter.Filter;
import com.aplana.dbmi.model.filter.TemplateForCreateNewCard;
import com.aplana.dbmi.model.filter.TemplateIdListFilter;
import com.aplana.dbmi.model.util.AttrUtils;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ObjectNotLockedException;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.dbmi.util.TemplateComparator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.portlet.*;
import java.io.IOException;
import java.util.*;

public class AddLinkedCardActionHandler extends AbstractAddLinkedCardActionHandler implements PortletFormManagerAware, Parametrized {

	protected static class TypedCardLinkItemCloseHandler implements CardPortletCardInfo.CloseHandler {
		protected Log logger = LogFactory.getLog(getClass());	
		protected ObjectId cardLinkId;
		protected ObjectId idsToLinkId;
		protected CardPortletSessionBean sessionBean;
		private CardStateInfo previousCardState = null;
		private boolean storeAttr;
		
		public TypedCardLinkItemCloseHandler(ObjectId cardLinkId, ObjectId idsToLinkId, CardPortletSessionBean sessionBean) {
			initialize(cardLinkId, idsToLinkId, sessionBean, null);			
		}
		
		public TypedCardLinkItemCloseHandler(ObjectId cardLinkId, ObjectId idsToLinkId, CardPortletSessionBean sessionBean, CardStateInfo previousCardState) {
			initialize(cardLinkId, idsToLinkId, sessionBean, previousCardState);
		}
		
		private void initialize(ObjectId cardLinkId, ObjectId idsToLinkId, CardPortletSessionBean sessionBean, CardStateInfo previousCardState){
			this.cardLinkId = cardLinkId;
			this.sessionBean = sessionBean;
			this.idsToLinkId = idsToLinkId;
			this.previousCardState=previousCardState;
		}
		
		public void setStoreAttr(boolean storeAttr){
			this.storeAttr = storeAttr;
		}
		
		private boolean checkCardNotLocked(ObjectId cardId){
			CheckLock checkLock = new CheckLock(cardId);
			try{
				sessionBean.getServiceBean().doAction(checkLock);
			}catch (ObjectNotLockedException e) {				
				return true;
			}catch (Exception e) {
				return false;
			}
			return false;
		}
		
		
		private void restoreCard(CardPortletCardInfo cardInfo){
			//���� �������� �����������, �� �� ����� � �������
			if(previousCardState==null || cardInfo == null || cardInfo.getCard().getId() == null || !checkCardNotLocked(cardInfo.getCard().getId())){
				return;
			}
			cardInfo.setMode(previousCardState.getMode());
			try {
				sessionBean.getServiceBean().doAction(new LockObject(cardInfo.getCard().getId()));
			} catch (DataException e) {				
				logger.error("Can't locked card", e);
			} catch (ServiceException e) {				
				logger.error("Can't locked card", e);
			}			
		}

		@SuppressWarnings("unchecked")
		public void afterClose(CardPortletCardInfo closedCardInfo, CardPortletCardInfo previousCardInfo) {
			ObjectId newCardId = closedCardInfo.getCard().getId();
			
			restoreCard(previousCardInfo);//��������������� ��������� ������������ ��������

			Attribute attr = previousCardInfo.getCard().getAttributeById(cardLinkId);
			if (newCardId != null && idsToLinkId == null) {
				if (attr.getId().getType().equals(TypedCardLinkAttribute.class)){
					TypedCardLinkAttribute typedAttr = (TypedCardLinkAttribute) attr;
					Collection<ReferenceValue> values = typedAttr.getReferenceValues();
					if(values == null || values.isEmpty()){
						DataServiceBean serviceBean = sessionBean.getServiceBean();
						try {
							values = serviceBean.listChildren(typedAttr.getReference(), ReferenceValue.class);
						} catch(Exception e) {e.printStackTrace();}
						if(values == null || values.isEmpty()) {
							sessionBean.setMessage("addtypedlink.error");
							return;
						}
					}
					if(!typedAttr.isMultiValued()) typedAttr.clear();
					typedAttr.addType((Long) newCardId.getId(), (Long)values.iterator().next().getId().getId());
				}
				else if(attr.getId().getType().equals(CardLinkAttribute.class)){
					CardLinkAttribute linkAttr = (CardLinkAttribute) attr;
					if (linkAttr.isMultiValued()) {
						linkAttr.addLinkedId(newCardId);
					} else { // �������� ������ ���� ��������
						linkAttr.addSingleLinkedId(newCardId);
					}
				}
				previousCardInfo.setAttributeEditorData(cardLinkId, AttributeEditor.KEY_VALUE_CHANGED, Boolean.TRUE);
			} else if(idsToLinkId != null && closedCardInfo.getCard().getAttributeById(idsToLinkId) != null){
				Attribute idsToLinkAttr = closedCardInfo.getCard().getAttributeById(idsToLinkId);
				if(cardLinkId.getType().equals(idsToLinkId.getType())){
					if(cardLinkId.getType().equals(PersonAttribute.class)){
						if(!attr.isMultiValued())
							((PersonAttribute) attr).setPerson(((PersonAttribute) idsToLinkAttr).getPerson());
						else
							((PersonAttribute) attr).getValues().addAll(((PersonAttribute) idsToLinkAttr).getValues());
					} else if(cardLinkId.getType().equals(CardLinkAttribute.class)){
						if(!attr.isMultiValued())
							((CardLinkAttribute) attr).addSingleLinkedId(((CardLinkAttribute) idsToLinkAttr).getSingleLinkedId());
						else
							((CardLinkAttribute) attr).addIdsLinked(((CardLinkAttribute) idsToLinkAttr).getIdsLinked());
					} else if(cardLinkId.getType().equals(TypedCardLinkAttribute.class)){
						if(!attr.isMultiValued()) {
							Map.Entry<Long,Long> entry = ((TypedCardLinkAttribute) idsToLinkAttr).getTypes().entrySet().iterator().next();
							attr.clear();
							((TypedCardLinkAttribute) attr).addType(entry.getKey(), entry.getValue());
						}
						((TypedCardLinkAttribute) attr).getTypes().putAll(((TypedCardLinkAttribute) idsToLinkAttr).getTypes());
					}
					previousCardInfo.setAttributeEditorData(cardLinkId, AttributeEditor.KEY_VALUE_CHANGED, Boolean.TRUE);
				} else if( (cardLinkId.getType().equals(TypedCardLinkAttribute.class) 
						|| (cardLinkId.getType().equals(DatedTypedCardLinkAttribute.class))) 
							&& idsToLinkAttr.getId().getType().equals(PersonAttribute.class)) {
					if(!attr.isMultiValued())
						((TypedCardLinkAttribute) attr).addSingleLinkedId(((PersonAttribute) idsToLinkAttr).getPerson().getCardId());
					else {
						for (Person person : ((PersonAttribute) idsToLinkAttr).getValues()) {
							((CardLinkAttribute) attr).addLinkedId((person).getCardId());
						}
					}
				} 
			}
			if(this.storeAttr){
				OverwriteCardAttributes overwriteCardAttributes = new OverwriteCardAttributes();
				overwriteCardAttributes.setAttributes(Collections.singletonList(previousCardInfo.getCard().getAttributeById(cardLinkId)));
				overwriteCardAttributes.setCardId(previousCardInfo.getCard().getId());
				try {
					sessionBean.getServiceBean().doAction(overwriteCardAttributes);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			sessionBean.setEditorData(cardLinkId, CardLinkPickerAttributeEditor.KEY_CACHE_RESET, true);
		}
	}

	public static final String TEMPLATE_ID_PARAM = "template";
	public static final String TEMPLATE_IDS_PARAM = "templatesToChooseFrom";
	public static final String IS_LINKED_PARAM = "isLinked";
	public static final String IDS_TO_LINK_ATTR_PARAM = "useThisAttrInsteadOfCardId";
	public static final String PARAM_ATTR_COPY = "copyToNewCard";
	protected static final String PARAM_STATES_ALLOWED = "statesAllowed";
	private static final Object STORE_ATTR = "storeAttr";

	protected ObjectId templateId;
	protected ArrayList<ObjectId> templateIds = new ArrayList<ObjectId>();
	protected HashMap<ObjectId, ObjectId> parentAttributes = new HashMap<ObjectId, ObjectId>();
	protected boolean isLinked = false;
	protected ObjectId idsToLinkAttrId;
	protected Collection<ObjectId> statesAllowed;
	//protected Map<String, CardStateInfo> storageStates = new HashMap<String, AddLinkedCardActionHandler.CardStateInfo>();

	protected PortletFormManager manager;
	private boolean storeAttr;

	@Override
	protected Card createCard() throws DataException, ServiceException {
		CreateCard createCard = new CreateCard();
		createCard.setTemplate(templateId);
		createCard.setLinked(isLinked);
		createCard.setParent(getCardPortletSessionBean().getActiveCard());
		Card card = serviceBean.doAction(createCard);
		if (parentAttributes != null && parentAttributes.size()>0) {
			Card parentCard = getCardPortletSessionBean().getActiveCard();
			if (parentCard != null) {
				for (Map.Entry<ObjectId, ObjectId> entry: parentAttributes.entrySet()) {
					Attribute attrFrom = parentCard.getAttributeById(entry.getKey());
					Attribute attrTo = card.getAttributeById(entry.getValue());
					if (attrFrom != null && attrTo != null && attrFrom instanceof CardLinkAttribute
							&& attrTo instanceof CardLinkAttribute) {
						((CardLinkAttribute)attrTo)
								.setIdsLinked(((CardLinkAttribute)attrFrom).getIdsLinked());
					}
				}
			}
		}
		return card;
	}

	@Override
	public boolean isApplicableForUser() {
		try {
			Card card = getCardPortletSessionBean() == null ? null : getCardPortletSessionBean().getActiveCard();
			if (card != null) {
				if (getAttribute() instanceof BackLinkAttribute && card.getId() == null){
					return false;
				}
				boolean isAllowedState = (statesAllowed == null || statesAllowed.contains(card.getState()));
				if (!isAllowedState) {
					return false;
				}
			}
			CreateCard action = new CreateCard();
			action.setTemplate(templateId);
			return serviceBean.canDo(action);
		} catch (Exception e) {
			logger.error("Exception caught while checking user permissions for template", e);
			return false;
		}
	}

	public void setParameter(String name, String value) {
		if (PARAM_STATES_ALLOWED.equalsIgnoreCase(name)){
			statesAllowed = ObjectIdUtils.commaDelimitedStringToNumericIds(value, CardState.class);
		} else if (TEMPLATE_ID_PARAM.equals(name)) {
			this.templateId = ObjectIdUtils.getObjectId(Template.class, value, true);
		} else if (IS_LINKED_PARAM.equals(name)) {
			this.isLinked = Boolean.parseBoolean(value);
		} else if (TEMPLATE_IDS_PARAM.equals(name)){
			for (String s : value.split(",")){
				ObjectId id = ObjectId.predefined(Template.class, s.trim());
				if (id == null) {
					try {
						id = new ObjectId(Template.class, new Long(s.trim()));
					} catch (NumberFormatException e) {
						break;
					}
				}
				templateIds.add(id);
			}
		} else if (IDS_TO_LINK_ATTR_PARAM.equals(name)){
			//noinspection unchecked
			idsToLinkAttrId = ObjectIdUtils.getObjectId(
					Arrays.asList(CardLinkAttribute.class, TypedCardLinkAttribute.class, PersonAttribute.class), CardLinkAttribute.class, value, false
			);
		} else if (PARAM_ATTR_COPY.equals(name)){
			String[] pair = value.split("->");
			if (pair.length != 2) {
				logger.error("Illegal value of parameter " + name + ": " + value);
			}
			ObjectId sourceId = AttrUtils.getAttributeId(pair[0].trim());
			ObjectId destId = AttrUtils.getAttributeId(pair[1].trim());
			parentAttributes.put(sourceId, destId);
		} else if (STORE_ATTR.equals(name)) {
			storeAttr = Boolean.parseBoolean(value);
		}
	}

	//TODO ����������� � ��������� ������������.
	@Override
	protected void process(Attribute attr, List<ObjectId> cardIds, ActionRequest request, ActionResponse response)
			throws DataException {
		if(templateId == null){
			TemplateForCreationSelector selector = new TemplateForCreationSelector(getCardPortletSessionBean(), templateIds);
			selector.select(new TemplateForCreationSelector.SelectionHandler() {
				public void selected(ObjectId templateId) {
					AddLinkedCardActionHandler.this.templateId = templateId;
					openNewCard();
				}
			});
		} else {
			openNewCard();
		}
	}

	public void setPortletFormManager(PortletFormManager portletFormManager) {
		this.manager = portletFormManager;
	}

	protected void openNewCard(){
		CardPortletSessionBean sessionBean = getCardPortletSessionBean();
		try {
			
			CardPortletCardInfo cardInfo = sessionBean.getActiveCardInfo();
			Card child = createCard();
			CardStateInfo activeCardState = saveActiveCardState(sessionBean);
			
			TypedCardLinkItemCloseHandler closeHandler = new TypedCardLinkItemCloseHandler(attribute.getId(), idsToLinkAttrId, sessionBean, activeCardState);
			closeHandler.setStoreAttr(storeAttr);
	    	sessionBean.openNestedCard(
	    			child,
	    			closeHandler,
	    			cardInfo,
	    			true
	    	);
		} catch (Exception e) {
			logger.error("Can't redirect to card editing page", e);
			sessionBean.setMessageWithType("edit.link.error.create", new Object[] { e.getMessage() } , PortletMessageType.ERROR);
		}
	}
	
	private CardStateInfo saveActiveCardState(CardPortletSessionBean sessionBean){
		CardStateInfo activeCardState = null;
		CardPortletCardInfo cardInfo = sessionBean.getActiveCardInfo();
		ObjectId cardId = cardInfo.getCard().getId();		
		if(CardPortlet.CARD_EDIT_MODE.equals(cardInfo.getMode()) && (cardId != null) && checkCurrentUserLocked(cardId, sessionBean.getServiceBean())){
			activeCardState = createCardStateInfo(cardInfo, sessionBean);	
		}
		return activeCardState;
	}
	
	private CardStateInfo createCardStateInfo(CardPortletCardInfo cardInfo, CardPortletSessionBean sessionBean){
		CardStateInfo cardStateInfo = new CardStateInfo();		
		cardStateInfo.setMode(cardInfo.getMode());
		cardStateInfo.setPerson(sessionBean.getServiceBean().getPerson());
		return cardStateInfo;
	}
	
	private boolean checkCurrentUserLocked(ObjectId cardId, DataServiceBean serviceBean){
		CheckLock checkLock = new CheckLock(cardId);
		try{
			serviceBean.doAction(checkLock);
		}catch (Exception e) {
			logger.warn("The object is locked not current user!");
			return false;
		}
		return true;
	}
	
	
	protected static class CardStateInfo {
		private String mode;
		private Person person;

		public String getMode() {
			return mode;
		}

		public void setMode(String mode) {
			this.mode = mode;
		}

		public Person getPerson() {
			return person;
		}

		public void setPerson(Person person) {
			this.person = person;
		}
	}

	public static class TemplateForCreationSelector {

		private CardPortletSessionBean sessionBean;
		private PortletFormManager formManager;
		private List<ObjectId> templateIds;
		private SelectionHandler selectionHandler;

		public interface SelectionHandler {
		    void selected(ObjectId templateId);
		}

		public class TemplateChooseForm implements PortletForm {

		    public final static String JSP_PATH = "/WEB-INF/jsp/html/SelectTemplate.jsp";

		    public void doFormView(RenderRequest request,
			    RenderResponse response) throws IOException,
			    PortletException {
				request.getPortletSession().getPortletContext()
					.getRequestDispatcher(JSP_PATH).include(request, response);
		    }

		    public void processFormAction(ActionRequest request,
			    ActionResponse response) throws IOException,
			    PortletException {
				String action = request.getParameter(CardPortlet.ACTION_FIELD);
				if (CardPortlet.CREATE_CARD_ACTION.equals(action)) {
					String template = request.getParameter(CardPortlet.TEMPLATE_ID_FIELD);
					selected(new ObjectId(Template.class, new Long(template)));
				} else if (CardPortlet.BACK_ACTION.equals(action)){
					closeForm();
				}
			}
		}

		public TemplateForCreationSelector(CardPortletSessionBean sessionBean,
			List<ObjectId> templateIds) {
		    this.sessionBean = sessionBean;
		    this.templateIds = templateIds;
		    this.formManager = sessionBean.getActiveCardInfo().getPortletFormManager();
		}

		public void select(SelectionHandler handler) {
		    this.selectionHandler = handler;
		    initTemplatesList();
		    formManager.openForm(new TemplateChooseForm());
		}

		protected final void selected(ObjectId templateId) {
		    closeForm();
		    if (selectionHandler != null) {
				selectionHandler.selected(templateId);
		    }
		}

		protected final void closeForm(){
		    formManager.closeForm();
		}

		private void initTemplatesList() {
		    DataServiceBean serviceBean = sessionBean.getServiceBean();
		    final List<Template> result = new ArrayList<Template>();
		    try {
				Filter filter;
				if (templateIds != null && !templateIds.isEmpty()) {
					filter = new TemplateIdListFilter();
					((TemplateIdListFilter) filter).setTemplateIds(templateIds);
				} else {
					filter = new TemplateForCreateNewCard();
				}
				Collection<Template> templateList = serviceBean.filter(Template.class, filter);
				Collections.sort((List<Template>)templateList, new TemplateComparator());
				final CreateCard createCardAction = new CreateCard();
				for (Template template : templateList) {
					createCardAction.setTemplate(template.getId());
					if (serviceBean.canDo(createCardAction)) {
						result.add(template);
					}
				}
		    } catch (Exception e) {
				e.printStackTrace(System.out);
		    }
		    sessionBean.setTemplateList(result);
		}
	}
}
