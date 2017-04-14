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
package com.aplana.dbmi.search;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.CardStatusListStateSearchItem;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class CardStatusListStateSearchItemHandler implements
		StateSearchItemHandlerInterface<CardStatusListStateSearchItem> {

	public void handle(Collection<CardStatusListStateSearchItem> values, Search search) {
		Set<Object> result = new HashSet<Object>();
		
		for (CardStatusListStateSearchItem item : values) {
			if (!item.isChecked()) 
				continue;
			
			for (CardState cardState : item.getCardStates()) {
				result.add(cardState.getId().getId());
			}	

		}
		//���� �����, �� ������� ��� ����� �� ���� �������� �������� MultipleStateSearchItemAttribute
		if (result.isEmpty()) {
			for (CardStatusListStateSearchItem item : values) {
				for (CardState cardState : item.getCardStates()) {
					result.add(cardState.getId().getId());
				}	

			}
		}
		
		search.setStates(result);
	}
}
