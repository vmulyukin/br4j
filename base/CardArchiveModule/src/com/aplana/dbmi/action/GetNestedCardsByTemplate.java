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
package com.aplana.dbmi.action;

import java.util.List;

import com.aplana.dbmi.model.ObjectId;

/**
 * ��������� �������� �������� �������� �������, 
 * ��������������� ��������� ��������
 * @author ppolushkin
 *
 */
public class GetNestedCardsByTemplate implements Action {
	
	private static final long serialVersionUID = 1L;
	
	private ObjectId cardId;
	private List<ObjectId> usedTemplates;
	private List<ObjectId> excludedLinks;
	
	public ObjectId getCardId() {
		return cardId;
	}
	public void setCardId(ObjectId cardId) {
		this.cardId = cardId;
	}
	public List<ObjectId> getUsedTemplates() {
		return usedTemplates;
	}
	public void setUsedTemplates(List<ObjectId> usedTemplates) {
		this.usedTemplates = usedTemplates;
	}
	public List<ObjectId> getExcludedLinks() {
		return excludedLinks;
	}
	public void setExcludedLinks(List<ObjectId> excludedLinks) {
		this.excludedLinks = excludedLinks;
	}
	@SuppressWarnings("rawtypes")
	@Override
	public Class getResultType() {
		return List.class;
	}
	
	public boolean isCardId() {
		return cardId != null && cardId.getId() != null;
	}
	
	public boolean isTemplates() {
		return usedTemplates != null && !usedTemplates.isEmpty();
	}

}
