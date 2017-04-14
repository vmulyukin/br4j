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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.security.Security;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertStore;
import java.security.cert.CertStoreParameters;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.PKIXCertPathValidatorResult;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import sun.security.provider.certpath.OCSP;
import sun.security.provider.certpath.OCSP.RevocationStatus;
import sun.security.provider.certpath.OCSP.RevocationStatus.CertStatus;
import sun.security.util.ObjectIdentifier;
import sun.security.x509.AccessDescription;
import sun.security.x509.AuthorityInfoAccessExtension;
import sun.security.x509.DistributionPoint;
import sun.security.x509.GeneralName;
import sun.security.x509.GeneralNameInterface;
import sun.security.x509.URIName;
import sun.security.x509.X509CertImpl;

public class CertificateRevocationChecker {

	public X509Certificate getCertFromFile(String filePath) {
		try {
			return getCertFromInputStream(new FileInputStream(new File(filePath)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public X509Certificate getCertFromInputStream(InputStream stream) {
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

	public static void main(String[] args) {
		Security.setProperty("ocsp.enable", "false");
		CertificateRevocationChecker checker = new CertificateRevocationChecker();
		X509Certificate cert = checker.getCertFromFile("D:\\temp\\cert.cer");
		try {
			System.out.println(checker.checkCertChainByCRL(cert));
			System.out.println(checker.checkCertChaintByOCSP(cert));
			System.out.println(checker.checkCertByOCSP(cert));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private X509Certificate getIssuerCert(X509CertImpl certImpl){
		InputStream IssuerCertInputStream = null;
		try {
			IssuerCertInputStream = getIssuerCertURI(certImpl).toURL().openStream();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return getCertFromInputStream(IssuerCertInputStream);
	}
	
	public URI getIssuerCertURI(X509CertImpl certImpl){
		return getAccessDescriptionProp(certImpl, AccessDescription.Ad_CAISSUERS_Id);
	}
	
	public URI getOcspURI(X509CertImpl certImpl){
		return getAccessDescriptionProp(certImpl, AccessDescription.Ad_OCSP_Id);
	}
	
	private URI getAccessDescriptionProp(X509CertImpl certImpl, ObjectIdentifier identifier) {

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
	
	private List<URI> getCRL_URIs(X509CertImpl certImpl) throws IOException{
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
	
	public boolean checkCertChainByCRL(X509Certificate cert){
		X509CertImpl certImpl = null;
	    CertPath cp = null;
	    CertificateFactory cf = null;
		try {
			cf = CertificateFactory.getInstance("X509");
			Vector<X509Certificate> certs = new Vector<X509Certificate>();
			certs.add(cert);

			certImpl = X509CertImpl.toImpl(cert);
			cp = (CertPath)cf.generateCertPath(certs);
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		X509Certificate rootCACert = getIssuerCert(certImpl);
	    TrustAnchor ta = new TrustAnchor(rootCACert, null);
	    Set<TrustAnchor> trustedCerts = new HashSet<TrustAnchor>();
	    trustedCerts.add(ta);
	    try {
			for(URI uri: getCRL_URIs(certImpl)){
				PKIXParameters params = new PKIXParameters(trustedCerts);
			    if (uri.toURL() != null) {
			    	
					InputStream inStream = uri.toURL().openStream();
					X509CRL crl = (X509CRL)cf.generateCRL(inStream);
					inStream.close();
					System.out.println(crl.isRevoked(cert));
				    params.addCertStore(CertStore.getInstance("Collection", new CollectionCertStoreParameters(Collections.singletonList(crl))));
				}
			    CertPathValidator cpv = CertPathValidator.getInstance("PKIX");
			    PKIXCertPathValidatorResult cpv_result  = (PKIXCertPathValidatorResult) cpv.validate(cp, params);
			    X509Certificate trustedCert = (X509Certificate)cpv_result.getTrustAnchor().getTrustedCert();
			    if (trustedCert == null) {
			    	System.out.println("Trusted Cert = NULL");
			    	return false;
			    } else {
			    	System.out.println("Trusted CA DN = " +
				    trustedCert.getSubjectDN());
			    }
			}
	    	return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	public CertStatus checkCertByOCSP(X509Certificate cert){
		X509CertImpl certImpl;
		try {
			certImpl = X509CertImpl.toImpl(cert);
			X509Certificate issuerCert = getIssuerCert(certImpl);
			return OCSP.check(cert, issuerCert).getCertStatus();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return RevocationStatus.CertStatus.UNKNOWN;
		}

	}
	
	public boolean checkCertChaintByOCSP(X509Certificate cert){
		X509CertImpl certImpl = null;
	    CertPath cp = null;
	    CertificateFactory cf = null;
		try {
			cf = CertificateFactory.getInstance("X509");
			Vector<X509Certificate> certs = new Vector<X509Certificate>();
			certs.add(cert);

			certImpl = X509CertImpl.toImpl(cert);
			cp = (CertPath)cf.generateCertPath(certs);
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		X509Certificate rootCACert = getIssuerCert(certImpl);
	    // init trusted certs
	    TrustAnchor ta = new TrustAnchor(rootCACert, null);
	    Set trustedCertsSet = new HashSet();
	    trustedCertsSet.add(ta);

	    // init cert store
	    Set certSet = new HashSet();
	    certSet.add(rootCACert);
	    CertStoreParameters storeParams =
		new CollectionCertStoreParameters(certSet);
	    try{
		    CertStore store = CertStore.getInstance("Collection", storeParams);
		    
		    PKIXParameters params = null;
	        params = new PKIXParameters(trustedCertsSet);
	    	params.addCertStore(store);
		    Security.setProperty("ocsp.enable", "true");
		    URI ocspServer = getOcspURI(certImpl);
		    if (ocspServer != null) {
				Security.setProperty("ocsp.responderURL", ocspServer.toString());
				Security.setProperty("ocsp.responderCertSubjectName", rootCACert.getSubjectX500Principal().getName());
		    }
		    
		    CertPathValidator cpv = CertPathValidator.getInstance("PKIX");
		    PKIXCertPathValidatorResult cpv_result  =
			(PKIXCertPathValidatorResult) cpv.validate(cp, params);
		    X509Certificate trustedCert = (X509Certificate)
			cpv_result.getTrustAnchor().getTrustedCert();
		    
		    if (trustedCert == null) {
		    	System.out.println("Trsuted Cert = NULL");
		    	return false;
		    } else {
		    	System.out.println("Trusted CA DN = " + trustedCert.getSubjectDN());
		    	return true;
		    }
		    
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
}
