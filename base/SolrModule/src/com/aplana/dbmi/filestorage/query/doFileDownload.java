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

import java.text.MessageFormat;

import org.springframework.dao.DataAccessException;

import com.aplana.dbmi.action.DownloadFile;
import com.aplana.dbmi.action.Material;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.query.WriteQuery;
import com.aplana.dbmi.storage.ContentStorage;
import com.aplana.dbmi.storage.content.ContentReader;

/**
 * Query used to perform {@link DownloadFile} action.<br>
 * NOTE: {@link Material} object returned by this query have only {@link Material#getLength() length}
 * and  {@link Material#getName() name} properties initialized. Its {@link Material#getData() data}
 * property will be null and should be initialized on client side manually.  
 */
public class doFileDownload extends actionFileStorageUseBase implements WriteQuery {
	private static final long serialVersionUID = 1L;
	/**
	 * Identifier of 'Download file' action to be used in system log
	 */
	public static final String EVENT_ID = "GET_FILE"; // "FILE_DOWNLOAD";

	/**
	 * @return {@link #EVENT_ID}
	 */
	@Override
	public String getEvent() {
		return EVENT_ID;
	}

	public DownloadFile getDownloadFile()
	{
		return (DownloadFile) getAction(); 
	}
	
	/**
	 * Returns identifier of {@link com.aplana.dbmi.model.Card} material of which is being downloaded
	 */
	@Override
	public ObjectId getEventObject() {
		return getDownloadFile().getCardId();
	}

	/**
	 * Gets information about file attached to given 
	 * {@link com.aplana.dbmi.model.Card}/{@link com.aplana.dbmi.model.CardVersion} object 
	 * @return {@link Material} object representing information about file being downloaded
	 */
	@Override
	public Object processQuery() throws DataException 
	{
		final DownloadFile download = getDownloadFile();
		final ObjectId cardId  = download.getCardId();
		if (cardId == null)
			throw new IllegalStateException("Save the card before downloading file");
		final String sCardId = cardId.getId().toString();

		final String sOperInfo = MessageFormat.format( 
				"cardid:{0}, version:{1}",
				sCardId, download.getVersionId());
		logger.debug("file downloading: "+ sOperInfo);
		try {
			// �������� �� ��������� ��������� ...
			// final ContentStorageManager fstoreMan = super.getFSManager();
			final FileInfo info = queryCardFileInfo( cardId, download.getVersionId()); 

			// �������� ���� �� ��������� ��������� ...
			final ContentStorage fstorage = super.chkGetSingleStorageByURL(info.getFileUrl(), cardId, info.getFileName());
			final ContentReader reader = fstorage.getReader(info.getFileUrl());
			final int fileSize = (reader != null) ? (int) reader.getSize() : -1;
			if (fileSize < 0)
				throw new DataException("action.download.file.not.found.or.not.exists", 
						new Object[]{ sCardId + "(url='" + info.getFileUrl()+ "')"});

			// ������������ ���-�� �������� ������...
			final Material resultMaterial = new Material();

			resultMaterial.setCardId( cardId);
			resultMaterial.setVersionId(download.getVersionId());

			resultMaterial.setName( info.getFileName());
			resultMaterial.setUrl( info.getFileUrl());
			resultMaterial.setLength(fileSize);

			logger.info("file download: "+ sOperInfo 
					+ MessageFormat.format(", size:{0}, file:''{1}'', url: ''{2}''",
							resultMaterial.getLength(),
							resultMaterial.getName(),
							resultMaterial.getUrl()
							));
			
			return resultMaterial;
		} 
		catch ( DataAccessException e) {
			logFileVerError(e, sCardId, download.getVersionId(), "Error reading file attributes for card");
			throw new DataException("action.download.data", e);
		}
	}


}
