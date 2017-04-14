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
package com.aplana.dbmi.common.utils.pdf;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.activation.FileDataSource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.pdfbox.preflight.PreflightDocument;
import org.apache.pdfbox.preflight.ValidationResult;
import org.apache.pdfbox.preflight.ValidationResult.ValidationError;
import org.apache.pdfbox.preflight.exception.SyntaxValidationException;
import org.apache.pdfbox.preflight.parser.PreflightParser;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.itextpdf.text.pdf.parser.TextExtractionStrategy;
import com.itextpdf.text.pdf.parser.Vector;
import com.itextpdf.xmp.XMPException;
import com.itextpdf.xmp.XMPMeta;
import com.itextpdf.xmp.XMPMetaFactory;

/**
 *  ����������� ����� �� ������������ �������� ��� ������ � PDF �����������
 *
 * @author Vlad Alexandrov
 * @version 1.2
 * @since   2014-12-31
 */

public class PdfUtils
{
	protected static final Log logger = LogFactory.getLog(PdfUtils.class);

	// ����������� ������ ����� A4 210x297 ��, 
	// ��� ������������� c����������� ���������� 595x842 �����
	public static final int A4XSizeP = 595;
	public static final int A4YSizeP = 842;
	public static final float pointResolution = 72/25.4f;

	/**
	 * ����� ��� ������ �������� ������������ PDF ��������� ������� PDF/A-1b
	 * ������ ���������, �������� �c� ��������� ���������
	 * @param File file
	 * @return boolean
	 */
	static public boolean isValidPDFA1bFormat(File file)
	{
		ValidationResult result = null;

		try
		{
			FileDataSource fd = new FileDataSource(file);
			PreflightParser parser = new PreflightParser(fd);
			
			try
			{

				/* Parse the PDF file with PreflightParser that inherits from the NonSequentialParser.
				 * Some additional controls are present to check a set of PDF/A requirements.
				 * (Stream length consistency, EOL after some Keyword...)
				 */
				parser.parse();

				/* Once the syntax validation is done,
				 * the parser can provide a PreflightDocument
				 * (that inherits from PDDocument) 
				 * This document process the end of PDF/A validation.
				 */
				PreflightDocument document = parser.getPreflightDocument();
				document.validate();

				// Get validation result
				result = document.getResult();
				document.close();

			} catch (SyntaxValidationException e) {
				/* the parse method can throw a SyntaxValidationException
					* if the PDF file can't be parsed. In this case, the exception contains an instance of ValidationResult
				 */
				result = e.getResult();
			}

			// display validation result
			if(logger.isDebugEnabled()) {
				if (result.isValid()) {
					logger.debug("The file " + file.getName() + " is a valid PDF/A-1b file");
				} else {
					logger.debug("The file" + file.getName() + " is not valid, error(s) :");
					for (ValidationError error : result.getErrorsList()) {
						logger.debug((error.getErrorCode() + " : " + error.getDetails()));
					}
				}
			}
		}
		catch (Exception e)
		{
			logger.error("Cannot validate file" + file.getName(), e);
		}
			return (null == result) ? false : result.isValid();
	}

	/**
	 * ����� ��� �������� ��������� PDF ��������� �� ������������ ������� PDF/A-1
	 * ����������� ���������, �������� ������ ���������� (XMP Metadata) ���������
	 * @param InputStream inputStream
	 * @return boolean
	 */
	public static boolean isValidPDFA1Header(InputStream inputStream)
	{
		boolean result = false;
		try {
			PdfReader reader = new PdfReader(inputStream);
			try {
				byte[] metadata = reader.getMetadata();
				if (metadata != null) {
					XMPMeta xmp = XMPMetaFactory.parseFromBuffer(metadata);

					Integer pdfaVersion = xmp.getPropertyInteger("http://www.aiim.org/pdfa/ns/id/", "pdfaid:part");
					String pdfaConformance = xmp.getPropertyString("http://www.aiim.org/pdfa/ns/id/", "pdfaid:conformance");

					if ((pdfaVersion != null) && (pdfaVersion == 1) && (pdfaConformance != null)){
						logger.debug("The XMP header claims the file conforms to PDF/A-1 format");
						result = true;
					} else {
						logger.debug("The XMP header claims the file does not conform to PDF/A-1 format");
					}
				}
				else {
					logger.debug("The file does not conform to PDF/A-1 format");
				}
			}
			catch (XMPException e) {
				logger.debug("Error during parsing XMP header. Cannot validate header for PDF/A-1 conformance");
			}
			finally {
				reader.close();
			}
		}
		catch (IOException e) {
			logger.debug("Error during reading PDF document. Cannot validate XMP header for PDF/A-1 conformance");
		}
		finally {
			IOUtils.closeQuietly(inputStream);
		}
		return result;
	}

