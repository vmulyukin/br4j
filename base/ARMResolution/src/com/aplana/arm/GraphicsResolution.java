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
package com.aplana.arm;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

import java.util.Hashtable;

public class GraphicsResolution extends JApplet {
	private static final long serialVersionUID = 9217599707862826066L;

	GraphicsResolution gr;
	public JProgressBar pbar;
	public JDialog dialog;
	public JProgressBar pb;
	boolean loaded;

	private GRDrawingPanel drawpanel;
	private GRControls controls;
	private JScrollPane scrollpane;

	private boolean isGraphicsMessage;
	private String noteID;

	public GRImagesData imgData;
	public BufferedImage img;

	public Cursor pencil;
	public Cursor eraser;
	public Cursor custom;

	private Stack<UndoData> data = new Stack<UndoData>();
	private Stack<UndoData> undo_data = new Stack<UndoData>();
	private UndoData curdata;

	// ���������� ��������� ������
	private UndoData signData;
	private UndoData quMarkData;
	private UndoData checkMarkData;

	private String submitUrl = null;
	private String downloadUrl = null;
	private String fileParamName = null;
	private String cookies = null;
	private String saveType = null;
	private String cardNum = null;
	private Hashtable params = new Hashtable();
	private String signatureTemplate = null;

	public UndoData getCheckMarkData() {
		return checkMarkData;
	}

	public void setCheckMarkData(UndoData checkMarkData) {
		this.checkMarkData = checkMarkData;
	}

	private UndoData exMarkData;
	private List<UndoData> customData = new ArrayList<UndoData>();
	private UndoData selectedCustomData;

	private int brush_size;

	public String selectedColorButton;
	public String selectedSizeButton;

	public boolean showCustom;

	public void init() {
		initializeApplet(this);
	}

	public void setCookies(String inCookies) {
		cookies = inCookies;
	}

	public void setFieldName(String fldName) {
		fileParamName = fldName;
	}

	public void setCardNum(String inCardNum) {
		cardNum = inCardNum;
	}

	public void setSaveType(String inSaveType) {
		saveType = inSaveType;
	}

	public void setDownloadUrl(String inDownloadUrl) {
		downloadUrl = inDownloadUrl;
	}

	public void setUploadUrl(String inUploadUrl) {
		submitUrl = inUploadUrl;
	}

	public void setParam(String paramName, String paramVal) {
		try {
			params.put(paramName, paramVal);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

		}
	}

	public String getParam(String paramName) {
		String retVal = "";
		try {
			retVal = (String) params.get(paramName);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return retVal;
		}
	}

