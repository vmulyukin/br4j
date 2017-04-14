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
package com.aplana.crypto;

import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

public class CryptoObject {
	private String subject = "";
	private String base64cert = "";
	private String certhash = "";
	private String signature = "";
	private X509Certificate X509cert = null;

	public CryptoObject(String inBase64Cert) {
		try {
			if (inBase64Cert != null) {
				base64cert = inBase64Cert;
				X509cert = (X509Certificate) CryptoLayer.getInstance()
						.getCertFromStringBase64(inBase64Cert);
				setCertData(X509cert);
			} else {
				base64cert = "";
			}
			signature = "";
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public CryptoObject(String inBase64Cert, String inSignature, String cryptoLayerClass) {
		this(inBase64Cert);
		try {

			if (inSignature != null) {
				signature = inSignature;
			} else {
				signature = "";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public String getSubject() {
		return subject;
	}

	public String getBase64Cert() {
		return base64cert;
	}

	public String getCertHash() {
		return certhash;
	}

	public String getSignature() {
		return signature;
	}

	public Certificate getCertificate() {
		return X509cert;
	}

	private void setCertData(X509Certificate inCert) {
		try {
			subject = inCert.getSubjectX500Principal().getName();
			certhash = CryptoLayer.getInstance().getCertDigestString((Certificate) inCert);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
