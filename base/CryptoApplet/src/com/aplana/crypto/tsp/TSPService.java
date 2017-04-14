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
package com.aplana.crypto.tsp;

import java.io.IOException;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Random;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.cms.ContentInfo;
import org.bouncycastle.asn1.pkcs.Attribute;
import org.bouncycastle.asn1.tsp.TSTInfo;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.tsp.TSPAlgorithms;
import org.bouncycastle.tsp.TSPException;
import org.bouncycastle.tsp.TimeStampRequest;
import org.bouncycastle.tsp.TimeStampRequestGenerator;
import org.bouncycastle.tsp.TimeStampResponse;
import org.bouncycastle.tsp.TimeStampToken;

public class TSPService {

	public static final ASN1ObjectIdentifier timestampTokenOID = new ASN1ObjectIdentifier("1.2.840.113549.1.9.16.2.14");
	private Collection<URL> urls;
	
	public TSPService(String... addresses) throws MalformedURLException{
		urls = new ArrayList<URL>();
		for(String address : addresses){
			urls.add(new URL(address));
		}
	}
	
	private TimeStampToken getTimeStamp(byte[] data){

		byte[] reqData = null;
		TimeStampRequest request = null;
		try{
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
			messageDigest.update(data);
			byte[] digest = messageDigest.digest();
			TimeStampRequestGenerator reqGen = new TimeStampRequestGenerator();
			reqGen.setCertReq(true);
			request = reqGen.generate(
				    TSPAlgorithms.SHA1, digest, BigInteger.valueOf(new Random().nextLong()));
			reqData = request.getEncoded();
		} catch(Exception e){e.printStackTrace();}
		for(URL url : urls){
			try{
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				connection.setRequestMethod("POST");
				connection.setDoOutput(true);
				connection.getOutputStream().write(reqData);
				TimeStampResponse response = new TimeStampResponse(connection.getInputStream());
				connection.disconnect();		
				response.validate(request);
				return response.getTimeStampToken();
			} catch(Exception e){
				System.out.println("Failed to reclaim timestamp from address " + url + ". Reason: " + e.getMessage());
				e.printStackTrace();
			}
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public byte[] injectTimeStamp(byte[] pkcs7) throws CMSException{
		
		CMSSignedData data;
		try{
			data = new CMSSignedData(pkcs7);
		} catch(CMSException e){
			System.out.println("Error during timestamp injection: pkcs7 is not valid.");
			e.printStackTrace();
			return pkcs7;
		}
		SignerInformationStore signerStore = data.getSignerInfos();
		Collection<SignerInformation> signers = (Collection<SignerInformation>) signerStore.getSigners();
		Collection<SignerInformation> updatedSigners = new ArrayList<SignerInformation>();
		for(SignerInformation signer : signers){
			TimeStampToken timestampToken = getTimeStamp(signer.getSignature());
			if(timestampToken == null) {
				System.out.println("Timestamp had not been received. No injection will be performed.");
				return pkcs7;
			}
			AttributeTable unsignedAttributes = signer.getUnsignedAttributes() != null ? signer.getUnsignedAttributes() : new AttributeTable(new ASN1EncodableVector());
			if(unsignedAttributes.getAll(timestampTokenOID).size() == 0){	
				try{
					unsignedAttributes = unsignedAttributes.add(timestampTokenOID, timestampToken.toCMSSignedData().toASN1Structure());	
					signer = SignerInformation.replaceUnsignedAttributes(signer, unsignedAttributes);
				} catch(Exception e){e.printStackTrace();}
				updatedSigners.add(signer);
				
			}
		}
		data = CMSSignedData.replaceSigners(data, new SignerInformationStore(updatedSigners));
		try{
			pkcs7 = data.getEncoded();
		} catch(Exception e){e.printStackTrace();}
		return pkcs7;
	}
	
	public static Date getSignTime(byte[] pkcs7, int signerNumber){
		CMSSignedData data;
		try{
			data = new CMSSignedData(pkcs7);
		} catch(CMSException e){
			System.out.println("Error during decoding: pkcs7 is not valid.");
			e.printStackTrace();
			return null;
		}
		SignerInformationStore signerStore = data.getSignerInfos();
		@SuppressWarnings("unchecked")
		ArrayList<SignerInformation> signers = (ArrayList<SignerInformation>) signerStore.getSigners();
		SignerInformation signer = signers.get(signerNumber);
		Date date = null;
			AttributeTable unsignedAttributes = signer.getUnsignedAttributes();
			if(unsignedAttributes != null && unsignedAttributes.size() > 0){
				ASN1EncodableVector stamps = unsignedAttributes.getAll(timestampTokenOID);
				if(stamps.size() > 0) {
					ContentInfo info = ContentInfo.getInstance(((org.bouncycastle.asn1.cms.Attribute)stamps.get(0)).getAttrValues().getObjectAt(0).toASN1Primitive());
					try {
						TimeStampToken timestampToken = new TimeStampToken(info);
						date = timestampToken.getTimeStampInfo().getGenTime();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		return date;
	}
}
