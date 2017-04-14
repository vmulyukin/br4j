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
package com.aplana.crypto.verifications.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.security.Security;
import java.security.cert.CRLException;
import java.security.cert.CertPath;
import java.security.cert.CertPathBuilder;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorResult;
import java.security.cert.CertStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.PKIXBuilderParameters;
import java.security.cert.PKIXCertPathBuilderResult;
import java.security.cert.TrustAnchor;
import java.security.cert.X509CRL;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import sun.security.x509.X509CertImpl;

import com.aplana.crypto.CryptoLayer;
import com.aplana.crypto.verifications.CertCheckResult;
import com.aplana.crypto.verifications.CertValidator;

public class CertValidatorCRLJCP extends CertValidator {
	
	private String crlURL = null;
	private String issuerCertUrl = null;

	public CertValidatorCRLJCP(CryptoLayer cryptoLayer) {
		super(cryptoLayer);
	}
	
	public CertValidatorCRLJCP(CryptoLayer cryptoLayer, String crlURL) {
		super(cryptoLayer);
		this.crlURL = crlURL;
	}
	
	public CertValidatorCRLJCP(CryptoLayer cryptoLayer, String crlURL, String issuerCertUrl) {
		super(cryptoLayer);
		this.issuerCertUrl = issuerCertUrl;
		this.crlURL = crlURL;
	}

	private static Logger log = Logger.getLogger(CertValidatorCRLJCP.class
			.getName());

	public CertCheckResult validate(X509Certificate cert509) throws Exception {
		X509CertImpl certImpl = X509CertImpl.toImpl(cert509);
		log.info("Issuer's certificate is getting.");
		X509Certificate issuerCert = null;
		if(issuerCertUrl!=null && !issuerCertUrl.isEmpty()){
			byte[] issuerCertUrlBytes = null;
			if (issuerCertUrl.startsWith("ldap://")) {
				issuerCertUrlBytes = dowloadCRLfromLDAP(issuerCertUrl);
			} else {
				issuerCertUrlBytes = getBytesFromURL(issuerCertUrl);
			}
			if(issuerCertUrlBytes != null){
				issuerCert = getCertFromInputStream(new ByteArrayInputStream(issuerCertUrlBytes));
			}
		} else {
			issuerCert = getIssuerCert(certImpl);
		}

		List<Certificate> certList = new ArrayList<Certificate>();
		certList.add(cert509);
		// TODO ����� ������� ���� ����������� ������� ��� issuerCert
		try {
			cert509.checkValidity();
		} catch (CertificateException e) {
			DateFormat df = new SimpleDateFormat();
			String errMsg = "���������� �� ������������. ����� ��������: � "
					+ df.format(cert509.getNotBefore()) + " �� "
					+ df.format(cert509.getNotAfter());
			return new CertCheckResult(CertCheckResult.CERT_STATUS.ERROR, "1",
					errMsg);
		}

		String crlUrl = getCrlURL();
		if(crlUrl == null || crlUrl.isEmpty()){
			log.info("CRLs URL is getting.");
			List<URI> crlURLs = getCRL_URIs(certImpl);
	
			if (crlURLs == null || crlURLs.size() < 1) {
				throw new Exception("� ����������� ����������� ������ ����� ��������������� CRL");
			} else {
				crlUrl = crlURLs.get(0).toString();
			}
		}
		log.info("CRL is getting: "+crlUrl);
		byte[] crlBytes;
		if (crlUrl.startsWith("ldap://")) {
			crlBytes = dowloadCRLfromLDAP(crlUrl);
		} else {
			crlBytes = getBytesFromURL(crlUrl);
		}
		if (crlBytes == null)
			throw new Exception("CRL �� �������. ��� ������������ �������� ���.");
		log.info("CRL is checking.");
		X509CRL crlCert;
		boolean crlValid = false;
		try {
			crlValid = isCRLValid(crlBytes);
			crlCert = getCRLfromBytes(crlBytes);
		} catch (Exception e) {
			throw new Exception("�������� CRL �� �������.");
		}
		if (!crlValid)
			throw new Exception("CRL ���������");

		log.info("CertChain is creating.");
		Security.setProperty("ocsp.enable", "false");
		System.setProperty("com.sun.security.enableCRLDP", "false");
		System.setProperty("com.ibm.security.enableCRLDP", "false");
		if(issuerCert == null){//���� ��� ��������� �����������, �� �������� ������� ��������.
			if(crlCert.isRevoked(cert509)){
				return new CertCheckResult(CertCheckResult.CERT_STATUS.REVOKED, null, null);
			} else {
				return new CertCheckResult(CertCheckResult.CERT_STATUS.GOOD, null, null);
			}
		}
		TrustAnchor tr = new TrustAnchor(issuerCert, null);
		Set<TrustAnchor> trust = new HashSet<TrustAnchor>();
		trust.add(tr);
		

		try {
			PKIXBuilderParameters cpp = new PKIXBuilderParameters(trust, null);
			cpp.setSigProvider(null);
			List certListStore = new ArrayList();
			certListStore.addAll(certList);
			certListStore.add(crlCert);

			CollectionCertStoreParameters par = new CollectionCertStoreParameters(
					certListStore);
			CertStore store = CertStore.getInstance("Collection", par);
			cpp.addCertStore(store);
			X509CertSelector selector = new X509CertSelector();
			selector.setCertificate(cert509);
			cpp.setTargetCertConstraints(selector);

			cpp.setRevocationEnabled(false);
			PKIXCertPathBuilderResult res = (PKIXCertPathBuilderResult) CertPathBuilder
					.getInstance("PKIX").build(cpp);
			CertPath cp = res.getCertPath();

			log.info("CertChain is checking.");
			CertPathValidator cpv = CertPathValidator.getInstance("PKIX");
			cpp.setRevocationEnabled(true);
			CertPathValidatorResult cpvResult = cpv.validate(cp, cpp);

			return new CertCheckResult(CertCheckResult.CERT_STATUS.GOOD, null,
					null);

		} catch (Exception e) {
			e.printStackTrace();
			return new CertCheckResult(CertCheckResult.CERT_STATUS.ERROR, "-1",
					"������ ��� ���������� � ��������� ������� - "
							+ e.getMessage());
		}
	}

