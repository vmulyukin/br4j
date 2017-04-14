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
package com.aplana.module.optimizesolr;

import java.util.Map;

import javax.ejb.CreateException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.access.ContextSingletonBeanFactoryLocator;

import com.aplana.dbmi.storage.search.SearchService;


/**
 * Bean implementation class for Enterprise Bean: OptimizeSolr
 */
public class OptimizeSolr
	extends org.springframework.ejb.support.AbstractStatelessSessionBean
	implements javax.ejb.SessionBean
{
	/**
	 * �������� config-���������� ��� ������� ����������� ���������.
	 */
	public static final String PARAM_WAIT_FLUSH = "waitFlush";
	public static final String PARAM_WAIT_SEARCHER = "waitSearcher";

	static final long serialVersionUID = 773344L;

	public Log logger = LogFactory.getLog(getClass());
	
	// ����� �� ���������������...
	private boolean waitFlush = true;
	private boolean waitSearcher = true;
	
	public OptimizeSolr()
	{
		setBeanFactoryLocator(ContextSingletonBeanFactoryLocator.getInstance());
		setBeanFactoryLocatorKey("businessBeanFactory");
	}
	
	@Override
	protected void onEjbCreate() throws CreateException
	{
	}

	public void process(Map<String, String> parameters)
	{
		logger.info("OptimizeSolr: task started");
		final long start_ms = System.currentTimeMillis();
		try {
			setParams( parameters);
			SearchService solrSrvc = (SearchService) getBeanFactory().getBean("solrSearch");
			solrSrvc.optimize(waitFlush, waitSearcher);
		} catch (Exception e) {
			logger.error("User synchronization error", e);
		}
		finally {
			final long duration_ms = System.currentTimeMillis() - start_ms;
			logger.info( String.format( 
					"OptimizeSolr: task completed in %1.3f sec ", 
					 new Object[] { (0.001 * duration_ms) }
				));
		}
	}

	/**
	 * @param parameters
	 */
	protected void setParams(Map<String, String> parameters) {
		
		if (parameters.containsKey(PARAM_WAIT_FLUSH))
			this.waitFlush = Boolean.parseBoolean(parameters.get(PARAM_WAIT_FLUSH));

		if (parameters.containsKey(PARAM_WAIT_SEARCHER))
			this.waitSearcher = Boolean.parseBoolean(parameters.get(PARAM_WAIT_SEARCHER));
	}
}
