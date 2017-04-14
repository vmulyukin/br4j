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

/**
 * @author <a href="mailto:rabdullin@aplana.com">Ruslan Abdullin</a>
 */
public final class StorageConst {

	/** The delimiter that must be found in all URLs, i.e <b>:</b>. */
	public static final String TAIL_PROTOCOL = ":";
	
	/*
	 * The prefix of the hostname if any.
	 */
	public static final String PREFIX_HOST = "//";

	/**
	 * The delimiter (<b>@</b>) of host and user:passsword parts in the URL: 
	 * 		<��������>://<�����>:<������>@<����>:<����>/<����>
	 */
	public static final String DELIMITER_AUTH = "@";

	/**
	 * ������� � URL, �� �������� ������������ ��� ��������� ������ ���� � URL.
	 * �h����, ���� ��� ����, �� ��� ������ ���� ����� ������ � ����.
	 */
	final public static String PREFIX_STORAGENAME = "$";


	/**
	 * ����������� �� ����� storage
	 */
	public static final String ROOT_OF_STORAGE = "./";

	/**
	 * ����������� ���� (���������) ������ name-����� ������ URL. 
	 */
	public static final String DELIMITER_URL_LEVELS = "/";

	/**
	 * �������� ������ ������������ �� ���������. 
	 * ����� ������ ������������ ���� � URL ��� ������ �� ������� ����.
	 */
	public static final String STORAGE_NAME_DEFAULT = "default";

	/**
	 * �������� ��������� ������������� �� ���������. 
	 * ���� �������� ������������ ���� � URL �������� �� ������ ����.
	 */
	public final static String PROTOCOL_DEFAULT = "filestore";

	/**
	 * �������� ��� �������, ����� �� ������ ���� URL ������������� �����. 
	 */
	public final static String PROTOCOL_AUTOURL = "autostore";

	/**
	 * ��� ��������� ������������ ��������.
	 */
	public static final String BEAN_FSManager = "fileStorageManagerBean"; // ""java:/filestorage/DBMI"

	/**
	 * ��� ������ �������� ������ �� ���������.
	 */
	public static final String BEAN_FSDefault = "defaultFileStorageBean"; 

	public static final String BEAN_CONVERTER = "defaultPdfConvertMgrBean"; 

	/**
	 * ��� ������ �������� ������ � ���������� URL.
	 */
	public static final String BEAN_FSAUTO = "autoFileStorageBean"; 
	public static final String STORAGE_NAME_AUTO = "autoFileStorage";

	/**
	 * ��� ������ �������������� ��� ������ ������.
	 */
	public static final String BEAN_SearchDefault = "defaultSearchIndexBean";

	/**
	 * ��� ���������� ������� ��� ����������� ����� ������.
	 */
	public static final String BEAN_ContentTypeReestr = "fileContentTypeInfoBean";

	/**
	 * ����� ������, ��������������� ������� ������ �����.
	 */
	public static int FS_VERSION_CURRENT = 0;


	/**
	 * �������� ������, ����-��� ��������� "��� ������ ����� � ���������"
	 */
	public final static int FS_VERSION_NOFILE = -1;

}
