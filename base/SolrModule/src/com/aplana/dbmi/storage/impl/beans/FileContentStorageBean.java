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
package com.aplana.dbmi.storage.impl.beans;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;
import java.util.List;

import javax.naming.NamingException;

import org.apache.commons.lang.Validate;

import com.aplana.dbmi.storage.FileStoreSchema;
import com.aplana.dbmi.storage.content.ContentReader;
import com.aplana.dbmi.storage.content.ContentWriter;
import com.aplana.dbmi.storage.content.exceptions.ContentException;
import com.aplana.dbmi.storage.content.exceptions.UnsupportedContentUrlException;
import com.aplana.dbmi.storage.impl.AbstractContentStorage;
import com.aplana.dbmi.storage.impl.FileContentReader;
import com.aplana.dbmi.storage.impl.FileContentWriter;
import com.aplana.dbmi.storage.impl.FileStorageSettings;
import com.aplana.dbmi.storage.impl.StorageConst;
import com.aplana.dbmi.storage.impl.StorageUtils;
import com.aplana.dbmi.storage.impl.url.URLStorageStreamHandler;
import com.aplana.dbmi.storage.utils.IOHelper;


/**
 * @author <a href="mailto:ogalkin@aplana.com">Oleg Galkin</a>
 * @author <a href="mailto:rabdullin@aplana.com">Ruslan Abdullin</a>
*/
public class FileContentStorageBean extends AbstractContentStorage {

	public static final String STORE_PROTOCOL = "filestore";


	private File rootDirectory;
	private FileStoreSchema fileStoreSchema;
	
	/**
	 * ��� ������� ����� bean.xml �������� �������� ������ ����� FileStorageSettings,
	 * ������� ����� � ����� �������, 
	 * ���� �������� null, �� ������������ �����������* rootDirectory ��������.
	 */
	private String configPropNameRootDirectory = null;


	/* (non-Javadoc)
	 * @see org.springframework.jndi.JndiObjectLocator#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() throws IllegalArgumentException,
			NamingException 
	{
		super.afterPropertiesSet();
		if (this.getConfigPropNameRootDirectory() != null) {
			final String cfgRoot = 
				FileStorageSettings.getProps().getProperty(this.getConfigPropNameRootDirectory());
			if (cfgRoot != null && cfgRoot.length() > 0)
				this.setRootLocation(cfgRoot);
		}
	}


	/**
	 * Default constructor.
	 */
	public FileContentStorageBean() {
		// initSupportedProtocols();
		// this.fileStoreSchema = new DefaultFileStoreSchema();
		this( null );
	}


	public FileContentStorageBean(String rootDir) {
		this(rootDir, new DefaultFileStoreSchema());
	}


	public FileContentStorageBean(String rootDir, FileStoreSchema schema) {
		Validate.notNull(schema, "File store schema must not be null");
		initSupportedProtocols();
		this.fileStoreSchema = schema;
		setRootLocation(rootDir);
	}


	private void initSupportedProtocols()
	{
		addSupportedProtocol( STORE_PROTOCOL);
	}


	public FileStoreSchema setFileStoreSchema() {
		return this.fileStoreSchema;
	}
	
	public void setFileStoreSchema(FileStoreSchema newSchema) {
		this.fileStoreSchema = newSchema;
	}


	/**
	 * @param rootDirectory the rootDirectory to set
	 */
	public void setRootDirectory(File rootFile) {
		if (this.rootDirectory == rootFile) 
			return;

		if (rootFile == null) {
			this.rootDirectory = null;
			super.setRootLocation(null);
			return;
		}
		// Validate.notNull(rootDir, "Root directory must not be null");
		// if ((rootFile == null) || "".equals(rootFile))
		//	rootFile = new File( StorageConst.ROOT_OFSTORAGE);// "./";

		/*
		// (2010/04/01, RuSA) ����� ��������� �� ����, �.�. ��� ���������������� 
		 * �� ���� ����� ���� ����� � rootDirectory � configPropNameRootDirectory 
		 * � �������� ����� ����� ��������� ������ ����� ������� ����������.
		 * � �����, ������� �������� �� ���� �������� ������.
		//  
		if (!rootFile.exists()) {
			if (!rootFile.mkdir())
				throw new ContentException("Failed to create storage root " + rootFile.getAbsolutePath());
		}
		 */

		this.rootDirectory = rootFile.getAbsoluteFile();
		super.setRootLocation( this.rootDirectory.getPath());
	}


