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
package com.aplana.scanner;

import java.awt.image.BufferedImage;
import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.Printable;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.UUID;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.co.mmscomputing.device.scanner.Scanner;
import uk.co.mmscomputing.device.scanner.ScannerIOException;
import uk.co.mmscomputing.device.scanner.ScannerIOMetadata;
import uk.co.mmscomputing.device.scanner.ScannerListener;
import uk.co.mmscomputing.device.scanner.ScannerIOMetadata.Type;
import uk.co.mmscomputing.device.twain.TwainIOException;
import uk.co.mmscomputing.device.twain.TwainIdentity;
import uk.co.mmscomputing.device.twain.TwainScanner;

import com.aplana.scanner.model.Page;
import com.aplana.scanner.task.ImageTask;
import com.aplana.scanner.task.SaveTask;
import com.aplana.scanner.task.Task;
import com.aplana.scanner.task.UploadTask;
import com.aplana.scanner.twain.TwainPanel;
import com.aplana.scanner.ui.PageListModel;
import com.aplana.scanner.ui.ScannerPanel;

/**
 * Scanner controller.
 *
 * @author <a href="mailto:ogalkin@aplana.com">Oleg Galkin</a>
 */
public class ScannerController implements ScannerListener {
	private static final Log logger = LogFactory.getLog(ScannerController.class);
	
	public static final String PDF = "pdf";
	public static final String TIFF = "tiff";
	public static final String[] TIFF_EXTENSIONS = new String[] { "tif", "tiff" };

	private static final int TMP_DIR_MAX_ATTEMPTS = 9;
	private static final String TMP_DIR_PREFIX = "scan_";
	
	private File tmpDir;
	private ScannerApplet applet;
	private Scanner scanner;
	private PageListModel pageListModel = new PageListModel();
	private ScannerPanel scannerPanel;
	private ImageTask task;
	private Boolean JAIenable = null;

	private static final class Printer {
		private PrinterJob printerJob;
		private PageFormat page;
		private Book book;
		
		public Printer() {
			printerJob = PrinterJob.getPrinterJob();
			page = printerJob.defaultPage();
			Paper paper = page.getPaper();
			paper.setImageableArea(0, 0, paper.getWidth(), paper.getHeight());
			page.setPaper(paper);
			page = printerJob.validatePage(page);
			
			book = new Book();
		}
		
		public void append(Printable painter) {
			book.append(painter, page);
		}
		
		public void print() {
			printerJob.setPageable(book);
			if (printerJob.printDialog()) {
				try {
					printerJob.print();
				} catch (Throwable t) {
					logger.error("Failed to print document", t);
				}
			}
		}
	}
	
	/**
	 * Default constructor.
	 */
	public ScannerController(ScannerApplet applet) {
		this.applet = applet;
		this.scannerPanel = new ScannerPanel(this);
	}
	
	/**
	 * Gets the {@link ScannerApplet}.
	 */
	public ScannerApplet getApplet() {
		return applet;
	}

	/**
	 * Gets the scanner instance or creates it if it is <code>null</code>.
	 */
	public Scanner getScanner() {
		if (scanner == null) {
			try {
				scanner = Scanner.getDevice();
				if (scanner != null) {
					scanner.addListener(this);
					
					if (logger.isDebugEnabled() && scanner instanceof TwainScanner) {
						TwainIdentity[] identities = ((TwainScanner)scanner).getIdentities();
						for (TwainIdentity identity : identities)
							logger.debug(identity.toString());
					}
				}
			} catch (Exception e) {
				logger.error("Failed to load scanner", e);
			}
		}
		return scanner;
	}
	
	/**
	 * Gets the {@link PageListModel}.
	 */
	public PageListModel getPageListModel() {
		return pageListModel;
	}

	/**
	 * Gets the {@link ScannerPanel}.
	 */
	public ScannerPanel getScannerPanel() {
		return scannerPanel;
	}
	
