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
package com.aplana.dbmi.delegate;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.aplana.dbmi.action.CheckRolesForUser;
import com.aplana.dbmi.action.SearchResult;
import com.aplana.dbmi.model.Delegation;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.PersonView;
//import com.aplana.dbmi.model.PermissionDelegate;
import com.aplana.dbmi.model.PermissionSet;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.dbmi.util.JspUtils;

public class DelegateListSessionBean {

	private static final String RESKEY_TITLE_2ME = "DelegateListPortlet.title.2me";
	private static final String RESKEY_TITLE_HISTORY = "DelegateListPortlet.title.history";
	public static final String RESKEY_TITLE_MY = "DelegateListPortlet.title.my";
	/***************************************************************************
	 * DEFAULT_X 
	 ***************************************************************************/
	final static boolean DEFAULT_ShowBtnRefresh = true;
	final static boolean DEFAULT_ShowToolbar = true;
	final static int DEFAULT_DataOffset = 0;

	/**
	 * ������� ����������� �������� � ���������� �������������.
	 * @author RAbdullin
	 */
	public enum EFilterByActiveMode {
			/**
			 * ���������� � �������� � ����������
			 */
			_Both,			
			/**
			 * ���������� ������ ��������
			 */
			_ActiveOnly,
			/**
			 * ���������� ������ ����������
			 */
			_InactiveOnly
		};

	/***************************************************************************
	 * PRIVATE 
	 ***************************************************************************/
	private DataServiceBean dataServiceBean = null;

	private String backURL = null;
	private String userIdStr = null;
	// private String delegateId = null;

	private boolean sourceUserSelectable = false;

	// ���������� ������������ ����� FilterByActiveMode �� WEB-��������
	private boolean selectableFilterByActiveMode = true;// debug=true, normal=false; 
	private EFilterByActiveMode filterByActiveMode = EFilterByActiveMode._ActiveOnly;

	// ���������� ������������ ����� roleIdFilter �� WEB-��������
	private boolean selectableFilterByRoleId = true; 
	private ObjectId filterByRoleId = null; // null = ���������� ���


	/**
	 * ��������� ������������� ��������� ������ ������.
	 * ��������� ��������������� � true ��� ��������� pgTab, userId;
	 * ����� � false = ������ ������� ������� �����.
	 */
	private boolean needReload = true; 

	//  �������� ��������� � ���������
	private Title title = new Title();
	private TextMessage message;
	private EDelegatePgTab pgTab = EDelegatePgTab.delegateFromPerson; // 0..2

	private boolean showBtnRefresh = DEFAULT_ShowBtnRefresh;
	private boolean showToolbar = DEFAULT_ShowToolbar;

	// �������� ������� ��� ������������ ������ ������ dataList
	private int dataOffset = DEFAULT_DataOffset;
	private List<List<String>> dataList = null;
	private List<SearchResult.Column> columns = null;

	// ��������� ������ ���� �������������
	private List<PersonView> personsDic = null;

	// ��������� ������ ������������ �����
	//private Set<PermissionSet> permissionsDic;

	// ������ ������ ���������
	private final List<Delegation> allDelegates = 
		new ArrayList<Delegation>();

	// ������ (��������) ���������� �������� ������������
	private final List<Delegation> editDelegates = 
		new ArrayList<Delegation>();

	// ������ �����, ������� �������� �������������� �������������
	private String editAccessRoles;
	/**
	 * ���� ����������.
	 * @author RAbdullin
	 */
	static public enum EDelegatePgTab {
		delegateFromPerson,		// ������������� ������
		history,				// ������� �������������
		givenToPerson			// ��������� ���������� �� ������ 
	}

	// �������� �������: pg1=��� ��������/pg2=�������/pg3=���� ���
	// ����� ������� ��� ������ ����������
	private final Map<EDelegatePgTab, String> titleKeys = new HashMap<EDelegatePgTab, String>(3);

