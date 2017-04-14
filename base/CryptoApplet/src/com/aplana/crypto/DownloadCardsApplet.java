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
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

public class DownloadCardsApplet extends Applet {
    private static final long serialVersionUID = 1L;

    public final static String PARAM_CARD_ID = "cardId";
    public final static String PARAM_RECIPIENT_ID = "recipientId";
    public final static String ERROR_HEADER_NAME = "import-error";

    public final static String FILE_PART_NAME = "file";
    public final static String FILE_NAME_PART_NAME = "uploadfilename";
    public final static String PARAM_ACTION_ID = "actionid";
    public static final String PARAM_NAMESPACE = "namespace";
    public static final String PARAM_ATTACH_ID = "attachId";
    public static final String PARAM_ONLY_SIGNERS = "onlySigners";

    public static String NAME_OF_FILE = "��� �����";
    public static String NAME_OF_FOLDER = "��� �����";
    public static String FOLDER = "�����";
    public static String TYPE = "���";
    public static String CANCEL = "������";
    public static String SELECT = "�������";

    private static final String MESSAGES_NAME = "com.aplana.crypto.nls.messages";

    private String contextUrl;
    private String namespace;
    private String workingDirectory;

	//���������� ��� ����, ��� �� ��������� ���������� ������ ������������
	private String errorMessage;

    public String getContextUrl() {
	return this.contextUrl;
    }

    public void setContextUrl(String contextUrl) {
	this.contextUrl = contextUrl;
    }

    public String getNamespace() {
	return this.namespace;
    }

    public void setNamespace(String namespace) {
	this.namespace = namespace;
    }

    public String getWorkingDirectory() {
	return this.workingDirectory;
    }

    public void setWorkingDirectory(String workingDirectory) {
	this.workingDirectory = workingDirectory;
    }

    @Override
    public void init() {
	logMessage("Initialization of DownloadCardsApplet is started");
	NAME_OF_FILE = getParameter("download.applet.file.name");
	NAME_OF_FOLDER = getParameter("download.applet.folder.name");
	FOLDER = getParameter("download.applet.folder");
	TYPE = getParameter("download.applet.type");
	CANCEL = getParameter("download.applet.cancel");
	SELECT = getParameter("download.applet.select");
	logMessage("Initialization of DownloadCardsApplet is finished");
    }

    public void setLocale(String language, String country) {
	setLocale(new Locale(language, country));
	AccessController.doPrivileged(new PrivilegedAction<Void>() {
	    public Void run() {
		Locale.setDefault(getLocale());
		return null;
	    }
	});
    }

    public String selectFolder(final String prompt) throws AppletException {
		logMessage("Select folder operation is started");
		PrivilegedAction<String> selectFolderAction = new PrivilegedAction<String>() {
			public String run() {
			UIManager.put("FileChooser.fileNameLabelText", NAME_OF_FILE
				+ ":");
			UIManager.put("FileChooser.folderNameLabelText", NAME_OF_FOLDER
				+ ":");
			UIManager.put("FileChooser.lookInLabelText", FOLDER + ":");
			UIManager.put("FileChooser.filesOfTypeLabelText", TYPE + ":");
			UIManager.put("FileChooser.cancelButtonText", CANCEL);
			UIManager.put("FileChooser.openButtonText", SELECT);
			JFileChooser chooser = new JFileChooser();
			chooser.updateUI();
			chooser.setDialogTitle(prompt);
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooser.setAcceptAllFileFilterUsed(false);
			int result = chooser.showOpenDialog(null);

			if (result == JFileChooser.APPROVE_OPTION) {
				File file = chooser.getSelectedFile();
				logMessage("File is chosen " + file.getAbsolutePath());
				return file.getAbsolutePath();
			}
			logMessage("Selection is cancelled");
			return null;
			}
		};

		try {
			try {
				return AccessController.doPrivileged(selectFolderAction);
			} catch (AppletException ex) {
				throw ex;
			} catch (RuntimeException ex) {
				logError("Error during folder selection is occurred", ex);
				throw new AppletException(ex);
			}
		} catch (RuntimeException e) {
			setErrorMessage(e.getMessage());
			throw e;
		}
	}

	public void exportCardToFile(final String cardId, final String recipientId, final String fileName) throws AppletException {
		logMessage("Export card to file operation is started");
		try {
			try {
				String decodedFileName = URLDecoder.decode(fileName, "UTF-8");
				downloadCardToFile(cardId, recipientId, decodedFileName);
			} catch (IOException ex) {
				logError("Exception during export for (" + cardId + ", "
						+ recipientId + ") in file " + fileName, ex);
				throw new AppletException("cards.export", ex, cardId, fileName);
			} catch (AppletException ex) {
				throw ex;
			} catch (RuntimeException ex) {
				logError("Error during export card", ex);
				throw new AppletException(ex);
			}
		} catch (RuntimeException e) {
			setErrorMessage(e.getMessage());
			throw e;
		}
    }

