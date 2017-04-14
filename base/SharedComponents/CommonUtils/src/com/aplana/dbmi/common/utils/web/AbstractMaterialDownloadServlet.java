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
package com.aplana.dbmi.common.utils.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.security.AccessControlException;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.action.ConvertToPdf;
import com.aplana.dbmi.action.DownloadFile;
import com.aplana.dbmi.action.Material;
import com.aplana.dbmi.common.utils.file.DefinesTypeFile;
import com.aplana.dbmi.common.utils.file.MimeContentTypeReestrBean;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;

/**
 * Abstract servlet implementation class extended by {@link MaterialDownloadServlet} 
 * and {@link IReferentMaterialDownloadServlet}
 * 
 */
public abstract class AbstractMaterialDownloadServlet extends javax.servlet.http.HttpServlet {
	private static final long serialVersionUID = 1L;
	
	protected final Log logger = LogFactory.getLog(getClass());
	public static final String PARAM_CARD_ID = "MI_CARD_ID_FIELD";
	public static final String PARAM_VERSION_ID = "MI_VERSION_ID_FIELD";
	public static final String PARAM_PDF = "pdf";
	
	/*
	 * (non-Java-doc)
	 * 
	 * @see javax.servlet.http.HttpServlet#HttpServlet()
	 */
	public AbstractMaterialDownloadServlet() {
		super();
	}

	protected void sendErrorRedirect(HttpServletRequest request, HttpServletResponse response, String errorPageURL, Throwable e) throws ServletException, IOException {
		request.setAttribute("javax.servlet.jsp.jspException", e);
		getServletConfig().getServletContext().getRequestDispatcher(errorPageURL).forward(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
	}

	protected String encodeQuotedPrintable(byte[] data) {
		StringBuilder sb = new StringBuilder(data.length * 3);
		for (int i = 0; i < data.length; i++) {
			sb.append('=').append(Integer.toHexString(0xFF & data[i]));
		}
		return sb.toString().toUpperCase();
	}
	
	protected void writeResponse(DataServiceBean serviceBean,
			HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		String cardId = request.getParameter(PARAM_CARD_ID);
		String versionId = request.getParameter(PARAM_VERSION_ID);

		DownloadFile action = new DownloadFile();
		action.setCardId(new ObjectId(Card.class, Long.parseLong(cardId)));
		if (versionId != null && versionId.length() > 0)
			action.setVersionId(Integer.parseInt(versionId));

		Material material = serviceBean.doAction(action);

		String userAgent = request.getHeader("User-Agent");
		if (logger.isDebugEnabled()) {
			logger.debug("User-Agent=" + userAgent);
		}
		boolean isMSIE = userAgent != null && userAgent.contains("MSIE");
		
		String mimeType = MimeContentTypeReestrBean.getMimeType(material);
		boolean isPdf = DefinesTypeFile.isPDF(mimeType);
		boolean isConvertableToPdf = DefinesTypeFile.isConvertable(mimeType);

		boolean pdfConversionRequested = null != request.getParameter(PARAM_PDF);
		// the parameter indicates that pdf conversion must be performed
		boolean pdfConversionRequired = pdfConversionRequested && !isPdf && isConvertableToPdf; 
		
		String mime = MimeContentTypeReestrBean.DEFAULT_CONTENT_TYPE;
		
		if (isPdf || pdfConversionRequired) {
			mime = "application/pdf";
		}
		if (logger.isDebugEnabled()) {
			logger.debug("mime=" + mime);
		}
		response.setContentType(mime);

		if(pdfConversionRequested && (isPdf || pdfConversionRequired)) {
			response.setHeader("Content-Disposition", "inline");
		} else if (isMSIE) { 
				response.setHeader("Content-Disposition",
					"attachment; filename=\"" + URLEncoder.encode(material.getName(), "UTF-8") + '"' ); 
		} else { 
				response.setHeader("Content-Disposition",
					"attachment; filename=\"=?UTF-8?Q?" + encodeQuotedPrintable(material.getName().getBytes("UTF-8")) + "?=\""
				);
		}
		
		OutputStream out = null;
		InputStream in = material.getData();
		try {
			if(pdfConversionRequired) {
				// convert to pdf and write response
				ConvertToPdf convertionAction = new ConvertToPdf();
				convertionAction.setMaterial(material);
				InputStream pdfDocument = serviceBean.doAction(convertionAction);					
				
				int length = null == pdfDocument ? 0 : pdfDocument.available();
				if (length >= 0) {
					response.setContentLength(length);
					out = response.getOutputStream();
					IOUtils.copy(pdfDocument, out);
				}
			} else {
				final int length = material.getLength();
				if (length >= 0) {
					response.setContentLength(length);
					out = response.getOutputStream();
					IOUtils.copy(in, out);
				}
			}
		} finally {
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(out);
		}
	}
}