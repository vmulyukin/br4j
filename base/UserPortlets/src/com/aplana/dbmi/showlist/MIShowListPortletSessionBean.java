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
package com.aplana.dbmi.showlist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javax.portlet.PortletRequest;
import javax.portlet.PortletSession;
import javax.servlet.http.HttpServletRequest;

import com.aplana.dbmi.action.CheckRolesForUser;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.dbmi.model.WorkflowMove;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.PortletUtil;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.dbmi.service.ServletUtil;
import com.aplana.dbmi.service.UserPrincipal;
import com.aplana.dbmi.showlist.MIShowListPortlet.PagedList;

/**
 *
 * A sample Java bean that stores portlet instance data in portlet session.
 *
 */
public class MIShowListPortletSessionBean {
	
	public enum GroupExecutionMode{
		DISABLE, HEAD, ASISTENT
	};
	
	public enum GroupResolutionMode{
		DISABLE, HEAD, ASISTENT
	};	

	private DataServiceBean serviceBean = null;

	private String message = null; 
	private boolean isPrintMode = false; 

	private String cardPortletPath = "/wps/myportal/mi/card"; 
	
	private boolean isFavoritesViewPortlet = false;
	private boolean isRequestViewPortlet = false;
	
	// access
	private boolean canCreate = false; 
	private boolean canMoveAll = false;
	private boolean showCreate = true;	// ������� ���������� ������� ������ "������� ��������" (���� ����� false, �� ������� ���� �� �������� �������� ������ ������������) 
	
	// view list properties
	private int listSize = 10;
	private List metaDataDesc = null;
	private List dataList = null;
	private List<Card> cardList = null;
	private ReferenceValue title = null;
	private Map<Card, WorkflowMove> cardMoves = new HashMap<Card, WorkflowMove>();
	// >>> (2009/10/07)
	// preferences
	private boolean showHeader = true;
	private String headerJsp = null;  
	private String headerText = null;
	private String linkUrl = null;  
	private String linkPg = null;
	// <<< (2009/10/07)

	// >>> (2009/10/15)
	// preferences
	private boolean showBtnRefresh = true;
	private boolean showToolbar = true;
	// <<< (2009/10/15)

	// (2010/03, RuSA)
	private int dataOffset = 0; // 1 ��� �����, ����� � ������ ����� ���-�� ������� � id
	private Search execSearch;

	private Long currentTemplate;
	private Long showCardListPermissions;
	// ����� ����������� ��������/������� ������ � Excel
	private boolean canExportCards=false;	
	private boolean canImportCards=false;

	// id �������� ������� ��� �������� � csv
	private ObjectId csvTemplateCardId;
	private String customImportTitle=null;

	private String downloadImportTemplate; // ������ ��� ������� ��������
	// true: ���������� ������ ��������������/�������� � ������ ������ ������
	private boolean showRowIconEditView = true;

	private Set<Long> visitedcards = new HashSet<Long>();
	private ResourceBundle resourceBundle;
	private RowExData rowExData = null;
	private Search deloSearch;

	private GroupExecutionMode canGroupExection = GroupExecutionMode.DISABLE;
	
	private GroupResolutionMode groupResolutionMode = GroupResolutionMode.DISABLE;
	
	private String editAccessRoles;	// ������ �����, ������� � �������� �������� ����� �������������� (��������� ��������, ������������� ���������, ������������� �����)
	
	private Map<ObjectId, Map<ObjectId,ObjectId>> deloWorkflowMoves;
	
	protected DataServiceBean getServiceBean(PortletRequest request) {
		if (serviceBean == null) {
			initDataServiceBean(request);
		} else {
    		String userName = (String) request.getPortletSession().getAttribute(DataServiceBean.USER_NAME, PortletSession.APPLICATION_SCOPE);
            if (userName != null) {
                serviceBean.setUser(new UserPrincipal(userName));
                serviceBean.setIsDelegation(true);
                serviceBean.setRealUser(request.getUserPrincipal());
            } else {
                serviceBean.setUser(request.getUserPrincipal());
                serviceBean.setIsDelegation(false);
            }
		}
		return serviceBean;
	}
	
	public DataServiceBean getServiceBean(HttpServletRequest request, String namespace) {
		if (serviceBean == null) {
			initDataServiceBean(request);
		} else {
    		String userName = (String) request.getSession().getAttribute(DataServiceBean.USER_NAME);
            if (userName != null) {
                serviceBean.setUser(new UserPrincipal(userName));
                serviceBean.setIsDelegation(true);
                serviceBean.setRealUser(request.getUserPrincipal());
            } else {
                serviceBean.setUser(request.getUserPrincipal());
                serviceBean.setIsDelegation(false);
            }
		}
		return serviceBean;
	}
	
