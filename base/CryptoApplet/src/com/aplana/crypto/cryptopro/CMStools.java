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
 * $RCSfile: CMStools.java,v $
 * version $Revision: 1.3 $
 * created 21.04.2009 16:51:19 by kunina
 * last modified $Date: 2009/06/17 07:34:02 $ by $Author: kunina $
 * (C) ��� ������-��� 2004-2009.
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

import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.tools.Array;
import ru.CryptoPro.JCPRequest.GostCertificateRequest;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.logging.Logger;

/**
 * @author Copyright 2004-2009 Crypto-Pro. All rights reserved.
 * @.Version
 */
public class CMStools {
/**
 * ������� ����������
 */
public static String TEST_PATH = "E:\\Job\\test\\CMS";
/**
 * ���������� ����� �����������
 */
public static final String CERT_EXT = ".cer";
/**
 * ���������� �����
 */
public static final String CMS_EXT = ".p7b";
/**
 * �����������
 */
public static final String SEPAR = File.separator;

/**
 * ��� ����������� (���������, ����������)
 */
public static final String SIGN_KEY_NAME = "signkey";
public static final char[] SIGN_KEY_PASSWORD = null;
public static String SIGN_CERT_PATH =
        TEST_PATH + SEPAR + SIGN_KEY_NAME + CERT_EXT;

/**
 * ��� ���������� (���������, ����������)
 */
public static final String RECIP_KEY_NAME = "recipkey";
public static final char[] RECIP_KEY_PASSWORD = null;
public static String RECIP_CERT_PATH =
        TEST_PATH + SEPAR + RECIP_KEY_NAME + CERT_EXT;

/**
 * ��������� � �.�.
 */
public static final String STORE_TYPE = "HDImageStore";
public static final String KEY_ALG_NAME = JCP.GOST_DH_NAME;
public static final String DIGEST_ALG_NAME = JCP.GOST_DIGEST_NAME;

public static final String SEC_KEY_ALG_NAME = "GOST28147";

/**
 * OIDs ��� CMS
 */
public static final String STR_CMS_OID_DATA = "1.2.840.113549.1.7.1";
public static final String STR_CMS_OID_SIGNED = "1.2.840.113549.1.7.2";
public static final String STR_CMS_OID_ENVELOPED = "1.2.840.113549.1.7.3";

public static final String STR_CMS_OID_CONT_TYP_ATTR = "1.2.840.113549.1.9.3";
public static final String STR_CMS_OID_DIGEST_ATTR = "1.2.840.113549.1.9.4";
public static final String STR_CMS_OID_SIGN_TYM_ATTR = "1.2.840.113549.1.9.5";

public static final String STR_CMS_OID_TS = "1.2.840.113549.1.9.16.1.4";

public static final String DIGEST_OID = JCP.GOST_DIGEST_OID;
public static final String SIGN_OID = JCP.GOST_EL_KEY_OID;

/**
 * �������� ������
 */
public static final String DATA = "12345";
public static final String DATA_FILE = "data.txt";
public static String DATA_FILE_PATH = TEST_PATH + SEPAR + DATA_FILE;

/**
 * logger
 */
public static final Logger logger = Logger.getLogger("LOG");

/**
 * @param args *
 * @throws Exception /
 */
public static void main(String[] args) throws Exception {
    //�������� �����������
    createContainer(RECIP_KEY_NAME, RECIP_KEY_PASSWORD);
    createContainer(SIGN_KEY_NAME, SIGN_KEY_PASSWORD);
    //������� ������������
    expCert(RECIP_KEY_NAME, RECIP_CERT_PATH);
    expCert(SIGN_KEY_NAME, SIGN_CERT_PATH);
    //������ �������� ������
    Array.writeFile(DATA_FILE_PATH, DATA.getBytes());
}

/**
 * @param name ���
 * @param pathh ���� ��� ����������
 * @throws KeyStoreException /
 * @throws NoSuchAlgorithmException /
 * @throws IOException /
 * @throws CertificateException /
 */
private static void expCert(String name, String pathh) throws KeyStoreException,
        NoSuchAlgorithmException, IOException, CertificateException {
    final KeyStore ks = KeyStore.getInstance(STORE_TYPE);
    ks.load(null, null);
    final Certificate cert = ks.getCertificate(name);
    Array.writeFile(pathh, cert.getEncoded());
}

/**
 * @param name ��� ����������
 * @param password ������ �� ���������
 * @throws NoSuchAlgorithmException /
 * @throws IOException /
 * @throws SignatureException /
 * @throws InvalidKeyException /
 * @throws CertificateException /
 * @throws KeyStoreException /
 */
private static void createContainer(String name, char[] password)
        throws NoSuchAlgorithmException, IOException, SignatureException,
        InvalidKeyException, CertificateException, KeyStoreException {
    final KeyPairGenerator kg = KeyPairGenerator.getInstance(KEY_ALG_NAME);
    final KeyPair keyPair = kg.generateKeyPair();
    //������������� ���������������� �����������(������)
    final GostCertificateRequest req = new GostCertificateRequest();
    req.init(KEY_ALG_NAME, false);
    final byte[] encodedCert = req.getEncodedSelfCert(keyPair, "CN=" + name);

    //������������� ���������������� �����������
    final CertificateFactory cf = CertificateFactory.getInstance("X509");
    final Certificate[] certs;
    certs = new Certificate[1];
    certs[0] = cf.generateCertificate(new ByteArrayInputStream(encodedCert));

    //������ � ��������� �������� ���� � ��������������� ������������
    final KeyStore ks = KeyStore.getInstance(STORE_TYPE);
    ks.load(null, null);
    ks.setKeyEntry(name, keyPair.getPrivate(), password, certs);
}

/**
 * ��������� PrivateKey �� store.
 *
 * @param name alias �����
 * @param password ������ �� ����
 * @return PrivateKey
 * @throws Exception in key read
 */
public static PrivateKey loadKey(String name, char[] password)
        throws Exception {
    final KeyStore hdImageStore = KeyStore.getInstance(CMStools.STORE_TYPE);
    hdImageStore.load(null, null);
    return (PrivateKey) hdImageStore.getKey(name, password);
}

/**
 * ��������� certificate �� store.
 *
 * @param name alias �����������.
 * @return Certificate
 * @throws Exception in cert read
 */
public static Certificate loadCertificate(String name)
        throws Exception {
    final KeyStore hdImageStore = KeyStore.getInstance(CMStools.STORE_TYPE);
    hdImageStore.load(null, null);
    return hdImageStore.getCertificate(name);
}

/**
 * read certificate from file.
 *
 * @param fileName certificate file name
 * @return certificate
 * @throws IOException in cert read
 * @throws CertificateException if error file format
 */
public static Certificate readCertificate(String fileName) throws IOException,
        CertificateException {
    FileInputStream fis = null;
    BufferedInputStream bis = null;
    final Certificate cert;
    try {
        fis = new FileInputStream(fileName);
        bis = new BufferedInputStream(fis);
        final CertificateFactory cf = CertificateFactory.getInstance("X.509");
        cert = cf.generateCertificate(bis);
        return cert;
    } finally {
        if (bis != null) bis.close();
        if (fis != null) fis.close();
    }
}

/**
 * @param bytes bytes
 * @param digestAlgorithmName algorithm
 * @return digest
 * @throws Exception e
 */
public static byte[] digestm(byte[] bytes, String digestAlgorithmName)
        throws Exception {
    //calculation messageDigest
    final ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
    final MessageDigest digest = MessageDigest.getInstance(digestAlgorithmName);
    final DigestInputStream digestStream =
            new DigestInputStream(stream, digest);
    while (digestStream.available() != 0) digestStream.read();
    return digest.digest();
}
}
