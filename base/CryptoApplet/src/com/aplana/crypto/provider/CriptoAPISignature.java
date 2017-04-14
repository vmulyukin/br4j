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
package com.aplana.crypto.provider;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.io.UnsupportedEncodingException;

import com.aplana.crypto.provider.Crypt32.CERT_CHAIN_CONTEXT;
import com.aplana.crypto.provider.Crypt32.CERT_CHAIN_ENGINE_CONFIG;
import com.aplana.crypto.provider.Crypt32.CERT_CHAIN_PARA;
import com.aplana.crypto.provider.Crypt32.CERT_ENHKEY_USAGE;
import com.aplana.crypto.provider.Crypt32.CERT_ID;
import com.aplana.crypto.provider.Crypt32.CERT_ID.CERT_ID_VALUE;
import com.aplana.crypto.provider.Crypt32.CERT_USAGE_MATCH;
import com.aplana.crypto.provider.Crypt32.CRYPTOAPI_BLOB;
import com.aplana.crypto.provider.Crypt32.CRYPT_VERIFY_MESSAGE_PARA;
import com.aplana.crypto.provider.Crypt32._CRYPT_SIGN_MESSAGE_PARA;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

public class CriptoAPISignature {

	private ByteArrayOutputStream data;
	private int provType;
	private int hashAlg;
	private boolean batchOperation;
	private boolean isInit;
	PointerByReference prov;

	public CriptoAPISignature(int provType, int hashAlg, boolean batchOperation) {
		this.data = new ByteArrayOutputStream();
		this.provType = provType;
		this.hashAlg = hashAlg;
		this.batchOperation = batchOperation;
		this.isInit = false;
		this.prov = new PointerByReference();
	}

	public void update(byte b) throws IOException {
		data.write(new byte[] { b });
	}

	public void update(byte[] b, int off, int len) {
		data.write(b, off, len);
	}

	public void update(byte[] b) throws IOException {
		data.write(b);
	}

	public boolean checkProvider() {
		// �������� ��������� ����������������
		boolean result = false;
		PointerByReference prov = new PointerByReference();
		result = Advapi32.INST.CryptAcquireContextA(prov, null, null, provType,
				Advapi32.CRYPT_VERIFYCONTEXT);
		Advapi32.INST.CryptReleaseContext(prov.getValue(), 0);
		return result;
	}

	public byte[] sign(String keyContainerName) throws SignatureException {
		PointerByReference hash = new PointerByReference();
		try {

			System.out.println("sign: �������� ��������� ����������������");
			if (!(isInit && batchOperation)) {
				if (!Advapi32.INST.CryptAcquireContextA(prov, keyContainerName,
						null, provType, 0)) {
					System.out.println(getLastErrorMessage());
					throw new SignatureException(getLastErrorMessage());
				}
				isInit = true;
			}
			
			System.out.println("�������� ������� HASH");
			if (!Advapi32.INST.CryptCreateHash(prov.getValue(), hashAlg, 0, 0,
					hash)) {
				System.out.println(getLastErrorMessage());
				throw new SignatureException(getLastErrorMessage());
			}
			
			System.out.println("���������� ������������������ ���� ������.");
			if (!Advapi32.INST.CryptHashData(hash.getValue(),
					data.toByteArray(), data.size(), 0)) {
				System.out.println(getLastErrorMessage());
				throw new SignatureException(getLastErrorMessage());
			}
			
			System.out.println("����������� ������� ������� � ������������� ������. AT_SIGNATURE");
			IntByReference size = new IntByReference();
			if (!Advapi32.INST.CryptSignHashA(hash.getValue(),
					Advapi32.AT_SIGNATURE, null, 0, null, size)) {
				System.out.println(getLastErrorMessage());
				System.out.println("����������� ������� ������� � ������������� ������. AT_KEYEXCHANGE");
				if (!Advapi32.INST.CryptSignHashA(hash.getValue(),
						Advapi32.AT_KEYEXCHANGE, null, 0, null, size)) {
					System.out.println(getLastErrorMessage());
					throw new SignatureException(getLastErrorMessage());
				}
			}

			// ������������� ������ ��� ����� �������.
			byte[] signature = new byte[size.getValue()];
			
			System.out.println("������� ������� ������� �����������. AT_SIGNATURE");
			if (!Advapi32.INST.CryptSignHashA(hash.getValue(),
					Advapi32.AT_SIGNATURE, null, 0, signature, size)) {
				System.out.println(getLastErrorMessage());
				System.out.println("������� ������� ������� �����������. AT_KEYEXCHANGE");
				if (!Advapi32.INST.CryptSignHashA(hash.getValue(),
						Advapi32.AT_KEYEXCHANGE, null, 0, signature, size)) {
					System.out.println(getLastErrorMessage());
					throw new SignatureException(getLastErrorMessage());
				}
			}

			return signature;
		} catch (SignatureException ex) {
			throw ex;
		} finally {
			if (!Advapi32.INST.CryptDestroyHash(hash.getValue())) {
				throw new SignatureException(getLastErrorMessage());
			}
			if (!batchOperation) {
				if (!Advapi32.INST.CryptReleaseContext(prov.getValue(), 0)) {
					throw new SignatureException(getLastErrorMessage());
				}
			}
			try {
				data.close();
			} catch (Exception ignoreEx) {
			}
		}
	}

