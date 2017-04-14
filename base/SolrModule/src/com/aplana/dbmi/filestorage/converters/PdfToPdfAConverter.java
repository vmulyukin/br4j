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
package com.aplana.dbmi.filestorage.converters;

import com.aplana.dbmi.action.Material;
import com.aplana.dbmi.common.utils.pdf.PdfUtils;
import com.aplana.dbmi.filestorage.convertmanager.Task;
import com.aplana.dbmi.pdfa.converter.PDFAConverter;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.storage.ContentStorage;
import com.aplana.dbmi.storage.content.ContentReader;
import com.aplana.dbmi.storage.content.ContentWriter;
import com.aplana.dbmi.storage.impl.PdfConvertorSettings;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;

/**
 * ��������� pdf � PDF/A-x ������.
 * ���������� PDFAConverter �� ������ Ghost4j
 * 
 * @author Vlad Alexandrov
 *
 */
public class PdfToPdfAConverter implements PdfConverter {
	protected final Log logger = LogFactory.getLog(getClass());
	
	private PDFAConverter pdfaConverter;

	public PdfToPdfAConverter(PDFAConverter pdfaConverter) {
		this.pdfaConverter = pdfaConverter;
	}
	
	@Override
	public void stop() {
		//������ �� ������ � ��� �����������		
	}

	@Override
	public InputStream processQuery(Task task) throws DataException {
		final Material  material = task.getMaterial();
		InputStream result = null;

		// �������� �������� ����    	
		final String cacheFSname = "\\$"+ PdfConvertorSettings.getCacheStorageName();

		ContentStorage storage = task.getStorage();
		String cacheURL = material.getUrl().replaceAll( "\\$(.)+?\\b", cacheFSname);

		File tempSrcPdfFile = null;
		File tempDstPdfFile = null;

		try {
			tempSrcPdfFile = createTempFile(material.getCardId().getId() + "_src");

			// Write material into temporary file
			final InputStream data = material.getData();
			
			FileOutputStream fileOut = new FileOutputStream(tempSrcPdfFile);
			try {
				IOUtils.copy( data, fileOut);
			} finally {
				IOUtils.closeQuietly(data);
				IOUtils.closeQuietly(fileOut);
			}
			// ��������� ��������� �� ������������ ��������� PDF/A-1

			if (!PdfUtils.isValidPDFA1Header(new FileInputStream(tempSrcPdfFile))) {
				// ������������ � PDF/A-1
				tempDstPdfFile = createTempFile(material.getCardId().getId() + "_dst");

				pdfaConverter.convert(
						material.getCardId().getId(),
						PdfConvertorSettings.getConvertorLogDir(),
						tempSrcPdfFile.getAbsolutePath(),
						tempDstPdfFile.getAbsolutePath()
				);

				FileInputStream fileIn = new FileInputStream(tempDstPdfFile);
				final ContentWriter wr = storage.getWriter(cacheURL);
				if (wr != null) {
					wr.delete();
				}
				
				final OutputStream outputStream = wr.getContentOutputStream();
				try {
					IOUtils.copy( fileIn, outputStream);
				} finally {
					IOUtils.closeQuietly(fileIn);
					IOUtils.closeQuietly(outputStream);
				}

				final ContentReader reader = storage.getReader(cacheURL);
				if (reader != null) {
					result = reader.getContentInputStream();
				}
			}
		} catch (Exception e) {
			logger.error ("PDF/A converter failed to convert the file " + material.getName(), e);
			throw new DataException("converter.pdfa.fail", new Object[] {material.getName()});
		} finally {
			if (null != tempSrcPdfFile) {
				tempSrcPdfFile.delete();
				logger.debug( "PDF/A converter: temporary file " + tempSrcPdfFile.getAbsolutePath() + " deleted");
			}
			if (null != tempDstPdfFile) {
				tempDstPdfFile.delete();
				logger.debug( "PDF/A converter: temporary file " + tempDstPdfFile.getAbsolutePath() + " deleted");
			}
		}
	
		return result;
	}

	/**
	 * ����� ��� �������� ���������� �����
	 * @param id
	 * @return File temp output pdf file
	 */	
	private File createTempFile(String id) throws IOException {
		// �������� ���������� ��� �������� ��������� ������
		final File tempDir = new File(PdfConvertorSettings.getConvertorTempDir());
		if(! tempDir.exists()) {
			tempDir.mkdirs();
		}
		File tempFile = new File(PdfConvertorSettings.getConvertorTempDir() + "/" + getTempFileName(id));
  
		if(tempFile.exists()) {
			tempFile.delete();
		}
		tempFile.createNewFile();
		logger.debug("PDF/A converter: temporary file " + tempFile.getAbsolutePath() + " created");

		return tempFile;
	}

	/**
	 * ����� ��� ������������ ����� ���������� �����
	 * @param id
	 * @return String temp file name
	 */	
	private String getTempFileName(String id) {
		return System.currentTimeMillis() +  id;
	}
}
