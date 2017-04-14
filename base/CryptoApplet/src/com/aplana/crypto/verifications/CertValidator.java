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

package com.aplana.crypto.verifications;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import com.aplana.crypto.Base64;
import com.aplana.crypto.CryptoLayer;

import sun.security.util.ObjectIdentifier;
import sun.security.x509.AccessDescription;
import sun.security.x509.AuthorityInfoAccessExtension;
import sun.security.x509.DistributionPoint;
import sun.security.x509.GeneralName;
import sun.security.x509.GeneralNameInterface;
import sun.security.x509.URIName;
import sun.security.x509.X509CertImpl;

public abstract class CertValidator {

	protected CryptoLayer cryptoLayer;
	public static final int[] JCP_GOST_DIGEST_OID = new int[]{1, 2, 643, 2, 2, 9};//"1.2.643.2.2.9" = JCP.GOST_DIGEST_OID
	
	public CertValidator(CryptoLayer cryptoLayer) {
		this.cryptoLayer = cryptoLayer;
	}
	
	public abstract CertCheckResult validate(X509Certificate cert509) throws Exception;
	
	protected X509Certificate getCertFromInputStream(InputStream stream) {
		X509Certificate retVal = null;
		try {
			final CertificateFactory cf = CertificateFactory
					.getInstance("X509");
			if (stream != null) {
				retVal = (X509Certificate)cf.generateCertificate(stream);
				System.out.println("Certificate was got from file successfully!");
			} else {
				System.out.println("Unable to read Certificate...");
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return retVal;
		}
	}
	
	protected URI getIssuerCertURI(X509CertImpl certImpl){
		return getAccessDescriptionProp(certImpl, AccessDescription.Ad_CAISSUERS_Id);
	}
	
	protected URI getOcspURI(X509CertImpl certImpl){
		return getAccessDescriptionProp(certImpl, AccessDescription.Ad_OCSP_Id);
	}
	
	protected URI getAccessDescriptionProp(X509CertImpl certImpl, ObjectIdentifier identifier) {

		// Examine the certificate's AuthorityInfoAccess extension
		AuthorityInfoAccessExtension aia = certImpl
				.getAuthorityInfoAccessExtension();
		if (aia == null) {
			return null;
		}
		List<AccessDescription> descriptions = aia.getAccessDescriptions();
		for (AccessDescription description : descriptions) { 
			if (description.getAccessMethod().equals(identifier)) {
				GeneralName generalName = description.getAccessLocation();
				if (generalName.getType() == GeneralNameInterface.NAME_URI) {
					URIName uri = (URIName) generalName.getName();
					return uri.getURI();
				}
			}
		}
		return null;
	}
	
	protected List<URI> getCRL_URIs(X509CertImpl certImpl) throws IOException{
		List<URI> result = new ArrayList<URI>();
		List pointsList = (List)certImpl.getCRLDistributionPointsExtension().get("POINTS");
		for(Object object: pointsList){
			DistributionPoint point = (DistributionPoint)object;
			for(GeneralName name:point.getFullName().names()){
				result.add(((URIName)name.getName()).getURI());
			}
		}
		return result;
	}
	
	protected X509Certificate getIssuerCert(X509CertImpl certImpl){
		InputStream IssuerCertInputStream = null;
		try {
			URI issuerCertURI = getIssuerCertURI(certImpl);
			if(issuerCertURI != null){
				IssuerCertInputStream = getIssuerCertURI(certImpl).toURL().openStream();
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return getCertFromInputStream(IssuerCertInputStream);
	}
	
}