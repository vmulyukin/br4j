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
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

public interface Advapi32
        extends Library {

    public Advapi32 INST = (Advapi32) Native.loadLibrary("advapi32", Advapi32.class);
    public static final int PROV_GOST_2001_DH = 75;
    public static final int ALG_TYPE_ANY = 0;
    public static final int ALG_CLASS_HASH = (4 << 13);
    public static final int ALG_SID_GR3411 = (30);
    public static final int CALG_GR3411 = (ALG_CLASS_HASH | ALG_TYPE_ANY
            | ALG_SID_GR3411);
    public static final int HP_OID = 10;
    public static final int AT_KEYEXCHANGE = 1;
    public static final int AT_SIGNATURE = 2;
    public static final int X509_ASN_ENCODING = 1;
    public static final int PUBLICKEYBLOB = 6;
    public static final int X509_PUBLIC_KEY_INFO = 8;
    public static final int RSA_CSP_PUBLICKEYBLOB = 19;
    public static final String NTE_BAD_SIGNATURE = "80090006";
    public static final int HP_HASHVAL = 2;
    public static final int CRYPT_VERIFYCONTEXT = -268435456;
    public static final int KP_CERTIFICATE = 26;
	public static final int CRYPT_MACHINE_KEYSET = 32;
    public static final int PP_ENUMCONTAINERS = 2;
    public static final int  CRYPT_FIRST  = 1;
    public static final int CRYPT_NEXT = 2;
    public static final int ERROR_NO_MORE_ITEMS= 259;

    /**
     * ������������� ��������� ������ ����������
     * @param prov ���������� ����������, out-��������
     * @param pszContainer ��� ���������� ������
     * @param pszProvider ��� ����������
     * @param dwProvType ��� ����������
     * @param dwFlags �����
     * @return boolean
     */
    public boolean CryptAcquireContextA(
            PointerByReference prov,
            String pszContainer,
            String pszProvider,
            int dwProvType,
            int dwFlags);

    public boolean CryptCreateHash(
            Pointer prov,
            int alg,
            int key,
            int flags,
            PointerByReference hash);

    public boolean CryptReleaseContext(
            Pointer prov,
            int flags);

    public boolean CryptDestroyHash(
            Pointer hash);

    public boolean CryptGetHashParam(
            Pointer hash,
            int param,
            byte[] data,
            IntByReference dataLen,
            int flags);

    public boolean CryptHashData(
            Pointer hash,
            byte[] data,
            int dataLen,
            int flags);

    public boolean CryptSignHashA(
            Pointer hash,
            int keySpec,
            String description,
            int flags,
            byte[] signature,
            IntByReference signLen);

    public boolean CryptSetHashParam(
            Pointer hash,
            int param,
            byte[] data,
            int flags);

    public boolean CryptVerifySignatureA(
            Pointer hash,
            byte[] signature,
            int signLen,
            Pointer publicKey,
            String description,
            int flags);

    public boolean CryptGetUserKey(
            Pointer prov,
            int keySpec,
            PointerByReference key);

    public boolean CryptExportKey(
            Pointer key,
            Pointer expKey,
            int blobType,
            int flags,
            byte[] data,
            IntByReference keyLen);

    public boolean CryptImportKey(
            Pointer prov,
            byte[] data,
            int dataLen,
            Pointer pubKey,
            int flags,
            PointerByReference key);

    public boolean CryptDestroyKey(
            Pointer key);
    
    public boolean CryptGetKeyParam(
    		Pointer hKey,
    		int dwParam,
    		byte[] pbData,
    		IntByReference pdwDataLen,
    		int dwFlags);
    
    public boolean CryptGetProvParam(
    		Pointer prov,
    		int dwParam,
    		byte[] pbData,
    		IntByReference pdwDataLen,
    		int dwFlags);
    
}
