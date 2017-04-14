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
import com.aplana.dbmi.model.ObjectId;

/**
 * {@link com.aplana.dbmi.action.Action} used in {@link UploadFileStream}
 * to upload part of file into database.
 * <br>
 * returns null as result 
 */
public class UploadFilePart implements ObjectAction
{
	private static final long serialVersionUID = 2L;

	private ObjectId cardId;
	private byte[] data;

	private String url;
	private int length;
	private int offset;

	/**
	 * Gets an identifier of the {@link com.aplana.dbmi.model.Card} object for which 
	 * uploaded file part belongs  
	 * @return identifier of the {@link com.aplana.dbmi.model.Card} object for which 
	 * uploaded file part belongs
	 */
	public ObjectId getCardId() {
		return cardId;
	}

	/**
	 * Sets identifier of the {@link com.aplana.dbmi.model.Card} object for which uploaded file part belongs
	 * @param cardId identifier of the {@link com.aplana.dbmi.model.Card} object for which uploaded file part belongs
	 */
	public void setCardId(ObjectId cardId) {
		this.cardId = cardId;
	}

	/**
	 * Gets file part to be uploaded
	 * @return file part to be uploaded
	 */
	public byte[] getData() {
		return data;
	}

	/**
	 * Sets file part to be uploaded
	 * @param data binary file data to be uploaded
	 * @param length length of the uploaded data in bytes
	 */
	public void setData(byte[] data, int length) {
		this.data = data;
		this.length = length;
	}

	/**
	 * Gets size of uploaded file part in bytes
	 * @return size of uploaded file part in bytes
	 */
	public int getLength() {
		return length;
	}

	/**
	 * Gets offset from the file beginning of this file part
	 * @return offset of this file part
	 */
	public int getOffset() {
		return offset;
	}

	/**
	 * Sets offset from the file beginning of this file part
	 * @param offset offset of this file part
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
	 * @see com.aplana.dbmi.action.Action#getResultType()
	 */
	public Class getResultType() {
		return null;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
