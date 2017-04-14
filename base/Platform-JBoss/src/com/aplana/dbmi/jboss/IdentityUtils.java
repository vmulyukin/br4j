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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.TransactionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.portal.common.transaction.Transactions;
import org.jboss.portal.identity.IdentityException;
import org.jboss.portal.identity.MembershipModule;
import org.jboss.portal.identity.NoSuchUserException;
import org.jboss.portal.identity.RoleModule;
import org.jboss.portal.identity.User;
import org.jboss.portal.identity.UserModule;
import org.jboss.portal.identity.UserProfileModule;
import com.aplana.dbmi.PortalException;
import com.aplana.dbmi.PortalUser;
import com.aplana.dbmi.UserService;

/**
 * Helps to manage the user/role objects into a JBoss portal
 * 
 * @author Mnagni
 **/
public class IdentityUtils {

	static final String MSG_CANT_CONNECT_TO_USER_SERVICE = "Can't connect to user service";

	final static String ROLE_PREFIX = "dbmi_";

	static final String P_LOGIN = "user.name.nickName";
	static final String P_NAME = "user.name.given";			
	static final String P_FAMILY = "user.name.family";
	static final String P_EMAIL = "user.business-info.online.email";
	
	final static String PWD_DEFAULT = "123456";

	protected final Log logger = LogFactory.getLog(IdentityUtils.class);

	/**
	 * Returns the number of Users returned by the <code>UserModule.findUsers</code>.
	 * 
	 * @return the number of users.
	 */
	public int getUserCount() throws PortalException {
		final IdentityTransaction it = new IdentityTransaction() {

			protected String getErrorText() { return "Can't fetch users' count"; }

			public Object process() throws Exception {
				final UserModule userModule = getUserModule();
				return new Integer(userModule.getUserCount());
			}
		};
		final Integer result = (Integer)(it.doTransction()); 
		return (result != null) ? result.intValue() : -1;	
	}

	/**
	 * Returns the number of Users returned by the <code>UserModule.findUsers</code>.
	 * 
	 * @return the number of users.
	 */
	public Collection findAllUsers() throws PortalException {
		final IdentityTransaction it = new IdentityTransaction() {

			protected String getErrorText() { return "Can't find users"; }

			public Object process() throws Exception {
				final UserModule userModule = getUserModule();
				final int numUsers = userModule.getUserCount();
				logger.debug("Portal UserCount: " + numUsers);
				return getPersonList(userModule.findUsers(0, numUsers));
			}
		};
		return (Collection) it.doTransction();
	}

	/**
	 * Returns the number of Users returned by the <code>UserModule.findUsers</code>.
	 * 
	 * @return the number of users.
	 */
	public Collection findUsers( final int offset, final int limit) throws PortalException {
		final IdentityTransaction it = new IdentityTransaction() {

			protected String getErrorText() { 
				return "Can't find user (offset="+ offset+ ", limit="+ limit + ")"; 
			}

			public Object process() throws Exception {
				final UserModule userModule = getUserModule();
				return userModule.findUsers(offset, limit);
			}
		};
		return (Collection) it.doTransction();
	}

	public Map userProperties(final User user) throws PortalException {
		final IdentityTransaction it = new IdentityTransaction() {

			protected String getErrorText() { 
				return "Can't fetch properties for user '"+ 
							( (user != null) ? user.getUserName() : "null"); 
			}

			public Object process() throws Exception {
				final UserProfileModule userProfile = getUserProfileModule();
				return /*(Map)*/ userProfile.getProperties(user);
			}
		};
		return (Map) it.doTransction();
	}


	/**
	 * Returns the {@link PortalUser} associated with the <code>userName</code>
	 * 
	 * @param userName the user name
	 * @return the associated PortalUser
	 * */
	public PortalUser getByLogin( final String userName) throws PortalException {
		final IdentityTransaction it = new IdentityTransaction() {

			protected String getErrorText() { 
				return "Can't get profile for user ''"+ userName;
			}

			public Object process() throws Exception {
				final UserModule userModule = getUserModule();
				final UserProfileModule userProperties = getUserProfileModule();
				PortalUser person = null;
				try {
					person = userToPerson(userProperties.getProperties(
							userModule.findUserByUserName(userName)));
					if (logger.isTraceEnabled()) {					
						logger.trace("[DEBUG] Retrieved user data: login="
							+ person.getLogin() + "; full name="
							+ person.getFullName() + "; e-mail="
							+ person.getEmail());
					}
				} catch (NoSuchUserException e) {
					logger.warn("User data not found: login="
								+ userName);
				}
				return person;
			}
		};
		return (PortalUser) it.doTransction();
	}
	
