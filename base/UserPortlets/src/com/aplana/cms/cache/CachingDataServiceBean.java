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
package com.aplana.cms.cache;

import com.aplana.cms.ContentIds;
import com.aplana.cms.ContentRequest;
import com.aplana.dbmi.action.Action;
import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.action.ListProject;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.model.*;
import com.aplana.dbmi.service.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Iterator;

public class CachingDataServiceBean extends AsyncDataServiceBean {
	protected Log logger = LogFactory.getLog(getClass());

	public static final Integer DEF_LIFE_TIME = 60;		// 1 minute
	public static final String SEARCH_CACHE_PREFIX = "cache:";
	
	private static final HashMap<ObjectId, Integer> templates = new HashMap<ObjectId, Integer>();
	private static final HashMap<Object, Integer> search = new HashMap<Object, Integer>();
	static {
		templates.put(ContentIds.TPL_AREA, DEF_LIFE_TIME);
		templates.put(ContentIds.TPL_VIEW, DEF_LIFE_TIME);
		templates.put(ContentIds.TPL_NAVIGATION, DEF_LIFE_TIME);
		templates.put(ContentIds.TPL_DOCLIST, DEF_LIFE_TIME);
		templates.put(ContentIds.TPL_PAGES, DEF_LIFE_TIME);
		CacheConfig config = CacheConfig.getConfig();
		for (Iterator<String> itr = config.enumKeys(CacheConfig.KEY_PREFIX_TEMPLATE); itr.hasNext(); ) {
			String name = itr.next();
			templates.put(ObjectId.predefined(Template.class,
					name.substring(CacheConfig.KEY_PREFIX_TEMPLATE.length())),
					config.getIntValue(name, DEF_LIFE_TIME));
		}
		
		for (Iterator<String> itr = config.enumKeys(CacheConfig.KEY_PREFIX_SEARCH); itr.hasNext(); ) {
			String name = itr.next();
			search.put(name.substring(CacheConfig.KEY_PREFIX_SEARCH.length()),
					config.getIntValue(name, DEF_LIFE_TIME));
		}
		
		search.put(ContentIds.ATTR_CHILDREN.getId(), DEF_LIFE_TIME);
		//CacheConfig config = CacheConfig.getConfig();
		for (Iterator<String> itr = config.enumKeys(CacheConfig.KEY_PREFIX_CHIDREN); itr.hasNext(); ) {
			String name = itr.next();
			name = name.substring(CacheConfig.KEY_PREFIX_CHIDREN.length());
			ObjectId id = ObjectId.predefined(BackLinkAttribute.class, name);
			if (id == null)
				id = ObjectId.predefined(CardLinkAttribute.class, name);
			if (id == null)
				id = ObjectId.predefined(TypedCardLinkAttribute.class, name);
			if (id == null) {
				LogFactory.getLog(CachingDataServiceBean.class).warn("Undefined attribute: " + name);
				continue;
			}
			search.put(id.getId(), config.getIntValue(name, DEF_LIFE_TIME));
		}
	}
	
	public static CachingDataServiceBean createBean(ContentRequest request) {
		CachingDataServiceBean bean = new CachingDataServiceBean();
		bean.setSessionId(request.getSessionId());
		if (request.getSessionAttribute(DataServiceBean.USER_NAME) != null) {
		    bean.setUser(new UserPrincipal((String) request.getSessionAttribute(DataServiceBean.USER_NAME)));
		    bean.setIsDelegation(true);
		    bean.setRealUser(request.getUserPrincipal());
        } else {
            bean.setUser(request.getUserPrincipal());
        }
		bean.setAddress(request.getRemoteAddr());
		return bean;
	}
	
