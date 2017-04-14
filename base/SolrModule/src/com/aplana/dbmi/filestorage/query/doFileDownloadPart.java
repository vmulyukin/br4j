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

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;

import org.apache.commons.io.IOUtils;

import com.aplana.dbmi.action.DownloadFile;
import com.aplana.dbmi.action.Material;
import com.aplana.dbmi.action.file.DownloadFilePart;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.storage.ContentStorage;
import com.aplana.dbmi.storage.content.exceptions.ContentException;

/**
 * Query used to perform {@link DownloadFile} action.<br>
 * NOTE: {@link Material} object returned by this query have only {@link Material#getLength() length}
 * and  {@link Material#getName() name} properties initialized. Its {@link Material#getData() data}
 * property will be null and should be initialized on client side manually.  
 */
public class doFileDownloadPart extends actionFileStorageUseBase {
	private static final long serialVersionUID = 1L;
	/**
	 * Identifier of 'Download file' action to be used in system log
	 */
	public static final String EVENT_ID = "GET_FILE"; // "FILE_DOWNLOADPART";

	/**
	 * @return {@link #EVENT_ID}
	 */
	@Override
	public String getEvent() {
		return EVENT_ID;
	}

	public DownloadFilePart getDownloadFilePart()
	{
		return (DownloadFilePart) getAction(); 
	}
	
	/**
	 * Gets information about file attached to given 
	 * {@link com.aplana.dbmi.model.Card}/{@link com.aplana.dbmi.model.CardVersion} object 
	 * @return {@link Material} object representing information about file being downloaded
	 */
	@Override
	public Object /* byte[] */ processQuery() throws DataException 
	{
		final DownloadFilePart part = getDownloadFilePart();
		final ObjectId cardId  = part.getCardId();
		if (cardId == null) throw new IllegalStateException("Save card before downloading file");
		final String sCardId = cardId.getId().toString();

		final String sOperInfo = MessageFormat.format( 
				"cardid:{0}, version:{1}, offset:{2}, buffer size: {3}, url: ''{4}''",
				sCardId, part.getVersionId(), part.getOffset(), part.getLength(), part.getUrl() );
		logger.debug("file part downloading: "+ sOperInfo);
		try {
			// �������� �� ��������� ��������� ...
			final ContentStorage fstorage = super.chkGetSingleStorageByURL( part.getUrl(), cardId, ""/*filename*/);
			// final FilePartsDownloader partDownloader = fstorage.getFileDownloader( null, sCardId, part.getVersionId());
			// final ByteBuffer result = partDownloader.getFilePart( part.getOffset(), part.getLength() );

			//final ByteBuffer result = fstorage.getReader(part.getUrl()).getContentBytes(); 
			InputStream is = fstorage.getReader(part.getUrl()).getContentInputStream();
			byte [] buf = null;
			try {
				is.skip(part.getOffset());
				int bufLen = is.available();
				if (bufLen > part.getLength())
					bufLen = part.getLength();
				buf = new byte[bufLen];
				is.read(buf);
			} catch (IOException e) {
				logger.error(e);
			} finally {
				IOUtils.closeQuietly(is);
			}
			
			logger.info("file part downloaded (got from filestorage): "+ sOperInfo 
					+ MessageFormat.format(" read len: {0}", buf.length));

			// return (result != null) ? result.array() : new byte[0];
			return buf;

		} catch (ContentException ex) {
			logFileVerError(ex, sCardId, part.getVersionId(), "Error reading file part at "+sOperInfo);
			throw new DataException("action.download.data", ex);
		}
	}
	
}
