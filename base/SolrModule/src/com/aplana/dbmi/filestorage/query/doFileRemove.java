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

import java.sql.Types;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.aplana.dbmi.action.RemoveFile;

import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.query.WriteQuery;
import com.aplana.dbmi.storage.ContentStorage;
import com.aplana.dbmi.storage.content.ContentWriter;

public class doFileRemove extends actionFileStorageUseBase implements WriteQuery {
	private static final long serialVersionUID = 1L;
	/**
	 * Identifier of 'Remove file' action to be used in system log
	 */
	public static final String EVENT_ID = "REMOVE_FILE";

	/**
	 * @return {@link #EVENT_ID}
	 */
	@Override
	public String getEvent() {
		return EVENT_ID;
	}

	public RemoveFile getRemoveFile() {
		return (RemoveFile) getAction();
	}

	/**
	 * Returns identifier of {@link com.aplana.dbmi.model.Card} material of
	 * which is being removed
	 */
	@Override
	public ObjectId getEventObject() {
		return getRemoveFile().getCardId();
	}

	@Override
	public Object processQuery() throws DataException {
		//TODO: ����������� � Solr
		final RemoveFile remove = getRemoveFile();
		final ObjectId cardId = remove.getCardId();
		if (cardId == null)
			throw new IllegalStateException(
					"Save the card before removing file");
		final String sCardId = cardId.getId().toString();
		final HashMap<Integer, FileInfo> infos = new HashMap<Integer, FileInfo>();
		if (remove.isRemoveAll()) {
			@SuppressWarnings("unchecked")
			List<Integer> versions = getJdbcTemplate().queryForList(
					"SELECT version_id FROM card_version WHERE card_id=?",
					new Object[] { cardId.getId() }, Integer.class);
			for (Integer i : versions)
				infos.put(i, queryCardFileInfo(cardId, i));			
		} else
			infos.put(remove.getVersionId(),
					queryCardFileInfo(cardId, remove.getVersionId()));
		
		for (Map.Entry<Integer, FileInfo> entry : infos.entrySet()) {
			final FileInfo info = entry.getValue();
			final String sOperInfo = MessageFormat.format(
					"cardid:{0}, name:{1}", sCardId, info.getFileUrl());
			logger.debug("file removing: " + sOperInfo);
			// ���� �������, �� �� ��������� �����
			if (info.getFileUrl() == null) {
				logger.error("fail to remove file: " + sOperInfo
						+ ", cause file already removed ");
				//return null;
			} else {

				try {

					final ContentStorage fstorage = super
							.chkGetSingleStorageByURL(info.getFileUrl(),
									cardId, info.getFileName());
					ContentWriter out = fstorage.getWriter(info.getFileUrl());
					out.delete();
					logger.info("file remove: "
							+ sOperInfo
							+ MessageFormat.format(", file:{0}, URL:{1}",
									info.getFileName(), info.getFileUrl()));
				} catch (Exception e) {
					logger.fatal("fail to remove file: " + sOperInfo, e);
					throw new DataException("action.download.data", e);
				}
			}
			// ������� ������ ����� (@see com.aplana.dbmi.storage.impl.StorageUtils.FS_VERSION_CURRENT)
			try {
				int current = getJdbcTemplate()
				.queryForInt(
						"SELECT MAX(version_id) FROM card_version WHERE card_id=?",
						new Object[] { cardId.getId() });
				if (entry.getKey() <= 0 || entry.getKey() == current) {

					// ���������� ������� ��������: ��� ����� + ������
					getJdbcTemplate()
							.update("UPDATE card SET file_name=NULL, file_store_url=NULL, external_path=NULL, is_active=0 WHERE card_id=?",
									new Object[] { cardId.getId() },
									new int[] { Types.NUMERIC });

					// ��������������� ������� "FileSize" ("������ �����")...
					getJdbcTemplate()
							.update("DELETE FROM attribute_value WHERE card_id=? AND attribute_code=?",
									new Object[] { cardId.getId(),
											Attribute.ID_FILE_SIZE.getId() });
					getJdbcTemplate()
							.update("DELETE FROM card_version WHERE card_id=? AND version_id=?",
									new Object[] { cardId.getId(), current });
				} else { // �� card_version
					getJdbcTemplate()
					.update("DELETE FROM card_version WHERE card_id=? AND version_id=?",
							new Object[] { cardId.getId(),
									entry.getKey().intValue()});
				}
			} catch (Exception ex) {
				throw (ex instanceof DataException) ? (DataException) ex
						: new DataException(ex);
			}
		}	
		
		return null;
	}

}
