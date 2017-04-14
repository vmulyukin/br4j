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
package com.aplana.scanner.task;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import com.aplana.scanner.LocalizedException;
import com.aplana.scanner.ScannerController;
import com.aplana.scanner.model.Page;
import com.aplana.scanner.model.Page.ImageData;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;

/**
 * Task to output pages as PDF or TIFF.
 *
 * @author <a href="mailto:ogalkin@aplana.com">Oleg Galkin</a>
 */
public abstract class ImageTask extends Task<Void, Void> {
	private ScannerController controller;
	
	/**
	 * Constructs a task instance.
	 *
	 * @param controller  the {@link ScannerController}
	 */
	public ImageTask(ScannerController controller) {
		this.controller = controller;
	}
	
	/**
	 * Gets the {@link ScannerController}.
	 */
	public ScannerController getController() {
		return controller;
	}

	/**
	 * Writes pages to an output stream as PDF.
	 *
	 * @param  out  the <code>OutputStream</code> to write PDF to
	 * 
	 * @throws DocumentException  if an error occurs when writing PDF
	 * @throws IOException if an error occurs when reading an image file
	 */
	protected final void writePdf(OutputStream out) throws DocumentException, IOException {
		Document doc = new Document();
		PdfWriter writer = PdfWriter.getInstance(doc, out);
		PdfContentByte cb = null;
		
		int size = controller.getPageListModel().getSize();
		for (int i = 0; i < size; i++) {
			ImageData data = controller.getPageListModel().get(i).getImageData();
			Image image = Image.getInstance(data.getImage(), null);
			image.scalePercent(7200f / data.getXDotsPerInch(), 7200f / data.getYDotsPerInch());
			image.setAbsolutePosition(0, 0);
			doc.setPageSize(new Rectangle(image.getScaledWidth(), image.getScaledHeight()));
			if (i == 0) {
				doc.open();
				cb = writer.getDirectContent();
			} else
				doc.newPage();
			
			cb.addImage(image);
			updatePrepareImagesProgress((i + 1) * 100 / size);
			System.out.println(this + " Progress " + (i + 1) * 100 / size);
		}
		
		doc.close();
	}
	
	/**
	 * Writes pages to an output stream as TIFF.
	 *
	 * @param  out  the <code>OutputStream</code> to write TIFF to
	 * @throws IOException if an error occurs when writing TIFF
	 */
	protected final void writeTiff(OutputStream out) throws IOException {
		Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(ScannerController.TIFF);
		if (!writers.hasNext())
			throw new LocalizedException("warning.jai.unenable",
					"Java Advanced Imaging library not installed. Please contact the administrator.");

		ImageWriter writer = writers.next();
		ImageOutputStream ios = null;
		try {
			ImageWriteParam param = writer.getDefaultWriteParam();
			ios = ImageIO.createImageOutputStream(out);
			writer.setOutput(ios);
			
			// both TIFF writer can write sequences
			int size = controller.getPageListModel().getSize();
			writer.prepareWriteSequence(null);
			for (int i = 0; i < size; i++) {
				Page page = controller.getPageListModel().get(i);
				writer.writeToSequence(page.getIIOImage(writer, param), param);
				updatePrepareImagesProgress((i + 1) * 100 / size);
			}
			writer.endWriteSequence();
		} finally {
			if (ios != null) {
				try {
					ios.close();
				} catch (IOException e) {
					// ignore exception
				}
			}
		}
	}
	
	/**
	 * Sets the <code>SwingWorker</code>'s <code>progress</code> bound property when writing images.
	 *
	 * @param progress  the new progress value
	 */
	protected void updatePrepareImagesProgress(int progress) {
		setProgress(progress);
	}
}
