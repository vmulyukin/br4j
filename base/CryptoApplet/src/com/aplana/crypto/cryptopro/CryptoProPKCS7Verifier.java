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
/**
 * $RCSfile: CMSVerify.java,v $
 * version $Revision: 1.11 $
 * created 16.08.2007 11:28:24 by kunina
 * last modified $Date: 2009/04/24 11:12:13 $ by $Author: kunina $
 * (C) ��� ������-��� 2004-2007.
 *
 * ����������� ���, ������������ � ���� �����, ������������
 * ��� ����� ��������. ����� ���� ���������� ��� ������������� 
 * ��� ������� ���������� ������� � ��������� ��������� � ����.
 *
 * ������ ��� �� ����� ���� ��������������� �����������
 * ��� ������ ����������. �������� ������-��� �� ����� �������
 * ��������������� �� ���������������� ����� ����.
 */
package com.aplana.crypto.cryptopro;

import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.ASN.CryptographicMessageSyntax.ContentInfo;
import ru.CryptoPro.JCP.ASN.CryptographicMessageSyntax.DigestAlgorithmIdentifier;
import ru.CryptoPro.JCP.ASN.CryptographicMessageSyntax.SignedData;
import ru.CryptoPro.JCP.ASN.CryptographicMessageSyntax.SignerInfo;
import ru.CryptoPro.JCP.ASN.PKIX1Explicit88.Attribute;
import ru.CryptoPro.JCP.ASN.PKIX1Explicit88.Time;
import ru.CryptoPro.JCP.params.OID;
import ru.CryptoPro.JCP.tools.Array;

import com.aplana.crypto.PKCS7SignerInfo;
import com.aplana.crypto.PKCS7Verifier;
import com.aplana.crypto.tsp.TSPService;
import com.objsys.asn1j.runtime.Asn1BerDecodeBuffer;
import com.objsys.asn1j.runtime.Asn1BerEncodeBuffer;
import com.objsys.asn1j.runtime.Asn1ObjectIdentifier;
import com.objsys.asn1j.runtime.Asn1OctetString;
import com.objsys.asn1j.runtime.Asn1Type;
import com.objsys.asn1j.runtime.Asn1UTCTime;

/**
 * CMS Verify (����� ������������: 1)CMS, 2)�������� �����������, 3)store(?))
 * [�������� ������������ �������� � �������� � signedAttributes]
 * <p/>
 * ���������:
 * <p/>
 * CMS_samples.CMSSign
 * <p/>
 * csptest -lowsign -in data.txt -my key -sign -out data_low.sgn -add
 * <p/>
 * csptest -lowsign -in data.txt -my key -sign -out data_low.sgn (��� ����������
 * �����������)
 * <p/>
 * csptest -sfsign -in data.txt -my key -sign -out data_sf.sgn -add
 * <p/>
 * csptest -sfsign -in data.txt -my key -sign -out data_sf.sgn (��� ����������
 * �����������)
 * 
 * @author Copyright 2004-2009 Crypto-Pro. All rights reserved.
 * @.Version
 */
public class CryptoProPKCS7Verifier implements PKCS7Verifier {
	// CMS.java
	private static final String CMS_FILE = "cms_data_sgn";
	// CMSSign.java
	// private static final String CMS_FILE = "cms_data_low_sgn";
	// private static final String CMS_FILE = "cms_data_sf_sgn";

	private static final String CMS_FILE_PATH = CMStools.TEST_PATH
			+ CMStools.SEPAR + CMS_FILE + CMStools.CMS_EXT;

	private static StringBuffer out = new StringBuffer("");
	private static StringBuffer out1 = new StringBuffer("");
	private static int validsign;

	/**/
	public CryptoProPKCS7Verifier(){
	}