	public byte[] hash() throws SignatureException {

		PointerByReference prov = new PointerByReference();
		PointerByReference hash = new PointerByReference();
		try {

			System.out.println("hash: �������� ��������� ����������������");
			if (!Advapi32.INST.CryptAcquireContextA(prov, null, null, provType,
					Advapi32.CRYPT_VERIFYCONTEXT)) {
				System.out.println(getLastErrorMessage());
				throw new SignatureException(getLastErrorMessage());
			}
			
			System.out.println("�������� ������� HASH");
			if (!Advapi32.INST.CryptCreateHash(prov.getValue(), hashAlg, 0, 0,
					hash)) {
				System.out.println(getLastErrorMessage());
				throw new SignatureException(getLastErrorMessage());
			}
			
			System.out.println("���������� ������������������ ���� ������.");
			if (!Advapi32.INST.CryptHashData(hash.getValue(),
					data.toByteArray(), data.size(), 0)) {
				System.out.println(getLastErrorMessage());
				throw new SignatureException(getLastErrorMessage());
			}

			IntByReference hashSize = new IntByReference();
			if (!Advapi32.INST.CryptGetHashParam(hash.getValue(),
					Advapi32.HP_HASHVAL, null, hashSize, 0)) {
				System.out.println(getLastErrorMessage());
				throw new SignatureException(getLastErrorMessage());
			}

			byte[] hashVal = new byte[hashSize.getValue()];
			if (!Advapi32.INST.CryptGetHashParam(hash.getValue(),
					Advapi32.HP_HASHVAL, hashVal, hashSize, 0)) {
				System.out.println(getLastErrorMessage());
				throw new SignatureException(getLastErrorMessage());
			}

			return hashVal;
		} catch (SignatureException ex) {
			throw ex;
		} finally {
			if (!Advapi32.INST.CryptDestroyHash(hash.getValue())) {
				throw new SignatureException(getLastErrorMessage());
			}
			if (!Advapi32.INST.CryptReleaseContext(prov.getValue(), 0)) {
				throw new SignatureException(getLastErrorMessage());
			}
			try {
				data.close();
			} catch (Exception ignoreEx) {
			}
		}
	}