	private void initDataServiceBean(PortletRequest request) {
		this.serviceBean = PortletUtil.createService(request);
	}
	
	private void initDataServiceBean(HttpServletRequest request) {
		this.serviceBean = ServletUtil.createService(request);
	}
	
	public int getListSize() {
		return listSize;
	}

	public void setListSize(int listSize) {
		this.listSize = listSize;
	}
	
	public void reset() {
		this.metaDataDesc = null;
		this.dataList = null;
		this.title = null;
		this.visitedcards = new HashSet<Long>();
		execSearch = null;
	}

	public RowExData getRowExData() {
		return rowExData;
	}
	
	public void setRowExData(RowExData rowExData) {
		this.rowExData = rowExData;
	}

	public Set<Long> getVisitedCards(){
		return visitedcards;
	}

	public void setVisitedCards(Set<Long> cardIds){
		this.visitedcards = (cardIds != null) ? cardIds : new HashSet<Long>(); 
	}

	public MIShowListPortlet.PagedList getDataList() {
		return (PagedList) ((dataList != null) ? dataList : new MIShowListPortlet.PagedList(new ArrayList<Object>(), new Search.Filter()));
	}

	public void setDataList(List dataList) {
		this.dataList = dataList;
	}
	
	public List<Card> getCardList() {
		
		return (cardList != null) ? cardList : new ArrayList<Card>();
	}

	public void setCardList(List<Card> cardList) {
		this.cardList = cardList;
	}
	
	public List getMetaDataDesc() {
		return metaDataDesc == null ? new ArrayList() : metaDataDesc;
	}

	public void setMetaDataDesc(List metaDataDesc) {
		this.metaDataDesc = metaDataDesc;
	}

	public ReferenceValue getTitle() {
		return title ;
	}

	public void setTitle(ReferenceValue title) {
		this.title = title;
	}
	
	public boolean isPrintMode() {
		return isPrintMode;
	}

	public void setPrintMode(boolean isPrintMode) {
		this.isPrintMode = isPrintMode;
	}

	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getCardPortletPath() {
		return cardPortletPath;
	}
	public void setCardPortletPath(String cardPortletPath) {
		this.cardPortletPath = cardPortletPath;
	}
	public boolean isFavoritesViewPortlet() {
		return isFavoritesViewPortlet;
	}
	public void setFavoritesViewPortlet(boolean isFavoritesViewPortlet) {
		this.isFavoritesViewPortlet = isFavoritesViewPortlet;
	}
	public boolean isRequestViewPortlet() {
		return isRequestViewPortlet;
	}
	public void setRequestViewPortlet(boolean isRequestViewPortlet) {
		this.isRequestViewPortlet = isRequestViewPortlet;
	}
	public boolean isCanCreate() {
		return canCreate;
	}
	public void setCanCreate(boolean canCreate) {
		this.canCreate = canCreate;
	}
	public boolean isCanMoveAll() {
		return canMoveAll;
	}
	public void setCanMoveAll(boolean canMoveAll){
		this.canMoveAll = canMoveAll;
	}
	public Map<Card,WorkflowMove> getCardMoves() {
		return this.cardMoves;
	}
	public void setCardMoves(Map<Card, WorkflowMove> cardMoves) {
		this.cardMoves = cardMoves;
	}
	public boolean isCanExportCards() {
		return canExportCards;
	}
	public void setCanExportCards(boolean canExportCards) {
		this.canExportCards = canExportCards;
	}
	public boolean isCanImportCards() {
		return canImportCards;
	}
	public void setCanImportCards(boolean canImportCards) {
		this.canImportCards = canImportCards;
	}

	public int getDefaultSortColumn() {
		for (int i = 0; i < getMetaDataDesc().size(); i++) {
			SearchResult.Column col = (SearchResult.Column) metaDataDesc.get(i);
			if (col.getSorting() != SearchResult.Column.SORT_NONE)
				return i + 1;
		}
		return 1;
	}
	public void setShowHeader(boolean showHeader) {
		this.showHeader = showHeader;
	}
	public boolean isShowHeader() {
		return showHeader;
	}
	
	public void setLinkUrl(String linkUrl) {
		this.linkUrl = linkUrl;
	}
	public String getLinkUrl() {
		return linkUrl;
	}
	
	public void setLinkPg(String linkPg) {
		this.linkPg = linkPg;
	}
	public String getLinkPg() {
		return linkPg;
	}
	
	public void setHeaderJsp(String headerJsp) {
		this.headerJsp = headerJsp;
	}
	public String getHeaderJsp() {
		return headerJsp;
	}
	