	{
		titleKeys.put( EDelegatePgTab.delegateFromPerson, RESKEY_TITLE_MY); 
		titleKeys.put( EDelegatePgTab.history, RESKEY_TITLE_HISTORY);
		titleKeys.put( EDelegatePgTab.givenToPerson, RESKEY_TITLE_2ME);
	}


	/***************************************************************************
	 * INNER SUPPORT CLASSES
	 * @author RAbdullin
	 ***************************************************************************/
	static public class TextMessage {
		final String msg;
		final boolean error;

		public TextMessage(String msg, boolean error) {
			super();
			this.msg = msg;
			this.error = error;
		}

		public TextMessage(String msg) {
			this( msg, false);
		}

		public String getMsg() {
			return msg;
		}

		public boolean isError() {
			return error;
		}

	}


	static public class Title 
	{
		boolean show = true;
		String htmlText = null;

		boolean useJspName = false;
		String jspName = null;

		/**
		 * ���������������� html-������� � ������ �����������.
		 * @param htmlOrText: ������ ����� ��� ������� html-����.
		 * @param show: true=��������� ����������, false=���.
		 */
		public Title(String htmlOrText, boolean show) {
			super();
			this.htmlText = htmlOrText;
			this.show = show;
		}

		/**
		 * ���������������� �������.
		 * @param value
		 */
		public Title(String value) {
			this( value, true);
		}

		public Title() {
		}

		/**
		 * @return ������� ����� ��� html ��� �����������
		 */
		public String getHtmlText() {
			return this.htmlText;
		}

		/**
		 * @param valueText ������� ����� ��� html ��� �����������
		 */
		public void setHtmlText(String valueText) {
			this.htmlText = valueText;
		}

		/**
		 * @return ���� �� ����������: true = ����
		 */
		public boolean isShow() {
			return show;
		}

		/**
		 * @param show ���� �� ����������: true = ����
		 */
		public void setShow(boolean show) {
			this.show = show;
		}

		/**
		 * @return ���� �� ������������ jspName: true=����/ false=��� � 
		 * ���� ������������ htmlText.
		 */
		public boolean isUseJspName() {
			return useJspName;
		}

		/**
		 * @param useJspName ���� �� ������������ jspName: true=����/ false=��� 
		 * (�.�. ���� ������������ htmlText).
		 */
		public void setUseJspName(boolean useJspName) {
			this.useJspName = useJspName;
		}

		/**
		 * @return �������� jsp-�����.
		 */
		public String getJspName() {
			return jspName;
		}

		/**
		 * @param jspName �������� jsp-�����.
		 */
		public void setJspName(String jspName) {
			this.jspName = jspName;
		}

	}


	/***************************************************************************
	 * ACCESS
	 ***************************************************************************/
	/**
	 * �������� ������, ��� ���� ��������� ���������� ������������� ��������� 
	 * � delegates.
	 */
	public void clear() {
		this.needReload = true; // (!) �.�. ������ �� �����������

		this.showBtnRefresh = DEFAULT_ShowBtnRefresh;
		this.showToolbar = DEFAULT_ShowToolbar;

		this.dataOffset = DEFAULT_DataOffset;
		this.columns = null; 	// this.columns.clear();
		this.dataList = null;	// this.dataList.clear();

		this.title = null;
		this.message = null;
		this.backURL = null;

		// this.delegateId = null;
		this.personsDic = null;
		//this.permissionsDic = null;
		this.allDelegates.clear();
		this.editDelegates.clear();
	}


	public boolean isNeedReload() {
		return needReload;
	}


	public void setNeedReload(boolean needReload) {
		this.needReload = needReload;
	}


	public DataServiceBean getDataServiceBean() {
		return this.dataServiceBean;
	}


	public void setDataServiceBean(DataServiceBean serviceBean) {
		this.dataServiceBean = serviceBean;
	}


