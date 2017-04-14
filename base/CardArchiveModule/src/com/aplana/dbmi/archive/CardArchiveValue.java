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
package com.aplana.dbmi.archive;

import java.util.Set;

public class CardArchiveValue {
	
	private Long cardId;
	private Long parentCardId;
	private Long statusId;
	private Long templateId;
	private Set<AttributeValueArchiveValue> attrValues;
	
	public Long getCardId() {
		return cardId;
	}
	
	public void setCardId(Long cardId) {
		this.cardId = cardId;
	}
	
	public Long getParentCardId() {
		return parentCardId;
	}

	public void setParentCardId(Long parentCardId) {
		this.parentCardId = parentCardId;
	}

	public Long getStatusId() {
		return statusId;
	}
	
	public void setStatusId(Long statusId) {
		this.statusId = statusId;
	}
	
	public Long getTemplateId() {
		return templateId;
	}
	
	public void setTemplateId(Long templateId) {
		this.templateId = templateId;
	}

	public Set<AttributeValueArchiveValue> getAttrValues() {
		return attrValues;
	}

	public void setAttrValues(Set<AttributeValueArchiveValue> attrValues) {
		this.attrValues = attrValues;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((attrValues == null) ? 0 : attrValues.hashCode());
		result = prime * result + ((cardId == null) ? 0 : cardId.hashCode());
		result = prime * result
				+ ((parentCardId == null) ? 0 : parentCardId.hashCode());
		result = prime * result
				+ ((statusId == null) ? 0 : statusId.hashCode());
		result = prime * result
				+ ((templateId == null) ? 0 : templateId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CardArchiveValue other = (CardArchiveValue) obj;
		if (attrValues == null) {
			if (other.attrValues != null)
				return false;
		} else if (!attrValues.equals(other.attrValues))
			return false;
		if (cardId == null) {
			if (other.cardId != null)
				return false;
		} else if (!cardId.equals(other.cardId))
			return false;
		if (parentCardId == null) {
			if (other.parentCardId != null)
				return false;
		} else if (!parentCardId.equals(other.parentCardId))
			return false;
		if (statusId == null) {
			if (other.statusId != null)
				return false;
		} else if (!statusId.equals(other.statusId))
			return false;
		if (templateId == null) {
			if (other.templateId != null)
				return false;
		} else if (!templateId.equals(other.templateId))
			return false;
		return true;
	}

}
