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
import com.aplana.dbmi.action.CheckLock;
import com.aplana.dbmi.model.LogEntry;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.filter.Filter;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ObjectLockedException;
import com.aplana.dbmi.service.ObjectNotLockedException;
import com.aplana.dbmi.service.impl.access.AccessRuleManager;
import com.aplana.dbmi.service.impl.async.QueryContainer;
import com.aplana.dbmi.utils.QueryInspector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Query is a basic processing unit of DBMI system which represents user request
 * to perform some action/get some data.
 * So each query should be initialized with information about user
 * who performs request.
 * <br>
 * Business logic to be executed by query must be implemented by descendant classes
 * in {@link #processQuery()} method.
 * <br>
 * Each query could have validators, pre- and post- {@link ProcessorBase processors} specified.
 * <br>
 * It is possible to restrict access to query for specific subset of users by
 * setting its {@link #setAccessChecker(AccessCheckerBase) accessChecker} property.
 * <br>
 * In most cases Query instances should be created by executing one of
 * 'get*Query' methods of {@link QueryFactory}. QueryFactory will initialize
 * access checker and pre/post- processors of query as defined in system configuration.
 */
abstract public class QueryBase implements BeanFactoryAware, DatabaseClient,
		Serializable {

	/**
	 * �������������, ������������ ���� ���������� �������� ({@link QueryBase})
	 * ������������ ��� �����������. � ��� ������� ���������� ������ ����������
	 * ����� ������� �������� � �������������� ������.
	 * ({@link QueryBase})
	 * UNDEFINED - ���� �� ����������.
	 * PREPARE - ���� ���������� ������;
	 * VALIDATE - ���� ��������� ������;
	 * PREPROCESS - ���� ��������������� ��������� ������;
	 * POSTPROCESS - ���� ������������� ������.
	 */
	public enum QueryExecPhase {UNDEFINED, PREPARE, VALIDATE, PREPROCESS, POSTPROCESS}
	private static final long serialVersionUID = 10l;
	protected final Log logger = LogFactory.getLog(getClass());
	private transient BeanFactory factory;
	private transient JdbcTemplate jdbc;
	private UserData user;
	private AccessCheckerBase accessChecker;
	private List<ProcessorBase> prepareProcessors = new ArrayList<ProcessorBase>();
	private List<ProcessorBase> preProcessors = new ArrayList<ProcessorBase>();
	private List<ProcessorBase> postProcessors = new ArrayList<ProcessorBase>();
	private Queue<ProcessorBase> postProcessorQueue = new PriorityQueue<ProcessorBase>(1, new Comparator<ProcessorBase>() {
		@Override
		public int compare(ProcessorBase o1, ProcessorBase o2) {
			return o1.getQueueOrder() - o2.getQueueOrder();
		}
	});
	private List<ProcessorBase> validators = new ArrayList<ProcessorBase>();
	private List<ObjectId> cardsForRecalculateAL = new ArrayList<ObjectId>();
	private List<ObjectId> recalculatedALCards = new ArrayList<ObjectId>();
	private Filter filter;
	private UserData realUser;
	private String policy;
	private Integer priority;
	private boolean isAsync = false;
	private boolean isRestored = false;
	private Integer reservedLogActionId;
	private QueryExecuteOptions execOption;
	private QueryBase parentQuery;
	private QueryContainer queryContainer;
	private Integer sessionId;
	private QueryExecPhase execPhase = QueryExecPhase.UNDEFINED;
	private long level = 0l;


	// ���������� � �������� JVM ������������� ������� ���������� QueryBase
	// ������������� ��� �������� ��������� ��� �����
	private ObjectId uid = new ObjectId(this.getClass(), new QueryUID(this));
	private Database database;
	private AccessRuleManager accessManager;
	private UserData systemUser;
    
    private static final class QueryUID {
		private final long time;
		private final long hash;
    	
    	protected QueryUID(QueryBase query) {
    		this.time = System.currentTimeMillis();
    		this.hash = System.identityHashCode(query);
    	}

		@Override
		public String toString() {
			return time + "-" + hash;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (obj == null) return false;
			if (this == obj) return true;
			if (obj instanceof QueryUID) {
				QueryUID other = (QueryUID)obj;
				if (other.hash == this.hash && other.time == this.time)
					return true;
			}
			return false;
		}
    }
    
	public QueryBase renew(Object[] objs) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		QueryBase renewQuery = (QueryBase) getExecOption().getExecMethod().invoke(getQueryFactory(), objs);
		renewQuery.uid = this.uid;
		renewQuery.queryContainer = this.queryContainer;
		renewQuery.sessionId = this.sessionId;
		return renewQuery;
	}

	public void setJdbcTemplate(JdbcTemplate jdbc) {
		this.jdbc = jdbc;
		if (accessChecker != null)
			accessChecker.setJdbcTemplate(jdbc);
	    accessManager = new AccessRuleManager(jdbc);
	}

	protected JdbcTemplate getJdbcTemplate() {
		return jdbc;
	}

	/**
	 * Gets information about user who performed this query
	 * @return information about user who performed this query
	 */
	public UserData getUser() {
		return user;
	}

	/**
	 * Sets information about user who performed this query
	 * @param user {@link UserData} object with information about user who performed this query
	 */
	public void setUser(UserData user) {
		this.user = user;
		if (accessChecker != null)
			accessChecker.setUser(user);
	}

	/**
	 * Gets access checker object restricting access to this query
	 * @return {@link AccessCheckerBase access checker} object restricting access to this query.
	 * Could be null if no restriction required.
	 */
	public AccessCheckerBase getAccessChecker() {
		return accessChecker;
	}

	/**
	 * Sets access checker object restricting access to this query
	 * @param accessChecker {@link AccessCheckerBase access checker} object which
	 * defines access restriction to this query. Could be null if no restriction required.
	 */
	public void setAccessChecker(AccessCheckerBase accessChecker) {
		this.accessChecker = accessChecker;
		if (accessChecker != null) {
			accessChecker.setJdbcTemplate(getJdbcTemplate());
			accessChecker.setUser(getUser());
		}
	}

	/*public ProcessorBase getPreProcessor() {
		return preProcessor;
	}*/

	/**
	 * Adds new validator for this query. Note that validators will be executed in
	 * the same order as they were added.
	 *
	 * @link processor validator to be added
	 * @throws IllegalArgumentException if processor is null
	 */
	public void addValidator(ProcessorBase processor) {
		if (processor == null)
			throw new IllegalArgumentException("Processor can't be null");
		validators.add(processor);
	}

	/**
	 * Adds new pre-processor for this query. Note that pre-processors will be executed in
	 * the same order as they were added.
	 *
	 * @link processor pre-processor to be added
	 * @throws IllegalArgumentException if processor is null
	 */
	public void addPreProcessor(ProcessorBase processor) {
		if (processor == null)
			throw new IllegalArgumentException("Processor can't be null");
		preProcessors.add(processor);
	}

	public void addPrepareProcessor(ProcessorBase processor) {
		if (processor == null)
			throw new IllegalArgumentException("Processor can't be null");
		prepareProcessors.add(processor);
	}

	/*public ProcessorBase getPostProcessor() {
		return postProcessor;
	}*/

	/**
	 * Adds new post-processor for this query. Note that post-processors
	 * will be executed in the same order as they were added.
	 * @link processor post-processor to be added
	 * @throws IllegalArgumentException if processor is null
	 */
	public void addPostProcessor(ProcessorBase processor) {
		if (processor == null)
			throw new IllegalArgumentException("Processor can't be null");
		if (execPhase.equals(QueryExecPhase.UNDEFINED)) {
			postProcessors.add(processor);
		} else {
			processor.setQueueOrder(processor.getQueueOrder() + postProcessorQueue.size());
			postProcessorQueue.add(processor);
		}
	}

	/**
	 * Gets {@link Filter} object to be applied on query result.<br>
	 * NOTE: To be able to use result filtering {@link QueryBase} descendant
	 * must override protected method QueryBase.supportsFilter.
	 * @return {@link Filter} object to be applied on query result.
	 */
	public Filter getFilter() {
		return filter;
	}

	/**
	 * Sets {@link Filter} object to be applied on query result.<br>
	 * NOTE: To be able to use result filtering {@link QueryBase} descendant
	 * must override protected method QueryBase.supportsFilter.
	 * @param filter filter to be used for result filtering
	 * @throws DataException if this Query object is not supports
	 * result filtering and non-null filter object is specified
	 */
	public void setFilter(Filter filter) throws DataException {
		if (filter != null && !supportsFilter(filter.getClass()))
			throw new DataException("factory.filter", new Object[] {
					DataException.RESOURCE_PREFIX + getClass().getName(),
					DataException.RESOURCE_PREFIX + filter.getClass().getName() });
		this.filter = filter;
	}

	/**
	 * @param type Filter's type
	 * @return true if query supports filter
	 */
	protected boolean supportsFilter(Class<?> type) {
		return false;
	}

	/**
	 * Checks if this query could be performed by current user.
	 * If no {@link #setAccessChecker(AccessCheckerBase) access checker} was specified
	 * then returns true, otherwise
	 * execute access checker's {@link AccessCheckerBase#checkAccess() checkAccess} method
	 * and return its result.
	 * @return true if user could perform this query object, false otherwise
	 * @throws DataException in case of business-logic error in access checker
	 */
	/*abstract*/ final public boolean checkAccess() throws DataException {
		return accessChecker == null || accessChecker.checkAccess();
	}

	public void prepare() throws DataException {
		execPhase = QueryExecPhase.PREPARE;
		for (ProcessorBase processor : prepareProcessors) {
			if (logger.isDebugEnabled()) {
				logger.debug("Start PrepareProcessor=" + processor.getClass().getSimpleName() + "; on event=" + getEvent() + " objectId=" + getEventObject());
			}
			initProcessor(processor);
			long start = System.currentTimeMillis();
			QueryInspector.start(processor, "prepare", level);
			processor.process();
			QueryInspector.end(System.currentTimeMillis() - start, level);
			if (logger.isDebugEnabled()) {
				logger.debug("End PrepareProcessor=" + processor.getClass().getSimpleName() + "; on event=" + getEvent() + " objectId=" + getEventObject());
			}
		}
	}

	/**
	 * Execute all validators specified in this query object. This method is
	 * called by {@link DatabaseBean} before {@link #preProcess()} and always
	 * synchronously.
	 *
	 * @throws DataException
	 *             in case of checking error
	 */
	public void validate() throws DataException {
		execPhase = QueryExecPhase.VALIDATE;
		for (ProcessorBase processor : validators) {
			if (logger.isDebugEnabled()) {
				logger.debug("Start Validator=" +  processor.getClass().getSimpleName() + "; on event=" + getEvent() + " objectId=" + getEventObject());
			}
			initProcessor(processor);
			long start = System.currentTimeMillis();
			QueryInspector.start(processor, "validate", level);
			processor.process();
			QueryInspector.end(System.currentTimeMillis() - start, level);
			if (logger.isDebugEnabled()) {
				logger.debug("End Validator=" + processor.getClass().getSimpleName() + "; on event=" + getEvent() + " objectId=" + getEventObject());
			}
		}
	}

    protected void cleanAccessList(final ObjectId cardId) {
		if (cardId != null) {
			//accessManager.cleanAccessListByCard(cardId);
			//accessManager.cleanAccessListBySourceCard(cardId);
			accessManager.cleanAccessListByCardAndSourceAttrs(cardId);
		}
    }
	/**
	 * �������� ������� ���� ��� ���� �������� �� ������ cardsForRecalculateAL
	 * @throws DataException
	 */
	public void recalculateAccessList() throws DataException {
		if (cardsForRecalculateAL==null||cardsForRecalculateAL.isEmpty()){
			logger.debug("cardsForRecalculateAL for currentQuery is empty: maybe is not primary query");
			return;
		}
		long start = System.currentTimeMillis();
		/*int from = 0;
		boolean finish = false;
		while(!finish){
			int to = from + AccessRuleManager.BUCKET_SIZE;
			if(to > cardsForRecalculateAL.size()){
				to = cardsForRecalculateAL.size();
				finish = true;
			}
			List<ObjectId> subList = cardsForRecalculateAL.subList(from, to);
			accessManager.updateAccessToCard(subList, getOtherCardIds(subList));
			from = to;
		}*/
		
		accessManager.updateAccessToCard(cardsForRecalculateAL, getRecalculatedALCardsIds());
		logger.info("Finish! Permissions for " + cardsForRecalculateAL.size() + " cards added. " + (System.currentTimeMillis() - start) + " ms");
	}

	private List<Long> getOtherCardIds(ObjectId cardId){
		List<Long> result = new ArrayList<Long>();
		for(ObjectId id: cardsForRecalculateAL){
			if(!id.equals(cardId)){
				result.add((Long)id.getId());
			}
		}
        //��������� � ������ "���������" ����� ������������� ��������.
        result.addAll(getRecalculatedALCardsIds());
		return result;
	}

    /**
     * ��������� ������ ��������������� ��������, � �������������� ACL.
     */
    private List<Long> getRecalculatedALCardsIds() {
        List<Long> result = new ArrayList<Long>();
        for (ObjectId id : recalculatedALCards) {
            result.add((Long) id.getId());
        }
        return result;
    }


	/**
	 * �������� ������� ���� ��� ���������� �������� �� ������ cardsForRecalculateAL
	 * @throws DataException
	 */
	public void recalculateAccessList(ObjectId cardId) throws DataException {
		if (cardsForRecalculateAL==null||cardsForRecalculateAL.isEmpty()){
			logger.debug("cardsForRecalculateAL for currentQuery is empty: maybe is not primary query");
			return;
		}
		if (cardsForRecalculateAL.contains(cardId)){
			accessManager.updateAccessToCard(Collections.singletonList(cardId), getOtherCardIds(cardId));
            cardsForRecalculateAL.remove(cardId);
            if(!recalculatedALCards.contains(cardId)) {
                recalculatedALCards.add(cardId);
            }
			if (logger.isDebugEnabled()) {
				logger.debug("recalculate access list for card " + cardId.getId() + " is success, card remove from cardsForRecalculateAL");
			}
		}
	}

	/**
	 * Execute all pre-processors specified in this query object.
	 * This method is called by {@link DatabaseBean} before {@link #processQuery()}.
	 * @throws DataException in case of business-logic error
	 */
	public void preProcess() throws DataException {
		execPhase = QueryExecPhase.PREPROCESS;
		ProcessorBase processor;
		for (ProcessorBase preProcessor : preProcessors) {
			processor = preProcessor;
			if (logger.isDebugEnabled()) {
				logger.debug("Start PreProcessor=" + processor.getClass().getSimpleName() + "; on event=" + getEvent() + " objectId=" + getEventObject());
			}
			initProcessor(processor);
			long start = System.currentTimeMillis();
			QueryInspector.start(processor, "preProcess", level);
			processor.process();
			QueryInspector.end(System.currentTimeMillis() - start, level);
			if (logger.isDebugEnabled()) {
				logger.debug("End PreProcessor=" + processor.getClass().getSimpleName() + "; on event=" + getEvent() + " objectId=" + getEventObject());
			}
		}
	}

	/**
	 * Execute all post-processors specified in this query object.
	 * This method is called by {@link DatabaseBean} after {@link #processQuery()}.
	 * Note that this method could modify result of query,
	 * returned by {@link #processQuery()}.
	 * @param result result returned by {@link #processQuery()},
	 * i.e. it is a query result without post-processing.
	 * @return result of query after post-processing
	 * @throws DataException in case of business-logic error
	 */
	public Object postProcess(Object result) throws DataException {
		execPhase = QueryExecPhase.POSTPROCESS;
		ProcessorBase processor;
		for (ProcessorBase postProcessor : postProcessors) {
			processor = postProcessor;
			if (logger.isDebugEnabled()) {
				logger.debug("Start PostProcessor=" + processor.getClass().getSimpleName() + "; on event=" + getEvent() + " objectId=" + getEventObject());
			}
			initProcessor(processor);
			processor.setResult(result);
			long start = System.currentTimeMillis();
			QueryInspector.start(processor, "postProcess", level);
			Object changed = processor.process();
			QueryInspector.end(System.currentTimeMillis() - start, level);
			if (changed != null)
				result = changed;
			if (logger.isDebugEnabled()) {
				logger.debug("End PostProcessor=" + processor.getClass().getSimpleName() + "; on event=" + getEvent() + " objectId=" + getEventObject());
			}
		}
		while ((processor = postProcessorQueue.poll()) != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Start dynamic PostProcessor=" + processor.getClass().getSimpleName() + "; on event="+ getEvent() +" objectId=" + getEventObject());
			}
			initProcessor(processor);
			processor.setResult(result);
			long start = System.currentTimeMillis();
			QueryInspector.start(processor, "postProcessQueue", level);
			Object changed = processor.process();
			QueryInspector.end(System.currentTimeMillis() - start, level);
			if (changed != null)
				result = changed;
			if (logger.isDebugEnabled()) {
				logger.debug("End dynamic PostProcessor=" + processor.getClass().getSimpleName() + "; on event=" + getEvent() + " objectId=" + getEventObject());
			}
		}

		if (accessChecker != null && result != null) {
			accessChecker.filterResult(result);
		}

		return result;
	}

	/**
	 * This method initializes {@link ProcessorBase} descendant
	 * assosiated with this query object.<br>
	 * In default implementation it supplies processor with information
	 * about user who performs query and, if processor implements
	 * DatabaseClient interface, with jdbcTemplate used by query.
	 * <br>
	 * If extra initialization is required, then this method should be
	 * overriden in descendant class.
	 * @param processor processor to be initialized.
	 */
	protected void initProcessor(ProcessorBase processor) {
		processor.init(this);
		if (processor instanceof DatabaseClient) {
			((DatabaseClient) processor).setJdbcTemplate(getJdbcTemplate());
		}
		processor.setCurExecPhase(execPhase);
	}

	/**
	 * Creates new {@link LogEntry} object representing new entry to be
	 * written in DBMI system log.
	 * @return new {@link LogEntry} object representing new entry to be
	 * written in DBMI system log. If {@link #getEvent()} returns null then
	 * returns null.
	 * @see #getEvent()
	 * @see #getEventObject()
	 */
	final public LogEntry getLogEntry() {
		String event = getEvent();
		if (event == null)
			return null;
		LogEntry entry = new LogEntry();
		entry.setEvent(event);
		entry.setObject(getEventObject());
		entry.setUser(getUser().getPerson());
		entry.setAddress(getUser().getAddress());
		entry.setTimestamp(new Date());
		entry.setRealUser(getRealUser() != null ? getRealUser().getPerson() : null);
		entry.setUid(uid);
		entry.setParentUid(parentQuery != null ? parentQuery.uid : null);
		entry.setIdLogAction(reservedLogActionId);
		return entry;
	}

	/**
	 * Main method of query. Must be implemented in descendant classes.
	 * Should implement all business-logic to be performed by this query object.
	 * @return result of query
	 * @throws DataException in case of business-logic error
	 */
	abstract public Object processQuery() throws DataException;

	/**
	 * Gets string code of system event represented by this query.
	 * This code is used to write messages into DBMI system log.
	 * Should match one of ACTION_CODE column values in ACTION table.
	 * @return string code of system event represented by this query.
	 */
	public String getEvent() {
		return null;
	}

	/**
	 * Gets identifier of data object processed by this query. This value is used to
	 * write new messages in system log.
	 * @return identifier of data object processed by this query. Could be null.
	 * @see #getLogEntry()
	 */
	public ObjectId getEventObject() {
		return null;
	}

	/**
	 * Checks if given object is locked by user who performs this query.
	 * @param objId identifier of object to check
	 * @throws ObjectLockedException if given object is locked by different user
	 * @throws ObjectNotLockedException if given object is not locked
	 */
	protected void checkLock(ObjectId objId) throws DataException {
		Action action = new CheckLock(objId);
		ActionQueryBase query = getQueryFactory().getActionQuery(action);
		query.setAction(action);
		getDatabase().executeQuery(getUser(), query);
	}

	/**
	 * Sets BeanFactory used by this Query object.
	 * This method is called automatically by spring framework.
	 */
	public void setBeanFactory(BeanFactory factory) throws BeansException {
		this.factory = factory;
	}

	protected BeanFactory getBeanFactory()
	{
		return factory;
	}

	public QueryFactory getQueryFactory()
	{
		return (QueryFactory) factory.getBean(DataServiceBean.BEAN_QUERY_FACTORY);
	}

	protected Database getDatabase() {
		return this.database == null ? (Database) factory.getBean(DataServiceBean.BEAN_DATABASE) : this.database;
	}

	public void setDatabase(Database database) {
		this.database = database;
	}

	List<ProcessorBase> getPreProcessors() {
		return this.preProcessors;
	}

	List<ProcessorBase> getPostProcessors() {
		return this.postProcessors;
	}

	List<ProcessorBase> getValidators() {
		return this.validators;
	}

    public UserData getRealUser() {
        return realUser;
    }

    public void setRealUser(UserData realUser) {
        this.realUser = realUser;
    }

	/**
	 * Returns true, if this query must be performed asynchronously
	 *
	 * @return true if this query must be performed asynchronously ,otherwise
	 *         false
	 */
	public boolean isAsync() {
		return this.isAsync;
	}

	/**
	 * Sets whether the query performed asynchronously
	 *
	 * @param async
	 *            true if needed
	 */
	public void setAsync(boolean async) {
		this.isAsync = async;
	}

	/**
	 * Specifies whether the request is restored from a backup, or not
	 * @param isRestored true if restored, default false
	 */
	public void setRestored(boolean isRestored) {
		this.isRestored = isRestored;
	}

	/**
	 *  Whether the request is restored from a backup, or not
	 * @return true if restored, otherwise false
	 */
	public boolean isRestored() {
		return isRestored;
	}

	public void setAsyncPolicyName(String policy) {
		this.policy = policy;
	}

	public String getAsyncPolicyName() {
		return policy;
	}

	@Override
	public int hashCode() {
		int hash = 37;
		hash ^= ((jdbc != null) ? jdbc.hashCode() : 15487565)
				^ ((user != null) ? user.hashCode() : 17236172)
				^ Boolean.valueOf(isAsync).hashCode();
		return hash;
	}

	public void setExecOption(QueryExecuteOptions execOption) {
		this.execOption = execOption;
	}

	public QueryExecuteOptions getExecOption() {
		return execOption;
	}

	/**
	 * ��������� ������� �������� ���� {@link QueryBase} � ������ ��������
	 * ���������� ������ ��������. ��������������� ��������������� ����� �����������
	 * �������� ������������. ���� ������ �������� ��������, �� ���������� ���������� null.
	 * @param parentQuery ������ ������������ �������� ���� null ���� ������ �������� ��������.
	 */
	void setParentQuery(QueryBase parentQuery) {
		this.parentQuery = parentQuery;
	}

	/**
	 * ��������� ������� �������� ���� {@link QueryBase} � ������ ��������
	 * ����������� ������ ��������.
	 * @return ������ ������������ �������� ���� {@link QueryBase}
	 * ���� null ���� ������ �������� ��������.
	 */
	public QueryBase getParentQuery() {
		return parentQuery;
	}

	public ObjectId getUid() {
		return uid;
	}

	public void setSessionId(Integer sessionId) {
		this.sessionId = sessionId;
	}

	public Integer getSessionId() {
		return sessionId;
	}

	public List<ObjectId> getCardsForRecalculateAL() {
		return cardsForRecalculateAL;
	}

	public void setCardsForRecalculateAL(List<ObjectId> cardsForRecalculateAL) {
		this.cardsForRecalculateAL = cardsForRecalculateAL;
	}

	/**
	 * ���������� �������� � ������ ��������� � ��������������� ��������� �� ��������� � � ���� ������
	 * @param cardId - id �������� ��� ����������
	 */
	public void putCardIdInRecalculateAL(ObjectId cardId){
		if (!cardsForRecalculateAL.contains(cardId)) {
			cardsForRecalculateAL.add(cardId);
		}
        //������� �������� �� ����� �������������
        recalculatedALCards.remove(cardId);
	}

	/**
	 * �������� ������ ��������������� �������� (�� ����� ������������ ��������)
	 * ���� {@link QueryBase} � ������ �������� ����������� ������ ���������.
	 * @return ������ ��������������� �������� (������� ������, ���� � ���� ��� �������������, �.�. �� ������)
	 */
	protected QueryBase getPrimaryQuery(){
		QueryBase query = this;

		while (query.getParentQuery() != null){
			query = query.getParentQuery();
		}
		return query;
	}

	protected long getLevel() {
		return level;
	}

	protected void setLevel(long l) {
		level = l;
	}

	public UserData getSystemUser() throws DataException {
		if (systemUser == null) {
			systemUser = new UserData();
			systemUser.setPerson(getDatabase().resolveUser(Database.SYSTEM_USER));
			systemUser.setAddress("internal");
		}
		return systemUser;
	}
	
	public void setQueryContainer(QueryContainer qc) {
		this.queryContainer = qc;
	}
	
	public QueryContainer getQueryContainer() {
		return this.queryContainer;
	}
	
	public void setPriority(Integer priority) {
		this.priority = priority;
	}
	
	public Integer getPriority() {
		return priority;
	}

	public Integer getReservedLogActionId() {
		return reservedLogActionId;
	}

	public void setReservedLogActionId(Integer reservedLogActionId) {
		this.reservedLogActionId = reservedLogActionId;
	}
}