	public void setErrorMsg( String msg ) {
		this.message = new TextMessage( msg, true); 
	}

	public void setTextMessage( String msg ) {
		this.message = new TextMessage( msg); 
	}

	public TextMessage getMessage() {
		return message;
	}

	public void setMessage(TextMessage message) {
		this.message = message;
	}


	public Title getTitle() {
		return this.title;
	}

	public void setTitle(Title title) {
		
		this.title = title;
	}

	public void setTitleText(String sTitle) {
		
		setTitle( new Title(sTitle) );
	}


	/**
	 * @param index ���������.
	 * @return ��� ����� � ��� ������� ��� ���������� ���������.
	 */
	public String getTitleKey( EDelegatePgTab index )
	{
		return titleKeys.get( index);
	}

	/**
	 * @param index ���������� ��� �� ���������.
	 * @param resourceKey �������� ����� � �������������� �������.
	 */
	public void setTitleKey( EDelegatePgTab index, String resourceKey )
	{
		titleKeys.put( index, resourceKey);
	}


	/**
	 * @return ������� ����� �������� �������� (0..2)
	 */
	public EDelegatePgTab getPgTab() {
		return this.pgTab;
	}


	/**
	 * ������ ����� �������� �������� (0..2).
	 * @param pageTab
	 */
	public void setPgTab(EDelegatePgTab pageTab) {
		// this.pgTab = (pageTab >= 2) ? 2 : ((pageTab <= 0) ? 0 : pageTab);
		if (this.pgTab == pageTab) return;
		this.pgTab = pageTab;
		// this.needReload = true;
	}


	/**
	 * ������� �� ��������� ��������
	 * @param sNewPgTab: ����� ��������
	 */
	public void setPgTabByTag(String sNewPgTab) 
	{
		if (sNewPgTab == null || sNewPgTab.length() < 1)
			// �������� ��� ��������� �������
			return;

		// EDeletagePgTab.delegating; 
		final EDelegatePgTab newPgTab = EDelegatePgTab.valueOf(sNewPgTab);
		this.setPgTab(newPgTab);
	}


	public boolean isShowBtnRefresh() {
		return this.showBtnRefresh;
	}

	public void setShowBtnRefresh(boolean showBtnRefresh) {
		this.showBtnRefresh = showBtnRefresh;
	}

	/**
	 * @return true=���������� toolbar, false = ���.
	 */
	public boolean isShowToolbar() {
		return this.showToolbar;
	}

	public void setShowToolbar(boolean showToolbar) {
		this.showToolbar = showToolbar;
	}


	/**
	 * @return ������� ������ ������ ����������� ������� ������
	 */
	public int getDataOffset() {
		return dataOffset;
	}

	public void setDataOffset(int dataOffset) {
		this.dataOffset = dataOffset;
	}

	/**
	 * @return ������� ���������� ������ ��������� ������, ��. dataOffset.
	 */
	public List<List<String>> getDataList() {
		return dataList;
	}

	public void setDataList(List<List<String>> dataList) {
		this.dataList = dataList;
	}


	public String getBackURL() {
		return backURL;
	}


	public void setBackURL(String backURL) {
		this.backURL = backURL;
	}


	/**
	 * @return ������� �������� ������� ������, ��. dataOffset.
	 */
	public List<SearchResult.Column> getColumns() {
		return columns;
	}

	public void setColumns(List<SearchResult.Column> columns) {
		this.columns = columns;
	}


	public String getActivePgTitleKey() {
		return this.getTitleKey( getPgTab());
	}


	public String getUserIdStr() {
		return this.userIdStr;
	}


	public void setUserIdStr(String userid) {
		if (JspUtils.equals( this.userIdStr, userid)) return;
		this.userIdStr = userid;
		this.needReload = true;
	}


	/**
	 * �������� ������� ������ ���������.
	 * @return ������ ���������, ������� ����� ��������� ������������� ������ (� 
	 * ��� getId() ����� null).
	 */
	public List<Delegation> getAllDelegates() {
		return this.allDelegates;
	}

