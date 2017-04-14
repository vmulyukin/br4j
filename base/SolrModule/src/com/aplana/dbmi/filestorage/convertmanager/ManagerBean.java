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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.action.Material;
import com.aplana.dbmi.storage.ContentStorage;
import com.aplana.dbmi.storage.impl.StorageConst;


public class ManagerBean {

	private static ManagerBean instance = null;
	
	private PdfConvertManager fm;
	final protected Log logger = LogFactory.getLog(getClass());

	public void addTask(ContentStorage storage, Material material,
			Priority priority,
			EventListener iel ) 
	{
		final Task task= new TaskConvert();

		task.setMaterial(material);
		task.setPriority( getInt(priority) );
		task.setStorage(storage);

		if(iel != null)
			task.addIEventListener(iel);

		fm.addTask(task);
		fm.startThreadConverter();
	}

	public void addTask(ContentStorage storage, Material material,
			Priority priority) {
		addTask(storage, material, priority, null); 
	} 

	/**
	 * @param priority
	 * @return
	 */
	private int getInt(Priority priority) {
		int p  = (PdfConvertManager.PRIORITY_NORMAL + PdfConvertManager.PRIORITY_BACKGROUND)/2;
		if(priority == Priority.immediate)
			p= PdfConvertManager.PRIORITY_NORMAL;
		else if(priority == Priority.background)
			p= PdfConvertManager.PRIORITY_BACKGROUND;
		return p;
	}
	
	public void setPdfConvertManager(PdfConvertManager manager) {
		this.fm = manager;
	}

	public static ManagerBean ensureGetBean(org.springframework.beans.factory.BeanFactory beanFactory) {
		if(instance == null)  {
			instance = (ManagerBean)beanFactory.getBean(StorageConst.BEAN_CONVERTER);
		}
		return instance;
	}
}