	/**
	 * Creates portal user {@link PortalUser}
	 * 
	 * @param PortalUser the user name
	 * */
	public void createUser( final PortalUser pUser) throws PortalException {
		final IdentityTransaction it = new IdentityTransaction() {

			protected String getErrorText() { 
				return "Can't create portal user ''" + pUser.getFullName();
			}

			public Object process() throws Exception {
				final UserModule userModule = getUserModule();
				User usr = userModule.createUser(pUser.getLogin(), PWD_DEFAULT);
				final UserProfileModule userProperties = getUserProfileModule();
				userProperties.setProperty(usr, P_EMAIL, pUser.getEmail());
				String str = pUser.getFullName();
				String[] subs = str.split(" ");
				String familyName = "null";
				String givenName = "null";
				if( subs.length > 0 ) {
					familyName = subs[0];
				}
				if( subs.length > 1 ) {
					givenName = subs[1];
				}
				userProperties.setProperty(usr, P_FAMILY, familyName);
				userProperties.setProperty(usr, P_NAME, givenName);
				userProperties.setProperty(usr, User.INFO_USER_ENABLED, true);
				if (logger.isTraceEnabled()) {					
					logger.trace("[DEBUG] Created portal user: login="
						+ pUser.getLogin() + "; full name="
						+ pUser.getFullName() + "; e-mail="
						+ pUser.getEmail());
				}
				return usr;
			}
		};
		it.doTransction();
	}
	
	/**
	 * Removes portal user {@link PortalUser}
	 * 
	 * @param PortalUser the user name
	 * */
	public void removeUser( final PortalUser pUser) throws PortalException {
		final IdentityTransaction it = new IdentityTransaction() {

			protected String getErrorText() { 
				return "Can't create portal user ''" + pUser.getFullName();
			}

			public Object process() throws Exception {
				final UserModule userModule = getUserModule();
				User usr = userModule.findUserByUserName(pUser.getLogin());
				userModule.removeUser(usr.getId());
				return usr;
			}
		};
		it.doTransction();
	}
	
	/**
	 * Enables/disables portal user {@link PortalUser}
	 * 
	 * @param PortalUser the user name
	 * */
	public void enableUser( final PortalUser pUser, final boolean isEnabled) throws PortalException {
		final IdentityTransaction it = new IdentityTransaction() {

			protected String getErrorText() { 
				return "Cannot enable/disable portal user ''" + pUser.getFullName();
			}

			public Object process() throws Exception {
				final UserModule userModule = getUserModule();
				User usr = userModule.findUserByUserName(pUser.getLogin());
				final UserProfileModule userProperties = getUserProfileModule();
				userProperties.setProperty(usr, User.INFO_USER_ENABLED, isEnabled);
				return usr;
			}
		};
		it.doTransction();
	}
	
	/**
	 * Updates portal user {@link PortalUser}
	 * 
	 * @param PortalUser the user name
	 * */
	public void updateUser( final PortalUser pUser) throws PortalException {
		final IdentityTransaction it = new IdentityTransaction() {

			protected String getErrorText() { 
				return "Cannot update portal user ''" + pUser.getFullName();
			}

			public Object process() throws Exception {
				final UserModule userModule = getUserModule();
				User usr = userModule.findUserByUserName(pUser.getLogin());
				final UserProfileModule userProperties = getUserProfileModule();
				userProperties.setProperty(usr, P_EMAIL, pUser.getEmail());
				String str = pUser.getFullName();
				String[] subs = str.split(" ");
				String familyName = "null";
				String givenName = "null";
				if( subs.length > 0 ) {
					familyName = subs[0];
				}
				if( subs.length > 1 ) {
					givenName = subs[1];
				}
				userProperties.setProperty(usr, P_FAMILY, familyName);
				userProperties.setProperty(usr, P_NAME, givenName);
				if (logger.isTraceEnabled()) {					
					logger.trace("[DEBUG] Updated portal user: login="
						+ pUser.getLogin() + "; full name="
						+ pUser.getFullName() + "; e-mail="
						+ pUser.getEmail());
				}
				return usr;
			}
		};
		it.doTransction();
	}
	
