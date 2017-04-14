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

import com.aplana.dbmi.action.file.ActionPerformer;
import com.aplana.dbmi.action.file.ContinuousAction;
import com.aplana.dbmi.action.file.DatabaseIOException;
import com.aplana.dbmi.action.file.UploadFilePart;
import com.aplana.dbmi.action.file.UploadFileStream;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataService;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;
import com.aplana.dbmi.service.User;

/**
 * Action used to upload material file attached to given {@link Card}
 * into database.
 * <br>
 * Note that {@link Card} object should already exists in database, so
 * before uploading file to newly created card, store this card first.
 * <br>
 * Process of file uploading is divided in two stages.
 * <ul>
 * <li>
 * At first file body is uploaded into temporary table in db. This step is performed
 * in {@link #beforeMainAction()} methods
 * </li>
 * <li>
 * If first steps succeed, then {@link Card} object is updated with information about file
 * name, its name etc. At this step content of file is copied from temporary table
 * to corresponding record in CARD table
 * </li>
 * NOTE: this action must be performed via {@link DataServiceBean} wrapper only
 */
public class UploadFile implements ObjectAction<ObjectId>, ContinuousAction
{
	private static final long serialVersionUID = 2L;
	private ObjectId cardId;
	private ObjectId templateId;
	private String fileName;
	//private byte[] data;
	private int length;
	//private IOException error;

	private transient ActionPerformer service;
	private transient InputStream source;

	private transient String url = null;

	public String getUrl() {
		return url;
	}

	/**
	 * ��� ����� ������� ��-null �������� ����� ������������ ��������� ���������,
	 * ����� ����� ������������� ����� �������� � ��������� ��-���������.
	 * @param url
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * Gets identifier of {@link Card} object to which uploaded file belongs
	 * @return identifier of {@link Card} object to which uploading file belongs
	 */
	public ObjectId getCardId() {
		return cardId;
	}

	/**
	 * Sets identifier of {@link Card} object to which uploaded file belongs
	 * @param cardId identifier of {@link Card} object to which uploading file belongs
	 * @throws IllegalArgumentException if cardId is not a {@link Card} identifier.
	 */
	public void setCardId(ObjectId cardId) {
		if (cardId != null && !Card.class.equals(cardId.getType()))
			throw new IllegalArgumentException("Not a card ID");
		this.cardId = cardId;
	}

	/**
	 * Not used not
	 * TODO: remove?
	 */
	public ObjectId getTemplateId() {
		return templateId;
	}

	/**
	 * Not used now
	 * TODO: remove?
	 */
	public void setTemplateId(ObjectId templateId) {
		if (templateId != null && !Template.class.equals(templateId.getType()))
			throw new IllegalArgumentException("Not a template ID");
		this.templateId = templateId;
	}

	/**
	 * Gets name of uploaded file.
	 * It is a name of file itself without any directories.
	 * @return name of uploaded file.
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * Sets name of uploaded file.
	 * It should be a name of file itself without any directories.
	 * @param fileName desired value of the name of uploaded file
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * Gets size of uploaded file in bytes. Can be used for getting size of the file after successful upload (but not before!)
	 * @return size of uploaded file in bytes
	 */
	public int getLength() {
		return length;
	}

	/**
	 * Sets size of uploaded file in bytes
	 * @deprecated Setting this value actually doesn't do a thing. Use {@link UploadFilePart} to upload part of file.
	 * @param length size of uploaded files in bytes
	 */
	@Deprecated
	public void setLength(int length) {
		//this.length = length;
		//data = new byte[length];
	}

	/**
	 * Don't use this method
	 * @deprecated
	 */
	@Deprecated
	public InputStream getData() {
		throw new RuntimeException("Should not be called anymore");
		//return new ByteArrayInputStream(data);
	}

	/**
	 * Sets InputStream containing body of uploaded file.
	 * @param source InputStream containing body of uploaded file.
	 */
	public void setData(InputStream source) {
		this.source = source;
	}

	/**
	 * @deprecated
	 */
	@Deprecated
	public IOException getError() {
		return null;
	}

	/**
	 * @see Action#getResultType()
	 */
	public Class<ObjectId> getResultType() {
		return ObjectId.class;
	}

	/**
	 * @see ObjectAction#getObjectId()
	 */
	public ObjectId getObjectId() {
		return getCardId();
	}

	/**
	 * @see ContinuousAction#setService(DataService, User)
	 */
	public void setService(DataService service, User user) {
		DataServiceBean serviceBean = new DataServiceBean();
		serviceBean.setService(service, user);
		this.service = serviceBean;
	}

	/**
	 * @see ContinuousAction#setService(DataServiceBean)
	 */
	public void setService(ActionPerformer service) {
		this.service = service;
	}

	final static int BUFSIZE_UPLOADPART = 1024 * 1024;
	/**
	 * Initialization stage of action.
	 * At this point file body is uploaded to temporary storage in database.
	 * @return true if file body upload succeed, false otherwise
	 */
	public boolean beforeMainAction() throws DataException, ServiceException {
		try {
			final UploadFileStream upload = new UploadFileStream(this, service);

			length = 0;

			upload.setUrl(this.getUrl());

			final byte[] buf = new byte[BUFSIZE_UPLOADPART];
			while (true) {
				int len = source.read(buf);
				if (len == -1)
					break;
				upload.write(buf, 0, len);
				length += len;
			}
			upload.flush();

			this.setUrl( upload.getUrl());

			return true;

		} catch (IOException e) {
			if (e instanceof DatabaseIOException)
				throw ((DatabaseIOException) e).getDataException();
			throw new DataException(e);
		}
	}

	/**
	 * Do nothing
	 */
	public void afterMainAction(Object result) throws DataException, ServiceException {
	}
}
