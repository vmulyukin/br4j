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
package com.aplana.dbmi.card.actionhandler.multicard;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.util.AttrUtils;

public class MappingUtils {
	
	protected static Log logger = LogFactory.getLog(MappingUtils.class);
	/**
	 * ��������� ID �������� �� ������ ����������� �������
	 * @param attr ������ ���� <����� ���� ��������>:<��� ��������>
	 * @return ID ��������
	 * @throws MappingSystemException ���� 
	 * @throws IllegalArgumentException
	 */
	protected static ObjectId stringToAttrId(String attr) throws MappingSystemException {
		String[] classAttr = attr.split(":");
		if (classAttr.length != 2) { 
			IllegalArgumentException e = new IllegalArgumentException("String attribute format must be \"<attr type>:<attr code>\"");
			logger.error(e.getMessage());
			throw e;
		}
		try {
			Class c = AttrUtils.getAttrClass(classAttr[0]);
			if (c == null) {
				MappingSystemException e = new MappingSystemException("Class for alias " + classAttr[0] + " not found");
				logger.error(e.getMessage());
				throw e;
			}
			ObjectId id = ObjectId.predefined(c, classAttr[1]);
			if (id == null) {
				MappingSystemException e = new MappingSystemException("ObjectId for " + attr + " not found");
				logger.error(e.getMessage());
				throw e;
			}
			return id;
		} catch (IllegalArgumentException e) {
			logger.error("Class for alias " + classAttr[0] + " not found");
			throw e;
		}
	}


}
