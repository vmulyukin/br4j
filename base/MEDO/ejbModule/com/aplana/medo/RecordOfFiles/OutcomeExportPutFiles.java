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
 * 
 */
package com.aplana.medo.RecordOfFiles;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import com.aplana.medo.MedoException;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

/**
 * @author PPanichev
 *
 */
public class OutcomeExportPutFiles {

    	public static final String NAME_FILE = "document.xml";

	private Log logger = LogFactory.getLog(getClass());
	private String outFolderExport = null;
	private String out_dir = null;
	private File exportingFile = null;
	private String cardUID = null;

	public OutcomeExportPutFiles(String outFolderExport, String cardUID) {
		this.outFolderExport = outFolderExport;
		this.cardUID = cardUID;
	}

	/* public String getFileName() {
	return nameFile;
    }

    public String getOutDir() {
	return out_dir;
    }*/

	public synchronized File putFile(Document XML) throws MedoException {
		Boolean result = true; // ������� �� ����������� ������ ������

		try {
		    	setOutDir();
			// ���������� � �������� ����, � ������� ����� ������������� ������
			setFile(NAME_FILE, out_dir);

			// synchronized (exportingFile) { // �������������� ������ � �����
				if (!exportingFile.exists()) { // ���� ���� �� ����������
					// C������� ����
					final File dr = new File(out_dir); // ������� ������� ���
					// ������
					// synchronized (dr) { // �������������� ������ � ��������
						if (dr.exists()) // ��������� ������� ��
							// �������������
							exportingFile.createNewFile(); // ���� ����������, ������
						// ���� � ��������
						else {
							dr.mkdirs(); // ���� ������� �� ����������,
							// ������ ���
							exportingFile.createNewFile(); // ������ � �������� ����
						}
					// }
				// }

				if (!exportingFile.canWrite()) { // ���� ���� ����������, ���������
					// ����������� ������ � ����
					System.out.println("������ � ���� : " + exportingFile.getAbsolutePath()
							+ " ����������!"); // ���������� ��� ���
					// ������������� ������ � ����
					logger
					.error("����: " + exportingFile.getAbsolutePath()
							+ " - �� �������� ��� ������.");
					exportingFile = null;
				} else { // ���� ������ � ���� ��������
					// ���������� � ����
					try {
						final FileOutputStream out = new FileOutputStream(exportingFile);
						final OutputFormat format = new OutputFormat("XML", "WINDOWS-1251", false);
						final XMLSerializer serializer = new XMLSerializer(out, format);
						serializer.serialize(XML.getDocumentElement());
						out.close();
					} catch (IOException ex) {
						logger
						.error(
								"Error during updated by links xml serialization",
								ex);
						result = false;
					}
				}
			}
		} catch (Exception IO) { // ������� ��������� �� ������
			result = false;
			logger.error(IO.toString(), IO);
		} finally {
			if (result) {
				logger.info("������ �����: " + exportingFile.getAbsolutePath() + " ���������.");
			} else
				logger.error("������ �����: " + exportingFile.getAbsolutePath() + " �� ���������!");
		}
		return exportingFile;
	}

	public String getAbsoluteFileName() {
	    String workingDirName = outFolderExport + "/" + cardUID;
	    String absoluteFileName = workingDirName + "/" + NAME_FILE;
	    return absoluteFileName;
	}
	
	private void setOutDir() {
	    final File workingDir = new File(outFolderExport + "/" + cardUID);
		workingDir.mkdir();
		out_dir = workingDir.getAbsolutePath();
	}

	private void setFile(String name, String dir) {
		exportingFile = new File(dir, name);
	}

}
