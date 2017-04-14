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
import com.aplana.dbmi.action.ObjectAction;
import com.aplana.dbmi.model.DataObject;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.cache.CachingChildrenQuery;
import com.aplana.dbmi.service.impl.cache.CachingObjectQuery;
import com.aplana.dbmi.service.impl.cache.CachingQuery;
import com.aplana.dbmi.utils.StrUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xpath.domapi.XPathEvaluatorImpl;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.xpath.XPathEvaluator;
import org.w3c.dom.xpath.XPathResult;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * This class is used to create new instances of {@link QueryBase} descendant classes.
 * <br>
 * QueryFactory is configured through special configuration file queries.xml
 * <ul>Configuration file defines:
 * <li>for every {@link Action} type - {@link ActionQueryBase} descendant to process actions of this type;
 * </li>
 * <li>for every {@link DataObject} type - set of queries used to save/delete or fetch
 * object and its children from database;
 * </li>
 * <li>for every processor implementing {@link Parametrized} interface set of initialization parameters;  
 * </li>
 * </ul>
 * <br>
 * Also for every query in file could be defined set of {@link ProcessorBase pre/post-processors}, 
 * as well as {@link AccessCheckerBase access checker}.
 * <br>
 * After initialization QueryFactory could be used to create properly initialized instances of
 * {@link QueryBase query} classes by calling one of following methods:
 * {@link #getActionQuery(Action)}, {@link #getChildrenQuery(Class, Class)}, {@link #getListQuery(Class)},
 * {@link #getFetchQuery(Class)}, {@link #getSaveQuery(DataObject)}, {@link #getDeleteQuery(ObjectId)}.
 * <br>
 */
public class QueryFactory implements BeanFactoryAware, InitializingBean {
	protected Log logger = LogFactory.getLog(getClass());

	private static final String TAG_ROOT = "queries";
	private static final String TAG_OBJECT = "object";
	private static final String TAG_LIST_QUERY = "list-all";
	private static final String TAG_CHILDREN_QUERY = "list-children";
	private static final String TAG_GET_QUERY = "get-one";
	private static final String TAG_SAVE_QUERY = "store";
	private static final String TAG_DELETE_QUERY = "delete";
	private static final String TAG_ACTION = "action";
	private static final String TAG_QUERY = "query";
	private static final String TAG_ACCESS = "access";
	private static final String TAG_VALIDATOR= "validator";
	private static final String TAG_PREPROCESS = "pre-process";
	private static final String TAG_POSTPROCESS = "post-process";
	//private static final String TAG_EVENT = "log-event";
	private static final String TAG_SPECIFIC = "specific";
	private static final String TAG_PARAMETER = "parameter";

	private static final String ATTR_TYPE = "type";
	private static final String ATTR_PACKAGE = "package";
	private static final String ATTR_CLASS = "class";
	private static final String ATTR_SELECTOR_BEAN = "selectorBean";
	private static final String ATTR_PROPERTY = "property";
	private static final String ATTR_VALUE = "value";
	private static final String ATTR_EQUALS = "equals";
	private static final String ATTR_RUNORDER = "runOrder";
	private static final String ATTR_LOAD = "load";
	private static final String ATTR_NAME = "name";
	private static final String ATTR_PKG_MODEL = "model-package";
	private static final String ATTR_PKG_ACTION = "action-package";
	private static final String ATTR_PKG_QUERY = "query-package";
	private static final String ATTR_PKG_ACCESS = "access-package";
	private static final String ATTR_PKG_PROCESS = "process-package";
	private static final String ATTR_PKG_SELECTOR = "selector-package";
	private static final String ATTR_CACHE = "cache";
	private static final String ATTR_ASYNC = "async";
	private static final String ATTR_POLICY = "policy";
	private static final String ATTR_PRIORITY = "priority";
	
	private static final String PKG_MODEL = "com.aplana.dbmi.model";
	private static final String PKG_ACTION = "com.aplana.dbmi.action";
	private static final String PKG_QUERY = "com.aplana.dbmi.service.impl.query";
	private static final String PKG_ACCESS = "com.aplana.dbmi.service.impl.access";
	private static final String PKG_SELECTOR = "com.aplana.dbmi.service.impl";

	/**
	 * �������� �������� � ����������� ��� ��������� ���������������� ���������� ����������.
	 */
	private static final String PROP_NAME_PARAMETERS = "parameters";

	private BeanFactory beanFactory;
	private String xmlFile;
	private static final HashMap<String, QueryDescriptor> listQueries = new HashMap<String, QueryDescriptor>();
	private static final HashMap<String, Map<String, QueryDescriptor>> childrenQueries = new HashMap<String,Map<String, QueryDescriptor>>();
	private static final HashMap<String, QueryDescriptor> fetchQueries = new HashMap<String, QueryDescriptor>();
	private static final HashMap<String, QueryDescriptor> saveQueries = new HashMap<String, QueryDescriptor>();
	private static final HashMap<String, QueryDescriptor> deleteQueries = new HashMap<String, QueryDescriptor>();
	private static final HashMap<String, QueryDescriptor> actionQueries = new HashMap<String, QueryDescriptor>();
	private static Boolean isInitialized = false;
	
	private static final String suffix = "queries.xml" ;
	
	private static final HashMap<String, Method> execMethods = new HashMap<String, Method>();
	static {
		Method method;
		try {
			method = QueryFactory.class.getMethod("getActionQuery", Action.class);
			execMethods.put("getActionQuery", method);
			method = QueryFactory.class.getMethod("getActionQuery", Class.class);
			execMethods.put("getSimpleActionQuery", method);
			method = QueryFactory.class.getMethod("getListQuery", Class.class);
			execMethods.put("getListQuery", method);
			method = QueryFactory.class.getMethod("getChildrenQuery", Class.class, Class.class);
			execMethods.put("getChildrenQuery", method);
			method = QueryFactory.class.getMethod("getFetchQuery", Class.class);
			execMethods.put("getFetchQuery", method);
			method = QueryFactory.class.getMethod("getSaveQuery", DataObject.class);
			execMethods.put("getSaveQuery", method);
			method = QueryFactory.class.getMethod("getDeleteQuery", ObjectId.class);
			execMethods.put("getDeleteQuery", method);
		} catch (SecurityException e) {
			throw new RuntimeException("Error preparing query factory", e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException("Error preparing query factory", e);
		}
	}
	
	private boolean useCache = true;
	
	/**
	 * Initializes QueryFactory instance with configuration file
	 * @param path path to QueryFactory configuration file (usually queries.xml) 
	 */
	public void setXmlFile(String path) {
		this.xmlFile = path;
	}
	
	public void afterPropertiesSet() throws Exception {
		synchronized (QueryFactory.class) {
			if(isInitialized) {
				return;
			}
			
			try {
				Resource[] xmls = new PathMatchingResourcePatternResolver(getClass().getClassLoader()).getResources(xmlFile);
				for (Resource xml : xmls) {
					String slash = SystemUtils.IS_OS_UNIX ? "/" : "";
					ZipFile zf = new ZipFile(slash + xml.getURL().getFile().split("!")[0].split("file:/")[1]);
					Enumeration<? extends ZipEntry> e = zf.entries();
					while (e.hasMoreElements()) {
						ZipEntry ze = e.nextElement();
						String fileName = ze.getName();
						if (fileName.endsWith(suffix)) {
							logger.info("Processing " + zf.getName() + "\\" + fileName);
							new Initializer().parse(zf.getInputStream(ze));
						}
					}
				}
			} catch (Exception e) {
				logger.error("Error reading query factory configuration", e);
				throw new RuntimeException("Error reading query factory configuration", e);
			}
			
			isInitialized = true;	
		}
	}

	/**
	 * Gets {@link QueryBase query} object used to list all objects of given type
	 * @param type type of objects to fetch
	 * @return {@link QueryBase query} object used to list all objects of given type
	 * @throws DataException if no list query was specified for given type
	 */
	public QueryBase getListQuery(Class<?> type) throws DataException
	{
		QueryDescriptor descriptor = listQueries.get(type.getName());
		if (descriptor == null)
			throw new DataException("factory.list", new Object[] { type });
		QueryBase query = createQuery(descriptor, null);
		QueryExecuteOptions opt = new QueryExecuteOptions();
		opt.setExecMethod(execMethods.get("getListQuery"));
		query.setExecOption(opt);
		return query;
	}

	/**
	 * Gets {@link ChildrenQueryBase query} object to be used for fetching all children objects of given type for object of given parent type 
	 * @param parentType type of parent object whose children needs to be fetched
	 * @param childrenType type of children objects to be fetched
	 * @return {@link ChildrenQueryBase} descendant
	 * @throws DataException if no children query was specified for given combination of parent and children types
	 */
	public ChildrenQueryBase getChildrenQuery(Class<?> parentType, Class<?> childrenType) throws DataException {
		HashMap<String, QueryDescriptor> map = (HashMap<String, QueryDescriptor>) childrenQueries.get(parentType.getName());
		if (map == null)
			throw new DataException("factory.children", new Object[] { parentType, childrenType });
		QueryDescriptor descriptor = map.get(childrenType.getName());
		if (descriptor == null)
			throw new DataException("factory.children", new Object[] { parentType, childrenType });
		QueryExecuteOptions opt = new QueryExecuteOptions();
		opt.setExecMethod(execMethods.get("getChildrenQuery"));
		ChildrenQueryBase query = (ChildrenQueryBase)createQuery(descriptor, null);
		query.setExecOption(opt);
		return query;
	}

	/**
	 * Gets {@link ObjectQueryBase query} object to be used for fetching single object of given type from database
	 * @param type type of object to be fetched
	 * @return {@link ObjectQueryBase} descendant used to fetch single object of given type from database
	 * @throws DataException if no fetch query specified for given type
	 */
	public ObjectQueryBase getFetchQuery(Class<?> type) throws DataException {
		QueryDescriptor descriptor = fetchQueries.get(type.getName());
		if (descriptor == null)
			throw new DataException("factory.fetch", new Object[] { type });
		ObjectQueryBase query = (ObjectQueryBase)createQuery(descriptor, null);
		QueryExecuteOptions opt = new QueryExecuteOptions();
		opt.setExecMethod(execMethods.get("getFetchQuery"));
		query.setExecOption(opt);
		return query;
		
	}

	/**
	 * Gets {@link SaveQueryBase query} object to be used to save given {@link DataObject} 
	 * descendant in database.<br>
	 * NOTE: this method could return different {@link SaveQueryBase} implementation for
	 * objects of same type if configuration file defines 'specific' clauses for corresponding
	 * data type.
	 * @param object object to be saved
	 * @return {@link SaveQueryBase query} descendant to be used to save given object
	 * @throws DataException If no save query specified for given type of objects
	 */
	public SaveQueryBase getSaveQuery(DataObject object) throws DataException {
		Class<?> type = object.getClass();
		QueryDescriptor descriptor = saveQueries.get(type.getName());
		if (descriptor == null)
			throw new DataException("factory.store", new Object[] { type });
		SaveQueryBase query = (SaveQueryBase)createQuery(descriptor, object);
		QueryExecuteOptions opt = new QueryExecuteOptions();
		opt.setExecMethod(execMethods.get("getSaveQuery"));
		query.setExecOption(opt);
		return query;
	}

	/**
	 * Gets {@link ObjectQueryBase query} object to be used for deletion of object with given identifier.<br>
	 * NOTE: this method could return different {@link ObjectQueryBase} implementation for
	 * objects of same type if configuration file defines 'specific' clauses for corresponding
	 * data type.
	 * @param id identifier of object to be deleted
	 * @return {@link ObjectQueryBase} descendant configured as delete query for given type of objects
	 * @throws DataException if no delete query is specified for given type of objects
	 */
	public ObjectQueryBase getDeleteQuery(ObjectId id) throws DataException
	{
		Class<?> type = id.getType();
		QueryDescriptor descriptor = deleteQueries.get(type.getName());
		if (descriptor == null)
			throw new DataException("factory.delete", new Object[] { type });
		ObjectQueryBase query = (ObjectQueryBase)createQuery(descriptor, fetchObject(id));
		QueryExecuteOptions opt = new QueryExecuteOptions();
		opt.setExecMethod(execMethods.get("getDeleteQuery"));
		query.setExecOption(opt);
		return query;
	}
	
	/**
	 * Gets query object specified for processing of given action.
	 * NOTE: this method could return different {@link ActionQueryBase} implementation for
	 * action objects of same type if configuration file defines 'specific' clauses for corresponding
	 * action.
	 * @param action {@link Action} to be processed
	 * @return query object intended for processing of given {@link Action}
	 * @throws DataException if no action query is specified for actions of given type
	 */
	public ActionQueryBase getActionQuery(Action action) throws DataException
	{
		Class<?> type = action.getClass();
		QueryDescriptor descriptor = actionQueries.get(type.getName());
		if (descriptor == null)
			throw new DataException("factory.action", new Object[] { type.getName() });
		/*Object obj = action;
		if (action instanceof ObjectAction)
			obj = fetchObject(((ObjectAction) action).getObjectId());*/
		ActionQueryBase query = (ActionQueryBase) createQuery(descriptor, action/*obj*/);
		QueryExecuteOptions opt = new QueryExecuteOptions();
		opt.setExecMethod(execMethods.get("getActionQuery"));
		query.setExecOption(opt);
		return query;
	}

	/**
	 * Gets query object intended for processing actions of given type.<br>
	 * NOTE: this method ignores all 'specific' clauses if any exists.
	 * @param type type of action to be processed. Should be a {@link Action} descendant.
	 * @return query object intended for processing of actions of given type
	 * @throws DataException if no action query is specified for actions of given type
	 */
	public ActionQueryBase getActionQuery(Class<?> type) throws DataException
	{
		QueryDescriptor descriptor = actionQueries.get(type.getName());
		if (descriptor == null)
			throw new DataException("factory.action", new Object[] { type.getName() });
		ActionQueryBase query = (ActionQueryBase) createQuery(descriptor, null);
		QueryExecuteOptions opt = new QueryExecuteOptions();
		opt.setExecMethod(execMethods.get("getSimpleActionQuery"));
		query.setExecOption(opt);
		return query;
	}

	/**
	 * Gets access checker to be used by query used to save given object
	 * NOTE: this method could return different {@link AccessCheckerBase} implementation for
	 * different objects of same type if configuration file defines 'specific' clauses for corresponding
	 * save query. 
	 * @param object object to be saved
	 * @return access checker to be used by save query
	 * @throws DataException if no save query defined for given object type
	 */
	public AccessCheckerBase getObjectAccessChecker(DataObject object) throws DataException
	{
		Class<?> type = object.getClass();
		QueryDescriptor descriptor = saveQueries.get(type.getName());
		if (descriptor == null)
			throw new DataException("factory.store", new Object[] { type.getName() });
		ProcessorDescriptor pd = descriptor.getAccessChecker();
		if (pd == null)
			return null;
		return (AccessCheckerBase) createProcessor(pd, object).get(ATTR_CLASS);
	}
	
	/**
	 * Gets access checker to be used by query used to save objects of given type.
	 * NOTE: this method ignores all 'specific' clauses if any exists.
	 * @param type type object to be saved
	 * @return access checker assigned for save query
	 * @throws DataException if no save query defined for given object type
	 */
	public AccessCheckerBase getObjectAccessChecker(Class<?> type) throws DataException
	{
		QueryDescriptor descriptor = saveQueries.get(type.getName());
		if (descriptor == null)
			throw new DataException("factory.store", new Object[] { type.getName() });
		ProcessorDescriptor pd = descriptor.getAccessChecker();
		if (pd == null)
			return null;
		return (AccessCheckerBase) createProcessor(pd, null).get(ATTR_CLASS);
	}
	
	/**
	 * Gets access checker to be used by query used to process actions of given type.
	 * NOTE: this method ignores all 'specific' clauses if any exists.
	 * @param type type �� {@link Action} to be processed
	 * @return access checker assigned for action query
	 * @throws DataException if no save query defined for given {@link Action} type
	 */
	public AccessCheckerBase getActionAccessChecker(Class<?> type) throws DataException
	{
		QueryDescriptor descriptor = actionQueries.get(type.getName());
		if (descriptor == null)
			throw new DataException("factory.action", new Object[] { type.getName() });
		ProcessorDescriptor pd = descriptor.getAccessChecker();
		if (pd == null)
			return null;
		return (AccessCheckerBase) createProcessor(pd, null).get(ATTR_CLASS);
	}
	
	/**
	 * Sets bean factory.
	 * This method is called automatically by spring framework.
	 */
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException
	{
		this.beanFactory = beanFactory;
	}

	public boolean isUseCache() {
		return useCache;
	}

	public void setUseCache(boolean useCache) {
		this.useCache = useCache;
	}
/*
	public boolean isUseAsynchMode() {
		return ParametrizedProcessor.EnableAsync;
	}
	

	public void setUseAsynchMode(boolean flag) {
		ParametrizedProcessor.EnableAsync = flag;
		logger.info("asynch mode is " + (ParametrizedProcessor.EnableAsync ? "on" : "off") );
	}
*/
	private QueryBase createQuery(QueryDescriptor qd, Object obj) throws DataException
	{
		ProcessorDescriptor pd = qd.getQuery();		
		if (pd == null)
			throw new DataException("factory.config");
		Map<String, Object> res = createProcessor(pd, obj);
		QueryBase query = (QueryBase) res.get(ATTR_CLASS);
		if (query == null)
			throw new DataException("factory.config");
		Boolean async = (Boolean)res.get(ATTR_ASYNC);
		String policy = (String) res.get(ATTR_POLICY);
		Integer prior  = (Integer) res.get(ATTR_PRIORITY);
		if (qd.getCacheTime() > 0)
			query = proxyQuery(query, qd.getCacheTime());
		pd = qd.getAccessChecker();
		if (pd != null) {
			query.setAccessChecker((AccessCheckerBase) createProcessor(pd, obj).get(ATTR_CLASS));
		}

		for (ProcessorDescriptor processorDescriptor : qd.getValidators()) {
			pd = processorDescriptor;
			res = createProcessor(pd, obj);
			ProcessorBase proc = (ProcessorBase) res.get(ATTR_CLASS);
			if (proc != null) {
				query.addValidator(proc);
			}
			//TODO: ����-���� �����
			if (res.get(ATTR_ASYNC) != null && async == null)
				async = (Boolean) res.get(ATTR_ASYNC);
			if (res.get(ATTR_POLICY) != null && policy == null)
				policy = (String) res.get(ATTR_POLICY);
			if (res.get(ATTR_PRIORITY) != null && prior == null)
				prior = (Integer) res.get(ATTR_PRIORITY);
		}

		for (ProcessorDescriptor processorDescriptor : qd.getPreProcessors()) {
			pd = processorDescriptor;
			// if (logger.isTraceEnabled())	logger.trace("\t\t pre-processor desc " + pd.getClassName()+ " order = " + pd.getOrder());
			res = createProcessor(pd, obj);
			ProcessorBase proc = (ProcessorBase) res.get(ATTR_CLASS);
			if (proc != null) {
				if (pd.getOrder() < 0) {
					query.addPrepareProcessor(proc);
				} else {
					query.addPreProcessor(proc);
				}
			}
			//TODO: ����-���� �����
			if (res.get(ATTR_ASYNC) != null && async == null)
				async = (Boolean) res.get(ATTR_ASYNC);
			if (res.get(ATTR_POLICY) != null && policy == null)
				policy = (String) res.get(ATTR_POLICY);
			if (res.get(ATTR_PRIORITY) != null && prior == null)
				prior = (Integer) res.get(ATTR_PRIORITY);
		}
		for (ProcessorDescriptor processorDescriptor : qd.getPostProcessors()) {
			pd = processorDescriptor;
			// if (logger.isTraceEnabled()) logger.trace("\t\t post-processor desc " + pd.getClassName()+ " order = " + pd.getOrder());
			res = createProcessor(pd, obj);
			ProcessorBase proc = (ProcessorBase) res.get(ATTR_CLASS);
			if (proc != null) {
				query.addPostProcessor(proc);
			}
			//TODO: ����-���� �����
			if (res.get(ATTR_ASYNC) != null && async == null)
				async = (Boolean) res.get(ATTR_ASYNC);
			if (res.get(ATTR_POLICY) != null && policy == null)
				policy = (String) res.get(ATTR_POLICY);
			if (res.get(ATTR_PRIORITY) != null && prior == null)
				prior = (Integer) res.get(ATTR_PRIORITY);
		}
		if (async != null)
			query.setAsync(async);
		if (policy != null)
			query.setAsyncPolicyName(policy);
		if (prior != null)
			query.setPriority(prior);
		
		if ( logger.isTraceEnabled() 
			|| (
					logger.isDebugEnabled() 
					&& ( 
						(obj instanceof DataObject)
						|| (obj instanceof ObjectId)
						|| (obj instanceof ObjectAction)
					)
				)
			) 
		{
			final StringBuffer sb = new StringBuffer();
			sb.append( "prepared createQuery( queryDesc=")
				.append( qd.getQuery() != null ? qd.getQuery().getClassName() : qd.getClass() )
				.append(", obj=");
			getObjectInfo( sb, obj);
			sb.append(") \r\n");
			if (query.getPreProcessors().size() > 0) {
				sb.append("\t pre-processors ");
				traceProcessors( sb, query.getPreProcessors());
			}
			if (query.getPostProcessors().size() > 0) {
				sb.append("\t post-processors ");
				traceProcessors( sb, query.getPostProcessors());
			}
			logger.debug(sb.toString());
		}

		return query;
	}
	
	/**
	 * @param buf to append
	 * @param obj that appended
	 */
	static void getObjectInfo(StringBuffer buf, Object obj) {
		if (obj == null) {
			buf.append("NULL");
			return;
		}
		buf.append( "\n\t class=").append(obj.getClass());
		if (obj instanceof DataObject) {
			final DataObject dat = (DataObject) obj;
			buf.append(",\n\t datId=").append( dat.getId());
		} else if (obj instanceof ObjectId) {
			final ObjectId id = (ObjectId) obj;
			buf.append(",\n\t id=").append(id);
		} else if (obj instanceof ObjectAction) { 
			final ObjectAction action = (ObjectAction) obj;
			buf.append(",\n\t action objId=").append(action.getObjectId());
		}
		buf.append(",\n\t toString=").append(obj);
	}

	/**
	 * @param buf to append
	 * @param list of processors
	 */
	private void traceProcessors(StringBuffer buf, List<ProcessorBase> list) {
		buf.append("\t*count=").append(list.size()).append("\r\n");
		for (int i = 0; i < list.size(); i++) {
			final ProcessorBase pb = list.get(i);
			buf.append("\t\t[p").append(i+1).append("] ").append(pb.getClass());

			// ����� ���������� ����������� (���� ���� ����� getParameters)
			// pb.getClass().getDeclaredMethod("getParameters", parameterTypes)
			try {
				@SuppressWarnings("unchecked")
				final Map<String, String> args = (Map<String, String>) PropertyUtils.getProperty(pb, PROP_NAME_PARAMETERS);
				buf.append( MessageFormat.format(
						"\t  args via property processor.''{0}'': {1}",
						PROP_NAME_PARAMETERS, (args == null ? "NULL" : String.valueOf(args.size()) )
					));
				if (args != null) {
					int iarg = 0;
					for (Map.Entry<String, String> entry: args.entrySet() ) {
						iarg++;
						buf.append(MessageFormat.format(
								"\n\t\t\t [arg{0}] {1}={2} ",
								iarg,
								entry.getKey(), entry.getValue()
						));
					}
				}
			} catch (Exception ex) {
				final String info = ex.getMessage();
				if (info != null && info.contains("Unknown property"))
					buf.append(MessageFormat.format("\t  args inaccessible via ''{0}''", PROP_NAME_PARAMETERS));
				else
					buf.append(MessageFormat.format(
							"\t (*) fail to get processor args via ''{0}'': {1}",
							PROP_NAME_PARAMETERS, info));
			}
			buf.append("\r\n");
		}
	}

	private QueryBase proxyQuery(QueryBase query, int lifeTime) {
		if (!useCache)
			return query;
		QueryBase proxy;
		if (query instanceof ChildrenQueryBase)
			proxy = new CachingChildrenQuery((ChildrenQueryBase) query, lifeTime);
		else if (query instanceof ObjectQueryBase)
			proxy = new CachingObjectQuery((ObjectQueryBase) query, lifeTime);
		else
			proxy = new CachingQuery(query, lifeTime);
		proxy.setBeanFactory(beanFactory);
		return proxy;
	}
	
	private Map<String, Object> createProcessor(ProcessorDescriptor descriptor, Object obj) throws DataException
	{
		descriptor = descriptor.findApplicable(obj);
		Map<String, Object> result = new HashMap<String, Object>();
		String className = descriptor.getClassName();
		if (className == null) {
			return result;
		}
		Object processor;
		try {
			processor = Class.forName(className).newInstance();
		} catch (Exception e) {
			logger.error("Error instantiating " + className, e);
			throw new DataException("factory.create", new Object[] { className });
		}
		if (processor instanceof BeanFactoryAware)
			((BeanFactoryAware) processor).setBeanFactory(beanFactory);
		if (processor instanceof Parametrized && descriptor.getParameters() != null)
			for (Map.Entry<String, String> param : descriptor.getParameters().entrySet()) {
				((Parametrized) processor).setParameter(param.getKey(), param.getValue());
			}
		result.put(ATTR_ASYNC, descriptor.isAsync());
		result.put(ATTR_POLICY, descriptor.getPolicyName());
		result.put(ATTR_PRIORITY, descriptor.getPriority());
		result.put(ATTR_CLASS, processor);
		return result;
	}
	
	private DataObject fetchObject(ObjectId id) throws DataException {
		ObjectQueryBase fetchQuery = getFetchQuery(id.getType());
		fetchQuery.setAccessChecker(null);
		fetchQuery.setId(id);
		Database db = (Database) beanFactory.getBean(DataServiceBean.BEAN_DATABASE);
		UserData user = new UserData();
		user.setPerson(db.resolveUser(Database.SYSTEM_USER));
		user.setAddress("internal");
		return (DataObject) db.executeQuery(user, fetchQuery);
	}
	
	class Initializer {
		private XPathEvaluator xpath = new XPathEvaluatorImpl();
		
		private String pkgModel = PKG_MODEL;
		private String pkgAction = PKG_ACTION;
		private String pkgQuery = PKG_QUERY;
		private String pkgAccess = PKG_ACCESS;
		private String pkgProcess = PKG_QUERY;
		private String pkgSelector = PKG_SELECTOR;
		
		private ArrayList<Selector> specifics = new ArrayList<Selector>();
		
		public void parse(InputStream config) throws SAXException, IOException, ParserConfigurationException, DataException {
			Document queries = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(config);
			
			String pkg = ((XPathResult) xpath.evaluate("/" + TAG_ROOT + "/@" + ATTR_PKG_MODEL,
					queries, null, XPathResult.STRING_TYPE, null)).getStringValue();
			if (pkg != null && pkg.length() > 0)
				pkgModel = pkg;
			pkg = ((XPathResult) xpath.evaluate("/" + TAG_ROOT + "/@" + ATTR_PKG_ACTION,
					queries, null, XPathResult.STRING_TYPE, null)).getStringValue();
			if (pkg != null && pkg.length() > 0)
				pkgAction = pkg;
			pkg = ((XPathResult) xpath.evaluate("/" + TAG_ROOT + "/@" + ATTR_PKG_QUERY,
					queries, null, XPathResult.STRING_TYPE, null)).getStringValue();
			if (pkg != null && pkg.length() > 0)
				pkgQuery = pkg;
			pkg = ((XPathResult) xpath.evaluate("/" + TAG_ROOT + "/@" + ATTR_PKG_ACCESS,
					queries, null, XPathResult.STRING_TYPE, null)).getStringValue();
			if (pkg != null && pkg.length() > 0)
				pkgAccess = pkg;
			pkg = ((XPathResult) xpath.evaluate("/" + TAG_ROOT + "/@" + ATTR_PKG_PROCESS,
					queries, null, XPathResult.STRING_TYPE, null)).getStringValue();
			if (pkg != null && pkg.length() > 0)
				pkgProcess = pkg;
			pkg = ((XPathResult) xpath.evaluate("/" + TAG_ROOT + "/@" + ATTR_PKG_SELECTOR,
					queries, null, XPathResult.STRING_TYPE, null)).getStringValue();
			if (pkg != null && pkg.length() > 0)
				pkgSelector = pkg;
			
			XPathResult result = (XPathResult) xpath.evaluate("/" + TAG_ROOT + "/" + TAG_OBJECT + "/" + TAG_LIST_QUERY,
					queries, null, XPathResult.UNORDERED_NODE_ITERATOR_TYPE, null);
			for (Node item = result.iterateNext(); item != null; item = result.iterateNext()) {
				String objType = getTypeName(item.getParentNode(), pkgModel);
				QueryDescriptor descriptor = listQueries.get(objType);
				if (descriptor == null)
					listQueries.put(objType, descriptor = new QueryDescriptor());
				readQuery(item, descriptor);
			}
			result = (XPathResult) xpath.evaluate("/" + TAG_ROOT + "/" + TAG_OBJECT + "/" + TAG_CHILDREN_QUERY,
					queries, null, XPathResult.UNORDERED_NODE_ITERATOR_TYPE, null);
			for (Node item = result.iterateNext(); item != null; item = result.iterateNext()) {
				String objType = getTypeName(item.getParentNode(), pkgModel);
				String childType = getTypeName(item, pkgModel);
				HashMap<String, QueryDescriptor> map = (HashMap<String, QueryDescriptor>) childrenQueries.get(objType);
				if (map == null)
					childrenQueries.put(objType, map = new HashMap<String, QueryDescriptor>());
				QueryDescriptor descriptor = map.get(childType);
				if (descriptor == null)
					map.put(childType, descriptor = new QueryDescriptor());
				readQuery(item, descriptor);
			}
			result = (XPathResult) xpath.evaluate("/" + TAG_ROOT + "/" + TAG_OBJECT + "/" + TAG_GET_QUERY,
					queries, null, XPathResult.UNORDERED_NODE_ITERATOR_TYPE, null);
			for (Node item = result.iterateNext(); item != null; item = result.iterateNext()) {
				String objType = getTypeName(item.getParentNode(), pkgModel);
				QueryDescriptor descriptor = fetchQueries.get(objType);
				if (descriptor == null)
					fetchQueries.put(objType, descriptor = new QueryDescriptor());
				readQuery(item, descriptor);
			}
			result = (XPathResult) xpath.evaluate("/" + TAG_ROOT + "/" + TAG_OBJECT + "/" + TAG_SAVE_QUERY,
					queries, null, XPathResult.UNORDERED_NODE_ITERATOR_TYPE, null);
			for (Node item = result.iterateNext(); item != null; item = result.iterateNext()) {
				String objType = getTypeName(item.getParentNode(), pkgModel);
				QueryDescriptor descriptor = saveQueries.get(objType);
				if (descriptor == null)
					saveQueries.put(objType, descriptor = new QueryDescriptor());
				readQuery(item, descriptor);
			}
			result = (XPathResult) xpath.evaluate("/" + TAG_ROOT + "/" + TAG_OBJECT + "/" + TAG_DELETE_QUERY,
					queries, null, XPathResult.UNORDERED_NODE_ITERATOR_TYPE, null);
			for (Node item = result.iterateNext(); item != null; item = result.iterateNext()) {
				String objType = getTypeName(item.getParentNode(), pkgModel);
				QueryDescriptor descriptor = deleteQueries.get(objType);
				if (descriptor == null)
					deleteQueries.put(objType, descriptor = new QueryDescriptor());
				readQuery(item, descriptor);
			}
			result = (XPathResult) xpath.evaluate("/" + TAG_ROOT + "/" + TAG_ACTION,
					queries, null, XPathResult.UNORDERED_NODE_ITERATOR_TYPE, null);
			for (Node item = result.iterateNext(); item != null; item = result.iterateNext()) {
				String actionType = getTypeName(item, pkgAction);
				QueryDescriptor descriptor = actionQueries.get(actionType);
				if (descriptor == null)
					actionQueries.put(actionType, descriptor = new QueryDescriptor());
				readQuery(item, descriptor);
			}
		}
		
		private void readQuery(Node tag, QueryDescriptor descriptor) throws DataException {
			XPathResult result = (XPathResult) xpath.evaluate(TAG_QUERY,
					tag, null, XPathResult.ORDERED_NODE_ITERATOR_TYPE, null);
			for (Node item = result.iterateNext(); item != null; item = result.iterateNext()) {
				ProcessorDescriptor pd = descriptor.getQuery();
				if (pd == null)
					descriptor.setQuery(pd = new ProcessorDescriptor());
				for (Selector sel : specifics) {
					ProcessorDescriptor spec = pd.getSpecific(sel);
					if (spec == null)
						pd.addSpecific(sel, spec = new ProcessorDescriptor());
					pd = spec;
				}
				if (pd.getClassName() != null)
					throw new DataException("factory.config.dupquery",
							new Object[] { ((Element) tag).getAttribute(ATTR_TYPE) });
				pd.setClassName(getClassName(item, pkgQuery));
				processParameters(item, pd);
			}
			result = (XPathResult) xpath.evaluate(TAG_ACCESS,
					tag, null, XPathResult.ORDERED_NODE_ITERATOR_TYPE, null);
			for (Node item = result.iterateNext(); item != null; item = result.iterateNext()) {
				ProcessorDescriptor pd = descriptor.getAccessChecker();
				if (pd == null)
					descriptor.setAccessChecker(pd = new ProcessorDescriptor());
				for (Selector sel : specifics) {
					ProcessorDescriptor spec = pd.getSpecific(sel);
					if (spec == null)
						pd.addSpecific(sel, spec = new ProcessorDescriptor());
					pd = spec;
				}
				if (pd.getClassName() != null)
					throw new DataException("factory.config.dupaccess",
							new Object[] { ((Element) tag).getAttribute(ATTR_TYPE) });
				pd.setClassName(getClassName(item, pkgAccess));
				processParameters(item, pd);
			}
			
			result = (XPathResult) xpath.evaluate(TAG_VALIDATOR,
					tag, null, XPathResult.ORDERED_NODE_ITERATOR_TYPE, null);
			for (Node item = result.iterateNext(); item != null; item = result.iterateNext()) {
				ProcessorDescriptor pd = new ProcessorDescriptor();

				if ( ((Element) item).hasAttribute(ATTR_RUNORDER)) {
					final String order = ((XPathResult) xpath.evaluate("@" + ATTR_RUNORDER,
							item, null, XPathResult.STRING_TYPE, null)).getStringValue();
					pd.setRunOrder( Integer.parseInt(order.trim()) );
				}

				descriptor.addValidator(pd);
				for (Selector sel : specifics) {
					ProcessorDescriptor spec = new ProcessorDescriptor();
					pd.addSpecific(sel, spec);
					pd = spec;
				}
				pd.setClassName(getClassName(item, pkgProcess));
				processParameters(item, pd);
			}
			
			result = (XPathResult) xpath.evaluate(TAG_PREPROCESS,
					tag, null, XPathResult.ORDERED_NODE_ITERATOR_TYPE, null);
			for (Node item = result.iterateNext(); item != null; item = result.iterateNext()) {
				ProcessorDescriptor pd = new ProcessorDescriptor();

				if ( ((Element) item).hasAttribute(ATTR_RUNORDER)) {
					final String order = ((XPathResult) xpath.evaluate("@" + ATTR_RUNORDER,
							item, null, XPathResult.STRING_TYPE, null)).getStringValue();
					pd.setRunOrder( Integer.parseInt(order.trim()) );
				}

				descriptor.addPreProcessor(pd);
				for (Selector sel : specifics) {
					ProcessorDescriptor spec = new ProcessorDescriptor();
					pd.addSpecific(sel, spec);
					pd = spec;
				}
				pd.setClassName(getClassName(item, pkgProcess));
				processParameters(item, pd);
			}
			result = (XPathResult) xpath.evaluate(TAG_POSTPROCESS,
					tag, null, XPathResult.ORDERED_NODE_ITERATOR_TYPE, null);
			for (Node item = result.iterateNext(); item != null; item = result.iterateNext()) {
				ProcessorDescriptor pd = new ProcessorDescriptor();

				if ( ((Element) item).hasAttribute(ATTR_RUNORDER)) {
					final String order = ((XPathResult) xpath.evaluate("@" + ATTR_RUNORDER,
							item, null, XPathResult.STRING_TYPE, null)).getStringValue();
					pd.setRunOrder( Integer.parseInt(order.trim()) );
				}

				descriptor.addPostProcessor(pd);
				for (Selector sel : specifics) {
					ProcessorDescriptor spec = new ProcessorDescriptor();
					pd.addSpecific(sel, spec);
					pd = spec;
				}
				pd.setClassName(getClassName(item, pkgProcess));
				processParameters(item, pd);
			}
			result = (XPathResult) xpath.evaluate(TAG_SPECIFIC,
					tag, null, XPathResult.ORDERED_NODE_ITERATOR_TYPE, null);
			for (Node item = result.iterateNext(); item != null; item = result.iterateNext()) {
				Selector selector;
				if (((Element) item).hasAttribute(ATTR_CLASS)) {
					String className = getClassName(item, pkgSelector);
					try {
						selector = (Selector) Class.forName(className).newInstance();
					} catch (Exception e) {
						throw new DataException("factory.create", new Object[] { className });
					}
					
					if (((Element) item).hasAttribute(ATTR_ASYNC)) {
						String value = ((Element) item).getAttribute(ATTR_ASYNC);
						((Asynchronous) selector).setAsync(StrUtils.stringToBool(value, false));
						checkAsyncCollision(descriptor, selector);
					}
					
					if (((Element) item).hasAttribute(ATTR_POLICY)) {
						String value = ((Element) item).getAttribute(ATTR_POLICY).trim();
						((Asynchronous) selector).setPolicyName(value);
					}
					
					if (((Element) item).hasAttribute(ATTR_PRIORITY)) {
						String value = ((Element) item).getAttribute(ATTR_PRIORITY).trim();
						((Asynchronous) selector).setPriority(Integer.valueOf(value));
					}
				} else if (((Element) item).hasAttribute(ATTR_SELECTOR_BEAN)) {
					String selectorBeanName = ((Element) item).getAttribute(ATTR_SELECTOR_BEAN);
					if (selectorBeanName == null || selectorBeanName.length() == 0) {
						throw new DataException("factory.config.specific.noproperty",
								new Object[] {ATTR_SELECTOR_BEAN});
					}
					try {
						selector = (Selector) beanFactory.getBean(selectorBeanName);
					} catch (BeansException ex) {
						throw new DataException("factory.config.specific.nobean",
								new Object[] {selectorBeanName}, ex);
					}
				} else {
					String propName = ((XPathResult) xpath.evaluate("@" + ATTR_PROPERTY,
							item, null, XPathResult.STRING_TYPE, null)).getStringValue();
					if (propName == null || propName.length() == 0)
						throw new DataException("factory.config.specific.noproperty",
								new Object[] { ((Element) tag).getAttribute(ATTR_TYPE) });
					String propValue = ((XPathResult) xpath.evaluate("@" + ATTR_VALUE,
							item, null, XPathResult.STRING_TYPE, null)).getStringValue();
					if (propValue == null)
						throw new DataException("factory.config.specific.nopropvalue",
								new Object[] { ((Element) tag).getAttribute(ATTR_TYPE), propName });
					selector = new PropertySelector(propName, propValue);

					// �������� (default=���������) ...
					final XPathResult oper = (XPathResult) 
							xpath.evaluate("@" + ATTR_EQUALS, item, null, XPathResult.STRING_TYPE, null);
					if (oper != null)
						((PropertySelector) selector).setOperEquals(StrUtils.stringToBool(oper.getStringValue(), true));

					if (((Element) item).hasAttribute(ATTR_LOAD)) {
						String value = ((Element) item).getAttribute(ATTR_LOAD);
						((PropertySelector) selector).setLoad(StrUtils.stringToBool(value, false));
					}
					
					if (((Element) item).hasAttribute(ATTR_ASYNC)) {
						String value = ((Element) item).getAttribute(ATTR_ASYNC);
						((PropertySelector) selector).setAsync(StrUtils.stringToBool(value, false));
						checkAsyncCollision(descriptor, selector);
					}
					
					if (((Element) item).hasAttribute(ATTR_POLICY)) {
						String value = ((Element) item).getAttribute(ATTR_POLICY).trim();
						((PropertySelector) selector).setPolicyName(value);
						//checkAsyncCollision(descriptor, selector);
					}
					
					if (((Element) item).hasAttribute(ATTR_PRIORITY)) {
						String value = ((Element) item).getAttribute(ATTR_PRIORITY).trim();
						((PropertySelector) selector).setPriority(Integer.valueOf(value));
					}
				}
				if (selector instanceof BeanFactoryAware)
					((BeanFactoryAware) selector).setBeanFactory(beanFactory);
				
				specifics.add(selector); 
				readQuery(item, descriptor);
				specifics.remove(specifics.size() - 1);
			}
			//result = (XPathResult) xpath.evaluate("./@" + ATTR_CACHE,
			//		tag, null, XPathResult.NUMBER_TYPE, null);
			if (((Element) tag).hasAttribute(ATTR_CACHE)) {
				String value = ((Element) tag).getAttribute(ATTR_CACHE);
				try {
					descriptor.setCacheTime(Integer.parseInt(value));
				} catch (NumberFormatException e) {
					logger.warn("Illegal cache time value: " + value + "; ignored");
				}
			}
		}

		private void checkAsyncCollision(QueryDescriptor qd, Selector selector) throws DataException {
			if (qd.getQuery() != null && qd.getQuery().getSpecific(selector) != null) {
				if (selector instanceof Asynchronous && qd.getQuery().getSpecific(selector).isAsync() != ((Asynchronous)selector).isAsync()) {
					throw new DataException("factory.config.specific.dubasync");
				}
			}
			List<List<ProcessorDescriptor>> allProcessors = new ArrayList<List<ProcessorDescriptor>>();
			allProcessors.add(qd.getValidators());
			allProcessors.add(qd.getPreProcessors());
			allProcessors.add(qd.getPostProcessors());
			for (List<ProcessorDescriptor> processors : allProcessors) {
				for (ProcessorDescriptor pd : processors) {
					if(selector instanceof Asynchronous && pd.getSpecific(selector) != null && pd.getSpecific(selector).isAsync() != ((Asynchronous)selector).isAsync()) {
						throw new DataException("factory.config.specific.dubasync");
					}
				}
			}
		}
		
		private String getClassName(Node tag, String defPackage) throws DataException {
			String pkg = ((XPathResult) xpath.evaluate("@" + ATTR_PACKAGE,
					tag, null, XPathResult.STRING_TYPE, null)).getStringValue();
			if (pkg == null || pkg.length() == 0)
				pkg = defPackage;
			String name = ((XPathResult) xpath.evaluate("@" + ATTR_CLASS,
					tag, null, XPathResult.STRING_TYPE, null)).getStringValue();
			if (name == null || name.length() == 0)
				name = ((XPathResult) xpath.evaluate("text()",
						tag, null, XPathResult.STRING_TYPE, null)).getStringValue();
			if (name == null || name.length() == 0)
				throw new DataException("factory.config.noclass");
			return pkg + "." + name;
		}

		private String getTypeName(Node tag, String defPackage) throws DataException {
			String pkg = ((XPathResult) xpath.evaluate("@" + ATTR_PACKAGE,
					tag, null, XPathResult.STRING_TYPE, null)).getStringValue();
			if (pkg == null || pkg.length() == 0)
				pkg = defPackage;
			String name = ((XPathResult) xpath.evaluate("@" + ATTR_TYPE,
					tag, null, XPathResult.STRING_TYPE, null)).getStringValue();
			if (name == null || name.length() == 0)
				throw new DataException("factory.config.notype");
			return pkg + "." + name;
		}
		
		private void processParameters(Node tag, ProcessorDescriptor pd) {
			XPathResult params = (XPathResult) xpath.evaluate(TAG_PARAMETER,
					tag, null, XPathResult.ORDERED_NODE_ITERATOR_TYPE, null);
			for (Node param = params.iterateNext(); param != null; param = params.iterateNext()) {
				String name = ((XPathResult) xpath.evaluate("@" + ATTR_NAME,
						param, null, XPathResult.STRING_TYPE, null)).getStringValue();
				String value = ((XPathResult) xpath.evaluate("@" + ATTR_VALUE,
						param, null, XPathResult.STRING_TYPE, null)).getStringValue();
				if (value == null)
					value = ((XPathResult) xpath.evaluate("text()",
							param, null, XPathResult.STRING_TYPE, null)).getStringValue();
				pd.addParameter(name, value);
			}
		}
	}
}
