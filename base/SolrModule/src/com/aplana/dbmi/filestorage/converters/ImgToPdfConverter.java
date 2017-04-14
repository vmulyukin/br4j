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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.action.Material;
import com.aplana.dbmi.filestorage.convertmanager.ResourceMessageReader;
import com.aplana.dbmi.filestorage.convertmanager.Task;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.storage.ContentStorage;
import com.aplana.dbmi.storage.content.ContentReader;
import com.aplana.dbmi.storage.content.ContentWriter;
import com.aplana.dbmi.storage.impl.PdfConvertorSettings;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ICC_Profile;
import com.itextpdf.text.pdf.PdfAConformanceLevel;
import com.itextpdf.text.pdf.PdfAWriter;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * ��������� ��� ��������������� ����������� (�����  "*.tif" � "*.tiff")
 * ��� ��������������� ���������� ���������� iText-5.5.0
 * @author ���������
 *
 */
public class ImgToPdfConverter implements PdfConverter {

	protected final Log logger = LogFactory.getLog(getClass());
	protected final ResourceMessageReader messageReader = new ResourceMessageReader();
	private Material material = null;
	private String tempFileName = null;
	// TODO:make configurable parameters (size & page size)
	final static int PAGE_WIDTH = 585;
	final static int PAGE_HEIGHT = 830;
	final static String FONT_LOCATION_TIMES = "DejaVuSerif.ttf";

	public ImgToPdfConverter() {
	}
	
	public void stop() {
		//������ �� ������ � ��� �����������		
	}

	public InputStream processQuery(Task task) throws DataException {
		material = task.getMaterial();
		InputStream result = null;

		final String cacheFSname = "\\$" + PdfConvertorSettings.getCacheStorageName();
		ContentStorage storage = task.getStorage();
		String cacheURL = material.getUrl().replaceAll("\\$(.)+?\\b", cacheFSname);

		InputStream imgbuffer = null;
		try {
			imgbuffer = task.getMaterial().getData();
		} catch (Exception e) {
			logger.error("Error readng task \"" + task.toString(), e);
		}

		result = convertImgToPdf(imgbuffer, storage, cacheURL, cacheFSname);
		return result;
	}

	private InputStream convertImgToPdf(InputStream imgbuffer, ContentStorage storage, String cacheURL, String cacheFSname) throws DataException {
		OutputStream os = null;
		InputStream result = null;
		final Document document = new Document();
		try {
			final ContentWriter wr = storage.getWriter(cacheURL);
			if (wr != null) {
				wr.delete();
			}
			os = wr.getContentOutputStream();

			final PdfAWriter writer = PdfAWriter.getInstance(document, os,  PdfAConformanceLevel.PDF_A_1B);
			writer.createXmpMetadata();
		    
			document.setPageSize(PageSize.A4);
			document.setMargins(5, 5, 5, 5);
			try {
				document.open();
				fillDocument(document, imgbuffer, writer);

				ICC_Profile icc = ICC_Profile.getInstance(PdfConvertorSettings.getSRgbIccProfileStream());
				writer.setOutputIntents("Custom", "", "http://www.color.org", "sRGB IEC61966-2.1", icc);
			} catch (Exception e) {
				wr.delete();
				logger.error("Fill document error", e);
				throw e;
			} finally {
				document.close();
			}
			InputStream is = null;
			final ContentReader cr = storage.getReader(cacheURL);
			if (cr != null) {
				is = cr.getContentInputStream();
			}
			result = is;
				
			if (result != null) {
				logger.info(MessageFormat
					.format("PDF image of  ''{1}''\n\t cached into storage ''{0}''\n\t as ''{2}'' ",
					cacheFSname, material.getUrl(), cacheURL));
			}
		} catch (Exception e) {
			logger.error("Error at Pdf convertion of temporary file \""
					+ getTempFileName() + "\" at directory \""
					+ PdfConvertorSettings.getConvertorTempDir() + "\"", e);
			throw new DataException(this.getClass().getName(), e);
		} finally {
			IOUtils.closeQuietly(os);
		}
		return result;
	}

	protected void fillDocument(Document document, InputStream imgbuffer, PdfWriter writer)
			throws Exception {
		final Element element = singlePageProcessing(imgbuffer);
		addElementToNewPage(document, element);
	}

	protected Element singlePageProcessing(InputStream imgbuffer) throws Exception {
		Image image;
		try {
			image = Image.getInstance(toByteArray(imgbuffer));
		} catch (Exception e) {
			logger.info("This file contains errors", e);
			throw e;
		}
		scaleImage(image);
		return image;
	}

	protected void addElementToNewPage(Document document, Element element)
			throws DocumentException {
		document.newPage();
		document.add(element);
	}

	// not used, must throw exception instead of generate PDF with incorrect content
	/*
	protected Element processingPageContainsError() throws Exception {
		String msg = messageReader.getMessage("converter.page.error");

		Paragraph paragraph = new Paragraph(msg,
				createFont(FONT_LOCATION_TIMES));
		return paragraph;
	}*/

	protected Font createFont(String font_location) throws Exception {
		BaseFont baseFont = BaseFont.createFont(font_location,
				BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
		return new Font(baseFont, Font.DEFAULTSIZE, Font.NORMAL);
	}

	public static void scaleImage(Image image) {
		float scalew = image.getWidth() / PAGE_WIDTH;
		float scaleh = image.getHeight() / PAGE_HEIGHT;

		float scale = (scaleh > scalew) ? scaleh : scalew;
		if (scale < 1)
			scale = 1;
		image.scaleAbsoluteWidth(image.getWidth() / scale);
		image.scaleAbsoluteHeight(image.getHeight() / scale);
	}

	private String getTempFileName() {
		if (null != tempFileName)
			return tempFileName;

		tempFileName = System.currentTimeMillis()
				+ material.getCardId().getId().toString();
		return tempFileName;
	}

	private byte[] toByteArray(InputStream data) throws IOException {
		return IOUtils.toByteArray(data);
	}
}
