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
package com.aplana.ireferent;

import java.util.Collection;
import java.util.List;

import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.Person;
import com.aplana.ireferent.types.WSOCollection;
import com.aplana.ireferent.types.WSObject;
import com.aplana.ireferent.util.ServiceUtils;

public final class SimpleObjectFactory  extends WSObjectFactory {
	
	SimpleObjectFactory() {
	}

	@Override
	public Object newWSObject(Card card) throws IReferentException {
		WSObject wsobject = new WSObject();
		wsobject.setId(card.getId().getId().toString());
		return wsobject;
	}
	
	@Override
	public WSOCollection newWSOCollection(Search search)
		    throws IReferentException {
		logger.info("SimpleFactory.newWSOCollection(search)->searchCards begin:");
		Collection<Card> collectionCards =  ServiceUtils.searchCards(serviceBean, search,
				null);
		logger.info("SimpleFactory.newWSOCollection(search)->searchCards end.");
		logger.info("SimpleFactory.newWSOCollection(search)->create begin:");
		logger.info("SimpleFactory.newWSOCollection(search)->create size = " + collectionCards.size());
		WSOCollection wsCollection = newWSOCollectionSimpleData(collectionCards);
		logger.info("SimpleFactory.newWSOCollection(search)->create end.");
		return wsCollection;
	}
	
    private WSOCollection newWSOCollectionSimpleData(
    	    Collection<Card> cards) throws IReferentException {
    	WSOCollection wsoCollection = new WSOCollection();
    	List<Object> data = wsoCollection.getData();
    	Person person = serviceBean.getPerson();
    	if ( person != null){
    		wsoCollection.setUserId( person.getCardId().getId().toString() );
    	}
    	for (Card card : cards) {
    	    data.add(newWSObject(card));
    	}
    	return wsoCollection;
    }
}