    protected void downloadCardToFile(String cardId, String recipientId,
	    String fileName) throws IOException {
	logMessage("Request server for export card");
	File file = new File(workingDirectory, fileName);
	Map<String, String> parameters = new TreeMap<String, String>();
	parameters.put(PARAM_CARD_ID, cardId);
	parameters.put(PARAM_RECIPIENT_ID, recipientId);
	URL url = generateUrl("downloadCard", parameters);
	logMessage("URL: " + url.toExternalForm() + "(" + url.toString() + ")");
	GetHttpRequest request = new GetHttpRequest(url);
	InputStream inputStream = null;
	try {
	    inputStream = request.get();
	} catch (IOException ex) {
	    throw new AppletException("cards.servlet");
	}
	logMessage("Write file to " + file.getAbsolutePath());
	writeStreamToFile(inputStream, file);
	downloadFilesFromCard(request);
    }

    protected void downloadFilesFromCard(GetHttpRequest request)
	    throws IOException {
	String result = request.getHeaderField("fileCards");
	if (result == null || "".equals(result)) {
	    return;
	}
	String[] filesToBeDownloaded = result.split(",");
	for (String fileToBeDownloaded : filesToBeDownloaded) {
	    String[] fileToBeDownloadedParts = fileToBeDownloaded.split("=");
	    if (fileToBeDownloadedParts.length != 2) {
		throw new IllegalStateException(fileToBeDownloaded
			+ " should be key-value pair: 'key=value'");
	    }
	    long fileCardId = Long.parseLong(fileToBeDownloadedParts[0]);
	    String filePath;
	    try {
		filePath = URLDecoder.decode(fileToBeDownloadedParts[1],
			"UTF-8");
	    } catch (UnsupportedEncodingException ex) {
		throw new IllegalStateException(ex);
	    }

	    downloadFile(fileCardId, filePath);
	}
    }

    public void downloadFile(long fileCardId, String filePath)
	    throws IOException {
	URL url = new URL(contextUrl
		+ "/MaterialDownloadServlet?MI_CARD_ID_FIELD=" + fileCardId);
	GetHttpRequest request = new GetHttpRequest(url);
	InputStream inputStream = null;
	try {
	    inputStream = request.get();
	} catch (IOException ex) {
	    throw new AppletException("cards.servlet");
	}
	writeStreamToFile(inputStream, new File(workingDirectory, filePath));
    }

    private void writeStreamToFile(final InputStream inputStream,
	    final File file) {
	PrivilegedAction<Void> writeFileAction = new PrivilegedAction<Void>() {
	    public Void run() {
		new File(workingDirectory).mkdirs();
		if (file.exists()) {
		    if (!confirmRewriteFile(file)) {
			return null;
		    }
		    logMessage("File " + file.getAbsolutePath()
			    + " will be rewritten");
		}

		try {
		    FileOutputStream fileStream = new FileOutputStream(file);
		    copyData(inputStream, fileStream);
		    return null;
		} catch (IOException e) {
		    throw new AppletException("exported.card.writeToFile", e,
			    file.getAbsolutePath());
		}
	    }

	    private boolean confirmRewriteFile(File f) {
		ResourceBundle messages = ResourceBundle
			.getBundle(MESSAGES_NAME);
		String confirmationText = MessageFormat.format(messages
			.getString("confirmation.text"), f.getAbsolutePath());
		String confirmationTitle = messages
			.getString("confirmation.title");
		return JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(
			null, confirmationText, confirmationTitle,
			JOptionPane.YES_NO_OPTION);
	    }
	};
	AccessController.doPrivileged(writeFileAction);
    }

    protected void copyData(InputStream in, OutputStream out)
	    throws IOException {
	try {
	    byte[] buffer = new byte[4 * 1024];
	    int read;
	    while ((read = in.read(buffer)) != -1) {
		out.write(buffer, 0, read);
	    }
	    out.flush();
	} finally {
	    if (null != out) {
		try {
		    out.close();
		} catch (IOException e) {
		    logError("Error during close file stream", e);
		}
	    }
	}

    }

