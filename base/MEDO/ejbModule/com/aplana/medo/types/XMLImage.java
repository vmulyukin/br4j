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
package com.aplana.medo.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.io.FilenameUtils;

import com.aplana.dmsi.types.DMSIObject;

@XmlAccessorType(XmlAccessType.PROPERTY)
@XmlType(name = "", propOrder = { "image", "format", "filename" })
public class XMLImage extends DMSIObject {

    protected byte[] image;
    protected String format;
    protected String filename;
    @SuppressWarnings("unused")
    private String materialName;

    @XmlElement(name = "Image", required = true)
    public byte[] getImage() {
	return image;
    }

    public void setImage(byte[] value) {
	this.image = value;
    }

    @XmlElement(name = "Format", required = true)
    public String getFormat() {
	return format;
    }

    public void setFormat(String value) {
	this.format = value;
	String newFileName = this.filename;
	if (filename == null || "".equals(filename)) {
	    newFileName = "defaultName";
	}
	setFilename(newFileName);

    }

    @XmlElement(name = "Filename", required = true)
    public String getFilename() {
	return filename;
    }

    public void setFilename(String value) {
	this.filename = createFileName(value);
	setMaterialName(this.filename);
    }

    @XmlTransient
    private void setMaterialName(String value) {
	this.materialName = value;
    }

    private String createFileName(String name) {
	if ("".equals(FilenameUtils.getExtension(this.filename))
		&& this.format != null && !"".equals(this.format)) {
	    return name + "." + format;
	}
	return name;
    }
}