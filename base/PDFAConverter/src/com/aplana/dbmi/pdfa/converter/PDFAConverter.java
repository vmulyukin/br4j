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
package com.aplana.dbmi.pdfa.converter;
 
import gnu.cajo.invoke.Remote;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ghost4j.Ghostscript;
import org.ghost4j.GhostscriptException;
import org.ghost4j.converter.AbstractRemoteConverter;
import org.ghost4j.converter.ConverterException;
import org.ghost4j.converter.RemoteConverter;
import org.ghost4j.document.Document;
import org.ghost4j.document.DocumentException;
import org.ghost4j.util.JavaFork;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;


/**
 * Конвертер pdf файла в PDF/A-x формат с поддержкой многопоточности.
 * На основе Ghost4j-0.5.1
 * 
 * @author Vlad Alexandrov
 *
 */
public class PDFAConverter extends AbstractRemoteConverter {
	protected final Log logger = LogFactory.getLog(getClass());
	
	/** 
	 * Include the feature or operation in the output file, the file will not be PDF/A compliant. 
	 * Because the document Catalog is emitted before this is encountered, 
	 * the file will still contain PDF/A metadata but will not be compliant. 
	 * A warning will be emitted in this case.
	 */
	public static final int OPTION_PDFA_POLICY_INCLUDE = 0;
	/**
	 * The feature or operation is ignored, the resulting PDF file will be PDF/A compliant. 
	 * A warning wil be emitted for every elided feature.
	 */
	public static final int OPTION_PDFA_POLICY_IGNORE = 1;
	/**
	 * Processing of the file is aborted with an error, the exact error may vary depending 
	 * on the nature of the PDF/A incompatibility.
	 */
	public static final int OPTION_PDFA_POLICY_ABORT = 2;

	
	/**
	 * Option to specify PDF/A-1 version compatibility
	 */
	public static final int OPTION_PDFA_VERSION_1 = 1;
	/**
	 * Option to specify PDF/A-2 version compatibility
	 */
	public static final int OPTION_PDFA_VERSION_2 = 2;
	
    public static final int OPTION_PROCESSCOLORMODEL_RGB = 0;
    public static final int OPTION_PROCESSCOLORMODEL_CMYK = 1;
  
    private int dPDFACompatibilityPolicy = OPTION_PDFA_POLICY_IGNORE;
    private int PDFACompatibilityVersion = OPTION_PDFA_VERSION_1;
    private int processColorModel = OPTION_PROCESSCOLORMODEL_RGB;

    private String iccFilePath = null;
    private String pdfADefFilePath = null;

    /**
     * Main method used to start the converter in standalone 'slave mode'.
     * 
     * @param args
     * @throws ConverterException
     */
    public static void main(String args[]) throws ConverterException {
    	startRemoteConverter(new PDFAConverter());
    }
  
    /**
     * Run method called to perform the actual process of the converter.
     * 
     * @param srcFileName
     * @param dstFileName
     * @throws IOException
     * @throws ConverterException
     * @throws DocumentException
     */
    public void run(Object id, String convertDir, String srcFileName, String dstFileName)
	    throws IOException, ConverterException, DocumentException {

    	// get Ghostscript instance
    	Ghostscript gs = Ghostscript.getInstance();
    	
    	// prepare Ghostscript interpreter parameters
    	int argCount = 12;
    	if (getIccFilePath() != null && !getIccFilePath().isEmpty()){
    		argCount++;
    	}
    	if (getPdfADefFilePath() != null && !getPdfADefFilePath().isEmpty()){
    		argCount++;
    	}

    	/*
    	 *  Here is a sample command line to invoke Ghostscript for generating a PDF/A document :
    	 *  
    	 *  gs -dPDFA=1 -dBATCH -dNOPAUSE -dNOOUTERSAVE -sColorConversionStrategy=/UseDeviceIndependentColor
    	 *  -sProcessColorModel=DeviceRGB -sOutputICCProfile=sRGB.icc 
    	 *  -sDEVICE=pdfwrite -sOutputFile=out.pdf PDFA_def.ps input.pdf
    	 */
    	String[] gsArgs = new String[argCount];
		String logFileName = convertDir+"/"+id+"_"+new Date().getTime()+".log";

    	gsArgs[0] = "-ps2pdf";
    	gsArgs[1] = "-dPDFA";
    	gsArgs[2] = "-dNOPAUSE";
    	gsArgs[3] = "-dBATCH";
    	gsArgs[4] = "-dNOOUTERSAVE";
    	gsArgs[5] = "-sstdout="+logFileName;

    	int paramPosition = 5;

    	// create a device-independent color
    	paramPosition++;
    	gsArgs[paramPosition] = "-sColorConversionStrategy=/UseDeviceIndependentColor";
 
    	paramPosition++;
    	switch (this.processColorModel){
    		case OPTION_PROCESSCOLORMODEL_CMYK:
    			gsArgs[paramPosition] = "-sProcessColorModel=DeviceCMYK";
    			break;
    		default:
    			gsArgs[paramPosition] = "-sProcessColorModel=DeviceRGB";
    	}

    	if (getIccFilePath()!= null && !getIccFilePath().isEmpty()) {
    		// Output ICC color profile
    		paramPosition++;
    		gsArgs[paramPosition] = "-sOutputICCProfile=" + getIccFilePath();
    	}
  
    	paramPosition++;
    	gsArgs[paramPosition] = "-sDEVICE=pdfwrite";
	
    	paramPosition++;
    	gsArgs[paramPosition] = "-dPDFACompatibilityPolicy=" + dPDFACompatibilityPolicy;
	
    	// output to file
    	paramPosition++;
    	gsArgs[paramPosition] = "-sOutputFile=" + dstFileName;
    	
    	if (getPdfADefFilePath() != null && !getPdfADefFilePath().isEmpty()){
    		// PDF/A definition file
    		paramPosition++;
    		gsArgs[paramPosition] = getPdfADefFilePath();
    	}
    	
    	// input file
    	paramPosition++;
    	gsArgs[paramPosition] = srcFileName;

    	try {
    		// execute and exit interpreter
    		synchronized (gs) {
	    		gs.initialize(gsArgs);
	    		gs.exit();
    		}
			File logFile = new File(logFileName);
			if (logFile.exists()) {
				logFile.delete();
			}
    	} catch (GhostscriptException e) {
    		throw new ConverterException(e);
    	} finally {
    		// delete Ghostscript instance
    		try {
    			Ghostscript.deleteInstance();
    		} catch (GhostscriptException e) {
    			throw new ConverterException(e);
    		}
    	}
    }
    
