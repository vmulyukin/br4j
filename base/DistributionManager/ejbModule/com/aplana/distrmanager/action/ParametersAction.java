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
package com.aplana.distrmanager.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.aplana.dbmi.action.ObjectAction;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.ReferenceValue;
import com.aplana.distrmanager.util.IdStringPair;

public class ParametersAction implements ObjectAction {

	private static final long serialVersionUID = 10L;

	private Result result = null;
	private ObjectId cardId = null;

	public Result getResult() {
		return result;
	}

	public void setResult(Result result) {
		this.result = result;
	}

	public Class<Result> getResultType() {
		return Result.class;
	}

	public void setCardId(ObjectId cardId) {
		this.cardId = cardId;
	}

	public ObjectId getObjectId() {
		return this.cardId;
	}

	public static class Result {

		private ReferenceValue refValue;
		private List<Attribute> listParameters = new ArrayList<Attribute>();
		private List<IdStringPair> attrPairsList = new ArrayList<IdStringPair>();
		private Map<String, String> parameters = new HashMap<String, String>();

		private Result() {
		}

		public static Result instance() {
			return new Result();
		}

		public ReferenceValue getRefValue() {
			return this.refValue;
		}

		public void setRefValue(ReferenceValue refValue) {
			this.refValue = refValue;
		}

		public List<Attribute> getListParameters() {
			return this.listParameters;
		}

		public void addListParameter(Attribute attr) {
			this.listParameters.add(attr);
		}

		public void setPair(List<IdStringPair> listPair) {
			this.attrPairsList.clear();
			this.attrPairsList.addAll(listPair);
		}

		public List<IdStringPair> getPair() {
			return this.attrPairsList;
		}

		public void setParameter(String key, String value) {
			parameters.put(key, value);
		}

		public String getParameter(String key) {
			return parameters.get(key);
		}

		public void fillParametersByMap(Map<String, String> values) {
			parameters.clear();
			parameters.putAll(values);
		}
	}
}