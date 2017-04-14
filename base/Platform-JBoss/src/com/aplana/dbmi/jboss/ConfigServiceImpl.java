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
package com.aplana.dbmi.jboss;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.filefilter.WildcardFileFilter;

import com.aplana.dbmi.ConfigService;

/**
 * JBoss AS specific implementation of {@link ConfigService} interface.
 * Config filenames are relative to the JBoss config directory.
 */
public class ConfigServiceImpl implements ConfigService {
    public InputStream loadConfigFile(String file) throws IOException {
        return getConfigFileUrl(file).openStream();
    }

    public URL getConfigFileUrl(String file) throws IOException {
        String url = System.getProperty("jboss.server.config.url") + file;
        return new URL(url);
    }

	public List<InputStream> loadMultipleConfigFiles(String folder, String filePattern)
			throws IOException {
		File dir = new File(new URL(System.getProperty("jboss.server.config.url")).getPath()+folder);
		FileFilter fileFilter = new WildcardFileFilter(filePattern);
		File[] files = dir.listFiles(fileFilter); dir.isDirectory();
		List<InputStream> result = new LinkedList<InputStream>();
		for (File file:files) {
			   result.add(file.toURI().toURL().openStream());
			}
		return result;
	}
}
