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
package com.aplana.dbmi.service.impl.query;

import java.util.HashMap;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.MaterialAttribute;

/**
 * Utility class used to perform extra initialization of attribute objects
 */
public class AttributeUtils
{
	/**
	 * Key to store material file name in map 
	 * passed to {@link #fillMaterial(MaterialAttribute, HashMap)} method.
	 */
	public static final Object MATERIAL_FILE = new Integer(MaterialAttribute.MATERIAL_FILE);
	/**
	 * Key to store material URL in map 
	 * passed to {@link #fillMaterial(MaterialAttribute, HashMap)} method.
	 */
	public static final Object MATERIAL_URL = new Integer(MaterialAttribute.MATERIAL_URL);

	/**
	 * Initializes materialType and materialName properties of given {@link MaterialAttribute}
	 * @param attr {@link MaterialAttribute} to be initialized
	 * @param names Map containing information about material attached to card. Following keys
	 * are allowed: {@link #MATERIAL_FILE}, {@link #MATERIAL_URL}. Values should be empty.
	 */
	public static void fillMaterial(MaterialAttribute attr, HashMap<?, ?> names) {
		if (names.containsKey(MATERIAL_FILE)) {
			attr.setMaterialType(MaterialAttribute.MATERIAL_FILE);
			attr.setMaterialName((String) names.get(MATERIAL_FILE));
		} else if (names.containsKey(MATERIAL_URL)) {
			attr.setMaterialType(MaterialAttribute.MATERIAL_URL);
			attr.setMaterialName((String) names.get(MATERIAL_URL));
		}
	}
	
	/**
	 * ��������� ����� �� ������� null ��� �������� ������
	 * @param attr
	 * @return
	 */
	public static boolean isEmpty(Attribute attr) {
		if(attr == null)
			return true;
		if(attr.isEmpty())
			return true;
		return false;
	}
}
