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
package com.aplana.soz.adapter;

import com.aplana.soz.SOZConfigFacade;
import com.aplana.soz.SOZException;
import com.aplana.soz.client.DMSExchangeService;
import com.aplana.soz.model.attachement.AttachedFileType;
import com.aplana.soz.model.attachement.PutDMSMessageRequest;
import com.aplana.soz.model.attachement.PutDMSMessageResponse;
import com.aplana.soz.model.communication.*;
import com.aplana.soz.service.DMSExchangePortType;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import java.io.*;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class SOZAdapter {

    private boolean isActive = false;
    private ExportDirIterator dirIterator;
    private Unmarshaller unmarshaller;
    private DMSExchangePortType port;

    protected final Log logger = LogFactory.getLog(getClass());

    public final static String NAMESPACE_URI = "urn://x-artefacts-it-ru/dob/poltava/dmsx/1.0";
    public final static String SUCCESS_RESULT_CODE = "SUCCESS";

    public void initConfiguration() throws SOZException {
        String exportDirectoryStr = SOZConfigFacade.getInstance().getExportDirectory();
        File exportDirectory = new File(exportDirectoryStr);
        if (!exportDirectory.isDirectory()) {
            throw notADir(exportDirectoryStr, SOZConfigFacade.EXPORT_FOLDER_PROPERTY);
        }
        String exportDirectoryOkStr = SOZConfigFacade.getInstance().getExportResultOkDirectory();
        File exportResultOkDirectory = new File(exportDirectoryOkStr);
        if (exportResultOkDirectory.exists() && (!exportResultOkDirectory.isDirectory())) {
            throw notADir(exportDirectoryOkStr, SOZConfigFacade.RESULT_OK_EXPORT_FOLDER_PROPERTY);
        }
        String exportDirectoryFailStr = SOZConfigFacade.getInstance().getExportResultFailDirectory();
        File exportResultFailDirectory = new File(exportDirectoryFailStr);
        if (exportResultFailDirectory.exists() && (!exportResultFailDirectory.isDirectory())) {
            throw notADir(exportDirectoryFailStr, SOZConfigFacade.RESULT_FAIL_EXPORT_FOLDER_PROPERTY);
        }
        dirIterator = new ExportDirIterator(exportDirectory, exportResultOkDirectory, exportResultFailDirectory);

        String url = SOZConfigFacade.getInstance().getServerURL();
        URL serverURL;
        try {
            serverURL = new URL(url);
        } catch (MalformedURLException e) {
            logger.error(e);
            throw new SOZException(
                    "\"" + url + "\" is not a valid URL!\n" +
                            "Please configure property " + SOZConfigFacade.SOZ_SERVER_URL
                            + " in " + SOZConfigFacade.CONFIG_FILE_NAME);
        }

        DMSExchangeService service = new DMSExchangeService(serverURL,
                new QName(NAMESPACE_URI, SOZConfigFacade.getInstance().getDMSExchangeService()));
        port = service.getPort(new QName(NAMESPACE_URI,
                SOZConfigFacade.getInstance().getDMSExchangePort()), DMSExchangePortType.class);

        try {
            JAXBContext jc = JAXBContext.newInstance("com.aplana.soz.model.communication");
            unmarshaller = jc.createUnmarshaller();
        } catch (JAXBException e) {
            logger.error(e);
            throw new SOZException(e);
        }


        logger.info("SOZAdapter is configured properly.");
    }

    private SOZException notADir(String dirStr, String propertieName) throws SOZException {
        return new SOZException(
                "\"" + dirStr + "\" is not a directory!\n" +
                        "Please configure property " + propertieName
                        + " in " + SOZConfigFacade.CONFIG_FILE_NAME);
    }

    public boolean isActive() {
        return isActive;
    }

    public Result runControlledExport() throws SOZException {
        try {
            isActive = true;
            return runExport();
        } finally {
            isActive = false;
        }
    }

    public Result runExport() throws SOZException {
        Result ret = new Result();
        while (dirIterator.hasNext()) {
            boolean success = false;
            File next = dirIterator.next();
            FileInputStream fis = null;
            try {
                Communication communication;
                try {
                    fis = new FileInputStream(next);
                    communication = (Communication) unmarshaller.unmarshal(fis);
                    fis.close();
                } catch (IOException e) {
                    throw new SOZException("Can't read " + next.getAbsolutePath(), e);
                } catch (JAXBException e) {
                    e.printStackTrace();
                    throw new SOZException("Can't unmarshall " + next.getAbsolutePath(), e);
                }
                ret.addTotal();
                logger.info("Sending " + next.getAbsolutePath() + " to service");
                PutDMSMessageRequest request = new PutDMSMessageRequest();
                request.setCommunication(communication);

                addAttachedFiles(request, communication, next.getParentFile());

                PutDMSMessageResponse putDMSMessageResponse = port.putDMSMessage(request);
                if (putDMSMessageResponse != null && putDMSMessageResponse.getResult() != null) {
                    String resultCode = putDMSMessageResponse.getResult().getResultCode();
                    logger.info("Result is: " + resultCode);
                    if (SUCCESS_RESULT_CODE.equals(resultCode)) {
                        success = true;
                    } else {
                        logger.warn(putDMSMessageResponse.getResult().getResultMessage());
                    }
                } else {
                    logger.warn("Result is NULL");
                }

                if (success) {
                    dirIterator.moveToOk();
                    logger.info("File " + next.getAbsolutePath() + " was moved to OK folder");
                    ret.addOk();
                } else {
                    dirIterator.moveToFail();
                    logger.info("File " + next.getAbsolutePath() + " was moved to FAIL folder");
                    ret.addFailed();
                }

            } catch (SOZException e) {
                try {
                    if (fis != null) {
                        fis.close();
                    }
                    dirIterator.moveToFail();
                    logger.info("File " + next.getAbsolutePath() + " was moved to FAIL folder");
                    ret.addFailed();
                } catch (Exception e1) {
                    // swallow exception here
                    logger.error(e1);
                }
                throw e;
            }
        }
        return ret;
    }

    private void addAttachedFiles(PutDMSMessageRequest request, Communication communication, File dir) {

        Communication.Files files = communication.getFiles();

        if (files != null && files.getFile() != null && files.getFile().size() > 0) {
            List<AttachedFileType> attachedFile = request.getAttachedFile();

            for (AssociatedFile associatedFile : files.getFile()) {
                String localFileName = associatedFile.getLocalName();
                BigInteger localId = associatedFile.getLocalId();
                FileType fileType = associatedFile.getType();

                if (localFileName != null) {
                    try {
                        File file = new File(dir, localFileName);
                        if (file.exists() && file.isFile()) {
                            byte[] fileContent = FileUtils.readFileToByteArray(file);
                            AttachedFileType attachedFileType = new AttachedFileType();
                            attachedFileType.setLocalName(localFileName);
                            if (localId != null) attachedFileType.setLocalId(localId.toString());
                            if (fileType != null) attachedFileType.setType(fileType.value());
                            attachedFileType.setValue(fileContent);
                            attachedFile.add(attachedFileType);
                            logger.debug("File \"" + localFileName + "\" was attached to the request.");
                        } else if (!file.exists()) {
                            logger.error("File \"" + localFileName + "\" does not exists in document folder. Skip it.");
                        } else if (!file.isFile()) {
                            logger.error("\"" + localFileName + "\" is not a file. Skip it.");
                        }
                    } catch (IOException e) {
                        logger.error("Error reading attached file \"" + localFileName
                                + "\". Skip it.", e);
                    }
                }
            }


        }
    }

    /**
     * for test use only
     *
     * @param args
     */
    public static void main(String[] args) {
        try {
            SOZAdapter soz = new SOZAdapter();
            soz.initConfiguration();
            soz.runControlledExport();
        } catch (SOZException e) {
            e.printStackTrace();
        }
    }
}