	/**
	 * �������� CMS
	 * 
	 * @param buffer
	 *            �����
	 * @param certs
	 *            �����������
	 * @param data
	 *            ������
	 * @throws Exception
	 *             e
	 */
	public PKCS7SignerInfo[] verify(byte[] pkcs7, Certificate[] certs, byte[] data)
			throws Exception {
		// clear buffers fo logs
		out = new StringBuffer("");
		out1 = new StringBuffer("");
		
		List<PKCS7SignerInfo> result = new ArrayList<PKCS7SignerInfo>();
		
		final Asn1BerDecodeBuffer asnBuf = new Asn1BerDecodeBuffer(pkcs7);
		final ContentInfo all = new ContentInfo();
		all.decode(asnBuf);
		if (!new OID(CMStools.STR_CMS_OID_SIGNED).eq(all.contentType.value))
			throw new Exception("Not supported");
		final SignedData cms = (SignedData) all.content;
		final byte[] text;
		if (cms.encapContentInfo.eContent != null)
			text = cms.encapContentInfo.eContent.value;
		else if (data != null)
			text = data;
		else
			throw new Exception("No content for verify");
		OID digestOid = null;
		final DigestAlgorithmIdentifier digestAlgorithmIdentifier = new DigestAlgorithmIdentifier(
				new OID(CMStools.DIGEST_OID).value);
		for (int i = 0; i < cms.digestAlgorithms.elements.length; i++) {
			if (cms.digestAlgorithms.elements[i].algorithm
					.equals(digestAlgorithmIdentifier.algorithm)) {
				digestOid = new OID(
						cms.digestAlgorithms.elements[i].algorithm.value);
				break;
			}
		}
		if (digestOid == null)
			throw new Exception("Unknown digest");
		final OID eContTypeOID = new OID(
				cms.encapContentInfo.eContentType.value);
		if (cms.certificates != null) {
			// �������� �� ��������� ������������
			CMStools.logger.info("Validation on certificates founded in CMS.");
			for (int i = 0; i < cms.certificates.elements.length; i++) {
				final Asn1BerEncodeBuffer encBuf = new Asn1BerEncodeBuffer();
				cms.certificates.elements[i].encode(encBuf);

				final CertificateFactory cf = CertificateFactory
						.getInstance("X.509");
				final X509Certificate cert = (X509Certificate) cf
						.generateCertificate(encBuf.getInputStream());

				for (int j = 0; j < cms.signerInfos.elements.length; j++) {
					final SignerInfo info = cms.signerInfos.elements[j];
					if (!digestOid.equals(new OID(
							info.digestAlgorithm.algorithm.value)))
						throw new Exception("Not signed on certificate.");
					boolean checkResult = false;
					try{
						checkResult = verifyOnCert(cert,
							cms.signerInfos.elements[j], text, eContTypeOID);
					}catch(Exception ex){
						//���������� ������, ������� �� �������
						ex.printStackTrace();
					}
					
					Date time = null;
					
					//TODO ��� ������������ ���������� ������������ ���� ����� ���������
					time = TSPService.getSignTime(pkcs7, j);
					
					CMStools.logger.info("Time from time stamp: " + time);
					
					if(time == null){
						final Attribute[] signAttrElem = info.signedAttrs.elements;
	
				        //��������� �������� "����� �������" "1 2 840 113549 1 9 5"
				        final Asn1ObjectIdentifier contentTypeOid = new Asn1ObjectIdentifier(
				                (new OID(CMStools.STR_CMS_OID_SIGN_TYM_ATTR)).value);
	
				        for (int r = 0; r < signAttrElem.length; r++) {
				            final Asn1ObjectIdentifier oid = signAttrElem[r].type;
				            if (oid.equals(contentTypeOid)) {
				            	Attribute timeAttr = signAttrElem[r];
				            	if (timeAttr.values.elements[0] != null){
				            		Time cpTime = (Time)timeAttr.values.elements[0];
				            		time = ((Asn1UTCTime)cpTime.getElement()).getTime().getTime();
				            		CMStools.logger.info("Time from time attribute: " + time);
				            	}
				            }
				        }			
					}
			        
					
					result.add(getSignerInfo(checkResult, j, i, cert, time));
				}
			}
		} else if (certs != null) {
			// �������� �� ��������� ������������
			CMStools.logger
					.info("Certificates for validation not found in CMS.\n"
							+ "      Try verify on specified certificates...");
			for (int i = 0; i < certs.length; i++) {
				final X509Certificate cert = (X509Certificate) certs[i];
				for (int j = 0; j < cms.signerInfos.elements.length; j++) {
					final SignerInfo info = cms.signerInfos.elements[j];
					if (!digestOid.equals(new OID(
							info.digestAlgorithm.algorithm.value)))
						throw new Exception("Not signed on certificate.");
					final boolean checkResult = verifyOnCert(cert,
							cms.signerInfos.elements[j], text, eContTypeOID);
					// TODO �������� �������� �����
					Date time = new Date();
					result.add(getSignerInfo(checkResult, j, i, cert, time));
				}
			}
		} else {
			CMStools.logger.warning("Certificates for validation not found");
		}
		
		return result.toArray(new PKCS7SignerInfo[result.size()]);
	}

