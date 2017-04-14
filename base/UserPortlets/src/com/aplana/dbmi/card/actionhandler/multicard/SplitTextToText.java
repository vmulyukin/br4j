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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.card.CardPortletSessionBean;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.TextAttribute;

public class SplitTextToText implements MappingProcedure {

	protected Log logger = LogFactory.getLog(getClass());

	public void init(CardPortletSessionBean session, String[] args)
			throws MappingUserException, MappingSystemException{
		// �������� ������ 1 ��������
		if (args.length != 1)
			throw new MappingSystemException("One argument expected, but " + args.length + " given: " + args);
		StringAttribute splitting;
		try {
			// �������� StringAttribute, �������� �������� ����� ������������
			splitting = (StringAttribute)session.getActiveCard().getAttributeById(
					MappingUtils.stringToAttrId(args[0]));
		} catch (ClassCastException e) {
			MappingSystemException me = new MappingSystemException("Type of attribute \"" + args[0] + "\" must be " 
											+ TextAttribute.class.getCanonicalName() + " or "
											+ StringAttribute.class.getCanonicalName() , e);
			logger.error(me.getMessage());
			throw me;
		}
		if (splitting == null) {
			MappingSystemException e = new MappingSystemException("Attribute " + args[0] + " not found");
			logger.error(e.getMessage());
			throw e;
		}
		text = Collections.singletonList(splitting.getValue()).iterator();
	}

	private Iterator<String> text;
	
	public boolean execute(List<Attribute> attrs) throws MappingSystemException {
		// �������� ����� 1 �������
		if (attrs.size() != 1) {
			MappingSystemException e = new MappingSystemException(getClass().getSimpleName() + " needs exactly 1 attribute");
			logger.error(e.getMessage(), e);
			throw e;
		}
		
		// ���� ��������� ������, ���������� false
		if (!text.hasNext())
			return false;
		
		try {
			((StringAttribute)(attrs.get(0))).setValue(text.next());
		} catch (ClassCastException e) {
			MappingSystemException me = new MappingSystemException("Attribute must be a type of " 
											+ TextAttribute.class.getCanonicalName() + " or "
											+ StringAttribute.class.getCanonicalName() , e);
			logger.error(me.getMessage());
			throw me;
		}
		return true;
	}

}