	public byte[] dowloadCRLfromLDAP(String ldapURL) {
		Map<String, String> env = new Hashtable<String, String>();
		env.put(Context.INITIAL_CONTEXT_FACTORY,
				"com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, ldapURL); // ldap://localhost:389/ou=People,o=acme,dc=my-domain,dc=com";

		try {
			DirContext ctx = new InitialDirContext(
					(Hashtable<String, String>) env);
			Attributes avals = (Attributes) ctx.getAttributes("");
			Attribute aval = (Attribute) avals
					.get("certificateRevocationList;binary");
			byte[] crlBytes = (byte[]) aval.get();
			if ((crlBytes == null) || (crlBytes.length == 0)) {
				throw new Error("Can not download CRL from: " + ldapURL);
			} else {
				return crlBytes;
			}
		} catch (NamingException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static byte[] getBytesFromURL(String url) throws Exception {
		byte[] contentByte = null;
		try {
			URL urlFile = new URL(url);
			InputStream in = null;
			try {
				in = urlFile.openStream();
				contentByte = getBytesFromStream(in);
			} catch (IOException e) {
				e.printStackTrace();
				throw new Exception("������ ��� ������� crl � �������");
			} finally {
				try {
					if (in != null)
						in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
			throw new Exception("������������ url ");
		}
		return contentByte;
	}

	public static byte[] getBytesFromStream(InputStream in) throws IOException {
		byte[] buffer = new byte[1024];
		int len;
		int size = 0;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		while ((len = in.read(buffer)) >= 0) {
			out.write(buffer, 0, len);
			size = size + len;
		}
		return out.toByteArray();
	}

	public boolean isCRLValid(byte[] crlBytes) throws CertificateException,
			CRLException {
		X509CRL crl = getCRLfromBytes(crlBytes);
		Date dtNow = new Date();
		Date dt = crl.getNextUpdate();
		return dt.after(dtNow);
	}

	public X509CRL getCRLfromBytes(byte[] crlBytes)
			throws CertificateException, CRLException {
		CertificateFactory cf = CertificateFactory.getInstance("X509");
		X509CRL crl = (X509CRL) cf.generateCRL(new ByteArrayInputStream(
				crlBytes));
		return crl;
	}

	public String getCrlURL() {
		return crlURL;
	}

	public void setCrlURL(String crlURL) {
		this.crlURL = crlURL;
	}

	public String getIssuerCertUrl() {
		return issuerCertUrl;
	}

	public void setIssuerCertUrl(String issuerCertUrl) {
		this.issuerCertUrl = issuerCertUrl;
	}
	
}