	/**
	 * Gets a temporary directory used to store page files.
	 */
	public synchronized File getTmpDir() {
		if (tmpDir == null) {
			try {
				tmpDir = createTmpDir();
			} catch (IOException e) {
				logger.warn("Failed to create temporary directory", e);
			}
		}
		return tmpDir;
	}

	/**
	 * Starts a new document by removing all previously scanned pages.
	 */
	public void newDocument() {
		pageListModel.clear();
	}
	
	/**
	 * Prints the currently selected page.
	 */
	public void printPage() {
		final Page page = scannerPanel.getSelectedPage();
		if (page != null) {
			new Thread() {
				public void run() {
					Printer printer = new Printer();
					printer.append(page);
					printer.print();
				}
			}.start();
		}
	}
	
	/**
	 * Rotates the currently selected page.
	 * 
	 * @throws IOException if an error occurs when rotating the page
	 */
	public void rotatePage() throws IOException {
		synchronized(pageListModel) {
			int index = scannerPanel.getSelectedPageIndex();
			if (index >= 0) {
				Page page = pageListModel.get(index);
				Page rotated = page.rotate();
				pageListModel.set(index, rotated);
			}
		}
	}
	
	/**
	 * Deletes the currently selected page.
	 */
	public void deletePage() {
		synchronized(pageListModel) {
			int index = scannerPanel.getSelectedPageIndex();
			if (index >= 0) {
				pageListModel.remove(index);
				int select =  index < pageListModel.getSize() ? index : pageListModel.getSize() - 1;
				scannerPanel.selectPage(select);
				pageListModel.syncPointers();
			}
		}
	}
	
	/**
	 * Scans an image from the selected/default TWAIN source.
	 */
	public void scan() {
		try {
			scanner.acquire();
		} catch (ScannerIOException e) {
			scanner.fireExceptionUpdate(e);
		}
	}
	
	/**
	 * Uploads the scanned pages to the server.
	 * 
	 * @param formatName  the string containing the name of the format to store images
	 *                    ("pdf" or "tiff")
	 */
	public void uploadPages(String formatName) {
		task = new UploadTask(this, formatName);
		task.addPropertyChangeListener(scannerPanel);
		task.addExceptionListener(scannerPanel);
		task.execute();
	}
	
	/**
	 * Saves the scanned pages to a local file.
	 * 
	 * @param file  the <code>File</code> to store the scanned pages
	 */
	public void savePages(File file) {
		task = new SaveTask(this, file);
		task.addPropertyChangeListener(scannerPanel);
		task.addExceptionListener(scannerPanel);
		task.execute();
	}
	
	private void addPage(BufferedImage image) throws IOException {
		Page page = new Page(this, image, pageListModel.getNextNumber());
		int index = pageListModel.add(page);
		if(index == 0){
			scannerPanel.selectPage(index);
		}
	}
	
	/**
	 * Selects TWAIN data source.
	 */
	public void select() {
		try {
			scanner.select();
		} catch (ScannerIOException e) {
			scanner.fireExceptionUpdate(e);
		}
	}
	
	/**
	 * Waits for scanner to finish.
	 */
	public void stop() {
		if (scanner != null)
			scanner.waitToExit();
		if (task != null)
			task.cancel(true);
	}
	
	/**
	 * Deletes the temporary directory used to store page files.
	 */
	public void deleteTmpDir() {
		FileUtils.deleteQuietly(tmpDir);
		tmpDir = null;
	}

