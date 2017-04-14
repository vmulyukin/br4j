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
package com.aplana.scanner.ui;

import java.io.File;

import javax.swing.filechooser.FileFilter;

import org.apache.commons.io.FilenameUtils;

/**
 * <code>FileFilter</code> that filters files by their extensions.
 *
 * @author <a href="mailto:ogalkin@aplana.com">Oleg Galkin</a>
 */
public class ExtensionFileFilter extends FileFilter {
	private String[] fileExts;
	private String description;
	private boolean useExtensionsInDescription = true;
	private String fullDescription;
	
	/**
	 * Constructs a <code>FileFilter</code> by its description and accessible file extensions.
	 *
	 * @param description  the filter description. Depending on {@link #useExtensionsInDescription}
	 *                     value the full description may include extensions in brackets
	 *                     (i.e. JPEG File (*.jpg)).
	 * @param fileExts     an array of file extensions the filter is applied to
	 * 
	 * @see #setExtensionsInDescription(boolean)
	 */
	public ExtensionFileFilter(String description, String... fileExts) {
		this.description = description;
		this.fileExts = fileExts;
	}

	/* (non-Javadoc)
	 * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
	 */
	@Override
	public boolean accept(File f) {
		if (f.isDirectory()) return true;
		
		String extension = FilenameUtils.getExtension(f.getName());
		for (String fileExt : fileExts) {
			if (fileExt.equalsIgnoreCase(extension))
				return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.swing.filechooser.FileFilter#getDescription()
	 */
	@Override
	public String getDescription() {
		if (fullDescription == null) {
			if (useExtensionsInDescription) {
				StringBuilder sb = new StringBuilder(description);
				sb.append(" (");
				for (int i = 0; i < fileExts.length; i++) {
					if (i == 0)
						sb.append("*.").append(fileExts[i]);
					else
						sb.append("; *.").append(fileExts[i]);
				}
				sb.append(')');
				fullDescription = sb.toString();
			} else
				fullDescription = description;
		}
		return fullDescription;
	}

	/**
	 * Gets whether extensions are added to the filter description. Defaulted to <code>true</code>.
	 *
	 * @see #setExtensionsInDescription(boolean)
	 */
	public boolean isExtensionsInDescription() {
		return useExtensionsInDescription;
	}

	/**
	 * Sets whether to use extensions in description of this filter.
	 * <p/>
	 * If extensions in description are used then the filter description will include extensions in
	 * brackets (i.e. JPEG File (*.jpg)).
	 *
	 * @param b  <code>true</code> if extensions should be added to the filter description,
	 *           <code>false</code> otherwise
	 */
	public void setExtensionsInDescription(boolean b) {
		useExtensionsInDescription = b;
		fullDescription = null;
	}

	/**
	 * Gets file extensions for the filter.
	 */
	public String[] getFileExtensions() {
		return fileExts;
	}
}
