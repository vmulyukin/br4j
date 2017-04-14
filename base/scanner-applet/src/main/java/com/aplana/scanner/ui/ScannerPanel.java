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
package com.aplana.scanner.ui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.EventHandler;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.TransferHandler;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdesktop.swingworker.SwingWorker;

import com.aplana.scanner.ScannerApplet;
import com.aplana.scanner.ScannerController;
import com.aplana.scanner.model.Page;
import com.aplana.scanner.task.ImageTask;
import com.aplana.scanner.task.TaskExceptionListener;
import com.aplana.scanner.util.ResourceLoader;

/**
 * Main panel.
 *
 * @author <a href="mailto:ogalkin@aplana.com">Oleg Galkin</a>
 */
public class ScannerPanel extends JPanel implements ListDataListener, ListSelectionListener,
				PropertyChangeListener, TaskExceptionListener {
	private static final long serialVersionUID = -3978847483689089419L;
	
	private static final Log logger = LogFactory.getLog(ScannerPanel.class);
	private static final DataFlavor PAGE_DATA_FLAVOR;
	private static final String WARNING_TITLE = ScannerApplet.getMessage("warning.title");
	private static final String INITIALIZING_MESSAGE =
		ScannerApplet.getMessage("label.dialog.initStatus");
	
	public static final String IMAGE_PATH = "com/aplana/scanner/images/";
	public static final String NEW_IMAGE_PATH = IMAGE_PATH + "new.png";
	public static final String SAVE_IMAGE_PATH = IMAGE_PATH + "save.png";
	public static final String PRINT_IMAGE_PATH = IMAGE_PATH + "print.png";
	public static final String ROTATE_IMAGE_PATH = IMAGE_PATH + "rotate.png";
	public static final String DELETE_IMAGE_PATH = IMAGE_PATH + "delete.png";
	public static final String SCAN_IMAGE_PATH = IMAGE_PATH + "scan.png";
	public static final String SELECT_IMAGE_PATH = IMAGE_PATH + "select.png";
	
	private ScannerController controller;
	private JList pageList;
	private ImagePanel imagePanel;
	private JButton saveButton;
	private SplitButton uploadButton;
	private JButton printButton;
	private JButton rotateButton;
	private JButton deleteButton;
	private ActionListener uploadPdfActionListener;
	private ActionListener uploadTiffActionListener;
	private JFileChooser fileSave;
	private ProgressDialog progressDialog;

	
	private final class PageListTransferHandler extends TransferHandler {
		private static final long serialVersionUID = -5496629373223218125L;

		/* (non-Javadoc)
		 * @see javax.swing.TransferHandler#canImport(javax.swing.JComponent, java.awt.datatransfer.DataFlavor[])
		 */
		@Override
		public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
			if (pageList == comp) {
				for (DataFlavor flavor : transferFlavors) {
					if (PAGE_DATA_FLAVOR.equals(flavor))
						return true;
				}
			}
			return false;
		}

		/* (non-Javadoc)
		 * @see javax.swing.TransferHandler#createTransferable(javax.swing.JComponent)
		 */
		@Override
		protected Transferable createTransferable(JComponent c) {
			JList list = (JList)c;
			return new DefaultTransferable(PAGE_DATA_FLAVOR, list.getSelectedValue());
		}

		/* (non-Javadoc)
		 * @see javax.swing.TransferHandler#getSourceActions(javax.swing.JComponent)
		 */
		@Override
		public int getSourceActions(JComponent c) {
			return MOVE;
		}

		/* (non-Javadoc)
		 * @see javax.swing.TransferHandler#importData(javax.swing.JComponent, java.awt.datatransfer.Transferable)
		 */
		@Override
		public boolean importData(JComponent comp, Transferable t) {
			try {
				Page page = (Page)t.getTransferData(PAGE_DATA_FLAVOR);
				if (page != null) {
					JList list = (JList)comp;
					PageListModel model = (PageListModel)list.getModel();
					
					int index = list.getSelectedIndex();
					if (index == -1)
						index = 0;					
					
					int transferIndex = model.indexOf(page);
					if (index < transferIndex) {
						// move the transferred page before the selected one
						synchronized(model) {
							model.add(index, page);
							model.remove(transferIndex + 1);
						}
						return true;
					} else if (index > transferIndex) {
						// move the transferred page after the selected one
						synchronized(model) {
							if (index == (model.getSize() - 1))
								model.add(page);
							else
								model.add(index + 1, page);
							model.remove(transferIndex);
						}
						return true;
					}
				}
			} catch (Exception e) {
				// suppress exception
			}
			return false;
		}
	}
	
	static {
		DataFlavor flavor = null;
		try {
			flavor =
				new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + "; class=" + Page.class.getName());
		} catch (ClassNotFoundException e) {
			assert false;
		}
		PAGE_DATA_FLAVOR = flavor;
	}
	
	/**
	 * Constructs the panel.
	 *
	 * @param controller  the {@link ScannerController}
	 */
	public ScannerPanel(ScannerController controller) {
		this.controller = controller;
		
		uploadPdfActionListener = (ActionListener)EventHandler.create(
						ActionListener.class, this, "uploadPdf");
		uploadTiffActionListener = (ActionListener)EventHandler.create(
						ActionListener.class, this, "uploadTiff");
		
		setLayout(new BorderLayout());
		
		// create buttons
		JPanel buttonPanel = new JPanel(new BorderLayout());
		JPanel subPanel = new JPanel();
		subPanel.setLayout(new BoxLayout(subPanel, BoxLayout.PAGE_AXIS));
		createButtonPanel(subPanel);
		buttonPanel.add(subPanel, BorderLayout.NORTH);
		add(buttonPanel, BorderLayout.EAST);
		
		imagePanel = new ImagePanel();
		JScrollPane imageSP = new JScrollPane(imagePanel);
		imageSP.getVerticalScrollBar().setUnitIncrement(100);
		imageSP.getHorizontalScrollBar().setUnitIncrement(100);
		
		// create page list
		createPageList();
		JScrollPane pageListPane = new JScrollPane(pageList);
		pageListPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		
		// divide image and page list with a splitter
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, pageListPane, imageSP);
		splitPane.setDividerSize(3);
		splitPane.setDividerLocation(130);
		add(splitPane, BorderLayout.CENTER);
		
		fileSave = new JFileChooser();
		FileFilter pdfFilter = new ExtensionFileFilter(
						ScannerApplet.getMessage("fileFilter.description.pdf"), "pdf");
		fileSave.addChoosableFileFilter(pdfFilter);
		if(this.controller.isJAIenable()){
			FileFilter tiffFilter = new ExtensionFileFilter(
							ScannerApplet.getMessage("fileFilter.description.tiff"),
							ScannerController.TIFF_EXTENSIONS);
			fileSave.addChoosableFileFilter(tiffFilter);
		}
		fileSave.setAcceptAllFileFilterUsed(false);
		fileSave.setFileFilter(pdfFilter);
	}
	
	private void createButtonPanel(JPanel panel) {
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(0, 1));
		
		JButton newButton = new JButton(getNewAction());
		buttonPanel.add(newButton);
		
		saveButton = new JButton(getSaveAction());
		saveButton.setEnabled(false);
		buttonPanel.add(saveButton);
		
		JButton mainUploadButton = new JButton(ScannerApplet.getMessage("label.action.pdf"));
		uploadButton = new SplitButton(mainUploadButton, SwingConstants.SOUTH);
		uploadButton.addMenuItem(new JMenuItem(getUploadPdfAction()));
		if(this.controller.isJAIenable()){
			uploadButton.addMenuItem(new JMenuItem(getUploadTiffAction()));
		}
		uploadButton.addActionListener(uploadPdfActionListener);
		uploadButton.setEnabled(false);
		buttonPanel.add(uploadButton);
		
		printButton = new JButton(getPrintAction());
		printButton.setEnabled(false);
		buttonPanel.add(printButton);
		
		rotateButton = new JButton(getRotateAction());
		rotateButton.setEnabled(false);
		buttonPanel.add(rotateButton);
		
		deleteButton = new JButton(getDeleteAction());
		deleteButton.setEnabled(false);
		buttonPanel.add(deleteButton);
		
		panel.add(buttonPanel);
		
		JComponent scannerPanel = controller.getScanGUI();
		if (scannerPanel != null)
			panel.add(scannerPanel);
	}
	
	private void createPageList() {
		pageList = new JList(controller.getPageListModel());
		controller.getPageListModel().addListDataListener(this);
		pageList.addListSelectionListener(this);
		pageList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		pageList.setCellRenderer(new PageListCellRenderer());
		pageList.setTransferHandler(new PageListTransferHandler());
		pageList.setDragEnabled(true);
	}
	
	@SuppressWarnings("serial")
	private Action getNewAction() {
		return new AbstractAction(ScannerApplet.getMessage("label.button.new"),
						new ImageIcon(ResourceLoader.getResource(NEW_IMAGE_PATH))) {
			public void actionPerformed(ActionEvent e) {
				PageListModel model = (PageListModel)pageList.getModel();
				if (model.getSize() > 0) {
					int retVal = JOptionPane.showConfirmDialog(getFrame(),
									ScannerApplet.getMessage("confirm.new.message"),
									ScannerApplet.getMessage("confirm.new.title"), JOptionPane.OK_CANCEL_OPTION);
					if (retVal == JOptionPane.OK_OPTION)
						controller.newDocument();
				}
			}
		};
	};
	
	@SuppressWarnings("serial")
	private Action getSaveAction() {
		return new AbstractAction(ScannerApplet.getMessage("label.button.save"),
						new ImageIcon(ResourceLoader.getResource(SAVE_IMAGE_PATH))) {
			public void actionPerformed(ActionEvent e) {
				int retVal = fileSave.showSaveDialog(getFrame());
				if (retVal == JFileChooser.APPROVE_OPTION) {
					// check file extension
					File file = fileSave.getSelectedFile();
					String extension = FilenameUtils.getExtension(file.getName());
					ExtensionFileFilter filter = (ExtensionFileFilter)fileSave.getFileFilter();
					String[] exts = filter.getFileExtensions();
					for (String ext : exts) {
						if (ext.equalsIgnoreCase(extension)) {
							savePages(file);
							return;
						}
					}
					
					// add file extension
					savePages(new File(file.getAbsolutePath() + "." + exts[0]));
				}
			}
		};
	}
	
	private void savePages(File file) {
		progressDialog = new ProgressDialog(getFrame(),	ScannerApplet.getMessage("label.dialog.save"),
						INITIALIZING_MESSAGE, 0, 100);
		progressDialog.pack();
		progressDialog.setLocationRelativeTo(this);
		controller.savePages(file);
		progressDialog.setVisible(true);
	}
	
	@SuppressWarnings("serial")
	private Action getUploadPdfAction() {
		final String text = ScannerApplet.getMessage("label.action.pdf");
		return new AbstractAction(text) {
			public void actionPerformed(ActionEvent e) {
				uploadButton.setText(text);
				uploadButton.removeActionListener(uploadTiffActionListener);
				uploadButton.addActionListener(uploadPdfActionListener);
				uploadPdf();
			}
		};
	}
	
	@SuppressWarnings("serial")
	private Action getUploadTiffAction() {
		final String text = ScannerApplet.getMessage("label.action.tiff");
		return new AbstractAction(text) {
			public void actionPerformed(ActionEvent e) {
				uploadButton.setText(text);
				uploadButton.removeActionListener(uploadPdfActionListener);
				uploadButton.addActionListener(uploadTiffActionListener);
				uploadTiff();
			}
		};
	}
	
	@SuppressWarnings("serial")
	private Action getPrintAction() {
		return new AbstractAction(ScannerApplet.getMessage("label.button.print"),
						new ImageIcon(ResourceLoader.getResource(PRINT_IMAGE_PATH))) {
			public void actionPerformed(ActionEvent e) {
				controller.printPage();
			}
		};
	}
	
	@SuppressWarnings("serial")
	private Action getRotateAction() {
		return new AbstractAction("", new ImageIcon(ResourceLoader.getResource(ROTATE_IMAGE_PATH))) {
			public void actionPerformed(ActionEvent e) {
				try {
					controller.rotatePage();
				} catch (Throwable t) {
					logger.error("Failed to rotate image", t);
					JOptionPane.showMessageDialog(getFrame(),
									ScannerApplet.getMessage("warning.rotate.message"),
									WARNING_TITLE, JOptionPane.WARNING_MESSAGE);
				}
			}
		};
	}
	
	@SuppressWarnings("serial")
	private Action getDeleteAction() {
		return new AbstractAction(ScannerApplet.getMessage("label.button.delete"),
						new ImageIcon(ResourceLoader.getResource(DELETE_IMAGE_PATH))) {
			public void actionPerformed(ActionEvent e) {
				controller.deletePage();
			}
		};
	}
	
	/**
	 * Event handler for PDF uploading.
	 */
	public void uploadPdf() {
		upload(ScannerController.PDF);
	}
	
	/**
	 * Event handler for TIFF uploading.
	 */
	public void uploadTiff() {
		upload(ScannerController.TIFF);
	}
	
	private void upload(String formatName) {
		progressDialog = new ProgressDialog(getFrame(),	ScannerApplet.getMessage("label.dialog.upload"),
						INITIALIZING_MESSAGE, 0, 100);
		progressDialog.pack();
		progressDialog.setLocationRelativeTo(this);
		controller.uploadPages(formatName);
		progressDialog.setVisible(true);
	}
	
	/**
	 * Selects the page with the specified index.
	 *
	 * @param index  the page index
	 */
	public void selectPage(int index) {
		if (index != -1) {
			pageList.setSelectedIndex(index);
			pageList.ensureIndexIsVisible(index);
		} else
			pageList.clearSelection();
	}
	
	/**
	 * Gets the index of the selected page.
	 */
	public int getSelectedPageIndex() {
		return pageList.getSelectedIndex();
	}
	
	/**
	 * Gets the selected page.
	 */
	public Page getSelectedPage() {
		return (Page)pageList.getSelectedValue();
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.ListDataListener#contentsChanged(javax.swing.event.ListDataEvent)
	 */
	public void contentsChanged(ListDataEvent e) {
		PageListModel model = (PageListModel)pageList.getModel();
		int index = pageList.getSelectedIndex();
		if (index >= e.getIndex0() && index <= e.getIndex1())
			imagePanel.setImage(model.get(index).getImage(imagePanel));
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.ListDataListener#intervalAdded(javax.swing.event.ListDataEvent)
	 */
	public void intervalAdded(ListDataEvent e) {
		updateActionsForAllPages();
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.ListDataListener#intervalRemoved(javax.swing.event.ListDataEvent)
	 */
	public void intervalRemoved(ListDataEvent e) {
		updateActionsForAllPages();
	}

	/* (non-Javadoc)
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	public void valueChanged(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting()) {
			int index = pageList.getSelectedIndex();
			if (index == -1) {
				setPageActionsEnabled(false);
				imagePanel.setImage(null);
			} else {
				setPageActionsEnabled(true);
				PageListModel model = (PageListModel)pageList.getModel();
				imagePanel.setImage(model.get(index).getImage(imagePanel));
			}
		}
	}

	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		if ("progress".equals(evt.getPropertyName())) {
			SwingWorker<?, ?> task = (SwingWorker<?, ?>)evt.getSource();
			if (progressDialog.isCanceled()){
				task.cancel(true);
			}		
			if(task.isCancelled()){
				logger.warn(task + " is canselled");
				return;
			}
			int progress = (Integer)evt.getNewValue();
			progressDialog.setProgress(progress);
			String message = String.format(ScannerApplet.getMessage("label.dialog.progressStatus"),
							progress);
			progressDialog.setStatus(message);
		} else if ("state".equals(evt.getPropertyName()) &&
						SwingWorker.StateValue.DONE == evt.getNewValue()) {
			progressDialog.dispose();
		}
	}

	/* (non-Javadoc)
	 * @see com.aplana.scanner.task.TaskExceptionListener#exceptionThrown(java.lang.Object, java.lang.Throwable)
	 */
	public void exceptionThrown(Object source, Throwable t) {
		JOptionPane.showMessageDialog(getFrame(), t.getLocalizedMessage(),
						WARNING_TITLE, JOptionPane.WARNING_MESSAGE);
	}

	private void updateActionsForAllPages() {
		PageListModel model = (PageListModel)pageList.getModel();
		boolean enable = !model.isEmpty();
		saveButton.setEnabled(enable);
		uploadButton.setEnabled(enable);
	}

	private void setPageActionsEnabled(boolean enable) {
		printButton.setEnabled(enable);
		rotateButton.setEnabled(enable);
		deleteButton.setEnabled(enable);
	}
	
	private Frame getFrame() {
		Container c = getParent();
		while (c != null && !(c instanceof Frame))
			c = c.getParent();
		return (Frame)c;
	}
}
