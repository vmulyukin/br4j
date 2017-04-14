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
package com.aplana.scanner.model;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOInvalidTreeException;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import javax.swing.ImageIcon;

import com.sun.media.imageio.plugins.tiff.BaselineTIFFTagSet;
import com.sun.media.imageio.plugins.tiff.TIFFDirectory;
import com.sun.media.imageio.plugins.tiff.TIFFField;
import com.sun.media.imageio.plugins.tiff.TIFFTag;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import uk.co.mmscomputing.imageio.bmp.BMPMetadata;

import com.aplana.scanner.ScannerController;
/**
 * Scanned page.
 *
 * @author <a href="mailto:ogalkin@aplana.com">Oleg Galkin</a>
 */
public class Page extends AbstractModel implements Printable {
	private static final Log logger = LogFactory.getLog(Page.class);
	
	private static final String IIOMETADATA_PROPERTY = "iiometadata";
	private static final int PAGE_ICON_SIZE = 72;
	
	private File file;
	private SoftReference<ImageData> dataRef;
	private ScannerController controller;
	private int number;

	private ImageIcon imageIcon;
	
	public void setNumber(int number) {
		this.number = number;
	}
	
	/**
	 * Image data that holds <code>BufferedImage</code> along with its DPI.
	 */
	public static class ImageData {
		private BufferedImage image;
		private int xDotsPerInch;
		private int yDotsPerInch;
		
		/**
		 * Constructs the object instance.
		 *
		 * @param image         the <code>BufferedImage</code>
		 * @param xDotsPerInch  the number of horizontal dots per inch
		 * @param yDotsPerInch  the number of vertical dots per inch
		 */
		public ImageData(BufferedImage image, int xDotsPerInch, int yDotsPerInch) {
			this.image = image;
			this.xDotsPerInch = xDotsPerInch;
			this.yDotsPerInch = yDotsPerInch;
		}

		/**
		 * Gets the image.
		 */
		public BufferedImage getImage() {
			return image;
		}

		/**
		 * Gets the horizontal resolution.
		 */
		public int getXDotsPerInch() {
			return xDotsPerInch;
		}

		/**
		 * Gets the vertical resolution.
		 */
		public int getYDotsPerInch() {
			return yDotsPerInch;
		}
	}
	
	/**
	 * Creates a page by the specified image.
	 *
	 * @param  controller  the {@link ScannerController}
	 * @param  image       the <code>BufferedImage</code>
	 * @throws IOException if an error occurs when writing the image to a file
	 */
	public Page(ScannerController controller, BufferedImage image, int number) throws IOException {
		this.controller = controller;
		this.number = number;

		ImageData data;
		Object obj = image.getProperty(IIOMETADATA_PROPERTY);
		if (obj != null && obj instanceof BMPMetadata) {
			BMPMetadata metadata = (BMPMetadata)obj;
			data = new ImageData(image, metadata.getXDotsPerInch(), metadata.getYDotsPerInch());
		} else
			data = new ImageData(image, 200, 200);
		
		setImageData(data);
	}

	
	private Page(ScannerController controller, ImageData data, int number) throws IOException {
		this.controller = controller;
		this.number = number;
		setImageData(data);
	}
	
	/**
	 * Gets an <code>IIOImage</code> for the page.
	 *
	 * @param  writer  the <code>ImageWriter</code>
	 * @param  param   the <code>ImageWriteParam</code>
	 * @return an <code>IIOImage</code> containing the page image and metadata
	 * @throws ImageFormatException if the image file format is invalid
	 * @throws IOException IOException if an error occurs when reading the image file
	 */
	public IIOImage getIIOImage(ImageWriter writer, ImageWriteParam param)
					throws IOException {
		ImageData data = getImageData();
		IIOMetadata metadata = getTiffMetadata(writer, param, data);
		return new IIOImage(data.getImage(), null, metadata);
	}
	
	/**
	 * Gets an image of this page.
	 * 
	 * @param  component  the <code>Component</code> the image is displayed in
	 * @return an image of this page
	 */
	public Image getImage(Component component) {
		Image image = null;
		try {
			ImageData data = getImageData();
			// scale to 72 dpi
			image = ImageCache.getInstance().getImage(component, this,
							72 * data.getImage().getWidth() / data.getXDotsPerInch(),
							72 * data.getImage().getHeight() / data.getYDotsPerInch());
		} catch (IOException e) {
			logger.error("Failed to get image for file: " + file.getPath(), e);
		}
		return image;
	}
	
	/**
	 * Gets a thumbnail image of this page.
	 *
	 * @param  component  the <code>Component</code> the image is displayed in
	 * @return the <code>ImageIcon</code> as a thumbnail image
	 */
	public ImageIcon getThumbnail(Component component) {
		if(this.imageIcon != null){
			return this.imageIcon;
		}
		Image thumbnail = null;
		try {
			thumbnail = ImageCache.getInstance().getImage(
							component, this, PAGE_ICON_SIZE, PAGE_ICON_SIZE);
		} catch (IOException e) {
			logger.error("Failed to get thumbnail for file: " + file.getPath(), e);
		}
		this.imageIcon = thumbnail != null ? new ImageIcon(thumbnail) : null;
		return this.imageIcon;
	}
	
