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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import com.aplana.dbmi.action.ConvertToPdf;
import com.aplana.dbmi.action.DownloadFile;
import com.aplana.dbmi.action.Material;
import com.aplana.dbmi.common.utils.file.DefinesTypeFile;
import com.aplana.dbmi.common.utils.file.MimeContentTypeReestrBean;
import com.aplana.dbmi.common.utils.web.AbstractMaterialDownloadServlet;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.dbmi.service.ServletUtil;
import com.aplana.web.tag.util.StringUtils;

public class ShowMaterialServlet extends AbstractMaterialDownloadServlet {

	public static final String PARAM_NONAME = "noname";
	public static final String PARAM_PDF = "pdf";
	public static final String PARAM_ATTACHMENT = "IS_ATTACH";
	
	ObjectId ATTR_DOCLINKS = ObjectId.predefined(CardLinkAttribute.class, "jbr.files");
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		try {
			String cardId = request.getParameter(CardPortlet.CARD_ID_FIELD);
			String versionId = request.getParameter(CardPortlet.VERSION_ID_FIELD);
			String isAttach = request.getParameter(PARAM_ATTACHMENT);
			boolean isAttachment = false;
			if (isAttach!=null && !isAttach.equals(""))
				isAttachment = Boolean.parseBoolean(isAttach);
			
			DataServiceBean serviceBean = ServletUtil.createService(request);
			logger.info("ID - " + cardId);
			
			if (cardId != null ) {
				if (!isAttachment){
					//���������������� ��������� �������� id-���� � ����, ����� ����� �������� �� �������� ����
					ObjectId cardObjId = new ObjectId(Card.class, Long.parseLong(cardId));
					Card card = serviceBean.getById(cardObjId);
					
					//id-����� �������� ��������
					Set<ObjectId> attachCardIds = new HashSet<ObjectId>();
					attachCardIds.addAll(card.<CardLinkAttribute>getAttributeById(ATTR_DOCLINKS).getIdsLinked());
					
					String userAgent = request.getHeader("User-Agent");
					logger.info("User-Agent=" + userAgent);
					//boolean isMSIE = userAgent != null && userAgent.contains("MSIE");
					response.setContentType("text/html");
					
					PrintWriter out = response.getWriter();
					 out.println(
					"<html>" +
					"<body>" + 
					"<script type=\"text/javascript\" lang=\"javascript\">");
					 out.println(
					" window.onload = openAllWindows();" +
					" " +
					" function openAllWindows() {"
					 );
					 
					for (ObjectId aid: attachCardIds){
						out.println(" openWindowForAttachment("+aid.getId().toString()+");");
						//out.println("alert(\"Ogogo!\");");
					}
					
					 out.println(
					" window.close(); };" +
					" " +
					" function openWindowForAttachment(cardId){" +
					" if(cardId!=null)" +
					"		window.open(\"/DBMI-UserPortlets/ShowMaterialServlet?MI_CARD_ID_FIELD=\"+cardId+\"&IS_ATTACH=true\",\"newWindow_\"+cardId);" +
					//"	alert(\"�����!\");"+
					" }" +
					"</script>" +
					"</body>" +
					"</html>"		 
					 );
					 
					if(null != out) {
						out.flush();
						out.close();
					}
					
				} else {
									
					ObjectId cardObjId = new ObjectId(Card.class, Long.parseLong(cardId));
					//Card card = serviceBean.getById(cardObjId);
					
					//List<String> urls = new ArrayList<String>();
					/*for (ObjectId cid: attachCardIds){
						DownloadFile action = new DownloadFile();
						action.setCardId(cid);
						if (versionId != null && versionId.length() > 0)
							action.setVersionId(Integer.parseInt(versionId));
		
						Material material = (Material) serviceBean.doAction(action);
						//String url = material.getUrl();
						//urls.add(url);
					}*/
					//>>>>>>>>> ����� ��� ��������
					DownloadFile action = new DownloadFile();
					action.setCardId(cardObjId);
					if (versionId != null && versionId.length() > 0)
						action.setVersionId(Integer.parseInt(versionId));
	
					Material material = serviceBean.doAction(action);
					
					String userAgent = request.getHeader("User-Agent");
					logger.info("User-Agent=" + userAgent);
					boolean isMSIE = userAgent != null && userAgent.contains("MSIE");
					
					String mimeType = MimeContentTypeReestrBean.getMimeType(material);
					boolean isConvertableToPdf = DefinesTypeFile.isConvertable(mimeType);
					boolean isPdf = DefinesTypeFile.isPDF(mimeType);
					boolean pdfConversionRequested = null != request.getParameter(PARAM_PDF);
					boolean pdfConversionRequired = pdfConversionRequested && !isPdf && isConvertableToPdf; 
					boolean isImage = DefinesTypeFile.isImage(mimeType);
					String mime = MimeContentTypeReestrBean.DEFAULT_CONTENT_TYPE;
					
					if (isPdf || pdfConversionRequired){
						mime = "application/pdf";
					} else if (isImage)	{
						String type = StringUtils.getFilenameExtension(material.getName());
						if (type != null) {
							mime = "image/"+type;
						} else {
							mime = mimeType;
						}
					} else {
						if (isMSIE) {
							response.setHeader("Content-Disposition", "attachment; filename=\"" + URLEncoder.encode(material.getName(), "UTF-8") + '"');
						} else {
							response.setHeader("Content-Disposition", "attachment; filename=\"=?UTF-8?Q?" + encodeQuotedPrintable(material.getName().getBytes("UTF-8")) + "?=\"");
						}
					
					}
					
					logger.info("mime=" + mime);
					response.setContentType(mime);
					
					/*if(pdfConversionRequested && (isPdf || pdfConversionRequired)){
						response.setHeader("Content-Disposition", "inline");
					} else {
						if (isMSIE) {
							response.setHeader("Content-Disposition", "attachment; filename=\"" + URLEncoder.encode(material.getName(), "UTF-8") + '"');
						} else {
							response.setHeader("Content-Disposition", "attachment; filename=\"=?UTF-8?Q?" + encodeQuotedPrintable(material.getName().getBytes("UTF-8")) + "?=\"");
						}
					}*/ //���������������, ����� �������� ����������� � ����, � �� ������������ � ����������
								
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
							int length = material.getLength();
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
		} catch(DataException e) {
			response.setContentType("text/html; charset=UTF-8");
			PrintWriter out = response.getWriter();
			out.print("<p>"+e.getMessage()+"</p>");
			logger.error(e);
		} catch (Exception e) {
			logger.error(e);
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}
}
