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
package com.aplana.dbmi.service.impl.async;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.core.support.SqlLobValue;
import org.springframework.jdbc.support.nativejdbc.JBossNativeJdbcExtractor;

import com.aplana.dbmi.action.Action;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.User;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.service.impl.DataServiceBean;
import com.aplana.dbmi.service.impl.Database;
import com.aplana.dbmi.service.impl.QueryBase;
import com.aplana.dbmi.service.impl.QueryExecuteOptions;
import com.aplana.dbmi.service.impl.QueryFactory;
import com.aplana.dbmi.service.impl.SaveQueryBase;
import com.aplana.dbmi.service.impl.UserData;
import com.aplana.dbmi.service.impl.locks.LockManagementSPI;
import com.aplana.dbmi.service.impl.locks.LockManagementBean.Lock;

/**
 * This class can save and restore {@link QueryContainer queries}. <br>
 * Sample on Spring IoC:
 * 
 * <pre>
 * {@code 
 * <bean id="backupManager" class="com.aplana.dbmi.service.impl.async.AsyncBackupBean">
 * 		<property name="dataSource" ref="dataSource" />
 * </bean> 
 * ...
 * <bean id="asyncQueriesQueue" class="com.aplana.dbmi.service.impl.async.AsyncQueriesQueue"
 * 		init-method="init">
 * 		<constructor-arg value="20" />
 * 		<property name="backupManager" ref="backupManager" />
 * </bean>}
 * </pre>
 */
public class AsyncBackupBean extends JdbcDaoSupport implements BeanFactoryAware {

	private static final String SQL_GET_QUERIES = "SELECT query_id, options, objects, query_user, lock FROM async_queue";
	private static final String SQL_GET_QUERY = "SELECT query_id, options, objects, query_user, lock FROM async_queue WHERE query_id = ?";
	private static final String SQL_REMOVE_QUERIES = "DELETE FROM async_queue";
	private static final String SQL_REMOVE_QUERY = "DELETE FROM async_queue WHERE query_id = ?";
	private static final String SQL_ADD_QUERY = "INSERT INTO async_queue(query_id, options, objects, query_user, lock, add_date) select ?, ?, ?, ?, ?, ? where not exists (select 1 from async_queue where query_id = ?)";
	private Database db;
	private Boolean loaded = false;
	private QueryFactory factory;
	private BeanFactory beanFactory;
	private LockManagementSPI lockManager;
	private static final Log logger = LogFactory.getLog(AsyncBackupBean.class);

	@Override
	protected void initTemplateConfig() {
		getJdbcTemplate()
				.setNativeJdbcExtractor(new JBossNativeJdbcExtractor());
	}

	/**
	 * Restore all queries from DB
	 * 
	 * @throws DataException
	 */
	public void restore() throws DataException {
		synchronized (this) {
			if (loaded)
				return;
			loaded = true;
		}
		this.factory = (QueryFactory) beanFactory.getBean(DataServiceBean.BEAN_QUERY_FACTORY);
		this.db = (Database) beanFactory.getBean(DataServiceBean.BEAN_DATABASE);
		this.lockManager = (LockManagementSPI) beanFactory.getBean(DataServiceBean.LOCK_MANAGEMENT_BEAN);
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> rawQueries = getJdbcTemplate().queryForList(SQL_GET_QUERIES);
		List<QueryBase> queries = fill(rawQueries);
		if (!queries.isEmpty()) {
			getJdbcTemplate().update(SQL_REMOVE_QUERIES);
		} else
			return;
		for (QueryBase query : queries) {
			db.executeQuery(query.getUser(), query);
		}
	}

	/**
	 * Write query to DB
	 * 
	 * @throws DataException
	 */
	public void write(QueryContainer query) throws DataException {
		QueryContainer.BackupData data = query.getBackupData();
		getJdbcTemplate().update(
				SQL_ADD_QUERY,
				new Object[] { query.getId(),
						new SqlLobValue(data.getOptions().toByteArray()),
						new SqlLobValue(data.getWriteObjects().toByteArray()),
						new SqlLobValue(data.getUser().toByteArray()),
						new SqlLobValue(data.getLock().toByteArray()),
						query.getAddingTime(),
						query.getId() },
				new int[] { Types.BIGINT, Types.BLOB, Types.BLOB, Types.BLOB,
						Types.BLOB, Types.TIMESTAMP, Types.BIGINT });
	}

