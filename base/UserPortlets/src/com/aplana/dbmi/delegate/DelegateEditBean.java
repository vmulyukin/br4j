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

import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.util.Calendar;

import com.aplana.dbmi.model.Delegation;
import com.aplana.dbmi.model.PersonView;
//import com.aplana.dbmi.model.PermissionDelegate;
//import com.aplana.dbmi.model.PermissionSet;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.util.JspUtils;

/**
 * ������ ��� ������ ����� �������������� ������ ����������� ��������.
 * @author RAbdullin
 */
public class DelegateEditBean {

	//private String roleId = null;
	private String user_from;
	private String user_to;
	private List<PersonView> userList;
	private String currentUserId;

	private String to_date;
	private String from_date;
	private boolean createNew = true;
	
	private boolean refreshUserFrom;
	
	private boolean editAccessExists=false;


	//private boolean exclusive; 
	//private boolean active;

//	private final List<PermissionSet> permissionSets = new ArrayList<PermissionSet>();

	// (!) ��������� ����� ����� ������ ������ ������ (��������, � ������
	// �������� ���� � ������������ ����������),
	// (-1) = ����� ��������
	private int editDelegateIdx = -1;


	/**
	 * @return idx �������������� ��������, ��������, ���������� ������ ������ 
	 * ������ ������. ������������� ������� ����� ������ � ���-�� �������.
	 */
	public int getEditDelegateIdx() {
		return editDelegateIdx;
	}

	public void setEditDelegateIdx(int value) {
		this.editDelegateIdx = value;
	}

	
/*	public List<PermissionSet> getPermissionSets() {
		return permissionSets;
	}

	public void setPermissionSets(List<PermissionSet> list) {
		this.permissionSets.clear();
		if (list != null)
			this.permissionSets.addAll(list);
	}
*/

	/*public String getRoleId() {
		return roleId;
	}
	public void setRoleId(String role) {
		this.roleId = role;
	}*/


	public String getUser_from() {
		return user_from;
	}
	public void setUser_from(String user_from) {
		this.user_from = user_from;
	}


	public String getUser_to() {
		return user_to;
	}
	public void setUser_to(String user_to) {
		this.user_to = user_to;
	}


	public String getTo_date() {
		return to_date;
	}
	public void setTo_date(String to_date) {
		if(to_date != null && !to_date.equals(""))
			to_date += " 23:59:59";
		this.to_date = to_date;
	}


	public String getFrom_date() {
		return from_date;
	}
	public void setFrom_date(String from_date) {
		if(from_date != null && !from_date.equals(""))
			from_date += " 00:00:00";
		this.from_date = from_date;
	}


	/*public boolean isExclusive() {
		return exclusive;
	}
	public void setExclusive(boolean exclusive) {
		this.exclusive = exclusive;
	}


	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}*/


	public void clear() {
		editDelegateIdx = -1;

		//roleId = null;
		user_from = ""; // "" = myself
		user_to = null;

		to_date = null;
		from_date = null;

		//exclusive= false; 
		//active = true;

		// roles.upload();
	}


	/**
	 * ����� �������� �� source
	 * @param source
	 * @param idx: ��������� ������,��������� � source/ ��������, ���������� 
	 * ������ ������ ������ ������.
	 */
	public void setDelegateData(Delegation source, int idx) 
	{
		editDelegateIdx = idx;
		if (source == null) 
		{
			clear();
			return;
		}
		// delegateId = source.getId();

		//active = source.isActive();
		//exclusive = !source.isFromPersonHasAccessToo(); 

		//roleId = JspUtils.convertId2Str( source.getPermissonSetId());
		user_from = JspUtils.convertId2Str(source.getFromPersonId());
		user_to = JspUtils.convertId2Str(source.getToPersonId());

		from_date = JspUtils.Date2Str(source.getStartAt());
		to_date = JspUtils.Date2Str(source.getEndAt()); 
	}

	/**
	 * ������ �������� � dest
	 * @param dest
	 */
	public void getDelegateData(Delegation dest)
		throws DataException
	{
		if (dest == null) return; 
		// delegateId => ?

		//dest.setActive( this.isActive());
		//dest.setFromPersonHasAccessToo( !this.isExclusive()); 

		//long id = JspUtils.convertStr2IdLong(this.getRoleId());
		//dest.setPermissonSetId( id);

		Long id = JspUtils.convertStr2IdLong( this.getUser_from());
		dest.setFromPersonId( id);

		id = JspUtils.convertStr2IdLong( this.getUser_to());
		dest.setToPersonId( id);
		
	    dest.setPeriod( JspUtils.Str2Date(this.from_date),  JspUtils.Str2Date(this.to_date));
	}

	public List<PersonView> getUserList() {
		return userList;
	}

	public void setUserList(List<PersonView> userList) {
		this.userList = userList;
	}

    /**
     * @param currentUserId the currentUserId to set
     */
    public void setCurrentUserId(String currentUserId) {
        this.currentUserId = currentUserId;
    }

    /**
     * @return the currentUserId
     */
    public String getCurrentUserId() {
        return currentUserId;
    }

	public boolean isCreateNew() {
		return createNew;
	}

	public void setCreateNew(boolean createNew) {
		this.createNew = createNew;
	}

	public boolean isRefreshUserFrom() {
		return refreshUserFrom;
	}

	public void setRefreshUserFrom(boolean refreshUserFrom) {
		this.refreshUserFrom = refreshUserFrom;
	}

	public boolean isEditAccessExists() {
		return editAccessExists;
	}

	public void setEditAccessExists(boolean editAccessExists) {
		this.editAccessExists = editAccessExists;
	}
}
