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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.FilePartSource;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.PartSource;
import org.apache.commons.httpclient.methods.multipart.StringPart;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import netscape.javascript.JSObject;
//import com.aplana.scanner.JSObject;
import com.aplana.scanner.LocalizedException;
import com.aplana.scanner.ScannerApplet;
import com.aplana.scanner.ScannerController;
import com.aplana.scanner.upload.ProgressRequestEntity;
import com.aplana.scanner.upload.UploaderProgressListener;
import com.lowagie.text.DocumentException;

/**
 * Task to upload pages to the server.
 *
 * @author <a href="mailto:ogalkin@aplana.com">Oleg Galkin</a>
 */
public class UploadTask extends ImageTask {
	private static final Log logger = LogFactory.getLog(UploadTask.class);
	
	private String formatName;
	
	/**
	 * Constructs a task instance.
	 *
	 * @param controller  the {@link ScannerController}
	 * @param formatName  the string containing the name of the format to store images
	 *                    ("pdf" or "tiff")
	 */
	public UploadTask(ScannerController controller, String formatName) {
		super(controller);
		this.formatName = formatName;
	}

	/* (non-Javadoc)
	 * @see org.jdesktop.swingworker.SwingWorker#doInBackground()
	 */
	@Override
	protected Void doInBackground() throws Exception {
		setProgress(0);
		try {
			ScannerApplet applet = getController().getApplet(); 
			Part filePart = getFilePart(applet.getFilename(), formatName);
			Part namespacePart = new StringPart("namespace", applet.getNamespace());
			Part targetAttrPart = new StringPart("targetAttr", applet.getTargetAttr());
			uploadData(applet.getTargetUrl(),	applet.getTargetAttr(), filePart, namespacePart, targetAttrPart);
			getController().newDocument();
		} catch (Throwable t) {
			logger.error("Failed to upload images", t);
			if (t instanceof LocalizedException)
				setThrowable(t);
			else {
				setThrowable(new LocalizedException("warning.upload.message",
								"Failed to upload the scanned pages.", t));
			}
		}
		return null;
	}
	

	private Part getFilePart(String filename, String formatName)
					throws DocumentException, IOException {
		File file = File.createTempFile("upload", null, getController().getTmpDir());
		OutputStream out = new FileOutputStream(file);
		try {
			if (ScannerController.PDF.equals(formatName))
				writePdf(out);
			else if (ScannerController.TIFF.equals(formatName))
				writeTiff(out);
			else
				throw new IllegalArgumentException("Invalid format name: " + formatName);
		} finally {
			IOUtils.closeQuietly(out);
		}
		
		StringBuilder sb = new StringBuilder(filename)
			.append('-')
			.append(getTimestamp())
			.append('.')
			.append(formatName);
		PartSource partSource =	new FilePartSource(sb.toString(), file);
		return new FilePart("file", partSource);
	}
	
	/**
	 * Posts a multipart request to the server.
	 */
	private void uploadData(String targetUrl, String targetAttr, Part... parts) throws IOException  {		
		long bytesToSend = 0;
		for (Part part : parts)
			bytesToSend += part.length();
		
		PostMethod filePost = new PostMethod(targetUrl);
		filePost.setRequestEntity(new MultipartRequestEntity(parts, filePost.getParams()));
		HttpClient client = new HttpClient();
		
		// set cookies
		client.getState().addCookies(getCookies(targetUrl));
		
		if (logger.isDebugEnabled()) {
			StringBuilder sb = new StringBuilder();
			sb.append("Uploading the image to ")
				.append(targetUrl)
				.append(". Request parts are {");
			for (int i = 0; i < parts.length; i++) {
				if (i != 0)
					sb.append(", ");
				sb.append(parts[i].getName());
			}
			sb.append("}");
			logger.debug(sb.toString());
		}
		int statusCode = 400; 
		String retVal = null;
		try {
			client.executeMethod(filePost);
			statusCode = filePost.getStatusCode();
			retVal = filePost.getResponseHeader("cardId").getValue();
			if (200 != statusCode) {
				logger.warn("Server failed to process the scanned pages. HTTP response status code: " +
								statusCode);
				throw new LocalizedException("warning.server.message",
					"Server failed to process the scanned pages.\nPlease consult the system administrator. ");
			}
		} finally {
			filePost.releaseConnection();
			if ((200 == statusCode) && retVal!=null){
				String javascriptCode = "sign_request("+retVal+");";
				JSObject win = (JSObject) JSObject.getWindow(this.getController().getApplet());
				win.eval(javascriptCode);
			}
		}
	}
	
	/**
	 * Gets all the cookies for the request.
	 */
	private Cookie[] getCookies(String targetUrl) throws MalformedURLException {
		List<Cookie> cookies = new ArrayList<Cookie>();
		String cookie = (String)getController().getApplet().jsEval("document.cookie");
		if (cookie != null) {
			String host = new URL(targetUrl).getHost();
			for (String pair : cookie.split("; ?")) {
				String name;
				String value = null;
				int offset = pair.indexOf('=');
				if (offset == -1) {
					name = pair;
				} else {
					name = pair.substring(0, offset);
					value = pair.substring(offset + 1);
				}
				
				if (logger.isDebugEnabled())
					logger.debug("Found cookie '" + name + "' with value: " + value);
				
				cookies.add(new Cookie(host, name, value, "/", null, false));
			}
		}
		return cookies.toArray(new Cookie[cookies.size()]);
	}
	
	private String getTimestamp() {
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		return format.format(new Date());
	}
}
