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
package com.aplana.dbmi.card.cardlinkpicker.descriptor;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import com.aplana.dbmi.card.hierarchy.Messages;
import com.aplana.dbmi.card.hierarchy.descriptor.HierarchyDescriptor;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardState;
import com.aplana.dbmi.model.ListAttribute;
import com.aplana.dbmi.model.ObjectId;

public class CardLinkPickerDescriptor {
	public static final String DEFAULT_REF_ID = "default";
	private List<CardLinkPickerVariantDescriptor> variants;
	private Messages messages;
	private ObjectId choiceAttrId;
	private boolean localChoice;
	private CardLinkPickerVariantDescriptor defaultVariantDescriptor;
	private HashMap<String, Collection<String>> dropDownItems;
	private HashMap<String, String> mapItemsTemplates;
	private HashMap<String, String> mapItemsAttributes;
	private boolean sharedValues  = false;

	public HashMap<String, String> getMapItemsAttributes() {
		return mapItemsAttributes;
	}

	public void setMapItemsAttributes(HashMap<String, String> mapItemsAttributes) {
		this.mapItemsAttributes = mapItemsAttributes;
	}

	public CardLinkPickerVariantDescriptor getDefaultVariantDescriptor() {
		return defaultVariantDescriptor;
	}

	public void setDefaultVariantDescriptor(
			CardLinkPickerVariantDescriptor defaultVariantDescriptor) {
		this.defaultVariantDescriptor = defaultVariantDescriptor;
	}

	public List<CardLinkPickerVariantDescriptor> getVariants() {
		return variants;
	}

	public void setVariants(List<CardLinkPickerVariantDescriptor> variants) {
		this.variants = variants;
	}

	public Messages getMessages() {
		return messages;
	}

	public void setMessages(Messages messages) {
		this.messages = messages;
	}
	
	public CardLinkPickerVariantDescriptor getVariantDescriptor(ObjectId choiceReferenceValueId) {
		if (choiceReferenceValueId == null) {
			return defaultVariantDescriptor;
		}
		for( CardLinkPickerVariantDescriptor vd : variants) {
			if (choiceReferenceValueId.equals(vd.getChoiceReferenceValueId()))
				return vd;
		}
		return defaultVariantDescriptor;
	}

	public CardLinkPickerVariantDescriptor getVariantDescriptor(Card c) {
		ObjectId refValId = null;
		if (choiceAttrId != null) {
			final ListAttribute attr = (ListAttribute)c.getAttributeById(choiceAttrId);
			if (attr != null && !attr.isEmpty()) {
				refValId = attr.getValue().getId();
			}
		}
		return getVariantDescriptor(refValId);		
	}

	public CardLinkPickerVariantDescriptor getVariantDescriptor(String alias) {
		for( CardLinkPickerVariantDescriptor vd : variants) {
			if (vd != null && vd.getAlias().equals(alias))
				return vd;
		}
		return null;
	}

	public ObjectId getChoiceAttrId() {
		return choiceAttrId;
	}

	public void setChoiceAttrId(ObjectId choiceAttrId) {
		this.choiceAttrId = choiceAttrId;
	}

	public HashMap<String, Collection<String>> getDropDownItems() {
		return dropDownItems;
	}

	public void setDropDownItems(
			HashMap<String, Collection<String>> dropDownStates) {
		this.dropDownItems = dropDownStates;
	}

	public HashMap<String, String> getMapItemsTemplates() {
		return mapItemsTemplates;
	}

	public void setMapItemsTemplates(HashMap<String, String> mapItemsTemplates) {
		this.mapItemsTemplates = mapItemsTemplates;
	}

	public boolean isSharedValues() {
		return sharedValues;
	}

	public void setSharedValues(boolean sharedValues) {
		this.sharedValues = sharedValues;
	}

	public boolean isLocalChoice() {
		return localChoice;
	}

	public void setLocalChoice(boolean localChoice) {
		this.localChoice = localChoice;
	}

}