	public void initializeApplet(GraphicsResolution applet) {
		try {
			System.gc();
			try {
				submitUrl = getParameter("submitUrl");
				downloadUrl = getParameter("downloadUrl");
				fileParamName = getParameter("fileParamName");
				saveType = getParameter("saveType");
				signatureTemplate = getParameter("signatureSample");
				System.out.println("Submit URL: " + submitUrl);
				System.out.println("Download URL: " + downloadUrl);
				System.out.println("File parameter Name: " + fileParamName);
				System.out.println("Save type: " + saveType);
				System.out.println("Signature template: " + signatureTemplate);
				// cookies = getParameter("cookies");

				UIManager.setLookAndFeel(UIManager
						.getSystemLookAndFeelClassName());
			} catch (Exception e) {
			}

			loaded = false;
			brush_size = 1;

			Toolkit toolkit = Toolkit.getDefaultToolkit();
			ImageIcon icon = getIcon("eraser.gif");
			if (icon == null) {
				eraser = new Cursor(Cursor.DEFAULT_CURSOR);
			} else {
				eraser = toolkit.createCustomCursor(icon.getImage(), new Point(
						1, 31), "Eraser");
			}

			icon = getIcon("pencil.gif");
			if (icon == null) {
				pencil = new Cursor(Cursor.DEFAULT_CURSOR);
			} else {
				pencil = toolkit.createCustomCursor(icon.getImage(), new Point(
						1, 31), "Pencil");
			}

			icon = getIcon("custom.gif");
			if (icon == null) {
				custom = new Cursor(Cursor.DEFAULT_CURSOR);
			} else {
				custom = toolkit.createCustomCursor(icon.getImage(), new Point(
						1, 1), "Custom");
			}

			drawpanel = new GRDrawingPanel(applet);
			drawpanel.setFocusable(true);

			JPanel pnl = new JPanel();
			pbar = new JProgressBar();
			pbar.setValue(0);
			pbar.setStringPainted(true);
			pbar.setAlignmentY(JProgressBar.CENTER_ALIGNMENT);
			pnl.setLayout(new GridBagLayout());
			pnl.add(pbar);

			scrollpane = new JScrollPane(pnl);
			// scrollpane = new JScrollPane(drawpanel);

			scrollpane.setWheelScrollingEnabled(true);
			scrollpane.setFocusable(true);

			controls = new GRControls(drawpanel, applet);
			controls.setName("Controls");
			controls.setFocusable(true);

			// String lefthand = getEnviroment("BRARM_lefthand");
			String lefthand = ""; // TODO

			if (lefthand.equals("")) {
				applet.add(BorderLayout.EAST, controls);
				applet.add(BorderLayout.CENTER, scrollpane);
			} else {
				applet.add(BorderLayout.WEST, controls);
				applet.add(BorderLayout.CENTER, scrollpane);
			}
			System.out.println("createPartControl END.");

			// loadDataFromURL("");
			// loadDataFromURL("http://192.168.100.101:8080/DBMI-UserPortlets/MaterialDownloadServlet?MI_CARD_ID_FIELD=9162");
			loadDataFromURL(downloadUrl);
			/*
			 * if (downloadUrl != null) loadDataFromURL(downloadUrl); else
			 * loadEmptyImage("c:\\background.jpg");
			 */
		} catch (Exception e) {
			ExceptionLoggerFile.Log(e);
		}

	}

