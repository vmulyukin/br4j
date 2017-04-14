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

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.StringArray;
import com.sun.jna.Union;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;

public interface Crypt32 extends Library {

	public Crypt32 INST = (Crypt32) Native
			.loadLibrary("crypt32", Crypt32.class);

	public static int PKCS_7_ASN_ENCODING = 0x10000;
	public static String szOID_PKCS_7 = "1.2.840.113549.1.7";
	public static String CERT_STORE_PROV_SYSTEM = "\10";
	public static int CERT_SYSTEM_STORE_CURRENT_USER = 1 << 16;
	public static String CERT_STORE_NAME = "MY";
	public static String ROOT_CERT_STORE_NAME = "ROOT";
	public static int CERT_FIND_SUBJECT_STR = 8 << 16 | 7;
	public static int CERT_FIND_EXISTING = 13 << 16;
	public static int CERT_CLOSE_STORE_FORCE_FLAG = 2;
	public static int USAGE_MATCH_TYPE_AND = 0;
	public static int CERT_CHAIN_CACHE_END_CERT = 1;
	public static int CERT_COMPARE_CERT_ID = 16;
	public static int CERT_COMPARE_SHIFT = 16;
	public static int CERT_FIND_CERT_ID = CERT_COMPARE_CERT_ID << CERT_COMPARE_SHIFT;
	public static int CERT_ID_KEY_IDENTIFIER = 2;
	public static int CERT_KEY_IDENTIFIER_PROP_ID = 20;

	public static class CERT_PUBLIC_KEY_INFO extends Structure {

		public CRYPT_ALGORITHM_IDENTIFIER algorithm;
		public CRYPT_BIT_BLOB publicKey;
	}

	public static class CRYPT_ALGORITHM_IDENTIFIER extends Structure {

		public String objId;
		public CRYPT_BIT_BLOB parameters;
	}

	public static class CRYPT_BIT_BLOB extends Structure {

		public int dataSize;
		public Pointer data;
	}

	public static class CRYPT_VERIFY_MESSAGE_PARA extends Structure {
		public int cbSize;
		public int dwMsgAndCertEncodingType;
		public Pointer hCryptProv;
		public Pointer pfnGetSignerCertificate;
		public Pointer pvGetArg;
	}

	public static class _CRYPT_SIGN_MESSAGE_PARA extends Structure {
		public int cbSize; // DWORD
		public int dwMsgEncodingType; // DWORD
		public Pointer pSigningCert; // PCCERT_CONTEXT
		public CRYPT_ALGORITHM_IDENTIFIER HashAlgorithm;
		public Pointer pvHashAuxInfo; // void *
		public int cMsgCert; // DWORD
		public Pointer rgpMsgCert; // PCCERT_CONTEXT *
		public int cMsgCrl; // DWORD
		public Pointer rgpMsgCrl; // PCCRL_CONTEXT *
		public int cAuthAttr; // DWORD
		public Pointer rgAuthAttr; // PCRYPT_ATTRIBUTE *
		public int cUnauthAttr; // DWORD
		public Pointer rgUnauthAttr; // PCRYPT_ATTRIBUTE *
		public int dwFlags; // DWORD
		public int dwInnerContentType;
	}

	public static class CERT_TRUST_STATUS extends Structure {
		public int dwErrorStatus;
		public int dwInfoStatus;
	}

	public static class CERT_CHAIN_CONTEXT extends Structure {
		public int cbSize;
		public CERT_TRUST_STATUS TrustStatus;
		public int cChain;
		public Pointer rgpChain;
		public int cLowerQualityChainContext;
		public Pointer rgpLowerQualityChainContext;
		public boolean fHasRevocationFreshnessTime;
		public int dwRevocationFreshnessTime;
	}

	public static class CERT_CHAIN_PARA extends Structure {
		public int cbSize;
		public CERT_USAGE_MATCH RequestedUsage;
		public CERT_USAGE_MATCH RequestedIssuancePolicy;
		public int dwUrlRetrievalTimeout;
		public boolean fCheckRevocationFreshnessTime;
		public int dwRevocationFreshnessTime;
		public Pointer pftCacheResync;
	}

	public static class CERT_USAGE_MATCH extends Structure {
		public int dwType;
		public CERT_ENHKEY_USAGE Usage;
	}

	public static class CERT_ENHKEY_USAGE extends Structure {
		public int cUsageIdentifier;
		public String rgpszUsageIdentifier;
	}

	public static class CERT_CHAIN_ENGINE_CONFIG extends Structure {
		public int cbSize;
		public Pointer hRestrictedRoot;
		public Pointer hRestrictedTrust;
		public Pointer hRestrictedOther;
		public int cAdditionalStore;
		public Pointer rghAdditionalStore;
		public int dwFlags;
		public int dwUrlRetrievalTimeout;
		public int MaximumCachedCertificates;
		public int CycleDetectionModulus;
		public Pointer hExclusiveRoot;
		public Pointer hExclusiveTrustedPeople;
	}

