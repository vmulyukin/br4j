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
package com.aplana.dbmi.support.action;

import com.aplana.dbmi.action.ObjectAction;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.LinkAttribute;
import com.aplana.dbmi.model.ObjectId;

public class CreateResolution implements ObjectAction<ObjectId> {
	private static final long serialVersionUID = 1L;
	
	private Card parentCard;
	private ObjectId templateId;
	private Attribute linkAttr;

	public CreateResolution(Card parent, ObjectId templateId, Attribute attr) {
		this.parentCard = parent;
		this.templateId = templateId;
		this.linkAttr = attr;
	}
	
	@Override
	public Class<?> getResultType() {
		return ObjectId.class;
	}

	@Override
	public ObjectId getObjectId() {
		return parentCard.getId();
	}
	
	public Card getParentCard() {
		return parentCard;
	}
	
	public ObjectId getTemplateId() {
		return templateId;
	}
	
	public LinkAttribute getLinkAttr() {
		return (LinkAttribute) linkAttr;
	}
}
