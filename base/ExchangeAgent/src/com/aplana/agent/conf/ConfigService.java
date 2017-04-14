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
package com.aplana.agent.conf;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
  * Config filenames are relative to the JBoss config directory.
 * @author atsvetkov
 *
 */
public class ConfigService {    
    
    public final static String TRANSPORT_AGENT_FOLDER = "dbmi/transportAgent/";
    
    public static InputStream loadConfigFile(String file) throws IOException {
        return getConfigFileUrl(file).openStream();
    }

    public static URL getConfigFileUrl(String file) throws IOException {
        String url = System.getProperty("jboss.server.config.url") + TRANSPORT_AGENT_FOLDER + file;
        return new URL(url);
    }
    
    public static URL getResourceURL(String file) {
    	return ConfigService.class.getResource(file);
    }
}