	/* (non-Javadoc)
	 * @see uk.co.mmscomputing.device.scanner.ScannerListener#update(uk.co.mmscomputing.device.scanner.ScannerIOMetadata.Type, uk.co.mmscomputing.device.scanner.ScannerIOMetadata)
	 */
	public void update(Type type, ScannerIOMetadata metadata) {
		if (type.equals(ScannerIOMetadata.ACQUIRED)) {
			// scan BufferedImage
			final BufferedImage image = metadata.getImage();
			Task<Void, Void> task = new Task<Void, Void>() {
				protected Void doInBackground() throws Exception {
					try {
						addPage(image);
					} catch (Throwable t) {
						logger.error("Failed to display scanned image", t);
						setThrowable(new LocalizedException("warning.addPage.message",
										"Failed to display the scanned page.", t));
					}
					return null;
				}
			};
			task.addExceptionListener(scannerPanel);
			task.execute();
		} else if (type.equals(ScannerIOMetadata.FILE)) {
			// acquired image as file (twain only for the time being)
			final File file = metadata.getFile();
			Task<Void, Void> task = new Task<Void, Void>() {
				protected Void doInBackground() throws Exception {
					try {
						open(file.getPath());
					} catch (Throwable t) {
						logger.error("Failed to display scanned image", t);
						setThrowable(new LocalizedException("warning.open.message",
										"Failed to read the scanned pages from a file.", t));
					} finally {
						if (!file.delete())
							logger.warn("Could not delete file: " + file.getPath());
					}
					return null;
				}
			};
			task.addExceptionListener(scannerPanel);
			task.execute();
		} else if (type.equals(ScannerIOMetadata.EXCEPTION)) {
			logger.error("Scanner exception", metadata.getException());
			final Exception e = new LocalizedException("warning.scanner.message",
							"Scanner failure occurred.", metadata.getException());
			// notify on EDT
			if (SwingUtilities.isEventDispatchThread())
				scannerPanel.exceptionThrown(this, e);
			else {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						scannerPanel.exceptionThrown(ScannerController.this, e);
					}
				});
			}
		}
	}

	/**
	 * Adds a listener that is notified each time a scanner event occurs.
	 *
	 * @param listener  the <code>ScannerListener</code> to add
	 */
	public void addListener(ScannerListener listener) {
		Scanner scanner = getScanner();
		if (scanner != null)
			scanner.addListener(listener);
	}
	
	/**
	 * Gets the component with controls that handles scanning.
	 *
	 * @return the component with scan controls
	 */
	public JComponent getScanGUI() {
		Scanner scanner = getScanner();
		if (scanner != null) {
			try {
				return new TwainPanel(this);
			} catch (TwainIOException e) {
				logger.warn("TWAIN driver cannot be loaded", e);
			}
		}
		return null;
	}

	/**
	 * Adds all the images from a file to the page list.
	 *
	 * @param  filename  the name of the file
	 * @throws IOException if an error occurs reading the information from the file
	 */
	public void open(String filename) throws IOException {
		String ext = filename.substring(filename.lastIndexOf('.') + 1);
		Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName(ext);
		if (!readers.hasNext())
			throw new IOException("No reader for format '" + ext + "' is available");
		
		ImageReader reader = readers.next();
		File file = new File(filename);
		ImageInputStream iis = ImageIO.createImageInputStream(file);
		try {
			reader.setInput(iis, false, true);
			int numImages = reader.getNumImages(true);
			for (int i = 0; i < numImages; i++) {
				int index = pageListModel.add(new Page(this, reader.read(i), pageListModel.getNextNumber()));
				if (i == (numImages - 1))
					scannerPanel.selectPage(index);
			}
		} finally {
		  iis.close();
		}
	}
	
	private static File createTmpDir() throws IOException {
		final File sysTmpDir = new File(System.getProperty("java.io.tmpdir"));
		File tmpDir;
		final int maxAttempts = TMP_DIR_MAX_ATTEMPTS;
		int attemptCount = 0;
		do {
			attemptCount++;
			if (attemptCount > maxAttempts) {
				throw new IOException("Failed to create a unique temporary directory after " +
								maxAttempts + " attempts");
			}
			String dirName = UUID.randomUUID().toString();
			tmpDir = new File(sysTmpDir, TMP_DIR_PREFIX + dirName);
		} while (tmpDir.exists());
		
		if (!tmpDir.mkdirs())
			throw new IOException("Failed to create directory " + tmpDir.getAbsolutePath());
		
		return tmpDir;
	}
	
	public Boolean isJAIenable(){
		if(JAIenable == null){
			Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName(ScannerController.TIFF);
			if(writers.hasNext()){
				return true;
			} else {
				return false;
			}
		} else {
			return JAIenable;
		}
	}
}
