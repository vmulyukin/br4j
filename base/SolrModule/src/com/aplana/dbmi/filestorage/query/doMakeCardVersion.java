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

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Types;
import java.text.MessageFormat;

import org.springframework.jdbc.core.JdbcTemplate;

import com.aplana.dbmi.action.ChangeState;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardVersion;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.DatabaseClient;
import com.aplana.dbmi.service.impl.ProcessorBase;
import com.aplana.dbmi.service.impl.query.DoChangeState;
import com.aplana.dbmi.service.impl.query.SaveCard;
import com.aplana.dbmi.storage.ContentStorage;
import com.aplana.dbmi.storage.ContentStorageManager;
import com.aplana.dbmi.storage.content.ContentWriter;
import com.aplana.dbmi.storage.content.exceptions.ContentException;
import com.aplana.dbmi.storage.impl.StorageConst;
import com.aplana.dbmi.storage.impl.StorageUtils;

/**
 * {@link ProcessorBase} descendant used to create new {@link CardVersion}
 * of given {@link Card} object before/after some action be executed.<br>
 * Typically this processor is used in conjuction with {@link SaveCard} or
 * {@link DoChangeState} queries.  
 */
public class doMakeCardVersion extends ProcessorBase implements DatabaseClient {
	private static final long serialVersionUID = 1L;
	private JdbcTemplate jdbc;

	public void setJdbcTemplate(JdbcTemplate jdbc)
	{
		this.jdbc = jdbc;
	}

	public JdbcTemplate getJdbcTemplate()
	{
		return jdbc;
	}

	private ObjectId getCardId()
	{
		if (getObject() != null)
			return getObject().getId();
		if (getAction() instanceof ChangeState)
			return ((ChangeState) getAction()).getObjectId();
		return null;
	}

	/**
	 * Creates new {@link CardVersion} representing current state of card being
	 * processed by query.
	 * @return null
	 */
	@Override
	public Object process() throws DataException
	{
		final ObjectId cardId = getCardId();
		if (cardId == null)
			return null;

		// ������� ��������� ������ ...
		//������� ����� �.�. 09.03.2011 
		//�������� �������� ������������ ������ ������. ������ �������� ������� ���������� ����� � ������ ����������� ����������� � ���������� ������ �������������� ������
		int version = getJdbcTemplate().queryForInt(
				"SELECT nextval('seq_version_id')");
		
		logger.info("New version = " + version + " cardId = " + cardId.getId());
		/**int version = 0;
		try {
			// generate new sequential version
			version = getJdbcTemplate().queryForInt(
					"SELECT MAX(version_id) FROM card_version WHERE card_id=?",
					new Object[] { cardId.getId() });
		} catch(IncorrectResultSizeDataAccessException e) {
			// It's ok, there's no versions for this card
		}
		++version;*/
		
		// ���������� ��������� ������� action_log � version_id. ��� ���� ��������� ���� action_log_id ��� ����� ������.
		// PPanichev 26.09.2014
		int action_log_id = getJdbcTemplate().queryForInt(
				"SELECT nextval('seq_action_log_id')");
		getCurrentQuery().setReservedLogActionId(new Integer(action_log_id));
		logger.info("New action_log_id = " + action_log_id + " cardId = " + cardId.getId() + " version = " + version);		

		// �������� ������ � �������� ���������...
		final String newFullUrl = createFileVersion( cardId, version);
		
		//�������� ������� � 01.04.2011. � ������ �������� group by, ��� ��� ��������� �������� � ������������� �������� CHANGED
		//� ���������, ��� ��������� � ������ �������������� ����� xpkcard_version
		
		// ������� ����, �������� �� action_log_id
		// PPanichev 26.09.2014
		getJdbcTemplate().update(
				"INSERT INTO card_version \n" +
				"\t (card_id, version_id, action_log_id, status_id, parent_card_id, file_store_url, file_name, external_path) \n" +
				"\t SELECT c.card_id, ?, ?, c.status_id, c.parent_card_id, " +
				" /*file_storage*/ ?, file_name, external_path \n" +
				"\t FROM card c \n" +
				"\t WHERE c.card_id=? " +
				"\t group by c.card_id, c.status_id, c.parent_card_id, file_name, external_path",
				new Object[] { 
						new Integer(version), 
						new Integer(action_log_id),
						newFullUrl, 
						cardId.getId() 
					},
				new int[] { Types.NUMERIC, Types.NUMERIC, Types.VARCHAR, Types.NUMERIC }
			);

		// ����������� ���������� ...
		getJdbcTemplate().update(
				"INSERT INTO attribute_value_hist \n" +
				"\t (card_id, version_id, attribute_code, number_value, \n" +
				"\t  string_value, date_value, value_id, another_value) \n" +
				"\t\t SELECT card_id, ? as version_id, attribute_code, number_value, \n" +
				"\t\t        string_value, date_value, value_id, another_value \n" +
				"\t\t FROM attribute_value WHERE card_id=? ",
				new Object[] { new Integer(version), cardId.getId() },
				new int[] { Types.NUMERIC, Types.NUMERIC }
				);
		getJdbcTemplate().update(
				"INSERT INTO access_control_list_hist \n" +
				"\t (card_id, version_id, role_code, value_id, person_id) \n" +
				"\t\t SELECT card_id, ? as version_id, role_code, value_id, person_id \n" +
				"\t\t FROM access_control_list \n" +
				"\t\t WHERE card_id=? ",
				new Object[] { new Integer(version), cardId.getId() },
				new int[] { Types.NUMERIC, Types.NUMERIC }
				);
		return null;
	}