	/**
	 * Changes portal user {@link PortalUser} password
	 * 
	 * @param String login user login
	 * @param String password new password
	 * */
	public void changePassword(final String login, final String password) throws PortalException {
		final IdentityTransaction it = new IdentityTransaction() {

			protected String getErrorText() { 
				return "Cannot update portal user ''" + login;
			}

			public Object process() throws Exception {
				final UserModule userModule = getUserModule();
				User usr = userModule.findUserByUserName(login);
				final UserProfileModule userProperties = getUserProfileModule();
				usr.updatePassword(password);
				if (logger.isTraceEnabled()) {					
					logger.trace("[DEBUG] Changed password for portal user: login=");
				}
				return true;
			}
		};
		it.doTransction();
	}
	
	/**
	 * Validates portal user {@link PortalUser} password
	 * 
	 * @param String login user login
	 * @param String password current password
	 * */
	public void validatePassword(final String login, final String password) throws PortalException {

		final IdentityTransaction it = new IdentityTransaction() {

			protected String getErrorText() { 
				return "Invalid user password ''" + login;
			}

			public Object process() throws Exception {
				try {
					final UserModule userModule = getUserModule();
					User usr = userModule.findUserByUserName(login);
					final UserProfileModule userProperties = getUserProfileModule();
					if (!usr.validatePassword(password))
						throw new PortalException(login);
				}catch (Exception e){
					throw new PortalException(e);
				}
				return true;
			}
		};
		it.doTransction();
	}


	/**
	 * Associates a given user  to a specific role
	 * @author Mnagni
	 *
	 *  @param login the user login
	 *  @param role the role to assign
	 * @throws PortalException 
	 **/
	public void grantRole(String login, String role) throws PortalException {
		updateRole(login, role, false);
	}

	/**
	 * ���������� ������������� ���������� ����� �������� ����������� ������ 
	 * ����������� jbr-�����. ���� ���� ��� ����������,�� ��� �� �����������.
	 * @param jbrRoleCodes ������ ���� �����.
	 * @return ���-�� ������� ����������� �����, (-1) ���� jbrRoleCodes ������ ����.
	 * @throws PortalException 
	 */
	public int synchJbrRoles(String[] jbrRoleCodes) throws PortalException
	{
		if (jbrRoleCodes == null || jbrRoleCodes.length < 1) 
			return -1;

		// ��������� ���� ���������� ����� � ���� Set<String> ...
		final RoleModule roleModule = getRoleModule();

		final IdentityTransaction itRoles = new IdentityTransaction() {

			protected String getErrorText() {
				return "Problem getting portal roles ";
			}

			/**
			 * ��������� ������ �������� ������������ ���������� �����.
			 * ���-�� ������ �� null.
			 */
			protected Object process() throws Exception {
				// Set<org.jboss.portal.identity.Role>
				final Set jRoles = roleModule.findRoles();
				if (jRoles == null) 
					return new HashSet(0);

				// ��������� ������ ����-�������� ���������� ����� 
				final Set/*<String>*/ result = new HashSet(jRoles.size());
				for (Iterator iterator = jRoles.iterator(); iterator.hasNext();) {
					final org.jboss.portal.identity.Role jRole = 
						(org.jboss.portal.identity.Role) iterator.next();
					result.add( jRole.getName());
				}
				return result;
			}
		};
		final Set/*<String>*/ portalRoles = (Set) itRoles.doTransction();
		logger.info("found portal roles ("+ portalRoles.size() +"): \n\t" + portalRoles.toString() );

		// ���������� ������������� ����� � ������...
		int success = 0, errors = 0, skipped = 0;
		for (int i = 0; i < jbrRoleCodes.length; i++) {
			// �������������/����������� jbr-���� � �������� ���������� ���
			final String roleId = makePortalRoleIdByDBMIRole( jbrRoleCodes[i]); 
			final String roleLabel = jbrRoleCodes[i];

			if (portalRoles != null && portalRoles.contains(roleId)) {
				++skipped;
				continue;
			}

			// ���������� ���������� ����...
			try {
				final IdentityTransaction it = new IdentityTransaction() {

					protected String getErrorText() {
						return "Problem synchronizing portal role \'" + roleId + "\' with label \'" + roleLabel + "\'";
					}

					protected Object process() throws Exception {
						roleModule.createRole( roleId, roleLabel);
						if (logger.isDebugEnabled())
							logger.debug("Added role " + roleId + " with label " + roleLabel);
						return null;
					}
				};
				it.doTransction();
				++success;
			} catch (Exception ex) {
				++errors;
				// logger.error( ex.getMessage()); <- no need due to logging inside IdentityTransaction
			}
		} // for

		logger.info( String.format("processed over %d roles: \n"
					+ "\t\t SUCCESSFULLY ADDED: %d\n"
					+ "\t\t ALREADY EXISTS    : %d\n"
					+ "\t\t ERRORS            : %d\n"
				,
				new Object[] { 
					new Integer(jbrRoleCodes.length), new Integer(success),
					new Integer(skipped), new Integer(errors) 
			}));
		return success;
	}

