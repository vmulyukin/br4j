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
import java.util.concurrent.Semaphore;

import com.aplana.dbmi.action.ConvertToPdf;
import com.aplana.dbmi.action.Material;
import com.aplana.dbmi.filestorage.convertmanager.EventListener;
import com.aplana.dbmi.filestorage.convertmanager.ManagerBean;
import com.aplana.dbmi.filestorage.convertmanager.Priority;
import com.aplana.dbmi.filestorage.convertmanager.Task;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.query.WriteQuery;
import com.aplana.dbmi.storage.ContentStorage;
import com.aplana.dbmi.storage.content.ContentReader;
import com.aplana.dbmi.storage.impl.PdfConvertorSettings;

/**
 * Query used to perform {@link ConvertToPdf} action.
 */
public class doConvertToPdf extends actionFileStorageUseBase implements WriteQuery {
	private static final long serialVersionUID = 1L;

	/**
	 * Identifier of 'Convert to pdf' action to be used in system log
	 */
	public static final String EVENT_ID = "CONVERT_TO_PDF"; // "CONVERT_TO_PDF";


	//------------------------------------------------------------pdf converter
	// private Semaphore semaphore = new Semaphore(1);
	InputStream result = null;
	Exception taskException = null;

	boolean flagnotify=false;
	//------------------------------------------------------------

	/**
	 * @return {@link #EVENT_ID}
	 */
	@Override
	public String getEvent() {
		return EVENT_ID;
	}

	public ConvertToPdf getConvertToPdf() {
		return getAction(); 
	}

	/**
	 * Returns identifier of {@link com.aplana.dbmi.model.Card} material of which is being converted
	 */
	@Override
	public ObjectId getEventObject() {
		return getConvertToPdf().getObjectId();
	}

	/**
	 * Converts the file attached to given 
	 * {@link com.aplana.dbmi.model.Card}/{@link com.aplana.dbmi.model.CardVersion} object 
	 * @return byte[] object representing file being downloaded in pdf format
	 */
	/*
	public void onEvent( Task task, Exception exception) { ... }
	}
	 */

	@Override
	public Object processQuery() throws DataException 
	{

		final ConvertToPdf convert = getConvertToPdf();
		final Material material  = convert.getMaterial();

		if ( material == null)  return null;

		this.result = null;
		this.flagnotify = false;
		this.taskException = null;

		// �������� ������� ����� � ���� ...
		final String cacheFSname = "\\$"+ PdfConvertorSettings.getCacheStorageName();
		String cacheURL  = null;
		ContentStorage storage = null;
		if (material.getUrl() != null)
		{
			// �������� ������������ �������� storage �� "cache "...
			cacheURL = material.getUrl().replaceAll( "\\$(.)+?\\b", cacheFSname);

			// ��������� ���-��������� ...
			try {
				storage = super.chkGetSingleStorageByURL(cacheURL, material.getCardId(), material.getName());
			} catch(Exception ex){
				logger.error( MessageFormat.format("fail to get cache storage ''{0}'' -> no chache is used", cacheFSname), ex);
			}
			if (storage == null) {
				logger.warn( MessageFormat.format("cache storage ''{0}'' not found", cacheFSname));
			} else {
				logger.debug( MessageFormat.format("cache storage ''{0}'' is used", cacheFSname));
				final ContentReader reader = storage.getReader(cacheURL);
				// TODO: ���� ������ ���������� ����� (!), �� ��� ���� �� �� ��������� ����
				if (reader != null) {
					// ������ ���� � ���� ...
					logger.info( MessageFormat.format("PDF image got from cache storage ''{0}'' ", cacheFSname));
					if (reader.getSize() > 0)
						result = reader.getContentInputStream();
					return result;
				}
			}
		}
		//------------------------------------------------------------pdf converter
			// getBeanFactory().getBean(StorageConst.BEAN_CONVERTER, ManagerBean.class);
			final ManagerBean mb =  ManagerBean.ensureGetBean( getBeanFactory() );

			final Semaphore semaphore = new Semaphore(1);
			mb.addTask(storage, material, Priority.immediate, new EventListener()
				{
					public void onEvent(Task task, Exception exception) {
						synchronized (semaphore) {	
							result = task.getResult();
							taskException = exception;
							flagnotify = true;
							semaphore.notify();
						}
				}} );

			try {
				synchronized (semaphore) {
					semaphore.wait(Long.valueOf(PdfConvertorSettings.getTimedOut()));
					if(taskException != null){
						logger.error( "PDF convertion failed:\n", taskException);
						throw new DataException("converter.pdf.fail_1", new Object[] {material.getName()}, taskException);
					}

					if(!flagnotify){
						logger.warn( "PDF convertion timeout: longer than "+ PdfConvertorSettings.getTimedOut() + " msec");
						throw new DataException("converter.timedout", new Object[]{ PdfConvertorSettings.getTimedOut() } );
					}

					if(result == null)
						throw new DataException("converter.fail_1", new Object[] {material.getName()});

				}
			} catch (InterruptedException e1) {
				logger.error( "PDF convertion timeout: longer than "+ PdfConvertorSettings.getTimedOut() + " msec", e1);
				// TODO: here result can be assigned as PDF with message "System is busy. Please  try later again."
			}
			// if(result==null) throw new DataException("nothing converted");
			return result;
	}

}