	public void setHeaderText(String headerText) {
		this.headerText = headerText;
	}
	public String getHeaderText() {
		return headerText;
	}
	/**
	 * @param(showBtnRefresh) true to show "Refresh list" button
	 */
	public void setShowBtnRefresh(boolean showBtnRefresh) {
		this.showBtnRefresh = showBtnRefresh;
	}
	/**
	 * @return true if button "Refresh list" is visible
	 */
	public boolean isShowBtnRefresh() {
		return showBtnRefresh;
	}
	/**
	 * @param(showToolbar) true if icons must be visible (at the top-right of the portlet area) 
	 */
	public void setShowToolbar(boolean showToolbar) {
		this.showToolbar = showToolbar;
	}
	/**
	 * @return true, if icons are visible (at the top-right of the portlet area) 
	 */
	public boolean isShowToolbar() {
		return showToolbar;
	}
	
	/**
	 * @return �������� �������� ������� ������ ������ � dataList,
	 * ����� ������� ��� ���-�� ���-�� ������� � �������������� ������, 
	 * ��������, ��� ������ ����� � ������ ���� ������� id ����� 1.
	 */
	public int getDataOffset()
	{
		return this.dataOffset; 
	}

	public void setDataOffset(int value)
	{
		this.dataOffset = value;
	}
	public Search getExecSearch() {
		return execSearch;
	}
	public void setExecSearch(Search execSearch) {
		this.execSearch = execSearch;
	}
	public Long getCurrentTemplate() {
		return currentTemplate;
	}
	public void setCurrentTemplate(Long currentTemplate) {
		this.currentTemplate = currentTemplate;
	}
	public void setCurrentTemplate(long currentTemplate) {
		this.currentTemplate = new Long(currentTemplate);
	}
	public void setCurrentTemplate(String currentTemplate) {
		try {
			this.currentTemplate = new Long(currentTemplate);
		} catch (NumberFormatException e) {
			this.currentTemplate = null;
		}
	}
	
	public Long getShowCardListPermissions() {
		return showCardListPermissions;
	}
	public void setShowCardListPermissions(Long showCardListPermissions) {
		this.showCardListPermissions = showCardListPermissions;
	}
	public void setShowCardListPermissions(long showCardListPermissions) {
		this.showCardListPermissions = new Long(showCardListPermissions);
	}
	public void setShowCardListPermissions(String showCardListPermissions) {
		try {
			this.showCardListPermissions = new Long(showCardListPermissions);
		} catch (NumberFormatException e) {
			this.showCardListPermissions = null;
		}
	}

	/**
	 * @return true, if the first icon Edit/View in any result row is visible 
	 */
	public boolean isShowRowIconEditView() {
		return this.showRowIconEditView;
	}

	/**
	 * @param showRowIconEditView set true, to enable the first icon Edit/View in any result row 
	 */
	public void setShowRowIconEditView(boolean showRowIconEditView) {
		this.showRowIconEditView = showRowIconEditView;
	}

