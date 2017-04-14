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
package com.aplana.cms.tags;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.json.JSONArray;
import org.springframework.util.StringUtils;

import com.aplana.cms.ContentProducer;
import com.aplana.cms.ContentViewPortlet;
import com.aplana.cms.ProcessRequest;
import com.aplana.cms.ProcessResponse;
import com.aplana.cms.Tag;
import com.aplana.crypto.CryptoApplet;
import com.aplana.crypto.CryptoLayer;
import com.aplana.dbmi.Portal;
import com.aplana.dbmi.card.CardPortlet;
import com.aplana.dbmi.card.JspAttributeEditor;
import com.aplana.dbmi.card.MaterialDownloadServlet;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.service.DataServiceBean;
import com.aplana.util.DigitalSignatureUtil;
import com.aplana.cms.ServletProcessResponse;

/**
 * Extended functionality of {@link ModifyTag} for signing cards with digital signature.
 * 
 * @author EStatkevich
 */
public class ModifyWithDSTag extends ModifyTag {
	
	private static final String CURRENT_FORM_NAME = "modifyWithDS";
	private static final String PARAM_STRINGS_ARRAY = "signParam_stringsArray";
	private static final String PARAM_STRINGS_ARRAY_HASH = "signParam_stringsArrayHash";
	private static final String PARAM_SIGN_ATTR_XML = "signParam_signAttrXML";
	private static final String PARAM_CURRENT_SIGNATURE = "signParam_currentSignature";
	private static final String PARAM_IDS = "signParam_ids";
	private static final String PARAM_REQUEST = "requestFlag";

	private ArrayList<String> signatureParams;
	private String requestContextPath;
	private String clientCryptoLayer;
	private String clientCryptoLayerParams;

	private boolean isDSSupport;
	
	/**
	 * @see com.aplana.cms.tags.ModifyTag#prepareData(com.aplana.cms.Tag, com.aplana.dbmi.model.Card, com.aplana.cms.ContentProducer)
	 */
	public boolean prepareData(Tag tag, Card item, ContentProducer cms) throws Exception {
		boolean isPrepared = super.prepareData(tag, item, cms);
		if (isPrepared) {
			isDSSupport = DigitalSignatureUtil.isDsSupport(cms.getService());
			if (isDSSupport) {
				//����������� ����� ajax-������ ����� ����� �����������
				//signatureParams = DigitalSignatureUtil.prepareSignatureParams(cms.getService(), item, true, cms.getResponse().encodeURL(cms.getRequest().getContextPath()  + "/MaterialDownloadServlet?" + CardPortlet.CARD_ID_FIELD + "="));
				requestContextPath = cms.getRequest().getContextPath();
				// initialization of CryptoLayer is required to get parameters later
				CryptoLayer cryptoLayer = CryptoLayer.getInstance(Portal.getFactory().getConfigService());
				clientCryptoLayer = CryptoLayer.getConfigParam(CryptoLayer.CLIENT_CRYPTO_LAYER);
				clientCryptoLayerParams = CryptoLayer.getConfigParam(CryptoLayer.CLIENT_CRYPTO_LAYER_PARAMS);
			}
		}
		return isPrepared;
	}
	
	/**
	 * @see com.aplana.cms.tags.ModifyTag#prepareUrlParams(com.aplana.cms.ContentProducer)
	 */
	@Override
	protected HashMap<String, String> prepareUrlParams(ContentProducer cms) {
		HashMap<String, String> params = super.prepareUrlParams(cms);
		params.remove(ContentViewPortlet.PARAM_FORM);
		params.put(ContentViewPortlet.PARAM_FORM, CURRENT_FORM_NAME);
		return params;
	}

