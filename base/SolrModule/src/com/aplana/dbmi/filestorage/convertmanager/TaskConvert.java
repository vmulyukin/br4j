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
package com.aplana.dbmi.filestorage.convertmanager;

import java.io.InputStream;
import java.text.MessageFormat;
import java.util.LinkedList;

import com.aplana.dbmi.action.Material;
import com.aplana.dbmi.storage.ContentStorage;

public class TaskConvert implements Task {

	private InputStream result = null;
	private Material material = null;

	final private LinkedList<EventListener> listiel = new LinkedList<EventListener>();;
	private double priority = 0.0;
	private long start;

	private ContentStorage storage =null;


	public TaskConvert() {
	}

	public Material getMaterial() {
		return material;
	}
	public void setMaterial(Material material) {
		this.material = material;
	}

	public double getPriority() 	{
		return this.priority;
	}

	public void setPriority(double p) 	{
		this.priority = p;
	}

	public void addIEventListener(EventListener iel) {
		listiel.add(iel);
	}

	public EventListener[] getListEventListeners() {
		return this.listiel.toArray(new EventListener[0]);
	}

	public synchronized void setResult(InputStream result) {
		this.result=result;		
	}

	public synchronized InputStream getResult() {
		return this.result;
	}

	public void setStorage(ContentStorage storage) {
		this.storage=storage;
	}

	public ContentStorage getStorage() {
		return this.storage;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		// return super.toString();
		return MessageFormat.format( "task url=''{0}'', priority={1}",
						(this.material == null ? null : material.getUrl() ), this.priority
					);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int hash = 1;
		hash = prime * hash
				+ ((this.listiel == null) ? 0 : this.listiel.hashCode());
		hash = prime * hash
				+ ((this.material == null) ? 0 : this.material.hashCode());
		return hash;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final TaskConvert other = (TaskConvert) obj;
		if (this.listiel == null) {
			if (other.listiel != null)
				return false;
		} else if (!this.listiel.equals(other.listiel))
			return false;
 
		if (this.material == null || this.material.getUrl() == null) {
			if (other.material != null || other.material.getUrl() != null)
				return false;
		} else if (  (other.material == null) || !this.material.getUrl().equals(other.material.getUrl()))
			return false;
		return true;
	}
	
	public void setStartTime(long t) {
		this.start = t;
	}

	@Override
	public long getWaitingTime() {
		return System.currentTimeMillis() - start;
	}
}