	public boolean verify(byte[] sigBytes, PublicKey publicKey)
			throws SignatureException {
		PointerByReference hash = new PointerByReference();
		PointerByReference publicKeyPointer = new PointerByReference();
		try {
			// �������� ��������� ����������������
			if (!(isInit && batchOperation)) {
				if (!Advapi32.INST.CryptAcquireContextA(prov, null, null,
						provType, Advapi32.CRYPT_VERIFYCONTEXT)) {
					throw new SignatureException(getLastErrorMessage());
				}
			}

			// �������� ������� HASH
			if (!Advapi32.INST.CryptCreateHash(prov.getValue(), hashAlg, 0, 0,
					hash)) {
				throw new SignatureException(getLastErrorMessage());
			}

			// ���������� ������������������ ���� ������.
			if (!Advapi32.INST.CryptHashData(hash.getValue(),
					data.toByteArray(), data.size(), 0)) {
				throw new SignatureException(getLastErrorMessage());
			}

			// -------------------------------------
			// ������������� ���������� �����
			// -------------------------------------
			// ����������� ������� ��������� CERT_PUBLIC_KEY_INFO
			IntByReference keyInfoSize = new IntByReference();
			if (!Crypt32.INST.CryptDecodeObject(Advapi32.X509_ASN_ENCODING,
					Advapi32.X509_PUBLIC_KEY_INFO, publicKey.getEncoded(),
					publicKey.getEncoded().length, 0, null, keyInfoSize)) {
				throw new SignatureException(getLastErrorMessage());
			}

			// �������� ������ ��� ��������� CERT_PUBLIC_KEY_INFO
			byte[] keyInfo = new byte[keyInfoSize.getValue()];

			// ��������������� ������������� ���������� ����� �
			// CERT_PUBLIC_KEY_INFO
			if (!Crypt32.INST.CryptDecodeObject(Advapi32.X509_ASN_ENCODING,
					Advapi32.X509_PUBLIC_KEY_INFO, publicKey.getEncoded(),
					publicKey.getEncoded().length, 0, keyInfo, keyInfoSize)) {
				throw new SignatureException(getLastErrorMessage());
			}

			// ������ ���������� ����� � �������� ������ ����������
			if (!Crypt32.INST.CryptImportPublicKeyInfo(prov.getValue(),
					Advapi32.X509_ASN_ENCODING, keyInfo, publicKeyPointer)) {
				throw new SignatureException(getLastErrorMessage());
			}

			// �������� ���
			boolean result = false;
			if (Advapi32.INST.CryptVerifySignatureA(hash.getValue(), sigBytes,
					sigBytes.length, publicKeyPointer.getValue(), null, 0)) {
				result = true;
			} else {
				throw new SignatureException(getLastErrorMessage());
			}
			return result;

		} catch (SignatureException ex) {
			throw ex;
		} finally {
			if (!Advapi32.INST.CryptDestroyKey(publicKeyPointer.getValue())) {
				throw new SignatureException(getLastErrorMessage());
			}
			if (!Advapi32.INST.CryptDestroyHash(hash.getValue())) {
				throw new SignatureException(getLastErrorMessage());
			}
			if (!Advapi32.INST.CryptReleaseContext(prov.getValue(), 0)) {
				throw new SignatureException(getLastErrorMessage());
			}
			try {
				data.close();
			} catch (Exception ignoreEx) {
			}
		}
	}

	private String getLastErrorMessage() {
		int error = Kernel32.INST.GetLastError();
		char[] buf = new char[1024];
		int lenW = Kernel32.INST.FormatMessageW(
				Kernel32.FORMAT_MESSAGE_FROM_SYSTEM
						| Kernel32.FORMAT_MESSAGE_IGNORE_INSERTS, null, error,
				0, buf, buf.length, null);
		String message = new String(buf, 0, lenW);
		return "Error (" + Integer.toHexString(error) + ")" + " " + message;
	}

	public void clear() {
		data = new ByteArrayOutputStream();
	}

	public void release() throws SignatureException {
		if (!Advapi32.INST.CryptReleaseContext(prov.getValue(), 0)) {
			throw new SignatureException(getLastErrorMessage());
		}
	}

	public boolean verifyMessage(byte[] pkcs7) throws SignatureException {
		boolean result = false;

		CRYPT_VERIFY_MESSAGE_PARA verifyParam = new CRYPT_VERIFY_MESSAGE_PARA();
		verifyParam.cbSize = verifyParam.size();
		verifyParam.dwMsgAndCertEncodingType = Advapi32.X509_ASN_ENCODING
				| Crypt32.PKCS_7_ASN_ENCODING;

		Pointer[] pDatas = new Pointer[1];
		pDatas[0] = new Memory(data.toByteArray().length);
		pDatas[0].write(0, data.toByteArray(), 0, data.toByteArray().length);

		if (Crypt32.INST
				.CryptVerifyDetachedMessageSignature(verifyParam, 0, pkcs7,
						pkcs7.length, 1, pDatas, new int[] { data.size() },
						null)) {
			result = true;
		} else {
			throw new SignatureException(getLastErrorMessage());
		}
		return result;
	}

