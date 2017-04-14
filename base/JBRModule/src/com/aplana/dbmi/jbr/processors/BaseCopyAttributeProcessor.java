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
/**
 * 
 */
package com.aplana.dbmi.jbr.processors;

import com.aplana.dbmi.jbr.util.IdUtils;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.query.AttributeTypes;

/**
 * @author RAbdullin
 *
 */
public abstract class BaseCopyAttributeProcessor extends ProcessCard {

	static final String PARAM_TYPE = "type";
	static final String PARAM_FROM = "from";
	static final String PARAM_TO = "to";
	static final String PARAM_WRITE_OPERATION = "writeOperation";
	static final String PARAM_UPDATE_ACCESS_LIST = "updateAccessList";	// ����� �������� - ���� ���������� ������� ����
	static final String PARAM_ATTR_IS_ONLY_MODEL = "isOnlyModel";

	// ���������
	protected String typeCh;
	protected String attrCodeOrKeyFrom; // ��� ��� ���� �� objectids.properties;
	protected String attrCodeOrKeyTo; // ��� ��� ���� �� objectids.properties;
	protected boolean append = false;			// ��������� ��� ������ �������� ������� �������� (true - ���������, false - ��������� ��������)
	protected boolean updateAccessList = false;	// ��������� ��� ��� ������� ���� - ������ ����������� ���������, ������� ����� �� ������ �� ����� (�� ��������� �� ����)
	protected boolean isOnlyModel = false;		// �� ��������� ��������� ���� � �� � ������ ������
	// ���������� ����
	protected String attrcodeFrom;
	protected String attrcodeTo;

	protected BaseCopyAttributeProcessor() {
		super();
	}

	@Override
	public Object process() throws DataException {
		attrcodeFrom = attrCodeOrKeyFrom;
		attrcodeTo = attrCodeOrKeyTo;

		// ���� ����� ��� - ������� �������� ���� ���������, ������ ��� ������
		// ����� �� objectids.properties...
		if (typeCh != null && typeCh.length() > 0) {
			final Class<?> attrType = AttributeTypes.getAttributeClass(typeCh);

			ObjectId id = chkInitAttr(attrCodeOrKeyFrom, attrType);
			if (id != null)
				attrcodeFrom = (String) id.getId();

			id = chkInitAttr(attrCodeOrKeyTo, attrType);
			if (id != null)
				attrcodeTo = (String) id.getId();
		} else {
			ObjectId id = IdUtils.smartMakeAttrId( attrcodeFrom, Attribute.class);
			attrcodeFrom = (String) id.getId();

			id = IdUtils.smartMakeAttrId( attrcodeTo, Attribute.class);
			attrcodeTo = (String) id.getId();
		}

		return null;
	}

	protected ObjectId chkInitAttr(String attrKeyOrCode, Class<?> attrClass) {
		final ObjectId result = ObjectId.predefined(attrClass, attrKeyOrCode);
		if (result == null)
			logger.warn( "No predefined id for key " + attrClass.getName() + ": " + attrKeyOrCode + " -> using as is");
		return result;
	}

	public void setParameter(String name, String value) {
		if (PARAM_TYPE.equalsIgnoreCase(name))
			this.typeCh = value;
		else if (PARAM_FROM.equalsIgnoreCase(name))
			this.attrCodeOrKeyFrom = value;
		else if (PARAM_TO.equalsIgnoreCase(name))
			this.attrCodeOrKeyTo = value;
		else if (PARAM_TO.equalsIgnoreCase(name))
			this.attrCodeOrKeyTo = value;
		else if (PARAM_WRITE_OPERATION.equalsIgnoreCase(name))
			this.append = "append".equalsIgnoreCase(value.trim());
		else if (PARAM_UPDATE_ACCESS_LIST.equalsIgnoreCase(name))
			this.updateAccessList = "true".equalsIgnoreCase(value.trim());
		else if (PARAM_ATTR_IS_ONLY_MODEL.equalsIgnoreCase(name))
			this.isOnlyModel = "true".equalsIgnoreCase(value.trim());
		else
			super.setParameter(name, value);
	}
}