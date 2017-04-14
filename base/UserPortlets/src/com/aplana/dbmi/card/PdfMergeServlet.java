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
package com.aplana.dbmi.card;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.security.AccessControlException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.dbmi.action.ConvertToPdf;
import com.aplana.dbmi.action.DownloadFile;
import com.aplana.dbmi.action.Material;
import com.aplana.dbmi.ajax.JasperReportServlet;
import com.aplana.dbmi.common.utils.file.DefinesTypeFile;
import com.aplana.dbmi.common.utils.file.MimeContentTypeReestrBean;
import com.aplana.dbmi.common.utils.pdf.PdfUtils;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServletUtil;

/**
 * Servlet for downloading and merging Jasper report
 * and file cards.
 * Downloaded PDF files are merged into single PDF document which is returned to client.
 * 
 * Parameters:
 * jasper report parameters: nameConfig, cardId
 * cardIds - comma separated list of file card Ids.
 * 		Note: files should be in PDF or convertable to PDF.
 *
 * @author Vlad Alexandrov
 * @version 1.1
 * @since   2014-08-08
 */

public class PdfMergeServlet extends JasperReportServlet
{

	private static final long serialVersionUID = 1L;

	protected final Log logger = LogFactory.getLog(getClass());

	public static final String PARAM_URLS = "urls";
	public static final String PARAM_CARD_IDS = "cardIds";

	public static final String PDF_MIME_TYPE = "application/pdf";

	public PdfMergeServlet() {
		super();
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
	try {
		String configName = request.getParameter(PARAM_NAME_CONFIG);
		String cardIds = request.getParameter(PARAM_CARD_IDS);

		if (null != cardIds || null != configName) {
			DataServiceBean serviceBean = ServletUtil.createService(request);
			writeResponse(serviceBean, request, response);
		}else {
			//cardIds or configName must be supplied
			response.sendError(HttpServletResponse.SC_BAD_REQUEST );
		}
	} catch (AccessControlException e) {
		logger.error(e.getMessage());
		response.sendError(HttpServletResponse.SC_FORBIDDEN);
	} catch (DataException e) {
		sendError(request, response, e);
	} catch (Exception e) {
		logger.error(e.getMessage());
		response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	}
}

	protected void sendErrorRedirect(HttpServletRequest request, HttpServletResponse response, String errorPageURL, Throwable e) throws ServletException, IOException {
		request.setAttribute("javax.servlet.jsp.jspException", e);
		getServletConfig().getServletContext().getRequestDispatcher(errorPageURL).forward(request, response);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
	}

	protected void writeResponse(DataServiceBean serviceBean,
			HttpServletRequest request, HttpServletResponse response) throws Exception
	{
		//Input streams list that will be merged into single PDF document
		List<InputStream> inList = new ArrayList<InputStream>();

		OutputStream out = null;
		try {
			String configName = request.getParameter(PARAM_NAME_CONFIG);
			if (null != configName && !configName.isEmpty()) {
				ByteArrayOutputStream output = null;
				try {
					// 1. Get Jasper report
					output = new ByteArrayOutputStream();
					super.writeReportToStream(request, response, output);
					inList.add(new ByteArrayInputStream(output.toByteArray()));
				} catch (Exception e) {
					logger.error("Failed to download Jasper report for config: " + configName, e);
					throw new DataException("print.report.download.fail");
				}finally {
					IOUtils.closeQuietly(output);
				}
			}

			// 2. Get file cards content
			String cardIdsString = request.getParameter(PARAM_CARD_IDS);

			List<String> cardIdsList = new ArrayList<String>();

			if (cardIdsString != null) {
				cardIdsList = Arrays.asList(cardIdsString.split(","));
			}
			for (String cardId : cardIdsList ) {
				Material material = null;
				try {
					DownloadFile action = new DownloadFile();
					action.setCardId(new ObjectId(Card.class, Long.parseLong(cardId)));
					material = (Material) serviceBean.doAction(action);
				} catch (Exception e) {
					logger.error("Failed to download material file for card id " + cardId, e);
					throw new DataException("print.material.download.fail", new Object[] { cardId });
				}

				String mimeType = MimeContentTypeReestrBean.getMimeType(material);
				boolean isPdf = DefinesTypeFile.isPDF(mimeType);
				boolean isConvertableToPdf = DefinesTypeFile.isConvertable(mimeType);

				InputStream in = material.getData();
				if(!isPdf && isConvertableToPdf) {
					try {
						// Convert to pdf
						ConvertToPdf convertionAction = new ConvertToPdf();
						convertionAction.setMaterial(material);
						InputStream pdfDocument = (InputStream) serviceBean.doAction(convertionAction);
						int length = null == pdfDocument ? 0 : pdfDocument.available();
						if (length > 0) {
							inList.add(pdfDocument);
						}
					} catch (Exception e) {
						logger.error("Failed to convert material file " + material.getName() + " to PDF format", e);
						throw new DataException("print.convert.fail", new Object[] { material.getName()});
					}
				} else if (isPdf) {
					// Already in pdf format
					final int length = material.getLength();
					if (length > 0) {
						inList.add(in);
					}
				} else {
					logger.error("Material file " + material.getName() + " is not convertable to PDF");
					throw new DataException("print.convert.not.supported", new Object[] { material.getName()});
				}
			}

			if (inList.size() > 0) {
				out = response.getOutputStream();
				if (inList.size() == 1) {
					// Single document - copy as is
					IOUtils.copy(inList.get(0), out);
				}else {
					// Multiple documents - perform  merge
					PdfUtils.doMerge(inList, out);
				}
			} else {
				logger.error("Cannot load any materials");
				throw new DataException("print.load.fail");
			}
			response.setHeader("Content-Disposition", "inline");
			response.setContentType(PDF_MIME_TYPE);

		} finally {
			// Do some clean up
			for (InputStream in : inList ) {
				IOUtils.closeQuietly(in);
			}
			IOUtils.closeQuietly(out);
		}
	}

	protected void sendError(HttpServletRequest request, HttpServletResponse response, Throwable t) throws ServletException, IOException 
	{
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = response.getWriter();
		ResourceBundle messages = ResourceBundle.getBundle("com.aplana.dbmi.card.nl.CardPortletResource", request.getLocale());
		try {
			out.println("<html>");
			out.println("<body>");
			out.println("<br>");
			out.println("<font color=\"red\">" + t.getMessage() + "<br></font>");
			out.println("<br>");
			out.println("</body>");
			out.println("</html>");
		} finally {
			out.close();
		}
	}
}