	public void destroy() {
		try {
			if (imgData != null)
				imgData.finalize();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		imgData = null;

		if (img != null)
			img.flush();
		img = null;

	}

	public GRControls getControls() {
		return controls;
	}

	public boolean isGraphicsMessage() {
		return this.isGraphicsMessage;
	}

	public void setIsGraphicsMessage(boolean flag) {
		System.out.println("flag: " + flag);
		this.isGraphicsMessage = flag;
	}

	public ImageIcon getIcon(String imgName) {
		ImageIcon icon = null;
		URL path = null;
		try {
			path = this.getClass().getResource("images/" + imgName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (path != null) {
			// System.out.println(path);
			icon = new ImageIcon(path, "1");
		}

		return icon;
	}

	public String getNoteID() {
		return noteID;
	}

	public void setNoteID(String noteID) {
		this.noteID = noteID;
	}

	public void setBrush_size(int brush_size) {
		this.brush_size = brush_size;
	}

	public int getBrush_size() {
		return brush_size;
	}

	public boolean isDataEmpty() {
		return data.empty();
	}

	public boolean isUndoEmpty() {
		return undo_data.empty();
	}

	public void undo() {
		if (!data.empty()) {
			UndoData lastaction = (UndoData) data.pop();
			undo_data.push(lastaction);
		}
		// drawpanel.repaint();
		drawpanel.paintIntoBuffer();
	}

	public void clear() {
		// img = imgData.loadOriginalImagesData();
		// TODO
		data.clear();
		undo_data.clear();
		// drawpanel.repaint();
		drawpanel.paintIntoBuffer();

		System.out.println("������������ ����������� ���������");
		if (isGraphicsMessage()) {
			// JOptionPane.showMessageDialog(this, "��������� �������.");
			// showStatus("������� �������.");
		} else {
			// JOptionPane.showMessageDialog(this, "��������� �������.");
		}

	}

	public void redo() {
		if (!undo_data.empty()) {
			UndoData lastaction = (UndoData) undo_data.pop();
			data.push(lastaction);
		}
		// drawpanel.repaint();
		drawpanel.paintIntoBuffer();
	}

	public void save() throws Exception {
		drawpanel.paintIntoImage();

		controls.getButton("save").setEnabled(false);
		controls.getButton("clear").setEnabled(false);
		drawpanel.deactivatePanel();
		controls.repaint();

		pb = new JProgressBar(0, 100);
		pb.setPreferredSize(new Dimension(175, 20));
		pb.setStringPainted(true);
		pb.setValue(0);
		JPanel center_panel = new JPanel();
		center_panel.add(pb, BorderLayout.CENTER);
		dialog = new JDialog(JOptionPane.getFrameForComponent(this));
		dialog.setTitle("����������");
		dialog.getContentPane().add(new JPanel(), BorderLayout.NORTH);
		dialog.getContentPane().add(center_panel, BorderLayout.CENTER);
		dialog.getContentPane().add(new JPanel(), BorderLayout.SOUTH);
		dialog.pack();
		dialog.setLocationRelativeTo(null);
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

		dialog.setVisible(true);
		dialog.toFront();

		Runnable datasaver = new Runnable() {
			public void run() {

				imgData.saveImages();
				data.clear();
				undo_data.clear();
				// drawpanel.repaint();
				drawpanel.paintIntoBuffer();
				dialog.dispose();

				if (!isGraphicsMessage) {
					controls.getButton("save").setEnabled(true);
					controls.getButton("clear").setEnabled(true);
					drawpanel.activatePanel();
					// JOptionPane.showMessageDialog(gr,
					// "��������� ���������.");
					// showStatus("��������� ���������.");
				} else {
					// TODO

					// JOptionPane.showMessageDialog(gr,
					// "��������� ����������.");
					// showStatus("��������� ����������.");

				}

			};
		};

		ExecutorService threadExecutor = Executors.newFixedThreadPool(1);
		threadExecutor.execute(datasaver);
	}

	public void post() {
		postData(submitUrl, cookies, fileParamName);
	}

	public void postData(String targetURL, String trgCookie, String fieldName) {
		drawpanel.paintIntoImage();

		controls.getButton("save").setEnabled(false);
		controls.getButton("clear").setEnabled(false);
		drawpanel.deactivatePanel();
		controls.repaint();

		pb = new JProgressBar(0, 100);
		pb.setPreferredSize(new Dimension(175, 20));
		pb.setStringPainted(true);
		pb.setValue(0);
		JPanel center_panel = new JPanel();
		center_panel.add(pb, BorderLayout.CENTER);
		dialog = new JDialog(JOptionPane.getFrameForComponent(this));
		dialog.setTitle("����������");
		dialog.getContentPane().add(new JPanel(), BorderLayout.NORTH);
		dialog.getContentPane().add(center_panel, BorderLayout.CENTER);
		dialog.getContentPane().add(new JPanel(), BorderLayout.SOUTH);
		dialog.pack();
		dialog.setLocationRelativeTo(null);
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

		dialog.setVisible(true);
		dialog.toFront();
		final String trgURL = targetURL;
		final String cookie = trgCookie;
		final String fldName = fieldName;

		Runnable datasaver = new Runnable() {
			public void run() {
				if (saveType == null) {
					System.out.println("Posting images to field "
							+ fileParamName);
					imgData.postImages(trgURL, cookie, fldName);
				} else {

					System.out.println("Posting string to field: "
							+ fileParamName);
					imgData
							.postImagesAsString(trgURL, cookie, fldName,
									cardNum);
				}
				data.clear();
				undo_data.clear();
				// drawpanel.repaint();
				drawpanel.paintIntoBuffer();
				dialog.dispose();

				if (!isGraphicsMessage) {
					controls.getButton("save").setEnabled(true);
					controls.getButton("clear").setEnabled(true);
					drawpanel.activatePanel();
				} else {

				}

			};
		};

		ExecutorService threadExecutor = Executors.newFixedThreadPool(1);
		threadExecutor.execute(datasaver);
	}

	public Stack getData() {
		return data;
	}

	@SuppressWarnings("unchecked")
	public void setData(Stack data) {
		this.data = data;
	}

	public Stack getUndo_data() {
		return undo_data;
	}

	@SuppressWarnings("unchecked")
	public void setUndo_data(Stack undo_data) {
		this.undo_data = undo_data;
	}

	public UndoData getCurdata() {
		return curdata;
	}

	public void setCurdata(UndoData curdata) {
		this.curdata = curdata;
	}

	public JScrollPane getScrollpane() {
		return scrollpane;
	}

	public void setScrollpane(JScrollPane scrollpane) {
		this.scrollpane = scrollpane;
	}

	public void loadDataFromURL(String url) {
		// JOptionPane.showMessageDialog(this, "�������� ��������.");
		gr = this;

		JPanel panel = new JPanel();
		drawpanel.add(panel);
		final String tmpUrl = url;
		Runnable dataloader = new Runnable() {
			public void run() {
				imgData = new GRImagesData(gr);
				img = imgData.getDocumentImageFromURL(tmpUrl);
				controls.activatePanel();
				drawpanel.activatePanel();
				System.out.println("Updating UI");
				scrollpane.setViewportView(drawpanel);
				scrollpane.updateUI();
				loaded = true;
				System.out.println("load Data complete.");
			}

		};

		ExecutorService threadExecutor = Executors.newFixedThreadPool(1);
		threadExecutor.execute(dataloader);

	}

	public void loadEmptyImage(String ImagePath) {
		// JOptionPane.showMessageDialog(this, "�������� ��������.");
		gr = this;

		JPanel panel = new JPanel();
		drawpanel.add(panel);
		Runnable dataloader = new Runnable() {
			public void run() {
				imgData = new GRImagesData(gr);
				img = imgData.getDocumentImageFromFile("ImagePath");
				controls.activatePanel();
				drawpanel.activatePanel();
				System.out.println("Updating UI");
				scrollpane.setViewportView(drawpanel);
				scrollpane.updateUI();
				loaded = true;
				System.out.println("load Data complete.");
			}

		};

		ExecutorService threadExecutor = Executors.newFixedThreadPool(1);
		threadExecutor.execute(dataloader);

	}

	public void setSignData(UndoData signData) {
		this.signData = signData;
	}

	public UndoData getSignData() {
		return signData;
	}

	public UndoData getQuMarkData() {
		return quMarkData;
	}

	public void setQuMarkData(UndoData quMarkData) {
		this.quMarkData = quMarkData;
	}

	public UndoData getExMarkData() {
		return exMarkData;
	}

	public void setExMarkData(UndoData exMarkData) {
		this.exMarkData = exMarkData;
	}

	public List<UndoData> getCustomData() {
		return customData;
	}

	public void setCustomData(List<UndoData> customData) {
		this.customData = customData;
	}

	public void addCustomData(UndoData data) {
		customData.add(data);
	}

	public void setSelectedCustomData(UndoData data) {
		this.selectedCustomData = data;
	}

	public UndoData getSelectedCustomData() {
		return selectedCustomData;
	}

	public UndoData getDataByKey(String key) {
		if (key.equals("sign")) {
			return signData;
		} else if (key.equals("custom")) {
			return selectedCustomData;
		} else if (key.equals("exmark")) {
			return exMarkData;
		} else if (key.equals("qumark")) {
			return quMarkData;
		} else if (key.equals("checkmark")) {
			return checkMarkData;
		} else {
			return null;
		}
	}

	public String getSignatureTemplate() {
		return signatureTemplate;
	}

}