	final static String ADMIN_PREFIX = "admin";

	/**
	 * ��������� ������������� ���������� ����� ������������ �������� �� ����������� jbr-�����.
	 * @param personRoles: ����� String(userLogin) -> Collection<String(jbrRoleCode)>
	 * � ��������� ������ jbr-�����, ��� ��������� ����������� ����� ����, 
	 * ����� �������� ������� {@link ROLE_PREFIX}),(!) ���� ������ ������ ����� 
	 * jbr-���� '$', �� ����������� �� ������ ��������������� ���������� ����.
	 * 
	 * @return ���-�� ������� ����������� ������������ �����, 
	 * (-1) ���� personRoles ������ ����.
	 */
	public int synchJbrPersonRoles( final Map personRoles, boolean skipAdmins) throws PortalException
	{
		if (personRoles == null || personRoles.isEmpty())
			return -1;

		final MembershipModule mmModule = getMembershipModule();
		final RoleModule roleModule = getRoleModule();
		final UserModule userModule = getUserModule();

		int success = 0, errors = 0, skipped = 0;
		for (Iterator iterator = personRoles.entrySet().iterator(); iterator.hasNext();) 
		{
			final Map.Entry item = (Map.Entry) iterator.next();
			final String login = (String) item.getKey();
			final Collection roles = (Collection) item.getValue();
			if (login == null) {
				++skipped;
				continue;
			}

			// ��� �������������� ������� ����� �������� ������ �������...
			if ( skipAdmins && login.toLowerCase().startsWith(ADMIN_PREFIX)) {
				++skipped;
				logger.warn("Portal administrator found under \'" + login
						+ "\' skipped, cause only manual updates can be performed for portal administators"
					);
				continue;
			}

			// ��������� ������ ���� ���������� �����...
			final List/*String[]*/ portalRoleNames = new ArrayList( (roles == null) ? 0 : roles.size() );
			if (roles != null) {
				// mmModule.getRoles(juser);  <- (!) use inside transaction only
				int i = 0;
				for (Iterator iterRole = roles.iterator(); iterRole.hasNext();i++) {
					String jbrRole = (String) iterRole.next();
					if(jbrRole.equals(UserService.ADMINISTRATOR)) {
						portalRoleNames.add(UserService.PORTAL_ADMINISTRATOR);
					}
					// �������������/����������� jbr-���� � �������� ���������� ���
					final String arole = makePortalRoleIdByDBMIRole( jbrRole );
					portalRoleNames.add( arole);
				}
			}

			//  ������ ���������� ����� ��� ������ ������������...
			try {
				final IdentityTransaction it = new IdentityTransaction() {

					protected String getErrorText() {
						return "Error synchronizing user roles between JBR & portal for login \'"+ login + "\'";
					}

					protected Object process() throws Exception {
						final User juser = userModule.findUserByUserName(login);

						// ��������� ������ ���������� ���� ������������� ��� ���������
						final Set/*<Role>*/ jUserOldRoles = mmModule.getRoles(juser);
						// ������� � ������ ������ ������ ���������� ���� ������������...
						if (jUserOldRoles != null) {
							final Object[] theRoles = jUserOldRoles.toArray();
							// for (Iterator iterRole = rolesUserOldRoles.iterator(); iterRole.hasNext();) {
							for (int i = 0; i < theRoles.length; i++) {
								// final org.jboss.portal.identity.Role role = (org.jboss.portal.identity.Role) iterRole.next();
								final org.jboss.portal.identity.Role role = (org.jboss.portal.identity.Role) theRoles[i];
								if (!isDBMINameOfPortalRole(role.getName())) {
									if (!role.getName().equals(UserService.PORTAL_ADMINISTRATOR) ||  
											role.getName().equals(UserService.PORTAL_ADMINISTRATOR)
											&& portalRoleNames.contains(UserService.PORTAL_ADMINISTRATOR)) {
										// ��� �� jbr-���� -> �������� �
										logger.trace( "keep portal role '"
											+ role.getName() +"'(\"" 
											+ role.getDisplayName()
											+ "\") for user '" + juser.getUserName()
											+ "'"
										);
										portalRoleNames.add(role.getName());
									}
								}
							}
						}

						// ��������� ����������� �����...
						final String[] roleNames = new String[portalRoleNames.size()];
						int i = 0;
						for (Iterator iterRoleName = portalRoleNames.iterator(); iterRoleName.hasNext();) {
							roleNames[i++] = (String) iterRoleName.next();
						}
						final Set /*<org.jboss.portal.identity.Role>*/ jUserNewRoles = 
							roleModule.findRolesByNames(roleNames);

						logger.debug("assigning for user '" + juser.getUserName()
								+ "' roles [" + jUserNewRoles + "]");

						mmModule.assignRoles( juser, jUserNewRoles); 

						return new Boolean(true);
					}
				};
				final Boolean result = (Boolean) it.doTransction();
				if (result != null && result.booleanValue()) {
					++success;
				} else 
					++skipped;
			} catch(Exception ex) {
				++errors;
				// logger.error( ex.getMessage()); <- no need due to logging inside IdentityTransaction
			}
		}

		logger.info( String.format("processed over %d roles: \n"
				+ "\t\t SUCCESSFULLY ADDED: %d\n"
				+ "\t\t ALREADY EXISTS    : %d\n"
				+ "\t\t ERRORS            : %d\n"
			,
			new Object[] { 
				new Integer(personRoles.size()), new Integer(success),
				new Integer(skipped), new Integer(errors) 
		}));
		return success;
	}