	public byte[] signAndEncodeMessage(String keyContainerName)
			throws SignatureException {

		byte[] pbSignedBlob = null;
		Pointer hCertStore = null;
		Pointer pSignerCert = null;
		Pointer pRootCert = null;
		CERT_CHAIN_CONTEXT certChainContext = null;
		try {

			// �������� ���������� ���������������� ����������
			X509Certificate certInKeystore = (X509Certificate) getCertificate(keyContainerName);

			// �������� key_id �����������
			byte[] keyId = certInKeystore.getExtensionValue("2.5.29.14");

			// ��������� ��������� ������������
			hCertStore = Crypt32.INST.CertOpenSystemStoreA(null,
					Crypt32.CERT_STORE_NAME);
			if (hCertStore == null)
				throw new SignatureException(getLastErrorMessage());

			// ��������� ��������� ������ ����������� � ���������
			CERT_ID certId = new CERT_ID();
			certId.dwIdChoice = Crypt32.CERT_ID_KEY_IDENTIFIER;
			certId.value = new CERT_ID_VALUE();
			certId.value.setType(CRYPTOAPI_BLOB.class);
			certId.value.KeyId = new CRYPTOAPI_BLOB();
			certId.value.KeyId.cbData = keyId.length - 4;

			// KEY_ID ��� ������ ���� ������� � ���������� ��������
			Pointer cerId = new Memory(keyId.length - 4);
			cerId.write(0, keyId, 4, keyId.length - 4);
			certId.value.KeyId.pbData = cerId;

			// �������� ����������
			pSignerCert = Crypt32.INST.CertFindCertificateInStore(hCertStore,
					Advapi32.X509_ASN_ENCODING | Crypt32.PKCS_7_ASN_ENCODING,
					0, Crypt32.CERT_FIND_CERT_ID, certId, null);
			if (pSignerCert == null)
				throw new SignatureException(getLastErrorMessage());

			// ������� ������ ���������� �� �����������
			Memory rgpMsgCert = new Memory(Pointer.SIZE); // 4
			rgpMsgCert.setPointer(0, pSignerCert);

			Pointer[] cers = new Pointer[1];
			cers[0] = pSignerCert;

			// ������� ��������� ��� ������� � ������� PKCS7
			_CRYPT_SIGN_MESSAGE_PARA pSignPara = new _CRYPT_SIGN_MESSAGE_PARA();
			pSignPara.cbSize = pSignPara.size(); // 68
			pSignPara.dwMsgEncodingType = Advapi32.X509_ASN_ENCODING
					| Crypt32.PKCS_7_ASN_ENCODING;
			pSignPara.pSigningCert = pSignerCert;
			pSignPara.HashAlgorithm.objId = Crypt32.szOID_PKCS_7;
			pSignPara.HashAlgorithm.parameters.data = null;
			pSignPara.cMsgCert = 1;
			pSignPara.rgpMsgCert = (Pointer) rgpMsgCert;

			Pointer[] pDatas = new Pointer[1];
			pDatas[0] = new Memory(data.toByteArray().length);
			pDatas[0]
					.write(0, data.toByteArray(), 0, data.toByteArray().length);

			System.out.println("��������� ������� �������");
			IntByReference pcbSignedBlob = new IntByReference();
			if (!Crypt32.INST.CryptSignMessage(pSignPara, true, 1, pDatas,
					new int[] { data.size() }, null, pcbSignedBlob)) {
				System.out.println(getLastErrorMessage());
				throw new SignatureException(getLastErrorMessage());
			}
			int size = pcbSignedBlob.getValue();
			System.out.println("Prepare signed message size: " + size);
			pbSignedBlob = new byte[size];

			System.out.println("��������� ��������������� �������");
			if (!Crypt32.INST.CryptSignMessage(pSignPara, true, 1, pDatas,
					new int[] { data.size() }, pbSignedBlob, pcbSignedBlob)) {
				System.out.println(getLastErrorMessage());
				throw new SignatureException(getLastErrorMessage());
			}
			size = pcbSignedBlob.getValue();
			System.out.println("Signed message size: " + size);

			// �������� ������ �����
			byte[] result = new byte[size];
			for (int i = 0; i < result.length; i++) {
				result[i] = pbSignedBlob[i];
			}

			return result;

		} catch (SignatureException e) {
			throw e;
		} finally {

			if (pSignerCert != null)
				if (!Crypt32.INST.CertFreeCertificateContext(pSignerCert))
					System.out
							.println("Certificate context wasn't freed. Reason: "
									+ getLastErrorMessage());
			if (hCertStore != null)
				if (!Crypt32.INST.CertCloseStore(hCertStore,
						Crypt32.CERT_CLOSE_STORE_FORCE_FLAG))
					System.out.println("Store wasn't closed. Reason: "
							+ getLastErrorMessage());
		}
	}

