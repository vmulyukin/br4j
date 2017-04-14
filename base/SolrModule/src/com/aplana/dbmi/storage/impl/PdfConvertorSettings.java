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
package com.aplana.dbmi.storage.impl;

import com.aplana.dbmi.Portal;
import com.aplana.dbmi.pdfa.converter.PDFAConverter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

public class PdfConvertorSettings {

	private static Log logger = LogFactory.getLog(PdfConvertorSettings.class);

	private final static String CONFIG = "dbmi/openoffice/pdfConvertor.properties";
	private final static String PDFA_CONFIG = "dbmi/PDFAConverter/pdfaConverter.properties";

	final static String CONVERTOR_TEMP_DIR = "convertor.temp.dir";
	final static String CONVERTOR_LOG_DIR = "convertor.log.dir";
	final static String CONVERTOR_CACHE_STORAGE = "convertor.cache.storage";

	final static String CONVERTOR_QUOTA_TIME="converter.quota.time";
	final static String CONVERTER_REACTION_TIME="converter.reaction.time";	
	final static String CONVERTER_TIMED_OUT="converter.timed.out";	

	final static String CONVERTER_QUEUE_SIZE="converter.queue.size";
	final static String CONVERTER_ACTIVE_QUEUE_SIZE="converter.active.queue.size";

	final static String OPEN_OFFICE_HOST = "openoffice.host";
	final static String OPEN_OFFICE_PORT = "openoffice.port";
	final static String OPEN_OFFICE_TEMP_DIR = "openoffice.temp.dir";
	
	
	//PDFA Converter properties
	final static String CONVERTOR_PDFA_VERSION = "converter.pdfa.version";
	final static String CONVERTOR_PDFA_COMPATIBILITY_POLICY = "converter.pdfa.compatibility.policy";
	final static String CONVERTOR_PDFA_COLOR_MODEL = "converter.color.model";
	final static String CONVERTOR_PDFA_ICC_FILE_PATH = "converter.icc.profile.file.path";
	final static String CONVERTOR_PDFA_DEF_FILE_PATH = "converter.pdfa.definition.file.path";
	
	//PDFA Converter default properties
	final static int DEFAULT_PDFA_VERSION = 1;
	final static int DEFAULT_PDFA_COMPATIBILITY_POLICY = 1;
	final static int DEFAULT_PDFA_COLOR_MODEL = 0;
	final static String DEFAULT_PDFA_DEF_FILE = "PDFA_def.ps";
	
	final static String SRGB_ICC_PROFILE = "dbmi/PDFAConverter/ICCProfiles/sRGB.icc";

	private static Properties props;
	private static Properties pdfaProps;
	
	private static Object synch = new Object();

	public static Properties getProps() {
		synchronized  (synch) { // (PdfConvertorSettings.class)
			if (props == null) {			
				try {
					final InputStream is = Portal.getFactory().getConfigService().getConfigFileUrl(CONFIG).openStream();
					try {
					final Properties p = new Properties();
					p.load(is);
					props = p;
					} finally {
						IOUtils.closeQuietly(is);
					}
				} catch (IOException e) {
					logger.error("Couldn't read settings file " + CONFIG, e);
				}
			}
		}
		
		return props;
	}

	public static Properties getPdfaProps() {
		synchronized  (synch) { // (PdfConvertorSettings.class)
			if (pdfaProps == null) {			
				try {
					final InputStream is = Portal.getFactory().getConfigService().getConfigFileUrl(PDFA_CONFIG).openStream();
					try {
						final Properties p = new Properties();
						p.load(is);
						pdfaProps = p;
					} finally {
						IOUtils.closeQuietly(is);
					}
				} catch (IOException e) {
					logger.error("Couldn't read settings file " + PDFA_CONFIG, e);
				}
			}
		}
		
		return pdfaProps;
	}
	
	public static String getConvertorTempDir() {
		return getProps().getProperty(CONVERTOR_TEMP_DIR);
	}

	public static String getConvertorLogDir() {
		String fullPath = getProps().getProperty(CONVERTOR_LOG_DIR);
		try {
			fullPath = new File(fullPath).getAbsolutePath();
		} catch (Exception e){
			logger.error("Couldn't read " + fullPath +" converter log dir. Wrong path.", e);
		}
		return fullPath;
	}
	
	public static String getCacheStorageName() {
		return getProps().getProperty(CONVERTOR_CACHE_STORAGE);
	}

	public static String getQuotaTime(){
		return getProps().getProperty(CONVERTOR_QUOTA_TIME);
	}

	public static String getReactionTime(){
		return getProps().getProperty(CONVERTER_REACTION_TIME);
	}

	public static String getTimedOut(){
		return getProps().getProperty(CONVERTER_TIMED_OUT);
	}

	public static String getQueueSize(){
		return getProps().getProperty(CONVERTER_QUEUE_SIZE);
	}
	