	/**
	 * Revokes to given user a specific role
	 * @author Mnagni
	 *
	 *  @param login the user login
	 *  @param role the role to assign
	 * @throws PortalException 
	 **/
	public void revokeRole(String login, String role) throws PortalException {
		updateRole(login, role, true);
	}

	/**
	 * Get portal role id for JBR-role tag. Generally only prefix
	 * {@link ROLE_PREFIX} is added.
	 * (!) If the jbrRole starts with '$' -> the rest of the role string 
	 * supposed to be the directly named portal role (no prefix will be added). 
	 * @return portal-native role jbp_name.
	 */
	static final String makePortalRoleIdByDBMIRole( final String jbrRole)
	{
		if (jbrRole == null || jbrRole.length() < 1) 
			return jbrRole;
		if (jbrRole.startsWith(com.aplana.dbmi.UserService.PREFIX_PORTAL_ROLENAME))
			// �������� ������������ �������� ���������� ��� ����...
			return jbrRole.substring(com.aplana.dbmi.UserService.PREFIX_PORTAL_ROLENAME.length());
		return ROLE_PREFIX + jbrRole;
	}

	/**
	 * ��������� �������� �� ���������� ���� ����� ��������� jbr-�����.
	 * (�.�. ���� �� � ������ ����� ������� "dbmi_")
	 * @param name: ����������� �������� ���������� ����.
	 * @return true, ���� �������� ����� jbr-�����.
	 */
	static final boolean isDBMINameOfPortalRole(String name)
	{
		return (name != null) && (name.toLowerCase().startsWith(ROLE_PREFIX.toLowerCase()));
	}