	/**
	 * @see com.aplana.cms.tags.ModifyTag#writeFormContent(java.io.PrintWriter, com.aplana.cms.Tag, com.aplana.dbmi.model.Card, com.aplana.cms.ContentProducer)
	 */
	@Override
	protected void writeFormContent(PrintWriter out, Tag tag, Card item, ContentProducer cms) {
		super.writeFormContent(out, tag, item, cms);

		if (isDSSupport) {

			out.write("<applet name=\"CryptoApplet\" id=\"CryptoApplet\"");
			out.write("	codebase=\"");
			out.write(requestContextPath);
			out.write("\" archive=\"SJBCrypto.jar\"");
			out.write("	code=\"com.aplana.crypto.CryptoApplet.class\" WIDTH=\"1\" HEIGHT=\"1\">");
			out.write("<param name=\"signOnLoad\" value=\"false\">");
			out.write("<param name=\"crypto.layer\" value=\"");
			out.write(clientCryptoLayer);
			out.write("\"> <param name=\"crypto.layer.params\" value=\"");
			out.write(clientCryptoLayerParams);
			out.write("\"> <param name=\"");
			out.write(CryptoApplet.CURENT_USER_PARAMETER);
			out.write("\" value=\"");
			out.write(cms.getService().getPerson().getId().getId().toString());
			out.write("\">");
			out.write("<param name=\"separate_jvm\" value=\"true\">");
			out.write("<H1>WARNING!</H1>");
			out.write("	The browser you are using is unable to load Java Applets!");
			out.write("</applet>");
			//����������� ����� ajax-������ ����� ����� �����������
			/*out.write("<input type=\"hidden\" id=\"" + PARAM_STRINGS_ARRAY + "\" value=\"" + StringEscapeUtils.escapeHtml(signatureParams.get(0)) + "\">");
			out.write("<input type=\"hidden\" id=\"" + PARAM_STRINGS_ARRAY_HASH + "\" value=\"" + StringEscapeUtils.escapeHtml(signatureParams.get(1)) + "\">");
			out.write("<input type=\"hidden\" id=\"" + PARAM_SIGN_ATTR_XML + "\" value=\"" + StringEscapeUtils.escapeHtml(signatureParams.get(2)) + "\">");
			out.write("<input type=\"hidden\" id=\"" + PARAM_CURRENT_SIGNATURE + "\" value=\"" + StringEscapeUtils.escapeHtml(signatureParams.get(3)) + "\">");
			out.write("<input type=\"hidden\" id=\"" + PARAM_IDS + "\" value=\"" + StringEscapeUtils.escapeHtml(signatureParams.get(4)) + "\">");*/
			out.write("<input type=\"hidden\" id=\"" + PARAM_REQUEST + "\" name = \"" + PARAM_REQUEST + "\" value=\"\">");

			out.write("<input id=\"signature\" type=\"hidden\" class=\"attrString\" name=\"");
			out.write(JspAttributeEditor.getAttrHtmlId(item.getAttributeById(DigitalSignatureUtil.ATTR_SIGNATURE)));
			out.write("\" value=\"\" />");
		}
	}

	/**
	 * @see com.aplana.cms.tags.ModifyTag#processForm(com.aplana.cms.ProcessRequest, com.aplana.cms.ProcessResponse, com.aplana.dbmi.service.DataServiceBean)
	 */
	@Override
	public boolean processForm(ProcessRequest request, ProcessResponse response, DataServiceBean service) {

		boolean isProcessed = false;
		String paramFlag = request.getParameter(PARAM_REQUEST);
		String signatureParamName = JspAttributeEditor.getAttrHtmlId((String) DigitalSignatureUtil.ATTR_SIGNATURE.getId());
		String signatureParamValue = request.getParameter(signatureParamName);
		if(StringUtils.hasLength(paramFlag)){
			try{
				ObjectId cardId = new ObjectId(Card.class, Long.parseLong(request.getParameter(PARAM_CARD)));
				Card card = (Card) service.getById(cardId);
				ArrayList<String> signatureParams = DigitalSignatureUtil.prepareSignatureParams(service, card, true, response.encodeURL(request.getContextPath()  + "/MaterialDownloadServlet?" + MaterialDownloadServlet.PARAM_CARD_ID + "="));
				JSONArray jsonObject = new JSONArray(signatureParams);
				HttpServletResponse servletResponse = ((ServletProcessResponse)response).getServletResponse();
				servletResponse.setContentType("text/xml");
				servletResponse.getWriter().write(jsonObject.toString());
			} catch (Exception e){
				logger.error("Error processing form", e);
				request.setSessionAttribute(ContentProducer.SESS_ATTR_ERROR, e.getMessage());
				return false;
			}
			isProcessed = true;
		} else if (StringUtils.hasLength(signatureParamValue)) {
			try {
				ObjectId cardId = new ObjectId(Card.class, Long.parseLong(request.getParameter(PARAM_CARD)));
				Card card = (Card) service.getById(cardId);
				DigitalSignatureUtil.storeDigitalSignature(signatureParamValue, card, service, true);
			} catch (Exception e) {
				logger.error("Error processing form", e);
				request.setSessionAttribute(ContentProducer.SESS_ATTR_ERROR, e.getMessage());
				return false;
			}
			isProcessed = true;
			} else {
				isProcessed = super.processForm(request, response, service);
		}
		return isProcessed;
	}
}
