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
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.JProgressBar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.aplana.arm.PostHTTPRequest;
import java.net.URLEncoder;

public class DXLOperations {

	@SuppressWarnings("unchecked")
	public static List getBufferedImagesFromInputStream(JProgressBar pbar,
			InputStream is) throws Exception {
		List images = new ArrayList();
		String dxl;
		try {
			if (pbar != null)
				pbar.setValue(10);
			if (pbar != null)
				pbar.setValue(20);
			if (pbar != null)
				pbar.setValue(30);
			if (pbar != null)
				pbar.setValue(40);
			final InputStream tmpIs = is;
			BufferedImage image = AccessController
					.doPrivileged(new PrivilegedAction<BufferedImage>() {
						public BufferedImage run() {
							BufferedImage bImg = null;
							try {
								bImg = javax.imageio.ImageIO.read(tmpIs);
							} catch (Exception e) {
								e.printStackTrace();
								bImg = new BufferedImage(700, 600,
										BufferedImage.TYPE_INT_RGB);
							} finally {
								return bImg;
							}
						}
					});

			if (pbar != null)
				pbar.setValue(70);

			images.add(image);
			image.flush();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			return images;
		}
	}

	@SuppressWarnings("unchecked")
	public static List getBufferedImagesFromURL(JProgressBar pbar, String url)
			throws Exception {
		List images = new ArrayList();
		String dxl;
		try {
			if (pbar != null)
				pbar.setValue(10);
			if (pbar != null)
				pbar.setValue(20);
			if (pbar != null)
				pbar.setValue(30);
			if (pbar != null)
				pbar.setValue(40);
			final URL tmpUrl = new URL(url);
			BufferedImage image = AccessController
					.doPrivileged(new PrivilegedAction<BufferedImage>() {
						public BufferedImage run() {
							BufferedImage bImg = null;
							try {
								bImg = javax.imageio.ImageIO.read(tmpUrl);
							} catch (Exception e) {
								e.printStackTrace();
								bImg = new BufferedImage(700, 600,
										BufferedImage.TYPE_INT_RGB);
							} finally {
								return bImg;
							}
						}
					});

			if (pbar != null)
				pbar.setValue(70);

			images.add(image);
			image.flush();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			return images;
		}
	}

	@SuppressWarnings("unchecked")
	public static List getBufferedImagesFromFile(JProgressBar pbar,
			String filepath) throws Exception {
		List images = new ArrayList();
		String dxl;
		try {
			if (pbar != null)
				pbar.setValue(10);
			if (pbar != null)
				pbar.setValue(20);
			if (pbar != null)
				pbar.setValue(30);
			if (pbar != null)
				pbar.setValue(40);
			final String tmpFilepath = filepath;
			BufferedImage image = AccessController
					.doPrivileged(new PrivilegedAction<BufferedImage>() {
						public BufferedImage run() {
							BufferedImage bImg = null;
							try {
								File srcFile = new File(tmpFilepath);
								if (srcFile.exists()) {
									FileInputStream fis = new FileInputStream(
											srcFile);
									bImg = javax.imageio.ImageIO.read(fis);
									fis.close();
								} else {
									System.out
											.println("Unable to locate the source file...");
									bImg = new BufferedImage(700, 600,
											BufferedImage.TYPE_INT_RGB);
								}
							} catch (Exception e) {
								e.printStackTrace();
								bImg = new BufferedImage(700, 600,
										BufferedImage.TYPE_INT_RGB);
							} finally {
								return bImg;
							}
						}
					});

			if (pbar != null)
				pbar.setValue(70);

			images.add(image);
			image.flush();
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			return images;
		}
	}

