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


import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;

/**
 * Used to remove material file attached to given {@link Card} from database.
 *
 */
public class RemoveFile implements ObjectAction {
	
	private static final long serialVersionUID = 1L;
	private ObjectId cardId;
	private String fileName;
	private int versionId = Material.CURRENT_VERSION;
	private boolean removeAll = true;
	
	public Class<?> getResultType() {
		return null;
	}

	public ObjectId getObjectId() {
		return getCardId();
	}
	
	/**
	 * Gets identifier of {@link Card} object to which belonged the removed file 
	 * @return identifier of {@link Card} object to which belonged the removed file 
	 */
	public ObjectId getCardId() {
		return cardId;
	}
	
	/**
	 * Sets identifier of {@link Card} object to which belongs the removed file 
	 * @param cardId identifier of {@link Card} object to which belongs the removed file 
	 * @throws IllegalArgumentException if cardId is not a {@link Card} identifier.
	 */
	public void setCardId(ObjectId cardId) {
		if (cardId != null && !Card.class.equals(cardId.getType()))
			throw new IllegalArgumentException("Not a card ID");
		this.cardId = cardId;
	}
	
	/**
	 * Gets name of removed file.
	 * It is a name of file itself without any directories.
	 * @return name of removed file.
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * Sets name of removed file.
	 * It should be a name of file itself without any directories.
	 * @param fileName desired value of the name of removed file
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	
	/**
	 * Gets version of material to be removed.
	 * If returned value is equal to {@link Material#CURRENT_VERSION} then
	 * current version will be used. Otherwise file from {@link com.aplana.dbmi.model.CardVersion} with
	 * given {@link com.aplana.dbmi.model.CardVersion#getVersion() version number} will be removed. 
	 * @return version of material to be removed
	 */
	public int getVersionId() {
		return versionId;
	}

	/**
	 * Sets version of material to be removed.
	 * By default it is equals to {@link Material#CURRENT_VERSION} which means that current version
	 * of file is required.
	 * @param versionId version of material to be removed
	 */
	public void setVersionId(int versionId) {
		this.versionId = versionId;
	}
	/**
	 * Returns which version of material will be removed.<br> If <code>true</code> all versions will be removed, otherwise version specified in {@link #setVersionId(int)} will be removed.<br>
	 * By default is <code>true</code>.
	 * @return which version of material will be removed.
	 */
	
	public boolean isRemoveAll(){
		return this.removeAll;
	}
	/**
	 * Sets which version of material will be removed.<br>
	 * If <code>true</code> all versions will be removed, otherwise version specified in {@link #setVersionId(int)} will be removed.<br>
	 * By default is <code>true</code>.
	 * @param removeAll 
	 */
	public void setRemoveAll(boolean removeAll){
		this.removeAll = removeAll;
	}
}