	public void setAllDelegates(Collection<Delegation> list) {
		allDelegates.clear();
		editDelegates.clear(); // �.�. ������������� ������ �������� ������������� ������
		if (list != null)
			allDelegates.addAll(list);
	}

	/**
	 * �������� ������� ������ ���������.
	 * @return ������ ���������, ������� ����� ��������� ������������� ������ (� 
	 * ��� getId() ����� null).
	 */
	public List<Delegation> getEditDelegates() {
		return this.editDelegates;
	}

	public void setEditDelegates(Collection<Delegation> list) {
		if (list == this.editDelegates) return;
		editDelegates.clear(); 
		if (list != null)
			editDelegates.addAll(list);
	}


	/**
	 * �������� ������� ����� �������� �� ��� id.
	 * @param id
	 * @return
	 */
	public Delegation getEditDelegateById(ObjectId id) {
		return getEditDelegateById( (id == null) ? null : id.getId().toString() );
	}


	/**
	 * �������� ������� �� �������� ������ �� ��� �������.
	 * @param orderIndex ������ � ������.
	 * @return ������� ������� ��� null, ���� ��� ������ �������.
	 */
	public Delegation getEditDelegateByIdx(int orderIndex) {
		if (editDelegates == null || orderIndex < 0 || orderIndex >= editDelegates.size())
			return null;
		return editDelegates.get(orderIndex);
	}


	/**
	 * �������� ������� �� �������� ������ �� ��� id.
	 * @param id ��������.
	 * @return ������� ������� ��� null, ���� ��� ������ id.
	 */
	public Delegation getEditDelegateById(String id) {
		if ( (id != null) &&
			 (editDelegates != null) && !editDelegates.isEmpty()) {
			for (Delegation item: editDelegates) {
				if ( item != null 
						&& item.getId() != null 
						&& id.equals( item.getId().getId().toString()) 
					) // FOUND
					return item;
			}
		}
		return null; // NOT FOUND
	}
	
	public boolean isDelegationEditable(int idx) {
		
		Delegation delegation = getEditDelegateByIdx(idx);
		
		if(delegation == null || delegation.getId() == null)
			return false;
		
		if(getDataServiceBean() == null)
			return false;
		
		if(delegation.getEditable() == Delegation.Editable.NOT_SET)
			delegation.setEditable(
					DelegateHelper.isDelegationEditable(getDataServiceBean(), (Long)delegation.getId().getId()) ?
							Delegation.Editable.YES : Delegation.Editable.NO);
				
		return delegation.getEditable() == Delegation.Editable.YES ? true : false;
	}


	/**
	 * �������� ������� ��������� �������������.
	 * @return
	 */
	public List<PersonView> getPersonsDic() {
		return this.personsDic;
	}


	public void setPersonsDic(List<PersonView> list) {
		this.personsDic = list;
	}

	/* *
	 * �������� ������� ��������� ������������ �����.
	 * @return
	 *//*
	public Set<PermissionSet> getPermissionsDic() {
		return permissionsDic;
	}*/


	/* *
	 * ������ �����-������� ������������ �����.
	 * @param permissionsDic
	 */
/*	public void setPermissionsDic(Set<PermissionSet> permissions) {
		this.permissionsDic = permissions;
	}*/


	/**
	 * @return true, ���� ����� �������� � ������� ��������� ������������/ 
	 * false = ���� �� ������ = ��������.
	 */
	public boolean isSourceUserSelectable() {
		return this.sourceUserSelectable;
	}

	/**
	 * @param isSelectable: true, ����� ����� ���� �������� ��������� 
	 * ������������ � ������� �����������, false = �������� ������ ����� 
	 * ������� ������������; 
	 */
	public void setSourceUserSelectable(boolean isSelectable) {
		this.sourceUserSelectable = isSelectable;
	}


