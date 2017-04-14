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
import java.text.MessageFormat;
import java.util.ResourceBundle;

import com.aplana.crypto.CryptoLayer;
import com.aplana.dbmi.Portal;
import com.aplana.dbmi.model.Card;
import com.aplana.dbmi.model.ContextProvider;
import com.aplana.dbmi.model.ObjectId;
import com.aplana.dbmi.model.StringAttribute;
import com.aplana.dbmi.service.DataException;
import com.aplana.dbmi.service.impl.ProcessorBase;

public class GenerateCertificateName extends ProcessorBase {

	private static final long serialVersionUID = 6126502682619638099L;

	public static final ObjectId nameAttrId = ObjectId.predefined(StringAttribute.class, "name");
	public static final ObjectId certAttrId = ObjectId.predefined(StringAttribute.class, "jbr.certificate.cert");

	static final ResourceBundle bundle = ResourceBundle.getBundle("jbr", ContextProvider.getContext().getLocale());

	@Override
	public Object process() throws DataException {
		Card card = (Card)getObject();

		if (card == null) {
			return null;
		}

		StringAttribute nameAttr = (StringAttribute)card.getAttributeById(nameAttrId);
		StringAttribute certAttr = (StringAttribute)card.getAttributeById(certAttrId);
	
		X509Certificate cert509 = (X509Certificate)CryptoLayer.getInstance(Portal.getFactory().getConfigService()).getCertFromStringBase64(certAttr.getStringValue());
		//������ ����� ������������ ������ ������������ ����
		if (cert509 == null) {
			throw new DataException("jbr.cert.invalid.certificate");
		}
		nameAttr.setValue(MessageFormat.format(bundle.getString("certificate.store.namePattern"), new Object[] {cert509.getSerialNumber(), cert509.getNotBefore(), cert509.getNotAfter()}));
		
		return card;
	}

}
