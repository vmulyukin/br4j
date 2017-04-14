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
package com.aplana.medo.gate;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ini4j.InvalidFileFormatException;
import org.ini4j.Wini;

public class MEDOGate {
	Properties props;
	private static MEDOGate instance;
	private boolean active;
    protected final Log logger = LogFactory.getLog(getClass());
    static final String INI_FILE_EXT = "ini";
    static final String INI_ADDRESEE_SECTION_NAME = "��������";
    static final String INI_FILES_SECTION_NAME = "�����";
    static final String INI_LETTER_SECTION_NAME = "������ �� �� ���"; 
    static final String INI_FILES_THEME_PARAM_NAME = "����";
	
    protected MEDOGate(){}
	
	public static MEDOGate getInstance(){
		if (instance == null)
			instance = new MEDOGate();
		return instance;
	}
	
	public void setProperties(Properties props){
		this.props = props;
	}
	
	public void execute(){
		try {
			active = true;
			if (props == null){
				logger.error("Configuration is not defined");
				return;
			}
			String tmp = props.getProperty("InboundFolders");
			if (tmp == null || tmp.equals("")){
				logger.error("The inbound folder is not defined.");
				return;
			}
			String[] inboundFolders = tmp.split(";");
			List<File> inFolders = new ArrayList<File>();
			for (int i=0; i<inboundFolders.length; i++){
				File file = new File(inboundFolders[i]);
				if ((file.exists() && file.isDirectory()))
					inFolders.add(file);
				else
					logger.warn("The inbound folder "+inboundFolders[i]+" does not exist.");
			}
			
			tmp = props.getProperty("OutboundFolder");
			File outFolder = new File(tmp);
			if (!(outFolder.exists() && outFolder.isDirectory())){
				logger.error("The outbound folder "+tmp+" does not exist.");
				return;
			}
			
			tmp = props.getProperty("TicketFolder");
			File ticketFolder = new File(tmp);
			if (!(ticketFolder.exists() && ticketFolder.isDirectory())){
				logger.error("The folder for tickets "+tmp+" does not exist.");
				return;
			}
			
			for (File inFolder : inFolders){
				logger.info("Start processing inbound folder "
							+inFolder.getPath()+":");
				MEDOFolderFilter filter = new MEDOFolderFilter();
				File[] eligible = inFolder.listFiles(filter);  
				logger.info(eligible.length+" document folder(s) found");
				for (int i=0; i < eligible.length; i++){
					logger.info("processind document "+eligible[i].getName()+" ...");
					List<String> destinations = getDestinations(eligible[i]);
					if (destinations.isEmpty()){
						logger.warn("No one destination for document "+eligible[i].getName()+" is not defined. Skipped.");
						continue;
					}
					logger.info("checking its destinations...");
					destinations = filterDestinations(destinations, outFolder);
					if (destinations.isEmpty()){
						logger.warn("No one destination for document "+eligible[i].getName()+" is eligible. Skipped.");
						continue;
					}
					boolean deliver = false;
					for (String destination : destinations)
						try {
							File dest = new File(outFolder, destination);
							logger.info("Copying document folder "+eligible[i].getName()+" to "+dest.getName());
							FileUtils.copyDirectory(eligible[i], new File(dest, eligible[i].getName()));
							logger.info("folder "+eligible[i].getName()+" is copied");
							deliver = true;
						} catch (IOException e) {
							logger.error("Error met when moving document folder"+eligible[i].getName()+": ", e);
							continue;
						}
						if (deliver){
							generateTicket(eligible[i], ticketFolder);
							logger.info("Deleting document folder "+eligible[i].getName());
							try {
								FileUtils.deleteDirectory(eligible[i]);
								logger.info("folder "+eligible[i].getName()+" is deleted");
							} catch (IOException e) {
								logger.error("Error met when deleting document folder"+eligible[i].getName()+": ", e);
							}
						}
				}
				logger.info("end processing inbound folder "+inFolder.getPath());
			}
		}finally{
			active = false;
			logger.info("MEDOGate has finished its work.");
		}
	}
	
	void generateTicket(File docFolder, File ticketFolder){
		try {
			Wini wini = getINIFile(docFolder);
			if (wini == null){
				logger.error("can not find INI file in document folder "+docFolder.getName());
				return;
			}
			wini.remove(INI_FILES_SECTION_NAME);
			String descr = wini.get(INI_LETTER_SECTION_NAME, INI_FILES_THEME_PARAM_NAME);
			wini.put(INI_LETTER_SECTION_NAME, INI_FILES_THEME_PARAM_NAME, "����������: "+descr);
			OutputStreamWriter osw = new OutputStreamWriter(
					new FileOutputStream(new File(ticketFolder, wini.getFile().getName())), Charset.forName("Cp1251"));
			wini.store(osw);
		} catch (InvalidFileFormatException e) {
			logger.error("Invalid Ini file format in document folder "+ docFolder.getName(), e);
		} catch (IOException e) {
			logger.error("Output operation error when writing Ini file to ticket folder"+ticketFolder.getName(), e);
		}
	}
	
	List<String> filterDestinations(List<String> destinations, File parentFolder){
		List<String> eligible = new ArrayList<String>(destinations); 
		for (ListIterator<String> iter=eligible.listIterator(); iter.hasNext(); ){
			File destFolder = new File(parentFolder, iter.next());
			if (!destFolder.exists()){
				logger.warn("Destination "+destFolder.getName()+" does not exist. Skipped.");
				iter.remove();
			}
		}
		return eligible;
	}
	
	List<String> getDestinations(File folder){
		List<String> dests = new ArrayList<String>();
		try {
			Wini wini = getINIFile(folder);
			if (wini == null)
				return dests;
			Wini.Section sec = wini.get(INI_ADDRESEE_SECTION_NAME);
			for (Map.Entry<String, String> entry : sec.entrySet()){
				dests.add(entry.getValue());
			}
		} catch (InvalidFileFormatException e) {
			logger.error("Invalid Ini file format in document folder "+ folder.getName(), e);
		} catch (IOException e) {
			logger.error("Input operation error when reading Ini file in document folder"+folder.getName(), e);
		}
		return dests;
	}
	
	Wini getINIFile(File folder) throws InvalidFileFormatException, IOException{
		File[] files = folder.listFiles(new MEDOIniFileFilter());
		if (files == null || files.length == 0){
			logger.warn("can not fing INI file in folder "+folder.getName());
			return null;
		}
		File iniFile = files[0];
		InputStreamReader isr = new InputStreamReader(
									new FileInputStream(iniFile), Charset.forName("Cp1251"));
		Wini wini = new Wini(isr);
		wini.setFile(iniFile);
		return wini;
	}

	public boolean isActive() {
		return active;
	}

	class MEDOFolderFilter implements FileFilter{
		public boolean accept(File folder) {
			if (!(folder.exists() && folder.isDirectory())){
				logger.info("File "+folder.getName()+" skipped");
				return false;
			}
			File[] files = folder.listFiles(new MEDOIniFileFilter());
			if (files.length == 1)
				return true;
			logger.info("Folder "+folder.getName()+" does not contain INI file. Skipped.");
			return false;
		}
	}
	
	class MEDOIniFileFilter implements FilenameFilter{
		public boolean accept(File dir, String fName) {
			if (fName.equalsIgnoreCase(dir.getName()+"."+INI_FILE_EXT))
				return true;
			return false;
		}
	}
}
