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

import java.io.FileInputStream;
import java.security.AccessController;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PrivilegedAction;
import java.security.Provider;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import ru.CryptoPro.JCP.tools.Array;

public class JCPCryptoLayer extends CryptoLayer {
	public static final String KEYSTORE_TYPE = "KEYSTORE_TYPE";
	public static final String SIGN_ALGORITHM = "SIGN_ALGORITHM";
	public static final String HASH_ALGORITHM = "HASH_ALGORITHM";

	protected List signByteArray(String ksName, String keyAlias,
			List<byte[]> inData, List<byte[]> inHash, String password) {
		List retVal = new Vector(2);
		String err = null;
		Enumeration aliases = null;
		PrivateKey key = null;
		final char[] passwordChr = (password.length() > 0 ? password
				.toCharArray() : null);
		final String storePath = ksName;

		try {
			if (checkProvider()) {
				// System.out.println("KeyStore Path is: "+ storePath);
				// System.out.println("KeyStore Pass is: "+ new
				// String(passwordChr));

				// Provider pjce = new ru.lissi.provider.LirJCE();
				// Provider pjsse = new
				// ru.lissi.net.ssl.internal.ssl.Provider();

				// final KeyStore ks = KeyStore.getInstance(KeyStoreName,
				// pjsse);

				final KeyStore ks = KeyStore
						.getInstance(getLayerParam(KEYSTORE_TYPE));

				Boolean result = AccessController
						.doPrivileged(new PrivilegedAction<Boolean>() {
							public Boolean run() {
								try {
									FileInputStream keyStoreStream = null;
									if (!isHideContainerField()) {
										keyStoreStream = new FileInputStream(
												storePath);
									}
									ks.load(keyStoreStream, passwordChr);
									return true;
								} catch (Exception e) {
									e.printStackTrace();
									return false;
								}
							}
						});

				if (result) {
					// String keyAliasSign = keyAlias.equals("1")?"2":keyAlias;
					String keyAliasSign = getKeyAlias(ks);
					key = (PrivateKey) ks.getKey(keyAliasSign, passwordChr);

					System.out.println("Private key loaded ("
							+ key.getAlgorithm() + ")");

					List<byte[][]> signatures = new ArrayList<byte[][]>();

					try {
						for (int i = 0; i < inData.size(); i++) {
							Signature sign = Signature
									.getInstance(getLayerParam(SIGN_ALGORITHM));
							sign.initSign(key);
							sign.update(inHash.get(i));
							byte[][] signature = {sign.sign(), new byte[]{}};
							signatures.add(signature);
						}

					} catch (NoSuchAlgorithmException e) {
						err = e.toString();
					} catch (SignatureException e) {
						err = e.toString();
					} catch (InvalidKeyException e) {
						err = e.toString();
					} catch (ExceptionInInitializerError e){
						err = e.getCause().getMessage();
					}
					retVal.add(0, err);
					retVal.add(1, signatures);
				}
			} else {
				System.out.println("JCP isn't installed!");
			}

		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			return retVal;
		}
	}

	public List verifyByteArraySignature(byte[] inData, byte[] inSignature,
			Certificate inCert) {
		List retVal = new Vector(2);
		String err = null;
		boolean ver = false;

		try {
			try {
				final Signature sig = Signature
						.getInstance(getLayerParam(SIGN_ALGORITHM));
				sig.initVerify(inCert.getPublicKey());
				sig.update(inData);
				ver = sig.verify(inSignature);
			} catch (Throwable e) {
				err = e.toString();
			}
			retVal.add(0, err);
			retVal.add(1, String.valueOf(ver));

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return retVal;
		}
	}

	public String getCertDigestString(Certificate cert){
		String retVal = null;
		try{
			X509Certificate inCert = (X509Certificate) cert;
			MessageDigest md = MessageDigest
					.getInstance(getLayerParam(HASH_ALGORITHM));
			byte[] der = inCert.getEncoded();
			md.update(der);
			byte[] digest = md.digest();
			retVal = Base64.byteArrayToBase64(digest);
		} catch (Throwable e){
			e.printStackTrace();
		} 
		return retVal;
	}

	public String getCertificateHash(String keyStorePath, String keyAlias,
			String password) {
		String retVal = null;
		String err = null;
		Key key = null;
		byte[] signature = null;
		Certificate cert = null;
		final char[] passwordChr = (password.length() > 0 ? password
				.toCharArray() : null);
		final String storePath = keyStorePath;
		try {
			if (checkProvider()) {
				System.out.println("Getting Certificate hash...");
				// Provider pjsse = new
				// ru.lissi.net.ssl.internal.ssl.Provider();
				// Security.insertProviderAt(pjsse, 3);

				final KeyStore ks = KeyStore
						.getInstance(getLayerParam(KEYSTORE_TYPE));

				Boolean result = AccessController
						.doPrivileged(new PrivilegedAction<Boolean>() {
							public Boolean run() {
								try {
									FileInputStream keyStoreStream = null;
									if (!isHideContainerField()) {
										keyStoreStream = new FileInputStream(
												storePath);
									}
									ks.load(keyStoreStream, passwordChr);
									return true;
								} catch (Exception e) {
									e.printStackTrace();
									return false;
								}
							}
						});

				cert = ks.getCertificate(getKeyAlias(ks));
				retVal = getCertDigestString(cert);

			} else {
				System.out.println("JCP doesn't work!");
			}

		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			return retVal;
		}
	}

	public byte[] getByteArrayDigest(byte[] inByteArray) {
		byte[] retVal = null;

		try {
			MessageDigest digest = MessageDigest
					.getInstance(getLayerParam(HASH_ALGORITHM));
			digest.update(inByteArray);
			retVal = digest.digest();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		return retVal;
	}

	@Override
	protected boolean checkProvider() {
		boolean ret;
		try {
			Signature.getInstance(getLayerParam(SIGN_ALGORITHM));
			ret = true;
		} catch (Throwable e) {
			e.printStackTrace();
			ret = false;
		}
		return ret;
	}
	
	/**
	 * ������������ ������ ����������� ������
	 * @return ������ �������� ����������� ������ (null � ������ ������)
	 */
	@Override
	public List<String> getKeyStoreList() {
		List<String> stores = new ArrayList<String>(); 

		KeyStore ks = null;
		try {
			ks = KeyStore.getInstance(getLayerParam(KEYSTORE_TYPE));
			if (ks != null) {
				try {
					ks.load(null, null);
					Enumeration<String> aliases = ks.aliases();
					while (aliases.hasMoreElements()) {
						String alias = (String) aliases.nextElement();
						stores.add(alias);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (KeyStoreException e) {
			System.err.println(e.getStackTrace()[0] + " - " + e.getMessage());
		}	
		
		if (stores.size() == 0) {
			System.out.println("���������� ������ �� �������.");
			return null;
		} else {
			return stores;
		}
	}
}