	/**
	 * Updates the user Roles set.
	 * 
	 * @param login the user id
	 * @param role the role to update
	 * @param remove <code>true</code> if the role has to be removed, 
	 * 	<code>false</code> if the role has to be added
	 * */
	private boolean updateRole(final String login, final String role, final boolean remove) 
		throws PortalException 
	{
		final IdentityTransaction it = new IdentityTransaction() {

			protected String getErrorText() { 
				return "Can't update role '"+ role+ 
						"' for user '"+ login + "', remove="+ remove; 
			}

			public Object process() throws Exception {
				final User juser = getUserModule().findUserByUserName(login);
				final MembershipModule mmModule = getMembershipModule();
				final Set immRoles = mmModule.getRoles(juser); //returns an immutable Set
				final Set jroles = new HashSet();
				java.util.Collections.addAll(jroles, immRoles.toArray());
				org.jboss.portal.identity.Role jrole = getRoleModule().findRoleByName(
						makePortalRoleIdByDBMIRole(role) ); // (!) get native name
				if (remove) {
					jroles.remove(jrole);
				} else {
					jroles.add(jrole);
				}
				mmModule.assignRoles(juser, jroles);
				if (logger.isDebugEnabled()) {
					logger.debug( ((remove) ? "Revoked" : "Added")+ " role " + jrole+ " to JBoss user " + juser);
				}
				return new Boolean(true);
			}
		};

		// initPortal();
		// final Collection coll = findAllUsers();
		final Boolean result = (Boolean) it.doTransction();
		return (result != null) && result.booleanValue();
	}

	/**
	 * Converts a Set of <code>org.jboss.portal.identity.User</code> to a
	 * <code>List</code> of {@link PortalUser} using the getProperties(User)
	 * UserProfileModule interface. In the returned <code>Map</code> the
	 * User MUST contain the following properties:
	 * <ul>
	 * <li>uid</li>
	 * <li>cn</li>
	 * <li>ibm-primaryEmail</li>
	 * </ul>
	 * 
	 * @param users
	 *            the <code>Set</code> of users to convert
	 * @return the converted <code>List</code> of {@link Person}
	 */
	Collection getPersonList(Set users) throws PortalException {
		final ArrayList personList = new ArrayList(); 
		logger.debug(" Set<User>.size(): " + users.size());
		final Iterator iterator = users.iterator();
		final UserProfileModule usrModule = getUserProfileModule();
		while (iterator.hasNext()) {
			final User user = (User) iterator.next();
			logger.trace(" getProperties of user: " + user);
			PortalUser person = null;
			try {
				final Map userProperties = usrModule.getProperties(user);
				person = userToPerson(userProperties);
			} catch (IdentityException e) {
				e.printStackTrace();
				logger.debug( "Error converting portal user " + user+ " to jbr user. The user will be skipped.");
				continue;
			}
			personList.add(person);
		}
		return personList;
	}

	/**
	 * Maps the values from <code>userProperties</code> map to 
	 * a {@link PortalUser} instance.
	 * @param	userProperties the properties <code>Map</code>
	 * @return  the equivalent {@link PortalUser}; 
	 * */
	PortalUser userToPerson(Map userProperties) 
	{
		/* These parameters are to be extracted in future? */
		final PortalUser person = new PortalUser();
		person.setLogin((String) userProperties.get(P_LOGIN));
		person.setFullName((String) userProperties.get(P_FAMILY) + " " + (String) userProperties.get(P_NAME));
		person.setEmail((String) userProperties.get(P_EMAIL));
		if (logger.isTraceEnabled()) {
			logger.trace("[DEBUG] Retrieved user data: login="
					+ person.getLogin() + "; full name="
					+ person.getFullName() + "; e-mail="
					+ person.getEmail());
		}
		return person;
	}

