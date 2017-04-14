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
package com.aplana.dbmi.module.docflow;

import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.BeanNameAware;

import com.aplana.dbmi.model.ObjectId;

@Deprecated
public class DocumentManager implements BeanNameAware {
	public static final String BEAN_MANAGER = "docManager";
	
	protected Log logger = LogFactory.getLog(getClass());
	private HashMap<ObjectId, ManagedThread> threads = new HashMap<ObjectId, ManagedThread>();
	
	synchronized public void addDocument(ObjectId id, Runnable processor) {
		ManagedThread thread;
		if (threads.containsKey(id)) {
			thread = threads.get(id);
			thread.addAttempt();
			logger.info("[" + name + "] Document " + id.getId() + " is already processing, asked for additional retry");
		} else {
			thread = new ManagedThread(this, processor);
			thread.setName(name + "#" + id.getId());
			threads.put(id, thread);
			thread.start();
			logger.info("[" + name + "]Document " + id.getId() + " assigned to processing");
		}
	}
	
	synchronized void remove(Object id) {
		threads.values().remove(id);
	}

	private String name;
	public void setBeanName(String name) { this.name = name; }
	public String getName() { return name; }
}
