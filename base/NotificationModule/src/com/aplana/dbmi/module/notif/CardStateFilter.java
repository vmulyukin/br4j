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
package com.aplana.dbmi.module.notif;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.ObjectId;

public class CardStateFilter implements CardFilter {
	
	private List<ObjectId> suitableStates;
	
	public void setSuitableStateId(ObjectId cardStateId) {
		if (!CardState.class.equals(cardStateId.getType()))
			throw new IllegalArgumentException("cardStateId must be a CardState ID");
		suitableStates = Collections.singletonList(cardStateId);
	}
	
	public void setSuitableState(String state) {
		setSuitableStateId(ObjectId.predefined(CardState.class, state));
	}
	
	public void setSuitableStates(List<String> states) {
		suitableStates = new ArrayList<ObjectId>(states.size());
		for (Iterator<String> itr = states.iterator(); itr.hasNext(); ) {
			String state = itr.next();
			suitableStates.add(ObjectId.predefined(CardState.class, state));
		}
	}
	
	public void setSuitableStateIds(List<ObjectId> states) {
		this.suitableStates = states;
	}

	@Override
	public boolean isCardSuitable(Card card) {
		if (suitableStates == null)
			throw new IllegalStateException("suitableStates must be assigned before use");
		return suitableStates.contains(card.getState());
	}

}