	/**
	 * ������� �������� ������� �� ��������� �����������
	 * 
	 * @param cert
	 *            ���������� ��� ��������
	 * @param text
	 *            ����� ��� ��������
	 * @param info
	 *            �������
	 * @return ����� �� �������
	 * @throws Exception
	 *             ������
	 */
	private boolean verifyOnCert(X509Certificate cert, SignerInfo info,
			byte[] text, OID eContentTypeOID) throws Exception {
		// �������
		final byte[] sign = info.signature.value;
		// ������ ��� �������� �������
		final byte[] data;
		if (info.signedAttrs == null) {
			// ��������� ������� �� ������������
			// ������ ��� �������� �������
			data = text;
		} else {
			// ������������ ��������� ������� (SignedAttr)
			final Attribute[] signAttrElem = info.signedAttrs.elements;

			// �������� ��������� content-type
			final Asn1ObjectIdentifier contentTypeOid = new Asn1ObjectIdentifier(
					(new OID(CMStools.STR_CMS_OID_CONT_TYP_ATTR)).value);
			Attribute contentTypeAttr = null;

			for (int r = 0; r < signAttrElem.length; r++) {
				final Asn1ObjectIdentifier oid = signAttrElem[r].type;
				if (oid.equals(contentTypeOid)) {
					contentTypeAttr = signAttrElem[r];
				}
			}

			if (contentTypeAttr == null)
				throw new Exception("content-type attribute not present");

			if (!contentTypeAttr.values.elements[0]
					.equals(new Asn1ObjectIdentifier(eContentTypeOID.value)))
				throw new Exception(
						"content-type attribute OID not equal eContentType OID");

			// �������� ��������� message-digest
			final Asn1ObjectIdentifier messageDigestOid = new Asn1ObjectIdentifier(
					(new OID(CMStools.STR_CMS_OID_DIGEST_ATTR)).value);

			Attribute messageDigestAttr = null;

			for (int r = 0; r < signAttrElem.length; r++) {
				final Asn1ObjectIdentifier oid = signAttrElem[r].type;
				if (oid.equals(messageDigestOid)) {
					messageDigestAttr = signAttrElem[r];
				}
			}

			if (messageDigestAttr == null)
				throw new Exception("message-digest attribute not present");

			final Asn1Type open = messageDigestAttr.values.elements[0];
			final Asn1OctetString hash = (Asn1OctetString) open;
			final byte[] md = hash.value;

			// ���������� messageDigest
			final byte[] dm = CMStools.digestm(text, CMStools.DIGEST_ALG_NAME);

			if (!Array.toHexString(dm).equals(Array.toHexString(md)))
				throw new Exception("message-digest attribute verify failed");

			// �������� ��������� signing-time
			final Asn1ObjectIdentifier signTimeOid = new Asn1ObjectIdentifier(
					(new OID(CMStools.STR_CMS_OID_SIGN_TYM_ATTR)).value);

			Attribute signTimeAttr = null;

			for (int r = 0; r < signAttrElem.length; r++) {
				final Asn1ObjectIdentifier oid = signAttrElem[r].type;
				if (oid.equals(messageDigestOid)) {
					signTimeAttr = signAttrElem[r];
				}
			}

			if (signTimeAttr != null) {
				// �������� (�������������)
			}

			// ������ ��� �������� �������
			final Asn1BerEncodeBuffer encBufSignedAttr = new Asn1BerEncodeBuffer();
			info.signedAttrs.encode(encBufSignedAttr);
			data = encBufSignedAttr.getMsgCopy();
		}
		// �������� �������
		final Signature signature = Signature
				.getInstance(JCP.GOST_EL_SIGN_NAME);
		signature.initVerify(cert);
		signature.update(data);
		return signature.verify(sign);
	}

	/**
	 * write log
	 * 
	 * @param checkResult
	 *            ������ �� ��������
	 * @param signNum
	 *            ����� �������
	 * @param certNum
	 *            ����� �����������
	 * @param cert
	 *            ����������
	 */
	private PKCS7SignerInfo getSignerInfo(boolean checkResult, int signNum, int certNum,
			X509Certificate cert, Date time) {
		PKCS7SignerInfo result = new PKCS7SignerInfo();
		result.setCertificate(cert);
		result.setValid(checkResult);
		result.setTime(time);
		
		return result;
	}
}
