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
/**
 * 
 */
package com.aplana.dbmi.filestorage.query;

import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.Types;

import com.aplana.dbmi.action.Material;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ActionQueryBase;
import com.aplana.dbmi.storage.ContentStorage;
import com.aplana.dbmi.storage.ContentStorageManager;
import com.aplana.dbmi.storage.content.ContentReader;
import com.aplana.dbmi.storage.content.exceptions.ContentException;
import com.aplana.dbmi.storage.impl.SolrContentReaderStream;
import com.aplana.dbmi.storage.impl.StorageConst;
import com.aplana.dbmi.storage.impl.StorageUtils;
import com.aplana.dbmi.storage.search.SearchException;
import com.aplana.dbmi.storage.search.SearchService;
import com.aplana.dbmi.storage.utils.FileUtils;

/**
 * @author RAbdullin
 * ������� ����� ��� ���������� ������������� FileStorageService bean'�, 
 * ���������/�������������� ���������� ��/��� ���������������� ������. 
 */
public abstract class actionFileStorageUseBase extends ActionQueryBase {
	private static final long serialVersionUID = 1L;

	/**
	 * �������� �������� �������� ������.
	 * @return
	 */
	public ContentStorageManager getFSManager()
	{
		return (ContentStorageManager) getBeanFactory().getBean( StorageConst.BEAN_FSManager, ContentStorageManager.class);
	}


	/**
	 * �������� �������� ��������� �� ���������.
	 * @return
	 */
	public ContentStorage getDefaultFileStorage()
	{
		/*
		final Object bean = getBeanFactory().getBean(BEANFS);
		if (bean == null || !(bean instanceof ContentStorage))
			throw new BeanNotOfRequiredTypeException( BEANFS, ContentStorage.class, 
							(bean != null) ? bean.getClass() : null);
		return (ContentStorage) bean;
		 */
		return (ContentStorage) getBeanFactory().getBean( StorageConst.BEAN_FSDefault, ContentStorage.class);
	}


	/**
	 * �������� ��������� ����������.
	 * @return
	 */
	public SearchService getSearchService()
	{
		// return getFSManager().getSearchIndexer();
		return (SearchService) getBeanFactory().getBean( StorageConst.BEAN_SearchDefault, SearchService.class);
	}

	/**
	 * ��������� �� �� ���� �� �����. 
	 * @param cardId	id ��������,
	 * @param vesrion	������ ����� (@SEE StorageConst.FS_VERSION_XXX)
	 * @return ��� ����� � ��� url �� ������ � ������� card. 
	 * (!) ���� url �� ����� ���� � ����, �� ��������� ����������� url 
	 * (��� ����� ������� ��������):
	 *  	autostore:$autoFileStorage/./(cardId/100)/cardId/...��������-�����-�����.../.##������
	 *  
	 *  ����� (cardId/100) = ����� ����� �� ������� id �������� �� 100.
	 */
	public FileInfo queryCardFileInfo(ObjectId cardId, int version) 
	{
		return StorageUtils.queryCardFileInfo( this.getJdbcTemplate(), cardId, version, this.logger);
	}


	/**
	 * �������� ��������� �� ���������� URL.
	 * @param url ���� � ����� � �����-���� ���������;
	 * @param infoCardId (��������������) id ��������, � ������� ��������� url;
	 * @param infoFileName (��������������) ��� �����, ���������� � url;
	 * @return
	 * @throws DataException 
	 * @throws ContentException 
	 */
	public ContentStorage chkGetSingleStorageByURL( String url, ObjectId infoCardId, String infoFileName) 
		throws DataException, ContentException
	{
		ContentStorage[] fstorages = null;
		try {
			fstorages = getFSManager().getContentStorageByUrl(url);
		} catch (MalformedURLException ex) {
			// e.printStackTrace();
			// url ���������������� ...
			throw new DataException( "store.files.url.invalid_1", new Object[] {url} , ex);
		}
		if (fstorages == null || fstorages.length == 0)
			// url ���������������� ...
			throw new DataException( "store.files.url.invalid_1", new Object[] {url} );

		if (fstorages.length > 1){ // ����� ����� -> ��������������...
			logger.warn( getInfo( fstorages, infoCardId, infoFileName, url).toString() );
		}

		return fstorages[0];
	}

