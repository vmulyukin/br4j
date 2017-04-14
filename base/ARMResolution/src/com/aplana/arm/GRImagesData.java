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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.ProgressMonitor;

public class GRImagesData {
	private GraphicsResolution parent;
	private List images = new ArrayList();
	private List<Dimension> img_sizes = new ArrayList<Dimension>();
	ProgressMonitor progressMonitor;

	public GRImagesData(GraphicsResolution parent) {
		this.parent = parent;
	}

	public void getImagesFromFile(String img) {

		try {
			parent.selectedColorButton = "blue";
			parent.selectedSizeButton = "1px";
			parent.showCustom = true;
			System.out.println("Getting images from DXLOperations");
			images = DXLOperations.getBufferedImagesFromFile(parent.pbar, img);
			System.out.println("Number of images is: " + images.size());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

		}

	}

	public void getImagesFromURL(String img) {

		try {
			parent.selectedColorButton = "blue";
			parent.selectedSizeButton = "1px";
			parent.showCustom = true;
			System.out.println("Getting images from DXLOperations");
			images = DXLOperations.getBufferedImagesFromURL(parent.pbar, img);
			System.out.println("Number of images is: " + images.size());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

		}

	}

	@SuppressWarnings("unchecked")
	private UndoData getUndoDataFromDocument(String imgString) {
		if (imgString != null && imgString.trim().length()>0) {
			int min_x = 9999;
			int min_y = 9999;
			// String srcString = null; // Get from srcURL!!!
			System.out.println("Image String is: " + imgString);
			String[] points = imgString.split("~");
			for (String drawingString : points) {
				String[] pp = drawingString.split("#");
				for (String point : pp) {
					int pos = point.indexOf(",");
					int x = Integer.parseInt(point.substring(0, pos));
					int y = Integer.parseInt(point.substring(pos + 1));
					if (x < min_x)
						min_x = x;
					if (y < min_y)
						min_y = y;
				}
			}
			Boolean hasPoints = false;
			UndoData data = new UndoData();
			for (String drawingString : points) {
				DrawingData drawData = new DrawingData();
				String[] pp = drawingString.split("#");
				for (String point : pp) {
					int pos = point.indexOf(",");
					int x = Integer.parseInt(point.substring(0, pos));
					int y = Integer.parseInt(point.substring(pos + 1));
					// System.out.println(x+", "+y);
					drawData.points.add(new Point((x - min_x), (y - min_y)));
					hasPoints = true;
				}
				data.add(drawData);
			}
			if (hasPoints) {
				return data;
			} else {
				return null;
			}
		} else {
			return null;
		}

	}

	private void loadCustomImages(String[] tnameArr /*
													 * Sign, quMark, exMark,
													 * checkMark
													 */, String[] srcURL) {
		int i = 0;
		String tname = null;
		System.out.println("Load custom images!!!");
		String signString = parent.getSignatureTemplate();
		tname="Sign";
		loadCustomImage(tname, signString);

		for (String URL : srcURL) {
			tname = tnameArr[i];

			// loadCustomImage(tname, URL);
			//loadCustomImage(tname, signString);
			//
			// if (tname.equals("Sign")) {
			// UndoData signData = getUndoDataFromDocument(URL);
			// parent.setSignData(signData);
			// } else if (tname.equals("quMark")) {
			// UndoData quMarkData = getUndoDataFromDocument(URL);
			// parent.setQuMarkData(quMarkData);
			// } else if (tname.equals("exMark")) {
			// UndoData exMarkData = getUndoDataFromDocument(URL);
			// parent.setExMarkData(exMarkData);
			// } else if (tname.equals("checkMark")) {
			// UndoData checkMarkData = getUndoDataFromDocument(URL);
			// parent.setCheckMarkData(checkMarkData);
			// } else {
			//								
			// UndoData data = getUndoDataFromDocument(URL);
			// data.name = tname;
			// data.cursor = getCustomCursorFromUndoData(data);
			// parent.addCustomData(data);
			//				
			// if (selCustomName != null) {
			// if (selCustomName.equals(tname)) {
			// parent.setSelectedCustomData(data);
			// parent.getControls().getButton("custom")
			// .setToolTipText(tname);
			// }
			// }
			// } // if - else
			// // if - else

			i++;
		}

	}

