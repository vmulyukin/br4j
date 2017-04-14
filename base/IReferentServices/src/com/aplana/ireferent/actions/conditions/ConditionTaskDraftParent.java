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
package com.aplana.ireferent.actions.conditions;

import java.util.Collection;
import java.util.List;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.ireferent.IReferentException;
import com.aplana.ireferent.types.WSOFormAction;
import com.aplana.ireferent.types.WSOItem;
import com.aplana.ireferent.types.WSOMTask;
import com.aplana.ireferent.util.ExtensionUtils;

/**
 * @author PPanichev
 *
 */
public class ConditionTaskDraftParent extends ConditionParametrized {
	
	public boolean isPerformed(Object object, Object action, DataServiceBean serviceBean) throws IReferentException {
		boolean result = false;		
		List<WSOItem> extensions = ExtensionUtils.getExtensions((WSOFormAction)action);
		for (WSOItem extension : extensions) {
		    String extensionId = extension.getId();
		    if (null != extensionId && extensionId.equals("parentTask")) {
		    	List<Object> values = ExtensionUtils
		    			.getExtensionValues(extension);
		    	if (null != values && !values.isEmpty()) {
		    		WSOMTask task = (WSOMTask)values.get(0);
		    		 Collection<Card> cards = getFilteredCards(serviceBean, task);		 
		    		if (cards.size() == 1) {
		    			result = true;
		    			break;
		    		} else 
		    		if (cards.size() > 1) {
		    			throw new IReferentException("Too many cards 'Report' in task: " + task.getId());
		    		}
		    	}
		    }
		    
		}
		return result;
	}
}
