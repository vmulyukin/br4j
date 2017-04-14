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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.aplana.dbmi.card.actionhandler.CardPortletActionsManager;
import com.aplana.dbmi.gui.BlockView;
import com.aplana.dbmi.gui.EmbeddablePortletFormManager;
import com.aplana.dbmi.gui.TabsManager;
import com.aplana.dbmi.message.CardChangeEvent;
import com.aplana.dbmi.message.Event;
import com.aplana.dbmi.message.EventContext;
import com.aplana.dbmi.message.EventListener;
import com.aplana.dbmi.model.AttributeViewParam;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataService;
import com.aplana.dbmi.service.ServiceException;

public class CardPortletCardInfo implements EventListener {
	public interface CloseHandler {
		void afterClose(CardPortletCardInfo closedCardInfo, CardPortletCardInfo previousCardInfo) throws DataException, ServiceException;
	}
	
	/**
	 * Class implementing this interface can perform custom actions
	 * instead of saving card to the database 
	 * @author dstarostin
	 */
	public interface CustomStoreHandler {
		/**
		 * This method invokes instead of {@link DataService#saveObject(com.aplana.dbmi.service.User, com.aplana.dbmi.model.DataObject)}
		 * from {@link CardPortlet}
		 * @throws DataException if something goes wrong
		 */
		void storeCard() throws DataException;
		/**
		 * @return title of the button responsible for custom store action 
		 */
		String getStoreButtonTitle();
		
		String getCloseActionString();
		
		/**
		 * Enum ��� ��������� �������� �������� �������� �� ������ "�������"
		 * @author ppolushkin
		 *
		 */
		public enum CloseHandlerPolicy {
			
			// ������� ��� ������� �������������
			DIRECT("direct"),
			
			// ������ ������������ ������ �� ������������� ��������
			CONFIRM("confirm");
			
			private String policyName;
			
			public String getPolicyName() {
				return policyName;
			}
			
			CloseHandlerPolicy(String policyName) {
				this.policyName = policyName;
			}
		}
	}
	
	/**
	 * ��������� ��� ��������� ����� �������
	 * @author ppolushkin
	 *
	 */
	public interface CustomChangeStateHandler {
		
		WorkflowMove getWorkflowMove();
		
		void setWorkflowMove(WorkflowMove flow);
		
		void changeState() throws DataException, ServiceException;
		/**
		 * @return title of the button responsible for custom store action 
		 */
		String getChangeStateButtonTitle();
		
		String getChangeStateActionName();
		
		boolean isShowFirstSaveButton();
	}
	
	public void setCardState(CardState cardState) {
		this.cardState = cardState;
	}
	private String mode;
	private CardState cardState;
	private Card card;
	final private Map< ObjectId, Map<String, Object>> attributeEditorsData = 
		new HashMap<ObjectId, Map<String, Object>>();
	private boolean canChange;
	private boolean canCreate;	
	private List<WorkflowMove> availableWorkflowMoves = new ArrayList<WorkflowMove>();
	private Collection<AttributeViewParam> attributeViewParams;
	private boolean isPrintMode = false;
	private boolean openedInEditMode = false;
	final private EmbeddablePortletFormManager portletFormManager = new EmbeddablePortletFormManager();
	private CloseHandler closeHandler;
	private boolean refreshRequired = true;
	private boolean reloadRequired = false;
	private TabsManager tabsManager = new TabsManager();
	private CardPortletActionsManager actionsManager;
	private CustomStoreHandler storeHandler;
	private CustomChangeStateHandler changeStateHandler;
	private CardPortletCardInfo parentCardInfo;
	private Map<ObjectId, List<BlockView>> tabInfo;

	/**
	 * �������� �������������� ��������� (������ � �������������), ������� �� ������������
	 * � ������ ��������� �������� (��������)
	 */
	final private Set<String> collapsedItems = new HashSet<String>();
	
	/**
	 * Set id of blocks that must be collapsed in currently
	 */
	private HashSet<String> currentViewBlocks = null;

	public CardPortletCardInfo() {
	}

	public TabsManager getTabsManager() {
		return tabsManager;
	}

	public void setTabsManager(TabsManager tabsManager) {
		this.tabsManager = tabsManager;
	}

	public CloseHandler getCloseHandler() {
		return closeHandler;
	}

	public void setCloseHandler(CloseHandler closeHandler) {
		this.closeHandler = closeHandler;
	}
	
	public boolean isOpenedInEditMode() {
		return openedInEditMode;
	}

	public void setOpenedInEditMode(boolean openedInEditMode) {
		this.openedInEditMode = openedInEditMode;
	}

	public EmbeddablePortletFormManager getPortletFormManager() {
		return portletFormManager;
	}

	public Card getCard() {
		return card;
	}

	public void setCard(Card card) {
		this.card = card;
		
		/* ��������� �� BR4J00040199 (mem leak, 14.08.2015)
		if (card.getId() != null){
			EventContext.getInstance().regEventListener(
					new CardChangeEvent(card.getId()), 
					this);
		}*/
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		//���� ����������� �����, �� ������� ������������ �������
		if (mode != null && !mode.equals(this.mode)) {
			this.tabInfo = null;
		}
		this.mode = mode;
	}
	
	public boolean isPrintMode() {
		return isPrintMode;
	}

	public void setPrintMode(boolean isPrintMode) {
		this.isPrintMode = isPrintMode;
	}
	
	public boolean isItemCollapsed(String item) {
		return collapsedItems.contains(item);
	}
	
	public void setItemCollapsed(String item, boolean collapsed) {
		if (collapsed)
			collapsedItems.add(item);
		else
			collapsedItems.remove(item);
	}
	
	public boolean isCanChange() {
		return canChange;
	}

