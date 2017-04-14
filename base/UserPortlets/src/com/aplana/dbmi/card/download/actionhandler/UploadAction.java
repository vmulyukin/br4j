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
package com.aplana.dbmi.card.download.actionhandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.aplana.dbmi.service.DataException;

public abstract class UploadAction extends FileActionHandler {

    public final static String PART_NAME_FILE = "file";
    public final static String PART_NAME_FILE_NAME = "uploadfilename";

    private String fileName;
    private InputStream fileStream;
    private long fileSize;

    @Override
    public void process(HttpServletRequest request, HttpServletResponse response)
	    throws DataException {
	if (ServletFileUpload.isMultipartContent(request)) {
	    List<FileItem> multiParts = getMultiparts(request);
	    try {
		for (Iterator<FileItem> it = multiParts.iterator(); it
			.hasNext();) {
		    FileItem item = it.next();
		    String fieldName = item.getFieldName();
		    if (fieldName.equals(PART_NAME_FILE_NAME)) {
			// file name (form parameter?)
			this.fileName = getUploadFileName(item);
		    } else if (fieldName.equals(PART_NAME_FILE)) {
			// file itself
			this.fileStream = item.getInputStream();
			this.fileSize = item.getSize();
			logDebugMessage("Got file size: [" + getFileSize()
				+ "]");
			logDebugMessage("Got original file name (unused): ["
				+ item.getName() + "]");
		    }
		}
	    } catch (IOException e) {
		logger.error(
			"Exception during processing of multipart request", e);
		throw new DataException(e);
	    }
	    logDebugMessage("Multipart request parsing done, processing...");
	    try {
		response.setContentType("text/plain");
		PrintWriter out = response.getWriter();
		String result = processFile(getFileStream(), getFileName(), request);
		out.write(result);
	    } catch (IOException ex) {
		throw new DataException(ex);
	    }
	}
    }

    protected List<FileItem> getMultiparts(HttpServletRequest request) {
	if (ServletFileUpload.isMultipartContent(request)) {
	    FileItemFactory factory = new DiskFileItemFactory();
	    ServletFileUpload upload = new ServletFileUpload(factory);
	    try {
		@SuppressWarnings("unchecked")
		List<FileItem> multiParts = upload.parseRequest(request);
		if (multiParts == null || multiParts.isEmpty()) {
		    logger.error("Multi part items is empty");
		    return Collections.emptyList();
		}
		return multiParts;
	    } catch (FileUploadException e) {
		logger.error(
			"Exception during processing of multipart request", e);
	    }
	}
	return Collections.emptyList();
    }

    protected String getUploadFileName(FileItem item) {
	String uploadfilename = item.getString();
	final String encodingName = "UTF-8";
	try {
	    uploadfilename = URLDecoder.decode(uploadfilename, encodingName);
	} catch (UnsupportedEncodingException e) {
	    logger.error("Unsupported encoding " + encodingName, e);
	}
	if (logger.isDebugEnabled())
	    logger.debug("Decoded file name: [" + uploadfilename + "]");
	return uploadfilename;
    }

    protected abstract String processFile(InputStream sourceStream,
	    String fileName, HttpServletRequest request) throws DataException;

    protected String getFileName() {
	return this.fileName;
    }

    protected InputStream getFileStream() {
	return this.fileStream;
    }

    protected long getFileSize() {
	return this.fileSize;
    }

    protected void logDebugMessage(String message) {
	if (logger.isDebugEnabled()) {
	    logger.debug(message);
	}
    }

}
