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
package com.aplana.dbmi.card.dialog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.aplana.dbmi.card.AttributeEditorDialog;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.ServiceException;

public class GroupExecutionSameCardEditorDialog extends AttributeEditorDialog {
	
	private Set<Card> cards;
	
	public Set<Card> getCards() {
		return cards;
	}

	public void setCards(Set<Card> cards) {
		this.cards = cards;
	}

	public List<ArrayList<Object>> getData() throws DataException, ServiceException{
		
		if(cache && values!=null && !values.isEmpty()){
			return values;
		}
		
		Collection<ObjectId> childrenCards = EditorDialogHelper.loadDeepChildren(serviceBean, card);
		
		if(childrenCards.size() < 2) {
			return new ArrayList<ArrayList<Object>>(0);
		}
		
		Map<Card, ArrayList<Object>> map = EditorDialogHelper.getDataDialogCard(serviceBean, 
											childrenCards, card);
		
		values = new ArrayList<ArrayList<Object>>(map.values());
		
		cards = map.keySet();
		
		return values;
	}
	
	public String getJSONData() throws JSONException {
		List<ArrayList<Object>> values = null;
		try {
			values = getData();
		} catch (DataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		JSONObject result = new JSONObject();
		
		JSONArray ar = new JSONArray();
		
		for (List<Object> arrayList : values) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("id", arrayList.get(0));
			jsonObject.put("text", arrayList.get(1));
			jsonObject.put("idRes", arrayList.get(2));
			jsonObject.put("template", arrayList.get(3));
			ar.put(jsonObject);	
		}
		
		result.put("values", ar);
		
		return result.toString();		
	}
}
