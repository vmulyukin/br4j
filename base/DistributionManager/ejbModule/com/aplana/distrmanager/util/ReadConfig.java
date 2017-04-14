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
package com.aplana.distrmanager.util;

import java.io.IOException;
import java.util.Properties;

public class ReadConfig {
	   
    protected Properties options;
    // Properties ��� ��������
    protected String outFolderPath = "";
    
    public ReadConfig(Properties optionsBean) throws IOException {
    	this.options = optionsBean;
    	readConfig();
    }
	
    /**
     * Reads options from {@link #CONFIG_FILE} file and properties from
     * {@link #PROPERTIES_FILE} and initialize appropriate fields of bean
     */
	private void readConfig() throws IOException {
    readOutConfig();
    readInConfig();
	}
	
	private void readOutConfig() {
		this.outFolderPath = options.getProperty("outFolder");
	}
	
	private void readInConfig() {
		//stub
	}

	/**
     * @return Out Folder
     */
	public String getOutFolderPath() {
		return outFolderPath;
	}
}
