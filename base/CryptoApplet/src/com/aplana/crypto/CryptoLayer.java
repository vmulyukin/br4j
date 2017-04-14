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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.LineNumberReader;
import java.io.IOException;
import java.net.URL;
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
import java.security.cert.X509Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateEncodingException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.management.RuntimeErrorException;
import javax.servlet.ServletContext;

import org.apache.commons.lang.ArrayUtils;

import com.aplana.dbmi.ConfigService;


public abstract class CryptoLayer {
	public static final String CONFIG_FILE = "dbmi/card/signature/cryptoLayer.properties";	
	//public static final String ALGORITHM_SIGN = "GOST_DS";
	//public static final String ALGORITHM_HASH = "HASH_34_11_94";	
	//public static final String PROVIDER = "LirJCE";
	//public static final String KeyStoreName = "PKCS12";
	
	public static final String SERVER_CRYPTO_LAYER = "server.crypto.layer";
	public static final String SERVER_CRYPTO_LAYER_PARAMS = "server.crypto.layer.params";
	public static final String CLIENT_CRYPTO_LAYER = "client.crypto.layer";
	public static final String CLIENT_CRYPTO_LAYER_PARAMS = "client.crypto.layer.params";
	public static final String CLIENT_TIMESTAMP_ADDRESS = "client.timestamp.address";

	public static final String HIDE_CONTEINER_FIELD = "HIDE_CONTEINER_FIELD";
	public static final String HIDE_PASSWORD_FIELD = "HIDE_PASSWORD_FIELD";

	private static CryptoLayer m_instance; 
	private static Properties m_config;
	protected String m_layaerParams;
	private Certificate pkcs7SignerCertificate;
	private URL codebase;
	
	public URL getCodebase() {
		return codebase;
	}

	public void setCodebase(URL codebase) {
		this.codebase = codebase;
	}

