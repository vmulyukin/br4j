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
package com.aplana.cms.tags;

import com.aplana.cms.ContentProducer;
import com.aplana.cms.Tag;
import com.aplana.cms.TagProcessor;
import com.aplana.dbmi.delegate.DelegateHelper;
import com.aplana.dbmi.model.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.PrintWriter;
import java.util.List;


public class DelegatePersonsTag implements TagProcessor
{
    private JSONArray fromPersons;
    private JSONArray toPersons;
    private boolean isFromPersonSelectable;

	public boolean prepareData(Tag tag, Card item, ContentProducer cms) throws Exception {
		List<PersonView> userList = DelegateHelper.getPersonDictionary(cms.getService());
        fromPersons = getFromPersonsJSONArray(userList);
        toPersons = getToPersonsJSONArray(userList, cms.getService().getPerson().getId());
        // ��������� ������ � ��� �������� �� ������������ ���� ������������� ������������
        isFromPersonSelectable = DelegateHelper.isFromPersonSelectable(cms.getService(), true);
		return true;
	}

	public void writeHtml(PrintWriter out, Tag tag, Card item, ContentProducer cms) {
        out.write("<span style=\"display:none;\" id=\"delegationEditForm_fromPersonsValue\">" + fromPersons.toString() + "</span>");
        out.write("<span style=\"display:none;\" id=\"delegationEditForm_toPersonsValue\">" + toPersons.toString() + "</span>");
        out.write("<input type=\"hidden\" id=\"delegationEditForm_isFromPersonSelectableValue\" value=\"" + isFromPersonSelectable + "\"/>");
	}

    private JSONArray getFromPersonsJSONArray(List<PersonView> personList) throws JSONException {
        JSONArray fromPersons = new JSONArray();

        if(null == personList || personList.isEmpty()) {
            return fromPersons;
        }

        for(PersonView person : personList) {
            JSONObject personJSON = new JSONObject();
            personJSON.put("id", person.getId().getId());
            personJSON.put("name", person.getFullName());
            fromPersons.put(personJSON);
        }

        return fromPersons;
    }

    private JSONArray getToPersonsJSONArray(List<PersonView> personList, ObjectId currentUserId) throws JSONException {
        JSONArray toPersons = new JSONArray();

        if(null == personList || personList.isEmpty()) {
            return toPersons;
        }

        for(PersonView person : personList) {
        	if(!person.getId().equals(currentUserId)) { //skip current person
	            JSONObject personJSON = new JSONObject();
	            personJSON.put("id", person.getId().getId());
	            personJSON.put("name", person.getFullName());
	            toPersons.put(personJSON);
        	}
        }

        return toPersons;
    }

}