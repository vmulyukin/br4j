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

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.media.jai.RenderedImageAdapter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.filestorage.converters.ImgToPdfConverter;
import com.aplana.dbmi.filestorage.converters.strategy.ConvertStrategy;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfWriter;
import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageDecoder;

/**
 * Strategy of converting TIFF to PDF by JAI with ITextPdf library.
 * JAI get {@link RenderedImage} from tiff file, convert it into {@link BufferedImage}
 * and then add this image into pdf file {@link Document} object of iText library.
 * @author desu
 */
public class JaiTiffConvertStrategy implements ConvertStrategy {
	protected final Log logger = LogFactory.getLog(getClass());
	
	@Override
	public boolean convert(Document document, InputStream inputStream, PdfWriter writer) {
		try {
			ImageDecoder tiffDecoder = ImageCodec.createImageDecoder("tiff", inputStream, null);
		    int pages = tiffDecoder.getNumPages();
		    
		    if (pages > 0) {
		        PdfContentByte cb = writer.getDirectContent();
		        for (int i = 0; i<pages; i++) {
		        	RenderedImage image = tiffDecoder.decodeAsRenderedImage(i);
		        	BufferedImage page = convertRenderedImageToBufferedImage(image);
		            processImage(page, document, cb);
		        }
		    }
		    return true;
		} catch (Exception e) {
			logger.error("Can't convert by '" + getClass().getSimpleName() + "'", e);
			return false;
		}
	}
	
    private BufferedImage convertRenderedImageToBufferedImage(RenderedImage rawImageData) {
        RenderedImageAdapter planarImage = new RenderedImageAdapter(rawImageData);
        return planarImage.getAsBufferedImage();
    }

    private void processImage(BufferedImage page, Document document, PdfContentByte cb) throws IOException, DocumentException {
        Image pdfImage = Image.getInstance(page, null);
        pdfImage.setAbsolutePosition(0, 0);
        ImgToPdfConverter.scaleImage(pdfImage);
        cb.addImage(pdfImage);
        document.newPage();
    }
}
