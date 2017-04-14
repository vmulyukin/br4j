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
package com.aplana.dbmi.storage.impl;

import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.apache.commons.logging.Log;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.aplana.dbmi.filestorage.query.FileInfo;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.storage.utils.TranslitConvertor;

/**
 * @author <a href="mailto:rabdullin@aplana.com">Ruslan Abdullin</a>
 */
public final class StorageUtils {

	/**
	 * �������� � ������������ ����, ����� ���������� ������� � ������� �������� ����� �� ������.
	 * @param wildUrl
	 * @return
	 */
	public static String normalizeUrl( String wildUrl )
	{
		return (wildUrl == null) ? "" : wildUrl.trim().replace('\\', '/'); 
	}


	/**
	 * �������� ������ ����:
	 *  <��������>://<�����>:<������>@<����>:<����>/<����>
	 */
	public static String compileURL( String protocol, String authority, String host, int port, String filePart)
	{
		final StringBuffer buf = new StringBuffer();
		if (protocol != null && !protocol.equals(""))
			buf.append(protocol.trim()).append(StorageConst.TAIL_PROTOCOL);

		if (host != null && !host.equals("")) { 

			buf.append(StorageConst.PREFIX_HOST); // "//"

			if (authority != null && !authority.equals(""))
				buf.append(authority.trim()).append(StorageConst.DELIMITER_AUTH);
			buf.append(host.trim());

			if (port >= 0)
				buf.append(':').append(port);
			
			buf.append(StorageConst.DELIMITER_URL_LEVELS);
		}
		if (filePart != null) { 
			// if (!filePart.startsWith(StorageConst.DELIMITER_URL_LEVELS)) buf.append(StorageConst.DELIMITER_URL_LEVELS);
			buf.append(filePart.trim()); 
		}
		return StorageUtils.normalizeUrl( buf.toString());
	}


	/**
	 * �������� ����� url, ������� path-����� � url.
	 * @param urlBase
	 * @param newFilePart
	 * @return
	 */
	public static String compileURL( URL urlBase, String newFilePart )
	{
		return compileURL( urlBase.getProtocol(), urlBase.getAuthority(), urlBase.getHost(), urlBase.getPort(), newFilePart);
	}

	/**
	 * ������ �������� ������ ��� ��������� � URL.toString() (��� ���������� ������������)
	 * @param urlBase
	 * @return
	 */
	public static String compileURL( URL urlBase)
	{
		return compileURL( urlBase.getProtocol(), urlBase.getAuthority(), urlBase.getHost(), urlBase.getPort(), urlBase.getFile());
	}


	/**
	 * ������� ���������� ������� � ��������� � ������� �������.
	 * ������ ������ � null-�������� ����� ������������ @link StorageConst.defaultStorageName.
	 * @param wildStorageName
	 * @return
	 */
	public static String normalizeStorageName(String wildStorageName)
	{
		if (wildStorageName == null)
			wildStorageName = StorageConst.STORAGE_NAME_DEFAULT;
		else {
			wildStorageName = wildStorageName.trim();
			if ("".equals(wildStorageName))
				wildStorageName = StorageConst.STORAGE_NAME_DEFAULT;
		}
		return wildStorageName.toLowerCase();
	}


	/**
	 * ������� ���������� ������� � ��������� � ������ �������.
	 * ������ ������ � null-�������� ����� ������������ @link StorageConst.defaultProtocol.
	 * @param protocol
	 * @return
	 */
	public static String normalizeProtocol(String protocol)
	{
		if (protocol == null)
			protocol = StorageConst.PROTOCOL_DEFAULT;
		else {
			protocol = protocol.trim();
			if ("".equals(protocol))
				protocol = StorageConst.PROTOCOL_DEFAULT;
		}
		return protocol.trim().toLowerCase();
	}


	/**
	 * �����, ��������������� ������� ������ �����.
	 */
	public static int FS_VERSION_CURRENT = 0;


