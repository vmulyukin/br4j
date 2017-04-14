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
import ru.lissi.crypto.Gost_28147Key;
import ru.lissi.crypto.DefaultCryptoParams;
import ru.lissi.crypto.spec.*;
import ru.lissi.provider.*;
import javax.crypto.*;
import java.security.AlgorithmParameters;
import java.security.Key;
import java.security.Provider;
import java.security.Security;

public class CryptoSimmetric {	
	Gost_28147ParameterSpec par = null;
	Cipher cipher = null;
	Key key = null;
	
	public CryptoSimmetric(String simmKey){
		try{	
		// �������� ���������
			Provider pjce = new ru.lissi.provider.LirJCE();
            Provider pjsse = new ru.lissi.net.ssl.internal.ssl.Provider();
            Security.insertProviderAt(pjsse, 3);
            Security.insertProviderAt(pjce, 4);
            
			par = DefaultCryptoParams.getCipherPar();
			cipher = Cipher.getInstance(LirJCE.GOST28147_89_NAME);	   
			key = new Gost_28147Key(simmKey.getBytes());
			
		}catch(Exception e){
			System.out.println(e.getMessage());
		}
	}
	
	public String decrypt(String inString){
		String result = null;
		
		try{
			
			byte[] encrypted = inString.getBytes();		
			cipher.init(Cipher.DECRYPT_MODE, key, par);        
			byte[] decrypted = cipher.doFinal(encrypted, 0, encrypted.length);		
			result = new String(decrypted);
		
		}catch(Exception e){
			System.out.println("Decryption has failed");
			//e.printStackTrace();			
		}
		return result;
	}
	
	public String encrypt(String inString){
		String result = null;
		try{
			
		 // ��������� ������ ������
			byte[] plaintext = inString.getBytes();
			cipher.init(Cipher.ENCRYPT_MODE, key, par);   
			byte[] encrypted = cipher.doFinal(plaintext, 0, plaintext.length);
			result = new String(encrypted);
		
		}catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
  
}
