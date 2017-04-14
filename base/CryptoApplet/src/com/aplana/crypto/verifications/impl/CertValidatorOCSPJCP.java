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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.lang.ArrayUtils;

import com.aplana.crypto.Base64;
import com.aplana.crypto.CryptoLayer;
import com.aplana.crypto.verifications.CertCheckResult;
import com.aplana.crypto.verifications.CertCheckResult.CERT_STATUS;
import com.aplana.crypto.verifications.CertValidator;
import com.objsys.asn1j.runtime.Asn1BerDecodeBuffer;
import com.objsys.asn1j.runtime.Asn1BerEncodeBuffer;
import com.objsys.asn1j.runtime.Asn1DerDecodeBuffer;
import com.objsys.asn1j.runtime.Asn1DerEncodeBuffer;
import com.objsys.asn1j.runtime.Asn1Exception;
import com.objsys.asn1j.runtime.Asn1Null;
import com.objsys.asn1j.runtime.Asn1OctetString;

import ru.CryptoPro.JCP.ASN.PKIX1Explicit88.AlgorithmIdentifier;
import ru.CryptoPro.JCP.ASN.PKIX1Explicit88.CertificateSerialNumber;
import ru.CryptoPro.JCP.ASN.PKIX1Explicit88.SubjectPublicKeyInfo;
import ru.CryptoPro.JCP.ASN.PKIXOCSP.BasicOCSPResponse;
import ru.CryptoPro.JCP.ASN.PKIXOCSP.CertID;
import ru.CryptoPro.JCP.ASN.PKIXOCSP.CertStatus;
import ru.CryptoPro.JCP.ASN.PKIXOCSP.OCSPRequest;
import ru.CryptoPro.JCP.ASN.PKIXOCSP.OCSPResponse;
import ru.CryptoPro.JCP.ASN.PKIXOCSP.OCSPResponseStatus;
import ru.CryptoPro.JCP.ASN.PKIXOCSP.OCSPVersion;
import ru.CryptoPro.JCP.ASN.PKIXOCSP.ReqCert;
import ru.CryptoPro.JCP.ASN.PKIXOCSP.Request;
import ru.CryptoPro.JCP.ASN.PKIXOCSP.ResponseBytes;
import ru.CryptoPro.JCP.ASN.PKIXOCSP.ResponseData;
import ru.CryptoPro.JCP.ASN.PKIXOCSP.SingleResponse;
import ru.CryptoPro.JCP.ASN.PKIXOCSP.TBSRequest;
import ru.CryptoPro.JCP.ASN.PKIXOCSP._SeqOfRequest;
import ru.CryptoPro.reprov.x509.AccessDescription;
import ru.CryptoPro.reprov.x509.AuthorityInfoAccessExtension;
import ru.CryptoPro.reprov.x509.CRLDistributionPointsExtension;
import ru.CryptoPro.reprov.x509.CertificateExtensions;
import ru.CryptoPro.reprov.x509.DistributionPoint;
import ru.CryptoPro.reprov.x509.ExtendedKeyUsageExtension;
import ru.CryptoPro.reprov.x509.GeneralName;
import ru.CryptoPro.reprov.x509.GeneralNames;
import ru.CryptoPro.reprov.x509.X509CertInfo;
import sun.security.x509.X509CertImpl;

public class CertValidatorOCSPJCP extends CertValidator{
	public static final int[] PKIX_OCSP_BASIC = {1, 3, 6, 1, 5, 5, 7, 48, 1, 1};
	public static final String OCSP_SIGN_AUTH = "1.3.6.1.5.5.7.3.9";
	private String issuerCertUrl = null;
	public CertValidatorOCSPJCP(CryptoLayer cryptoLayer) {
		super(cryptoLayer);
	}

	private static Logger log = Logger.getLogger(CertValidatorOCSPJCP.class.getName());
	