	public static StringBuffer getInfo(final ContentStorage[] storages, 
			ObjectId cardId, 
			String filename, 
			String fileUrl)
	{
		final StringBuffer sb = new StringBuffer();
		sb	.append( "FileStorage: multiple storages found for card#" )
			.append( (cardId == null || cardId.getId() == null) ? null : cardId.getId().toString() )
			.append( "\n\t file '"+ filename+ "'")
			.append( "\n\t url '"+ fileUrl+ "' \n");
		for (int i = 0; i < storages.length; i++) {
			final ContentStorage contentStorage = storages[i];
			sb.append("\t").append(i+1).append("\t ").append(contentStorage).append("\n");
		}
		sb.append("\t only first is used \n");
		return sb;
	}

	/**
	 * ���������� ������� ��������: ��� ����� + ������
	 * 
	 * @param fileName: ��� �����
	 * @param fileLength: ������ �����
	 * @param contentUrl: URL
	 * @param cardId: card id
	 * 
	 */
	protected void updateCardTable(String fileName, int fileLength, String contentUrl, ObjectId cardId)
	{
		getJdbcTemplate().update(
				"UPDATE card SET file_name=?, file_store_url=?, external_path=NULL WHERE card_id=?",
				new Object[] {
					FileUtils.sanitizeFilename(fileName),
					contentUrl,
					cardId.getId()
			},
			new int[] { Types.VARCHAR, Types.VARCHAR, Types.NUMERIC });

		// ��������������� ������� "FileSize" ("������ �����")...
		getJdbcTemplate().update(
				"DELETE FROM attribute_value WHERE card_id=? AND attribute_code=?",
				new Object[] { cardId.getId(), Attribute.ID_FILE_SIZE.getId() });

		getJdbcTemplate().update(
				"INSERT INTO attribute_value (card_id, attribute_code, number_value) " +
				"VALUES (?, ?, ?)",
				new Object[] {
					cardId.getId(),
					Attribute.ID_FILE_SIZE.getId(),
					new Integer(fileLength)
			});
	}
	
	protected void updateSearchIndex( String idx, 
			ContentReader stm, 
			String fileName, 
			String contentType) 
	{
		try {
			final SolrContentReaderStream solrStm = 
				new SolrContentReaderStream( stm, contentType);
			getSearchService().index( idx, fileName, solrStm );
		} catch (SearchException ex) {
			logger.warn( "Solr search problem: ", ex);
		} catch (IOException ex) {
			logger.warn( "Solr index I/O problem: ", ex);
		}
		catch (Throwable ex) { // (OutOfMemoryError ex) {
			logger.error( "Solr index generic problem: ", ex);
		}
	}
	/**
	 * ������������� ����������.
	 * 
	 * @param cause: �������� ����������.
	 * 
	 * @param cardId: id ��������, ��������� ������; ����� ��������� ��� �����, 
	 * �� ���� "" ��� null, �� cardId �� ����� ����������.
	 *  
	 * @param ver: ������ ����� � �������� cardId, ����:
	 * 		(-1) (FileVersion.VERNOFILE) ��� 0 (Material.CURRENT_VERSION), 
	 * �� �� ������������.
	 * 
	 * @param info: ���� � log (����: "attribute read error").
	 *  
	 */
	protected void logFileVerError( Exception cause, final String cardId, 
			int ver, final String info)
	{
		final StringBuffer msg = new StringBuffer();
		
		msg.append(info);
		
		final boolean needCardId = (cardId != null) && (cardId.length() > 0);
		final boolean needVer = 
			(ver != StorageConst.FS_VERSION_NOFILE) && (ver != Material.CURRENT_VERSION);
		
		if (needCardId || needVer) {
			msg.append(" [");
			if (needCardId)	msg.append(" cardId=").append(cardId);
		    if (needVer) msg.append(" versionId=").append(ver);
			msg.append("]");
		}
		logger.error(msg.toString(), cause);
	}


	/**
	 * ��������� �������������� � ������ ������� ���������� c ����� DataException, 
	 * ������ ��� ��� �������� ����������.
	 *  
	 * @param cause: �������� ����������.
	 * 
	 * @param logit: true, ����� ��� �� ������������ �������� cause � ����������
	 * info (����: "attribute read error")
	 * 
	 * @param info: ���� � log (����: "attribute read error"), ������������ 
	 * ��� logit = true.
	 * 
	 * @throws DataException 
	 * 
	 */
	public void RethrowDataException( Exception cause, final String info, boolean logit) 
		throws DataException
	{
		if (logit) 
			logFileVerError(cause, null, StorageConst.FS_VERSION_NOFILE, info);

		final DataException rethrow = (cause instanceof DataException) 
				? (DataException) cause 
				: new DataException(cause);
		throw rethrow;
	}
}