	public static void saveBufferedImageTo(BufferedImage img) throws Exception,
			IOException {
		System.out.println("Saving an image to temporary file...");
		// 1 �������� �������������� ��������
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageOutputStream ios = null;
		try {
			ios = ImageIO.createImageOutputStream(baos);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Find a gif writer
		ImageWriter writer = null;
		Iterator iter = ImageIO.getImageWritersByFormatName("JPEG");
		writer = (ImageWriter) iter.next();
		ImageWriteParam iwp = writer.getDefaultWriteParam();
		IIOImage iioImage = new IIOImage(img, null, (IIOMetadata) null);

		writer.setOutput(ios);
		try {
			writer.write((IIOMetadata) null, iioImage, iwp);
			// writer.write(iioImage);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		ios.flush();
		baos.flush();
		System.out.println("LENGTH: " + baos.size());
		System.out.println("Saving the file!");

		final ByteArrayOutputStream tmpBaos = baos;

		Boolean saveRes = AccessController
				.doPrivileged(new PrivilegedAction<Boolean>() {
					public Boolean run() {
						try {
							File tmpFile = new File("c:\\Temp\\img.jpg");
							if (tmpFile.exists()) {
								tmpFile.delete();
							}
							FileOutputStream fos = new FileOutputStream(tmpFile);
							tmpBaos.writeTo(fos);
							return new Boolean(true);
						} catch (Exception e) {
							e.printStackTrace();
							return new Boolean(false);
						}
					}
				});

		if (saveRes.booleanValue()) {
			System.out.println("File saved successfully as c:\\Temp\\img.gif");
		} else {
			System.out.println("Failed to save file...");
		}

		ios.reset();
		baos.reset();
	}

	public static void postBufferedImageTo(BufferedImage img, String targetURL,
			String cookies, String fieldName) throws Exception, IOException {

		System.out.println("Uploading image to URL...");
		// 1 �������� �������������� ��������
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageOutputStream ios = null;
		try {
			ios = ImageIO.createImageOutputStream(baos);
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Find a gif writer
		ImageWriter writer = null;
		Iterator iter = ImageIO.getImageWritersByFormatName("JPEG");
		writer = (ImageWriter) iter.next();
		ImageWriteParam iwp = writer.getDefaultWriteParam();
		IIOImage iioImage = new IIOImage(img, null, (IIOMetadata) null);

		writer.setOutput(ios);
		try {
			writer.write((IIOMetadata) null, iioImage, iwp);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		ios.flush();
		baos.flush();
		System.out.println("LENGTH: " + baos.size());
		System.out.println("Saving the file!");
		final ByteArrayOutputStream tmpBaos = baos;
		final String trgURL = targetURL;
		final String trgCookies = cookies;
		final String fldName = fieldName;

		Boolean saveRes = AccessController
				.doPrivileged(new PrivilegedAction<Boolean>() {
					public Boolean run() {
						try {
							// Creating new request
							InputStream result = null;
							ByteArrayInputStream bios = new ByteArrayInputStream(
									tmpBaos.toByteArray());
							String cookieName = "";
							String[] cookiesArr = null;
							String cookie = null;
							String cookieVal[] = new String[2];
							System.out.println("Target URL is: " + trgURL);
							URL trgUrl = null;
							trgUrl = new URL(trgURL);
							PostHTTPRequest postRequest = null;
							postRequest = new PostHTTPRequest(trgUrl);

							// Parsing cookies
							if (trgCookies != null) {
								cookiesArr = trgCookies.split(";");
								for (int i = 0; i < cookiesArr.length; i++) {
									cookie = cookiesArr[i];
									System.out.println("Coockie: " + cookie);
									if (!cookie.endsWith("=")) {
										cookieVal = cookie.split("=");
										try {
											System.out.println("Cookiename: "
													+ cookieVal[0]
													+ " cookieval: "
													+ cookieVal[1]);
											postRequest.setCookie(cookieVal[0],
													cookieVal[1]);
										} catch (Exception ex) {
											ex.printStackTrace();
										}
									} else {
										cookieName = cookie.substring(0, cookie
												.length() - 1);
										postRequest.setCookie(cookieName, "");
									}
								}

							}
							postRequest.setParameter(fldName, "Resolution",
									bios);
							result = postRequest.post();

							return new Boolean(true);
						} catch (Exception e) {
							e.printStackTrace();
							return new Boolean(false);
						}
					}
				});

		if (saveRes.booleanValue()) {
			System.out.println("File uploaded successfully!");
		} else {
			System.out.println("Failed to upload file...");
		}

		ios.reset();
		baos.reset();
	}

	public static void postStringTo(String inString, String targetURL,
			String cookies, String fieldName, String cardNum) throws Exception,
			IOException {

		System.out.println("Uploading string to URL...");
		// 1 �������� �������������� ��������

		final String trgURL = targetURL;
		final String trgCookies = cookies;
		final String fldName = fieldName;
		final String strVal = URLEncoder.encode(inString, "UTF-8");
		final String inCardNum = cardNum;

		final String outString = "card=" + cardNum + "&" + fieldName + "="
				+ strVal;

		Boolean saveRes = AccessController
				.doPrivileged(new PrivilegedAction<Boolean>() {
					public Boolean run() {
						try {
							// Creating new request
							InputStream result = null;
							String cookieName = "";
							String[] cookiesArr = null;
							String cookie = null;
							String cookieVal[] = new String[2];
							URL trgUrl = null;
							trgUrl = new URL(trgURL);
							PostHTTPRequest postRequest = null;
							postRequest = new PostHTTPRequest(trgUrl,
									"application/x-www-form-urlencoded");

							// Parsing cookies
							if (trgCookies != null) {
								cookiesArr = trgCookies.split(";");
								for (int i = 0; i < cookiesArr.length; i++) {
									cookie = cookiesArr[i];
									System.out.println("Coockie: " + cookie);
									if (!cookie.endsWith("=")) {
										cookieVal = cookie.split("=");
										try {
											System.out.println("Cookiename: "
													+ cookieVal[0]
													+ " cookieval: "
													+ cookieVal[1]);
											postRequest.setCookie(cookieVal[0],
													cookieVal[1]);
										} catch (Exception ex) {
											ex.printStackTrace();
										}
									} else {
										cookieName = cookie.substring(0, cookie
												.length() - 1);
										postRequest.setCookie(cookieName, "");
									}
								}

							}
							// String outStr = "ABCDEFGHIJK";
							System.out.println("Target URL: " + trgURL);
							System.out.println("Target Field: " + fldName);
							System.out.println("Target Card Number: "
									+ inCardNum);
							System.out.println("Posting String: " + outString);
							//postRequest.setString(outString);
							
							
							postRequest.setPostField("card", inCardNum);
							postRequest.setPostField(fldName, strVal);
							String resString = postRequest
									.getSimplePostString();
							System.out.println("NEW String: " + resString);
							postRequest.setString(outString);
							
							
							result = postRequest.postString();

							return new Boolean(true);
						} catch (Exception e) {
							e.printStackTrace();
							return new Boolean(false);
						}
					}
				});

		if (saveRes.booleanValue()) {
			System.out.println("String uploaded successfully!");
		} else {
			System.out.println("Failed to upload string...");
		}

	}

}