    public String uploadFilesToCards(final String uploadExtensions) {
	logMessage("Cards import is started");
	PrivilegedAction<String> importCardsAction = new PrivilegedAction<String>() {

	    private StringBuilder importedCards = new StringBuilder();

	    public String run() {
		File[] files = searchFilesInWorkingDirectoryEndsWith(uploadExtensions);
		for (File file : files) {
		    try {
			InputStream result = importXmlToCard(file);
			String importResult = readDataFromStream(result,
				"UTF-8");
			importResult = processImportResult(importResult);
			appendCardResult(importResult);
		    } catch (IOException ex) {
			logError("Exception during upload file to card", ex);
			throw new AppletException("cards.import", ex, file
				.getAbsolutePath());
		    }
		}
		return getCardResult();
	    }

	    private String processImportResult(String result)
		    throws IOException {
		if (result == null || "".equals(result)) {
		    return "";
		}
		String[] mainParts = result.split("\\?");
		String cardId = "";
		if (mainParts.length > 0) {
		    cardId = mainParts[0];
		}
		String[] filesToBeUploaded = {};
		if (mainParts.length > 1 && !"".equals(mainParts[1])) {
		    filesToBeUploaded = mainParts[1].split("&");
		}
		StringBuilder errors = new StringBuilder();
		for (String fileToBeUploaded : filesToBeUploaded) {
		    String[] fileToBeUploadedParts = fileToBeUploaded
			    .split("=");
		    if (fileToBeUploadedParts.length != 2) {
			throw new IllegalStateException(fileToBeUploaded
				+ " should be key-value pair: 'key=value'");
		    }
		    long fileCardId = Long.parseLong(fileToBeUploadedParts[0]);
		    String filePath;
		    try {
			filePath = URLDecoder.decode(fileToBeUploadedParts[1],
				"UTF-8");
		    } catch (UnsupportedEncodingException ex) {
			throw new IllegalStateException(ex);
		    }
		    try {
			uploadFileToCard(fileCardId, filePath);
		    } catch (AppletException ex) {
			if (errors.length() > 0) {
			    errors.append(";");
			}
			errors.append(String.format("(%s,%s):", fileCardId,
				filePath));
			errors.append(ex.getMessage());
		    }
		}
		if (errors.length() > 0) {
		    throw new AppletException("upload.fileExceptions", errors
			    .toString());
		}
		return cardId;
	    }

	    private void uploadFileToCard(long cardId, String fileName)
		    throws IOException {
		File file = resolveFile(fileName);
		logMessage("Request server (post to) for upload file "
			+ file.getAbsolutePath());
		if (!file.exists()) {
		    throw new AppletException("upload.fileDoesNotExist", file
			    .getAbsolutePath());
		}
		Map<String, String> parameters = new TreeMap<String, String>();
		parameters.put(PARAM_CARD_ID, String.valueOf(cardId));
		URL url = generateUrl("fileUpload", parameters);
		URLConnection connection = url.openConnection();
		connection.setUseCaches(false);
		connection.setDefaultUseCaches(false);
		PostHTTPRequest request = new PostHTTPRequest(connection);
		String encodedFileName = URLEncoder.encode(file.getName(),
			"UTF-8");
		request.setParameter(FILE_NAME_PART_NAME, encodedFileName);
		request.setParameter(FILE_PART_NAME, file);
		try {
		    request.post();
		} catch (IOException ex) {
		    throw new AppletException("cards.servlet");
		}
	    }

	    private void appendCardResult(String card) {
		if (importedCards.length() > 0) {
		    importedCards.append(",");
		}
		importedCards.append(card);
	    }

	    private String getCardResult() {
		return importedCards.toString();
	    }
	};

	try {
	    return AccessController.doPrivileged(importCardsAction);
	} catch (AppletException ex) {
	    throw ex;
	} catch (RuntimeException ex) {
	    logError("Error during cards import", ex);
	    throw new AppletException(ex);
	}
    }

    protected String readDataFromStream(InputStream result, String charsetName) {
	try {
	    BufferedReader reader = new BufferedReader(new InputStreamReader(
		    result, charsetName));
	    StringBuilder resultBuilder = new StringBuilder();
	    String str;
	    while ((str = reader.readLine()) != null) {
		if (resultBuilder.length() > 0) {
		    resultBuilder.append("\n");
		}
		resultBuilder.append(str);
	    }
	    return resultBuilder.toString();
	} catch (IOException ex) {
	    throw new AppletException("import.response.write", ex);
	}
    }

