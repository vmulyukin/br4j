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
package com.aplana.dbmi.module.usersync;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.CreateException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.access.ContextSingletonBeanFactoryLocator;
import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.PortalFactory;
import com.aplana.dbmi.PortalUser;
import com.aplana.dbmi.UserService;
import com.aplana.dbmi.model.SystemRole;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.DataServiceBean;
import com.aplana.dbmi.service.impl.Database;
import com.aplana.dbmi.service.impl.QueryBase;
import com.aplana.dbmi.service.impl.QueryFactory;
import com.aplana.dbmi.service.impl.UserData;


/**
 * Bean implementation class for Enterprise Bean: UserRegistrySync
 * (!) Can be used as TASK in JBossReferent.
 */
public class UserRegistrySync
	extends
		org.springframework.ejb.support.AbstractStatelessSessionBean
	implements
		javax.ejb.SessionBean
{
	static final long serialVersionUID = 3L;

	public final Log logger = LogFactory.getLog(getClass());

	//public static final long CLEAR_DELAY = 5L * 24 * 60 * 60 * 1000;

	private final ConfigUserRegistrySync config = new ConfigUserRegistrySync();

	public UserRegistrySync() {
		setBeanFactoryLocator(ContextSingletonBeanFactoryLocator.getInstance());
		setBeanFactoryLocatorKey("businessBeanFactory");
	}

	protected void onEjbCreate() throws CreateException {
		config.loadConfig();
	}

	public String getConfig() {
		return this.config.getConfigName();
	}

	public void setConfig(String path) {
		this.config.setConfigName(path);
	}


	public void process(Map parameters)
	{
		logger.debug("UserRegistrySync: Task started");
		if (parameters != null)
			config.putAll(parameters);

		final long start_ms = System.currentTimeMillis();
		try {
			final Database db = (Database) getBeanFactory().getBean(DataServiceBean.BEAN_DATABASE);
			final PortalFactory portal = Portal.getFactory();

			if ( config.getBoolValue( ConfigUserRegistrySync.SYNC_USERS_EN, false) ) {
				synchUsersFromPortal(db, portal);
			} else logger.warn("syncUsers is off");

			if ( config.getBoolValue( ConfigUserRegistrySync.SYNC_ROLES_EN, true) ) {
				synchRolesIntoPortal(db, portal);
			} else logger.warn("syncRoles is off");

			if ( config.getBoolValue( ConfigUserRegistrySync.SYNC_USERROLES_EN, true) ) {
				synchUserRolesIntoPortal(db, portal);
			} else logger.warn("syncUserRoles is off");

		} finally {
			final long end_ms = System.currentTimeMillis();
			logger.info( String.format(
					"UserRegistrySync: Task completed in %1.3f sec",
					new Object[] { new Double((end_ms - start_ms)/1000.0) }));
		}
	}

	/**
	 * ��������� ������������ ������������� ������� � ��.
	 * @param destDB: �� ����������.
	 * @return ���-�� ���������� �������������.
	 */
	private int synchUsersFromPortal(final Database destDB, final PortalFactory srcPortal)
	{
		logger.debug("SyncUser entered");

		final long start_ms = System.currentTimeMillis();
		final Date syncStart = new Date(start_ms - 1000);

		int userCount = 0;
		int userCountError = 0;
		try {
			PortalUser pUser = null;
			for( Iterator itr = srcPortal.getUserService().getAllUsers(); itr.hasNext();) {
				pUser = (PortalUser) itr.next();
				try {
					destDB.syncUser(pUser);
					userCount++;
				}
				catch (Exception e) {
					logger.error( "Synchronization (from portal) error for person: " + pUser.getLogin(), e);
					userCountError++;
				}
			}
			// syncStart.setTime(syncStart.getTime() - CLEAR_DELAY);
			destDB.clearUsers(syncStart);
		} catch (Exception e) {
			logger.error( "Users synchronization (from portal) error ", e);
		} finally {
			final long end_ms = System.currentTimeMillis();
			logger.info( String.format(" SyncUser from portal into DB finished in \n" +
					"\t\t time: %1.3f sec for %d users. \n\t\t Errors in sync for %d users", 
					new Object[] { 
						new Double((end_ms - start_ms)/1000.0),
						new Long(userCount),
						new Long(userCountError)
				}));
		}
		return userCount+userCountError;
	}

	/**
	 * ��������� ������������ jbr-����� �� �� � ������.
	 * @param srcDB: �������� �� ��� ��������� ������ ����������� jbr-�����.
	 */
	private void synchRolesIntoPortal(final Database srcDB, final PortalFactory destPortal)
	{
		logger.debug("SyncRoles entered");
		final long start_ms = System.currentTimeMillis();

		int rolesCount = 0;
		try {
			// ��������� ��������� �����...
			final QueryFactory qFactory = (QueryFactory) getBeanFactory().getBean(
					DataServiceBean.BEAN_QUERY_FACTORY, QueryFactory.class);
			final QueryBase query = qFactory.getListQuery(SystemRole.class);

			final Collection roles = (Collection) srcDB.executeQuery( getSystemUser(srcDB), query);
			if (roles != null && !roles.isEmpty()) { 
				rolesCount = roles.size();

				// ��������� ������ ����� �����...
				final String[] jbrRoleCodes = new String[rolesCount];
				int i = 0;
				for (Iterator iterator = roles.iterator(); iterator.hasNext(); i++) {
					final SystemRole jbrRole = (SystemRole) iterator.next();
					jbrRoleCodes[i] = (String) jbrRole.getId().getId();
				}

				destPortal.getUserService().synchJbrRoles(jbrRoleCodes);
			} else {
				logger.warn("(!?) no system jbr-roles found");
			}

		} catch (Exception e) {
			logger.error( "System roles synchronization (into portal) error ", e);
		} finally {
			final long end_ms = System.currentTimeMillis();
			logger.info( String.format(" SyncRoles from DB into portal finished in \n" +
					"\t\t time: %1.3f sec for %d roles ", 
					new Object[] { 
						new Double((end_ms - start_ms)/1000.0),
						new Long( rolesCount)
				}));
		}
	}

	/**
	 * ��������� ������������ ����� ������������� �� �� � ������.
	 * @param srcDB: �������� �� ��� ��������� ������ ������������� � ��
	 * ����������� jbr-�����.
	 */
	private void synchUserRolesIntoPortal(final Database srcDB, final PortalFactory destPortal)
	{
		logger.debug("SyncUserRoles entered");
		final long start_ms = System.currentTimeMillis();

		int count = 0;
		try {
			// ������ ������������ ���� �����
			final Set /*<String>*/ skipLogins = 
				new HashSet( config.getList(ConfigUserRegistrySync.LIST_USERROLES_SKIP));

			// ������ ������������ �����....
			final Set everybodyRoles = encodeNativePortalNames( config.getList(ConfigUserRegistrySync.LIST_USERROLES_EVERYBODY));

			// ��������� ����� login-������������ -> ������ ��������� �����...
			final Map mapPersonRoles = new HashMap(); // <String, Collection<String>>
			srcDB.executeQuery( 
				getSystemUser(srcDB), 
				new QueryBase(){
					public Object processQuery() throws DataException {
						getJdbcTemplate().query( 
							"SELECT p.person_login, r.role_code \n" +
							"FROM person_role r \n" +
							"		INNER JOIN person p ON r.person_id=p.person_id \n",
							new RowMapper(){
								public Object mapRow(ResultSet rs, int rowNum)
										throws SQLException 
								{
									final String login = rs.getString(1);
									final String role = rs.getString(2);
									if (login != null && role != null) {
										if (skipLogins.contains(login)) {
											logger.info("Login \'"+ login +"\' configured to be skipped");
										} else {
											// ���������� ����� ���� � ������ ����� ������������...
											Set userRoles = (Set) mapPersonRoles.get(login);
											if (userRoles == null){
												userRoles = new HashSet( everybodyRoles.size() + 5);
												// �������� ����� ��� ���� ����.
												userRoles.addAll(everybodyRoles);
												mapPersonRoles.put(login, userRoles);
											}
											userRoles.add( role);
										}
									}
									return null;
								}}
						);
						return null;
					}}
				);
			if (!mapPersonRoles.isEmpty()) { 
				count = mapPersonRoles.size();
				// �������������...
				destPortal.getUserService().synchJbrPersonRoles(mapPersonRoles, true);
			} else {
				logger.warn("(!?) no personal jbr-roles found");
			}

		} catch (Exception e) {
			logger.error( "Person roles synchronization (into portal) error ", e);
		} finally {
			final long end_ms = System.currentTimeMillis();
			logger.info( String.format(" SyncPersonRoles from DB into portal finished in \n" +
					"\t\t time: %1.3f sec for %d persons", 
					new Object[] { 
						new Double((end_ms - start_ms)/1000.0),
						new Long(count)
				}));
		}
	}


	/**
	 * ������� ������ ���������� ���� ������ ��� �������� � UserService � 
	 * �������� �������� ���������� ����. ���������� �������� � ������. 
	 * @param list: List<String> ������ ���� ���������� �����.
	 * @return
	 */
	private Set encodeNativePortalNames(List list) {
		final Set /*String*/ result = new HashSet(list.size());
		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			final String item = (String) iterator.next();
			result.add(UserService.PREFIX_PORTAL_ROLENAME + item);
		}
		return result;
	}


	private UserData systemUser = null;
	public UserData getSystemUser( Database db) throws DataException {
		if (systemUser == null) {
			systemUser = new UserData();
			systemUser.setPerson(db.resolveUser(Database.SYSTEM_USER));
			systemUser.setAddress("internal");
		}
		return systemUser;
	}

}