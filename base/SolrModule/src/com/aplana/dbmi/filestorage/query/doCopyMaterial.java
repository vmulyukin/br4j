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
import java.text.MessageFormat;

import org.apache.commons.io.IOUtils;
import org.hsqldb.Types;

import com.aplana.dbmi.action.Material;
import com.aplana.dbmi.action.file.CopyMaterial;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.query.WriteQuery;
import com.aplana.dbmi.storage.ContentStorage;
import com.aplana.dbmi.storage.content.ContentReader;
import com.aplana.dbmi.storage.content.ContentWriter;
import com.aplana.dbmi.storage.content.exceptions.ContentException;

public class doCopyMaterial extends actionFileStorageUseBase implements WriteQuery {
	private static final long serialVersionUID = 1L;

	@Override
	public Object processQuery() throws DataException 
	{
		final CopyMaterial action = (CopyMaterial)getAction();
		final Object srcId = action.getFromCardId().getId();
		final Object dstId = action.getToCardId().getId();		
		final ObjectId srcCardId = new ObjectId(Card.class, srcId);		
		final ContentStorage dstFstorage = super.getDefaultFileStorage();
		final FileInfo srcInfo = queryCardFileInfo(srcCardId, Material.CURRENT_VERSION);
		
		if (srcInfo.getFileName() == null||srcInfo.getFileName().isEmpty()){
			if (logger != null)
				logger.warn( "Source file has null FileName");
			return null;
			
		}
		
		final ContentStorage srcFstorage = super.chkGetSingleStorageByURL(srcInfo.getFileUrl(), srcCardId, srcInfo.getFileName());
		final ContentReader reader = srcFstorage.getReader(srcInfo.getFileUrl());
		final int fileSize = (reader != null) ? (int) reader.getSize() : -1;
		if (fileSize < 0) 
		{
			logger.info(MessageFormat.format("{0}: No valid attachment for card {1}. Exiting.", getClass(), srcId));
			return null;
		}

		ContentWriter writer = dstFstorage.getWriter(null);
		
		//������ ����� ������������� �������������
		String dstUrl = (writer.getContentUrl() != null) ? writer.getContentUrl().toExternalForm() : null;
		
		//����������� ���� �����
		//writer.putContent(reader.getContentBytes());
		try {
			IOUtils.copy(reader.getContentInputStream(), writer.getContentOutputStream());
		} catch (ContentException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		}
		
		String filename = (String)getJdbcTemplate().queryForObject(
			"select file_name from card where card_id = ?",
			new Object[] { srcId },
			new int[] { Types.NUMERIC },
			String.class
		);		

		try {
			// ���������� ������� ��������
			updateCardTable(filename, fileSize, dstUrl, action.getToCardId());

			// ���������� ���������� �������
			// �������� �� ����������� ������������� ������
			if (dstFstorage != null)
			{
				final ContentReader freader =  dstFstorage.getReader(dstUrl);
				// update Solr index ...
				updateSearchIndex( dstId.toString(), freader, filename, null);
				logger.debug("post updateSearchIndex successfull,  URL: "+ dstUrl);
			}
		} catch (Exception ex) {
			throw (ex instanceof DataException) ? (DataException)ex: new DataException(ex);
		}
		/*
		getJdbcTemplate().update(
			"update card dst \n" +
			"set \n" +
			"\t dst.file_name = (?), \n" +
			"\t dst.external_path = src.external_path, \n" +
			"\t dst.file_store_url = src.file_store_url \n" +
			"from card src \n" +
			"where src.card_id = (?) and dst.card_id = (?) ",
			new Object[] { filename, srcId, dstId },
			new int[] { Types.VARCHAR, Types.NUMERIC, Types.NUMERIC }
		);
		*/
		//(2010/03/19, RuSA) ���� �� ���������� � ����� ���������? ������ �� ��������.
		// 25.01.2011, �.�. - ����, �������.
		logger.info( MessageFormat.format( "{0}: material copied from Card#{1} to Card#{2} ", 
				getClass(), srcId, dstId ));

		return null;
	}
}
