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
package com.aplana.dbmi.model;

/**
 * Class representing available {@link Card} states (records from CARD_STATUS table).
 * Usually CardState defines some stage of processing in {@link Card card's}
 * {@link Workflow workflow}.<br>
 * Card state could be changed by performing {@link WorkflowMove}.	
 * @author dsultanbekov
 */
public class CardState extends NamedLockableObject
{
	private static final long serialVersionUID = 6L;
	/**
	 * DRAFT status. Initial status in stanard dbmi workflow
	 */
	public static final ObjectId DRAFT = ObjectId.predefined(CardState.class, "draft");
	/**
	 * FOR APPROVAL card status identifier
	 */
	public static final ObjectId FOR_APPROVAL = ObjectId.predefined(CardState.class, "for-approval");
	/**
	 * FOR REVISION card status identifier
	 */
	public static final ObjectId FOR_REVISION = ObjectId.predefined(CardState.class, "for-revision");
	/**
	 * PUBLISHED card status identifier
	 */
	public static final ObjectId PUBLISHED = ObjectId.predefined(CardState.class, "published");
	/**
	 * ARCHIVE card status identifier
	 */
	public static final ObjectId ARCHIVE = ObjectId.predefined(CardState.class, "archive");
	
	public static final ObjectId DELO = ObjectId.predefined(CardState.class, "delo");
	
	public static final ObjectId REGISTRATION = ObjectId.predefined(CardState.class, "registration");
	public static final ObjectId CONSIDERATION  = ObjectId.predefined(CardState.class, "consideration");
	public static final ObjectId EXECUTION  = ObjectId.predefined(CardState.class, "execution");
	public static final ObjectId READY_TO_DELO  = ObjectId.predefined(CardState.class, "ready-to-write-off");

	private LocalizedString defaultMoveName;

	/**
	 * Default constructor
	 */
	public CardState() {
		name = new LocalizedString();
		defaultMoveName = new LocalizedString();
	}
	
	/**
	 * Creates {@link ObjectId} instance with type = CardState and id = stateId
	 * @param stateId identifier of status 
	 * @return {@link ObjectId} identifier of CardStatus object with given stateId
	 */
	public static ObjectId getId(long stateId) {
		return new ObjectId(CardState.class, stateId);
	}
	
	/**
	 * Gets localized name of card state
	 * @return localized name of card state
	 */
	public LocalizedString getName() {
		return name;
	}

	/**
	 * Returns default localized name to be used by {@link WorkflowMove} instances which 
	 * leads {@link Card} to this state. This name is used as a button title in GUI
	 * if corresponding {@link WorkflowMove} instance doesn't provide own name 
	 * @return default localized name of {@link WorkflowMove}
	 */
	public LocalizedString getDefaultMoveName() {
		return defaultMoveName;
	}
}