	public CertCheckResult validate(X509Certificate cert509) throws Exception {	
		try{
			cert509.checkValidity();
			return validateByOCSP(cert509);
		} catch (CertificateException e) {
			DateFormat df = new SimpleDateFormat();
			String errMsg = "���������� �� ������������. ����� ��������: � " + df.format(cert509.getNotBefore()) + " �� " + df.format(cert509.getNotAfter());
			return new CertCheckResult(CertCheckResult.CERT_STATUS.ERROR, "1", errMsg);
		}
	}

	public CertCheckResult validateByOCSP(X509Certificate cert509) throws Exception {
		X509CertImpl certImpl = X509CertImpl.toImpl(cert509);
		return certValidate(cert509, getIssuerCert(certImpl), getOcspURI(certImpl).toURL());
	}
	
	private CertCheckResult certValidate(X509Certificate cert, X509Certificate issuerCert, URL ocspURL) throws Exception{
		
		CertID certID ;
		try {
			certID = getCerID(cert, issuerCert);
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e.getMessage());
		}
		
		byte[] dataRequst = makeRequest(certID);
		log.info("OCSP request ist created");
		
		byte[] dataResponse = sendRequest(dataRequst, ocspURL);
		log.info("response is received");
		
		return parseResponse(dataResponse, certID);
	}

	public CertCheckResult parseResponse(byte[] respons, CertID certID) throws Exception {

		try {
			CertCheckResult result;
			OCSPResponse ocspResponse = new OCSPResponse();
			ocspResponse.decode(new Asn1DerDecodeBuffer(respons));

			OCSPResponseStatus respState = ocspResponse.responseStatus;
			String[] OCSPRespStatus = { "Response has valid confirmations", "Illegal confirmation request", "Internal error in issuer",
										"Try again later", "not used", "Must sign the request", "Request unauthorized" };
			if (respState.value != 0) {
				return new CertCheckResult(CERT_STATUS.ERROR, "" + respState.value, OCSPRespStatus[respState.value]);
			} else {
				ResponseBytes respByte = ocspResponse.responseBytes;
				// минимум - id-pkix-ocsp-basic = { 1 3 6 1 5 5 7 48 1 1 }
				if (!Arrays.equals(PKIX_OCSP_BASIC, respByte.responseType.value))
					throw new Exception(Arrays.toString(respByte.responseType.value));

				Asn1OctetString oct = respByte.response;
				Asn1BerEncodeBuffer berbuf = new Asn1BerEncodeBuffer();
				oct.encode(berbuf, false);
				Asn1BerDecodeBuffer andecbuf = new Asn1BerDecodeBuffer(berbuf.getMsgCopy());
				BasicOCSPResponse respBasicResp = new BasicOCSPResponse();
				respBasicResp.decode(andecbuf);
				
				if (respBasicResp.certs.elements.length < 1) throw new Exception("� OCSP ������ ����������� ���������� ������.");
				
				berbuf = new Asn1BerEncodeBuffer();
				respBasicResp.certs.elements[0].encode(berbuf);
				String ocspCertB64 = Base64.byteArrayToBase64(berbuf.getMsgCopy());
				X509Certificate cert = (X509Certificate) cryptoLayer.getX509CertFromStringBase64(ocspCertB64);
				cert.checkValidity();

				X509CertInfo info = new X509CertInfo(cert.getTBSCertificate());
				CertificateExtensions certExts = (CertificateExtensions) info.get(X509CertInfo.EXTENSIONS);	
				ExtendedKeyUsageExtension ocspExt = (ExtendedKeyUsageExtension) certExts.get("ExtendedKeyUsage");
				String OCSPSigning = ocspExt.getExtendedKeyUsage().get(0).toString();
				if (!OCSPSigning.equals(OCSP_SIGN_AUTH)) throw new Exception("���������� OCSP ������ �� ����� ���������� �� ������� OCSP ������.");

				ResponseData respData = respBasicResp.tbsResponseData;

				berbuf = new Asn1BerEncodeBuffer();
				respData.encode(berbuf);
				byte[] data = berbuf.getMsgCopy();
				byte[] signature = respBasicResp.signature.value;
				ArrayUtils.reverse(signature);
				List ret = cryptoLayer.verifyByteArraySignature(data, signature, cert);
				final String err = (String) ret.get(0);
				if (err != null || !"true".equals(ret.get(1))) {
					throw new Exception("������� ������ OCSP ������ �� �����.");
				}
				SingleResponse response = respData.responses.elements[0];
				ReqCert reqCert = response.reqCert;
				CertID cerIDresp = (CertID) reqCert.getElement();
				boolean certEq = certID.serialNumber.equals(cerIDresp.serialNumber);
				certEq = certEq && certID.issuerNameHash.equals(cerIDresp.issuerNameHash);
				certEq = certEq && certID.issuerKeyHash.equals(cerIDresp.issuerKeyHash);
				certEq = certEq && certID.hashAlgorithm.algorithm.equals(cerIDresp.hashAlgorithm.algorithm);
				if (!certEq) throw new Exception("���������� � ������� � ������ OCSP �� ��������� � ������������ �� ������ OCSP.");

				CertStatus state = response.certStatus;
				String status = state.getElemName().toUpperCase();
				log.info("Certificates status - " + status);
				CERT_STATUS st = CERT_STATUS.valueOf(status);
				result = new CertCheckResult(st, null, null);
				return result;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("������ ��� ������� ������ �� ������ OCS");
		} 
	}
	
	private CertID getCerID(X509Certificate cert, X509Certificate issuerCert) throws Asn1Exception, IOException {
		
		CertID certID = new CertID();
		AlgorithmIdentifier hashAlgId = new AlgorithmIdentifier(JCP_GOST_DIGEST_OID);
		hashAlgId.parameters = new Asn1Null();
		certID.hashAlgorithm = hashAlgId;
		certID.issuerNameHash = new Asn1OctetString(cryptoLayer.getByteArrayDigest((cert.getIssuerX500Principal().getEncoded())));
		SubjectPublicKeyInfo spki = new SubjectPublicKeyInfo();
		Asn1BerDecodeBuffer derbuf = new Asn1BerDecodeBuffer(issuerCert.getPublicKey().getEncoded());
		spki.decode(derbuf);
		byte[] pubkeyBytes = spki.subjectPublicKey.value;
		certID.issuerKeyHash = new Asn1OctetString(cryptoLayer.getByteArrayDigest(pubkeyBytes));
		certID.serialNumber = new CertificateSerialNumber(cert.getSerialNumber());
		return certID;
	}
	
	private byte[] makeRequest(CertID certID) throws Exception {

		_SeqOfRequest requestList = new _SeqOfRequest(1);
		ReqCert reqCert = new ReqCert();
		reqCert.set_certID(certID);
		requestList.elements[0] = new Request(reqCert);

		TBSRequest tbsReq = new TBSRequest();
		tbsReq.version = new OCSPVersion(0); 
		tbsReq.requestorName = null; 
		tbsReq.requestList = requestList;

		OCSPRequest req = new OCSPRequest(tbsReq);

		byte[] data = null; 
		Asn1DerEncodeBuffer encData = new Asn1DerEncodeBuffer();
		req.encode(encData);
		data = encData.getMsgCopy();
		return data;
	}

	private static byte[] sendRequest(byte[] request, URL url) throws Exception {
		byte[] data = null;

		OutputStream out = null;
		InputStream is = null;

		try {
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setUseCaches(false);
			con.setDefaultUseCaches(false);
			con.setDoOutput(true);
			con.setDoInput(true);
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/ocsp-request");
			con.setRequestProperty("Accept", "application/ocsp-response");
			out = con.getOutputStream();
			out.write(request);
			if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
				throw new Exception(con.getResponseMessage());
			} else {
				is = con.getInputStream();
				byte[] buf = new byte[1024];
				int len;
				ByteArrayOutputStream baout = new ByteArrayOutputStream();
				while ((len = is.read(buf)) > 0) {
					baout.write(buf, 0, len);
				}
				data = baout.toByteArray();
			}
		} catch (IOException e) {
			//e.printStackTrace();
			throw new Exception(e.getMessage());
		} finally {
			try {
				if (out != null) out.close();
				if (is != null) is.close();
			} catch (IOException e) {
				
			}
		}
		return data;
	}
}