	@Override
	public String getRootLocation() {
		try {
			return rootDirectory.getCanonicalPath();
		} catch (Exception e) {
			logger.warn("Unable to get root location", e);
			return super.getRootLocation();
		}
	}

	
	@Override
	public void setRootLocation(String newroot) {
		this.setRootDirectory( (newroot == null) ? null : new File(newroot) );
	}


	/**
	 * ��� ������� ����� bean.xml �������� �������� ������ ����� FileStorageSettings,
	 * ������� ����� � ����� �������, ���� �������� null, �� ������������ ����������� 
	 * rootDirectory ��������.
	 */
	public String getConfigPropNameRootDirectory() {
		return this.configPropNameRootDirectory;
	}


	/**
	 * ��� ������� ����� bean.xml �������� �������� ������ ����� FileStorageSettings,
	 * ������� ����� � ����� �������, ���� �������� null, �� ������������ ����������� 
	 * rootDirectory ��������.
	 */
	public void setConfigPropNameRootDirectory(String configPropNameRootDirectory) {
		this.configPropNameRootDirectory = configPropNameRootDirectory;
	}


	@Override
	public boolean isUrlSupported(URL url) 
	{
		if (url == null) 
			return false;

		// ��������� ��������...
		if (url.getProtocol() != null) { 
			if (!super.isUrlSupported(url))
				return false;
		}

		url = makeRelUrl(url);
		// ��������� ����...
		// && url.getPath().startsWith( rootDirectory.getAbsolutePath())
		// ������������� ��� ������� storage url - ������������ ��� �������
		return (url.getPath() != null) && url.getPath().startsWith( StorageConst.ROOT_OF_STORAGE);
	}

	protected URL makeRelUrl( String contentUrl) throws MalformedURLException
	{
		return makeRelUrl( new URL( null, contentUrl, this.getUrlHandler()) );
	}