    protected File[] searchFilesInWorkingDirectoryEndsWith(
	    final String fileEnding) {
	logMessage("Search files ends with " + fileEnding + " in "
		+ workingDirectory);
	final File rootDirectory = new File(workingDirectory);
	File[] files = rootDirectory.listFiles(new FileFilter() {
	    public boolean accept(File file) {
		return !file.isDirectory()
			&& file.getName().toUpperCase().endsWith(
				fileEnding.toUpperCase());
	    }
	});

	if (files == null) {
	    files = new File[0];
	}
	logMessage("There were found " + files.length + " files");
	return files;
    }

    protected File resolveFile(String fileName) {
	File file = new File(fileName);
	if (!file.isAbsolute()) {
	    file = new File(workingDirectory, fileName);
	}
	return file;
    }

    protected URL generateUrl(String actionId, Map<String, String> parameters) {
	try {
	    StringBuilder urlBuilder = new StringBuilder(contextUrl
		    + "/FileCardServlet?" + PARAM_ACTION_ID + "=" + actionId
		    + "&" + PARAM_NAMESPACE + "=" + namespace);
	    if (parameters != null) {
		for (Entry<String, String> parameter : parameters.entrySet()) {
		    urlBuilder.append(String.format("&%s=%s", parameter
			    .getKey(), parameter.getValue()));
		}
	    }
	    return new URL(urlBuilder.toString());
	} catch (MalformedURLException ex) {
	    throw new IllegalStateException(ex);
	}
    }

    protected InputStream importXmlToCard(File file) throws IOException {
	logMessage("Request server (post to) for import cards");
	URL url = generateUrl("uploadCard", null);
	URLConnection connection = url.openConnection();
	connection.setUseCaches(false);
	connection.setDefaultUseCaches(false);
	PostHTTPRequest request = new PostHTTPRequest(connection);
	request.setParameter(FILE_NAME_PART_NAME, URLEncoder.encode(file
		.getName(), "UTF-8"));
	request.setParameter(FILE_PART_NAME, file);
	try {
	    return request.post();
	} catch (IOException ex) {
	    throw new AppletException("cards.servlet");
	}
    }

    protected static void logMessage(String message) {
	System.out.println("applet: " + message);
    }

    protected static void logError(String message, Throwable throwable) {
	System.err.println("applet: " + message);
	throwable.printStackTrace();
    }

    public void writeHistory(long fileCardId) throws IOException {
	Map<String, String> parameters = new TreeMap<String, String>();
	parameters.put(PARAM_CARD_ID, String.valueOf(fileCardId));
	URL url = generateUrl("writeHistory", parameters);
	URLConnection connection = url.openConnection();
	connection.setUseCaches(false);
	connection.setDefaultUseCaches(false);
	PostHTTPRequest request = new PostHTTPRequest(connection);
	try {
	    request.post();
	} catch (IOException ex) {
	    throw new AppletException("cards.servlet");
	}
    }

    @SuppressWarnings("unchecked")
    public void downloadSignatures(long cardId, long fileCardId,
	    String signersOnly) throws IOException {
	Map<String, String> parameters = new TreeMap<String, String>();
	parameters.put(PARAM_CARD_ID, String.valueOf(cardId));
	parameters.put(PARAM_ONLY_SIGNERS, signersOnly);
	parameters.put(PARAM_ATTACH_ID, String.valueOf(fileCardId));
	URL url = generateUrl("getSignatures", parameters);
	GetHttpRequest request = new GetHttpRequest(url);
	InputStream inputStream = null;
	try {
	    inputStream = request.get();
	    ObjectInputStream ois = new ObjectInputStream(inputStream);
	    List<List<String>> signatureDatas = (List<List<String>>) ois
		    .readObject();
	    for (int i = 0; i < signatureDatas.size(); i++) {
		List<String> signData = signatureDatas.get(i);
		String signerName = signData.get(0);
		String signFileName = String.valueOf(i + 1) + "_" + signerName
			+ ".sig";
		String certFileName = String.valueOf(i + 1) + "_" + signerName
			+ ".cer";
		String signature = signData.get(1);
		byte[] signatureBytes = hexStringToByteArray(signature);
		String cert = signData.get(2);
		writeStreamToFile(new ByteArrayInputStream(signatureBytes),
			new File(workingDirectory, signFileName));
		if (!"".equals(cert)) {
		    writeStreamToFile(
			    new ByteArrayInputStream(cert.getBytes()),
			    new File(workingDirectory, certFileName));
		}
	    }
	} catch (Exception ex) {
	    throw new AppletException("cards.servlet");
	}
    }

    private byte[] hexStringToByteArray(String s) {
	int len = s.length();
	byte[] data = new byte[len / 2];
	for (int i = 0; i < len; i += 2) {
	    data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character
		    .digit(s.charAt(i + 1), 16));
	}
	return data;
    }

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
}