	/**
	 * Returns an instance of the
	 * <code>org.jboss.portal.identity.UserModule</code>
	 * 
	 * @return the UserModule
	 */
//	private SessionFactory getIdentitySessionFactory()
//		throws PortalException {
//		try {
//			return ((SessionFactory) new InitialContext().lookup("java:portal/IdentitySessionFactory"));
//		} catch (NamingException e) {
//			//throw new DataException("sync.user.list", new Object[] { e.getMessage() });
//			final String info = MSG_CANT_CONNECT_TO_USER_SERVICE + ":: get session factory fail";
//			logger.error( info, e);
//			throw new PortalException( info, e);
//		}
//	}

	/**
	 * Returns an instance of the
	 * <code>org.jboss.portal.identity.UserModule</code>
	 * 
	 * @return the UserModule
	 */
	UserModule getUserModule() throws PortalException {
		try {
			return ((UserModule) new InitialContext().lookup("java:portal/UserModule"));
		} catch (NamingException e) {
			//throw new DataException("sync.user.list", new Object[] { e.getMessage() });
			final String info = MSG_CANT_CONNECT_TO_USER_SERVICE + ":: get usermodule fail";
			logger.error( info, e);
			throw new PortalException( info, e);
		}
	}

	/**
	 * Returns an instance of the <code>org.jboss.portal.identity.UserProfileModule</code>
	 * 
	 * @return the UserProfileModule
	 */
	UserProfileModule getUserProfileModule() throws PortalException {
		try {
			return (UserProfileModule) (new InitialContext().lookup("java:portal/UserProfileModule"));
		} catch (NamingException e) {
			//throw new DataException("sync.user.list", new Object[] { e.getMessage() });
			final String info = MSG_CANT_CONNECT_TO_USER_SERVICE + ":: get userProfileModule fail";
			logger.error( info, e);
			throw new PortalException( info, e);
		}
	}

	/**
	 * Returns an instance of the <code>org.jboss.portal.identity.MembershipModule</code>
	 * 
	 * @return the MembershipModule
	 */
	MembershipModule getMembershipModule() throws PortalException {
		try {
			return (MembershipModule) (new InitialContext().lookup("java:portal/MembershipModule"));
		} catch (NamingException e) {
			// e.printStackTrace(); throw new DataException("sync.user.list", new Object[] { e.getMessage() });
			final String info = MSG_CANT_CONNECT_TO_USER_SERVICE + ":: get userMemberShipModule fail";
			logger.error( info, e);
			throw new PortalException( info, e);
		}
	}

	/**
	 * Returns an instance of the <code>org.jboss.portal.identity.RoleModule</code>
	 * 
	 * @return the RoleModule
	 */
	RoleModule getRoleModule() throws PortalException {
		try {
			return (RoleModule) (new InitialContext().lookup("java:portal/RoleModule"));
		} catch (NamingException e) {
			// e.printStackTrace(); throw new DataException("sync.user.list", new Object[] { e.getMessage() });
			final String info = MSG_CANT_CONNECT_TO_USER_SERVICE + ":: get roleModule fail";
			logger.error( info, e);
			throw new PortalException( info, e);
		}
	}

	/**
	 * Contains all the necesary machinery to envelop the user/role management 
	 * into a transaction. The {@link #doTransction()} method wraps the 
	 * {@link #process()} method using a transaction from the 
	 * <code>java:/TransactionManager</code> service
	 * 
	 * @author Mnagni
	 **/
	private abstract class IdentityTransaction {


		public IdentityTransaction() {
		}

		/**
		 * Executes the transaction.
		 * (!) All exceptions logged under text getErrorText() and exception
		 * 	reraised as PortalException wrapper.
		 **/
		public final Object doTransction() throws PortalException {
			try {
				final TransactionManager tm 
					= (TransactionManager) new InitialContext().lookup("java:/TransactionManager");
				return Transactions.required( tm,
						new Transactions.Runnable() {
							public Object run() throws Exception {
								return process();
							}
						}
				);
			} catch (Exception ex) {
				final String info = getErrorText();
				if (logger.isDebugEnabled())
					logger.warn( info, ex);
				else 
					logger.warn(info + "\n\t" + ex.getMessage());
				throw new PortalException( info, ex.getMessage());
			}
		}

		/**
		 * Must return information text about current operation.
		 * @return
		 */
		protected abstract String getErrorText();

		/**
		 * Contains the transaction core code. 
		 * @return the result of the process
		 **/
		protected abstract Object process() throws Exception;
	}

}