	public Certificate getCertificate(String keyContainerName)
			throws SignatureException {
		PointerByReference key = null;
		try {
			System.out.println("getCertificate: �������� ��������� ����������������");
			if (!(isInit && batchOperation)) {
				if (!Advapi32.INST.CryptAcquireContextA(prov, keyContainerName,
						null, provType, 0)) {
					System.out.println(getLastErrorMessage());
					throw new SignatureException(getLastErrorMessage());
				}
				isInit = true;
			}

			System.out.println("��������� ��������� ����� AT_SIGNATURE");
			key = new PointerByReference();
			if (!Advapi32.INST.CryptGetUserKey(prov.getValue(),
					Advapi32.AT_SIGNATURE, key)) {
				System.out.println(getLastErrorMessage());
				System.out.println("��������� ��������� ����� AT_KEYEXCHANGE");
				if (!Advapi32.INST.CryptGetUserKey(prov.getValue(),
						Advapi32.AT_KEYEXCHANGE, key)) {
					System.out.println(getLastErrorMessage());
					throw new SignatureException(getLastErrorMessage());
				}
			}

			System.out.println("��������� �����������: �������� ������");
			IntByReference cerSize = new IntByReference();
			if (!Advapi32.INST.CryptGetKeyParam(key.getValue(),
					Advapi32.KP_CERTIFICATE, null, cerSize, 0)) {
				System.out.println(getLastErrorMessage());
				throw new SignatureException(getLastErrorMessage());
			}
			System.out.println("��������� �����������: �������� ����������");
			byte[] cerDer = new byte[cerSize.getValue()];
			if (!Advapi32.INST.CryptGetKeyParam(key.getValue(),
					Advapi32.KP_CERTIFICATE, cerDer, cerSize, 0)) {
				System.out.println(getLastErrorMessage());
				throw new SignatureException(getLastErrorMessage());
			}

			// ��������������� ����������
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			X509Certificate cert = (X509Certificate) cf
					.generateCertificate(new ByteArrayInputStream(cerDer));

			return cert;
		} catch (SignatureException ex) {
			throw ex;
		} catch (Exception ex) {
			throw new SignatureException("Error get certificate", ex);
		} finally {
			if (key.getValue() != null
					&& !Advapi32.INST.CryptDestroyKey(key.getValue())) {
				throw new SignatureException(getLastErrorMessage());
			}
			if (!batchOperation) {
				if (!Advapi32.INST.CryptReleaseContext(prov.getValue(), 0)) {
					throw new SignatureException(getLastErrorMessage());
				}
			}
			try {
				data.close();
			} catch (Exception ignoreEx) {
			}
		}
	}
	
	public List<String> getKeyStoreListCSP() {
		List<String> stores = new ArrayList<String>(); 
		String err = null;
		
		PointerByReference prov = new PointerByReference();
		if (Advapi32.INST.CryptAcquireContextA(prov, null, null, provType, Advapi32.CRYPT_VERIFYCONTEXT)) {
			System.out.println("CryptAcquireContext created");
			IntByReference size = new IntByReference(1024);
			byte[] data = new byte[size.getValue()];
			if (Advapi32.INST.CryptGetProvParam(prov.getValue(), Advapi32.PP_ENUMCONTAINERS, data, size, Advapi32.CRYPT_FIRST)) {
				System.out.println("CryptGetProvParam return first \"true\"");
				try {
					stores.add(new String(data, "cp1251").trim());
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}

				while (Advapi32.INST.CryptGetProvParam(prov.getValue(), Advapi32.PP_ENUMCONTAINERS, data, size, Advapi32.CRYPT_NEXT)) {
					System.out.println("CryptGetProvParam next \"true\"");
					try {
						stores.add(new String(data, "cp1251").trim());
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					data = new byte[size.getValue()];
				}
				
				if (Advapi32.ERROR_NO_MORE_ITEMS != Kernel32.INST.GetLastError()){
					System.out.println("ERROR: CryptGetProvParam - not ERROR_NO_MORE_ITEMS ");
					err = getLastErrorMessage();
				}
			} else {
				err = getLastErrorMessage();
			}

			if (prov!=null) {
				Advapi32.INST.CryptReleaseContext(prov.getValue(), 0);
			}
		} else {
			err = getLastErrorMessage();
		}
		
		if (err!=null) {
			System.out.println(err);
			return null;
		} else {
			return stores;
		}
		
	}
}
