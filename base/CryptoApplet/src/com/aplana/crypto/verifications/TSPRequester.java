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

import java.io.*;
import java.net.*;
import java.security.Signature;
import java.security.cert.Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.ArrayUtils;

import ru.CryptoPro.JCP.ASN.CPPKIXCMP.PKIFreeText;
import ru.CryptoPro.JCP.ASN.CryptographicMessageSyntax.*;
import ru.CryptoPro.JCP.ASN.PKIX1Explicit88.AlgorithmIdentifier;
import ru.CryptoPro.JCP.ASN.PKIXCMP.*;
import ru.CryptoPro.JCP.ASN.PKIXTSP.*;
import com.objsys.asn1j.runtime.*;

import com.aplana.crypto.Base64;


public class TSPRequester {

	public static final int[] JCP_GOST_DIGEST_OID = new int[]{1, 2, 643, 2, 2, 9};
	
	public static String getTimeStamp(byte[] hash, String URL){
		
		byte[] reqBytes = createRequest(hash);
		if (reqBytes == null) {
			return null;
		}
		byte[] resBytes = sendRequest(reqBytes, URL);
		if (resBytes == null) {
			return null;
		}
		return Base64.byteArrayToBase64(resBytes) ;
	}
	
	static public String getErrorFromTS(String tstamp) {
		StringBuffer res = new StringBuffer();
		byte[] resBytes = Base64.base64ToByteArray(tstamp);
		TimeStampResp response = new TimeStampResp();
	
		try {
			response.decode(new Asn1DerDecodeBuffer(resBytes));
			PKIStatusInfo statusinfo = response.status;
			PKIStatus status = statusinfo.status;			
			
			long statuscode = status.value;
			if (statuscode > 1) { 		
				PKIFailureInfo failinfo = statusinfo.failInfo;
				PKIFreeText freetext = statusinfo.statusString;
				res.append("PKIStatus = " + statuscode);
				res.append(" PKIFailureInfo = " + failinfo);
				res.append(" PKIFreeText = " + freetext);
	
			} else { 
				res.append("0");
			}
		} catch (Asn1Exception e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return res.toString();
	}

	static public String getDTFromTS(String tstamp) {
		StringBuffer res = new StringBuffer();
		TSTInfo info = getTSTInfofromTS(tstamp);
		if (info != null) {
			Asn1GeneralizedTime time = info.genTime; //UTC time (Coordinated Universal Time)
			try {
				Date ts = time.getTime().getTime();
				DateFormat dt = new SimpleDateFormat("dd.MM.yyyy HH.mm.ss");
				res.append(dt.format(ts));
			} catch (Asn1Exception e) {
				e.printStackTrace();
			}
		}
		return res.toString();
	}
	
	static public String getSNFromTS(String tstamp) {
		TSTInfo info = getTSTInfofromTS(tstamp);
		if (info != null) {
			return info.serialNumber.value.toString();
		} else {
			return "";
		}
		
	}
	
	static public boolean checkSignatureCMS(SignedData cms, Certificate cert) {
	       
	       boolean res = false;

	        try {
	        	Signature sig = Signature.getInstance("CryptoProSignature");
	            sig.initVerify(cert.getPublicKey());
	           
	            SignerInfo info = cms.signerInfos.elements[0];
	           
	            final Asn1BerEncodeBuffer encBufSignedAttr = new Asn1BerEncodeBuffer();
	            info.signedAttrs.encode(encBufSignedAttr);
	            byte[] data = encBufSignedAttr.getMsgCopy();
	           
	            sig.update(data);

	            SignatureValue signature = cms.signerInfos.elements[0].signature;
	            byte[] signBytes = signature.value;
	            ArrayUtils.reverse(signBytes);
	            res = sig.verify(signBytes);

	        } catch (Exception e) {
	            e.printStackTrace();
	        }

	        return res;
	}
	
	
	static private byte[] createRequest(byte[] hash){
		byte[] reqBytes = null;

		TimeStampReq_version reqVersion = new TimeStampReq_version("1");
		AlgorithmIdentifier hashAlgorithm = new AlgorithmIdentifier(JCP_GOST_DIGEST_OID);
		MessageImprint hashedMessage = new MessageImprint(hashAlgorithm, hash);
		TimeStampReq request = new TimeStampReq(reqVersion, hashedMessage);

		try {
			Asn1DerEncodeBuffer encData = new Asn1DerEncodeBuffer();
			request.encode(encData);
			reqBytes = encData.getMsgCopy();
		} catch (Asn1Exception e) {
			e.printStackTrace();
		}
		return reqBytes;
	}
	

	static private byte[] sendRequest(byte[] request, String URLTSA) {
		byte[] respBytes = null; 
		
		try {
			URL url = new URL(URLTSA);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setUseCaches(false);
			con.setDefaultUseCaches(false);
			con.setDoOutput(true);
		    con.setDoInput(true);
		    con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/timestamp-query");
			con.setRequestProperty("Content-length", String.valueOf(request.length));
			
			OutputStream out = null;
			InputStream is = null;
			
			try {
				out = con.getOutputStream();
				out.write(request);
			} catch (IOException e) {
				e.printStackTrace();
			} finally  {
				if (out != null)
					out.close();
			}
			
			 if (con.getResponseCode() != HttpURLConnection.HTTP_OK) {
				System.out.println(con.getResponseMessage());
			 } else {
					try {
						is = con.getInputStream();
						byte[] buf = new byte[1024];
						int len;
						ByteArrayOutputStream baout = new ByteArrayOutputStream();
						while ((len = is.read(buf)) > 0) {
							baout.write(buf, 0, len);
						}
						respBytes = baout.toByteArray();
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
						if (is != null)
							is.close();
					}
			 }
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (ProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return respBytes;
	}
	

	static private TSTInfo getTSTInfofromTS(String tstamp){
		TSTInfo tstInfo = null;
		byte[] resBytes = Base64.base64ToByteArray(tstamp);
		TimeStampResp response = new TimeStampResp();
		
		try {
			response.decode(new Asn1DerDecodeBuffer(resBytes));
	
			TimeStampToken tsToken = response.timeStampToken;
			Asn1DerEncodeBuffer encData = new Asn1DerEncodeBuffer();
			tsToken.encode(encData);
			byte[] contbytes = encData.getMsgCopy();
			
			ContentInfo content = new ContentInfo(); 
			content.decode(new Asn1DerDecodeBuffer(contbytes));
			SignedData cms = (SignedData) content.content;
	
			EncapsulatedContentInfo contInfo = cms.encapContentInfo;

			Asn1OctetString eContent = contInfo.eContent;
			tstInfo = new TSTInfo();
			Asn1BerDecodeBuffer tstData = new Asn1BerDecodeBuffer(eContent.value);
			tstInfo.decode(tstData);
			

		} catch (Asn1Exception e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return tstInfo;
	}

}
