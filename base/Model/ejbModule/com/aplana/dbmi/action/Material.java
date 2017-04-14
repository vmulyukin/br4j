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

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;

/**
 * Model class used to represent file attached to given {@link Card} object.<br>
 * Also could be used to represent previous version of file attached to
 * given {@link com.aplana.dbmi.model.CardVersion} object.
 * <br>
 * If value returning by {@link #getVersionId()} is equal to {@link #CURRENT_VERSION}
 * then this Material object represents file attached to current version of {@link Card}.
 * If it is not then this Material file represents file attached to one of the
 * {@link com.aplana.dbmi.model.CardVersion previous editions} of the {@link Card}.
 */
public class Material implements Serializable
{
	private static final long serialVersionUID = 4L;

	/**
	 * Constant used as a version number for current version of {@link Card} objects
	 */
	public static final int CURRENT_VERSION = 0; // (2009/11/30, RuSA) OLD: (-1);
	private String name;
	private int length;
	private ObjectId cardId;
	private int versionId = CURRENT_VERSION;
	private String url;

	transient private InputStream data;

	/**
	 * Gets {@link InputStream} containing file body.
	 * Usually it will be a {@link com.aplana.dbmi.action.file.DownloadFileStream} instance. 
	 * @return {@link InputStream} containing file body
	 */
	public InputStream getData() {
		return data;
	}
	
	/**
	 * Sets {@link InputStream} containing file body.
	 * Usually it will be a {@link com.aplana.dbmi.action.file.DownloadFileStream} instance. 
	 * @param data {@link InputStream} containing file body
	 */
	public void setData(InputStream data) {
		this.data = data;
	}
	
	/** @deprecated */
	public IOException getError() {
		return null;
	}

	/**
	 * Gets size of attached file in bytes
	 * @return size of attached file in bytes
	 */
	public int getLength() {
		return length;
	}
	
	/**
	 * Sets size of attached file in bytes
	 * @param length desired value of file size
	 */
	public void setLength(int length) {
		this.length = length;
		//this.data = new byte[length];
	}
	
	/**
	 * Gets name of attached file
	 * @return name of attached file
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Sets name of attached file
	 * @param name of attached file
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets identifier of {@link Card} object to which this material is attached.
	 * Please note that if this material is attached to {@link com.aplana.dbmi.model.CardVersion},
	 * the value returning by this method will be a identifier of corresponding {@link Card} anyway.
	 * @return identifier of {@link Card} object to which this material is attached
	 */
	public ObjectId getCardId() {
		return cardId;
	}

	/**
	 * Sets identifier of {@link Card} object to which this material is attached.
	 * Please note that if this material is attached to {@link com.aplana.dbmi.model.CardVersion}, 
	 * the value of this property should be a {@link Card} identifier anyway.
	 * @param cardId identifier of {@link Card}
	 */
	public void setCardId(ObjectId cardId) {
		if (!Card.class.equals(cardId.getType()))
			throw new IllegalArgumentException("Not a card id");
		this.cardId = cardId;
	}

	/**
	 * Gets card version number to which this material is attached.
	 * If value returning by this method is equal to {@link #CURRENT_VERSION}
	 * then this Material object represents file attached to current version of {@link Card}.
	 * If it is not then this Material file represents file attached to one of the
	 * {@link com.aplana.dbmi.model.CardVersion previous editions} of the {@link Card}.
	 * @return card version number
	 */
	public int getVersionId() {
		return versionId;
	}

	/**
	 * Sets version of {@link Card} to which this material is attached.
	 * By default value of this property is set to {@link #CURRENT_VERSION} and
	 * material represents current version of file. 
	 * @param versionId card version number
	 */
	public void setVersionId(int versionId) {
		this.versionId = versionId;
	}

	/**
	 * The file url like: 
	 * 		<protocol>://<login_name>:<psw>@<host>:<port>/<storage_name>/<file_name>
	 * Example: "filestore://localhost/@default/2010/...".
	 * @return
	 */
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
