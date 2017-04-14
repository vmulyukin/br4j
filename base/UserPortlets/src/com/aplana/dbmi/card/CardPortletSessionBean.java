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
package com.aplana.dbmi.card;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.portlet.PortletRequest;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.card.CardPortletSessionBean.PortletMessage.PortletMessageType;
import com.aplana.dbmi.card.doclinked.DoclinkCreateData;
import com.aplana.dbmi.crypto.SignatureData;
import com.aplana.dbmi.gui.LinkChooserBean;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.TypedCardLinkAttribute;
import com.aplana.dbmi.service.AsyncDataServiceBean;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.owriter.manager.OWriterSessionManager;

/**
 * A sample Java bean that stores portlet instance data in portlet session.
 */
public class CardPortletSessionBean implements EditorsDataContainingSessionBean{
	private AsyncDataServiceBean serviceBean = null;
	private ResourceBundle resourceBundle = null;
	//private String message = null; 
	private PortletMessage message = null; // = new PortletMessage();
	private String adminEmail = "";
	private String backURL = null; 
	private List<Template> templateList = null;
	private LinkedList<CardPortletCardInfo> cards;
	private DoclinkCreateData doclinkCreateData;
	private CardPortletDialog dialog;
	
	private AttributeEditorDialog attributeEditorDialog;
	private List<Card> groupExecutionReportsSameCard;
	private List<ObjectId> dublicateIds = null;
	private boolean disableDS = false;
	private Person currentPerson;
	private boolean verify_ds = false;
	private OWriterSessionManager sessionManager = null;
	private ObjectId viewMode;
	private Map<Long, String> repeatedDocuments; 
	

	public CardPortletSessionBean() {
		reset();
	}

	/**
	 * After the user select the link to add in the editor, 
	 * this object contains the necessary information to add a content TAG to the 
	 * active editor.
	 **/  
	private LinkChooserBean linkChooserBean = new LinkChooserBean();
	private String stampPosition;
	
	public LinkChooserBean getLinkChooserBean() {
		return linkChooserBean;
	}

	public void setLinkChooserBean(LinkChooserBean linkChooserBean) {
		this.linkChooserBean = linkChooserBean;
	}
	
	public void reset() {
		message = null; // message.setMessage(null);
		backURL = null; 
		// Select Template Mode Properties	
		templateList = null;
		cards = new LinkedList<CardPortletCardInfo>();
	}
	
	public AsyncDataServiceBean getServiceBean() {
		return serviceBean;
	}
	
	public void setDataServiceBean(AsyncDataServiceBean serviceBean) {
		this.serviceBean = serviceBean;
	}
	
	public CardPortletCardInfo getActiveCardInfo() {
		return (cards.isEmpty()) ? null : cards.getLast();
	}
	
	public void setActiveCardInfo(CardPortletCardInfo cardInfo) {
		cards.add(cardInfo);
	}
	
	public void closeActiveCard() throws DataException, ServiceException {
		final CardPortletCardInfo lastCardInfo = cards.removeLast();
		if (lastCardInfo.getCloseHandler() != null) {
			lastCardInfo.getCloseHandler().afterClose(lastCardInfo, getActiveCardInfo());
		}
		if (getActiveCardInfo() != null) {
			// ���������� �������� �������� �� ������...
			final boolean isViewMode = CardPortlet.CARD_VIEW_MODE.equals(getActiveCardInfo().getMode());
			if (isViewMode)
				getActiveCardInfo().setRefreshRequired(true);
		}
	}
	
	public List<CardPortletCardInfo> getOpenedActiveCards() {
		ArrayList<CardPortletCardInfo> openedCards = new ArrayList<CardPortletCardInfo>();
		for (CardPortletCardInfo c : cards) {
			if(c.getCard().getId() != null){
				if (!c.getCard().getId().equals(getActiveCard().getId()) && CardPortlet.CARD_EDIT_MODE.equals(c.getMode())) {
					openedCards.add(c);
				}
			}
		}
		return openedCards;
	}
	
	/**
	 * (ppolushkin, 34772)
	 * ������������ � CardEdit � CardView ������ getOpenedActiveCards() ��� ������������ ������ ��������,
	 * ������� ���������� ��������� ��� �������� �������/��������
	 * 
	 * ����� ������������� ����� getOpenedActiveCards(), ����� ������� �������� � ������ ���������, 
	 * ������� ������� ����� �� ����� ���� ����������� (� �� �������� ��������� �� ����� ����), 
	 * ������ � ����������� �������� � CardEdit.jsp ����� ��������� ��� �������� � ���� ���� ������� �������.
	 */
	public List<CardPortletCardInfo> getAllOpenedActiveCards() {
		ArrayList<CardPortletCardInfo> openedCards = new ArrayList<CardPortletCardInfo>();
		for (CardPortletCardInfo c : cards) {
			if(c.getCard().getId() != null){
				if (CardPortlet.CARD_EDIT_MODE.equals(c.getMode())) {
					openedCards.add(c);
				}
			}
		}
		return openedCards;
	}
	
