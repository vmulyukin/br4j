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
package com.aplana.dbmi.model.search.ext;

import com.aplana.dbmi.model.util.AttrUtils;

import java.util.Collection;

public class LinkedObjectId extends MultipleObjectId {
	
	private String linkedCode;
	
	private boolean backLinked;

	public LinkedObjectId(Class type, Object id, String linkedCode) {
		super(type, id, null);
		parseLinkedCode(linkedCode);
	}

	public LinkedObjectId(Class type, Object id, String linkedCode, Collection extraIds) {
		super(type, id, extraIds);
		parseLinkedCode(linkedCode);
	}

	private void parseLinkedCode(String linkedCode){
		String[] parts = linkedCode.split(":");
		if(parts.length == 2){
			this.linkedCode = parts[1];
			if(parts[0].equals(AttrUtils.ATTR_TYPE_BACKLINK)) {
				backLinked = true;
			}
		} else {
			this.linkedCode = linkedCode;
		}
	}


	public String getLinkedCode() {
		return linkedCode;
	}

	public boolean isBackLinked() {
		return backLinked;
	}

	public void setLinkedCode(String linkedCode) {
		this.linkedCode = linkedCode;
	}

}