	/**
	 * @param cardId
	 * @param newVersion
	 * @throws DataException 
	 */
	private String createFileVersion(ObjectId cardId, int newVersion) 
		throws DataException 
	{
		// �������� � �������� ��������� ...
			if (cardId == null) 
				return null;

			final String sId = cardId.getId().toString();
			logger.debug( MessageFormat.format(
					"creating new file version: cardid:{0} new ver:{1}", 
					sId, newVersion));

			// ������ ������� ���� �� �����...
			final FileInfo cardInfo = 
				StorageUtils.queryCardFileInfo( this.getJdbcTemplate(), cardId, logger);

			if (cardInfo.getFileName() == null || "".equals(cardInfo.getFileName()))
				// ��� ������ �����
				return null;

			final ContentStorageManager mangr = this.getFSManager();
			final ContentStorage fstorage = this.chkGetSingleStorageByURL( cardInfo.getFileUrl(), cardId);

			final ContentWriter wr = fstorage.getWriter( cardInfo.getFileUrl());

			// final EOperationResult resCode = fstorage.snapshootFile(sId, newVersion);
			String resultUrl = null;

			String info = "";
			if (!wr.isExists()) {
				info = "current version not exists, nothing to copy";
			} else {
				info = "current version copied";
				final URL newUrl = wr.copy(null);
				resultUrl = mangr.makeExternalUrl(fstorage, newUrl);
			}
			logger.info( MessageFormat.format(
					"new file version created: cardid:{0} new ver:{1}, result: {2} at url: ''{3}''", 
					sId, newVersion, info, resultUrl ));
			return resultUrl;
//		catch (SearchException ex) {
//			ex.printStackTrace();
//			throw new DataException(ex);
//			// throw (ex instanceof DataException) ? (DataException) ex : new DataException(ex);
//		}
	}

//	// public static final String BEANFS = "fileStorage";
//	public FileStorageService getFileStorageService() {
//	return (FileStorageService) getBeanFactory().getBean(actionFileStorageUseBase.ActionFileStorageUseBase, FileStorageService.class);
//	}

	/**
	 * �������� �������� �������� ������.
	 * @return
	 */
	public ContentStorageManager getFSManager()
	{
		return (ContentStorageManager) getBeanFactory().getBean( StorageConst.BEAN_FSManager, ContentStorageManager.class);
	}

	/**
	 * �������� ��������� �� ���������� URL.
	 * @param url ���� � ����� � �����-���� ���������;
	 * @param infoCardId (��������������) id ��������, � ������� ��������� url;
	 * @return
	 * @throws DataException 
	 * @throws ContentException 
	 */
	public ContentStorage chkGetSingleStorageByURL( String url, ObjectId infoCardId) 
		throws DataException, ContentException
	{
		ContentStorage[] fstorages = null;
		try {
			fstorages = getFSManager().getContentStorageByUrl(url);
		} catch (MalformedURLException ex) {
			// url ���������������� ...
			throw new DataException( "store.files.url.invalid_1", new Object[] {url} , ex);
		}
		if (fstorages == null || fstorages.length == 0)
			// url ���������������� ...
			throw new DataException( "store.files.url.invalid_1", new Object[] {url} );

		if (fstorages.length > 1){ // ����� ����� -> ��������������...
			logger.warn( MessageFormat.format("multiple storage registered for card#{0} url {1} -> useing first one", 
					infoCardId, url )); 
		}

		return fstorages[0];
	}

}
