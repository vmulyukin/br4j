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
package com.aplana.distrmanager.util;

import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.ObjectId;

public class IdStringPair {
	
	private String source;
	public final IdUtils.DataIdDescriptor dest;

	public IdStringPair() { 
		source = new String();
		dest = new IdUtils.DataIdDescriptor();
	}

	public IdStringPair(String src, IdUtils.DataIdDescriptor dst) {
		super();
		this.source = (src != null) ? src : new String();
		this.dest = (dst != null) ? dst : new IdUtils.DataIdDescriptor();
	}
	
	public void setVal(String val) {
		this.source = val;
	}
	
	public String getVal() {
		return this.source;
	}
	
	public ObjectId destId() {
		return dest.getId();
	}
}
