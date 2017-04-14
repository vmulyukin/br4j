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

import java.security.AccessController;
import java.security.NoSuchAlgorithmException;
import java.security.PrivilegedAction;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import com.aplana.crypto.provider.CriptoAPISignature;
import com.aplana.crypto.tsp.TSPService;

public class WindowsCryproAPILayer extends CryptoLayer{
	public static final String PROVIDER_TYPE = "PROVIDER_TYPE";
	public static final String ALGORITHM = "ALGORITHM";

	protected List signByteArray(final String ksName, final String keyAlias,
			final List<byte[]> inData, final List<byte[]> inHash, final String password) {
		return AccessController.doPrivileged(new PrivilegedAction<List>() {
			public List run() {
				List retVal = new Vector(2);
				String err = null;
				CriptoAPISignature criptoAPISignature = null;
				try {
					retVal.add(0, null);
					List<byte[][]> containers = new ArrayList<byte[][]>();
					retVal.add(1, containers);
					
					criptoAPISignature = new CriptoAPISignature(getIntLayaerParam(PROVIDER_TYPE), getIntLayaerParam(ALGORITHM), true);
					
					for (int i=0; i<inHash.size(); i++){
						byte[][] signatures = new byte[2][];
						criptoAPISignature.clear();
						criptoAPISignature.update(inHash.get(i));
						signatures[0] = criptoAPISignature.sign(ksName);
		
						if (inData != null){
							criptoAPISignature.clear();
							criptoAPISignature.update(inData.get(i));
							signatures[1] = criptoAPISignature.signAndEncodeMessage(ksName);
							String timestampAddress = getLayerParam("TIMESTAMP_SERVER");
							if(timestampAddress != null){
								TSPService service = new TSPService(timestampAddress);
								signatures[1] = service.injectTimeStamp(signatures[1]);
							}
						}
						
						containers.add(signatures);
					}
					
				} catch (Throwable e) {
					e.printStackTrace();
					retVal.add(0, e.getMessage());
				}finally{
					try {
						criptoAPISignature.release();
					} catch (SignatureException e) {
						e.printStackTrace();
					}
				}
				return retVal;
			}
		});
	}
	public List verifyByteArraySignature(final byte[] inData,	final byte[] inSignature, final Certificate inCert) {
		return AccessController.doPrivileged(new PrivilegedAction<List>() {
			public List run() {
				List retVal = new Vector(2);
				String err = null;
				boolean verResult = false;
				try {
					try {
						CriptoAPISignature criptoAPISignature = new CriptoAPISignature(getIntLayaerParam(PROVIDER_TYPE), getIntLayaerParam(ALGORITHM), false);
						criptoAPISignature.update(inData);
						verResult = criptoAPISignature.verify(inSignature, inCert.getPublicKey());
					} catch (Exception e) {
						err = e.toString();
					}
					retVal.add(0, err);
					retVal.add(1, String.valueOf(verResult));
		
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					return retVal;
				}
			}
		});
	}

	@Override
	public String getCertDigestString(final Certificate cert) throws NoSuchAlgorithmException, CertificateEncodingException {
		return AccessController.doPrivileged(new PrivilegedAction<String>() {
			public String run() {
				try {
					CriptoAPISignature criptoAPISignature = new CriptoAPISignature(getIntLayaerParam(PROVIDER_TYPE), getIntLayaerParam(ALGORITHM), false);
					criptoAPISignature.update(cert.getEncoded());
					byte[] hash = criptoAPISignature.hash();
					return Base64.byteArrayToBase64(hash);
				} catch (Throwable e) {
					e.printStackTrace();
					return null;
				}
			}
		});
	}

	public String getCertificateHash(final String keyStorePath, String keyAlias, String password) {
		return AccessController.doPrivileged(new PrivilegedAction<String>() {
			public String run() {
				try {
					CriptoAPISignature criptoAPISignature = new CriptoAPISignature(getIntLayaerParam(PROVIDER_TYPE), getIntLayaerParam(ALGORITHM), false);
					Certificate cert = criptoAPISignature.getCertificate(keyStorePath);
					return getCertDigestString(cert);
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			}
		});
	}

	public byte[] getByteArrayDigest(final byte[] inByteArray) {
			return AccessController.doPrivileged(new PrivilegedAction<byte[]>() {
				public byte[] run() {
					try {
						CriptoAPISignature criptoAPISignature = new CriptoAPISignature(getIntLayaerParam(PROVIDER_TYPE), getIntLayaerParam(ALGORITHM), false);
						criptoAPISignature.update(inByteArray);
						return criptoAPISignature.hash();
					} catch (Throwable e) {
						e.printStackTrace();
						return null;
					}
					
				}
			});
	}

	@Override
	protected boolean checkProvider() {
		return AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
			public Boolean run() {
				CriptoAPISignature criptoAPISignature = new CriptoAPISignature(getIntLayaerParam(PROVIDER_TYPE), getIntLayaerParam(ALGORITHM), false);
				return criptoAPISignature.checkProvider();
			}
		});
	}

	@Override
	public boolean isHidePasswordField() {
		return true;
	}
	
	@Override
	public List<String> getKeyStoreList() {
		return AccessController.doPrivileged(new PrivilegedAction<List<String>>() {
			public List<String> run() {
				CriptoAPISignature criptoAPISignature = new CriptoAPISignature(getIntLayaerParam(PROVIDER_TYPE), getIntLayaerParam(ALGORITHM), false);
				return criptoAPISignature.getKeyStoreListCSP();
			}
		});

	}
}
