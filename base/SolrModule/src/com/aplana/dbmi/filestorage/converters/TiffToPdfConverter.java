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

import com.aplana.dbmi.filestorage.converters.strategy.ConvertStrategy;
import com.aplana.dbmi.filestorage.converters.strategy.tiff.ITextTiffConvertStrategy;
import com.aplana.dbmi.service.DataException;
import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * ��������� ��� ��������������� "*.tif" � "*.tiff"
 * ��� ��������������� ���������� ��������� {@link ConvertStrategy}.
 * �� ������ ������ ���������� 2 ���������� ���������:
 *   1) ��������� ������ iTextPdf 5.5.0
 *   2) ��������� JAI 1.1.3 � iTextPdf 5.5.0
 * @author desu
 */
public class TiffToPdfConverter extends ImgToPdfConverter {
	private ArrayList<ConvertStrategy> strategies = new ArrayList<ConvertStrategy>();
	
	public TiffToPdfConverter() {
		strategies.add(new ITextTiffConvertStrategy());

		// workaround jvm crash (libgs.so segfault)
		//strategies.add(new JaiTiffConvertStrategy());
	}

	@Override
	protected void fillDocument(Document document, InputStream imgBuffer, PdfWriter writer) throws Exception {
		//copy image to temp file (for strategies)
		File tmpFile = File.createTempFile("img", "stream");
		FileOutputStream fos = new FileOutputStream(tmpFile);
		IOUtils.copy(imgBuffer, fos);
		IOUtils.closeQuietly(imgBuffer);
		IOUtils.closeQuietly(fos);
		
		//trying to convert at least by one of the strategies
		for (ConvertStrategy strategy : strategies) {
    		InputStream imgStream = new FileInputStream(tmpFile);
			try {
				boolean successConvert = strategy.convert(document, imgStream, writer);
				if (successConvert) {
					tmpFile.delete();
					return;
				}
			} finally {
				IOUtils.closeQuietly(imgStream);
			}
		}
		
		//can't convert by all strategies -> delete tmp file and throw exception
		tmpFile.delete();
		throw new DataException(messageReader.getMessage("converter.page.error"));
	}
}