	/**
	 * ���������� ��������, ������� � ��������� ������ ������������ CardPortlet'��
	 * ��������� ������ getActiveCardInfo().getCard();
	 * @return ��������, �������� � ��������� ������
	 * @throws NullPointerException ���� getActiveCardInfo() ���������� null
	 */
	public Card getActiveCard() {
		return getActiveCardInfo().getCard();
	}
	
	/**
	 * ���������� �������� ������������ �� ������ ����������� (ADMIN_703174)
	 * @param attr - ����������� �������
	 * @return true - ���� ��������� �������� �������������, false - �� ����
	 */
	public boolean isNeedToSkipCurrentUser(Attribute attr) {
		//���� ������� �� ������ ����������� - ��������� �������� ������������ �� ����
		if(attr == null || !attr.getId().equals(ObjectId.predefined(TypedCardLinkAttribute.class, "jbr.visa.template.person")))
			return false;
		Card parentCard = null;
		if(getActiveCardInfo() != null && getActiveCardInfo().getParentCardInfo() != null)
			parentCard = getActiveCardInfo().getParentCardInfo().getCard();
		if(parentCard == null)
			return false;
		//���� ������������ �������� ������� - �����������,
		//������ �� � �������� ������ �������� ����������� ��� ��������������� ������������
		//������������� �������� ������������ ���� ��������� �� ������ �����������
		if(parentCard.getTemplate().equals(ObjectId.predefined(Template.class, "jbr.visa")))
			return true;
		return false;
	}
	
	/**
	 * @deprecated
	 * @return
	 */
	@Deprecated
	public String getCurrentMode() {
		return getActiveCardInfo().getMode();
	}

	public List<Template> getTemplateList() {
		return templateList == null ? new ArrayList<Template>() : templateList ;
	}

	public void setTemplateList(List<Template> templateList) {
		this.templateList = templateList;
	}

	/// >>>
	public String getMessage() {
		return (message == null) ? null : message.getMessage();
	}
	public void setMessage(String message) {
		if (this.message != null)
			this.message.setMessage(message);
		else 
			this.message = new PortletMessage(message);
	}
	
	public void setMessageWithType(String message, PortletMessageType messageType) {
	
		if (this.message != null){
			this.message.setMessage(message);
			this.message.setMessageType(messageType);
		}
		else 
			this.message = new PortletMessage(message, messageType);
	}

	public PortletMessage getPortletMessage(){
		return this.message;
	}

	public void setPortletMessage(PortletMessage pm){
		this.message = pm;
	}
	/// <<<

	public String getBackURL() {
		return backURL;
	}

	public void setBackURL(String backURL) {
		this.backURL = backURL;
	}

	public String getAdminEmail() {
		return adminEmail;
	}

