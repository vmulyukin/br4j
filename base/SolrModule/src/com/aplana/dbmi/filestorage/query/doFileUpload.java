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

import java.io.InputStream;
import java.text.MessageFormat;

import com.aplana.dbmi.action.Material;
import com.aplana.dbmi.action.UploadFile;
import com.aplana.dbmi.common.utils.file.MimeContentTypeReestrBean;
import com.aplana.dbmi.filestorage.convertmanager.ManagerBean;
import com.aplana.dbmi.filestorage.convertmanager.MaterialMapper;
import com.aplana.dbmi.filestorage.convertmanager.Priority;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.query.WriteQuery;
import com.aplana.dbmi.storage.ContentStorage;
import com.aplana.dbmi.storage.content.ContentReader;
import com.aplana.dbmi.storage.content.ContentWriter;
import com.aplana.dbmi.storage.content.exceptions.ContentException;
import com.aplana.dbmi.storage.impl.PdfConvertorSettings;

/**
 * Query used to perform {@link UploadFile} action.
 * @see doFileUploadPart
 */
public class doFileUpload extends actionFileStorageUseBase implements WriteQuery {
	private static final long serialVersionUID = 1L;
	/**
	 * Identifier of 'Download file' action to be used in system log
	 */
	public static final String EVENT_ID = "REG_FILE"; // "FILE_UPLOAD";

	/**
	 * @return {@link #EVENT_ID}
	 */
	@Override
	public String getEvent() {
		return EVENT_ID;
	}

	public UploadFile getUploadFile()
	{
		return (UploadFile) getAction(); 
	}
	
	/**
	 * @return identifier of {@link Card} whose material is being uploading
	 */
	@Override
	public ObjectId getEventObject() {
		return getUploadFile().getCardId();
	}

	/**
	 * Updates information in CARD table with information about uploaded file. Also modifies
	 * value of {@link Attribute#ID_FILE_SIZE} attribute.
	 * NOTE: content of file should be uploaded into temporary table before executing this method
	 * (see {@link UploadFile#beforeMainAction() method}  
	 */
	@Override
	public Object processQuery() 
		throws DataException
	{
		final UploadFile upload = this.getUploadFile();
		final ObjectId cardId  = upload.getCardId();
		if (cardId == null)
			throw new IllegalStateException("Save card before uploading file");
		final String sCardId = cardId.getId().toString();

		// ���������� � �������� ���������...
		final ContentStorage fstorage = super.getDefaultFileStorage();

		// final FileStorageService fstorage = super.getFileStorageService(); 
		// final FilePartsUploader partUploader = fstorage.getFileUploader(null);
		String contentType = MimeContentTypeReestrBean.DEFAULT_CONTENT_TYPE;
		ContentReader reader = fstorage.getReader(upload.getUrl());
		if (reader != null) {
			contentType = MimeContentTypeReestrBean.getMimeType(reader.getContentInputStream(), upload.getFileName());
		}

		final String sOperInfo = MessageFormat.format( "cardid:{0}, content:{1}, file:''{2}''", sCardId, contentType, upload.getFileName());
		String contentUrl = upload.getUrl();
		if (fstorage != null && contentUrl == null)
		{	// (!) ������ ��������� �����, ��-����� ������ �������� ������ ��� ���,
			// ������ ���� ������ diFileUploadPart ...
			ContentWriter out = null;
			try {
				// (!) ����� ���������� ������������� URL...
				out = fstorage.getWriter( upload.getUrl());
				out.setContentType(contentType);
				contentUrl = (out.getContentUrl() != null) ? out.getContentUrl().toExternalForm() : null;

				// partUploader.commitFileParts( sCardId, upload.getFileName(), contentType);
				logger.info( "upload commited: " + sOperInfo );

			} catch (Exception ex) {
				try {
					logger.warn( "dropping uploaded file dew to errors: " + sOperInfo, ex);
					// partUploader.dropFileParts( sCardId, upload.getFileName());
					if (out != null) out.delete();
					logger.warn( "dropped uploaded file (dew to errors): " + sOperInfo);
				} catch (Exception x){
					ex.printStackTrace();
					logger.fatal( "fail to drop uploaded file dew to double-exception: " + sOperInfo, ex);
				}

				throw (ex instanceof DataException) ? (DataException)ex: new DataException(ex);
			}
		}
		logger.info( "upload commited: " + sOperInfo + ", url '"+ contentUrl + "'" );

		//-----
		try {
			//----�������� ����������� ����� �� ��������� 
			final Material material = (Material)getJdbcTemplate().queryForObject(
										"SELECT file_name,file_store_url FROM card WHERE card_id=?", 
										new Object[]{ cardId.getId() }, 
										new MaterialMapper());
			logger.debug("Material was obtained '"+material.getUrl()+"' URL: "+contentUrl);
			if(material.getUrl()!=null){
				final String cacheURL = material.getUrl().replaceAll( "\\$(.)+?\\b", getCacheFileStorageName() );
				final ContentStorage cstorage = super.chkGetSingleStorageByURL(cacheURL, cardId, material.getName());
				final ContentWriter cwriter= cstorage.getWriter(cacheURL);
				cwriter.delete();
				logger.debug("old cached pdf file deleted: '"+cacheURL+ "', card_id: "+ cardId);
			}

			// ���������� ������� ��������
			updateCardTable(upload.getFileName(), upload.getLength(), contentUrl, cardId);

			// ���������� ���������� �������
			// �������� �� ����������� ������������� ������
			if (fstorage != null)
			{
				final ContentReader freader =  fstorage.getReader(contentUrl);
				// update Solr index ...
				updateSearchIndex( sCardId, freader, upload.getFileName(), contentType);
				logger.debug("post updateSearchIndex successfull,  URL: "+contentUrl);
			}

			// update pdf/a-1 converter image...
			if (fstorage != null)
			{
				final ContentReader freader =  fstorage.getReader(contentUrl);
				if (upload.getUrl()==null)
					upload.setUrl(contentUrl);
				cachePdfAImage( upload, freader.getContentInputStream());
			}

		} catch (Exception ex) {
			throw (ex instanceof DataException) ? (DataException)ex: new DataException(ex);
		}

		return cardId;
	}

	private String getCacheFileStorageName() {
		return "\\$"+ PdfConvertorSettings.getCacheStorageName();
	}

	private void  cachePdfAImage( UploadFile upload, InputStream stm) 
			throws ContentException, DataException 
	{
		final Material material = new Material();
		material.setCardId( upload.getCardId());
		material.setData( stm);
		material.setName( upload.getFileName());
		material.setUrl( upload.getUrl());			

		final String cacheURL  = material.getUrl().replaceAll( "\\$(.)+?\\b", getCacheFileStorageName() );
		final ContentStorage cstorage = super.chkGetSingleStorageByURL( cacheURL, material.getCardId(), material.getName());

		final ManagerBean mb =  ManagerBean.ensureGetBean( getBeanFactory() );

		mb.addTask(cstorage, material, Priority.background);
	}

}
