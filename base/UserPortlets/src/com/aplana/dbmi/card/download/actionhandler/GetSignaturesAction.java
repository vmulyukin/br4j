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

import java.io.ByteArrayInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.aplana.dbmi.crypto.Base64Encoder;
import com.aplana.dbmi.crypto.SignatureConfig;
import com.aplana.dbmi.crypto.SignatureData;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.HtmlAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.PersonAttribute;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.util.ObjectIdUtils;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.DataServiceBean;

public class GetSignaturesAction extends FileActionHandler {
	
	public static final String PARAM_CARD_ID = "cardId";

	public static final String PARAM_ATTACH_ID = "attachId";

	public static final String PARAM_ONLY_SIGNERS = "onlySigners";

	private static final ObjectId SIGNER_ATTR_ID = ObjectId.predefined(CardLinkAttribute.class, "jbr.sign.set"); 

	public static final ObjectId SIGN_PERSON = ObjectId.predefined(PersonAttribute.class, "jbr.sign.person");

	public static final ObjectId SIGNATURE_ATTRIBUTE = ObjectId.predefined(
	        HtmlAttribute.class, "jbr.uzdo.signature");
	public static final ObjectId NAME_ATTRIBUTE = new ObjectId(StringAttribute.class, "NAME");

	@SuppressWarnings("unchecked")
	public void process(HttpServletRequest request, HttpServletResponse response)
			throws DataException {
		DataServiceBean ds = this.getServiceBean();
		String target_cardId = request.getParameter( PARAM_CARD_ID );
		ObjectId oidCard = new ObjectId( Card.class, Long.parseLong(target_cardId) );
		String target_attachId = request.getParameter( PARAM_ATTACH_ID );
		ObjectId oidAttach = new ObjectId( Card.class, Long.parseLong(target_attachId) );
		String onlySigners = request.getParameter( PARAM_ONLY_SIGNERS );
		try {
			Card attach = (Card) ds.getById(oidAttach);
			Card card = (Card) ds.getById(oidCard);
			CardLinkAttribute signerAttribute = (CardLinkAttribute) card.getAttributeById(SIGNER_ATTR_ID);
			HtmlAttribute signAttr = (HtmlAttribute) attach.getAttributeById(SIGNATURE_ATTRIBUTE);
			response.setContentType( "text/plain" );
			ObjectOutputStream oos = new ObjectOutputStream(response.getOutputStream());
			List<List<String>> data = new ArrayList<List<String>>();
			if (signAttr == null || signAttr.getValue() == null || signAttr.getValue().length() == 0){	
				oos.writeObject(data);
				return;
			}
			Collection linkedIds = null;
			if (signerAttribute != null) {
				linkedIds = signerAttribute.getIdsLinked();
			}
			List<ObjectId> signers = new ArrayList<ObjectId>();
			if (linkedIds != null && !linkedIds.isEmpty()) {
			Iterator iterator = linkedIds.iterator();
				while(iterator.hasNext()) {
					ObjectId cardId = (ObjectId) iterator.next();
					Card signCard = (Card) ds.getById(cardId);
					PersonAttribute signatoryAttr = (PersonAttribute) signCard.getAttributeById(SIGN_PERSON);
					signers.add(signatoryAttr.getPerson().getCardId());
				}
			}
			ObjectId userId = ds.getPerson().getCardId();
			
			List<SignatureData> signatureDatas = SignatureData.getAllSignaturesInfo(signAttr.getStringValue(), attach);
			for(SignatureData signData : signatureDatas) {
				boolean result = signData.verify(ds, true);
				boolean check = false;
				if ((signers.contains(signData.getSigner().getId())) 
						|| (onlySigners.equals("false") && userId.equals(signData.getSigner().getId()))) {
					check = true;
				}
				if ((result || signData.getCertHash() == null || "".equals(signData.getCertHash())) && check) {// ��� ����� ������� ������ ��������� PARAM_ONLY_SIGNERS � PARAM_CARD_ID
					List<String> elem = new ArrayList<String>();
					String signerName = signData.getSigner().getAttributeById(NAME_ATTRIBUTE).getStringValue();
			    	String signature = signData.getDataString();
			    	Document doc = null;
					try {
						String strXML = "<root>" + signature + "</root>";

						doc = DocumentBuilderFactory.newInstance().newDocumentBuilder()
								.parse(new ByteArrayInputStream(strXML.getBytes()));
					} catch (Exception e) {
						logger.error("Error initializing signature data from xml", e);
					}
					Element root = doc.getDocumentElement();
					Element signNode = (Element) root.getLastChild();
					String pkcs7 = signNode.getAttribute("pkcs7");
			    	String cert;
			    	if (signData.getCertHash() == null || "".equals(signData.getCertHash())) {
			    		cert = "";
			    	} else {
				    	cert = "-----BEGIN CERTIFICATE-----\n";
				    	String str = Base64Encoder.byteArrayToBase64(signData.getCert509().getEncoded());
				    	int index = 0;
				    	while (index + 64 < str.length()) {// ������ ��� ����� ���������� ������������(�� 64 ������� � ������)
				    		cert += str.substring(index, index + 64);
				    		cert += "\n";
				    		index += 64;
				    	}
				    	cert += str.substring(index, str.length());//���������� ����������
				    	cert += "\n-----END CERTIFICATE-----\n";
			    	}
			    	elem.add(signerName);
			    	elem.add(pkcs7);
			    	elem.add(cert);
			    	data.add(elem);
				}
			}
			oos.writeObject(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
