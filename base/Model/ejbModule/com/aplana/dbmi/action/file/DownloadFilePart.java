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
package com.aplana.dbmi.action.file;

import com.aplana.dbmi.action.ObjectAction;
import com.aplana.dbmi.action.Material;
import com.aplana.dbmi.model.ObjectId;

/**
 * Action used in {@link DownloadFileStream} to fetch portion of
 * file attached to given {@link com.aplana.dbmi.model.Card} 
 * (or {@link com.aplana.dbmi.model.CardVersion}) object
 * from database
 */
public class DownloadFilePart implements ObjectAction<byte[]> {
	private static final long serialVersionUID = 3L;

	private ObjectId cardId;
	private int versionId = Material.CURRENT_VERSION;
	private int length;
	private int offset;
	private String url;

	/**
	 * Gets identifier of {@link com.aplana.dbmi.model.Card} to download material from
	 * @return identifier of {@link com.aplana.dbmi.model.Card} to download material from
	 */
	public ObjectId getCardId() {
		return cardId;
	}

	/**
	 * Sets identifier of {@link com.aplana.dbmi.model.Card} to download material from
	 * @param cardId desired value of {@link com.aplana.dbmi.model.Card} identifier
	 */
	public void setCardId(ObjectId cardId) {
		this.cardId = cardId;
	}

	/**
	 * Gets size of file portion to be fetched
	 * @return number of bytes to be fetched
	 */
	public int getLength() {
		return length;
	}

	/**
	 * Sets size of file portion to be fetched
	 * @param length number of bytes to be fetched
	 */
	public void setLength(int length) {
		this.length = length;
	}

	/**
	 * Gets offset from the beginning of file to start fetching from
	 * @return number of bytes from beginning of file to skip
	 */
	public int getOffset() {
		return offset;
	}

	/**
	 * Sets offset from the beginning of file to start fetching from
	 * @param offset number of bytes from beginning of file to skip
	 */
	public void setOffset(int offset) {
		this.offset = offset;
	}

	/**
	 * @see ObjectAction#getObjectId()
	 */
	public ObjectId getObjectId() {
		return cardId;
	}

	/**
	 * @see com.aplana.dbmi.action.Action#getResultType
	 */
	public Class<byte[]> getResultType() {
		return byte[].class;
	}

	/**
	 * Gets version number of card to download material from.
	 * By default it is equals to {@link Material#CURRENT_VERSION} which means that
	 * current version is required.
	 * @return version of card to download material from.
	 */
	public int getVersionId() {
		return versionId;
	}

	/**
	 * Sets version number of card to download material from.
	 * @param versionId version of card to download material from.
	 */
	public void setVersionId(int versionId) {
		this.versionId = versionId;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