	/**
	 * Returns query renewed from backup
	 * 
	 * @param id
	 *            query id
	 * @return query
	 * @throws DataException
	 */
	public QueryBase get(long id) throws DataException {
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> rawQueries = getJdbcTemplate().queryForList(
				SQL_GET_QUERY, new Object[] { id }, new int[] { Types.BIGINT });
		List<QueryBase> queries = fill(rawQueries);
		if (queries.isEmpty())
			return null;
		else
			return queries.get(0);
	}

	/**
	 * Removes query from DB
	 * 
	 * @param q
	 *            query
	 * @throws DataException
	 */
	public void remove(long id) {
		getJdbcTemplate().update(SQL_REMOVE_QUERY, new Object[] { id },
				new int[] { Types.BIGINT });
		if(logger.isDebugEnabled()) {
			logger.debug("Removed query (id="+id+") from async_queue");
		}
	}

	private List<QueryBase> fill(List<Map<String, Object>> rawQueries)
			throws DataException {

		// needed to sort in added order
		SortedMap<Long, Object[]> qs = new TreeMap<Long, Object[]>();
		try {
			for (Map<String, Object> m : rawQueries) {
				long id = (Long) m.get("query_id");
				byte[] q = (byte[]) m.get("options");
				// Action/DataObject first
				byte[] o = (byte[]) m.get("objects");
				byte[] u = (byte[]) m.get("query_user");
				byte[] l = (byte[]) m.get("lock");
				ObjectInputStream stream = new ObjectInputStream(
						new ByteArrayInputStream(q));
				QueryExecuteOptions opt = (QueryExecuteOptions) stream
						.readObject();
				stream = new ObjectInputStream(new ByteArrayInputStream(o));
				Object[] qos = (Object[]) stream.readObject();
				stream = new ObjectInputStream(new ByteArrayInputStream(u));
				User user = (User) stream.readObject();
				stream = new ObjectInputStream(new ByteArrayInputStream(l));
				Lock lock = (Lock) stream.readObject();
				qs.put(id, new Object[] { opt, qos, user, lock });
				stream.close();
			}
		} catch (IOException e) {
			logger.error("Exception during restoring queries.", e);
			throw new DataException("general.runtime.async.adding", e);
		} catch (ClassNotFoundException e) {
			logger.error("Exception during restoring queries.", e);
			throw new DataException("general.runtime.async.adding", e);
		}
		List<QueryBase> queries = new ArrayList<QueryBase>();
		for (Map.Entry<Long, Object[]> entry : qs.entrySet()) {
			QueryExecuteOptions opt = (QueryExecuteOptions) entry.getValue()[0];
			Object[] qos = (Object[]) entry.getValue()[1];
			Method mtd = opt.getExecMethod();
			User user = ((User) entry.getValue()[2]);
			Lock lock = ((Lock) entry.getValue()[3]);
			// TODO: Other methods (delete/fetch/...) can set other object from
			// qos
			QueryBase query = null;
			try {
				query = (QueryBase) mtd.invoke(factory, qos[0]);
				if (query instanceof ActionQueryBase) {
					((ActionQueryBase) query).setAction((Action) qos[0]);
				} else if (query instanceof SaveQueryBase) {
					((SaveQueryBase) query).setObject((DataObject) qos[0]);
				}
			} catch (IllegalArgumentException e1) {
				logger.error("Exception during restoring queries.", e1);
				throw new DataException("general.runtime.async.adding", e1);
			} catch (IllegalAccessException e1) {
				logger.error("Exception during restoring queries.", e1);
				throw new DataException("general.runtime.async.adding", e1);
			} catch (InvocationTargetException e1) {
				logger.error("Exception during restoring queries.", e1);
				throw new DataException("general.runtime.async.adding", e1);
			}
			query.setRestored(true);
			if (lock != null) {
				lockManager.restore(query, lock);
			}
			query.setUser(UserData.read(user));
			queries.add(query);
		}
		return queries;
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;

	}
}