	/**
	 * ��������� ���������� �� �����.
	 * @param jdbc	���������� ��� ������ ���� ����� �� ��,
	 * @param cardId	��������,
	 * @param version	������ ����� (��. ����� FS_VERSION_XXX)
	 * @param logger	������ (�.�. null)
	 * @return	���� � ������ ����� � ��� url, 
	 * (!) ��� ������ � ������� ���������� ��������, �.�. ��� url � ��, 
	 * ����� ������������ �������������� ��� � ������� �����.
	 */
	public static FileInfo queryCardFileInfo(JdbcTemplate jdbc,
			ObjectId cardId, int version, Log logger) 
	{
		final int DEVIDER = 100;

		final String sqlText = (version <= 0) 
				// ������ � �������� ������� ��������...
					? "select file_name, file_store_url from card where card_id=?"
				// ������ � ������� ������ ...
					: "select file_name, file_store_url from card_version where card_id=? and version_id=" + version;
		FileInfo info = (FileInfo) jdbc.queryForObject(
				sqlText,
				new Object[] { cardId.getId() },
				new int[] { Types.NUMERIC },
				new RowMapper() {
					public Object mapRow(ResultSet rs, int rowNumber) throws SQLException {
						return new FileInfo( /* filename */rs.getString(1), /* fileUrl */ rs.getString(2));
					}}
			);

		if (	info.getFileName() != null &&
				( info.getFileUrl() == null || "".equals(info.getFileUrl()) )
			) 
		{	// ��� ��� url �������� ��� ������� �������� ��������� 
			// -> ���������� URL ��������
			final Long cardNum = (Long) cardId.getId();
			final StringBuffer oldStyleUrlBuf = new StringBuffer();

			// ������������ ������ ����, ������ ����: 
			// 		protocol:$BeanName/./grp/cardId/file_name_translit.ext.##ver
			// ��������: nullstorage:$nullFileStorage/./138/13860/13820-20100320155929265.pdf.##0
			//
			oldStyleUrlBuf
				// "protocol:"
				.append( StorageConst.PROTOCOL_AUTOURL).append( StorageConst.TAIL_PROTOCOL) 
				// "x:$y/./"
				.append( StorageConst.PREFIX_STORAGENAME).append( StorageConst.STORAGE_NAME_AUTO)
				.append( StorageConst.DELIMITER_URL_LEVELS).append( StorageConst.ROOT_OF_STORAGE)  // "/./"
				// "<cardGroup>/<cardId>/"
				.append( String.format("%d%s%d%s", 
						new Object[] { 	cardNum/DEVIDER, StorageConst.DELIMITER_URL_LEVELS, 
										cardNum, StorageConst.DELIMITER_URL_LEVELS}) )
				// "<file_Name>" 
				.append( TranslitConvertor.strEncode( info.getFileName()) )
				// ".##version"
				// �.�. ������ '#' ������������ ��� �������� url, �� �������� ��� � hex-���� (���('#')=35=0x23)...
				.append(".%23%23").append(version)
				;
			info = new FileInfo( info.getFileName(), oldStyleUrlBuf.toString());

			if (logger != null)
				logger.warn( "file '"+ info.getFileName()+ "' has null url in the DB -> auto url generated '"+ info.getFileUrl() +"'");
		}
		return info;
	}

	/**
	 * ���������� ������ queryCardFileInfo(4�����) ��� ��������� ���� ������� ������.
	 * @param jdbc
	 * @param cardId
	 * @param logger
	 * @return ���� �� ������� ������ �����.
	 */
	public static FileInfo queryCardFileInfo(JdbcTemplate jdbc, ObjectId cardId, Log logger)
	{
		return queryCardFileInfo( jdbc, cardId, FS_VERSION_CURRENT, logger);
	}

//	TODO: (2010/03/23, RuSA) ����� ������� ������������� � ������������� �� ������ ����������, ����� ����� �������� �������� ���: 
//	public static FileInfo queryCardFileInfo( JdbcTemplate jdbc, ObjectId cardId, int ver)
//	{
//		return (FileInfo) jdbc.queryForObject(
//				"select file_name, file_store_url \n" +
//				"from "+ ((ver<=0)? "card" : "card_version" ) +" \n" +
//				"where card_id=? ", 
//				new Object[] { cardId.getId() },
//				new int[] { Types.NUMERIC },
//				new RowMapper() {
//					public Object mapRow(ResultSet rs, int rowNumber)
//						throws SQLException 
//					{
//						return new FileInfo( /* filename */rs.getString(1), /* fileUrl */ rs.getString(2));
//					}}
//				);
//	}

}
