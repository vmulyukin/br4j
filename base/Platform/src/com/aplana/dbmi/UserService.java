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
package com.aplana.dbmi;

import java.util.Iterator;
import java.util.Map;

/**
 * Exposes methods related to portal users
 * (!) Users are synchronized from portal into DB not back.
 * (!) List of static JBR-roles are synchronized from DB into portal not back.
 * (!) List of static JBR-roles for each db-user are synchronized from DB into portal not back.
 */
public interface UserService {

	/**
	 * 
	 */
	public static final String ADMINISTRATOR = "A";
	public static final String PORTAL_USER = "User";
	public static final String PORTAL_ADMINISTRATOR = "Admin";

	/**
	 * �������, ����������� � ������ ���� ��� �������� �������� ���������� ����
	 * ��� ������/������������� ����� �� jbr � portal.
	 * ���� ��� ������ �������� ����� ��������� jbr-������ (� � ���, ��������, 
	 * ����� �������� ������ ������� ��� ���������� � �������� ����� �������).
	 */
	public static final String PREFIX_PORTAL_ROLENAME = "$$";

	/**
	 * Returns an <code>Iterator</code> over the elements of a
	 * <code>Collection</code> containing users information
	 * 
	 * @return the iterator
	 */
	public Iterator getAllUsers() throws PortalException;
	
	/**
	 * Returns the {@link PortalUser} corresponding to 
	 * <code>id</code> which is the unique login identifier
	 * 
	 * @param id  the login identifier
	 * @return the PortalUser related to <code>id</code>
	 */
	public PortalUser getByLogin(String id) throws PortalException;
	
	/**
	 * Creates {@link PortalUser}
	 * 
	 * @param pUser PortalUser
	 */
	public void createUser(PortalUser pUser) throws PortalException;

	/**
	 * Removes {@link PortalUser}
	 * 
	 * @param pUser PortalUser
	 */
	public void removeUser(PortalUser pUser) throws PortalException;
	
	public void enableUser(PortalUser pUser, boolean isEnabled) throws PortalException;
	
	public void updateUser(PortalUser pUser) throws PortalException;
	
	public void changePassword(final String login, final String password) throws PortalException;
	public void validatePassword(final String login, final String password) throws PortalException;
	
	public void addAdminRole(String login) throws PortalException;
	public void deleteAdminRole(String login) throws PortalException;

	/**
	 * Associates a given user  to a specific role
	 * @author Mnagni
	 *
	 *  @param login the user login
	 *  @param role the role to assign, also see {@link PREFIX_PORTAL_ROLENAME}
	 **/
	public void grantRole(String login, String jbrRole) throws PortalException;

	/**
	 * Revokes to given user a specific role
	 * @author Mnagni
	 *
	 *  @param login the user login
	 *  @param role the role to assign
	 **/	
	public void revokeRole(String login, String jbrRole) throws PortalException;


	/**
	 * �������� ��������� ��� ���������� ���� ����-��� ����������� jbr-����.
	 * @param jbrRole,  also see {@link PREFIX_PORTAL_ROLENAME}
	 * @return
	 */
	public String getPortalRoleByJbrRole(String jbrRole);


	/**
	 * ���������� ������������� ���������� ����� �������� ����������� ������ 
	 * ����������� jbr-�����. ������������ ���� �� �����������.
	 * @param jbrRoleCodes ������ �����,  also see {@link PREFIX_PORTAL_ROLENAME}.
	 * @return ���-�� ������� ����������� �����.
	 * @throws PortalException 
	 */
	public int synchJbrRoles(String[] jbrRoleCodes) throws PortalException;


	/**
	 * ������������� ������������� ���������� ����� ������������� �������� �� 
	 * ����������� jbr-�����.
	 * @param personRoles: ����� String(userLogin) -> Collection<String(jbrRoleCode)>
	 * @return ���-�� ������� ����������� ������������ ������� �����,
	 *  also see {@link PREFIX_PORTAL_ROLENAME}.
	 * @throws PortalException 
	 */
	public int synchJbrPersonRoles(Map personRoles, boolean skipAdmins) throws PortalException;

}
