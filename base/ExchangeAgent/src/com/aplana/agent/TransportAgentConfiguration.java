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
package com.aplana.agent;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.ini4j.InvalidFileFormatException;
import org.ini4j.Options;

import com.aplana.agent.conf.ConfigService;

/**
 * Reads configuration from properties files specific to particular {@link Plugin} implementation.
 * @author atsvetkov
 *
 */
public class TransportAgentConfiguration {

    protected final static Logger logger = Logger.getLogger(TransportAgentConfiguration.class);

    /**
     * Reads the configuration from Properties file. If any error occurs method throws {@link RuntimeException}.
     * @return {@link Properties}
     */
    public static Properties getConfiguration(String resourceFile) {
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(ConfigService.loadConfigFile(resourceFile));
            Options options = new Options(inputStreamReader);
            Properties properties = new Properties();
            properties.putAll(options);
            return properties;
        } catch (InvalidFileFormatException e) {
            throw new IllegalArgumentException(e.getMessage(), e.getCause());
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException(e.getMessage(), e.getCause());
        } catch (IOException e) {            
            throw new IllegalArgumentException(e.getMessage(), e.getCause());
        }
   
    }

}
