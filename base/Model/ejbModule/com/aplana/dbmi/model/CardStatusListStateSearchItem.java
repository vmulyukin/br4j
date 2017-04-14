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
package com.aplana.dbmi.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represents state search attribute. 
 * It contains collection of {@link CardState} that could change state(It could be checked/unchecked) and 
 * will be used at search. 
 * 
 * @author skashanski
 *
 */
public class CardStatusListStateSearchItem extends StateSearchItem {
	
	private Collection<CardState> cardStates = new ArrayList<CardState>();
	
	public CardStatusListStateSearchItem(ObjectId id) {
		super(id);
	}

	public Collection<CardState> getCardStates() {
		return cardStates;
	}

	public void setCardStates(Collection<CardState> cardStates) {
		this.cardStates = cardStates;
	}

	@Override
	public Collection<DataObject> getValues() {
		List<DataObject> values  = new ArrayList<DataObject>();
		
		for(CardState cardState : cardStates) {
			values.add(cardState);
		}
		
		return values;
	}
	
	
	
	 
	
	
	
}
