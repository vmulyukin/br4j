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
package com.aplana.owriter.token;

import java.security.spec.KeySpec;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.aplana.owriter.utils.TimeBasedCache;

/**
 * ����� ��� ��������� �������. ��� ����������� ����� ���������� ������������ ���������� TripleDES.
 * ������ � ��������������� �� ���������������� �������� ����������� � time-based ����.
 *
 * @author Vlad Alexandrov
 * @version 1.0
 * @since   2014-10-03
 */
public class SecureTokenGenerator {
	protected final Log logger = LogFactory.getLog(getClass());
	
	private static final String myEncryptionScheme = "DESede";
    private static final String UNICODE_FORMAT = "UTF8";
    private static final int TOKEN_MAXAGE_VALUE = 30 * 1000;	// 30 seconds
    private static final int TOKEN_CACHE_CLEANUP_TIMEOUT_VALUE = 600 * 1000;	// 10 minutes
    private KeySpec ks;
    private SecretKeyFactory skf;
    private Cipher cipher;
    private byte[] arrayBytes;

    private SecretKey key;
    private static TimeBasedCache tokenTimeBasedCache = 
    		new TimeBasedCache(TOKEN_MAXAGE_VALUE, TOKEN_CACHE_CLEANUP_TIMEOUT_VALUE);

    public SecureTokenGenerator(String encryptionKey) throws Exception {
    	arrayBytes = encryptionKey.getBytes(UNICODE_FORMAT);
    	ks = new DESedeKeySpec(arrayBytes);
    	skf = SecretKeyFactory.getInstance(myEncryptionScheme);
    	cipher = Cipher.getInstance(myEncryptionScheme);
    	key = skf.generateSecret(ks);
	}

    public String generateToken(String unencryptedString){
    	String token = this.encrypt(unencryptedString);
    	if (null != token)
    		tokenTimeBasedCache.put(token, unencryptedString);
    	return token;
    }
    
    public static synchronized String getCachedUnencryptedString(String token){
    	String unencryptedString = (String)(tokenTimeBasedCache.get(token));
    	tokenTimeBasedCache.remove(token);
 
    	return unencryptedString;
    }

    private String encrypt(String unencryptedString) {
    	String encryptedString = null;
    	try {
    		cipher.init(Cipher.ENCRYPT_MODE, key);
    		byte[] plainText = unencryptedString.getBytes(UNICODE_FORMAT);
    		byte[] encryptedText = cipher.doFinal(plainText);
    		encryptedString = new String(Base64.encodeBase64(encryptedText, false, true));
    	}catch (Exception e) {
    		logger.error(e.getMessage(), e);
    	}
    	return encryptedString;
    }
}
