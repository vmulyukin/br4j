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
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.aplana.dbmi.action.ExportCardToXml;
import com.aplana.dbmi.action.ExportCardToXml.Result;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServiceException;

/**
 * implementation of the FileActionHandler that download card to file
 */
public class CardDownloadAction extends FileActionHandler {

    public final static String PARAM_CARD_ID = "cardId";
    public final static String PARAM_RECIPIENT_ID = "recipientId";

    @Override
    public void process(HttpServletRequest request, HttpServletResponse response)
	    throws DataException {
	String filename = "card_out.xml";

	long cardId = getLongValueOfParameter(request, PARAM_CARD_ID);
	long recipientId = getLongValueOfParameter(request, PARAM_RECIPIENT_ID);

	// set the http content type to "APPLICATION/OCTET-STREAM
	response.setContentType("APPLICATION/OCTET-STREAM");

	// initialize the http content-disposition header to
	// indicate a file attachment with the default filename
	String disHeader = "Attachment;Filename=\"" + filename + "\"";
	response.setHeader("Content-Disposition", disHeader);

	Result exportResult = exportCard(cardId, recipientId);
	Map<ObjectId, String> files = exportResult.getFiles();
	String filesDescription = formatFilesDescription(files);
	response.addHeader("fileCards", filesDescription);
	// transfer the file byte-by-byte to the response object
	InputStream cardInputStream = exportResult.getData();

	try {
	    OutputStream out = response.getOutputStream();
	    byte[] buffer = new byte[1024];

	    int length;
	    // copy the file content in bytes
	    while ((length = cardInputStream.read(buffer)) > 0) {
		out.write(buffer, 0, length);
	    }
	} catch (IOException ex) {
	    throw new DataException(
		    "jbr.dmsi.exportActionHandler.responseWriting", ex);
	} finally {
	    if (cardInputStream != null) {
		try {
		    cardInputStream.close();
		} catch (IOException e) {
		    logger.error(
			    "Exception caught during close cardInputStream", e);
		}
	    }
	}

    }

    private String formatFilesDescription(Map<ObjectId, String> files) {
	StringBuilder filesDescription = new StringBuilder();
	for (Entry<ObjectId, String> file : files.entrySet()) {
	    ObjectId cardId = file.getKey();
	    String fileName = file.getValue();
	    if (filesDescription.length() > 0) {
		filesDescription.append(",");
	    }
	    try {
		filesDescription.append(cardId.getId() + "="
			+ URLEncoder.encode(fileName, "UTF-8"));
	    } catch (UnsupportedEncodingException ex) {
		throw new IllegalStateException(ex);
	    }
	}
	return filesDescription.toString();
    }

    private long getLongValueOfParameter(HttpServletRequest request, String name)
	    throws DataException {
	String paramValue = request.getParameter(name);
	if (paramValue != null) {
	    try {
		return Long.parseLong(paramValue);
	    } catch (NumberFormatException ex) {
	    }
	}
	throw new DataException("jbr.dmsi.fileCardServlet.invalidParameter",
		new Object[] { name, "" });

    }

    private Result exportCard(long cardIdValue, long recipientIdValue)
	    throws DataException {
	DataServiceBean dataServiceBean = getServiceBean();
	ExportCardToXml action = new ExportCardToXml();

	ObjectId cardId = new ObjectId(Card.class, cardIdValue);
	ObjectId recipientId = new ObjectId(Card.class, recipientIdValue);

	action.setCardId(cardId);
	action.setRecipientId(recipientId);
	try {
	    return (Result) dataServiceBean.doAction(action);
	} catch (ServiceException e) {
	    logger.error("Error when invoke ExportCardToXml action", e);
	    throw new DataException();
	}

    }

}
