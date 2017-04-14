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
package com.aplana.dbmi.filestorage.converters.strategy.tiff;

import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.filestorage.converters.ImgToPdfConverter;
import com.aplana.dbmi.filestorage.converters.strategy.ConvertStrategy;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.io.RandomAccessSource;
import com.itextpdf.text.io.RandomAccessSourceFactory;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.RandomAccessFileOrArray;
import com.itextpdf.text.pdf.codec.TiffImage;

/**
 * Strategy of converting TIFF to PDF by ITextPdf library
 * Gets {@link RandomAccessSource} from input stream and gets {@link TiffImage}
 * from it. Then adds this image into pdf by iText library.
 * @author desu
 */
public class ITextTiffConvertStrategy implements ConvertStrategy {
	protected final Log logger = LogFactory.getLog(getClass());
	
	@Override
	public boolean convert(Document document, InputStream inputStream, PdfWriter writer) {
		RandomAccessSourceFactory factory = new RandomAccessSourceFactory();
		RandomAccessSource source = null;
		RandomAccessFileOrArray tiffSourceFile = null;
		
		try {
			source = factory.createSource(inputStream);
			tiffSourceFile = new RandomAccessFileOrArray(source);
		    int ammountOfPages = TiffImage.getNumberOfPages(tiffSourceFile);
			Element element=null;
			for (int i=0; i<ammountOfPages; ++i) {				
				element = multiPageProcessing(tiffSourceFile, i+1);
				document.newPage();
				document.add(element);
			}
			return true;
		} catch (Exception e) {
			logger.error("Can't convert by '" + getClass().getSimpleName() + "'", e);
			return false;
		} finally {
			if (source != null) {
				try {
					source.close();
				} catch (Exception e) {
					logger.error("Can't close " + source.getClass(), e);
				}
			}
		}
	}
	
	private Element multiPageProcessing(RandomAccessFileOrArray tiffSourceFile, int index) throws Exception {
		Image image = TiffImage.getTiffImage(tiffSourceFile, index);
		ImgToPdfConverter.scaleImage(image);
		return image;				
	}
}
