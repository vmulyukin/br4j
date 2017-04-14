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
package com.aplana.dbmi.jbr.processors;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;

import com.aplana.crypto.CryptoLayer;
import com.aplana.dbmi.Portal;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.CardLinkAttribute;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.model.Template;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ObjectQueryBase;
import com.aplana.dbmi.service.impl.ProcessorBase;

public class SetPersonActualCertificate extends ProcessorBase{

	public static final ObjectId certificatesAttrId = ObjectId.predefined(CardLinkAttribute.class, "jbr.person.certificates");
	public static final ObjectId actualCertificateAttrId = ObjectId.predefined(CardLinkAttribute.class, "jbr.person.actualCertificate");
	public static final ObjectId certificateTemplateId = ObjectId.predefined(Template.class, "jbr.certificate");
	public static final ObjectId certAttrId = ObjectId.predefined(StringAttribute.class, "jbr.certificate.cert");

	@Override
	public Object process() throws DataException {
		Card card = (Card)getObject();

		if (card == null) {
			return null;
		}

		CardLinkAttribute certificatesAttr = (CardLinkAttribute)card.getAttributeById(certificatesAttrId);
		CardLinkAttribute actualCertificateAttr = (CardLinkAttribute)card.getAttributeById(actualCertificateAttrId);
		
		if(!actualCertificateAttr.isEmpty()){
			if(checkCertificate(actualCertificateAttr.getSingleLinkedId())){
				return card;
			} else {
				throw new DataException("select.valid.certificate");
			}
		} else {
			ObjectId actualCertificateId = null;
			for (Iterator<ObjectId> certificateCardIdIterator = certificatesAttr.getIdsLinked().iterator(); certificateCardIdIterator.hasNext();) {
				ObjectId cert = certificateCardIdIterator.next();
				if(checkCertificate(cert)){
					actualCertificateId = cert; 
				}
			}
			if(actualCertificateId!=null){
				actualCertificateAttr.clear();
				actualCertificateAttr.addLinkedId(actualCertificateId);
			}
		}	
		return card;
	}

	private Card loadCard(ObjectId cardId) throws DataException{
		final ObjectQueryBase cardQuery = getQueryFactory().getFetchQuery(Card.class);
		cardQuery.setAccessChecker(null);
		cardQuery.setId(cardId);
		return (Card) getDatabase().executeQuery(getSystemUser(), cardQuery);
	}
	
	private boolean checkCertificate(ObjectId certId) throws DataException{
		Card certificateCard = loadCard(certId);
		StringAttribute certAttr = (StringAttribute)certificateCard.getAttributeById(certAttrId);
		try {
			X509Certificate cert509 = (X509Certificate)CryptoLayer.getInstance(Portal.getFactory().getConfigService()).getCertFromStringBase64(certAttr.getStringValue());
			cert509.checkValidity();
			return true;
		}
		catch (Exception ex) {return false;}
	}
}