    public int getPDFACompatibilityPolicy() {
    	return dPDFACompatibilityPolicy;
    }

	public void setPDFACompatibilityPolicy(int PDFAPolicy) {
		this.dPDFACompatibilityPolicy = PDFAPolicy;
	}
	
    public int getPDFAVersion() {
    	return PDFACompatibilityVersion;
    }

	public void setPDFAVersion(int PDFAVersion) {
		this.PDFACompatibilityVersion = PDFAVersion;
	}
	
    public int getProcessColorModel() {
    	return processColorModel;
    }

    public void setProcessColorModel(int processColorModel) {
    	this.processColorModel = processColorModel;
    }
    
    public String getIccFilePath(){
    	return iccFilePath;
    }

    public void setIccFilePath(String iccFilePath){
    	this.iccFilePath = iccFilePath;
    }
    
    public String getPdfADefFilePath(){
    	return pdfADefFilePath;
    }

    public void setPdfADefFilePath(String pdfADefFilePath){
    	this.pdfADefFilePath = pdfADefFilePath;
    }
    
    public boolean remoteConvert(Object id, String dir, String srcFileName, String dstFileName) throws IOException,
    	ConverterException, DocumentException {
    	run(id, dir, srcFileName, dstFileName);
    	return true;
    }
    

    public void convert(Object id, String dir, String scrFileName, String dstFileName)
    	throws IOException, ConverterException, DocumentException {

    	if (maxProcessCount == 0) {
    		// perform actual processing
    		run(id, dir, scrFileName, dstFileName);
    	} else {
    		// wait for a process to get free
    		this.waitForFreeProcess();
    		processCount++;

    		// check if current class supports stand alone mode
    		if (!this.isStandAloneModeSupported()) {
    			throw new ConverterException(
    			"Standalone mode is not supported by this converter: no 'main' method found");
    		}

    		// prepare new JVM
    		JavaFork fork = this.buildJavaFork();

    		// set JVM Xms parameter
    		int xmsValue = 8;
    		fork.setXms(xmsValue + "m");
    		
    		// set JVM Xmx parameter
    		int xmxValue = 16;
    		fork.setXmx(xmxValue + "m");

    		int cajoPort;

    		try {
    			// start remove server
    			cajoPort = this.startRemoteServer(fork);

    			// get remote component
    			Object remote = this.getRemoteComponent(cajoPort, RemoteConverter.class);

    			// copy converter settings to remote converter
    			Remote.invoke(remote, "copySettings", this.extractSettings());

    			// perform remote conversion
    			Remote.invoke(remote, "remoteConvert", new Object[] {id, dir, scrFileName, dstFileName});

    		} catch (IOException e) {
    			logger.error("Can't convert file by ghostscript", e);
    			throw e;
    		} catch (Exception e) {
    			logger.error("Can't convert file by ghostscript", e);
    			throw new ConverterException(e);
    		} finally {
    			processCount--;
    			fork.stop();
    		}
    	}
    }
    
    @Override
    public void run(Document document, OutputStream outputStream)
    	throws IOException, ConverterException, DocumentException {
    	// Not used in this converter
    }
}