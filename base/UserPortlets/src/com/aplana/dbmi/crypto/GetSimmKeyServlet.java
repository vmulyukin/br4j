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
package com.aplana.dbmi.crypto;

import java.io.IOException;
import java.security.cert.X509Certificate;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.aplana.crypto.CryptoLayer;
import com.aplana.crypto.cryptoserviceproxy.CryptoService;
import com.aplana.dbmi.action.Search;
import com.aplana.dbmi.ajax.AbstractDBMIAjaxServlet;
import com.aplana.dbmi.card.CertAttributeEditor;
import com.aplana.dbmi.model.Attribute;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.Person;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.service.DataServiceBean;

public class GetSimmKeyServlet extends AbstractDBMIAjaxServlet {
	static final long serialVersionUID = 1L;
		
	public String verWebSer = "false";
	public String cryptoServerUrl = null;

	private CryptoService cryptoService;
	

	protected void generateResponse(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		
		DataServiceBean dataService = getDataServiceBean(request);		
		String respString = "";
		Card persCard  = null;
		Person pers = null;
		//String simmKey = null;
		String certHash = null;
		
		final String sessionVar_Key = "APPLICATION_CRYPTO_SIMMKEY";
		Boolean certStatus = false;
		try {
			try{	
				persCard = (Card)dataService.getById(new ObjectId(Card.class, 1));
			}catch(Exception e){
				
			}
			persCard = null;
			Search action = new Search();
			boolean result = dataService.canDo(action); //��� ������������� ������������ ������ DS
			
			//�������� �� �������������
			if(!dataService.getIsDelegation()){
				pers = dataService.getPerson();
			} else { //���� ���� �������������, �� � ����� ��������� ������������, �.� �� �� ���������� ������.
				pers = dataService.getRealUser().getPerson();
			}
			
			if(pers == null){
				logger.error("Couldn't get Person");
				return;
			}else{
				logger.debug("Person is: " + pers.getFullName());
			}
					
			if(pers.getCardId() == null){
				logger.error("Couldn't get Person Card ID");
				return;
			}
			ObjectId id = pers.getCardId();
			logger.debug("Got Person Card id" + id.toString());
			X509Certificate cert509 = null;
			try{				
				persCard = (Card)dataService.getById(id);
				CardLinkAttribute attrActualCert = (CardLinkAttribute)persCard.getAttributeById(SignatureData.actualCertificateAttrId);
				if (attrActualCert != null && attrActualCert.getLinkedCount()>0){
					Card cert = (Card)dataService.getById(attrActualCert.getIdsArray()[0]);
					StringAttribute attrHash = (StringAttribute)cert.getAttributeById(CertAttributeEditor.certHashAttrId);  
					final Attribute certAttr = cert.getAttributeById(SignatureData.certAttrId);
					if (certAttr != null) {
						cert509 = (X509Certificate) CryptoLayer.getInstance().getCertFromStringBase64(certAttr.getStringValue());
						if (cert509 == null) {
							logger.error("Person card has wrong cert attribute!");
							return;
						}
						certStatus =  SignatureData.checkCertificate(cert509);
						if(!certStatus) {
							logger.error("Cert revacation checking failed");
						}
					} else {
						logger.error("Person card has no cert attribute!");
						return;
					}
					if(attrHash != null){
						certHash = attrHash.getStringValue();
					}else{
						logger.error("Person card has no CertHash attribute");
						return;
					}					
				}else{
					logger.error("Person card has no Certificates");
					return;					
				}
					
			}catch(Exception e){				
				logger.error("error getting cert hash " + id, e);
				return;
			}
			
			//���������� �� ������ hash, �� � certficateId � ����� � ���������� ������� ���������� xml �������
			response.getWriter().print(cert509.getSerialNumber().toString().replace("\"", ""));
			response.getWriter().print(", ");
			response.getWriter().print(cert509.getIssuerX500Principal().toString().replace("\"", ""));
			response.getWriter().print("#delim#");
			response.getWriter().print(certHash);
			response.getWriter().print("#delim#");
			response.getWriter().print(certStatus);
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}
}