	//private String m_keystoreName;
	/**
	 * �������� ���������� ������� ������ ��� ������ ��������������� 
	 * @param context
	 * @return
	 */
	public static CryptoLayer getInstance(ConfigService configService){
		CryptoLayer result = null;
		try{
			if (m_instance == null){
				loadConfig(configService);
				String className = getConfigParam(SERVER_CRYPTO_LAYER);
				if (className == null){
					throw new Exception("Property " + SERVER_CRYPTO_LAYER + " need set");
				}
				Class cryptoLayerClass = Class.forName(className);
				m_instance = (CryptoLayer)cryptoLayerClass.newInstance();
				m_instance.m_layaerParams = getConfigParam(SERVER_CRYPTO_LAYER_PARAMS);
			}
			result = m_instance;
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return result;
	}

	public static CryptoLayer getInstance(String className, String params){
		CryptoLayer result = null;
		try{
			if (m_instance == null){
				Class cryptoLayerClass = Class.forName(className);
				m_instance = (CryptoLayer)cryptoLayerClass.newInstance();
				m_instance.m_layaerParams = params;
			}
			result = m_instance;
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return result;
	}

	public static CryptoLayer getInstance(){
		if (m_instance == null){
			throw new RuntimeException("Call getInstance with params first, before call without params");
		}
		return m_instance;
	}
	
	
	public byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);
           
        long length = file.length();        
        byte[] bytes = new byte[(int)length];
    
        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
               && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }   
        
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file "+file.getName());
        }
            
        is.close();
        return bytes;
    } 
	
	@SuppressWarnings("finally")
	public Certificate getCertFromFile(String filePath) {
		File certFile = new File(filePath);
		Certificate retVal = null;

		try {
			final CertificateFactory cf = CertificateFactory
					.getInstance("X509");
			if (certFile != null) {
				retVal = cf.generateCertificate(new FileInputStream(certFile));
				System.out
						.println("Certificate was got from file successfully!");
			} else {
				System.out.println("Unable to read Certificate...");
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return retVal;
		}
	}

	@SuppressWarnings("finally")
	public Certificate getCertFromStream(InputStream inStream) {
		Certificate retVal = null;

		try {
			final CertificateFactory cf = CertificateFactory
					.getInstance("X509");
			if (inStream != null) {
				retVal = cf.generateCertificate(inStream);
				System.out
						.println("Certificate was got from Stream successfully!");
			} else {
				System.out.println("Unable to read Certificate...");
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return retVal;
		}
	}

	@SuppressWarnings("finally")
	public Certificate getCertFromStringBase64(String inString) {
		Certificate retVal = null;
		Base64 decoder = new Base64();

		try {
			final CertificateFactory cf = CertificateFactory
					.getInstance("X.509");
			if (inString != null) {
				byte[] certBytes = Base64.base64ToByteArray(inString);
				ByteArrayInputStream bais = new ByteArrayInputStream(certBytes);
				retVal = cf.generateCertificate(bais);
				System.out
						.println("Certificate was got from String Base 64 successfully!");
			} else {
				System.out.println("Unable to read Certificate...");
			}

		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			return retVal;
		}
	}
	
	public X509Certificate getX509CertFromStringBase64(String inString) {
		return (X509Certificate) getCertFromStringBase64(inString);
	}

	public String getCertStringFromFileBase64(String filePath) {
		String retVal = null;
		final String inCertFilePath = filePath;
		try {
			retVal = AccessController
					.doPrivileged(new PrivilegedAction<String>() {
						public String run() {
							String certString = "";
							String clear = null;

							try {
								File certFile = new File(inCertFilePath);
								if (!certFile.exists())
									return null;
								else {
									final String beginString = "-----BEGIN CERTIFICATE-----";
									final String endString = "-----END CERTIFICATE-----";
									FileReader fileReader = new FileReader(
											certFile);
									LineNumberReader lineReader = new LineNumberReader(
											fileReader);
									String lineString = null;
									while ((lineString = lineReader.readLine()) != null) {
										if (!lineString.equals(beginString)
												&& !lineString
														.equals(endString))
											certString = certString
													.concat(lineString);
									}

								}
							} catch (Exception e) {
								e.printStackTrace();
							} finally {
								return certString;
							}
						}
					});

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return retVal;
		}
	}
	
	public String getCertStringFromStream(final InputStream inputStream) {
		String retVal = null;
		try {
			retVal = AccessController
					.doPrivileged(new PrivilegedAction<String>() {
						public String run() {
							String certString = "";

							try {
								if (null == inputStream) {
									return null;
								}
								
								final String beginString = "-----BEGIN CERTIFICATE-----";
								final String endString = "-----END CERTIFICATE-----";
								LineNumberReader lineReader = new LineNumberReader(new InputStreamReader(inputStream));
								String lineString = null;
								while ((lineString = lineReader.readLine()) != null) {
									if (!lineString.equals(beginString)
											&& !lineString
													.equals(endString))
										certString = certString
												.concat(lineString);
								}
							} catch (Exception e) {
								e.printStackTrace();
								certString = null;
							}
							
							if(certString.length() == 0) {
								certString = null;
							}
							
							return certString;
						}
					});

		} catch (Exception e) {
			e.printStackTrace();
		}
		return retVal;
	}

	@SuppressWarnings("finally")
	public String[] getStringContentSignature(String cerStoreName,
			String certName, String contentData, String contentHash, String password) {
		StringBuffer[] retVal = new StringBuffer[]{new StringBuffer(), new StringBuffer()};
		List<byte[][]> bytesList = null;
		try {
			//String resString = new String(contentHash.getBytes("UTF-8"));
			bytesList = signContentString(cerStoreName, certName, contentData, contentHash, password);
			for (byte[][] bs : bytesList) {
				if (retVal[0].length() > 0){
					retVal[0].append(";");
				}
				retVal[0].append(Base64.byteArrayToBase64(bs[0]));
				
				if (bs[1] != null){
					for(int i = 0; i < bs[1].length; i++){
						String hex = Integer.toHexString((int)bs[1][i] & 0xFF).toUpperCase();
						retVal[1].append(hex.length() == 1 ? "0" + hex : hex);
					}
					if (retVal[1].length() > 0) retVal[1].append(";");
				}
			}
			
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			return new String[]{retVal[0].toString(), retVal[1].toString()};
		}
	}

	@SuppressWarnings("finally")
	public boolean checkStringContentSignature(String inString,
			String inSignature, Certificate inCert) {
		boolean retVal = false;
		byte[] byteSignature = null;
		String os = System.getProperty("os.name");
		System.out.println("OS is: " + os);
		try {
			String resString = new String(inString.getBytes("UTF-8"));
			byteSignature = Base64.base64ToByteArray(inSignature);
			retVal = checkStringSignature(resString, byteSignature, inCert);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return retVal;
		}
	}

	@SuppressWarnings("finally")
	public String[] getLocalFileSignature(String cerStoreName,
			String certName, String filePath, String password) {
		Base64 encoder = new Base64();
		StringBuffer out = new StringBuffer();
		String s = null;
		byte bytesArray[][] = null;
		try {
			bytesArray = signLocalFile(cerStoreName, certName, filePath, password);
			s = Base64.byteArrayToBase64(bytesArray[0]);
			for(int i = 0; i < bytesArray[1].length; i++) {
				String hex = Integer.toHexString((int)bytesArray[1][i] & 0xFF).toUpperCase();
				out.append(hex.length() == 1 ? "0" + hex : hex);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return new String[]{s, out.toString()};
		}
	}

	@SuppressWarnings("finally")
	public boolean checkLocalFileSignature(String filePath,
			String inSignature, Certificate inCert) {
		boolean retVal = false;
		byte[] byteSignature = null;
		try {
			byteSignature = Base64.base64ToByteArray(inSignature);
			retVal = checkFileSignature(filePath, byteSignature, inCert);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return retVal;
		}
	}

	@SuppressWarnings("finally")
	public boolean checkStreamFileSignature(InputStream fileInStream,
			String inSignature, Certificate inCert, Integer attachSize) {
		boolean retVal = false;
		byte[] byteSignature = null;
		Base64 decoder = new Base64();
		try {
			byteSignature = Base64.base64ToByteArray(inSignature);
			System.out.println("Byte signature from Base64 got!");
			retVal = checkStreamSignature(fileInStream, byteSignature, inCert,
					attachSize);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return retVal;
		}
	}

	@SuppressWarnings("finally")
	public List<byte[][]> signContentString(String cerStoreName,
			String certName, String contentData, String contentHash, String password) {
		List<byte[][]> retVal = null;
		List ret = null;

		try {			
			System.out.println("Creating signature...");
			System.out.println("cerStoreName="+cerStoreName);
			System.out.println("certName="+certName);
			System.out.println("contentHash="+contentHash);
			
			String[] contentHashs = contentHash.split(";");
			List<byte[]> inHash = new ArrayList<byte[]>();
			for (int i = 0; i < contentHashs.length; i++) {
				inHash.add(contentHashs[i].getBytes());				
			}

			List<byte[]> inData = null;
			if (contentData != null){
				inData = new ArrayList<byte[]>();
				String[] contentDatas = contentData.split("\\$");
				for (int i = 0; i < contentDatas.length; i++) {
					String[] contentDataAttrs = contentDatas[i].split(";");
					byte[] contentDataAttrsBytes = null;
					for(int j = 0; j<contentDataAttrs.length; j++){
					//Larin. ������� ��������, ���������� ������� �� ������ �������� ����������
					//if(contentDatas[i].matches("[^_&&[\\w+/]]*")){
						if (contentDataAttrs[j].indexOf("MI_CARD_ID_FIELD")>0){//link
							//������ �� ���� � �������
							URL url = new URL(codebase, contentDataAttrs[j]);
							InputStream stream = url.openStream();
							ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
							for(int b = stream.read(); b != -1; b = stream.read()) byteStream.write(b);
							contentDataAttrsBytes = ArrayUtils.addAll(contentDataAttrsBytes, byteStream.toByteArray());
						}else{
							//�������������� � Base64 ��������
							contentDataAttrsBytes =ArrayUtils.addAll(contentDataAttrsBytes, Base64.Base64semicolonDelimitedToByteArray(contentDataAttrs[j]));
						}
						
					}
					inData.add(contentDataAttrsBytes);	
				}
			}
			
			ret = signByteArray(cerStoreName, certName, inData, inHash, password);
			if (ret.get(0) == null) {
				retVal = (List<byte[][]>) ret.get(1);
				System.out.println("String signature: " + new String(retVal.get(0)[0]));
			} else {
				System.out.println("Error creating signature! " + ret.get(0).toString());
			}
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			return retVal;
		}
	}

	public boolean checkStringSignature(String inString,
			byte[] inSignature, Certificate inCert) {
		boolean retVal = false;
		byte[] stringBytes = null;

		try {
			if (inString == null)
				return retVal;
			stringBytes = inString.getBytes();
			final List ret = verifyByteArraySignature(stringBytes, inSignature,
					inCert);
			final String err = (String) ret.get(0);
			if (err == null) {
				if ("true".equals(ret.get(1))) {
					retVal = true;
				} else {
				}
			} else {
				System.out.println("Error verifying signature!!! " + err);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return retVal;
		}
	}

	@SuppressWarnings("finally")
	public byte[][] signLocalFile(String cerStoreName, String certName,
			String inFilePath, String password) {
		final File srcFile = new File(inFilePath);
		byte[][] retVal = null;
		List<byte[]> inData = new ArrayList<byte[]>();
		List ret = null;

		try {

			System.out.println("Creating signature...");
			byte[] bytes = AccessController
					.doPrivileged(new PrivilegedAction<byte[]>() {
						public byte[] run() {
							try {
								return getBytesFromFile(srcFile);
							} catch (Exception e) {
								e.printStackTrace();
								return null;
							}
						}
					});

			inData.add(bytes);
			ret = signByteArray(cerStoreName, certName, inData, null, password);
			if (ret.get(0) == null) {
				retVal = ((List<byte[][]>) ret.get(1)).get(0);
				System.out.println("String signature: " + retVal.toString());
			} else {
				System.out.println("Error creating signature!");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return retVal;
		}

	}

	@SuppressWarnings("finally")
	public boolean checkFileSignature(String inFilePath,
			byte[] inSignature, Certificate inCert) {
		boolean retVal = false;
		final File inFile = new File(inFilePath);
		byte[] fileBytes = null;

		try {
			if (!inFile.exists())
				return retVal;
			// fileBytes=Array.readFile(inFile);
			fileBytes = AccessController
					.doPrivileged(new PrivilegedAction<byte[]>() {
						public byte[] run() {
							try {
								return getBytesFromFile(inFile);
							} catch (Exception e) {
								e.printStackTrace();
								return null;
							}
						}
					});
			final List ret = verifyByteArraySignature(fileBytes, inSignature,
					inCert);

			final String err = (String) ret.get(0);
			if (err == null) {
				if ("true".equals(ret.get(1))) {
					retVal = true;
				} else {
				}
			} else {
				System.out.println("Error!!!");
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return retVal;
		}
	}

	@SuppressWarnings("finally")
	public boolean checkStreamSignature(InputStream inStream,
			byte[] inSignature, Certificate inCert, Integer streamSize) {
		boolean retVal = false;
		int bytesRead = 0;

		try {
			byte[] streamBytes = new byte[streamSize];
			bytesRead = inStream.read(streamBytes);
			System.out.print("Bytes read: ");
			//System.out.println(bytesRead);
			final List ret = verifyByteArraySignature(streamBytes, inSignature,
					inCert);

			final String err = (String) ret.get(0);
			if (err == null) {
				if ("true".equals(ret.get(1))) {
					System.out.println("The signature is true");
					retVal = true;
				} else {
					System.out.println("The signature is not true");
				}
			} else {
				System.out.println("Error!!!");
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return retVal;
		}
	}

	protected abstract boolean checkProvider() ;
	
	protected abstract List signByteArray(String ksName, String keyAlias,
			List<byte[]> inData, List<byte[]> inHash, String password);
	
	public abstract List verifyByteArraySignature(byte[] inData,
			byte[] inSignature, Certificate inCert);
	
	public abstract byte[] getByteArrayDigest(byte[] inByteArray);
	
	public byte[] getFileDigest(String inFilePath) {
		byte[] retVal = null;
		final File srcFile = new File(inFilePath);
		try {
			byte[] bytes = AccessController
					.doPrivileged(new PrivilegedAction<byte[]>() {
						public byte[] run() {
							try {
								return getBytesFromFile(srcFile);
							} catch (Exception e) {
								e.printStackTrace();
								return null;
							}
						}
					});
			retVal = getByteArrayDigest(bytes);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return retVal;
		}
	}

	public String getFileDigestString(String filePath) {
		String retVal = null;
		Base64 encoder = new Base64();

		try {
			retVal = Base64.byteArrayToBase64(getFileDigest(filePath));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return retVal;
		}
	}

	public byte[] getInputStreamDigest(InputStream inStream,
			Integer streamSize) {
		byte[] retVal = null;
		try {
			byte[] streamBytes = new byte[streamSize];
			int bytesRead = inStream.read(streamBytes);
			System.out.print("Bytes read: ");
			System.out.println(bytesRead);
			retVal = getByteArrayDigest(streamBytes);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return retVal;
		}
	}

	public String getInputStreamDigestString(InputStream inStream,
			Integer streamSize) {
		String retVal = null;
		Base64 encoder = new Base64();

		try {
			retVal = Base64.byteArrayToBase64(getInputStreamDigest(inStream,
					streamSize));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return retVal;
		}
	}

	public String hexify(byte bytes[]) {

		char[] hexDigits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'a', 'b', 'c', 'd', 'e', 'f' };

		StringBuffer buf = new StringBuffer(bytes.length * 2);

		for (int i = 0; i < bytes.length; ++i) {
			buf.append(hexDigits[(bytes[i] & 0xf0) >> 4]);
			buf.append(hexDigits[bytes[i] & 0x0f]);
		}

		return buf.toString();
	}

	public abstract String getCertDigestString(Certificate cert)
			throws NoSuchAlgorithmException, CertificateEncodingException;
		
	public abstract String getCertificateHash(String keyStorePath, String keyAlias, String password);
	/**
	 * ��������� ������� ������ ����� � ���������. ����� ������ ����������� 1, �� ������ �� ��� ������ ������������� ���������
	 * ��� ������ ������������� �� 1. ������� ����� ������ ����� � ���������
	 * @param ks
	 * @return
	 * @throws KeyStoreException 
	 */
	public String getKeyAlias(KeyStore ks) throws KeyStoreException{
		String result = null;
		Enumeration<String> aliasEnum = ks.aliases();  
		if (aliasEnum.hasMoreElements()){
			result = aliasEnum.nextElement();
		}
		return result;
	}	

	public Certificate getCertFromStore(String storeName, String alias) {
		Certificate retVal = null;
		try {
			final KeyStore hdImageStore = KeyStore.getInstance(storeName);
			hdImageStore.load(null, null);
			retVal = hdImageStore.getCertificate(alias);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return retVal;
		}
	}

	public PrivateKey getKeyFromStore(String storeName, String alias,
			char[] password) {
		PrivateKey retVal = null;
		try {
			final KeyStore hdImageStore = KeyStore.getInstance(storeName);
			hdImageStore.load(null, null);
			retVal = (PrivateKey) hdImageStore.getKey(alias, password);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return retVal;
		}
	}

	public final void copyInputStream(InputStream in, OutputStream out)
			throws IOException {
		byte[] buffer = new byte[1024];
		int len;

		while ((len = in.read(buffer)) >= 0)
			out.write(buffer, 0, len);

		in.close();
		out.close();
	}

	public CryptoObject getCryptoObjectFromFile(String inFileName) {
		CryptoObject retVal = null;
		String base64certString = null;
		try {
			if(isBas64Cert(inFileName)){
				base64certString = getCertStringFromFileBase64(inFileName);				
			}else{
				base64certString = getCertStringFromFile(inFileName);								
			}
			retVal = new CryptoObject(base64certString);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return retVal;
		}
	}	

	private String getCertStringFromFile(String inFileName) {
		String retVal = null;
		final String inCertFilePath = inFileName;
		try {
			retVal = AccessController
					.doPrivileged(new PrivilegedAction<String>() {
						@SuppressWarnings("finally")
						public String run() {
							String certString = null;
							ByteArrayOutputStream byteArray = null;
							FileInputStream fileReader = null;
							try {
								File certFile = new File(inCertFilePath);
								byteArray = new ByteArrayOutputStream();
								if (!certFile.exists())
									return null;
								else {
									fileReader = new FileInputStream(
											certFile);
									byte[] buf = new byte[512];
									int size = 0;
									while ((size = fileReader.read(buf))>0) {
										byteArray.write(buf, 0, size);
									}
								}
								//��������� � base64
								certString = Base64.byteArrayToBase64(byteArray.toByteArray());
							} catch (Exception e) {
								e.printStackTrace();
							} finally {
								try{
									byteArray.close();
									fileReader.close();
								}catch(Exception ignoreEx){}
								return certString;
							}
						}
					});

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return retVal;
		}
	}

	/**
	 * ��������� ��������� �����������
	 * @param inFileName
	 * @return
	 */
	private boolean isBas64Cert(String inFileName){
		boolean retVal = false;
		final String inCertFilePath = inFileName;
		try {
			retVal = AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
				public Boolean run() {
					boolean result = false;
					FileReader fileReader = null;
					LineNumberReader lineReader = null;
					try {
						File certFile = new File(inCertFilePath);
						if (certFile.exists()){
							final String beginString = "BEGIN CERTIFICATE";
							fileReader = new FileReader(
									certFile);
							lineReader = new LineNumberReader(
									fileReader);
							String lineString = null;
							lineString = lineReader.readLine();
							if (lineString.indexOf(beginString)>-1){
								result = true;
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						try{
							fileReader.close();
							lineReader.close();
						}catch(Exception ignoreEx){}
					}
					return result;
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return retVal;
		}
		
		
	}
	
	public CryptoObject getCryptoObjectFromFileBase64(String inFileName) {
		CryptoObject retVal = null;
		String base64certString = null;
		try {
			base64certString = getCertStringFromFileBase64(inFileName);
			retVal = new CryptoObject(base64certString);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return retVal;
		}
	}

	private static void loadConfig(ConfigService configService) throws Exception {
		if(m_config == null) {		
			InputStream is = null;
			try {
				is = configService.loadConfigFile(CONFIG_FILE);
				m_config = new Properties();
				m_config.load(is);		
			}catch(Exception ex){
				ex.printStackTrace();
				m_config = null;
			}finally {
				is.close();
			}
		}
	}

	public static String getConfigParam(String configName){
		return m_config.getProperty(configName);
	}

	protected String getLayerParam(String paramName){
		String result = null;
		
		String[] params = m_layaerParams.split(";");
		for(int i=0; i<params.length; i++){
			String[] param = params[i].split("=");
			if (param[0].trim().equalsIgnoreCase(paramName)){
				result = param[1].trim();
				break;
			}
		}
		return result;
	}

	protected int getIntLayaerParam(String paramName){
		String paramValueAsString = getLayerParam(paramName);
		return Integer.parseInt(paramValueAsString);
	}

	protected boolean getBoolLayaerParam(String paramName){
		String paramValueAsString = getLayerParam(paramName);
		if (paramValueAsString == null){
			paramValueAsString = "false";
		}
		return Boolean.parseBoolean(paramValueAsString);
	}	

	public boolean isHidePasswordField(){
		return getBoolLayaerParam(HIDE_PASSWORD_FIELD);
	}
	
	public boolean isHideContainerField(){
		return getBoolLayaerParam(HIDE_CONTEINER_FIELD);		
	}

	public PKCS7Result verifyPKCS7(byte[] pkcs7, byte[] data) throws Exception{
		PKCS7Verifier verifier = getPKCS7Verifier();
		PKCS7SignerInfo[] result = verifier.verify(pkcs7, null, data);
		PKCS7SignerInfo singleResult = null;
		for (int i = 0; i < result.length; i++) {		
				singleResult = result[i];
				pkcs7SignerCertificate = result[i].getCertificate(); 
				if (result[i].isValid()) break;		
		}
		
		PKCS7Result pkcs7Result = new PKCS7Result();
		pkcs7Result.setValid(singleResult.isValid());
		pkcs7Result.setCert(pkcs7SignerCertificate);
		if (singleResult != null){
			pkcs7Result.setTime(singleResult.getTime());
		}
		return pkcs7Result;
	}

	
	private PKCS7Verifier getPKCS7Verifier() throws Exception{
		String className = getLayerParam("PKCS7_VERIFIER");
		if (className == null){
			throw new Exception ("criptoLayer param PKCS7Verifier not set");
		}
		Class classObject = Class.forName(className);
		PKCS7Verifier result = (PKCS7Verifier) classObject.newInstance();
		return result;		
	}

	public Certificate getPkcs7SignerCertificate() {
		return pkcs7SignerCertificate;
	}
	
	/**
	 * ������������ ������ ����������� ������.
	 * @return ������ �������� ����������� ������ (null � ������ ������)
	 */
	public abstract List<String> getKeyStoreList();

}