	public void setResourceBundle(ResourceBundle bundle) {
		this.resourceBundle = bundle;
	}
	
	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}

	public void setGroupExectionMode(GroupExecutionMode groupExecutionMode) {
		this.canGroupExection = groupExecutionMode;
	}

	public GroupExecutionMode getGroupExectionMode() {
		return canGroupExection;
	}
	
	public boolean isCanGroupExecution(){
		return !GroupExecutionMode.DISABLE.equals(canGroupExection);
	}

	public GroupResolutionMode getGroupResolutionMode() {
		return groupResolutionMode;
	}

	public void setGroupResolutionMode(GroupResolutionMode groupResolutionMode) {
		this.groupResolutionMode = groupResolutionMode;
	}
	
	public boolean isCanGroupResolution(){
		return !GroupResolutionMode.DISABLE.equals(groupResolutionMode);
	}

	public boolean isShowCreate() {
		return showCreate;
	}

	public void setShowCreate(boolean showCreate) {
		this.showCreate = showCreate;
	}

	public String getCustomImportTitle() {
		return customImportTitle;
	}

	public void setCustomImportTitle(String customImportTitle) {
		this.customImportTitle = customImportTitle;
	}

	public void setDownloadImportTemplate(String downloadImportTemplate) {
		this.downloadImportTemplate = downloadImportTemplate;
	}
	
	public String getDownloadImportTemplate() {
		return downloadImportTemplate;
	}

	public String getEditAccessRoles() {
		return editAccessRoles;
	}

	public void setEditAccessRoles(String editAccessRoles) {
		this.editAccessRoles = editAccessRoles;
	}
	
	/**
	 * ��������� ������� � �������� ������������ ���� �� �������������� �������� � ��������.
	 * ����� ���������� ���������� ���������� � ������ ������ � ����� ��� ����, 
	 * ����� ��� ���������/������������ ���� � ������ ������ ���� ��� ����� ������ ���������/��������� ��� ������������ 
	 * @return true - ����� ����, false - ���� ���
	 */
	public boolean isEditAccessExists() throws DataException, ServiceException{
		if (editAccessRoles != null&&!editAccessRoles.isEmpty()){
			CheckRolesForUser checkAction = new CheckRolesForUser();
			checkAction.setPersonLogin(serviceBean.getUserName());
			checkAction.setRoles(editAccessRoles);
			return (Boolean)serviceBean.doAction(checkAction);
		}
		// ���� ��������������� ���� �� ������, �� ����� �� �������������� � �������� ������������ ����
		return true;
	}

	public ObjectId getCsvTemplateCardId() {
		return csvTemplateCardId;
	}

	public void setCsvTemplateCardId(ObjectId csvTemplateCardId) {
		this.csvTemplateCardId = csvTemplateCardId;
	}
	
	/**
	 * ���������� WorkflowMove ��� �������� �������� �� �������� ������� � ���� 
	 * @param templateId - ������� ������ ��������
	 * @param statusId - ������� ������ ��������
	 * @return WorkflowMove ��� �������� � ����
	 */
	public WorkflowMove getDeloWorkflowMove(ObjectId templateId, ObjectId statusId){
		if (this.deloWorkflowMoves == null){
			this.deloWorkflowMoves = getDeloWorkflowMoves();
		}
		if(this.deloWorkflowMoves.get(templateId) == null ||
				this.deloWorkflowMoves.get(templateId).get(statusId) == null){
			return null;
		}
		return DataObject.createFromId(this.deloWorkflowMoves.get(templateId).get(statusId));
	}
	
	private Map<ObjectId, Map<ObjectId,ObjectId>> getDeloWorkflowMoves(){
		Map<ObjectId, Map<ObjectId,ObjectId>> deloWorkflowMoves = new HashMap<ObjectId, Map<ObjectId,ObjectId>>();
		ObjectId incomingId = ObjectId.predefined(Template.class, "jbr.incoming");
		ObjectId ogId = ObjectId.predefined(Template.class, "jbr.incomingpeople");
		Map<ObjectId,ObjectId> incomingWfms = new HashMap<ObjectId, ObjectId>(){{
			put(CardState.REGISTRATION, IdUtils.getWfmId("jbr.incoming.registration.delo"));
			put(CardState.READY_TO_DELO, IdUtils.getWfmId("jbr.incoming.ready-to-write-off.delo"));
			put(CardState.EXECUTION, IdUtils.getWfmId("jbr.incoming.execution.archive"));
			put(CardState.CONSIDERATION, IdUtils.getWfmId("jbr.incoming.consideration.delo"));
		}};
		//� �� � �� ���� ��
		deloWorkflowMoves.put(incomingId, incomingWfms);
		deloWorkflowMoves.put(ogId, incomingWfms);
		//���������
		ObjectId outcomingId = ObjectId.predefined(Template.class, "jbr.outcoming");
		Map<ObjectId,ObjectId> outcomingWfms = new HashMap<ObjectId, ObjectId>(){{
			put(CardState.REGISTRATION, IdUtils.getWfmId("jbr.outcoming.registration.delo"));
		}};
		deloWorkflowMoves.put(outcomingId,outcomingWfms);
		//���-���
		ObjectId npaId = ObjectId.predefined(Template.class, "jbr.npa");
		ObjectId ordId = ObjectId.predefined(Template.class, "jbr.ord");
		Map<ObjectId,ObjectId> ordWfms = new HashMap<ObjectId, ObjectId>(){{
			put(CardState.EXECUTION, IdUtils.getWfmId("jbr.ord.execution.archive"));
			put(CardState.READY_TO_DELO, IdUtils.getWfmId("jbr.ord.ready-to-write-off.delo"));
		}};
		deloWorkflowMoves.put(npaId, ordWfms);
		deloWorkflowMoves.put(ordId, ordWfms);
		//����������
		ObjectId internalId = ObjectId.predefined(Template.class, "jbr.interndoc");
		Map<ObjectId,ObjectId> internalWfms = new HashMap<ObjectId, ObjectId>(){{
			put(CardState.EXECUTION, IdUtils.getWfmId("jbr.interndoc.execution.archive"));
			put(CardState.READY_TO_DELO, IdUtils.getWfmId("jbr.interndoc.ready-to-write-off.delo"));
		}};
		deloWorkflowMoves.put(internalId, internalWfms);
		return deloWorkflowMoves;
	}

	public Search getDeloSearch() {
		return deloSearch;
	}

	public void setDeloSearch(Search deloSearch) {
		this.deloSearch = deloSearch;
		if(deloSearch != null ){
			List<SearchResult.Column> columns = new ArrayList<SearchResult.Column>();
			columns.add(new SearchResult.Column(Card.ATTR_TEMPLATE));
			columns.add(new SearchResult.Column(Card.ATTR_STATE));
			this.deloSearch.setColumns(columns);
		}
	}
}