	public static class CERT_ID extends Structure {
		public int dwIdChoice;
		public CERT_ID_VALUE value;

		public static class CERT_ID_VALUE extends Union {
			public CERT_ISSUER_SERIAL_NUMBER IssuerSerialNumber;
			public CRYPTOAPI_BLOB KeyId;
			public CRYPTOAPI_BLOB HashId;
		};
	}

	public static class CERT_ISSUER_SERIAL_NUMBER extends Structure {
		public CRYPTOAPI_BLOB Issuer;
		public CRYPTOAPI_BLOB SerialNumber;
	}

	public static class CRYPTOAPI_BLOB extends Structure {
		public int cbData;
		public Pointer pbData;
	}

	public boolean CryptImportPublicKeyInfo(Pointer prov, int certEncodingType,
			byte[] info, PointerByReference key);

	public boolean CryptImportPublicKeyInfoEx(Pointer prov,
			int certEncodingType, CERT_PUBLIC_KEY_INFO cerInfo, int keyAlg,
			int flags, Pointer auxInfo, PointerByReference key);

	public boolean CryptDecodeObject(int certEncodingType, int structType,
			byte[] encoded, int encodedSize, int flags, byte[] structInfo,
			IntByReference structInfoSize);

	public Pointer CertOpenStore( // HCERTSTORE
			String lpszStoreProvider, // __in LPCSTR
			int dwMsgAndCertEncodingType, // __in DWORD
			Pointer hCryptProv, // HCRYPTPROV_LEGACY, should be set to null
			int dwFlags, // __in DWORD
			String pvPara // __in const void *
	);

	public Pointer CertOpenSystemStoreA( // HCERTSTORE
			Pointer hprov, // __in HCRYPTPROV_LEGACY
			String szSubsystemProtocol // __in LPTCSTR
	);

	public Pointer CertFindCertificateInStore(Pointer hCertStore,// __in
																	// HCERTSTORE
			int dwCertEncodingType,// __in DWORD
			int dwFindFlags,// __in DWORD
			int dwFindType,// __in DWORD
			Structure pvFindPara,// __in const void *
			Pointer pPrevCertContext // __in PCCERT_CONTEXT
	);

	public boolean CryptSignMessage(_CRYPT_SIGN_MESSAGE_PARA pSignPara, // PCRYPT_SIGN_MESSAGE_PARA,
																		// eq.
																		// to
																		// _CRYPT_SIGN_MESSAGE_PARA
																		// *,
																		// will
																		// be
																		// interpreted
																		// as
																		// structure
																		// pointer
																		// by
																		// JNA
																		// by
																		// default
			boolean fDetachedSignature, // __in BOOL
			int cToBeSigned, // __in DWORD
			Pointer[] rgpbToBeSigned, // __in const BYTE *[],
			int[] rgcbToBeSigned, // __in DWORD
			byte[] pbSignedBlob, // _out BYTE *
			IntByReference pcbSignedBlob // __inout DWORD *
	);

	public boolean CryptVerifyMessageSignature(
			CRYPT_VERIFY_MESSAGE_PARA pVerifyPara, int dwSignerIndex,
			byte[] pbSignedBlob, int cbSignedBlob, IntByReference pbDecoded,
			byte[] pcbDecoded, PointerByReference ppSignerCert);

	public boolean CryptVerifyDetachedMessageSignature(
			CRYPT_VERIFY_MESSAGE_PARA pVerifyPara, int dwSignerIndex,
			byte[] pbDetachedSignedBlob, int cbDetachedSignedBlob,
			int cToBeSigned, Pointer[] rgpbToBeSigned, int[] rgcbToBeSigned,
			PointerByReference ppSignerCert);

	boolean CertFreeCertificateContext(Pointer pCertContext // __in
															// PCCERT_CONTEXT
	);

	boolean CertCloseStore(Pointer hCertStore, // __in HCERTSTORE
			int dwFlags // __in DWORD
	);

	public boolean CertGetCertificateChain(Pointer hChainEngine,
			Pointer pCertContext, Pointer pTime, Pointer hAdditionalStore,
			CERT_CHAIN_PARA pChainPara, int dwFlags, Pointer pvReserved,
			Pointer ppChainContext);

	public void CertFreeCertificateChain(Pointer pChainContext);

	public boolean CertCreateCertificateChainEngine(
			CERT_CHAIN_ENGINE_CONFIG pConfig, PointerByReference phChainEngine);

	public Pointer CertCreateCertificateContext(int dwCertEncodingType,
			byte[] pbCertEncoded, int cbCertEncoded);

	public boolean CertGetCertificateContextProperty(Pointer pCertContext,
			int dwPropId, byte[] pvData, IntByReference pcbData);

}
