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
package com.aplana.dbmi.action;

import java.io.InputStream;
import java.util.ArrayList;

/**
 * �����-������ ������� csv
 * @author PPanichev
 *
 */
public class ParseImportFile implements Action {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public final static String FILE_ATTRIBUTE_DELIMETER = ";";
	public final static String HEAD_MANDATORY_FLAG = "%";
	public final static String HEAD_DOUBLET_CHECK_FLAG = "!";
	public final static String HEAD_CUSTOM_ATTRIBUTE_CODE_FLAG="@";
	//public final static String SYSTEM_ROLE="system_role";
		
	private InputStream file;
	public enum TypeImportObject {
		system_role, 
		system_group
	};
	private TypeImportObject typeImportObject;

	public Class getResultType() {
		// TODO Auto-generated method stub
		return ArrayList.class;
	}

	public InputStream getFile() {
		return file;
	}

	public void setFile(InputStream file) {
		this.file = file;
	}

	public TypeImportObject getTypeImportObject() {
		return typeImportObject;
	}

	public void setTypeImportObject(TypeImportObject typeImportObject) {
		this.typeImportObject = typeImportObject;
	}
}