	protected URL makeRelUrl( URL url) // throws MalformedURLException
	{
		if (url == null) return null;
		final URLStorageStreamHandler h = this.getUrlHandler();
		if (h != null && url.getPath() != null && !url.getPath().startsWith(StorageConst.ROOT_OF_STORAGE))
		{	// ���� �� ������������� ���� (���������� �� � "./") ...
			try {
				final URLConnection conn = h.openConnection(url);
				url = conn.getURL();
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
		return url;
	}

	@Override
	public ContentReader getReader(String contentUrl) 
		throws UnsupportedContentUrlException
	{
		if (contentUrl == null) return null;
		contentUrl = StorageUtils.normalizeUrl(contentUrl);
		try {
			final URL url = makeRelUrl( contentUrl);
				final File file = chkOpenFileRead(url);
				final ContentReader reader = file.exists() ? new FileContentReader( this, file, url) : null;
			logger.debug( MessageFormat.format( "ContentReader {0} created for content URL \"{1}\"", reader, contentUrl));
			return reader;
		} catch (MalformedURLException ex) {
			throw new UnsupportedContentUrlException( this, contentUrl, ex);
		} 
	}

	@Override
	public ContentWriter getWriter(String contentUrl)
		throws UnsupportedContentUrlException
	{
		try {
			final boolean isnew = (contentUrl == null);

			// (!) ������������ ����� �� �������������...
			contentUrl = StorageUtils.normalizeUrl( (isnew) ? generateNewUrl() : contentUrl );

			final URL url = makeRelUrl( contentUrl);
			final File file = chkOpenFileWrite( url, isnew);

			// create writer
			final ContentWriter writer = new FileContentWriter( this, file, url);
			logger.debug( MessageFormat.format( "ContentWriter {0} created for content URL \"{1}\"", writer, url));
			return writer;
		} 
		catch (MalformedURLException ex) {
			throw new UnsupportedContentUrlException( this, contentUrl, "Failed to get writer", ex);
		}
		catch (IOException e) {
			throw new ContentException( "Failed to get writer", e);
		}
	}


	/**
	 * ���������� URL ��� ������ �����.
	 * @return
	 * @throws IOException
	 */
	String generateNewUrl() {
		return StorageUtils.normalizeUrl( fileStoreSchema.createUniqueUrl(this));
	}

	/**
	 * ��������� ������������ URL � ���������� ��������� ����.
	 * @param url
	 * @return ��������� ���� ����-��� ���������� URL.
	 * @throws MalformedURLException
	 */
	private File chkOpenFileRead(URL url) 
		throws UnsupportedContentUrlException 
	{
		makeRelUrl(url);
		if (!isUrlSupported(url))
			throw new UnsupportedContentUrlException(this, (url == null) ? null : url.getPath() );

		final File result = new File (rootDirectory, makeLocalFileName(url.getPath()) );
		// if (!result.exists()) throw new UnsupportedContentUrlException(this, result.getAbsolutePath() );
		return result;
	}


	// static String[] tryEncoding = new String[] { "utf-8", "ascii" };
	/**
	 * �������� ��� ����� ����� ������������� url ����.
	 * @param urlPath
	 * @return
	 */
	String makeLocalFileName(String urlPath)
	{
		String result = null;
		try {
			result = java.net.URLDecoder.decode( urlPath, "utf-8");
		} catch (UnsupportedEncodingException e) {
			result = urlPath;
		}
		return result;
	}

	/**
	 * 
	 * @param contentUrl
	 * @param forceCreate
	 * @return
	 * @throws IOException
	 */
	private File chkOpenFileWrite(URL contentUrl, boolean forceCreate) 
		throws IOException 
	{
		File file= null;
		String stageMsg = "open file"; 
		try {
			 file = chkOpenFileRead(contentUrl);

			// create the directory if it doesn't exist
			 stageMsg = "prepare directories";
			if (!IOHelper.ensureDirs(file.getParentFile()))
				throw new IOException( MessageFormat.format( "Fail to create directories by \"{0}\"", file.getParentFile()));

			// create a new empty file
			 stageMsg = "check existance";
			if (forceCreate || !file.exists()) {
				stageMsg = "create file";
				if (!file.createNewFile())
					throw new IOException( "Fail to create file");
			}

		} catch(Exception ex) {
			// stageMsg = "Check existance by URL \"{0}\"";
			final String info = MessageFormat.format( "Open file by URL \"{0}\" \n\t error at {1}", contentUrl, stageMsg);
			logger.error(info, ex);
			throw new IOException( info);
		}

		return file;
	}

	/*
	 * ���������������� ����� ��������� ����������.
	 */
	public void setListProtocols(List<Object> list)
	{
		if (list == null) return;
		protocolsSupported.clear();
		for (Object item: list) {
			this.addSupportedProtocol((String) item);
		}
	}


	/*
	private String makeContentUrl(File file) {

		final String path = file.getAbsolutePath();

		// check if it belongs to the store
		if (!path.startsWith(rootDirectory.getAbsolutePath()))
			throw new ContentException( "File " + file + " does not belong to the root location");

		// strip off the file separator char if present
		int index = rootDirectory.getAbsolutePath().length();
		if (path.charAt(index) == File.separatorChar) index++;

		// strip off the root path and adds the protocol prefix
		final String url = STORE_PROTOCOL + StorageConst.PROTOCOL_DELIMITER + path.substring(index);
		// replace '\' with '/' so that URLs are consistent across all file systems
		return url.replace('\\', '/');
	}
	 */

}
