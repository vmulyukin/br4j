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

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Properties;

import javax.jws.HandlerChain;
import javax.jws.WebService;

import com.aplana.crypto.verifications.CertCheckResult;
import com.aplana.crypto.verifications.impl.CertValidatorCRLJCP;
import com.aplana.crypto.verifications.impl.CertValidatorOCSPJCP;

@WebService
public class CryptoService {
	public static final String SERVER_CRYPTO_LAYER = "server.crypto.layer";
	public static final String SERVER_CRYPTO_LAYER_PARAMS = "server.crypto.layer.params";
	public static final String CERTIFICATE_CRL_CHECK_ENABLE = "certificate.crl_checking";
	public static final String CERTIFICATE_CRL_URL = "certificate.crl_url";
	public static final String CERTIFICATE_ISSIER_CERT_URL = "certificate.crl_issuer_cert";
	public static final String CERTIFICATE_OCSP_CHECK_ENABLE = "certificate.ocsp_checking";
	
	private static Properties configProp; 
	private static Properties checkingConfigProp;

	public boolean checkStringContentSignature(String document, String sign, String base64certificate) throws CryptoServiceException {
		try {
			
			CryptoLayer cryptoLayer = CryptoLayer.getInstance(getClassNameConfig(), getParamsConfig());
			
			Certificate certificate =  cryptoLayer.getCertFromStringBase64(base64certificate);			
			
			return cryptoLayer.checkStringContentSignature(document, sign, certificate);

		} catch (Exception ex) {
			throw new CryptoServiceException(ex);
		}
	}

	public byte[] getByteArrayDigest(byte[] byteArray) throws CryptoServiceException {
		try {
			
			CryptoLayer cryptoLayer = CryptoLayer.getInstance(getClassNameConfig(), getParamsConfig());
						
			return cryptoLayer.getByteArrayDigest(byteArray);

		} catch (Exception ex) {
			throw new CryptoServiceException(ex);
		}
	}
	
	
	
	private String getClassNameConfig() throws IOException{
		if (configProp == null){
			loadConfig();
		}
		return configProp.getProperty(SERVER_CRYPTO_LAYER);
	}
	
	private String getParamsConfig() throws IOException{
		if (configProp == null){
			loadConfig();
		}
		return configProp.getProperty(SERVER_CRYPTO_LAYER_PARAMS);
	}
	
	private void loadConfig() throws IOException{
		  //FileInputStream stream = new FileInputStream("cryptoLayer.properties");
		InputStream stream = getClass().getClassLoader().getResourceAsStream("cryptoLayer.properties");
		configProp = new Properties();
           configProp.load(stream); 
           stream.close();
	}
	
	private void loadCheckingConfig() throws IOException{
		InputStream stream = getClass().getClassLoader().getResourceAsStream("certChecking.properties");
		checkingConfigProp = new Properties();
		checkingConfigProp.load(stream); 
		stream.close();
	}
	
	public WsPkcs7Result verifyPKCS7(byte[] pkcs7, byte[] data) throws CryptoServiceException{
		try {
			CryptoLayer cryptoLayer = CryptoLayer.getInstance(getClassNameConfig(), getParamsConfig());
						
			PKCS7Result result = cryptoLayer.verifyPKCS7(pkcs7, data);
			
			WsPkcs7Result wsResult = new WsPkcs7Result();
			wsResult.setValid(result.isValid());
			wsResult.setCert(result.getCert().getEncoded());
			wsResult.setDate(result.getTime());
			return wsResult; 

		} catch (Exception ex) {
			throw new CryptoServiceException(ex);
		}
	}
	
	public boolean checkCertificate(byte[] certBytes) throws CryptoServiceException{
		boolean result = false;
		try{
			final CertificateFactory cf = CertificateFactory
					.getInstance("X509");
			ByteArrayInputStream bais = new ByteArrayInputStream(certBytes);
		    X509Certificate cert509 = (X509Certificate) cf.generateCertificate(bais);
			CryptoLayer cryptoLayer = CryptoLayer.getInstance(getClassNameConfig(), getParamsConfig());
			loadCheckingConfig();
			boolean crlCheking = Boolean.parseBoolean(checkingConfigProp.getProperty(CERTIFICATE_CRL_CHECK_ENABLE));
			String crlUrl = checkingConfigProp.getProperty(CERTIFICATE_CRL_URL);
			String issuerCertUrl = checkingConfigProp.getProperty(CERTIFICATE_ISSIER_CERT_URL);
			boolean ocspCheking = Boolean.parseBoolean(checkingConfigProp.getProperty(CERTIFICATE_OCSP_CHECK_ENABLE));
			if(!crlCheking && !ocspCheking)
				return true;
			if(crlCheking){
				result = new CertValidatorCRLJCP(cryptoLayer,crlUrl,issuerCertUrl).validate(cert509).isGood();
				if(!result)
					return result;
			}
			if(ocspCheking){
				result = new CertValidatorOCSPJCP(cryptoLayer).validate(cert509).isGood();
				if(!result)
					return result;
			}
			return result;
		} catch (IOException ex) {
			return true;
		} catch (Exception e) {
			throw new CryptoServiceException(e);
		}
	}
}
