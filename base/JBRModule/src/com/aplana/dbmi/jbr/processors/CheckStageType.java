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

import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.DateAttribute;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;

public class CheckStageType extends ProcessCard {


	final static String PARAM_THROWIFNULL = "throw_if_incomes_null";
	 public static final ObjectId STAGE_TYPE = ObjectId.predefined(ListAttribute.class, "jbr.stage.type");



	/*
	 * ��������� ���������� ���� �������� �� �����������.
	 * 
	 */
	@Override
	public Object process() throws DataException {

		final Card card = super.getCard();
		if (card == null)
			return null;

		ListAttribute  stageTypeLA = (ListAttribute) card.getAttributeById(STAGE_TYPE);
			if (stageTypeLA.getValue() == null){
				throw new DataException("jbr.card.check.stage.type" );		
			}		
		return null;
	}

	
	
}
