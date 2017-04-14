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
package com.aplana.dbmi.jbr.processors;

import com.aplana.dbmi.jbr.util.CheckingAttributes;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.service.DataException;

public abstract class ProcessCardWithConditions extends ProcessCard {

	/**
	 * ���� ���������� ���� ��������� ���� ������� ��������� ������� ��� �������,
	 * ���������� ������������� �� ProcessCardWithConditions ������ ProcessCard
	 * � ������� �������� ������� ������� checkContidions.
	 */
	private static final String PARAM_CONDITION_ATTR = "condition";
	
	private static final long serialVersionUID = 1L;
	protected CheckingAttributes conditionAttrs;
	
	/**
	 * ������ true, ���� �������� ������������� ���� ��������.
	 */
	protected boolean checkContidions(Card card) throws DataException {
		if(!((conditionAttrs != null && conditionAttrs.check(card, getUser()))||conditionAttrs==null)){
			return false;
		} else {
			return true;
		}
	}

	public void setParameter(String name, String value) {
		if (PARAM_CONDITION_ATTR.equalsIgnoreCase(name)) {
			try {
				if (conditionAttrs == null) 
					conditionAttrs = new CheckingAttributes(getQueryFactory(), getDatabase(), getSystemUser());
				conditionAttrs.addCondition(value);
			} catch (Exception e) {
				logger.error("������ ��� �������� ���������", e);
			}
		} else
			super.setParameter(name, value);		
	}
}
