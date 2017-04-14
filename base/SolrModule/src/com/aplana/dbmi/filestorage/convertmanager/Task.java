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

import com.aplana.dbmi.action.Material;
import com.aplana.dbmi.storage.ContentStorage;

public interface Task {
	
	public Material getMaterial();
	public void setMaterial(Material material);

	public double getPriority();
	public void setPriority(double p);

	public void addIEventListener(EventListener iel);
	public EventListener[] getListEventListeners();

	public InputStream getResult() ;
	public void setResult(InputStream result);

	// public void onError(Exception exception);

	public ContentStorage getStorage();
	public void setStorage(ContentStorage storage);
	
	public long getWaitingTime();
	public void setStartTime(long t);
}