	/**
	 * @return true, ���� �������� ����� ������ ����������� �������� ��������.
	 */
	public boolean isSelectableFilterByActiveMode() {
		return selectableFilterByActiveMode;
	}


	/**
	 * ���������(true)/���������(false) ����� ������ ����������� �������� 
	 * ��������, default = false.
	 */
	public void setSelectableFilterByActiveMode(boolean value) {
		this.selectableFilterByActiveMode = value;
	}


	/**
	 * @return ������� ����������� �������� � ���������� �������������.
	 */
	public EFilterByActiveMode getFilterByActiveMode() {
		return filterByActiveMode;
	}

	/* *
	 * ������ ������� ����������� �������� � ���������� �������������.
	 * @param value 
	 */
	public void setFilterByActiveMode(EFilterByActiveMode value) {
		this.filterByActiveMode = value;
	}


	public String getFilterByActiveModeStr() {
		return this.filterByActiveMode.toString();
	}

	/* *
	 * ������ ������� ����������� ���������� �������.
	 * @param value: ����������� ������ ����������, Null � ������ ������ - 
	 * ��������� ������� ��������, ��������� �������� - ������� _Both.
	 */
	public void setFilterByActiveModeStr(String value) {
		if (value == null) return;
		EFilterByActiveMode mode = EFilterByActiveMode._Both;
		if (value != null) {
			try {
				value = value.trim();
				if (value.length() == 0) return;
				mode = EFilterByActiveMode.valueOf(value);
			} catch (IllegalArgumentException ex) {
				DelegateListPortlet.logger.warn( MessageFormat.format( 
						"Invalid active mode filter mnemonic ''{0}'' -> ignored to ''{1}''", 
						value, mode));
			}
		}
		this.setFilterByActiveMode(mode);
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
		if (editAccessRoles != null&&!editAccessRoles.isEmpty()&&dataServiceBean!=null){
			CheckRolesForUser checkAction = new CheckRolesForUser();
			checkAction.setPersonLogin(dataServiceBean.getUserName());
			checkAction.setRoles(editAccessRoles);
			return (Boolean)dataServiceBean.doAction(checkAction);
		}
		// ���� ��������������� ���� �� ������, �� ����� �� �������������� � �������� ������������ ����
		return true;
	}

	/* *
	 * @return true=���������� ����� ����.
	 */
/*	public boolean isSelectableFilterByRoleId() {
		return selectableFilterByRoleId;
	}*/


	/* *
	 * ���������(true)/���������(false) ����� ������������ �����, default = true.
	 */
/*	public void setSelectableFilterByRoleId(boolean value) {
		this.selectableFilterByRoleId = value;
	}*/


	/* *
	 * ������ �� ���� �������������, ������������� ��� EnWebSelectRole==true.
	 * @return ID ���� ������������� ��� ����������, null=��� �������, �.�. 
	 * "����� ����".
	 */
/*	public ObjectId getFilterByRoleId() {
		return filterByRoleId;
	}*/


	/* *
	 * ������ ������ �� ����� �������������, default = null.
	 * @param value: ���� �������������, null=����� ����.
	 */
/*	public void setFilterByRoleId(ObjectId value) {
		this.filterByRoleId = value;
	}*/


/*	public String getFilterByRoleIdStr() {
		return (filterByRoleId == null || filterByRoleId.getId() == null) 
						? ""
						: String.valueOf( ((Long) filterByRoleId.getId()).longValue() );
	}*/

/*	public void setFilterByRoleIdStr(String value) {
		ObjectId roleId = null;
		if (value != null && value.trim().length() > 0) {
			try {
				final Long numId = new Long(value.trim());
				roleId = new ObjectId( PermissionSet.class, numId);
			} catch(NumberFormatException ex) {
				roleId = null;
			}
		}
		setFilterByRoleId( roleId);
	}*/


}
