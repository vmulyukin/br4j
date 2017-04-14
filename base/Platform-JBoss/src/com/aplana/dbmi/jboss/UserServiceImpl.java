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
package com.aplana.dbmi.jboss;

import com.aplana.dbmi.PortalException;
import com.aplana.dbmi.PortalUser;
import com.aplana.dbmi.UserService;

import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class UserServiceImpl implements UserService {

	protected final Log logger = LogFactory.getLog(getClass());

	/**
	 * Returns an <code>Iterator</code> over the elements of a
	 * <code>Collection</code> containing users information
	 * 
	 * @return the iterator
	 */
	public Iterator getAllUsers() throws PortalException {
		//try {
			final IdentityUtils iu = new IdentityUtils();			
			return iu.findAllUsers().iterator();
		/*} catch (DataException e) {
			throw new DataException("sync.user.list", 
						new Object[] { e.getMessage() });
		}*/
	}

	public PortalUser getByLogin(String id) throws PortalException {
		//try {
			final IdentityUtils iu = new IdentityUtils();
			return iu.getByLogin(id);
		/*} catch (Exception e) {
			e.printStackTrace();
			throw new DataException("sync.user.fetch", new Object[] {
					e.getMessage(), id });
		}*/
	}
	
	public void createUser(PortalUser pUser) throws PortalException {
		//try {
			final IdentityUtils iu = new IdentityUtils();
			iu.createUser(pUser);
	}

	public void removeUser(PortalUser pUser) throws PortalException {
		//try {
			final IdentityUtils iu = new IdentityUtils();
			iu.removeUser(pUser);
	}
	
	public void enableUser(PortalUser pUser, boolean isEnabled) throws PortalException {
		//try {
			final IdentityUtils iu = new IdentityUtils();
			iu.enableUser(pUser, isEnabled);
	}
	
	public void updateUser(PortalUser pUser) throws PortalException {
		//try {
			final IdentityUtils iu = new IdentityUtils();
			iu.updateUser(pUser);
	}
	
	public void changePassword(final String login, final String password) throws PortalException {
		final IdentityUtils iu = new IdentityUtils();
		iu.changePassword(login, password);
	}

	public void validatePassword(final String login, final String password) throws PortalException {
		final IdentityUtils iu = new IdentityUtils();
		iu.validatePassword(login, password);
	}

	/* (non-Javadoc)
	 * @see com.aplana.dbmi.UserService#grantRole(java.lang.String, java.lang.String)
	 */
	public void grantRole(String login, String role) throws PortalException {
		final IdentityUtils iu = new IdentityUtils();
		iu.grantRole(login, role);
	}

	/* (non-Javadoc)
	 * @see com.aplana.dbmi.UserService#revokeRole(java.lang.String, java.lang.String)
	 */
	public void revokeRole(String login, String role) throws PortalException {
		final IdentityUtils iu = new IdentityUtils();
		iu.revokeRole(login, role);
	}

	/* (non-Javadoc)
	 * @see com.aplana.dbmi.UserService#addAdminRole(java.lang.String)
	 */
	public void addAdminRole(String login) throws PortalException {
		// logger.warn("WARNING! User " + login + " needs to be assigned administator role; not implemented yet!");
		grantRole(login, UserService.ADMINISTRATOR);
	}

	/* (non-Javadoc)
	 * @see com.aplana.dbmi.UserService#deleteAdminRole(java.lang.String)
	 */
	public void deleteAdminRole(String login) throws PortalException {
		// logger.warn("WARNING! User " + login + " needs to be revoked administator role; not implemented yet!");
		revokeRole(login, UserService.ADMINISTRATOR);
	}

	/* (non-Javadoc)
	 * @see com.aplana.dbmi.UserService#getPortalRoleByJbrRole(java.lang.String)
	 */
	public String getPortalRoleByJbrRole(String jbrRole) {
		return IdentityUtils.makePortalRoleIdByDBMIRole(jbrRole);
	}

	/* (non-Javadoc)
	 * @see com.aplana.dbmi.UserService#synchJbrPersonRoles(java.util.Map)
	 */
	public int synchJbrPersonRoles(Map personRoles, boolean skipAdmins) throws PortalException {
		final IdentityUtils iu = new IdentityUtils();
		return iu.synchJbrPersonRoles(personRoles, skipAdmins);
	}

	/* (non-Javadoc)
	 * @see com.aplana.dbmi.UserService#synchJbrRoles(java.lang.String[])
	 */
	public int synchJbrRoles(String[] jbrRoleCodes) throws PortalException {
		final IdentityUtils iu = new IdentityUtils();
		return iu.synchJbrRoles(jbrRoleCodes);
	}
}
