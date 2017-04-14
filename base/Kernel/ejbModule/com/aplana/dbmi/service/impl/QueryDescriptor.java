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

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Class representing configuration of single query as it is
 * defined in {@link QueryFactory} configuration file.<br>
 */
public class QueryDescriptor implements Serializable
{
	private static final long serialVersionUID = 3294250442033655993L;
	
	private ProcessorDescriptor query;
	private ProcessorDescriptor accessChecker;
	private List<ProcessorDescriptor> preProcessors = new LinkedList<ProcessorDescriptor>();
	private List<ProcessorDescriptor> postProcessors = new LinkedList<ProcessorDescriptor>();
	private List<ProcessorDescriptor> validators = new LinkedList<ProcessorDescriptor>();
	private int cacheTime = 0;
	
	/**
	 * Gets descriptor of query itself
	 * @return descriptor of query itself
	 */
	public ProcessorDescriptor getQuery() {
		return query;
	}
	
	/**
	 * Gets descriptor of access checker to be used for query
	 * @return descriptor of access checker to be used for query
	 */
	public ProcessorDescriptor getAccessChecker() {
		return accessChecker;
	}

	/**
	 * Gets list of validator descriptors defined for this query
	 * @return list of {@link ProcessorDescriptor} representing 
	 * validators defined for this query
	 */	
	public List<ProcessorDescriptor> getValidators() {
		return validators;
	}
	
	/**
	 * Gets list of pre-processor descriptors defined for this query
	 * @return list of {@link ProcessorDescriptor} representing 
	 * pre-processors defined for this query
	 */	
	public List<ProcessorDescriptor> getPreProcessors() {
		return preProcessors;
	}
	
	/**
	 * Gets list of post-processor descriptors defined for this query
	 * @return list of {@link ProcessorDescriptor} representing 
	 * post-processors defined for this query
	 */
	public List<ProcessorDescriptor> getPostProcessors() {
		return postProcessors;
	}
	
	/**
	 * Sets descriptor of query class
	 * @param query descriptor of query class
	 */
	public void setQuery(ProcessorDescriptor query) {
		this.query = query;
	}
	
	/**
	 * Sets description of access checker to be used by query
	 * @param accessChecker descriptor of {@link AccessCheckerBase access checker} to be used
	 */
	public void setAccessChecker(ProcessorDescriptor accessChecker) {
		this.accessChecker = accessChecker;
	}
	
	/**
	 * Adds validator description to this QueryDescriptor 
	 * @param validator descriptor of validator to be added
	 */
	public void addValidator(ProcessorDescriptor validator) {
		add(validators, validator);
	}
	
	/**
	 * Adds pre-processor description to this QueryDescriptor 
	 * @param preProcessor descriptor of pre-processor to be added
	 */
	public void addPreProcessor(ProcessorDescriptor preProcessor) {
		add(preProcessors, preProcessor);
	}
	
	/**
	 * Adds post-processor description to this QueryDescriptor
	 * @param postProcessor descriptor of post-processor to be added
	 */
	public void addPostProcessor(ProcessorDescriptor postProcessor) {
		add(postProcessors, postProcessor);
	}

	public int getCacheTime() {
		return cacheTime;
	}

	public void setCacheTime(int cacheTime) {
		this.cacheTime = cacheTime;
	}
	
	private void add(List<ProcessorDescriptor> list, ProcessorDescriptor pd) {
        int index = Collections.binarySearch(list, pd);
        if(index<0){
            index = -(index+1);
        }
        list.add(index, pd);
    }
}
