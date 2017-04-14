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

import java.applet.Applet;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
//import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
//import java.net.UnknownHostException;
import java.security.AccessController;
import java.security.AllPermission;
import java.security.Permission;
import java.security.PrivilegedAction;
import java.security.cert.Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import netscape.javascript.JSObject;

//import org.apache.commons.net.ntp.NTPUDPClient;

//import java.util.Hashtable;

@SuppressWarnings("serial")
public class CryptoApplet extends Applet {
	public static final String CURENT_USER_PARAMETER = "curent.user";
	
	private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	//private static NTPUDPClient nTPClient = new NTPUDPClient();	
	//private InetAddress hostAddr = null;

	private String keystore = null;
	private String keystorePassword = null;
	private String currentUser = null;
	
	@Override
	public void init() {
		System.out.println("Loaded JB CryptoApplet!");
		
		String javascriptCode = "window.checkPermission = "+ this.checkPermissions();
		JSObject win = (JSObject) JSObject.getWindow(this);
		System.out.println(javascriptCode);
		win.eval(javascriptCode);
		

		String signOnLoad = getParameter("signOnLoad");
		currentUser = getParameter(CURENT_USER_PARAMETER);

		// ������������� CryptoLayer
		String cryptoLayerClass = getParameter("crypto.layer");
		String cryptoLayerParams = getParameter("crypto.layer.params");
		
		/*
		String serverNTPAddress = getParameter("timestamp.address");
		try{
			hostAddr = InetAddress.getByName(serverNTPAddress);	
		} catch (UnknownHostException e){System.out.println("NTP client initialization fail. Reason: " + e.getMessage()); e.printStackTrace();}
		nTPClient.setDefaultTimeout(5000);
		*/
		
		CryptoLayer.getInstance(cryptoLayerClass, cryptoLayerParams);
		CryptoLayer.getInstance().setCodebase(getCodeBase());
		if (null != signOnLoad && Boolean.TRUE.toString().equals(signOnLoad)) {
			try {
				getAppletContext().showDocument(
						new URL("javascript:getSignature()"));
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		win.eval("if (typeof signAction == 'function'){signAction();}");
		// ��� �������
		/*
		 * String fileName = getFileName(); CryptoObject co =
		 * getCryptoObjectFromFile(fileName);
		 * System.out.println(co.getBase64Cert());
		 * System.out.println(co.getCertHash());
		 * System.out.println(co.getSubject());
		 */
		
	}

	/**
	 * 
	 * @param content
	 *            Type ��� ��������. 2 - ����, 1 - ������ �����
	 * @param certName
	 *            ��� �����������
	 * @param contentString
	 *            ������������� �������
	 * @param savedKeystore
	 *            ���� ��� ��� ��������� ��������� ������
	 * @param savedKeystorePassword
	 *            ������ � ���������, ������������ ������� � JCP
	 * @param certHash
	 *            hash ����������� ������������, ������� ����������� ��������
	 * @return
	 */
	public String[] getSignature(int contentType, String certName,
			String contentData, String contentHash, String savedKeystore,
			String savedKeystorePassword, String certHash) {
		String retVal[] = null;
		String keystore = null;
		String keystorePassword = null;

		System.out.println(contentType + " - " + certName + " - " + contentHash
				+ " - " + savedKeystore + " - " + savedKeystorePassword + " - " + certHash);
		System.out.println("Data length: " + contentData.length());

		/*
		 * ������������ �������� ������ � keystore ��� JCP. ������ ������
		 * �������� � ��������� base64 � �������� windows.name dom ������
		 * ��������. ���� ������ ��������� ������ �� ������� � �������� �������
		 * �� �������. � ������ �������� ������������� ������ �������� ��
		 * ��������� ����� �� ��������� ������ � ������������� ����. ���� ���
		 * ������������� ������ ����������� �� http ��������� � �������� ���� �
		 * ������� base64, ��� ������ �������� ������ �������� ������. ����
		 * ������������ �������� �� ���� 100 �������� ������������ ��������
		 * ������ �� ����� ����� ���������� � �������������? ����� ������ �����
		 * �� ������ ������ � �������� ������� � ������ "�����" ������ ����
		 * ������ �������. ���� � keystore �������� � �����, ��� ��� ��
		 * ������������ ������� �������� ��� ������.
		 */
		List storeList = CryptoLayer.getInstance().getKeyStoreList();
		if(userChanged(savedKeystore)||!storeList.contains(getStoredSubstring(savedKeystore))){
			String ans = "";
			Object[] stores = storeList.toArray();
			if(stores.length == 1){
				ans = (String)stores[0];
			} else {
				ans = (String)JOptionPane.showInputDialog(new JFrame(), "��������", "����� ���������� ������", 
						JOptionPane.PLAIN_MESSAGE, null, stores, stores[0]);
			}
			System.out.println("��������� ���������: " + ans);
			keystore = ans;
		} else {
			keystore = getStoredSubstring(savedKeystore);
			System.out.println("����������� ���������: " + savedKeystore);
		}
		if (keystore == null || keystore.length() == 0){
			return new String[] { "", "" };
		}
			
		if((savedKeystorePassword == null || savedKeystorePassword.length() == 0)&& !CryptoLayer.getInstance().isHidePasswordField()){
			CryptoParamsDialog dlg = new CryptoParamsDialog(new JFrame(),"��������� ��������� �����",
															userChanged(savedKeystore) ? ""	: getStoredSubstring(savedKeystore), 
															CryptoLayer.getInstance().isHidePasswordField(),
															CryptoLayer.getInstance().isHideContainerField()
															);
			if (dlg.isCancelled()) {
				return new String[] { "", "" };
			}
			keystorePassword = new String(dlg.getPassword());
		} else {
			// �������� ��� ������������ �� ���������
			
			keystorePassword = new String(
					Base64.base64ToByteArray(getStoredSubstring(savedKeystorePassword)));
		}

		// �������� ������������ ����� � �����������,
		// ������� ��������� � �������� �������
		String privateCertHash = CryptoLayer.getInstance().getCertificateHash(
				keystore, certName, String.valueOf(keystorePassword));
		System.out.println("privateCertHash = " + privateCertHash);

		if (privateCertHash != null) {
			if (privateCertHash.equals(certHash) == false) {
				return new String[] {
						"@error ��������� ���� �� ������������� ����������� ������������!",
						"" };
			}
		} else {
			return new String[] {
					"@error ������ �������� ��������� ��� ������!", "" };
		}

		// �������
		if (contentType == 1) {
			// ������� ������
			// ������������ ��������� � Base64
			// ���������� �� ����� �������
			/*
			 * String t[] = contentData.split(";"); StringBuffer b = new
			 * StringBuffer(); for(int i = 0; i < t.length; i++) { if(i > 0)
			 * b.append(";");
			 * b.append(Base64.byteArrayToBase64(t[i].getBytes())); }
			 */
			retVal = CryptoLayer.getInstance().getStringContentSignature(
					keystore, certName, contentData, contentHash,
					keystorePassword);
		} else {
			// ������� �����
			contentData = getFileDigestString(contentData);
			retVal = CryptoLayer.getInstance().getStringContentSignature(
					keystore, certName, contentData, contentHash,
					keystorePassword);
		}
		if (retVal != null) {
			System.out.println("String content signature is: " + retVal[0]);
			System.out.println("String content pkcs7 is: " + retVal[1]);
			// ���� keystorePassword �� �����, �������� ��� CryptoAPI ��
			// ��������� ��� ����������� ������� blank_password ����� ����������
			// ������� �� ������ � ���� ����� ��� ���
			if (keystorePassword == null || keystorePassword.length() == 0) {
				keystorePassword = "blank_password";
			}
		}

		// ��������� ������ � ���� � ����� � ���������� ������ ������. ����� ���
		// ����� ����� �� ������ java �������� ��� ���������� � ����� �
		// ���������� windows.name
		this.setKeystore(currentUser + ":" + keystore);
		this.setKeystorePassword(currentUser + ":"
				+ Base64.byteArrayToBase64(keystorePassword.getBytes()));

		return retVal;
	}

	private String getStoredSubstring(String storedString) {
		String result = "";
		if (storedString != null && storedString.length() > 0
				&& storedString.indexOf(":") > 0) {
			result = storedString.substring(storedString.indexOf(":") + 1);
		}
		return result;
	}

	private boolean userChanged(String keystore) {
		boolean result = false;
		if (keystore != null){
			result = !keystore.startsWith(currentUser);
		}else{
			result = true;
		}
		return result; 
	}

	public Certificate getCertFromFile(String filePath) {
		Certificate retVal = null;
		retVal = CryptoLayer.getInstance().getCertFromFile(filePath);
		return retVal;
	}

	public String[] getStringContentSignature(String cerStoreName,
			String certName, String contentData, String contentHash,
			String password) {
		String retVal[] = null;
		System.out.println(cerStoreName + " - " + certName + " - "
				+ contentHash);
		retVal = CryptoLayer.getInstance().getStringContentSignature(
				cerStoreName, certName, contentData, contentHash, password);
		if (retVal != null) {
			System.out.println("String content signature is: " + retVal);
		}
		return retVal;
	}

	public boolean checkStringContentSignature(String inString,
			String inSignature, Certificate inCert) {
		boolean retVal = false;
		retVal = CryptoLayer.getInstance().checkStringContentSignature(
				inString, inSignature, inCert);
		return retVal;
	}

	public String[] getLocalFileSignature(String cerStoreName, String certName,
			String filePath, String password) {
		String retVal[] = null;
		retVal = CryptoLayer.getInstance().getLocalFileSignature(cerStoreName,
				certName, filePath, password);
		System.out.println("Local file signature is: " + retVal);
		return retVal;
	}

	public boolean checkLocalFileSignature(String filePath, String inSignature,
			Certificate inCert) {
		boolean retVal = false;

		retVal = CryptoLayer.getInstance().checkLocalFileSignature(filePath,
				inSignature, inCert);
		return retVal;
	}

	@SuppressWarnings("unchecked")
	public String getFileName(String prompt) {
		final String uiPrompt = prompt;

		String retVal = (String) AccessController
				.doPrivileged(new PrivilegedAction() {
					public Object run() {
						String fileName = "";
						JFileChooser choose = new JFileChooser();

						UIManager.put("FileChooser.fileNameLabelText",
								"��� �����:");
						UIManager.put("FileChooser.folderNameLabelText",
								"��� �����:");
						UIManager.put("FileChooser.lookInLabelText", "�����:");
						UIManager.put("FileChooser.filesOfTypeLabelText",
								"���:");
						UIManager.put("FileChooser.cancelButtonText", "������");
						UIManager.put("FileChooser.openButtonText", "�������");
						choose.updateUI();
						choose.setDialogTitle(uiPrompt);
						choose.setFileSelectionMode(JFileChooser.FILES_ONLY);
						choose.setAcceptAllFileFilterUsed(false);
						int result = choose.showOpenDialog(null);

						if (result == JFileChooser.APPROVE_OPTION) {
							File file = choose.getSelectedFile();
							fileName = file.getAbsolutePath().toString();
						}
						return fileName;
					}
				});

		return retVal;
	}

	@SuppressWarnings("unchecked")
	public String getFileName() {
		return getFileName("�������� ����");
	}

	public String postMaterialCard(String targetURL, String cookies,
			String filePath, String fileSignature, String cardId) {
		String retVal = "true";

		String[] cookiesArr = null;
		String cookie = null;
		String cookieVal[] = new String[2];
		PostHTTPRequest postRequest = null;
		File inFile = null;
		InputStream result = null;
		URL trgUrl = null;
		String cookieName = "";
		try {
			// Creating new request
			System.out.println("Target URL is: " + targetURL);
			trgUrl = new URL(targetURL);
			postRequest = new PostHTTPRequest(trgUrl);

			// Parsing kookies
			cookiesArr = cookies.split(";");
			for (int i = 0; i < cookiesArr.length; i++) {
				cookie = cookiesArr[i];
				System.out.println("Coockie: " + cookie);
				if (!cookie.endsWith("=")) {
					cookieVal = cookie.split("=");
					try {
						System.out.println("Cookiename: " + cookieVal[0]
								+ " cookieval: " + cookieVal[1]);
						postRequest.setCookie(cookieVal[0], cookieVal[1]);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				} else {
					cookieName = cookie.substring(0, cookie.length() - 1);
					postRequest.setCookie(cookieName, "");
				}
			}

			final String pathToFile = filePath;
			inFile = AccessController
					.doPrivileged(new PrivilegedAction<File>() {
						public File run() {
							try {
								return new File(pathToFile);
							} catch (Exception e) {
								e.printStackTrace();
								return null;
							}
						}
					});

			postRequest.setParameter("path", inFile);
			postRequest.setParameter("fileSignature", fileSignature);
			postRequest.setParameter("cardId", cardId);
			result = postRequest.post();
			retVal = readStream(result);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return retVal;
	}

	public boolean postMaterialCard(String targetURL, String cookies,
			String filePath, String fileSignature) {
		boolean retVal = true;

		String[] cookiesArr = null;
		String cookie = null;
		String cookieVal[] = new String[2];
		PostHTTPRequest postRequest = null;
		File inFile = null;
		InputStream result = null;
		URL trgUrl = null;
		String cookieName = "";
		try {
			// Creating new request
			System.out.println("Target URL is: " + targetURL);
			trgUrl = new URL(targetURL);
			postRequest = new PostHTTPRequest(trgUrl);

			// Parsing kookies
			cookiesArr = cookies.split(";");
			for (int i = 0; i < cookiesArr.length; i++) {
				cookie = cookiesArr[i];
				System.out.println("Coockie: " + cookie);
				if (!cookie.endsWith("=")) {
					cookieVal = cookie.split("=");
					try {
						System.out.println("Cookiename: " + cookieVal[0]
								+ " cookieval: " + cookieVal[1]);
						postRequest.setCookie(cookieVal[0], cookieVal[1]);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				} else {
					cookieName = cookie.substring(0, cookie.length() - 1);
					postRequest.setCookie(cookieName, "");
				}
			}

			final String pathToFile = filePath;
			inFile = AccessController
					.doPrivileged(new PrivilegedAction<File>() {
						public File run() {
							try {
								return new File(pathToFile);
							} catch (Exception e) {
								e.printStackTrace();
								return null;
							}
						}
					});

			postRequest.setParameter("path", inFile);
			postRequest.setParameter("fileSignature", fileSignature);
			result = postRequest.post();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return retVal;
		}
	}

	private String readStream(InputStream is) throws IOException {
		if (null == is) {
			return null;
		}

		StringWriter writer = new StringWriter();
		char[] buffer = new char[1024];

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					is, "UTF-8"));
			int n;
			while ((n = reader.read(buffer)) != -1) {
				writer.write(buffer, 0, n);
			}
		} finally {
			is.close();
		}

		return writer.toString();
	}

	public String getFileDigestString(String filePath) {
		String retVal = null;
		retVal = CryptoLayer.getInstance().getFileDigestString(filePath);
		System.out.println("File digest is: " + retVal);
		return retVal;
	}

	public String getCertHash(String cerStoreName, String certName,
			String password) {
		String retVal = null;
		try {
			retVal = CryptoLayer.getInstance().getCertificateHash(cerStoreName,
					certName, password);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return retVal;
		}
	}

	public CryptoObject getCryptoObjectFromFile(String filePath) {
		CryptoObject retVal = null;
		try {
			retVal = CryptoLayer.getInstance()
					.getCryptoObjectFromFile(filePath);
			System.out.println(retVal.getBase64Cert());
			System.out.println(retVal.getCertHash());
			System.out.println(retVal.getSubject());
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			return retVal;
		}
	}
	
	private boolean checkPermissions (){
		SecurityManager sManager = System.getSecurityManager();
		try{
			sManager.checkPermission(new AllPermission());
		}
		catch (SecurityException e) {
			return false;
		}
		return true;
	}

	public void setKeystore(String keystore) {
		this.keystore = keystore;
	}

	public String getKeystore() {
		return keystore;
	}

	public void setKeystorePassword(String keystorePassword) {
		this.keystorePassword = keystorePassword;
	}

	public String getKeystorePassword() {
		return keystorePassword;
	}
	
	public String getTime(){
		return format.format(new Date());
	}

	/*
	public String getNTPTime(){
		Date date = null;
		try{
			nTPClient.open();
			date = new Date(nTPClient.getTime(hostAddr).getReturnTime());
		} catch(Exception e){
			System.out.println("Unable to get time from NTP Server \"" + hostAddr + "\". Reason: " + e.getMessage() + "\nThe local date will be used."); 
			e.printStackTrace();
			date = new Date();
		}
		finally{
			if (nTPClient.isOpen()) nTPClient.close();
		}
		return format.format(date);
	}
	*/
}