	private void loadCustomImage(String tname/*
												 * Sign, quMark, exMark,
												 * checkMark
												 */, String srcURL) {
		int i = 0;

		if (tname.equals("Sign")) {
			UndoData signData = getUndoDataFromDocument(srcURL);
			parent.setSignData(signData);
		} else if (tname.equals("quMark")) {
			UndoData quMarkData = getUndoDataFromDocument(srcURL);
			parent.setQuMarkData(quMarkData);
		} else if (tname.equals("exMark")) {
			UndoData exMarkData = getUndoDataFromDocument(srcURL);
			parent.setExMarkData(exMarkData);
		} else if (tname.equals("checkMark")) {
			UndoData checkMarkData = getUndoDataFromDocument(srcURL);
			parent.setCheckMarkData(checkMarkData);
		} else {
			UndoData data = getUndoDataFromDocument(srcURL);
			data.name = tname;
			// data.cursor = getCustomCursorFromUndoData(data);
			parent.addCustomData(data);
			/*
			 * if (selCustomName != null) { if (selCustomName.equals(tname)) {
			 * parent.setSelectedCustomData(data);
			 * parent.getControls().getButton("custom") .setToolTipText(tname); } }
			 */

		} // if - else if - else

	}

	// public BufferedImage loadOriginalImagesData() {
	// Session sess = null;
	// Document doc = null;
	// Document originalDoc = null;
	// try {
	// NotesThread.sinitThread();
	// sess = NotesFactory.createSession();
	// doc = getCurrentDocument(sess);
	// if (doc != null) {
	// originalDoc = getOriginalDocument(doc, sess);
	// doc.removeItem("RTF");
	// if (doc.getItemValueString("savePoints").equals("1")){
	// doc.removeItem("POINTS");
	// }
	// Item rtf = originalDoc.getFirstItem("RTF");
	// if (rtf != null) {
	// rtf.copyItemToDocument(doc);
	// }
	// doc.replaceItemValue("hasGraphics", "");
	// doc.save();
	//
	// img_sizes.clear();
	// images = DXLOperations.getBufferedImagesFromDocument(sess, doc, null);
	//
	// } else {
	// System.out
	// .println("������: �� ������� �������� notes-��������.");
	// }
	// } catch (Exception e) {
	// e.printStackTrace();
	// } finally {
	// try {
	// if (doc != null)
	// doc.recycle();
	// if (originalDoc != null)
	// originalDoc.recycle();
	// if (sess != null)
	// sess.recycle();
	// } catch (NotesException e) {
	// e.printStackTrace();
	// }
	// NotesThread.stermThread();
	// }
	// return getDocumentImage();
	//
	// }
	//
	// private Document getCurrentDocument(Session sess) {
	// Document doc = null;
	// Database db = null;
	// try {
	// String unid = sess.getEnvironmentString("BR_GraphicsResolutionUNID",
	// false);
	// String server = sess.getEnvironmentString("BR_GraphicsResolutionServer",
	// false);
	// System.out.println(server);
	// String path = sess.getEnvironmentString("BR_GraphicsResolutionPath",
	// false);
	// System.out.println(path);
	//			
	// System.out.println("UNID �������� ���������: " + unid);
	// if (unid.length() != 0) {
	// db = sess.getDatabase(server, path);
	// if (db != null) {
	// if (!db.isOpen()){
	// db.open();
	// }
	//					
	// doc = db.getDocumentByUNID(unid);
	// } else {
	// System.out
	// .println("������: �� ������� ������� ���� ����������.");
	// }
	// }
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// return doc;
	// }
	//
	// private Document createOriginalDoc(Document doc, Database db) throws
	// NotesException{
	// Document odoc;
	//		
	// odoc = doc.copyToDatabase(db);
	// odoc.replaceItemValue("ARMBOSS_ParentUNID", doc
	// .getUniversalID());
	// Vector items = odoc.getItems();
	// for (int i = 0; i < items.size(); i++) {
	// Item itm = (Item) items.get(i);
	// if (!itm.getName().equals("RTF")) {
	// itm.remove();
	// }
	// }
	// odoc.replaceItemValue("Form", "GR_Original");
	// odoc.replaceItemValue("DocumentUNID", doc.getUniversalID());
	// odoc.save();
	// return odoc;
	// }
	//	
	//	
	// private Document getOriginalDocument(Document doc, Session sess) {
	// if (doc == null) {
	// System.out.println("������: ������� �������� = null.");
	// return null;
	// }
	// Document odoc = null;
	// String ounid = "";
	// try {
	// Database db = doc.getParentDatabase();
	// ounid = doc.getItemValueString("ARMBOSS_OriginalUNID");
	// System.out.println("o unid: " + ounid);
	// if (ounid.equals("")) {
	// odoc = createOriginalDoc(doc, db);
	// doc.replaceItemValue("ARMBOSS_OriginalUNID", odoc.getUniversalID());
	// doc.save();
	// System.out.println("�������� ����������� ������.");
	// ounid = odoc.getUniversalID();
	// } else {
	// try {
	// odoc = db.getDocumentByUNID(ounid);
	// } catch (NotesException ne) {// ���������� ����������
	// }
	// if (odoc == null) {
	// odoc = createOriginalDoc(doc, db);
	// doc.replaceItemValue("ARMBOSS_OriginalUNID", odoc.getUniversalID());
	// doc.save();
	// }
	// }
	//
	// } catch (Exception e) {
	// e.printStackTrace();
	// } finally {
	//
	// }
	// return odoc;
	// }
	public BufferedImage getDocumentImageFromURL(String url) {
		getImages(url);
		// getImagesFromFile("c:\\Temp\\img.gif");
		System.out.println("URL is: " + url);
		if (url != null) {
			getImagesFromURL(url);
		}

		BufferedImage resultImg = null;
		try {
			BufferedImage img;
			int width = 0;
			int height = 0;

			for (int i = 0; i < images.size(); i++) {
				img = (BufferedImage) images.get(i);
				if (img.getWidth() > width) {
					width = img.getWidth();
				}
				height = height + img.getHeight();
				img.flush();
			}
			if (width == 0 || height == 0) {
				width = 600;
				height = 700;
				resultImg = new BufferedImage(width, height,
						BufferedImage.TYPE_INT_RGB);
				Graphics rGr = resultImg.getGraphics();
				rGr.setColor(Color.white);
				rGr.fillRect(0, 0, width, height);
				img = new BufferedImage(width, height,
						BufferedImage.TYPE_INT_RGB);
				rGr.drawImage(img, 0, height, null);
				img.flush();
				/*
				 * height = 0; for (int i = 0; i < images.size(); i++) { img =
				 * (BufferedImage) images.get(i); img_sizes.add(new
				 * Dimension(img.getWidth(), img.getHeight()));
				 * rGr.drawImage(img, 0, height, null); height = height +
				 * img.getHeight(); img.flush(); }
				 */
				images.clear();
				images = null;
				rGr.dispose();
				System.out
						.println("����������� ��������� �� �������� ���������.");

			} else {
				resultImg = new BufferedImage(width, height,
						BufferedImage.TYPE_INT_RGB);
				Graphics rGr = resultImg.getGraphics();
				rGr.setColor(Color.white);
				rGr.fillRect(0, 0, width, height);
				height = 0;
				for (int i = 0; i < images.size(); i++) {
					img = (BufferedImage) images.get(i);
					img_sizes
							.add(new Dimension(img.getWidth(), img.getHeight()));
					rGr.drawImage(img, 0, height, null);
					height = height + img.getHeight();
					img.flush();
				}
				images.clear();
				images = null;
				rGr.dispose();
				System.out
						.println("����������� ��������� �� �������� ���������.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resultImg;
	}

	public BufferedImage getDocumentImageFromFile(String filePath) {

		getImagesFromFile(filePath);
		/*
		 * System.out.println("URL is: " + url); if (url != null) {
		 * getImagesFromURL(url); }
		 */
		BufferedImage resultImg = null;
		try {
			BufferedImage img;
			int width = 0;
			int height = 0;

			for (int i = 0; i < images.size(); i++) {
				img = (BufferedImage) images.get(i);
				if (img.getWidth() > width) {
					width = img.getWidth();
				}
				height = height + img.getHeight();
				img.flush();
			}
			if (width == 0 || height == 0) {
				width = 600;
				height = 700;
				resultImg = new BufferedImage(width, height,
						BufferedImage.TYPE_INT_RGB);
				Graphics rGr = resultImg.getGraphics();
				rGr.setColor(Color.white);
				rGr.fillRect(0, 0, width, height);
				img = new BufferedImage(width, height,
						BufferedImage.TYPE_INT_RGB);
				rGr.drawImage(img, 0, height, null);
				img.flush();
				/*
				 * height = 0; for (int i = 0; i < images.size(); i++) { img =
				 * (BufferedImage) images.get(i); img_sizes.add(new
				 * Dimension(img.getWidth(), img.getHeight()));
				 * rGr.drawImage(img, 0, height, null); height = height +
				 * img.getHeight(); img.flush(); }
				 */
				images.clear();
				images = null;
				rGr.dispose();
				System.out
						.println("����������� ��������� �� �������� ���������.");

			} else {
				resultImg = new BufferedImage(width, height,
						BufferedImage.TYPE_INT_RGB);
				Graphics rGr = resultImg.getGraphics();
				rGr.setColor(Color.white);
				rGr.fillRect(0, 0, width, height);
				height = 0;
				for (int i = 0; i < images.size(); i++) {
					img = (BufferedImage) images.get(i);
					img_sizes
							.add(new Dimension(img.getWidth(), img.getHeight()));
					rGr.drawImage(img, 0, height, null);
					height = height + img.getHeight();
					img.flush();
				}
				images.clear();
				images = null;
				rGr.dispose();
				System.out
						.println("����������� ��������� �� �������� ���������.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resultImg;
	}

	public void saveImages() {
		try {

			BufferedImage saveimg;
			Dimension img;
			int x = 0;
			int y = 0;
			int w = 0;
			int h = 0;
			parent.pb.setMaximum(img_sizes.size());
			for (int i = 0; i < img_sizes.size(); i++) {
				img = (Dimension) img_sizes.get(i);
				w = img.width;
				h = img.height;
				saveimg = parent.img.getSubimage(x, y, w, h);
				y = y + h;

				DXLOperations.saveBufferedImageTo(saveimg);
				saveimg.flush();
				saveimg = null;

				parent.pb.setValue(i);
				parent.dialog.toFront();

			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}

	}

	public void postImages(String trgURL, String cookie, String fieldName) {
		try {

			BufferedImage saveimg;
			Dimension img;
			int x = 0;
			int y = 0;
			int w = 0;
			int h = 0;
			parent.pb.setMaximum(img_sizes.size());
			for (int i = 0; i < img_sizes.size(); i++) {
				img = (Dimension) img_sizes.get(i);
				w = img.width;
				h = img.height;
				saveimg = parent.img.getSubimage(x, y, w, h);
				y = y + h;

				DXLOperations.postBufferedImageTo(saveimg, trgURL, cookie,
						fieldName);
				saveimg.flush();
				saveimg = null;

				parent.pb.setValue(i);
				parent.dialog.toFront();

			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}

	}

	public void postImagesAsString(String trgURL, String cookie,
			String fieldName, String cardNum) {
		try {

			String imgString = getPointsInformation();
			// System.out.println("Points data is: " + imgString);
			DXLOperations.postStringTo(imgString, trgURL, cookie, fieldName,
					cardNum);

			// parent.pb.setValue(i);
			parent.dialog.toFront();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}

	}

	protected void finalize() throws Throwable {

	}

	public void setSelectedColorOrSize(String actionCommand) {
		try {

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}
	}

	private void savePointsInformation(String trgURL) {
		// ��������� � ����� ��� ������������ ���������������
		// doc.removeItem(itemName);
		System.out.println("saving points");
		String resVal = new String();

		Iterator undoIterator = parent.getData().iterator();
		while (undoIterator.hasNext()) {
			UndoData undo = (UndoData) undoIterator.next();

			Iterator iter = undo.getDrawData().iterator();
			while (iter.hasNext()) {
				DrawingData element = (DrawingData) iter.next();
				String drawingString = "";
				Iterator it = element.points.iterator();
				// System.out.println("size: "+element.points.size());
				while (it.hasNext()) {
					Point p = (Point) it.next();
					// drawingString += (p.x - min_x)+","+(p.y - min_y);
					drawingString += (p.x) + "," + (p.y);
					drawingString += "#";
				} // end iterator
				System.out.println(drawingString);
				resVal += drawingString + "~";
			}
		}

	}

	private String getPointsInformation() {
		// ��������� � ����� ��� ������������ ���������������
		// doc.removeItem(itemName);
		System.out.println("saving points");
		String retVal = new String();

		Iterator undoIterator = parent.getData().iterator();
		while (undoIterator.hasNext()) {
			UndoData undo = (UndoData) undoIterator.next();

			Iterator iter = undo.getDrawData().iterator();
			while (iter.hasNext()) {
				DrawingData element = (DrawingData) iter.next();
				String drawingString = "";
				Iterator it = element.points.iterator();
				// System.out.println("size: "+element.points.size());
				while (it.hasNext()) {
					Point p = (Point) it.next();
					// drawingString += (p.x - min_x)+","+(p.y - min_y);
					drawingString += (p.x) + "," + (p.y);
					drawingString += "#";
				} // end iterator
				System.out.println(drawingString);
				retVal += drawingString + "~";
			}
		}
		return retVal;
	}

	public void getImages(String srcURL) {

		try {
			parent.selectedColorButton = "blue";
			parent.selectedSizeButton = "1px";
			parent.showCustom = true;
			String[] imgsTypes = null;
			String[] imgsURLs = null;
			loadCustomImages(imgsTypes, imgsURLs);
			images = DXLOperations
					.getBufferedImagesFromURL(parent.pbar, srcURL);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}

	}

}