	public void setAdminEmail(String adminEmail) {
		this.adminEmail = adminEmail;
	}

	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}

	public void setResourceBundle(ResourceBundle resourceBundle) {
		this.resourceBundle = resourceBundle;
	}

	public void setMessage(String key, Object[] params) {
		/*
		message = resourceBundle.getString(key);
		if (params != null)
			message = MessageFormat.format(message, params);
		 */
		String msg = resourceBundle.getString(key);
		if (params != null)
			msg = MessageFormat.format(msg, params);
		this.setMessage(msg);
	}
	
	public void setMessageWithType(String key, Object[] params, PortletMessageType messageType){
		
		String msg = resourceBundle.getString(key);
		if (params != null)
			msg = MessageFormat.format(msg, params);
		this.setMessageWithType(msg, messageType);
	}

	/**
	 * @deprecated
	 * @param attrId
	 * @param key
	 * @return
	 */
	@Deprecated
	public Object getAttributeEditorData(ObjectId attrId, String key) {
		return getActiveCardInfo().getAttributeEditorData(attrId, key);
	}
	
	/**
	 * @deprecated
	 * @param attrId
	 * @param key
	 * @param data
	 */
	@Deprecated
	public void setAttributeEditorData(ObjectId attrId, String key, Object data) {
		getActiveCardInfo().setAttributeEditorData(attrId, key, data);
	}

	/**
	 * @deprecated
	 * @param form
	 */
	@Deprecated
	public void openForm(PortletForm form) {
		getActiveCardInfo().getPortletFormManager().openForm(form);
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	public void closeForm() {
		getActiveCardInfo().getPortletFormManager().closeForm();
	}

	/**
	 * ������� ����� �������� ��� "���������"
	 * ���� �������� "�����" - �.�. � ������ Id, �� �������� ����������� � ������
	 * ��������������, � ��������� ������ ����� ��������� ������������ ��������� ����� openInEditMode
	 */
	public void openNestedCard(Card card, CardPortletCardInfo.CloseHandler closeHandler, boolean openInEditMode) {
		CardPortletCardInfo cardInfo = null;
		if (card!=null&&card.getId()!=null){
			cardInfo = getCardPortletCardInfo(card.getId());
		}
		if (cardInfo==null){
			cardInfo = createCardPortletCardInfo();
			cardInfo.setCloseHandler(closeHandler);
			
			// �������� ����� ���� ������, ���� �������� ������� ��������,
			// �� ������� ��� ���� �������
			if (card != null) {
				cardInfo.setCard(card);
				ObjectId cardId = card.getId();		
				if (cardId == null || openInEditMode) {
					cardInfo.setMode(CardPortlet.CARD_EDIT_MODE);
					cardInfo.setOpenedInEditMode(true);
				} else {
					cardInfo.setMode(CardPortlet.CARD_VIEW_MODE);
				}
			}
		}
		setActiveCardInfo(cardInfo);
	}
	
	public void openNestedCard(Card card, CardPortletCardInfo.CloseHandler closeHandler, 
			CardPortletCardInfo parentCardInfo, boolean openInEditMode) {
		openNestedCard(card, closeHandler, openInEditMode);
		getActiveCardInfo().setParentCardInfo(parentCardInfo);
	}

	protected CardPortletCardInfo createCardPortletCardInfo() {
		CardPortletCardInfo cardInfo = new CardPortletCardInfo();
		return cardInfo;
	}

	/**
	 * ���� ������ �������� ��� ����������� �����, �� �������� � � �������� ���������
	 * @param cardId
	 * @return
	 */
	protected CardPortletCardInfo getCardPortletCardInfo(ObjectId cardId){
		if (cards!=null&&!cards.isEmpty()){
			for (CardPortletCardInfo cardInfo : cards){
				if (cardId.equals(cardInfo.getCard().getId())){
					return  cardInfo;
				}
			}
		}
		return null;
	}
	/**
	 * @return ������ ��� �������� ��������� ����������.
	 */
	public DoclinkCreateData getDoclinkCreateData() {
		return this.doclinkCreateData;
	}

	/**
	 * @param doclinkCreateData ������ ��� �������� ��������� ����������.
	 */
	public void setDoclinkCreateData(DoclinkCreateData doclinkCreateData) {
		this.doclinkCreateData = doclinkCreateData;
	}

	public CardPortletDialog getDialog() {
		return dialog;
	}

	public void setDialog(CardPortletDialog dialog) {
		this.dialog = dialog;
	}


	public static class PortletMessage{
		
		public static final String STYLE_INFO = "msg";
		public static final String STYLE_ERROR = "err_msg";
		public static final String STYLE_EVENT = "evt_msg";

		private String message;
		private List<ObjectId> cardIds;
		private Map<ObjectId, String> container;
		private PortletMessageType messageType;
		
		/**
		 * @author Aleksandr Smirnov
		 * ��� ��������� 
		 */
	    public enum PortletMessageType{
	        ERROR,		// ������
	        INFO,		// ����������� ���������
	        EVENT		// �������� �������
	    }

		public PortletMessage(){
			this.messageType = PortletMessageType.INFO;
		}

		public PortletMessage( String msg){
			this( msg, null, null, PortletMessageType.INFO);
		}

		public PortletMessage(String msg, List<ObjectId> cardIds){
			this( msg, cardIds, null, PortletMessageType.INFO);
		}
		
		public PortletMessage( String msg, PortletMessageType messageType){
			this( msg, null, null, messageType);
		}

		public PortletMessage(String msg, List<ObjectId> cardIds, Map<ObjectId,String> container){
			this( msg, cardIds, null, PortletMessageType.INFO);
		}
		
		public PortletMessage(String msg, List<ObjectId> cardIds, Map<ObjectId,String> container, PortletMessageType messageType){
			this.message = msg;
			this.cardIds = cardIds;
			this.container = container;
			this.messageType = messageType;
		}

		public String getMessageStyle(){
			
			if (this.messageType.equals(PortletMessageType.ERROR)){
				return STYLE_ERROR;
			}else if (this.messageType.equals(PortletMessageType.EVENT)){
				return STYLE_EVENT;
			}
			return STYLE_INFO;
		}
		
		public String getMessage(){
			return this.message;
		}

		public void setMessage(String msg){
			this.message = msg;
		}

		public List<ObjectId> getCardIds(){
			return this.cardIds;
		}

		public void setCardIds(List<ObjectId> cardIds){
			this.cardIds = cardIds;
		}

		public Map<ObjectId, String> getContainer(){
			return this.container;
		}

		public void setContainer( Map<ObjectId,String> container ){
			this.container = container;
		}
		
		public PortletMessageType getMessageType(){
			return this.messageType;
		}
		
		public void setMessageType(PortletMessageType messageType){
			this.messageType = messageType;
		}

	}
	
	public boolean isDsSupport(PortletRequest request) throws DataException, ServiceException{
		DataServiceBean dataService = getServiceBean();		
		Card persCard  = null;
		Person pers = null;
		String certHash = null;
		boolean dsSupport = false;
		
		final String dsSupportAttrName = "DS_SUPPORT";
		
		Boolean dsSupportAttrValue = (Boolean)request.getPortletSession().getAttribute(dsSupportAttrName);
		
		if (dsSupportAttrValue != null){
			dsSupport = dsSupportAttrValue.booleanValue();
		}else{
		
			Search action = new Search();
			dataService.canDo(action); //��� ������������� ������������ ������ DS
			
			pers = dataService.getPerson();
			if(pers != null && pers.getCardId() != null){
					
				ObjectId id = pers.getCardId();
				
				persCard = (Card)dataService.getById(id);
				CardLinkAttribute attrHash = (CardLinkAttribute) persCard.getAttributeById(SignatureData.actualCertificateAttrId);
				if(attrHash != null){
					ObjectId[] certs = attrHash.getIdsArray();
					if (certs != null && certs.length>0){
						dsSupport = true;
					}
				}						
			}
			request.getPortletSession().setAttribute(dsSupportAttrName, dsSupport);
		}

		return dsSupport;
	}

	public Object getEditorData(ObjectId attrId, String key) {
		Object result = null;
		if (null != getActiveCardInfo()) {
			result = getActiveCardInfo().getAttributeEditorData(attrId, key);
		}
		return result;
	}

	public void setEditorData(ObjectId attrId, String key, Object data) {
		getActiveCardInfo().setAttributeEditorData(attrId, key, data);	
	}

	public AttributeEditorDialog getAttributeEditorDialog() {
		return attributeEditorDialog;
	}

	public void setAttributeEditorDialog(AttributeEditorDialog attributeEditorDialog) {
		this.attributeEditorDialog = attributeEditorDialog;
	}
	
	public List<Card> getGroupExecutionReportsSameCard() {
		return groupExecutionReportsSameCard;
	}

	public void setGroupExecutionReportsSameCard(List<Card> groupExecutionReportsSameCard) {
		this.groupExecutionReportsSameCard = groupExecutionReportsSameCard;
	}
	
	public OWriterSessionManager getOWriterSessionManager() {
		return sessionManager;
	}

	public void setOWriterSessionManager(OWriterSessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}
	
	public void setDublicates(List<ObjectId> dublicateIds){
		this.dublicateIds=dublicateIds;
	}
	
	public List<ObjectId> getDublicates(){
		return dublicateIds;
	}
	
	public void clearDublicates(){
		dublicateIds = null;
	}

	public boolean isDisableDS() {
		return disableDS;
	}

	public void setDisableDS(boolean disableDS) {
		this.disableDS = disableDS;
	}

	public Person getCurrentPerson() {
		return currentPerson;
	}

	public void setCurrentPerson(Person currentPerson) {
		this.currentPerson = currentPerson;
	}

	public boolean isVerify_ds() {
		Card card = getActiveCard();
		ListAttribute verify_ds_attr = (ListAttribute) card.getAttributeById(CardPortlet.JBR_VERIFY_DS);
		if (verify_ds_attr != null && verify_ds_attr.getValue() != null && verify_ds_attr.getValue().getId().equals(CardPortlet.JBR_CONTROL_YES)){
			return true;
		} else {
			return false;
		}
	}
	
	public ObjectId getViewMode() {
		return viewMode;
	}

	public void setViewMode(ObjectId viewMode) {
		this.viewMode = viewMode;
	}

	public void setStampPosition(String stampPosition) {
		this.stampPosition = stampPosition;		
	}

	public String getStampPosition() {
		return this.stampPosition;
	}
	
	public Map<Long, String> getRepeatedDocuments() {
		return repeatedDocuments;
	}
	
	public void setRepeatedDocuments(Map<Long, String> repeatedDocuments) {
		this.repeatedDocuments = repeatedDocuments;
	}

}
