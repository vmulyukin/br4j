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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * Class used to represent configuration settings for some abstract processor.
 * <br>
 * <ul>Could be used to represent settings of following objects:
 * <li>{@link QueryBase} descendants;</li>
 * <li>{@link AccessCheckerBase} descendants;</li>
 * <li>{@link ProcessorBase} descendants.</li>
 * </ul>
 * Each processor descriptor could have set of initialization parameters defined, as well as
 * set of alternative descriptors to be used to process objects which satisfies 
 * conditions presented by some {@link Selector} object.
 */
public class ProcessorDescriptor implements Serializable, Comparable<ProcessorDescriptor>
{
	private static final long serialVersionUID = 5216456843790413414L;
	
	private String className;
	private HashMap<String, String> parameters;
	private HashMap<Selector, ProcessorDescriptor> specials;
	private Integer runOrder;
	private Boolean async;
	private String policy;
	private Integer priority;
	
	/**
	 * Sets default class name of processor 
	 * @param className default class name of processor
	 */
	public void setClassName(String className) {
		this.className = className;
	}
	
	/**
	 * Adds initialization parameter to this ProcessorDescriptor
	 * @param name name of parameter
	 * @param value value of parameter
	 */
	public void addParameter(String name, String value) {
		if (parameters == null)
			parameters = new HashMap<String, String>();
		parameters.put(name, value);
	}
	
	/**
	 * Adds specific {@link ProcessorDescriptor} to be used instead of this one
	 * if processed object satisfies given {@link Selector}
	 * @param selector {@link Selector} representing condition of alternative descriptor usage 
	 * @param descriptor alternative processor descriptor
	 */
	public void addSpecific(Selector selector, ProcessorDescriptor descriptor) {
		if (specials == null)
			specials = new HashMap<Selector, ProcessorDescriptor>();
		specials.put(selector, descriptor);
		if (selector instanceof Asynchronous) {
			descriptor.setPolicyName(((Asynchronous)selector).getPolicyName());
			descriptor.setPriority(((Asynchronous)selector).getPriority());
			if (((Asynchronous)selector).isAsync() != null) {
				descriptor.setAsync(((Asynchronous)selector).isAsync());
			}
		}
	}
	
	/**
	 * Gets default class name of processor object.
	 * @see #getClassName()
	 * @return class name of processor object
	 */
	public String getClassName() {
		return className;
	}
	
	/* *
	 * Gets class name of processor to be used to process given object.
	 * @param object object to be processed by processor. 
	 * @return 	If there is a 'specific' processor description defined and given object
	 * satisfies conditions of corresponding {@link Selector} then returns this alternative class name,
	 * otherwise returns {@link #getClassName()}. 
	 */
	/*public String getClassName(Object object) {
		if (object == null || specials == null)
			return getClassName();
		for (Iterator itr = specials.keySet().iterator(); itr.hasNext(); ) {
			Selector key = (Selector) itr.next();
			if (key.satisfies(object))
				return ((ProcessorDescriptor) specials.get(key)).getClassName(object);
		}
		return getClassName();
	}*/

	/**
	 * Gets map of initialization parameters defined for this processor 
	 * @return map of initialization parameters defined for this processor
	 */
	public Map<String, String> getParameters() {
		return parameters;
	}
	
	/**
	 * Checks if this {@link ProcessorDescriptor} has an alternative 'special' descriptors
	 * @return true if this {@link ProcessorDescriptor} has an alternative 'special' descriptors,
	 * false otherwise
	 */
	public boolean hasSpecials() {
		return specials != null;
	}
	
	/**
	 * Gets 'special' descriptor to be used to process objects which satisfies conditions
	 * presented by given {@link Selector} object
	 * @param selector
	 * @return alternative descriptor to be used for objects satisfying given {@link Selector} or
	 * null if no alternative descriptors is specified for given {@link Selector}
	 */
	public ProcessorDescriptor getSpecific(Selector selector) {
		if (specials == null)
			return null;
		return (ProcessorDescriptor) specials.get(selector);
	}
	
	/**
	 * Gets the concrete descriptor of processor to be used to process given object.
	 * 
	 * @param object object to be processed by processor. 
	 * @return 	If there is a 'specific' processor description defined and given object
	 * satisfies conditions of corresponding {@link Selector} then returns this alternative descriptor,
	 * otherwise returns itself. 
	 */
	public ProcessorDescriptor findApplicable(Object object) {
		if (object == null || specials == null)
			return this;
		for (Iterator<Selector> itr = specials.keySet().iterator(); itr.hasNext(); ) {
			Selector key = (Selector) itr.next();
			if (key.satisfies(object))
				return ((ProcessorDescriptor) specials.get(key)).findApplicable(object);
		}
		return this;
	}

	/**
	 * @return the runOrder of the processor in the action-exec handling,
	 * the lower value means the earlier running of this processor in the action track.
	 * default value null the native run order "as is" in the queries.xml descriptors.
	 */
	public Integer getRunOrder() {
		return this.runOrder;
	}

	/**
	 * as getRunOrder but plain integer.
	 * @return
	 */
	public int getOrder() {
		return (this.runOrder != null) ? this.runOrder.intValue() : 0;
	}

	/**
	 * @param runOrder the runOrder to set
	 */
	public void setRunOrder(Integer runOrder) {
		this.runOrder = runOrder;
	}

	public void setAsync(Boolean async) {
		this.async = async;
	}

	public Boolean isAsync() {
		return async;
	}
	
	public void setPolicyName(String policy) {
		this.policy = policy;
	}
	
	public String getPolicyName() {
		return policy;
	}
	
	public void setPriority(Integer priority) {
		this.priority = priority;
	}
	
	public Integer getPriority() {
		return priority;
	}

	@Override
	public int compareTo(ProcessorDescriptor o) {
		return this.getOrder() - o.getOrder();
	}
}