	public <T> T doAction(Action<T> action) throws DataException, ServiceException {
		return doAction(action, ExecuteOption.UNDEFINED); 
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T doAction(Action<T> action, ExecuteOption option) throws DataException, ServiceException{
		if (!isCacheableAction(action)){
			removeActionObjectFromCache(action);
			return super.doAction(action, option);
		}
		try {
			if (logger.isDebugEnabled()) {
				logger.debug(CacheManager.getCacheStats());
			}
			CachedAction cachedAction = new CachedAction(action, option);
			long t1 = System.currentTimeMillis();
			CacheEx cache = CacheManager.getCache();
			long t2 = System.currentTimeMillis();
			if (logger.isDebugEnabled()) {
				logger.debug("Caching: Got CacheEx instance in: " + (t2 - t1) + " ms");
			}
			t1 = System.currentTimeMillis();
			T result = (T)cache.get(cachedAction);
			t2 = System.currentTimeMillis();
			if (logger.isDebugEnabled()) {
				logger.debug("Caching: Got action from cache (" + cachedAction + "). Get time: " + (t2 - t1) + " ms");
				CacheManager.getCache().setGetTime(t2 - t1);
			}
			return result;
		} catch (DataException e) {
			throw e;
		} catch (Exception e) {
			throw new ServiceException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public <T extends DataObject> T getById(ObjectId id) throws DataException, ServiceException {
		if (!isCacheableObject(id))
			return super.getById(id);
		try {	
			if (logger.isDebugEnabled()) {
				logger.debug(CacheManager.getCacheStats());
			}
			long t1 = System.currentTimeMillis();
			CacheEx cache = CacheManager.getCache();
			long t2 = System.currentTimeMillis();
			if (logger.isDebugEnabled()) {
				logger.debug("Caching: Got CacheEx instance in: " + (t2 - t1) + " ms");
			}
			t1 = System.currentTimeMillis();
			T obj = (T)cache.get(new CachedObject(id));
			t2 = System.currentTimeMillis();
			if (logger.isDebugEnabled()) {
				logger.debug("Caching: Got object from cache, id = " + id.getId() + ". Get time: " + (t2 - t1) + " ms");
				CacheManager.getCache().setGetTime(t2 - t1);
			}
			return obj;
		} catch (DataException e) {
			throw e;
		} catch (Exception e) {
			throw new ServiceException(e);
		}
	}
	
	public ObjectId saveObject(DataObject obj) throws DataException, ServiceException {
		if (obj.getId() != null && isCacheableObject(obj.getId())) {
			if (logger.isDebugEnabled()) {
				logger.debug(CacheManager.getCacheStats());
			}
			long t1 = System.currentTimeMillis();
			CacheEx cache = CacheManager.getCache();
			long t2 = System.currentTimeMillis();
			if (logger.isDebugEnabled()) {
				logger.debug("Caching: Got CacheEx instance in: " + (t2 - t1) + " ms");
			}
			t1 = System.currentTimeMillis();
			cache.remove(new CachedObject(obj.getId()));
			t2 = System.currentTimeMillis();
			if (logger.isDebugEnabled()) {
				logger.debug("Caching: Removed object from cache, id = " + obj.getId().getId() + ". Remove time: " + (t2 - t1) + " ms");
				CacheManager.getCache().setRemoveTime(t2 - t1);
			}
		}
		return super.saveObject(obj);
	}

	protected User getUser() {
		try {
			if (super.getAsyncService() == null)
				throw new IllegalStateException("Error initializing service");
		} catch (ServiceException e) {
			throw new IllegalStateException("Error initializing service", e);
		}
		return super.getUser();
	}
	
	private Object getLinksId(Action<?> action) {
		if (action instanceof ListProject)
			return ((ListProject) action).getAttribute().getId();
		else if (action instanceof Search) {
			String name = ((Search) action).getNameEn();
			if (name != null && name.startsWith(SEARCH_CACHE_PREFIX))
				return name.substring(SEARCH_CACHE_PREFIX.length());
			//System.out.println("***Search: " + name);
		}
		return "";
	}
	
	private void removeActionObjectFromCache(Action<?> action) {
		if(action instanceof ChangeState){
			if (logger.isDebugEnabled()) {
				logger.debug(CacheManager.getCacheStats());
			}
			long t1 = System.currentTimeMillis();
			CacheManager.getCache().remove(new CachedObject(((ChangeState) action).getCard().getId()));
			long t2 = System.currentTimeMillis();
			if (logger.isDebugEnabled()) {
				logger.debug("Caching: Removed action from cache, action = " + action + ". Remove time: " + (t2 - t1) + " ms");
				CacheManager.getCache().setRemoveTime(t2-t1);
			}
		}
	}
	
	private boolean isCacheableAction(Action<?> action) {
		return search.containsKey(getLinksId(action));
	}
	
	private boolean isCacheableObject(ObjectId id) {
		return Card.class.equals(id.getType());
	}
	
	private int getResultLifeTime(Action<?> action) {
		return search.get(getLinksId(action));
	}
	
	private int getObjectLifeTime(DataObject object) {
		if (!(object instanceof Card))
			return 0;
		Card card = (Card) object;
		if (templates.containsKey(card.getTemplate()))
			return templates.get(card.getTemplate());
		return 5;
	}

	private class CachedAction extends AbstractCacheableEx
	{
		private Action<?> action;
		private ExecuteOption option;
		
		protected CachedAction(Action<?> action, ExecuteOption option) {
			super(new CacheId(action, getUser()));
			this.action = action;
			this.option=option;
			
		}

		public Object getValue() throws Exception {
		    Object result;
			if (logger.isDebugEnabled()) {
				logger.debug("Caching: Call AsyncDataService.doAction(" + action + ")");
			}
		    long t1 = System.currentTimeMillis();
		    result = getAsyncService().doAction(getUser(), getIsDelegation(), getRealUser(), action, option,  sessionId);
		    long t2 = System.currentTimeMillis();
			if (logger.isDebugEnabled()) {
				logger.debug("Caching: Got action value (" + this + ") from db, action = " + action + ". Get time: " + (t2 - t1) + " ms");
			}
			setExpiration(getResultLifeTime(action));
			return result;
		}
	}
	
	private class CachedObject extends AbstractCacheableEx
	{
		private ObjectId objectId;
		
		protected CachedObject(ObjectId id) {
			super(new CacheId(id, getUser()));
			this.objectId = id;
		}

		public Object getValue() throws Exception {
			if (logger.isDebugEnabled()) {
				logger.debug("Caching: Call AsyncDataService.getById(" + objectId.getId() + ")");
			}
			long t1 = System.currentTimeMillis();
			DataObject result = getAsyncService().getById(getUser(), getIsDelegation(), getRealUser(), objectId, sessionId);
			long t2 = System.currentTimeMillis();
			if (logger.isDebugEnabled()) {
				logger.debug("Caching: Got object value (" + this + ") from db, objectId = " + objectId.getId() + ". Get time: " + (t2 - t1) + " ms");
			}
			setExpiration(getObjectLifeTime(result));
			return result;
		}
	}
}