	public static String getActiveTaskQueueSize(){
		return getProps().getProperty(CONVERTER_ACTIVE_QUEUE_SIZE);
	}

	public static String getServerConnectionHost() {
		return getProps().getProperty(OPEN_OFFICE_HOST);
	}
	
	public static String getServerConnectionPort() {
		return getProps().getProperty(OPEN_OFFICE_PORT);
	}
	
	public static String getServerTempDir() {
		return getProps().getProperty(OPEN_OFFICE_TEMP_DIR);
	}
	
	public static String getPdfAIccFilePath() {
		final String relativePath = getPdfaProps().getProperty(CONVERTOR_PDFA_ICC_FILE_PATH);
		String fullPath = null;
		try {
			fullPath = new File(Portal.getFactory().getConfigService().getConfigFileUrl(relativePath).toURI())
				.getAbsolutePath();
		}
		catch (Exception e){
			logger.error("Couldn't read " + relativePath +" icc profile file ", e);
		}
		return fullPath;
	}
	
	public static String getPdfADefFilePath() {
		final String relativePath = getPdfaProps().getProperty(CONVERTOR_PDFA_DEF_FILE_PATH, DEFAULT_PDFA_DEF_FILE);
		String fullPath = null;
		try {
			fullPath = new File(Portal.getFactory().getConfigService().getConfigFileUrl(relativePath).toURI())
				.getAbsolutePath();
		}
		catch (Exception e){
			logger.error("Couldn't read " + relativePath +" pdfa definition file ", e);
		}
		return fullPath;
	}

	public static int getPdfAVersion() {
		String value = getPdfaProps().getProperty(CONVERTOR_PDFA_VERSION);
		if (value != null && value.length() > 0) {
			try {
				return Integer.valueOf(value);
			} catch(Exception ex) {
				logger.error("problem getting PDFA version: '" + value + "', default  " +
						DEFAULT_PDFA_VERSION + " is used instead" , ex);
			}
		}
		return DEFAULT_PDFA_VERSION;
	}
	
	public static int getPdfACompatibilityPolicy() {
		String value = getPdfaProps().getProperty(CONVERTOR_PDFA_COMPATIBILITY_POLICY);
		if (value != null && value.length() > 0) {
			try {
				return Integer.valueOf(value);
			} catch(Exception ex) {
				logger.error("problem getting PDFA compatibility version: '" + value + "', default  " +
						DEFAULT_PDFA_COMPATIBILITY_POLICY + " is used instead" , ex);
			}
		}
		return DEFAULT_PDFA_COMPATIBILITY_POLICY;
	}

	public static int getPdfAColorModel() {
		String value = getPdfaProps().getProperty(CONVERTOR_PDFA_COLOR_MODEL);
		if (value != null && value.length() > 0) {
			try {
				return Integer.valueOf(value);
			} catch(Exception ex) {
				logger.error("problem getting PDFA color model: '" + value + "', default  " +
						DEFAULT_PDFA_COLOR_MODEL + " is used instead" , ex);
			}
		}
		return DEFAULT_PDFA_COLOR_MODEL;
	}
	
	public static InputStream getSRgbIccProfileStream() {
		InputStream is = null;
		try {
			is = Portal.getFactory().getConfigService().getConfigFileUrl(SRGB_ICC_PROFILE).openStream();
		} catch (IOException e) {
			logger.error("Couldn't read " + SRGB_ICC_PROFILE +" file ", e);
		}
		return is;
	}
	
    public static synchronized void updatePdfADefFile(){

    	final ArrayList<String> outLines = new ArrayList<String>();
    	String iccFilePath = getPdfAIccFilePath();
    	if (iccFilePath == null || iccFilePath.isEmpty()){
    		return;
    	}
    	else {
    		// Ghostscript cannot open icc profile specified in PDFA_def.ps file with single '\' 
    		// as a path separator, so need to escape it
    		iccFilePath = iccFilePath.replace("\\", "\\\\");
    	}
    	final int processColorModel = getPdfAColorModel();
    	final String pdfADefFilePath = getPdfADefFilePath();

    	File f = new File(pdfADefFilePath);
    	try{
    		List<String> lines = FileUtils.readLines(f, "UTF-8");
    		Iterator<String> it = lines.iterator();
    		while(it.hasNext()){
    			String line = it.next();
    			if (line.contains("/ICCProfile")){
    				line = "/ICCProfile (" + iccFilePath + ")";
    			}
    			else if (line.contains("/OutputConditionIdentifier")){
    				switch (processColorModel){
    					case PDFAConverter.OPTION_PROCESSCOLORMODEL_CMYK:
    						line = "/OutputConditionIdentifier (CMYK)";
    						break;
    					default:
    						line = "/OutputConditionIdentifier (sRGB)";
    				}
    			}
    			outLines.add(line);
    		}
			FileUtils.writeLines(f, "UTF-8", outLines);
		}
    	catch (IOException e){
    		logger.error("Couldn't update " + pdfADefFilePath +" file ", e);
    	}
    }
}