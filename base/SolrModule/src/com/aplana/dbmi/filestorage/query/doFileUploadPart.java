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
package com.aplana.dbmi.filestorage.query;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.MessageFormat;

import org.apache.commons.io.IOUtils;

import com.aplana.dbmi.action.file.UploadFilePart;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.storage.ContentStorage;
import com.aplana.dbmi.storage.content.ContentWriter;

/**
 * Query used to perform {@link UploadFilePart} action.
 */
public class doFileUploadPart extends actionFileStorageUseBase {
	private static final long serialVersionUID = 1L;
	/**
	 * Identifier of 'Download file' action to be used in system log
	 */
	public static final String EVENT_ID = "PUT_FILE"; // "FILE_UPLOADPART";

	/**
	 * @return {@link #EVENT_ID}
	 */
	@Override
	public String getEvent() {
		return EVENT_ID;
	}

	public UploadFilePart getUploadFilePart()
	{
		return (UploadFilePart) getAction(); 
	}
	
	/**
	 * @return identifier of {@link Card} whose material is being uploading
	 */
	@Override
	public ObjectId getEventObject() {
		return getUploadFilePart().getCardId();
	}

	/**
	 * Saves chunk of uploaded file in temporary table
	 * @return null
	 */
	@Override
	public Object processQuery() throws DataException
	{
		final UploadFilePart part = getUploadFilePart();
		final ObjectId cardId  = part.getCardId();

		if (cardId == null)
			throw new IllegalStateException("Save card before uploading file parts");

		String sOperInfo = "getting storage context...";
		try {
			// ���������� ����� ����� � �������� ���������
			final String sCardId = cardId.getId().toString();

			ContentStorage fstorage = null;
			ContentWriter writer = null;
			if (part.getUrl() != null) { 
				// ���������� ������������ ��� ...
				fstorage = super.chkGetSingleStorageByURL( part.getUrl(), cardId, ""/*filename*/);
				writer = fstorage.getWriter(part.getUrl());
			} else {
				// ���������� ����� ���...
				fstorage = super.getDefaultFileStorage();
				writer = fstorage.getWriter(null); // (!) ��� ������������ ����� ���
				part.setUrl( super.getFSManager().makeExternalUrl( fstorage, writer.getContentUrl() ));
			}

			sOperInfo = MessageFormat.format( 
					"cardid:{0}, offset:{1}, buffer size: {2}, url: ''{3}''",
					sCardId, part.getOffset(), part.getLength(), part.getUrl() );
			logger.debug("file part uploading: "+ sOperInfo);
			// partUploader.putFilePart( sCardId, FilePartsUploader.AUTOFILENAME, part.getOffset(), part.getData(), part.getLength());
			final InputStream src = new ByteArrayInputStream( part.getData(), 0, part.getLength()); 
			try {
			writer.putContent( src, part.getOffset(), part.getLength());
			} finally {
				IOUtils.closeQuietly(src);
			}

			logger.info("file part uploaded (put into filestorage): "+ sOperInfo);
		}
		catch (Exception ex)
		{
			logger.error("file part upload problem: "+ sOperInfo, ex);
			throw (ex instanceof DataException) ? (DataException)ex: new DataException(ex);
		}

		return null;
	}
}
