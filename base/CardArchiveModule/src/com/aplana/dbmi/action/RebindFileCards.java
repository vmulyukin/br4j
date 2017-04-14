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

import java.util.HashSet;
import java.util.Set;

import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Template;

/**
 * <b>Action used to bind all founded file cards to the card, specified in
 * {@link #setDestCardId(ObjectId)}. </b><br>
 * File cards will be searched recursively in {@link CardLinkAttribute
 * CardLinkAttributes} on the card which specified in
 * {@link #setLinkAttrIds(Set)}. By default searched in all attributes,
 * expected attributes, specified in {@link #setLinkAttrIds(Set)}. <br>
 * {@link Template Templates} of a file card will be specified in
 * {@link #setTargetTemplateIds(Set)}. By default file template identifier is
 * '<b>File</b>' (284).<br>
 * All found cards will bind to the card pointed via {@link #setDestCardId(ObjectId)} 
 * using CardLinkAttribute, specified by {@link #setDestAttrid(ObjectId)}.
 * Also you can set {@link #setNesting(int) nesting} for the search tree.
 */
public class RebindFileCards implements ObjectAction {

	private static final long serialVersionUID = 1L;

	private ObjectId destCardId;
	private ObjectId destAttrId = ObjectId.predefined(CardLinkAttribute.class, "jbr.files");
	private Set<ObjectId> linkAttrIds = new HashSet<ObjectId>();
	private Set<ObjectId> targetTemplateIds = new HashSet<ObjectId>();
	private Set<ObjectId> cardTreeTemplates = new HashSet<ObjectId>(); 
	private boolean excludeLinkAttrIds = true;
	private boolean excludeTemplateIds = true;
	{
		this.targetTemplateIds.add(new ObjectId(Template.class, 284));
	}
	// ������������� ����������� -1, ����� ������ ��� ����� ������
	private int nesting = -1;
	
	/**
	 * Sets the depth of the tree from which find file cards. <br> Default is '-1' (All tree).
	 */
	public void setNesting(int nesting) {
		this.nesting = nesting;
	}
	
	/**
	 * Returns the depth of the tree from which find file cards. <br> Default is '-1' (All tree).
	 */
	public int getNesting(){
		return this.nesting;
	}
	
	/**
	 * This method sets identifier of {@link CardLinkAttribute}, where to
	 * bind all found cards
	 * 
	 * @param destAttrId identifier of {@link CardLinkAttribute}
	 */
	public void setDestAttrid(ObjectId destAttrId) {
		if (!CardLinkAttribute.class.equals(destAttrId.getType()))
			throw new IllegalArgumentException(
					"CardLinkAttribute identifier is required");
		this.destAttrId = destAttrId;
	}

	/**
	 * Returns identifier of {@link CardLinkAttribute}, where to bind all
	 * found cards
	 * 
	 * @return identifier of {@link CardLinkAttribute}
	 */
	public ObjectId getDestAttrId() {
		return this.destAttrId;
	}

	/**
	 * This method sets identifier of {@link com.aplana.dbmi.model.Card Card}
	 * whose descendants (file cards) will be rebind to it.
	 * 
	 * @param destCardId
	 */
	public void setDestCardId(ObjectId destCardId) {
		this.destCardId = destCardId;
	}

	/**
	 * Returns identifier of {@link com.aplana.dbmi.model.Card Card} whose
	 * descendants (file cards) will be rebind
	 * 
	 * @return identifier of {@link com.aplana.dbmi.model.Card Card}
	 */
	public ObjectId getDestCardId() {
		return this.destCardId;
	}

	/**
	 * This method sets identifiers of {@link CardLinkAttribute} on cards which
	 * will be ignored or used to search file cards. If
	 * {@link #setExcludeLinkAttrIds(boolean) excludeLinkAttrIds} not specified
	 * or is <b>true</b>, identifiers will be ignored to search.
	 * 
	 * @param linkAttrIds
	 *            identifiers of {@link CardLinkAttribute}	 
	 */
	public void setLinkAttrIds(Set<ObjectId> linkAttrIds) {
		for (ObjectId attrId : linkAttrIds) {
			if (attrId == null || !CardLinkAttribute.class.equals(attrId.getType())) {
				throw new IllegalArgumentException(
						"CardLinkAttribute identifier is required");
			}
			this.linkAttrIds.add(attrId);
		}
	}

	/**
	 * Returns identifiers of {@link CardLinkAttribute} on cards which will be
	 * ignored.
	 * 
	 * @return identifiers of {@link CardLinkAttribute}
	 */
	public Set<ObjectId> getLinkAttrIds() {
		return this.linkAttrIds;
	}
	
	/**
	 * Returns how to use attributes specified by {@link #setExcludeLinkAttrIds(boolean)}
	 * @return true or false
	 */
	public boolean isExcludeLinkAttrIds() {
		return this.excludeLinkAttrIds;
	}

	/**
	 * Sets how to use {@link #setLinkAttrIds(Set) attributes} 
	 * @param excludeLinkAttrIds
	 *            if <i>true</i> then <b>attrs</b> will be ignored, otherwise
	 *            <b>attrs</b> will be used. You can not specify it ( then it
	 *            will use the default value - true)
	 */
	public void setExcludeLinkAttrIds(boolean excludeLinkAttrIds) {
		this.excludeLinkAttrIds = excludeLinkAttrIds;
	}

	/**
	 * This method sets identifiers of card {@link Templates} which should be rebind.
	 * 
	 * @param targetTemplateIds identifiers of {@link Template}
	 */
	public void setTargetTemplateIds(Set<ObjectId> targetTemplateIds) {

		if (targetTemplateIds == null || targetTemplateIds.isEmpty())
			return;
		for (ObjectId templId : targetTemplateIds) {
			if (templId == null || !Template.class.equals(templId.getType())) {
				throw new IllegalArgumentException(
						"Template identifier is required");
			}
		}
		this.targetTemplateIds = targetTemplateIds;
	}

	/**
	 * Returns identifiers of file card {@link Template}
	 * 
	 * @return identifiers of {@link Template}
	 */
	public Set<ObjectId> getTargetTemplateIds() {
		return this.targetTemplateIds;
	}

	public Class<?> getResultType() {
		return null;
	}

	public ObjectId getObjectId() {
		return this.destCardId;
	}

	public Set<ObjectId> getCardTreeTemplates() {
		return cardTreeTemplates;
	}

	public void setCardTreeTemplates(Set<ObjectId> cardTreeTemplates) {
		this.cardTreeTemplates = cardTreeTemplates;
	}

	public boolean isExcludeTemplateIds() {
		return excludeTemplateIds;
	}

	public void setExcludeTemplateIds(boolean excludeTemplateIds) {
		this.excludeTemplateIds = excludeTemplateIds;
	}
	
	

}