	public void setCanChange(boolean canChange) {
		this.canChange = canChange;
	}
	
	public boolean isCanCreate() {
		return canCreate;
	}

	public void setCanCreate(boolean canCreate) {
		this.canCreate = canCreate;
	}
	
	public CardState getCardState() {
		return cardState;
	}

	public List<WorkflowMove> getAvailableWorkflowMoves() {
		return availableWorkflowMoves;
	}

	/**
	 * @return ������ ��������� {@link WorkflowMove}, ��������������� � ���������� 
	 * �������� ������������� ��������.
	 */
	public List<WorkflowMove> getAvailableWorkflowMovesSorted() {
		final List<WorkflowMove> result = new ArrayList<WorkflowMove>();
		if (availableWorkflowMoves != null) {
			result.addAll(availableWorkflowMoves);

			Collections.sort(result, new Comparator<WorkflowMove>(){
				public int compare(WorkflowMove wfm1, WorkflowMove wfm2) {
					if (wfm1 == null || wfm1.getMoveName() == null)
						return (wfm2 == null  || wfm2.getMoveName() == null) ? 0 : -1;
					return wfm1.getMoveName().compareToIgnoreCase(wfm2.getMoveName());
				}});
		}

		return result;
	}

	public void setAvailableWorkflowMoves(List<WorkflowMove> list) {
		this.availableWorkflowMoves = (list != null)
			? list : new ArrayList<WorkflowMove>();
	}

	public void setAttributeEditorData(ObjectId attrId, String key, Object data) {
		Map<String, Object> editorData = attributeEditorsData.get(attrId);
		if (editorData == null) {
			editorData = new HashMap<String, Object>();
			attributeEditorsData.put(attrId, editorData);
		}
		editorData.put(key, data);
	}

	public Object getAttributeEditorData(ObjectId attrId, String key) {
		final Map<String, Object> editorData = attributeEditorsData.get(attrId);
		return (editorData == null) ? null : editorData.get(key);	
	}

	public void resetAttributeEditorData(ObjectId attrId) {
		if (attrId != null)
			attributeEditorsData.remove(attrId);
	}

	public void resetAttributeEditorData(ObjectId attrId, String key) {
		if (attrId != null && key != null) {
			final Map<String, Object> editorData = attributeEditorsData.get(attrId);
			if (editorData != null) editorData.remove(key);
		}
	}

	public void clearAttributeEditorsData() {
		attributeEditorsData.clear();
	}
	
	/**
	 * Returns currentViewBlocks
	 * @return currentViewBlocks
	 */
	public Set<String> getCurrentViewBlocks() {
		return currentViewBlocks;
	}
	
	/**
	 * Returns <code>true</code> if currentViewBlocks contains given id of block
	 * @param block id block in the form of "column_order", where the column and order correspond to fields TemplateBlock of object of the given block
	 * @return <code>true</code> if currentViewBlocks contains given id of block
	 */
	public boolean containsCurrentViewBlocks(String block) {
		return currentViewBlocks.contains(block);
	}
	
	/**
	 * Adds id block to currentViewBlock
	 * @param block id block in the form of "column_order", where the column and order correspond to fields TemplateBlock of object of the given block
	 */
	public void addItemCurrentViewBlocks(String block) {
		currentViewBlocks.add(block);
	}
	
	/**
	 * Clears currentViewBlocks
	 */
	public void clearCurrentViewBlocks() {
		currentViewBlocks = new HashSet<String>();
	}

	public Collection<AttributeViewParam> getAttributeViewParams() {
		return attributeViewParams;
	}

	public void setAttributeViewParams(Collection<AttributeViewParam> attrViewParams) {
		this.attributeViewParams = attrViewParams;
	}
	
	public boolean isRefreshRequired() {
		return refreshRequired;
	}

	public void setRefreshRequired(boolean refreshRequired) {
		this.refreshRequired = refreshRequired;
	}
	
	public CardPortletActionsManager getActionsManager() {
		return actionsManager;
	}

	public void setActionsManager(CardPortletActionsManager actionsManager) {
		this.actionsManager = actionsManager;
	}

	public CustomStoreHandler getStoreHandler() {
		return storeHandler;
	}

	public void setStoreHandler(CustomStoreHandler storeHandler) {
		this.storeHandler = storeHandler;
	}
	
	public CustomChangeStateHandler getChangeStateHandler() {
		return changeStateHandler;
	}

	public void setChangeStateHandler(CustomChangeStateHandler changeStateHandler) {
		this.changeStateHandler = changeStateHandler;
	}

	public boolean getReloadRequired(){
		return reloadRequired;
	}

	public void setReloadRequired(boolean reloadRequired){
		this.reloadRequired = reloadRequired;
	}
	
	public void onEvent(Event event) {		
		refreshRequired = true;
		reloadRequired = true;
	}
	
	public void release(){
		/* ��������� �� BR4J00040199 (mem leak, 14.08.2015)
		if (card != null && card.getId() != null){
			EventContext.getInstance().unregEventListener(
					new CardChangeEvent(card.getId()), 
					this);
		}*/
	}
	
	public void clearAvailableWorkflowMoves() {
		availableWorkflowMoves = new ArrayList<WorkflowMove>();
	}
	
	public CardPortletCardInfo getParentCardInfo() {
		return parentCardInfo;
	}

	public void setParentCardInfo(CardPortletCardInfo parentCardInfo) {
		this.parentCardInfo = parentCardInfo;
	}
	
	public void setTabInfo(Map<ObjectId, List<BlockView>> tabInfo) {
		this.tabInfo = tabInfo;
	}
	
	public Map<ObjectId, List<BlockView>> getTabInfo() {
		return tabInfo;
	}
}