	/**
	 * Merge multiple pdf into one pdf
	 *
	 * @param list
	 *			of pdf input stream
	 * @param outputStream
	 *			output file output stream
	 * @throws DocumentException
	 * @throws IOException
	 */
	public static void doMerge(List<InputStream> list, OutputStream outputStream)
			throws DocumentException, IOException {
		Document document = new Document();
		PdfWriter writer = PdfWriter.getInstance(document, outputStream);
		document.open();
		PdfContentByte cb = writer.getDirectContent();

		for (InputStream in : list) {
			PdfReader reader = new PdfReader(in);
			for (int i = 1; i <= reader.getNumberOfPages(); i++) {
				document.newPage();
				//import the page from source pdf
				PdfImportedPage page = writer.getImportedPage(reader,i);
				int rotation = reader.getPageRotation(i);
                float xScale = (float) (writer.getPageSize().getWidth() / reader.getPageSize(i).getWidth());
                float yScale = (float) (writer.getPageSize().getHeight() / reader.getPageSize(i).getHeight());
                float scale = Math.min(xScale, yScale);
                scale = Math.min(1, scale);//�� �����������, ������ �������
                
                //TODO: ���� ��� ����������� ��������� ��� ����� �������� �������� ������ � ���������� ��������. ���� ��������� ��� ���������.
                if (rotation == 90){
                    cb.addTemplate(page, 0, -1f, 1f, 0, 0, reader.getPageSizeWithRotation(i).getHeight());
                } else  if (rotation == 270) {
                    cb.addTemplate(page, 0, 1f, -1f, 0, reader.getPageSizeWithRotation(i).getWidth(), 0);
                } else {
                    cb.addTemplate(page,scale,0,0, scale,0,0);
                }
			}
		}
		document.close();
	}

	/**
	 * ����� ��� ��������� ��������� ���������� ��������� ������� ������ � PDF ���������
	 * @param InputStream inputStream
	 * @return boolean
	 */	
	public static Position getLastPosition (String seek, PdfReader reader) throws IOException
	{
		TextExtractionStrategy strategy = new TextLocations(seek);
		Position position = null;
		int pagenumber = reader.getNumberOfPages();
		for(int i = pagenumber; i > 0 ; i--) {
			PdfTextExtractor.getTextFromPage(reader, i, strategy);
			TextLocations.TextChunk textChunk = ((TextLocations)strategy).getLastPosition();
			if (null != textChunk) {
				position = new Position(i, textChunk.getEndLocation().get(Vector.I1) - 
						textChunk.getFont().getWidthPoint(seek, textChunk.getFontSize()),
						textChunk.getEndLocation().get(Vector.I2));
				break;
			}
	    }
		return position;
	}
	
	public static BaseFont createFont(String fontName) throws DocumentException, IOException {
		// PDF/A-1 ������ ������� ����� ������ ���� �������� � ��������
		BaseFont font = BaseFont.createFont(fontName, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
    	font.setSubset(false);
    	return font;
	}
	
	public static class Position {
		int page;
		float X;
		float Y;
		
		public Position (int page, float X, float Y) {
			this.page = page;
			this.X = X;
			this.Y = Y;
		}
		public int getPageNum() {
			return page;
		}
		public float getX() {
			return X;
		}
		public float getY() {
			return Y;
		}
		public void setPage(int page) {
			this.page = page;
		}
		public void setX(float x) {
			X = x;
		}
		public void setY(float y) {
			Y = y;
		}
	}
}
