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
 * Stores state view of block for of type template, of status card and of type block  
 * @author Agadelshin
 *
 */
public class BlockViewParam extends LockableObject {
	private static final long serialVersionUID = 1L;
	/**
	 * Identifier of state view of block - collapse block
	 */
	public static final int COLLAPSE = 0;
	
	/**
	 * Identifier of state view of block - collapse block if all attributes is empty
	 */
	public static final int COLLAPSE_IF_EMPTY = 1;
	
	/**
	 * Identifier of state view of block - block is open
	 */
	public static final int OPEN = 2;
	
	private ObjectId templateId;
	private ObjectId blockId;
	private ObjectId statusId;
	private int stateBlock; // state of view block 
	
	/**
	 * Set template identifier
	 * @param id value of template identifier
	 */
	public void setTemplate(long id) {
		this.templateId = new ObjectId(Template.class, id);
	}
	
	/**
	 * Set block identifier
	 * @param id value block code
	 */
	public void setBlock(String id) {
		this.blockId = new ObjectId(AttributeBlock.class, id);
	}
	
	/**
	 * Set card status identifier
	 * @param id value of card status identifier
	 */
	public void setCardStatus(long id) {
		this.statusId = new ObjectId(CardState.class, id);
	}
	
	/**
	 * Sets state of view block 
	 * @param stateBlock state of view block
	 */
	public void setStateBlock(int stateBlock) {
		this.stateBlock = stateBlock;
	}
	
	/**
	 * Sets BlockViewParam identifier
	 * @param id value of BlockViewParam identifier
	 */
	public void setId(long id) {
		super.setId(new ObjectId(BlockViewParam.class, id));
	}
	
	/**
	 * Gets identifier of {@link Template} associated with given state of view
	 * @return identifier of template
	 */
	public ObjectId getTemplate() {
		return templateId;
	}
	
	/**
	 * Gets identifier of {@link AttributeBlock} associated with given state of view
	 * @return code block
	 */
	public ObjectId getBlock() {
		return blockId;
	}
	
	/**
	 * Gets identifier of {@link CardStatus} associated with given state of view
	 * @return identifier of card status
	 */
	public ObjectId getCardStatus() {
		return statusId;
	}
	
	/**
	 * Gets state of view for given of type template, of status card and of type block  
	 * @return state of view block
	 */
	public int getStateBlock() {
		return stateBlock;
	}
}