	/**
	 * Rotates the page clockwise.
	 * 
	 * @return the rotated page
	 * @throws ImageFormatException if the image file format is invalid
	 * @throws IOException if an error occurs when reading or writing the image file
	 */
	public Page rotate() throws IOException {
		ImageData data = getImageData();
		int width = data.getImage().getWidth();
		int height = data.getImage().getHeight();
		
		BufferedImage image = new BufferedImage(height, width, data.getImage().getType());
		Graphics2D g = image.createGraphics();
		AffineTransform rotate = new AffineTransform(0, 1, -1, 0, height, 0);
		g.drawRenderedImage(data.getImage(), rotate);
		g.dispose();
		
		ImageData newData = new ImageData(image, data.getYDotsPerInch(), data.getXDotsPerInch());
		return new Page(controller, newData, this.number);
	}
	
	/* (non-Javadoc)
	 * @see java.awt.print.Printable#print(java.awt.Graphics, java.awt.print.PageFormat, int)
	 */
	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
					throws PrinterException {
		try {
			ImageData data = getImageData();
			Graphics2D g = (Graphics2D)graphics;
			double xs = pageFormat.getImageableWidth() / data.getImage().getWidth();
			double ys = pageFormat.getImageableHeight() / data.getImage().getHeight();
			double newScale = Math.min(xs, ys);
			g.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
			g.drawImage(data.getImage(), 0, 0, (int)(data.getImage().getWidth() * newScale),
							(int)(data.getImage().getHeight() * newScale), null);
			return Printable.PAGE_EXISTS;
		} catch (Exception e) {
			throw new PrinterException("Failed to print page: " + e.getMessage());
		}
	}
	
	/**
	 * Gets the page image data.
	 * 
	 * @return the {@link ImageData}
	 * @throws ImageFormatException if the image file format is invalid
	 * @throws IOException if an error occurs when reading the image file
	 */
	public synchronized ImageData getImageData() throws IOException {
		ImageData data = dataRef.get();
		if (data == null) {
			InputStream in = new FileInputStream(file);
			BufferedImage image = ImageIO.read(in);
			Iterator<ImageReader> i= ImageIO.getImageReadersByFormatName("jpg");
            ImageReader imageReader = i.next();
            ImageInputStream stream = ImageIO.createImageInputStream(file);
            imageReader.setInput(stream, true);
			int[] dpi = getResolution(imageReader);
			
			data = new ImageData(image, dpi[0], dpi[1]);
			
			dataRef = new SoftReference<ImageData>(data);
		}
		return data;
	}
	
	protected int[] getResolution(ImageReader r) throws IOException {
		  int hdpi=96, vdpi=96;
		  double mm2inch=25.4;
		  NodeList lst;
		  Element node=(Element)r.getImageMetadata(0).getAsTree("javax_imageio_1.0");
		  lst=node.getElementsByTagName("HorizontalPixelSize");
		  if (lst != null && lst.getLength() == 1)   hdpi=(int)(mm2inch / Float.parseFloat(((Element)lst.item(0)).getAttribute("value")));
		  lst=node.getElementsByTagName("VerticalPixelSize");
		  if (lst != null && lst.getLength() == 1)   vdpi=(int)(mm2inch / Float.parseFloat(((Element)lst.item(0)).getAttribute("value")));
		  return new int[]{hdpi,vdpi};
	}
	
	/**
	 * Sets the image data for the page.
	 *
	 * @param  data  the {@link ImageData}
	 * @throws IOException if an error occurs when writing the image file
	 */
	protected synchronized void setImageData(ImageData data) throws IOException {
		if (file == null)
			file = File.createTempFile("page", ".jpg", controller.getTmpDir());
		
		OutputStream out = new FileOutputStream(file);
		ImageIO.write(data.getImage(), "jpg", file);
		
		dataRef = new SoftReference<ImageData>(data);
	}
	
	private IIOMetadata getTiffMetadata(ImageWriter writer, ImageWriteParam param,
					ImageData data) throws IIOInvalidTreeException {
		
		ImageTypeSpecifier imageType = ImageTypeSpecifier.createFromRenderedImage(data.getImage());
		IIOMetadata metadata = writer.getDefaultImageMetadata(imageType, param);
		TIFFDirectory dir = TIFFDirectory.createFromMetadata(metadata);
		BaselineTIFFTagSet base = BaselineTIFFTagSet.getInstance();
		
		// set TIFF resolution
		TIFFTag tagXRes = base.getTag(BaselineTIFFTagSet.TAG_X_RESOLUTION);
		TIFFTag tagYRes = base.getTag(BaselineTIFFTagSet.TAG_Y_RESOLUTION);
		TIFFField fieldXRes = new TIFFField(tagXRes, TIFFTag.TIFF_RATIONAL, 1,
						new long[][] { { data.getXDotsPerInch(), 1 } });
		TIFFField fieldYRes = new TIFFField(tagYRes, TIFFTag.TIFF_RATIONAL, 1,
						new long[][] { { data.getYDotsPerInch(), 1 } });
		dir.addTIFFField(fieldXRes);
		dir.addTIFFField(fieldYRes);
		
		// set compression
		TIFFTag tagCompression = base.getTag(BaselineTIFFTagSet.TAG_COMPRESSION);
		TIFFField fieldCompression =
			new TIFFField(tagCompression, BaselineTIFFTagSet.COMPRESSION_LZW);
		dir.addTIFFField(fieldCompression);
		
		return dir.getAsMetadata();
	}
	
	public int getNumber() {
		return number;
	}
}
