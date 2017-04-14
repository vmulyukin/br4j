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
package com.aplana.dbmi.service.impl;

import com.aplana.dbmi.action.Action;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.QueryBase.QueryExecPhase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

import java.io.Serializable;

/**
 * Base class for all pre/post-processors implementations.<br>
 * Pre/post-processors is a special objects, defining additional actions to be
 * performed by system before/after given query object gets processed.<br>
 */
abstract public class ProcessorBase implements BeanFactoryAware, Serializable {
	private static final long serialVersionUID = 3L;

	protected final Log logger = LogFactory.getLog(getClass());

	private transient BeanFactory factory;
	private UserData user;
	private UserData realUser;
	private DataObject object;
	private Action action;
	private Object result;
	private int queueOrder = 0;
	private QueryExecPhase curExecPhase = QueryExecPhase.UNDEFINED;
	private QueryBase currentQuery;

	public void init (QueryBase query){
		if (query != null){
			setUser(query.getUser());
			setRealUser(query.getRealUser());
			setCurrentQuery(query);
		}
	}

	public void setQueueOrder(int order) {
		this.queueOrder = order;
	}

	/**
	 * @return Order of processor into query processor's queue
	 */
	public int getQueueOrder() {
		return queueOrder;
	}

	/**
	 * Gets information about user, who performs query
	 * @return information about user, who performs query
	 */
	public UserData getUser() {
		return user;
	}

	/**
	 * Sets information about user, who performs query
	 * @param user information about user, who performs query
	 */
	public void setUser(UserData user) {
		this.user = user;
	}

	public UserData getRealUser(){
		return realUser;
	}

	public void setRealUser(UserData realUser){
		this.realUser = realUser;
	}

	/**
	 * Gets object, processed by query.<br>
	 * Note that this property is not initialized by default.
	 * You should override protected {@link QueryBase#initProcessor(ProcessorBase)}
	 * method to be able to use it in query processors.
	 * @return {@link DataObject} processed by query
	 */
	public DataObject getObject() {
		return object;
	}

	/**
	 * Sets object, processed by query.<br>
	 * Note that this property is not initialized by default.
	 * You should override protected {@link QueryBase#initProcessor(ProcessorBase)}
	 * method to be able to use it in query processors.
	 * @param object {@link DataObject} processed by query
	 */
	public void setObject(DataObject object) {
		this.object = object;
	}

	/**
	 * Gets {@link Action} processed by query.<br>
	 * Could be used in processors associated with {@link ActionQueryBase} descendants
	 * only.
	 * @return {@link Action} processed by query.
	 */
	@SuppressWarnings("unchecked")
	public <T extends Action> T getAction() {
		return (T)action;
	}

	/**
	 * Gets {@link Action} processed by query.<br>
	 * Could be used in processors associated with {@link ActionQueryBase} descendants
	 * only.
	 * @param action {@link Action} processed by query.
	 */
	public void setAction(Action action) {
		this.action = action;
	}

	/**
	 * Gets result of query execution (possibly with changes performed by
	 * other pre-processors).<br>
	 * This method could be used in post-processors only.<br>
	 * Every post-processor can change result of query by returning non-null object
	 * as result of {@link #process()} method.
	 * @return result of query execution.
	 */
	public Object getResult() {
		return result;
	}

	/**
	 * Sets result of query execution.<br>
	 * This method could be used in post-processors only.<br>
	 * This property is initialized by {@link QueryBase} object before the call
	 * of {@link #process()} method.
	 * <br>
	 * Every post-processor can change result of query by returning non-null object
	 * as result of {@link #process()} method.
	 * @param result result of query execution
	 */
	public void setResult(Object result) {
		this.result = result;
	}

	public BeanFactory getBeanFactory() {
		return factory;
	}

	/**
	 * Sets bean factory. This method is called by spring framework.
	 */
	public void setBeanFactory(BeanFactory factory) {
		this.factory = factory;
	}

	/**
	 * Gets {@link QueryFactory} object used by {@link DataServiceBean}.
	 * Could be used to perform other queries during pre/post-processing.
	 * @return {@link QueryFactory} object used by {@link DataServiceBean}.
	 */
	public QueryFactory getQueryFactory()
	{
		return (QueryFactory) factory.getBean(DataServiceBean.BEAN_QUERY_FACTORY);
	}

	/**
	 * Gets {@link Database} DAO object used by {@link DataServiceBean}.
	 * Could be used to perform other queries during pre/post-processing.
	 * @return {@link Database} DAO object used by {@link DataServiceBean}.
	 */
	public Database getDatabase()
	{
		return (Database) factory.getBean(DataServiceBean.BEAN_DATABASE);
	}

	public QueryExecPhase getCurExecPhase() {
		return curExecPhase;
	}

	/**
	 * ��������� ������� ���� ���������� �������� ({@link QueryBase})
	 */
	public void setCurExecPhase(QueryExecPhase curExecPhase) {
		this.curExecPhase = curExecPhase;
	}

	/**
	 * ��������� ������� �������� ���� {@link QueryBase} � ������ ��������
	 * ���������� ������ ���������. ��������������� ��������������� ����� �����������
	 * ����������.
	 * @param currentQuery ������ �������� ��������.
	 */
	void setCurrentQuery(QueryBase currentQuery) {
		this.currentQuery = currentQuery;
	}

	/**
	 * ��������� ������� �������� ���� {@link QueryBase} � ������ ��������
	 * ���������� ������ ���������. ������������ ��� ������������� ������ ����������.
	 * @return ������ �������� ��������.
	 */
	protected QueryBase getCurrentQuery() {
		return currentQuery;
	}

	/**
	 * �������� ������ ��������������� �������� (�� ����� ������������ ��������)
	 * ���� {@link QueryBase} � ������ �������� ����������� ������ ���������.
	 * ������������ ��� ������������� ������ ����������.
	 * @return ������ ��������������� �������� ���� null ���� ������ ���������
	 * ��� ������ � ������ �� ���������� ������������� �������.
	 */
	protected QueryBase getPrimaryQuery(){
		QueryBase query = getCurrentQuery();
		if (query == null){
			return null;
		}

		while (query.getParentQuery() != null){
			query = query.getParentQuery();
		}
		return query;
	}

	/**
	 * �������� ������������ (��� ��������), ��������������� �������� ��������
	 * @return ������������ ��� �������, �������������� �������� ��������
	 */
	protected UserData getPrimaryUser(){
		return getPrimaryQuery().getUser();
	}

	/**
	 * �������� ��������������� ������������, ��������������� �������� ��������
	 * @return �������������� ������������, �������������� �������� ��������
	 */
	protected UserData getPrimaryRealUser(){
		return getPrimaryQuery().getRealUser();
	}

    private UserData systemUser;
	public UserData getSystemUser() throws DataException {
		if (systemUser == null) {
			systemUser = new UserData();
			systemUser.setPerson(getDatabase().resolveUser(Database.SYSTEM_USER));
			systemUser.setAddress("internal");
		}
		return systemUser;
	}

	/**
	 * Main method of class.
	 * All required pre/post-processing steps for query
	 * should be implemented here in descendant classes.
	 * @return Result of pre/post-processing. For pre-processors this value is ignored.
	 * Post-processors could change result of query by returning non-null object here.
	 * If post-processors returns null value, then result of query will not be changed.
	 * @throws DataException in case of business-logic error.
	 */
	abstract public Object process() throws DataException;
}
